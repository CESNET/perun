package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.FacilityAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.FacilityAttributesModuleImplApi;

/**
 * Created by Oliver Mrázik on 3. 7. 2014.
 * author: Oliver Mrázik
 * version: 2014-07-03
 */
public class urn_perun_facility_attribute_def_def_ldapBaseDN extends FacilityAttributesModuleAbstract implements FacilityAttributesModuleImplApi {

	@Override
	public void checkAttributeValue(PerunSessionImpl perunSession, Facility facility, Attribute attribute) throws WrongAttributeValueException {

		if (attribute.getValue() == null) {
			throw new WrongAttributeValueException(attribute, facility, "attribute is null");
		}

		String value = (String) attribute.getValue();
		if (value.length() < 3) {
			throw new WrongAttributeValueException(attribute, facility, "attribute has to start with \"ou=\" or \"dc=\"");
		}

		String sub = value.substring(0,3);

		if ( !(sub.equalsIgnoreCase("ou=") || sub.equalsIgnoreCase("dc=")) ) {
			throw new WrongAttributeValueException(attribute, facility, "attribute has to start with \"ou=\" or \"dc=\"");
		}
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_FACILITY_ATTR_DEF);
		attr.setFriendlyName("ldapBaseDN");
		attr.setDisplayName("LDAP base DN");
		attr.setType(String.class.getName());
		attr.setDescription("Base part of DN, which will be used for all entities propagated to facility. Should be like \"ou=sth,dc=example,dc=domain\" (without quotes)");
		return attr;
	}
}
