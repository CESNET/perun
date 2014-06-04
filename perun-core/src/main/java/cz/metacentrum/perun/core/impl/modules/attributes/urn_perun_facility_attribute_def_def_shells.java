package cz.metacentrum.perun.core.impl.modules.attributes;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.FacilityAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.FacilityAttributesModuleImplApi;

/**
 * Checks if all the shells at specified facility are in proper format.
 *
 * @date 21.4.2011 9:44:49
 * @author Lukáš Pravda   <luky.pravda@gmail.com>
 */
public class urn_perun_facility_attribute_def_def_shells extends FacilityAttributesModuleAbstract implements FacilityAttributesModuleImplApi  {

	@Override
	/**
	 * Checks if the facility has properly set shells. There must be at least one
	 * shell per facility which must match regular expression
	 * e.g. corretct unix path.
	 */
	public void checkAttributeValue(PerunSessionImpl perunSession, Facility facility, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		List<String> shells = (List<String>) attribute.getValue();

		if (shells == null) {
			throw new WrongAttributeValueException(attribute, "This attribute cannot be null.");
		}

		if (!shells.isEmpty()) {
			for (String st : shells) {
				perunSession.getPerunBl().getModulesUtilsBl().checkFormatOfShell(st, attribute);
			}
		} else {
			throw new WrongAttributeValueException(attribute);
		}
	}

	@Override
	/**
	 * Method for filling shells at specified facility is not implemented yet.
	 * Probably it will not be neccessary.
	 */
	public Attribute fillAttribute(PerunSessionImpl session, Facility facility, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException {
		return new Attribute(attribute);
	}

	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_FACILITY_ATTR_DEF);
		attr.setFriendlyName("shells");
		attr.setDisplayName("Available shells");
		attr.setType(ArrayList.class.getName());
		attr.setDescription("All available shells");
		return attr;
	}

}
