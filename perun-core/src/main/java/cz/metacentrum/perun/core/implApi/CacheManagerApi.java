package cz.metacentrum.perun.core.implApi;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Holder;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;

import java.util.List;

/**
 * Api with all search and update methods that can be used to work with cache.
 *
 * @author Simona Kruppova
 */
public interface CacheManagerApi {

	/**
	 * Returns true if cache was updated in transaction.
	 *
	 * @return true if cache was updated in transaction, false if it was not updated or if there is no transaction
	 */
	boolean wasCacheUpdatedInTransaction();

	/**
	 * Gets all <b>non-empty</b> attributes associated with the primary holder.
	 * Gets only non-virtual attributes.
	 * Should be used only if wasCacheUpdatedInTransaction is false, else may return incorrect results.
	 *
	 * @param holder primary holder
	 * @return list of attributes
	 * @throws InternalErrorException
	 */
	List<Attribute> getAllNonEmptyAttributes(Holder holder) throws InternalErrorException;

	/**
	 * Gets all <b>non-empty</b> attributes associated with the primary holder and secondary holder.
	 * Gets only non-virtual, non-core attributes.
	 * Should be used only if wasCacheUpdatedInTransaction is false, else may return incorrect results.
	 *
	 * @param primaryHolder primary holder
	 * @param secondaryHolder secondary holder
	 * @return list of attributes
	 * @throws InternalErrorException
	 */
	List<Attribute> getAllNonEmptyAttributes(Holder primaryHolder, Holder secondaryHolder) throws InternalErrorException;

	/**
	 * Gets all attributes associated with the primary holder. Name of attributes starts with startPartOfName.
	 * Gets only non-virtual attributes.
	 * Should be used only if wasCacheUpdatedInTransaction is false, else may return incorrect results.
	 *
	 * @param startPartOfName attribute name starts with this part
	 * @param holder primary holder
	 * @return list of attributes whose name starts with startPartOfName
	 * @throws InternalErrorException
	 */
	List<Attribute> getAllAttributesByStartPartOfName(String startPartOfName, Holder holder) throws InternalErrorException;

	/**
	 * Gets all <b>non-empty</b> attributes associated with any user on the facility.
	 * Gets only non-virtual, non-core attributes.
	 * It may happen that it returns more attributes than it should, because it may return also attributes for users that do not exist (see javadoc for removeAllAttributes methods)
	 * Should be used only if wasCacheUpdatedInTransaction is false, else may return incorrect results.
	 *
	 * @param facilityId facility id
	 * @return list of attributes
	 * @throws InternalErrorException
	 */
	List<Attribute> getUserFacilityAttributesForAnyUser(int facilityId) throws InternalErrorException;

	/**
	 * Gets all <b>non-empty</b> attributes associated with the user and any facility.
	 * Should be used only if wasCacheUpdatedInTransaction is false, else may return incorrect results.
	 *
	 * @param user User
	 * @return list of attributes
	 * @throws InternalErrorException
	 */
	List<Attribute> getAllUserFacilityAttributes(User user) throws InternalErrorException;

	/**
	 * Gets all attributes associated with the primary holder and secondary holder which have name in list attrNames (empty and virtual too).
	 * If secondary holder is null, it returns attributes for primary holder.
	 *
	 * @param attrNames list of attributes names
	 * @param primaryHolder primary holder
	 * @param secondaryHolder secondary holder
	 * @return list of attributes
	 */
	List<Attribute> getAttributesByNames(List<String> attrNames, Holder primaryHolder, Holder secondaryHolder);

	/**
	 * Returns all attributes with not-null value which fit the attributeDefinition.
	 * Gets only non-virtual, non-core attributes.
	 * Should be used only if wasCacheUpdatedInTransaction is false, else may return incorrect results.
	 *
	 * @param attributeDefinition attribute definition
	 * @return list of attributes
	 * @throws InternalErrorException
	 */
	List<Attribute> getAttributesByAttributeDefinition(AttributeDefinition attributeDefinition) throws InternalErrorException;

	/**
	 * Gets all virtual attributes associated with the primary holder and secondary holder. If secondary holder is null, it returns attributes for primary holder.
	 * Should be used only if wasCacheUpdatedInTransaction is false, else may return incorrect results.
	 *
	 * @param primaryHolderType primary holder type
	 * @param secondaryHolderType secondary holder type
	 * @return list of attributes
	 * @throws InternalErrorException
	 */
	List<Attribute> getVirtualAttributes(Holder.HolderType primaryHolderType, Holder.HolderType secondaryHolderType) throws InternalErrorException;

