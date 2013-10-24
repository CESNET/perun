package cz.metacentrum.perun.perun_notification;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Properties;

/**
 * DB independant jabber test
 *
 * @author Tomáš Tunkl
 * @author Pavel Zlámal <256627@mail.muni.cz>
 * @version $Id$
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:perun-notification-applicationcontext-jabber-test.xml"})
public class JabberTest {

    private static final Logger logger = LoggerFactory.getLogger(JabberTest.class);

    @Autowired
    public Properties propertiesBean;

    public void setPropertiesBean(Properties propertiesBean) {
        this.propertiesBean = propertiesBean;
    }

    public JabberTest() {}

    @Test
    public void testJabberTest() throws Exception {

        try {

            String serverName = propertiesBean.getProperty("notif.jabber.jabberServer");
            String serviceName = propertiesBean.getProperty("notif.jabber.serviceName");
            String port = propertiesBean.getProperty("notif.jabber.port");

            String login = propertiesBean.getProperty("notif.jabber.username");
            String pass = propertiesBean.getProperty("notif.jabber.password");

            String sendTo = propertiesBean.getProperty("notif.jabber.test.sendTo");


            ConnectionConfiguration config = new ConnectionConfiguration(serverName, Integer.parseInt(port), serviceName);
            XMPPConnection connection = new XMPPConnection(config);

            connection.connect();
            SASLAuthentication.supportSASLMechanism("PLAIN", 0);

            connection.login(login, pass);

            Message message = new Message();
            message.setTo(sendTo);
            message.setSubject("Subject");
            message.setBody("Body");
            message.setType(Message.Type.headline);
            connection.sendPacket(message);

            connection.disconnect();

        } catch (XMPPException ex) {
            logger.error("Error during jabber establish connection.", ex);
        }
    }
}