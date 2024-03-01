package cz.metacentrum.perun.core.impl.modules.attributes;

import static cz.metacentrum.perun.core.impl.Utils.EMAIL_PATTERN;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.impl.modules.ModulesConfigLoader;
import cz.metacentrum.perun.core.impl.modules.ModulesYamlConfigLoader;
import cz.metacentrum.perun.core.implApi.modules.attributes.MemberVirtualAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.MemberVirtualAttributesModuleImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.SkipValueCheckDuringDependencyCheck;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Returns list of member's mails, by default preferredMail and memberMail. Returned attributes are configurable,
 * supports string and list values of member or user attributes with email format.
 * <p>
 * Example configuration:
 * <p>
 * attributeNames: - urn:perun:user:attribute-def:def:preferredMail - urn:perun:member:attribute-def:def:mail
 *
 * @author Johana Supikova <xsupikova@fi.muni.cz>
 */
@SkipValueCheckDuringDependencyCheck
public class urn_perun_member_attribute_def_virt_mails extends MemberVirtualAttributesModuleAbstract
    implements MemberVirtualAttributesModuleImplApi {

  static final Logger LOG = LoggerFactory.getLogger(urn_perun_member_attribute_def_virt_mails.class);
  private static final String ATTRIBUTE_NAMES_PROPERTY = "attributeNames";
  final ModulesConfigLoader loader = new ModulesYamlConfigLoader();
  private final List<String> defaultAttributeNames =
      List.of(AttributesManager.NS_USER_ATTR_DEF + ":preferredMail", AttributesManager.NS_MEMBER_ATTR_DEF + ":mail");

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_MEMBER_ATTR_VIRT);
    attr.setFriendlyName("mails");
    attr.setDisplayName("Mails");
    attr.setType(ArrayList.class.getName());
    attr.setDescription(
        "Returns email addresses of member, if not configured otherwise, uses preferredMail and memberMail.");
    return attr;
  }

  @Override
  public Attribute getAttributeValue(PerunSessionImpl sess, Member member, AttributeDefinition attributeDef) {
    Attribute newAttribute = new Attribute(attributeDef);

    ArrayList<String> allMails = new ArrayList<>();
    List<String> attributeNames;

    if (loader.moduleFileExists(getClass().getSimpleName())) {
      attributeNames =
          loader.loadStringListOrDefault(getClass().getSimpleName(), ATTRIBUTE_NAMES_PROPERTY, defaultAttributeNames);
    } else {
      attributeNames = defaultAttributeNames;
    }

    User user = sess.getPerunBl().getUsersManagerBl().getUserByMember(sess, member);

    for (String emailAttrDef : attributeNames) {
      try {
        Attribute attribute;
        if (emailAttrDef.startsWith(AttributesManager.NS_USER_ATTR_DEF)) {
          attribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, user, emailAttrDef);
        } else if (emailAttrDef.startsWith(AttributesManager.NS_MEMBER_ATTR_DEF)) {
          attribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, member, emailAttrDef);
        } else {
          LOG.trace("getAttributeValue(unsupported attribute type={})", emailAttrDef);
          continue;
        }

        List<String> attributeValues;
        if (attribute.getType().equals(String.class.getName())) {
          if (attribute.valueAsString() == null) {
            continue;
          }
          attributeValues = List.of(attribute.valueAsString());
        } else if (attribute.getType().equals(List.class.getName()) ||
                   attribute.getType().equals(ArrayList.class.getName())) {
          if (attribute.valueAsList() == null) {
            continue;
          }
          attributeValues = attribute.valueAsList();
        } else {
          LOG.trace("getAttributeValue(unsupported attribute type={})", attribute.getType());
          continue;
        }

        for (String mail : attributeValues) {
          Matcher emailMatcher = EMAIL_PATTERN.matcher(mail);
          if (!emailMatcher.matches()) {
            LOG.trace("getAttributeValue(unsupported email format={})", mail);
            continue;
          }
          allMails.add(mail);
        }
      } catch (WrongAttributeAssignmentException | AttributeNotExistsException e) {
        LOG.trace("getAttributeValue(member={},attribute={},failedEmailAttribute={})", member, attributeDef,
            emailAttrDef);
      }
    }
    // return only unique emails
    newAttribute.setValue(new ArrayList<>(new HashSet<>(allMails)));
    return newAttribute;

  }
}
