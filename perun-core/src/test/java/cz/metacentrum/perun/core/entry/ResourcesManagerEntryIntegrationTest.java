package cz.metacentrum.perun.core.entry;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.AssignedGroup;
import cz.metacentrum.perun.core.api.AssignedMember;
import cz.metacentrum.perun.core.api.AssignedResource;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.BanOnResource;
import cz.metacentrum.perun.core.api.Candidate;
import cz.metacentrum.perun.core.api.EnrichedBanOnResource;
import cz.metacentrum.perun.core.api.EnrichedGroup;
import cz.metacentrum.perun.core.api.EnrichedResource;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.GroupResourceAssignment;
import cz.metacentrum.perun.core.api.GroupResourceStatus;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.MemberGroupStatus;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.ResourceTag;
import cz.metacentrum.perun.core.api.ResourcesManager;
import cz.metacentrum.perun.core.api.RichResource;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.Status;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.VosManager;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotDefinedOnResourceException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.RelationExistsException;
import cz.metacentrum.perun.core.api.exceptions.ResourceExistsException;
import cz.metacentrum.perun.core.api.exceptions.ResourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ServiceAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotAssignedException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.SubGroupCannotBeRemovedException;
import cz.metacentrum.perun.core.api.exceptions.UserNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.blImpl.AuthzResolverBlImpl;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Integration tests of ResourcesManager.
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class ResourcesManagerEntryIntegrationTest extends AbstractPerunIntegrationTest {

  private static final String CLASS_NAME = "ResourcesManager.";

  private static final String A_R_C_ID = "urn:perun:resource:attribute-def:core:id";

  // these are in DB only when needed and must be setUp"type"() in right order before use !!
  private Vo vo;
  private Member member;
  private Facility facility;
  private Group group;
  private Group subGroup;
  private Resource resource;
  private Service service;
  private ResourcesManager resourcesManager;


  // setUp methods moved to every test method to save testing time !!

  @Test
  public void activateGroupResourceAssignment() throws Exception {
    System.out.println(CLASS_NAME + "activateGroupResourceAssignment");

    vo = setUpVo();
    member = setUpMember(vo);
    group = setUpGroup(vo, member);
    facility = setUpFacility();
    resource = setUpResource();

    resourcesManager.assignGroupToResource(sess, group, resource, false, false, false);
    resourcesManager.deactivateGroupResourceAssignment(sess, group, resource);
    resourcesManager.activateGroupResourceAssignment(sess, group, resource, false);

    List<AssignedGroup> groups = resourcesManager.getGroupAssignments(sess, resource, null);

    assertThat(groups.size()).isEqualTo(1);
    assertThat(groups.get(0).getStatus()).isEqualTo(GroupResourceStatus.ACTIVE);
  }

  @Test
  public void activateGroupResourceAssignmentAsync() throws Exception {
    System.out.println(CLASS_NAME + "activateGroupResourceAssignmentAsync");

    vo = setUpVo();
    member = setUpMember(vo);
    group = setUpGroup(vo, member);
    facility = setUpFacility();
    resource = setUpResource();

    resourcesManager.assignGroupToResource(sess, group, resource, false, true, false);

    List<Group> groups = resourcesManager.getAssignedGroups(sess, resource);
    assertThat(groups).isEmpty();

    resourcesManager.activateGroupResourceAssignment(sess, group, resource, true);

    groups = resourcesManager.getAssignedGroups(sess, resource);
    assertThat(groups).containsExactly(group);
  }

  @Test
  public void activateGroupResourceAssignmentWhenGroupNotExists() throws Exception {
    System.out.println(CLASS_NAME + "activateGroupResourceAssignmentWhenGroupNotExists");

    vo = setUpVo();
    facility = setUpFacility();
    resource = setUpResource();

    assertThatExceptionOfType(GroupNotExistsException.class).isThrownBy(
        () -> resourcesManager.activateGroupResourceAssignment(sess, new Group(), resource, false));
  }

  @Test
  public void activateGroupResourceAssignmentWhenResourceNotExists() throws Exception {
    System.out.println(CLASS_NAME + "activateGroupResourceAssignmentWhenResourceNotExists");

    vo = setUpVo();
    member = setUpMember(vo);
    group = setUpGroup(vo, member);

    assertThatExceptionOfType(ResourceNotExistsException.class).isThrownBy(
        () -> resourcesManager.activateGroupResourceAssignment(sess, group, new Resource(), false));
  }

  @Test
  public void addAdmin() throws Exception {
    System.out.println(CLASS_NAME + "addAdmin");
    vo = setUpVo();
    member = setUpMember(vo);
    facility = setUpFacility();
    resource = setUpResource();
    User u = perun.getUsersManagerBl().getUserByMember(sess, member);

    resourcesManager.addAdmin(sess, resource, u);
    final List<User> admins = resourcesManager.getAdmins(sess, resource, false);

    assertNotNull(admins);
    assertTrue(admins.size() > 0);
  }

  @Test
  public void addAdminWithGroup() throws Exception {
    System.out.println(CLASS_NAME + "addAdminWithGroup");
    vo = setUpVo();
    member = setUpMember(vo);
    group = setUpGroup(vo, member);
    facility = setUpFacility();
    resource = setUpResource();

    resourcesManager.addAdmin(sess, resource, group);
    final List<Group> admins = resourcesManager.getAdminGroups(sess, resource);

    assertNotNull(admins);
    assertTrue(admins.size() > 0);
    assertTrue(admins.contains(group));
  }

  @Test
  public void addResourceSelfServiceGroup() throws Exception {
    System.out.println(CLASS_NAME + "addResourceSelfServiceGroup");

    vo = setUpVo();
    facility = setUpFacility();
    member = setUpMember(vo);
    group = setUpGroup(vo, member);

    Resource resource = setUpResource();

    resourcesManager.addResourceSelfServiceGroup(sess, resource, group);

    List<String> roles = AuthzResolverBlImpl.getGroupRoleNames(sess, group);

    assertTrue(roles.contains("RESOURCESELFSERVICE"));
  }

  @Test
  public void addResourceSelfServiceUser() throws Exception {
    System.out.println(CLASS_NAME + "addResourceSelfServiceGroup");

    vo = setUpVo();
    facility = setUpFacility();
    Resource resource = setUpResource();
    User user = setUpUser("Milos", "Zeman");

    resourcesManager.addResourceSelfServiceUser(sess, resource, user);

    List<String> roles = AuthzResolverBlImpl.getUserRoleNames(sess, user);

    assertTrue(roles.contains("RESOURCESELFSERVICE"));
  }

  @Test
  public void assginGroupToResource() throws Exception {
    System.out.println(CLASS_NAME + "assignGroupToResource");

    vo = setUpVo();
    facility = setUpFacility();
    resource = setUpResource();
    assertNotNull("unable to create resource", resource);
    member = setUpMember(vo);
    group = setUpGroup(vo, member);
    resourcesManager.assignGroupToResource(sess, group, resource, false, false, false);

    List<Group> assignedGroups = resourcesManager.getAssignedGroups(sess, resource);
    assertEquals("one group should be assigned to our Resource", 1, assignedGroups.size());
    assertTrue("our group shoud be assigned to resource", assignedGroups.contains(group));

  }

  public void assginGroupToResourceWhenGroupAlreadyAssigned() throws Exception {
    System.out.println(CLASS_NAME + "assignGroupToResourceWhenGroupAlreadyAssigned");

    vo = setUpVo();
    facility = setUpFacility();
    member = setUpMember(vo);
    group = setUpGroup(vo, member);
    resource = setUpResource();

    resourcesManager.assignGroupToResource(sess, group, resource, false, false, false);
    resourcesManager.assignGroupToResource(sess, group, resource, false, false, false);
    // shouldn't add group twice

  }

  @Test(expected = GroupNotExistsException.class)
  public void assginGroupToResourceWhenGroupNotExists() throws Exception {
    System.out.println(CLASS_NAME + "assignGroupToResourceWhenGroupNotExists");

    vo = setUpVo();
    facility = setUpFacility();
    resource = setUpResource();
    assertNotNull("unable to create resource", resource);
    resourcesManager.assignGroupToResource(sess, new Group(), resource, false, false, false);
    // shouldn't find group

  }

  @Test(expected = ResourceNotExistsException.class)
  public void assginGroupToResourceWhenResourceNotExists() throws Exception {
    System.out.println(CLASS_NAME + "assignGroupToResourceWhenResourceNotExists");

    vo = setUpVo();
    facility = setUpFacility();
    member = setUpMember(vo);
    group = setUpGroup(vo, member);
    resourcesManager.assignGroupToResource(sess, group, new Resource(), false, false, false);
    // shouldn't find resource

  }

  @Test
  public void assignGroupToResourceAsInactive() throws Exception {
    System.out.println(CLASS_NAME + "assignGroupToResourceAsInactive");

    vo = setUpVo();
    facility = setUpFacility();
    resource = setUpResource();
    assertNotNull("unable to create resource", resource);
    member = setUpMember(vo);
    group = setUpGroup(vo, member);
    resourcesManager.assignGroupToResource(sess, group, resource, false, true, false);

    AssignedGroup expectedGroup =
        new AssignedGroup(new EnrichedGroup(group, null), GroupResourceStatus.INACTIVE, null, null, false);

    List<AssignedGroup> assignedGroups = resourcesManager.getGroupAssignments(sess, resource, null);
    assertEquals("one group should be assigned to our Resource", 1, assignedGroups.size());
    assertTrue("our group should be assigned to resource Expected: " + group.getName() + ", Actual: " +
                   assignedGroups.get(0).getEnrichedGroup().getGroup().getName(),
        assignedGroups.contains(expectedGroup));
    assertEquals("our group should be assigned to resource as inactive", assignedGroups.get(0).getStatus(),
        GroupResourceStatus.INACTIVE);
  }

  @Test
  public void assignGroupToResourceWithSubgroup() throws Exception {
    System.out.println(CLASS_NAME + "assignGroupToResourceWithSubgroup");

    vo = setUpVo();
    facility = setUpFacility();
    resource = setUpResource();
    assertNotNull("unable to create resource", resource);
    member = setUpMember(vo);

    group = setUpGroup(vo, member);
    subGroup = setUpSubGroup(group);
    AssignedGroup expectedGroup =
        new AssignedGroup(new EnrichedGroup(group, null), GroupResourceStatus.ACTIVE, null, null, true);
    AssignedGroup expectedSubGroup = new AssignedGroup(new EnrichedGroup(subGroup, null), GroupResourceStatus.ACTIVE,
        expectedGroup.getEnrichedGroup().getGroup().getId(), null, true);

    resourcesManager.assignGroupToResource(sess, group, resource, false, false, true);

    List<AssignedGroup> assignedGroups = resourcesManager.getGroupAssignments(sess, resource, null);
    assertEquals("two groups (group with subgroup) should be assigned to our Resource", 2, assignedGroups.size());

    assertTrue("Our group should be assigned to resource.", assignedGroups.contains(expectedGroup));
    assertTrue("Our subgroup should be assigned to resource.", assignedGroups.contains(expectedSubGroup));
  }

  @Test
  public void assignGroupsToResource() throws Exception {
    System.out.println(CLASS_NAME + "assignGroupsToResource");

    vo = setUpVo();
    facility = setUpFacility();
    resource = setUpResource();
    service = setUpService();
    assertNotNull("unable to create resource", resource);
    member = setUpMember(vo);
    group = setUpGroup(vo, member);

    Group group2 = new Group("ResourcesManagerTestGroup2", "");
    group2 = perun.getGroupsManager().createGroup(sess, vo, group2);
    perun.getGroupsManager().addMember(sess, group2, member);

    List<Group> groups = new ArrayList<>();
    groups.add(group);
    groups.add(group2);
    resourcesManager.assignService(sess, resource, service);
    resourcesManager.assignGroupsToResource(sess, groups, resource, false, false, false);

    List<Group> assignedGroups = resourcesManager.getAssignedGroups(sess, resource);
    assertEquals("all groups should be assigned to our Resource", assignedGroups.size(), groups.size());
    assertTrue("our groups should be assigned to resource", assignedGroups.containsAll(groups));
  }

  @Test
  public void assignInactiveGroupToResourceActivatesItsSubgroups() throws Exception {
    System.out.println(CLASS_NAME + "assignInactiveGroupToResourceActivatesItsSubgroups");

    vo = setUpVo();
    member = setUpMember(vo);
    facility = setUpFacility();
    Resource inactiveResource = setUpResource();

    group = setUpGroup(vo, member);
    subGroup = setUpSubGroup(group);

    sess.getPerun().getResourcesManager().assignGroupToResource(sess, group, inactiveResource, false, true, true);

    List<AssignedGroup> assignedGroups =
        sess.getPerun().getResourcesManager().getGroupAssignments(sess, inactiveResource, List.of());

    AssignedGroup assignedGroup =
        new AssignedGroup(new EnrichedGroup(group, List.of()), GroupResourceStatus.INACTIVE, null, null, true);

    AssignedGroup assignedSubgroup =
        new AssignedGroup(new EnrichedGroup(subGroup, List.of()), GroupResourceStatus.ACTIVE, group.getId(), null,
            true);

    assertThat(assignedGroups).containsExactlyInAnyOrder(assignedGroup, assignedSubgroup);
  }

  @Test
  public void assignMembersGroupToResourceWithSubgroups() throws Exception {
    System.out.println(CLASS_NAME + "assignMembersGroupToResourceWithSubgroups");

    vo = setUpVo();
    facility = setUpFacility();
    resource = setUpResource();
    assertNotNull("unable to create resource", resource);
    member = setUpMember(vo);

    group = setUpGroup(vo, member);
    Group members = sess.getPerun().getGroupsManager().getGroupByName(sess, vo, VosManager.MEMBERS_GROUP);
    AssignedGroup expectedMembersGroupActive =
        new AssignedGroup(new EnrichedGroup(members, null), GroupResourceStatus.ACTIVE, null, null, true);
    AssignedGroup expectedGroupActive = new AssignedGroup(new EnrichedGroup(group, null), GroupResourceStatus.ACTIVE,
        expectedMembersGroupActive.getEnrichedGroup().getGroup().getId(), null, true);

    resourcesManager.assignGroupToResource(sess, members, resource, false, false, true);

    List<AssignedGroup> assignedGroups = resourcesManager.getGroupAssignments(sess, resource, null);
    assertEquals("two groups should be assigned to our Resource('members' and other test group)", 2,
        assignedGroups.size());

    assertTrue("our group should be assigned to resource.", assignedGroups.contains(expectedGroupActive));
    assertTrue("our members group should be assigned to resource", assignedGroups.contains(expectedMembersGroupActive));
  }

  @Test
  public void assignService() throws Exception {
    System.out.println(CLASS_NAME + "assignService");

    vo = setUpVo();
    facility = setUpFacility();
    resource = setUpResource();
    service = setUpService();

    resourcesManager.assignService(sess, resource, service);
    List<Service> services = resourcesManager.getAssignedServices(sess, resource);
    assertTrue("resource should have 1 service", services.size() == 1);
    assertTrue("our service should be assigned to our resource", services.contains(service));

  }

  @Test(expected = ResourceNotExistsException.class)
  public void assignServiceWhenResourceNotExists() throws Exception {
    System.out.println(CLASS_NAME + "assignServiceWhenResourceNotExists");

    service = setUpService();

    resourcesManager.assignService(sess, new Resource(), service);
    // shouldn't find resource

  }

  @Test(expected = ServiceAlreadyAssignedException.class)
  public void assignServiceWhenServiceAlreadyAssigned() throws Exception {
    System.out.println(CLASS_NAME + "assignServiceWhenServiceAlreadyAssigned");

    vo = setUpVo();
    facility = setUpFacility();
    resource = setUpResource();
    service = setUpService();

    resourcesManager.assignService(sess, resource, service);
    resourcesManager.assignService(sess, resource, service);
    // shouldn't add service twice

  }

  @Test(expected = ServiceNotExistsException.class)
  public void assignServiceWhenServiceNotExists() throws Exception {
    System.out.println(CLASS_NAME + "assignServiceWhenServiceNotExists");

    vo = setUpVo();
    facility = setUpFacility();
    resource = setUpResource();

    resourcesManager.assignService(sess, resource, new Service());
    // shouldn't find service

  }

  @Test
  public void autoAssignSubgroupWithInactiveSourceGroup() throws Exception {
    System.out.println(CLASS_NAME + "autoAssignSubgroupWithInactiveSourceGroup");

    vo = setUpVo();
    member = setUpMember(vo);
    group = setUpGroup(vo, member);
    facility = setUpFacility();
    Resource inactiveResource = setUpResource();

    sess.getPerun().getResourcesManager().assignGroupToResource(sess, group, inactiveResource, false, true, true);
    sess.getPerun().getResourcesManager().deactivateGroupResourceAssignment(sess, group, inactiveResource);

    // subgroup gets assigned on creation
    subGroup = setUpSubGroup(group);

    List<AssignedGroup> assignedGroups =
        sess.getPerun().getResourcesManager().getGroupAssignments(sess, inactiveResource, List.of());

    AssignedGroup assignedGroup =
        new AssignedGroup(new EnrichedGroup(group, List.of()), GroupResourceStatus.INACTIVE, null, null, true);
    AssignedGroup assignedSubgroup =
        new AssignedGroup(new EnrichedGroup(subGroup, List.of()), GroupResourceStatus.ACTIVE, group.getId(), null,
            true);

    assertThat(assignedGroups).containsExactlyInAnyOrder(assignedGroup, assignedSubgroup);
  }

  @Test
  public void bulkAssignRemoveResourceTags() throws Exception {
    System.out.println(CLASS_NAME + "bulkAssignRemoveResourceTags");

    vo = setUpVo();
    facility = setUpFacility();
    resource = setUpResource();
    ResourceTag tag1 = setUpResourceTag();
    ResourceTag tag2 = new ResourceTag(1, "ResourceManagerTestResourceTag2", vo.getId());
    tag2 = perun.getResourcesManager().createResourceTag(sess, tag2, vo);

    resourcesManager.assignResourceTagsToResource(sess, List.of(tag1, tag2), resource);
    List<ResourceTag> tags = perun.getResourcesManager().getAllResourcesTagsForResource(sess, resource);

    assertThat(tags).containsExactly(tag1, tag2);

    resourcesManager.removeResourceTagsFromResource(sess, List.of(tag1, tag2), resource);

    tags = perun.getResourcesManager().getAllResourcesTagsForResource(sess, resource);

    assertThat(tags).isEmpty();

  }

  @Test
  public void copyAttributes() throws Exception {
    System.out.println(CLASS_NAME + "copyAttributes");

    vo = setUpVo();
    facility = setUpFacility();
    resource = setUpResource();

    // set up second resource
    Resource newResource = new Resource();
    newResource.setName("SecondResource");
    newResource.setDescription("pro kopirovani");
    Resource secondResource = resourcesManager.createResource(sess, newResource, vo, facility);

    // add first attribute to source
    Attribute firstAttribute = setUpAttribute1();
    perun.getAttributesManager().setAttribute(sess, resource, firstAttribute);

    // add second attribute to both
    Attribute secondAttribute = setUpAttribute2();
    perun.getAttributesManager().setAttribute(sess, resource, secondAttribute);
    perun.getAttributesManager().setAttribute(sess, secondResource, secondAttribute);

    // add third attribute to destination
    Attribute thirdAttribute = setUpAttribute3();
    perun.getAttributesManager().setAttribute(sess, secondResource, thirdAttribute);

    // copy
    resourcesManager.copyAttributes(sess, resource, secondResource);

    // tests
    List<Attribute> destinationAttributes = perun.getAttributesManager().getAttributes(sess, secondResource);
    assertNotNull(destinationAttributes);
    assertTrue((destinationAttributes.size() - perun.getAttributesManager().getAttributes(sess, resource).size()) == 1);
    assertTrue(destinationAttributes.contains(firstAttribute));
    assertTrue(destinationAttributes.contains(secondAttribute));
    assertTrue(destinationAttributes.contains(thirdAttribute));
  }

  @Test
  public void copyGroups() throws Exception {
    System.out.println(CLASS_NAME + "copyGroups");

    vo = setUpVo();
    facility = setUpFacility();
    resource = setUpResource();
    member = setUpMember(vo);
    group = setUpGroup(vo, member);
    resourcesManager.assignGroupToResource(sess, group, resource, false, false, false);

    // set up second resource
    Resource newResource = new Resource();
    newResource.setName("SecondResource");
    newResource.setDescription("pro kopirovani");
    Resource secondResource = resourcesManager.createResource(sess, newResource, vo, facility);

    resourcesManager.copyGroups(sess, resource, secondResource);

    //test
    assertTrue(resourcesManager.getAssignedGroups(sess, secondResource).contains(group));
  }

  @Test(expected = InternalErrorException.class)
  public void copyResourceDifferentVO() throws Exception {
    System.out.println(CLASS_NAME + "copyResourceDifferentVO");
    facility = setUpFacility();
    vo = setUpVo();
    resource = setUpResource();

    Vo diffVo = new Vo(1, "TestVo", "TestingVo");
    diffVo = perun.getVosManagerBl().createVo(sess, diffVo);
    assertNotNull("unable to create testing Vo", diffVo);

    Resource resource1 = new Resource();
    resource1.setName("TestResource");
    resource1.setDescription("TestingResource");
    resource1.setVoId(diffVo.getId());
    resource1.setFacilityId(facility.getId());

    resourcesManager.copyResource(sess, resource, resource1, true);
  }

  @Test
  public void copyResourceSameVO() throws Exception {
    System.out.println(CLASS_NAME + "copyResourceSameVO");
    vo = setUpVo();
    facility = setUpFacility();
    resource = setUpResource();
    resource.setVoId(vo.getId());
    resource.setFacilityId(facility.getId());
    assertNotNull("resource", resource);

    Resource resource1 = new Resource();
    resource1.setName("TestingResource");
    resource1.setDescription("TestingResource");
    resource1.setVoId(vo.getId());
    resource1.setFacilityId(facility.getId());

    resourcesManager.copyResource(sess, resource, resource1, true);

    Resource existingResource = resourcesManager.getResourceByName(sess, vo, facility, resource1.getName());
    assertNotNull("Resource was not created", existingResource);
  }

  @Test
  public void copyResourceWithEverythingFilled() throws Exception {
    System.out.println(CLASS_NAME + "copyResourceWithEverythingFilled");
    vo = setUpVo();
    facility = setUpFacility();
    resource = setUpResource();
    resource.setVoId(vo.getId());
    resource.setFacilityId(facility.getId());
    assertNotNull("resource", resource);

    // Setup of groups,services and tags
    Member member = setUpMember(vo);
    group = setUpGroup(vo, member);
    service = setUpService();
    ResourceTag resTag = setUpResourceTag();
    resourcesManager.assignService(sess, resource, service);
    resourcesManager.assignGroupToResource(sess, group, resource, false, false, false);
    resourcesManager.assignResourceTagToResource(sess, resTag, resource);

    Resource destinationResource = new Resource();
    destinationResource.setName("DestinationResource");
    destinationResource.setDescription("DestinationResource");
    destinationResource.setVoId(vo.getId());
    destinationResource.setFacilityId(facility.getId());

    // Setup of resource-member attribute
    AttributeDefinition resourceMemberAttrDef = new AttributeDefinition();
    resourceMemberAttrDef.setNamespace(AttributesManager.NS_MEMBER_RESOURCE_ATTR_DEF);
    resourceMemberAttrDef.setFriendlyName("memberResourceAttribute");
    resourceMemberAttrDef.setType(Integer.class.getName());
    resourceMemberAttrDef = perun.getAttributesManagerBl().createAttribute(sess, resourceMemberAttrDef);

    Attribute resourceMemberAttr =
        perun.getAttributesManagerBl().getAttribute(sess, member, resource, resourceMemberAttrDef.getName());
    resourceMemberAttr.setValue(1);
    perun.getAttributesManagerBl().setAttribute(sess, member, resource, resourceMemberAttr);

    resourcesManager.copyResource(sess, resource, destinationResource, true);

    Resource createdResource = resourcesManager.getResourceByName(sess, vo, facility, destinationResource.getName());
    assertNotNull("Resource was not created.", createdResource);

    // group, service and resource tags copy check
    assertFalse("Group assigned to original resource not copied to destination resource.",
        resourcesManager.getAssignedGroups(sess, createdResource).isEmpty());
    assertFalse("Service not copied to destination resource.",
        resourcesManager.getAssignedServices(sess, createdResource).isEmpty());
    assertFalse("Resource tag not created for destination resource.",
        resourcesManager.getAllResourcesTagsForResource(sess, createdResource).isEmpty());

    // resource-member attributes check
    List<Attribute> resMembAttrs = perun.getAttributesManagerBl().getAttributes(sess, member, createdResource);
    assertFalse("Created resource does not contain any resource-member attributes.", resMembAttrs.isEmpty());
    assertTrue(
        "Created resource does not contain template resource-member attribute (or copied value of attribute is wrong).",
        resMembAttrs.contains(resourceMemberAttr));
  }

  @Test(expected = ResourceExistsException.class)
  public void copyResourceWithExistingNameInDestinationFacility() throws Exception {
    System.out.println(CLASS_NAME + "copyResourceWithExistingNameInDestinationFacility");

    vo = setUpVo();
    facility = setUpFacility();

    resource = setUpResource();
    Resource resource1 = setUpResource();

    String newResourceName = "TestResource";

    resource1 = resourcesManager.createResource(sess, resource1, vo, facility);
    assertNotNull("unable to create resource1 before copying", resource1);

    resourcesManager.copyResource(sess, resource, resource1, false);
  }

  @Test
  public void copyServices() throws Exception {
    System.out.println(CLASS_NAME + "copyServices");

    vo = setUpVo();
    facility = setUpFacility();
    resource = setUpResource();
    service = setUpService();
    resourcesManager.assignService(sess, resource, service);

    // set up second resource
    Resource newResource = new Resource();
    newResource.setName("SecondResource");
    newResource.setDescription("pro kopirovani");
    Resource secondResource = resourcesManager.createResource(sess, newResource, vo, facility);

    resourcesManager.copyServices(sess, resource, secondResource);

    //test
    assertTrue(resourcesManager.getAssignedServices(sess, secondResource).contains(service));
  }

  // TODO jak otestovat další 2 výjimky na atributy ?

  @Test
  public void createGroupAssignsSubgroup() throws Exception {
    System.out.println(CLASS_NAME + "createGroupAssignsSubgroup");

    vo = setUpVo();
    member = setUpMember(vo);
    group = setUpGroup(vo, member);
    facility = setUpFacility();
    resource = setUpResource();

    sess.getPerun().getResourcesManager().assignGroupToResource(sess, group, resource, false, false, true);

    // subgroup gets assigned upon creation
    subGroup = setUpSubGroup(group);

    AssignedGroup assignedGroup =
        new AssignedGroup(new EnrichedGroup(group, List.of()), GroupResourceStatus.ACTIVE, null, null, true);
    AssignedGroup assignedSubgroup =
        new AssignedGroup(new EnrichedGroup(subGroup, List.of()), GroupResourceStatus.ACTIVE, group.getId(), null,
            true);

    List<AssignedGroup> assignedGroups =
        sess.getPerun().getResourcesManager().getGroupAssignments(sess, resource, List.of());
    assertThat(assignedGroups).containsExactlyInAnyOrder(assignedGroup, assignedSubgroup);
  }

  @Test
  public void createGroupAssignsSubgroupTree() throws Exception {
    System.out.println(CLASS_NAME + "createGroupAssignsSubgroupTree");

    vo = setUpVo();
    member = setUpMember(vo);
    group = setUpGroup(vo, member);
    facility = setUpFacility();
    resource = setUpResource();

    sess.getPerun().getResourcesManager().assignGroupToResource(sess, group, resource, false, false, true);

    // subgroup gets assigned on creation
    subGroup = setUpSubGroup(group);
    Group subGroup2 = setUpSubGroup(subGroup);
    Group subGroup3 = setUpSubGroup(subGroup2);

    AssignedGroup assignedSubgroup21 =
        new AssignedGroup(new EnrichedGroup(subGroup2, List.of()), GroupResourceStatus.ACTIVE, group.getId(), null,
            true);
    AssignedGroup assignedSubgroup22 =
        new AssignedGroup(new EnrichedGroup(subGroup3, List.of()), GroupResourceStatus.ACTIVE, group.getId(), null,
            true);


    List<AssignedGroup> assignedGroups =
        sess.getPerun().getResourcesManager().getGroupAssignments(sess, resource, List.of());
    assertThat(assignedGroups).contains(assignedSubgroup21, assignedSubgroup22);
  }

  @Test
  public void createResource() throws Exception {
    System.out.println(CLASS_NAME + "createResource");

    vo = setUpVo();
    facility = setUpFacility();

    Resource resource = new Resource();
    resource.setName("ResourcesManagerTestResource2");
    resource.setDescription("Testovaci2");
    Resource returnedResource = resourcesManager.createResource(sess, resource, vo, facility);
    assertNotNull("unable to create Resource", returnedResource);
    assertEquals("created and returned resource should be the same", returnedResource, resource);

  }

  @Test
  public void createResourceSetsUUID() throws Exception {
    System.out.println(CLASS_NAME + "createResourceSetsUUID");

    vo = setUpVo();
    facility = setUpFacility();
    Resource resource = new Resource(-1, "test", "", facility.getId(), vo.getId());

    resource = resourcesManager.createResource(sess, resource, vo, facility);

    assertThat(resource.getUuid()).isNotNull();
    assertThat(resource.getUuid().version()).isEqualTo(4);
  }

  @Test(expected = FacilityNotExistsException.class)
  public void createResourceWhenFacilityNotExists() throws Exception {
    System.out.println(CLASS_NAME + "createResourceWhenFacilityNotExists");

    vo = setUpVo();

    Resource resource = new Resource();
    resource.setName("ResourcesManagerTestResource2");
    resource.setDescription("Testovaci2");
    resourcesManager.createResource(sess, resource, vo, new Facility());
    // shouldn't find facility

  }

  @Test(expected = ResourceExistsException.class)
  public void createResourceWithExistingNameInSameFacilitySameVo() throws Exception {
    System.out.println(CLASS_NAME + "createResourceWithExistingNameInSameFacilitySameVo");

    vo = setUpVo();
    facility = setUpFacility();

    Resource resource1 = new Resource();
    resource1.setName("ResourcesManagerTestResource");
    resource1.setDescription("Test Resource one");

    Resource resource2 = new Resource();
    resource2.setName("ResourcesManagerTestResource");
    resource2.setDescription("Test Resource two");

    resourcesManager.createResource(sess, resource1, vo, facility);
    resourcesManager.createResource(sess, resource2, vo, facility);
  }

  @Test
  public void deactivateGroupResourceAssignment() throws Exception {
    System.out.println(CLASS_NAME + "deactivateGroupResourceAssignment");

    vo = setUpVo();
    member = setUpMember(vo);
    group = setUpGroup(vo, member);
    facility = setUpFacility();
    resource = setUpResource();

    resourcesManager.assignGroupToResource(sess, group, resource, false, false, false);
    resourcesManager.deactivateGroupResourceAssignment(sess, group, resource);

    List<AssignedGroup> groups = resourcesManager.getGroupAssignments(sess, resource, null);

    assertThat(groups.size()).isEqualTo(1);
    assertThat(groups.get(0).getStatus()).isEqualTo(GroupResourceStatus.INACTIVE);
  }

  @Test
  public void deactivateGroupResourceAssignmentWhenGroupNotExists() throws Exception {
    System.out.println(CLASS_NAME + "deactivateGroupResourceAssignmentWhenGroupNotExists");

    vo = setUpVo();
    facility = setUpFacility();
    resource = setUpResource();

    assertThatExceptionOfType(GroupNotExistsException.class).isThrownBy(
        () -> resourcesManager.deactivateGroupResourceAssignment(sess, new Group(), resource));
  }

  @Test
  public void deactivateGroupResourceAssignmentWhenResourceNotExists() throws Exception {
    System.out.println(CLASS_NAME + "deactivateGroupResourceAssignmentWhenResourceNotExists");

    vo = setUpVo();
    member = setUpMember(vo);
    group = setUpGroup(vo, member);

    assertThatExceptionOfType(ResourceNotExistsException.class).isThrownBy(
        () -> resourcesManager.deactivateGroupResourceAssignment(sess, group, new Resource()));
  }

  @Test
  public void deleteAllResources() throws Exception {
    System.out.println(CLASS_NAME + "deleteAllResources");

    vo = setUpVo();
    facility = setUpFacility();
    resource = setUpResource();
    assertNotNull("unable to create resource before deletion", resource);

    resourcesManager.deleteAllResources(sess, vo);

    List<Resource> resources = resourcesManager.getResources(sess, vo);
    assertTrue("resources not deleted", resources.isEmpty());

  }

  @Test(expected = VoNotExistsException.class)
  public void deleteAllResourcesWhenVoNotExists() throws Exception {
    System.out.println(CLASS_NAME + "deleteAllResourcesWhenVoNotExists");

    resourcesManager.deleteAllResources(sess, new Vo());

  }

  @Test
  public void deleteResource() throws Exception {
    System.out.println(CLASS_NAME + "deleteResource");

    vo = setUpVo();
    facility = setUpFacility();
    resource = setUpResource();
    assertNotNull("unable to create resource before deletion", resource);

    resourcesManager.deleteResource(sess, resource);

    List<Resource> resources = resourcesManager.getResources(sess, vo);
    assertTrue("resource not deleted", resources.isEmpty());

  }

  @Ignore //Resource can be deleted with assigned group
  @Test(expected = RelationExistsException.class)
  public void deleteResourceWhenRelationExists() throws Exception {
    System.out.println(CLASS_NAME + "deleteResourceWhenRelationExists");

    vo = setUpVo();
    facility = setUpFacility();
    resource = setUpResource();
    assertNotNull("unable to create resource before deletion", resource);
    member = setUpMember(vo);
    group = setUpGroup(vo, member);
    resourcesManager.assignGroupToResource(sess, group, resource, false, false, false);

    resourcesManager.deleteResource(sess, resource);
    // shouldn't delete resource with assigned group

  }

  @Test
  public void deleteResourceWithGroupResourceAttributes() throws Exception {
    System.out.println(CLASS_NAME + "deleteAllResourcesWhenVoNotExists");

    vo = setUpVo();
    facility = setUpFacility();
    resource = setUpResource();
    member = setUpMember(vo);
    group = setUpGroup(vo, member);

    perun.getResourcesManagerBl().assignGroupToResource(sess, group, resource, false, false, false);
    List<Attribute> attributes = setUpGroupResourceAttribute(group, resource);

    List<Attribute> retAttributes = perun.getAttributesManagerBl().getAttributes(sess, resource, group, false);
    assertEquals("Only one group resource attribute is set.", retAttributes.size(), 1);
    assertEquals("Not the correct attribute returned", attributes.get(0), retAttributes.get(0));

    perun.getResourcesManagerBl().deleteResource(sess, resource);
    retAttributes = perun.getAttributesManagerBl().getAttributes(sess, resource, group, false);
    assertEquals("There is still group resource attribute after deleting resource", retAttributes.size(), 0);
  }

  @Test
  public void deleteResourceWithSubgroupsAssigned() throws Exception {
    System.out.println(CLASS_NAME + "deleteResourceWithSubgroupsAssigned");

    vo = setUpVo();
    facility = setUpFacility();
    resource = setUpResource();
    member = setUpMember(vo);
    group = setUpGroup(vo, member);
    setUpSubGroup(group);

    perun.getResourcesManagerBl().assignGroupToResource(sess, group, resource, false, false, true);

    perun.getResourcesManagerBl().deleteResource(sess, resource);

    List<Resource> resources = resourcesManager.getResources(sess, vo);
    assertTrue("resource not deleted", resources.isEmpty());
  }

  @Test
  public void getAdminGroups() throws Exception {
    System.out.println(CLASS_NAME + "getAdminGroups");
    vo = setUpVo();
    member = setUpMember(vo);
    group = setUpGroup(vo, member);
    facility = setUpFacility();
    resource = setUpResource();

    resourcesManager.addAdmin(sess, resource, group);

    assertTrue(resourcesManager.getAdminGroups(sess, resource).contains(group));
  }

  @Test
  public void getAdmins() throws Exception {
    System.out.println(CLASS_NAME + "getAdmins");
    vo = setUpVo();
    facility = setUpFacility();
    resource = setUpResource();

    // Set up resource admin
    member = setUpMember(vo);
    User user = perun.getUsersManagerBl().getUserByMember(sess, member);
    resourcesManager.addAdmin(sess, resource, user);

    // Set up resource admin group
    group = setUpGroup(vo, member);
    resourcesManager.addAdmin(sess, resource, group);

    // Set up second resource admin
    Candidate candidate = new Candidate();
    candidate.setFirstName("Josef");
    candidate.setId(4);
    candidate.setMiddleName("");
    candidate.setLastName("Novak");
    candidate.setTitleBefore("");
    candidate.setTitleAfter("");
    UserExtSource userExtSource =
        new UserExtSource(new ExtSource(0, "testExtSource", "cz.metacentrum.perun.core.impl.ExtSourceInternal"),
            Long.toHexString(Double.doubleToLongBits(Math.random())));
    candidate.setUserExtSource(userExtSource);
    candidate.setAttributes(new HashMap<>());

    Member member2 = perun.getMembersManagerBl().createMemberSync(sess, vo, candidate);
    User user2 = perun.getUsersManagerBl().getUserByMember(sess, member2);
    perun.getGroupsManager().addMember(sess, group, member2);

    // Test all admins
    List<User> admins = resourcesManager.getAdmins(sess, resource, false);
    assertTrue("list shoud have 2 admins", admins.size() == 2);
    assertTrue("our member as direct user should be admin", admins.contains(user));
    assertTrue("our member as member of admin group should be admin", admins.contains(user2));

    // Test only direct admins (without groups of admins)
    admins = resourcesManager.getAdmins(sess, resource, true);
    assertTrue("list should have only 1 admin", admins.size() == 1);
    assertTrue("our member as direct user should be in list of admins", admins.contains(user));
    assertTrue("our member as member of admin group shouldn't be in list of admins", !admins.contains(user2));
  }

  @Test
  public void getAdminsIfNotExist() throws Exception {
    System.out.println(CLASS_NAME + "getAdminsIfNotExist");
    vo = setUpVo();
    facility = setUpFacility();
    resource = setUpResource();

    assertTrue(resourcesManager.getAdmins(sess, resource, false).isEmpty());
  }

  @Test
  public void getAllResources() throws Exception {
    System.out.println(CLASS_NAME + "getAllResources");

    vo = setUpVo();
    facility = setUpFacility();
    resource = setUpResource();
    Resource resource2 = setUpResource2();


    List<Resource> resources = resourcesManager.getAllResources(sess);

    assertThat(resources).contains(resource, resource2);
    assertEquals(2, resources.size());
  }

  @Test
  public void getAllResourcesByResourceTag() throws Exception {
    System.out.println(CLASS_NAME + "getAllResourcesByResourceTag");

    vo = setUpVo();
    facility = setUpFacility();
    resource = setUpResource();
    ResourceTag tag = setUpResourceTag();

    resourcesManager.assignResourceTagToResource(sess, tag, resource);
    List<Resource> resources = perun.getResourcesManager().getAllResourcesByResourceTag(sess, tag);
    assertTrue("Resource with tag is not returned by same tag", resources.contains(resource));

  }

  @Test
  public void getAllResourcesTagsForResource() throws Exception {
    System.out.println(CLASS_NAME + "getAllResourcesTagsForResource");

    vo = setUpVo();
    facility = setUpFacility();
    resource = setUpResource();
    ResourceTag tag = setUpResourceTag();

    resourcesManager.assignResourceTagToResource(sess, tag, resource);
    List<ResourceTag> tags = perun.getResourcesManager().getAllResourcesTagsForResource(sess, resource);
    assertTrue("Created tag is not returned from resource", tags.contains(tag));

  }

  @Test
  public void getAllResourcesTagsForVo() throws Exception {
    System.out.println(CLASS_NAME + "getAllResourcesTagsForVo");

    vo = setUpVo();
    ResourceTag tag = setUpResourceTag();
    List<ResourceTag> tags = perun.getResourcesManager().getAllResourcesTagsForVo(sess, vo);
    assertTrue("Created tag is not returned from VO", tags.contains(tag));

  }

  @Test
  public void getAllowedMembers() throws Exception {
    System.out.println(CLASS_NAME + "getAllowedMembers");

    vo = setUpVo();
    facility = setUpFacility();
    resource = setUpResource();
    assertNotNull("unable to create resource", resource);
    member = setUpMember(vo);
    group = setUpGroup(vo, member);
    resourcesManager.assignGroupToResource(sess, group, resource, false, false, false);

    List<Member> members = resourcesManager.getAllowedMembers(sess, resource);
    assertEquals("our resource should have 1 allowed member", 1, members.size());
    assertTrue("our member should be between allowed on resource", members.contains(member));

  }

  @Test(expected = ResourceNotExistsException.class)
  public void getAllowedMembersWhenResourceNotExists() throws Exception {
    System.out.println(CLASS_NAME + "getAllowedMembersResourceNotExists");

    resourcesManager.getAllowedMembers(sess, new Resource());
    // shouldn't find resource

  }

  @Test
  public void getAllowedUsers() throws Exception {
    System.out.println(CLASS_NAME + "getAllowedUsers");

    vo = setUpVo();
    facility = setUpFacility();
    resource = setUpResource();
    assertNotNull("unable to create resource", resource);
    member = setUpMember(vo);
    User user = perun.getUsersManagerBl().getUserByMember(sess, member);
    group = setUpGroup(vo, member);
    resourcesManager.assignGroupToResource(sess, group, resource, false, false, false);

    List<User> users = resourcesManager.getAllowedUsers(sess, resource);
    assertTrue("our resource should have 1 allowed user", users.size() == 1);
    assertTrue("our user should be between allowed on resource", users.contains(user));

  }

  @Test
  public void getAssignedGroups() throws Exception {
    System.out.println(CLASS_NAME + "getAssignedGroups");

    vo = setUpVo();
    member = setUpMember(vo);
    group = setUpGroup(vo, member);
    facility = setUpFacility();
    resource = setUpResource();

    resourcesManager.assignGroupToResource(sess, group, resource, false, false, false);

    List<Group> groups = resourcesManager.getAssignedGroups(sess, resource);
    assertTrue("only one group should be assigned", groups.size() == 1);
    assertTrue("our group should be assigned", groups.contains(group));

  }

  @Test
  public void getAssignedGroupsResourceMember() throws Exception {
    System.out.println(CLASS_NAME + "getAssignedGroupsResourceMember");

    vo = setUpVo();
    member = setUpMember(vo);
    group = setUpGroup(vo, member);
    facility = setUpFacility();
    resource = setUpResource();

    resourcesManager.assignGroupToResource(sess, group, resource, false, false, false);

    List<Group> groups = resourcesManager.getAssignedGroups(sess, resource, member);
    assertTrue("only one group should be assigned", groups.size() == 1);
    assertTrue("our group should be assigned", groups.contains(group));

  }

  @Test(expected = ResourceNotExistsException.class)
  public void getAssignedGroupsWhenResourceNotExists() throws Exception {
    System.out.println(CLASS_NAME + "getAssignedGroupsWhenResourceNotExists");

    resourcesManager.getAssignedGroups(sess, new Resource());
    // shouldn't find resource
  }

  @Test
  public void getAssignedMembers() throws Exception {
    System.out.println(CLASS_NAME + "getAssignedMembers");

    vo = setUpVo();
    member = setUpMember(vo);
    group = setUpGroup(vo, member);
    facility = setUpFacility();
    resource = setUpResource();
    Resource sndResource = setUpResource2();

    // both the resources assign to the group
    resourcesManager.assignGroupToResource(sess, group, resource, false, false, false);
    resourcesManager.assignGroupToResource(sess, group, sndResource, false, false, false);

    List<Member> members = resourcesManager.getAssignedMembers(sess, resource);
    assertTrue(members.size() == 1);
    assertTrue(members.contains(member));

    members = resourcesManager.getAssignedMembers(sess, sndResource);
    assertTrue(members.size() == 1);
    assertTrue(members.contains(member));

    // disabling member shouldn't have any effect
    perun.getMembersManagerBl().disableMember(sess, member);

    members = resourcesManager.getAssignedMembers(sess, resource);
    assertTrue(members.size() == 1);
    assertTrue(members.contains(member));

    members = resourcesManager.getAssignedMembers(sess, sndResource);
    assertTrue(members.size() == 1);
    assertTrue(members.contains(member));

    // removing group should have effect
    resourcesManager.removeGroupFromResource(sess, group, sndResource);

    members = resourcesManager.getAssignedMembers(sess, resource);
    assertTrue(members.size() == 1);
    assertTrue(members.contains(member));

    members = resourcesManager.getAssignedMembers(sess, sndResource);
    assertTrue(members.isEmpty());

  }

  @Test
  public void getAssignedMembersWithStatus() throws Exception {
    System.out.println(CLASS_NAME + "getAssignedMembersWithStatus");

    vo = setUpVo();
    facility = setUpFacility();
    resource = setUpResource();

    Group group1 = perun.getGroupsManager().createGroup(sess, vo, new Group("group1", "group1")); // active
    Group group2 = perun.getGroupsManager().createGroup(sess, vo, new Group("group2", "group2")); // inactive
    Group group3 = perun.getGroupsManager().createGroup(sess, vo, new Group("group3", "group3")); // not assigned

    Member member1 = setUpMember(vo);
    Member member2 = setUpMember(vo);
    Member notAssignedMember = setUpMember(vo);

    perun.getGroupsManager().addMember(sess, group1, member1);
    perun.getGroupsManager().addMember(sess, group2, member1);
    perun.getGroupsManager().addMember(sess, group3, member1);
    perun.getGroupsManager().addMember(sess, group2, member2);
    perun.getGroupsManager().addMember(sess, group3, notAssignedMember);

    perun.getResourcesManagerBl().assignGroupToResource(sess, group1, resource, false, false, false);
    perun.getResourcesManagerBl().assignGroupToResource(sess, group2, resource, false, false, false);
    perun.getResourcesManagerBl().deactivateGroupResourceAssignment(sess, group2, resource);

    List<AssignedMember> assignedMembers = perun.getResourcesManagerBl().getAssignedMembersWithStatus(sess, resource);
    List<Member> members = assignedMembers.stream().map(AssignedMember::getRichMember).collect(toList());

    // contains member1 and member2
    assertTrue(members.size() == 2);
    assertTrue(members.containsAll(List.of(member1, member2)));
    assertFalse(members.contains(notAssignedMember));

    AssignedMember assignedMem1 =
        assignedMembers.stream().filter(m -> m.getRichMember().equals(member1)).findAny().get();
    AssignedMember assignedMem2 =
        assignedMembers.stream().filter(m -> m.getRichMember().equals(member2)).findAny().get();

    // statuses are correctly prioritized
    assertTrue(assignedMem1.getStatus().equals(GroupResourceStatus.ACTIVE));
    assertTrue(assignedMem2.getStatus().equals(GroupResourceStatus.INACTIVE));
  }

  @Test
  public void getAssignedResources() throws Exception {
    System.out.println(CLASS_NAME + "getAssignedResources");

    vo = setUpVo();
    member = setUpMember(vo);
    group = setUpGroup(vo, member);
    facility = setUpFacility();
    resource = setUpResource();

    resourcesManager.assignGroupToResource(sess, group, resource, false, false, false);

    List<Resource> resources = resourcesManager.getAssignedResources(sess, group);
    assertTrue("group should have be on 1 resource", resources.size() == 1);
    assertTrue("our resource should be on our group", resources.contains(resource));

  }

  @Test
  public void getAssignedResourcesForMember() throws Exception {
    System.out.println(CLASS_NAME + "getAssignedResourcesForMember");

    vo = setUpVo();
    member = setUpMember(vo);
    group = setUpGroup(vo, member);
    facility = setUpFacility();
    resource = setUpResource();
    Resource sndResource = setUpResource2();

    // both the resources assign to the group
    resourcesManager.assignGroupToResource(sess, group, resource, false, false, false);
    resourcesManager.assignGroupToResource(sess, group, sndResource, false, false, false);

    List<Resource> resources = resourcesManager.getAssignedResources(sess, member);
    assertTrue("member should be assigned to 2 resources", resources.size() == 2);
    assertTrue("assigned resources should be in returned list",
        resources.containsAll(Arrays.asList(resource, sndResource)));

    // disabling member shouldn't have any effect
    perun.getMembersManagerBl().disableMember(sess, member);

    resources = resourcesManager.getAssignedResources(sess, member);
    assertTrue("member should be assigned to 2 resources", resources.size() == 2);
    assertTrue("assigned resources should be in returned list",
        resources.containsAll(Arrays.asList(resource, sndResource)));

    // removing group should have effect
    resourcesManager.removeGroupFromResource(sess, group, sndResource);

    resources = resourcesManager.getAssignedResources(sess, member);
    assertTrue("member should be assigned to single resources", resources.size() == 1);
    assertTrue("assigned resource should be in returned list", resources.contains(resource));

  }

  @Test
  public void getAssignedResourcesForMemberAndService() throws Exception {
    System.out.println(CLASS_NAME + "getAssignedResourcesForMemberAndService");

    vo = setUpVo();
    member = setUpMember(vo);
    group = setUpGroup(vo, member);
    facility = setUpFacility();
    resource = setUpResource();
    Resource sndResource = setUpResource2();
    service = setUpService();

    // both the resources assign to the group
    resourcesManager.assignGroupToResource(sess, group, resource, false, false, false);
    resourcesManager.assignGroupToResource(sess, group, sndResource, false, false, false);
    // but only one of them assign to the service
    resourcesManager.assignService(sess, resource, service);

    List<Resource> resources = resourcesManager.getAssignedResources(sess, member, service);
    assertTrue("there should have been only 1 assigned resource", resources.size() == 1);
    assertTrue("our resource should be in our resource list", resources.contains(resource));
  }

  @Test
  public void getAssignedResourcesForMemberWithStatus() throws Exception {
    System.out.println(CLASS_NAME + "getAssignedResourcesForMemberWithStatus");

    vo = setUpVo();
    member = setUpMember(vo);
    group = setUpGroup(vo, member);
    facility = setUpFacility();
    resource = setUpResource();
    Resource resource2 = setUpResource2();

    // both the resources assign to the group
    resourcesManager.assignGroupToResource(sess, group, resource, false, false, false);
    resourcesManager.assignGroupToResource(sess, group, resource2, false, false, false);

    AssignedResource resource1Active =
        new AssignedResource(new EnrichedResource(resource, null), GroupResourceStatus.ACTIVE, null, null, facility,
            false);
    AssignedResource resource2Active =
        new AssignedResource(new EnrichedResource(resource2, null), GroupResourceStatus.ACTIVE, null, null, facility,
            false);
    AssignedResource resource2Inactive =
        new AssignedResource(new EnrichedResource(resource2, null), GroupResourceStatus.INACTIVE, null, null, facility,
            false);

    List<AssignedResource> resources = resourcesManager.getAssignedResourcesWithStatus(sess, member);
    assertEquals("member should be assigned to 2 resources", 2, resources.size());
    assertTrue("assigned resources should be in returned list",
        resources.containsAll(List.of(resource1Active, resource2Active)));

    // deactivating group on resource should make the status inactive
    resourcesManager.deactivateGroupResourceAssignment(sess, group, resource2);

    resources = resourcesManager.getAssignedResourcesWithStatus(sess, member);
    assertEquals("member should be assigned to 2 resources", 2, resources.size());
    assertTrue("assigned resources should be in returned list",
        resources.containsAll(List.of(resource1Active, resource2Inactive)));

    // removing group should remove the resource
    resourcesManager.removeGroupFromResource(sess, group, resource2);

    resources = resourcesManager.getAssignedResourcesWithStatus(sess, member);
    assertEquals("member should be assigned to a single resources", 1, resources.size());
    assertTrue("assigned resource should be in returned list", resources.contains(resource1Active));
  }

  @Test
  public void getAssignedResourcesForMemberWithStatus_twoGroupsToOneResource() throws Exception {
    System.out.println(CLASS_NAME + "getAssignedResourcesForMemberWithStatus_twoGroupsToOneResource");

    vo = setUpVo();
    member = setUpMember(vo);
    group = setUpGroup(vo, member);
    Group group2 = new Group("ResourcesManagerTestGroup2", "");
    group2 = perun.getGroupsManager().createGroup(sess, vo, group2);
    perun.getGroupsManager().addMember(sess, group2, member);
    facility = setUpFacility();
    resource = setUpResource();

    // both the resources assign to the group
    resourcesManager.assignGroupToResource(sess, group2, resource, false, false, false);
    resourcesManager.deactivateGroupResourceAssignment(sess, group2, resource);
    resourcesManager.assignGroupToResource(sess, group, resource, false, false, false);

    AssignedResource resourceActive =
        new AssignedResource(new EnrichedResource(resource, null), GroupResourceStatus.ACTIVE, null, null, facility,
            false);
    AssignedResource resourceInactive =
        new AssignedResource(new EnrichedResource(resource, null), GroupResourceStatus.INACTIVE, null, null, facility,
            false);

    List<AssignedResource> resources = resourcesManager.getAssignedResourcesWithStatus(sess, member);
    assertEquals("member should be assigned to a single resources", 1, resources.size());
    assertTrue("assigned resources should be in returned list", resources.contains(resourceActive));

    // removing group should change the status
    resourcesManager.removeGroupFromResource(sess, group, resource);

    resources = resourcesManager.getAssignedResourcesWithStatus(sess, member);
    assertEquals("member should be assigned to a single resources", 1, resources.size());
    assertTrue("assigned resource should be in returned list", resources.contains(resourceInactive));
  }

  @Test(expected = GroupNotExistsException.class)
  public void getAssignedResourcesWhenGroupNotExists() throws Exception {
    System.out.println(CLASS_NAME + "getAssignedResourcesWhenGroupNotExists");

    resourcesManager.getAssignedResources(sess, new Group());
    // shouldn't find group

  }

  @Test
  public void getAssignedRichResources() throws Exception {
    System.out.println(CLASS_NAME + "getAssignedRichResources");

    vo = setUpVo();
    member = setUpMember(vo);
    group = setUpGroup(vo, member);
    facility = setUpFacility();
    resource = setUpResource();
    RichResource rr = new RichResource(resource);
    rr.setFacility(perun.getResourcesManager().getFacility(sess, resource));

    resourcesManager.assignGroupToResource(sess, group, resource, false, false, false);

    List<RichResource> resources = resourcesManager.getAssignedRichResources(sess, group);
    assertTrue("group should have be on 1 rich resource", resources.size() == 1);
    assertTrue("our rich resource should be on our group", resources.contains(rr));
    for (RichResource rich : resources) {
      assertTrue("facility property must be filled!", rich.getFacility() != null);
    }

  }

  @Test
  public void getAssignedRichResourcesForMember() throws Exception {
    System.out.println(CLASS_NAME + "getAssignedRichResourcesForMember");

    vo = setUpVo();
    member = setUpMember(vo);
    group = setUpGroup(vo, member);
    facility = setUpFacility();
    resource = setUpResource();
    RichResource richResource = new RichResource(resource);
    richResource.setFacility(facility);
    richResource.setVo(vo);
    Resource sndResource = setUpResource2();

    resourcesManager.assignGroupToResource(sess, group, resource, false, false, false);
    resourcesManager.assignGroupToResource(sess, group, sndResource, false, false, false);

    RichResource rr = perun.getResourcesManagerBl().getRichResourceById(sess, resource.getId());
    RichResource rr2 = perun.getResourcesManagerBl().getRichResourceById(sess, sndResource.getId());

    List<RichResource> resources = resourcesManager.getAssignedRichResources(sess, member);
    assertTrue("member should be assigned to 2 resources", resources.size() == 2);
    assertTrue("assigned resources should be in returned list", resources.containsAll(Arrays.asList(rr, rr2)));

    // disabling member shouldn't have any effect
    perun.getMembersManagerBl().disableMember(sess, member);

    resources = resourcesManager.getAssignedRichResources(sess, member);
    assertTrue("member should be assigned to 2 resources", resources.size() == 2);
    assertTrue("assigned resources should be in returned list", resources.containsAll(Arrays.asList(rr, rr2)));

    // removing group should have effect
    resourcesManager.removeGroupFromResource(sess, group, sndResource);

    resources = resourcesManager.getAssignedRichResources(sess, member);
    assertTrue("member should be assigned to single resources", resources.size() == 1);
    assertTrue("assigned resource should be in returned list", resources.contains(rr));

  }

  @Test
  public void getAssignedRichResourcesForMemberAndService() throws Exception {
    System.out.println(CLASS_NAME + "getAssignedRichResourcesForMemberAndService");

    vo = setUpVo();
    member = setUpMember(vo);
    group = setUpGroup(vo, member);
    facility = setUpFacility();
    resource = setUpResource();
    RichResource richResource = new RichResource(resource);
    richResource.setFacility(facility);
    richResource.setVo(vo);
    Resource sndResource = setUpResource2();
    service = setUpService();

    // both the resources assign to the group
    resourcesManager.assignGroupToResource(sess, group, resource, false, false, false);
    resourcesManager.assignGroupToResource(sess, group, sndResource, false, false, false);
    // but only one of them assign to the service
    resourcesManager.assignService(sess, resource, service);

    List<RichResource> resources = resourcesManager.getAssignedRichResources(sess, member, service);
    assertTrue("there should have been only 1 assigned rich resource", resources.size() == 1);
    assertTrue("our rich resource should be in our resource list", resources.contains(richResource));

  }

  @Test(expected = GroupNotExistsException.class)
  public void getAssignedRichResourcesWhenGroupNotExists() throws Exception {
    System.out.println(CLASS_NAME + "getAssignedRichResourcesWhenGroupNotExists");

    resourcesManager.getAssignedRichResources(sess, new Group());
    // shouldn't find group

  }

  @Test
  public void getAssignedServices() throws Exception {
    System.out.println(CLASS_NAME + "getAssignedServices");

    vo = setUpVo();
    facility = setUpFacility();
    resource = setUpResource();
    service = setUpService();

    resourcesManager.assignService(sess, resource, service);
    List<Service> services = resourcesManager.getAssignedServices(sess, resource);
    assertTrue("resource should have 1 service", services.size() == 1);
    assertTrue("our service should be assigned to our resource", services.contains(service));

  }

  @Test(expected = ResourceNotExistsException.class)
  public void getAssignedServicesWhenResourceNotExists() throws Exception {
    System.out.println(CLASS_NAME + "getAssignedServicesWhenResourceNotExists");

    resourcesManager.getAssignedServices(sess, new Resource());
    // shouldn't find resource

  }

  @Test
  public void getAssociatedMembers() throws Exception {
    System.out.println(CLASS_NAME + "getAssociatedMembers");

    vo = setUpVo();
    facility = setUpFacility();
    resource = setUpResource();

    Member member1 = setUpMember(vo);
    Member member2 = setUpMember(vo);
    Member member3 = setUpMember(vo);

    Group group1 = setUpGroup(vo, member1);
    Group group2 = perun.getGroupsManager().createGroup(sess, vo, new Group("Test2", "Test2"));
    perun.getGroupsManagerBl().addMember(sess, group2, member2);
    Group group3 = perun.getGroupsManager().createGroup(sess, vo, new Group("Test3", "Test3"));
    perun.getGroupsManagerBl().addMember(sess, group3, member3);

    perun.getResourcesManagerBl().assignGroupToResource(sess, group1, resource, false, false, false);
    perun.getResourcesManagerBl().assignGroupToResource(sess, group2, resource, false, true, false);

    List<Member> result = perun.getResourcesManagerBl().getAssociatedMembers(sess, resource);
    assertThat(result).containsExactlyInAnyOrderElementsOf(List.of(member1, member2));
    assertThat(result).doesNotContain(member3);
  }

  @Test
  public void getAssociatedResources() throws Exception {
    System.out.println(CLASS_NAME + "getAssociatedResources");

    vo = setUpVo();
    member = setUpMember(vo);
    group = setUpGroup(vo, member);
    facility = setUpFacility();
    resource = setUpResource();

    resourcesManager.assignGroupToResource(sess, group, resource, false, true, false);

    List<Resource> resources = perun.getResourcesManagerBl().getAssociatedResources(sess, group);
    assertTrue("group should be associated with 1 resource", resources.size() == 1);
    assertTrue("our resource should be associated with our group", resources.contains(resource));

  }

  @Test
  public void getAssociatedResourcesForMember() throws Exception {
    System.out.println(CLASS_NAME + "getAssociatedResourcesForMember");

    vo = setUpVo();
    member = setUpMember(vo);
    Member member2 = setUpMember(vo);

    group = setUpGroup(vo, member);
    facility = setUpFacility();
    resource = setUpResource();

    resourcesManager.assignGroupToResource(sess, group, resource, false, true, false);

    List<Resource> resources = perun.getResourcesManagerBl().getAssociatedResources(sess, member);
    assertThat(resources).containsExactly(resource);

    resources = perun.getResourcesManagerBl().getAssociatedResources(sess, member2);
    assertThat(resources).doesNotContain(resource);

  }

  @Test
  public void getAssociatedUsers() throws Exception {
    System.out.println(CLASS_NAME + "getAssociatedUsers");

    vo = setUpVo();
    facility = setUpFacility();
    resource = setUpResource();

    Member member1 = setUpMember(vo);
    Member member2 = setUpMember(vo);
    Member member3 = setUpMember(vo);

    User user1 = perun.getUsersManagerBl().getUserByMember(sess, member1);
    User user2 = perun.getUsersManagerBl().getUserByMember(sess, member2);
    User user3 = perun.getUsersManagerBl().getUserByMember(sess, member3);

    Group group1 = setUpGroup(vo, member1);
    Group group2 = perun.getGroupsManager().createGroup(sess, vo, new Group("Test2", "Test2"));
    perun.getGroupsManagerBl().addMember(sess, group2, member2);
    Group group3 = perun.getGroupsManager().createGroup(sess, vo, new Group("Test3", "Test3"));
    perun.getGroupsManagerBl().addMember(sess, group3, member3);

    perun.getResourcesManagerBl().assignGroupToResource(sess, group1, resource, false, false, false);
    perun.getResourcesManagerBl().assignGroupToResource(sess, group2, resource, false, true, false);

    List<User> result = perun.getResourcesManagerBl().getAssociatedUsers(sess, resource);
    assertThat(result).containsExactlyInAnyOrderElementsOf(List.of(user1, user2));
    assertThat(result).doesNotContain(user3);
  }

  @Test
  public void getBan() throws Exception {
    System.out.println(CLASS_NAME + "getBan");
    vo = setUpVo();
    facility = setUpFacility();
    resource = setUpResource();
    member = setUpMember(vo);
    group = setUpGroup(vo, member);
    perun.getResourcesManagerBl().assignGroupToResource(sess, group, resource, false, false, false);

    BanOnResource banOnResource = setUpBan(new Date());
    banOnResource = resourcesManager.setBan(sess, banOnResource);

    BanOnResource returnedBan =
        resourcesManager.getBan(sess, banOnResource.getMemberId(), banOnResource.getResourceId());
    assertEquals(banOnResource, returnedBan);
  }

  @Test
  public void getBanById() throws Exception {
    System.out.println(CLASS_NAME + "getBanById");
    vo = setUpVo();
    facility = setUpFacility();
    resource = setUpResource();
    member = setUpMember(vo);
    group = setUpGroup(vo, member);
    perun.getResourcesManagerBl().assignGroupToResource(sess, group, resource, false, false, false);

    BanOnResource banOnResource = setUpBan(new Date());
    banOnResource = resourcesManager.setBan(sess, banOnResource);

    BanOnResource returnedBan = resourcesManager.getBanById(sess, banOnResource.getId());
    assertEquals(banOnResource, returnedBan);
  }

  @Test
  public void getBansForMember() throws Exception {
    System.out.println(CLASS_NAME + "getBansForMember");
    vo = setUpVo();
    facility = setUpFacility();
    resource = setUpResource();
    member = setUpMember(vo);
    group = setUpGroup(vo, member);
    perun.getResourcesManagerBl().assignGroupToResource(sess, group, resource, false, false, false);

    BanOnResource banOnResource = setUpBan(new Date());
    banOnResource = resourcesManager.setBan(sess, banOnResource);

    List<BanOnResource> returnedBans = resourcesManager.getBansForMember(sess, banOnResource.getMemberId());
    assertEquals(banOnResource, returnedBans.get(0));
  }

  @Test
  public void getBansForResource() throws Exception {
    System.out.println(CLASS_NAME + "getBansForResource");
    vo = setUpVo();
    facility = setUpFacility();
    resource = setUpResource();
    member = setUpMember(vo);
    group = setUpGroup(vo, member);
    perun.getResourcesManagerBl().assignGroupToResource(sess, group, resource, false, false, false);

    BanOnResource banOnResource = setUpBan(new Date());
    banOnResource = resourcesManager.setBan(sess, banOnResource);

    List<BanOnResource> returnedBans = resourcesManager.getBansForResource(sess, banOnResource.getResourceId());
    assertEquals(banOnResource, returnedBans.get(0));
  }

  @Test
  public void getEnrichedBansForResource() throws Exception {
    System.out.println(CLASS_NAME + "getEnrichedBansForResource");
    vo = setUpVo();
    facility = setUpFacility();
    resource = setUpResource();
    member = setUpMember(vo);
    group = setUpGroup(vo, member);
    perun.getResourcesManagerBl().assignGroupToResource(sess, group, resource, false, false, false);

    BanOnResource banOnResource = setUpBan(new Date());
    resourcesManager.setBan(sess, banOnResource);
    AttributeDefinition mmbrAttrDef = setUpMemberAttribute();
    AttributeDefinition usrAttrDef = setUpUserAttribute();
    Attribute mmbrAttr = new Attribute(mmbrAttrDef, "member attribute value");
    Attribute userAttr = new Attribute(usrAttrDef, "user attribute value");
    perun.getAttributesManagerBl()
        .setAttribute(sess, perun.getUsersManagerBl().getUserByMember(sess, member), userAttr);
    perun.getAttributesManagerBl().setAttribute(sess, member, mmbrAttr);

    List<EnrichedBanOnResource> returnedBans = resourcesManager.getEnrichedBansForResource(sess, resource.getId(),
        List.of(mmbrAttr.getName(), userAttr.getName()));
    assertEquals(1, returnedBans.size());
    assertThat(returnedBans.get(0).getMember().getMemberAttributes()).containsExactly(mmbrAttr);
    assertThat(returnedBans.get(0).getMember().getUserAttributes()).containsExactly(userAttr);
    assertThat(returnedBans.get(0).getBan()).isEqualTo(banOnResource);
    assertThat(returnedBans.get(0).getResource()).isEqualTo(resource);
  }

  @Test
  public void getEnrichedBansForUser() throws Exception {
    System.out.println(CLASS_NAME + "getEnrichedBansForUser");
    vo = setUpVo();
    facility = setUpFacility();
    resource = setUpResource();
    member = setUpMember(vo);
    group = setUpGroup(vo, member);
    perun.getResourcesManagerBl().assignGroupToResource(sess, group, resource, false, false, false);

    BanOnResource banOnResource = setUpBan(new Date());
    resourcesManager.setBan(sess, banOnResource);
    AttributeDefinition mmbrAttrDef = setUpMemberAttribute();
    AttributeDefinition usrAttrDef = setUpUserAttribute();
    Attribute mmbrAttr = new Attribute(mmbrAttrDef, "member attribute value");
    Attribute userAttr = new Attribute(usrAttrDef, "user attribute value");
    perun.getAttributesManagerBl()
        .setAttribute(sess, perun.getUsersManagerBl().getUserByMember(sess, member), userAttr);
    perun.getAttributesManagerBl().setAttribute(sess, member, mmbrAttr);

    List<EnrichedBanOnResource> returnedBans = resourcesManager.getEnrichedBansForUser(sess, member.getUserId(),
        List.of(mmbrAttr.getName(), userAttr.getName()));
    assertEquals(1, returnedBans.size());
    assertThat(returnedBans.get(0).getMember().getMemberAttributes()).containsExactly(mmbrAttr);
    assertThat(returnedBans.get(0).getMember().getUserAttributes()).containsExactly(userAttr);
    assertThat(returnedBans.get(0).getBan()).isEqualTo(banOnResource);
    assertThat(returnedBans.get(0).getResource()).isEqualTo(resource);
  }

  @Test
  public void getEnrichedResourceByIdWithAllAttributes() throws Exception {
    System.out.println(CLASS_NAME + "getEnrichedResourceByIdWithAllAttributes");

    vo = setUpVo();
    facility = setUpFacility();
    Resource resource = setUpResource();

    EnrichedResource enrichedResource = resourcesManager.getEnrichedResourceById(sess, resource.getId(), null);

    assertThat(enrichedResource.getResource()).isEqualTo(resource);
    assertThat(enrichedResource.getAttributes()).isNotEmpty();
  }

  @Test
  public void getEnrichedResourceByIdWithGivenAttributes() throws Exception {
    System.out.println(CLASS_NAME + "getEnrichedResourceByIdWithGivenAttributes");

    vo = setUpVo();
    facility = setUpFacility();
    Resource resource = setUpResource();

    EnrichedResource enrichedResource =
        resourcesManager.getEnrichedResourceById(sess, resource.getId(), Collections.singletonList(A_R_C_ID));

    assertThat(enrichedResource.getResource()).isEqualTo(resource);
    assertThat(enrichedResource.getAttributes()).hasSize(1);
    assertThat(enrichedResource.getAttributes().get(0).getName()).isEqualTo(A_R_C_ID);
  }

  @Test
  public void getEnrichedResourcesForFacilityWithAllAttributes() throws Exception {
    System.out.println(CLASS_NAME + "getEnrichedResourcesForFacilityWithAllAttributes");

    vo = setUpVo();
    facility = setUpFacility();
    Resource resource = setUpResource();

    List<EnrichedResource> enrichedResources = resourcesManager.getEnrichedResourcesForFacility(sess, facility, null);

    assertThat(enrichedResources).hasSize(1);
    assertThat(enrichedResources.get(0).getResource()).isEqualTo(resource);
    assertThat(enrichedResources.get(0).getAttributes()).isNotEmpty();
  }

  @Test
  public void getEnrichedResourcesForFacilityWithGivenAttributes() throws Exception {
    System.out.println(CLASS_NAME + "getEnrichedResourcesForFacilityWithGivenAttributes");

    vo = setUpVo();
    facility = setUpFacility();
    setUpResource();

    List<EnrichedResource> enrichedResources =
        resourcesManager.getEnrichedResourcesForFacility(sess, facility, Collections.singletonList(A_R_C_ID));

    assertThat(enrichedResources).hasSize(1);
    assertThat(enrichedResources.get(0).getAttributes()).hasSize(1);
    assertThat(enrichedResources.get(0).getAttributes().get(0).getName()).isEqualTo(A_R_C_ID);
  }

  @Test
  public void getEnrichedResourcesForVoWithAllAttributes() throws Exception {
    System.out.println(CLASS_NAME + "getEnrichedResourcesForVoWithAllAttributes");

    vo = setUpVo();
    facility = setUpFacility();
    Resource resource = setUpResource();

    List<EnrichedResource> enrichedResources = resourcesManager.getEnrichedResourcesForVo(sess, vo, null);

    assertThat(enrichedResources).hasSize(1);
    assertThat(enrichedResources.get(0).getResource()).isEqualTo(resource);
    assertThat(enrichedResources.get(0).getAttributes()).isNotEmpty();
  }

  @Test
  public void getEnrichedResourcesForVoWithGivenAttributes() throws Exception {
    System.out.println(CLASS_NAME + "getEnrichedResourcesForVoWithGivenAttributes");

    vo = setUpVo();
    facility = setUpFacility();
    setUpResource();

    List<EnrichedResource> enrichedResources =
        resourcesManager.getEnrichedResourcesForVo(sess, vo, Collections.singletonList(A_R_C_ID));

    assertThat(enrichedResources).hasSize(1);
    assertThat(enrichedResources.get(0).getAttributes()).hasSize(1);
    assertThat(enrichedResources.get(0).getAttributes().get(0).getName()).isEqualTo(A_R_C_ID);
  }

  @Test
  public void getFacility() throws Exception {
    System.out.println(CLASS_NAME + "getFacility");

    vo = setUpVo();
    facility = setUpFacility();
    resource = setUpResource();

    Facility returnedFacility = resourcesManager.getFacility(sess, resource);
    assertNotNull("unable to get facility from resource", returnedFacility);
    assertEquals("original and returned facility should be the same", returnedFacility, facility);

  }

  @Test(expected = ResourceNotExistsException.class)
  public void getFacilityWhenResourceNotExists() throws Exception {
    System.out.println(CLASS_NAME + "getFacilityWhenResourceNotExists");

    resourcesManager.getFacility(sess, new Resource());
    // shouldn't find resource

  }

  @Test
  public void getGroupAssignments() throws Exception {
    System.out.println(CLASS_NAME + "getGroupAssignments");

    vo = setUpVo();
    member = setUpMember(vo);
    group = setUpGroup(vo, member);
    facility = setUpFacility();
    resource = setUpResource();

    resourcesManager.assignGroupToResource(sess, group, resource, false, false, false);

    List<AssignedGroup> groups = resourcesManager.getGroupAssignments(sess, resource, null);
    AssignedGroup expectedGroup =
        new AssignedGroup(new EnrichedGroup(group, null), GroupResourceStatus.ACTIVE, null, null, false);

    assertThat(groups.size()).isEqualTo(1);
    assertThat(groups).containsExactly(expectedGroup);
    assertThat(groups.get(0).getEnrichedGroup().getAttributes()).containsExactlyInAnyOrderElementsOf(
        perun.getAttributesManager().getAttributes(sess, group));
  }

  @Test
  public void getGroupAssignmentsWhenResourceNotExists() throws Exception {
    System.out.println(CLASS_NAME + "getGroupAssignmentsWhenResourceNotExists");

    assertThatExceptionOfType(ResourceNotExistsException.class).isThrownBy(
        () -> resourcesManager.getGroupAssignments(sess, new Resource(), null));
  }

  @Test
  public void getGroupResourceAssignmentsReturnsEmptyList() throws Exception {
    System.out.println(CLASS_NAME + "getGroupResourceAssignmentsReturnsEmptyList");

    List<GroupResourceAssignment> assignments =
        perun.getResourcesManagerBl().getGroupResourceAssignments(sess, List.of(GroupResourceStatus.ACTIVE));
    assertThat(assignments).isEmpty();
  }

  @Test
  public void getGroupResourceAssignmentsWithAllStatuses() throws Exception {
    System.out.println(CLASS_NAME + "getGroupResourceAssignmentsWithAllStatuses");

    vo = setUpVo();
    member = setUpMember(vo);
    group = setUpGroup(vo, member);
    facility = setUpFacility();
    resource = setUpResource();
    Resource resource2 = setUpResource2();

    resourcesManager.assignGroupToResource(sess, group, resource, false, false, false);
    resourcesManager.assignGroupToResource(sess, group, resource2, false, false, false);
    resourcesManager.deactivateGroupResourceAssignment(sess, group, resource2);

    GroupResourceAssignment expectedAssignment =
        new GroupResourceAssignment(group, resource, GroupResourceStatus.ACTIVE, null);
    GroupResourceAssignment expectedAssignment2 =
        new GroupResourceAssignment(group, resource2, GroupResourceStatus.INACTIVE, null);

    List<GroupResourceAssignment> assignments = perun.getResourcesManagerBl().getGroupResourceAssignments(sess, null);
    assertThat(assignments).containsExactlyInAnyOrder(expectedAssignment, expectedAssignment2);

    assignments = perun.getResourcesManagerBl().getGroupResourceAssignments(sess, Collections.emptyList());
    assertThat(assignments).containsExactlyInAnyOrder(expectedAssignment, expectedAssignment2);

    assignments =
        perun.getResourcesManagerBl().getGroupResourceAssignments(sess, Arrays.asList(GroupResourceStatus.values()));
    assertThat(assignments).containsExactlyInAnyOrder(expectedAssignment, expectedAssignment2);
  }

  @Test
  public void getGroupResourceAssignmentsWithGivenStatus() throws Exception {
    System.out.println(CLASS_NAME + "getGroupResourceAssignmentsWithGivenStatus");

    vo = setUpVo();
    member = setUpMember(vo);
    group = setUpGroup(vo, member);
    facility = setUpFacility();
    resource = setUpResource();
    Resource resource2 = setUpResource2();

    List<GroupResourceAssignment> assignments =
        perun.getResourcesManagerBl().getGroupResourceAssignments(sess, List.of(GroupResourceStatus.ACTIVE));
    assertThat(assignments).isEmpty();

    resourcesManager.assignGroupToResource(sess, group, resource, false, false, false);
    resourcesManager.assignGroupToResource(sess, group, resource2, false, false, false);
    resourcesManager.deactivateGroupResourceAssignment(sess, group, resource2);

    GroupResourceAssignment expectedAssignment =
        new GroupResourceAssignment(group, resource, GroupResourceStatus.ACTIVE, null);

    assignments = perun.getResourcesManagerBl().getGroupResourceAssignments(sess, List.of(GroupResourceStatus.ACTIVE));
    assertThat(assignments).containsExactly(expectedAssignment);
  }

  @Test
  public void getMailingServiceRichResourcesWithMember() throws Exception {
    System.out.println(CLASS_NAME + "getMailingServiceRichResourcesWithMember");

    vo = setUpVo();
    member = setUpMember(vo);
    group = setUpGroup(vo, member);
    facility = setUpFacility();

    service = setUpService("mailman_cesnet", setUpMailingListAttribute());
    resource = setUpResource();
    Resource anotherResource = setUpResource2();

    RichResource richResource = new RichResource(resource);
    richResource.setFacility(facility);
    richResource.setVo(vo);

    // assign group to both resources
    resourcesManager.assignGroupToResource(sess, group, resource, false, false, false);
    resourcesManager.assignGroupToResource(sess, group, anotherResource, false, false, false);

    // assign service to ONE of the resources
    resourcesManager.assignService(sess, resource, service);

    List<RichResource> resources = resourcesManager.getMailingServiceRichResourcesWithMember(sess, member);

    assertEquals("there should have been only 1 assigned rich resource", 1, resources.size());
    assertTrue("different rich resource expected", resources.contains(richResource));
  }

  @Test
  public void getResourceAssignments() throws Exception {
    System.out.println(CLASS_NAME + "getResourceAssignments");

    vo = setUpVo();
    member = setUpMember(vo);
    group = setUpGroup(vo, member);
    facility = setUpFacility();
    resource = setUpResource();

    resourcesManager.assignGroupToResource(sess, group, resource, false, false, false);

    List<AssignedResource> resources = resourcesManager.getResourceAssignments(sess, group, null);
    AssignedResource expectedResource =
        new AssignedResource(new EnrichedResource(resource, null), GroupResourceStatus.ACTIVE, null, null, facility,
            false);

    assertThat(resources.size()).isEqualTo(1);
    assertThat(resources).containsExactly(expectedResource);
    assertThat(resources.get(0).getEnrichedResource().getAttributes()).containsExactlyInAnyOrderElementsOf(
        perun.getAttributesManager().getAttributes(sess, resource));
    assertThat(resources.get(0).getFacility().getName()).isEqualTo(facility.getName());
  }

  @Test
  public void getResourceAssignmentsContainsTags() throws Exception {
    System.out.println(CLASS_NAME + "getResourceAssignmentsContainsTags");

    vo = setUpVo();
    member = setUpMember(vo);
    group = setUpGroup(vo, member);
    facility = setUpFacility();
    resource = setUpResource();
    ResourceTag resourceTag = new ResourceTag(1, "This is a tag", vo.getId());
    resourcesManager.createResourceTag(sess, resourceTag, vo);

    resourcesManager.assignGroupToResource(sess, group, resource, false, false, false);
    resourcesManager.assignResourceTagToResource(sess, resourceTag, resource);

    List<AssignedResource> resources = resourcesManager.getResourceAssignments(sess, group, null);

    assertThat(resources.get(0).getResourceTags()).containsOnly(resourceTag);
  }

  @Test
  public void getResourceAssignmentsWhenGroupNotExists() throws Exception {
    System.out.println(CLASS_NAME + "getResourceAssignmentsWhenGroupNotExists");

    assertThatExceptionOfType(GroupNotExistsException.class).isThrownBy(
        () -> resourcesManager.getResourceAssignments(sess, new Group(), null));
  }

  @Test
  public void getResourceById() throws Exception {
    System.out.println(CLASS_NAME + "getResourceById");

    vo = setUpVo();
    facility = setUpFacility();
    resource = setUpResource();

    Resource returnedResource = resourcesManager.getResourceById(sess, resource.getId());
    assertNotNull("unable to get our resource from DB", returnedResource);
    assertEquals("created and returned resource should be the same", returnedResource, resource);
    assertThat(returnedResource.getUuid()).isNotNull();
    assertThat(returnedResource.getUuid().version()).isEqualTo(4);
  }

  @Test(expected = ResourceNotExistsException.class)
  public void getResourceByIdWhenResourceNotExist() throws Exception {
    System.out.println(CLASS_NAME + "getResourceByIdWhenResourceNotExists");

    resourcesManager.getResourceById(sess, 0);
    // shouldn't find Resource

  }

  @Test
  public void getResources() throws Exception {
    System.out.println(CLASS_NAME + "getResources");

    vo = setUpVo();
    facility = setUpFacility();
    resource = setUpResource();

    List<Resource> resources = resourcesManager.getResources(sess, vo);
    assertTrue("our VO should have one resource", resources.size() == 1);
    assertTrue("our resource should be between VO resources", resources.contains(resource));

  }

  @Test
  public void getResourcesByIds() throws Exception {
    System.out.println(CLASS_NAME + "getResourcesByIds");

    vo = setUpVo();
    facility = setUpFacility();
    resource = setUpResource();
    List<Resource> resources = resourcesManager.getResourcesByIds(sess, Collections.singletonList(resource.getId()));
    assertEquals(resources.size(), 1);
    assertTrue(resources.contains(resource));

    Resource resource2 = setUpResource2();
    resources = resourcesManager.getResourcesByIds(sess, Arrays.asList(resource.getId(), resource2.getId()));
    assertEquals(resources.size(), 2);
    assertTrue(resources.contains(resource));
    assertTrue(resources.contains(resource2));

    resources = resourcesManager.getResourcesByIds(sess, Collections.singletonList(resource2.getId()));
    assertEquals(resources.size(), 1);
    assertTrue(resources.contains(resource2));
  }

  @Test
  public void getResourcesCount() throws Exception {
    System.out.println(CLASS_NAME + "getResourcesCount");

    vo = setUpVo();
    facility = setUpFacility();
    resource = setUpResource();

    int count = resourcesManager.getResourcesCount(sess);
    assertTrue(count > 0);
  }

  @Test
  public void getResourcesForUserWithAllStatuses() throws Exception {
    System.out.println(CLASS_NAME + "getResourcesForUserWithAllStatuses");

    vo = setUpVo();
    facility = setUpFacility();
    resource = setUpResource();
    member = setUpMember(vo);
    group = setUpGroup(vo, member);

    User user = perun.getUsersManagerBl().getUserByMember(sess, member);
    resourcesManager.assignGroupToResource(sess, group, resource, false, false, false);

    List<Resource> resources = perun.getResourcesManagerBl().getResources(sess, user, null, List.of(), null);
    assertThat(resources).containsOnly(resource);
  }

  @Test
  public void getResourcesForUserWithAllowedStatusesForMember() throws Exception {
    System.out.println(CLASS_NAME + "getResourcesForUserWithAllowedStatusesForMember");

    vo = setUpVo();
    facility = setUpFacility();
    resource = setUpResource();
    member = setUpMember(vo);
    group = setUpGroup(vo, member);

    User user = perun.getUsersManagerBl().getUserByMember(sess, member);
    resourcesManager.assignGroupToResource(sess, group, resource, false, false, false);

    perun.getGroupsManagerBl().expireMemberInGroup(sess, member, group);

    List<Resource> resources =
        perun.getResourcesManagerBl().getResources(sess, user, null, List.of(MemberGroupStatus.EXPIRED), null);
    assertThat(resources).containsOnly(resource);

    resources = perun.getResourcesManagerBl().getResources(sess, user, null, List.of(MemberGroupStatus.VALID), null);
    assertThat(resources).doesNotContain(resource);

    perun.getGroupsManagerBl().reactivateMember(sess, member, group);
    perun.getMembersManagerBl().expireMember(sess, member);

    resources = perun.getResourcesManagerBl().getResources(sess, user, List.of(Status.EXPIRED), null, null);
    assertThat(resources).contains(resource);

    resources = perun.getResourcesManagerBl().getResources(sess, user, List.of(Status.VALID), null, null);
    assertThat(resources).doesNotContain(resource);
  }

  @Test
  public void getResourcesForUserWithAllowedStatusesForResource() throws Exception {
    System.out.println(CLASS_NAME + "getResourcesForUserWithAllowedStatuses");

    vo = setUpVo();
    facility = setUpFacility();
    resource = setUpResource();
    member = setUpMember(vo);
    group = setUpGroup(vo, member);

    User user = perun.getUsersManagerBl().getUserByMember(sess, member);
    resourcesManager.assignGroupToResource(sess, group, resource, false, false, false);

    List<Resource> resources = perun.getResourcesManagerBl()
                                   .getResources(sess, user, List.of(Status.VALID), List.of(MemberGroupStatus.VALID),
                                       List.of(GroupResourceStatus.ACTIVE));
    assertThat(resources).containsOnly(resource);

    resourcesManager.deactivateGroupResourceAssignment(sess, group, resource);

    resources = perun.getResourcesManagerBl()
                    .getResources(sess, user, List.of(Status.VALID), List.of(MemberGroupStatus.VALID),
                        List.of(GroupResourceStatus.INACTIVE));
    assertThat(resources).containsOnly(resource);

    resources = perun.getResourcesManagerBl()
                    .getResources(sess, user, List.of(Status.VALID), List.of(MemberGroupStatus.VALID), null);
    assertThat(resources).containsOnly(resource);

    resources = perun.getResourcesManagerBl()
                    .getResources(sess, user, List.of(Status.VALID), List.of(MemberGroupStatus.VALID),
                        List.of(GroupResourceStatus.ACTIVE));
    assertThat(resources).doesNotContain(resource);
  }

  @Test
  public void getResourcesSpecifiedByVoAndFacilityWhereUserIsAdmin() throws Exception {
    System.out.println(CLASS_NAME + "getResourcesSpecifiedByVoAndFacilityWhereUserIsAdmin");
    vo = setUpVo();
    member = setUpMember(vo);
    facility = setUpFacility();
    resource = setUpResource();
    User u = perun.getUsersManagerBl().getUserByMember(sess, member);

    resourcesManager.addAdmin(sess, resource, u);
    List<Resource> resources = resourcesManager.getResourcesWhereUserIsAdmin(sess, facility, vo, u);

    assertNotNull(resources);
    assertTrue(resources.contains(resource));
  }

  @Test
  public void getResourcesSpecifiedByVoAndFacilityWhereUserIsNotAdminButHisGroupIs() throws Exception {
    System.out.println(CLASS_NAME + "getResourcesSpecifiedByVoAndFacilityWhereUserIsNotAdminButHisGroupIs");
    vo = setUpVo();
    member = setUpMember(vo);
    facility = setUpFacility();
    resource = setUpResource();
    group = setUpGroup(vo, member);
    User u = perun.getUsersManagerBl().getUserByMember(sess, member);

    resourcesManager.addAdmin(sess, resource, group);
    List<Resource> resources = resourcesManager.getResourcesWhereUserIsAdmin(sess, facility, vo, u);

    assertNotNull(resources);
    assertTrue(resources.contains(resource));
  }

  @Test
  public void getResourcesSpecifiedByVoWhereUserIsAdmin() throws Exception {
    System.out.println(CLASS_NAME + "getResourcesSpecifiedByVoWhereUserIsAdmin");

    vo = setUpVo();
    member = setUpMember(vo);
    facility = setUpFacility();
    Facility facility2 = new Facility();
    facility2.setName("ResourcesManagerTestFacility2");
    facility2 = perun.getFacilitiesManager().createFacility(sess, facility2);
    resource = setUpResource();
    Resource resource2 = new Resource();
    resource2.setName("name");
    resource2 = resourcesManager.createResource(sess, resource2, vo, facility2);
    User u = perun.getUsersManagerBl().getUserByMember(sess, member);

    resourcesManager.addAdmin(sess, resource, u);
    resourcesManager.addAdmin(sess, resource2, u);
    List<Resource> resources = resourcesManager.getResourcesWhereUserIsAdmin(sess, vo, u);

    assertNotNull(resources);
    assertThat(resources).containsOnly(resource, resource2);
  }

  @Test
  public void getResourcesSpecifiedByVoWhereUserIsNotAdminButHisGroupIs() throws Exception {
    System.out.println(CLASS_NAME + "getResourcesSpecifiedByVoWhereUserIsNotAdminButHisGroupIs");
    vo = setUpVo();
    member = setUpMember(vo);
    facility = setUpFacility();
    resource = setUpResource();
    group = setUpGroup(vo, member);
    User u = perun.getUsersManagerBl().getUserByMember(sess, member);

    resourcesManager.addAdmin(sess, resource, group);
    List<Resource> resources = resourcesManager.getResourcesWhereUserIsAdmin(sess, vo, u);

    assertNotNull(resources);
    assertThat(resources).containsOnly(resource);
  }

  @Test(expected = VoNotExistsException.class)
  public void getResourcesWhenVoNotExists() throws Exception {
    System.out.println(CLASS_NAME + "getResourcesWhenVoNotExists");

    resourcesManager.getResources(sess, new Vo());
    // shouldn't find VO

  }

  @Test
  public void getResourcesWhereGroupIsAdmin() throws Exception {
    System.out.println(CLASS_NAME + "getResourcesWhereGroupIsAdmin");
    vo = setUpVo();
    member = setUpMember(vo);
    facility = setUpFacility();
    resource = setUpResource();
    group = setUpGroup(vo, member);

    resourcesManager.addAdmin(sess, resource, group);
    List<Resource> resources = resourcesManager.getResourcesWhereGroupIsAdmin(sess, facility, vo, group);

    assertNotNull(resources);
    assertTrue(resources.contains(resource));
  }

  @Test
  public void getResourcesWhereUserIsAdmin() throws Exception {
    System.out.println(CLASS_NAME + "getResourcesWhereUserIsAdmin");
    vo = setUpVo();
    member = setUpMember(vo);
    facility = setUpFacility();
    resource = setUpResource();
    User u = perun.getUsersManagerBl().getUserByMember(sess, member);

    resourcesManager.addAdmin(sess, resource, u);
    List<Resource> resources = resourcesManager.getResourcesWhereUserIsAdmin(sess, u);

    assertNotNull(resources);
    assertTrue(resources.contains(resource));
  }

  @Test
  public void getResourcesWhereUserIsNotAdminButHisGroupIs() throws Exception {
    System.out.println(CLASS_NAME + "getResourcesWhereUserIsNotAdminButHisGroupIs");
    vo = setUpVo();
    member = setUpMember(vo);
    facility = setUpFacility();
    resource = setUpResource();
    group = setUpGroup(vo, member);
    User u = perun.getUsersManagerBl().getUserByMember(sess, member);

    resourcesManager.addAdmin(sess, resource, group);
    List<Resource> resources = resourcesManager.getResourcesWhereUserIsAdmin(sess, u);

    assertNotNull(resources);
    assertTrue(resources.contains(resource));
  }

  @Test
  public void getRichResources() throws Exception {
    System.out.println(CLASS_NAME + "getRichResources");

    vo = setUpVo();
    facility = setUpFacility();
    resource = setUpResource();

    RichResource rr = new RichResource(resource);
    rr.setFacility(perun.getResourcesManager().getFacility(sess, resource));
    List<RichResource> resources = resourcesManager.getRichResources(sess, vo);
    assertTrue("our VO should have one rich resource", resources.size() == 1);
    assertTrue("our rich resource should be between VO resources", resources.contains(rr));
    for (RichResource rich : resources) {
      assertTrue("facility property must be filled!", rich.getFacility() != null);
    }

  }

  @Test
  public void getRichResourcesByIds() throws Exception {
    System.out.println(CLASS_NAME + "getRichResourcesByIds");

    vo = setUpVo();
    facility = setUpFacility();
    resource = setUpResource();
    RichResource richResource = perun.getResourcesManager().getRichResourceById(sess, resource.getId());
    List<RichResource> resources =
        resourcesManager.getRichResourcesByIds(sess, Collections.singletonList(resource.getId()));
    assertEquals(resources.size(), 1);
    assertTrue(resources.contains(richResource));

    Resource resource2 = setUpResource2();
    RichResource richResource2 = perun.getResourcesManager().getRichResourceById(sess, resource2.getId());
    resources = resourcesManager.getRichResourcesByIds(sess, Arrays.asList(resource.getId(), resource2.getId()));
    assertEquals(resources.size(), 2);
    assertTrue(resources.contains(richResource));
    assertTrue(resources.contains(richResource2));

    resources = resourcesManager.getRichResourcesByIds(sess, Collections.singletonList(resource2.getId()));
    assertEquals(resources.size(), 1);
    assertTrue(resources.contains(richResource2));
  }

  @Test(expected = VoNotExistsException.class)
  public void getRichResourcesWhenVoNotExists() throws Exception {
    System.out.println(CLASS_NAME + "getRichResourcesWhenVoNotExists");

    resourcesManager.getRichResources(sess, new Vo());
    // shouldn't find VO

  }

  @Test
  public void getVo() throws Exception {
    System.out.println(CLASS_NAME + "getVo");

    vo = setUpVo();
    facility = setUpFacility();
    resource = setUpResource();
    assertNotNull("unable to create resource", resource);

    Vo returnedVo = resourcesManager.getVo(sess, resource);
    assertNotNull("unable to get VO from resource", returnedVo);
    assertEquals("original and returned VO should be the same", returnedVo, vo);

  }

  @Test(expected = ResourceNotExistsException.class)
  public void getVoWhenResourceNotExist() throws Exception {
    System.out.println(CLASS_NAME + "getVoWhenResourceNotExists");

    resourcesManager.getVo(sess, new Resource());

  }

  @Test
  public void isResourceLastAssignedServices() throws Exception {
    System.out.println(CLASS_NAME + "isResourceLastAssignedServices");

    vo = setUpVo();
    facility = setUpFacility();
    resource = setUpResource();
    service = setUpService();
    Resource anotherResource = setUpResource2();

    resourcesManager.assignService(sess, resource, service);
    resourcesManager.assignService(sess, anotherResource, service);

    assertEquals(0, resourcesManager.isResourceLastAssignedServices(sess, resource, List.of(service)).size());

    resourcesManager.removeService(sess, anotherResource, service);
    assertEquals(service, resourcesManager.isResourceLastAssignedServices(sess, resource, List.of(service)).get(0));
  }

  @Test
  public void removeAdmin() throws Exception {
    System.out.println(CLASS_NAME + "removeAdmin");
    vo = setUpVo();
    facility = setUpFacility();
    resource = setUpResource();
    member = setUpMember(vo);
    User u = perun.getUsersManagerBl().getUserByMember(sess, member);

    resourcesManager.addAdmin(sess, resource, u);
    assertEquals(u, resourcesManager.getAdmins(sess, resource, false).get(0));

    resourcesManager.removeAdmin(sess, resource, u);
    assertFalse(resourcesManager.getAdmins(sess, resource, false).contains(u));
  }

  @Test(expected = UserNotAdminException.class)
  public void removeAdminWhichNotExists() throws Exception {
    System.out.println(CLASS_NAME + "removeAdminWhichNotExists");
    vo = setUpVo();
    facility = setUpFacility();
    resource = setUpResource();
    member = setUpMember(vo);
    User u = perun.getUsersManagerBl().getUserByMember(sess, member);

    resourcesManager.removeAdmin(sess, resource, u);
  }

  @Test
  public void removeAdminWithGroup() throws Exception {
    System.out.println(CLASS_NAME + "removeAdminWithGroup");
    vo = setUpVo();
    member = setUpMember(vo);
    group = setUpGroup(vo, member);
    facility = setUpFacility();
    resource = setUpResource();

    resourcesManager.addAdmin(sess, resource, group);
    assertTrue(resourcesManager.getAdminGroups(sess, resource).contains(group));

    resourcesManager.removeAdmin(sess, resource, group);
    assertFalse(resourcesManager.getAdminGroups(sess, resource).contains(group));
  }

  @Test
  public void removeBan() throws Exception {
    System.out.println(CLASS_NAME + "removeBan");
    vo = setUpVo();
    facility = setUpFacility();
    resource = setUpResource();
    member = setUpMember(vo);
    group = setUpGroup(vo, member);
    perun.getResourcesManagerBl().assignGroupToResource(sess, group, resource, false, false, false);

    BanOnResource banOnResource = setUpBan(new Date());
    banOnResource = resourcesManager.setBan(sess, banOnResource);

    List<BanOnResource> bansOnResource = resourcesManager.getBansForResource(sess, banOnResource.getResourceId());
    assertTrue(bansOnResource.size() == 1);

    perun.getResourcesManagerBl().removeBan(sess, banOnResource.getMemberId(), banOnResource.getResourceId());

    bansOnResource = resourcesManager.getBansForResource(sess, banOnResource.getResourceId());
    assertTrue(bansOnResource.isEmpty());
  }

  @Test
  public void removeBanById() throws Exception {
    System.out.println(CLASS_NAME + "removeBan");
    vo = setUpVo();
    facility = setUpFacility();
    resource = setUpResource();
    member = setUpMember(vo);
    group = setUpGroup(vo, member);
    perun.getResourcesManagerBl().assignGroupToResource(sess, group, resource, false, false, false);

    BanOnResource banOnResource = setUpBan(new Date());
    banOnResource = resourcesManager.setBan(sess, banOnResource);

    List<BanOnResource> bansOnResource = resourcesManager.getBansForResource(sess, banOnResource.getResourceId());
    assertTrue(bansOnResource.size() == 1);

    perun.getResourcesManagerBl().removeBan(sess, banOnResource.getId());

    bansOnResource = resourcesManager.getBansForResource(sess, banOnResource.getResourceId());
    assertTrue(bansOnResource.isEmpty());
  }

  @Test
  public void removeExpiredBansIfExist() throws Exception {
    System.out.println(CLASS_NAME + "removeExpiredBansIfExist");
    vo = setUpVo();
    facility = setUpFacility();
    resource = setUpResource();
    member = setUpMember(vo);
    group = setUpGroup(vo, member);
    perun.getResourcesManagerBl().assignGroupToResource(sess, group, resource, false, false, false);

    Date now = new Date();
    Date yesterday = new Date(now.getTime() - (1000 * 60 * 60 * 24));
    BanOnResource banOnResource = setUpBan(yesterday);
    banOnResource = resourcesManager.setBan(sess, banOnResource);

    List<BanOnResource> bansOnResource = resourcesManager.getBansForResource(sess, banOnResource.getResourceId());
    assertTrue(bansOnResource.size() == 1);

    perun.getResourcesManagerBl().removeAllExpiredBansOnResources(sess);

    bansOnResource = resourcesManager.getBansForResource(sess, banOnResource.getResourceId());
    assertTrue(bansOnResource.isEmpty());
  }

  @Test
  public void removeExpiredBansIfNotExist() throws Exception {
    System.out.println(CLASS_NAME + "removeExpiredBansIfNotExist");
    vo = setUpVo();
    facility = setUpFacility();
    resource = setUpResource();
    member = setUpMember(vo);
    group = setUpGroup(vo, member);
    perun.getResourcesManagerBl().assignGroupToResource(sess, group, resource, false, false, false);

    Date now = new Date();
    Date tommorow = new Date(now.getTime() + (1000 * 60 * 60 * 24));
    BanOnResource banOnResource = setUpBan(tommorow);
    banOnResource = resourcesManager.setBan(sess, banOnResource);

    List<BanOnResource> bansOnResource = resourcesManager.getBansForResource(sess, banOnResource.getResourceId());
    assertTrue(bansOnResource.size() == 1);

    perun.getResourcesManagerBl().removeAllExpiredBansOnResources(sess);

    bansOnResource = resourcesManager.getBansForResource(sess, banOnResource.getResourceId());
    assertTrue(bansOnResource.size() == 1);
  }

  @Test
  public void removeGroupFromResource() throws Exception {
    System.out.println(CLASS_NAME + "removeGroupFromResource");

    vo = setUpVo();
    facility = setUpFacility();
    member = setUpMember(vo);
    group = setUpGroup(vo, member);
    resource = setUpResource();
    resourcesManager.assignGroupToResource(sess, group, resource, false, false, false);

    resourcesManager.removeGroupFromResource(sess, group, resource);
    List<Group> groups = resourcesManager.getAssignedGroups(sess, resource);
    assertTrue("assignedGroups should be empty", groups.isEmpty());

  }

  @Test(expected = GroupNotExistsException.class)
  public void removeGroupFromResourceWhenGroupNotExists() throws Exception {
    System.out.println(CLASS_NAME + "removeGroupFromResourceWhenGroupNotExists");

    vo = setUpVo();
    facility = setUpFacility();
    resource = setUpResource();

    resourcesManager.removeGroupFromResource(sess, new Group(), resource);
    // shouldn't find Group

  }

  @Test(expected = ResourceNotExistsException.class)
  public void removeGroupFromResourceWhenResourceNotExists() throws Exception {
    System.out.println(CLASS_NAME + "removeGroupFromResourceWhenResourceNotExists");

    vo = setUpVo();
    member = setUpMember(vo);
    group = setUpGroup(vo, member);

    resourcesManager.removeGroupFromResource(sess, group, new Resource());
    // shouldn't find resource

  }

  @Test
  public void removeGroupFromResourceWhereGroupIsInactive() throws Exception {
    System.out.println(CLASS_NAME + "removeGroupFromResourceWhereGroupIsInactive");

    vo = setUpVo();
    member = setUpMember(vo);
    group = setUpGroup(vo, member);
    facility = setUpFacility();
    resource = setUpResource();
    resourcesManager.assignGroupToResource(sess, group, resource, false, false, false);
    resourcesManager.deactivateGroupResourceAssignment(sess, group, resource);

    assertThatNoException().isThrownBy(() -> resourcesManager.removeGroupFromResource(sess, group, resource));
  }

  @Test(expected = SubGroupCannotBeRemovedException.class)
  @Ignore //Because of removing grouper
  public void removeGroupFromResourceWhichIsSubGroup() throws Exception {
    System.out.println(CLASS_NAME + "removeGroupFromResourceWhichIsSubGroup");

    vo = setUpVo();
    member = setUpMember(vo);
    group = setUpGroup(vo, member);
    subGroup = setUpSubGroup(group);
    facility = setUpFacility();
    resource = setUpResource();
    resourcesManager.assignGroupToResource(sess, group, resource, false, false, false);

    resourcesManager.removeGroupFromResource(sess, subGroup, resource);
    // shouldn't remove subGroup when parent group was assigned

  }

  @Test(expected = GroupNotDefinedOnResourceException.class)
  public void removeGroupFromResourceWhichWasNotDefinedOnResource() throws Exception {
    System.out.println(CLASS_NAME + "removeGroupFromResourceWhichWasNotDefinedOnResource");

    vo = setUpVo();
    member = setUpMember(vo);
    group = setUpGroup(vo, member);
    facility = setUpFacility();
    resource = setUpResource();

    resourcesManager.removeGroupFromResource(sess, group, resource);

  }

  @Test
  public void removeGroupFromResourceWithSubgroups() throws Exception {
    System.out.println(CLASS_NAME + "removeGroupFromResourceWithSubgroups");

    vo = setUpVo();
    member = setUpMember(vo);
    group = setUpGroup(vo, member);
    facility = setUpFacility();
    resource = setUpResource();

    subGroup = setUpSubGroup(group);
    Group subGroup2 = setUpSubGroup(subGroup);
    Group subGroup3 = setUpSubGroup(subGroup2);
    Group subGroup4 = setUpSubGroup(subGroup3);

    resourcesManager.assignGroupToResource(sess, group, resource, false, false, true);

    List<AssignedGroup> assignedGroups = resourcesManager.getGroupAssignments(sess, resource, null);
    assertThat(assignedGroups.size()).isEqualTo(5);

    // removing parent group from resource should unassign all subgroups
    resourcesManager.removeGroupFromResource(sess, group, resource);
    assignedGroups = resourcesManager.getGroupAssignments(sess, resource, null);
    assertThat(assignedGroups).isEmpty();
  }

  @Test
  public void removeGroupFromResourceWithoutManuallyAssignedSubgroup() throws Exception {
    System.out.println(CLASS_NAME + "removeGroupFromResourceWithoutManuallyAssignedSubgroup");

    vo = setUpVo();
    member = setUpMember(vo);
    group = setUpGroup(vo, member);
    facility = setUpFacility();
    resource = setUpResource();

    // subgroups should get assigned to resource upon creation
    resourcesManager.assignGroupToResource(sess, group, resource, false, false, true);
    subGroup = setUpSubGroup(group);

    // manually assign subgroup
    resourcesManager.assignGroupToResource(sess, subGroup, resource, false, false, false);

    resourcesManager.removeGroupFromResource(sess, group, resource);
    List<AssignedGroup> assignedGroups = resourcesManager.getGroupAssignments(sess, resource, null);

    AssignedGroup assignedSubgroup =
        new AssignedGroup(new EnrichedGroup(subGroup, List.of()), GroupResourceStatus.ACTIVE, null, null, false);
    assertThat(assignedGroups).containsExactly(assignedSubgroup);
  }

  @Test
  public void removeGroupFromResourceWithoutParentGroup() throws Exception {
    System.out.println(CLASS_NAME + "removeGroupFromResourceWithoutParentGroup");

    vo = setUpVo();
    member = setUpMember(vo);
    group = setUpGroup(vo, member);
    facility = setUpFacility();
    resource = setUpResource();

    subGroup = setUpSubGroup(group);
    setUpSubGroup(subGroup);

    resourcesManager.assignGroupToResource(sess, group, resource, false, false, false);
    resourcesManager.assignGroupToResource(sess, subGroup, resource, false, false, true);

    List<Group> assignedGroups = resourcesManager.getAssignedGroups(sess, resource);
    assertThat(assignedGroups.size()).isEqualTo(3);

    // parent group shouldn't be affected
    resourcesManager.removeGroupFromResource(sess, subGroup, resource);
    assignedGroups = resourcesManager.getAssignedGroups(sess, resource);
    assertThat(assignedGroups).containsExactly(group);
  }

  @Test
  public void removeResourceSelfServiceGroup() throws Exception {
    System.out.println(CLASS_NAME + "removeResourceSelfServiceGroup");

    vo = setUpVo();
    facility = setUpFacility();
    member = setUpMember(vo);
    group = setUpGroup(vo, member);

    Resource resource = setUpResource();

    resourcesManager.addResourceSelfServiceGroup(sess, resource, group);

    resourcesManager.removeResourceSelfServiceGroup(sess, resource, group);

    List<String> roles = AuthzResolverBlImpl.getGroupRoleNames(sess, group);

    assertFalse(roles.contains("RESOURCESELFSERVICE"));
  }

  @Test
  public void removeResourceSelfServiceUser() throws Exception {
    System.out.println(CLASS_NAME + "removeResourceSelfServiceUser");

    vo = setUpVo();
    facility = setUpFacility();
    Resource resource = setUpResource();
    User user = setUpUser("Milos", "Zeman");

    resourcesManager.addResourceSelfServiceUser(sess, resource, user);

    resourcesManager.removeResourceSelfServiceUser(sess, resource, user);

    List<String> roles = AuthzResolverBlImpl.getUserRoleNames(sess, user);

    assertFalse(roles.contains("RESOURCESELFSERVICE"));
  }

  @Test
  public void removeService() throws Exception {
    System.out.println(CLASS_NAME + "removeService");

    vo = setUpVo();
    facility = setUpFacility();
    resource = setUpResource();
    service = setUpService();

    resourcesManager.assignService(sess, resource, service);

    resourcesManager.removeService(sess, resource, service);
    List<Service> services = resourcesManager.getAssignedServices(sess, resource);
    assertTrue("shouldn't have any assigned service", services.isEmpty());

  }

  @Test
  public void removeServiceForResources() throws Exception {
    System.out.println(CLASS_NAME + "removeServiceForResources");

    vo = setUpVo();
    facility = setUpFacility();
    resource = setUpResource();
    Resource resource2 = setUpResource2();
    service = setUpService();

    resourcesManager.assignService(sess, resource, service);
    resourcesManager.assignService(sess, resource2, service);

    assertThat(resourcesManager.getAssignedServices(sess, resource)).contains(service);
    assertThat(resourcesManager.getAssignedServices(sess, resource2)).contains(service);

    resourcesManager.removeService(sess, List.of(resource2, resource), service);

    assertThat(resourcesManager.getAssignedServices(sess, resource)).doesNotContain(service);
    assertThat(resourcesManager.getAssignedServices(sess, resource2)).doesNotContain(service);

  }

  @Test(expected = ResourceNotExistsException.class)
  public void removeServiceWhenResourceNotExists() throws Exception {
    System.out.println(CLASS_NAME + "removeServiceWhenResourceNotExists");

    service = setUpService();

    resourcesManager.removeService(sess, new Resource(), service);
    // shouldn't find resource

  }

  @Test(expected = ServiceNotAssignedException.class)
  public void removeServiceWhenServiceNotAssigned() throws Exception {
    System.out.println(CLASS_NAME + "removeServiceWhenServiceNotAssigned");

    vo = setUpVo();
    facility = setUpFacility();
    resource = setUpResource();
    service = setUpService();

    resourcesManager.removeService(sess, resource, service);
    // shouldn't be able to remove not added service

  }

  @Test(expected = ServiceNotExistsException.class)
  public void removeServiceWhenServiceNotExists() throws Exception {
    System.out.println(CLASS_NAME + "removeServiceWhenServiceNotExists");

    vo = setUpVo();
    facility = setUpFacility();
    resource = setUpResource();

    resourcesManager.removeService(sess, resource, new Service());
    // shouldn't find service

  }

  @Test
  public void setBan() throws Exception {
    System.out.println(CLASS_NAME + "setBan");
    vo = setUpVo();
    facility = setUpFacility();
    resource = setUpResource();
    member = setUpMember(vo);
    group = setUpGroup(vo, member);
    perun.getResourcesManagerBl().assignGroupToResource(sess, group, resource, false, false, false);

    BanOnResource banOnResource = setUpBan(new Date());

    BanOnResource returnedBan = resourcesManager.setBan(sess, banOnResource);
    banOnResource.setId(returnedBan.getId());
    assertEquals(banOnResource, returnedBan);
  }

  @Before
  public void setUp() {
    resourcesManager = perun.getResourcesManager();
  }

  private Attribute setUpAttribute1() throws Exception {
    AttributeDefinition attrDef = new AttributeDefinition();
    attrDef.setNamespace(AttributesManager.NS_RESOURCE_ATTR_DEF);
    attrDef.setDescription("Test attribute description");
    attrDef.setFriendlyName("testingAttribute");
    attrDef.setType(String.class.getName());
    attrDef = perun.getAttributesManagerBl().createAttribute(sess, attrDef);
    Attribute attribute = new Attribute(attrDef);
    attribute.setValue("Testing value");
    return attribute;
  }

  private Attribute setUpAttribute2() throws Exception {
    AttributeDefinition attrDef = new AttributeDefinition();
    attrDef.setNamespace(AttributesManager.NS_RESOURCE_ATTR_DEF);
    attrDef.setDescription("Test attribute2 description");
    attrDef.setFriendlyName("testingAttribute2");
    attrDef.setType(String.class.getName());
    attrDef = perun.getAttributesManagerBl().createAttribute(sess, attrDef);
    Attribute attribute = new Attribute(attrDef);
    attribute.setValue("Testing value for second attribute");
    return attribute;
  }

  private Attribute setUpAttribute3() throws Exception {
    AttributeDefinition attrDef = new AttributeDefinition();
    attrDef.setNamespace(AttributesManager.NS_RESOURCE_ATTR_DEF);
    attrDef.setDescription("Test attribute3 description");
    attrDef.setFriendlyName("testingAttribute3");
    attrDef.setType(String.class.getName());
    attrDef = perun.getAttributesManagerBl().createAttribute(sess, attrDef);
    Attribute attribute = new Attribute(attrDef);
    attribute.setValue("Testing value for third attribute");
    return attribute;
  }

  private BanOnResource setUpBan(Date validity) {
    BanOnResource banOnResource = new BanOnResource();
    banOnResource.setMemberId(member.getId());
    banOnResource.setResourceId(resource.getId());
    banOnResource.setDescription("Popisek");
    banOnResource.setValidityTo(validity);
    return banOnResource;
  }

  // PRIVATE METHODS -----------------------------------------------------------

  private Candidate setUpCandidate() {

    String userFirstName = Long.toHexString(Double.doubleToLongBits(Math.random()));
    String userLastName = Long.toHexString(Double.doubleToLongBits(Math.random()));
    String extLogin =
        Long.toHexString(Double.doubleToLongBits(Math.random()));              // his login in external source

    Candidate candidate = new Candidate();  //Mockito.mock(Candidate.class);
    candidate.setFirstName(userFirstName);
    candidate.setId(0);
    candidate.setMiddleName("");
    candidate.setLastName(userLastName);
    candidate.setTitleBefore("");
    candidate.setTitleAfter("");
    ExtSource extSource = new ExtSource(0, "testExtSource", "cz.metacentrum.perun.core.impl.ExtSourceInternal");
    UserExtSource userExtSource = new UserExtSource(extSource, extLogin);
    candidate.setUserExtSource(userExtSource);
    candidate.setAttributes(new HashMap<>());
    return candidate;

  }

  private Facility setUpFacility() throws Exception {

    Facility facility = new Facility();
    facility.setName("ResourcesManagerTestFacility");
    facility = perun.getFacilitiesManager().createFacility(sess, facility);
    /*
         Owner owner = new Owner();
         owner.setName("ResourcesManagerTestOwner");
         owner.setContact("testingOwner");
         perun.getOwnersManager().createOwner(sess, owner);
         perun.getFacilitiesManager().addOwner(sess, facility, owner);
         */
    return facility;

  }

  private Group setUpGroup(Vo vo, Member member) throws Exception {

    Group group = new Group("ResourcesManagerTestGroup", "");
    group = perun.getGroupsManager().createGroup(sess, vo, group);
    perun.getGroupsManager().addMember(sess, group, member);
    return group;

  }

  private List<Attribute> setUpGroupResourceAttribute(Group group, Resource resource) throws Exception {
    AttributeDefinition attrDef = new AttributeDefinition();
    attrDef.setNamespace(AttributesManager.NS_GROUP_RESOURCE_ATTR_DEF);
    attrDef.setDescription("Test Group resource attribute desc");
    attrDef.setFriendlyName("testingAttributeGR");
    attrDef.setType(String.class.getName());
    attrDef = perun.getAttributesManagerBl().createAttribute(sess, attrDef);
    Attribute attribute = new Attribute(attrDef);
    attribute.setValue("super string value");
    perun.getAttributesManagerBl().setAttribute(sess, resource, group, attribute);
    attribute = perun.getAttributesManagerBl().getAttribute(sess, resource, group,
        AttributesManager.NS_GROUP_RESOURCE_ATTR_DEF + ":" + attrDef.getFriendlyName());
    List<Attribute> attributes = new ArrayList<>();
    attributes.add(attribute);
    return attributes;
  }

  private Attribute setUpMailingListAttribute() throws Exception {
    Attribute attribute = new Attribute(perun.getAttributesManagerBl()
                                            .getAttributeDefinition(sess,
                                                AttributesManager.NS_MEMBER_RESOURCE_ATTR_DEF + ":optOutMailingList"));
    attribute.setValue("Testing value for mailing list attribute");
    return attribute;
  }

  private Member setUpMember(Vo vo) throws Exception {

    Candidate candidate = setUpCandidate();
    Member member = perun.getMembersManagerBl().createMemberSync(sess, vo, candidate);
    // set first candidate as member of test VO
    assertNotNull("No member created", member);
    usersForDeletion.add(perun.getUsersManager().getUserByMember(sess, member));
    // save user for deletion after test
    return member;

  }

  private Attribute setUpMemberAttribute() throws Exception {
    AttributeDefinition attrDef = new AttributeDefinition();
    attrDef.setNamespace(AttributesManager.NS_MEMBER_ATTR_DEF);
    attrDef.setDescription("Test attribute description");
    attrDef.setFriendlyName("testingAttribute");
    attrDef.setType(String.class.getName());
    attrDef = perun.getAttributesManagerBl().createAttribute(sess, attrDef);
    Attribute attribute = new Attribute(attrDef);
    attribute.setValue("Testing value");
    return attribute;
  }

  private Resource setUpResource() throws Exception {

    Resource resource = new Resource();
    resource.setName("ResourcesManagerTestResource");
    resource.setDescription("Testovaci");
    resource = resourcesManager.createResource(sess, resource, vo, facility);
    return resource;

  }

  private Resource setUpResource2() throws Exception {

    Resource resource = new Resource();
    resource.setName("ResourcesManagerTestResource2");
    resource.setDescription("Testovaci2");
    resource = resourcesManager.createResource(sess, resource, vo, facility);
    return resource;

  }

  private ResourceTag setUpResourceTag() throws Exception {

    ResourceTag tag = new ResourceTag(0, "ResourceManagerTestResourceTag", vo.getId());
    tag = perun.getResourcesManager().createResourceTag(sess, tag, vo);
    assertNotNull("unable to create testing ResourceTag", tag);
    return tag;

  }

  private Service setUpService() throws Exception {

    Service service = new Service();
    service.setName("ResourcesManagerTestService");
    service = perun.getServicesManager().createService(sess, service);

    return service;

  }

  private Service setUpService(String name, Attribute attribute) throws Exception {

    Service service = new Service();
    service.setName(name);
    service = perun.getServicesManager().createService(sess, service);
    perun.getServicesManager().addRequiredAttributes(sess, service, List.of(attribute));

    return service;

  }

  private Group setUpSubGroup(Group group) throws Exception {

    Group subGroup = new Group("ResourcesManagerTestSubGroup", "");
    subGroup = perun.getGroupsManager().createGroup(sess, group, subGroup);
    perun.getGroupsManager().addMember(sess, subGroup, member);
    return subGroup;

  }

  private User setUpUser(String firstName, String lastName) throws Exception {

    User user = new User();
    user.setFirstName(firstName);
    user.setMiddleName("");
    user.setLastName(lastName);
    user.setTitleBefore("");
    user.setTitleAfter("");
    assertNotNull(perun.getUsersManagerBl().createUser(sess, user));

    return user;
  }

  private Attribute setUpUserAttribute() throws Exception {
    AttributeDefinition attrDef = new AttributeDefinition();
    attrDef.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
    attrDef.setDescription("Test attribute description");
    attrDef.setFriendlyName("testingAttribute");
    attrDef.setType(String.class.getName());
    attrDef = perun.getAttributesManagerBl().createAttribute(sess, attrDef);
    Attribute attribute = new Attribute(attrDef);
    attribute.setValue("Testing value");
    return attribute;
  }

  private Vo setUpVo() throws Exception {

    Vo newVo = new Vo(0, "ResourceManagerTestVo", "RMTestVo");
    Vo returnedVo = perun.getVosManager().createVo(sess, newVo);
    assertNotNull("unable to create testing Vo", returnedVo);
    return returnedVo;

  }

  @Test
  public void updateBan() throws Exception {
    System.out.println(CLASS_NAME + "updateBan");
    vo = setUpVo();
    facility = setUpFacility();
    resource = setUpResource();
    member = setUpMember(vo);
    group = setUpGroup(vo, member);
    perun.getResourcesManagerBl().assignGroupToResource(sess, group, resource, false, false, false);

    BanOnResource banOnResource = setUpBan(new Date());
    banOnResource = resourcesManager.setBan(sess, banOnResource);
    banOnResource.setDescription("New description");
    banOnResource.setValidityTo(new Date(banOnResource.getValidityTo().getTime() + 1000000));
    resourcesManager.updateBan(sess, banOnResource);

    BanOnResource returnedBan = resourcesManager.getBanById(sess, banOnResource.getId());
    assertEquals(banOnResource, returnedBan);
  }

  @Test
  public void updateResource() throws Exception {
    System.out.println(CLASS_NAME + "updateResource");

    vo = setUpVo();
    facility = setUpFacility();
    resource = setUpResource();

    resource.setName("UpdatedName1");
    final Resource updatedResource1 = resourcesManager.updateResource(sess, resource);
    assertEquals(updatedResource1, resourcesManager.getResourceById(sess, resource.getId()));

    resource.setDescription("ChangedDescription");
    final Resource updatedResource2 = resourcesManager.updateResource(sess, resource);
    assertEquals(updatedResource2, resourcesManager.getResourceById(sess, resource.getId()));
  }

  @Test(expected = ResourceExistsException.class)
  public void updateResourceWithExistingName() throws Exception {
    System.out.println(CLASS_NAME + "updateResourceWithExistingName");

    vo = setUpVo();
    facility = setUpFacility();
    Resource resource1 = setUpResource();
    Resource resource2 = setUpResource2();

    resource1.setName(resource2.getName());
    resourcesManager.updateResource(sess, resource1);
  }

  @Test
  public void updateResourceWithExistingNameInDifferentFacilities() throws Exception {
    System.out.println(CLASS_NAME + "updateResourceWithExistingNameInDifferentFacilities");

    vo = setUpVo();
    facility = setUpFacility();
    Facility facility2 = new Facility();
    facility2.setName("DifferentFacility");
    facility2 = perun.getFacilitiesManagerBl().createFacility(sess, facility2);

    Resource resource1 = setUpResource();
    Resource resource2 = new Resource();
    resource2.setName("DifferentResource");
    resource2.setDescription("DifferentDescription");
    resource2 = perun.getResourcesManagerBl().createResource(sess, resource2, vo, facility2);

    resource2.setName(resource1.getName());
    resource2.setDescription(resource1.getDescription());
    resourcesManager.updateResource(sess, resource2);

    assertEquals(resource1.getName(), resource2.getName());
    assertNotEquals(resource1.getId(), resource2.getId());
    assertNotEquals(resource1, resource2);
  }

}
