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
 * ResourceAttributeProcessor processes attribute-related events specifically for resources.
 * Provides implementations for handling events such as attribute setting, attribute removal,
 * and virtual attribute changes.
 *
 * Regex patterns primarily defined in `perun-ldapc.xml` are used to match the incoming messages to determine the type
 * of event and delegate the processing task to the respective method.
 */
public class ResourceAttributeProcessor extends AbstractAttributeProcessor {

  private static final Logger LOG = LoggerFactory.getLogger(ResourceAttributeProcessor.class);

  private static Pattern resourceSetPattern = Pattern.compile(" set for Resource:\\[(.*)\\]");
  private static Pattern resourceRemovePattern = Pattern.compile(" removed for Resource:\\[(.*)\\]");
  private static Pattern resourceAllAttrsRemovedPattern =
      Pattern.compile("All attributes removed for Resource:\\[(.*)\\]");
  private static Pattern resourceVirtualChangePattern = Pattern.compile(" changed for Resource:\\[(.*)\\]");

  public ResourceAttributeProcessor() {
    super(MessageBeans.RESOURCE_F, resourceSetPattern, resourceRemovePattern, resourceAllAttrsRemovedPattern,
        resourceVirtualChangePattern);
  }

  public void processAllAttributesRemoved(String msg, MessageBeans beans) {

    // ensure we have the correct beans available
    if (beans.getResource() == null) {
      return;
    }
    try {
      LOG.debug("Removing all attributes from resource {}", beans.getResource());
      perunResource.removeAllAttributes(beans.getResource());
    } catch (NamingException e) {
      LOG.error("Error removing attributes:", e);
    }
  }

  public void processAttributeRemoved(String msg, MessageBeans beans) {

    // ensure we have the correct beans available
    if (beans.getAttributeDef() == null || beans.getResource() == null) {
      return;
    }
    try {
      LOG.debug("Removing attribute {} from resource {}", beans.getAttributeDef(), beans.getResource());
      perunResource.modifyEntry(beans.getResource(), beans.getAttributeDef());
    } catch (NamingException | InternalErrorException e) {
      LOG.error("Error removing attribute:", e);
    }
  }

  public void processAttributeSet(String msg, MessageBeans beans) {

    // ensure we have the correct beans available
    if (beans.getAttribute() == null || beans.getResource() == null) {
      return;
    }
    try {
      LOG.debug("Setting attribute {} for resource {}", beans.getAttribute(), beans.getResource());
      perunResource.modifyEntry(beans.getResource(), beans.getAttribute());
    } catch (NamingException | InternalErrorException e) {
      LOG.error("Error setting attribute:", e);
    }
  }

  public void processVirtualAttributeChanged(String msg, MessageBeans beans) {
    PerunBl perun = (PerunBl) ldapcManager.getPerunBl();
    if (beans.getAttribute() == null || beans.getResource() == null) {
      return;
    }
    try {
      Attribute virtAttr = perun.getAttributesManagerBl()
          .getAttribute(ldapcManager.getPerunSession(), beans.getResource(), beans.getAttribute().getName());

      LOG.debug("Changing virtual attribute {} for resource {}", virtAttr, beans.getResource());
      perunResource.modifyEntry(beans.getResource(), virtAttr);

    } catch (InternalErrorException | AttributeNotExistsException | WrongAttributeAssignmentException |
             NamingException e) {
      LOG.error("Error changing virtual attribute:", e);
    }
  }
}
