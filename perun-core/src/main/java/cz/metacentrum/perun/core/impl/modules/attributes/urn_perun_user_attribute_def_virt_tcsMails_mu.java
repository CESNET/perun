package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.audit.events.AttributesManagerEvents.AllAttributesRemovedForUser;
import cz.metacentrum.perun.audit.events.AttributesManagerEvents.AttributeChangedForUser;
import cz.metacentrum.perun.audit.events.AttributesManagerEvents.AttributeRemovedForUser;
import cz.metacentrum.perun.audit.events.AttributesManagerEvents.AttributeSetForUser;
import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.SkipValueCheckDuringDependencyCheck;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleImplApi;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Get all emails from Perun for purpose of TCS.
 *
 * @author Michal Stava stavamichal@gmail.com
 */
@SkipValueCheckDuringDependencyCheck
public class urn_perun_user_attribute_def_virt_tcsMails_mu extends UserVirtualAttributesModuleAbstract
    implements UserVirtualAttributesModuleImplApi {

  private static final String preferredMailFriendlyName = "preferredMail";
  private static final String isMailFriendlyName = "ISMail";
  private static final String publicMailsFriendlyName = "publicAliasMails";
  private static final String privateMailsFriendlyName = "privateAliasMails";
  private static final String o365MailsFriendlyName = "o365EmailAddresses:mu";

  private static final String A_U_D_preferredMail =
      AttributesManager.NS_USER_ATTR_DEF + ":" + preferredMailFriendlyName;
  private static final String A_U_D_ISMail = AttributesManager.NS_USER_ATTR_DEF + ":" + isMailFriendlyName;
  private static final String A_U_D_publicAliasMails =
      AttributesManager.NS_USER_ATTR_DEF + ":" + publicMailsFriendlyName;
  private static final String A_U_D_privateAliasMails =
      AttributesManager.NS_USER_ATTR_DEF + ":" + privateMailsFriendlyName;
  private static final String A_U_D_o365EmailAddressesMU =
      AttributesManager.NS_USER_ATTR_DEF + ":" + o365MailsFriendlyName;

  private final static Logger log = LoggerFactory.getLogger(urn_perun_user_attribute_def_virt_tcsMails_mu.class);

  @Override
  public Attribute getAttributeValue(PerunSessionImpl sess, User user, AttributeDefinition attributeDefinition) {

    Attribute attribute = new Attribute(attributeDefinition);

    // to keep actual mails in order
    SortedSet<String> tcsMailsValue = new TreeSet<>();
    // to filter out case insensitive duplicates
    Set<String> compareSet = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

    List<String> namesOfAttributes =
        Arrays.asList(A_U_D_preferredMail, A_U_D_ISMail, A_U_D_o365EmailAddressesMU, A_U_D_publicAliasMails,
            A_U_D_privateAliasMails);

    List<Attribute> sourceAttributes =
        sess.getPerunBl().getAttributesManagerBl().getAttributes(sess, user, namesOfAttributes);
    Map<String, Attribute> attributesMap =
        sourceAttributes.stream().collect(Collectors.toMap(Attribute::getName, Function.identity()));

    // ensure attributes order
    for (String attrName : namesOfAttributes) {
      Attribute sourceAttribute = attributesMap.get(attrName);
      if (sourceAttribute != null) {
        // gather values of all attributes
        if (sourceAttribute.getType().equals(String.class.getName())) {
          if (sourceAttribute.getValue() != null) {
            if (!compareSet.contains(sourceAttribute.valueAsString().trim())) {
              // add only case-insensitive unique values
              compareSet.add(sourceAttribute.valueAsString().trim());
              tcsMailsValue.add(sourceAttribute.valueAsString().trim());
            }
          }
        } else if (sourceAttribute.getType().equals(ArrayList.class.getName())) {
          if (sourceAttribute.getValue() != null) {
            sourceAttribute.valueAsList().stream().map(String::trim).collect(Collectors.toList()).forEach(s -> {
              if (!compareSet.contains(s)) {
                // add only case-insensitive unique values
                compareSet.add(s);
                tcsMailsValue.add(s);
              }
            });
          }
        } else {
          //unexpected type of value, log it and skip the attribute
          log.error("Unexpected type of attribute (should be String or ArrayList) {}. It will be skipped.",
              sourceAttribute);
        }
      } else {
        log.warn("When counting value of attribute {} we are missing source attribute {}. It will be skipped.",
            this.getAttributeDefinition(), attrName);
      }
    }
    attribute.setValue(new ArrayList<>(tcsMailsValue));

    return attribute;
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

  /**
   * Return true if attribute name is one of affected attributes in this virtual module.
   *
   * @param nameOfAttribute name of attribute to check
   * @return true if attribute is one of affected, false otherwise
   */
  private boolean isAffectedAttribute(String nameOfAttribute) {
    if (preferredMailFriendlyName.equals(nameOfAttribute)) {
      return true;
    } else if (isMailFriendlyName.equals(nameOfAttribute)) {
      return true;
    } else if (o365MailsFriendlyName.equals(nameOfAttribute)) {
      return true;
    } else if (publicMailsFriendlyName.equals(nameOfAttribute)) {
      return true;
    } else if (privateMailsFriendlyName.equals(nameOfAttribute)) {
      return true;
    }

    return false;
  }

  @Override
  public List<String> getStrongDependencies() {
    return Arrays.asList(
        A_U_D_preferredMail,
        A_U_D_ISMail,
        A_U_D_publicAliasMails,
        A_U_D_privateAliasMails,
        A_U_D_o365EmailAddressesMU);
  }

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_USER_ATTR_VIRT);
    attr.setFriendlyName("tcsMails:mu");
    attr.setDisplayName("Computed TCS mails for MU");
    attr.setType(ArrayList.class.getName());
    attr.setDescription("All mails for TCS. Computed from different emails in Perun.");
    return attr;
  }

}
