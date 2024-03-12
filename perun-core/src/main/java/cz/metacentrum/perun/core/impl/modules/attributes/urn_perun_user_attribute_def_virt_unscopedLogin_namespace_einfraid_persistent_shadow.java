package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.audit.events.AttributesManagerEvents.AllAttributesRemovedForUser;
import cz.metacentrum.perun.audit.events.AttributesManagerEvents.AttributeChangedForUser;
import cz.metacentrum.perun.audit.events.AttributesManagerEvents.AttributeRemovedForUser;
import cz.metacentrum.perun.audit.events.AttributesManagerEvents.AttributeSetForUser;
import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.SkipValueCheckDuringDependencyCheck;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SkipValueCheckDuringDependencyCheck
public class urn_perun_user_attribute_def_virt_unscopedLogin_namespace_einfraid_persistent_shadow
    extends urn_perun_user_attribute_def_virt_unscopedLogin_namespace {

  private static final String loginNamespaceEINFRAFriendlyName = "login-namespace:einfraid-persistent-shadow";
  private static final String A_U_D_loginNamespaceEINFRA =
      AttributesManager.NS_USER_ATTR_DEF + ":" + loginNamespaceEINFRAFriendlyName;

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_USER_ATTR_VIRT);
    attr.setFriendlyName("unscopedLogin-namespace:einfraid-persistent-shadow");
    attr.setDisplayName("EINFRA unscoped login");
    attr.setType(String.class.getName());
    attr.setDescription("EINFRA user login without the scope");
    return attr;
  }

  @Override
  public List<String> getStrongDependencies() {
    return Collections.singletonList(A_U_D_loginNamespaceEINFRA);
  }

  /**
   * Return true if attribute name is one of affected attributes in this virtual module.
   *
   * @param nameOfAttribute name of attribute to check
   * @return true if attribute is one of affected, false otherwise
   */
  private boolean isAffectedAttribute(String nameOfAttribute) {
    return loginNamespaceEINFRAFriendlyName.equals(nameOfAttribute);
  }

  @Override
  public List<AuditEvent> resolveVirtualAttributeValueChange(PerunSessionImpl perunSession, AuditEvent message)
      throws AttributeNotExistsException {
    List<AuditEvent> resolvingMessages = new ArrayList<>();

    // handle source user attributes changes
    if (message instanceof AttributeSetForUser &&
        isAffectedAttribute(((AttributeSetForUser) message).getAttribute().getFriendlyName())) {
      AttributeDefinition attributeDefinition = perunSession.getPerunBl().getAttributesManagerBl()
          .getAttributeDefinition(perunSession, this.getAttributeDefinition().getName());
      resolvingMessages.add(
          new AttributeChangedForUser(new Attribute(attributeDefinition), ((AttributeSetForUser) message).getUser()));

    } else if (message instanceof AttributeRemovedForUser &&
               isAffectedAttribute(((AttributeRemovedForUser) message).getAttribute().getFriendlyName())) {
      AttributeDefinition attributeDefinition = perunSession.getPerunBl().getAttributesManagerBl()
          .getAttributeDefinition(perunSession, this.getAttributeDefinition().getName());
      resolvingMessages.add(new AttributeChangedForUser(new Attribute(attributeDefinition),
          ((AttributeRemovedForUser) message).getUser()));

    } else if (message instanceof AllAttributesRemovedForUser) {
      AttributeDefinition attributeDefinition = perunSession.getPerunBl().getAttributesManagerBl()
          .getAttributeDefinition(perunSession, this.getAttributeDefinition().getName());
      resolvingMessages.add(new AttributeChangedForUser(new Attribute(attributeDefinition),
          ((AllAttributesRemovedForUser) message).getUser()));

    }

    return resolvingMessages;
  }
}
