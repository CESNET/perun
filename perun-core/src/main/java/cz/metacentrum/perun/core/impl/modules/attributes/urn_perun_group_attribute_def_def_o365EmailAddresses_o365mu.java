package cz.metacentrum.perun.core.impl.modules.attributes;

import com.google.common.collect.Lists;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.AttributesManagerBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupAttributesModuleImplApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static cz.metacentrum.perun.core.impl.Utils.emailPattern;
import static cz.metacentrum.perun.core.impl.Utils.hasDuplicate;
import static cz.metacentrum.perun.core.impl.Utils.ucoEmailPattern;

/**
 * Module for email addresses for Office365 at Masaryk University.
 * Implements checks for attribute urn:perun:group:attribute-def:def:o365EmailAddresses:o365mu.
 * <p>
 * Requirements:
 * <ul>
 * <li>type is list</li>
 * <li>all values are email addresses</li>
 * <li>must contain at least one value if urn:perun:group:attribute-def:def:adName:o365mu is set</li>
 * <li>no uco based emails among the list values</li>
 * <li>no duplicates among the list values</li>
 * <li>no duplicates among all values of this attribute and all values of
 * attribute urn:perun:member:attribute-def:def:o365EmailAddresses:mu</li>
 * </ul>
 *
 * @author Michal Stava &lt;stavamichal@gmail.com&gt;
 */
public class urn_perun_group_attribute_def_def_o365EmailAddresses_o365mu extends GroupAttributesModuleAbstract implements GroupAttributesModuleImplApi {

	private final static Logger log = LoggerFactory.getLogger(urn_perun_group_attribute_def_def_o365EmailAddresses_o365mu.class);
	static final String ADNAME_ATTRIBUTE = AttributesManager.NS_GROUP_ATTR_DEF + ":adName:o365mu";
	static final String USER_O365EMAIL_ADDRESSES_MU_ATTRIBUTE = AttributesManager.NS_USER_ATTR_DEF + ":o365UserEmailAddresses:mu";

	@Override
	public void checkAttributeSyntax(PerunSessionImpl sess, Group group, Attribute attribute) throws WrongAttributeValueException {
		log.trace("checkAttributeSyntax(group={},attribute={})", group, attribute);
		List<String> emails = attribute.valueAsList();

		if (emails == null) return;

		//check syntax of all values
		for (String email : emails) {
			Matcher emailMatcher = emailPattern.matcher(email);
			if (!emailMatcher.matches())
				throw new WrongAttributeValueException(attribute, group, "Email " + email + " is not in correct form.");
			Matcher ucoEmailMatcher = ucoEmailPattern.matcher(email);
			if (ucoEmailMatcher.matches())
				throw new WrongAttributeValueException(attribute, group, "Email " + email + " is based on UCO which is not supported.");
		}

		//check for duplicities
		if (hasDuplicate(emails)) {
			throw new WrongAttributeValueException(attribute, group, "duplicate values");
		}
	}

	@Override
	public void checkAttributeSemantics(PerunSessionImpl sess, Group group, Attribute attribute) throws WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
		List<String> emails = attribute.valueAsList();

		if (emails == null) emails = new ArrayList<>();
		//at least one value if adName is set
		AttributesManagerBl am = sess.getPerunBl().getAttributesManagerBl();
		try {
			if (emails.isEmpty()) {
				Attribute adName = am.getAttribute(sess, group, ADNAME_ATTRIBUTE);
				if (adName.getValue() != null) {
					throw new WrongReferenceAttributeValueException(attribute, adName, group, null, group, null, "at least one email must be defined");
				}

				return;
			}
		} catch (AttributeNotExistsException e) {
			throw new InternalErrorException(e.getMessage(), e);
		}

		//No need to check duplicities among other groups (attribute is unique)
		//check for duplicities among users attributes and this one
		try {
			AttributesManagerBl attributesManagerBl = sess.getPerunBl().getAttributesManagerBl();
			Attribute userO365EmailAddresses = new Attribute(sess.getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, USER_O365EMAIL_ADDRESSES_MU_ATTRIBUTE));
			userO365EmailAddresses.setValue(emails);
			Set<Pair<Integer, Integer>> usersPairs = attributesManagerBl.getPerunBeanIdsForUniqueAttributeValue(sess, userO365EmailAddresses);
			if (!usersPairs.isEmpty()) {
				throw new WrongReferenceAttributeValueException(attribute, userO365EmailAddresses, group, null, "member " + BeansUtils.getSingleId(usersPairs) + " ");
			}
		} catch(AttributeNotExistsException ex) {
			//If attribute not exists, we can log it and skip it, because there are no duplicates in not existing attributes
			log.debug("Attribute {} not exists to check duplicities in it while checkAttributeSemantics for {}.", USER_O365EMAIL_ADDRESSES_MU_ATTRIBUTE, attribute);
		}
	}
	/**
	 * Prefills value created by joining value of urn:perun:groupe:attribute-def:def:adName:o365mu with "@group.muni.cz"
	 */
	@Override
	public Attribute fillAttribute(PerunSessionImpl sess, Group group, AttributeDefinition attrDef) throws WrongAttributeAssignmentException {
		if (!AttributesManager.NS_GROUP_ATTR_DEF.equals(attrDef.getNamespace())) throw new WrongAttributeAssignmentException(attrDef);
		try {
			Attribute result = new Attribute(attrDef);
			String adName = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, group, ADNAME_ATTRIBUTE).valueAsString();
			if (adName != null) {
				result.setValue(Lists.newArrayList(adName + "@group.muni.cz"));
			}
			return result;
		} catch (AttributeNotExistsException e) {
			throw new InternalErrorException(e.getMessage(), e);
		}
	}

	@Override
	public List<String> getDependencies() {
		List<String> dependencies = new ArrayList<>();
		dependencies.add(ADNAME_ATTRIBUTE);
		dependencies.add(USER_O365EMAIL_ADDRESSES_MU_ATTRIBUTE);
		return dependencies;
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_GROUP_ATTR_DEF);
		attr.setFriendlyName("o365EmailAddresses:o365mu");
		attr.setDisplayName("MU O365 email addresses in o365mu");
		attr.setType(ArrayList.class.getName());
		attr.setUnique(true);
		attr.setDescription("Email address for Office365 at Masaryk University in o365mu namespace");
		return attr;
	}
}
