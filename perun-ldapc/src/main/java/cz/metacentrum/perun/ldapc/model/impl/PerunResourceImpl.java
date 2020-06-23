package cz.metacentrum.perun.ldapc.model.impl;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.ldapc.model.PerunAttribute;
import cz.metacentrum.perun.ldapc.model.PerunFacility;
import cz.metacentrum.perun.ldapc.model.PerunGroup;
import cz.metacentrum.perun.ldapc.model.PerunResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.support.LdapNameBuilder;

import javax.naming.Name;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

public class PerunResourceImpl extends AbstractPerunEntry<Resource> implements PerunResource {

	private final static Logger log = LoggerFactory.getLogger(PerunResourceImpl.class);

	@Autowired
	private PerunGroup perunGroup;
	@Autowired
	private PerunFacility perunFacility;

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
						(PerunAttribute.SingleValueExtractor<Resource>) (resource, attrs) -> resource.getName()
				),
				new PerunAttributeDesc<>(
						PerunAttribute.PerunAttributeNames.ldapAttrPerunResourceId,
						PerunAttribute.REQUIRED,
						(PerunAttribute.SingleValueExtractor<Resource>) (resource, attrs) -> String.valueOf(resource.getId())
				),
				new PerunAttributeDesc<>(
						PerunAttribute.PerunAttributeNames.ldapAttrPerunFacilityId,
						PerunAttribute.REQUIRED,
						(PerunAttribute.SingleValueExtractor<Resource>) (resource, attrs) -> String.valueOf(resource.getFacilityId())
				),
				new PerunAttributeDesc<>(
						PerunAttribute.PerunAttributeNames.ldapAttrPerunVoId,
						PerunAttribute.REQUIRED,
						(PerunAttribute.SingleValueExtractor<Resource>) (resource, attrs) -> String.valueOf(resource.getVoId())
				),
				new PerunAttributeDesc<>(
						PerunAttribute.PerunAttributeNames.ldapAttrDescription,
						PerunAttribute.OPTIONAL,
						(PerunAttribute.SingleValueExtractor<Resource>) (resource, attrs) -> resource.getDescription()
				),
				new PerunAttributeDesc<>(
						PerunAttribute.PerunAttributeNames.ldapAttrPerunFacilityDn,
						PerunAttribute.OPTIONAL,
						(PerunAttribute.SingleValueExtractor<Resource>) (resource, attrs) -> perunFacility.getEntryDN(String.valueOf(resource.getFacilityId())).toString()
								+ "," + ldapProperties.getLdapBase()
				)
		);
	}

	public void addResource(Resource resource) {
		addEntry(resource);
	}

	public void deleteResource(Resource resource) {
		deleteEntry(resource);
	}


	@Override
	public void updateResource(Resource resource) {
		modifyEntry(resource);
	}

	@Override
	public void assignGroup(Resource resource, Group group) {
		DirContextOperations entry = findByDN(buildDN(resource));
		entry.addAttributeValue(PerunAttribute.PerunAttributeNames.ldapAttrAssignedGroupId, String.valueOf(group.getId()));
		ldapTemplate.modifyAttributes(entry);
		entry = perunGroup.findById(String.valueOf(group.getVoId()), String.valueOf(group.getId()));
		entry.addAttributeValue(PerunAttribute.PerunAttributeNames.ldapAttrAssignedToResourceId, String.valueOf(resource.getId()));
		ldapTemplate.modifyAttributes(entry);
	}

	@Override
	public void removeGroup(Resource resource, Group group) {
		DirContextOperations entry = findByDN(buildDN(resource));
		entry.removeAttributeValue(PerunAttribute.PerunAttributeNames.ldapAttrAssignedGroupId, String.valueOf(group.getId()));
		ldapTemplate.modifyAttributes(entry);
		entry = perunGroup.findById(String.valueOf(group.getVoId()), String.valueOf(group.getId()));
		entry.removeAttributeValue(PerunAttribute.PerunAttributeNames.ldapAttrAssignedToResourceId, String.valueOf(resource.getId()));
		ldapTemplate.modifyAttributes(entry);
	}

	protected void doSynchronizeGroups(DirContextOperations entry, List<Group> assignedGroups) {
		List<String> groupIds = new ArrayList<String>();
		for (Group group : assignedGroups) {
			groupIds.add(String.valueOf(group.getId()));
		}
		entry.setAttributeValues(PerunAttribute.PerunAttributeNames.ldapAttrAssignedGroupId, groupIds.toArray());
	}

	@Override
	public void synchronizeResource(Resource resource, Iterable<Attribute> attrs, List<Group> assignedGroups) {
		SyncOperation syncOp = beginSynchronizeEntry(resource, attrs);
		doSynchronizeGroups(syncOp.getEntry(), assignedGroups);
		commitSyncOperation(syncOp);
	}

	@Override
	public void synchronizeGroups(Resource resource, List<Group> assignedGroups) {
		DirContextOperations entry = findByDN(buildDN(resource));
		doSynchronizeGroups(entry, assignedGroups);
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
	protected void mapToContext(Resource bean, DirContextOperations context) {
		context.setAttributeValue("objectclass", PerunAttribute.PerunAttributeNames.objectClassPerunResource);
		mapToContext(bean, context, getAttributeDescriptions());
	}

	@Override
	public List<Name> listEntries() {
		return ldapTemplate.search(query().
						where("objectclass").is(PerunAttribute.PerunAttributeNames.objectClassPerunResource),
				getNameMapper());
	}

}
