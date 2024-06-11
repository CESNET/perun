package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.audit.events.AttributesManagerEvents.AllAttributesRemovedForUserExtSource;
import cz.metacentrum.perun.audit.events.AttributesManagerEvents.AttributeRemovedForUes;
import cz.metacentrum.perun.audit.events.AttributesManagerEvents.AttributeSetForUes;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.SkipValueCheckDuringDependencyCheck;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributeCollectedFromUserExtSource;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Checks specified users mails in google namespace.
 *
 * @author Michal Stava  &lt;stavamichal@gmail.com&gt;
 */
@SkipValueCheckDuringDependencyCheck
public class urn_perun_user_attribute_def_virt_mails_namespace_google extends
    UserVirtualAttributeCollectedFromUserExtSource {

  private static final String EXTSOURCE = "https://login.cesnet.cz/google-idp/";

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_USER_ATTR_VIRT);
    attr.setFriendlyName("mails-namespace:google");
    attr.setDisplayName("Emails in namespace:google");
    attr.setType(ArrayList.class.getName());
    attr.setDescription("Emails in google namespace");
    return attr;
  }

  @Override
  public String getDestinationAttributeFriendlyName() {
    return "mails-namespace:google";
  }

  @Override
  public String getSourceAttributeFriendlyName() {
    return "mail";
  }

  @Override
  public List<AttributeHandleIdentifier> getHandleIdentifiers() {
    List<AttributeHandleIdentifier> handleIdentifiers = new ArrayList<>();
    handleIdentifiers.add(auditEvent -> {
      if (auditEvent instanceof AllAttributesRemovedForUserExtSource &&
              ((AllAttributesRemovedForUserExtSource) auditEvent).getUserExtSource().getExtSource().getName()
                  .equals(EXTSOURCE)) {
        return ((AllAttributesRemovedForUserExtSource) auditEvent).getUserExtSource().getUserId();
      } else {
        return null;
      }
    });
    handleIdentifiers.add(auditEvent -> {
      if (auditEvent instanceof AttributeSetForUes &&
              ((AttributeSetForUes) auditEvent).getAttribute().getFriendlyName()
                  .equals(getSourceAttributeFriendlyName()) &&
              ((AttributeSetForUes) auditEvent).getUes().getExtSource().getName().equals(EXTSOURCE)) {
        return ((AttributeSetForUes) auditEvent).getUes().getUserId();
      } else {
        return null;
      }
    });
    handleIdentifiers.add(auditEvent -> {
      if (auditEvent instanceof AttributeRemovedForUes &&
              ((AttributeRemovedForUes) auditEvent).getAttribute().getFriendlyName()
                  .equals(getSourceAttributeFriendlyName()) &&
              ((AttributeRemovedForUes) auditEvent).getUes().getExtSource().getName().equals(EXTSOURCE)) {
        return ((AttributeRemovedForUes) auditEvent).getUes().getUserId();
      } else {
        return null;
      }
    });
    return handleIdentifiers;
  }

  @Override
  public Predicate<UserExtSource> getExtSourceFilter(PerunSessionImpl sess) {
    return ues -> (ues != null && EXTSOURCE.equals(ues.getExtSource().getName()));
  }
}
