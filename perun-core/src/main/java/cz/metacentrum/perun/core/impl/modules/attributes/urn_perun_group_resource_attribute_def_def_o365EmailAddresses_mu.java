package cz.metacentrum.perun.core.impl.modules.attributes;

import com.google.common.collect.Lists;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupResourceMismatchException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.AttributesManagerBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupResourceAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupResourceAttributesModuleImplApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

import static cz.metacentrum.perun.core.impl.Utils.emailPattern;
import static cz.metacentrum.perun.core.impl.Utils.hasDuplicate;
import static cz.metacentrum.perun.core.impl.Utils.ucoEmailPattern;

/**
 * Module for email addresses for Office365 at Masaryk University.
 * Implements checks for attribute urn:perun:group-resource:attribute-def:def:o365EmailAddresses_mu.
 * <p>
 * Requirements:
 * <ul>
 * <li>type is list</li>
 * <li>all values are email addresses</li>
 * <li>must contain at least one value if urn:perun:group-resource:attribute-def:def:adName is set</li>
 * <li>no uco based emails among the list values</li>
 * <li>no duplicates among the list values</li>
 * <li>no duplicates among all values of this attribute and all values of
 * attribute urn:perun:member:attribute-def:def:o365EmailAddresses:mu</li>
 * </ul>
 *
 * @author Martin Kuba &lt;makub@ics.muni.cz>
 */
@SuppressWarnings("unused")
public class urn_perun_group_resource_attribute_def_def_o365EmailAddresses_mu extends GroupResourceAttributesModuleAbstract implements GroupResourceAttributesModuleImplApi {

	private final static Logger log = LoggerFactory.getLogger(urn_perun_group_resource_attribute_def_def_o365EmailAddresses_mu.class);

	private static final String NAMESPACE = AttributesManager.NS_GROUP_RESOURCE_ATTR_DEF;
	static final String ADNAME_ATTRIBUTE = NAMESPACE + ":adName";

	@Override
	public void checkAttributeSyntax(PerunSessionImpl sess, Group group, Resource resource, Attribute attribute) throws WrongAttributeValueException {
		log.trace("checkAttributeSyntax(resource={},group={},attribute={})", resource, group, attribute);
		List<String> emails = attribute.valueAsList();

		if (emails == null) return;

		//check syntax of all values
		for (String email : emails) {
			Matcher emailMatcher = emailPattern.matcher(email);
			if (!emailMatcher.matches())
				throw new WrongAttributeValueException(attribute, resource, group, "Email " + email + " is not in correct form.");
			Matcher ucoEmailMatcher = ucoEmailPattern.matcher(email);
			if (ucoEmailMatcher.matches())
				throw new WrongAttributeValueException(attribute, resource, group, "Email " + email + " is based on UCO which is not supported.");
		}

		//check for duplicities
		if (hasDuplicate(emails)) {
			throw new WrongAttributeValueException(attribute, resource, group, "duplicate values");
		}
	}

	@Override
	public void checkAttributeSemantics(PerunSessionImpl sess, Group group, Resource resource, Attribute attribute) throws WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
		List<String> emails = attribute.valueAsList();

		if (emails == null) emails = new ArrayList<>();
		//at least one value if adName is set
		AttributesManagerBl am = sess.getPerunBl().getAttributesManagerBl();
		try {
			if (emails.isEmpty()) {
				Attribute adName = am.getAttribute(sess, resource, group, ADNAME_ATTRIBUTE);
				if (adName.getValue() != null) {
					throw new WrongReferenceAttributeValueException(attribute, adName, resource, group, resource, group, "at least one email must be defined");
				}

				return;
			}
		} catch (AttributeNotExistsException | GroupResourceMismatchException e) {
			throw new InternalErrorException(e.getMessage(), e);
		}

		//check for duplicities among all members and groups
		AttributesManagerBl attributesManagerBl = sess.getPerunBl().getAttributesManagerBl();
		Set<Pair<Integer, Integer>> groupResourcePairs = attributesManagerBl.getPerunBeanIdsForUniqueAttributeValue(sess, attribute);
		groupResourcePairs.remove(new Pair<>(group.getId(), resource.getId()));
		if (!groupResourcePairs.isEmpty()) {
			throw new WrongReferenceAttributeValueException(attribute, attribute, group, resource, "some of the email addresses are already assigned to the following group_resource pairs: " + groupResourcePairs);
		}
		Attribute userO365EmailAddresses = new Attribute(new urn_perun_user_attribute_def_def_o365UserEmailAddresses_mu().getAttributeDefinition());
		userO365EmailAddresses.setValue(emails);
		Set<Pair<Integer, Integer>> userPairs = attributesManagerBl.getPerunBeanIdsForUniqueAttributeValue(sess, userO365EmailAddresses);
		if (!userPairs.isEmpty()) {
			throw new WrongReferenceAttributeValueException(attribute, userO365EmailAddresses, group, resource, "user " + BeansUtils.getSingleId(userPairs) + " ");
		}
	}

	/**
	 * Prefills value created by joining value of urn:perun:group_resource:attribute-def:def:adName with "@group.muni.cz"
	 */
	@Override
	public Attribute fillAttribute(PerunSessionImpl sess, Group group, Resource resource, AttributeDefinition attrDef) throws WrongAttributeAssignmentException {
		if (!NAMESPACE.equals(attrDef.getNamespace())) throw new WrongAttributeAssignmentException(attrDef);
		try {
			Attribute result = new Attribute(attrDef);
			Object adName = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, resource, group, ADNAME_ATTRIBUTE).getValue();
			if (adName != null && adName instanceof String) {
				result.setValue(Lists.newArrayList(adName + "@group.muni.cz"));
			}
			return result;
		} catch (GroupResourceMismatchException | AttributeNotExistsException e) {
			throw new InternalErrorException(e.getMessage(), e);
		}
	}

	@Override
	public List<String> getDependencies() {
		List<String> dependencies = new ArrayList<>();
		dependencies.add(ADNAME_ATTRIBUTE);
		dependencies.add(new urn_perun_user_attribute_def_def_o365UserEmailAddresses_mu().getAttributeDefinition().getName());
		return dependencies;
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(NAMESPACE);
		attr.setFriendlyName("o365EmailAddresses:mu");
		attr.setDisplayName("MU O365 email addresses");
		attr.setType(ArrayList.class.getName());
		attr.setUnique(true);
		attr.setDescription("Email address for Office365 at Masaryk University");
		return attr;
	}
}
