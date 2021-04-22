package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class urn_perun_group_attribute_def_virt_autoRegistrationEnabledTest {

	private static urn_perun_group_attribute_def_virt_autoRegistrationEnabled classInstance;
	private PerunSessionImpl session;
	private Group groupA;
	private Group groupB;
	private AttributeDefinition attrDef;

	@Before
	public void setUp() throws Exception {
		classInstance = new urn_perun_group_attribute_def_virt_autoRegistrationEnabled();
		session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);
		this.attrDef = classInstance.getAttributeDefinition();
		this.groupA = new Group("GroupA", "Group with auto registration enabled");
		this.groupB = new Group("GroupB", "Group with auto registration disabled");
	}

	@Test
	public void testAutoRegistrationEnabledGetAttributeValue() throws Exception {
		System.out.println("testAutoRegistrationEnabledGetAttributeValue()");
		when(session.getPerunBl().getGroupsManagerBl().isGroupForAutoRegistration(session, groupA)).thenReturn(true);
		boolean attributeValue = classInstance.getAttributeValue(session, groupA, attrDef).valueAsBoolean();
		assertTrue(attributeValue);
	}

	@Test
	public void testAutoRegistrationDisabledGetAttributeValue() throws Exception {
		System.out.println("testAutoRegistrationDisabledAttributeValue()");
		when(session.getPerunBl().getGroupsManagerBl().isGroupForAutoRegistration(session, groupB)).thenReturn(false);
		boolean attributeValue = classInstance.getAttributeValue(session, groupB, attrDef).valueAsBoolean();
		assertFalse(attributeValue);
	}
}