	/**
	 * Gets particular attribute for the primary holder and secondary holder by name. If secondary holder is null, it returns attribute for primary holder.
	 * If such attribute does not exist, it returns attribute definition.
	 *
	 * @param attrName attribute name defined in the particular manager
	 * @param primaryHolder primary holder
	 * @param secondaryHolder secondary holder
	 * @return attribute
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlying data source
	 */
	Attribute getAttributeByName(String attrName, Holder primaryHolder, Holder secondaryHolder) throws AttributeNotExistsException;

	/**
	 * Gets attribute by id, primary holder and secondary holder. If secondary holder is null, it returns attribute for primary holder.
	 * If such attribute does not exist, it returns attribute definition.
	 *
	 * @param id id of attribute to get
	 * @param primaryHolder primary holder
	 * @param secondaryHolder secondary holder
	 * @return attribute
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlying data source
	 */
	Attribute getAttributeById(int id, Holder primaryHolder, Holder secondaryHolder) throws AttributeNotExistsException;

	/**
	 * Gets all similar attribute names which start with startingPartOfAttributeName.
	 * Should be used only if wasCacheUpdatedInTransaction is false, else may return incorrect results.
	 *
	 * @param startingPartOfAttributeName starting part of attribute name (something like: urn:perun:user_facility:attribute-def:def:login-namespace:)
	 * @return list of similar attribute names
	 */
	List<String> getAllSimilarAttributeNames(String startingPartOfAttributeName);

	/**
	 * Gets attributes definitions (attribute without defined value).
	 * Should be used only if wasCacheUpdatedInTransaction is false, else may return incorrect results.
	 *
	 * @return list of attributes definitions
	 */
	List<AttributeDefinition> getAttributesDefinitions();

	/**
	 * Gets attributes definition (attribute without defined value) with specified namespace.
	 * It may happen that it returns more attribute definitions than it should, because it may return also attribute definitions for entities that do not exist (see javadoc for removeAllAttributes methods)
	 * Should be used only if wasCacheUpdatedInTransaction is false, else may return incorrect results.
	 *
	 * @param namespace get only attributes with this namespace
	 * @return list of attributes definitions
	 */
	List<AttributeDefinition> getAttributesDefinitionsByNamespace(String namespace);

	/**
	 * Gets attributes definitions by ids.
	 *
	 * @param attrIds ids to get attribute definitions by
	 * @return list of attribute definitions
	 */
	List<AttributeDefinition> getAttributesDefinitions(List<Integer> attrIds);

	/**
	 * Gets attribute definition (attribute without defined value).
	 *
	 * @param attrName attribute name defined in the particular manager
	 * @return attribute definition
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlying data source
	 */
	AttributeDefinition getAttributeDefinition(String attrName) throws AttributeNotExistsException;

	/**
	 * Gets attribute definition by id (attribute without defined value).
	 *
	 * @param id id of attribute definition to get
	 * @return attribute definition
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlying data source
	 */
	AttributeDefinition getAttributeDefinition(int id) throws AttributeNotExistsException;

	/**
	 * Gets all <b>non-empty</b> entityless attributes where subject equals key.
	 * Gets only non-virtual, non-core attributes.
	 * Should be used only if wasCacheUpdatedInTransaction is false, else may return incorrect results.
	 *
	 * @param key subject of entityless attribute
	 * @return list of entityless attributes
	 * @throws InternalErrorException
	 */
	List<Attribute> getAllNonEmptyEntitylessAttributes(String key) throws InternalErrorException;

	/**
	 * Gets all <b>non-empty</b> entityless attributes by attrName.
	 * Gets only non-virtual, non-core attributes.
	 * Should be used only if wasCacheUpdatedInTransaction is false, else may return incorrect results.
	 *
	 * @param attrName attribute name defined in the particular manager
	 * @return list of entityless attributes
	 * @throws InternalErrorException
	 */
	List<Attribute> getAllNonEmptyEntitylessAttributesByName(String attrName) throws InternalErrorException;

	/**
	 * Gets particular entityless attribute by name and key. If such attribute does not exist, it returns attribute definition.
	 *
	 * @param attrName attribute name defined in the particular manager
	 * @param key subject of entityless attribute
	 * @return entityless attribute
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlying data source
	 */
	Attribute getEntitylessAttribute(String attrName, String key) throws AttributeNotExistsException;

	/**
	 * Gets particular entityless attribute value by key and id. If such attribute does not exist, it returns null.
	 *
	 * @param attrId id of attribute
	 * @param key subject of entityless attribute
	 * @return attribute value in String
	 * @throws InternalErrorException
	 */
	String getEntitylessAttrValue(int attrId, String key) throws InternalErrorException;

