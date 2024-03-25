package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.SkipValueCheckDuringDependencyCheck;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleImplApi;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Checks specified users mails in microsoft namespace.
 */
@SkipValueCheckDuringDependencyCheck
public class urn_perun_user_attribute_def_virt_mails_namespace_microsoft extends UserVirtualAttributesModuleAbstract
    implements UserVirtualAttributesModuleImplApi {

  private static final Logger LOG =
      LoggerFactory.getLogger(urn_perun_user_attribute_def_virt_mails_namespace_microsoft.class);
  private static final String EXTSOURCE = "https://login.cesnet.cz/microsoft-idp/";
  private static final String A_UES_MAIL = AttributesManager.NS_UES_ATTR_DEF + ":mail";

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_USER_FACILITY_ATTR_VIRT);
    attr.setFriendlyName("mails-namespace:microsoft");
    attr.setDisplayName("Emails in namespace:microsoft");
    attr.setType(ArrayList.class.getName());
    attr.setDescription("Emails in microsoft namespace");
    return attr;
  }

  @Override
  public Attribute getAttributeValue(PerunSessionImpl sess, User user, AttributeDefinition attributeDefinition) {
    Set<String> microsoftMails = new HashSet<>();
    List<UserExtSource> userExtSources = sess.getPerunBl().getUsersManagerBl().getUserExtSources(sess, user);

    for (UserExtSource ues : userExtSources) {
      if (ues.getExtSource() != null && EXTSOURCE.equals(ues.getExtSource().getName())) {
        String login = ues.getLogin();
        if (login != null && !login.isEmpty()) {
          try {
            Attribute attribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, ues, A_UES_MAIL);
            if (attribute.getValue() != null) {
              microsoftMails.add((String) attribute.getValue());
            }
          } catch (AttributeNotExistsException ex) {
            //This should not happen, but if yes, skip this value
            LOG.error("Attribute {} not exists in Perun so value {} microsoft mails can't be get correctly.",
                A_UES_MAIL, user);
          } catch (WrongAttributeAssignmentException ex) {
            throw new InternalErrorException(ex);
          }
        }
      }
    }

    Attribute attribute = new Attribute(attributeDefinition);
    attribute.setValue(new ArrayList<>(microsoftMails));
    return attribute;
  }
}
