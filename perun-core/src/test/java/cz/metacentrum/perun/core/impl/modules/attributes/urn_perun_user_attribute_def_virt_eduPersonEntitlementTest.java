package cz.metacentrum.perun.core.impl.modules.attributes;


import cz.metacentrum.perun.audit.events.AttributesManagerEvents.AllAttributesRemovedForUser;
import cz.metacentrum.perun.audit.events.AttributesManagerEvents.AllAttributesRemovedForUserExtSource;
import cz.metacentrum.perun.audit.events.AttributesManagerEvents.AttributeRemovedForUes;
import cz.metacentrum.perun.audit.events.AttributesManagerEvents.AttributeRemovedForUser;
import cz.metacentrum.perun.audit.events.AttributesManagerEvents.AttributeSetForUes;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test module for urn:perun:user:attribute-def:virt:eduPersonEntitlement
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
public class urn_perun_user_attribute_def_virt_eduPersonEntitlementTest {

	private static urn_perun_user_attribute_def_virt_eduPersonEntitlement classInstance;
	private PerunSessionImpl session;
	private User user;
	private UserExtSource ues1;
	private UserExtSource ues2;
	private Attribute uesAtt1;
	private Attribute uesAtt2;
	private Attribute groupNamesAttr;
	private final String VALUE1 = "11entitlement11@somewhere.edu";
	private final String VALUE2 = "22entitlement22@somewhere.edu";
	private final String VALUE3 = "33entitlement33@somewhere.edu";
	private final String VALUE4 = "44entitlement44@somewhere.edu";
	private final String VALUE5 = "55entitlement55@somewhere.edu";

	@Before
	public void setVariables() {
		classInstance = new urn_perun_user_attribute_def_virt_eduPersonEntitlement();
		session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);

		user = new User();
		user.setId(1);

		ues1 = new UserExtSource(10, new ExtSource(100, "name1", "type1"), "login1");
		ues2 = new UserExtSource(20, new ExtSource(200, "name2", "type2"), "login2");
		ues1.setUserId(user.getId());
		ues2.setUserId(user.getId());

		uesAtt1 = new Attribute();
		uesAtt2 = new Attribute();
		uesAtt1.setValue(VALUE1);
		uesAtt2.setValue(VALUE2+";"+VALUE3);