	/**
	 * Gets list of keys of entityless attributes by attrName.
	 * Should be used only if wasCacheUpdatedInTransaction is false, else may return incorrect results.
	 *
	 * @param attrName attribute name defined in the particular manager
	 * @return list of keys (subjects) of entityless attributes
	 */
	List<String> getEntitylessAttrKeys(String attrName);

	/**
	 * Get all values for specified attribute.
	 * Gets only non-virtual, non-core attribute values.
	 * It may happen that it returns more attribute values than it should, because it may return also attribute values for entity that does not exist (see javadoc for removeAllAttributes methods)
	 * Should be used only if wasCacheUpdatedInTransaction is false, else may return incorrect results.
	 *
	 * @param holderType holder type
	 * @param attributeDefinition attribute definition, type of namespace should be same as holder type
	 * @return attribute values
	 */
	List<Object> getAllValues(Holder.HolderType holderType, AttributeDefinition attributeDefinition);

	/**
	 * Get all values for specified attribute.
	 * Gets only non-virtual, non-core attribute values.
	 * It may happen that it returns more attribute values than it should, because it may return also attribute values for entities that do not exist (see javadoc for removeAllAttributes methods)
	 * Should be used only if wasCacheUpdatedInTransaction is false, else may return incorrect results.
	 *
	 * @param primaryHolderType primary holder type
	 * @param secondaryHolderType secondary holder type
	 * @param attributeDefinition attribute definition, type of namespace should be same as holder types
	 * @return attribute values
	 */
	List<Object> getAllValues(Holder.HolderType primaryHolderType, Holder.HolderType secondaryHolderType, AttributeDefinition attributeDefinition);

	/**
	 * Gets all attributes associated with the primary holder which have id in list attrIds (empty and virtual too).
	 * Should be used only if wasCacheUpdatedInTransaction is false, else may return incorrect results.
	 *
	 * @param attrIds list of attribute ids
	 * @param primaryHolder primary holder
	 * @return list of attributes
	 * @throws InternalErrorException
	 */
	List<Attribute> getAttributesByIds(List<Integer> attrIds, Holder primaryHolder) throws InternalErrorException;

	/**
	 * Gets all attributes associated with the primary and secondary holder which have id in list attrIds (empty and virtual too).
	 * Should be used only if wasCacheUpdatedInTransaction is false, else may return incorrect results.
	 *
	 * @param attrIds list of attribute ids
	 * @param primaryHolder primary holder
	 * @param secondaryHolder secondary holder
	 * @return list of attributes
	 * @throws InternalErrorException
	 */
	List<Attribute> getAttributesByIds(List<Integer> attrIds, Holder primaryHolder, Holder secondaryHolder) throws InternalErrorException;

	/**
	 * Check if attribute exists in underlying data source.
	 * Should be used only if wasCacheUpdatedInTransaction is false, else may return incorrect results.
	 *
	 * @param attribute attribute to check
	 * @return true if attribute exists in underlying data source, false otherwise
	 */
	boolean checkAttributeExists(AttributeDefinition attribute);

	/**
	 * Stores the attribute associated with primary holder and secondary holder. If secondary holder is null, it stores the attribute by the primary holder.
	 * If there is transaction, it sets cache as updated in transaction.
	 *
	 * @param attribute attribute to set
	 * @param primaryHolder primary holder
	 * @param secondaryHolder secondary holder
	 * @throws InternalErrorException
	 */
	void setAttribute(Attribute attribute, Holder primaryHolder, Holder secondaryHolder) throws InternalErrorException;

	/**
	 * Stores the attribute associated with primary holder and secondary holder. If secondary holder is null, it stores the attribute by the primary holder.
	 * It checks whether the attribute already exists. If it does, it does not update createdBy and createdAt values.
	 * If there is transaction, it sets cache as updated in transaction.
	 *
	 * @param attribute attribute to set
	 * @param primaryHolder primary holder
	 * @param secondaryHolder secondary holder
	 * @throws InternalErrorException
	 */
	void setAttributeWithExistenceCheck(Attribute attribute, Holder primaryHolder, Holder secondaryHolder) throws InternalErrorException;

	/**
	 * Stores the attribute definition.
	 * If there is transaction, it sets cache as updated in transaction.
	 *
	 * @param attribute attribute definition to set
	 */
	void setAttributeDefinition(AttributeDefinition attribute);

