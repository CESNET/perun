package cz.metacentrum.perun.core.entry;

import cz.metacentrum.perun.core.api.AssignedGroup;
import cz.metacentrum.perun.core.api.AssignedResource;
import cz.metacentrum.perun.core.api.AuthzResolver;
import cz.metacentrum.perun.core.api.BanOnResource;
import cz.metacentrum.perun.core.api.EnrichedResource;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.ResourceTag;
import cz.metacentrum.perun.core.api.ResourcesManager;
import cz.metacentrum.perun.core.api.RichMember;
import cz.metacentrum.perun.core.api.RichResource;
import cz.metacentrum.perun.core.api.RichUser;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.ServicesPackage;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AlreadyAdminException;
import cz.metacentrum.perun.core.api.exceptions.BanAlreadyExistsException;
import cz.metacentrum.perun.core.api.exceptions.BanNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupAlreadyRemovedFromResourceException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotDefinedOnResourceException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupResourceMismatchException;
import cz.metacentrum.perun.core.api.exceptions.GroupResourceStatusException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.ResourceAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.ResourceExistsException;
import cz.metacentrum.perun.core.api.exceptions.ResourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ResourceTagAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.ResourceTagNotAssignedException;
import cz.metacentrum.perun.core.api.exceptions.ResourceTagNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.RoleCannotBeManagedException;
import cz.metacentrum.perun.core.api.exceptions.ServiceAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotAssignedException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ServicesPackageNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.bl.ResourcesManagerBl;
import cz.metacentrum.perun.core.blImpl.AuthzResolverBlImpl;
import cz.metacentrum.perun.core.impl.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author Slavek Licehammer glory@ics.muni.cz
 */
public class ResourcesManagerEntry implements ResourcesManager {

	final static Logger log = LoggerFactory.getLogger(ResourcesManagerEntry.class);

	private ResourcesManagerBl resourcesManagerBl;
	private PerunBl perunBl;

	public ResourcesManagerEntry(PerunBl perunBl) {
		this.perunBl = perunBl;
		this.resourcesManagerBl = perunBl.getResourcesManagerBl();
	}

	public ResourcesManagerEntry() {
	}

	@Override
	public Resource getResourceById(PerunSession sess, int id) throws PrivilegeException, ResourceNotExistsException {
		Utils.checkPerunSession(sess);

		Resource resource = getResourcesManagerBl().getResourceById(sess, id);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getResourceById_int_policy", resource)) {
			throw new PrivilegeException(sess, "getResourceById");
				}

