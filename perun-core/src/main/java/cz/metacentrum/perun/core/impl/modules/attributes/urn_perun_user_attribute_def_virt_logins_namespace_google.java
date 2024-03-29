package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.SkipValueCheckDuringDependencyCheck;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleImplApi;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Checks specified users logins in google namespace.
 *
 * @author Michal Stava   &lt;stavamichal@gmail.com&gt;
 */
@SkipValueCheckDuringDependencyCheck
public class urn_perun_user_attribute_def_virt_logins_namespace_google extends UserVirtualAttributesModuleAbstract
    implements UserVirtualAttributesModuleImplApi {

  private static final String NAMESPACE = "google";
  private static final String EXTSOURCE = "https://login.cesnet.cz/google-idp/";
  private static final Pattern pattern = Pattern.compile("^.+[@]google[.]extidp[.]cesnet[.]cz$");

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_USER_FACILITY_ATTR_VIRT);
    attr.setFriendlyName("logins-namespace:google");
    attr.setDisplayName("Logins in namespace:google");
    attr.setType(ArrayList.class.getName());
    attr.setDescription("Logins in google namespace");
    return attr;
  }

  @Override
  public Attribute getAttributeValue(PerunSessionImpl sess, User user, AttributeDefinition attributeDefinition) {
    Set<String> googleLogins = new HashSet<>();
    List<UserExtSource> userExtSources = sess.getPerunBl().getUsersManagerBl().getUserExtSources(sess, user);

    for (UserExtSource ues : userExtSources) {
      if (ues.getExtSource() != null && EXTSOURCE.equals(ues.getExtSource().getName())) {
        String login = ues.getLogin();
        if (login != null && !login.isEmpty()) {
          Matcher matcher = pattern.matcher(login);
          if (matcher.matches()) {
            googleLogins.add(login.replaceAll("[@].*$", ""));
          }
        }
      }
    }

    Attribute attribute = new Attribute(attributeDefinition);
    attribute.setValue(new ArrayList<>(googleLogins));
    return attribute;
  }
}
