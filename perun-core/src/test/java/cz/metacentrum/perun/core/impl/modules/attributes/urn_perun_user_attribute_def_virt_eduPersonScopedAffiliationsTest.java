package cz.metacentrum.perun.core.impl.modules.attributes;


import cz.metacentrum.perun.audit.events.AttributesManagerEvents.AllAttributesRemovedForUser;
import cz.metacentrum.perun.audit.events.AttributesManagerEvents.AllAttributesRemovedForUserExtSource;
import cz.metacentrum.perun.audit.events.AttributesManagerEvents.AttributeChangedForUser;
import cz.metacentrum.perun.audit.events.AttributesManagerEvents.AttributeSetForUser;
import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.MemberGroupStatus;
import cz.metacentrum.perun.core.api.Status;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
 * Test module for urn:perun:user:attribute-def:virt:eduPersonScopedAffiliations
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
	private Attribute userAtt1;
	private final String VALUE1 = "11aff11@somewhere.edu";
	private final String VALUE2 = "22aff22@somewhere.edu";
	private final String VALUE3 = "33aff33@somewhere.edu";
	private final String VALUE4 = "44aff44@somewhere.edu";
	private final String VALUE5 = "55aff55@somewhere.edu";
	private final String VALUE6 = "66aff66@somewhere.edu";
	private final String VALUE7 = "77aff77@somewhere.edu";

	private LocalDate valid;
	private LocalDate invalid;
	private Member validMember;
	private Member expiredMember;
	private Member groupMember1;
	private Member groupMember2;
	private final int gId1 = 1;
	private final int gId2 = 2;
	private Group group1;
	private Group group2;
	private Attribute groupAtt1;

	@Before
	public void setVariables() {
		classInstance = new urn_perun_user_attribute_def_virt_eduPersonScopedAffiliations();
		session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);

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

		userAtt1 = new Attribute();
		Map<String, String> mapValue = new LinkedHashMap<>();
		mapValue.put(VALUE6, valid.format(dateFormat));
		mapValue.put(VALUE7, invalid.format(dateFormat));
		userAtt1.setValue(mapValue);

		group1 = new Group(gId1, "G1", "G1_DSC", null, null, null, null, null, null);
		group2 = new Group(gId2, "G2", "G2_DSC", null, null, null, null, null, null);

		validMember = new Member(1, 1, 1, Status.VALID);
		expiredMember = new Member(2, 1, 1, Status.EXPIRED);

		groupMember1 = new Member(1, 1, 1, Status.VALID);
		groupMember1.setSourceGroupId(gId1);
		groupMember1.putGroupStatus(gId1, MemberGroupStatus.VALID);

		groupMember2 = new Member(1, 1, 1, Status.VALID);
		groupMember2.setSourceGroupId(gId2);
		groupMember2.putGroupStatus(gId2, MemberGroupStatus.EXPIRED);

		groupAtt1 = new Attribute();
		List<String> arrListValue = new ArrayList<>();
		arrListValue.add(VALUE4);
		arrListValue.add(VALUE5);
		groupAtt1.setValue(arrListValue);
	}

	@Test
	public void getAttributeValueFromAllSources() throws Exception {
		urn_perun_user_attribute_def_virt_eduPersonScopedAffiliations classInstance = new urn_perun_user_attribute_def_virt_eduPersonScopedAffiliations();
		PerunSessionImpl session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);

		String primarySourceAttributeName = classInstance.getSourceAttributeName();
		String secondarySourceAttrName = classInstance.getSecondarySourceAttributeName();
		String tertiarySourceAttrName = classInstance.getTertiarySourceAttributeName();

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

		// MANUALLY_ASSIGNED_AFFILIATIONS
		when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, secondarySourceAttrName)).thenReturn(
				userAtt1
		);

		// AFFILIATIONS_FROM_GROUP
		when(session.getPerunBl().getMembersManagerBl().getMembersByUser(session, user)).thenReturn(
				Arrays.asList(validMember, expiredMember)
		);
		when(session.getPerunBl().getGroupsManagerBl().getMemberGroups(session, validMember)).thenReturn(
				Arrays.asList(group1, group2)
		);
		when(session.getPerunBl().getGroupsManagerBl().getGroupMemberById(session, group1, validMember.getId())).thenReturn(
				groupMember1
		);
		when(session.getPerunBl().getGroupsManagerBl().getGroupMemberById(session, group2, validMember.getId())).thenReturn(
				groupMember2
		);
		when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, group1, tertiarySourceAttrName)).thenReturn(
				groupAtt1
		);

		Attribute receivedAttr = classInstance.getAttributeValue(session, user, classInstance.getAttributeDefinition());
		assertTrue(receivedAttr.getValue() instanceof List);
		assertEquals("destination attribute name wrong",classInstance.getDestinationAttributeFriendlyName(),receivedAttr.getFriendlyName());

		@SuppressWarnings("unchecked")
		List<String> actual = (List<String>) receivedAttr.getValue();
		Collections.sort(actual);
		List<String> expected = Arrays.asList(VALUE1, VALUE2, VALUE3, VALUE4, VALUE5, VALUE6);
		Collections.sort(expected);
		assertEquals("collected values are incorrect", expected, actual);
	}

	@Test
	public void getAttributeValueOnlyGroupAffiliations() throws Exception {
		urn_perun_user_attribute_def_virt_eduPersonScopedAffiliations classInstance = new urn_perun_user_attribute_def_virt_eduPersonScopedAffiliations();
		PerunSessionImpl session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);

		when(session.getPerunBl().getMembersManagerBl().getMembersByUser(session, user)).thenReturn(
			Arrays.asList(validMember, expiredMember)
		);
		when(session.getPerunBl().getGroupsManagerBl().getMemberGroups(session, validMember)).thenReturn(
			Arrays.asList(group1, group2)
		);
		when(session.getPerunBl().getGroupsManagerBl().getGroupMemberById(session, group1, validMember.getId())).thenReturn(
			groupMember1
		);
		when(session.getPerunBl().getGroupsManagerBl().getGroupMemberById(session, group2, validMember.getId())).thenReturn(
			groupMember2
		);

		String tertiarySourceAttrName = classInstance.getTertiarySourceAttributeName();
		when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, group1, tertiarySourceAttrName)).thenReturn(
			groupAtt1
		);

		Attribute receivedAttr = classInstance.getAttributeValue(session, user, classInstance.getAttributeDefinition());
		assertTrue(receivedAttr.getValue() instanceof List);
		assertEquals("destination attribute name wrong",classInstance.getDestinationAttributeFriendlyName(),receivedAttr.getFriendlyName());

		@SuppressWarnings("unchecked")
		List<String> actual = (List<String>) receivedAttr.getValue();
		Collections.sort(actual);
		List<String> expected = Arrays.asList(VALUE4, VALUE5);
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

	@Test
	public void getAttributeValueOnlyFromEduPersonScopedAffiliationsManuallyAssigned() throws Exception {
		urn_perun_user_attribute_def_virt_eduPersonScopedAffiliations classInstance = new urn_perun_user_attribute_def_virt_eduPersonScopedAffiliations();
		PerunSessionImpl session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);

		String secondarySourceAttrName = classInstance.getSecondarySourceAttributeName();
		when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, secondarySourceAttrName)).thenReturn(
			userAtt1
		);

		Attribute receivedAttr = classInstance.getAttributeValue(session, user, classInstance.getAttributeDefinition());
		assertTrue(receivedAttr.getValue() instanceof List);
		assertEquals("destination attribute name wrong",classInstance.getDestinationAttributeFriendlyName(),receivedAttr.getFriendlyName());

		@SuppressWarnings("unchecked")
		List<String> actual = (List<String>) receivedAttr.getValue();
		Collections.sort(actual);
		List<String> expected = new ArrayList<>();
		expected.add(VALUE6);
		Collections.sort(expected);
		assertEquals("collected values are incorrect", expected, actual);
	}

	@Test
	public void resolveAttributeValueChangeTest() throws Exception {
		when(session.getPerunBl().getUsersManagerBl().getUserById(session, 1)).thenReturn(user);
		AuditEvent event = new AllAttributesRemovedForUserExtSource(ues1);
		List auditEvents = classInstance.resolveVirtualAttributeValueChange(session, event);

		assertEquals(auditEvents.get(0).getClass(), AttributeChangedForUser.class);

		event = new AllAttributesRemovedForUser(user);
		auditEvents = classInstance.resolveVirtualAttributeValueChange(session, event);
		assertEquals(auditEvents.get(0).getClass(), AttributeChangedForUser.class);

		Attribute attribute = new Attribute();
		attribute.setFriendlyName("eduPersonScopedAffiliationsManuallyAssigned");

		event = new AttributeSetForUser(attribute, user);
		auditEvents = classInstance.resolveVirtualAttributeValueChange(session, event);
		assertEquals(auditEvents.get(0).getClass(), AttributeChangedForUser.class);

		when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, AttributesManager.NS_USER_ATTR_VIRT + ":" + "eduPersonScopedAffiliations")).thenReturn(attribute);
		auditEvents = classInstance.resolveVirtualAttributeValueChange(session, event);
		assertEquals(auditEvents.get(0).getClass(), AttributeChangedForUser.class);
	}

}
