package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupAttributesModuleImplApi;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Module to check value of application view configuration. Values represent columns to be shown in GUI.
 * Column names can be either basic column names (e.g. createdBy), or IdP extsource attribute name (e.g. schacHomeOrganization).
 * 'id' is not allowed to be set as it should be shown automatically for corresponding use case.
 * 'fedInfo' should be defined per each attribute. 'formData' values cannot be set.
 *
 * @author Johana Supikova <supikova@ics.muni.cz>
 */
public class urn_perun_group_attribute_def_def_applicationViewPreferences extends GroupAttributesModuleAbstract
    implements GroupAttributesModuleImplApi {

  private final List<String> basicColumnNames =
      List.of("voId", "voName", "groupId", "groupName", "type", "state", "extSourceName", "extSourceType", "user",
          "createdBy", "createdAt", "modifiedBy", "modifiedAt");

  @Override
  public void checkAttributeSyntax(PerunSessionImpl sess, Group group, Attribute attribute)
      throws WrongAttributeValueException {
    // null value is ok for syntax check
    if (attribute.getValue() == null) {
      return;
    }

    List<String> idpAttributeNames =
        sess.getPerunBl().getAttributesManagerBl().getIdpAttributeDefinitions(sess).stream()
            .map(AttributeDefinition::getFriendlyName)
            .toList();

    List<String> columns = attribute.valueAsList();
    HashSet<String> set = new HashSet<>(columns);
    if (set.size() != columns.size()) {
      throw new WrongAttributeValueException(attribute, "Duplicate column names are not permitted.");
    }
    for (String column : columns) {
      if (!basicColumnNames.contains(column) && !idpAttributeNames.contains(column)) {
        throw new WrongAttributeValueException(attribute, column + " is not a valid column name.");
      }
    }
  }

  @Override
  public void checkAttributeSemantics(PerunSessionImpl sess, Group group, Attribute attribute)
      throws WrongReferenceAttributeValueException {
    // null attribute
    if (attribute.getValue() == null) {
      throw new WrongReferenceAttributeValueException(attribute, "Column names attribute cannot be null.");
    }
  }

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_GROUP_ATTR_DEF);
    attr.setFriendlyName("applicationViewPreferences");
    attr.setDisplayName("Application view preferences");
    attr.setType(ArrayList.class.getName());
    attr.setDescription(
        "Columns to be shown in application page. Use applications page configuration dialogue to set the value.");
    return attr;
  }

}
