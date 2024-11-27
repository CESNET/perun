package cz.metacentrum.perun.dispatcher.activemq;

import jakarta.annotation.Resource;
import java.net.URI;
import java.util.Properties;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Start / stop and configure ActiveMQ server for Perun.
 *
 * @author Michal Karm Babacek
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
@Service(value = "perunActiveMQServer")
public class PerunActiveMQServer {

  private static final Logger LOG = LoggerFactory.getLogger(PerunActiveMQServer.class);

  private Properties dispatcherProperties;
  private BrokerService server = null;
  private boolean serverRunning = false;


  // ----- setters -------------------------------------


  public Properties getDispatcherProperties() {
    return dispatcherProperties;
  }


  // ----- methods -------------------------------------

  /**
   * TRUE if ActiveMQ server was correctly started.
   *
   * @return TRUE ActiveMQ is running / FALSE otherwise
   */
  public boolean isServerRunning() {
    return serverRunning;
  }

  @Resource(name = "dispatcherPropertiesBean")
  public void setDispatcherProperties(Properties dispatcherProperties) {
    this.dispatcherProperties = dispatcherProperties;
  }

  /**
   * Start and configure ActiveMQ server with default JMS queue (systemQueue).
   */
  public void startServer() {
    try {

      LOG.info("Starting PerunActiveMQServer.");

      // load dispatcher config, fallback to fixed values
      String host = dispatcherProperties.getProperty("dispatcher.ip.address", "127.0.0.1");
      String port = dispatcherProperties.getProperty("dispatcher.port", "6071");

      TransportConnector transportConnector = new TransportConnector();
      transportConnector.setUri(new URI("tcp://" + host + ":" + port));

      server = new BrokerService();
      server.setPersistent(true);
      server.setDataDirectory(dispatcherProperties.getProperty("dispatcher.datadir",
              "/tmp/perun-dispatcher-data"));
      server.addConnector(transportConnector);

      server.start();
      serverRunning = true;
      LOG.info("PerunActiveMQServer started.");

    } catch (Exception e) {
      LOG.error("Can't start PerunActiveMQServer server.", e);
    }
  }

  /**
   * Stop ActiveMQ server.
   */
  public void stopServer() {
    if (serverRunning) {
      try {
        server.stop();
        serverRunning = false;
        LOG.debug("PerunActiveMQServer has stopped.");
      } catch (Exception e) {
        LOG.error("Can't stop PerunActiveMQServer.", e);
      }
    }
  }

}
