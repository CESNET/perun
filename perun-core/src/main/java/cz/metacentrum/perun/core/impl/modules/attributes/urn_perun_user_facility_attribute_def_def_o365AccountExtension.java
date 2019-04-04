package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserFacilityAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserFacilityAttributesModuleImplApi;

import java.text.ParseException;
import java.util.Date;

/**
 *
 * @author V. Mecko vladimir.mecko@gmail.com
 */
public class urn_perun_user_facility_attribute_def_def_o365AccountExtension extends UserFacilityAttributesModuleAbstract implements UserFacilityAttributesModuleImplApi {

	@Override
	public void checkAttributeValue(PerunSessionImpl perunSession, User user, Facility facility, Attribute attribute) throws WrongAttributeValueException {
		String o365AccExtTime = (String) attribute.getValue();

		if(o365AccExtTime == null) return; //null is allowed value
		Date testDate;
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
