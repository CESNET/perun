package cz.metacentrum.perun.core.impl.modules.attributes;


import cz.metacentrum.perun.audit.events.AttributesManagerEvents.AllAttributesRemovedForUser;
import cz.metacentrum.perun.audit.events.AttributesManagerEvents.AllAttributesRemovedForUserExtSource;
import cz.metacentrum.perun.audit.events.AttributesManagerEvents.AttributeChangedForUser;
import cz.metacentrum.perun.audit.events.AttributesManagerEvents.AttributeSetForUser;
import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.impl.Utils;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Test module for urn:perun:user:attribute-def:virt:forwardedVoPersonExternalAffiliation
 *
 * @author Michal Berky
 */
public class urn_perun_user_attribute_def_virt_forwardedVoPersonExternalAffiliationTest {

	private static urn_perun_user_attribute_def_virt_forwardedVoPersonExternalAffiliation classInstance;
	private PerunSessionImpl session;
	private User user;
	private UserExtSource ues1;
	private UserExtSource ues2;
	private Attribute uesAtt1;
	private Attribute uesAtt2;
	private final String VALUE1 = "11aff11@somewhere.edu";
	private final String VALUE2 = "22aff22@somewhere.edu";
	private final String VALUE3 = "33aff33@somewhere.edu";

	@Before
	public void setVariables() {
		classInstance = new urn_perun_user_attribute_def_virt_forwardedVoPersonExternalAffiliation();
		session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);

		prepareCoreConfig();

		user = new User();
		user.setId(1);

		ues1 = new UserExtSource(10, new ExtSource(100, "name1", "type1"), "login1");
		ues2 = new UserExtSource(20, new ExtSource(200, "name2", "type2"), "login2");

		uesAtt1 = new Attribute();
		uesAtt2 = new Attribute();
		uesAtt1.setValue(VALUE1);
		uesAtt2.setValue(VALUE2+";"+VALUE3);
	}

	@Test
	public void getAttributeValueOnlyFromUserExtSources() throws Exception {
		when(session.getPerunBl().getUsersManagerBl().getUserExtSources(session, user)).thenReturn(
			Arrays.asList(ues1, ues2)
		);

		String attributeName = classInstance.getSourceAttributeName();
		when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, ues1, attributeName)).thenReturn(
			uesAtt1
		);
		when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, ues2, attributeName)).thenReturn(
			uesAtt2
		);

		Attribute receivedAttr = classInstance.getAttributeValue(session, user, classInstance.getAttributeDefinition());
		assertTrue(receivedAttr.getValue() instanceof List);
		assertEquals("destination attribute name wrong",classInstance.getDestinationAttributeFriendlyName(),receivedAttr.getFriendlyName());

		@SuppressWarnings("unchecked")
		List<String> actual = (List<String>) receivedAttr.getValue();
		Collections.sort(actual);
		List<String> expected = Arrays.asList(VALUE1, VALUE2, VALUE3);
		Collections.sort(expected);
		assertEquals("collected values are incorrect", expected, actual);
	}

	private static void prepareCoreConfig() {
		if (BeansUtils.getCoreConfig() == null) {
			CoreConfig testConfig = new CoreConfig();
			BeansUtils.setConfig(testConfig);
		}
	}
}
