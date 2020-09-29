package cz.metacentrum.perun.ldapc.model;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import org.springframework.ldap.core.DirContextOperations;

import javax.naming.Name;
import java.util.List;

/**
 * @param <T>
 * @author michal
 */
public interface PerunEntry<T extends PerunBean> {

	public interface SyncOperation {
		public boolean isNew();

		public DirContextOperations getEntry();
	}

	;

	/**
	 * @param bean
	 * @throws InternalErrorException
	 */
	void addEntry(T bean);

	/**
	 * @param bean
	 * @throws InternalErrorException
	 */
	void modifyEntry(T bean);

	/**
	 * @param bean
	 * @param attrNames
	 * @throws InternalErrorException
	 */
	void modifyEntry(T bean, String... attrNames);

	/**
	 * @param bean
	 * @param attrs
	 * @param attrNames
	 * @throws InternalErrorException
	 */
	void modifyEntry(T bean, Iterable<PerunAttribute<T>> attrs, String... attrNames);

	/**
	 * @param bean
	 * @param attrName
	 * @param attr
	 * @throws InternalErrorException
	 */
	void modifyEntry(T bean, AttributeDefinition attr);

	/**
	 * @param bean
	 * @param attrDef
	 * @param attr
	 * @throws InternalErrorException
	 */
	void modifyEntry(T bean, PerunAttribute<T> attrDef, AttributeDefinition attr);
	
	/**
	 * 
	 * @param bean
	 * @param attrs
	 */
	void modifyEntry(T bean, Iterable<Pair<PerunAttribute<T>, AttributeDefinition>> attrs);
	
	/**
	 * @param bean
	 * @throws InternalErrorException
	 */
	void deleteEntry(T bean);

	/**
	 *
	 */
	void deleteEntry(Name dn);

	/**
	 * @return
	 * @throws InternalErrorException
	 */
	List<Name> listEntries();

	/**
	 * @param bean
	 * @throws InternalErrorException
	 */
	SyncOperation beginSynchronizeEntry(T bean);

	/**
	 * @param bean
	 * @param attrs
	 * @throws InternalErrorException
	 */
	SyncOperation beginSynchronizeEntry(T bean, Iterable<Attribute> attrs);

	/**
	 * @param op
	 * @throws InternalErrorException
	 */
	void commitSyncOperation(SyncOperation op);

	/**
	 * @param bean
	 * @throws InternalErrorException
	 */
	void synchronizeEntry(T bean);

	/**
	 * @param bean
	 * @param attrs
	 * @throws InternalErrorException
	 */
	void synchronizeEntry(T bean, Iterable<Attribute> attrs);

	/**
	 * @param dn
	 * @return
	 */
	DirContextOperations findByDN(Name dn);

	/**
	 * @param id
	 * @return
	 */
	DirContextOperations findById(String... id);

	/**
	 * @param id
	 * @return
	 */
	Name getEntryDN(String... id);

	/**
	 * @param bean
	 * @return
	 */
	Boolean entryExists(T bean);

	/**
	 * Return true if entry attribute with ldapAttributeName in ldap exists.
	 *
	 * @param bean              bean of entry in perun
	 * @param ldapAttributeName name of user ldap attribute
	 * @return true if attribute in ldap exists, false if not
	 * @throws InternalErrorException if ldapAttributeName is null
	 */
	Boolean entryAttributeExists(T bean, String ldapAttributeName);

	/**
	 * Remove all attributes that were set using the Attribute bean.
	 *
	 * @param bean
	 */
	void removeAllAttributes(T bean);

	/**
	 * @return
	 */
	List<PerunAttribute<T>> getAttributeDescriptions();

	/**
	 * @param attributeDescriptions
	 */
	void setAttributeDescriptions(List<PerunAttribute<T>> attributeDescriptions);

	/**
	 * @return
	 */
	List<String> getUpdatableAttributeNames();

	/**
	 * @param updatableAttributeNames
	 */
	void setUpdatableAttributeNames(List<String> updatableAttributeNames);

	/**
	 * @return
	 */
	List<String> getPerunAttributeNames();
}