		groupNamesAttr = new Attribute();
		List<String> arrListValue = new ArrayList<>();
		arrListValue.add(VALUE4);
		arrListValue.add(VALUE5);
		groupNamesAttr.setValue(arrListValue);
	}

	@Test
	public void getAttributeValueFromAllSources() throws Exception {
		urn_perun_user_attribute_def_virt_eduPersonEntitlement classInstance = new urn_perun_user_attribute_def_virt_eduPersonEntitlement();
		PerunSessionImpl session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);

		String primarySourceAttributeName = classInstance.getSourceAttributeName();
		String secondarySourceAttrName = classInstance.getSecondarySourceAttributeName();

		// USER_EXT_SOURCE
		when(session.getPerunBl().getUsersManagerBl().getUserExtSources(session, user)).thenReturn(
				Arrays.asList(ues1, ues2)
		);
		when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, ues1, primarySourceAttributeName)).thenReturn(
				uesAtt1
		);
		when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, ues2, primarySourceAttributeName)).thenReturn(
				uesAtt2
		);

		// ENTITLEMENTS FROM GROUP_NAMES
		when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, secondarySourceAttrName)).thenReturn(
				groupNamesAttr
		);

		Attribute receivedAttr = classInstance.getAttributeValue(session, user, classInstance.getAttributeDefinition());
		assertTrue(receivedAttr.getValue() instanceof List);
		assertEquals("destination attribute name wrong",classInstance.getDestinationAttributeFriendlyName(),receivedAttr.getFriendlyName());

		@SuppressWarnings("unchecked")
		List<String> actual = (List<String>) receivedAttr.getValue();
		Collections.sort(actual);
		List<String> expected = Arrays.asList(VALUE1, VALUE2, VALUE3, VALUE4, VALUE5);
		Collections.sort(expected);
		assertEquals("collected values are incorrect", expected, actual);
	}

	@Test
	public void getAttributeValueOnlyGroupNames() throws Exception {
		urn_perun_user_attribute_def_virt_eduPersonEntitlement classInstance = new urn_perun_user_attribute_def_virt_eduPersonEntitlement();
		PerunSessionImpl session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);

		String secondarySourceAttrName = classInstance.getSecondarySourceAttributeName();

		when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, secondarySourceAttrName)).thenReturn(
				groupNamesAttr
		);

		Attribute receivedAttr = classInstance.getAttributeValue(session, user, classInstance.getAttributeDefinition());
		assertTrue(receivedAttr.getValue() instanceof List);
		assertEquals("destination attribute name wrong", classInstance.getDestinationAttributeFriendlyName(), receivedAttr.getFriendlyName());

		@SuppressWarnings("unchecked")
		List<String> actual = (List<String>) receivedAttr.getValue();
		Collections.sort(actual);
		List<String> expected = Arrays.asList(VALUE4, VALUE5);
		Collections.sort(expected);
		assertEquals("collected values are incorrect", expected, actual);
	}

	@Test
	public void getAttributeValueOnlyFromUserExtSources() throws Exception {
		urn_perun_user_attribute_def_virt_eduPersonEntitlement classInstance = new urn_perun_user_attribute_def_virt_eduPersonEntitlement();
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
		assertEquals("destination attribute name wrong", classInstance.getDestinationAttributeFriendlyName(), receivedAttr.getFriendlyName());

		@SuppressWarnings("unchecked")
		List<String> actual = (List<String>) receivedAttr.getValue();
		Collections.sort(actual);
		List<String> expected = Arrays.asList(VALUE1, VALUE2, VALUE3);
		Collections.sort(expected);
		assertEquals("collected values are incorrect", expected, actual);
	}

	@Test
	public void resolveAttributeValueChangeTest() throws Exception {
		when(session.getPerunBl().getUsersManagerBl().getUserById(session, 1)).thenReturn(user);

		AuditEvent event = new AllAttributesRemovedForUserExtSource(ues1);
		List<AuditEvent> auditEvents = classInstance.resolveVirtualAttributeValueChange(session, event);
		assertEquals(AttributeSetForUser.class, auditEvents.get(0).getClass());

		Attribute uesAttribute = new Attribute();
		uesAttribute.setFriendlyName(classInstance.getSourceAttributeFriendlyName());

		event = new AttributeSetForUes(uesAttribute, ues1);
		auditEvents = classInstance.resolveVirtualAttributeValueChange(session, event);
		assertEquals(AttributeSetForUser.class, auditEvents.get(0).getClass());

		event = new AttributeRemovedForUes(uesAttribute, ues1);
		auditEvents = classInstance.resolveVirtualAttributeValueChange(session, event);
		assertEquals(AttributeSetForUser.class, auditEvents.get(0).getClass());

		event = new AllAttributesRemovedForUser(user);
		auditEvents = classInstance.resolveVirtualAttributeValueChange(session, event);
		assertEquals(AttributeSetForUser.class, auditEvents.get(0).getClass());

		Attribute attribute = new Attribute();
		attribute.setFriendlyName(classInstance.getSecondarySourceAttributeFriendlyName());

		event = new AttributeSetForUser(attribute, user);
		auditEvents = classInstance.resolveVirtualAttributeValueChange(session, event);
		assertEquals(AttributeSetForUser.class, auditEvents.get(0).getClass());

		// change behavior to empty resulting entitlements
		when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, AttributesManager.NS_USER_ATTR_VIRT + ":" + "eduPersonEntitlement")).thenReturn(new Attribute());

		event = new AttributeSetForUser(attribute, user);
		auditEvents = classInstance.resolveVirtualAttributeValueChange(session, event);
		assertEquals(AttributeRemovedForUser.class, auditEvents.get(0).getClass());
	}

}
