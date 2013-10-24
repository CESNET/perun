package cz.metacentrum.perun.dispatcher.processing.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;

import cz.metacentrum.perun.dispatcher.jms.DispatcherQueue;
import cz.metacentrum.perun.dispatcher.jms.DispatcherQueuePool;
import cz.metacentrum.perun.dispatcher.model.Event;
import cz.metacentrum.perun.dispatcher.processing.EventLogger;
import cz.metacentrum.perun.dispatcher.processing.EventProcessor;
import cz.metacentrum.perun.dispatcher.processing.EventQueue;
import cz.metacentrum.perun.dispatcher.processing.SmartMatcher;

/**
 * 
 * @author Michal Karm Babacek JavaDoc coming soon...
 * 
 */
@org.springframework.stereotype.Service(value = "eventProcessor")
public class EventProcessorImpl implements EventProcessor {

	private final static Logger log = LoggerFactory.getLogger(EventProcessorImpl.class);

	@Autowired
	private EventQueue eventQueue;
	@Autowired
	private DispatcherQueuePool dispatcherQueuePool;
	@Autowired
	private EventLogger eventLogger;
	@Autowired
	private SmartMatcher smartMatcher;
	@Autowired
	private TaskExecutor taskExecutor;

	private EvProcessor evProcessor;

	public class EvProcessor implements Runnable {
		private boolean running = true;

		@Override
		public void run() {
			if (log.isDebugEnabled()) {
				log.debug("DEBUG LEVEL ENABLED:" + log.isDebugEnabled());
			}
			while (running) {
				try {
					Event event = eventQueue.poll();
					if (event != null) {
						if (log.isDebugEnabled()) {
							log.debug("Events in Queue(" + eventQueue.size() + ").Dispatchers("+dispatcherQueuePool.poolSize()+").Processing event...");
						}
						boolean orphan = true;
						for (DispatcherQueue dispatcherQueue : dispatcherQueuePool.getPool()) {
							long timeStamp = 0;
							if (log.isDebugEnabled()) {
								timeStamp = System.currentTimeMillis();
							}
							if (smartMatcher.doesItMatch(event, dispatcherQueue)) {
								orphan = false;
								dispatcherQueue.sendMessage(event.toString());
								eventLogger.logEvent(event, dispatcherQueue.getClientID());
								if (log.isDebugEnabled()) {
									long timeStamp2 = System.currentTimeMillis();
									log.debug("MATCH OK (took " + (timeStamp2 - timeStamp) + "ms) for " + dispatcherQueue.getClientID() + " AND " + event.toString());
								}
								break;
							}
							if (log.isDebugEnabled()) {
								long timeStamp2 = System.currentTimeMillis();
								log.debug("NO MATCH (took " + (timeStamp2 - timeStamp) + "ms) for " + dispatcherQueue.getClientID() + " AND " + event.toString());
							}
						}
						if (orphan) {
							eventLogger.logEvent(event, -1);
						}
					}
				} catch (Exception e) {
					log.error(e.toString(), e);
				}
				try {
					//TODO: Remove?
					Thread.sleep(10);
				} catch (InterruptedException e) {
					log.error(e.getMessage(), e);
				}
			}
		}

		public void stop() {
			running = false;
		}
	}

	@Override
	public void startPocessingEvents() {
		try {
			taskExecutor.execute(new EvProcessor());
		} catch (Exception e) {
			log.error(e.toString(), e);
		}
	}

	@Override
	public void stopPocessingEvents() {
		evProcessor.stop();
	}

	public EventQueue getEventQueue() {
		return eventQueue;
	}

	public void setEventQueue(EventQueue eventQueue) {
		this.eventQueue = eventQueue;
	}

	public DispatcherQueuePool getDispatcherQueuePool() {
		return dispatcherQueuePool;
	}

	public void setDispatcherQueuePool(DispatcherQueuePool dispatcherQueuePool) {
		this.dispatcherQueuePool = dispatcherQueuePool;
	}

	public EventLogger getEventLogger() {
		return eventLogger;
	}

	public void setEventLogger(EventLogger eventLogger) {
		this.eventLogger = eventLogger;
	}

	public SmartMatcher getSmartMatcher() {
		return smartMatcher;
	}

	public void setSmartMatcher(SmartMatcher smartMatcher) {
		this.smartMatcher = smartMatcher;
	}

	public TaskExecutor getTaskExecutor() {
		return taskExecutor;
	}

	public void setTaskExecutor(TaskExecutor taskExecutor) {
		this.taskExecutor = taskExecutor;
	}

}
