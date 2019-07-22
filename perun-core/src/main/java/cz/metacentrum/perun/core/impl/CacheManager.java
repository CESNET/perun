package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributeHolders;
import cz.metacentrum.perun.core.api.AttributeIdWithHolders;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.Holder;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.implApi.AttributesManagerImplApi;
import cz.metacentrum.perun.core.implApi.CacheManagerApi;
import org.infinispan.Cache;
import org.infinispan.CacheSet;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.context.Flag;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.query.Search;
import org.infinispan.query.dsl.QueryFactory;
import org.springframework.jdbc.core.JdbcPerunTemplate;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Class used for caching layer management. It deals also with nested transactions and it contains all search and update methods for the cache.
 *
 * @author Simona Kruppova
 */
public class CacheManager implements CacheManagerApi {

	private final EmbeddedCacheManager localCacheManager;
	private JdbcPerunTemplate jdbc;

	private static boolean cacheDisabled = true;

	private final AtomicInteger counter = new AtomicInteger(0);
	private final Object nestedCacheNamesKey;

	private static final String CACHE_NAME = "transactionalCache";
	private static final String SIMPLE_CACHE_NAME = "simpleCache";
	private static final String FOR_REMOVE = "ForRemove";

	private static final String PRIMARY_HOLDER = "primaryHolder";
	private static final String SECONDARY_HOLDER = "secondaryHolder";
	private static final String HOLDER_ID = "id";
	private static final String HOLDER_TYPE = "type";
	private static final String SUBJECT = "subject";
	private static final String ID = "idForSearch";
	private static final String NAME = "nameForSearch";
	private static final String NAMESPACE = "namespaceForSearch";
	private static final String FRIENDLY_NAME = "friendlyNameForSearch";
	private static final String TYPE = "typeForSearch";
	private static final String VALUE = "valueForSearch";
	private static final String SAVED_BY = "savedBy";

	private static final String VALUE_PLACEHOLDER = "valuePlaceholder";

	private enum AccessType {
		READ_NOT_UPDATED_CACHE, SET, REMOVE
	}

	public CacheManager(EmbeddedCacheManager localCacheManager) {
		this.localCacheManager = localCacheManager;
		this.nestedCacheNamesKey = new Object();
	}

	public void setPerunPool(DataSource perunPool) {
		this.jdbc = new JdbcPerunTemplate(perunPool);
		this.jdbc.setQueryTimeout(BeansUtils.getCoreConfig().getQueryTimeout());
	}

	public void stopCacheManager() {
		localCacheManager.stop();
	}

	public static boolean isCacheDisabled() {
		return cacheDisabled;
	}

	public static void setCacheDisabled(boolean cacheDisabled) {
		CacheManager.cacheDisabled = cacheDisabled;
	}

	public Object getNestedCacheNamesKey() {
		return nestedCacheNamesKey;
	}

	/**
	 * Clears cache.
	 */
	public void clearCache() {
		getCache(CACHE_NAME).clear();
	}

	@Override
	public boolean wasCacheUpdatedInTransaction() {
		Boolean updated = getWasCacheUpdatedFromTransaction();
		if(updated != null) return updated;
		return false;
	}

	/**
	 * Sets cache as updated in transaction. If there si no transaction, nothing happens.
	 */
	private void setCacheUpdatedInTransaction() {
		Boolean updated =  getWasCacheUpdatedFromTransaction();
		if(updated != null) {
			TransactionSynchronizationManager.unbindResource(this);
			TransactionSynchronizationManager.bindResource(this, Boolean.TRUE);
		}
	}

	/**
	 * Returns true if in nested transaction.
	 * @return true if in nested transaction, false if not
	 */
	private boolean isInNestedTransaction() {
		List<String> cacheNames = getNestedCacheNamesFromTransaction();
		return cacheNames != null && cacheNames.size() > 1;
	}

