package cz.metacentrum.perun.rpc;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main implements ServletContextListener {

  private static final Logger LOG = LoggerFactory.getLogger(Main.class);

  @Override
  public void contextDestroyed(ServletContextEvent arg0) {

  }

  @Override
  public void contextInitialized(ServletContextEvent servletContextEvent) {
    LOG.info("Perun RPC web application is now initialized");
  }
}
