package cz.metacentrum.perun.core.impl.modules.attributes;

import java.util.ArrayList;
import java.util.List;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserFacilityAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserFacilityAttributesModuleImplApi;

/**
 * Checks and fills shell for a specified user at the particular facility.
 *
 * @date 28.4.2011 20:51:05
 * @author Lukáš Pravda   <luky.pravda@gmail.com>
 */
public class urn_perun_user_facility_attribute_def_def_shell extends UserFacilityAttributesModuleAbstract implements UserFacilityAttributesModuleImplApi {

	/**
	 * Checks an attribute with shell for the user at the specified facility. There
	 * must be at least some facilities allowed and also the user must have
	 * an account there. In that case the new user's shell must be included
	 * in allowed shells and also need to have correct format.
	 */
	@Override
	public void checkAttributeValue(PerunSessionImpl session, User user, Facility facility, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
		String shell = (String) attribute.getValue();

		if (shell == null) return;

		session.getPerunBl().getModulesUtilsBl().checkFormatOfShell(shell, attribute);

		List<String> allowedShells = allShellsAtSpecifiedFacility(session, facility, user);

		if (allowedShells.isEmpty()) {
			throw new WrongReferenceAttributeValueException(attribute, null, user, facility, "There are no shells available at associated facilities");
		}

		if (!allowedShells.contains(shell)) {
			throw new WrongAttributeValueException(attribute, user, facility, "Such shell is not allowed at specified facility for the user.");
		}
	}

	/**
	 * Internal method for getting all allowed shells at specified facility
	 */
	private List<String> allShellsAtSpecifiedFacility(PerunSessionImpl session, Facility facility, User user) throws InternalErrorException, WrongAttributeAssignmentException {
		List<Resource> availableResources;
		availableResources = session.getPerunBl().getUsersManagerBl().getAllowedResources(session, facility, user);
		List<String> allowedShells = new ArrayList<>();

		for (Resource r : availableResources) {
			Attribute resourceAttr;
			try {
				resourceAttr = session.getPerunBl().getAttributesManagerBl().getAttribute(session, r, AttributesManager.NS_RESOURCE_ATTR_DEF + ":shells");
			} catch (AttributeNotExistsException ex) {
				throw new InternalErrorException("Attribute with all shells of facility " + facility.getId() + " could not be obtained", ex);
			}
			if (resourceAttr.getValue() != null) {
				allowedShells.addAll(((List<String>) resourceAttr.getValue()));
			}
		}
		return allowedShells;
	}

	@Override
	public List<String> getDependencies() {
		List<String> dependencies = new ArrayList<>();
		dependencies.add(AttributesManager.NS_RESOURCE_ATTR_DEF + ":shells");
		return dependencies;
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_FACILITY_ATTR_DEF);
		attr.setFriendlyName("shell");
		attr.setDisplayName("Shell");
		attr.setType(String.class.getName());
		attr.setDescription("Shell.");
		return attr;
	}
}
