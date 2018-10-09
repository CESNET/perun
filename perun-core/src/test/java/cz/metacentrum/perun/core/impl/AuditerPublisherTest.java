package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.audit.events.VoManagerEvents.VoCreated;
import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.implApi.AuditerPublisher;
import cz.metacentrum.perun.core.implApi.PubSubMechanism;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class AuditerPublisherTest extends AbstractPerunIntegrationTest {

	public static final String CLASS_NAME = AuditerPublisherTest.class.getSimpleName() + ".";

	@Autowired
	private AuditerPublisher auditerPublisher;

	@Autowired
	private PubSubMechanism pubSubMechanism;

	private PubSubMechanismImpl.Listener mockedListener = mock(PubSubMechanismImpl.Listener.class);


	@Test
	public void receiveMessage() throws Exception {
		System.out.println(CLASS_NAME + "receiveMessage");

		pubSubMechanism.addListener(mockedListener, VoCreated.class);

		Vo vo = new Vo(0, "AuditerPublisher testVo", "AP TestVo");
		VoCreated voCreatedEvent = new VoCreated(vo);
		perun.getAuditer().log(sess, voCreatedEvent);
		perun.getAuditer().flush();

		// wait for the AuditerPublisher
		Thread.sleep(10000);
		verify(mockedListener, times(1)).onEventReceived(any(VoCreated.class));
	}
}
