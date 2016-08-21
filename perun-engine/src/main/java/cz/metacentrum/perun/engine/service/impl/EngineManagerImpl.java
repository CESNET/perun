package cz.metacentrum.perun.engine.service.impl;

import cz.metacentrum.perun.engine.runners.GenCollector;
import cz.metacentrum.perun.engine.runners.GenPlanner;
import cz.metacentrum.perun.engine.runners.SendCollector;
import cz.metacentrum.perun.engine.runners.SendPlanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cz.metacentrum.perun.engine.jms.JMSQueueManager;
import cz.metacentrum.perun.engine.scheduling.SchedulingPool;
import cz.metacentrum.perun.engine.service.EngineManager;

/**
 * Implementation of EngineManager.
 *
 * @see cz.metacentrum.perun.engine.service.EngineManager
 *
 * @author Michal Karm Babacek
 * @author Michal Voců
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
@org.springframework.stereotype.Service(value = "engineManager")
public class EngineManagerImpl implements EngineManager {

	private final static Logger log = LoggerFactory.getLogger(EngineManagerImpl.class);

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


	public GenPlanner getGenPlanner() {
		return genPlanner;
	}

	@Autowired
	public void setGenPlanner(GenPlanner genPlanner) {
		this.genPlanner = genPlanner;
	}

	public GenCollector getGenCollector() {
		return genCollector;
	}

	@Autowired
	public void setGenCollector(GenCollector genCollector) {
		this.genCollector = genCollector;
	}

	public SendPlanner getSendPlanner() {
		return sendPlanner;
	}

	@Autowired
	public void setSendPlanner(SendPlanner sendPlanner) {
		this.sendPlanner = sendPlanner;
	}

	public SendCollector getSendCollector() {
		return sendCollector;
	}

	@Autowired
	public void setSendCollector(SendCollector sendCollector) {
		this.sendCollector = sendCollector;
	}

	public JMSQueueManager getJmsQueueManager() {
		return jmsQueueManager;
	}

	@Autowired
	public void setJmsQueueManager(JMSQueueManager jmsQueueManager) {
		this.jmsQueueManager = jmsQueueManager;
	}

	public SchedulingPool getSchedulingPool() {
		return schedulingPool;
	}

	@Autowired
	public void setSchedulingPool(SchedulingPool schedulingPool) {
		this.schedulingPool = schedulingPool;
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
