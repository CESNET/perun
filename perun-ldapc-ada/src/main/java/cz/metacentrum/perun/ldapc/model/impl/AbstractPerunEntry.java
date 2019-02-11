package cz.metacentrum.perun.ldapc.model.impl;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.NamingEnumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.support.LdapNameBuilder;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.ldapc.beans.LdapProperties;
import cz.metacentrum.perun.ldapc.model.PerunAttribute;
import cz.metacentrum.perun.ldapc.model.PerunEntry;

public abstract class AbstractPerunEntry<T extends PerunBean> implements InitializingBean, PerunEntry<T> {

	private final static Logger log = LoggerFactory.getLogger(AbstractPerunEntry.class);

	@Autowired
	protected LdapTemplate ldapTemplate;
	@Autowired
	protected LdapProperties ldapProperties;

	private List<PerunAttribute<T>> attributeDescriptions;
	private List<String> updatableAttributeNames; 
	
	public void afterPropertiesSet() {
		if(attributeDescriptions == null)
			attributeDescriptions = getDefaultAttributeDescriptions();
		else
			attributeDescriptions.addAll(getDefaultAttributeDescriptions());
		if(updatableAttributeNames == null)
			updatableAttributeNames = getDefaultUpdatableAttributes();
		else
			updatableAttributeNames.addAll(getDefaultUpdatableAttributes());
			
	}

	abstract protected List<String> getDefaultUpdatableAttributes();

	abstract protected List<PerunAttribute<T>> getDefaultAttributeDescriptions();

	/* (non-Javadoc)
	 * @see cz.metacentrum.perun.ldapc.model.impl.PerunEntry#addEntry(cz.metacentrum.perun.core.api.PerunBean)
	 */
	@Override
	public void addEntry(T bean) throws InternalErrorException {
		DirContextOperations context = new DirContextAdapter(buildDN(bean));
		mapToContext(bean, context);
		ldapTemplate.bind(context);
	}
	
	@Override
	public void modifyEntry(T bean) throws InternalErrorException {
		modifyEntry(bean, attributeDescriptions, updatableAttributeNames);
	}

	@Override
	public void modifyEntry(T bean, String... attrNames) throws InternalErrorException {
		modifyEntry(bean, attributeDescriptions, Arrays.asList(attrNames));
	}

	/* (non-Javadoc)
	 * @see cz.metacentrum.perun.ldapc.model.impl.PerunEntry#modifyEntry(cz.metacentrum.perun.core.api.PerunBean)
	 */
	@Override
	public void modifyEntry(T bean, Iterable<PerunAttribute<T>> attrs, String...attrNames) throws InternalErrorException {
		modifyEntry(bean, attrs, Arrays.asList(attrNames));
	}
	
	protected void modifyEntry(T bean, Iterable<PerunAttribute<T>> attrs, List<String> attrNames) throws InternalErrorException {
		DirContextOperations entry = findByDN(buildDN(bean));
		mapToContext(bean, entry, findAttributeDescriptionsByLdapName(attrs, attrNames));
		ldapTemplate.modifyAttributes(entry);
	}

	@Override
	public void modifyEntry(T bean, AttributeDefinition attr) throws InternalErrorException {
		DirContextOperations entry = findByDN(buildDN(bean));
		List<PerunAttribute<T>> attrDefs = findAttributeDescriptionsByPerunAttr(getAttributeDescriptions(), attr);
		if(attrDefs.isEmpty())
			throw new InternalErrorException("Attribute description for attribute " + attr.getName() + " not found");
		for(PerunAttribute<T> attrDef : attrDefs) {
			mapToContext(bean, entry, attrDef, attr);
		}
		ldapTemplate.modifyAttributes(entry);
	}

	@Override
	public void modifyEntry(T bean, PerunAttribute<T> attrDef, AttributeDefinition attr) throws InternalErrorException {
		DirContextOperations entry = findByDN(buildDN(bean));
		mapToContext(bean, entry, attrDef, attr);
		ldapTemplate.modifyAttributes(entry);
	}

	/* (non-Javadoc)
	 * @see cz.metacentrum.perun.ldapc.model.impl.PerunEntry#deleteEntry(cz.metacentrum.perun.core.api.PerunBean)
	 */
	@Override
	public void deleteEntry(T bean) throws InternalErrorException {
		try {
			ldapTemplate.unbind(buildDN(bean));
		} catch (NameNotFoundException e) {
			throw new InternalErrorException(e);
		}
	}
	

