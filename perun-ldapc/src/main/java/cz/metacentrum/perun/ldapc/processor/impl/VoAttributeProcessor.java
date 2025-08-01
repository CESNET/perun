package cz.metacentrum.perun.ldapc.processor.impl;

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
 * VoAttributeProcessor processes attribute-related events specifically for VOs.
 * Provides implementations for handling events such as attribute setting, attribute removal,
 * and virtual attribute changes.
 *
 * Regex patterns primarily defined in `perun-ldapc.xml` are used to match the incoming messages to determine the type
 * of event and delegate the processing task to the respective method.
 */
public class VoAttributeProcessor extends AbstractAttributeProcessor {

  private static final Logger LOG = LoggerFactory.getLogger(VoAttributeProcessor.class);

  private static Pattern voSetPattern = Pattern.compile(" set for Vo:\\[(.*)\\]");
  private static Pattern voRemovePattern = Pattern.compile(" removed for Vo:\\[(.*)\\]");
  private static Pattern voAllAttrsRemovedPattern = Pattern.compile("All attributes removed for Vo:\\[(.*)\\]");
  private static Pattern voVirtualChangePattern = Pattern.compile(" changed for Vo:\\[(.*)\\]");

  public VoAttributeProcessor() {
    super(MessageBeans.VO_F, voSetPattern, voRemovePattern, voAllAttrsRemovedPattern, voVirtualChangePattern);
  }

  public void processAllAttributesRemoved(String msg, MessageBeans beans) {
    // ensure we have the correct beans available
    if (beans.getVo() == null) {
      return;
    }
    try {
      LOG.debug("Removing all attributes from vo {}", beans.getVo());
      perunVO.removeAllAttributes(beans.getVo());
    } catch (NamingException e) {
      LOG.error("Error removing attributes from vo {}: {}", beans.getVo().getId(), e);
    }
  }

  public void processAttributeRemoved(String msg, MessageBeans beans) {
    // ensure we have the correct beans available
    if (beans.getAttributeDef() == null || beans.getVo() == null) {
      return;
    }
    try {
      LOG.debug("Removing attribute {} for vo {}", beans.getAttributeDef(), beans.getVo());
      perunVO.modifyEntry(beans.getVo(), beans.getAttributeDef());
    } catch (NamingException | InternalErrorException e) {
      LOG.error("Error removing attribute {} from vo {}: {}", beans.getAttributeDef().getId(), beans.getVo().getId(),
          e);
    }
  }

  public void processAttributeSet(String msg, MessageBeans beans) {
    // ensure we have the correct beans available
    if (beans.getAttribute() == null || beans.getVo() == null) {
      return;
    }
    try {
      LOG.debug("Setting attribute {} for vo {}", beans.getAttribute(), beans.getVo());
      perunVO.modifyEntry(beans.getVo(), beans.getAttribute());
    } catch (NamingException | InternalErrorException e) {
      LOG.error("Error setting attribute {} for vo {}: {}", beans.getAttribute().getId(), beans.getVo().getId(), e);
    }
  }

  public void processVirtualAttributeChanged(String msg, MessageBeans beans) {
    if (beans.getAttribute() == null || beans.getVo() == null) {
      return;
    }
    try {
      LOG.debug("Changing virtual attribute {} for vo {}", beans.getAttribute(), beans.getVo());
      perunVO.modifyEntry(beans.getVo(), ((PerunBl) ldapcManager.getPerunBl()).getAttributesManagerBl()
          .getAttribute(ldapcManager.getPerunSession(), beans.getVo(), beans.getAttribute().getName()));
    } catch (WrongAttributeAssignmentException | InternalErrorException | AttributeNotExistsException |
             NamingException e) {
      LOG.error("Error changing virtual attribute {} for vo {}: {}", beans.getAttribute().getId(),
          beans.getVo().getId(), e);
    }
  }

}
