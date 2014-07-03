package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.implApi.modules.attributes.FacilityAttributesModuleImplApi;
import org.junit.Test;

/**
 * Created by Oliver Mrázik on 3. 7. 2014.
 * author: Oliver Mrázik
 * version: 2014-07-03
 */
public class urn_perun_facility_attribute_def_def_ldapBaseDNTest {

	FacilityAttributesModuleImplApi module = new urn_perun_facility_attribute_def_def_ldapBaseDN();

	@Test
	public void testCheckAttributeValue() throws Exception {
		Attribute attribute = new Attribute(module.getAttributeDefinition());
		attribute.setValue("dc=example,dc=domain");

		module.checkAttributeValue(null, null, attribute);

		attribute.setValue("ou=example,dc=domain");

		module.checkAttributeValue(null, new Facility(), attribute);
	}

	@Test(expected = WrongAttributeValueException.class)
	public void testCheckAttributeValueEmptyString() throws Exception {
		Attribute attribute = new Attribute(module.getAttributeDefinition());
		attribute.setValue("");

		module.checkAttributeValue(null, new Facility(), attribute);
	}

	@Test(expected = WrongAttributeValueException.class)
	public void testCheckAttributeValueFailsLessChars() throws Exception {
		Attribute attribute = new Attribute(module.getAttributeDefinition());
		attribute.setValue("ou");

		module.checkAttributeValue(null, new Facility(), attribute);
	}

	@Test(expected = WrongAttributeValueException.class)
	public void testCheckAttributeValueWrongChars() throws Exception {
		Attribute attribute = new Attribute(module.getAttributeDefinition());
		attribute.setValue("cn=example,dc=domain");

		module.checkAttributeValue(null, new Facility(), attribute);
	}
}
