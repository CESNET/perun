package cz.metacentrum.perun.dispatcher.hornetq;

import java.util.Properties;

import org.hornetq.core.config.impl.FileConfiguration;
import org.hornetq.core.server.HornetQServer;
import org.hornetq.core.server.HornetQServers;
import org.hornetq.jms.server.JMSServerManager;
import org.hornetq.jms.server.impl.JMSServerManagerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author Michal Karm Babacek JavaDoc coming soon...
 * 
 */
@org.springframework.stereotype.Service(value = "perunHornetQServer")
public class PerunHornetQServer {

	private final static Logger log = LoggerFactory
			.getLogger(PerunHornetQServer.class);

	@Autowired
	private Properties dispatcherPropertiesBean;
	private FileConfiguration configuration = null;
	private HornetQServer server = null;
	private JMSServerManager jmsServerManager = null;
	private boolean serverRunning = false;

	public void startServer() {
		try {

			System.setProperty("perun.dispatcher.hornetq.remoting.netty.host",
					dispatcherPropertiesBean.getProperty("dispatcher.ip.address"));
			System.setProperty("perun.dispatcher.hornetq.remoting.netty.port",
					dispatcherPropertiesBean.getProperty("dispatcher.port"));
			System.setProperty("perun.dispatcher.hornetq.datadir",
					dispatcherPropertiesBean.getProperty("dispatcher.datadir"));
			
			configuration = new FileConfiguration();
			configuration.setConfigurationUrl("hornetq-configuration.xml");
			configuration.start();

			server = HornetQServers.newHornetQServer(configuration);
			jmsServerManager = new JMSServerManagerImpl(server,
					"hornetq-jms.xml");
			// if you want to use JNDI, simple inject a context here or don't
			// call this method and make sure the JNDI parameters are set.
			jmsServerManager.setContext(null);
			jmsServerManager.start();
			serverRunning = true;

		} catch (Exception e) {
			log.error(e.toString(), e);
		}
	}

	public void stopServer() {
		if (serverRunning && jmsServerManager != null) {
			try {
				jmsServerManager.stop();
				server.stop();
				configuration.stop();
				serverRunning = false;
			} catch (Exception e) {
				log.error(e.toString(), e);
			}
		}
	}

	public JMSServerManager getJMSServerManager() {
		return jmsServerManager;
	}

	public boolean isServerRunning() {
		return serverRunning;
	}

	public void setDispatcherPropertiesBean(Properties propertiesBean) {
		this.dispatcherPropertiesBean = propertiesBean;
	}

	public Properties getDispatcherPropertiesBean() {
		return dispatcherPropertiesBean;
	}
}
