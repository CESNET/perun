package cz.metacentrum.perun.core.impl;

import static org.assertj.core.api.Assertions.assertThat;

import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.GroupResourceStatus;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.bl.ResourcesManagerBl;
import cz.metacentrum.perun.core.implApi.ResourcesManagerImplApi;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;


/**
 * @author Johana Supikova <xsupikov@fi.muni.cz>
 */
public class ResourceAssignmentCheckerTest extends AbstractPerunIntegrationTest {

  private static final String CLASS_NAME = "ResourceAssignmentChecker.";

  ResourceAssignmentChecker resourceAssignmentChecker;
  ResourcesManagerImplApi resourcesManagerImpl;
  ResourcesManagerBl resourcesManagerBl;

  private Vo vo;
  private Group group = new Group("Group1", "testovaci1");
  private Group group2 = new Group("Group2", "testovaci2");
  private Group group3 = new Group("Group3", "testovaci3");
  private Group group4 = new Group("Group4", "testovaci4");
  private Facility facility;
  private Resource resource;

  @Test
  public void assignedSubgroupGetsAssignmentAdded() throws Exception {
    System.out.println(CLASS_NAME + "assignedSubgroupGetsAssignmentAdded");

    // simulates some subgroups are assigned manually but automatic assignment is missing for all
    resourcesManagerImpl.assignGroupToResource(sess, group, resource, true);
    resourcesManagerImpl.assignGroupToResourceState(sess, group, resource, GroupResourceStatus.ACTIVE);

    resourcesManagerImpl.assignGroupToResource(sess, group2, resource, true);
    resourcesManagerImpl.assignGroupToResourceState(sess, group2, resource, GroupResourceStatus.ACTIVE);

    resourcesManagerImpl.assignGroupToResource(sess, group4, resource, true);
    resourcesManagerImpl.assignGroupToResourceState(sess, group4, resource, GroupResourceStatus.ACTIVE);

    // missing automatic assignment for group3
    assertThat(resourcesManagerBl.getAssignedGroups(sess, resource)).containsExactlyInAnyOrder(group, group2, group4);

    resourceAssignmentChecker.fixInconsistentGroupResourceAssignments();
    // only automatic assignments
    assertThat(resourcesManagerBl.getGroupAssignments(sess, resource, List.of()).stream()
        .filter(g -> g.getSourceGroupId() != null).collect(Collectors.toList())).hasSize(4);
  }

  @Test
  public void leftoutSubgroupGetsAssigned() throws Exception {
    System.out.println(CLASS_NAME + "leftoutSubgroupGetsAssigned");

    // simulates only source group is manually assigned
    resourcesManagerImpl.assignGroupToResource(sess, group, resource, true);
    resourcesManagerImpl.assignGroupToResourceState(sess, group, resource, GroupResourceStatus.ACTIVE);

    assertThat(resourcesManagerBl.getGroupAssignments(sess, resource, List.of())).hasSize(1);

    resourceAssignmentChecker.fixInconsistentGroupResourceAssignments();
    assertThat(resourcesManagerBl.getGroupAssignments(sess, resource, List.of())).hasSize(4);
  }

  @Test
  public void redundantSubgroupGetsRemoved() throws Exception {
    System.out.println(CLASS_NAME + "redundantSubgroupGetsRemoved");

    // simulates source group is no longer assigned
    resourcesManagerImpl.assignAutomaticGroupToResource(sess, group2, resource, group);
    resourcesManagerImpl.assignGroupToResourceState(sess, group2, resource, GroupResourceStatus.ACTIVE);

    assertThat(resourcesManagerBl.getAssignedGroups(sess, resource)).containsExactly(group2);

    resourceAssignmentChecker.fixInconsistentGroupResourceAssignments();
    assertThat(resourcesManagerBl.getAssignedGroups(sess, resource)).isEmpty();
  }

  @Test
  public void removedFromAllResources() throws Exception {
    System.out.println(CLASS_NAME + "removedFromAllResources");

    Resource resource2 = setUpResource2();

    // simulates leftovers are in more resources
    resourcesManagerImpl.assignAutomaticGroupToResource(sess, group2, resource, group);
    resourcesManagerImpl.assignGroupToResourceState(sess, group2, resource, GroupResourceStatus.ACTIVE);

    resourcesManagerImpl.assignAutomaticGroupToResource(sess, group2, resource2, group);
    resourcesManagerImpl.assignGroupToResourceState(sess, group2, resource2, GroupResourceStatus.ACTIVE);

    assertThat(resourcesManagerBl.getAssignedResources(sess, group2)).containsExactlyInAnyOrder(resource, resource2);

    resourceAssignmentChecker.fixInconsistentGroupResourceAssignments();
    assertThat(resourcesManagerBl.getAssignedResources(sess, group2)).isEmpty();
  }

  @Before
  public void setUp() throws Exception {

    resourcesManagerImpl =
        (ResourcesManagerImplApi) ReflectionTestUtils.getField(perun.getResourcesManagerBl(), "resourcesManagerImpl");
    if (resourcesManagerImpl == null) {
      throw new RuntimeException("Failed to get resourcesManagerImpl");
    }

    resourceAssignmentChecker = new ResourceAssignmentChecker(perun);
    resourcesManagerBl = perun.getResourcesManagerBl();

    setUpVo();
    setUpGroups();
    setUpFacility();
    setUpResource();
  }

  private void setUpFacility() throws Exception {
    facility = new Facility();
    facility.setName("TestFacility");
    facility.setDescription("testFacility");
    facility = perun.getFacilitiesManagerBl().createFacility(sess, facility);
  }

  // group <- group2 <- group3
  //       <- group4
  private void setUpGroups() throws Exception {
    group = perun.getGroupsManagerBl().createGroup(sess, vo, group);
    group2 = perun.getGroupsManagerBl().createGroup(sess, group, group2);
    group3 = perun.getGroupsManagerBl().createGroup(sess, group2, group3);
    group4 = perun.getGroupsManagerBl().createGroup(sess, group, group4);
  }

  private void setUpResource() throws Exception {
    resource = new Resource(-1, "Test", "Test", facility.getId());
    resource = perun.getResourcesManagerBl().createResource(sess, resource, vo, facility);
  }

  private Resource setUpResource2() throws Exception {
    Resource resource2 = new Resource(-2, "Test2", "Test2", facility.getId());
    resource2 = perun.getResourcesManagerBl().createResource(sess, resource2, vo, facility);
    return resource2;
  }

  private void setUpVo() throws Exception {
    vo = new Vo(-1, "TestVo", "testVo");
    vo = perun.getVosManagerBl().createVo(sess, vo);
  }

  @Test
  public void unaffectedGroupStaysAssigned() throws Exception {
    System.out.println(CLASS_NAME + "unaffectedGroupStaysAssigned");

    // simulates unaffected parent group of redundant group stays assigned
    resourcesManagerImpl.assignAutomaticGroupToResource(sess, group3, resource, group);
    resourcesManagerImpl.assignGroupToResourceState(sess, group3, resource, GroupResourceStatus.ACTIVE);

    resourcesManagerImpl.assignGroupToResource(sess, group2, resource, false);
    resourcesManagerImpl.assignGroupToResourceState(sess, group2, resource, GroupResourceStatus.ACTIVE);

    assertThat(resourcesManagerBl.getAssignedGroups(sess, resource)).containsExactlyInAnyOrder(group2, group3);

    resourceAssignmentChecker.fixInconsistentGroupResourceAssignments();
    assertThat(resourcesManagerBl.getAssignedGroups(sess, resource)).containsExactly(group2);
  }

}
