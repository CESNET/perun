package cz.metacentrum.perun.ldapc.processor.impl;

import cz.metacentrum.perun.ldapc.processor.AttributeProcessor;
import cz.metacentrum.perun.ldapc.processor.EventDispatcher.MessageBeans;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * The AbstractAttributeProcessor class is an abstract base implementation for handling attribute-related events
 * in the Perun-LDAP connector.
 * This class handles pattern matching for events related to attribute changes, delegating specific processing
 * tasks to its concrete implementations.
 * This class uses regex patterns to match messages and invoke the appropriate attribute-related processing methods.
 * Concrete subclasses must implement the abstract methods to provide specific handling for each type of event.
 * Preferably register the patterns using `perun-ldapc.xml`
 */
public abstract class AbstractAttributeProcessor extends AbstractEventProcessor implements AttributeProcessor {

  private int beanFlag;
  private Pattern setPattern;
  private Pattern removePattern;
  private Pattern allAttrsRemovedPattern;
  private Pattern virtualAttrChangePattern;


  public AbstractAttributeProcessor(int beanFlag, Pattern setPattern, Pattern removePattern,
                                    Pattern allAttrsRemovedPattern, Pattern virtualAttrChangePattern) {
    super();
    this.beanFlag = beanFlag;
    this.setPattern = setPattern;
    this.removePattern = removePattern;
    this.allAttrsRemovedPattern = allAttrsRemovedPattern;
    this.virtualAttrChangePattern = virtualAttrChangePattern;
  }

  public abstract void processAllAttributesRemoved(String msg, MessageBeans beans);

  public abstract void processAttributeRemoved(String msg, MessageBeans beans);

  public abstract void processAttributeSet(String msg, MessageBeans beans);

  @Override
  public void processEvent(String msg, MessageBeans beans) {
    Matcher matcher = setPattern.matcher(msg);
    int mask = MessageBeans.ATTRIBUTE_F | beanFlag;
    if (matcher.find() && (beans.getPresentBeansMask() & mask) == mask) {
      processAttributeSet(msg, beans);
      return;
    }
    matcher = removePattern.matcher(msg);
    mask = MessageBeans.ATTRIBUTEDEF_F | beanFlag;
    if (matcher.find() && (beans.getPresentBeansMask() & mask) == mask) {
      processAttributeRemoved(msg, beans);
      return;
    }
    matcher = virtualAttrChangePattern.matcher(msg);
    mask = MessageBeans.ATTRIBUTE_F | beanFlag;
    if (matcher.find() && (beans.getPresentBeansMask() & mask) == mask) {
      processVirtualAttributeChanged(msg, beans);
    }
    mask = beanFlag;
    matcher = allAttrsRemovedPattern.matcher(msg);
    if (matcher.find() && (beans.getPresentBeansMask() & mask) == mask) {
      processAllAttributesRemoved(msg, beans);
      return;
    }
    // OK - we do not know how to handle this one
  }

  public abstract void processVirtualAttributeChanged(String msg, MessageBeans beans);

}
