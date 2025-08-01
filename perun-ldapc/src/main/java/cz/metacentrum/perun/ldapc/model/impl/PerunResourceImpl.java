package cz.metacentrum.perun.ldapc.model.impl;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.ldapc.model.PerunAttribute;
import cz.metacentrum.perun.ldapc.model.PerunFacility;
import cz.metacentrum.perun.ldapc.model.PerunGroup;
import cz.metacentrum.perun.ldapc.model.PerunResource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.naming.Name;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.support.LdapNameBuilder;

/**
 * Provides implementation of operations to modify the Resource entities in the LDAP directory.
 */
public class PerunResourceImpl extends AbstractPerunEntry<Resource> implements PerunResource {

  private static final Logger LOG = LoggerFactory.getLogger(PerunResourceImpl.class);

  @Autowired
  private PerunGroup perunGroup;
  @Autowired
  private PerunFacility perunFacility;

  public void addResource(Resource resource) {
    addEntry(resource);
  }

  @Override
  public void assignGroup(Resource resource, Group group) {
    DirContextOperations entry = findByDN(buildDN(resource));
    entry.addAttributeValue(PerunAttribute.PerunAttributeNames.LDAP_ATTR_ASSIGNED_GROUP_ID,
        String.valueOf(group.getId()));
    ldapTemplate.modifyAttributes(entry);
    entry = perunGroup.findById(String.valueOf(group.getVoId()), String.valueOf(group.getId()));
    entry.addAttributeValue(PerunAttribute.PerunAttributeNames.LDAP_ATTR_ASSIGNED_TO_RESOURCE_ID,
        String.valueOf(resource.getId()));
    ldapTemplate.modifyAttributes(entry);
  }

  @Override
  protected Name buildDN(Resource bean) {
    return getEntryDN(String.valueOf(bean.getVoId()), String.valueOf(bean.getId()));
  }

  public void deleteResource(Resource resource) {
    deleteEntry(resource);
  }

  protected void doSynchronizeGroups(DirContextOperations entry, List<Group> assignedGroups) {
    List<String> groupIds = new ArrayList<String>();
    for (Group group : assignedGroups) {
      groupIds.add(String.valueOf(group.getId()));
    }
    entry.setAttributeValues(PerunAttribute.PerunAttributeNames.LDAP_ATTR_ASSIGNED_GROUP_ID, groupIds.toArray());
  }

  @Override
  protected List<PerunAttribute<Resource>> getDefaultAttributeDescriptions() {
    return Arrays.asList(
        new PerunAttributeDesc<>(PerunAttribute.PerunAttributeNames.LDAP_ATTR_COMMON_NAME, PerunAttribute.REQUIRED,
            (PerunAttribute.SingleValueExtractor<Resource>) (resource, attrs) -> resource.getName()),
        new PerunAttributeDesc<>(PerunAttribute.PerunAttributeNames.LDAP_ATTR_PERUN_RESOURCE_ID,
            PerunAttribute.REQUIRED,
            (PerunAttribute.SingleValueExtractor<Resource>) (resource, attrs) -> String.valueOf(resource.getId())),
        new PerunAttributeDesc<>(PerunAttribute.PerunAttributeNames.LDAP_ATTR_PERUN_FACILITY_ID,
            PerunAttribute.REQUIRED,
            (PerunAttribute.SingleValueExtractor<Resource>) (resource, attrs) -> String.valueOf(
                resource.getFacilityId())),
        new PerunAttributeDesc<>(PerunAttribute.PerunAttributeNames.LDAP_ATTR_PERUN_VO_ID, PerunAttribute.REQUIRED,
            (PerunAttribute.SingleValueExtractor<Resource>) (resource, attrs) -> String.valueOf(resource.getVoId())),
        new PerunAttributeDesc<>(PerunAttribute.PerunAttributeNames.LDAP_ATTR_DESCRIPTION, PerunAttribute.OPTIONAL,
            (PerunAttribute.SingleValueExtractor<Resource>) (resource, attrs) -> resource.getDescription()),
        new PerunAttributeDesc<>(PerunAttribute.PerunAttributeNames.LDAP_ATTR_PERUN_FACILITY_DN,
            PerunAttribute.OPTIONAL, (PerunAttribute.SingleValueExtractor<Resource>) (resource, attrs) ->
            perunFacility.getEntryDN(String.valueOf(resource.getFacilityId())).toString() + "," +
            ldapProperties.getLdapBase()),
        new PerunAttributeDesc<>(PerunAttribute.PerunAttributeNames.LDAP_ATTR_UUID, PerunAttribute.OPTIONAL,
            (PerunAttribute.SingleValueExtractor<Resource>) (resource, attrs) -> resource.getUuid().toString()));
  }

  @Override
  protected List<String> getDefaultUpdatableAttributes() {
    return Arrays.asList(PerunAttribute.PerunAttributeNames.LDAP_ATTR_COMMON_NAME,
        PerunAttribute.PerunAttributeNames.LDAP_ATTR_DESCRIPTION);
  }

  @Override
  public Name getEntryDN(String... id) {
    return LdapNameBuilder.newInstance().add(PerunAttribute.PerunAttributeNames.LDAP_ATTR_PERUN_VO_ID, id[0])
        .add(PerunAttribute.PerunAttributeNames.LDAP_ATTR_PERUN_RESOURCE_ID, id[1]).build();
  }

  @Override
  public List<Name> listEntries() {
    return ldapTemplate.search(
        query().where("objectclass").is(PerunAttribute.PerunAttributeNames.OBJECT_CLASS_PERUN_RESOURCE),
        getNameMapper());
  }

  @Override
  protected void mapToContext(Resource bean, DirContextOperations context) {
    context.setAttributeValue("objectclass", PerunAttribute.PerunAttributeNames.OBJECT_CLASS_PERUN_RESOURCE);
    mapToContext(bean, context, getAttributeDescriptions());
  }

  @Override
  public void removeGroup(Resource resource, Group group) {
    DirContextOperations entry = findByDN(buildDN(resource));
    entry.removeAttributeValue(PerunAttribute.PerunAttributeNames.LDAP_ATTR_ASSIGNED_GROUP_ID,
        String.valueOf(group.getId()));
    ldapTemplate.modifyAttributes(entry);
    entry = perunGroup.findById(String.valueOf(group.getVoId()), String.valueOf(group.getId()));
    entry.removeAttributeValue(PerunAttribute.PerunAttributeNames.LDAP_ATTR_ASSIGNED_TO_RESOURCE_ID,
        String.valueOf(resource.getId()));
    ldapTemplate.modifyAttributes(entry);
  }

  @Override
  public void synchronizeGroups(Resource resource, List<Group> assignedGroups) {
    DirContextOperations entry = findByDN(buildDN(resource));
    doSynchronizeGroups(entry, assignedGroups);
    ldapTemplate.modifyAttributes(entry);
  }

  @Override
  public void synchronizeResource(Resource resource, Iterable<Attribute> attrs, List<Group> assignedGroups) {
    SyncOperation syncOp = beginSynchronizeEntry(resource, attrs);
    doSynchronizeGroups(syncOp.getEntry(), assignedGroups);
    commitSyncOperation(syncOp);
  }

  @Override
  public void updateResource(Resource resource) {
    modifyEntry(resource);
  }

}
