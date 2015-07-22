package cz.metacentrum.perun.perun_notification;

import com.dumbster.smtp.SimpleSmtpServer;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.notif.managers.PerunNotifNotificationManager;
import cz.metacentrum.perun.notif.managers.SchedulingManagerImpl;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.sql.Connection;
import java.sql.SQLException;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

/**
 * Setup local DB and smtp server to send test mail notifications
 *
 * @author Tomáš Tunkl
 * @author Pavel Zlámal <256627@mail.muni.cz>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:perun-notification-applicationcontext-test.xml", "classpath:perun-notification-applicationcontext-scheduling-test.xml"})
public class AbstractTest {

	@Autowired
	protected PerunBl perun;

	protected PerunSession sess;
	
	protected static SimpleSmtpServer smtpServer;
	
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

	@Before
	public void startSmtpServer() {
		if (smtpServer == null) {
			smtpServer = SimpleSmtpServer.start(8086);
		}
	}

	@Before
	public void setUpSess() throws Exception {
		final PerunPrincipal pp = new PerunPrincipal("perunTests", ExtSourcesManager.EXTSOURCE_NAME_INTERNAL, ExtSourcesManager.EXTSOURCE_INTERNAL);
		sess = perun.getPerunSession(pp);
	}
	
	@After
	public void stopSmtpServer() {
		if (smtpServer != null) {
			smtpServer.stop();
			smtpServer = null;
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
}
