package cz.metacentrum.perun.core.entry;

import cz.metacentrum.perun.core.api.AuthzResolver;
import cz.metacentrum.perun.core.api.BanOnResource;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
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
import cz.metacentrum.perun.core.api.exceptions.GroupAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.GroupAlreadyRemovedFromResourceException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotDefinedOnResourceException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.ResourceAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.ResourceExistsException;
import cz.metacentrum.perun.core.api.exceptions.ResourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ResourceTagAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.ResourceTagNotAssignedException;
import cz.metacentrum.perun.core.api.exceptions.ResourceTagNotExistsException;
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
import java.util.Iterator;
import java.util.List;

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
	public Resource getResourceById(PerunSession sess, int id) throws InternalErrorException, PrivilegeException, ResourceNotExistsException {
		Utils.checkPerunSession(sess);

		Resource resource = getResourcesManagerBl().getResourceById(sess, id);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, resource) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, resource) &&
				!AuthzResolver.isAuthorized(sess, Role.RPC) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getResourceById");
				}

		return resource;
	}

	@Override
	public RichResource getRichResourceById(PerunSession sess, int id) throws InternalErrorException, PrivilegeException, ResourceNotExistsException {
		Utils.checkPerunSession(sess);

		RichResource rr = getResourcesManagerBl().getRichResourceById(sess, id);
                Vo richResourceVo = rr.getVo();
                Facility richResourceFacility = rr.getFacility();

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, richResourceVo) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, richResourceVo) &&
				!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, richResourceFacility) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getRichResourceById");
				}

		return rr;

	}

	@Override
	public Resource getResourceByName(PerunSession sess, Vo vo, Facility facility, String name) throws InternalErrorException, PrivilegeException,
				 ResourceNotExistsException, VoNotExistsException, FacilityNotExistsException {
					 Utils.checkPerunSession(sess);

					 getPerunBl().getVosManagerBl().checkVoExists(sess, vo);
					 getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);

					 Resource resource = getResourcesManagerBl().getResourceByName(sess, vo, facility, name);

					 // Authorization
					 if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
							 !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo) &&
							 !AuthzResolver.isAuthorized(sess, Role.RPC) &&
						 	!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
						 throw new PrivilegeException(sess, "getResourceByName");
							 }

					 return resource;
	}

	@Override
	public Resource createResource(PerunSession sess, Resource resource, Vo vo, Facility facility) throws InternalErrorException, PrivilegeException, VoNotExistsException, FacilityNotExistsException, ResourceExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility)) {
			throw new PrivilegeException(sess, "createResource");
				}

		return getResourcesManagerBl().createResource(sess, resource, vo, facility);
	}

	@Override
	public Resource copyResource(PerunSession sess, Resource templateResource, Resource destinationResource, boolean withGroups) throws InternalErrorException, ResourceNotExistsException, PrivilegeException, ResourceExistsException {
		Utils.checkPerunSession(sess);
		Utils.notNull(templateResource, "Template Resource");
		Utils.notNull(destinationResource, "Destination Resource");
		getResourcesManagerBl().checkResourceExists(sess, templateResource);

		//check if user is facility admin of the template resource
		if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, templateResource)) {
			throw new PrivilegeException(sess, "User is not facility admin of template Resource's facility.");
		}

		//check if user is facility admin of the destination resource
		if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, destinationResource)) {
			throw new PrivilegeException(sess, "User is not facility admin of destination Resource's facility.");
		}

		if(withGroups) {
			if(destinationResource.getVoId() != templateResource.getVoId()) {
				throw new InternalErrorException("Resources are not from the same VO.");
			}

			if(!AuthzResolver.isAuthorized(sess, Role.VOADMIN, templateResource)) {
				throw new PrivilegeException(sess, "User needs vo admin rights for copying the groups and group related attributes.");
			}
		}

		return getResourcesManagerBl().copyResource(sess, templateResource, destinationResource, withGroups);
	}

	@Override
	public void deleteResource(PerunSession sess, Resource resource) throws InternalErrorException, ResourceNotExistsException, PrivilegeException, ResourceAlreadyRemovedException, GroupAlreadyRemovedFromResourceException, FacilityNotExistsException {
		Utils.checkPerunSession(sess);
		Facility facility = getPerunBl().getFacilitiesManagerBl().getFacilityById(sess, resource.getFacilityId());

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, resource) &&
				!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility)) {
			throw new PrivilegeException(sess, "deleteResource");
				}

		getResourcesManagerBl().checkResourceExists(sess, resource);

		getResourcesManagerBl().deleteResource(sess, resource);
	}

	@Override
	public void deleteAllResources(PerunSession sess, Vo vo) throws InternalErrorException, VoNotExistsException, PrivilegeException, ResourceAlreadyRemovedException, GroupAlreadyRemovedFromResourceException {
		Utils.checkPerunSession(sess);

		//Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo)) {
			throw new PrivilegeException(sess, "deleteAllResources");
		}

		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		getResourcesManagerBl().deleteAllResources(sess, vo);
	}

	@Override
	public Facility getFacility(PerunSession sess, Resource resource) throws InternalErrorException, ResourceNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);
		getResourcesManagerBl().checkResourceExists(sess, resource);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, resource) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, resource) &&
				!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, resource) &&
				!AuthzResolver.isAuthorized(sess, Role.ENGINE) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getFacility");
				}

		return getResourcesManagerBl().getFacility(sess, resource);
	}

	@Override
	public Vo getVo(PerunSession sess, Resource resource) throws InternalErrorException, ResourceNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);
		getResourcesManagerBl().checkResourceExists(sess, resource);
		Vo vo = getPerunBl().getResourcesManagerBl().getVo(sess, resource);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.ENGINE) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getVo");
				}

		return vo;
	}

	@Override
	public List<Member> getAllowedMembers(PerunSession sess, Resource resource) throws InternalErrorException, ResourceNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, resource) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, resource) &&
				!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, resource) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getAllowedMembers");
		}

		return getResourcesManagerBl().getAllowedMembers(sess, resource);
	}

	@Override
	public List<User> getAllowedUsers(PerunSession sess, Resource resource) throws InternalErrorException, ResourceNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, resource) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, resource) &&
				!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, resource) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getAllowedUsers");
		}

		return getResourcesManagerBl().getAllowedUsers(sess, resource);
	}

	@Override
	public List<Service> getAssignedServices(PerunSession sess, Resource resource) throws InternalErrorException, ResourceNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);
		getResourcesManagerBl().checkResourceExists(sess, resource);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, resource) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, resource) &&
				!AuthzResolver.isAuthorized(sess, Role.ENGINE) &&
				!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, resource) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getAssignedServices");
				}

		return getResourcesManagerBl().getAssignedServices(sess, resource);
	}

	@Override
	public List<Member> getAssignedMembers(PerunSession sess, Resource resource) throws InternalErrorException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, resource) &&
			!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, resource) &&
			!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, resource) &&
			!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER))
			{
			throw new PrivilegeException(sess, "getAssignedMembers");
		}

		return getResourcesManagerBl().getAssignedMembers(sess, resource);
	}

	@Override
	public List<RichMember> getAssignedRichMembers(PerunSession sess, Resource resource) throws InternalErrorException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, resource) &&
			!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, resource) &&
			!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, resource) &&
			!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER))
			{
			throw new PrivilegeException(sess, "getAssignedRichMembers");
		}

		return getResourcesManagerBl().getAssignedRichMembers(sess, resource);
	}

	@Override
	public void assignGroupToResource(PerunSession sess, Group group, Resource resource) throws InternalErrorException, PrivilegeException, GroupNotExistsException, ResourceNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException, GroupAlreadyAssignedException {
		Utils.checkPerunSession(sess);
		getResourcesManagerBl().checkResourceExists(sess, resource);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, resource) &&
				!AuthzResolver.isAuthorized(sess, Role.RESOURCEADMIN, resource)) {

			if (!AuthzResolver.isAuthorized(sess, Role.RESOURCESELFSERVICE, resource) ||
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)) {

				throw new PrivilegeException(sess, "assignGroupToResource");
			}
		}

		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);

		getResourcesManagerBl().assignGroupToResource(sess, group, resource);
	}

	@Override
	public void assignGroupsToResource(PerunSession perunSession, List<Group> groups, Resource resource) throws InternalErrorException, PrivilegeException, GroupNotExistsException, ResourceNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException, GroupAlreadyAssignedException {
		Utils.checkPerunSession(perunSession);
		Utils.notNull(groups, "groups");
		getResourcesManagerBl().checkResourceExists(perunSession, resource);

		// Authorization
		if (!AuthzResolver.isAuthorized(perunSession, Role.VOADMIN, resource) &&
				!AuthzResolver.isAuthorized(perunSession, Role.RESOURCEADMIN, resource)) {

			if (!AuthzResolver.isAuthorized(perunSession, Role.RESOURCESELFSERVICE, resource)) {
				throw new PrivilegeException(perunSession, "assignGroupsToResource");
			}

			for (Group group : groups) {
				if (!AuthzResolver.isAuthorized(perunSession, Role.GROUPADMIN, group)) {
					throw new PrivilegeException(perunSession, "assignGroupsToResource");
				}
			}
		}

		for(Group g: groups) {
			getPerunBl().getGroupsManagerBl().checkGroupExists(perunSession, g);
		}

		getResourcesManagerBl().assignGroupsToResource(perunSession, groups, resource);
	}

	@Override
	public void assignGroupToResources(PerunSession perunSession, Group group, List<Resource> resources) throws InternalErrorException, PrivilegeException, GroupNotExistsException, ResourceNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException, GroupAlreadyAssignedException {
		Utils.checkPerunSession(perunSession);
		Utils.notNull(resources, "resources");
		getPerunBl().getGroupsManagerBl().checkGroupExists(perunSession, group);

		// Authorization
		if (!AuthzResolver.isAuthorized(perunSession, Role.VOADMIN, group)) {
			throw new PrivilegeException(perunSession, "assignGroupToResources");
		}

		for(Resource r: resources) {
			getResourcesManagerBl().checkResourceExists(perunSession, r);
		}

		getResourcesManagerBl().assignGroupToResources(perunSession, group, resources);
	}

	@Override
	public void removeGroupFromResource(PerunSession sess, Group group, Resource resource) throws InternalErrorException, PrivilegeException, GroupNotExistsException, ResourceNotExistsException, GroupNotDefinedOnResourceException, GroupAlreadyRemovedFromResourceException {
		Utils.checkPerunSession(sess);
		getResourcesManagerBl().checkResourceExists(sess, resource);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, resource) &&
			!AuthzResolver.isAuthorized(sess, Role.RESOURCEADMIN, resource)) {

			if (!AuthzResolver.isAuthorized(sess, Role.RESOURCESELFSERVICE, resource) ||
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)) {

				throw new PrivilegeException(sess, "removeGroupFromResource");
			}
		}

		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);

		getResourcesManagerBl().removeGroupFromResource(sess, group, resource);
	}

	@Override
	public void removeGroupsFromResource(PerunSession perunSession, List<Group> groups, Resource resource) throws InternalErrorException, PrivilegeException, GroupNotExistsException, ResourceNotExistsException, GroupNotDefinedOnResourceException, GroupAlreadyRemovedFromResourceException {
		Utils.checkPerunSession(perunSession);
		Utils.notNull(groups, "groups");
		getResourcesManagerBl().checkResourceExists(perunSession, resource);

		// Authorization
		if (!AuthzResolver.isAuthorized(perunSession, Role.VOADMIN, resource) &&
				!AuthzResolver.isAuthorized(perunSession, Role.RESOURCEADMIN, resource)) {

			if (!AuthzResolver.isAuthorized(perunSession, Role.RESOURCESELFSERVICE, resource)) {
				throw new PrivilegeException(perunSession, "removeGroupsFromResource");
			}

			for (Group group : groups) {
				if (!AuthzResolver.isAuthorized(perunSession, Role.GROUPADMIN, group)) {
					throw new PrivilegeException(perunSession, "removeGroupsFromResource");
				}
			}
		}

		for(Group g: groups) {
			getPerunBl().getGroupsManagerBl().checkGroupExists(perunSession, g);
		}

		getResourcesManagerBl().removeGroupsFromResource(perunSession, groups, resource);
	}

	@Override
	public void removeGroupFromResources(PerunSession perunSession, Group group, List<Resource> resources) throws InternalErrorException, PrivilegeException, GroupNotExistsException, ResourceNotExistsException, GroupNotDefinedOnResourceException, GroupAlreadyRemovedFromResourceException {
		Utils.checkPerunSession(perunSession);
		Utils.notNull(resources, "resources");
		getPerunBl().getGroupsManagerBl().checkGroupExists(perunSession, group);

		// Authorization
		if (!AuthzResolver.isAuthorized(perunSession, Role.VOADMIN, group)) {
			throw new PrivilegeException(perunSession, "removeGroupFromResources");
		}

		for(Resource r: resources) {
			getResourcesManagerBl().checkResourceExists(perunSession, r);
		}

		getResourcesManagerBl().removeGroupFromResources(perunSession, group, resources);
	}

	@Override
	public List<Group> getAssignedGroups(PerunSession sess, Resource resource) throws InternalErrorException, PrivilegeException, ResourceNotExistsException {
		Utils.checkPerunSession(sess);
		getResourcesManagerBl().checkResourceExists(sess, resource);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, resource) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, resource) &&
				!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, resource) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getAssignedGroups");
				}

		return getResourcesManagerBl().getAssignedGroups(sess, resource);
	}

	@Override
	public List<Group> getAssignedGroups(PerunSession sess, Resource resource, Member member) throws InternalErrorException, PrivilegeException, ResourceNotExistsException {
		Utils.checkPerunSession(sess);
		getResourcesManagerBl().checkResourceExists(sess, resource);

		Facility facility = getPerunBl().getResourcesManagerBl().getFacility(sess, resource);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, resource) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, resource) &&
				!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getAssignedGroups");
		}

		return getResourcesManagerBl().getAssignedGroups(sess, resource, member);
	}

	@Override
	public List<Resource> getAssignedResources(PerunSession sess, Group group) throws InternalErrorException, GroupNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group) &&
				!AuthzResolver.isAuthorized(sess, Role.ENGINE) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, group) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getAssignedResources");
				}

		return getResourcesManagerBl().getAssignedResources(sess, group);
	}

	@Override
	public List<RichResource> getAssignedRichResources(PerunSession sess, Group group) throws InternalErrorException, GroupNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, group) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getAssignedRichResources");
				}

		return getResourcesManagerBl().getAssignedRichResources(sess, group);
	}

	@Override
	public void assignService(PerunSession sess, Resource resource, Service service) throws InternalErrorException, PrivilegeException, ResourceNotExistsException, ServiceNotExistsException, ServiceAlreadyAssignedException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getResourcesManagerBl().checkResourceExists(sess, resource);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, resource)) {
			throw new PrivilegeException(sess, "assignService");
		}

		getPerunBl().getServicesManagerBl().checkServiceExists(sess, service);

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
		if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, resource)) {
			throw new PrivilegeException(sess, "assignServices");
		}

		getResourcesManagerBl().assignServices(sess, resource, services);
	}

	@Override
	public void assignServicesPackage(PerunSession sess, Resource resource, ServicesPackage servicesPackage) throws InternalErrorException, PrivilegeException, ResourceNotExistsException, ServicesPackageNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
			throw new PrivilegeException(sess, "assignServicesPackage");
		}

		getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getServicesManagerBl().checkServicesPackageExists(sess, servicesPackage);

		getResourcesManagerBl().assignServicesPackage(sess, resource, servicesPackage);
	}

	@Override
	public void removeService(PerunSession sess, Resource resource, Service service) throws InternalErrorException, PrivilegeException, ResourceNotExistsException, ServiceNotExistsException, ServiceNotAssignedException {
		Utils.checkPerunSession(sess);
		getResourcesManagerBl().checkResourceExists(sess, resource);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, resource)) {
			throw new PrivilegeException(sess, "removeServices");
		}

		getPerunBl().getServicesManagerBl().checkServiceExists(sess, service);

		getResourcesManagerBl().removeService(sess, resource, service);
	}

	@Override
	public void removeServicesPackage(PerunSession sess, Resource resource, ServicesPackage servicesPackage) throws InternalErrorException, PrivilegeException, ResourceNotExistsException, ServicesPackageNotExistsException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
			throw new PrivilegeException(sess, "removeServicesPackage");
		}

		getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getServicesManagerBl().checkServicesPackageExists(sess, servicesPackage);

		getResourcesManagerBl().removeServicesPackage(sess, resource, servicesPackage);
	}

	@Override
	public List<Resource> getResources(PerunSession sess, Vo vo) throws InternalErrorException, PrivilegeException, VoNotExistsException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.RESOURCEADMIN) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getResources");
		}

		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		List<Resource> resources = getResourcesManagerBl().getResources(sess, vo);

		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			List<Resource> allowedResources = new ArrayList<>();
			for (Resource resource : resources) {
				if (AuthzResolver.isAuthorized(sess, Role.RESOURCEADMIN, resource)) {
					allowedResources.add(resource);
				}
			}
			resources = allowedResources;
		}

		return resources;
	}

	@Override
	public List<RichResource> getRichResources(PerunSession sess, Vo vo) throws InternalErrorException, PrivilegeException, VoNotExistsException {
		Utils.checkPerunSession(sess);

		// Authorization
		if ((!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo)) &&
				!AuthzResolver.isAuthorized(sess, Role.RESOURCEADMIN) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getRichResources");
		}

		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		List<RichResource> resources = getResourcesManagerBl().getRichResources(sess, vo);

		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			List<RichResource> allowedResources = new ArrayList<>();
			for (RichResource resource : resources) {
				if (AuthzResolver.isAuthorized(sess, Role.RESOURCEADMIN, resource)) {
					allowedResources.add(resource);
				}
			}
			resources = allowedResources;
		}

		return resources;
	}

	@Override
	public int getResourcesCount(PerunSession sess, Vo vo) throws InternalErrorException, PrivilegeException, VoNotExistsException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getResourcesCount");
		}

		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		return getResourcesManagerBl().getResourcesCount(sess, vo);
	}

	@Override
	public int getResourcesCount(PerunSession sess) throws InternalErrorException {
		Utils.checkPerunSession(sess);

		return getResourcesManagerBl().getResourcesCount(sess);
	}

	@Override
	public List<Resource> getAllowedResources(PerunSession sess, Member member) throws InternalErrorException, MemberNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, member) && !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, member)
				&& !AuthzResolver.isAuthorized(sess, Role.ENGINE) && !AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)
				&& !AuthzResolver.isAuthorized(sess, Role.SELF, member)) {
			throw new PrivilegeException(sess, "getAllowedResources");
				}

		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);

		return getResourcesManagerBl().getAllowedResources(sess, member);
	}

	@Override
	public List<Resource> getAssignedResources(PerunSession sess, Member member) throws InternalErrorException, PrivilegeException, MemberNotExistsException {
		Utils.checkPerunSession(sess);

		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		Vo vo = getPerunBl().getMembersManagerBl().getMemberVo(sess, member);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, member) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, member) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.SELF, member) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getAssignedResources");
				}

		return getResourcesManagerBl().getAssignedResources(sess, member);
	}

	@Override
	public List<Resource> getAssignedResources(PerunSession sess, Member member, Service service) throws InternalErrorException, PrivilegeException, MemberNotExistsException, ServiceNotExistsException {
		Utils.checkPerunSession(sess);

		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getPerunBl().getServicesManagerBl().checkServiceExists(sess, service);
		Vo vo = getPerunBl().getMembersManagerBl().getMemberVo(sess, member);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, member) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, member) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.SELF, member) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getAssignedResources");
				}

		return getResourcesManagerBl().getAssignedResources(sess, member, service);
	}

	@Override
	public List<RichResource> getAssignedRichResources(PerunSession sess, Member member) throws InternalErrorException, PrivilegeException, MemberNotExistsException {
		Utils.checkPerunSession(sess);

		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		Vo vo = getPerunBl().getMembersManagerBl().getMemberVo(sess, member);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, member) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, member) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.SELF, member) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getAssignedRichResources");
				}

		return getResourcesManagerBl().getAssignedRichResources(sess, member);
	}

	@Override
	public List<RichResource> getAssignedRichResources(PerunSession sess, Member member, Service service) throws InternalErrorException, PrivilegeException, MemberNotExistsException, ServiceNotExistsException {
		Utils.checkPerunSession(sess);

		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getPerunBl().getServicesManagerBl().checkServiceExists(sess, service);
		Vo vo = getPerunBl().getMembersManagerBl().getMemberVo(sess, member);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, member) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, member) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.SELF, member) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getAssignedRichResources");
				}

		return getResourcesManagerBl().getAssignedRichResources(sess, member, service);
	}

	@Override
	public Resource updateResource(PerunSession sess, Resource resource) throws ResourceNotExistsException, InternalErrorException, PrivilegeException, ResourceExistsException {
		Utils.notNull(sess, "sess");
		resourcesManagerBl.checkResourceExists(sess, resource);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, resource)) {
			throw new PrivilegeException(sess, "updateResource");
		}

		return resourcesManagerBl.updateResource(sess, resource);
	}

	@Override
	public ResourceTag createResourceTag(PerunSession perunSession, ResourceTag resourceTag, Vo vo) throws InternalErrorException, PrivilegeException, VoNotExistsException {
		Utils.notNull(perunSession, "perunSession");
		Utils.notNull(resourceTag, "resourceTag");
		getPerunBl().getVosManagerBl().checkVoExists(perunSession, vo);

		if (!AuthzResolver.isAuthorized(perunSession, Role.VOADMIN, vo)) {
			throw new PrivilegeException(perunSession, "createResourceTag");
		}

		return resourcesManagerBl.createResourceTag(perunSession, resourceTag, vo);
	}

	@Override
	public ResourceTag updateResourceTag(PerunSession perunSession, ResourceTag resourceTag) throws InternalErrorException, PrivilegeException, ResourceTagNotExistsException, VoNotExistsException {
		Utils.notNull(perunSession, "perunSession");
		Utils.notNull(resourceTag, "resourceTag");
		getResourcesManagerBl().checkResourceTagExists(perunSession, resourceTag);
		Vo vo = getPerunBl().getVosManagerBl().getVoById(perunSession, resourceTag.getVoId());

		if (!AuthzResolver.isAuthorized(perunSession, Role.VOADMIN, vo)) {
			throw new PrivilegeException(perunSession, "updateResourceTag");
		}

		return resourcesManagerBl.updateResourceTag(perunSession, resourceTag);
	}

	@Override
	public void deleteResourceTag(PerunSession perunSession, ResourceTag resourceTag) throws InternalErrorException, PrivilegeException, VoNotExistsException, ResourceTagAlreadyAssignedException {
		Utils.notNull(perunSession, "perunSession");
		Utils.notNull(resourceTag, "resourceTag");
		Vo vo = getPerunBl().getVosManagerBl().getVoById(perunSession, resourceTag.getVoId());

		if (!AuthzResolver.isAuthorized(perunSession, Role.VOADMIN, vo)) {
			throw new PrivilegeException(perunSession, "deleteResourceTag");
		}
		resourcesManagerBl.deleteResourceTag(perunSession, resourceTag);
	}

	@Override
	public void deleteAllResourcesTagsForVo(PerunSession perunSession, Vo vo) throws InternalErrorException, PrivilegeException, VoNotExistsException, ResourceTagAlreadyAssignedException {
		Utils.notNull(perunSession, "perunSession");
		getPerunBl().getVosManagerBl().checkVoExists(perunSession, vo);

		if (!AuthzResolver.isAuthorized(perunSession, Role.VOADMIN, vo)) {
			throw new PrivilegeException(perunSession, "deleteAllResourcesTagsForVo");
		}
		resourcesManagerBl.deleteAllResourcesTagsForVo(perunSession, vo);
	}

	@Override
	public void assignResourceTagToResource(PerunSession perunSession, ResourceTag resourceTag, Resource resource) throws InternalErrorException, PrivilegeException, ResourceTagNotExistsException, ResourceNotExistsException, ResourceTagAlreadyAssignedException {
		Utils.notNull(perunSession, "perunSession");
		Utils.notNull(resourceTag, "resourceTag");
		resourcesManagerBl.checkResourceExists(perunSession, resource);
		resourcesManagerBl.checkResourceTagExists(perunSession, resourceTag);
		if(resourceTag.getVoId() != resource.getVoId()) throw new ConsistencyErrorException("ResourceTag is from other Vo than Resource to which you want to assign it.");

		if (!AuthzResolver.isAuthorized(perunSession, Role.VOADMIN, resource)) {
			throw new PrivilegeException(perunSession, "assignResourceTagToResource");
		}
		resourcesManagerBl.assignResourceTagToResource(perunSession, resourceTag, resource);
	}

	@Override
	public void removeResourceTagFromResource(PerunSession perunSession, ResourceTag resourceTag, Resource resource) throws InternalErrorException, PrivilegeException, ResourceTagNotExistsException, ResourceNotExistsException, ResourceTagNotAssignedException {
		Utils.notNull(perunSession, "perunSession");
		Utils.notNull(resourceTag, "resourceTag");
		resourcesManagerBl.checkResourceExists(perunSession, resource);
		resourcesManagerBl.checkResourceTagExists(perunSession, resourceTag);
		if(resourceTag.getVoId() != resource.getVoId()) throw new ConsistencyErrorException("ResourceTag is from other Vo than Resource to which you want to remove from.");

		if (!AuthzResolver.isAuthorized(perunSession, Role.VOADMIN, resource)) {
			throw new PrivilegeException(perunSession, "removeResourceTagFromResource");
		}
		resourcesManagerBl.removeResourceTagFromResource(perunSession, resourceTag, resource);
	}

	@Override
	public void removeAllResourcesTagFromResource(PerunSession perunSession, Resource resource) throws InternalErrorException, PrivilegeException, ResourceNotExistsException {
		Utils.notNull(perunSession, "perunSession");
		resourcesManagerBl.checkResourceExists(perunSession, resource);

		if (!AuthzResolver.isAuthorized(perunSession, Role.VOADMIN, resource)) {
			throw new PrivilegeException(perunSession, "removeAllResourcesTagFromResource");
		}
		resourcesManagerBl.removeAllResourcesTagFromResource(perunSession, resource);
	}

	@Override
	public List<Resource> getAllResourcesByResourceTag(PerunSession perunSession, ResourceTag resourceTag) throws InternalErrorException, PrivilegeException, VoNotExistsException, ResourceTagNotExistsException {
		Utils.notNull(perunSession, "perunSession");
		Utils.notNull(resourceTag, "resourceTag");
		resourcesManagerBl.checkResourceTagExists(perunSession, resourceTag);
		Vo vo = getPerunBl().getVosManagerBl().getVoById(perunSession, resourceTag.getVoId());

		if (!AuthzResolver.isAuthorized(perunSession, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(perunSession, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(perunSession, "getAllResourcesByResourceTag");
			//TODO: what about GROUPADMIN?
		}
		return resourcesManagerBl.getAllResourcesByResourceTag(perunSession, resourceTag);
	}

	@Override
	public List<ResourceTag> getAllResourcesTagsForVo(PerunSession perunSession, Vo vo) throws InternalErrorException, PrivilegeException, VoNotExistsException {
		Utils.notNull(perunSession, "perunSession");
		getPerunBl().getVosManagerBl().checkVoExists(perunSession, vo);

		if (!AuthzResolver.isAuthorized(perunSession, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(perunSession, Role.VOOBSERVER, vo) &&
				!AuthzResolver.isAuthorized(perunSession, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(perunSession, "getAllResourcesTagsForVo");
			//TODO: what about GROUPADMIN?
				}

		return resourcesManagerBl.getAllResourcesTagsForVo(perunSession, vo);
	}

	@Override
	public List<ResourceTag> getAllResourcesTagsForResource(PerunSession perunSession, Resource resource) throws InternalErrorException, ResourceNotExistsException, PrivilegeException {
		Utils.notNull(perunSession, "perunSession");
		resourcesManagerBl.checkResourceExists(perunSession, resource);

		if (!AuthzResolver.isAuthorized(perunSession, Role.VOADMIN, resource) &&
				!AuthzResolver.isAuthorized(perunSession, Role.VOOBSERVER, resource) &&
				!AuthzResolver.isAuthorized(perunSession, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(perunSession, "getAllResourcesTagsForResource");
			//TODO: What about GROUPADMIN?
				}

		return resourcesManagerBl.getAllResourcesTagsForResource(perunSession, resource);
	}

	@Override
	public void copyAttributes(PerunSession sess, Resource sourceResource, Resource destinationResource) throws InternalErrorException, PrivilegeException, ResourceNotExistsException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);

		getResourcesManagerBl().checkResourceExists(sess, sourceResource);
		getResourcesManagerBl().checkResourceExists(sess, destinationResource);

		// Authorization - facility admin of the both resources required
		if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, sourceResource)) {
			throw new PrivilegeException(sess, "copyAttributes");
		}
		if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, destinationResource)) {
			throw new PrivilegeException(sess, "copyAttributes");
		}

		getResourcesManagerBl().copyAttributes(sess, sourceResource, destinationResource);
	}

	@Override
	public void copyServices(PerunSession sess, Resource sourceResource, Resource destinationResource) throws InternalErrorException, ResourceNotExistsException, PrivilegeException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);

		getResourcesManagerBl().checkResourceExists(sess, sourceResource);
		getResourcesManagerBl().checkResourceExists(sess, destinationResource);

		// Authorization - facility admin of the both resources required
		if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, sourceResource)) {
			throw new PrivilegeException(sess, "copyServices");
		}
		if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, destinationResource)) {
			throw new PrivilegeException(sess, "copyServices");
		}

		getResourcesManagerBl().copyServices(sess, sourceResource, destinationResource);
	}

	@Override
	public void copyGroups(PerunSession sess, Resource sourceResource, Resource destinationResource) throws InternalErrorException, ResourceNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		getResourcesManagerBl().checkResourceExists(sess, sourceResource);
		getResourcesManagerBl().checkResourceExists(sess, destinationResource);

		// Authorization - vo admin of the both resources required
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, sourceResource)) {
			throw new PrivilegeException(sess, "copyGroups");
		}
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, destinationResource)) {
			throw new PrivilegeException(sess, "copyGroups");
		}

		getResourcesManagerBl().copyGroups(sess, sourceResource, destinationResource);
	}

	@Override
	public List<User> getAdmins(PerunSession perunSession, Resource resource, boolean onlyDirectAdmins) throws InternalErrorException, PrivilegeException, ResourceNotExistsException {
		Utils.checkPerunSession(perunSession);
		getResourcesManagerBl().checkResourceExists(perunSession, resource);

		// Authorization
		if (!AuthzResolver.isAuthorized(perunSession, Role.VOADMIN, resource) &&
				!AuthzResolver.isAuthorized(perunSession, Role.RESOURCEADMIN, resource) &&
				!AuthzResolver.isAuthorized(perunSession, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(perunSession, "getAdmins");
		}

		return getResourcesManagerBl().getAdmins(perunSession, resource, onlyDirectAdmins);
	}

	@Override
	public List<RichUser> getRichAdmins(PerunSession perunSession, Resource resource, List<String> specificAttributes, boolean allUserAttributes, boolean onlyDirectAdmins) throws InternalErrorException, UserNotExistsException, PrivilegeException, ResourceNotExistsException {
		Utils.checkPerunSession(perunSession);
		getResourcesManagerBl().checkResourceExists(perunSession, resource);

		if(!allUserAttributes) Utils.notNull(specificAttributes, "specificAttributes");

		// Authorization
		if (!AuthzResolver.isAuthorized(perunSession, Role.VOADMIN, resource) &&
				!AuthzResolver.isAuthorized(perunSession, Role.RESOURCEADMIN, resource) &&
				!AuthzResolver.isAuthorized(perunSession, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(perunSession, "getRichAdmins");
		}

		return getPerunBl().getUsersManagerBl().filterOnlyAllowedAttributes(perunSession, getResourcesManagerBl().getRichAdmins(perunSession, resource, specificAttributes, allUserAttributes, onlyDirectAdmins));
	}

	@Override
	public List<Resource> getResourcesWhereUserIsAdmin(PerunSession sess, User user) throws InternalErrorException, UserNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if(!AuthzResolver.isAuthorized(sess, Role.SELF, user) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getResourcesWhereUserIsAdmin");
		}

		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);

		return getResourcesManagerBl().getResourcesWhereUserIsAdmin(sess, user);
	}

	@Override
	public List<Resource> getResourcesWhereUserIsAdmin(PerunSession sess, Facility facility, Vo vo, User authorizedUser) throws InternalErrorException, PrivilegeException, UserNotExistsException, FacilityNotExistsException, VoNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, authorizedUser);

		List<Resource> resources = getResourcesManagerBl().getResourcesWhereUserIsAdmin(sess, facility, vo, authorizedUser);

		//Vo manager of the vo and perunobserver can see all returned resources
		if(AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) ||
				AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)){
			return resources;
		}

		//Resource manager can see only resources where he has role resource manager (filter them)
		if(AuthzResolver.isAuthorized(sess, Role.RESOURCEADMIN)) {
			return filterNotAuthorizedResource(sess, resources);
		} else {
			throw new PrivilegeException(sess, "getResourcesByResourceManager");
		}
	}

	@Override
	public List<Resource> getResourcesWhereUserIsAdmin(PerunSession sess, Vo vo, User authorizedUser) throws InternalErrorException, PrivilegeException, UserNotExistsException, VoNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, authorizedUser);

		List<Resource> resources = getResourcesManagerBl().getResourcesWhereUserIsAdmin(sess, vo, authorizedUser);

		//Vo manager of the vo, perunobserver and voobserver can see all returned resources
		if(AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) ||
			AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo) ||
			AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)){
			return resources;
		}

		//Resource manager can see only resources where he has role resource manager (filter them)
		if(AuthzResolver.isAuthorized(sess, Role.RESOURCEADMIN)) {
			return filterNotAuthorizedResource(sess, resources);
		} else {
			throw new PrivilegeException(sess, "getResourcesWhereUserIsAdmin");
		}
	}

	@Override
	public List<Resource> getResourcesWhereGroupIsAdmin(PerunSession sess, Facility facility, Vo vo, Group authorizedGroup) throws InternalErrorException, PrivilegeException, GroupNotExistsException, FacilityNotExistsException, VoNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, authorizedGroup);

		List<Resource> resources = getResourcesManagerBl().getResourcesWhereGroupIsAdmin(sess, facility, vo, authorizedGroup);

		//Vo manager of the vo and perunobserver can see all returned resources
		if(AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) ||
				AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)){
			return resources;
		}

		//Resource manager can see only resources where he has role resource manager (filter them)
		if(AuthzResolver.isAuthorized(sess, Role.RESOURCEADMIN)) {
			return filterNotAuthorizedResource(sess, resources);
		} else {
			throw new PrivilegeException(sess, "getResourcesByResourceManager");
		}
	}

	@Override
	public List<Group> getAdminGroups(PerunSession sess, Resource resource) throws InternalErrorException, ResourceNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);
		getResourcesManagerBl().checkResourceExists(sess, resource);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, resource) &&
				!AuthzResolver.isAuthorized(sess, Role.RESOURCEADMIN, resource) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getAdminGroups");
		}

		return getResourcesManagerBl().getAdminGroups(sess, resource);
	}

	@Override
	public void addAdmin(PerunSession sess, Resource resource, User user) throws InternalErrorException, UserNotExistsException, PrivilegeException, AlreadyAdminException, ResourceNotExistsException {
		Utils.checkPerunSession(sess);
		getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, resource)) {
			throw new PrivilegeException(sess, "addAdmin");
		}

		AuthzResolverBlImpl.setRole(sess, user, resource, Role.RESOURCEADMIN);
	}

	@Override
	public void addAdmin(PerunSession sess, Resource resource, Group group) throws InternalErrorException, GroupNotExistsException, PrivilegeException, AlreadyAdminException, ResourceNotExistsException {
		Utils.checkPerunSession(sess);
		getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, resource)) {
			throw new PrivilegeException(sess, "addAdmin");
		}

		AuthzResolverBlImpl.setRole(sess, group, resource, Role.RESOURCEADMIN);
	}

	@Override
	public void removeAdmin(PerunSession sess, Resource resource, User user) throws InternalErrorException, UserNotExistsException, PrivilegeException, UserNotAdminException, ResourceNotExistsException {
		Utils.checkPerunSession(sess);
		getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, resource)) {
			throw new PrivilegeException(sess, "removeAdmin");
		}

		AuthzResolverBlImpl.unsetRole(sess, user, resource, Role.RESOURCEADMIN);
	}

	@Override
	public void removeAdmin(PerunSession sess, Resource resource, Group group) throws InternalErrorException, GroupNotExistsException, PrivilegeException, GroupNotAdminException, ResourceNotExistsException {
		Utils.checkPerunSession(sess);
		getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, resource)) {
			throw new PrivilegeException(sess, "removeAdmin");
		}

		AuthzResolverBlImpl.unsetRole(sess, group, resource, Role.RESOURCEADMIN);
	}

	@Override
	public BanOnResource setBan(PerunSession sess, BanOnResource banOnResource) throws InternalErrorException, PrivilegeException, BanAlreadyExistsException, ResourceNotExistsException {
		Utils.checkPerunSession(sess);
		Utils.notNull(banOnResource, "banOnResource");

		Resource resource = getResourcesManagerBl().getResourceById(sess, banOnResource.getResourceId());

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, resource)) {
			throw new PrivilegeException(sess, "setBan");
		}

		return getResourcesManagerBl().setBan(sess, banOnResource);
	}

	@Override
	public BanOnResource getBanById(PerunSession sess, int banId) throws InternalErrorException, BanNotExistsException, PrivilegeException, ResourceNotExistsException {
		Utils.checkPerunSession(sess);

		BanOnResource ban = getResourcesManagerBl().getBanById(sess, banId);
		Resource resource = getResourcesManagerBl().getResourceById(sess, ban.getResourceId());

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, resource) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getBanById");
		}

		return ban;
	}

	@Override
	public BanOnResource getBan(PerunSession sess, int memberId, int resourceId) throws InternalErrorException, BanNotExistsException, PrivilegeException, MemberNotExistsException, ResourceNotExistsException {
		Utils.checkPerunSession(sess);
		Member member = getPerunBl().getMembersManagerBl().getMemberById(sess, memberId);
		Resource resource = getPerunBl().getResourcesManagerBl().getResourceById(sess, resourceId);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, resource) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getBan");
		}

		return getResourcesManagerBl().getBan(sess, memberId, resourceId);
	}

	@Override
	public List<BanOnResource> getBansForMember(PerunSession sess, int memberId) throws InternalErrorException, MemberNotExistsException, ResourceNotExistsException {
		Utils.checkPerunSession(sess);
		Member member = getPerunBl().getMembersManagerBl().getMemberById(sess, memberId);

		List<BanOnResource> usersBans = getResourcesManagerBl().getBansForMember(sess, memberId);

		if (AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			return usersBans;
		}

		//filtering
		Iterator<BanOnResource> iterator = usersBans.iterator();
		while(iterator.hasNext()) {
			BanOnResource banForFiltering = iterator.next();
			Resource resource = getResourcesManagerBl().getResourceById(sess, banForFiltering.getResourceId());
			if(!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, resource)) iterator.remove();
		}

		return usersBans;
	}

	@Override
	public List<BanOnResource> getBansForResource(PerunSession sess, int resourceId) throws InternalErrorException, PrivilegeException, ResourceNotExistsException {
		Utils.checkPerunSession(sess);
		Resource resource = getPerunBl().getResourcesManagerBl().getResourceById(sess, resourceId);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, resource) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getBansForResource");
		}

		return getResourcesManagerBl().getBansForResource(sess, resourceId);
	}

	@Override
	public BanOnResource updateBan(PerunSession sess, BanOnResource banOnResource) throws InternalErrorException, PrivilegeException, MemberNotExistsException, BanNotExistsException, ResourceNotExistsException {
		Utils.checkPerunSession(sess);
		this.getResourcesManagerBl().checkBanExists(sess, banOnResource.getId());
		Member member = getPerunBl().getMembersManagerBl().getMemberById(sess, banOnResource.getMemberId());
		Resource resource = getPerunBl().getResourcesManagerBl().getResourceById(sess, banOnResource.getResourceId());

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, resource)) {
			throw new PrivilegeException(sess, "updateBan");
		}

		banOnResource = getResourcesManagerBl().updateBan(sess, banOnResource);
		return banOnResource;
	}

	@Override
	public void removeBan(PerunSession sess, int banId) throws InternalErrorException, PrivilegeException, BanNotExistsException, ResourceNotExistsException {
		Utils.checkPerunSession(sess);
		BanOnResource ban = this.getResourcesManagerBl().getBanById(sess, banId);

		Resource resource = getResourcesManagerBl().getResourceById(sess, ban.getResourceId());

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, resource)) {
			throw new PrivilegeException(sess, "removeBan");
		}

		getResourcesManagerBl().removeBan(sess, banId);
	}

	@Override
	public void removeBan(PerunSession sess, int memberId, int resourceId) throws InternalErrorException, BanNotExistsException, PrivilegeException, ResourceNotExistsException {
		Utils.checkPerunSession(sess);
		BanOnResource ban = this.getResourcesManagerBl().getBan(sess, memberId, resourceId);

		Resource resource = getResourcesManagerBl().getResourceById(sess, ban.getResourceId());

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, resource)) {
			throw new PrivilegeException(sess, "removeBan");
		}

		getResourcesManagerBl().removeBan(sess, memberId, resourceId);
	}

	@Override
	public void addResourceSelfServiceUser(PerunSession sess, Resource resource, User user) throws InternalErrorException, PrivilegeException, AlreadyAdminException {
		Utils.checkPerunSession(sess);

		Vo vo;
		Facility facility;

		try {
			facility = sess.getPerun().getFacilitiesManager().getFacilityById(sess, resource.getFacilityId());
		} catch (FacilityNotExistsException e) {
			throw new InternalErrorException("Failed to find facility for given resource.");
		}

		try {
			vo = sess.getPerun().getVosManager().getVoById(sess, resource.getVoId());
		} catch (VoNotExistsException e) {
			throw new InternalErrorException("Failed to find vo for given resource.");
		}

		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
		    !AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility)) {
			throw new PrivilegeException(sess, "addResourceSelfServiceUser");
		}

		getResourcesManagerBl().addResourceSelfServiceUser(sess, resource, user);
	}

	@Override
	public void addResourceSelfServiceGroup(PerunSession sess, Resource resource, Group group) throws InternalErrorException, PrivilegeException, AlreadyAdminException {
		Utils.checkPerunSession(sess);

		Vo vo;
		Facility facility;

		try {
			facility = sess.getPerun().getFacilitiesManager().getFacilityById(sess, resource.getFacilityId());
		} catch (FacilityNotExistsException e) {
			throw new InternalErrorException("Failed to find facility for given resource.");
		}

		try {
			vo = sess.getPerun().getVosManager().getVoById(sess, resource.getVoId());
		} catch (VoNotExistsException e) {
			throw new InternalErrorException("Failed to find vo for given resource.");
		}

		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
			!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility)) {
			throw new PrivilegeException(sess, "addResourceSelfServiceGroup");
		}

		getResourcesManagerBl().addResourceSelfServiceGroup(sess, resource, group);
	}

	@Override
	public void removeResourceSelfServiceUser(PerunSession sess, Resource resource, User user) throws InternalErrorException, PrivilegeException, UserNotAdminException {
		Utils.checkPerunSession(sess);

		Vo vo;
		Facility facility;

		try {
			facility = sess.getPerun().getFacilitiesManager().getFacilityById(sess, resource.getFacilityId());
		} catch (FacilityNotExistsException e) {
			throw new InternalErrorException("Failed to find facility for given resource.");
		}

		try {
			vo = sess.getPerun().getVosManager().getVoById(sess, resource.getVoId());
		} catch (VoNotExistsException e) {
			throw new InternalErrorException("Failed to find vo for given resource.");
		}

		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
			!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility)) {
			throw new PrivilegeException(sess, "removeResourceSelfServiceUser");
		}

		getResourcesManagerBl().removeResourceSelfServiceUser(sess, resource, user);
	}

	@Override
	public void removeResourceSelfServiceGroup(PerunSession sess, Resource resource, Group group) throws InternalErrorException, PrivilegeException, GroupNotAdminException {
		Utils.checkPerunSession(sess);

		Vo vo;
		Facility facility;

		try {
			facility = sess.getPerun().getFacilitiesManager().getFacilityById(sess, resource.getFacilityId());
		} catch (FacilityNotExistsException e) {
			throw new InternalErrorException("Failed to find facility for given resource.");
		}

		try {
			vo = sess.getPerun().getVosManager().getVoById(sess, resource.getVoId());
		} catch (VoNotExistsException e) {
			throw new InternalErrorException("Failed to find vo for given resource.");
		}

		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
			!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility)) {
			throw new PrivilegeException(sess, "removeResourceSelfServiceGroup");
		}

		getResourcesManagerBl().removeResourceSelfServiceGroup(sess, resource, group);
	}

	/**
	 * Filter out resources where user in session has not role resource admin for them
	 *
	 * @param sess
	 * @param resources list of resources to be filtered
	 * @return list of filtered resources
	 * @throws InternalErrorException
	 */
	private List<Resource> filterNotAuthorizedResource(PerunSession sess, List<Resource> resources) throws InternalErrorException {
		Iterator<Resource> resIterator = resources.iterator();

		if (AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			return resources;
		}
		while(resIterator.hasNext()) {
			Resource resource = resIterator.next();
			if(!AuthzResolver.isAuthorized(sess, Role.RESOURCEADMIN, resource)) resIterator.remove();
		}

		return resources;
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
