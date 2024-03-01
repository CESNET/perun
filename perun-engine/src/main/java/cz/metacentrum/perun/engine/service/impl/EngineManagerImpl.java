package cz.metacentrum.perun.engine.service.impl;

import cz.metacentrum.perun.engine.jms.JMSQueueManager;
import cz.metacentrum.perun.engine.runners.GenCollector;
import cz.metacentrum.perun.engine.runners.GenPlanner;
import cz.metacentrum.perun.engine.runners.SendCollector;
import cz.metacentrum.perun.engine.runners.SendPlanner;
import cz.metacentrum.perun.engine.scheduling.SchedulingPool;
import cz.metacentrum.perun.engine.service.EngineManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Implementation of EngineManager.
 *
 * @author Michal Karm Babacek
 * @author Michal Voců
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 * @see cz.metacentrum.perun.engine.service.EngineManager
 */
@org.springframework.stereotype.Service(value = "engineManager")
public class EngineManagerImpl implements EngineManager {

  private static final Logger LOG = LoggerFactory.getLogger(EngineManagerImpl.class);

  private GenPlanner genPlanner;
  private GenCollector genCollector;
  private SendPlanner sendPlanner;
  private SendCollector sendCollector;
  private JMSQueueManager jmsQueueManager;
  private SchedulingPool schedulingPool;

  public EngineManagerImpl() {
  }

  public EngineManagerImpl(JMSQueueManager jmsQueueManager, SchedulingPool schedulingPool) {
    this.jmsQueueManager = jmsQueueManager;
    this.schedulingPool = schedulingPool;
  }


  // ----- setters ------------------------------

  public GenCollector getGenCollector() {
    return genCollector;
  }

  public GenPlanner getGenPlanner() {
    return genPlanner;
  }

  public JMSQueueManager getJmsQueueManager() {
    return jmsQueueManager;
  }

  public SchedulingPool getSchedulingPool() {
    return schedulingPool;
  }

  public SendCollector getSendCollector() {
    return sendCollector;
  }

  public SendPlanner getSendPlanner() {
    return sendPlanner;
  }

  @Autowired
  public void setGenCollector(GenCollector genCollector) {
    this.genCollector = genCollector;
  }

  @Autowired
  public void setGenPlanner(GenPlanner genPlanner) {
    this.genPlanner = genPlanner;
  }

  @Autowired
  public void setJmsQueueManager(JMSQueueManager jmsQueueManager) {
    this.jmsQueueManager = jmsQueueManager;
  }

  @Autowired
  public void setSchedulingPool(SchedulingPool schedulingPool) {
    this.schedulingPool = schedulingPool;
  }

  @Autowired
  public void setSendCollector(SendCollector sendCollector) {
    this.sendCollector = sendCollector;
  }

  @Autowired
  public void setSendPlanner(SendPlanner sendPlanner) {
    this.sendPlanner = sendPlanner;
  }


  // ----- methods ------------------------------

  @Override
  public void startMessaging() {
    jmsQueueManager.start();
  }

  @Override
  public void startRunnerThreads() {
    new Thread(genPlanner, "genPlanner").start();
    new Thread(genCollector, "genCollector").start();
    new Thread(sendPlanner, "sendPlanner").start();
    new Thread(sendCollector, "sendCollector").start();
  }

  @Override
  public void stopRunnerThreads() {
    genPlanner.stop();
    genCollector.stop();
    sendPlanner.stop();
    sendCollector.stop();
  }

}
