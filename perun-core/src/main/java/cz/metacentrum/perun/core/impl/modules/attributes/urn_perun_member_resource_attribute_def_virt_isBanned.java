package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.BanOnFacility;
import cz.metacentrum.perun.core.api.BanOnResource;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MemberResourceMismatchException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceMemberVirtualAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceMemberVirtualAttributesModuleImplApi;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Module for getting information if member is banned on resource.
 *
 * Get true if exists ban of member on resource or his user on facility.
 *
 * @author Michal Stava
 */
public class urn_perun_member_resource_attribute_def_virt_isBanned extends ResourceMemberVirtualAttributesModuleAbstract implements ResourceMemberVirtualAttributesModuleImplApi {

	private final static Logger log = LoggerFactory.getLogger(urn_perun_member_resource_attribute_def_virt_isBanned.class);

	private final Pattern banModification = Pattern.compile("Ban ([a-zA-Z]+):\\[(.|\\s)*\\] was (set|removed|updated) for (member|user)Id ([0-9]+) on (resource|facility)Id ([0-9]+)", Pattern.MULTILINE);
	private final String OPERATION_SET = "set";
	private final String OPERATION_REMOVED = "removed";
	private final String OPERATION_UPDATED = "updated";

	@Override
    public Attribute getAttributeValue(PerunSessionImpl sess, Resource resource, Member member, AttributeDefinition attributeDefinition) throws InternalErrorException {
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
	public List<String> resolveVirtualAttributeValueChange(PerunSessionImpl perunSession, String message) throws InternalErrorException, WrongReferenceAttributeValueException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<String> resolvingMessages = new ArrayList<>();
		if(message == null) return resolvingMessages;
		
		Matcher banModificationMatcher = banModification.matcher(message);
		List<Pair<Resource, Member>> listOfAffectedObjects = new ArrayList<>();

		String operationType = "";

		if(banModificationMatcher.find()) {
			try {
				String banType = banModificationMatcher.group(1);
				operationType = banModificationMatcher.group(3);
				int firstHolderId = Integer.valueOf(banModificationMatcher.group(5));
				int secondHolderId = Integer.valueOf(banModificationMatcher.group(7));

				if(operationType.equals(OPERATION_UPDATED)) {
					operationType = OPERATION_SET;
				} else if (!operationType.equals(OPERATION_SET) && !operationType.equals(OPERATION_REMOVED)) {
					throw new InternalErrorException("Type of operation '" + operationType + "' is unknown by module.");
				}

				if(banType.equals(BanOnResource.class.getSimpleName())) {
					Member member = perunSession.getPerunBl().getMembersManagerBl().getMemberById(perunSession, firstHolderId);
					Resource resource = perunSession.getPerunBl().getResourcesManagerBl().getResourceById(perunSession, secondHolderId);
					listOfAffectedObjects.add(new Pair(resource, member));
				} else if(banType.equals(BanOnFacility.class.getSimpleName())) {
					User user = perunSession.getPerunBl().getUsersManagerBl().getUserById(perunSession, firstHolderId);
					Facility facility = perunSession.getPerunBl().getFacilitiesManagerBl().getFacilityById(perunSession, secondHolderId);
					listOfAffectedObjects = getAffectedMemberResourceObjects(perunSession, user, facility);
				} else {
					throw new InternalErrorException("Type of ban '" + banType + "' is unkown by module.");
				}
			} catch (Exception e) {
				log.error("Can't resolve virtual attribute value change for " + this.getClass().getSimpleName() + " module because of exception.", e);
				//return empty array, do not throw exception because it can create problems
				return new ArrayList<>();
			}
		}

		for(Pair<Resource, Member> affectedObjects : listOfAffectedObjects) {
			try {
				Attribute attrVirtMemberResourceIsBanned = perunSession.getPerunBl().getAttributesManagerBl().getAttribute(perunSession, affectedObjects.getLeft(), affectedObjects.getRight(), AttributesManager.NS_MEMBER_RESOURCE_ATTR_VIRT + ":isBanned");

				resolvingMessages.add(attrVirtMemberResourceIsBanned.serializeToString() + " " + operationType + " for " + affectedObjects.getLeft().serializeToString() + " and " + affectedObjects.getRight().serializeToString());
			} catch (AttributeNotExistsException ex) {
				//This means that attribute isBanned not exists at all so we can skip this process
				log.info("Virtual attribute {} not exists.", this.getClass().getSimpleName());
				break;
			} catch (MemberResourceMismatchException ex) {
				throw new InternalErrorException(ex);
			}
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
