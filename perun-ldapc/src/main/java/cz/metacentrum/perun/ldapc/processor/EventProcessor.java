package cz.metacentrum.perun.ldapc.processor;

import cz.metacentrum.perun.ldapc.processor.EventDispatcher.DispatchEventCondition;
import cz.metacentrum.perun.ldapc.processor.EventDispatcher.MessageBeans;

import java.util.Collection;

public interface EventProcessor {

	public void setDispatchConditions(Collection<DispatchEventCondition> condition);

	public void processEvent(String msg, MessageBeans beans);
}