	@Override
	public void synchronizeEntry(T bean) throws InternalErrorException {
		DirContextOperations entry;
		boolean newEntry = false;
		try {
			entry = findByDN(buildDN(bean));
		} catch(NameNotFoundException e) {
			newEntry = true;
			entry = new DirContextAdapter(buildDN(bean));
		}
		if(newEntry) {
			log.debug("Creating new entry {} ", entry.toString());
			// map with objectclasses
			mapToContext(bean, entry);
			ldapTemplate.bind(entry);
		} else {
			log.debug("Modifying entry {} ", entry.toString());
			// map without objectclasses (entry exists)
			mapToContext(bean, entry, getAttributeDescriptions());
			ldapTemplate.modifyAttributes(entry);
		}
	}

	@Override
	public void synchronizeEntry(T bean, Iterable<Attribute> attrs) throws InternalErrorException {
		DirContextOperations entry;
		boolean newEntry = false;
		try {
			entry = findByDN(buildDN(bean));
		} catch(NameNotFoundException e) {
			newEntry = true;
			entry = new DirContextAdapter(buildDN(bean));
		}
		mapToContext(bean, entry);
		for (Attribute attribute : attrs) {
			for(PerunAttribute<T> attributeDesc : findAttributeDescriptionsByPerunAttr(attributeDescriptions, attribute)) {
				mapToContext(bean, entry, attributeDesc, attribute);
			}
		}
		if(newEntry) {
			ldapTemplate.bind(entry);
		} else {
			ldapTemplate.modifyAttributes(entry);
		}
	}

	@Override
	public DirContextOperations findByDN(Name dn) {
		return ldapTemplate.lookupContext(dn);
	}


	@Override
	public DirContextOperations findById(String ...id) {
		return ldapTemplate.lookupContext(getEntryDN(id));
	}

	abstract public Name getEntryDN(String ...id);
	
	@Override
	public Boolean entryAttributeExists(T bean, String ldapAttributeName) { 
		DirContextOperations entry = findByDN(buildDN(bean));
		String value = entry.getStringAttribute(ldapAttributeName);
		return (value != null);
	}

	@Override
	public void removeAllAttributes(T bean) {
		DirContextOperations entry = findByDN(buildDN(bean));
		/* we have to find all existing entry attributes to resolve all the present options in names */
		NamingEnumeration<String> attrNames = entry.getAttributes().getIDs();
		while(attrNames.hasMoreElements()) {
			String attrName = attrNames.nextElement();
			Iterable<PerunAttribute<T>> attrDefs = findAttributeDescriptionsByLdapName(getAttributeDescriptions(), Arrays.asList(attrName));
			for(PerunAttribute<T> attrDef: attrDefs) {
				if(attrDef.requiresAttributeBean() && !attrDef.isRequired()) {
					entry.setAttributeValues(attrName, null);
				}
			}
		}
		if(entry.getModificationItems().length > 0) {
			ldapTemplate.modifyAttributes(entry);
		}
	}

	@Override
	public Boolean entryExists(T bean) {
		DirContextOperations entry;
		try {
			entry = findByDN(buildDN(bean));
		} catch (NameNotFoundException e) {
			return false;
		}
		if(entry == null) 
			return false;
		else
			return true; 
	}
	
	@Override
	public List<PerunAttribute<T>> getAttributeDescriptions() {
		return attributeDescriptions;
	}

	@Override
	public void setAttributeDescriptions(List<PerunAttribute<T>> attributeDescriptions) {
		this.attributeDescriptions = attributeDescriptions;
	}

	@Override
	public List<String> getUpdatableAttributeNames() {
		return updatableAttributeNames;
	}

	@Override
	public void setUpdatableAttributeNames(List<String> updatableAttributeNames) {
		this.updatableAttributeNames = updatableAttributeNames;
	}

	@Override
	public List<String> getPerunAttributeNames() {
		List<String> attrNames = new ArrayList<String>();
		for(PerunAttribute<T> attrDesc: getAttributeDescriptions()) {
			if(!attrDesc.requiresAttributeBean()) continue;
			AttributeValueExtractor extractor = attrDesc.isMultiValued() ? (AttributeValueExtractor)attrDesc.getMultipleValuesExtractor() : (AttributeValueExtractor)attrDesc.getSingleValueExtractor();
			attrNames.add(extractor.getNamespace() + ":" + extractor.getName());
		}
		return attrNames;
	}

	protected String getBaseDN() {
		return ldapProperties.getLdapBase();
	}
	
