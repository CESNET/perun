package cz.metacentrum.perun.ldapc.processor;

import cz.metacentrum.perun.ldapc.processor.EventDispatcher.DispatchEventCondition;
import cz.metacentrum.perun.ldapc.processor.EventDispatcher.MessageBeans;
import java.util.Collection;

/**
 * The EventProcessor interface provides a structure for processing events retrieved from the Perun core.
 * Implementations of this interface are responsible for managing the conditions for events to be handled (mainly via
 * `perun-ldapc.xml`) and then for processing the events matching their conditions with the appropriate methods.
 */
public interface EventProcessor {

  public void processEvent(String msg, MessageBeans beans);

  public void setDispatchConditions(Collection<DispatchEventCondition> condition);
}
