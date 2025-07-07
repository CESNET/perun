package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.MemberAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.MemberAttributesModuleImplApi;
import java.text.ParseException;
import java.util.Date;
import java.util.LinkedHashMap;


/**
 * Holds timestamps of member's dates of expiration/archivation. These values should only be set automatically by Perun
 *
 * @author David Flor <davidflor@seznam.cz>
 */
public class urn_perun_member_attribute_def_def_lifecycleTimestamps extends MemberAttributesModuleAbstract
    implements MemberAttributesModuleImplApi {

  @Override
  public void checkAttributeSyntax(PerunSessionImpl perunSession, Member member, Attribute attribute)
      throws WrongAttributeValueException {
    if (attribute.getValue() == null) {
      return;
    }
    LinkedHashMap<String, String> timestamps = attribute.valueAsMap();
    for (String key : timestamps.keySet()) {
      if (!key.equals("expiredAt") && !key.equals("archivedAt")) {
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
  public void checkAttributeSemantics(PerunSessionImpl perunSession, Member member, Attribute attribute) throws
      WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
    if (attribute.getValue() == null) {
      return;
    }
    LinkedHashMap<String, String> timestamps = attribute.valueAsMap();
    for (String key : timestamps.keySet()) {
      String value = timestamps.get(key);
      if (value == null) {
        throw new WrongReferenceAttributeValueException("Timestamp value for key '" + key + "' is null.");
      }
      Date today = new Date();
      Date testDate;
      try {
        testDate = BeansUtils.getDateFormatterWithoutTime().parse(value);
        if (testDate.compareTo(today) > 0) {
          throw new WrongAttributeAssignmentException("Timestamp '" + testDate + "' for key '" + key +
                                                          "' is in the future.");
        }
      } catch (ParseException ex) {
        throw new WrongAttributeAssignmentException("Date parsing failed for key '" + key + "'.", ex);
      }
    }
  }

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_MEMBER_ATTR_DEF);
    attr.setFriendlyName("lifecycleTimestamps");
    attr.setDisplayName("Lifecycle timestamps");
    attr.setType(LinkedHashMap.class.getName());
    attr.setDescription("Timestamps of when member entered a status of expiration/archivation.");
    return attr;
  }
}
