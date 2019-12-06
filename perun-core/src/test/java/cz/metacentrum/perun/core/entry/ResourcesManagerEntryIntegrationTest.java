package cz.metacentrum.perun.core.entry;

import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.BanOnResource;
import cz.metacentrum.perun.core.api.Candidate;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.ResourceTag;
import cz.metacentrum.perun.core.api.ResourcesManager;
import cz.metacentrum.perun.core.api.RichResource;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.ServicesPackage;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotDefinedOnResourceException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.RelationExistsException;
import cz.metacentrum.perun.core.api.exceptions.ResourceExistsException;
import cz.metacentrum.perun.core.api.exceptions.ResourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ServiceAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotAssignedException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ServicesPackageNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.SubGroupCannotBeRemovedException;
import cz.metacentrum.perun.core.api.exceptions.UserNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.blImpl.AuthzResolverBlImpl;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Integration tests of ResourcesManager.
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class ResourcesManagerEntryIntegrationTest extends AbstractPerunIntegrationTest {

	private final static String CLASS_NAME = "ResourcesManager.";

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
	public void setUp() {
		resourcesManager = perun.getResourcesManager();
	}


	@Test
	public void getResourceById() throws Exception {
		System.out.println(CLASS_NAME + "getResourceById");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();

		Resource returnedResource = resourcesManager.getResourceById(sess, resource.getId());
		assertNotNull("unable to get our resource from DB", returnedResource);
		assertEquals("created and returned resource should be the same",returnedResource,resource);

	}

	@Test (expected=ResourceNotExistsException.class)
	public void getResourceByIdWhenResourceNotExist() throws Exception {
		System.out.println(CLASS_NAME + "getResourceByIdWhenResourceNotExists");

		resourcesManager.getResourceById(sess, 0);
		// shouldn't find Resource

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

	@Test (expected = ResourceExistsException.class)
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

	@Test (expected=FacilityNotExistsException.class)
	public void createResourceWhenFacilityNotExists() throws Exception {
		System.out.println(CLASS_NAME + "createResourceWhenFacilityNotExists");

		vo = setUpVo();

		Resource resource = new Resource();
		resource.setName("ResourcesManagerTestResource2");
		resource.setDescription("Testovaci2");
		resourcesManager.createResource(sess, resource, vo, new Facility());
		// shouldn't find facility

	}


	@Test (expected=ResourceExistsException.class)
	public void copyResourceWithExistingNameInDestinationFacility() throws Exception{
		System.out.println(CLASS_NAME + "copyResourceWithExistingNameInDestinationFacility");

		vo = setUpVo();
		facility = setUpFacility();

		resource = setUpResource();
		Resource resource1 = setUpResource();

		String newResourceName = "TestResource";

		resource1 = resourcesManager.createResource(sess, resource1, vo, facility);
		assertNotNull("unable to create resource1 before copying",resource1);

		resourcesManager.copyResource(sess, resource, resource1, false);
	}


	@Test (expected=InternalErrorException.class)
	public void copyResourceDifferentVO() throws Exception{
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
	public void copyResourceSameVO() throws Exception{
		System.out.println(CLASS_NAME + "copyResourceSameVO");
		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		resource.setVoId(vo.getId());
		resource.setFacilityId(facility.getId());
		assertNotNull("resource",resource);

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
	public void copyResourceWithEverythingFilled() throws Exception{
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
		resourcesManager.assignGroupToResource(sess, group, resource);
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

		Attribute resourceMemberAttr = perun.getAttributesManagerBl().getAttribute(sess, member, resource, resourceMemberAttrDef.getName());
		resourceMemberAttr.setValue(1);
		perun.getAttributesManagerBl().setAttribute(sess, member, resource, resourceMemberAttr);

		resourcesManager.copyResource(sess, resource, destinationResource, true);

		Resource createdResource = resourcesManager.getResourceByName(sess, vo, facility, destinationResource.getName());
		assertNotNull("Resource was not created.", createdResource);

		// group, service and resource tags copy check
		assertFalse("Group assigned to original resource not copied to destination resource.", resourcesManager.getAssignedGroups(sess, createdResource).isEmpty());
		assertFalse("Service not copied to destination resource.", resourcesManager.getAssignedServices(sess, createdResource).isEmpty());
		assertFalse("Resource tag not created for destination resource.", resourcesManager.getAllResourcesTagsForResource(sess, createdResource).isEmpty());

		// resource-member attributes check
		List<Attribute> resMembAttrs = perun.getAttributesManagerBl().getAttributes(sess, member, createdResource);
		assertFalse("Created resource does not contain any resource-member attributes.", resMembAttrs.isEmpty());
		assertTrue("Created resource does not contain template resource-member attribute (or copied value of attribute is wrong).",resMembAttrs.contains(resourceMemberAttr));
	}

	@Test
	public void deleteResource() throws Exception {
		System.out.println(CLASS_NAME + "deleteResource");

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
		System.out.println(CLASS_NAME + "deleteResourceWhenRelationExists");

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
		System.out.println(CLASS_NAME + "deleteAllResources");

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
		System.out.println(CLASS_NAME + "deleteAllResourcesWhenVoNotExists");

		resourcesManager.deleteAllResources(sess, new Vo());

	}

	@Test
	public void deleteResourceWithGroupResourceAttributes() throws Exception {
		System.out.println(CLASS_NAME + "deleteAllResourcesWhenVoNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		member = setUpMember(vo);
		group = setUpGroup(vo, member);

		perun.getResourcesManagerBl().assignGroupToResource(sess, group, resource);
		List<Attribute> attributes = setUpGroupResourceAttribute(group, resource);

		List<Attribute> retAttributes = perun.getAttributesManagerBl().getAttributes(sess, resource, group, false);
		assertEquals("Only one group resource attribute is set.",retAttributes.size(), 1);
		assertEquals("Not the correct attribute returned", attributes.get(0), retAttributes.get(0));

		perun.getResourcesManagerBl().deleteResource(sess, resource);
		retAttributes = perun.getAttributesManagerBl().getAttributes(sess, resource, group, false);
		assertEquals("There is still group resource attribute after deleting resource", retAttributes.size(), 0);
	}

	@Test
	public void getFacility() throws Exception {
		System.out.println(CLASS_NAME + "getFacility");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();

		Facility returnedFacility = resourcesManager.getFacility(sess, resource);
		assertNotNull("unable to get facility from resource",returnedFacility);
		assertEquals("original and returned facility should be the same",returnedFacility,facility);

	}

	@Test (expected=ResourceNotExistsException.class)
	public void getFacilityWhenResourceNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getFacilityWhenResourceNotExists");

		resourcesManager.getFacility(sess, new Resource());
		// shouldn't find resource

	}

	@Test
	public void getVo() throws Exception {
		System.out.println(CLASS_NAME + "getVo");

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
		System.out.println(CLASS_NAME + "getVoWhenResourceNotExists");

		resourcesManager.getVo(sess, new Resource());

	}

	@Test
	public void getAllowedMembers() throws Exception {
		System.out.println(CLASS_NAME + "getAllowedMembers");

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
		System.out.println(CLASS_NAME + "getAllowedUsers");

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
		System.out.println(CLASS_NAME + "getAllowedMembersResourceNotExists");

		resourcesManager.getAllowedMembers(sess, new Resource());
		// shouldn't find resource

	}

	@Test
	public void assginGroupToResource() throws Exception {
		System.out.println(CLASS_NAME + "assignGroupToResource");

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
		System.out.println(CLASS_NAME + "assignGroupToResourceWhenGroupNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		assertNotNull("unable to create resource",resource);
		resourcesManager.assignGroupToResource(sess, new Group(), resource);
		// shouldn't find group

	}

	@Test (expected=ResourceNotExistsException.class)
	public void assginGroupToResourceWhenResourceNotExists() throws Exception {
		System.out.println(CLASS_NAME + "assignGroupToResourceWhenResourceNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		member = setUpMember(vo);
		group = setUpGroup(vo, member);
		resourcesManager.assignGroupToResource(sess, group, new Resource());
		// shouldn't find resource

	}

	@Test (expected=GroupAlreadyAssignedException.class)
	public void assginGroupToResourceWhenGroupAlreadyAssigned() throws Exception {
		System.out.println(CLASS_NAME + "assignGroupToResourceWhenGroupAlreadyAssigned");

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
		System.out.println(CLASS_NAME + "removeGroupFromResource");

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
		System.out.println(CLASS_NAME + "removeGroupFromResourceWhenGroupNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();

		resourcesManager.removeGroupFromResource(sess, new Group(), resource);
		// shouldn't find Group

	}

	@Test (expected=ResourceNotExistsException.class)
	public void removeGroupFromResourceWhenResourceNotExists() throws Exception {
		System.out.println(CLASS_NAME + "removeGroupFromResourceWhenResourceNotExists");

		vo = setUpVo();
		member = setUpMember(vo);
		group = setUpGroup(vo, member);

		resourcesManager.removeGroupFromResource(sess, group, new Resource());
		// shouldn't find resource

	}

	@Test (expected=SubGroupCannotBeRemovedException.class)
	@Ignore //Because of removing grouper
	public void removeGroupFromResourceWhichIsSubGroup() throws Exception {
		System.out.println(CLASS_NAME + "removeGroupFromResourceWhichIsSubGroup");

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
		System.out.println(CLASS_NAME + "removeGroupFromResourceWhichWasNotDefinedOnResource");

		vo = setUpVo();
		member = setUpMember(vo);
		group = setUpGroup(vo, member);
		facility = setUpFacility();
		resource = setUpResource();

		resourcesManager.removeGroupFromResource(sess, group, resource);

	}


	@Test
	public void getAssignedGroups() throws Exception {
		System.out.println(CLASS_NAME + "getAssignedGroups");

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

	@Test
	public void getAssignedGroupsResourceMember() throws Exception {
		System.out.println(CLASS_NAME + "getAssignedGroupsResourceMember");

		vo = setUpVo();
		member = setUpMember(vo);
		group = setUpGroup(vo, member);
		facility = setUpFacility();
		resource = setUpResource();

		resourcesManager.assignGroupToResource(sess, group, resource);

		List<Group> groups = resourcesManager.getAssignedGroups(sess, resource, member);
		assertTrue("only one group should be assigned",groups.size() == 1);
		assertTrue("our group should be assigned",groups.contains(group));

	}

	@Test (expected=ResourceNotExistsException.class)
	public void getAssignedGroupsWhenResourceNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getAssignedGroupsWhenResourceNotExists");

		resourcesManager.getAssignedGroups(sess, new Resource());
		// shouldn't find resource
	}

	@Test
	public void getAssignedResources() throws Exception {
		System.out.println(CLASS_NAME + "getAssignedResources");

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
		System.out.println(CLASS_NAME + "getAssignedRichResourcesWhenGroupNotExists");

		resourcesManager.getAssignedRichResources(sess, new Group());
		// shouldn't find group

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
		service = setUpService();

		// both the resources assign to the group
		resourcesManager.assignGroupToResource(sess, group, resource);
		resourcesManager.assignGroupToResource(sess, group, sndResource);
		// but only one of them assign to the service
		resourcesManager.assignService(sess, resource, service);

		List<Resource> resources = resourcesManager.getAssignedResources(sess, member, service);
		assertTrue("there should have been only 1 assigned resource",resources.size() == 1);
		assertTrue("our resource should be in our resource list",resources.contains(resource));
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
		Resource sndResource = setUpResource2();
		service = setUpService();

		// both the resources assign to the group
		resourcesManager.assignGroupToResource(sess, group, resource);
		resourcesManager.assignGroupToResource(sess, group, sndResource);
		// but only one of them assign to the service
		resourcesManager.assignService(sess, resource, service);

		List<RichResource> resources = resourcesManager.getAssignedRichResources(sess, member, service);
		assertTrue("there should have been only 1 assigned rich resource",resources.size() == 1);
		assertTrue("our rich resource should be in our resource list",resources.contains(richResource));
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
		assertTrue("resource should have 1 service",services.size() == 1);
		assertTrue("our service should be assigned to our resource",services.contains(service));

	}

	@Test (expected=ServiceNotExistsException.class)
	public void assignServiceWhenServiceNotExists() throws Exception {
		System.out.println(CLASS_NAME + "assignServiceWhenServiceNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();

		resourcesManager.assignService(sess, resource, new Service());
		// shouldn't find service

	}

	@Test (expected=ResourceNotExistsException.class)
	public void assignServiceWhenResourceNotExists() throws Exception {
		System.out.println(CLASS_NAME + "assignServiceWhenResourceNotExists");

		service = setUpService();

		resourcesManager.assignService(sess, new Resource(), service);
		// shouldn't find resource

	}

	@Test (expected=ServiceAlreadyAssignedException.class)
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

	@Test
	public void getAssignedServices() throws Exception {
		System.out.println(CLASS_NAME + "getAssignedServices");

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
		System.out.println(CLASS_NAME + "getAssignedServicesWhenResourceNotExists");

		resourcesManager.getAssignedServices(sess, new Resource());
		// shouldn't find resource

	}

	@Test
	public void assignServicesPackage() throws Exception {
		System.out.println(CLASS_NAME + "assignServicesPackage");

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
		System.out.println(CLASS_NAME + "assignServicesPackageWhenResourceNotExists");

		service = setUpService();
		ServicesPackage servicesPackage = setUpServicesPackage(service);

		resourcesManager.assignServicesPackage(sess, new Resource(), servicesPackage);
		// shouldn't find resource

	}

	@Test (expected=ServicesPackageNotExistsException.class)
	public void assignServicesPackageWhenPackageNotExists() throws Exception {
		System.out.println(CLASS_NAME + "assignServicesPackageWhenPackageNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();

		resourcesManager.assignServicesPackage(sess, resource, new ServicesPackage());
		// shouldn't find package

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

	@Test (expected=ResourceNotExistsException.class)
	public void removeServiceWhenResourceNotExists() throws Exception {
		System.out.println(CLASS_NAME + "removeServiceWhenResourceNotExists");

		service = setUpService();

		resourcesManager.removeService(sess, new Resource(), service);
		// shouldn't find resource

	}

	@Test (expected=ServiceNotExistsException.class)
	public void removeServiceWhenServiceNotExists() throws Exception {
		System.out.println(CLASS_NAME + "removeServiceWhenServiceNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();

		resourcesManager.removeService(sess, resource, new Service());
		// shouldn't find service

	}

	@Test (expected=ServiceNotAssignedException.class)
	public void removeServiceWhenServiceNotAssigned() throws Exception {
		System.out.println(CLASS_NAME + "removeServiceWhenServiceNotAssigned");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		service = setUpService();

		resourcesManager.removeService(sess, resource, service);
		// shouldn't be able to remove not added service

	}

	@Test
	public void removeServicesPackage() throws Exception {
		System.out.println(CLASS_NAME + "removeServicesPackage");

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
		System.out.println(CLASS_NAME + "removeServicesPackageWhenResourceNotExists");

		service = setUpService();
		ServicesPackage servicesPackage = setUpServicesPackage(service);

		resourcesManager.removeServicesPackage(sess, new Resource(), servicesPackage);
		// shouldn't find resource

	}

	@Test (expected=ServicesPackageNotExistsException.class)
	public void removeServicesPackageWhenPackageNotExists() throws Exception {
		System.out.println(CLASS_NAME + "removeServicesPackageWhenPackageNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();

		resourcesManager.removeServicesPackage(sess, resource, new ServicesPackage());
		// shouldn't find services package

	}

	@Test
	public void getResources() throws Exception {
		System.out.println(CLASS_NAME + "getResources");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();

		List<Resource> resources = resourcesManager.getResources(sess, vo);
		assertTrue("our VO should have one resource",resources.size() == 1);
		assertTrue("our resource should be between VO resources",resources.contains(resource));

	}

	@Test (expected=VoNotExistsException.class)
	public void getResourcesWhenVoNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getResourcesWhenVoNotExists");

		resourcesManager.getResources(sess, new Vo());
		// shouldn't find VO

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
		assertTrue("our VO should have one rich resource",resources.size() == 1);
		assertTrue("our rich resource should be between VO resources",resources.contains(rr));
		for (RichResource rich : resources){
			assertTrue("facility property must be filled!",rich.getFacility()!=null);
		}

	}

	@Test (expected=VoNotExistsException.class)
	public void getRichResourcesWhenVoNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getRichResourcesWhenVoNotExists");

		resourcesManager.getRichResources(sess, new Vo());
		// shouldn't find VO

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

	@Test
	public void copyGroups() throws Exception {
		System.out.println(CLASS_NAME + "copyGroups");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		member = setUpMember(vo);
		group = setUpGroup(vo, member);
		resourcesManager.assignGroupToResource(sess, group, resource);

		// set up second resource
		Resource newResource = new Resource();
		newResource.setName("SecondResource");
		newResource.setDescription("pro kopirovani");
		Resource secondResource = resourcesManager.createResource(sess, newResource, vo, facility);

		resourcesManager.copyGroups(sess, resource, secondResource);

		//test
		assertTrue(resourcesManager.getAssignedGroups(sess, secondResource).contains(group));
	}

	@Test
	public void getResourcesCount() throws Exception {
		System.out.println(CLASS_NAME + "getResourcesCount");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();

		int count = resourcesManager.getResourcesCount(sess);
		assertTrue(count>0);
	}

	@Test
	public void setBan() throws Exception {
		System.out.println(CLASS_NAME + "setBan");
		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		member = setUpMember(vo);
		group = setUpGroup(vo, member);
		perun.getResourcesManagerBl().assignGroupToResource(sess, group, resource);

		BanOnResource banOnResource = new BanOnResource();
		banOnResource.setMemberId(member.getId());
		banOnResource.setResourceId(resource.getId());
		banOnResource.setDescription("Popisek");
		banOnResource.setValidityTo(new Date());

		BanOnResource returnedBan = resourcesManager.setBan(sess, banOnResource);
		banOnResource.setId(returnedBan.getId());
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
		perun.getResourcesManagerBl().assignGroupToResource(sess, group, resource);

		BanOnResource banOnResource = new BanOnResource();
		banOnResource.setMemberId(member.getId());
		banOnResource.setResourceId(resource.getId());
		banOnResource.setDescription("Popisek");
		banOnResource.setValidityTo(new Date());
		banOnResource = resourcesManager.setBan(sess, banOnResource);

		BanOnResource returnedBan = resourcesManager.getBanById(sess, banOnResource.getId());
		assertEquals(banOnResource, returnedBan);
	}

	@Test
	public void getBan() throws Exception {
		System.out.println(CLASS_NAME + "getBan");
		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		member = setUpMember(vo);
		group = setUpGroup(vo, member);
		perun.getResourcesManagerBl().assignGroupToResource(sess, group, resource);

		BanOnResource banOnResource = new BanOnResource();
		banOnResource.setMemberId(member.getId());
		banOnResource.setResourceId(resource.getId());
		banOnResource.setDescription("Popisek");
		banOnResource.setValidityTo(new Date());
		banOnResource = resourcesManager.setBan(sess, banOnResource);

		BanOnResource returnedBan = resourcesManager.getBan(sess, banOnResource.getMemberId(), banOnResource.getResourceId());
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
		perun.getResourcesManagerBl().assignGroupToResource(sess, group, resource);

		BanOnResource banOnResource = new BanOnResource();
		banOnResource.setMemberId(member.getId());
		banOnResource.setResourceId(resource.getId());
		banOnResource.setDescription("Popisek");
		banOnResource.setValidityTo(new Date());
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
		perun.getResourcesManagerBl().assignGroupToResource(sess, group, resource);

		BanOnResource banOnResource = new BanOnResource();
		banOnResource.setMemberId(member.getId());
		banOnResource.setResourceId(resource.getId());
		banOnResource.setDescription("Popisek");
		banOnResource.setValidityTo(new Date());
		banOnResource = resourcesManager.setBan(sess, banOnResource);

		List<BanOnResource> returnedBans = resourcesManager.getBansForResource(sess, banOnResource.getResourceId());
		assertEquals(banOnResource, returnedBans.get(0));
	}

	@Test
	public void updateBan() throws Exception {
		System.out.println(CLASS_NAME + "updateBan");
		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		member = setUpMember(vo);
		group = setUpGroup(vo, member);
		perun.getResourcesManagerBl().assignGroupToResource(sess, group, resource);

		BanOnResource banOnResource = new BanOnResource();
		banOnResource.setMemberId(member.getId());
		banOnResource.setResourceId(resource.getId());
		banOnResource.setDescription("Popisek");
		banOnResource.setValidityTo(new Date());
		banOnResource = resourcesManager.setBan(sess, banOnResource);
		banOnResource.setDescription("New description");
		banOnResource.setValidityTo(new Date(banOnResource.getValidityTo().getTime() + 1000000));
		resourcesManager.updateBan(sess, banOnResource);

		BanOnResource returnedBan = resourcesManager.getBanById(sess, banOnResource.getId());
		assertEquals(banOnResource, returnedBan);
	}

	@Test
	public void removeBanById() throws Exception {
		System.out.println(CLASS_NAME + "removeBan");
		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		member = setUpMember(vo);
		group = setUpGroup(vo, member);
		perun.getResourcesManagerBl().assignGroupToResource(sess, group, resource);

		BanOnResource banOnResource = new BanOnResource();
		banOnResource.setMemberId(member.getId());
		banOnResource.setResourceId(resource.getId());
		banOnResource.setDescription("Popisek");
		banOnResource.setValidityTo(new Date());
		banOnResource = resourcesManager.setBan(sess, banOnResource);

		List<BanOnResource> bansOnResource = resourcesManager.getBansForResource(sess, banOnResource.getResourceId());
		assertTrue(bansOnResource.size() == 1);

		perun.getResourcesManagerBl().removeBan(sess, banOnResource.getId());

		bansOnResource = resourcesManager.getBansForResource(sess, banOnResource.getResourceId());
		assertTrue(bansOnResource.isEmpty());
	}

	@Test
	public void removeBan() throws Exception {
		System.out.println(CLASS_NAME + "removeBan");
		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		member = setUpMember(vo);
		group = setUpGroup(vo, member);
		perun.getResourcesManagerBl().assignGroupToResource(sess, group, resource);

		BanOnResource banOnResource = new BanOnResource();
		banOnResource.setMemberId(member.getId());
		banOnResource.setResourceId(resource.getId());
		banOnResource.setDescription("Popisek");
		banOnResource.setValidityTo(new Date());
		banOnResource = resourcesManager.setBan(sess, banOnResource);

		List<BanOnResource> bansOnResource = resourcesManager.getBansForResource(sess, banOnResource.getResourceId());
		assertTrue(bansOnResource.size() == 1);

		perun.getResourcesManagerBl().removeBan(sess, banOnResource.getMemberId(), banOnResource.getResourceId());

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
		perun.getResourcesManagerBl().assignGroupToResource(sess, group, resource);

		BanOnResource banOnResource = new BanOnResource();
		banOnResource.setMemberId(member.getId());
		banOnResource.setResourceId(resource.getId());
		banOnResource.setDescription("Popisek");
		Date now = new Date();
		Date yesterday = new Date(now.getTime() - (1000 * 60 * 60 * 24));
		banOnResource.setValidityTo(yesterday);
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
		perun.getResourcesManagerBl().assignGroupToResource(sess, group, resource);

		BanOnResource banOnResource = new BanOnResource();
		banOnResource.setMemberId(member.getId());
		banOnResource.setResourceId(resource.getId());
		banOnResource.setDescription("Popisek");
		Date now = new Date();
		Date tommorow = new Date(now.getTime() + (1000 * 60 * 60 * 24));
		banOnResource.setValidityTo(tommorow);
		banOnResource = resourcesManager.setBan(sess, banOnResource);

		List<BanOnResource> bansOnResource = resourcesManager.getBansForResource(sess, banOnResource.getResourceId());
		assertTrue(bansOnResource.size() == 1);

		perun.getResourcesManagerBl().removeAllExpiredBansOnResources(sess);

		bansOnResource = resourcesManager.getBansForResource(sess, banOnResource.getResourceId());
		assertTrue(bansOnResource.size() == 1);
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
		UserExtSource userExtSource = new UserExtSource(new ExtSource(0, "testExtSource",
				"cz.metacentrum.perun.core.impl.ExtSourceInternal"), Long.toHexString(Double.doubleToLongBits(Math.random())));
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

	@Test(expected=UserNotAdminException.class)
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

	// PRIVATE METHODS -----------------------------------------------------------

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

	private Vo setUpVo() throws Exception {

		Vo newVo = new Vo(0, "ResourceManagerTestVo", "RMTestVo");
		Vo returnedVo = perun.getVosManager().createVo(sess, newVo);
		assertNotNull("unable to create testing Vo",returnedVo);
		return returnedVo;

	}

	private ResourceTag setUpResourceTag() throws Exception {

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

	private Service setUpService() throws Exception {

		Service service = new Service();
		service.setName("ResourcesManagerTestService");
		service = perun.getServicesManager().createService(sess, service);

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
		attribute = perun.getAttributesManagerBl().getAttribute(sess, resource, group, AttributesManager.NS_GROUP_RESOURCE_ATTR_DEF + ":" + attrDef.getFriendlyName());
		List<Attribute> attributes = new ArrayList<>();
		attributes.add(attribute);
		return attributes;
	}

}
