package cz.metacentrum.perun.core.impl.modules.attributes;


import cz.metacentrum.perun.audit.events.AttributesManagerEvents.AllAttributesRemovedForUser;
import cz.metacentrum.perun.audit.events.AttributesManagerEvents.AllAttributesRemovedForUserExtSource;
import cz.metacentrum.perun.audit.events.AttributesManagerEvents.AttributeRemovedForUser;
import cz.metacentrum.perun.audit.events.AttributesManagerEvents.AttributeSetForUser;
import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test module for urn:perun:user:attribute-def:def:eduPersonScopedAffiliationsManuallyAssigned
 *
 * @author Martin Kuba makub@ics.muni.cz
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
public class urn_perun_user_attribute_def_virt_eduPersonScopedAffiliationsTest {

	private static urn_perun_user_attribute_def_virt_eduPersonScopedAffiliations classInstance;
	private PerunSessionImpl session;
	private User user;
	private UserExtSource ues1;
	private UserExtSource ues2;
	private Attribute uesAtt1;
	private Attribute uesAtt2;
	private Attribute userAtt;
	private final String VALUE1 = "member@somewhere.edu";
	private final String VALUE2 = "affiliate@company.com";
	private final String VALUE3 = "library-walk-in@company.com";
	private final String KEY1 = "member@somewhere.org";
	private final String KEY2 = "affiliate@somewhere.edu";
	private LocalDate valid;
	private LocalDate invalid;

	@Before
	public void setVariables() {
		valid = LocalDate.now();
		invalid = valid.minusDays(1);
		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");

		user = new User();
		user.setId(1);

		ues1 = new UserExtSource(10, new ExtSource(100, "name1", "type1"), "login1");
		ues2 = new UserExtSource(20, new ExtSource(200, "name2", "type2"), "login2");

		uesAtt1 = new Attribute();
		uesAtt2 = new Attribute();
		uesAtt1.setValue(VALUE1);
		uesAtt2.setValue(VALUE2+";"+VALUE3);

		userAtt = new Attribute();
		Map<String, String> MAP_VALUE = new LinkedHashMap<>();
		MAP_VALUE.put(KEY1, valid.format(dateFormat));
		MAP_VALUE.put(KEY2, invalid.format(dateFormat));
		userAtt.setValue(MAP_VALUE);

		classInstance = new urn_perun_user_attribute_def_virt_eduPersonScopedAffiliations();
		session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);
	}

	@Test
	public void getAttributeValueFromBothSources() throws Exception {
		urn_perun_user_attribute_def_virt_eduPersonScopedAffiliations classInstance = new urn_perun_user_attribute_def_virt_eduPersonScopedAffiliations();
		PerunSessionImpl session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);

		when(session.getPerunBl().getUsersManagerBl().getUserExtSources(session, user)).thenReturn(
				Arrays.asList(ues1, ues2)
		);

		String sourceAttributeName = classInstance.getSourceAttributeName();
		String secondarySourceAttrName = classInstance.getSecondarySourceAttributeName();
		when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, ues1, sourceAttributeName)).thenReturn(
				uesAtt1
		);
		when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, ues2, sourceAttributeName)).thenReturn(
				uesAtt2
		);
		when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, secondarySourceAttrName)).thenReturn(
				userAtt
		);

		Attribute receivedAttr = classInstance.getAttributeValue(session, user, classInstance.getAttributeDefinition());
		assertTrue(receivedAttr.getValue() instanceof List);
		assertEquals("destination attribute name wrong",classInstance.getDestinationAttributeFriendlyName(),receivedAttr.getFriendlyName());

		@SuppressWarnings("unchecked")
		List<String> actual = (List<String>) receivedAttr.getValue();
		Collections.sort(actual);
		List<String> expected = Arrays.asList(VALUE1, VALUE2, VALUE3, KEY1);
		Collections.sort(expected);
		assertEquals("collected values are incorrect", expected, actual);
	}

	@Test
	public void getAttributeValueOnlyFromUserExtSources() throws Exception {
		urn_perun_user_attribute_def_virt_eduPersonScopedAffiliations classInstance = new urn_perun_user_attribute_def_virt_eduPersonScopedAffiliations();
		PerunSessionImpl session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);

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

	@SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
	@Test
	public void getAttributeValueOnlyFromEduPersonScopedAffiliationsManuallyAssigned() throws Exception {
		urn_perun_user_attribute_def_virt_eduPersonScopedAffiliations classInstance = new urn_perun_user_attribute_def_virt_eduPersonScopedAffiliations();
		PerunSessionImpl session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);

		String secondarySourceAttrName = classInstance.getSecondarySourceAttributeName();
		when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, secondarySourceAttrName)).thenReturn(
				userAtt
		);

		Attribute receivedAttr = classInstance.getAttributeValue(session, user, classInstance.getAttributeDefinition());
		assertTrue(receivedAttr.getValue() instanceof List);
		assertEquals("destination attribute name wrong",classInstance.getDestinationAttributeFriendlyName(),receivedAttr.getFriendlyName());

		@SuppressWarnings("unchecked")
		List<String> actual = (List<String>) receivedAttr.getValue();
		Collections.sort(actual);
		List<String> expected = Arrays.asList(KEY1);
		Collections.sort(expected);
		assertEquals("collected values are incorrect", expected, actual);
	}

	@Test
	public void resolveAttributeValueChangeTest() throws Exception {
		when(session.getPerunBl().getUsersManagerBl().getUserById(session, 1)).thenReturn(user);
		AuditEvent event = new AllAttributesRemovedForUserExtSource(ues1);
		List<AuditEvent> auditEvents = classInstance.resolveVirtualAttributeValueChange(session, event);

		assertEquals(auditEvents.get(0).getClass(), AttributeSetForUser.class);

		event = new AllAttributesRemovedForUser(user);
		auditEvents = classInstance.resolveVirtualAttributeValueChange(session, event);
		System.out.println(auditEvents);
		assertEquals(auditEvents.get(0).getClass(), AttributeSetForUser.class);

		Attribute attribute = new Attribute();
		attribute.setFriendlyName("eduPersonScopedAffiliationsManuallyAssigned");

		event = new AttributeSetForUser(attribute, user);
		auditEvents = classInstance.resolveVirtualAttributeValueChange(session, event);
		assertEquals(auditEvents.get(0).getClass(), AttributeSetForUser.class);

		when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, AttributesManager.NS_USER_ATTR_VIRT + ":" + "eduPersonScopedAffiliations")).thenReturn(attribute);
		auditEvents = classInstance.resolveVirtualAttributeValueChange(session, event);
		assertEquals(auditEvents.get(0).getClass(), AttributeRemovedForUser.class);
	}

}
