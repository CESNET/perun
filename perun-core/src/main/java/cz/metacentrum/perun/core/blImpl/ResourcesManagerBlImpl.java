package cz.metacentrum.perun.core.blImpl;

import java.util.*;

import cz.metacentrum.perun.audit.events.ResourceManagerEvents.AdminGroupAddedForResource;
import cz.metacentrum.perun.audit.events.ResourceManagerEvents.AdminGroupRemovedForResource;
import cz.metacentrum.perun.audit.events.ResourceManagerEvents.AdminUserAddedForResource;
import cz.metacentrum.perun.audit.events.ResourceManagerEvents.AdminUserRemovedForResource;
import cz.metacentrum.perun.audit.events.ResourceManagerEvents.BanRemovedForResource;
import cz.metacentrum.perun.audit.events.ResourceManagerEvents.BanSetForResource;
import cz.metacentrum.perun.audit.events.ResourceManagerEvents.BanUpdatedForResource;
import cz.metacentrum.perun.audit.events.ResourceManagerEvents.FacilitySetForResource;
import cz.metacentrum.perun.audit.events.ResourceManagerEvents.GroupAssignedToResource;
import cz.metacentrum.perun.audit.events.ResourceManagerEvents.GroupRemovedFromResource;
import cz.metacentrum.perun.audit.events.ResourceManagerEvents.ResourceCreated;
import cz.metacentrum.perun.audit.events.ResourceManagerEvents.ResourceDeleted;
import cz.metacentrum.perun.audit.events.ResourceManagerEvents.ResourceUpdated;
import cz.metacentrum.perun.audit.events.ResourceManagerEvents.ServiceAssignedToResource;
import cz.metacentrum.perun.audit.events.ResourceManagerEvents.ServiceRemovedFromResource;
import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.api.exceptions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.metacentrum.perun.core.bl.AttributesManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.bl.ResourcesManagerBl;
import cz.metacentrum.perun.core.implApi.ResourcesManagerImplApi;

/**
 *
 * @author Slavek Licehammer glory@ics.muni.cz
 */
public class ResourcesManagerBlImpl implements ResourcesManagerBl {

	final static Logger log = LoggerFactory.getLogger(ResourcesManagerBlImpl.class);

	private ResourcesManagerImplApi resourcesManagerImpl;
	private PerunBl perunBl;

	public ResourcesManagerBlImpl(ResourcesManagerImplApi resourcesManagerImpl) {
		this.resourcesManagerImpl = resourcesManagerImpl;
	}

	@Override
	public Resource getResourceById(PerunSession sess, int id) throws InternalErrorException, ResourceNotExistsException {
		return getResourcesManagerImpl().getResourceById(sess, id);
	}

	@Override
	public RichResource getRichResourceById(PerunSession sess, int id) throws InternalErrorException, ResourceNotExistsException {
		return getResourcesManagerImpl().getRichResourceById(sess, id);
	}

	@Override
	public Resource getResourceByName(PerunSession sess, Vo vo, Facility facility, String name) throws InternalErrorException, ResourceNotExistsException {
		return getResourcesManagerImpl().getResourceByName(sess, vo, facility, name);
	}

	@Override
	public Resource createResource(PerunSession sess, Resource resource, Vo vo, Facility facility) throws InternalErrorException, FacilityNotExistsException, ResourceExistsException {
		try{
			Resource existingResource = getResourcesManagerImpl().getResourceByName(sess, vo, facility, resource.getName());
			throw new ResourceExistsException(existingResource);
		} catch (ResourceNotExistsException e) {
			resource = getResourcesManagerImpl().createResource(sess, vo, resource, facility);
			getPerunBl().getAuditer().log(sess, new ResourceCreated(resource));
		}

		return resource;
	}

	@Override
	public Resource copyResource(PerunSession sess, Resource templateResource, Resource destinationResource, boolean withGroups) throws ResourceExistsException, InternalErrorException, FacilityNotExistsException {
		Resource newResource = new Resource();
		Vo destinationVo = this.getVo(sess, destinationResource);
		Facility destinationFacility = this.getFacility(sess, destinationResource);

		newResource.setName(destinationResource.getName());
		newResource = this.createResource(sess, newResource, destinationVo, destinationFacility);

		//resource attributes
		List<Attribute> templateResourceAttributes = perunBl.getAttributesManagerBl().getAttributes(sess,templateResource);
		for (Attribute resourceAttribute : templateResourceAttributes) {
			try {
				if (!resourceAttribute.getNamespace().startsWith(AttributesManager.NS_RESOURCE_ATTR_VIRT) &&
						!resourceAttribute.getNamespace().startsWith(AttributesManager.NS_RESOURCE_ATTR_CORE)) {
					perunBl.getAttributesManagerBl().setAttribute(sess, newResource, resourceAttribute);
				}
			} catch (WrongAttributeValueException | WrongAttributeAssignmentException | WrongReferenceAttributeValueException ex) {
				throw new ConsistencyErrorException("DB inconsistency while copying attributes from one resource to another. Cause:{}", ex);
			}
		}

		//if withGroups is true we also copy groups and group-resource/member-resource attributes
		if(withGroups){
			List<Group> templateResourceGroups = perunBl.getResourcesManagerBl().getAssignedGroups(sess, templateResource);
			try {
				assignGroupsToResource(sess, templateResourceGroups, newResource);
				for (Group group : templateResourceGroups) {
					List<Attribute> groupResourceAttrs = perunBl.getAttributesManagerBl().getAttributes(sess, templateResource, group);
					for (Attribute attr : groupResourceAttrs) {
						if (!attr.getNamespace().startsWith(AttributesManager.NS_GROUP_RESOURCE_ATTR_VIRT)) {
							perunBl.getAttributesManagerBl().setAttribute(sess, newResource, group, attr);
						}
					}
				}
			} catch (GroupResourceMismatchException | WrongAttributeValueException | GroupAlreadyAssignedException |
				WrongAttributeAssignmentException | WrongReferenceAttributeValueException ex) {
				throw new ConsistencyErrorException("DB inconsistency while copying group-resource attributes. Cause:{}", ex);
			}

			List<Member> templateResourceMembers = perunBl.getResourcesManagerBl().getAssignedMembers(sess, templateResource);
			try {
				for (Member member : templateResourceMembers) {
					List<Attribute> memberResourceAttrs = perunBl.getAttributesManagerBl().getAttributes(sess, templateResource, member);
					for (Attribute attr : memberResourceAttrs) {
						if (!attr.getNamespace().startsWith(AttributesManager.NS_MEMBER_RESOURCE_ATTR_VIRT)) {
							perunBl.getAttributesManagerBl().setAttribute(sess, newResource, member, attr);
						}
					}
				}
			} catch (MemberResourceMismatchException | WrongAttributeValueException|
					WrongAttributeAssignmentException| WrongReferenceAttributeValueException ex) {
				throw new ConsistencyErrorException("DB inconsistency while copying group-resource attributes. Cause:{}", ex);
			}
		}

		//services
		List<Service> services = getAssignedServices(sess, templateResource);
		for (Service service : services) {
			try {
				getResourcesManagerImpl().assignService(sess, newResource, service);
			} catch (ServiceAlreadyAssignedException ex) {
				throw new ConsistencyErrorException("Service was already assigned to this resource. {}", ex);
			}
		}

		//tags
		List<ResourceTag> templateResourceTags = getAllResourcesTagsForResource(sess, templateResource);
		for(ResourceTag resourceTag : templateResourceTags) {
				getResourcesManagerImpl().assignResourceTagToResource(sess, resourceTag, newResource);
		}

		return newResource;
	}

