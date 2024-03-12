package cz.metacentrum.perun.ldapc.model.impl;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.ldapc.model.PerunAttribute;
import cz.metacentrum.perun.ldapc.model.PerunFacility;
import java.util.Arrays;
import java.util.List;
import javax.naming.Name;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.support.LdapNameBuilder;

public class PerunFacilityImpl extends AbstractPerunEntry<Facility> implements PerunFacility {

  @Override
  public void addFacility(Facility facility) {
    addEntry(facility);
  }

  @Override
  protected Name buildDN(Facility bean) {
    return getEntryDN(String.valueOf(bean.getId()));
  }

  @Override
  public void deleteFacility(Facility facility) {
    deleteEntry(facility);
  }

  @Override
  protected List<PerunAttribute<Facility>> getDefaultAttributeDescriptions() {
    return Arrays.asList(
        new PerunAttributeDesc<>(PerunAttribute.PerunAttributeNames.LDAP_ATTR_COMMON_NAME, PerunAttribute.REQUIRED,
            (PerunAttribute.SingleValueExtractor<Facility>) (facility, attrs) -> facility.getName()),
        new PerunAttributeDesc<>(PerunAttribute.PerunAttributeNames.LDAP_ATTR_PERUN_FACILITY_ID,
            PerunAttribute.REQUIRED,
            (PerunAttribute.SingleValueExtractor<Facility>) (facility, attrs) -> String.valueOf(facility.getId())),
        new PerunAttributeDesc<>(PerunAttribute.PerunAttributeNames.LDAP_ATTR_DESCRIPTION, PerunAttribute.OPTIONAL,
            (PerunAttribute.SingleValueExtractor<Facility>) (facility, attrs) -> facility.getDescription()));
  }

  @Override
  protected List<String> getDefaultUpdatableAttributes() {
    return Arrays.asList(PerunAttribute.PerunAttributeNames.LDAP_ATTR_COMMON_NAME,
        PerunAttribute.PerunAttributeNames.LDAP_ATTR_DESCRIPTION);
  }

  @Override
  public Name getEntryDN(String... id) {
    return LdapNameBuilder.newInstance().add(PerunAttribute.PerunAttributeNames.LDAP_ATTR_PERUN_FACILITY_ID, id[0])
        .build();
  }

  @Override
  public List<Name> listEntries() {
    return ldapTemplate.search(
        query().where("objectclass").is(PerunAttribute.PerunAttributeNames.OBJECT_CLASS_PERUN_FACILITY),
        getNameMapper());
  }

  @Override
  protected void mapToContext(Facility bean, DirContextOperations context) {
    context.setAttributeValue("objectclass", PerunAttribute.PerunAttributeNames.OBJECT_CLASS_PERUN_FACILITY);
    mapToContext(bean, context, getAttributeDescriptions());
  }

  @Override
  public void synchronizeFacility(Facility facility, Iterable<Attribute> attrs) {
    synchronizeEntry(facility, attrs);
  }

  @Override
  public void updateFacility(Facility facility) {
    modifyEntry(facility);
  }


}
