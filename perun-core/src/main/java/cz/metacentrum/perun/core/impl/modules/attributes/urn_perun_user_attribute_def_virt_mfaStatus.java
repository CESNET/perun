package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleAbstract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Attribute module for mfaStatus attribute.
 * Contains string based on the value of mfaEnforceSettings (ENFORCED_ALL, ENFORCED_PARTIALLY, <empty string>).
 *
 * @author Matej Hakoš <492968@mail.muni.cz>
 */
public class urn_perun_user_attribute_def_virt_mfaStatus extends UserVirtualAttributesModuleAbstract {
  private static final String MFA_ENFORCE_SETTINGS = AttributesManager.NS_USER_ATTR_DEF + ":mfaEnforceSettings";
  private static Logger log = LoggerFactory.getLogger(urn_perun_user_attribute_def_virt_mfaStatus.class);

  @Override
  public Attribute getAttributeValue(PerunSessionImpl perunSession, User user, AttributeDefinition attribute) {
    Attribute attr = new Attribute(attribute);
    String namespace = attribute.getFriendlyNameParameter();

    String mfaEnforceSettings = MFA_ENFORCE_SETTINGS + ":" + namespace;
    try {
      Attribute mfaSettings =
          perunSession.getPerunBl().getAttributesManagerBl().getAttribute(perunSession, user, mfaEnforceSettings);
      if (mfaSettings.getValue() == null || mfaSettings.getValue() == "") {
        attr.setValue("");
      } else if (mfaSettings.getValue().equals("{\"all\":true}")) {
        attr.setValue("ENFORCED_ALL");
      } else {
        attr.setValue("ENFORCED_PARTIALLY");
      }
    } catch (AttributeNotExistsException e) {
      // Log non-existing attribute
      log.warn("Attribute {} does not exist.", mfaEnforceSettings);
    } catch (WrongAttributeAssignmentException e) {
      // If the attribute does not exist, mfaStatus is empty string
    }
    return attr;
  }
}
