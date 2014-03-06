package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 * @author Jakub Peschel <410368@mail.muni.cz
 */
public class urn_perun_user_facility_attribute_def_virt_defaultUnixGIDTest {

    public urn_perun_user_facility_attribute_def_virt_defaultUnixGIDTest() {
    }
    private static urn_perun_user_facility_attribute_def_virt_defaultUnixGID classInstance;
    private static PerunSessionImpl session;
    private static Attribute prefferedAttr;
    private static Attribute defDefGIDAttr;
    private static Attribute resourceGIDListAttr;
    private static List<String> listOfGIDsListString;
    private static User user;
    private static Facility facility;
    private static Resource resource;
    private static Resource resource1;
    private static List<Resource> resourceList;
    private static String namespace;
    private static Group group;
    private static List<Group> groups;
    private static List<User> users;
    private static Member member;
    private static List<Member> members;

    @Before
    public void setUp() {
        classInstance = new urn_perun_user_facility_attribute_def_virt_defaultUnixGID();
        session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);
        prefferedAttr = new Attribute();
        defDefGIDAttr = new Attribute();
        resourceGIDListAttr = new Attribute();
        listOfGIDsListString = new ArrayList<>();
        for (Integer i = 0; i < 5; i++) {
            listOfGIDsListString.add(i.toString());
        }
        prefferedAttr.setValue(new ArrayList(listOfGIDsListString));
        resourceGIDListAttr.setValue(2);
        namespace = "Test";


        user = new User();
        users = new ArrayList<User>();
        users.add(user);
        
        facility = new Facility();
        resource = new Resource();
        resource.setName("myResource");
        resource.setDescription("desc");

        resource1 = new Resource();
        resource1.setId(1);
        resource1.setName("myResource");
        resource1.setDescription("desc");

        resourceList = new ArrayList<>();
        resourceList.add(resource1);
        
        group = new Group("group", "desc");
        groups = new ArrayList<>();
        groups.add(group);
        
