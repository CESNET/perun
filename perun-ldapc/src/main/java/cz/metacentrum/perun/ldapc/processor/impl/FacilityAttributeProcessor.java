package cz.metacentrum.perun.ldapc.processor.impl;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.ldapc.processor.EventDispatcher.MessageBeans;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.NamingException;

/**
 * FacilityAttributeProcessor processes attribute-related events specifically for facilities.
 * Provides implementations for handling events such as attribute setting, attribute removal,
 * and virtual attribute changes.
 *
 * Regex patterns primarily defined in `perun-ldapc.xml` are used to match the incoming messages to determine the type
 * of event and delegate the processing task to the respective method.
 */
public class FacilityAttributeProcessor extends AbstractAttributeProcessor {

  private static final Logger LOG = LoggerFactory.getLogger(FacilityAttributeProcessor.class);

  private static Pattern facilitySetPattern = Pattern.compile(" set for Facility:\\[(.*)\\]");
  private static Pattern facilityRemovePattern = Pattern.compile(" removed for Facility:\\[(.*)\\]");
  private static Pattern facilityAllAttrsRemovedPattern =
      Pattern.compile("All attributes removed for Facility:\\[(.*)\\]");
  private static Pattern facilityVirtualChangePattern = Pattern.compile(" changed for Facility:\\[(.*)\\]");

  public FacilityAttributeProcessor() {
    super(MessageBeans.FACILITY_F, facilitySetPattern, facilityRemovePattern, facilityAllAttrsRemovedPattern,
        facilityVirtualChangePattern);
  }

  public void processAllAttributesRemoved(String msg, MessageBeans beans) {

    // ensure we have the correct beans available
    if (beans.getFacility() == null) {
      return;
    }
    try {
      LOG.debug("Removing all attributes from facility {}", beans.getFacility());
      perunFacility.removeAllAttributes(beans.getFacility());
    } catch (NamingException e) {
      LOG.error("Error removing attributes:", e);
    }
  }

  public void processAttributeRemoved(String msg, MessageBeans beans) {

    // ensure we have the correct beans available
    if (beans.getAttributeDef() == null || beans.getFacility() == null) {
      return;
    }
    try {
      LOG.debug("Removing attribute {} from facility {}", beans.getAttributeDef(), beans.getFacility());
      perunFacility.modifyEntry(beans.getFacility(), beans.getAttributeDef());
    } catch (NamingException | InternalErrorException e) {
      LOG.error("Error removing attribute:", e);
    }
  }

  public void processAttributeSet(String msg, MessageBeans beans) {

    // ensure we have the correct beans available
    if (beans.getAttribute() == null || beans.getFacility() == null) {
      return;
    }
    try {
      LOG.debug("Setting attribute {} for facility {}", beans.getAttribute(), beans.getFacility());
      perunFacility.modifyEntry(beans.getFacility(), beans.getAttribute());
    } catch (NamingException | InternalErrorException e) {
      LOG.error("Error setting attribute:", e);
    }
  }

  public void processVirtualAttributeChanged(String msg, MessageBeans beans) {
    PerunBl perun = (PerunBl) ldapcManager.getPerunBl();
    if (beans.getAttribute() == null || beans.getFacility() == null) {
      return;
    }
    try {
      Attribute virtAttr = perun.getAttributesManagerBl()
          .getAttribute(ldapcManager.getPerunSession(), beans.getFacility(), beans.getAttribute().getName());

      LOG.debug("Changing virtual attribute {} for facility {}", virtAttr, beans.getFacility());
      perunFacility.modifyEntry(beans.getFacility(), virtAttr);

    } catch (InternalErrorException | AttributeNotExistsException | WrongAttributeAssignmentException |
             NamingException e) {
      LOG.error("Error changing virtual attribute:", e);
    }
  }
}
