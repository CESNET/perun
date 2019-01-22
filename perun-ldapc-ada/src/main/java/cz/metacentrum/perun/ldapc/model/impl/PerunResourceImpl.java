package cz.metacentrum.perun.ldapc.model.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.naming.Name;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.support.LdapNameBuilder;

import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.ldapc.model.PerunAttribute;
import cz.metacentrum.perun.ldapc.model.PerunGroup;
import cz.metacentrum.perun.ldapc.model.PerunResource;

public class PerunResourceImpl extends AbstractPerunEntry<Resource> implements PerunResource {

	private final static Logger log = LoggerFactory.getLogger(PerunResourceImpl.class);

	@Autowired
	private PerunGroup perunGroup;
	
	@Override
	protected List<String> getDefaultUpdatableAttributes() {
		return Arrays.asList(
				PerunAttribute.PerunAttributeNames.ldapAttrCommonName,
				PerunAttribute.PerunAttributeNames.ldapAttrDescription);
	}

	@Override
	protected List<PerunAttribute<Resource>> getDefaultAttributeDescriptions() {
		return Arrays.asList(
				new PerunAttributeDesc<>(
						PerunAttribute.PerunAttributeNames.ldapAttrCommonName, 
						PerunAttribute.REQUIRED, 
						(PerunAttribute.SingleValueExtractor<Resource>)(resource, attrs) -> resource.getName()
						),
				new PerunAttributeDesc<>(
						PerunAttribute.PerunAttributeNames.ldapAttrPerunResourceId, 
						PerunAttribute.REQUIRED, 
						(PerunAttribute.SingleValueExtractor<Resource>)(resource, attrs) -> String.valueOf(resource.getId())
						),
				new PerunAttributeDesc<>(
						PerunAttribute.PerunAttributeNames.ldapAttrPerunFacilityId, 
						PerunAttribute.REQUIRED, 
						(PerunAttribute.SingleValueExtractor<Resource>)(resource, attrs) -> String.valueOf(resource.getFacilityId())
						),
				new PerunAttributeDesc<>(
						PerunAttribute.PerunAttributeNames.ldapAttrPerunVoId, 
						PerunAttribute.REQUIRED, 
						(PerunAttribute.SingleValueExtractor<Resource>)(resource, attrs) -> String.valueOf(resource.getVoId())
						),
				new PerunAttributeDesc<>(
						PerunAttribute.PerunAttributeNames.ldapAttrDescription, 
						PerunAttribute.OPTIONAL, 
						(PerunAttribute.SingleValueExtractor<Resource>)(resource, attrs) -> resource.getDescription()
						)
				);
	}

	public void addResource(Resource resource, String entityID) throws InternalErrorException {
		addEntry(resource);
		// get info about entityID attribute if exists
		if(entityID != null) { 
			try {
				DirContextOperations context = findByDN(buildDN(resource));
				context.setAttributeValue(PerunAttribute.PerunAttributeNames.ldapAttrEntityID, entityID);
				ldapTemplate.modifyAttributes(context);
			} catch (Exception e) {
				throw new InternalErrorException(e);
			}
		}
	}

	public void deleteResource(Resource resource) throws InternalErrorException {
		deleteEntry(resource);
	}


	@Override
	public void updateResource(Resource resource) throws InternalErrorException {
		modifyEntry(resource); 

	}

	@Override
	public void assignGroup(Resource resource, Group group) throws InternalErrorException {
		DirContextOperations entry = findByDN(buildDN(resource));
		entry.addAttributeValue(PerunAttribute.PerunAttributeNames.ldapAttrAssignedGroupId, String.valueOf(group.getId()));
		ldapTemplate.modifyAttributes(entry);
		entry = perunGroup.findById(String.valueOf(group.getVoId()), String.valueOf(group.getId()));
		entry.addAttributeValue(PerunAttribute.PerunAttributeNames.ldapAttrAssignedToResourceId, String.valueOf(resource.getId()));
		ldapTemplate.modifyAttributes(entry);
	}

	@Override
	public void removeGroup(Resource resource, Group group) throws InternalErrorException {
		DirContextOperations entry = findByDN(buildDN(resource));
		entry.removeAttributeValue(PerunAttribute.PerunAttributeNames.ldapAttrAssignedGroupId, String.valueOf(group.getId()));
		ldapTemplate.modifyAttributes(entry);
		entry = perunGroup.findById(String.valueOf(group.getVoId()), String.valueOf(group.getId()));
		entry.removeAttributeValue(PerunAttribute.PerunAttributeNames.ldapAttrAssignedToResourceId, String.valueOf(resource.getId()));
		ldapTemplate.modifyAttributes(entry);
	}

	@Override
	public void synchronizeGroups(Resource resource, List<Group> assignedGroups) throws InternalErrorException {
		DirContextOperations entry = findByDN(buildDN(resource));
		List<String> groupIds = new ArrayList<String>();
		for (Group group : assignedGroups) {
			groupIds.add(String.valueOf(group.getId()));
		}
		entry.setAttributeValues(PerunAttribute.PerunAttributeNames.ldapAttrAssignedGroupId, groupIds.toArray());
		ldapTemplate.modifyAttributes(entry);
	}

	@Override
	public Name getEntryDN(String... id) {
		return LdapNameBuilder.newInstance()
				.add(PerunAttribute.PerunAttributeNames.ldapAttrPerunVoId, id[0])
				.add(PerunAttribute.PerunAttributeNames.ldapAttrPerunResourceId, id[1])
				.build();
	}

	@Override
	protected Name buildDN(Resource bean) {
		return getEntryDN(String.valueOf(bean.getVoId()), String.valueOf(bean.getId()));
	}

	@Override
	protected void mapToContext(Resource bean, DirContextOperations context) throws InternalErrorException {
		context.setAttributeValue("objectclass", PerunAttribute.PerunAttributeNames.objectClassPerunResource);
		mapToContext(bean, context, getAttributeDescriptions());
	}

}