	/**
	 * Gets cache by name.
	 * @param cacheName cache name
	 * @return cache with ignore return values flag set
	 */
	private Cache<Object, Object> getCache(String cacheName) {
		return localCacheManager.getCache(cacheName).getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES);
	}

	/**
	 * Gets cache.
	 * In nested transaction, it returns cache by accessType parameter.
	 * 1. If it's READ_NOT_UPDATED_CACHE, it returns normal cache, not the nested one.
	 * 2. If it's SET, it returns nested cache for set.
	 * 3. If it's REMOVE, it returns nested cache for remove.
	 *
	 * @param accessType READ_NOT_UPDATED_CACHE, SET or REMOVE
	 * @return cache with ignore return values flag set
	 */
	private Cache<Object, Object> getCache(AccessType accessType) {

		if(accessType == AccessType.SET || accessType == AccessType.REMOVE) {

			//if in nested transaction, return nested transaction cache
			if(isInNestedTransaction()) {
				List<String> cacheNames = getNestedCacheNamesFromTransaction();
				String name = cacheNames.get(cacheNames.size() - 1);

				if(accessType == AccessType.SET) return localCacheManager.getCache(name).getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES);
				else return localCacheManager.getCache(name + FOR_REMOVE).getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES);
			}
		}

		return localCacheManager.getCache(CACHE_NAME).getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES);
	}

	/**
	 * Gets list of nested cache names for read cache operations. The most nested transaction cache is the first in list.
	 * @return list of cache names
	 */
	private List<String> getNestedCacheNamesForRead() {
		List<String> cacheNames = getNestedCacheNamesFromTransaction();
		List<String> cacheNamesToReturn = new ArrayList<>();

		if(cacheNames != null) {
			cacheNamesToReturn.addAll(cacheNames);
			//remove the normal cache name, we want only those that are nested
			cacheNamesToReturn.remove(0);
			Collections.reverse(cacheNamesToReturn);
		} else {
			cacheNamesToReturn.add(CACHE_NAME);
		}

		return cacheNamesToReturn;
	}

	/**
	 * Gets list of nested cache names from transaction.
	 * @return list of cache names, null if there is no transaction and/or resource does not exist
	 */
	private List<String> getNestedCacheNamesFromTransaction() {
		return (List<String>) TransactionSynchronizationManager.getResource(this.getNestedCacheNamesKey());
	}

	/**
	 * Gets information if cache was updated in transaction.
	 * @return true if cache was updated in transaction, false if it was not, null if there is no transaction and/or resource does not exist
	 */
	private Boolean getWasCacheUpdatedFromTransaction() {
		return (Boolean) TransactionSynchronizationManager.getResource(this);
	}

	/**
	 * Gets transaction manager from cache.
	 * @return cache transaction manager
	 */
	public TransactionManager getCacheTransactionManager() {
		return this.getCache(AccessType.READ_NOT_UPDATED_CACHE).getAdvancedCache().getTransactionManager();
	}

	/**
	 * Get the non empty attributes namespaces by primary and secondary holder.
	 *
	 * @param primaryHolderType primary holder type
	 * @param secondaryHolderType secondary holder type
	 * @return list of namespaces
	 * @throws InternalErrorException
	 */
	private List<String> getNonEmptyAttributesNamespaces(Holder.HolderType primaryHolderType, Holder.HolderType secondaryHolderType) throws InternalErrorException {

		List<String> nonEmptyAttrsNamespaces = new ArrayList<>();

		if(primaryHolderType == Holder.HolderType.MEMBER && secondaryHolderType == Holder.HolderType.RESOURCE) {
			nonEmptyAttrsNamespaces.add(AttributesManager.NS_MEMBER_RESOURCE_ATTR_DEF);
			nonEmptyAttrsNamespaces.add(AttributesManager.NS_MEMBER_RESOURCE_ATTR_OPT);
		} else if(primaryHolderType == Holder.HolderType.MEMBER && secondaryHolderType == Holder.HolderType.GROUP) {
			nonEmptyAttrsNamespaces.add(AttributesManager.NS_MEMBER_GROUP_ATTR_DEF);
			nonEmptyAttrsNamespaces.add(AttributesManager.NS_MEMBER_GROUP_ATTR_OPT);
		} else if(primaryHolderType == Holder.HolderType.USER && secondaryHolderType == Holder.HolderType.FACILITY) {
			nonEmptyAttrsNamespaces.add(AttributesManager.NS_USER_FACILITY_ATTR_DEF);
			nonEmptyAttrsNamespaces.add(AttributesManager.NS_USER_FACILITY_ATTR_OPT);
		} else if(primaryHolderType == Holder.HolderType.GROUP && secondaryHolderType == Holder.HolderType.RESOURCE) {
			nonEmptyAttrsNamespaces.add(AttributesManager.NS_GROUP_RESOURCE_ATTR_DEF);
			nonEmptyAttrsNamespaces.add(AttributesManager.NS_GROUP_RESOURCE_ATTR_OPT);
		} else if(primaryHolderType == Holder.HolderType.FACILITY && secondaryHolderType == null) {
			nonEmptyAttrsNamespaces.add(AttributesManager.NS_FACILITY_ATTR_DEF);
			nonEmptyAttrsNamespaces.add(AttributesManager.NS_FACILITY_ATTR_OPT);
		} else if(primaryHolderType == Holder.HolderType.MEMBER && secondaryHolderType == null) {
			nonEmptyAttrsNamespaces.add(AttributesManager.NS_MEMBER_ATTR_DEF);
			nonEmptyAttrsNamespaces.add(AttributesManager.NS_MEMBER_ATTR_OPT);
		} else if(primaryHolderType == Holder.HolderType.VO && secondaryHolderType == null) {
			nonEmptyAttrsNamespaces.add(AttributesManager.NS_VO_ATTR_DEF);
			nonEmptyAttrsNamespaces.add(AttributesManager.NS_VO_ATTR_OPT);
		} else if(primaryHolderType == Holder.HolderType.GROUP && secondaryHolderType == null) {
			nonEmptyAttrsNamespaces.add(AttributesManager.NS_GROUP_ATTR_DEF);
			nonEmptyAttrsNamespaces.add(AttributesManager.NS_GROUP_ATTR_OPT);
		} else if(primaryHolderType == Holder.HolderType.HOST && secondaryHolderType == null) {
			nonEmptyAttrsNamespaces.add(AttributesManager.NS_HOST_ATTR_DEF);
			nonEmptyAttrsNamespaces.add(AttributesManager.NS_HOST_ATTR_OPT);
		} else if(primaryHolderType == Holder.HolderType.RESOURCE && secondaryHolderType == null) {
			nonEmptyAttrsNamespaces.add(AttributesManager.NS_RESOURCE_ATTR_DEF);
			nonEmptyAttrsNamespaces.add(AttributesManager.NS_RESOURCE_ATTR_OPT);
		} else if(primaryHolderType == Holder.HolderType.USER && secondaryHolderType == null) {
			nonEmptyAttrsNamespaces.add(AttributesManager.NS_USER_ATTR_DEF);
			nonEmptyAttrsNamespaces.add(AttributesManager.NS_USER_ATTR_OPT);
		} else if(primaryHolderType == Holder.HolderType.UES && secondaryHolderType == null) {
			nonEmptyAttrsNamespaces.add(AttributesManager.NS_UES_ATTR_DEF);
			nonEmptyAttrsNamespaces.add(AttributesManager.NS_UES_ATTR_OPT);
		} else if(primaryHolderType == null && secondaryHolderType == null) {
			nonEmptyAttrsNamespaces.add(AttributesManager.NS_ENTITYLESS_ATTR_DEF);
			nonEmptyAttrsNamespaces.add(AttributesManager.NS_ENTITYLESS_ATTR_OPT);
		} else {
			throw new InternalErrorException("Holder type combination " + primaryHolderType + "," + secondaryHolderType + " is not defined.");
		}
		return nonEmptyAttrsNamespaces;
	}

	/**
	 * Get core attribute namespaces by primary holder.
	 *
	 * @param primaryHolderType primary holder type
	 * @return list of namespaces
	 * @throws InternalErrorException
	 */
	private List<String> getCoreAttributesNamespace(Holder.HolderType primaryHolderType) throws InternalErrorException {

		List<String> attrsNamespaces = new ArrayList<>();

		if(primaryHolderType == Holder.HolderType.FACILITY) {
			attrsNamespaces.add(AttributesManager.NS_FACILITY_ATTR_CORE);
		} else if(primaryHolderType == Holder.HolderType.MEMBER) {
			attrsNamespaces.add(AttributesManager.NS_MEMBER_ATTR_CORE);
		} else if(primaryHolderType == Holder.HolderType.VO) {
			attrsNamespaces.add(AttributesManager.NS_VO_ATTR_CORE);
		} else if(primaryHolderType == Holder.HolderType.GROUP) {
			attrsNamespaces.add(AttributesManager.NS_GROUP_ATTR_CORE);
		} else if(primaryHolderType == Holder.HolderType.HOST) {
			attrsNamespaces.add(AttributesManager.NS_HOST_ATTR_CORE);
		} else if(primaryHolderType == Holder.HolderType.RESOURCE) {
			attrsNamespaces.add(AttributesManager.NS_RESOURCE_ATTR_CORE);
		} else if(primaryHolderType == Holder.HolderType.USER) {
			attrsNamespaces.add(AttributesManager.NS_USER_ATTR_CORE);
		} else if(primaryHolderType == Holder.HolderType.UES) {
			attrsNamespaces.add(AttributesManager.NS_UES_ATTR_CORE);
		} else {
			throw new InternalErrorException("Holder type " + primaryHolderType + " is not defined.");
		}
		return attrsNamespaces;
	}

	/**
	 * Get virtual attribute namespace by primary and secondary holder.
	 *
	 * @param primaryHolderType primary holder type
	 * @param secondaryHolderType secondary holder type
	 * @return namespace
	 * @throws InternalErrorException
	 */
	private String getVirtualAttributesNamespace(Holder.HolderType primaryHolderType, Holder.HolderType secondaryHolderType) throws InternalErrorException {

		String virtualAttrNamespace;

		if(primaryHolderType == Holder.HolderType.MEMBER && secondaryHolderType == Holder.HolderType.RESOURCE) {
			virtualAttrNamespace = AttributesManager.NS_MEMBER_RESOURCE_ATTR_VIRT;
		} else if(primaryHolderType == Holder.HolderType.USER && secondaryHolderType == Holder.HolderType.FACILITY) {
			virtualAttrNamespace = AttributesManager.NS_USER_FACILITY_ATTR_VIRT;
		} else if(primaryHolderType == Holder.HolderType.MEMBER && secondaryHolderType == Holder.HolderType.GROUP) {
			virtualAttrNamespace = AttributesManager.NS_MEMBER_GROUP_ATTR_VIRT;
		} else if(primaryHolderType == Holder.HolderType.FACILITY && secondaryHolderType == null) {
			virtualAttrNamespace = AttributesManager.NS_FACILITY_ATTR_VIRT;
		} else if(primaryHolderType == Holder.HolderType.MEMBER && secondaryHolderType == null) {
			virtualAttrNamespace = AttributesManager.NS_MEMBER_ATTR_VIRT;
		} else if(primaryHolderType == Holder.HolderType.VO && secondaryHolderType == null) {
			virtualAttrNamespace = AttributesManager.NS_VO_ATTR_VIRT;
		}  else if(primaryHolderType == Holder.HolderType.GROUP && secondaryHolderType == null) {
			virtualAttrNamespace = AttributesManager.NS_GROUP_ATTR_VIRT;
		} else if(primaryHolderType == Holder.HolderType.HOST && secondaryHolderType == null) {
			virtualAttrNamespace = AttributesManager.NS_HOST_ATTR_VIRT;
		} else if(primaryHolderType == Holder.HolderType.RESOURCE && secondaryHolderType == null) {
			virtualAttrNamespace = AttributesManager.NS_RESOURCE_ATTR_VIRT;
		} else if(primaryHolderType == Holder.HolderType.USER && secondaryHolderType == null) {
			virtualAttrNamespace = AttributesManager.NS_USER_ATTR_VIRT;
		} else if(primaryHolderType == Holder.HolderType.UES && secondaryHolderType == null) {
			virtualAttrNamespace = AttributesManager.NS_UES_ATTR_VIRT;
		} else {
			throw new InternalErrorException("Holder type combination " + primaryHolderType + "," + secondaryHolderType + " is not defined.");
		}

		return virtualAttrNamespace;
	}

	/**
	 * Get attributes by ids namespaces.
	 *
	 * @param primaryHolderType primary holder type
	 * @param secondaryHolderType secondary holder type
	 * @return list of namespaces
	 * @throws InternalErrorException
	 */
	private List<String> getAttributesByIdsNamespaces(Holder.HolderType primaryHolderType, Holder.HolderType secondaryHolderType) throws InternalErrorException {

		List<String> attrsNamespaces = new ArrayList<>();

		if(primaryHolderType == Holder.HolderType.MEMBER && secondaryHolderType == Holder.HolderType.GROUP) {
			attrsNamespaces.add(AttributesManager.NS_MEMBER_GROUP_ATTR_DEF);
			attrsNamespaces.add(AttributesManager.NS_MEMBER_GROUP_ATTR_OPT);
			attrsNamespaces.add(AttributesManager.NS_MEMBER_GROUP_ATTR_VIRT);
		} else if(primaryHolderType == Holder.HolderType.MEMBER && secondaryHolderType == Holder.HolderType.RESOURCE) {
			attrsNamespaces.add(AttributesManager.NS_MEMBER_RESOURCE_ATTR_DEF);
			attrsNamespaces.add(AttributesManager.NS_MEMBER_RESOURCE_ATTR_OPT);
			attrsNamespaces.add(AttributesManager.NS_MEMBER_RESOURCE_ATTR_VIRT);
		} else if(primaryHolderType == Holder.HolderType.GROUP && secondaryHolderType == Holder.HolderType.RESOURCE) {
			attrsNamespaces.add(AttributesManager.NS_GROUP_RESOURCE_ATTR_DEF);
			attrsNamespaces.add(AttributesManager.NS_GROUP_RESOURCE_ATTR_OPT);
			attrsNamespaces.add(AttributesManager.NS_GROUP_RESOURCE_ATTR_VIRT);
		} else if(primaryHolderType == Holder.HolderType.USER && secondaryHolderType == Holder.HolderType.FACILITY) {
			attrsNamespaces.add(AttributesManager.NS_USER_FACILITY_ATTR_DEF);
			attrsNamespaces.add(AttributesManager.NS_USER_FACILITY_ATTR_OPT);
			attrsNamespaces.add(AttributesManager.NS_USER_FACILITY_ATTR_VIRT);
		} else if(primaryHolderType == Holder.HolderType.VO && secondaryHolderType == null) {
			attrsNamespaces.add(AttributesManager.NS_VO_ATTR_CORE);
			attrsNamespaces.add(AttributesManager.NS_VO_ATTR_DEF);
			attrsNamespaces.add(AttributesManager.NS_VO_ATTR_OPT);
			attrsNamespaces.add(AttributesManager.NS_VO_ATTR_VIRT);
		} else if(primaryHolderType == Holder.HolderType.MEMBER && secondaryHolderType == null) {
			attrsNamespaces.add(AttributesManager.NS_MEMBER_ATTR_CORE);
			attrsNamespaces.add(AttributesManager.NS_MEMBER_ATTR_DEF);
			attrsNamespaces.add(AttributesManager.NS_MEMBER_ATTR_OPT);
			attrsNamespaces.add(AttributesManager.NS_MEMBER_ATTR_VIRT);
		} else if(primaryHolderType == Holder.HolderType.GROUP && secondaryHolderType == null) {
			attrsNamespaces.add(AttributesManager.NS_GROUP_ATTR_CORE);
			attrsNamespaces.add(AttributesManager.NS_GROUP_ATTR_DEF);
			attrsNamespaces.add(AttributesManager.NS_GROUP_ATTR_OPT);
		} else if(primaryHolderType == Holder.HolderType.USER && secondaryHolderType == null) {
			attrsNamespaces.add(AttributesManager.NS_USER_ATTR_CORE);
			attrsNamespaces.add(AttributesManager.NS_USER_ATTR_DEF);
			attrsNamespaces.add(AttributesManager.NS_USER_ATTR_OPT);
			attrsNamespaces.add(AttributesManager.NS_USER_ATTR_VIRT);
		} else if(primaryHolderType == Holder.HolderType.FACILITY && secondaryHolderType == null) {
			attrsNamespaces.add(AttributesManager.NS_FACILITY_ATTR_CORE);
			attrsNamespaces.add(AttributesManager.NS_FACILITY_ATTR_DEF);
			attrsNamespaces.add(AttributesManager.NS_FACILITY_ATTR_OPT);
			attrsNamespaces.add(AttributesManager.NS_FACILITY_ATTR_VIRT);
		} else if(primaryHolderType == Holder.HolderType.RESOURCE && secondaryHolderType == null) {
			attrsNamespaces.add(AttributesManager.NS_RESOURCE_ATTR_CORE);
			attrsNamespaces.add(AttributesManager.NS_RESOURCE_ATTR_DEF);
			attrsNamespaces.add(AttributesManager.NS_RESOURCE_ATTR_OPT);
			attrsNamespaces.add(AttributesManager.NS_RESOURCE_ATTR_VIRT);
		} else if(primaryHolderType == Holder.HolderType.HOST && secondaryHolderType == null) {
			attrsNamespaces.add(AttributesManager.NS_HOST_ATTR_CORE);
			attrsNamespaces.add(AttributesManager.NS_HOST_ATTR_DEF);
			attrsNamespaces.add(AttributesManager.NS_HOST_ATTR_OPT);
		} else if(primaryHolderType == Holder.HolderType.UES && secondaryHolderType == null) {
			attrsNamespaces.add(AttributesManager.NS_UES_ATTR_CORE);
			attrsNamespaces.add(AttributesManager.NS_UES_ATTR_DEF);
			attrsNamespaces.add(AttributesManager.NS_UES_ATTR_OPT);
			attrsNamespaces.add(AttributesManager.NS_UES_ATTR_VIRT);
		} else {
			throw new InternalErrorException("Holder type combination " + primaryHolderType + "," + secondaryHolderType + " is not defined.");
		}
		return attrsNamespaces;
	}

	@Override
	public List<Attribute> getAllNonEmptyAttributes(Holder holder) throws InternalErrorException {
		QueryFactory qf = Search.getQueryFactory(this.getCache(AccessType.READ_NOT_UPDATED_CACHE));

		org.infinispan.query.dsl.Query query =
				qf.from(AttributeHolders.class)
						.having(NAMESPACE).in(getCoreAttributesNamespace(holder.getType()))
						.and().having(SAVED_BY).eq(AttributeHolders.SavedBy.ID)
						.and(qf.having(PRIMARY_HOLDER + "." + HOLDER_ID).eq(holder.getId())
								.and().having(PRIMARY_HOLDER + "." + HOLDER_TYPE).eq(holder.getType())
								.and().having(SECONDARY_HOLDER).isNull()
								.or(qf.having(PRIMARY_HOLDER).isNull()
										.and().having(SECONDARY_HOLDER).isNull()
										.and().having(SUBJECT).isNull()))
						.or(qf.having(NAMESPACE).in(getNonEmptyAttributesNamespaces(holder.getType(), null))
							.and().having(SAVED_BY).eq(AttributeHolders.SavedBy.ID)
							.and().not().having(VALUE).isNull()
							.and(qf.having(PRIMARY_HOLDER + "." + HOLDER_ID).eq(holder.getId())
									.and().having(PRIMARY_HOLDER + "." + HOLDER_TYPE).eq(holder.getType())
									.and().having(SECONDARY_HOLDER).isNull()
									.or(qf.having(PRIMARY_HOLDER).isNull()
											.and().having(SECONDARY_HOLDER).isNull()
											.and().having(SUBJECT).isNull())))
						.toBuilder().build();

		return BeansUtils.getAttributesFromAttributeHolders(query.list());
	}

	@Override
	public List<Attribute> getAllNonEmptyAttributes(Holder primaryHolder, Holder secondaryHolder) throws InternalErrorException {
		QueryFactory qf = Search.getQueryFactory(this.getCache(AccessType.READ_NOT_UPDATED_CACHE));

		org.infinispan.query.dsl.Query query =
				qf.from(AttributeHolders.class)
						.having(PRIMARY_HOLDER + "." + HOLDER_ID).eq(primaryHolder.getId())
						.and().having(PRIMARY_HOLDER + "." + HOLDER_TYPE).eq(primaryHolder.getType())
						.and().having(SECONDARY_HOLDER + "." + HOLDER_ID).eq(secondaryHolder.getId())
						.and().having(SECONDARY_HOLDER + "." + HOLDER_TYPE).eq(secondaryHolder.getType())
						.and().having(NAMESPACE).in(getNonEmptyAttributesNamespaces(primaryHolder.getType(), secondaryHolder.getType()))
						.and().having(SAVED_BY).eq(AttributeHolders.SavedBy.ID)
						.toBuilder().build();

		return BeansUtils.getAttributesFromAttributeHolders(query.list());
	}

	@Override
	public List<Attribute> getAllAttributesByStartPartOfName(String startPartOfName, Holder holder) throws InternalErrorException {
		QueryFactory qf = Search.getQueryFactory(this.getCache(AccessType.READ_NOT_UPDATED_CACHE));

		List<String> namespaces = getNonEmptyAttributesNamespaces(holder.getType(), null);
		namespaces.addAll(getCoreAttributesNamespace(holder.getType()));

		org.infinispan.query.dsl.Query query =
				qf.from(AttributeHolders.class)
						.having(NAMESPACE).in(namespaces)
						.and().having(NAME).like(startPartOfName + "%")
						.and().having(SAVED_BY).eq(AttributeHolders.SavedBy.ID)
						.and(qf.having(PRIMARY_HOLDER + "." + HOLDER_ID).eq(holder.getId())
								.and().having(PRIMARY_HOLDER + "." + HOLDER_TYPE).eq(holder.getType())
								.and().having(SECONDARY_HOLDER).isNull()
								.or(qf.having(PRIMARY_HOLDER).isNull()
										.and().having(SECONDARY_HOLDER).isNull()
										.and().having(SUBJECT).isNull()))
						.toBuilder().build();

		return removeDuplicates(query.list());
	}

	/**
	 * Removes duplicate attributes.
	 * Duplicate means that the same attribute is there with and also without value. If that happens, it removes the attribute without value.
	 *
	 * @param attributeHoldersWithDuplicates list of attribute holders with duplicates
	 * @return list of attributes without duplicates
	 */
	private List<Attribute> removeDuplicates(List<AttributeHolders> attributeHoldersWithDuplicates) {
		List<Integer> definitionIds = new ArrayList<>();
		List<Integer> valuesIds = new ArrayList<>();
		List<AttributeHolders> attrHoldersToReturn = new ArrayList<>();

		//find all attributes with value
		for (AttributeHolders attrHolder: attributeHoldersWithDuplicates) {
			if(attrHolder.getValue() != null) {
				valuesIds.add(attrHolder.getId());
				attrHoldersToReturn.add(attrHolder);
			} else {
				definitionIds.add(attrHolder.getId());
			}
		}

		//remove definitions that already are in attrsToReturn list with value
		definitionIds.removeAll(valuesIds);

		//add definitions for which no attribute with value was found
		for(Integer defId: definitionIds) {
			for(AttributeHolders attrHolder: attributeHoldersWithDuplicates) {
				if(attrHolder.getId() == defId) attrHoldersToReturn.add(attrHolder);
			}
		}

		return BeansUtils.getAttributesFromAttributeHolders(attrHoldersToReturn);
	}

	@Override
	public List<Attribute> getUserFacilityAttributesForAnyUser(int facilityId) throws InternalErrorException {
		QueryFactory qf = Search.getQueryFactory(this.getCache(AccessType.READ_NOT_UPDATED_CACHE));

		org.infinispan.query.dsl.Query query =
				qf.from(AttributeHolders.class)
						.having(PRIMARY_HOLDER + "." + HOLDER_TYPE).eq(Holder.HolderType.USER)
						.and().having(SECONDARY_HOLDER + "." + HOLDER_ID).eq(facilityId)
						.and().having(SECONDARY_HOLDER + "." + HOLDER_TYPE).eq(Holder.HolderType.FACILITY)
						.and().having(NAMESPACE).in(getNonEmptyAttributesNamespaces(Holder.HolderType.USER, Holder.HolderType.FACILITY))
						.and().not().having(VALUE).isNull()
						.and().having(SAVED_BY).eq(AttributeHolders.SavedBy.ID)
						.toBuilder().build();

		return BeansUtils.getAttributesFromAttributeHolders(query.list());
	}

	@Override
	public List<Attribute> getAllUserFacilityAttributes(User user) throws InternalErrorException {
		QueryFactory qf = Search.getQueryFactory(this.getCache(AccessType.READ_NOT_UPDATED_CACHE));

		org.infinispan.query.dsl.Query query =
				qf.from(AttributeHolders.class)
						.having(PRIMARY_HOLDER + "." + HOLDER_TYPE).eq(Holder.HolderType.USER)
						.and().having(PRIMARY_HOLDER + "." + HOLDER_ID).eq(user.getId())
						.and().having(SECONDARY_HOLDER + "." + HOLDER_TYPE).eq(Holder.HolderType.FACILITY)
						.and().having(NAMESPACE).in(getNonEmptyAttributesNamespaces(Holder.HolderType.USER, Holder.HolderType.FACILITY))
						.and().not().having(VALUE).isNull()
						.and().having(SAVED_BY).eq(AttributeHolders.SavedBy.ID)
						.build();

		return BeansUtils.getAttributesFromAttributeHolders(query.list());
	}

	@Override
	public List<Attribute> getAttributesByNames(List<String> attrNames, Holder primaryHolder, Holder secondaryHolder) {
		List<Attribute> attributes = new ArrayList<>();

		for(String attrName: attrNames) {
			try {
				Attribute attr = this.getAttributeByName(attrName, primaryHolder, secondaryHolder);
				attributes.add(attr);
			} catch (AttributeNotExistsException e) {
				//if attribute does not exist, we do not add it to list of attributes to return
			}
		}

		return attributes;
	}

	public List<Attribute> getAttributesByAttributeDefinition(AttributeDefinition attributeDefinition) throws InternalErrorException {
		QueryFactory qf = Search.getQueryFactory(this.getCache(AccessType.READ_NOT_UPDATED_CACHE));

		String entity = attributeDefinition.getEntity();
		Holder.HolderType primaryHolderType = null;
		Holder.HolderType secondaryHolderType = null;

		switch (entity) {
			case "facility":
				primaryHolderType = Holder.HolderType.FACILITY;
				break;
			case "vo":
				primaryHolderType = Holder.HolderType.VO;
				break;
			case "group":
				primaryHolderType = Holder.HolderType.GROUP;
				break;
			case "host":
				primaryHolderType = Holder.HolderType.HOST;
				break;
			case "resource":
				primaryHolderType = Holder.HolderType.RESOURCE;
				break;
			case "member":
				primaryHolderType = Holder.HolderType.MEMBER;
				break;
			case "user":
				primaryHolderType = Holder.HolderType.USER;
				break;
			case "user_ext_source":
				primaryHolderType = Holder.HolderType.UES;
				break;
			case "member_resource":
				primaryHolderType = Holder.HolderType.MEMBER;
				secondaryHolderType = Holder.HolderType.RESOURCE;
				break;
			case "member_group":
				primaryHolderType = Holder.HolderType.MEMBER;
				secondaryHolderType = Holder.HolderType.GROUP;
				break;
			case "user_facility":
				primaryHolderType = Holder.HolderType.USER;
				secondaryHolderType = Holder.HolderType.FACILITY;
				break;
			case "group_resource":
				primaryHolderType = Holder.HolderType.GROUP;
				secondaryHolderType = Holder.HolderType.RESOURCE;
				break;
			case "entityless":
				break;
			default:
				throw new InternalErrorException("Entity does not exist: " + entity);
		}

		org.infinispan.query.dsl.Query query;

		if(primaryHolderType == null) {
			query = qf.from(AttributeHolders.class)
							.having(NAME).eq(attributeDefinition.getName())
							.and().not().having(VALUE).isNull()
							.and().having(SAVED_BY).eq(AttributeHolders.SavedBy.ID)
							.and().having(PRIMARY_HOLDER).isNull()
							.and().having(SECONDARY_HOLDER).isNull()
							.toBuilder().build();
		} else if(secondaryHolderType != null) {
			query = qf.from(AttributeHolders.class)
							.having(NAME).eq(attributeDefinition.getName())
							.and().not().having(VALUE).isNull()
							.and().having(SAVED_BY).eq(AttributeHolders.SavedBy.ID)
							.and().having(PRIMARY_HOLDER + "." + HOLDER_TYPE).eq(primaryHolderType)
							.and().having(SECONDARY_HOLDER + "." + HOLDER_TYPE).eq(secondaryHolderType)
							.toBuilder().build();
		} else {
			query = qf.from(AttributeHolders.class)
							.having(NAME).eq(attributeDefinition.getName())
							.and().not().having(VALUE).isNull()
							.and().having(SAVED_BY).eq(AttributeHolders.SavedBy.ID)
							.and().having(PRIMARY_HOLDER + "." + HOLDER_TYPE).eq(primaryHolderType)
							.and().having(SECONDARY_HOLDER).isNull()
							.toBuilder().build();
		}

		return BeansUtils.getAttributesFromAttributeHolders(query.list());
	}

	@Override
	public List<Attribute> getVirtualAttributes(Holder.HolderType primaryHolderType, Holder.HolderType secondaryHolderType) throws InternalErrorException {
		QueryFactory qf = Search.getQueryFactory(this.getCache(AccessType.READ_NOT_UPDATED_CACHE));

		org.infinispan.query.dsl.Query query =
				qf.from(AttributeHolders.class)
						.having(PRIMARY_HOLDER).isNull()
						.and().having(SECONDARY_HOLDER).isNull()
						.and().having(SUBJECT).isNull()
						.and().having(NAMESPACE).eq(getVirtualAttributesNamespace(primaryHolderType, secondaryHolderType))
						.and().having(SAVED_BY).eq(AttributeHolders.SavedBy.ID)
						.toBuilder().build();

		return BeansUtils.getAttributesFromAttributeHolders(query.list());
	}

	@Override
	public Attribute getAttributeByName(String attrName, Holder primaryHolder, Holder secondaryHolder) throws AttributeNotExistsException {
		return this.getAttribute(null, attrName, primaryHolder, secondaryHolder, null);
	}

	@Override
	public Attribute getAttributeById(int id, Holder primaryHolder, Holder secondaryHolder) throws AttributeNotExistsException {
		return this.getAttribute(id, null, primaryHolder, secondaryHolder, null);
	}

	/**
	 * Gets attribute by name or by id. It should have name or id not null.
	 * It should have primary holder, both holders or subject not null.
	 *
	 * @param attributeId attribute id
	 * @param attributeName attribute name
	 * @param primaryHolder primary holder
	 * @param secondaryHolder secondary holder
	 * @param subject subject
	 * @return attribute by name or id
	 * @throws AttributeNotExistsException
	 */
	private Attribute getAttribute(Integer attributeId, String attributeName, Holder primaryHolder, Holder secondaryHolder, String subject) throws AttributeNotExistsException {
		AttributeIdWithHolders attrId = new AttributeIdWithHolders(attributeId, attributeName, primaryHolder, secondaryHolder, subject);

		if(isInNestedTransaction()) {
			return getAttributeInNestedTransaction(attrId);
		} else {
			Cache<Object, Object> cache = this.getCache(AccessType.READ_NOT_UPDATED_CACHE);
			AttributeHolders attr = (AttributeHolders) cache.get(attrId);
			if(attr != null) {
				return new Attribute(attr, true);
			} else {
				//attribute with holders does not exist, try to find attribute definition by name or id
				if(attributeName != null) {
					return new Attribute(this.getAttributeDefinition(attributeName));
				} else {
					return new Attribute(this.getAttributeDefinition(attributeId));
				}
			}
		}
	}

	/**
	 * Gets attribute in nested transaction.
	 *
	 * @param attributeIdWithHolders attributeIdWithHolders object, that should have set name or id and holders or subject
	 * @return attribute by name or id
	 * @throws AttributeNotExistsException
	 */
	private Attribute getAttributeInNestedTransaction(AttributeIdWithHolders attributeIdWithHolders) throws AttributeNotExistsException {
		List<String> nestedCacheNames = this.getNestedCacheNamesForRead();

		for(String cacheNameForSet: nestedCacheNames) {

			String cacheNameForRemove = cacheNameForSet + FOR_REMOVE;
			Cache<Object, Object> cacheForRemove = this.getCache(cacheNameForRemove);

			//if attribute was removed in nested transaction, try to find definition by name or id
			if(cacheForRemove.get(attributeIdWithHolders) != null) {

				if(attributeIdWithHolders.getAttributeName() != null) {
					return new Attribute(this.getAttributeDefinition(attributeIdWithHolders.getAttributeName()));
				} else {
					return new Attribute(this.getAttributeDefinition(attributeIdWithHolders.getAttributeId()));
				}
			}

			Cache<Object, Object> cacheForSet = this.getCache(cacheNameForSet);
			//if attribute was set in nested transaction, return it
			AttributeHolders attr = (AttributeHolders) cacheForSet.get(attributeIdWithHolders);
			if(attr != null) {
				return new Attribute(attr, true);
			}
		}

		//attribute was not set, nor removed in nested transaction, look into normal cache
		Cache<Object, Object> cache = this.getCache(AccessType.READ_NOT_UPDATED_CACHE);
		AttributeHolders attr = (AttributeHolders) cache.get(attributeIdWithHolders);

		if(attr != null) {
			return new Attribute(attr, true);
		}

		if(attributeIdWithHolders.getAttributeName() != null) {
			return new Attribute(this.getAttributeDefinition(attributeIdWithHolders.getAttributeName()));
		} else {
			return new Attribute(this.getAttributeDefinition(attributeIdWithHolders.getAttributeId()));
		}
	}

	@Override
	public List<String> getAllSimilarAttributeNames(String startingPartOfAttributeName) {
		QueryFactory qf = Search.getQueryFactory(this.getCache(AccessType.READ_NOT_UPDATED_CACHE));

		org.infinispan.query.dsl.Query query =
				qf.from(AttributeHolders.class)
						.having(NAME).like(startingPartOfAttributeName + "%")
						.and().having(PRIMARY_HOLDER).isNull()
						.and().having(SECONDARY_HOLDER).isNull()
						.and().having(SUBJECT).isNull()
						.and().having(SAVED_BY).eq(AttributeHolders.SavedBy.ID)
						.toBuilder().build();

		List<String> attrNames = new ArrayList<>();
		List<AttributeHolders> attrHolders = query.list();

		for(AttributeHolders attr: attrHolders) {
			attrNames.add(attr.getName());
		}

		return attrNames;
	}

	@Override
	public List<AttributeDefinition> getAttributesDefinitions() {
		QueryFactory qf = Search.getQueryFactory(this.getCache(AccessType.READ_NOT_UPDATED_CACHE));

		org.infinispan.query.dsl.Query query =
				qf.from(AttributeHolders.class)
						.having(PRIMARY_HOLDER).isNull()
						.and().having(SECONDARY_HOLDER).isNull()
						.and().having(SUBJECT).isNull()
						.and().having(SAVED_BY).eq(AttributeHolders.SavedBy.ID)
						.toBuilder().build();

		return BeansUtils.getAttributeDefinitionsFromAttributeHolders(query.list());
	}

	@Override
	public List<AttributeDefinition> getAttributesDefinitionsByNamespace(String namespace) {
		QueryFactory qf = Search.getQueryFactory(this.getCache(AccessType.READ_NOT_UPDATED_CACHE));

		org.infinispan.query.dsl.Query query =
				qf.from(AttributeHolders.class)
						.having(PRIMARY_HOLDER).isNull()
						.and().having(SECONDARY_HOLDER).isNull()
						.and().having(SUBJECT).isNull()
						.and().having(NAMESPACE).eq(namespace)
						.and().having(SAVED_BY).eq(AttributeHolders.SavedBy.ID)
						.toBuilder().build();

		return BeansUtils.getAttributeDefinitionsFromAttributeHolders(query.list());
	}

	@Override
	public List<AttributeDefinition> getAttributesDefinitions(List<Integer> attrIds) {
		List<AttributeDefinition> attrDefs = new ArrayList<>();

		for(Integer id: attrIds) {
			try {
				AttributeDefinition attrDef = this.getAttributeDefinition(id);
				attrDefs.add(attrDef);
			} catch (AttributeNotExistsException e) {
				//if attribute definition does not exist, we do not add it to list of attribute definitions to return
			}
		}

		return attrDefs;
	}

	@Override
	public AttributeDefinition getAttributeDefinition(String attrName) throws AttributeNotExistsException {
		return this.getAttributeDefinition(null, attrName);
	}

	@Override
	public AttributeDefinition getAttributeDefinition(int id) throws AttributeNotExistsException {
		return this.getAttributeDefinition(id, null);
	}

	/**
	 * Gets attribute definition by name or by id. It should have name or id not null.
	 *
	 * @param attributeId attribute id
	 * @param attributeName attribute name
	 * @return attribute definition by name or id
	 * @throws AttributeNotExistsException
	 */
	private AttributeDefinition getAttributeDefinition(Integer attributeId, String attributeName) throws AttributeNotExistsException {
		AttributeIdWithHolders attributeIdWithHolders = new AttributeIdWithHolders(attributeId, attributeName, null, null, null);

		if(isInNestedTransaction()) {
			return this.getAttributeDefinitionInNestedTransaction(attributeIdWithHolders);
		} else {
			AttributeHolders attrHolders = (AttributeHolders) this.getCache(AccessType.READ_NOT_UPDATED_CACHE).get(attributeIdWithHolders);
			if(attrHolders == null) {
				if(attributeName != null) {
					throw new AttributeNotExistsException("Attribute - attribute.name='" + attributeName + "'");
				} else {
					throw new AttributeNotExistsException("Attribute - attribute.id='" + attributeId + "'");
				}
			}
			return new AttributeDefinition(attrHolders);
		}
	}

	/**
	 * Gets attribute definition in nested transaction.
	 *
	 * @param attributeIdWithHolders attributeIdWithHolders object, that should have set name or id
	 * @return attribute definition by name or id
	 * @throws AttributeNotExistsException
	 */
	private AttributeDefinition getAttributeDefinitionInNestedTransaction(AttributeIdWithHolders attributeIdWithHolders) throws AttributeNotExistsException {
		List<String> nestedCacheNames = this.getNestedCacheNamesForRead();

		for(String cacheNameForSet: nestedCacheNames) {

			//if attributeDefinition was set in nested transaction, return it
			Cache<Object, Object> cacheForSet = this.getCache(cacheNameForSet);
			AttributeHolders attr = (AttributeHolders) cacheForSet.get(attributeIdWithHolders);
			if(attr != null) {
				return new AttributeDefinition(attr);
			}
		}

		//attribute definition was not set in nested transaction, look into normal cache
		AttributeHolders attr = (AttributeHolders) this.getCache(AccessType.READ_NOT_UPDATED_CACHE).get(attributeIdWithHolders);

		if(attr == null) {
			if(attributeIdWithHolders.getAttributeName() != null) {
				throw new AttributeNotExistsException("Attribute - attribute.name='" + attributeIdWithHolders.getAttributeName() + "'");
			} else {
				throw new AttributeNotExistsException("Attribute - attribute.id='" + attributeIdWithHolders.getAttributeId() + "'");
			}
		}
		return new AttributeDefinition(attr);
	}

	@Override
	public List<Attribute> getAllNonEmptyEntitylessAttributes(String key) throws InternalErrorException {
		QueryFactory qf = Search.getQueryFactory(this.getCache(AccessType.READ_NOT_UPDATED_CACHE));

		org.infinispan.query.dsl.Query query =
				qf.from(AttributeHolders.class)
						.having(PRIMARY_HOLDER).isNull()
						.and().having(SECONDARY_HOLDER).isNull()
						.and().having(SUBJECT).eq(key)
						.and().having(NAMESPACE).in(getNonEmptyAttributesNamespaces(null, null))
						.and().not().having(VALUE).isNull()
						.and().having(SAVED_BY).eq(AttributeHolders.SavedBy.ID)
						.toBuilder().build();

		return BeansUtils.getAttributesFromAttributeHolders(query.list());
	}

	@Override
	public List<Attribute> getAllNonEmptyEntitylessAttributesByName(String attrName) throws InternalErrorException {
		QueryFactory qf = Search.getQueryFactory(this.getCache(AccessType.READ_NOT_UPDATED_CACHE));

		org.infinispan.query.dsl.Query query =
				qf.from(AttributeHolders.class)
						.having(PRIMARY_HOLDER).isNull()
						.and().having(SECONDARY_HOLDER).isNull()
						.and().having(NAME).eq(attrName)
						.and().having(NAMESPACE).in(getNonEmptyAttributesNamespaces(null, null))
						.and().not().having(VALUE).isNull()
						.and().having(SAVED_BY).eq(AttributeHolders.SavedBy.ID)
						.toBuilder().build();

		return BeansUtils.getAttributesFromAttributeHolders(query.list());
	}

	@Override
	public Attribute getEntitylessAttribute(String attrName, String key) throws AttributeNotExistsException {
		return this.getAttribute(null, attrName, null, null, key);
	}

	@Override
	public String getEntitylessAttrValue(int attrId, String key) throws InternalErrorException {
		AttributeIdWithHolders id = new AttributeIdWithHolders(attrId, key);

		if(isInNestedTransaction()) {
			return this.getEntitylessAttrValueInNestedTransaction(id);
		} else {
			AttributeHolders attr = (AttributeHolders) this.getCache(AccessType.READ_NOT_UPDATED_CACHE).get(id);
			if(attr != null) {
				return BeansUtils.attributeValueToString(attr);
			}
			return null;
		}
	}

	/**
	 * Gets entityless attribute value in nested transaction.
	 *
	 * @param attributeIdWithHolders attributeIdWithHolders object
	 * @return attribute value or null, if no attribute was found
	 * @throws InternalErrorException
	 */
	private String getEntitylessAttrValueInNestedTransaction(AttributeIdWithHolders attributeIdWithHolders) throws InternalErrorException {
		List<String> nestedCacheNames = this.getNestedCacheNamesForRead();

		for(String cacheNameForSet: nestedCacheNames) {

			String cacheNameForRemove = cacheNameForSet + FOR_REMOVE;
			Cache<Object, Object> cacheForRemove = this.getCache(cacheNameForRemove);

			//if attribute was removed in nested transaction, return null
			if(cacheForRemove.get(attributeIdWithHolders) != null) {
				return null;
			}

			Cache<Object, Object> cacheForSet = this.getCache(cacheNameForSet);
			//if attribute was set in nested transaction, return its value
			AttributeHolders attr = (AttributeHolders) cacheForSet.get(attributeIdWithHolders);
			if(attr != null) {
				return BeansUtils.attributeValueToString(attr);
			}
		}

		//attribute was not set, nor removed in nested transactions, look into normal cache
		Cache<Object, Object> cache = this.getCache(AccessType.READ_NOT_UPDATED_CACHE);
		AttributeHolders attr = (AttributeHolders) cache.get(attributeIdWithHolders);

		if(attr != null) {
			return BeansUtils.attributeValueToString(attr);
		}
		return null;
	}

	@Override
	public List<String> getEntitylessAttrKeys(String attrName) {
		QueryFactory qf = Search.getQueryFactory(this.getCache(AccessType.READ_NOT_UPDATED_CACHE));

		org.infinispan.query.dsl.Query query =
				qf.from(AttributeHolders.class)
						.having(PRIMARY_HOLDER).isNull()
						.and().having(SECONDARY_HOLDER).isNull()
						.and().not().having(SUBJECT).isNull()
						.and().having(NAME).like(attrName)
						.and().having(SAVED_BY).eq(AttributeHolders.SavedBy.ID)
						.toBuilder().build();

		List<String> attrKeys = new ArrayList<>();
		List<AttributeHolders> attrHolders = query.list();

		for(AttributeHolders attr: attrHolders) {
			attrKeys.add(attr.getSubject());
		}

		return attrKeys;
	}

	@Override
	public List<Object> getAllValues(Holder.HolderType holderType, AttributeDefinition attributeDefinition) {
		QueryFactory qf = Search.getQueryFactory(this.getCache(AccessType.READ_NOT_UPDATED_CACHE));

		org.infinispan.query.dsl.Query query =
				qf.from(AttributeHolders.class)
						.having(ID).eq(attributeDefinition.getId())
						.and().having(PRIMARY_HOLDER + "." + HOLDER_TYPE).eq(holderType)
						.and().having(SECONDARY_HOLDER).isNull()
						.and().not().having(VALUE).isNull()
						.and().having(SAVED_BY).eq(AttributeHolders.SavedBy.ID)
						.toBuilder().build();

		List<AttributeHolders> attrs = query.list();
		List<Object> values = new ArrayList<>();

		for(AttributeHolders a: attrs) {
			values.add(a.getValue());
		}

		return values;
	}

	@Override
	public List<Object> getAllValues(Holder.HolderType primaryHolderType, Holder.HolderType secondaryHolderType, AttributeDefinition attributeDefinition) {
		QueryFactory qf = Search.getQueryFactory(this.getCache(AccessType.READ_NOT_UPDATED_CACHE));

		org.infinispan.query.dsl.Query query =
				qf.from(AttributeHolders.class)
						.having(ID).eq(attributeDefinition.getId())
						.and().having(PRIMARY_HOLDER + "." + HOLDER_TYPE).eq(primaryHolderType)
						.and().having(SECONDARY_HOLDER + "." + HOLDER_TYPE).eq(secondaryHolderType)
						.and().having(SAVED_BY).eq(AttributeHolders.SavedBy.ID)
						.toBuilder().build();

		List<AttributeHolders> attrs = query.list();
		List<Object> values = new ArrayList<>();

		for(AttributeHolders a: attrs) {
			values.add(a.getValue());
		}

		return values;
	}

	@Override
	public List<Attribute> getAttributesByIds(List<Integer> attrIds, Holder primaryHolder) throws InternalErrorException {
		if(attrIds.isEmpty()) return new ArrayList<>();

		QueryFactory qf = Search.getQueryFactory(this.getCache(AccessType.READ_NOT_UPDATED_CACHE));

		org.infinispan.query.dsl.Query query =
				qf.from(AttributeHolders.class)
						.having(NAMESPACE).in(getAttributesByIdsNamespaces(primaryHolder.getType(), null))
						.and().having(ID).in(attrIds)
						.and().having(SAVED_BY).eq(AttributeHolders.SavedBy.ID)
						.and(qf.having(PRIMARY_HOLDER + "." + HOLDER_ID).eq(primaryHolder.getId())
								.and().having(PRIMARY_HOLDER + "." + HOLDER_TYPE).eq(primaryHolder.getType())
								.and().having(SECONDARY_HOLDER).isNull()
								.or(qf.having(PRIMARY_HOLDER).isNull()
										.and().having(SECONDARY_HOLDER).isNull()
										.and().having(SUBJECT).isNull()))
						.toBuilder().build();

		return removeDuplicates(query.list());
	}

	@Override
	public List<Attribute> getAttributesByIds(List<Integer> attrIds, Holder primaryHolder, Holder secondaryHolder) throws InternalErrorException {
		if(attrIds.isEmpty()) return new ArrayList<>();

		QueryFactory qf = Search.getQueryFactory(this.getCache(AccessType.READ_NOT_UPDATED_CACHE));

		org.infinispan.query.dsl.Query query =
				qf.from(AttributeHolders.class)
						.having(NAMESPACE).in(getAttributesByIdsNamespaces(primaryHolder.getType(), secondaryHolder.getType()))
						.and().having(ID).in(attrIds)
						.and().having(SAVED_BY).eq(AttributeHolders.SavedBy.ID)
						.and(qf.having(PRIMARY_HOLDER + "." + HOLDER_ID).eq(primaryHolder.getId())
								.and().having(PRIMARY_HOLDER + "." + HOLDER_TYPE).eq(primaryHolder.getType())
								.and().having(SECONDARY_HOLDER + "." + HOLDER_ID).eq(secondaryHolder.getId())
								.and().having(SECONDARY_HOLDER + "." + HOLDER_TYPE).eq(secondaryHolder.getType())
								.or(qf.having(PRIMARY_HOLDER).isNull()
										.and().having(SECONDARY_HOLDER).isNull()
										.and().having(SUBJECT).isNull()))
						.toBuilder().build();

		return removeDuplicates(query.list());
	}

	@Override
	public boolean checkAttributeExists(AttributeDefinition attribute) {

		/*
		 * We do not query cache, since it would have to find definition by multiple conditions within whole content.
		 * We re-use getAttributeDefinition() by ID or Name, which we expect to be present in attribute definition object.
		 * Then we can check other params on equals, just like select to DB does.
		 */
		AttributeDefinition found;
			try {
				found = getAttributeDefinition(attribute.getId());
				if (!Objects.equals(found.getId(),attribute.getId())) return false;
				if (!Objects.equals(found.getName(),attribute.getName())) return false;
				if (!Objects.equals(found.getFriendlyName(),attribute.getFriendlyName())) return false;
				if (!Objects.equals(found.getNamespace(),attribute.getNamespace())) return false;
				if (!Objects.equals(found.getType(),attribute.getType())) return false;
				return true;
			} catch (AttributeNotExistsException e) {

			}
		try {
			found = getAttributeDefinition(attribute.getName());
			if (!Objects.equals(found.getId(),attribute.getId())) return false;
			if (!Objects.equals(found.getName(),attribute.getName())) return false;
			if (!Objects.equals(found.getFriendlyName(),attribute.getFriendlyName())) return false;
			if (!Objects.equals(found.getNamespace(),attribute.getNamespace())) return false;
			if (!Objects.equals(found.getType(),attribute.getType())) return false;
			return true;
		} catch (AttributeNotExistsException e) {

		}
		return false;

		/*
		QueryFactory qf = Search.getQueryFactory(this.getCache(AccessType.READ_NOT_UPDATED_CACHE));

		org.infinispan.query.dsl.Query query =
				qf.from(AttributeHolders.class)
						.maxResults(1)
						.having(NAME).eq(attribute.getName())
						.and().having(FRIENDLY_NAME).eq(attribute.getFriendlyName())
						.and().having(NAMESPACE).eq(attribute.getNamespace())
						.and().having(ID).eq(attribute.getId())
						.and().having(TYPE).eq(attribute.getType())
						.and().having(PRIMARY_HOLDER).isNull()
						.and().having(SECONDARY_HOLDER).isNull()
						.and().having(SUBJECT).isNull()
						.and().having(SAVED_BY).eq(AttributeHolders.SavedBy.ID)
						.toBuilder().build();

		return 1 == query.getResultSize();
		*/

	}

	@Override
	public void setAttribute(Attribute attribute, Holder primaryHolder, Holder secondaryHolder) throws InternalErrorException {
		this.setCacheUpdatedInTransaction();
		this.setAttributeForInit(attribute, primaryHolder, secondaryHolder);
	}

	/**
	 * Store the attribute associated with primary holder and secondary holder. If secondary holder is null, it stores the attribute for the primary holder.
	 * This method is used in initialization, cache is not set as updated.
	 *
	 * @param attribute attribute to set
	 * @param primaryHolder primary holder
	 * @param secondaryHolder secondary holder
	 * @throws InternalErrorException
	 */
	private void setAttributeForInit(Attribute attribute, Holder primaryHolder, Holder secondaryHolder) throws InternalErrorException {
		AttributeIdWithHolders attrId = new AttributeIdWithHolders(attribute.getId(), primaryHolder, secondaryHolder);
		AttributeIdWithHolders attrId1 = new AttributeIdWithHolders(attribute.getName(), primaryHolder, secondaryHolder);
		AttributeHolders attributeHolders = new AttributeHolders(attribute, primaryHolder, secondaryHolder, AttributeHolders.SavedBy.ID);
		AttributeHolders attributeHolders1 = new AttributeHolders(attribute, primaryHolder, secondaryHolder, AttributeHolders.SavedBy.NAME);

		this.storeKeyValuePairs(attrId, attrId1, attributeHolders, attributeHolders1);
	}

	/**
	 * Stores two key-value pairs representing one attribute (stored by name and by id) into cache.
	 * If in nested transaction, it removes the stored keys mappings from ForRemove cache, if they are present.
	 *
	 * @param attrId key to store value under
	 * @param attrId1 key1 to store value1 under
	 * @param attributeHolders value
	 * @param attributeHolders1 value1
	 */
	private void storeKeyValuePairs(AttributeIdWithHolders attrId, AttributeIdWithHolders attrId1, AttributeHolders attributeHolders, AttributeHolders attributeHolders1) {
		Cache<Object, Object> cache = this.getCache(AccessType.SET);

		cache.put(attrId, attributeHolders);
		cache.put(attrId1, attributeHolders1);

		if(this.isInNestedTransaction()) {
			Cache<Object, Object> cacheForRemove = this.getCache(AccessType.REMOVE);
			//if we are setting previously removed attribute, we need to remove it from removed attributes cache
			cacheForRemove.remove(attrId);
			cacheForRemove.remove(attrId1);
		}
	}

	@Override
	public void setAttributeWithExistenceCheck(Attribute attribute, Holder primaryHolder, Holder secondaryHolder) throws InternalErrorException {
		this.setCacheUpdatedInTransaction();

		AttributeIdWithHolders attrId = new AttributeIdWithHolders(attribute.getId(), primaryHolder, secondaryHolder);
		AttributeIdWithHolders attrId1 = new AttributeIdWithHolders(attribute.getName(), primaryHolder, secondaryHolder);

		try {
			Attribute attr = getAttributeById(attribute.getId(), primaryHolder, secondaryHolder);
			attribute.setValueCreatedAt(attr.getValueCreatedAt());
			attribute.setValueCreatedBy(attr.getValueCreatedBy());
		} catch (AttributeNotExistsException e) {
			// If attribute does not exist, we do not need to save the old createdAt and createdBy values.
		}

		AttributeHolders attributeHolders = new AttributeHolders(attribute, primaryHolder, secondaryHolder, AttributeHolders.SavedBy.ID);
		AttributeHolders attributeHolders1 = new AttributeHolders(attribute, primaryHolder, secondaryHolder, AttributeHolders.SavedBy.NAME);

		this.storeKeyValuePairs(attrId, attrId1, attributeHolders, attributeHolders1);
	}

	@Override
	public void setAttributeDefinition(AttributeDefinition attribute) {
		this.setCacheUpdatedInTransaction();
		this.setAttributeDefinitionForInit(attribute);
	}

	/**
	 * Store the attribute definition.
	 * This method is used in initialization, cache is not set as updated.
	 *
	 * @param attribute attribute definition to set
	 */
	private void setAttributeDefinitionForInit(AttributeDefinition attribute) {
		Cache<Object, Object> cache = this.getCache(AccessType.SET);
		AttributeIdWithHolders attrId = new AttributeIdWithHolders(attribute.getId());
		AttributeIdWithHolders attrId1 = new AttributeIdWithHolders(attribute.getName());
		AttributeHolders attributeHolders = new AttributeHolders(attribute, AttributeHolders.SavedBy.ID);
		AttributeHolders attributeHolders1 = new AttributeHolders(attribute, AttributeHolders.SavedBy.NAME);

		cache.put(attrId, attributeHolders);
		cache.put(attrId1, attributeHolders1);
	}

	@Override
	public void setEntitylessAttribute(Attribute attribute, String key) throws InternalErrorException {
		this.setCacheUpdatedInTransaction();
		this.setEntitylessAttributeForInit(attribute, key);
	}

	/**
	 * Store the entityless attribute by key.
	 * This method is used in initialization, cache is not set as updated.
	 *
	 * @param attribute entityless attribute
	 * @param key subject of entityless attribute
	 * @throws InternalErrorException
	 */
	private void setEntitylessAttributeForInit(Attribute attribute, String key) throws InternalErrorException {
		AttributeIdWithHolders attrId = new AttributeIdWithHolders(attribute.getId(), key);
		AttributeIdWithHolders attrId1 = new AttributeIdWithHolders(attribute.getName(), key);
		AttributeHolders attributeHolders = new AttributeHolders(attribute, key, AttributeHolders.SavedBy.ID);
		AttributeHolders attributeHolders1 = new AttributeHolders(attribute, key, AttributeHolders.SavedBy.NAME);

		this.storeKeyValuePairs(attrId, attrId1, attributeHolders, attributeHolders1);
	}

	@Override
	public void setEntitylessAttributeWithExistenceCheck(Attribute attribute, String key) throws InternalErrorException {
		this.setCacheUpdatedInTransaction();

		AttributeIdWithHolders attrId = new AttributeIdWithHolders(attribute.getId(), key);
		AttributeIdWithHolders attrId1 = new AttributeIdWithHolders(attribute.getName(), key);

		try {
			Attribute attr = getEntitylessAttribute(attribute.getName(), key);
			attribute.setValueCreatedAt(attr.getValueCreatedAt());
			attribute.setValueCreatedBy(attr.getValueCreatedBy());
		} catch (AttributeNotExistsException e) {
			// If attribute does not exist, we do not need to save the old createdAt and createdBy values.
		}

		AttributeHolders attributeHolders = new AttributeHolders(attribute, key, AttributeHolders.SavedBy.ID);
		AttributeHolders attributeHolders1 = new AttributeHolders(attribute, key, AttributeHolders.SavedBy.NAME);

		this.storeKeyValuePairs(attrId, attrId1, attributeHolders, attributeHolders1);
	}

	/**
	 * Store attributes by primary holder or by primary and secondary holder.
	 * Used only in cache initialization.
	 *
	 * @param attributes list of attribute holders to set
	 * @throws InternalErrorException
	 */
	private void setAttributes(List<AttributeHolders> attributes) throws InternalErrorException {
		for(AttributeHolders attr: attributes) {
			this.setAttributeForInit(attr, attr.getPrimaryHolder(), attr.getSecondaryHolder());
		}
	}

	/**
	 * Store attributes by key.
	 * Used only in cache initialization.
	 *
	 * @param attributes list of attribute holders to set
	 * @throws InternalErrorException
	 */
	private void setEntitylessAttributes(List<AttributeHolders> attributes) throws InternalErrorException {
		for(AttributeHolders attr: attributes) {
			this.setEntitylessAttributeForInit(attr, attr.getSubject());
		}
	}

	/**
	 * Store attribute definitions.
	 * Used only in cache initialization.
	 *
	 * @param attributes list of attribute definitions to set
	 */
	private void setAttributesDefinitions(List<AttributeDefinition> attributes) {
		for(AttributeDefinition attrDef: attributes) {
			this.setAttributeDefinitionForInit(attrDef);
		}
	}

	@Override
	public void updateAttributeDefinition(AttributeDefinition attributeDefinition) {
		this.setCacheUpdatedInTransaction();

		Cache<Object, Object> cache = this.getCache(AccessType.SET);

		AttributeIdWithHolders id = new AttributeIdWithHolders(attributeDefinition.getId());
		AttributeIdWithHolders id1 = new AttributeIdWithHolders(attributeDefinition.getName());

		try {
			AttributeDefinition attrDef = this.getAttributeDefinition(attributeDefinition.getId());
			attributeDefinition.setCreatedBy(attrDef.getCreatedBy());
			attributeDefinition.setCreatedByUid(attrDef.getCreatedByUid());
			attributeDefinition.setCreatedAt(attrDef.getCreatedAt());
		} catch (AttributeNotExistsException e) {
			// If attribute does not exist, we do not need to save the old createdAt, createdBy and createdByUid values.
		}

		AttributeHolders attributeHolders = new AttributeHolders(attributeDefinition, AttributeHolders.SavedBy.ID);
		AttributeHolders attributeHolders1 = new AttributeHolders(attributeDefinition, AttributeHolders.SavedBy.NAME);

		cache.put(id, attributeHolders);
		cache.put(id1, attributeHolders1);
	}

	@Override
	public void deleteAttribute(int id, PerunSession session, AttributesManagerImplApi attributesManager) throws InternalErrorException {

		/* if cache was updated in transaction, we need to reinitialize it
		It is because there is a possibility that the query by id would not return all attributes that should be deleted and the cache would end up in inconsistent state */
		if(this.wasCacheUpdatedInTransaction()) {
			this.initialize(session, attributesManager);
		} else {
			Cache<Object, Object> cache = this.getCache(AccessType.READ_NOT_UPDATED_CACHE);
			QueryFactory qf = Search.getQueryFactory(cache);

			org.infinispan.query.dsl.Query query =
					qf.from(AttributeHolders.class)
							.having(ID).eq(id)
							.and().having(SAVED_BY).eq(AttributeHolders.SavedBy.ID)
							.toBuilder().build();

			List<AttributeHolders> attrsToDelete = query.list();

			for(AttributeHolders attr: attrsToDelete) {
				cache.remove(new AttributeIdWithHolders(attr.getId(), null, attr.getPrimaryHolder(), attr.getSecondaryHolder(), attr.getSubject()));
				cache.remove(new AttributeIdWithHolders(null, attr.getName(), attr.getPrimaryHolder(), attr.getSecondaryHolder(), attr.getSubject()));
			}
		}

		this.setCacheUpdatedInTransaction();
	}

	@Override
	public void removeAttribute(AttributeDefinition attribute, Holder primaryHolder, Holder secondaryHolder) {
		this.removeAttribute(attribute.getId(), attribute.getName(), primaryHolder, secondaryHolder, null);
	}

	@Override
	public void removeEntitylessAttribute(AttributeDefinition attribute, String key) {
		this.removeAttribute(attribute.getId(), attribute.getName(), null, null, key);
	}

	/**
	 * Removes attribute by holders or by subject. It should have primary holder, both holders or subject not null.
	 * If in nested transaction, it stores the removed keys to ForRemove cache, instead of deleting them.
	 *
	 * @param attributeId attribute id, not null
	 * @param attributeName attribute name, not null
	 * @param primaryHolder primary holder
	 * @param secondaryHolder secondary holder
	 * @param subject subject
	 */
	private void removeAttribute(Integer attributeId, String attributeName, Holder primaryHolder, Holder secondaryHolder, String subject) {
		this.setCacheUpdatedInTransaction();

		Cache<Object, Object> cache = this.getCache(AccessType.REMOVE);
		AttributeIdWithHolders attrId = new AttributeIdWithHolders(attributeId, null, primaryHolder, secondaryHolder, subject);
		AttributeIdWithHolders attrId1 = new AttributeIdWithHolders(null, attributeName, primaryHolder, secondaryHolder, subject);

		if(!this.isInNestedTransaction()) {
			cache.remove(attrId);
			cache.remove(attrId1);
		} else {
			cache.put(attrId, VALUE_PLACEHOLDER);
			cache.put(attrId1, VALUE_PLACEHOLDER);
		}
	}

	@Override
	public void removeAllAttributes(Holder holder) {
		//if in nested transaction, no attributes are removed
		if(isInNestedTransaction()) return;

		this.setCacheUpdatedInTransaction();

		Cache<Object, Object> cache = this.getCache(AccessType.READ_NOT_UPDATED_CACHE);
		QueryFactory qf = Search.getQueryFactory(cache);

		org.infinispan.query.dsl.Query query =
				qf.from(AttributeHolders.class)
						.having(PRIMARY_HOLDER + "." + HOLDER_ID).eq(holder.getId())
						.and().having(PRIMARY_HOLDER + "." + HOLDER_TYPE).eq(holder.getType())
						.and().having(SECONDARY_HOLDER).isNull()
						.and().having(SAVED_BY).eq(AttributeHolders.SavedBy.ID)
						.toBuilder().build();

		List<AttributeHolders> attrsToDelete = query.list();

		for(AttributeHolders attr: attrsToDelete) {
			cache.remove(new AttributeIdWithHolders(attr.getId(), attr.getPrimaryHolder(), null));
			cache.remove(new AttributeIdWithHolders(attr.getName(), attr.getPrimaryHolder(), null));
		}
	}

	@Override
	public void removeAllAttributes(Holder primaryHolder, Holder secondaryHolder) {
		//if in nested transaction, no attributes are removed
		if(isInNestedTransaction()) return;

		this.setCacheUpdatedInTransaction();

		Cache<Object, Object> cache = this.getCache(AccessType.READ_NOT_UPDATED_CACHE);
		QueryFactory qf = Search.getQueryFactory(cache);

		org.infinispan.query.dsl.Query query =
				qf.from(AttributeHolders.class)
						.having(PRIMARY_HOLDER + "." + HOLDER_ID).eq(primaryHolder.getId())
						.and().having(PRIMARY_HOLDER + "." + HOLDER_TYPE).eq(primaryHolder.getType())
						.and().having(SECONDARY_HOLDER + "." + HOLDER_ID).eq(secondaryHolder.getId())
						.and().having(SECONDARY_HOLDER + "." + HOLDER_TYPE).eq(secondaryHolder.getType())
						.and().having(SAVED_BY).eq(AttributeHolders.SavedBy.ID)
						.toBuilder().build();

		List<AttributeHolders> attrsToDelete = query.list();

		for(AttributeHolders attr: attrsToDelete) {
			cache.remove(new AttributeIdWithHolders(attr.getId(), attr.getPrimaryHolder(), attr.getSecondaryHolder()));
			cache.remove(new AttributeIdWithHolders(attr.getName(), attr.getPrimaryHolder(), attr.getSecondaryHolder()));
		}
	}

	@Override
	public void removeAllAttributes(Holder primaryHolder, Holder.HolderType secondaryHolderType) {
		//if in nested transaction, no attributes are removed
		if(isInNestedTransaction()) return;

		this.setCacheUpdatedInTransaction();

		Cache<Object, Object> cache = this.getCache(AccessType.READ_NOT_UPDATED_CACHE);
		QueryFactory qf = Search.getQueryFactory(cache);

		org.infinispan.query.dsl.Query query =
				qf.from(AttributeHolders.class)
						.having(PRIMARY_HOLDER + "." + HOLDER_ID).eq(primaryHolder.getId())
						.and().having(PRIMARY_HOLDER + "." + HOLDER_TYPE).eq(primaryHolder.getType())
						.and().having(SECONDARY_HOLDER + "." + HOLDER_TYPE).eq(secondaryHolderType)
						.and().having(SAVED_BY).eq(AttributeHolders.SavedBy.ID)
						.toBuilder().build();

		List<AttributeHolders> attrsToDelete = query.list();

		for(AttributeHolders attr: attrsToDelete) {
			cache.remove(new AttributeIdWithHolders(attr.getId(), attr.getPrimaryHolder(), attr.getSecondaryHolder()));
			cache.remove(new AttributeIdWithHolders(attr.getName(), attr.getPrimaryHolder(), attr.getSecondaryHolder()));
		}
	}

	@Override
	public void removeAllAttributes(Holder.HolderType primaryHolderType, Holder secondaryHolder) {
		//if in nested transaction, no attributes are removed
		if(isInNestedTransaction()) return;

		this.setCacheUpdatedInTransaction();

		Cache<Object, Object> cache = this.getCache(AccessType.READ_NOT_UPDATED_CACHE);
		QueryFactory qf = Search.getQueryFactory(cache);

		org.infinispan.query.dsl.Query query =
				qf.from(AttributeHolders.class)
						.having(PRIMARY_HOLDER + "." + HOLDER_TYPE).eq(primaryHolderType)
						.and().having(SECONDARY_HOLDER + "." + HOLDER_ID).eq(secondaryHolder.getId())
						.and().having(SECONDARY_HOLDER + "." + HOLDER_TYPE).eq(secondaryHolder.getType())
						.and().having(SAVED_BY).eq(AttributeHolders.SavedBy.ID)
						.toBuilder().build();

		List<AttributeHolders> attrsToDelete = query.list();

		for(AttributeHolders attr: attrsToDelete) {
			cache.remove(new AttributeIdWithHolders(attr.getId(), attr.getPrimaryHolder(), attr.getSecondaryHolder()));
			cache.remove(new AttributeIdWithHolders(attr.getName(), attr.getPrimaryHolder(), attr.getSecondaryHolder()));
		}
	}

	/**
	 * Commits cache changes.
	 */
	public void commit() {
		try {
			this.getCacheTransactionManager().commit();
		} catch (RollbackException e) {
			throw new UnexpectedRollbackException("Transaction has been unexpectedly rolled back instead of committed.", e);
		} catch (HeuristicMixedException | HeuristicRollbackException | SystemException e) {
			throw new TransactionSystemException("Unexpected system error occurred.", e);
		}
	}

	/**
	 * Rollbacks cache changes.
	 */
	public void rollback() {
		try {
			this.getCacheTransactionManager().rollback();
		} catch (SystemException e) {
			throw new TransactionSystemException("Unexpected system error occurred.", e);
		}
	}

	/**
	 * Begins new transaction for cache.
	 *
	 * It creates transaction resource that contains information, if there was any write operation performed on cache in this transaction.
	 */
	public void newTopLevelTransaction() {
		Boolean updated = getWasCacheUpdatedFromTransaction();

		if(updated == null) {
			TransactionSynchronizationManager.bindResource(this, Boolean.FALSE);
		} else {
			TransactionSynchronizationManager.unbindResource(this);
			TransactionSynchronizationManager.bindResource(this, Boolean.FALSE);
		}

		try {
			this.getCacheTransactionManager().begin();
		} catch (NotSupportedException e) {
			throw new CannotCreateTransactionException("The thread is already associated with a transaction.", e);
		} catch (SystemException e) {
			throw new TransactionSystemException("Unexpected system error occurred.", e);
		}
	}

	/**
	 * Creates new nested transaction for cache.
	 *
	 * It creates two new caches, one for attributes that were set in nested transaction, one for the ones that were removed.
	 * It also creates transaction resource with nested cache names. First in the list is normal cache name, then the first nested cache name, then the second..
	 */
	public void newNestedTransaction() {
		List<String> transactionCacheNames = getNestedCacheNamesFromTransaction();

		if(transactionCacheNames == null) {
			transactionCacheNames = new ArrayList<>();
			TransactionSynchronizationManager.bindResource(this.getNestedCacheNamesKey(), transactionCacheNames);
		}

		if(transactionCacheNames.isEmpty()) {
			transactionCacheNames.add(CACHE_NAME);
		}

		String cacheName = SIMPLE_CACHE_NAME + counter.getAndIncrement();

		Configuration configuration = localCacheManager.getCacheConfiguration(SIMPLE_CACHE_NAME);
		localCacheManager.defineConfiguration(cacheName, configuration);
		localCacheManager.defineConfiguration(cacheName + FOR_REMOVE, configuration);

		transactionCacheNames.add(cacheName);
	}

	/**
	 * Cleans nested transaction for cache.
	 *
	 * It updates list of nested cache names. It also stops the caches used in the cleaned nested transaction.
	 */
	public void cleanNestedTransaction() {
		List<String> transactionCacheNames = getNestedCacheNamesFromTransaction();
		int cacheToStopIndex = transactionCacheNames.size() - 1;
		String cacheName = transactionCacheNames.get(cacheToStopIndex);

		//clear and stop the caches used in nested transaction
		localCacheManager.removeCache(cacheName);
		localCacheManager.removeCache(cacheName + FOR_REMOVE);

		//remove the cache name from list of caches used in transaction
		transactionCacheNames.remove(cacheToStopIndex);
	}

	/**
	 * It flushes changes from nested transaction for cache.
	 * The changes are flushed to the second most nested cache or to the normal cache, if this was the last nested transaction.
	 * It cleans the nested transaction after the flush.
	 */
	public void flushNestedTransaction() {
		List<String> transactionCacheNames = getNestedCacheNamesFromTransaction();

		String cacheNameToFlush = transactionCacheNames.get(transactionCacheNames.size() - 1);
		String cacheName = transactionCacheNames.get(transactionCacheNames.size() - 2);

		Cache<Object, Object> cacheForSetToFlush = this.getCache(cacheNameToFlush);
		Cache<Object, Object> cacheForRemoveToFlush = this.getCache(cacheNameToFlush + FOR_REMOVE);
		Cache<Object, Object> cacheForSet = this.getCache(cacheName);

		for (Object o: cacheForSetToFlush.keySet()) {
			cacheForSet.put(o, cacheForSetToFlush.get(o));
		}

		//if there is more than one nested transaction, we need to put removed attributes to cache for remove
		if(transactionCacheNames.size() > 2) {
			Cache<Object, Object> cacheForRemove = this.getCache(cacheName + FOR_REMOVE);
			for (Object o: cacheForRemoveToFlush.keySet()) {
				cacheForRemove.put(o, VALUE_PLACEHOLDER);
			}
		} else {
			for (Object o: cacheForRemoveToFlush.keySet()) {
				cacheForSet.remove(o);
			}
		}

		cleanNestedTransaction();
	}

	/**
	 * Cleans top level transaction.
	 */
	public void clean() {
		List<String> transactionCacheNames = getNestedCacheNamesFromTransaction();
		//if there was nested transaction, unbind its resource
		if(transactionCacheNames != null) {
			transactionCacheNames.clear();
			TransactionSynchronizationManager.unbindResourceIfPossible(this.getNestedCacheNamesKey());
		}
		TransactionSynchronizationManager.unbindResourceIfPossible(this);
	}

	@Override
	public void initialize(PerunSession sess, AttributesManagerImplApi attributesManagerImpl) throws InternalErrorException  {
		Cache<Object, Object> cache = this.getCache(AccessType.SET);

		//clear the cache
		CacheSet<Object> keySet = cache.keySet();
		for(Object key: keySet) {
			cache.remove(key);
		}

		List<AttributeDefinition> attrDefs;
		List<AttributeHolders> attrs;

		//save attribute definitions
		attrDefs = jdbc.query("select " + AttributesManagerImpl.attributeDefinitionMappingSelectQuery + " from attr_names ", AttributesManagerImpl.ATTRIBUTE_DEFINITION_MAPPER);
		this.setAttributesDefinitions(attrDefs);

		//save attributes with values
		saveAttributesForInit(sess, attributesManagerImpl, "facility", Holder.HolderType.FACILITY);
		saveAttributesForInit(sess, attributesManagerImpl, "vo", Holder.HolderType.VO);
		saveAttributesForInit(sess, attributesManagerImpl, "group", Holder.HolderType.GROUP);
		saveAttributesForInit(sess, attributesManagerImpl, "host", Holder.HolderType.HOST);
		saveAttributesForInit(sess, attributesManagerImpl, "resource", Holder.HolderType.RESOURCE);
		saveAttributesForInit(sess, attributesManagerImpl, "member", Holder.HolderType.MEMBER);
		saveAttributesForInit(sess, attributesManagerImpl, "user", Holder.HolderType.USER);
		saveAttributesForInit(sess, attributesManagerImpl, "user_ext_source", Holder.HolderType.UES);
		saveAttributesForInit(sess, attributesManagerImpl, "member", "resource", Holder.HolderType.MEMBER, Holder.HolderType.RESOURCE);
		saveAttributesForInit(sess, attributesManagerImpl, "member", "group", Holder.HolderType.MEMBER, Holder.HolderType.GROUP);
		saveAttributesForInit(sess, attributesManagerImpl, "user", "facility", Holder.HolderType.USER, Holder.HolderType.FACILITY);
		saveAttributesForInit(sess, attributesManagerImpl, "group", "resource", Holder.HolderType.GROUP, Holder.HolderType.RESOURCE);

		//save entityless attributes with values
		attrs = jdbc.query("select " + AttributesManagerImpl.getAttributeMappingSelectQuery("entityless_attr_values") + ", subject from attr_names " +
				"join entityless_attr_values on id=attr_id where (attr_value is not null or attr_value_text is not null)",
				new AttributesManagerImpl.AttributeHoldersRowMapper(sess, attributesManagerImpl, null, null));
		this.setEntitylessAttributes(attrs);

		System.out.println("Cache initialization finished");
	}

	/**
	 * Gets attributes for specified primary holder from relational database and saves them into cache. Gets only attributes with values.
	 *
	 * @param sess perun session
	 * @param attributesManagerImpl attributes manager impl
	 * @param primaryHolder primary holder in string format, it needs to be same as in attr_values table name (f.e. "user" for user_attr_values table)
	 * @param primaryHolderType primary holder type
	 * @throws InternalErrorException
	 */
	private void saveAttributesForInit(PerunSession sess, AttributesManagerImplApi attributesManagerImpl, String primaryHolder, Holder.HolderType primaryHolderType) throws InternalErrorException {
		String tableName = primaryHolder + "_attr_values";

		List<AttributeHolders> attrs = jdbc.query("select " + AttributesManagerImpl.getAttributeMappingSelectQuery(tableName)
						+ ", "+ primaryHolder +"_id as primary_holder_id from attr_names " +
						"join " + tableName + " on id=attr_id where (attr_value is not null or attr_value_text is not null)",
				new AttributesManagerImpl.AttributeHoldersRowMapper(sess, attributesManagerImpl, primaryHolderType, null));

		this.setAttributes(attrs);
	}

	/**
	 * Gets attributes for specified primary holder and secondary holder from relational database and saves them into cache. Gets only attributes with values.
	 *
	 * @param sess perun session
	 * @param attrManagerImpl attributes manager impl
	 * @param primHolder primary holder in string format, it needs to be same as in attr_values table name (f.e. "group" for group_resource_attr_values table)
	 * @param secHolder secondary holder in string format, it needs to be same as in attr_values table name (f.e. "resource" for group_resource_attr_values table)
	 * @param primHolderType primary holder type
	 * @param secHolderType secondary holder type
	 * @throws InternalErrorException
	 */
	private void saveAttributesForInit(PerunSession sess, AttributesManagerImplApi attrManagerImpl, String primHolder, String secHolder, Holder.HolderType primHolderType, Holder.HolderType secHolderType) throws InternalErrorException {
		String tableName = primHolder + "_" + secHolder + "_attr_values";

		List<AttributeHolders> attrs = jdbc.query("select " + AttributesManagerImpl.getAttributeMappingSelectQuery(tableName) +
						", "+ primHolder +"_id as primary_holder_id , " + secHolder + "_id as secondary_holder_id from attr_names " +
						"join " + tableName + " on id=attr_id where (attr_id < 10 and attr_value is not null or attr_value_text is not null)",
				new AttributesManagerImpl.AttributeHoldersRowMapper(sess, attrManagerImpl, primHolderType, secHolderType));

		this.setAttributes(attrs);
	}
}
