package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.FacilityManagerEvents.FacilityCreated;
import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.implApi.PubSubMechanism;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * This test uses only the 'publishAndWait' method so it can verify
 * that the listeners has been called. In real code, it is better to
 * use the 'publishAsync'.
 *
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class PubSubMechanismTest extends AbstractPerunIntegrationTest {

	@Autowired
	private PubSubMechanism pubSubMechanism;

	private static final String CLASS_NAME = PubSubMechanismTest.class.getSimpleName() + ".";

	private PubSubMechanismImpl.Listener mockedListener1 = mock(PubSubMechanismImpl.Listener.class);
	private PubSubMechanismImpl.Listener mockedListener2 = mock(PubSubMechanismImpl.Listener.class);

	@Test
	public void publish() {
		System.out.println(CLASS_NAME + "publishAndWait");

		Facility facility = new Facility(1, "facility");
		AuditEvent event = new FacilityCreated(facility);

		pubSubMechanism.addListener(mockedListener1, event.getClass());

		pubSubMechanism.publishAndWait(event);

		verify(mockedListener1, times(1)).onEventReceived(event);
	}

	@Test
	public void publishWithTwoListeners() {
		System.out.println(CLASS_NAME + "publishWithTwoListeners");

		Facility facility = new Facility(1, "facility");
		AuditEvent event = new FacilityCreated(facility);

		pubSubMechanism.addListener(mockedListener1, event.getClass());
		pubSubMechanism.addListener(mockedListener2, event.getClass());

		pubSubMechanism.publishAndWait(event);

		verify(mockedListener1, times(1)).onEventReceived(event);
		verify(mockedListener2, times(1)).onEventReceived(event);
	}

	@Test
	public void publishWithTwoListenersOnlyOneMatching() {
		System.out.println(CLASS_NAME + "publishWithTwoListenersOnlyOneMatching");

		Facility facility = new Facility(1, "facility");
		AuditEvent event = new FacilityCreated(facility);

		pubSubMechanism.addListener(mockedListener1, event.getClass(), Collections.singletonList("facility.id=1"));
		pubSubMechanism.addListener(mockedListener2, event.getClass(), Collections.singletonList("facility.id=2"));

		pubSubMechanism.publishAndWait(event);

		verify(mockedListener1, times(1)).onEventReceived(event);
		verify(mockedListener2, times(0)).onEventReceived(event);
	}

	@Test
	public void publishWithMatchingParams() {
		System.out.println(CLASS_NAME + "publishWithMatchingParams");

		Facility facility = new Facility(1, "facility");
		AuditEvent event = new FacilityCreated(facility);

		pubSubMechanism.addListener(mockedListener1, event.getClass(), Arrays.asList("facility.name=facility", "facility.id=1"));

		pubSubMechanism.publishAndWait(event);

		verify(mockedListener1, times(1)).onEventReceived(event);
	}

	@Test
	public void publishWithNotMatchingParams() {
		System.out.println(CLASS_NAME + "publishWithNotMatchingParams");

		Facility facility = new Facility(1, "facility");
		AuditEvent event = new FacilityCreated(facility);

		pubSubMechanism.addListener(mockedListener1, event.getClass(), Collections.singletonList("facility.id=2"));

		pubSubMechanism.publishAndWait(event);

		verify(mockedListener1, times(0)).onEventReceived(event);
	}

	@Test
	public void removeListener() {
		System.out.println(CLASS_NAME + "removeListener");

		Facility facility = new Facility(1, "facility");
		AuditEvent event = new FacilityCreated(facility);

		pubSubMechanism.addListener(mockedListener1, event.getClass());
		pubSubMechanism.removeListener(mockedListener1, event.getClass());

		pubSubMechanism.publishAndWait(event);

		verify(mockedListener1, times(0)).onEventReceived(event);
	}

	@Test
	public void removeListenerParams() {
		System.out.println(CLASS_NAME + "removeListenerParams");

		Facility facility = new Facility(1, "facility");
		AuditEvent event = new FacilityCreated(facility);

		List<String> params = Collections.singletonList("facility.name=notFacility");

		pubSubMechanism.addListener(mockedListener1, event.getClass(), params);
		pubSubMechanism.removeListenerParameters(mockedListener1, event.getClass(), params);

		pubSubMechanism.publishAndWait(event);

		verify(mockedListener1, times(1)).onEventReceived(event);
	}
}