	@Override
	public void deleteResource(PerunSession sess, Resource resource) throws InternalErrorException, RelationExistsException, ResourceAlreadyRemovedException, GroupAlreadyRemovedFromResourceException {
		//Get facility for audit messages
		Facility facility = this.getFacility(sess, resource);

		// Remove binding between resource and service
		List<Service> services = getAssignedServices(sess, resource);
		for (Service service: services) {
			try {
				getResourcesManagerImpl().removeService(sess, resource, service);
			} catch (ServiceNotAssignedException e) {
				throw new ConsistencyErrorException(e);
			}
		}

		List<Group> groups = getAssignedGroups(sess, resource);
		for (Group group: groups){
			try {
				removeGroupFromResource(sess, group, resource);
			} catch (GroupNotDefinedOnResourceException ex) {
				throw new GroupAlreadyRemovedFromResourceException(ex);
			}
		}

		// Remove attr values for the resource
		try {
			perunBl.getAttributesManagerBl().removeAllAttributes(sess, resource);
		} catch(AttributeValueException ex) {
			throw new ConsistencyErrorException("All services are removed from this resource. There is no required attribute. So all attribtes for this resource can be removed withou problem.", ex);
		}

		// Remove group-resource attr values for all group and resource
		try {
			this.perunBl.getAttributesManagerBl().removeAllGroupResourceAttributes(sess, resource);
		} catch (WrongAttributeValueException ex) {
			throw new InternalErrorException(ex);
		} catch (WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		} catch (WrongReferenceAttributeValueException ex) {
			throw new InternalErrorException(ex);
		} catch (GroupResourceMismatchException ex) {
			throw new InternalErrorException(ex);
		}
		//Remove all resources tags
		this.removeAllResourcesTagFromResource(sess, resource);

		//Remove all resource bans
		List<BanOnResource> bansOnResource = this.getBansForResource(sess, resource.getId());
		for(BanOnResource banOnResource : bansOnResource) {
			try {
				this.removeBan(sess, banOnResource.getId());
			} catch (BanNotExistsException ex) {
				//it is ok, we just want to remove it anyway
			}
		}

		//Because resource will be tottaly deleted, we can also delete all member-resource attributes
		try {
			this.perunBl.getAttributesManagerBl().removeAllMemberResourceAttributes(sess, resource);
		} catch (WrongAttributeValueException | WrongAttributeAssignmentException | WrongReferenceAttributeValueException ex) {
			throw new InternalErrorException(ex);
		}

		// Get the resource VO
		Vo vo = this.getVo(sess, resource);
		getResourcesManagerImpl().deleteResource(sess, vo, resource);
		getPerunBl().getAuditer().log(sess, new ResourceDeleted(resource, facility, services));
	}

	@Override
	public void deleteAllResources(PerunSession sess, Vo vo) throws InternalErrorException, RelationExistsException, ResourceAlreadyRemovedException, GroupAlreadyRemovedFromResourceException {
		for(Resource r: this.getResources(sess, vo)) {
			deleteResource(sess, r);
		}
	}

	@Override
	public Facility getFacility(PerunSession sess, Resource resource) throws InternalErrorException {
		try {
			return getPerunBl().getFacilitiesManagerBl().getFacilityById(sess, resource.getFacilityId());
		} catch (FacilityNotExistsException e) {
			throw new ConsistencyErrorException("Resource doesn't have assigned facility", e);
		}
	}

	@Override
	public Vo getVo(PerunSession sess, Resource resource) throws InternalErrorException {
		try {
			return getPerunBl().getVosManagerBl().getVoById(sess, resource.getVoId());
		} catch (VoNotExistsException e) {
			throw new ConsistencyErrorException("Resource is assigned to the non-existent VO.", e);
		}
	}

	@Override
	public List<User> getAllowedUsers(PerunSession sess, Resource resource) throws InternalErrorException {
		return getResourcesManagerImpl().getAllowedUsers(sess, resource);
	}

