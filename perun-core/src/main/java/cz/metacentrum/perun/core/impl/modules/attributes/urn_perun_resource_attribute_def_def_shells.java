package cz.metacentrum.perun.core.impl.modules.attributes;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceAttributesModuleImplApi;
import javax.mail.Session;

/**
 * Checks and fills shells at specified resource
 *
 * @date 28.4.2011 14:48:04
 * @author Lukáš Pravda   <luky.pravda@gmail.com>
 */
public class urn_perun_resource_attribute_def_def_shells extends ResourceAttributesModuleAbstract implements ResourceAttributesModuleImplApi {

	private static final String A_F_shells = AttributesManager.NS_FACILITY_ATTR_DEF + ":shells";

	/**
	 * Fills the list of shells at the specified resource from facility
	 */
	@Override
	public Attribute fillAttribute(PerunSessionImpl perunSession, Resource resource, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException {
		Attribute atr = new Attribute(attribute);
		Facility facility = perunSession.getPerunBl().getResourcesManagerBl().getFacility(perunSession, resource);

		Attribute allShellsPerFacility;
		try {
			allShellsPerFacility = perunSession.getPerunBl().getAttributesManagerBl().getAttribute(perunSession, facility, A_F_shells);
		} catch (AttributeNotExistsException ex) {
			throw new InternalErrorException("Attribute with list of shells from facility " + facility.getId() +" could not obtained.",ex);
		}

		atr.setValue((List<String>) allShellsPerFacility.getValue());

		return atr;
	}

	/**
	 * Checks the attribute with all available shells from resource's facility
	 */
	@Override
	public void checkAttributeValue(PerunSessionImpl perunSession, Resource resource, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
		List<String> shells = (List<String>) attribute.getValue();

		if (shells == null) {
			throw new WrongAttributeValueException(attribute, "Attribute was not filled, therefore there is nothing to be checked.");
		}

		if (!shells.isEmpty()) {
			for (String st : shells) {
				perunSession.getPerunBl().getModulesUtilsBl().checkFormatOfShell(st, attribute);
			}
		}

		Facility facility = perunSession.getPerunBl().getResourcesManagerBl().getFacility(perunSession, resource);
		Attribute allShellsPerFacility;
		try {
			allShellsPerFacility = perunSession.getPerunBl().getAttributesManagerBl().getAttribute(perunSession, facility, A_F_shells);
		} catch (AttributeNotExistsException ex) {
			throw new InternalErrorException("Attribute with list of shells from facility " + facility.getId() +" could not obtained.",ex);
		}


		if (allShellsPerFacility.getValue() == null) {
			throw new WrongReferenceAttributeValueException(attribute, allShellsPerFacility);
		} else {
			if (!((List<String>) allShellsPerFacility.getValue()).containsAll(shells)) {
				throw new WrongAttributeValueException(attribute, "Some shells from specified resource are not at home facility " + facility.getId());
			}
		}
	}

	@Override
	public List<String> getDependencies() {
		List<String> dependecies = new ArrayList<>();
		dependecies.add(A_F_shells);
		return dependecies;
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_RESOURCE_ATTR_DEF);
		attr.setFriendlyName("shells");
		attr.setDisplayName("Available shells");
		attr.setType(ArrayList.class.getName());
		attr.setDescription("All available shells");
		return attr;
	}
}
