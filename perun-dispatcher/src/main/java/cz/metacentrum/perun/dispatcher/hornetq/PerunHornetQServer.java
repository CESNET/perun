package cz.metacentrum.perun.dispatcher.hornetq;

import java.util.Properties;

import javax.annotation.Resource;

import org.hornetq.core.config.impl.FileConfiguration;
import org.hornetq.core.server.HornetQServer;
import org.hornetq.core.server.HornetQServers;
import org.hornetq.jms.server.JMSServerManager;
import org.hornetq.jms.server.impl.JMSServerManagerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Start / stop and configure HornetQ server for Perun.
 *
 * @author Michal Karm Babacek
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
@Service(value = "perunHornetQServer")
public class PerunHornetQServer {

	private final static Logger log = LoggerFactory.getLogger(PerunHornetQServer.class);

	private Properties dispatcherProperties;
	private FileConfiguration configuration = null;
	private HornetQServer server = null;
	private JMSServerManager jmsServerManager = null;
	private boolean serverRunning = false;


	// ----- setters -------------------------------------


	public Properties getDispatcherProperties() {
		return dispatcherProperties;
	}


	@Resource(name="dispatcherPropertiesBean")
	public void setDispatcherProperties(Properties dispatcherProperties) {
		this.dispatcherProperties = dispatcherProperties;
	}


	// ----- methods -------------------------------------


	/**
	 * Start and configure HornetQ server with default JMS queue (systemQueue).
	 */
	public void startServer() {
		try {

			log.debug("Starting HornetQ server...");
			System.setProperty("perun.dispatcher.hornetq.remoting.netty.host", dispatcherProperties.getProperty("dispatcher.ip.address"));
			System.setProperty("perun.dispatcher.hornetq.remoting.netty.port", dispatcherProperties.getProperty("dispatcher.port"));
			System.setProperty("perun.dispatcher.hornetq.datadir", dispatcherProperties.getProperty("dispatcher.datadir"));

			// config from dispatcher classpath
			configuration = new FileConfiguration();
			configuration.setConfigurationUrl("hornetq-configuration.xml");
			configuration.start();

			server = HornetQServers.newHornetQServer(configuration);
			jmsServerManager = new JMSServerManagerImpl(server,"hornetq-jms.xml");
			// if you want to use JNDI, simple inject a context here or don't
			// call this method and make sure the JNDI parameters are set.
			jmsServerManager.setContext(null);
			jmsServerManager.start();
			serverRunning = true;
			log.debug("HornetQ server started.");

		} catch (Exception e) {
			log.error("Can't start HornetQ server: {}", e);
		}
	}

	/**
	 * Stop HornetQ server.
	 */
	public void stopServer() {
		if (serverRunning && jmsServerManager != null) {
			try {
				jmsServerManager.stop();
				server.stop();
				configuration.stop();
				serverRunning = false;
				log.debug("HornetQ server has stopped.");
			} catch (Exception e) {
				log.error("Can't stop HornetQ server: {}", e);
			}
		}
	}

	/**
	 * Gets JMS server manager
	 *
	 * @return JMS server manager
	 */
	public JMSServerManager getJMSServerManager() {
		return jmsServerManager;
	}

	/**
	 * TRUE if HornetQ server was correctly started.
	 *
	 * @return TRUE HornetQ is running / FALSE otherwise
	 */
	public boolean isServerRunning() {
		return serverRunning;
	}

}
