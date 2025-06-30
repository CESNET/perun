package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.MemberGroupAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.MemberGroupAttributesModuleImplApi;
import java.text.ParseException;
import java.util.Date;
import java.util.LinkedHashMap;

/**
 * Holds timestamps of group member's dates of expiration.
 * These values should only be set automatically by Perun.
 *
 * @author David Flor <davidflor@seznam.cz>
 */
public class urn_perun_member_group_attribute_def_def_lifecycleTimestamps extends MemberGroupAttributesModuleAbstract
    implements MemberGroupAttributesModuleImplApi {

  @Override
  public void checkAttributeSyntax(PerunSessionImpl perunSession, Member member, Group group, Attribute attribute)
      throws WrongAttributeValueException {
    if (attribute.getValue() == null) {
      return;
    }
    LinkedHashMap<String, String> timestamps = attribute.valueAsMap();
    for (String key : timestamps.keySet()) {
      if (!key.equals("expiredAt")) {
        throw new WrongAttributeValueException(attribute, member, "Key '" + key + "' is not valid.");
      }
      try {
        BeansUtils.getDateFormatterWithoutTime().parse(timestamps.get(key));
      } catch (ParseException ex) {
        throw new WrongAttributeValueException(attribute, "Date parsing failed for key '" + key + "'.", ex);
      }
    }
  }

  @Override
  public void checkAttributeSemantics(PerunSessionImpl perunSession, Member member, Group group, Attribute attribute)
      throws WrongReferenceAttributeValueException {
    if (attribute.getValue() == null) {
      return;
    }
    LinkedHashMap<String, String> timestamps = attribute.valueAsMap();
    for (String key : timestamps.keySet()) {
      String value = timestamps.get(key);
      if (value == null) { // Handle the null case
        throw new WrongReferenceAttributeValueException("Timestamp value for key '" + key + "' is null.");
      }
      Date today = new Date();
      Date testDate;
      try {
        testDate = BeansUtils.getDateFormatterWithoutTime().parse(value);
        if (testDate.compareTo(today) > 0) {
          throw new WrongReferenceAttributeValueException("Timestamp '" + testDate + "' for key '" + key +
                                                          "' is in the future.");
        }
      } catch (ParseException ex) {
        throw new WrongReferenceAttributeValueException("Date parsing failed for key '" + key + "'.", ex);
      }
    }
  }

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_MEMBER_GROUP_ATTR_DEF);
    attr.setFriendlyName("lifecycleTimestamps");
    attr.setDisplayName("Lifecycle timestamps");
    attr.setType(LinkedHashMap.class.getName());
    attr.setDescription("Timestamp of when group member entered a status of expiration.");
    return attr;
  }
}
