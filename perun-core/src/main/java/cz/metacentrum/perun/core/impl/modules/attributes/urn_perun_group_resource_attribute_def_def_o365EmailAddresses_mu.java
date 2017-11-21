package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupResourceMismatchException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.AttributesManagerBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceGroupAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceGroupAttributesModuleImplApi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;

import static cz.metacentrum.perun.core.impl.Utils.emailPattern;
import static cz.metacentrum.perun.core.impl.Utils.hasDuplicate;

/**
 * Module for email addresses for Office365 at Masaryk University.
 * Implements checks for attribute urn:perun:group-resource:attribute-def:def:o365EmailAddresses_mu.
 * <p>
 * Requirements:
 * <ul>
 * <li>type is list</li>
 * <li>all values are email addresses</li>
 * <li>must contain at least one value if urn:perun:group-resource:attribute-def:def:adName is set</li>
 * <li>no duplicates among the list values</li>
 * <li>no duplicates among all values of this attribute and all values of
 * attribute urn:perun:member:attribute-def:def:o365EmailAddresses:mu</li>
 * </ul>
 *
 * @author Martin Kuba &lt;makub@ics.muni.cz>
 */
@SuppressWarnings("unused")
public class urn_perun_group_resource_attribute_def_def_o365EmailAddresses_mu extends ResourceGroupAttributesModuleAbstract implements ResourceGroupAttributesModuleImplApi {

	private static final String NAMESPACE = AttributesManager.NS_GROUP_RESOURCE_ATTR_DEF;
	static final String ADNAME_ATTRIBUTE = NAMESPACE + ":adName";

	@Override
	public void checkAttributeValue(PerunSessionImpl sess, Resource resource, Group group, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
		List<String> emails;
		//get values
		Object value = attribute.getValue();
		if (value == null) {
			emails = Collections.emptyList();
		} else if (value instanceof List) {
			//noinspection unchecked
			emails = (List<String>) value;
		} else {
			throw new WrongAttributeValueException(attribute, resource, group, "is of type " + value.getClass() + ", but should be ArrayList");
		}
		
		//check syntax of all values
		for (String email : emails) {
			Matcher emailMatcher = emailPattern.matcher(email);
			if (!emailMatcher.matches())
				throw new WrongAttributeValueException(attribute, resource, group, "Email " + email + " is not in correct form.");
		}

		//at least one value if adName is set
		AttributesManagerBl am = sess.getPerunBl().getAttributesManagerBl();
		try {
			Object adName = am.getAttribute(sess, resource, group, ADNAME_ATTRIBUTE).getValue();
			if (adName != null && emails.isEmpty()) {
				throw new WrongAttributeValueException(attribute, resource, group, "at least one email must be defined");
			}
		} catch (AttributeNotExistsException | GroupResourceMismatchException e) {
			throw new InternalErrorException(e.getMessage(), e);
		}

		//check for duplicities
		if (hasDuplicate(emails)) {
			throw new WrongAttributeValueException(attribute, resource, group, "duplicate values");
		}

		//check for duplicities among all members and groups
		//TODO not implemented yet
	}

	/**
	 * Prefills value created by joining value of urn:perun:group_resource:attribute-def:def:adName with "@group.muni.cz"
	 */
	@Override
	public Attribute fillAttribute(PerunSessionImpl sess, Resource resource, Group group, AttributeDefinition attrDef) throws InternalErrorException, WrongAttributeAssignmentException {
		if (!NAMESPACE.equals(attrDef.getNamespace())) throw new WrongAttributeAssignmentException(attrDef);
		try {
			Attribute result = new Attribute(attrDef);
			Object adName = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, resource, group, ADNAME_ATTRIBUTE).getValue();
			if (adName != null && adName instanceof String) {
				result.setValue(adName + "@group.muni.cz");
			}
			return result;
		} catch (GroupResourceMismatchException | AttributeNotExistsException e) {
			throw new InternalErrorException(e.getMessage(), e);
		}
	}


	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(NAMESPACE);
		attr.setFriendlyName("o365EmailAddresses:mu");
		attr.setDisplayName("MU O365 email addresses");
		attr.setType(ArrayList.class.getName());
		attr.setDescription("Email address for Office365 at Masaryk University");
		return attr;
	}
}
