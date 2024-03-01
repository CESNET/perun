package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.UserExtSourceExistsException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.impl.modules.ModulesConfigLoader;
import cz.metacentrum.perun.core.impl.modules.ModulesYamlConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Class for checking logins uniqueness in the namespace and filling researcher-access-persistent id.
 * It is only storage! Use module login researcher-access_persistent for access the value.
 */
@Deprecated
public class urn_perun_user_attribute_def_def_login_namespace_researcher_access_persistent_shadow
    extends urn_perun_user_attribute_def_def_login_namespace {

  private final static Logger log = LoggerFactory.getLogger(
      urn_perun_user_attribute_def_def_login_namespace_umbrellaid_persistent_shadow.class);

  private final static String CONFIG_EXT_SOURCE_NAME_RESEARCHER_ACCESS = "extSourceNameResearcherAccess";
  private final static String CONFIG_DOMAIN_NAME_RESEARCHER_ACCESS = "domainNameResearcherAccess";
  private final static String FRIENDLY_NAME = "login-namespace:researcher-access-persistent-shadow";
  private final static String FRIENDLY_NAME_PARAMETER = "researcher-access-persistent-shadow";

  private final ModulesConfigLoader loader = new ModulesYamlConfigLoader();

  /**
   * Format is: "[uuid]@{getDomainName()}" where [hash] represents a version 4 UUID (randomly generated).
   */
  @Override
  public Attribute fillAttribute(PerunSessionImpl perunSession, User user, AttributeDefinition attribute) {

    Attribute filledAttribute = new Attribute(attribute);

    if (attribute.getFriendlyName().equals(FRIENDLY_NAME)) {
      String domain = "@" + getDomainName();
      UUID uuid = UUID.randomUUID();
      filledAttribute.setValue(uuid.toString() + domain);
      return filledAttribute;
    } else {
      // without value
      return filledAttribute;
    }
  }

  /**
   * ChangedAttributeHook() sets UserExtSource with following properties:
   * - extSourceType is IdP
   * - extSourceName is {getExtSourceName()}
   * - user's extSource login is the same as his persistent attribute
   */
  @Override
  public void changedAttributeHook(PerunSessionImpl session, User user, Attribute attribute) {
    try {
      String userNamespace = attribute.getFriendlyNameParameter();

      if (userNamespace.equals(FRIENDLY_NAME_PARAMETER) && attribute.getValue() != null &&
          !attribute.valueAsString().isEmpty()) {
        ExtSource extSource = session.getPerunBl()
            .getExtSourcesManagerBl()
            .getExtSourceByName(session, getExtSourceName());
        UserExtSource userExtSource = new UserExtSource(extSource, 0, attribute.getValue().toString());

        session.getPerunBl().getUsersManagerBl().addUserExtSource(session, user, userExtSource);
      }
    } catch (UserExtSourceExistsException ex) {
      log.warn("Attribute: {}, External source already exists for the user.", FRIENDLY_NAME_PARAMETER, ex);
    } catch (ExtSourceNotExistsException ex) {
      throw new InternalErrorException("Attribute: " + FRIENDLY_NAME_PARAMETER +
          ", IdP external source doesn't exist.", ex);
    }
  }

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
    attr.setFriendlyName(FRIENDLY_NAME);
    attr.setDisplayName("Researcher Access login");
    attr.setType(String.class.getName());
    attr.setDescription("Login for Researcher Access. Do not use it directly! " +
        "Use \"user:virt:login-namespace:researcher-access-persistent\" attribute instead.");
    return attr;
  }

  /**
   * Get name of the extSource where the login will be set.
   *
   * @return extSource name for the login
   */
  private String getExtSourceName() {
    return loader.loadString(getClass().getSimpleName(), CONFIG_EXT_SOURCE_NAME_RESEARCHER_ACCESS);
  }

  /**
   * Get domain name for the login.
   *
   * @return domain name for the login
   */
  private String getDomainName() {
    return loader.loadString(getClass().getSimpleName(), CONFIG_DOMAIN_NAME_RESEARCHER_ACCESS);
  }
}
