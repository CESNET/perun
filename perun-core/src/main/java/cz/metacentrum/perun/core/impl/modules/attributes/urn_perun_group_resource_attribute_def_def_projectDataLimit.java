package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.GroupResourceMismatchException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.blImpl.ModulesUtilsBlImpl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupResourceAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupResourceAttributesModuleImplApi;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Project directory hard data quota module
 *
 * @author Michal Stava stavamichal@gmail.com
 */
public class urn_perun_group_resource_attribute_def_def_projectDataLimit extends GroupResourceAttributesModuleAbstract
    implements GroupResourceAttributesModuleImplApi {

  private static final String A_GR_projectDataQuota =
      AttributesManager.NS_GROUP_RESOURCE_ATTR_DEF + ":projectDataQuota";
  private static final Pattern testingPattern = Pattern.compile("^[0-9]+([.][0-9]+)?[KMGTPE]$");

  //Definition of K = KB, M = MB etc.
  static final long K = 1024;
  static final long M = K * 1024;
  static final long G = M * 1024;
  static final long T = G * 1024;
  static final long P = T * 1024;
  static final long E = P * 1024;

  @Override
  public void checkAttributeSemantics(PerunSessionImpl perunSession, Group group, Resource resource,
                                      Attribute attribute)
      throws WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
    Attribute attrProjectDataQuota;
    String projectDataQuota = null;

    //Check if attribute value has the right exp pattern (can be null)
    if (attribute.getValue() == null) {
      return;
    }

    //Get ProjectDataLimit value
    Pair<BigDecimal, String> limitNumberAndLetter =
        ModulesUtilsBlImpl.getNumberAndUnitFromString(attribute.valueAsString());
    BigDecimal limitNumber = limitNumberAndLetter.getLeft();
    String projectDataLimitLetter = limitNumberAndLetter.getRight();

    //Get ProjectDataQuota attribute
    try {
      attrProjectDataQuota = perunSession.getPerunBl().getAttributesManagerBl()
          .getAttribute(perunSession, resource, group, A_GR_projectDataQuota);
    } catch (AttributeNotExistsException ex) {
      throw new ConsistencyErrorException(
          "Attribute with projectDataQuota from resource " + resource.getId() + " and group + " + group.getId() +
          " could not obtain.", ex);
    } catch (GroupResourceMismatchException ex) {
      throw new InternalErrorException(ex);
    }

    if (attrProjectDataQuota != null) {
      projectDataQuota = attrProjectDataQuota.valueAsString();
    }

    //Get ProjectDataQuota value
    Pair<BigDecimal, String> quotaNumberAndLetter = ModulesUtilsBlImpl.getNumberAndUnitFromString(projectDataQuota);
    BigDecimal quotaNumber = quotaNumberAndLetter.getLeft();
    String projectDataQuotaLetter = quotaNumberAndLetter.getRight();

    if (quotaNumber.compareTo(BigDecimal.valueOf(0)) < 0) {
      throw new WrongReferenceAttributeValueException(attribute, attrProjectDataQuota,
          attrProjectDataQuota + " cant be less than 0.");
    }

    //Compare ProjectDataLimit with ProjectDataQuota
    if (quotaNumber.compareTo(BigDecimal.valueOf(0)) == 0) {
      if (limitNumber.compareTo(BigDecimal.valueOf(0)) != 0) {
        throw new WrongReferenceAttributeValueException(attribute, attrProjectDataQuota,
            "Try to set limited limit, but there is still set unlimited Quota.");
      }
    } else if ((quotaNumber.compareTo(BigDecimal.valueOf(0)) != 0) &&
               (limitNumber.compareTo(BigDecimal.valueOf(0)) != 0) && projectDataQuotaLetter != null) {

      switch (projectDataLimitLetter) {
        case "K":
          limitNumber = limitNumber.multiply(BigDecimal.valueOf(K));
          break;
        case "M":
          limitNumber = limitNumber.multiply(BigDecimal.valueOf(M));
          break;
        case "T":
          limitNumber = limitNumber.multiply(BigDecimal.valueOf(T));
          break;
        case "P":
          limitNumber = limitNumber.multiply(BigDecimal.valueOf(P));
          break;
        case "E":
          limitNumber = limitNumber.multiply(BigDecimal.valueOf(E));
          break;
        default:
          limitNumber = limitNumber.multiply(BigDecimal.valueOf(G));
          break;
      }

      switch (projectDataQuotaLetter) {
        case "K":
          quotaNumber = quotaNumber.multiply(BigDecimal.valueOf(K));
          break;
        case "M":
          quotaNumber = quotaNumber.multiply(BigDecimal.valueOf(M));
          break;
        case "T":
          quotaNumber = quotaNumber.multiply(BigDecimal.valueOf(T));
          break;
        case "P":
          quotaNumber = quotaNumber.multiply(BigDecimal.valueOf(P));
          break;
        case "E":
          quotaNumber = quotaNumber.multiply(BigDecimal.valueOf(E));
          break;
        default:
          quotaNumber = quotaNumber.multiply(BigDecimal.valueOf(G));
          break;
      }

      if (limitNumber.compareTo(quotaNumber) < 0) {
        throw new WrongReferenceAttributeValueException(attribute, attrProjectDataQuota, group, resource, group,
            resource, attribute + " must be more than or equals to " + attrProjectDataQuota);
      }
    }
  }

  @Override
  public void checkAttributeSyntax(PerunSessionImpl perunSession, Group group, Resource resource, Attribute attribute)
      throws WrongAttributeValueException {
    if (attribute.getValue() == null) {
      return;
    }

    Matcher testMatcher = testingPattern.matcher(attribute.valueAsString());
    if (!testMatcher.find()) {
      throw new WrongAttributeValueException(attribute, resource, group,
          "Format of quota must be something like ex.: 1.30M or 2500K, but it is " + attribute.getValue());
    }

    BigDecimal limitNumber = ModulesUtilsBlImpl.getNumberAndUnitFromString(attribute.valueAsString()).getLeft();

    if (limitNumber.compareTo(BigDecimal.valueOf(0)) < 0) {
      throw new WrongAttributeValueException(attribute, attribute + " can't be less than 0.");
    }
  }

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_GROUP_RESOURCE_ATTR_DEF);
    attr.setFriendlyName("projectDataLimit");
    attr.setDisplayName("Project soft data quota.");
    attr.setType(String.class.getName());
    attr.setDescription("Project hard quota including units (M,G,T, ...), G is default.");
    return attr;
  }

  @Override
  public List<String> getDependencies() {
    return Collections.singletonList(A_GR_projectDataQuota);
  }
}
