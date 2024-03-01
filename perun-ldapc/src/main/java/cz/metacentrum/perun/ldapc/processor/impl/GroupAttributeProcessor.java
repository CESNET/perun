package cz.metacentrum.perun.ldapc.processor.impl;

import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.ldapc.processor.EventDispatcher.MessageBeans;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.NamingException;

import java.util.regex.Pattern;

public class GroupAttributeProcessor extends AbstractAttributeProcessor {

  private final static Logger log = LoggerFactory.getLogger(GroupAttributeProcessor.class);

  private static Pattern groupSetPattern = Pattern.compile(" set for Group:\\[(.*)\\]");
  private static Pattern groupRemovePattern = Pattern.compile(" removed for Group:\\[(.*)\\]");
  private static Pattern groupAllAttrsRemovedPattern = Pattern.compile("All attributes removed for Group:\\[(.*)\\]");
  private static Pattern groupVirtualChangePattern = Pattern.compile(" changed for Group:\\[(.*)\\]");

  public GroupAttributeProcessor() {
    super(MessageBeans.GROUP_F, groupSetPattern, groupRemovePattern, groupAllAttrsRemovedPattern,
        groupVirtualChangePattern);
  }

  public void processAttributeSet(String msg, MessageBeans beans) {
    // ensure we have the correct beans available
    if (beans.getAttribute() == null || beans.getGroup() == null) {
      return;
    }
    try {
      log.debug("Setting attribute {} for group {}", beans.getAttribute(), beans.getGroup());
      perunGroup.modifyEntry(beans.getGroup(), beans.getAttribute());
    } catch (NamingException | InternalErrorException e) {
      log.error("Error setting attribute {} for group {}: {}", beans.getAttribute().getId(), beans.getGroup().getId(),
          e);
    }
  }

  public void processAttributeRemoved(String msg, MessageBeans beans) {
    // ensure we have the correct beans available
    if (beans.getAttributeDef() == null || beans.getGroup() == null) {
      return;
    }
    try {
      log.debug("Removing attribute {} for group {}", beans.getAttributeDef(), beans.getGroup());
      perunGroup.modifyEntry(beans.getGroup(), beans.getAttributeDef());
    } catch (NamingException | InternalErrorException e) {
      log.error("Error removing attribute {} from group {}: {}", beans.getAttributeDef().getId(),
          beans.getGroup().getId(), e);
    }
  }

  public void processAllAttributesRemoved(String msg, MessageBeans beans) {
    // ensure we have the correct beans available
    if (beans.getGroup() == null) {
      return;
    }
    try {
      log.debug("Removing all attributes from group {}", beans.getGroup());
      perunGroup.removeAllAttributes(beans.getGroup());
    } catch (NamingException e) {
      log.error("Error removing attributes from group {}: {}", beans.getGroup().getId(), e);
    }
  }

  public void processVirtualAttributeChanged(String msg, MessageBeans beans) {
    if (beans.getAttribute() == null || beans.getGroup() == null) {
      return;
    }
    try {
      log.debug("Changing virtual attribute {} for group {}", beans.getAttribute(), beans.getGroup());
      perunGroup.modifyEntry(beans.getGroup(), ((PerunBl) ldapcManager.getPerunBl()).getAttributesManagerBl().
          getAttribute(ldapcManager.getPerunSession(), beans.getGroup(), beans.getAttribute().getName()));
    } catch (WrongAttributeAssignmentException | InternalErrorException | AttributeNotExistsException |
             NamingException e) {
      log.error("Error changing virtual attribute {} for group {}: {}", beans.getAttribute().getId(),
          beans.getGroup().getId(), e);
    }
  }

}
