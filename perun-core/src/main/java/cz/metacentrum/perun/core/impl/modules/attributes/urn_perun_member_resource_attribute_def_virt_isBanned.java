package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.audit.events.AttributesManagerEvents.AttributeChangedForResourceAndMember;
import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.FacilityManagerEvents.BanRemovedForFacility;
import cz.metacentrum.perun.audit.events.FacilityManagerEvents.BanSetForFacility;
import cz.metacentrum.perun.audit.events.FacilityManagerEvents.BanUpdatedForFacility;
import cz.metacentrum.perun.audit.events.ResourceManagerEvents.BanRemovedForResource;
import cz.metacentrum.perun.audit.events.ResourceManagerEvents.BanSetForResource;
import cz.metacentrum.perun.audit.events.ResourceManagerEvents.BanUpdatedForResource;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ResourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.MemberResourceVirtualAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.MemberResourceVirtualAttributesModuleImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.SkipValueCheckDuringDependencyCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Module for getting information if member is banned on resource.
 *
 * Get true if exists ban of member on resource or his user on facility.
 *
 * @author Michal Stava
 */
@SkipValueCheckDuringDependencyCheck
public class urn_perun_member_resource_attribute_def_virt_isBanned extends MemberResourceVirtualAttributesModuleAbstract implements MemberResourceVirtualAttributesModuleImplApi {

	private final static String A_MR_V_isBanned = AttributesManager.NS_MEMBER_RESOURCE_ATTR_VIRT + ":isBanned";
	private final static Logger log = LoggerFactory.getLogger(urn_perun_member_resource_attribute_def_virt_isBanned.class);

	@Override
    public Attribute getAttributeValue(PerunSessionImpl sess, Member member, Resource resource, AttributeDefinition attributeDefinition) throws InternalErrorException {
        Attribute attribute = new Attribute(attributeDefinition);
		//Default value is false
		attribute.setValue(false);

		Facility facility;
		try {
			facility = sess.getPerunBl().getFacilitiesManagerBl().getFacilityById(sess, resource.getFacilityId());
		} catch (FacilityNotExistsException ex) {
			throw new InternalErrorException("Facility for Resource " + resource + " not exists!");
		}

		User user;
		try {
			user = sess.getPerunBl().getUsersManagerBl().getUserById(sess, member.getUserId());
		} catch (UserNotExistsException ex) {
			throw new InternalErrorException("User for Member " + member + " not exists!");
		}

		//Ban on resource exists?
		if(sess.getPerunBl().getResourcesManagerBl().banExists(sess, member.getId(), resource.getId())) attribute.setValue(true);
		//Ban on facility exists?
        if(sess.getPerunBl().getFacilitiesManagerBl().banExists(sess, user.getId(), facility.getId())) attribute.setValue(true);

        return attribute;

    }

	@Override
	public List<AuditEvent> resolveVirtualAttributeValueChange(PerunSessionImpl perunSession, AuditEvent message) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<AuditEvent> resolvingMessages = new ArrayList<>();
		if (message == null) return resolvingMessages;

		if (message instanceof BanSetForResource) {
			return resolveBanChangedForResource(perunSession, ((BanSetForResource) message).getMemberId(), ((BanSetForResource) message).getResourceId());

		} else if (message instanceof BanRemovedForResource) {
			return resolveBanChangedForResource(perunSession, ((BanRemovedForResource) message).getMemberId(), ((BanRemovedForResource) message).getResourceId());

		} else if (message instanceof BanUpdatedForResource) {
			return resolveBanChangedForResource(perunSession, ((BanUpdatedForResource) message).getMemberId(), ((BanUpdatedForResource) message).getResourceId());

		} else if (message instanceof BanSetForFacility) {
			return resolveBanChangedForFacility(perunSession, ((BanSetForFacility) message).getUserId(), ((BanSetForFacility) message).getFacilityId());

		} else if (message instanceof BanRemovedForFacility) {
			return resolveBanChangedForFacility(perunSession, ((BanRemovedForFacility) message).getUserId(), ((BanRemovedForFacility) message).getFacilityId());

		} else if (message instanceof BanUpdatedForFacility) {
			return resolveBanChangedForFacility(perunSession, ((BanUpdatedForFacility) message).getUserId(), ((BanUpdatedForFacility) message).getFacilityId());
		}

