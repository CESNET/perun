package cz.metacentrum.perun.core.entry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;

import cz.metacentrum.perun.core.api.*;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotDefinedOnResourceException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.RelationExistsException;
import cz.metacentrum.perun.core.api.exceptions.ResourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ServiceAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotAssignedException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ServicesPackageNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.SubGroupCannotBeRemovedException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;

/**
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @version $Id: 9d63b2ad68112c8a312d8fefd2c9eae09a78d168 $
 */

public class ResourcesManagerEntryIntegrationTest extends AbstractPerunIntegrationTest {

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

	@Before
	public void setUp() throws Exception {

		resourcesManager = perun.getResourcesManager();

	}


	@Test
	public void getResourceById() throws Exception {
		System.out.println("ResourcesManager.getResourceById");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();

		Resource returnedResource = resourcesManager.getResourceById(sess, resource.getId());
		assertNotNull("unable to get our resource from DB", returnedResource);
		assertEquals("created and returned resource should be the same",returnedResource,resource);

	}

	@Test (expected=ResourceNotExistsException.class)
	public void getResourceByIdWhenResourceNotExist() throws Exception {
		System.out.println("ResourcesManager.getResourceByIdWhenResourceNotExists");

		resourcesManager.getResourceById(sess, 0);
		// shouldn't find Resource

	}

	@Test
	public void createResource() throws Exception {
		System.out.println("ResourcesManager.createResource");

		vo = setUpVo();
		facility = setUpFacility();

		Resource resource = new Resource();
		resource.setName("ResourcesManagerTestResource2");
		resource.setDescription("Testovaci2");
		Resource returnedResource = resourcesManager.createResource(sess, resource, vo, facility);
		assertNotNull("unable to create Resource", returnedResource);
		assertEquals("created and returned resource should be the same", returnedResource, resource);

	}

	@Test (expected=FacilityNotExistsException.class)
	public void createResourceWhenFacilityNotExists() throws Exception {
		System.out.println("ResourcesManager.createResourceWhenFacilityNotExists");

		vo = setUpVo();
                
		Resource resource = new Resource();
		resource.setName("ResourcesManagerTestResource2");
		resource.setDescription("Testovaci2");
		resourcesManager.createResource(sess, resource, vo, new Facility());
		// shouldn't find facility

	}

	@Test
	public void deleteResource() throws Exception {
		System.out.println("ResourcesManager.deleteResource");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		assertNotNull("unable to create resource before deletion",resource);

		resourcesManager.deleteResource(sess, resource);

		List<Resource> resources = resourcesManager.getResources(sess, vo);
		assertTrue("resource not deleted", resources.isEmpty());

	}

