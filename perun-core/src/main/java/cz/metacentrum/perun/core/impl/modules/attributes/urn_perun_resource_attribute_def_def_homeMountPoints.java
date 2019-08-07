package cz.metacentrum.perun.core.impl.modules.attributes;

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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Milan Halenar <255818@mail.muni.cz>
 * @date 27.4.2011
 */
public class urn_perun_resource_attribute_def_def_homeMountPoints extends ResourceAttributesModuleAbstract implements ResourceAttributesModuleImplApi {

	private static final String A_F_homeMountPoints = AttributesManager.NS_FACILITY_ATTR_DEF + ":homeMountPoints";
	private static final Pattern pattern = Pattern.compile("^/[-a-zA-Z.0-9_/]*$");

	/**
	 * Fill with attribute from underlying facility
	 * @param perunSession
	 * @param resource
	 * @param attribute
	 * @return
	 * @throws InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 */
	@Override
	public Attribute fillAttribute(PerunSessionImpl perunSession, Resource resource, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException {
		Facility facility = perunSession.getPerunBl().getResourcesManagerBl().getFacility(perunSession, resource);

		Attribute facilityAttr;
		try {
			facilityAttr = perunSession.getPerunBl().getAttributesManagerBl().getAttribute(perunSession, facility, A_F_homeMountPoints);
		} catch (AttributeNotExistsException ex) {
			throw new InternalErrorException("Attribute which is essentials for fill the value of checked attribute doesn't exists.", ex);
		}
		Attribute toReturn = new Attribute(attribute);
		toReturn.setValue(facilityAttr.getValue());
		return toReturn;
	}
	/**
	 * Allows only homeMountPoints which are contained in underlying facility
	 * @param perunSession
	 * @param resource
	 * @param attribute
	 * @throws InternalErrorException
	 * @throws WrongAttributeValueException
	 * @throws WrongReferenceAttributeValueException
	 * @throws WrongAttributeAssignmentException
	 */
	@Override
	public void checkAttributeSemantics(PerunSessionImpl perunSession, Resource resource, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
		if (attribute.getValue() == null) {
			throw new WrongAttributeValueException(attribute);
		}
		Facility facility = perunSession.getPerunBl().getResourcesManagerBl().getFacility(perunSession, resource);

		Attribute facilityAttr;
		try {
			facilityAttr = perunSession.getPerunBl().getAttributesManagerBl().getAttribute(perunSession, facility, A_F_homeMountPoints);
		} catch (AttributeNotExistsException ex) {
			throw new InternalErrorException(ex);
		}

		if(facilityAttr.getValue() == null) throw new WrongReferenceAttributeValueException(attribute, facilityAttr, "Reference attribute have null value.");

		if (!((List<String>) facilityAttr.getValue()).containsAll((List<String>) attribute.getValue())) {
			throw new WrongAttributeValueException(attribute);
		}
		List<String> homeMountPoints = (List<String>) attribute.getValue();
		if (!homeMountPoints.isEmpty()) {
			for (String st : homeMountPoints) {
				Matcher match = pattern.matcher(st);
				if (!match.matches()) {
					throw new WrongAttributeValueException(attribute, "Bad homeMountPoints attribute format " + st);
				}
			}
		}
	}

	@Override
	public List<String> getDependencies() {
		List<String> dependecies = new ArrayList<>();
		dependecies.add(A_F_homeMountPoints);
		return dependecies;
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_RESOURCE_ATTR_DEF);
		attr.setFriendlyName("homeMountPoints");
		attr.setDisplayName("Home mount points");
		attr.setType(ArrayList.class.getName());
		attr.setDescription("All available home mount points.");
		return attr;
	}
}