        member = new Member();
        member.setUserId(user.getId());
        members = new ArrayList<>();
        members.add(member);
        
        
    }

    @Test
    public void getAttributeValueTest() throws Exception {
        System.out.println("urn_perun_user_facility_attribute_def_virt_defaultUnixGid.GetAttributeValue()");

        when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSessionImpl.class), any(Facility.class), any(User.class), eq(AttributesManager.NS_USER_FACILITY_ATTR_DEF + ":defaultUnixGID"))).thenReturn(defDefGIDAttr);
        when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSessionImpl.class), any(Facility.class), eq(AttributesManager.NS_FACILITY_ATTR_DEF + ":unixGID-namespace")).getValue()).thenReturn(namespace);
        when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSessionImpl.class), any(Resource.class), eq(AttributesManager.NS_RESOURCE_ATTR_DEF + ":unixGID-namespace:" + namespace))).thenReturn(resourceGIDListAttr);
        when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSessionImpl.class), any(User.class), eq(AttributesManager.NS_USER_ATTR_DEF + ":preferredDefaultUnixGIDs-namespace:" + namespace))).thenReturn(prefferedAttr);

        when(session.getPerunBl().getUsersManagerBl().getAllowedResources(any(PerunSessionImpl.class), any(Facility.class), any(User.class))).thenReturn(resourceList);
        
        when(session.getPerunBl().getGroupsManagerBl().getAssignedGroupsToResource(any(PerunSessionImpl.class), any(Resource.class))).thenReturn(groups);

        when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSessionImpl.class), any(Group.class), eq(AttributesManager.NS_GROUP_ATTR_DEF + ":unixGID-namespace:" + namespace)).getValue()).thenReturn(20);
                
        when(session.getPerunBl().getMembersManagerBl().getMembersByUser(any(PerunSessionImpl.class), any(User.class))).thenReturn(members);
        when(session.getPerunBl().getGroupsManagerBl().getGroupMembers(any(PerunSessionImpl.class), any(Group.class))).thenReturn(members);
        
        Attribute testAttr = classInstance.getAttributeValue(session, facility, user, session.getPerunBl().getAttributesManagerBl().getAttributeDefinition(session, AttributesManager.NS_USER_FACILITY_ATTR_VIRT + ""));
        assertEquals(resourceGIDListAttr, testAttr);

    }

    @Test
    public void getAttributeValueWhenDefaultUnixGidIsSetTest() throws Exception {
        System.out.println("urn_perun_user_facility_attribute_def_virt_defaultUnixGid.getAttributeValueWhenDefaultUnixGidIsSetTest()");

        defDefGIDAttr.setValue(125);
        when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSessionImpl.class), any(Facility.class), any(User.class), eq(AttributesManager.NS_USER_FACILITY_ATTR_DEF + ":defaultUnixGID"))).thenReturn(defDefGIDAttr);
        when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSessionImpl.class), any(Facility.class), eq(AttributesManager.NS_FACILITY_ATTR_DEF + ":unixGID-namespace")).getValue()).thenReturn(namespace);
        when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSessionImpl.class), any(Resource.class), eq(AttributesManager.NS_RESOURCE_ATTR_DEF + ":unixGID-namespace:" + namespace))).thenReturn(resourceGIDListAttr);
        when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSessionImpl.class), any(User.class), eq(AttributesManager.NS_USER_ATTR_DEF + ":preferredDefaultUnixGIDs-namespace:" + namespace))).thenReturn(prefferedAttr);

        when(session.getPerunBl().getUsersManagerBl().getAllowedResources(any(PerunSessionImpl.class), any(Facility.class), any(User.class))).thenReturn(resourceList);
        
        when(session.getPerunBl().getGroupsManagerBl().getAssignedGroupsToResource(any(PerunSessionImpl.class), any(Resource.class))).thenReturn(groups);
        when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSessionImpl.class), any(Group.class), eq(AttributesManager.NS_GROUP_ATTR_DEF + ":unixGID-namespace:" + namespace)).getValue()).thenReturn(20);
        
        when(session.getPerunBl().getMembersManagerBl().getMembersByUser(any(PerunSessionImpl.class), any(User.class))).thenReturn(members);
        when(session.getPerunBl().getGroupsManagerBl().getGroupMembers(any(PerunSessionImpl.class), any(Group.class))).thenReturn(members);
        
        Attribute testAttr = classInstance.getAttributeValue(session, facility, user, session.getPerunBl().getAttributesManagerBl().getAttributeDefinition(session, AttributesManager.NS_USER_FACILITY_ATTR_VIRT + ""));
        assertEquals(defDefGIDAttr, testAttr);

    }

    @Test
    public void getAttributeValueWhenDefaultUGIDIsSetWithoutPrefferedGID() throws Exception {
        System.out.println("urn_perun_user_facility_attribute_def_virt_defaultUnixGid.getAttributeValueWhenDefaultUGIDIsSetWithoutPrefferedGID");

        defDefGIDAttr.setValue(125);
        when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSessionImpl.class), any(Facility.class), any(User.class), eq(AttributesManager.NS_USER_FACILITY_ATTR_DEF + ":defaultUnixGID"))).thenReturn(defDefGIDAttr);
        when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSessionImpl.class), any(Facility.class), eq(AttributesManager.NS_FACILITY_ATTR_DEF + ":unixGID-namespace")).getValue()).thenReturn(namespace);
        when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSessionImpl.class), any(Resource.class), eq(AttributesManager.NS_RESOURCE_ATTR_DEF + ":unixGID-namespace:" + namespace))).thenReturn(resourceGIDListAttr);
        when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSessionImpl.class), any(User.class), eq(AttributesManager.NS_USER_ATTR_DEF + ":preferredDefaultUnixGIDs-namespace:" + namespace))).thenReturn(new Attribute());

        when(session.getPerunBl().getUsersManagerBl().getAllowedResources(any(PerunSessionImpl.class), any(Facility.class), any(User.class))).thenReturn(resourceList);
        
        when(session.getPerunBl().getGroupsManagerBl().getAssignedGroupsToResource(any(PerunSessionImpl.class), any(Resource.class))).thenReturn(groups);
        when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSessionImpl.class), any(Group.class), eq(AttributesManager.NS_GROUP_ATTR_DEF + ":unixGID-namespace:" + namespace)).getValue()).thenReturn(20);
                
        when(session.getPerunBl().getMembersManagerBl().getMembersByUser(any(PerunSessionImpl.class), any(User.class))).thenReturn(members);
        when(session.getPerunBl().getGroupsManagerBl().getGroupMembers(any(PerunSessionImpl.class), any(Group.class))).thenReturn(members);
        
        Attribute testAttr = classInstance.getAttributeValue(session, facility, user, session.getPerunBl().getAttributesManagerBl().getAttributeDefinition(session, AttributesManager.NS_USER_FACILITY_ATTR_VIRT + ""));
        assertEquals(defDefGIDAttr, testAttr);
    }

    @Test
    public void getAttributeValueWithoutPrefferedGID() throws Exception {
        System.out.println("urn_perun_user_facility_attribute_def_virt_defaultUnixGid.getAttributeValueWithoutPrefferedGID()");

        when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSessionImpl.class), any(Facility.class), any(User.class), eq(AttributesManager.NS_USER_FACILITY_ATTR_DEF + ":defaultUnixGID"))).thenReturn(defDefGIDAttr);
        when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSessionImpl.class), any(Facility.class), eq(AttributesManager.NS_FACILITY_ATTR_DEF + ":unixGID-namespace")).getValue()).thenReturn(namespace);
        when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSessionImpl.class), any(Resource.class), eq(AttributesManager.NS_RESOURCE_ATTR_DEF + ":unixGID-namespace:" + namespace))).thenReturn(resourceGIDListAttr);
        when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSessionImpl.class), any(User.class), eq(AttributesManager.NS_USER_ATTR_DEF + ":preferredDefaultUnixGIDs-namespace:" + namespace))).thenReturn(new Attribute());

        when(session.getPerunBl().getUsersManagerBl().getAllowedResources(any(PerunSessionImpl.class), any(Facility.class), any(User.class))).thenReturn(resourceList);

        when(session.getPerunBl().getGroupsManagerBl().getAssignedGroupsToResource(any(PerunSessionImpl.class), any(Resource.class))).thenReturn(groups);
        when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSessionImpl.class), any(Group.class), eq(AttributesManager.NS_GROUP_ATTR_DEF + ":unixGID-namespace:" + namespace)).getValue()).thenReturn(20);
        
        when(session.getPerunBl().getMembersManagerBl().getMembersByUser(any(PerunSessionImpl.class), any(User.class))).thenReturn(members);
        when(session.getPerunBl().getGroupsManagerBl().getGroupMembers(any(PerunSessionImpl.class), any(Group.class))).thenReturn(members);        
        
        Attribute testAttr = classInstance.getAttributeValue(session, facility, user, session.getPerunBl().getAttributesManagerBl().getAttributeDefinition(session, AttributesManager.NS_USER_FACILITY_ATTR_VIRT + ""));
        assertEquals(resourceGIDListAttr, testAttr);
    }
}