	@Override
	public boolean isUserAssigned(PerunSession sess, User user, Resource resource) throws InternalErrorException {
		return getResourcesManagerImpl().isUserAssigned(sess, user, resource);
	}

	@Override
	public boolean isUserAllowed(PerunSession sess, User user, Resource resource) throws InternalErrorException {
		if (this.isUserAssigned(sess, user, resource)) {
			Vo vo = this.getVo(sess, resource);
			Member member;
			try {
				member = getPerunBl().getMembersManagerBl().getMemberByUser(sess, vo, user);
			} catch (MemberNotExistsException e) {
				throw new ConsistencyErrorException("Non-existent member is assigned to the resource.", e);
			}
			return !getPerunBl().getMembersManagerBl().haveStatus(sess, member, Status.INVALID);
		} else {
			return false;
		}
	}

	@Override
	public boolean isGroupAssigned(PerunSession sess, Group group, Resource resource) throws InternalErrorException {
		return getResourcesManagerImpl().isGroupAssigned(sess, group, resource);
	}

	@Override
	public List<Member> getAllowedMembers(PerunSession sess, Resource resource) throws InternalErrorException {
		return getResourcesManagerImpl().getAllowedMembers(sess, resource);
	}

	@Override
	public List<Member> getAssignedMembers(PerunSession sess, Resource resource) throws InternalErrorException {
		return getResourcesManagerImpl().getAssignedMembers(sess, resource);
	}

	@Override
	public List<RichMember> getAssignedRichMembers(PerunSession sess, Resource resource) throws InternalErrorException {
		List<Member> listOfMembers = getResourcesManagerImpl().getAssignedMembers(sess, resource);
		return getPerunBl().getMembersManagerBl().convertMembersToRichMembers(sess, listOfMembers);
	}


	@Override
	public List<Service> getAssignedServices(PerunSession sess, Resource resource) throws InternalErrorException {
		List<Service> services = new ArrayList<Service>();
		List<Integer> servicesIds = getResourcesManagerImpl().getAssignedServices(sess, resource);

		try {
			for(Integer serviceId: servicesIds) {
				services.add(getPerunBl().getServicesManagerBl().getServiceById(sess, serviceId));
			}
		} catch(ServiceNotExistsException ex) {
			throw new ConsistencyErrorException(ex);
		}

		return services;
	}


	@Override
	public void assignGroupToResource(PerunSession sess, Group group, Resource resource) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, GroupAlreadyAssignedException {
		Vo groupVo = getPerunBl().getGroupsManagerBl().getVo(sess, group);

		// Check if the group and resource belongs to the same VO
		if (!groupVo.equals(this.getVo(sess, resource))) {
			throw new InternalErrorException("Group " + group + " and resource " + resource + " belongs to the different VOs");
		}

		if(isGroupAssigned(sess, group, resource)) throw new GroupAlreadyAssignedException(group);

		//first we must assign group to resource and then set na check attributes, because methods checkAttributesValue and fillAttribute need actual state to work correctly
		getResourcesManagerImpl().assignGroupToResource(sess, group, resource);
		getPerunBl().getAuditer().log(sess, new GroupAssignedToResource(group, resource));


		//fill and check required attributes' values for the group

		try {
			//FIXME predelat na metody ktere umeji zpracovavat najednou group i group-resource atributy
			//List<Attribute> groupRequiredAttributes = getPerunBl().getAttributesManagerBl().getResourceRequiredAttributes(sess, resource, resource, group, true);

			//group-resource
			List<Attribute> resourceGroupRequiredAttributes = getPerunBl().getAttributesManagerBl().getResourceRequiredAttributes(sess, resource, resource, group);
			resourceGroupRequiredAttributes = getPerunBl().getAttributesManagerBl().fillAttributes(sess, resource, group, resourceGroupRequiredAttributes);
			getPerunBl().getAttributesManagerBl().setAttributes(sess, resource, group, resourceGroupRequiredAttributes);

			//group
			List<Attribute> groupRequiredAttributes = getPerunBl().getAttributesManagerBl().getResourceRequiredAttributes(sess, resource, group);
			groupRequiredAttributes = getPerunBl().getAttributesManagerBl().fillAttributes(sess, group, groupRequiredAttributes);
			getPerunBl().getAttributesManagerBl().setAttributes(sess, group, groupRequiredAttributes);

		} catch(WrongAttributeAssignmentException ex) {
			throw new ConsistencyErrorException(ex);
		} catch (GroupResourceMismatchException ex) {
			throw new ConsistencyErrorException(ex);
		}

		//fill and check required attributes' values for each member
		//and set defaultResource attribute if necessary
		List<Member> members = getPerunBl().getGroupsManagerBl().getGroupMembers(sess, group);
		Facility facility = getPerunBl().getResourcesManagerBl().getFacility(sess, resource);
		for(Member member : members) {
			User user = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
			try {
				getPerunBl().getAttributesManagerBl().setRequiredAttributes(sess, facility, resource, user, member);
			} catch(WrongAttributeAssignmentException ex) {
				throw new ConsistencyErrorException(ex);
			} catch(AttributeNotExistsException ex) {
				throw new ConsistencyErrorException(ex);
			} catch (MemberResourceMismatchException ex) {
				throw new ConsistencyErrorException(ex);
			}
		}

	}

	@Override
	public void assignGroupsToResource(PerunSession perunSession, List<Group> groups, Resource resource) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, GroupAlreadyAssignedException {
		for(Group g: groups) {
			this.assignGroupToResource(perunSession, g, resource);
		}
	}

	@Override
	public void assignGroupToResources(PerunSession perunSession, Group group, List<Resource> resources) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, GroupAlreadyAssignedException {
		for(Resource r: resources) {
			this.assignGroupToResource(perunSession, group, r);
		}
	}