		return resource;
	}

	@Override
	public List<Resource> getResourcesByIds(PerunSession sess, List<Integer> ids) throws PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getResourcesByIds_List<Integer>_policy")) {
			throw new PrivilegeException(sess, "getResourcesByIds");
		}

		List<Resource> resources = getResourcesManagerBl().getResourcesByIds(sess, ids);
		resources.removeIf(resource -> !AuthzResolver.authorizedInternal(sess, "filter-getResourcesByIds_List<Integer>_policy", resource));

		return resources;
	}

	@Override
	public EnrichedResource getEnrichedResourceById(PerunSession sess, int id, List<String> attrNames) throws PrivilegeException, ResourceNotExistsException {
		Utils.checkPerunSession(sess);

		EnrichedResource eResource = resourcesManagerBl.getEnrichedResourceById(sess, id, attrNames);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getEnrichedResourceById_int_List<String>_policy",
					eResource.getResource())) {
			throw new PrivilegeException(sess, "getEnrichedResourceById");
		}

		return resourcesManagerBl.filterOnlyAllowedAttributes(sess, eResource);
	}

	@Override
	public RichResource getRichResourceById(PerunSession sess, int id) throws PrivilegeException, ResourceNotExistsException {
		Utils.checkPerunSession(sess);

		RichResource rr = getResourcesManagerBl().getRichResourceById(sess, id);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getRichResourceById_int_policy", rr)) {
			throw new PrivilegeException(sess, "getRichResourceById");
		}

		return rr;

	}

	@Override
	public List<RichResource> getRichResourcesByIds(PerunSession sess, List<Integer> ids) throws PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getRichResourcesByIds_List<Integer>_policy")) {
			throw new PrivilegeException(sess, "getRichResourcesByIds");
		}

		List<RichResource> richResources = getResourcesManagerBl().getRichResourcesByIds(sess, ids);
		richResources.removeIf(richResource -> !AuthzResolver.authorizedInternal(sess, "filter-getRichResourcesByIds_List<Integer>_policy", richResource));

		return richResources;
	}

	@Override
	public Resource getResourceByName(PerunSession sess, Vo vo, Facility facility, String name) throws PrivilegeException,
				 ResourceNotExistsException, VoNotExistsException, FacilityNotExistsException {
					 Utils.checkPerunSession(sess);

					 getPerunBl().getVosManagerBl().checkVoExists(sess, vo);
					 getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);

					 Resource resource = getResourcesManagerBl().getResourceByName(sess, vo, facility, name);

					 // Authorization
					 if (!AuthzResolver.authorizedInternal(sess, "getResourceByName_Vo_Facility_String_policy", Arrays.asList(resource, vo, facility))) {
						 throw new PrivilegeException(sess, "getResourceByName");
					 }

					 return resource;
	}

	@Override
	public Resource createResource(PerunSession sess, Resource resource, Vo vo, Facility facility) throws PrivilegeException, VoNotExistsException, FacilityNotExistsException, ResourceExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "createResource_Resource_Vo_Facility_policy", Arrays.asList(vo, facility))) {
			throw new PrivilegeException(sess, "createResource");
		}

		return getResourcesManagerBl().createResource(sess, resource, vo, facility);
	}

	@Override
	public Resource copyResource(PerunSession sess, Resource templateResource, Resource destinationResource, boolean withGroups) throws ResourceNotExistsException, PrivilegeException, ResourceExistsException {
		Utils.checkPerunSession(sess);
		Utils.notNull(templateResource, "Template Resource");
		Utils.notNull(destinationResource, "Destination Resource");
		getResourcesManagerBl().checkResourceExists(sess, templateResource);

		//Authorization
		if (!AuthzResolver.authorizedInternal(sess, "copyResource_Resource_Resource_boolean_policy", templateResource) ||
			!AuthzResolver.authorizedInternal(sess, "copyResource_Resource_Resource_boolean_policy", destinationResource)) {
			throw new PrivilegeException(sess, "copyResource");
		}

		if(withGroups) {
			if(destinationResource.getVoId() != templateResource.getVoId()) {
				throw new InternalErrorException("Resources are not from the same VO.");
			}

			if(!AuthzResolver.authorizedInternal(sess, "withGroups-copyResource_Resource_Resource_boolean_policy", templateResource) ||
				!AuthzResolver.authorizedInternal(sess, "withGroups-copyResource_Resource_Resource_boolean_policy", destinationResource)) {
				throw new PrivilegeException(sess, "copyResource");
			}
		}

		return getResourcesManagerBl().copyResource(sess, templateResource, destinationResource, withGroups);
	}

	@Override
	public void deleteResource(PerunSession sess, Resource resource) throws ResourceNotExistsException, PrivilegeException, ResourceAlreadyRemovedException, GroupAlreadyRemovedFromResourceException, FacilityNotExistsException {
		Utils.checkPerunSession(sess);

		getResourcesManagerBl().checkResourceExists(sess, resource);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "deleteResource_Resource_policy", resource)) {
			throw new PrivilegeException(sess, "deleteResource");
				}

		getResourcesManagerBl().deleteResource(sess, resource);
	}

	@Override
	public void deleteAllResources(PerunSession sess, Vo vo) throws VoNotExistsException, PrivilegeException, ResourceAlreadyRemovedException, GroupAlreadyRemovedFromResourceException {
		Utils.checkPerunSession(sess);

		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		//Authorization
		if (!AuthzResolver.authorizedInternal(sess, "deleteAllResources_Vo_policy", vo)) {
			throw new PrivilegeException(sess, "deleteAllResources");
		}

		getResourcesManagerBl().deleteAllResources(sess, vo);
	}

	@Override
	public Facility getFacility(PerunSession sess, Resource resource) throws ResourceNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);
		getResourcesManagerBl().checkResourceExists(sess, resource);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getFacility_Resource_policy", resource)) {
			throw new PrivilegeException(sess, "getFacility");
				}

		return getResourcesManagerBl().getFacility(sess, resource);
	}

	@Override
	public Vo getVo(PerunSession sess, Resource resource) throws ResourceNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);
		getResourcesManagerBl().checkResourceExists(sess, resource);
		Vo vo = getPerunBl().getResourcesManagerBl().getVo(sess, resource);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getVo_Resource_policy", Arrays.asList(vo, resource))) {
			throw new PrivilegeException(sess, "getVo");
				}

		return vo;
	}

	@Override
	public List<Member> getAllowedMembers(PerunSession sess, Resource resource) throws ResourceNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getAllowedMembers_Resource_policy", resource)) {
			throw new PrivilegeException(sess, "getAllowedMembers");
		}

		return getResourcesManagerBl().getAllowedMembers(sess, resource);
	}

	@Override
	public List<User> getAllowedUsers(PerunSession sess, Resource resource) throws ResourceNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getAllowedUsers_Resource_policy", resource)) {
			throw new PrivilegeException(sess, "getAllowedUsers");
		}

		return getResourcesManagerBl().getAllowedUsers(sess, resource);
	}

	@Override
	public List<Service> getAssignedServices(PerunSession sess, Resource resource) throws ResourceNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);
		getResourcesManagerBl().checkResourceExists(sess, resource);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getAssignedServices_Resource_policy", resource)) {
			throw new PrivilegeException(sess, "getAssignedServices");
		}

		return getResourcesManagerBl().getAssignedServices(sess, resource);
	}

	@Override
	public List<Member> getAssignedMembers(PerunSession sess, Resource resource) throws PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getAssignedMembers_Resource_policy", resource)) {
			throw new PrivilegeException(sess, "getAssignedMembers");
		}

		return getResourcesManagerBl().getAssignedMembers(sess, resource);
	}

	@Override
	public List<RichMember> getAssignedRichMembers(PerunSession sess, Resource resource) throws PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getAssignedRichMembers_Resource_policy", resource)) {
			throw new PrivilegeException(sess, "getAssignedRichMembers");
		}

		return getResourcesManagerBl().getAssignedRichMembers(sess, resource);
	}

	@Override
	public void assignGroupToResource(PerunSession sess, Group group, Resource resource) throws PrivilegeException, GroupNotExistsException, ResourceNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException, GroupResourceMismatchException {
		Utils.checkPerunSession(sess);
		getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "assignGroupToResource_Group_Resource_policy", Arrays.asList(group, resource))) {
			throw new PrivilegeException(sess, "assignGroupToResource");
		}

		getResourcesManagerBl().assignGroupToResource(sess, group, resource);
	}

	@Override
	public void assignGroupsToResource(PerunSession perunSession, List<Group> groups, Resource resource) throws PrivilegeException, GroupNotExistsException, ResourceNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException, GroupResourceMismatchException {
		Utils.checkPerunSession(perunSession);
		Utils.notNull(groups, "groups");
		getResourcesManagerBl().checkResourceExists(perunSession, resource);

		// Authorization
		for (Group group: groups) {
			if (!AuthzResolver.authorizedInternal(perunSession, "assignGroupsToResource_List<Group>_Resource_policy", group, resource)) {
				throw new PrivilegeException(perunSession, "assignGroupsToResource");
			}
		}

		getResourcesManagerBl().assignGroupsToResource(perunSession, groups, resource);
	}

	@Override
	public void assignGroupToResources(PerunSession perunSession, Group group, List<Resource> resources) throws PrivilegeException, GroupNotExistsException, ResourceNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException, GroupResourceMismatchException {
		Utils.checkPerunSession(perunSession);
		Utils.notNull(resources, "resources");
		getPerunBl().getGroupsManagerBl().checkGroupExists(perunSession, group);
		for(Resource r: resources) {
			getResourcesManagerBl().checkResourceExists(perunSession, r);
		}

		// Authorization
		for (Resource resource: resources) {
			if (!AuthzResolver.authorizedInternal(perunSession, "assignGroupToResources_Group_List<Resource>_policy", resource, group)) {
				throw new PrivilegeException(perunSession, "assignGroupToResources");
			}
		}

		getResourcesManagerBl().assignGroupToResources(perunSession, group, resources);
	}

	@Override
	public void removeGroupFromResource(PerunSession sess, Group group, Resource resource) throws PrivilegeException, GroupNotExistsException, ResourceNotExistsException, GroupNotDefinedOnResourceException, GroupAlreadyRemovedFromResourceException {
		Utils.checkPerunSession(sess);
		getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "removeGroupFromResource_Group_Resource_policy", Arrays.asList(group, resource))) {
			throw new PrivilegeException(sess, "removeGroupFromResource");
		}

		getResourcesManagerBl().removeGroupFromResource(sess, group, resource);
	}

	@Override
	public void removeGroupsFromResource(PerunSession perunSession, List<Group> groups, Resource resource) throws PrivilegeException, GroupNotExistsException, ResourceNotExistsException, GroupNotDefinedOnResourceException, GroupAlreadyRemovedFromResourceException {
		Utils.checkPerunSession(perunSession);
		Utils.notNull(groups, "groups");
		getResourcesManagerBl().checkResourceExists(perunSession, resource);
		for(Group g: groups) {
			getPerunBl().getGroupsManagerBl().checkGroupExists(perunSession, g);
		}

		// Authorization
		for (Group group: groups) {
			if (!AuthzResolver.authorizedInternal(perunSession, "removeGroupsFromResource_List<Group>_Resource_policy", group, resource)) {
				throw new PrivilegeException(perunSession, "removeGroupsFromResource");
			}
		}

		getResourcesManagerBl().removeGroupsFromResource(perunSession, groups, resource);
	}

	@Override
	public void removeGroupFromResources(PerunSession perunSession, Group group, List<Resource> resources) throws PrivilegeException, GroupNotExistsException, ResourceNotExistsException, GroupNotDefinedOnResourceException, GroupAlreadyRemovedFromResourceException {
		Utils.checkPerunSession(perunSession);
		Utils.notNull(resources, "resources");
		getPerunBl().getGroupsManagerBl().checkGroupExists(perunSession, group);
		for(Resource r: resources) {
			getResourcesManagerBl().checkResourceExists(perunSession, r);
		}

		// Authorization
		for (Resource resource: resources) {
			if (!AuthzResolver.authorizedInternal(perunSession, "removeGroupFromResources_Group_List<Resource>_policy", resource, group)) {
				throw new PrivilegeException(perunSession, "removeGroupFromResources");
			}
		}

		getResourcesManagerBl().removeGroupFromResources(perunSession, group, resources);
	}

	@Override
	public List<Group> getAssignedGroups(PerunSession sess, Resource resource) throws PrivilegeException, ResourceNotExistsException {
		Utils.checkPerunSession(sess);
		getResourcesManagerBl().checkResourceExists(sess, resource);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getAssignedGroups_Resource_policy", resource)) {
			throw new PrivilegeException(sess, "getAssignedGroups");
		}

		List<Group> assignedGroups = getResourcesManagerBl().getAssignedGroups(sess, resource);

		assignedGroups.removeIf(assignedGroup -> !AuthzResolver.authorizedInternal(sess, "filter-getAssignedGroups_Resource_policy", assignedGroup, resource));

		return assignedGroups;
	}

	@Override
	public List<Group> getAssignedGroups(PerunSession sess, Resource resource, Member member) throws PrivilegeException, ResourceNotExistsException, MemberNotExistsException {
		Utils.checkPerunSession(sess);
		getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getAssignedGroups_Resource_Member_policy", Arrays.asList(resource, member))) {
			throw new PrivilegeException(sess, "getAssignedGroups");
		}

		return getResourcesManagerBl().getAssignedGroups(sess, resource, member);
	}

	@Override
	public List<Resource> getAssignedResources(PerunSession sess, Group group) throws GroupNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getAssignedResources_Group_policy", group)) {
			throw new PrivilegeException(sess, "getAssignedResources");
		}

		return getResourcesManagerBl().getAssignedResources(sess, group).stream()
			.filter(resource -> AuthzResolver.authorizedInternal(sess, "filter-getAssignedResources_Group_policy", group, resource))
			.collect(Collectors.toList());
	}

	@Override
	public List<RichResource> getAssignedRichResources(PerunSession sess, Group group) throws GroupNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getAssignedRichResources_Group_policy", group)) {
			throw new PrivilegeException(sess, "getAssignedRichResources");
				}

		return getResourcesManagerBl().getAssignedRichResources(sess, group).stream()
			.filter(resource -> AuthzResolver.authorizedInternal(sess, "filter-getAssignedRichResources_Group_policy", group, resource))
			.collect(Collectors.toList());
	}

	@Override
	public void assignService(PerunSession sess, Resource resource, Service service) throws PrivilegeException, ResourceNotExistsException, ServiceNotExistsException, ServiceAlreadyAssignedException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getServicesManagerBl().checkServiceExists(sess, service);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "assignService_Resource_Service_policy", Arrays.asList(resource, service))) {
			throw new PrivilegeException(sess, "assignService");
		}

		getResourcesManagerBl().assignService(sess, resource, service);
	}

	@Override
	public void assignServices(PerunSession sess, Resource resource, List<Service> services) throws PrivilegeException, ResourceNotExistsException, ServiceNotExistsException, ServiceAlreadyAssignedException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getResourcesManagerBl().checkResourceExists(sess, resource);

		for (Service service : services) {
			getPerunBl().getServicesManagerBl().checkServiceExists(sess, service);
		}

		// Authorization
		for (Service service: services) {
			if(!AuthzResolver.authorizedInternal(sess, "assignServices_Resource_List<Service>_policy", service, resource)){
				throw new PrivilegeException(sess, "assignServices");
			}
		}

		getResourcesManagerBl().assignServices(sess, resource, services);
	}

	@Override
	public void assignServicesPackage(PerunSession sess, Resource resource, ServicesPackage servicesPackage) throws PrivilegeException, ResourceNotExistsException, ServicesPackageNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);

		getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getServicesManagerBl().checkServicesPackageExists(sess, servicesPackage);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "assignServicesPackage_Resource_ServicesPackage_policy", Arrays.asList(resource, servicesPackage))) {
			throw new PrivilegeException(sess, "assignServicesPackage");
		}

		getResourcesManagerBl().assignServicesPackage(sess, resource, servicesPackage);
	}

	@Override
	public void removeService(PerunSession sess, Resource resource, Service service) throws PrivilegeException, ResourceNotExistsException, ServiceNotExistsException, ServiceNotAssignedException {
		Utils.checkPerunSession(sess);
		getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getServicesManagerBl().checkServiceExists(sess, service);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "removeService_Resource_Service_policy", Arrays.asList(resource, service))) {
			throw new PrivilegeException(sess, "removeServices");
		}

		getResourcesManagerBl().removeService(sess, resource, service);
	}

	@Override
	public void removeServices(PerunSession sess, Resource resource, List<Service> services) throws PrivilegeException, ResourceNotExistsException, ServiceNotExistsException, ServiceNotAssignedException {
		Utils.checkPerunSession(sess);
		getResourcesManagerBl().checkResourceExists(sess, resource);

		for (Service service : services) {
			getPerunBl().getServicesManagerBl().checkServiceExists(sess, service);
		}

		// Authorization
		for (Service service: services) {
			if(!AuthzResolver.authorizedInternal(sess, "removeServices_Resource_List<Service>_policy", service, resource)){
				throw new PrivilegeException(sess, "removeServices");
			}
		}

		getResourcesManagerBl().removeServices(sess, resource, services);
	}

	@Override
	public void removeServicesPackage(PerunSession sess, Resource resource, ServicesPackage servicesPackage) throws PrivilegeException, ResourceNotExistsException, ServicesPackageNotExistsException {
		Utils.checkPerunSession(sess);

		getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getServicesManagerBl().checkServicesPackageExists(sess, servicesPackage);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "removeServicesPackage_Resource_ServicesPackage_policy", Arrays.asList(resource, servicesPackage))) {
			throw new PrivilegeException(sess, "removeServicesPackage");
		}

		getResourcesManagerBl().removeServicesPackage(sess, resource, servicesPackage);
	}

	@Override
	public List<Resource> getResources(PerunSession sess, Vo vo) throws PrivilegeException, VoNotExistsException {
		Utils.checkPerunSession(sess);

		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getResources_Vo_policy", vo)) {
			throw new PrivilegeException(sess, "getResources");
		}

		List<Resource> resources = getResourcesManagerBl().getResources(sess, vo);

		List<Resource> allowedResources = new ArrayList<>();
		for (Resource resource : resources) {
			if (AuthzResolver.authorizedInternal(sess, "filter-getResources_Vo_policy", Arrays.asList(vo, resource))) {
				allowedResources.add(resource);
			}
		}
		resources = allowedResources;

		return resources;
	}

	@Override
	public List<RichResource> getRichResources(PerunSession sess, Vo vo) throws PrivilegeException, VoNotExistsException {
		Utils.checkPerunSession(sess);

		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getRichResources_Vo_policy", vo)) {
			throw new PrivilegeException(sess, "getRichResources");
		}

		List<RichResource> resources = getResourcesManagerBl().getRichResources(sess, vo);

		List<RichResource> allowedResources = new ArrayList<>();
		for (RichResource resource : resources) {
			if (AuthzResolver.authorizedInternal(sess, "filter-getRichResources_Vo_policy", Arrays.asList(vo, resource))) {
				allowedResources.add(resource);
			}
		}
		resources = allowedResources;

		return resources;
	}

	@Override
	public List<EnrichedResource> getEnrichedResourcesForVo(PerunSession sess, Vo vo, List<String> attrNames) throws VoNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getEnrichedResourcesForVo_Vo_policy", vo)) {
			throw new PrivilegeException(sess, "getEnrichedResourcesForVo");
		}

		return getResourcesManagerBl().getEnrichedRichResourcesForVo(sess, vo, attrNames).stream()
				.filter(eResource -> AuthzResolver.authorizedInternal(sess,
						"filter-getEnrichedResourcesForVo_Vo_policy", vo, eResource.getResource()))
				.map(eResource -> getResourcesManagerBl().filterOnlyAllowedAttributes(sess, eResource))
				.collect(Collectors.toList());
	}

	@Override
	public List<EnrichedResource> getEnrichedResourcesForFacility(PerunSession sess, Facility facility, List<String> attrNames) throws FacilityNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getEnrichedResourcesForFacility_Facility_policy", facility)) {
			throw new PrivilegeException(sess, "getEnrichedResourcesForFacility");
		}

		return getResourcesManagerBl().getEnrichedRichResourcesForFacility(sess, facility, attrNames).stream()
				.filter(eResource -> AuthzResolver.authorizedInternal(sess,
						"filter-getEnrichedResourcesForFacility_Facility_policy", facility, eResource.getResource()))
				.map(eResource -> getResourcesManagerBl().filterOnlyAllowedAttributes(sess, eResource))
				.collect(Collectors.toList());
	}

	@Override
	public int getResourcesCount(PerunSession sess, Vo vo) throws PrivilegeException, VoNotExistsException {
		Utils.checkPerunSession(sess);

		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getResourcesCount_Vo_policy", vo)) {
			throw new PrivilegeException(sess, "getResourcesCount");
		}

		return getResourcesManagerBl().getResourcesCount(sess, vo);
	}

	@Override
	public int getResourcesCount(PerunSession sess) {
		Utils.checkPerunSession(sess);

		return getResourcesManagerBl().getResourcesCount(sess);
	}

	@Override
	public List<Resource> getAllowedResources(PerunSession sess, Member member) throws MemberNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getAllowedResources_Member_policy", member)) {
			throw new PrivilegeException(sess, "getAllowedResources");
		}

		return getResourcesManagerBl().getAllowedResources(sess, member);
	}

	@Override
	public List<Resource> getAssignedResources(PerunSession sess, Member member) throws PrivilegeException, MemberNotExistsException {
		Utils.checkPerunSession(sess);

		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getAssignedResources_Member_policy", member)) {
			throw new PrivilegeException(sess, "getAssignedResources");
		}

		return getResourcesManagerBl().getAssignedResources(sess, member);
	}

	@Override
	public List<Resource> getAssignedResources(PerunSession sess, Member member, Service service) throws PrivilegeException, MemberNotExistsException, ServiceNotExistsException {
		Utils.checkPerunSession(sess);

		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getPerunBl().getServicesManagerBl().checkServiceExists(sess, service);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getAssignedResources_Member_Service_policy", Arrays.asList(member, service))) {
			throw new PrivilegeException(sess, "getAssignedResources");
		}

		return getResourcesManagerBl().getAssignedResources(sess, member, service);
	}

	@Override
	public List<RichResource> getAssignedRichResources(PerunSession sess, Member member) throws PrivilegeException, MemberNotExistsException {
		Utils.checkPerunSession(sess);

		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getAssignedRichResources_Member_policy", member)) {
			throw new PrivilegeException(sess, "getAssignedRichResources");
		}

		return getResourcesManagerBl().getAssignedRichResources(sess, member);
	}

	@Override
	public List<RichResource> getAssignedRichResources(PerunSession sess, Member member, Service service) throws PrivilegeException, MemberNotExistsException, ServiceNotExistsException {
		Utils.checkPerunSession(sess);

		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getPerunBl().getServicesManagerBl().checkServiceExists(sess, service);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getAssignedRichResources_Member_Service_policy", Arrays.asList(member, service))) {
			throw new PrivilegeException(sess, "getAssignedRichResources");
		}

		return getResourcesManagerBl().getAssignedRichResources(sess, member, service);
	}

	@Override
	public Resource updateResource(PerunSession sess, Resource resource) throws ResourceNotExistsException, PrivilegeException, ResourceExistsException {
		Utils.notNull(sess, "sess");
		resourcesManagerBl.checkResourceExists(sess, resource);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "updateResource_Resource_policy", resource)) {
			throw new PrivilegeException(sess, "updateResource");
		}

		return resourcesManagerBl.updateResource(sess, resource);
	}

	@Override
	public ResourceTag createResourceTag(PerunSession perunSession, ResourceTag resourceTag, Vo vo) throws PrivilegeException, VoNotExistsException {
		Utils.notNull(perunSession, "perunSession");
		Utils.notNull(resourceTag, "resourceTag");
		getPerunBl().getVosManagerBl().checkVoExists(perunSession, vo);

		// Authorization
		if (!AuthzResolver.authorizedInternal(perunSession, "createResourceTag_ResourceTag_Vo_policy", vo)) {
			throw new PrivilegeException(perunSession, "createResourceTag");
		}

		return resourcesManagerBl.createResourceTag(perunSession, resourceTag, vo);
	}

	@Override
	public ResourceTag updateResourceTag(PerunSession perunSession, ResourceTag resourceTag) throws PrivilegeException, ResourceTagNotExistsException, VoNotExistsException {
		Utils.notNull(perunSession, "perunSession");
		Utils.notNull(resourceTag, "resourceTag");
		getResourcesManagerBl().checkResourceTagExists(perunSession, resourceTag);

		// Authorization
		if (!AuthzResolver.authorizedInternal(perunSession, "updateResourceTag_ResourceTag_policy", resourceTag)) {
			throw new PrivilegeException(perunSession, "updateResourceTag");
		}

		return resourcesManagerBl.updateResourceTag(perunSession, resourceTag);
	}

	@Override
	public void deleteResourceTag(PerunSession perunSession, ResourceTag resourceTag) throws PrivilegeException, VoNotExistsException, ResourceTagAlreadyAssignedException, ResourceTagNotExistsException {
		Utils.notNull(perunSession, "perunSession");
		Utils.notNull(resourceTag, "resourceTag");
		getResourcesManagerBl().checkResourceTagExists(perunSession, resourceTag);

		// Authorization
		if (!AuthzResolver.authorizedInternal(perunSession, "deleteResourceTag_ResourceTag_policy", resourceTag)) {
			throw new PrivilegeException(perunSession, "deleteResourceTag");
		}
		resourcesManagerBl.deleteResourceTag(perunSession, resourceTag);
	}

	@Override
	public void deleteAllResourcesTagsForVo(PerunSession perunSession, Vo vo) throws PrivilegeException, VoNotExistsException, ResourceTagAlreadyAssignedException {
		Utils.notNull(perunSession, "perunSession");
		getPerunBl().getVosManagerBl().checkVoExists(perunSession, vo);

		// Authorization
		if (!AuthzResolver.authorizedInternal(perunSession, "deleteAllResourcesTagsForVo_Vo_policy", vo)) {
			throw new PrivilegeException(perunSession, "deleteAllResourcesTagsForVo");
		}

		resourcesManagerBl.deleteAllResourcesTagsForVo(perunSession, vo);
	}

	@Override
	public void assignResourceTagToResource(PerunSession perunSession, ResourceTag resourceTag, Resource resource) throws PrivilegeException, ResourceTagNotExistsException, ResourceNotExistsException, ResourceTagAlreadyAssignedException {
		Utils.notNull(perunSession, "perunSession");
		Utils.notNull(resourceTag, "resourceTag");
		resourcesManagerBl.checkResourceExists(perunSession, resource);
		resourcesManagerBl.checkResourceTagExists(perunSession, resourceTag);
		if(resourceTag.getVoId() != resource.getVoId()) throw new ConsistencyErrorException("ResourceTag is from other Vo than Resource to which you want to assign it.");

		// Authorization
		if (!AuthzResolver.authorizedInternal(perunSession, "assignResourceTagToResource_ResourceTag_Resource_policy", Arrays.asList(resource, resourceTag))) {
			throw new PrivilegeException(perunSession, "assignResourceTagToResource");
		}

		resourcesManagerBl.assignResourceTagToResource(perunSession, resourceTag, resource);
	}

	@Override
	public void removeResourceTagFromResource(PerunSession perunSession, ResourceTag resourceTag, Resource resource) throws PrivilegeException, ResourceTagNotExistsException, ResourceNotExistsException, ResourceTagNotAssignedException {
		Utils.notNull(perunSession, "perunSession");
		Utils.notNull(resourceTag, "resourceTag");
		resourcesManagerBl.checkResourceExists(perunSession, resource);
		resourcesManagerBl.checkResourceTagExists(perunSession, resourceTag);
		if(resourceTag.getVoId() != resource.getVoId()) throw new ConsistencyErrorException("ResourceTag is from other Vo than Resource to which you want to remove from.");

		// Authorization
		if (!AuthzResolver.authorizedInternal(perunSession, "removeResourceTagFromResource_ResourceTag_Resource_policy", Arrays.asList(resource, resourceTag))) {
			throw new PrivilegeException(perunSession, "removeResourceTagFromResource");
		}

		resourcesManagerBl.removeResourceTagFromResource(perunSession, resourceTag, resource);
	}

	@Override
	public void removeAllResourcesTagFromResource(PerunSession perunSession, Resource resource) throws PrivilegeException, ResourceNotExistsException {
		Utils.notNull(perunSession, "perunSession");
		resourcesManagerBl.checkResourceExists(perunSession, resource);

		// Authorization
		if (!AuthzResolver.authorizedInternal(perunSession, "removeAllResourcesTagFromResource_Resource_policy", resource)) {
			throw new PrivilegeException(perunSession, "removeAllResourcesTagFromResource");
		}

		resourcesManagerBl.removeAllResourcesTagFromResource(perunSession, resource);
	}

	@Override
	public List<Resource> getAllResourcesByResourceTag(PerunSession perunSession, ResourceTag resourceTag) throws PrivilegeException, VoNotExistsException, ResourceTagNotExistsException {
		Utils.notNull(perunSession, "perunSession");
		Utils.notNull(resourceTag, "resourceTag");
		resourcesManagerBl.checkResourceTagExists(perunSession, resourceTag);

		// Authorization
		if (!AuthzResolver.authorizedInternal(perunSession, "getAllResourcesByResourceTag_ResourceTag_policy", resourceTag)) {
			throw new PrivilegeException(perunSession, "getAllResourcesByResourceTag");
			//TODO: what about GROUPADMIN?
		}
		return resourcesManagerBl.getAllResourcesByResourceTag(perunSession, resourceTag);
	}

	@Override
	public List<ResourceTag> getAllResourcesTagsForVo(PerunSession perunSession, Vo vo) throws PrivilegeException, VoNotExistsException {
		Utils.notNull(perunSession, "perunSession");
		getPerunBl().getVosManagerBl().checkVoExists(perunSession, vo);

		// Authorization
		if (!AuthzResolver.authorizedInternal(perunSession, "getAllResourcesTagsForVo_Vo_policy", vo)) {
			throw new PrivilegeException(perunSession, "getAllResourcesTagsForVo");
			//TODO: what about GROUPADMIN?
				}

		return resourcesManagerBl.getAllResourcesTagsForVo(perunSession, vo);
	}

	@Override
	public List<ResourceTag> getAllResourcesTagsForResource(PerunSession perunSession, Resource resource) throws ResourceNotExistsException, PrivilegeException {
		Utils.notNull(perunSession, "perunSession");
		resourcesManagerBl.checkResourceExists(perunSession, resource);

		// Authorization
		if (!AuthzResolver.authorizedInternal(perunSession, "getAllResourcesTagsForResource_Resource_policy", resource)) {
			throw new PrivilegeException(perunSession, "getAllResourcesTagsForResource");
			//TODO: What about GROUPADMIN?
				}

		return resourcesManagerBl.getAllResourcesTagsForResource(perunSession, resource);
	}

	@Override
	public void copyAttributes(PerunSession sess, Resource sourceResource, Resource destinationResource) throws PrivilegeException, ResourceNotExistsException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);

		getResourcesManagerBl().checkResourceExists(sess, sourceResource);
		getResourcesManagerBl().checkResourceExists(sess, destinationResource);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "copyAttributes_Resource_Resource_policy", sourceResource) ||
			!AuthzResolver.authorizedInternal(sess, "copyAttributes_Resource_Resource_policy", destinationResource)) {
			throw new PrivilegeException(sess, "copyAttributes");
		}

		getResourcesManagerBl().copyAttributes(sess, sourceResource, destinationResource);
	}

	@Override
	public void copyServices(PerunSession sess, Resource sourceResource, Resource destinationResource) throws ResourceNotExistsException, PrivilegeException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);

		getResourcesManagerBl().checkResourceExists(sess, sourceResource);
		getResourcesManagerBl().checkResourceExists(sess, destinationResource);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "copyServices_Resource_Resource_policy", sourceResource) ||
			!AuthzResolver.authorizedInternal(sess, "copyServices_Resource_Resource_policy", destinationResource)) {
			throw new PrivilegeException(sess, "copyServices");
		}

		getResourcesManagerBl().copyServices(sess, sourceResource, destinationResource);
	}

	@Override
	public void copyGroups(PerunSession sess, Resource sourceResource, Resource destinationResource) throws ResourceNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		getResourcesManagerBl().checkResourceExists(sess, sourceResource);
		getResourcesManagerBl().checkResourceExists(sess, destinationResource);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "copyGroups_Resource_Resource_policy", sourceResource) ||
			!AuthzResolver.authorizedInternal(sess, "copyGroups_Resource_Resource_policy", destinationResource)) {
			throw new PrivilegeException(sess, "copyGroups");
		}

		getResourcesManagerBl().copyGroups(sess, sourceResource, destinationResource);
	}

	@Override
	public List<User> getAdmins(PerunSession perunSession, Resource resource, boolean onlyDirectAdmins) throws PrivilegeException, ResourceNotExistsException {
		Utils.checkPerunSession(perunSession);
		getResourcesManagerBl().checkResourceExists(perunSession, resource);

		// Authorization
		if (!AuthzResolver.authorizedInternal(perunSession, "getAdmins_Resource_boolean_policy", resource)) {
			throw new PrivilegeException(perunSession, "getAdmins");
		}

		return getResourcesManagerBl().getAdmins(perunSession, resource, onlyDirectAdmins);
	}

	@Override
	public List<RichUser> getRichAdmins(PerunSession perunSession, Resource resource, List<String> specificAttributes, boolean allUserAttributes, boolean onlyDirectAdmins) throws UserNotExistsException, PrivilegeException, ResourceNotExistsException {
		Utils.checkPerunSession(perunSession);
		getResourcesManagerBl().checkResourceExists(perunSession, resource);

		if(!allUserAttributes) Utils.notNull(specificAttributes, "specificAttributes");

		// Authorization
		if (!AuthzResolver.authorizedInternal(perunSession, "getRichAdmins_Resource_List<String>_boolean_boolean_policy", resource)) {
			throw new PrivilegeException(perunSession, "getRichAdmins");
		}

		return getPerunBl().getUsersManagerBl().filterOnlyAllowedAttributes(perunSession, getResourcesManagerBl().getRichAdmins(perunSession, resource, specificAttributes, allUserAttributes, onlyDirectAdmins));
	}

	@Override
	public List<Resource> getResourcesWhereUserIsAdmin(PerunSession sess, User user) throws UserNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);

		// Authorization
		if(!AuthzResolver.authorizedInternal(sess, "getResourcesWhereUserIsAdmin_User_policy", user)) {
			throw new PrivilegeException(sess, "getResourcesWhereUserIsAdmin");
		}

		return getResourcesManagerBl().getResourcesWhereUserIsAdmin(sess, user);
	}

	@Override
	public List<Resource> getResourcesWhereUserIsAdmin(PerunSession sess, Facility facility, Vo vo, User authorizedUser) throws PrivilegeException, UserNotExistsException, FacilityNotExistsException, VoNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, authorizedUser);

		//Authorization
		if(!AuthzResolver.authorizedInternal(sess, "getResourcesWhereUserIsAdmin_Facility_Vo_User_policy", facility, vo, authorizedUser)){
			throw new PrivilegeException(sess, "getResourcesByResourceManager");
		}
		List<Resource> resources = getResourcesManagerBl().getResourcesWhereUserIsAdmin(sess, facility, vo, authorizedUser);
		resources.removeIf(resource -> !AuthzResolver.authorizedInternal(sess, "filter-getResourcesWhereUserIsAdmin_Facility_Vo_User_policy", Arrays.asList(vo, facility, resource, authorizedUser)));

		return resources;
	}

	@Override
	public List<Resource> getResourcesWhereUserIsAdmin(PerunSession sess, Vo vo, User authorizedUser) throws PrivilegeException, UserNotExistsException, VoNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, authorizedUser);

		//Authorization
		if(!AuthzResolver.authorizedInternal(sess, "getResourcesWhereUserIsAdmin_Vo_User_policy", vo, authorizedUser)){
			throw new PrivilegeException(sess, "getResourcesWhereUserIsAdmin");
		}
		List<Resource> resources = getResourcesManagerBl().getResourcesWhereUserIsAdmin(sess, vo, authorizedUser);
		resources.removeIf(resource -> !AuthzResolver.authorizedInternal(sess, "filter-getResourcesWhereUserIsAdmin_Vo_User_policy", Arrays.asList(vo, resource, authorizedUser)));

		return resources;
	}

	@Override
	public List<Resource> getResourcesWhereGroupIsAdmin(PerunSession sess, Facility facility, Vo vo, Group authorizedGroup) throws PrivilegeException, GroupNotExistsException, FacilityNotExistsException, VoNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, authorizedGroup);

		//Authorization
		if(!AuthzResolver.authorizedInternal(sess, "getResourcesWhereGroupIsAdmin_Facility_Vo_Group_policy", Arrays.asList(facility, vo)) &&
			!AuthzResolver.authorizedInternal(sess, "authorizedGroup-getResourcesWhereGroupIsAdmin_Facility_Vo_Group_policy", authorizedGroup)){
			throw new PrivilegeException(sess, "getResourcesByResourceManager");
		}
		List<Resource> resources = getResourcesManagerBl().getResourcesWhereGroupIsAdmin(sess, facility, vo, authorizedGroup);
		resources.removeIf(resource -> !AuthzResolver.authorizedInternal(sess, "filter-getResourcesWhereGroupIsAdmin_Facility_Vo_Group_policy", Arrays.asList(vo, resource, facility)) &&
			!AuthzResolver.authorizedInternal(sess, "filter_authorizedGroup-getResourcesWhereGroupIsAdmin_Facility_Vo_Group_policy", authorizedGroup));

		return resources;
	}

	@Override
	public List<Group> getAdminGroups(PerunSession sess, Resource resource) throws ResourceNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);
		getResourcesManagerBl().checkResourceExists(sess, resource);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getAdminGroups_Resource_policy", resource)) {
			throw new PrivilegeException(sess, "getAdminGroups");
		}

		return getResourcesManagerBl().getAdminGroups(sess, resource);
	}

	@Override
	public void addAdmin(PerunSession sess, Resource resource, User user) throws UserNotExistsException, PrivilegeException, AlreadyAdminException, ResourceNotExistsException, RoleCannotBeManagedException {
		Utils.checkPerunSession(sess);
		getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);

		AuthzResolver.setRole(sess, user, resource, Role.RESOURCEADMIN);
	}

	@Override
	public void addAdmin(PerunSession sess, Resource resource, Group group) throws GroupNotExistsException, PrivilegeException, AlreadyAdminException, ResourceNotExistsException, RoleCannotBeManagedException {
		Utils.checkPerunSession(sess);
		getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);

		AuthzResolver.setRole(sess, group, resource, Role.RESOURCEADMIN);
	}

	@Override
	public void removeAdmin(PerunSession sess, Resource resource, User user) throws UserNotExistsException, PrivilegeException, UserNotAdminException, ResourceNotExistsException, RoleCannotBeManagedException {
		Utils.checkPerunSession(sess);
		getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);

		AuthzResolver.unsetRole(sess, user, resource, Role.RESOURCEADMIN);
	}

	@Override
	public void removeAdmin(PerunSession sess, Resource resource, Group group) throws GroupNotExistsException, PrivilegeException, GroupNotAdminException, ResourceNotExistsException, RoleCannotBeManagedException {
		Utils.checkPerunSession(sess);
		getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);

		AuthzResolver.unsetRole(sess, group, resource, Role.RESOURCEADMIN);
	}

	@Override
	public BanOnResource setBan(PerunSession sess, BanOnResource banOnResource) throws PrivilegeException, BanAlreadyExistsException, ResourceNotExistsException {
		Utils.checkPerunSession(sess);
		Utils.notNull(banOnResource, "banOnResource");

		Resource resource = getResourcesManagerBl().getResourceById(sess, banOnResource.getResourceId());

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "setBan_BanOnResource_policy", resource)) {
			throw new PrivilegeException(sess, "setBan");
		}

		return getResourcesManagerBl().setBan(sess, banOnResource);
	}

	@Override
	public BanOnResource getBanById(PerunSession sess, int banId) throws BanNotExistsException, PrivilegeException, ResourceNotExistsException {
		Utils.checkPerunSession(sess);

		BanOnResource ban = getResourcesManagerBl().getBanById(sess, banId);
		Resource resource = getResourcesManagerBl().getResourceById(sess, ban.getResourceId());

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "resource-getBanById_int_policy", Arrays.asList(resource, ban))) {
			throw new PrivilegeException(sess, "getBanById");
		}

		return ban;
	}

	@Override
	public BanOnResource getBan(PerunSession sess, int memberId, int resourceId) throws BanNotExistsException, PrivilegeException, MemberNotExistsException, ResourceNotExistsException {
		Utils.checkPerunSession(sess);
		Member member = getPerunBl().getMembersManagerBl().getMemberById(sess, memberId);
		Resource resource = getPerunBl().getResourcesManagerBl().getResourceById(sess, resourceId);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "resource-getBan_int_int_policy", Arrays.asList(member, resource))) {
			throw new PrivilegeException(sess, "getBan");
		}

		return getResourcesManagerBl().getBan(sess, memberId, resourceId);
	}

	@Override
	public List<BanOnResource> getBansForMember(PerunSession sess, int memberId) throws MemberNotExistsException, ResourceNotExistsException {
		Utils.checkPerunSession(sess);
		Member member = getPerunBl().getMembersManagerBl().getMemberById(sess, memberId);

		List<BanOnResource> usersBans = getResourcesManagerBl().getBansForMember(sess, memberId);

		//Authorization
		Iterator<BanOnResource> iterator = usersBans.iterator();
		while(iterator.hasNext()) {
			BanOnResource banForFiltering = iterator.next();
			Resource resource = getResourcesManagerBl().getResourceById(sess, banForFiltering.getResourceId());
			if(!AuthzResolver.authorizedInternal(sess, "getBansForMember_int_policy", Arrays.asList(banForFiltering, resource, member))) iterator.remove();
		}

		return usersBans;
	}

	@Override
	public List<BanOnResource> getBansForResource(PerunSession sess, int resourceId) throws PrivilegeException, ResourceNotExistsException {
		Utils.checkPerunSession(sess);
		Resource resource = getPerunBl().getResourcesManagerBl().getResourceById(sess, resourceId);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getBansForResource_int_policy", resource)) {
			throw new PrivilegeException(sess, "getBansForResource");
		}

		return getResourcesManagerBl().getBansForResource(sess, resourceId);
	}

	@Override
	public BanOnResource updateBan(PerunSession sess, BanOnResource banOnResource) throws PrivilegeException, MemberNotExistsException, BanNotExistsException, ResourceNotExistsException {
		Utils.checkPerunSession(sess);
		this.getResourcesManagerBl().checkBanExists(sess, banOnResource.getId());
		Member member = getPerunBl().getMembersManagerBl().getMemberById(sess, banOnResource.getMemberId());
		Resource resource = getPerunBl().getResourcesManagerBl().getResourceById(sess, banOnResource.getResourceId());

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "updateBan_BanOnResource_policy", Arrays.asList(banOnResource, member, resource))) {
			throw new PrivilegeException(sess, "updateBan");
		}

		banOnResource = getResourcesManagerBl().updateBan(sess, banOnResource);
		return banOnResource;
	}

	@Override
	public void removeBan(PerunSession sess, int banId) throws PrivilegeException, BanNotExistsException, ResourceNotExistsException {
		Utils.checkPerunSession(sess);
		BanOnResource ban = this.getResourcesManagerBl().getBanById(sess, banId);

		Resource resource = getResourcesManagerBl().getResourceById(sess, ban.getResourceId());

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "resource-removeBan_int_policy", Arrays.asList(ban, resource))) {
			throw new PrivilegeException(sess, "removeBan");
		}

		getResourcesManagerBl().removeBan(sess, banId);
	}

	@Override
	public void removeBan(PerunSession sess, int memberId, int resourceId) throws BanNotExistsException, PrivilegeException, ResourceNotExistsException {
		Utils.checkPerunSession(sess);
		BanOnResource ban = this.getResourcesManagerBl().getBan(sess, memberId, resourceId);

		Resource resource = getResourcesManagerBl().getResourceById(sess, ban.getResourceId());

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "resource-removeBan_int_int_policy", Arrays.asList(ban, resource))) {
			throw new PrivilegeException(sess, "removeBan");
		}

		getResourcesManagerBl().removeBan(sess, memberId, resourceId);
	}

	@Override
	public void addResourceSelfServiceUser(PerunSession sess, Resource resource, User user) throws PrivilegeException, AlreadyAdminException, ResourceNotExistsException, UserNotExistsException {
		Utils.checkPerunSession(sess);

		getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);

		//Authorization
		if (!AuthzResolver.authorizedInternal(sess, "addResourceSelfServiceUser_Resource_User_policy", Arrays.asList(resource, user))) {
			throw new PrivilegeException(sess, "addResourceSelfServiceUser");
		}

		getResourcesManagerBl().addResourceSelfServiceUser(sess, resource, user);
	}

	@Override
	public void addResourceSelfServiceGroup(PerunSession sess, Resource resource, Group group) throws PrivilegeException, AlreadyAdminException, ResourceNotExistsException, GroupNotExistsException {
		Utils.checkPerunSession(sess);

		getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);

		//Authorization
		if (!AuthzResolver.authorizedInternal(sess, "addResourceSelfServiceGroup_Resource_Group_policy", resource) &&
		!AuthzResolver.authorizedInternal(sess, "group-addResourceSelfServiceGroup_Resource_Group_policy", group)) {
			throw new PrivilegeException(sess, "addResourceSelfServiceGroup");
		}

		getResourcesManagerBl().addResourceSelfServiceGroup(sess, resource, group);
	}

	@Override
	public void removeResourceSelfServiceUser(PerunSession sess, Resource resource, User user) throws PrivilegeException, UserNotAdminException, ResourceNotExistsException, UserNotExistsException {
		Utils.checkPerunSession(sess);

		getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);

		//Authorization
		if (!AuthzResolver.authorizedInternal(sess, "removeResourceSelfServiceUser_Resource_User_policy", Arrays.asList(resource, user))) {
			throw new PrivilegeException(sess, "removeResourceSelfServiceUser");
		}

		getResourcesManagerBl().removeResourceSelfServiceUser(sess, resource, user);
	}

	@Override
	public void removeResourceSelfServiceGroup(PerunSession sess, Resource resource, Group group) throws PrivilegeException, GroupNotAdminException, ResourceNotExistsException, GroupNotExistsException {
		Utils.checkPerunSession(sess);

		getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);

		//Authorization
		if (!AuthzResolver.authorizedInternal(sess, "removeResourceSelfServiceGroup_Resource_Group_policy", resource) &&
			!AuthzResolver.authorizedInternal(sess, "group-removeResourceSelfServiceGroup_Resource_Group_policy", group)) {
			throw new PrivilegeException(sess, "removeResourceSelfServiceGroup");
		}

		getResourcesManagerBl().removeResourceSelfServiceGroup(sess, resource, group);
	}

	@Override
	public List<AssignedResource> getResourceAssignments(PerunSession sess, Group group, List<String> attrNames) throws PrivilegeException, GroupNotExistsException {
		Utils.checkPerunSession(sess);

		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getResourceAssignments_Group_policy", group)) {
			throw new PrivilegeException(sess, "getResourceAssignments");
		}

		List<AssignedResource> filteredResources = getResourcesManagerBl().getResourceAssignments(sess, group, attrNames).stream()
			.filter(assignedResource -> AuthzResolver.authorizedInternal(sess,
				"filter-getResourceAssignments_Group_policy", assignedResource.getEnrichedResource().getResource()))
			.collect(Collectors.toList());

		filteredResources.forEach(assignedResource ->
			assignedResource.setEnrichedResource(getResourcesManagerBl().filterOnlyAllowedAttributes(sess, assignedResource.getEnrichedResource())));

		return filteredResources;
	}

	@Override
	public List<AssignedGroup> getGroupAssignments(PerunSession sess, Resource resource, List<String> attrNames) throws PrivilegeException, ResourceNotExistsException {
		Utils.checkPerunSession(sess);

		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getGroupAssignments_Resource_policy", resource)) {
			throw new PrivilegeException(sess, "getGroupAssignments");
		}

		List<AssignedGroup> filteredGroups =  getResourcesManagerBl().getGroupAssignments(sess, resource, attrNames).stream()
			.filter(assignedGroup -> AuthzResolver.authorizedInternal(sess,
				"filter-getGroupAssignments_Resource_policy", assignedGroup.getEnrichedGroup().getGroup()))
			.collect(Collectors.toList());

		filteredGroups.forEach(assignedGroup ->
			assignedGroup.setEnrichedGroup(getPerunBl().getGroupsManagerBl().filterOnlyAllowedAttributes(sess, assignedGroup.getEnrichedGroup())));

		return filteredGroups;
	}

	@Override
	public void activateGroupResourceAssignment(PerunSession sess, Group group, Resource resource, boolean async) throws ResourceNotExistsException, GroupNotExistsException, PrivilegeException, WrongReferenceAttributeValueException, GroupNotDefinedOnResourceException, GroupResourceMismatchException, WrongAttributeValueException {
		Utils.checkPerunSession(sess);
		getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "activateGroupResourceAssignment_Group_Resource_boolean_policy", group, resource)) {
			throw new PrivilegeException(sess, "activateGroupResourceAssignment");
		}

		getResourcesManagerBl().activateGroupResourceAssignment(sess, group, resource, async);
	}

	@Override
	public void deactivateGroupResourceAssignment(PerunSession sess, Group group, Resource resource) throws PrivilegeException, ResourceNotExistsException, GroupNotExistsException, GroupNotDefinedOnResourceException, GroupResourceStatusException {
		Utils.checkPerunSession(sess);
		getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "deactivateGroupResourceAssignment_Group_Resource_policy", group, resource)) {
			throw new PrivilegeException(sess, "deactivateGroupResourceAssignment");
		}

		getResourcesManagerBl().deactivateGroupResourceAssignment(sess, group, resource);
	}

	/**
	 * Gets the resourcesManagerBl for this instance.
	 *
	 * @return The resourcesManagerBl.
	 */
	public ResourcesManagerBl getResourcesManagerBl() {
		return this.resourcesManagerBl;
	}

	/**
	 * Sets the perunBl for this instance.
	 *
	 * @param perunBl The perunBl.
	 */
	public void setPerunBl(PerunBl perunBl)
	{
		this.perunBl = perunBl;
	}

	/**
	 * Sets the resourcesManagerBl for this instance.
	 *
	 * @param resourcesManagerBl The resourcesManagerBl.
	 */
	public void setResourcesManagerBl(ResourcesManagerBl resourcesManagerBl)
	{
		this.resourcesManagerBl = resourcesManagerBl;
	}

	public PerunBl getPerunBl() {
		return this.perunBl;
	}


}
