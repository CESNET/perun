package cz.metacentrum.perun.perun_notification;

import com.dumbster.smtp.SimpleSmtpServer;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Setup local DB and smtp server to send test mail notifications
 *
 * @author Tomáš Tunkl
 * @author Pavel Zlámal <256627@mail.muni.cz>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:perun-notification-applicationcontext-test.xml", "classpath:perun-notification-applicationcontext-jdbc-test.xml", "classpath:perun-notification-applicationcontext-scheduling-test.xml" })
public class AbstractTest {

    protected static ApplicationContext springCtx;
    protected static SimpleSmtpServer smtpServer;

    @Autowired
    public static BasicDataSource dataSource2;

    public void setDataSource2(BasicDataSource dataSource2) {
        this.dataSource2 = dataSource2;
    }

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

    @BeforeClass
    public static void beforeTest() {

        try {

            // FIXME - we must initialize manually without other notification beans, since they can't be
            // FIXME - instantiated in @BeforeClass (they select from DB which is empty)
            springCtx = new ClassPathXmlApplicationContext("classpath:perun-notification-applicationcontext-jdbc-test.xml");
            dataSource2 = ((BasicDataSource)springCtx.getBean("dataSource2"));

            Connection conn = dataSource2.getConnection();
            Statement st = conn.createStatement();

            String theString = "drop all objects;";
            st.execute(theString);

            conn.commit();
            conn.close();
        } catch (SQLException ex) {
            System.err.println("Error during clear of db: " + ex.getMessage());
            ex.printStackTrace();
            throw new RuntimeException("Error in clearing of db.");
        }

        InputStream inputStream = AbstractTest.class.getClassLoader().getResourceAsStream("pn_data.sql");

        try {

            Connection conn = dataSource2.getConnection();
            Statement st = conn.createStatement();

            String theString = convertStreamToString(inputStream);
            st.execute(theString);

            conn.commit();
            conn.close();
        } catch (SQLException ex) {
            System.err.println("Error during db setting: " + ex.getMessage());
            ex.printStackTrace();
            throw new RuntimeException("Error in prepare of db.");
        }
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

    public static ApplicationContext getSpringCtx() {
        return springCtx;
    }

    public Connection getConnection() throws Exception {
        return dataSource2.getConnection();
    }
}
