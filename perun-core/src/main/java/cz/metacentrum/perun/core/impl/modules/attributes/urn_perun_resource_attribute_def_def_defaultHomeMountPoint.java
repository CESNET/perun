/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceAttributesModuleImplApi;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Milan Halenar <255818@mail.muni.cz>
 * @date 27.4.2011
 */
public class urn_perun_resource_attribute_def_def_defaultHomeMountPoint extends ResourceAttributesModuleAbstract
    implements ResourceAttributesModuleImplApi {

  private static final String A_R_homeMountPoints = AttributesManager.NS_RESOURCE_ATTR_DEF + ":homeMountPoints";
  private static final Pattern pattern = Pattern.compile("^/[-a-zA-Z.0-9_/]*$");

  /**
   * Checks if the homemountpoint is contained in list of homemountpoint at underlying facility
   *
   * @param perunSession
   * @param resource
   * @param attribute
   * @throws InternalErrorException
   * @throws WrongAttributeValueException
   * @throws WrongReferenceAttributeValueException
   * @throws WrongAttributeAssignmentException
   */
  @Override
  public void checkAttributeSemantics(PerunSessionImpl perunSession, Resource resource, Attribute attribute)
      throws WrongReferenceAttributeValueException, WrongAttributeAssignmentException {

    if (attribute.getValue() == null) {
      throw new WrongReferenceAttributeValueException(attribute, null, resource, null,
          "Attribute value cannot be null.");
    }

    Attribute resourceAttribute;
    try {
      resourceAttribute =
          perunSession.getPerunBl().getAttributesManagerBl().getAttribute(perunSession, resource, A_R_homeMountPoints);
    } catch (AttributeNotExistsException ex) {
      throw new InternalErrorException(ex);
    }

    if (resourceAttribute.getValue() == null) {
      throw new WrongReferenceAttributeValueException(attribute, resourceAttribute, resource, null, resource, null,
          "HomeMountPoints attribute cannot be null.");
    }

    List<?> homeMntPoints = resourceAttribute.valueAsList();
    if (!homeMntPoints.contains(attribute.getValue())) {
      throw new WrongReferenceAttributeValueException(attribute, resourceAttribute, resource, null, resource, null,
          "Attribute value ins't defined in underlying resource. Attribute name=" + A_R_homeMountPoints);
    }
  }

  /**
   * Checks syntax of attribute value, allows valid unix paths only.
   *
   * @param perunSession perun session
   * @param resource     resource for which you want to check validity of attribute
   * @param attribute    attribute to check
   * @throws InternalErrorException
   * @throws WrongAttributeValueException
   */
  @Override
  public void checkAttributeSyntax(PerunSessionImpl perunSession, Resource resource, Attribute attribute)
      throws WrongAttributeValueException {
    if (attribute.getValue() == null) {
      return;
    }

    Matcher match = pattern.matcher(attribute.valueAsString());
    if (!match.matches()) {
      throw new WrongAttributeValueException(attribute, "Wrong def. mount point format");
    }
  }

  @Override
  public Attribute fillAttribute(PerunSessionImpl perunSession, Resource resource, AttributeDefinition attribute)
      throws WrongAttributeAssignmentException {
    Attribute resourceAttribute;
    try {
      resourceAttribute =
          perunSession.getPerunBl().getAttributesManagerBl().getAttribute(perunSession, resource, A_R_homeMountPoints);
    } catch (AttributeNotExistsException ex) {
      throw new InternalErrorException("no homemountpoints set on this resource", ex);
    }
    Attribute retAttribute = new Attribute(attribute);

    List<?> homeMntPoints = (List<?>) resourceAttribute.getValue();
    if (homeMntPoints != null && homeMntPoints.size() > 0) {
      retAttribute.setValue(homeMntPoints.get(0));
    }
    return retAttribute;
  }

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_RESOURCE_ATTR_DEF);
    attr.setFriendlyName("defaultHomeMountPoint");
    attr.setDisplayName("Default home mount point");
    attr.setType(String.class.getName());
    attr.setDescription("Default home mount point for all members on this resource.");
    return attr;
  }

  @Override
  public List<String> getDependencies() {
    List<String> dependecies = new ArrayList<>();
    dependecies.add(A_R_homeMountPoints);
    return dependecies;
  }
}
