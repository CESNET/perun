package cz.metacentrum.perun.notif;

import com.dumbster.smtp.SimpleSmtpServer;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.PerunClient;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.notif.managers.PerunNotifNotificationManager;
import cz.metacentrum.perun.notif.managers.SchedulingManagerImpl;
import java.sql.Connection;
import java.sql.SQLException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Setup local DB and smtp server to send test mail notifications
 *
 * @author Tomáš Tunkl
 * @author Pavel Zlámal <256627@mail.muni.cz>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional(transactionManager = "springTransactionManager")
@ContextConfiguration(locations = {"classpath:perun-core.xml", "classpath:perun-notification.xml",
    "classpath:perun-notification-scheduler.xml"})
public class AbstractTest {

  protected static SimpleSmtpServer smtpServer;
  @Autowired
  protected PerunBl perun;
  protected PerunSession sess;
  @Autowired
  protected PerunNotifNotificationManager manager;

  @Autowired
  protected SchedulingManagerImpl schedulingManager;

  @Autowired
  private ApplicationContext appContext;

  public static String convertStreamToString(java.io.InputStream is) {
    java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
    return s.hasNext() ? s.next() : "";
  }

  @AfterClass
  public static void shutdown() {
    if (smtpServer != null) {
      smtpServer.stop();
    }
  }

  @Test
  public void dummyTest() {
    System.out.println("Dummy test to prevent: NoRunnableMethodsException");
  }

  public Connection getConnection() throws SQLException {
    // classic Autowire dataSource does not work
    return ((SimpleDriverDataSource) appContext.getBean("dataSource")).getConnection();
  }

  @Before
  public void setUpSess() throws Exception {
    final PerunPrincipal pp = new PerunPrincipal("perunTests", ExtSourcesManager.EXTSOURCE_NAME_INTERNAL,
        ExtSourcesManager.EXTSOURCE_INTERNAL);
    sess = perun.getPerunSession(pp, new PerunClient());
  }

  @Before
  public void startSmtpServer() {
    if (smtpServer == null) {
      smtpServer = SimpleSmtpServer.start(8086);
    }
  }

  @After
  public void stopSmtpServer() {
    if (smtpServer != null) {
      smtpServer.stop();
      smtpServer = null;
    }
  }
}
