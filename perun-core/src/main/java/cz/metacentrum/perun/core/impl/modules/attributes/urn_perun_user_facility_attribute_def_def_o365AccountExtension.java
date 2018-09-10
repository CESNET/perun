package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.FacilityUserAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.FacilityUserAttributesModuleImplApi;

import java.text.ParseException;
import java.util.Date;

/**
 *
 * @author V. Mecko vladimir.mecko@gmail.com
 */
public class urn_perun_user_facility_attribute_def_def_o365AccountExtension extends FacilityUserAttributesModuleAbstract implements FacilityUserAttributesModuleImplApi {

	@Override
	public void checkAttributeValue(PerunSessionImpl perunSession, Facility facility, User user, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
		String o365AccExtTime = (String) attribute.getValue();

		if(o365AccExtTime == null) return; //null is allowed value
		Date testDate = null;
		try {
			testDate = BeansUtils.getDateFormatterWithoutTime().parse(o365AccExtTime);
		} catch (ParseException ex) {
			throw new WrongAttributeValueException(attribute, "Date parsing failed", ex);
		}

		if (!BeansUtils.getDateFormatterWithoutTime().format(testDate).equals(o365AccExtTime)) {
			throw new WrongAttributeValueException(attribute, "Wrong format, yyyy-mm-dd expected.");
		}
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_FACILITY_ATTR_DEF);
		attr.setFriendlyName("o365AccountExtension");
		attr.setDisplayName("o365 Account Extension");
		attr.setType(String.class.getName());
		attr.setDescription("Expiration date of manually extended account in o365 (format yyyy-mm-dd).");
		return attr;
	}
}
