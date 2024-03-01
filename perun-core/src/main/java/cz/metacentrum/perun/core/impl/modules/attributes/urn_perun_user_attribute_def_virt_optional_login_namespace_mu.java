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

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Contains login in the MU namespace if it is available, if not the value is null
 *
 * @author David Flor
 */
@SkipValueCheckDuringDependencyCheck
public class urn_perun_user_attribute_def_virt_optional_login_namespace_mu
    extends urn_perun_user_attribute_def_virt_optional_login_namespace {

  private static final Pattern loginMUPattern = Pattern.compile("^([0-9]+)[@]muni[.]cz$");
  private static final String A_U_D_loginNamespace_mu = AttributesManager.NS_USER_ATTR_DEF + ":login-namespace:mu";
  private final String EXTSOURCE_MUNI_IDP2 = "https://idp2.ics.muni.cz/idp/shibboleth";

  @Override
  public Attribute getAttributeValue(PerunSessionImpl perunSession, User user, AttributeDefinition attribute) {
    Attribute attr = super.getAttributeValue(perunSession, user, attribute);

    //if attribute is still null (empty login in mu or not existing attribute), try to find uco in user ext sources
    if (attr.getValue() == null) {
      List<UserExtSource> userExtSources =
          perunSession.getPerunBl().getUsersManagerBl().getUserExtSources(perunSession, user);
      for (UserExtSource userExtSource : userExtSources) {
        ExtSource extSource = userExtSource.getExtSource();

        //Skip if extSource is not the one we are looking for
        if (userExtSource.getLogin() == null || extSource == null) {
          continue;
        }
        if (!ExtSourcesManager.EXTSOURCE_IDP.equals(extSource.getType())) {
          continue;
        }
        if (!EXTSOURCE_MUNI_IDP2.equals(extSource.getName())) {
          continue;
        }

        //Get login from this extSource and get only UCO from it
        String login = userExtSource.getLogin();
        Matcher loginMUMatcher = loginMUPattern.matcher(login);
        //This user has login in mu, but in weird format so skip this one
        if (!loginMUMatcher.find()) {
          continue;
        }
        //It is ok, take UCO from login and set it to attribute value
        String UCO = loginMUMatcher.group(1);
        attr.setValue(UCO);
        break;
      }
    }
    return attr;
  }

  @Override
  public List<String> getStrongDependencies() {
    return Collections.singletonList(A_U_D_loginNamespace_mu);
  }

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_USER_ATTR_VIRT);
    attr.setFriendlyName("optional-login-namespace:mu");
    attr.setDisplayName("Optional login in namespace: mu");
    attr.setType(String.class.getName());
    attr.setDescription("Contains an optional login in namespace mu if the user has it.");
    return attr;
  }
}
