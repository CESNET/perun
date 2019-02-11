package cz.metacentrum.perun.ldapc.processor.impl;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;

import cz.metacentrum.perun.ldapc.model.PerunGroup;
import cz.metacentrum.perun.ldapc.model.PerunResource;
import cz.metacentrum.perun.ldapc.model.PerunUser;
import cz.metacentrum.perun.ldapc.model.PerunVO;
import cz.metacentrum.perun.ldapc.processor.EventDispatcher;
import cz.metacentrum.perun.ldapc.processor.EventDispatcher.DispatchEventCondition;
import cz.metacentrum.perun.ldapc.service.LdapcManager;
import cz.metacentrum.perun.ldapc.processor.EventProcessor;

public abstract class AbstractEventProcessor implements EventProcessor, InitializingBean {

	protected interface PerunAttributeNames {
		
	}
	
	private EventDispatcher eventDispatcher;
	
	@Autowired
	protected LdapcManager ldapcManager;
	@Autowired
	protected PerunGroup perunGroup;
	@Autowired
	protected PerunResource perunResource;
	@Autowired
	protected PerunUser perunUser;
	@Autowired
	protected PerunVO perunVO;
	
	protected Collection<DispatchEventCondition> dispatchConditions;
	
	@Required
	@Autowired
	public void setEventDispatcher(EventDispatcher eventDispatcher) {
		this.eventDispatcher = eventDispatcher;
	}

	@Required
	@Override
	public void setDispatchConditions(Collection<DispatchEventCondition> condition) {
		if(dispatchConditions == null) dispatchConditions = new ArrayList<DispatchEventCondition>(10);
		dispatchConditions.addAll(condition);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		for (DispatchEventCondition dispatchEventCondition : dispatchConditions) {
			eventDispatcher.registerProcessor(this, dispatchEventCondition);
		}
	}

	
}
