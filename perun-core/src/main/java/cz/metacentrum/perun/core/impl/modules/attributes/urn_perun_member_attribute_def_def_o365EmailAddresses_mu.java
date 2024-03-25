package cz.metacentrum.perun.core.impl.modules.attributes;

import static cz.metacentrum.perun.core.impl.Utils.EMAIL_PATTERN;
import static cz.metacentrum.perun.core.impl.Utils.hasDuplicate;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.MemberAttributesModuleAbstract;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Module for email addresses for Office365 at Masaryk University. Implements checks for attribute
 * urn:perun:member:attribute-def:def:o365EmailAddresses_mu.
 * <p>
 * Requirements:
 * <ul>
 * <li>type is list</li>
 * <li>value can be null</li>
 * <li>if not null, than all values are email addresses</li>
 * <li>no duplicates among the list values</li>
 * <li>no duplicates among all values of this attribute for all members</li>
 * </ul>
 *
 * @author Martin Kuba &lt;makub@ics.muni.cz>
 */
@SuppressWarnings("unused")
public class urn_perun_member_attribute_def_def_o365EmailAddresses_mu extends MemberAttributesModuleAbstract {

  private static final Logger LOG =
      LoggerFactory.getLogger(urn_perun_member_attribute_def_def_o365EmailAddresses_mu.class);

  private static final String NAMESPACE = AttributesManager.NS_MEMBER_ATTR_DEF;

  public void checkAttributeSyntax(PerunSessionImpl perunSession, Member member, Attribute attribute)
      throws WrongAttributeValueException {
    Object value = attribute.getValue();
    List<String> emails;

    if (value == null) {
      return;
    } else if (!(value instanceof ArrayList)) {
      throw new WrongAttributeValueException(attribute, member,
          "is of type " + value.getClass() + ", but should be ArrayList");
    } else {
      emails = attribute.valueAsList();
    }

    //check for duplicities
    if (hasDuplicate(emails)) {
      throw new WrongAttributeValueException(attribute, member, "has duplicate values");
    }

    for (String email : emails) {
      Matcher emailMatcher = EMAIL_PATTERN.matcher(email);
      if (!emailMatcher.matches()) {
        throw new WrongAttributeValueException(attribute, member, "Email " + email + " is not in correct form.");
      }
    }
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
