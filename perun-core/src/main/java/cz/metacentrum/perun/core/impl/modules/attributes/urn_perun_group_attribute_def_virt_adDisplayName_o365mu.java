package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.ParentGroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupVirtualAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupVirtualAttributesModuleImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.SkipValueCheckDuringDependencyCheck;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Module for ad DisplayName defines way how to count a final value for display name of group in o365mu from existing
 * attributes. It expects existence of some attributes and hierarchy of groups.
 *
 * @author Michal Stava <stavamichal@gmail.com>
 */
@SkipValueCheckDuringDependencyCheck
public class urn_perun_group_attribute_def_virt_adDisplayName_o365mu extends GroupVirtualAttributesModuleAbstract
    implements GroupVirtualAttributesModuleImplApi {

  private static final Logger LOG =
      LoggerFactory.getLogger(urn_perun_group_attribute_def_virt_adDisplayName_o365mu.class);

  private static final String A_G_D_AD_DISPLAY_NAME_O365MU =
      AttributesManager.NS_GROUP_ATTR_DEF + ":adDisplayName:o365mu";
  private static final String A_G_D_INET_CISPR = AttributesManager.NS_GROUP_ATTR_DEF + ":inetCispr";
  private static final String A_G_D_INET_GROUP_NAME_CS = AttributesManager.NS_GROUP_ATTR_DEF + ":inetGroupNameCS";
  private static final String A_G_D_INET_GROUP_NAME_ABB_EN =
      AttributesManager.NS_GROUP_ATTR_DEF + ":inetGroupNameAbbEN";
  private static final String A_G_D_INET_WORKPLACES_TYPE_CS =
      AttributesManager.NS_GROUP_ATTR_DEF + ":inetWorkplacesTypeCS";

  private static final Pattern partOfUniversityPattern = Pattern.compile("^[1-9][1-9][0]{4}$");
  private static final Pattern otherWorkplacesPattern = Pattern.compile("^[1-9][0-9]{5}$");

  private static final String TOP_LEVEL_GROUP_CISPR = "000000";
  private static final String TOP_LEVEL_PREFIX = "MUNI";

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_GROUP_ATTR_VIRT);
    attr.setFriendlyName("adDisplayName:o365mu");
    attr.setDisplayName("O365 virtual display name");
    attr.setType(String.class.getName());
    attr.setDescription("Counted display name of group in namespace o365mu");
    return attr;
  }

  @Override
  public Attribute getAttributeValue(PerunSessionImpl sess, Group group, AttributeDefinition attributeDefinition) {
    Attribute attribute = new Attribute(attributeDefinition);

    String defaultAdDisplayName = getStringValueOfGroupAttribute(sess, group, A_G_D_AD_DISPLAY_NAME_O365MU);
    if (defaultAdDisplayName != null) {
      //there is a default value and we can take it
      attribute.setValue(defaultAdDisplayName);
      return attribute;
    }

    //If there is no way to take default value, count a new one from other attributes (if possible)
    //if any of these values are null, return empty attribute instead
    String cispr = getStringValueOfGroupAttribute(sess, group, A_G_D_INET_CISPR);
    if (cispr == null) {
      return attribute;
    }
    String typeOfWorkplaces = getStringValueOfGroupAttribute(sess, group, A_G_D_INET_WORKPLACES_TYPE_CS);
    if (typeOfWorkplaces == null) {
      return attribute;
    }

    String finalName = null;
    Matcher partOfUniversityMatcher = partOfUniversityPattern.matcher(cispr);
    Matcher otherWorkplacesMatcher = otherWorkplacesPattern.matcher(cispr);

    //Now there is a hardcoded logic defined by the prefix of number in CISPR
    if (cispr.equals(TOP_LEVEL_GROUP_CISPR)) {
      //if this is top level group, use predefined name + type of workplaces (defined by specific tree where group
      // exists)
      finalName = TOP_LEVEL_PREFIX + ", " + typeOfWorkplaces;
    } else if (partOfUniversityMatcher.matches()) {
      //if this workplace is part of university with cispr [1-9][1-9]0000, use name of group + type of workplaces
      String groupNameCS = getStringValueOfGroupAttribute(sess, group, A_G_D_INET_GROUP_NAME_CS);
      //we need not-null name of group to count this value correctly
      if (groupNameCS == null) {
        return attribute;
      }
      finalName = groupNameCS + ", " + typeOfWorkplaces;
    } else if (otherWorkplacesMatcher.matches()) {
      //other parts of university with expression as [1-9][0-9]{5}
      //for these groups we need to find parent group with part of university cispr and use also abbreviation for it
      // in the final name
      String groupNameCS = getStringValueOfGroupAttribute(sess, group, A_G_D_INET_GROUP_NAME_CS);
      if (groupNameCS != null) {
        String abbreviation = getParentGroupAbbreviation(sess, group);
        //we can use abbreviation only if exists
        if (abbreviation != null) {
          finalName = groupNameCS + ", " + abbreviation + ", " + typeOfWorkplaces;
        } else {
          finalName = groupNameCS + ", " + typeOfWorkplaces;
        }
      }
    }

    //for other type of groups as ^[0][0-9]{5}$ (except 000000) we don't want to generate any displayName

    attribute.setValue(finalName);
    return attribute;
  }

  /**
   * Try to find any parent group which is hierarchically higher than the group (its parent group, parent group of its
   * parent group until there is any other parent group we can reach) and it has cispr defined as part of university.
   * Return its abbreviation if any is set, otherwise return null (even if no such group exists).
   *
   * @param sess  perun session
   * @param group group to get abbreviation of parent group for it
   * @return abbreviation of searched parent group or null if there is none
   */
  private String getParentGroupAbbreviation(PerunSessionImpl sess, Group group) {
    Group workingGroup = group;
    while (workingGroup.getParentGroupId() != null) {
      try {
        workingGroup = sess.getPerunBl().getGroupsManagerBl().getParentGroup(sess, workingGroup);
      } catch (ParentGroupNotExistsException ex) {
        //We don't want this part of code to throw an exception even if it is weird behavior, return null instead,
        // log it
        //The only proper reason to get this exception is race-condition between this module and group structure changes
        LOG.error("Unexpected behavior when reaching parent group of " + workingGroup, ex);
        return null;
      }

      String parentGroupCispr = getStringValueOfGroupAttribute(sess, workingGroup, A_G_D_INET_CISPR);
      //If there is no cispr to check, continue in searching
      if (parentGroupCispr == null) {
        continue;
      }
      Matcher partOfUniversityMatcher = partOfUniversityPattern.matcher(parentGroupCispr);
      //if this is group we are looking for, get the abbreviation from it, if not then continue in searching
      if (partOfUniversityMatcher.matches()) {
        return getStringValueOfGroupAttribute(sess, workingGroup, A_G_D_INET_GROUP_NAME_ABB_EN);
      }
    }
    //If not found, return null instead
    return null;
  }

  /**
   * Return String value of attribute for a group and a name of attribute.
   * <p>
   * If assignment of attribute is not correct, throw InternalErrorException If attribute does not exist in the Perun,
   * return null instead (same as if value is not set or empty)
   *
   * @param sess            perun session
   * @param group           the group to get attribute for
   * @param nameOfAttribute name of attribute to get value for
   * @return value of attribute, null if not exists, it is empty or it is not set
   */
  private String getStringValueOfGroupAttribute(PerunSessionImpl sess, Group group, String nameOfAttribute) {
    Attribute attribute;
    try {
      attribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, group, nameOfAttribute);
    } catch (AttributeNotExistsException ex) {
      //If attribute not exists in Perun, it is the same for us as it's value was null, return null instead
      return null;
    } catch (WrongAttributeAssignmentException ex) {
      //This means problem in settings of the attribute, throw an exception
      throw new InternalErrorException(ex);
    }

    //this is just prevention to return empty string (which would be problem too)
    if (attribute.valueAsString() == null || attribute.valueAsString().isEmpty()) {
      return null;
    }

    return attribute.valueAsString();
  }
}
