package cz.metacentrum.perun.core.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import cz.metacentrum.perun.audit.events.FacilityManagerEvents.FacilityCreated;
import cz.metacentrum.perun.audit.events.ServicesManagerEvents.DestinationsRemovedFromAllServices;
import cz.metacentrum.perun.core.api.*;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.implApi.AuditerListener;

@Ignore
public class AuditerImplIntegrationTest extends AbstractPerunIntegrationTest {

	private Facility facility;      // uses creation of facility to store same system message in Auditer
	private String consumerName = "testConsumer";

	@Autowired
	private AuditerConsumer auditerConsumer;
	@Autowired
	private AuditerPublisher auditerPublisher;

	@Before
	public void checkAuditerExists() throws Exception {

		assertNotNull("unable to get auditer",perun.getAuditer());

	}

	@Test
	public void logMessage() throws Exception {
		System.out.println("AuditerTest.logMessage");

		perun.getAuditer().log(sess, "test message");
		//
		boolean contains = false;
		for(AuditerMessage m : perun.getAuditer().getMessages()){
			if(m.getMessage().equals("test message")){
				contains = true;
			}
		}

		assertTrue("shoud contain logged message",contains);
		//assertTrue("shoud contain logged message",perun.getAuditer().getMessages().contains("test message"));

	}

	/**
	 *
	 * @author Richard Husar 445238@mail.muni.cz
	 * @throws Exception
	 */
	@Test
	public void logMessageToJson() throws Exception{
		System.out.println("AuditerTest.logMessageToJson");
		setUpFacility();
		perun.getAuditer().log(sess, new FacilityCreated(facility));
		boolean contains = false;
		for(AuditerMessage m : perun.getAuditer().getMessages()){
			System.out.println(m);
			if(m.getMessage().contains("{\"facility\":{\"id\":3325,")){
				contains = true;
			}
		}
		assertTrue("shoud contain logged message",contains);

	}

	@Test
	public void logMessagesWithObject() throws Exception {
		System.out.println("AuditerTest.logMessagesWithObject");

		setUpFacility();

		perun.getAuditer().log(sess, "test message with {}", facility);
		//
		boolean contains = false;
		for(AuditerMessage m : perun.getAuditer().getMessages()){
			System.out.println(m);
			if(m.getMessage().contains("test message with Facility:[id=<3323>, name=<AuditorTestFacility>, description=<test description>]")){
				contains = true;
			}
		}

		assertTrue("shoud contain logged message",contains);
		//assertTrue("shoud contain logged message",perun.getAuditer().getMessages().contains("test message with "+facility));

	}

	@Test
	@Ignore //deprecated
	public void logMessagesWithTwoObjects() throws Exception {
		System.out.println("AuditerTest.logMessagesWithTwoObjects");

		setUpFacility();

		perun.getAuditer().log(sess, "test message with {} {}", facility, facility);
		assertTrue("shoud contain logged message",perun.getAuditer().getMessages().contains("test message with "+facility+" "+facility));

	}

	@Test
	@Ignore //deprecated
	public void logMessagesWithThreeObjects() throws Exception {
		System.out.println("AuditerTest.logMessagesWithThreeObjects");

		setUpFacility();

		perun.getAuditer().log(sess, "test message with {} {} {}", facility, facility, facility);
		assertTrue("shoud contain logged message",perun.getAuditer().getMessages().contains("test message with "+facility+" "+facility+" "+facility));

	}

	@Test
	public void flushAllMessages() throws Exception {
		System.out.println("AuditerTest.flushAllMessages");

		setUpFacility();
		assertTrue("auditer should contain at least one message",perun.getAuditer().getMessages().size()>=1);
		perun.getAuditer().flush();
		assertTrue("auditer should be empty after flush", perun.getAuditer().getMessages().isEmpty());

	}

	@Test
	public void cleanMessages() throws Exception {
		System.out.println("AuditerTest.cleanMessages");

		setUpFacility();
		assertTrue("auditer should contain at least one message",perun.getAuditer().getMessages().size()>=1);
		perun.getAuditer().clean();
		assertTrue("auditer should be empty after flush", perun.getAuditer().getMessages().isEmpty());
	}