	@Ignore //Resource can be deleted with assigned group
	@Test (expected=RelationExistsException.class)
	public void deleteResourceWhenRelationExists() throws Exception {
		System.out.println("ResourcesManager.deleteResourceWhenRelationExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		assertNotNull("unable to create resource before deletion",resource);
		member = setUpMember(vo);
		group = setUpGroup(vo, member);
		resourcesManager.assignGroupToResource(sess, group, resource);

		resourcesManager.deleteResource(sess, resource);
		// shouldn't delete resource with assigned group

	}

	@Test
	public void deleteAllResources() throws Exception {
		System.out.println("ResourcesManager.deleteAllResources");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		assertNotNull("unable to create resource before deletion",resource);

		resourcesManager.deleteAllResources(sess, vo);

		List<Resource> resources = resourcesManager.getResources(sess, vo);
		assertTrue("resources not deleted", resources.isEmpty());

	}

	@Test (expected=VoNotExistsException.class)
	public void deleteAllResourcesWhenVoNotExists() throws Exception {
		System.out.println("ResourcesManager.deleteAllResourcesWhenVoNotExists");

		resourcesManager.deleteAllResources(sess, new Vo());

	}

	@Test
	public void getFacility() throws Exception {
		System.out.println("ResourcesManager.getFacility");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();

		Facility returnedFacility = resourcesManager.getFacility(sess, resource);
		assertNotNull("unable to get facility from resource",returnedFacility);
		assertEquals("original and returned facility should be the same",returnedFacility,facility);

	}

	@Test (expected=ResourceNotExistsException.class)
	public void getFacilityWhenResourceNotExists() throws Exception {
		System.out.println("ResourcesManager.getFacilityWhenResourceNotExists");

		resourcesManager.getFacility(sess, new Resource());
		// shouldn't find resource

	}

	@Test
	public void setFacility() throws Exception {
		System.out.println("ResourcesManager.setFacility");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		assertNotNull("unable to create resource",resource);

		Facility newFacility = new Facility();
		newFacility.setName("ResourcesManagerTestFacility2");
		newFacility.setType("TestingFacility2");
		newFacility = perun.getFacilitiesManager().createFacility(sess, newFacility);
		/*
	     Owner owner = new Owner();
	     owner.setName("ResourcesManagerTestOwner2");
	     owner.setContact("testingOwner2");
	     perun.getOwnersManager().createOwner(sess, owner);
	     perun.getFacilitiesManager().addOwner(sess, newFacility, owner);
		 */
		resourcesManager.setFacility(sess, resource, newFacility);

		Facility returnedFacility = resourcesManager.getFacility(sess, resource);
		assertNotNull("unable to get Facility from resource",returnedFacility);
		assertEquals("unable to set different Facility",newFacility,returnedFacility);

	}

	@Test (expected=ResourceNotExistsException.class)
	public void setFacilityWhenResourceNotExists() throws Exception {
		System.out.println("ResourcesManager.setFacilityWhenResourceNotExists");

		facility = setUpFacility();

		resourcesManager.setFacility(sess, new Resource(), facility);
		// shouldn't find resource

	}

	@Test (expected=FacilityNotExistsException.class)
	public void setFacilityWhenFacilityNotExists() throws Exception {
		System.out.println("ResourcesManager.setFacilityWhenFacilityNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		assertNotNull("unable to create resource",resource);

		resourcesManager.setFacility(sess, resource, new Facility());
		// shouldn't find facility

	}

	@Test
	public void getVo() throws Exception {
		System.out.println("ResourcesManager.getVo");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		assertNotNull("unable to create resource",resource);

		Vo returnedVo = resourcesManager.getVo(sess, resource);
		assertNotNull("unable to get VO from resource",returnedVo);
		assertEquals("original and returned VO should be the same", returnedVo, vo);

	}

	@Test (expected=ResourceNotExistsException.class)
	public void getVoWhenResourceNotExist() throws Exception {
		System.out.println("ResourcesManager.getVoWhenResourceNotExists");

		resourcesManager.getVo(sess, new Resource());

	}

	@Test
	public void getAllowedMembers() throws Exception {
		System.out.println("ResourcesManager.getAllowedMembers");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		assertNotNull("unable to create resource",resource);
		member = setUpMember(vo);
		group = setUpGroup(vo, member);
		resourcesManager.assignGroupToResource(sess, group, resource);

		List<Member> members = resourcesManager.getAllowedMembers(sess, resource);
		assertTrue("our resource should have 1 allowed member",members.size() == 1);
		assertTrue("our member should be between allowed on resource",members.contains(member));

	}

	@Test
	public void getAllowedUsers() throws Exception {
		System.out.println("ResourcesManager.getAllowedUsers");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		assertNotNull("unable to create resource",resource);
		member = setUpMember(vo);
		User user = perun.getUsersManagerBl().getUserByMember(sess, member);
		group = setUpGroup(vo, member);
		resourcesManager.assignGroupToResource(sess, group, resource);

		List<User> users = resourcesManager.getAllowedUsers(sess, resource);
		assertTrue("our resource should have 1 allowed user",users.size() == 1);
		assertTrue("our user should be between allowed on resource",users.contains(user));

	}

	@Test (expected=ResourceNotExistsException.class)
	public void getAllowedMembersWhenResourceNotExists() throws Exception {
		System.out.println("ResourcesManager.getAllowedMembersResourceNotExists");

		resourcesManager.getAllowedMembers(sess, new Resource());
		// shouldn't find resource

	}

	@Test
	public void assginGroupToResource() throws Exception {
		System.out.println("ResourcesManager.assignGroupToResource");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		assertNotNull("unable to create resource",resource);
		member = setUpMember(vo);
		group = setUpGroup(vo, member);
		resourcesManager.assignGroupToResource(sess, group, resource);

		List<Group> assignedGroups = resourcesManager.getAssignedGroups(sess, resource);
		assertTrue("one group should be assigned to our Resource",assignedGroups.size() == 1);
		assertTrue("our group shoud be assigned to resource",assignedGroups.contains(group));

	}

	@Test (expected=GroupNotExistsException.class)
	public void assginGroupToResourceWhenGroupNotExists() throws Exception {
		System.out.println("ResourcesManager.assignGroupToResourceWhenGroupNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		assertNotNull("unable to create resource",resource);
		resourcesManager.assignGroupToResource(sess, new Group(), resource);
		// shouldn't find group

	}

	@Test (expected=ResourceNotExistsException.class)
	public void assginGroupToResourceWhenResourceNotExists() throws Exception {
		System.out.println("ResourcesManager.assignGroupToResourceWhenResourceNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		member = setUpMember(vo);
		group = setUpGroup(vo, member);
		resourcesManager.assignGroupToResource(sess, group, new Resource());
		// shouldn't find resource

	}

	@Test (expected=GroupAlreadyAssignedException.class)
	public void assginGroupToResourceWhenGroupAlreadyAssigned() throws Exception {
		System.out.println("ResourcesManager.assignGroupToResourceWhenGroupAlreadyAssigned");

		vo = setUpVo();
		facility = setUpFacility();
		member = setUpMember(vo);
		group = setUpGroup(vo, member);
		resource = setUpResource();

		resourcesManager.assignGroupToResource(sess, group, resource);
		resourcesManager.assignGroupToResource(sess, group, resource);
		// shouldn't add group twice

	}

	// TODO jak otestovat další 2 výjimky na atributy ?

	@Test
	public void removeGroupFromResource() throws Exception {
		System.out.println("ResourcesManager.removeGroupFromResource");

		vo = setUpVo();
		facility = setUpFacility();
		member = setUpMember(vo);
		group = setUpGroup(vo, member);
		resource = setUpResource();
		resourcesManager.assignGroupToResource(sess, group, resource);

		resourcesManager.removeGroupFromResource(sess, group, resource);
		List<Group> groups = resourcesManager.getAssignedGroups(sess, resource);
		assertTrue("assignedGroups should be empty",groups.isEmpty());

	}

	@Test (expected=GroupNotExistsException.class)
	public void removeGroupFromResourceWhenGroupNotExists() throws Exception {
		System.out.println("ResourcesManager.removeGroupFromResourceWhenGroupNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();

		resourcesManager.removeGroupFromResource(sess, new Group(), resource);
		// shouldn't find Group

	}

	@Test (expected=ResourceNotExistsException.class)
	public void removeGroupFromResourceWhenResourceNotExists() throws Exception {
		System.out.println("ResourcesManager.removeGroupFromResourceWhenResourceNotExists");

		vo = setUpVo();
		member = setUpMember(vo);
		group = setUpGroup(vo, member);

		resourcesManager.removeGroupFromResource(sess, group, new Resource());
		// shouldn't find resource

	}

	@Test (expected=SubGroupCannotBeRemovedException.class)
        @Ignore //Because of removing grouper
	public void removeGroupFromResourceWhichIsSubGroup() throws Exception {
		System.out.println("ResourcesManager.removeGroupFromResourceWhichIsSubGroup");

		vo = setUpVo();
		member = setUpMember(vo);
		group = setUpGroup(vo, member);
		subGroup = setUpSubGroup(group);
		facility = setUpFacility();
		resource = setUpResource();
		resourcesManager.assignGroupToResource(sess, group, resource);

		resourcesManager.removeGroupFromResource(sess, subGroup, resource);
		// shouldn't remove subGroup when parent group was assigned

	}

	@Test (expected=GroupNotDefinedOnResourceException.class)
	public void removeGroupFromResourceWhichWasNotDefinedOnResource() throws Exception {
		System.out.println("ResourcesManager.removeGroupFromResourceWhichWasNotDefinedOnResource");

		vo = setUpVo();
		member = setUpMember(vo);
		group = setUpGroup(vo, member);
		facility = setUpFacility();
		resource = setUpResource();

		resourcesManager.removeGroupFromResource(sess, group, resource);

	}


	@Test
	public void getAssignedGroups() throws Exception {
		System.out.println("ResourcesManager.getAssignedGroups");

		vo = setUpVo();
		member = setUpMember(vo);
		group = setUpGroup(vo, member);
		facility = setUpFacility();
		resource = setUpResource();

		resourcesManager.assignGroupToResource(sess, group, resource);

		List<Group> groups = resourcesManager.getAssignedGroups(sess, resource);
		assertTrue("only one group should be assigned",groups.size() == 1);
		assertTrue("our group should be assigned",groups.contains(group));

	}

	@Test (expected=ResourceNotExistsException.class)
	public void getAssignedGroupsWhenResourceNotExists() throws Exception {
		System.out.println("ResourcesManager.getAssignedGroupsWhenResourceNotExists");

		resourcesManager.getAssignedGroups(sess, new Resource());
		// shouldn't find resource
	}

	@Test
	public void getAssignedResources() throws Exception {
		System.out.println("ResourcesManager.getAssignedResources");

		vo = setUpVo();
		member = setUpMember(vo);
		group = setUpGroup(vo, member);
		facility = setUpFacility();
		resource = setUpResource();

		resourcesManager.assignGroupToResource(sess, group, resource);

		List<Resource> resources = resourcesManager.getAssignedResources(sess, group);
		assertTrue("group should have be on 1 resource",resources.size() == 1);
		assertTrue("our resource should be on our group",resources.contains(resource));

	}

	@Test (expected=GroupNotExistsException.class)
	public void getAssignedResourcesWhenGroupNotExists() throws Exception {
		System.out.println("ResourcesManager.getAssignedResourcesWhenGroupNotExists");

		resourcesManager.getAssignedResources(sess, new Group());
		// shouldn't find group

	}

	@Test
	public void getAssignedRichResources() throws Exception {
		System.out.println("ResourcesManager.getAssignedRichResources");

		vo = setUpVo();
		member = setUpMember(vo);
		group = setUpGroup(vo, member);
		facility = setUpFacility();
		resource = setUpResource();
		RichResource rr = new RichResource(resource);
		rr.setFacility(perun.getResourcesManager().getFacility(sess, resource));

		resourcesManager.assignGroupToResource(sess, group, resource);

		List<RichResource> resources = resourcesManager.getAssignedRichResources(sess, group);
		assertTrue("group should have be on 1 rich resource",resources.size() == 1);
		assertTrue("our rich resource should be on our group",resources.contains(rr));
		for (RichResource rich : resources){
			assertTrue("facility property must be filled!",rich.getFacility()!=null);
		}

	}

	@Test (expected=GroupNotExistsException.class)
	public void getAssignedRichResourcesWhenGroupNotExists() throws Exception {
		System.out.println("ResourcesManager.getAssignedRichResourcesWhenGroupNotExists");

		resourcesManager.getAssignedRichResources(sess, new Group());
		// shouldn't find group

	}

	@Test
	public void assignService() throws Exception {
		System.out.println("ResourcesManager.assignService");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		service = setUpService();

		resourcesManager.assignService(sess, resource, service);
		List<Service> services = resourcesManager.getAssignedServices(sess, resource);
		assertTrue("resource should have 1 service",services.size() == 1);
		assertTrue("our service should be assigned to our resource",services.contains(service));

	}

	@Test (expected=ServiceNotExistsException.class)
	public void assignServiceWhenServiceNotExists() throws Exception {
		System.out.println("ResourcesManager.assignServiceWhenServiceNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();

		resourcesManager.assignService(sess, resource, new Service());
		// shouldn't find service

	}

	@Test (expected=ResourceNotExistsException.class)
	public void assignServiceWhenResourceNotExists() throws Exception {
		System.out.println("ResourcesManager.assignServiceWhenResourceNotExists");

		service = setUpService();

		resourcesManager.assignService(sess, new Resource(), service);
		// shouldn't find resource

	}

	@Test (expected=ServiceAlreadyAssignedException.class)
	public void assignServiceWhenServiceAlreadyAssigned() throws Exception {
		System.out.println("ResourcesManager.assignServiceWhenServiceAlreadyAssigned");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		service = setUpService();

		resourcesManager.assignService(sess, resource, service);
		resourcesManager.assignService(sess, resource, service);
		// shouldn't add service twice

	}

	@Test
	public void getAssignedServices() throws Exception {
		System.out.println("ResourcesManager.getAssignedServices");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		service = setUpService();

		resourcesManager.assignService(sess, resource, service);
		List<Service> services = resourcesManager.getAssignedServices(sess, resource);
		assertTrue("resource should have 1 service",services.size() == 1);
		assertTrue("our service should be assigned to our resource",services.contains(service));

	}

	@Test (expected=ResourceNotExistsException.class)
	public void getAssignedServicesWhenResourceNotExists() throws Exception {
		System.out.println("ResourcesManager.getAssignedServicesWhenResourceNotExists");

		resourcesManager.getAssignedServices(sess, new Resource());
		// shouldn't find resource

	}

	@Test
	public void assignServicesPackage() throws Exception {
		System.out.println("ResourcesManager.assignServicesPackage");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		service = setUpService();
		ServicesPackage servicesPackage = setUpServicesPackage(service);

		resourcesManager.assignServicesPackage(sess, resource, servicesPackage);

		List<Service> services = resourcesManager.getAssignedServices(sess, resource);
		assertTrue("resource should have 1 service",services.size() == 1);
		assertTrue("our service should be assigned to our resource",services.contains(service));

	}

	@Test (expected=ResourceNotExistsException.class)
	public void assignServicesPackageWhenResourceNotExists() throws Exception {
		System.out.println("ResourcesManager.assignServicesPackageWhenResourceNotExists");

		service = setUpService();
		ServicesPackage servicesPackage = setUpServicesPackage(service);

		resourcesManager.assignServicesPackage(sess, new Resource(), servicesPackage);
		// shouldn't find resource

	}

	@Test (expected=ServicesPackageNotExistsException.class)
	public void assignServicesPackageWhenPackageNotExists() throws Exception {
		System.out.println("ResourcesManager.assignServicesPackageWhenPackageNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();

		resourcesManager.assignServicesPackage(sess, resource, new ServicesPackage());
		// shouldn't find package

	}

	@Test
	public void removeService() throws Exception {
		System.out.println("ResourcesManager.removeService");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		service = setUpService();

		resourcesManager.assignService(sess, resource, service);

		resourcesManager.removeService(sess, resource, service);
		List<Service> services = resourcesManager.getAssignedServices(sess, resource);
		assertTrue("shouldn't have any assigned service", services.isEmpty());

	}

	@Test (expected=ResourceNotExistsException.class)
	public void removeServiceWhenResourceNotExists() throws Exception {
		System.out.println("ResourcesManager.removeServiceWhenResourceNotExists");

		service = setUpService();

		resourcesManager.removeService(sess, new Resource(), service);
		// shouldn't find resource

	}

	@Test (expected=ServiceNotExistsException.class)
	public void removeServiceWhenServiceNotExists() throws Exception {
		System.out.println("ResourcesManager.removeServiceWhenServiceNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();

		resourcesManager.removeService(sess, resource, new Service());
		// shouldn't find service

	}

	@Test (expected=ServiceNotAssignedException.class)
	public void removeServiceWhenServiceNotAssigned() throws Exception {
		System.out.println("ResourcesManager.removeServiceWhenServiceNotAssigned");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		service = setUpService();

		resourcesManager.removeService(sess, resource, service);
		// shouldn't be able to remove not added service

	}

	@Test
	public void removeServicesPackage() throws Exception {
		System.out.println("ResourcesManager.removeServicesPackage");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		service = setUpService();
		ServicesPackage servicesPackage = setUpServicesPackage(service);

		resourcesManager.assignServicesPackage(sess, resource, servicesPackage);

		resourcesManager.removeServicesPackage(sess, resource, servicesPackage);
		List<Service> services = resourcesManager.getAssignedServices(sess, resource);
		assertTrue("resource shouldn't have any services assigned",services.isEmpty());

	}

	@Test (expected=ResourceNotExistsException.class)
	public void removeServicesPackageWhenResourceNotExists() throws Exception {
		System.out.println("ResourcesManager.removeServicesPackageWhenResourceNotExists");

		service = setUpService();
		ServicesPackage servicesPackage = setUpServicesPackage(service);

		resourcesManager.removeServicesPackage(sess, new Resource(), servicesPackage);
		// shouldn't find resource

	}

	@Test (expected=ServicesPackageNotExistsException.class)
	public void removeServicesPackageWhenPackageNotExists() throws Exception {
		System.out.println("ResourcesManager.removeServicesPackageWhenPackageNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();

		resourcesManager.removeServicesPackage(sess, resource, new ServicesPackage());
		// shouldn't find services package

	}

	@Test
	public void getResources() throws Exception {
		System.out.println("ResourcesManager.getResources");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();

		List<Resource> resources = resourcesManager.getResources(sess, vo);
		assertTrue("our VO should have one resource",resources.size() == 1);
		assertTrue("our resource should be between VO resources",resources.contains(resource));

	}

	@Test (expected=VoNotExistsException.class)
	public void getResourcesWhenVoNotExists() throws Exception {
		System.out.println("ResourcesManager.getResourcesWhenVoNotExists");

		resourcesManager.getResources(sess, new Vo());
		// shouldn't find VO

	}

	@Test
	public void getRichResources() throws Exception {
		System.out.println("ResourcesManager.getRichResources");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();

		RichResource rr = new RichResource(resource);
		rr.setFacility(perun.getResourcesManager().getFacility(sess, resource));
		List<RichResource> resources = resourcesManager.getRichResources(sess, vo);
		assertTrue("our VO should have one rich resource",resources.size() == 1);
		assertTrue("our rich resource should be between VO resources",resources.contains(rr));
		for (RichResource rich : resources){
			assertTrue("facility property must be filled!",rich.getFacility()!=null);
		}

	}

	@Test (expected=VoNotExistsException.class)
	public void getRichResourcesWhenVoNotExists() throws Exception {
		System.out.println("ResourcesManager.getRichResourcesWhenVoNotExists");

		resourcesManager.getRichResources(sess, new Vo());
		// shouldn't find VO

	}

    @Test
    public void updateResource() throws Exception {
        System.out.println("ResourcesManager.updateResource");

        vo = setUpVo();
        facility = setUpFacility();
        resource = setUpResource();

        Resource resourceToUpdate = resourcesManager.createResource(sess, resource, vo, facility);
        resourceToUpdate.setName("TESTNAME1");
        resource.setDescription("TESTDESC1");
        final Resource updatedResource1 = resourcesManager.updateResource(sess, resourceToUpdate);
        assertEquals(resourceToUpdate, updatedResource1);

        resourceToUpdate.setName("TESTNAME2");
        final Resource updatedResource2 = resourcesManager.updateResource(sess, resourceToUpdate);
        assertEquals(resourceToUpdate, updatedResource2);

        resource.setDescription("TESTDESC3");
        final Resource updatedResource3 = resourcesManager.updateResource(sess, resourceToUpdate);
        assertEquals(resourceToUpdate, updatedResource3);
    }


    @Test
    public void getAllResourcesTagsForResource() throws Exception {
        System.out.println("ResourcesManager.getAllResourcesTagsForResource");

        vo = setUpVo();
        facility = setUpFacility();
        resource = setUpResource();
        ResourceTag tag = setUpResoruceTag();

        resourcesManager.assignResourceTagToResource(sess, tag, resource);
        List<ResourceTag> tags = perun.getResourcesManager().getAllResourcesTagsForResource(sess, resource);
        assertTrue("Created tag is not returned from resource", tags.contains(tag));

    }

    @Test
    public void getAllResourcesTagsForVo() throws Exception {
        System.out.println("ResourcesManager.getAllResourcesTagsForVo");

        vo = setUpVo();
        ResourceTag tag = setUpResoruceTag();
        List<ResourceTag> tags = perun.getResourcesManager().getAllResourcesTagsForVo(sess, vo);
        assertTrue("Created tag is not returned from VO", tags.contains(tag));

    }

    @Test
    public void getAllResourcesByResourceTag() throws Exception {
        System.out.println("ResourcesManager.getAllResourcesByResourceTag");

        vo = setUpVo();
        facility = setUpFacility();
        resource = setUpResource();
        ResourceTag tag = setUpResoruceTag();

        resourcesManager.assignResourceTagToResource(sess, tag, resource);
        List<Resource> resources = perun.getResourcesManager().getAllResourcesByResourceTag(sess, tag);
        assertTrue("Resource with tag is not returned by same tag", resources.contains(resource));

    }

	// PRIVATE METHODS -----------------------------------------------------------

	private Vo setUpVo() throws Exception {

		Vo newVo = new Vo(0, "ResourceManagerTestVo", "RMTestVo");
		Vo returnedVo = perun.getVosManager().createVo(sess, newVo);
		assertNotNull("unable to create testing Vo",returnedVo);
		return returnedVo;

	}

    private ResourceTag setUpResoruceTag() throws Exception {

        ResourceTag tag = new ResourceTag(0, "ResourceManagerTestResourceTag", vo.getId());
        tag = perun.getResourcesManager().createResourceTag(sess, tag, vo);
        assertNotNull("unable to create testing ResourceTag", tag);
        return tag;

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

	private Group setUpGroup(Vo vo, Member member) throws Exception {

		Group group = new Group("ResourcesManagerTestGroup","");
		group = perun.getGroupsManager().createGroup(sess, vo, group);
		perun.getGroupsManager().addMember(sess, group, member);
		return group;

	}

	private Group setUpSubGroup(Group group) throws Exception {

		Group subGroup = new Group("ResourcesManagerTestSubGroup","");
		subGroup = perun.getGroupsManager().createGroup(sess, group, subGroup);
		perun.getGroupsManager().addMember(sess, subGroup, member);
		return subGroup;

	}

	private Candidate setUpCandidate(){

		String userFirstName = Long.toHexString(Double.doubleToLongBits(Math.random()));
		String userLastName = Long.toHexString(Double.doubleToLongBits(Math.random()));
		String extLogin = Long.toHexString(Double.doubleToLongBits(Math.random()));              // his login in external source

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
		candidate.setAttributes(new HashMap<String,String>());
		return candidate;

	}

	private Facility setUpFacility() throws Exception {

		Facility facility = new Facility();
		facility.setName("ResourcesManagerTestFacility");
		facility.setType("Testing");
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

	private Resource setUpResource() throws Exception {

		Resource resource = new Resource();
		resource.setName("ResourcesManagerTestResource");
		resource.setDescription("Testovaci");
		resource = resourcesManager.createResource(sess, resource, vo, facility);
		return resource;

	}

	private Service setUpService() throws Exception {

		Owner owner = new Owner();
		owner.setName("ResourcesManagerTestServiceOwner");
		owner.setContact("testingServiceOwner");
                owner.setType(OwnerType.technical);
		perun.getOwnersManager().createOwner(sess, owner);

		Service service = new Service();
		service.setName("ResourcesManagerTestService");
		service = perun.getServicesManager().createService(sess, service, owner);

		return service;

	}

	private ServicesPackage setUpServicesPackage(Service service) throws Exception {

		ServicesPackage servicesPackage = new ServicesPackage();
		servicesPackage.setName("ResourcesManagertTestSP");
		servicesPackage.setDescription("testingServicePackage");
		servicesPackage = perun.getServicesManager().createServicesPackage(sess, servicesPackage);
		perun.getServicesManager().addServiceToServicesPackage(sess, servicesPackage, service);

		return servicesPackage;

	}

}
