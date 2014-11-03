package cz.metacentrum.perun.perun_notification;

import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.notif.entities.*;
import cz.metacentrum.perun.notif.enums.PerunNotifNotifyTrigger;
import cz.metacentrum.perun.notif.enums.PerunNotifTypeOfReceiver;
import cz.metacentrum.perun.notif.exceptions.NotifReceiverAlreadyExistsException;
import cz.metacentrum.perun.notif.exceptions.NotifRegexAlreadyExistsException;
import cz.metacentrum.perun.notif.exceptions.NotifTemplateMessageAlreadyExistsException;
import cz.metacentrum.perun.notif.exceptions.PerunNotifRegexUsedException;
import cz.metacentrum.perun.notif.listener.NotificationListener;
import cz.metacentrum.perun.notif.managers.PerunNotifNotificationManager;
import cz.metacentrum.perun.notif.managers.PerunNotifNotificationManagerImpl;
import cz.metacentrum.perun.notif.managers.SchedulingManagerImpl;
import junit.framework.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.*;

/**
 * Unit test for simple App.
 */
public class AppTest extends AbstractTest {

	private static final Logger logger = LoggerFactory.getLogger(AppTest.class);

	@Test
	public void testNotificationListener() throws Exception {

		InputStream inputStream = AbstractTest.class.getClassLoader().getResourceAsStream("test_notification_listener.sql");

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

			throw new RuntimeException("Error during initialization of db.");
		}

		springCtx = new ClassPathXmlApplicationContext(
			"perun-beans.xml",
			"perun-datasources.xml",
			"perun-notification-applicationcontext-jdbc-test.xml",
			"perun-notification-applicationcontext-test.xml",
			"perun-notification-applicationcontext-scheduling-test.xml"
		);

		NotificationListener notificationListener = springCtx.getBean("notificationListener", NotificationListener.class);

		notificationListener.processOneAuditerMessage("Member:[id=<4054>, userId=<3354>, voId=<21>, status=<VALID>] created.");
		notificationListener.processOneAuditerMessage("Member:[id=<4054>, userId=<3354>, voId=<21>, status=<VALID>] validated.");

		SchedulingManagerImpl schedulingManager = springCtx.getBean("schedulingManager", SchedulingManagerImpl.class);

		schedulingManager.doNotification();

		int i = 0;
		boolean doWait = true;
		while (doWait) {
			if (smtpServer.getReceivedEmailSize() < 0) {
				if (i < 15) {
					try {
						Thread.sleep(1000);
					} catch (Exception ex) {
						logger.error("Error during sleep.", ex);
					}
				} else {
					doWait = false;
				}
			} else {
				doWait = false;
			}
			i++;
		}

