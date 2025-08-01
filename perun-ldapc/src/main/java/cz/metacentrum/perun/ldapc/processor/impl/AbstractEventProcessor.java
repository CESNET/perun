package cz.metacentrum.perun.ldapc.processor.impl;

import cz.metacentrum.perun.ldapc.model.PerunFacility;
import cz.metacentrum.perun.ldapc.model.PerunGroup;
import cz.metacentrum.perun.ldapc.model.PerunResource;
import cz.metacentrum.perun.ldapc.model.PerunUser;
import cz.metacentrum.perun.ldapc.model.PerunVO;
import cz.metacentrum.perun.ldapc.processor.EventDispatcher;
import cz.metacentrum.perun.ldapc.processor.EventDispatcher.DispatchEventCondition;
import cz.metacentrum.perun.ldapc.processor.EventProcessor;
import cz.metacentrum.perun.ldapc.service.LdapcManager;
import java.util.ArrayList;
import java.util.Collection;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The AbstractEventProcessor provides a base implementation for processing events in the Perun-LDAP connector.
 * It mainly handles the registration of Processors and their conditions in the EventDispatcher.
 * Implementing classes are then responsible for implementing the handler methods for each event (`processEvent` being
 * the fallback method).
 */
public abstract class AbstractEventProcessor implements EventProcessor, InitializingBean {

  @Autowired
  protected LdapcManager ldapcManager;
  @Autowired
  protected PerunGroup perunGroup;
  @Autowired
  protected PerunResource perunResource;
  @Autowired
  protected PerunFacility perunFacility;
  @Autowired
  protected PerunUser perunUser;
  @Autowired
  protected PerunVO perunVO;
  protected Collection<DispatchEventCondition> dispatchConditions;
  private EventDispatcher eventDispatcher;

  @Override
  public void afterPropertiesSet() throws Exception {
    for (DispatchEventCondition dispatchEventCondition : dispatchConditions) {
      eventDispatcher.registerProcessor(this, dispatchEventCondition);
    }
  }

  @Autowired
  @Override
  public void setDispatchConditions(Collection<DispatchEventCondition> condition) {
    if (dispatchConditions == null) {
      dispatchConditions = new ArrayList<DispatchEventCondition>(10);
    }
    dispatchConditions.addAll(condition);
  }

  @Autowired
  public void setEventDispatcher(EventDispatcher eventDispatcher) {
    this.eventDispatcher = eventDispatcher;
  }

  protected interface PerunAttributeNames {

  }
}
