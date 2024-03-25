package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.UserExtSourceExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.impl.modules.ModulesConfigLoader;
import cz.metacentrum.perun.core.impl.modules.ModulesYamlConfigLoader;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for checking logins uniqueness in the namespace and filling umbrellaid-persistent id. It is only storage! Use
 * module login umbrellaid_persistent for access the value.
 */
public class urn_perun_user_attribute_def_def_login_namespace_umbrellaid_persistent_shadow
    extends urn_perun_user_attribute_def_def_login_namespace {

  private static final Logger LOG =
      LoggerFactory.getLogger(urn_perun_user_attribute_def_def_login_namespace_umbrellaid_persistent_shadow.class);

  private static final String A_U_UMBRELLAID_IDENTIFIER = AttributesManager.NS_USER_ATTR_DEF + ":umbrellaIDIdentifier";
  private static final String CONFIG_EXT_SOURCE_NAME_UMBRELLA_ID = "extSourceNameUmbrellaID";
  private static final String CONFIG_DOMAIN_NAME_UMBRELLA_ID = "domainNameUmbrellaID";
  private static final String FRIENDLY_NAME = "login-namespace:umbrellaid-persistent-shadow";
  private static final String FRIENDLY_NAME_PARAMETER = "umbrellaid-persistent-shadow";

  private final ModulesConfigLoader loader = new ModulesYamlConfigLoader();

  /**
   * Generate the 64 most significant bits as long values.
   *
   * @return 64 bits as long values
   */
  private static long get64MostSignificantBits() {
    LocalDateTime start = LocalDateTime.of(1582, 10, 15, 0, 0, 0);
    Duration duration = Duration.between(start, LocalDateTime.now());
    long seconds = duration.getSeconds();
    long nanos = duration.getNano();
    long timeForUuidIn100Nanos = seconds * 10000000 + nanos * 100;
    long least12SignificantBitOfTime = (timeForUuidIn100Nanos & 0x000000000000FFFFL) >> 4;
    long version = 1 << 12;
    return (timeForUuidIn100Nanos & 0xFFFFFFFFFFFF0000L) + version + least12SignificantBitOfTime;
  }

  /**
   * Generate the 64 least significant bits as long values.
   *
   * @return 64 bits as long values
   */
  private static long get64LeastSignificantBits() {
    Random random = new Random();
    long random63BitLong = random.nextLong() & 0x3FFFFFFFFFFFFFFFL;
    long variant3BitFlag = 0x8000000000000000L;
    return random63BitLong + variant3BitFlag;
  }

  /**
   * ChangedAttributeHook() sets UserExtSource with following properties: - extSourceType is IdP - extSourceName is
   * {getExtSourceName()} - user's extSource login is the same as his persistent attribute
   */
  @Override
  public void changedAttributeHook(PerunSessionImpl session, User user, Attribute attribute) {
    try {
      String userNamespace = attribute.getFriendlyNameParameter();

      if (userNamespace.equals(FRIENDLY_NAME_PARAMETER) && attribute.getValue() != null &&
          !attribute.valueAsString().isEmpty()) {
        ExtSource extSource =
            session.getPerunBl().getExtSourcesManagerBl().getExtSourceByName(session, getExtSourceName());
        UserExtSource userExtSource = new UserExtSource(extSource, 0, attribute.getValue().toString());

        session.getPerunBl().getUsersManagerBl().addUserExtSource(session, user, userExtSource);
      }
    } catch (UserExtSourceExistsException ex) {
      LOG.warn("Attribute: {}, External source already exists for the user.", FRIENDLY_NAME_PARAMETER, ex);
    } catch (ExtSourceNotExistsException ex) {
      throw new InternalErrorException("Attribute: " + FRIENDLY_NAME_PARAMETER + ", IdP external source doesn't exist.",
          ex);
    }
  }

  /**
   * fillAttribute will set value from umbrellaIDIdentifier attribute or generate a version 1 UUID if there is no value
   * set for the attribute
   */
  @Override
  public Attribute fillAttribute(PerunSessionImpl perunSession, User user, AttributeDefinition attribute) {

    Attribute filledAttribute = new Attribute(attribute);

    if (attribute.getFriendlyName().equals(FRIENDLY_NAME)) {

      Attribute umbrellaIDIdentifier = null;

      try {
        umbrellaIDIdentifier = perunSession.getPerunBl().getAttributesManagerBl()
            .getAttribute(perunSession, user, A_U_UMBRELLAID_IDENTIFIER);
      } catch (WrongAttributeAssignmentException e) {
        throw new InternalErrorException(e);
      } catch (AttributeNotExistsException e) {
        LOG.warn("Attribute " + A_U_UMBRELLAID_IDENTIFIER + " does not exist while filling attribute " +
                 attribute.getName() + ".");
      }

      if (umbrellaIDIdentifier != null && umbrellaIDIdentifier.getValue() != null &&
          !umbrellaIDIdentifier.valueAsString().isEmpty()) {
        filledAttribute.setValue(umbrellaIDIdentifier.valueAsString());
      } else {
        long most64SigBits = get64MostSignificantBits();
        long least64SigBits = get64LeastSignificantBits();
        UUID uuid = new UUID(most64SigBits, least64SigBits);

        String domain = "@" + getDomainName();

        filledAttribute.setValue(uuid.toString() + domain);
      }
      return filledAttribute;
    } else {
      // without value
      return filledAttribute;
    }
  }

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
    attr.setFriendlyName(FRIENDLY_NAME);
    attr.setDisplayName("umbrellaID login");
    attr.setType(String.class.getName());
    attr.setDescription("Login for umbrellaID. Do not use it directly! " +
                        "Use \"user:virt:login-namespace:umbrellaid-persistent\" attribute instead.");
    return attr;
  }

  /**
   * Get domain name for the login.
   *
   * @return domain name for the login
   */
  public String getDomainName() {
    return loader.loadString(getClass().getSimpleName(), CONFIG_DOMAIN_NAME_UMBRELLA_ID);
  }

  /**
   * Get name of the extSource where the login will be set.
   *
   * @return extSource name for the login
   */
  private String getExtSourceName() {
    return loader.loadString(getClass().getSimpleName(), CONFIG_EXT_SOURCE_NAME_UMBRELLA_ID);
  }
}
