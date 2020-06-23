package cz.metacentrum.perun.ldapc.model.impl;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.ldapc.model.PerunAttribute;
import cz.metacentrum.perun.ldapc.model.PerunFacility;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.support.LdapNameBuilder;

import javax.naming.Name;
import java.util.Arrays;
import java.util.List;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

public class PerunFacilityImpl extends AbstractPerunEntry<Facility> implements PerunFacility {

	@Override
	protected List<String> getDefaultUpdatableAttributes() {
		return Arrays.asList(
				PerunAttribute.PerunAttributeNames.ldapAttrCommonName,
				PerunAttribute.PerunAttributeNames.ldapAttrDescription);
	}

	@Override
	protected List<PerunAttribute<Facility>> getDefaultAttributeDescriptions() {
		return Arrays.asList(
				new PerunAttributeDesc<>(
						PerunAttribute.PerunAttributeNames.ldapAttrCommonName,
						PerunAttribute.REQUIRED,
						(PerunAttribute.SingleValueExtractor<Facility>) (facility, attrs) -> facility.getName()
				),
				new PerunAttributeDesc<>(
						PerunAttribute.PerunAttributeNames.ldapAttrPerunFacilityId,
						PerunAttribute.REQUIRED,
						(PerunAttribute.SingleValueExtractor<Facility>) (facility, attrs) -> String.valueOf(facility.getId())
				),
				new PerunAttributeDesc<>(
						PerunAttribute.PerunAttributeNames.ldapAttrDescription,
						PerunAttribute.OPTIONAL,
						(PerunAttribute.SingleValueExtractor<Facility>) (facility, attrs) -> facility.getDescription()
				)
		);
	}

	@Override
	public void addFacility(Facility facility) {
		addEntry(facility);
	}

	@Override
	public void deleteFacility(Facility facility) {
		deleteEntry(facility);
	}

	@Override
	public void updateFacility(Facility facility) {
		modifyEntry(facility);
	}

	@Override
	public void synchronizeFacility(Facility facility, Iterable<Attribute> attrs) {
		synchronizeEntry(facility, attrs);
	}

	@Override
	public Name getEntryDN(String... id) {
		return LdapNameBuilder.newInstance()
				.add(PerunAttribute.PerunAttributeNames.ldapAttrPerunFacilityId, id[0])
				.build();
	}

	@Override
	protected Name buildDN(Facility bean) {
		return getEntryDN(String.valueOf(bean.getId()));
	}

	@Override
	protected void mapToContext(Facility bean, DirContextOperations context) {
		context.setAttributeValue("objectclass", PerunAttribute.PerunAttributeNames.objectClassPerunFacility);
		mapToContext(bean, context, getAttributeDescriptions());
	}

	@Override
	public List<Name> listEntries() {
		return ldapTemplate.search(query().
						where("objectclass").is(PerunAttribute.PerunAttributeNames.objectClassPerunFacility),
				getNameMapper());
	}


}
