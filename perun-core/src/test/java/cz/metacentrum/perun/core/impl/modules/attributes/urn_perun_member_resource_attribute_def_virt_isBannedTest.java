package cz.metacentrum.perun.core.impl.modules.attributes;

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
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
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
	private BanOnResource banOnResource;
	private BanOnFacility banOnFacility;
	private String message1;
	private String message2;
	private String message3;
	private String message4;
	private String message5;
	private String message6;
	private String wrongMessage;

	public urn_perun_member_resource_attribute_def_virt_isBannedTest() {
	}

	@Before
	public void setUp() throws Exception{
		classInstance = new urn_perun_member_resource_attribute_def_virt_isBanned();
		session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);
		facility = new Facility(1, "testFacility");
		resource = new Resource(1, "testResource", "desc", 1, 1);
		vo = new Vo(1, "testVo", "desc");
		user = new User(1, "name", "surname", "middlename", "title", "title");
		member = new Member(1, 1, 1, Status.VALID);
		banOnResource = new BanOnResource(1, new Date(), "test", 1, 1);
		banOnFacility = new BanOnFacility(1, new Date(), "test", 1, 1);
		isBanned = new Attribute(classInstance.getAttributeDefinition());
		isBanned.setValue(true);
		message1 = "Ban " + banOnResource.serializeToString()+ " was set for memberId 1 on resourceId 1";
		message2 = "Ban " + banOnResource.serializeToString()+ " was updated for memberId 1 on resourceId 1";
		message3 = "Ban " + banOnResource.serializeToString()+ " was removed for memberId 1 on resourceId 1";
		message4 = "Ban " + banOnFacility.serializeToString()+ " was set for userId 1 on facilityId 1";
		message5 = "Ban " + banOnFacility.serializeToString()+ " was updated for userId 1 on facilityId 1";
		message6 = "Ban " + banOnFacility.serializeToString()+ " was removed for userId 1 on facilityId 1";
		wrongMessage = "Ban " + banOnFacility.serializeToString()+ " was destroyed for userId 1 on facilityId 1";
	}

	@Test
	public void resolveVirtualAttributeValueChangeTest() throws Exception{
		System.out.println("urn_perun_user_facility_attribute_def_virt_defaultUnixGID.resolveVirtualAttributeValueChangeTest()");
				
		List<String> resolvedMessages;

		//for message 1, 2 and 3
		when(session.getPerunBl().getMembersManagerBl().getMemberById(any(PerunSessionImpl.class), anyInt())).thenReturn(member);
		when(session.getPerunBl().getResourcesManagerBl().getResourceById(any(PerunSessionImpl.class), anyInt())).thenReturn(resource);
		when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSessionImpl.class), any(Resource.class), any(Member.class), anyString())).thenReturn(isBanned);
		resolvedMessages = classInstance.resolveVirtualAttributeValueChange(session, message1);
		assertEquals(resolvedMessages.size(), 1);
		assertEquals(resolvedMessages.get(0), isBanned.serializeToString() + " set for " + resource.serializeToString() + " and " + member.serializeToString());
		resolvedMessages = classInstance.resolveVirtualAttributeValueChange(session, message2);
		assertEquals(resolvedMessages.get(0), isBanned.serializeToString() + " set for " + resource.serializeToString() + " and " + member.serializeToString());
		resolvedMessages = classInstance.resolveVirtualAttributeValueChange(session, message3);
		assertEquals(resolvedMessages.get(0), isBanned.serializeToString() + " removed for " + resource.serializeToString() + " and " + member.serializeToString());

		//for message 4, 5 and 6
		when(session.getPerunBl().getUsersManagerBl().getUserById(any(PerunSessionImpl.class), anyInt())).thenReturn(user);
		when(session.getPerunBl().getFacilitiesManagerBl().getFacilityById(any(PerunSessionImpl.class), anyInt())).thenReturn(facility);
		when(session.getPerunBl().getFacilitiesManagerBl().getAssignedResources(any(PerunSessionImpl.class), any(Facility.class))).thenReturn(Arrays.asList(resource));
		when(session.getPerunBl().getResourcesManagerBl().getAssignedMembers(any(PerunSessionImpl.class), any(Resource.class))).thenReturn(Arrays.asList(member));
		resolvedMessages = classInstance.resolveVirtualAttributeValueChange(session, message4);
		assertEquals(resolvedMessages.get(0), isBanned.serializeToString() + " set for " + resource.serializeToString() + " and " + member.serializeToString());
		resolvedMessages = classInstance.resolveVirtualAttributeValueChange(session, message5);
		assertEquals(resolvedMessages.get(0), isBanned.serializeToString() + " set for " + resource.serializeToString() + " and " + member.serializeToString());
		resolvedMessages = classInstance.resolveVirtualAttributeValueChange(session, message6);
		assertEquals(resolvedMessages.get(0), isBanned.serializeToString() + " removed for " + resource.serializeToString() + " and " + member.serializeToString());
	}

	@Test
	public void resolveVirtualAttributeValueChangeTestWithWrongMatch() throws Exception {
		System.out.println("urn_perun_user_facility_attribute_def_virt_defaultUnixGID.resolveVirtualAttributeValueChangeTestWithWrongMatch()");

		List<String> resolvedMessages;

		//for wrong message
		when(session.getPerunBl().getMembersManagerBl().getMemberById(any(PerunSessionImpl.class), anyInt())).thenReturn(member);
		when(session.getPerunBl().getResourcesManagerBl().getResourceById(any(PerunSessionImpl.class), anyInt())).thenReturn(resource);
		when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSessionImpl.class), any(Resource.class), any(Member.class), anyString())).thenReturn(isBanned);
		resolvedMessages = classInstance.resolveVirtualAttributeValueChange(session, wrongMessage);
		assertTrue(resolvedMessages.isEmpty());
	}
}