	@Override
	public void removeGroupFromResource(PerunSession sess, Group group, Resource resource) throws InternalErrorException, GroupNotDefinedOnResourceException, GroupAlreadyRemovedFromResourceException {
		Vo groupVo = getPerunBl().getGroupsManagerBl().getVo(sess, group);

		// Check if the group and resource belongs to the same VO
		if (!groupVo.equals(this.getVo(sess, resource))) {
			throw new InternalErrorException("Group " + group + " and resource " + resource + " belongs to the different VOs");
		}

		// Check if the group was defined on the resource
		if (!this.getAssignedGroups(sess, resource).contains(group)) {
			// Group is not defined on the resource
			throw new GroupNotDefinedOnResourceException(group.getName());
		}

		// Remove group
		getResourcesManagerImpl().removeGroupFromResource(sess, group, resource);
		getPerunBl().getAuditer().log(sess, new GroupRemovedFromResource(group, resource));

		// Remove group-resource attributes
		try {
			getPerunBl().getAttributesManagerBl().removeAllAttributes(sess, resource, group);
		} catch (WrongAttributeValueException e) {
			throw new InternalErrorException(e);
		} catch (WrongReferenceAttributeValueException e) {
			throw new InternalErrorException(e);
		} catch (GroupResourceMismatchException ex) {
			throw new ConsistencyErrorException(ex);
		}

		//check attributes and set new correct values if necessary
		List<Member> groupsMembers = getPerunBl().getGroupsManagerBl().getGroupMembers(sess, group);
		Facility facility = getFacility(sess, resource);
		List<User> allowedUsers = getPerunBl().getFacilitiesManagerBl().getAllowedUsers(sess, facility);
		for(Member member : groupsMembers) {
			User user = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
			if(!allowedUsers.contains(user)) { //user don't have acess to facility now
				//his attributes can keep original value

				//find required user-facility attributes (that which are not required can keep original value)
				List<Attribute> userFacilityAttributes = getPerunBl().getAttributesManagerBl().getRequiredAttributes(sess, facility, user);

				//find which of attributes are broken
				List<Attribute> brokenUserFacilityAttributes = new ArrayList<Attribute>();
				for(Attribute attribute : userFacilityAttributes) {
					try {
						getPerunBl().getAttributesManagerBl().checkAttributeValue(sess, facility, user, attribute);
					} catch(WrongAttributeAssignmentException ex) {
						throw new ConsistencyErrorException(ex);
					} catch(WrongAttributeValueException ex) {
						attribute.setValue(null);
						brokenUserFacilityAttributes.add(attribute);
					} catch(WrongReferenceAttributeValueException ex) {
						//TODO jeste o tom popremyslet
						//TODO this may fix it
						attribute.setValue(null);
						brokenUserFacilityAttributes.add(attribute);
					}
				}

				//fix broken attributes
				try {
					List<Attribute> fixedUserFacilityAttributes = getPerunBl().getAttributesManagerBl().fillAttributes(sess, facility, user, brokenUserFacilityAttributes);
					getPerunBl().getAttributesManagerBl().setAttributes(sess, facility, user, fixedUserFacilityAttributes);
				} catch(WrongAttributeAssignmentException ex) {
					throw new ConsistencyErrorException(ex);
				} catch(WrongAttributeValueException ex) {
					//TODO jeste o tom popremyslet
					//That's unresolveable problem
					throw new InternalErrorException("Can't set attributes for user-facility correctly. User=" + user + " Facility=" + facility + ".", ex);
				} catch(WrongReferenceAttributeValueException ex) {
					//TODO jeste o tom popremyslet
					//That's unresolveable problem
					throw new InternalErrorException("Can't set attributes for user-facility correctly. User=" + user + " Facility=" + facility + ".", ex);
				}
			}
		}
	}

	@Override
	public void removeGroupsFromResource(PerunSession perunSession, List<Group> groups, Resource resource) throws InternalErrorException, GroupNotDefinedOnResourceException, GroupAlreadyRemovedFromResourceException {
		for(Group g: groups) {
			this.removeGroupFromResource(perunSession, g, resource);
		}
	}

	@Override
	public void removeGroupFromResources(PerunSession perunSession, Group group, List<Resource> resources) throws InternalErrorException, GroupNotDefinedOnResourceException, GroupAlreadyRemovedFromResourceException {
		for(Resource r: resources) {
			this.removeGroupFromResource(perunSession, group, r);
		}
	}

	@Override
	public List<Group> getAssignedGroups(PerunSession sess, Resource resource) throws InternalErrorException {
		return getPerunBl().getGroupsManagerBl().getAssignedGroupsToResource(sess, resource);
	}

	@Override
	public List<Group> getAssignedGroups(PerunSession sess, Resource resource, Member member) throws InternalErrorException {
		return getPerunBl().getGroupsManagerBl().getAssignedGroupsToResource(sess, resource, member);
	}

	@Override
	public List<Resource> getAssignedResources(PerunSession sess, Group group) throws InternalErrorException {
		Vo vo = getPerunBl().getGroupsManagerBl().getVo(sess, group);
		return getResourcesManagerImpl().getAssignedResources(sess, vo, group);
	}

	@Override
	public List<RichResource> getAssignedRichResources(PerunSession sess, Group group) throws InternalErrorException {
		return getResourcesManagerImpl().getAssignedRichResources(sess, group);
	}

	@Override
	public void assignService(PerunSession sess, Resource resource, Service service) throws InternalErrorException, ServiceNotExistsException, ServiceAlreadyAssignedException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		getResourcesManagerImpl().assignService(sess, resource, service);
		getPerunBl().getAuditer().log(sess, new ServiceAssignedToResource(service, resource));

		AttributesManagerBl attributesManagerBl = getPerunBl().getAttributesManagerBl();

