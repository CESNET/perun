package cz.metacentrum.perun.ldapc.model;

import java.util.List;

import javax.naming.Name;

import org.springframework.ldap.core.DirContextOperations;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;

/**
 * 
 * @author michal
 *
 * @param <T>
 */
public interface PerunEntry<T extends PerunBean> {

	/**
	 * 
	 * @param bean
	 * @throws InternalErrorException
	 */
	void addEntry(T bean) throws InternalErrorException;

	/**
	 * 
	 * @param bean
	 * @throws InternalErrorException
	 */
	void modifyEntry(T bean) throws InternalErrorException;

	/**
	 * 
	 * @param bean
	 * @param attrNames
	 * @throws InternalErrorException
	 */
	void modifyEntry(T bean, String... attrNames) throws InternalErrorException;

	/**
	 * 
	 * @param bean
	 * @param attrs
	 * @param attrNames
	 * @throws InternalErrorException
	 */
	void modifyEntry(T bean, Iterable<PerunAttribute<T>> attrs, String... attrNames) throws InternalErrorException;

	/**
	 * 
	 * @param bean
	 * @param attrName
	 * @param attr
	 * @throws InternalErrorException
	 */
	void modifyEntry(T bean, AttributeDefinition attr) throws InternalErrorException;

	/**
	 * 
	 * @param bean
	 * @param attrDef
	 * @param attr
	 * @throws InternalErrorException
	 */
	void modifyEntry(T bean, PerunAttribute<T> attrDef, AttributeDefinition attr) throws InternalErrorException;
	
	/**
	 * 
	 * @param bean
	 * @throws InternalErrorException
	 */
	void deleteEntry(T bean) throws InternalErrorException;

	/**
	 * 
	 * @param bean
	 * @throws InternalErrorException
	 */
	void synchronizeEntry(T bean) throws InternalErrorException;
	
	/**
	 * 
	 * @param bean
	 * @param attrs
	 * @throws InternalErrorException 
	 */
	void synchronizeEntry(T bean, Iterable<Attribute> attrs) throws InternalErrorException;
	
	/**
	 * 
	 * @param dn
	 * @return
	 */
	DirContextOperations findByDN(Name dn);
	
	/**
	 * 
	 * @param id
	 * @return
	 */
	DirContextOperations findById(String ...id);

	/**
	 * 
	 * @param id
	 * @return
	 */
	Name getEntryDN(String ...id);
	
	/**
	 * 
	 * @param bean
	 * @return
	 */
	Boolean entryExists(T bean);
	
	/**
	 * Return true if entry attribute with ldapAttributeName in ldap exists.
	 *
	 * @param bean bean of entry in perun
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
	 * 
	 * @return
	 */
	List<PerunAttribute<T>> getAttributeDescriptions();

	/**
	 * 
	 * @param attributeDescriptions
	 */
	void setAttributeDescriptions(List<PerunAttribute<T>> attributeDescriptions);

	/**
	 * 
	 * @return
	 */
	List<String> getUpdatableAttributeNames();

	/**
	 * 
	 * @param updatableAttributeNames
	 */
	void setUpdatableAttributeNames(List<String> updatableAttributeNames);

	/**
	 * 
	 * @return
	 */
	List<String> getPerunAttributeNames(); 
}
