package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.AttributesManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.impl.modules.CeitecCrmConnector;
import cz.metacentrum.perun.core.impl.modules.ModulesYamlConfigLoader;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleImplApi;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Attribute represents CN (common name) of user in a CEITECs ActiveDirectory server.
 * <p>
 * It's value must be unique and have form of "lastName firstName [number]" where number is optional and starts with 2
 * when more than one user has same name.
 *
 * @author Pavel Zlamal <zlamal@cesnet.cz>
 */
public class urn_perun_user_attribute_def_def_cnCeitecAD extends UserAttributesModuleAbstract
    implements UserAttributesModuleImplApi {

  private static final CeitecCrmConnector ceitecCrmConnector = new CeitecCrmConnector();
  private static final ModulesYamlConfigLoader loader = new ModulesYamlConfigLoader();

  @Override
  public void checkAttributeSemantics(PerunSessionImpl perunSession, User user, Attribute attribute)
      throws WrongReferenceAttributeValueException {

    if (attribute.getValue() == null) {
      throw new WrongReferenceAttributeValueException(attribute, null, user, null, "Value can't be null");
    }

    // check existing DN
    Set<User> usersWithSameCN =
        new HashSet<>(perunSession.getPerunBl().getUsersManagerBl().getUsersByAttribute(perunSession, attribute));
    // check existing DN without accents
    String normalizedValue =
        java.text.Normalizer.normalize((String) attribute.getValue(), java.text.Normalizer.Form.NFD)
            .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    if (!Objects.equals(normalizedValue, attribute.getValue())) {
      Attribute changedAttribute = new Attribute(attribute);
      changedAttribute.setValue(normalizedValue);
      usersWithSameCN.addAll(
          perunSession.getPerunBl().getUsersManagerBl().getUsersByAttribute(perunSession, changedAttribute));
    }
    usersWithSameCN.remove(user); //remove self
    if (!usersWithSameCN.isEmpty()) {
      if (usersWithSameCN.size() > 1) {
        throw new ConsistencyErrorException("FATAL ERROR: Duplicated CN detected." + attribute + " " + usersWithSameCN);
      }
      throw new WrongReferenceAttributeValueException(attribute, attribute, user, null,
          usersWithSameCN.iterator().next(), null,
          "This CN " + attribute.getValue() + " is already occupied for CEITEC AD");
    }

  }

  @Override
  public Attribute fillAttribute(PerunSessionImpl session, User user, AttributeDefinition attribute) {

    Attribute filledAttribute = new Attribute(attribute);
    if (loader.moduleFileExists("Ceitec")) {
      return fillAttributeUsingCRM(session, user, filledAttribute);
    } else {
      return fillAttributeLocally(session, user, filledAttribute);
    }

  }

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
    attr.setFriendlyName("cnCeitecAD");
    attr.setDisplayName("CN in CEITEC AD");
    attr.setType(String.class.getName());
    attr.setDescription(
        "Users CN in CEITEC AD, it must have form of \"lastName firstName [number]\" where number starts with 2 for " +
        "users with same name.");
    return attr;
  }

  private Attribute fillAttributeLocally(PerunSessionImpl session, User user, Attribute filledAttribute) {

    String firstName = user.getFirstName();
    String lastName = user.getLastName();

    if (firstName == null || lastName == null) {
      // unable to fill
      return filledAttribute;
    }

    firstName = java.text.Normalizer.normalize(firstName, java.text.Normalizer.Form.NFD)
            .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    lastName = java.text.Normalizer.normalize(lastName, java.text.Normalizer.Form.NFD)
            .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

    int iterator = 1;
    while (iterator >= 1) {
      if (iterator > 1) {
        filledAttribute.setValue(lastName + " " + firstName + " " + iterator);
      } else {
        filledAttribute.setValue(lastName + " " + firstName);
      }
      try {
        checkAttributeSemantics(session, user, filledAttribute);
        return filledAttribute;
      } catch (WrongReferenceAttributeValueException ex) {
        // continue in a WHILE cycle
        iterator++;
      }
    }

    return filledAttribute;

  }

  private Attribute fillAttributeUsingCRM(PerunSessionImpl perunSession, User user, Attribute filledAttribute) {

    AttributesManagerBl attributesManagerBl = perunSession.getPerunBl().getAttributesManagerBl();
    try {
      // Exclusive lock for transaction
      Attribute lock = attributesManagerBl.getEntitylessAttributeForUpdate(perunSession, "CRM",
              AttributesManager.NS_ENTITYLESS_ATTR_DEF + ":ceitecCrmLock");
      String cn = getCNFromCRM(perunSession, user);
      filledAttribute.setValue(cn);
      return filledAttribute;
    } catch (AttributeNotExistsException e) {
      throw new InternalErrorException("Locking attribute ceitecCrmLock not found.");
    }

  }

  private String getCNFromCRM(PerunSessionImpl perunSession, User user) {

    PerunBl perunBl = perunSession.getPerunBl();
    Attribute eppns = null;
    Attribute ceitecId = null;

    try {
      eppns = perunBl.getAttributesManagerBl().getAttribute(perunSession, user,
              AttributesManager.NS_USER_ATTR_VIRT + ":eduPersonPrincipalNames");
    } catch (WrongAttributeAssignmentException | AttributeNotExistsException e) {
      throw new InternalErrorException(e);
    }

    try {
      ceitecId = perunBl.getAttributesManagerBl().getAttribute(perunSession, user,
              AttributesManager.NS_USER_ATTR_DEF + ":ceitecId");
    } catch (WrongAttributeAssignmentException | AttributeNotExistsException e) {
      throw new InternalErrorException(e);
    }

    try {
      return ceitecCrmConnector.getCn(ceitecId.valueAsString(), eppns, user);
    } catch (Exception ex) {
      return null;
    }

  }


}
