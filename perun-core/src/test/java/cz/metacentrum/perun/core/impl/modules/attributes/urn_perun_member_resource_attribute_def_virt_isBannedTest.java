package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.FacilityManagerEvents.BanRemovedForFacility;
import cz.metacentrum.perun.audit.events.FacilityManagerEvents.BanSetForFacility;
import cz.metacentrum.perun.audit.events.FacilityManagerEvents.BanUpdatedForFacility;
import cz.metacentrum.perun.audit.events.FacilityManagerEvents.SecurityTeamAssignedToFacility;
import cz.metacentrum.perun.audit.events.ResourceManagerEvents.BanRemovedForResource;
import cz.metacentrum.perun.audit.events.ResourceManagerEvents.BanSetForResource;
import cz.metacentrum.perun.audit.events.ResourceManagerEvents.BanUpdatedForResource;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.BanOnFacility;
import cz.metacentrum.perun.core.api.BanOnResource;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.Status;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
/**
 *
 * @author Michal Stava &lt;stavamichal@gmail.com&gt;
 */
public class urn_perun_member_resource_attribute_def_virt_isBannedTest {

	private static urn_perun_member_resource_attribute_def_virt_isBanned classInstance;
	private Attribute isBanned;
	private Resource resource;
	private Facility facility;
	private Vo vo;
	private Member member;
	private User user;
	private PerunSessionImpl session;
	private AuditEvent event1;
	private AuditEvent event2;
	private AuditEvent event3;
	private AuditEvent event4;
	private AuditEvent event5;
	private AuditEvent event6;
	private AuditEvent wrongEvent;

	public urn_perun_member_resource_attribute_def_virt_isBannedTest() {
	}

	@Before
	public void setUp() {
		classInstance = new urn_perun_member_resource_attribute_def_virt_isBanned();
		session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);
		facility = new Facility(1, "testFacility");
		resource = new Resource(1, "testResource", "des\nc", 1, 1);
		vo = new Vo(1, "testVo", "desc");
		user = new User(1, "name", "surname", "middlename", "title", "title");
		member = new Member(1, 1, 1, Status.VALID);
		isBanned = new Attribute(classInstance.getAttributeDefinition());
		isBanned.setValue(true);
		event1 = new BanSetForResource(new BanOnResource(), member.getId(), resource.getId());
		event2 = new BanUpdatedForResource(new BanOnResource(), member.getId(), resource.getId());
		event3 = new BanRemovedForResource(new BanOnResource(), member.getId(), resource.getId());
		event4 = new BanSetForFacility(new BanOnFacility(), user.getId(), facility.getId());
		event5 = new BanUpdatedForFacility(new BanOnFacility(), user.getId(), facility.getId());
		event6 = new BanRemovedForFacility(new BanOnFacility(), user.getId(), facility.getId());
		wrongEvent = new SecurityTeamAssignedToFacility();
	}

	@Test
	public void resolveVirtualAttributeValueChangeTest() throws Exception{
		System.out.println("urn_perun_user_facility_attribute_def_virt_defaultUnixGID.resolveVirtualAttributeValueChangeTest()");

		List<AuditEvent> resolvedEvents;

		//for message 1, 2 and 3
		when(session.getPerunBl().getMembersManagerBl().getMemberById(any(PerunSessionImpl.class), anyInt())).thenReturn(member);
		when(session.getPerunBl().getResourcesManagerBl().getResourceById(any(PerunSessionImpl.class), anyInt())).thenReturn(resource);
		when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSessionImpl.class), any(Member.class), any(Resource.class), anyString())).thenReturn(isBanned);
		resolvedEvents = classInstance.resolveVirtualAttributeValueChange(session, event1);
		assertEquals(resolvedEvents.size(), 1);
		assertEquals(resolvedEvents.get(0).getMessage(), isBanned.serializeToString() + " set for " + resource.serializeToString() + " and " + member.serializeToString() + ".");
		resolvedEvents = classInstance.resolveVirtualAttributeValueChange(session, event2);
		assertEquals(resolvedEvents.get(0).getMessage(), isBanned.serializeToString() + " set for " + resource.serializeToString() + " and " + member.serializeToString() + ".");
		resolvedEvents = classInstance.resolveVirtualAttributeValueChange(session, event3);
		assertEquals(resolvedEvents.get(0).getMessage(), isBanned.serializeToString() + " removed for " + resource.serializeToString() + " and " + member.serializeToString() + ".");

		//for message 4, 5 and 6
		when(session.getPerunBl().getUsersManagerBl().getUserById(any(PerunSessionImpl.class), anyInt())).thenReturn(user);
		when(session.getPerunBl().getFacilitiesManagerBl().getFacilityById(any(PerunSessionImpl.class), anyInt())).thenReturn(facility);
		when(session.getPerunBl().getFacilitiesManagerBl().getAssignedResources(any(PerunSessionImpl.class), any(Facility.class))).thenReturn(Collections.singletonList(resource));
		when(session.getPerunBl().getResourcesManagerBl().getAssignedMembers(any(PerunSessionImpl.class), any(Resource.class))).thenReturn(Collections.singletonList(member));
		resolvedEvents = classInstance.resolveVirtualAttributeValueChange(session, event4);
		assertEquals(resolvedEvents.get(0).getMessage(), isBanned.serializeToString() + " set for " + resource.serializeToString() + " and " + member.serializeToString() + ".");
		resolvedEvents = classInstance.resolveVirtualAttributeValueChange(session, event5);
		assertEquals(resolvedEvents.get(0).getMessage(), isBanned.serializeToString() + " set for " + resource.serializeToString() + " and " + member.serializeToString() + ".");
		resolvedEvents = classInstance.resolveVirtualAttributeValueChange(session, event6);
		assertEquals(resolvedEvents.get(0).getMessage(), isBanned.serializeToString() + " removed for " + resource.serializeToString() + " and " + member.serializeToString() + ".");
	}

	@Test
	public void resolveVirtualAttributeValueChangeTestWithWrongMatch() throws Exception {
		System.out.println("urn_perun_user_facility_attribute_def_virt_defaultUnixGID.resolveVirtualAttributeValueChangeTestWithWrongMatch()");

		List<AuditEvent> resolvedEvents;

		//for wrong message
		when(session.getPerunBl().getMembersManagerBl().getMemberById(any(PerunSessionImpl.class), anyInt())).thenReturn(member);
		when(session.getPerunBl().getResourcesManagerBl().getResourceById(any(PerunSessionImpl.class), anyInt())).thenReturn(resource);
		when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSessionImpl.class), any(Member.class), any(Resource.class), anyString())).thenReturn(isBanned);
		resolvedEvents = classInstance.resolveVirtualAttributeValueChange(session, wrongEvent);
		assertTrue(resolvedEvents.isEmpty());
	}
}
