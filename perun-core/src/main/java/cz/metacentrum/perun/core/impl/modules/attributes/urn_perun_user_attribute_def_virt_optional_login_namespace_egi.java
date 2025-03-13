package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.SkipValueCheckDuringDependencyCheck;
import java.util.Comparator;
import java.util.List;

/**
 * Contains login in the EGI namespace if it is available, if not the value is null.
 * Value is expected to be EPUID provided by CheckIn identity, also referred to as CUID (community unique identifier).
 *
 * @author Pavel Zlámal
 */
@SkipValueCheckDuringDependencyCheck
public class urn_perun_user_attribute_def_virt_optional_login_namespace_egi
        extends urn_perun_user_attribute_def_virt_optional_login_namespace {

  private static final String EXTSOURCE_CHECKIN = "https://aai.egi.eu/auth/realms/egi";

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_USER_ATTR_VIRT);
    attr.setFriendlyName("optional-login-namespace:egi");
    attr.setDisplayName("Optional login in namespace: egi");
    attr.setType(String.class.getName());
    attr.setDescription("Contains an optional login in namespace egi if the user has it.");
    return attr;
  }

  @Override
  public Attribute getAttributeValue(PerunSessionImpl perunSession, User user, AttributeDefinition attribute) {
    Attribute attr = new Attribute(attribute);

    List<UserExtSource> userExtSources =
            perunSession.getPerunBl().getUsersManagerBl().getUserExtSources(perunSession, user);

    // sort UES by IDs
    userExtSources.sort(Comparator.comparing(UserExtSource::getId));
    for (UserExtSource userExtSource : userExtSources) {
      ExtSource extSource = userExtSource.getExtSource();

      //Skip if extSource is not the one we are looking for
      if (userExtSource.getLogin() == null || extSource == null) {
        continue;
      }
      if (!ExtSourcesManager.EXTSOURCE_IDP.equals(extSource.getType())) {
        continue;
      }
      if (!EXTSOURCE_CHECKIN.equals(extSource.getName())) {
        continue;
      }

      //Get login from this extSource
      String login = userExtSource.getLogin();
      attr.setValue(login);
      break;
    }
    return attr;
  }

}