	/**
	 * Stores the entityless attribute.
	 * If there is transaction, it sets cache as updated in transaction.
	 *
	 * @param attribute entityless attribute to set
	 * @param key subject of entityless attribute
	 * @throws InternalErrorException
	 */
	void setEntitylessAttribute(Attribute attribute, String key) throws InternalErrorException;

	/**
	 * Stores the entityless attribute.
	 * It checks whether the attribute already exists. If it does, it does not update createdBy and createdAt values.
	 * If there is transaction, it sets cache as updated in transaction.
	 *
	 * @param attribute entityless attribute to set
	 * @param key subject of entityless attribute
	 * @throws InternalErrorException
	 */
	void setEntitylessAttributeWithExistenceCheck(Attribute attribute, String key) throws InternalErrorException;

	/**
	 * Updates attribute definition. It does not update createdBy, createdByUid and createdAt value.
	 * If there is transaction, it sets cache as updated in transaction.
	 *
	 * @param attributeDefinition attribute definition to update
	 */
	void updateAttributeDefinition(AttributeDefinition attributeDefinition);

	/**
	 * Deletes attribute by id - definition and all values. It will not behave correctly in nested transaction.
	 *
	 * If the cache was updated in transaction, it reinitializes it - it clears it and fills it with contents of underlying relational database.
	 * It needs to be done this way, else cache could end up in inconsistent state.
	 *
	 * If there is transaction, it sets cache as updated in transaction.
	 *
	 * @param id attribute id
	 * @param session perun session
	 * @param attributesManager attributes manager impl
	 * @throws InternalErrorException
	 */
	void deleteAttribute(int id, PerunSession session, AttributesManagerImplApi attributesManager) throws InternalErrorException;

	/**
	 * Unset particular attribute for holders. If secondary holder is null, it unsets attribute for the primary holder.
	 * Should be used only when there is attribute to remove.
	 * If there is transaction, it sets cache as updated in transaction.
	 *
	 * @param attribute attribute to remove
	 * @param primaryHolder primary holder
	 * @param secondaryHolder secondary holder
	 */
	void removeAttribute(AttributeDefinition attribute, Holder primaryHolder, Holder secondaryHolder);

	/**
	 * Unset particular entityless attribute with subject equals key.
	 * Should be used only when there is attribute to remove.
	 * If there is transaction, it sets cache as updated in transaction.
	 *
	 * @param attribute attribute to remove
	 * @param key subject of entityless attribute
	 */
	void removeEntitylessAttribute(AttributeDefinition attribute, String key);

	/**
	 * Tries to unset all attributes for primary holder.
	 * It may happen that not all the attributes are unset. No attributes are unset in nested transaction.
	 * If there is transaction, it sets cache as updated in transaction.
	 *
	 * @param holder primary holder to remove attributes from
	 */
	void removeAllAttributes(Holder holder);

	/**
	 * Tries to unset all attributes associated with primary and secondary holder.
	 * It may happen that not all the attributes are unset. No attributes are unset in nested transaction.
	 * If there is transaction, it sets cache as updated in transaction.
	 *
	 * @param primaryHolder primary holder to remove attributes from
	 * @param secondaryHolder secondary holder to remove attributes from
	 */
	void removeAllAttributes(Holder primaryHolder, Holder secondaryHolder);

	/**
	 * Tries to unset all attributes for primary holder with the given secondary holder type.
	 * It may happen that not all the attributes are unset. No attributes are unset in nested transaction.
	 * If there is transaction, it sets cache as updated in transaction.
	 *
	 * @param primaryHolder primary holder to remove attributes from
	 * @param secondaryHolderType secondary holder type to remove attributes from
	 */
	void removeAllAttributes(Holder primaryHolder, Holder.HolderType secondaryHolderType);

	/**
	 * Tries to unset all attributes for secondary holder with the given primary holder type.
	 * It may happen that not all the attributes are unset. No attributes are unset in nested transaction.
	 * If there is transaction, it sets cache as updated in transaction.
	 *
	 * @param primaryHolderType primary holder type to remove attributes from
	 * @param secondaryHolder secondary holder to remove attributes from
	 */
	void removeAllAttributes(Holder.HolderType primaryHolderType, Holder secondaryHolder);

	/**
	 * Initializes cache. It clears it and then fills it with attributes from relational database.
	 *
	 * @param sess perun session
	 * @param attributesManagerImpl attributes manager impl
	 * @throws InternalErrorException
	 */
	void initialize(PerunSession sess, AttributesManagerImplApi attributesManagerImpl) throws InternalErrorException;
}