	@Test
	public void getLastMessages() throws Exception {
		System.out.println("AuditeTest.getLastMessages");
		for (int i = 0; i < 20; i++) {
			perun.getAuditer().log(sess, "Testovaci text c."+ i +".");
		}
		List<AuditMessage> messages = perun.getAuditer().getMessages(20);
		assertEquals("getMessage(count) returns wrong count of messages", 20, messages.size());
	}

	@Test
	@Ignore //deprecated
	public void getCorrectMessageFromLastMessages() throws Exception {
		System.out.println("AuditeTest.getCorrectMessageFromLastMessages");
		perun.getAuditer().clean();
		for (int i = 0; i < 20; i++) {
			if(i==5) perun.getAuditer().log(sess, "Abdjsj&#(234JSK");
			else perun.getAuditer().log(sess, "Testovaci text c."+ i +".");

		}
		perun.getAuditer().flush();
		List<AuditMessage> messages = perun.getAuditer().getMessagesByCount(20);

		boolean contain=false;
		for (AuditMessage m:messages){
			if(m.getMsg().contains("Abdjsj&#(234JSK")) contain=true;
		}
		assertTrue("One of messages need to contain specific message.", contain);
	}

	/**
	 *
	 * @author Richard Husar 445238@mail.muni.cz
	 * @throws Exception
	 */
	@Test
	public void getCorrectJsonMessages() throws Exception{
		System.out.println("AuditerTest.getCorrectJsonMessages");
		perun.getAuditer().clean();

		for (int i = 1; i < 20; i = i++) {
			Facility testFacility = new Facility(++i,"AuditorTestFacility number "+ String.valueOf(i));
			testFacility = perun.getFacilitiesManager().createFacility(sess, testFacility);
			perun.getAuditer().log(sess, testFacility);
		}
		perun.getAuditer().flush();
		List<AuditMessage> messages = perun.getAuditer().getJSONMessages(20);

		boolean same=true;
		if(!messages.get(0).getMsg().contains("\"name\":\"AuditorTestFacility number "+ 20))
			same=false;
		assertTrue("Messages do not correspond", same);
	}




	@Test
	public void getSubscriberMessagesFacilityCreated() throws Exception{
		System.out.println("AuditerTest.getSubscriberMessagesFileredByFacilityCreated");
		perun.getAuditer().clean();
		//set up facility
		setUpFacility();

		perun.getAuditer().flush();

		List<String> shouldContain = new ArrayList<>();
		shouldContain.add("Test subscriber gets filtered message : 3326");

		//set up subscriber
		FacilityCreatedSubscriber testSubscriber = new FacilityCreatedSubscriber();
		testSubscriber.subscribe();
		auditerPublisher.publishMessages(auditerPublisher.getMessages());

		//wait for pubsubmechanizm to distribute message
		int counter = 100;
		boolean recieved = false;
		while(counter > 0 && !recieved){
			Thread.sleep(200);
			if(!testSubscriber.recievedMessages.isEmpty()){
				recieved = true;
			}
			counter--;
		}
		assertTrue("Subscriber did not recieve messages.",recieved);
		assertTrue("Should contain all filtered messages.",testSubscriber.recievedMessages.containsAll(shouldContain) && testSubscriber.recievedMessages.size() == 1);
	}


	private static PubsubMechanizm pubsubMechanizm = PubsubMechanizm.getInstance();

	/**
	 * Subscriber which is registered for listening to audit messages of FacilityCreated type with id of 3328
	 */
	private class FacilityCreatedSubscriber implements PubsubMechanizm.Listener
	{

		List<String> recievedMessages = new ArrayList<>();

		public void subscribe()
		{
			List<String> params = new ArrayList<String>();
			params.add("facility.id=3326");
			pubsubMechanizm.addListener(FacilityCreated.class, this, params);

		}
		@Override
		public void onEventReceived(Object event, Object object) {
			if(object instanceof FacilityCreated){
				FacilityCreated f = (FacilityCreated) object;
				recievedMessages.add("Test subscriber gets filtered message : " + f.getFacility().getId());
			}else if (object instanceof DestinationsRemovedFromAllServices) {
				DestinationsRemovedFromAllServices d = (DestinationsRemovedFromAllServices) object;
				recievedMessages.add("Test subscriber gets message: " + d.toString());
			}else{
				recievedMessages.add("Test message with unknown event type.");
			}

		}
	}
	
	