	protected Name addBaseDN(Name entryDN) {
		try {
			return LdapNameBuilder.newInstance(getBaseDN()).build().addAll(entryDN);
		} catch (InvalidNameException e) {
			return entryDN;
		}
	}
	
	abstract protected Name buildDN(T bean);
	
	abstract protected void mapToContext(T bean, DirContextOperations context) throws InternalErrorException;
	
	/**
	 * Takes data from Perun bean and stores them into LDAP entry (context) for creation or update.
	 * List of attributes to fill-in is given as parameter; if attribute has no value, it will be removed.
	 * Attribute definitions that require data from Attribute bean are ignored.
	 * 
	 * @param bean - Perun bean containing the basic data
	 * @param context - LDAP context (ie. entry) that should be filled
	 * @param attrs - list of known attributes
	 * @throws InternalErrorException
	 */
	protected void mapToContext(T bean, DirContextOperations context, Iterable<PerunAttribute<T>> attrs) throws InternalErrorException {
		for(PerunAttribute<T> attr: attrs) {
			if(attr.requiresAttributeBean())
				continue;
			String[] values;
			if(attr.isMultiValued()) {
				values = attr.getValues(bean);
			} else {
				if(attr.hasValue(bean)) {
					values = new String[] { attr.getValue(bean) };
				} else {
					values = null;
				}
			} 
			if(attr.isRequired() && (values == null || values.length == 0)) {
				throw new InternalErrorException("Value of required attribute " +  attr.getName() + " is empty");
			}
			context.setAttributeValues(attr.getName(), values);
		}
	}

	/**
	 * 
	 * 
	 * @param bean
	 * @param entry
	 * @param attrDef
	 * @param attr
	 */
	protected void mapToContext(T bean, DirContextOperations entry, PerunAttribute<T> attrDef, AttributeDefinition attr) throws InternalErrorException {
		Object[] values;
		if(attr instanceof Attribute) {
			if(attrDef.isMultiValued()) {
				values = attrDef.getValues(bean, (Attribute)attr);
			} else {
				if(attrDef.hasValue(bean, (Attribute)attr)) {
				values = Arrays.asList(attrDef.getValue(bean, (Attribute)attr)).toArray();
				} else {
					values = null;
				}
			}
			if(attrDef.isRequired() && (values == null || values.length == 0)) {
				throw new InternalErrorException("Value of required attribute " +  attrDef.getName() + " is empty");
			}
		} else {
			values = null;
		}
		entry.setAttributeValues(attrDef.getName(attr), values);
	}
	
	protected Iterable<PerunAttribute<T>> findAttributeDescriptionsByLdapName(Iterable<PerunAttribute<T>> attrs, Iterable<String> attrNames) {
		List<PerunAttribute<T>> result = new ArrayList<PerunAttribute<T>>();
		for(PerunAttribute<T> attrDesc : attrs) {
			String descName = attrDesc.getName();
			for(String attrName : attrNames) {
				if(descName.contains(";")) {
					// tagged names are taken as prefixes
					if(attrName.startsWith(descName)) result.add(attrDesc);
				} else {
					// names without options are compared as a whole 
					if(descName.equals(attrName)) result.add(attrDesc); 
				}
			}
		}
		return result;
	}

	/**
	 * Find attribute description for given Perun AttributeDefinition, ie. find which attribute describes
	 * how to extract value from this AttributeDefinition.
	 * 
	 * @param attrs
	 * @param attr
	 * @return
	 */
	protected List<PerunAttribute<T>> findAttributeDescriptionsByPerunAttr(List<PerunAttribute<T>> attrs, AttributeDefinition attr) {
		List<PerunAttribute<T>> result = new ArrayList<PerunAttribute<T>>();
		for (PerunAttribute<T> attrDef : attrs) {
			AttributeValueExtractor extractor = null;
			if(attrDef.isMultiValued()) {
				PerunAttribute.MultipleValuesExtractor<T> ext = attrDef.getMultipleValuesExtractor();
				if(ext instanceof AttributeValueExtractor) {
						extractor = (AttributeValueExtractor)ext;
				}
			} else {
				PerunAttribute.SingleValueExtractor<T> ext = attrDef.getSingleValueExtractor();
				if(ext instanceof AttributeValueExtractor) {
					extractor = (AttributeValueExtractor)ext;
				}
			}
			if(extractor != null && extractor.appliesToAttribute(attr)) {
				result.add(attrDef);
			}
		}
		return result;
	}

} 