		if (smtpServer.getReceivedEmailSize() > 0) {

		} else {
			fail("Email not received.");
		}
	}

	@Test
	public void testPerunNotifNotificationManager() throws InternalErrorException, PerunNotifRegexUsedException, NotifReceiverAlreadyExistsException, NotifRegexAlreadyExistsException, NotifTemplateMessageAlreadyExistsException {

		ApplicationContext springCtx = new ClassPathXmlApplicationContext("perun-beans.xml",
			"perun-datasources.xml",
			"perun-notification-applicationcontext-jdbc-test.xml",
			"perun-notification-applicationcontext-test.xml");

		PerunNotifNotificationManager manager = springCtx.getBean("perunNotifNotificationManager", PerunNotifNotificationManagerImpl.class);

		PerunNotifTemplate template = new PerunNotifTemplate();
		template.setNotifyTrigger(PerunNotifNotifyTrigger.STREAM);
		template.setOldestMessageTime(1L);
		Map<String, List<String>> primaryProperties = new HashMap<String, List<String>>();
		List<String> testProperty = new ArrayList<String>();
		testProperty.add("property1");
		primaryProperties.put("prop1", testProperty);
		template.setPrimaryProperties(primaryProperties);
		template.setYoungestMessageTime(0L);
		template.setSender("sender");

		manager.createPerunNotifTemplate(template);

		PerunNotifTemplate templateFromDb = manager.getPerunNotifTemplateById(template.getId());
		assertNotNull(templateFromDb);
		assertEquals(template, templateFromDb);
		assertEquals(template.getNotifyTrigger(), templateFromDb.getNotifyTrigger());
		assertEquals(template.getOldestMessageTime(), templateFromDb.getOldestMessageTime());
		assertEquals(template.getPrimaryProperties(), templateFromDb.getPrimaryProperties());
		assertEquals(template.getYoungestMessageTime(), templateFromDb.getYoungestMessageTime());
		assertEquals(template.getSender(), templateFromDb.getSender());

		PerunNotifObject object = new PerunNotifObject();
		object.setName("testName");
		Set<String> properties = new HashSet<String>();
		properties.add("testProperty");
		object.setProperties(properties);
		object.setObjectClass(Member.class);
		manager.createPerunNotifObject(object);

		PerunNotifObject objectFromDb = manager.getPerunNotifObjectById(object.getId());
		assertNotNull(objectFromDb);
		assertEquals(object, objectFromDb);
		assertEquals(object.getObjectClass(), objectFromDb.getObjectClass());
		assertEquals(object.getName(), objectFromDb.getName());
		assertEquals(object.getProperties(), objectFromDb.getProperties());

		PerunNotifRegex regex = new PerunNotifRegex();
		regex.setNote("note");
		regex.setRegex("regex");
		Set<PerunNotifObject> regexObjects = new HashSet<PerunNotifObject>();
		regexObjects.add(objectFromDb);
		regex.setObjects(regexObjects);
		manager.createPerunNotifRegex(regex);

		PerunNotifRegex regexFromDb = manager.getPerunNotifRegexById(regex.getId());
		assertNotNull(regexFromDb);
		assertEquals(regex, regexFromDb);
		assertEquals(regex.getNote(), regexFromDb.getNote());
		assertEquals(regex.getRegex(), regexFromDb.getRegex());
		assertEquals(regex.getObjects(), regexFromDb.getObjects());

		PerunNotifReceiver receiver = new PerunNotifReceiver();
		receiver.setTarget("target");
		receiver.setTemplateId(template.getId());
		receiver.setTypeOfReceiver(PerunNotifTypeOfReceiver.EMAIL_USER);

		manager.createPerunNotifReceiver(receiver);
		PerunNotifReceiver receiverFromDb = manager.getPerunNotifReceiverById(receiver.getId());
		assertNotNull(receiverFromDb);
		assertEquals(receiver, receiverFromDb);
		assertEquals(receiver.getTemplateId(), receiverFromDb.getTemplateId());
		assertEquals(receiver.getTarget(), receiverFromDb.getTarget());
		assertEquals(receiver.getTypeOfReceiver(), receiverFromDb.getTypeOfReceiver());

		PerunNotifTemplateMessage templateMessage = new PerunNotifTemplateMessage();
		templateMessage.setLocale(new Locale("cs"));
		templateMessage.setMessage("message");
		templateMessage.setTemplateId(template.getId());
		templateMessage.setSubject("cesky subject");

		manager.createPerunNotifTemplateMessage(templateMessage);
		PerunNotifTemplateMessage templateMessageFromDb = manager.getPerunNotifTemplateMessageById(templateMessage.getId());
		assertNotNull(templateMessageFromDb);
		assertEquals(templateMessage, templateMessageFromDb);
		assertEquals(templateMessage.getMessage(), templateMessageFromDb.getMessage());
		assertEquals(templateMessage.getTemplateId(), templateMessageFromDb.getTemplateId());
		assertEquals(templateMessage.getLocale(), templateMessageFromDb.getLocale());
		Assert.assertEquals(templateMessage.getSubject(), templateMessageFromDb.getSubject());

		templateFromDb = manager.getPerunNotifTemplateById(templateFromDb.getId());
		templateFromDb.addPerunNotifRegex(regex);
		templateFromDb = manager.updatePerunNotifTemplate(templateFromDb);

		//Test for complete load of template
		PerunNotifTemplate templateFromDbForTest = manager.getPerunNotifTemplateById(template.getId());

		assertNotNull(templateFromDbForTest.getPerunNotifTemplateMessages());
		assertNotNull(templateFromDbForTest.getMatchingRegexs());
		assertNotNull(templateFromDbForTest.getOldestMessageTime());
		assertNotNull(templateFromDbForTest.getYoungestMessageTime());
		assertNotNull(templateFromDbForTest.getNotifyTrigger());
		assertNotNull(templateFromDbForTest.getPrimaryProperties());
		assertNotNull(templateFromDbForTest.getReceivers());
		assertNotNull(templateFromDbForTest.getSerializedPrimaryProperties());

		assertTrue(templateFromDbForTest.getReceivers().contains(receiver));
		assertTrue(templateFromDbForTest.getMatchingRegexs().contains(regex));
		assertTrue(templateFromDbForTest.getPerunNotifTemplateMessages().contains(templateMessage));

		manager.removePerunNotifTemplateMessage(templateMessage.getId());
		assertNull(manager.getPerunNotifTemplateMessageById(templateMessage.getId()));

		manager.removePerunNotifReceiverById(receiver.getId());
		assertNull(manager.getPerunNotifReceiverById(receiver.getId()));

		manager.removePerunNotifTemplateRegexRelation(template.getId(), regex.getId());
		manager.removePerunNotifRegexById(regex.getId());
		assertNull(manager.getPerunNotifRegexById(regex.getId()));

		manager.removePerunNotifObjectById(object.getId());
		assertNull(manager.getPerunNotifObjectById(object.getId()));

		templateFromDb = manager.getPerunNotifTemplateById(template.getId());
		assertTrue(templateFromDb.getMatchingRegexs() == null || templateFromDb.getMatchingRegexs().isEmpty());
		assertTrue(templateFromDb.getPerunNotifTemplateMessages() == null || templateFromDb.getPerunNotifTemplateMessages().isEmpty());
		assertTrue(templateFromDb.getReceivers() == null || templateFromDb.getReceivers().isEmpty());

		manager.removePerunNotifTemplateById(template.getId());
		assertNull(manager.getPerunNotifTemplateById(template.getId()));
	}
}
