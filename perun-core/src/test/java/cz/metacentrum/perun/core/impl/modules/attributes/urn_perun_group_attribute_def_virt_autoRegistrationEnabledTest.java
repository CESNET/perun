package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class urn_perun_group_attribute_def_virt_autoRegistrationEnabledTest {

	private static urn_perun_group_attribute_def_virt_autoRegistrationEnabled classInstance;
	private PerunSessionImpl session;
	private Group groupA;
	private Group groupB;
	private Vo vo;
	private AttributeDefinition attrDef;

	@Before
	public void setUp() throws Exception {
		classInstance = new urn_perun_group_attribute_def_virt_autoRegistrationEnabled();
		session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);
		this.attrDef = classInstance.getAttributeDefinition();
		this.groupA = new Group("GroupA", "Group with auto registration enabled");
		this.groupA.setVoId(1);
		this.groupB = new Group("GroupB", "Group with auto registration disabled");
		this.groupB.setVoId(1);
		this.vo = new Vo(1, "vo", "");
	}

	@Test
	public void testAutoRegistrationEnabledGetAttributeValue() throws Exception {
		System.out.println("testAutoRegistrationEnabledGetAttributeValue()");
		when(session.getPerunBl().getGroupsManagerBl().isGroupForAnyAutoRegistration(session, groupA))
				.thenReturn(true);
		when(session.getPerunBl().getVosManagerBl().getVoById(session, groupA.getVoId()))
				.thenReturn(vo);
		when(session.getPerunBl().getVosManagerBl().usesEmbeddedGroupRegistrations(session, vo))
				.thenReturn(true);

		Boolean attributeValue = classInstance.getAttributeValue(session, groupA, attrDef).valueAsBoolean();
		assertThat(attributeValue)
				.isTrue();
	}

	@Test
	public void testAutoRegistrationDisabledGetAttributeValue() throws Exception {
		System.out.println("testAutoRegistrationDisabledAttributeValue()");
		when(session.getPerunBl().getGroupsManagerBl().isGroupForAnyAutoRegistration(session, groupB))
				.thenReturn(false);
		when(session.getPerunBl().getVosManagerBl().getVoById(session, groupB.getVoId()))
				.thenReturn(vo);
		when(session.getPerunBl().getVosManagerBl().usesEmbeddedGroupRegistrations(session, vo))
				.thenReturn(true);

		Boolean attributeValue = classInstance.getAttributeValue(session, groupB, attrDef).valueAsBoolean();
		assertThat(attributeValue)
				.isFalse();
	}

	@Test
	public void testAutoRegistrationNotUsed() throws Exception {
		System.out.println("testAutoRegistrationNotUsed()");
		when(session.getPerunBl().getGroupsManagerBl().isGroupForAnyAutoRegistration(session, groupB))
				.thenReturn(false);
		when(session.getPerunBl().getVosManagerBl().getVoById(session, groupB.getVoId()))
				.thenReturn(vo);
		when(session.getPerunBl().getVosManagerBl().usesEmbeddedGroupRegistrations(session, vo))
				.thenReturn(false);

		Boolean attributeValue = classInstance.getAttributeValue(session, groupB, attrDef).valueAsBoolean();
		assertThat(attributeValue)
				.isNull();
	}
}
