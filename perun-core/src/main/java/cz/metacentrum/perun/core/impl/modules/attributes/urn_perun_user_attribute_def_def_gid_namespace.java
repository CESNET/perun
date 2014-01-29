package cz.metacentrum.perun.core.impl.modules.attributes;

import java.util.ArrayList;
import java.util.List;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleImplApi;

/**
 * Checks and fills at specified facility users GID.
 *
 * @date 22.4.2011 10:43:48
 * @author Lukáš Pravda   <luky.pravda@gmail.com>
 */
public class urn_perun_user_attribute_def_def_gid_namespace extends UserAttributesModuleAbstract implements UserAttributesModuleImplApi {

  private static final String A_E_namespace_minGID = AttributesManager.NS_ENTITYLESS_ATTR_DEF + ":namespace-minGID";
  private static final String A_E_namespace_maxGID = AttributesManager.NS_ENTITYLESS_ATTR_DEF + ":namespace-maxGID";
  
  @Override
  /**
   * Checks the new GID of the user at the specified facility. The new GID must
   * not be lower than the min GID or greater than the max GID. Also no collision between 
   * existing user and the new user is allowed.
   */
  public void checkAttributeValue(PerunSessionImpl sess, User user, Attribute attribute) throws WrongAttributeValueException, WrongReferenceAttributeValueException, InternalErrorException, WrongAttributeAssignmentException {
    Integer GID = (Integer) attribute.getValue();
    String gidNamespace = attribute.getFriendlyNameParameter();

    if (GID == null) {
      throw new WrongAttributeValueException(attribute, "Attribute was not filled, therefore there is nothing to be checked.");
    }

    Attribute minGidAttribute = null;
    Attribute maxGidAttribute = null;
    try {
      minGidAttribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, gidNamespace, A_E_namespace_minGID);
      maxGidAttribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, gidNamespace, A_E_namespace_maxGID);
    } catch (AttributeNotExistsException e) {
      throw new ConsistencyErrorException("minUid and maxUid attributes are required", e);
    }

    Integer min = (Integer) minGidAttribute.getValue();
    Integer max = (Integer) maxGidAttribute.getValue();

    //GID is in proper range
    if (GID < min || GID > max) {
      throw new WrongAttributeValueException(attribute, "GID " + GID + " is not proper range (" + min + "," + max);
    }
    
    
  }

  @Override
  /**
   * Fills the new GID for the user at the specified facility. First empty slot
   * in range (minGID, maxGID) is returned.
   */
  public Attribute fillAttribute(PerunSessionImpl session, User user, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException {
    Attribute atr = new Attribute(attribute);

    Attribute otherAttribute;
    try {
      otherAttribute = session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, atr.getName());
    } catch (AttributeNotExistsException ex) {
      throw new InternalErrorException(ex);
    }

    if (atr.equals(otherAttribute)) {
      return atr;
    }
    return atr;
  }
    @Override
    public List<String> getDependencies() {
      List<String> dependencies = new ArrayList<String>();
      dependencies.add(A_E_namespace_maxGID);
      dependencies.add(A_E_namespace_minGID);
      return dependencies;
    }

    public AttributeDefinition getAttributeDefinition() {
      AttributeDefinition attr = new AttributeDefinition();
      attr.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
      attr.setFriendlyName("gid-namespace");
      attr.setType(Integer.class.getName());
      attr.setDescription("Gid namespace.");
      return attr;
    }
}