	/*
	 * XXX deprecated
	 @Test
	 public void auditerConsumerTest() throws Exception {
	 System.out.println("AuditerTest.auditerConsumerTest");

//get all odl mesages and throw them away
auditerConsumer.getMessages();

// system event creates message in auditer (check it)
setUpFacility();
assertTrue("auditer should contain four messages, currently contains " + perun.getAuditer().getMessages().size(),perun.getAuditer().getMessages().size()==4);

// save auditer messages and flush => process them with listener
List<String> messagesFromAuditer = perun.getAuditer().getMessages();
perun.getAuditer().flush();

// get messages from consumer
List<String> messagesFromConsumer = auditerConsumer.getMessages();

assertTrue("Auditer and Consumer should contain same messages!",messagesFromConsumer.containsAll(messagesFromAuditer));

	 }
	 */

	@Ignore //Deprecated
		@Test
		public void registerListener() throws Exception {
			System.out.println("AuditerTest.registerListener");

			final AuditerListenerDummy listener = new AuditerListenerDummy();
			assertTrue("unable to register Listener",perun.getAuditer().registerListener(listener, consumerName));
			assertFalse("shouldn't register Listener twice",perun.getAuditer().registerListener(listener, consumerName));

		}

	@Test
	@Ignore //Deprecated
	public void unregisterListener() throws Exception {
		System.out.println("AuditerTest.unregisterListener");

		final AuditerListenerDummy listener = new AuditerListenerDummy();
		assertFalse("shouldn't unregister listener when it's not registred",perun.getAuditer().unregisterListener(listener));
		assertTrue("unable to register listener",perun.getAuditer().registerListener(listener, consumerName));
		assertTrue("unable to unregister registered listener",perun.getAuditer().unregisterListener(listener));

	}


	@Test
	@Ignore //Deprecated
	public void checkListener() throws Exception {
		/* XXX deprecated
			 System.out.println("AuditerTest.checkListener");

			 AuditerListenerDummy listener = new AuditerListenerDummy();
			 assertTrue("unable to register Listener",perun.getAuditer().registerListener(listener, consumerName));
		// register dummy listener

		setUpFacility();
		assertTrue("auditer should contain one message",perun.getAuditer().getMessages().size()==1);
		// system event creates message in auditer (check it)

		List<String> messagesFromAuditer = perun.getAuditer().getMessages();
		perun.getAuditer().flush();
		// save auditer messages and flush => process them with listener

		List<String> messagesFromListener = listener.getMessages();
		// get messages from listener

		int counter = 0;
		while (messagesFromListener.isEmpty() && counter < 25) {
		counter++;
		System.out.println("Waiting for Listeners thread to finis "+counter);
		Thread.sleep(200);
		messagesFromListener = listener.getMessages();
		}
		// waiting for listener to process messages before further testing and get them again

		for (int i=0; i<messagesFromAuditer.size(); i++) {
		assertTrue("Auditer and Listener should contain same messages!",messagesFromListener.contains(messagesFromAuditer.get(i)));
		}
		// check listener's content against original auditer's content

*/

	}


	public void setAuditerConsumer(AuditerConsumer auditerConsumer) {
		this.auditerConsumer = auditerConsumer;
	}

	public void setAuditerPublisher(AuditerPublisher auditerPublisher) {
		this.auditerPublisher = auditerPublisher;
	}

	private class AuditerListenerDummy implements AuditerListener {

		private List<String> messages = new ArrayList<String>();

		@Override
		public void notifyWith(String message) {

			messages.add(message);

		}

		public List<String> getMessages() {

			return messages;

		}

	}

	// ------------- private methods ----------------------------------


	private void setUpFacility() throws Exception {

		facility = new Facility(0,"AuditorTestFacility", "test description", "testCreatedAt","testCreatedBy", "testModifiedAt", "testModifiedBy", 0, 0 );
		facility = perun.getFacilitiesManager().createFacility(sess, facility);
	}






}