		try {
			//group and group-resource
			List<Group> groups = getAssignedGroups(sess, resource);
			for(Group group : groups) {
				List<Attribute> attributes;
				attributes = attributesManagerBl.getRequiredAttributes(sess, service, resource, group, true);
				attributes = attributesManagerBl.fillAttributes(sess, resource, group, attributes, true);
				attributesManagerBl.setAttributes(sess, resource, group, attributes, true);
			}

			// call check of facility's resource's member's user's attributes
			Facility facility = getFacility(sess, resource);
			attributesManagerBl.checkAttributesValue(sess, facility, attributesManagerBl.getRequiredAttributes(sess, facility));
			attributesManagerBl.checkAttributesValue(sess, resource, attributesManagerBl.getRequiredAttributes(sess, resource));
			List<Member> members = getAllowedMembers(sess, resource);
			for(Member member : members) {
				User user = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
				// use complex method for getting and setting member-resource, member, user-facility and user-facility required attributes for the service
				getPerunBl().getAttributesManagerBl().setRequiredAttributes(sess, service, facility, resource, user, member);
			}
		} catch(WrongAttributeAssignmentException ex) {
			throw new ConsistencyErrorException(ex);
		} catch(AttributeNotExistsException ex) {
			throw new ConsistencyErrorException(ex);
		} catch (MemberResourceMismatchException ex) {
			throw new ConsistencyErrorException(ex);
		} catch (GroupResourceMismatchException ex) {
			throw new ConsistencyErrorException(ex);
		}
	}

	@Override
	public void assignServicesPackage(PerunSession sess, Resource resource, ServicesPackage servicesPackage) throws InternalErrorException, ServicesPackageNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		for(Service service : getPerunBl().getServicesManagerBl().getServicesFromServicesPackage(sess, servicesPackage)) {
			try {
				this.assignService(sess, resource, service);
			} catch (ServiceNotExistsException e) {
				throw new ConsistencyErrorException("Service from the package doesn't exist", e);
			} catch (ServiceAlreadyAssignedException e) {
				// FIXME a co delat tady? Pravdepodobne muzeme tise ignorovat
			}
		}
		log.info("All services from service package was assigned to the resource. servicesPackage={}, resource={}", servicesPackage, resource);
	}

	@Override
	public void removeService(PerunSession sess, Resource resource, Service service) throws InternalErrorException, ServiceNotExistsException, ServiceNotAssignedException {
		getResourcesManagerImpl().removeService(sess, resource, service);
		getPerunBl().getAuditer().log(sess, new ServiceRemovedFromResource(service, resource));
	}

	@Override
	public void removeServicesPackage(PerunSession sess, Resource resource, ServicesPackage servicesPackage) throws InternalErrorException, ServicesPackageNotExistsException {
		for(Service service : getPerunBl().getServicesManagerBl().getServicesFromServicesPackage(sess, servicesPackage)) {
			try {
				//FIXME odstranit pouze v pripade ze tato service neni v jinem servicesPackage prirazenem na resource
				this.removeService(sess, resource, service);
			} catch (ServiceNotExistsException e) {
				throw new ConsistencyErrorException("Service from the package doesn't exist", e);
			} catch (ServiceNotAssignedException e) {
				// FIXME a co delat tady? Pravdepodobne muzeme tise ignorovat
			}
		}
	}

	@Override
	public List<RichResource> getRichResources(PerunSession sess, Vo vo) throws InternalErrorException {
		return getResourcesManagerImpl().getRichResources(sess, vo);
	}

	@Override
	public List<Resource> getResources(PerunSession sess, Vo vo) throws InternalErrorException {
		return getResourcesManagerImpl().getResources(sess, vo);
	}

	@Override
	public List<Resource> getResourcesByIds(PerunSession sess, List<Integer> resourcesIds) throws InternalErrorException {
		return getResourcesManagerImpl().getResourcesByIds(sess, resourcesIds);
	}

	@Override
	public int getResourcesCount(PerunSession sess, Vo vo) throws InternalErrorException {
		return getResourcesManagerImpl().getResourcesCount(sess, vo);
	}

	@Override
	public int getResourcesCount(PerunSession sess) throws InternalErrorException {
		return getResourcesManagerImpl().getResourcesCount(sess);
	}

	@Override
	public List<Resource> getResourcesByAttribute(PerunSession sess, Attribute attribute) throws InternalErrorException, WrongAttributeAssignmentException {
		getPerunBl().getAttributesManagerBl().checkNamespace(sess, attribute, AttributesManager.NS_RESOURCE_ATTR);
		if(!(getPerunBl().getAttributesManagerBl().isDefAttribute(sess, attribute) || getPerunBl().getAttributesManagerBl().isOptAttribute(sess, attribute))) throw new WrongAttributeAssignmentException("This method can process only def and opt attributes");
		return getResourcesManagerImpl().getResourcesByAttribute(sess, attribute);

	}

	@Override
	public List<Resource> getAllowedResources(PerunSession sess, Member member) throws InternalErrorException {
		if(!getPerunBl().getMembersManagerBl().haveStatus(sess, member, Status.INVALID)) {
			return getAssignedResources(sess, member);
		} else {
			return new ArrayList<Resource>();
		}
	}

	@Override
	public List<Resource> getAllowedResources(PerunSession sess, Facility facility, User user) throws InternalErrorException {
		return getResourcesManagerImpl().getAllowedResources(sess, facility, user);
	}

	@Override
	public List<Resource> getAssignedResources(PerunSession sess, User user, Vo vo) throws InternalErrorException {
		return getResourcesManagerImpl().getAssignedResources(sess, user, vo);
	}

	@Override
	public List<Resource> getAssignedResources(PerunSession sess, Member member) throws InternalErrorException {
		return getResourcesManagerImpl().getAssignedResources(sess, member);
	}

	@Override
	public List<Resource> getAssignedResources(PerunSession sess, Member member, Service service) throws InternalErrorException {
		return getResourcesManagerImpl().getAssignedResources(sess, member, service);
	}

	@Override
	public List<RichResource> getAssignedRichResources(PerunSession sess, Member member) throws InternalErrorException {
		return getResourcesManagerImpl().getAssignedRichResources(sess, member);
	}

	@Override
	public List<RichResource> getAssignedRichResources(PerunSession sess, Member member, Service service) throws InternalErrorException {
		return getResourcesManagerImpl().getAssignedRichResources(sess, member, service);
	}

	@Override
	public ResourceTag createResourceTag(PerunSession perunSession, ResourceTag resourceTag, Vo vo) throws InternalErrorException {
		return getResourcesManagerImpl().createResourceTag(perunSession, resourceTag, vo);
	}

	@Override
	public ResourceTag updateResourceTag(PerunSession perunSession, ResourceTag resourceTag) throws InternalErrorException {
		return getResourcesManagerImpl().updateResourceTag(perunSession, resourceTag);
	}

	@Override
	public void deleteResourceTag(PerunSession perunSession, ResourceTag resourceTag) throws InternalErrorException, ResourceTagAlreadyAssignedException {
		List<Resource> tagResources = this.getAllResourcesByResourceTag(perunSession, resourceTag);
		if(!tagResources.isEmpty()) throw new ResourceTagAlreadyAssignedException("The resourceTag is alreadyUsed for some resources.", resourceTag);
		getResourcesManagerImpl().deleteResourceTag(perunSession, resourceTag);
	}

	@Override
	public void deleteAllResourcesTagsForVo(PerunSession perunSession, Vo vo) throws InternalErrorException, ResourceTagAlreadyAssignedException {
		List<ResourceTag> resourcesTagForVo = this.getAllResourcesTagsForVo(perunSession, vo);
		for(ResourceTag rt: resourcesTagForVo) {
			List<Resource> tagResources = this.getAllResourcesByResourceTag(perunSession, rt);
			if(!tagResources.isEmpty()) throw new ResourceTagAlreadyAssignedException("The resourceTag is alreadyUsed for some resources.", rt);
		}
		getResourcesManagerImpl().deleteAllResourcesTagsForVo(perunSession, vo);
	}

	@Override
	public void assignResourceTagToResource(PerunSession perunSession, ResourceTag resourceTag, Resource resource) throws InternalErrorException, ResourceTagAlreadyAssignedException {
		List<ResourceTag> allResourceTags = this.getAllResourcesTagsForResource(perunSession, resource);
		if(allResourceTags.contains(resourceTag)) throw new ResourceTagAlreadyAssignedException(resourceTag);
		getResourcesManagerImpl().assignResourceTagToResource(perunSession, resourceTag, resource);
	}

	@Override
	public void removeResourceTagFromResource(PerunSession perunSession, ResourceTag resourceTag, Resource resource) throws InternalErrorException, ResourceTagNotAssignedException {
		List<ResourceTag> allResourceTags = this.getAllResourcesTagsForResource(perunSession, resource);
		if(!allResourceTags.contains(resourceTag)) throw new ResourceTagNotAssignedException(resourceTag);
		getResourcesManagerImpl().removeResourceTagFromResource(perunSession, resourceTag, resource);
	}

	@Override
	public void removeAllResourcesTagFromResource(PerunSession perunSession, Resource resource) throws InternalErrorException {
		getResourcesManagerImpl().removeAllResourcesTagFromResource(perunSession, resource);
	}

	@Override
	public List<Resource> getAllResourcesByResourceTag(PerunSession perunSession, ResourceTag resourceTag) throws InternalErrorException {
		return getResourcesManagerImpl().getAllResourcesByResourceTag(perunSession, resourceTag);
	}

	@Override
	public List<ResourceTag> getAllResourcesTagsForVo(PerunSession perunSession, Vo vo) throws InternalErrorException {
		return getResourcesManagerImpl().getAllResourcesTagsForVo(perunSession, vo);
	}

	@Override
	public List<ResourceTag> getAllResourcesTagsForResource(PerunSession perunSession, Resource resource) throws InternalErrorException {
		return getResourcesManagerImpl().getAllResourcesTagsForResource(perunSession, resource);
	}

	@Override
	public void checkResourceExists(PerunSession sess, Resource resource) throws InternalErrorException, ResourceNotExistsException {
		getResourcesManagerImpl().checkResourceExists(sess, resource);
	}

	@Override
	public void checkResourceTagExists(PerunSession sess, ResourceTag resourceTag) throws InternalErrorException, ResourceTagNotExistsException {
		getResourcesManagerImpl().checkResourceTagExists(sess, resourceTag);
	}

	@Override
	public Resource updateResource(PerunSession sess, Resource resource) throws InternalErrorException, ResourceExistsException {
		Facility facility = getFacility(sess, resource);
		Vo vo = getVo(sess, resource);

		try {
			Resource existingResource = getResourcesManagerImpl().getResourceByName(sess, vo, facility, resource.getName());

			// if it is the same resource which is updated but the name stayed the same.
			if (existingResource.getId() == resource.getId()) {
				resource = getResourcesManagerImpl().updateResource(sess, resource);
				getPerunBl().getAuditer().log(sess, new ResourceUpdated(resource));
				return resource;
			}
			// if it is not the same resource - throw the exception. Resource can not be updated,
			// because there is already a resource with this name but with different id.
			throw new ResourceExistsException(existingResource);
		} catch (ResourceNotExistsException e) {
			resource = getResourcesManagerImpl().updateResource(sess, resource);
			getPerunBl().getAuditer().log(sess, new ResourceUpdated(resource));
		}

		return resource;
	}

	@Override
	public void copyAttributes(PerunSession sess, Resource sourceResource, Resource destinationResource) throws InternalErrorException, WrongReferenceAttributeValueException {
		List<Attribute> sourceAttributes = getPerunBl().getAttributesManagerBl().getAttributes(sess, sourceResource);
		List<Attribute> destinationAttributes = getPerunBl().getAttributesManagerBl().getAttributes(sess, destinationResource);

		// do not get virtual attributes from source resource, they can't be set to destination
		Iterator<Attribute> it = sourceAttributes.iterator();
		while (it.hasNext()) {
			if (it.next().getNamespace().startsWith(AttributesManager.NS_RESOURCE_ATTR_VIRT)) {
				it.remove();
			}
		}

		// create intersection of destination and source attributes
		List<Attribute> intersection = new ArrayList<>();
		intersection.addAll(destinationAttributes);
		intersection.retainAll(sourceAttributes);

		try {
			// delete all common attributes from destination resource
			getPerunBl().getAttributesManagerBl().removeAttributes(sess, destinationResource, intersection);
			// add all attributes from the source resource to the destination resource
			getPerunBl().getAttributesManagerBl().setAttributes(sess, destinationResource, sourceAttributes);
		} catch (WrongAttributeAssignmentException ex) {
			throw new InternalErrorException("Copying of attributes failed, wrong assignment.", ex);
		} catch (WrongAttributeValueException ex) {
			throw new ConsistencyErrorException("Copying of attributes failed.", ex);
		}

	}

	@Override
	public void copyServices(PerunSession sess, Resource sourceResource, Resource destinationResource) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		for (Service owner : getAssignedServices(sess, sourceResource)) {
			try {
				assignService(sess, destinationResource, owner);
			} catch (ServiceAlreadyAssignedException ex) {
				// we can ignore the exception in this particular case, service can exists in both of the resources
			} catch (ServiceNotExistsException ex) {
				throw new InternalErrorException("Service from source Resource does not exists when copying services.", ex);
			}
		}
	}

	@Override
	public void copyGroups(PerunSession sess, Resource sourceResource, Resource destinationResource) throws InternalErrorException {
		for (Group group: getAssignedGroups(sess, sourceResource)) {
			try {
				assignGroupToResource(sess, group, destinationResource);
			} catch (GroupAlreadyAssignedException ex) {
				// we can ignore the exception in this particular case, group can exists in both of the resources
			} catch (WrongAttributeValueException ex) {
				throw new InternalErrorException("Copying of groups failed.", ex);
			} catch (WrongReferenceAttributeValueException ex) {
				throw new InternalErrorException("Copying of groups failed.", ex);
			}
		}
	}

	@Override
	public List<User> getAdmins(PerunSession perunSession, Resource resource, boolean onlyDirectAdmins) throws InternalErrorException {
		if(onlyDirectAdmins) {
			return getResourcesManagerImpl().getDirectAdmins(perunSession, resource);
		} else {
			return getResourcesManagerImpl().getAdmins(perunSession, resource);
		}
	}

	@Override
	public List<RichUser> getRichAdmins(PerunSession perunSession, Resource resource, List<String> specificAttributes, boolean allUserAttributes, boolean onlyDirectAdmins) throws InternalErrorException, UserNotExistsException {
		List<User> users = this.getAdmins(perunSession, resource, onlyDirectAdmins);
		List<RichUser> richUsers;

		if(allUserAttributes) {
			richUsers = perunBl.getUsersManagerBl().getRichUsersWithAttributesFromListOfUsers(perunSession, users);
		} else {
			try {
				richUsers = getPerunBl().getUsersManagerBl().convertUsersToRichUsersWithAttributes(perunSession, perunBl.getUsersManagerBl().getRichUsersFromListOfUsers(perunSession, users), getPerunBl().getAttributesManagerBl().getAttributesDefinition(perunSession, specificAttributes));
			} catch (AttributeNotExistsException ex) {
				throw new InternalErrorException("One of Attribute not exist.", ex);
			}
		}
		return richUsers;
	}

	@Override
	public List<Resource> getResourcesWhereUserIsAdmin(PerunSession sess, User user) throws InternalErrorException {
		return resourcesManagerImpl.getResourcesWhereUserIsAdmin(sess, user);
	}

	@Override
	public List<Resource> getResourcesWhereUserIsAdmin(PerunSession sess, Facility facility, Vo vo, User authorizedUser) throws InternalErrorException {
		return getResourcesManagerImpl().getResourcesWhereUserIsAdmin(sess, facility, vo, authorizedUser);
	}

	@Override
	public List<Resource> getResourcesWhereGroupIsAdmin(PerunSession sess, Facility facility, Vo vo, Group authorizedGroup) throws InternalErrorException {
		return getResourcesManagerImpl().getResourcesWhereGroupIsAdmin(sess, facility, vo, authorizedGroup);
	}

    @Override
    public List<Group> getAdminGroups(PerunSession sess, Resource resource) throws InternalErrorException {
        return resourcesManagerImpl.getAdminGroups(sess, resource);
    }

	@Override
	public void addAdmin(PerunSession sess, Resource resource, User user) throws InternalErrorException, AlreadyAdminException {
		AuthzResolverBlImpl.setRole(sess, user, resource, Role.RESOURCEADMIN);
		getPerunBl().getAuditer().log(sess, new AdminUserAddedForResource(user, resource));
	}

	@Override
	public void addAdmin(PerunSession sess, Resource resource, Group group) throws InternalErrorException, AlreadyAdminException {
		List<Group> listOfAdmins = getAdminGroups(sess, resource);
		if (listOfAdmins.contains(group)) throw new AlreadyAdminException(group);

		AuthzResolverBlImpl.setRole(sess, group, resource, Role.RESOURCEADMIN);
		getPerunBl().getAuditer().log(sess, new AdminGroupAddedForResource(group, resource));
	}

	@Override
	public void removeAdmin(PerunSession sess, Resource resource, User user) throws InternalErrorException, UserNotAdminException {
		AuthzResolverBlImpl.unsetRole(sess, user, resource, Role.RESOURCEADMIN);
		getPerunBl().getAuditer().log(sess, new AdminUserRemovedForResource(user, resource));
	}

	@Override
	public void removeAdmin(PerunSession sess, Resource resource, Group group) throws InternalErrorException, GroupNotAdminException {
		List<Group> listOfAdmins = getAdminGroups(sess, resource);
		if (!listOfAdmins.contains(group)) throw new GroupNotAdminException(group);

		AuthzResolverBlImpl.unsetRole(sess, group, resource, Role.RESOURCEADMIN);
		getPerunBl().getAuditer().log(sess, new AdminGroupRemovedForResource(group, resource));
	}

	@Override
	public BanOnResource setBan(PerunSession sess, BanOnResource banOnResource) throws InternalErrorException, BanAlreadyExistsException {
		if(this.banExists(sess, banOnResource.getMemberId(), banOnResource.getResourceId())) throw new BanAlreadyExistsException(banOnResource);
		banOnResource = getResourcesManagerImpl().setBan(sess, banOnResource);
		getPerunBl().getAuditer().log(sess, new BanSetForResource(banOnResource, banOnResource.getMemberId(), banOnResource.getResourceId()));
		return banOnResource;
	}

	@Override
	public BanOnResource getBanById(PerunSession sess, int banId) throws InternalErrorException, BanNotExistsException {
		return getResourcesManagerImpl().getBanById(sess, banId);
	}

	@Override
	public boolean banExists(PerunSession sess, int memberId, int resourceId) throws InternalErrorException {
		return getResourcesManagerImpl().banExists(sess, memberId, resourceId);
	}

	@Override
	public boolean banExists(PerunSession sess, int banId) throws InternalErrorException {
		return getResourcesManagerImpl().banExists(sess, banId);
	}

	@Override
	public void checkBanExists(PerunSession sess, int memberId, int resourceId) throws InternalErrorException, BanNotExistsException {
		if(!getResourcesManagerImpl().banExists(sess, memberId, resourceId)) throw new BanNotExistsException("Ban for member " + memberId + " and resource " + resourceId + " not exists!");
	}

	@Override
	public void checkBanExists(PerunSession sess, int banId) throws InternalErrorException, BanNotExistsException {
		if(!getResourcesManagerImpl().banExists(sess, banId)) throw new BanNotExistsException("Ban with id " + banId + " not exists!");
	}

	@Override
	public BanOnResource getBan(PerunSession sess, int memberId, int resourceId) throws InternalErrorException, BanNotExistsException {
		return getResourcesManagerImpl().getBan(sess, memberId, resourceId);
	}

	@Override
	public List<BanOnResource> getBansForMember(PerunSession sess, int memberId) throws InternalErrorException {
		return getResourcesManagerImpl().getBansForMember(sess, memberId);
	}

	@Override
	public List<BanOnResource> getBansForResource(PerunSession sess, int resourceId) throws InternalErrorException {
		return getResourcesManagerImpl().getBansForResource(sess, resourceId);
	}

	@Override
	public List<BanOnResource> getAllExpiredBansOnResources(PerunSession sess) throws InternalErrorException {
		return getResourcesManagerImpl().getAllExpiredBansOnResources(sess);
	}

	@Override
	public BanOnResource updateBan(PerunSession sess, BanOnResource banOnResource) throws InternalErrorException {
		banOnResource = getResourcesManagerImpl().updateBan(sess, banOnResource);
		getPerunBl().getAuditer().log(sess, new BanUpdatedForResource(banOnResource, banOnResource.getMemberId(), banOnResource.getResourceId()));
		return banOnResource;
	}

	@Override
	public void removeBan(PerunSession sess, int banId) throws InternalErrorException, BanNotExistsException {
		BanOnResource ban = this.getBanById(sess, banId);
		getResourcesManagerImpl().removeBan(sess, banId);
		getPerunBl().getAuditer().log(sess, new BanRemovedForResource(ban, ban.getMemberId(), ban.getResourceId()));
	}

	@Override
	public void removeBan(PerunSession sess, int memberId, int resourceId) throws InternalErrorException, BanNotExistsException {
		BanOnResource ban = this.getBan(sess, memberId, resourceId);
		getResourcesManagerImpl().removeBan(sess, memberId, resourceId);
		getPerunBl().getAuditer().log(sess, new BanRemovedForResource(ban, memberId, resourceId));
	}

	@Override
	public void removeAllExpiredBansOnResources(PerunSession sess) throws InternalErrorException {
		List<BanOnResource> expiredBans = this.getAllExpiredBansOnResources(sess);
		for(BanOnResource expiredBan: expiredBans) {
			try {
				this.removeBan(sess, expiredBan.getId());
			} catch (BanNotExistsException ex) {
				log.error("Ban {} can't be removed because it not exists yet.",expiredBan);
				//Skipt this, probably already removed
			}
		}
	}

	@Override
	public List<Resource> getResources(PerunSession sess) throws InternalErrorException {
		return getResourcesManagerImpl().getResources(sess);
	}

	/**
	 * Gets the resourcesManagerImpl.
	 *
	 * @return The resourcesManagerImpl.
	 */
	public ResourcesManagerImplApi getResourcesManagerImpl() {
		return this.resourcesManagerImpl;
	}

	/**
	 * Gets the perunBl.
	 *
	 * @return The perunBl.
	 */
	public PerunBl getPerunBl() {
		return this.perunBl;
	}



	public void setPerunBl(PerunBl perunBl) {
		this.perunBl = perunBl;
	}
}
