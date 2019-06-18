package cz.metacentrum.perun.notif;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.notif.entities.*;
import cz.metacentrum.perun.notif.enums.PerunNotifNotifyTrigger;
import cz.metacentrum.perun.notif.enums.PerunNotifTypeOfReceiver;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.*;

/**
 * Unit test for simple App.
 */
public class AppTest extends AbstractTest {

	private static final Logger logger = LoggerFactory.getLogger(AppTest.class);

	private int userId;
	private int memberId;
	private int voId;

	@Test
	public void testNotificationListener() throws Exception {
		prepareData();

		schedulingManager.processOneAuditerMessage("Member:[id=<" + memberId + ">, userId=<" + userId + ">, voId=<" + voId + ">, status=<VALID>, sourceGroupId=<\\0>, suspendedTo=<\\0>] created.");
		schedulingManager.processOneAuditerMessage("Member:[id=<" + memberId + ">, userId=<" + userId + ">, voId=<" + voId + ">, status=<VALID>, sourceGroupId=<\\0>, suspendedTo=<\\0>] validated.");

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
	public void testPerunNotifNotificationManager() throws PerunException {

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

		manager.createPerunNotifTemplate(sess, template);

		PerunNotifTemplate templateFromDb = manager.getPerunNotifTemplateById(sess, template.getId());
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
		manager.createPerunNotifRegex(sess, regex);

		PerunNotifRegex regexFromDb = manager.getPerunNotifRegexById(sess, regex.getId());
		assertNotNull(regexFromDb);
		assertEquals(regex, regexFromDb);
		assertEquals(regex.getNote(), regexFromDb.getNote());
		assertEquals(regex.getRegex(), regexFromDb.getRegex());
		assertEquals(regex.getObjects(), regexFromDb.getObjects());

		PerunNotifReceiver receiver = new PerunNotifReceiver();
		receiver.setTarget("target");
		receiver.setTemplateId(template.getId());
		receiver.setTypeOfReceiver(PerunNotifTypeOfReceiver.EMAIL_USER);
		receiver.setLocale("cs");

		manager.createPerunNotifReceiver(sess, receiver);
		PerunNotifReceiver receiverFromDb = manager.getPerunNotifReceiverById(sess, receiver.getId());
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

		manager.createPerunNotifTemplateMessage(sess, templateMessage);
		PerunNotifTemplateMessage templateMessageFromDb = manager.getPerunNotifTemplateMessageById(sess, templateMessage.getId());
		assertNotNull(templateMessageFromDb);
		assertEquals(templateMessage, templateMessageFromDb);
		assertEquals(templateMessage.getMessage(), templateMessageFromDb.getMessage());
		assertEquals(templateMessage.getTemplateId(), templateMessageFromDb.getTemplateId());
		assertEquals(templateMessage.getLocale(), templateMessageFromDb.getLocale());
		assertEquals(templateMessage.getSubject(), templateMessageFromDb.getSubject());

		templateFromDb = manager.getPerunNotifTemplateById(sess, templateFromDb.getId());
		templateFromDb.addPerunNotifRegex(regex);
		templateFromDb = manager.updatePerunNotifTemplate(sess, templateFromDb);

		//Test for complete load of template
		PerunNotifTemplate templateFromDbForTest = manager.getPerunNotifTemplateById(sess, template.getId());

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

		manager.removePerunNotifTemplateMessage(sess, templateMessage.getId());
		assertNull(manager.getPerunNotifTemplateMessageById(sess, templateMessage.getId()));

		manager.removePerunNotifReceiverById(sess, receiver.getId());
		assertNull(manager.getPerunNotifReceiverById(sess, receiver.getId()));

		manager.removePerunNotifTemplateRegexRelation(sess, template.getId(), regex.getId());
		manager.removePerunNotifRegexById(sess, regex.getId());
		assertNull(manager.getPerunNotifRegexById(sess, regex.getId()));

		manager.removePerunNotifObjectById(object.getId());
		assertNull(manager.getPerunNotifObjectById(object.getId()));

		templateFromDb = manager.getPerunNotifTemplateById(sess, template.getId());
		assertTrue(templateFromDb.getMatchingRegexs() == null || templateFromDb.getMatchingRegexs().isEmpty());
		assertTrue(templateFromDb.getPerunNotifTemplateMessages() == null || templateFromDb.getPerunNotifTemplateMessages().isEmpty());
		assertTrue(templateFromDb.getReceivers() == null || templateFromDb.getReceivers().isEmpty());

		manager.removePerunNotifTemplateById(sess, template.getId());
		assertNull(manager.getPerunNotifTemplateById(sess, template.getId()));
	}

	private void prepareData() throws PerunException {
		// user
		User user = new User();
		user.setFirstName("John");
		user.setMiddleName("");
		user.setLastName("Smith");
		user.setTitleBefore("");
		user.setTitleAfter("");
		User newUser = perun.getUsersManagerBl().createUser(sess, user);
		userId = newUser.getId();

		// vo
		Vo vo = new Vo(0, "NotifTestVo", "NTestVo");
		Vo newVo = perun.getVosManager().createVo(sess, vo);
		voId = newVo.getId();

		// member
		Member member = perun.getMembersManagerBl().createMember(sess, newVo, newUser);
		memberId = member.getId();

		// attribute preferred laguage
		AttributeDefinition attrDef = perun.getAttributesManagerBl().getAttributeDefinition(sess, "urn:perun:user:attribute-def:def:preferredLanguage");
		Attribute attr = new Attribute(attrDef);
		attr.setValue("cs");
		System.out.println("attribute: " + perun.getAttributesManagerBl().getAttribute(sess, user, "urn:perun:user:attribute-def:def:preferredLanguage"));
		perun.getAttributesManagerBl().setAttribute(sess, user, attr);

		// template
		PerunNotifTemplate template = new PerunNotifTemplate();
		Map<String, List<String>> properties = new HashMap<>();
		properties.put("cz.metacentrum.perun.core.api.Member", new ArrayList<>(Arrays.asList("getId()", "getUserId()")));
		properties.put("METHOD", new ArrayList<>(Arrays.asList("getAttributesManagerBl().getAttribute(cz.metacentrum.perun.core.api.PerunSession, getUsersManagerBl().getUserByMember(cz.metacentrum.perun.core.api.PerunSession, cz.metacentrum.perun.core.api.Member), \"urn:perun:user:attribute-def:def:preferredLanguage\").getValue().equals(\"en\")",
			"getUsersManagerBl().getUserByMember(cz.metacentrum.perun.core.api.PerunSession, cz.metacentrum.perun.core.api.Member).getId()",
			"getAttributesManagerBl().getAttribute(cz.metacentrum.perun.core.api.PerunSession, getUsersManagerBl().getUserByMember(cz.metacentrum.perun.core.api.PerunSession, cz.metacentrum.perun.core.api.Member), \"urn:perun:user:attribute-def:def:preferredLanguage\").getValue().equals(\"cs\")")));
		template.setPrimaryProperties(properties);
		template.setNotifyTrigger(PerunNotifNotifyTrigger.ALL_REGEX_IDS);
		template.setYoungestMessageTime(10L);
		template.setOldestMessageTime(20L);
		template.setSender("noreply@meta.cz");

		// regex for creation
		PerunNotifRegex regexC = new PerunNotifRegex();
		regexC.setNote("Member created");
		regexC.setRegex("Member:.* created\\.");
		PerunNotifRegex newRegexC = manager.createPerunNotifRegex(sess, regexC);
		template.addPerunNotifRegex(newRegexC);

		// regex for validation
		PerunNotifRegex regexV = new PerunNotifRegex();
		regexV.setNote("Member validated");
		regexV.setRegex("Member:.* validated\\.");
		PerunNotifRegex newRegexV = manager.createPerunNotifRegex(sess, regexV);
		template.addPerunNotifRegex(newRegexV);

		// template message english
		PerunNotifTemplateMessage messageEn = new PerunNotifTemplateMessage();
		messageEn.setMessage("Good day,"
			+ "thank you for Your registration to virtual organization MetaCentrum VO,"
			+ "activity MetaCentrum association CESNET, which focuses on sophisticated computation."
			+ "Name: ${perun.getUsersManagerBl().getUserByMember(perunSession, retrievedObjects[\"" + newRegexC.getId() + "\"][\"Member\"]).getFirstName()}<br/>"
			+ "Surname: ${perun.getUsersManagerBl().getUserByMember(perunSession, retrievedObjects[\"" + newRegexC.getId() + "\"][\"Member\"]).getLastName()}<br/>"
			+ "Accounts are valid on machines till $membershipExpiration");
		messageEn.setLocale(Locale.forLanguageTag("en"));
		messageEn.setSubject("Subject");
		template.addPerunNotifTemplateMessage(messageEn);

		// template message czech
		PerunNotifTemplateMessage messageCs = new PerunNotifTemplateMessage();
		messageCs.setMessage("Dobrý den,\n" +
			"  děkujeme za Vaši registraci do virtualni organizace MetaCentrum VO,\n" +
			"aktivity MetaCentrum sdružení CESNET, zaměřené na náročné výpočty.\n" +
			"  Váš účet je nyní propagován na všechny servery, plně funkční bude\n" +
			"během hodiny.\n" +
			"  Jméno: ${perun.getUsersManagerBl().getUserByMember(perunSession, retrievedObjects[\"" + newRegexC.getId() + "\"][\"Member\"]).getFirstName()}<br/>\n" +
			"  Přijmení: ${perun.getUsersManagerBl().getUserByMember(perunSession, retrievedObjects[\"" + newRegexC.getId() + "\"][\"Member\"]).getLastName()}<br/>\n" +
			"Jazyk: ${perun.getAttributesManagerBl().getAttribute(perunSession, perun.getUsersManagerBl().getUserByMember(perunSession, retrievedObjects[\"" + newRegexC.getId() + "\"][\"Member\"]), \"urn:perun:user:attribute-def:def:preferredLanguage\").getValue()}");
		messageCs.setLocale(Locale.forLanguageTag("cs"));
		messageCs.setSubject("Subject");
		template.addPerunNotifTemplateMessage(messageCs);

		// receiver
		PerunNotifReceiver receiver = new PerunNotifReceiver();
		receiver.setLocale("cs");
		receiver.setTypeOfReceiver(PerunNotifTypeOfReceiver.EMAIL_USER);
		receiver.setTarget("cz.metacentrum.perun.core.api.Member.getUserId");
		template.setReceivers(new ArrayList<>(Arrays.asList(receiver)));

		manager.createPerunNotifTemplate(sess, template);

	}
}
