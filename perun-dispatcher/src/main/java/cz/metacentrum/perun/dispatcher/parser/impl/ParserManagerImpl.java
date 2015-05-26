package cz.metacentrum.perun.dispatcher.parser.impl;

import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;

import cz.metacentrum.perun.dispatcher.parser.AuditerListener;
import cz.metacentrum.perun.dispatcher.parser.Parser;
import cz.metacentrum.perun.dispatcher.parser.ParserManager;
import cz.metacentrum.perun.dispatcher.processing.EventQueue;
import org.springframework.stereotype.Service;

/**
 * 
 * @author Michal Karm Babacek JavaDoc coming soon...
 * 
 */
@Service(value = "parserManager")
public class ParserManagerImpl implements ParserManager {

	private final static Logger log = LoggerFactory
			.getLogger(ParserManager.class);
	// private List<Parser> parsers = new ArrayList<Parser>();
	@Autowired
	private EventQueue eventQueue;
	@Autowired
	private Properties dispatcherPropertiesBean;
	@Autowired
	private AuditerListener auditerListener;
	@Autowired
	private TaskExecutor taskExecutor;

	// @Autowired
	// private TaskExecutor taskExecutor;
	// @Autowired
	// private Parser parserPerunDB;
	// @Autowired
	// private Parser parserGrouper;

	@Override
	public void summonParsers() {
		// taskExecutor.execute(parserPerunDB);
		// parsers.add(parserPerunDB);
		// taskExecutor.execute(parserGrouper);
		// parsers.add(parserGrouper);
		String name = dispatcherPropertiesBean.getProperty("dispatcher.ip.address") + ":"
				+ dispatcherPropertiesBean.getProperty("dispatcher.port");
		auditerListener.setDispatcherName(name);
		auditerListener.setEventQueue(eventQueue);
		taskExecutor.execute(new Runnable() {

			@Override
			public void run() {
				auditerListener.init();

			}
		});
		// auditer.log(message);
		// auditer.flush();
		log.debug("I have created a new AuditerListenerImpl:"
				+ auditerListener.toString());
	}

	@Override
	public List<Parser> getParsers() {
		return null;
	}

	@Override
	public void disposeParsers() {
		// setAuditerListener(null);
		// TODO
	}

	public AuditerListener getAuditerListener() {
		return auditerListener;
	}

	public void setAuditerListener(AuditerListener auditerListener) {
		this.auditerListener = auditerListener;
	}

	public void setEventQueue(EventQueue eventQueue) {
		this.eventQueue = eventQueue;
	}

	public EventQueue getEventQueue() {
		return eventQueue;
	}

	public void setPropertiesBean(Properties propertiesBean) {
		this.dispatcherPropertiesBean = propertiesBean;
	}

	public Properties getPropertiesBean() {
		return dispatcherPropertiesBean;
	}

	public void setTaskExecutor(TaskExecutor taskExecutor) {
		this.taskExecutor = taskExecutor;
	}

	public TaskExecutor getTaskExecutor() {
		return taskExecutor;
	}

	/*
	 * public void setTaskExecutor(TaskExecutor taskExecutor) {
	 * this.taskExecutor = taskExecutor; }
	 * 
	 * public TaskExecutor getTaskExecutor() { return taskExecutor; }
	 * 
	 * public Parser getParserPerunDB() { return parserPerunDB; }
	 * 
	 * public void setParserPerunDB(Parser parserPerunDB) { this.parserPerunDB =
	 * parserPerunDB; }
	 * 
	 * public Parser getParserGrouper() { return parserGrouper; }
	 * 
	 * public void setParserGrouper(Parser parserGrouper) { this.parserGrouper =
	 * parserGrouper; }
	 */

}
