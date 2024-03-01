package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserPersistentShadowAttribute;

/**
 * Class for checking logins uniqueness in the namespace and filling bbmri-persistent id.
 * It is only storage! Use module login bbmri_persistent for access the value.
 *
 * @author Sona Mastrakova <sona.mastrakova@gmail.com>
 * @author Ondrej Velisek <ondrejvelisek@gmail.com>
 * @author Jakub Hruska <jhruska@mail.muni.cz>
 * @date 07.11.2016
 */
public class urn_perun_user_attribute_def_def_login_namespace_bbmri_persistent_shadow
    extends UserPersistentShadowAttribute {

  private final static String extSourceNameBbmri = "https://login.bbmri-eric.eu/idp/";
  private final static String domainNameBbmri = "bbmri.eu";
  private final static String attrNameBbmri = "login-namespace:bbmri-persistent-shadow";
  private final static String lifeScienceShadow =
      "urn:perun:user:attribute-def:def:login-namespace:lifescienceid-persistent-shadow";
  private final static String elixirShadow =
      "urn:perun:user:attribute-def:def:login-namespace:elixir-persistent-shadow";

  @Override
  public String getFriendlyName() {
    return attrNameBbmri;
  }

  @Override
  public String getExtSourceName() {
    return extSourceNameBbmri;
  }

  @Override
  public String getDomainName() {
    return domainNameBbmri;
  }

  @Override
  public String getDescription() {
    return "Login to BBMRI. Do not use it directly! " +
        "Use instead virt:bbmri-persistent attribute.";
  }

  @Override
  public String getDisplayName() {
    return "BBMRI login";
  }

  @Override
  public String getFriendlyNameParameter() {
    return "bbmri-persistent-shadow";
  }

  @Override
  public Attribute fillAttribute(PerunSessionImpl sess, User user, AttributeDefinition attribute) {
    // Check if user has login in namespace lifescienceid-persistent-shadow
    Attribute persistentShadow = null;
    try {
      persistentShadow = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, user, lifeScienceShadow);
    } catch (WrongAttributeAssignmentException | AttributeNotExistsException ignored) {
    }

    if (persistentShadow == null || persistentShadow.getValue() == null) {
      // Check if user has login in namespace elixir-persistent-shadow
      try {
        persistentShadow = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, user, elixirShadow);
      } catch (WrongAttributeAssignmentException | AttributeNotExistsException ignored) {
      }
    }

    if (persistentShadow != null && persistentShadow.getValue() != null) {
      Attribute filledAttribute = new Attribute(attribute);
      String value = persistentShadow.getValue().toString();
      String valueWithoutScope = value.split("@", 2)[0];
      String attrValue = valueWithoutScope + "@" + domainNameBbmri;
      filledAttribute.setValue(attrValue);
      return filledAttribute;
    }

    return super.fillAttribute(sess, user, attribute);
  }
}