		return resolvingMessages;
	}


	private List<AuditEvent> resolveBanChangedForResource(PerunSessionImpl perunSession, int memberId, int resourceId) throws InternalErrorException, AttributeNotExistsException {
		List<AuditEvent> resolvingMessages = new ArrayList<>();

		try {
			Member member = perunSession.getPerunBl().getMembersManagerBl().getMemberById(perunSession, memberId);
			Resource resource = perunSession.getPerunBl().getResourcesManagerBl().getResourceById(perunSession, resourceId);
			AttributeDefinition attributeDefinition = perunSession.getPerunBl().getAttributesManagerBl().getAttributeDefinition(perunSession, A_MR_V_isBanned);
			resolvingMessages.add(new AttributeChangedForResourceAndMember(new Attribute(attributeDefinition), resource, member));
		} catch (MemberNotExistsException | ResourceNotExistsException e) {
			log.error("Can't resolve virtual attribute value change for " + this.getClass().getSimpleName() + " module because of exception.", e);
		}

		return resolvingMessages;
	}

	private List<AuditEvent> resolveBanChangedForFacility(PerunSessionImpl perunSession, int userId, int facilityId) throws InternalErrorException {
		List<AuditEvent> resolvingMessages = new ArrayList<>();

		try {
			User user = perunSession.getPerunBl().getUsersManagerBl().getUserById(perunSession, userId);
			Facility facility = perunSession.getPerunBl().getFacilitiesManagerBl().getFacilityById(perunSession, facilityId);
			List<Pair<Resource, Member>> listOfAffectedObjects = getAffectedMemberResourceObjects(perunSession, user, facility);

			for (Pair<Resource, Member> affectedObjects : listOfAffectedObjects) {
				try {
					AttributeDefinition attributeDefinition = perunSession.getPerunBl().getAttributesManagerBl().getAttributeDefinition(perunSession, A_MR_V_isBanned);
					resolvingMessages.add(new AttributeChangedForResourceAndMember(new Attribute(attributeDefinition), affectedObjects.getLeft(), affectedObjects.getRight()));
				} catch (AttributeNotExistsException ex) {
					//This means that attribute isBanned not exists at all so we can skip this process
					log.info("Virtual attribute {} not exists.", this.getClass().getSimpleName());
					break;
				}
			}
		} catch (UserNotExistsException | FacilityNotExistsException e) {
			log.error("Can't resolve virtual attribute value change for " + this.getClass().getSimpleName() + " module because of exception.", e);
		}

		return resolvingMessages;
	}

	@Override
    public AttributeDefinition getAttributeDefinition() {
        AttributeDefinition attr = new AttributeDefinition();
        attr.setNamespace(AttributesManager.NS_MEMBER_RESOURCE_ATTR_VIRT);
        attr.setFriendlyName("isBanned");
        attr.setDisplayName("Is banned on Resource or Facility");
        attr.setType(Boolean.class.getName());
        attr.setDescription("True if member is banned on resource or all facility.");
        return attr;
    }

	/**
	 * Get List of pairs of Resource and Member affected by change in Ban (from user and facility)
	 *
	 * @param perunSession
	 * @param user affected user
	 * @param facility affected facility
	 * @return list of pairs of REsource and Members affected by Ban
	 * @throws InternalErrorException
	 */
	private List<Pair<Resource, Member>> getAffectedMemberResourceObjects(PerunSessionImpl perunSession, User user, Facility facility) throws InternalErrorException {
		List<Pair<Resource, Member>> listOfAffectedObjects = new ArrayList<>();
		List<Resource> assignedResources = perunSession.getPerunBl().getFacilitiesManagerBl().getAssignedResources(perunSession, facility);
		for(Resource resource: assignedResources) {
			List<Member> assignedMembers = perunSession.getPerunBl().getResourcesManagerBl().getAssignedMembers(perunSession, resource);
			for(Member member: assignedMembers) {
				if(member.getUserId() == user.getId()) {
					listOfAffectedObjects.add(new Pair(resource, member));
					break;
				}
			}
		}
		return listOfAffectedObjects;
	}

}
