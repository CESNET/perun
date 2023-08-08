package cz.metacentrum.perun.registrar.modules;

import com.google.common.base.Strings;
import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.api.exceptions.AlreadyMemberException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ExternallyManagedException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.NotGroupMemberException;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.registrar.exceptions.CantBeApprovedException;
import cz.metacentrum.perun.registrar.exceptions.RegistrarException;
import cz.metacentrum.perun.registrar.model.Application;
import cz.metacentrum.perun.registrar.model.ApplicationFormItemData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Registration module for BBMRI Collections
 * Module:
 * 1. reads input with collection IDs and checks, whether groups representing collections exist
 *    - group representing collection has attribute CollectionID assigned and value represents the ID
 * 2. adds users to the appropriate groups
 *
 * @author Jiri Mauritz <jirmaurtiz@gmail.com> (original)
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz> (modifications)
 */
@Deprecated
public class BBMRICollections extends DefaultRegistrarModule {

	private final static Logger log = LoggerFactory.getLogger(BBMRICollections.class);

	private static final String COLLECTION_IDS_FIELD = "collectionIds";
	private static final String DIRECTORY_GROUP_FIELD = "directory";
	private static final String COLLECTION_ID_ATTR_NAME = "urn:perun:group:attribute-def:def:collectionID";

	/**
	 * Find groups representing collections by input. Groups are looked for in subgroups
	 * of group the module is assigned to. Add user as a member to the groups.
	 *
	 * @param session who approves the application
	 * @param app application
	 * @return unchanged application
	 * @throws PerunException in case of internal error in Perun
	 */
	@Override
	public Application approveApplication(PerunSession session, Application app) throws VoNotExistsException, UserNotExistsException, PrivilegeException, MemberNotExistsException, RegistrarException, GroupNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException, ExternallyManagedException, WrongAttributeValueException, WrongReferenceAttributeValueException, NotGroupMemberException {
		// get perun and beans from session
		PerunBl perun = (PerunBl)session.getPerun();
		Vo vo = app.getVo();
		User user = app.getUser();
		Member member = perun.getMembersManagerBl().getMemberByUser(session, vo, user);

		// get the field of application with the collections
		Set<String> collectionIDsInApplication = getCollectionIDsFromApplication(session, app);

		// get map of collection IDs to group from Perun
		String directoryGroupName = this.getDirectoryGroupNameFromApplication(session, app);
		Group directoryGroup;
		try {
			directoryGroup = perun.getGroupsManager().getGroupByName(session, vo, directoryGroupName);
		} catch (GroupNotExistsException e) {
			throw new InternalErrorException("Target group does not exist");
		}
		Map<String, Group> collectionIDsToGroupsMap = getCollectionIDsToGroupsMap(session, perun, directoryGroup);

		// add user to all groups from the field on application
		for (String collectionID : collectionIDsInApplication) {
			Group collection = collectionIDsToGroupsMap.get(collectionID);
			if (collection == null) {
				log.debug("There are no groups for collectionID: {}", collectionID);
			} else {
				try {
					perun.getGroupsManager().addMember(session, collection, member);
				} catch (AlreadyMemberException ex) {
					// ignore
				}
			}
		}
		try {
			perun.getGroupsManager().removeMember(session, app.getGroup(), member);
		} catch (MemberNotExistsException | NotGroupMemberException e) {
			//we can ignore these exceptions
		}

		return app;
	}

	/**
	 * Checks whether all collection IDs found in user input really exists in Perun.
	 * If not, CantBeApproved exception is thrown.
	 *
	 * @param session who approves the application
	 * @param app     unchanged application
	 * @throws CantBeApprovedException if at least one collection ID does not exist in Perun
	 */
	public void canBeApproved(PerunSession session, Application app) throws PerunException {
		// get perun and beans from session
		PerunBl perun = (PerunBl)session.getPerun();
		Vo vo = app.getVo();

		// get all collection IDs from Perun
		String directoryGroupName = getDirectoryGroupNameFromApplication(session, app);
		Group directoryGroup;
		try {
			directoryGroup = perun.getGroupsManager().getGroupByName(session, vo, directoryGroupName);
		} catch (GroupNotExistsException e) {
			throw new InternalErrorException("Target group does not exist");
		}
		Set<String> collectionIDsInPerun = getCollectionIDs(session, perun, directoryGroup);

		// get the field of application with the collections
		Set<String> collectionIDsInApplication = getCollectionIDsFromApplication(session, app);

		// get non-existing collections
		collectionIDsInApplication.removeAll(collectionIDsInPerun);

		// difference must be empty
		if (!collectionIDsInApplication.isEmpty()) {
			throw new CantBeApprovedException("Collections " + collectionIDsInApplication + " do not exist." +
					"If you approve the application, these collections will be skipped.", "", "", "", true, app.getId());
		}
	}

	/**
	 * Gets name of target group, where subgroups representing collections are placed.
	 *
	 * @return collection IDs set
	 */
	private String getDirectoryGroupNameFromApplication(PerunSession session, Application app)
			throws RegistrarException, PrivilegeException
	{
		String directoryGroupName = null;
		List<ApplicationFormItemData> formData = registrar.getApplicationDataById(session, app.getId());
		for (ApplicationFormItemData field : formData) {
			if (DIRECTORY_GROUP_FIELD.equals(field.getShortname())) {
				directoryGroupName = field.getValue();
				break;
			}
		}

		if (directoryGroupName == null) {
			throw new InternalErrorException("There is no field with target group name on the registration form.");
		}

		return directoryGroupName;
	}

	/**
	 * Gets collection IDs from a field on the application form with short name.
	 *
	 * @return collection IDs set
	 */
	private Set<String> getCollectionIDsFromApplication(PerunSession session, Application app) throws RegistrarException, PrivilegeException {
		String collectionsString = null;
		List<ApplicationFormItemData> formData = registrar.getApplicationDataById(session, app.getId());
		for (ApplicationFormItemData field : formData) {
			if (COLLECTION_IDS_FIELD.equals(field.getShortname())) {
				collectionsString = field.getValue();
				break;
			}
		}

		if (collectionsString == null) {
			throw new InternalErrorException("There is no field with collection IDs on the registration form.");
		}

		// get set of collection IDs from application
		Set<String> collectionIDsInApplication = new HashSet<>();
		for (String collection : collectionsString.split("[,\n ]+")) {
			collectionIDsInApplication.add(collection.trim());
		}

		return collectionIDsInApplication;
	}

	/**
	 * Gets collections as map of collectionID => Group.
	 *
	 * @return Map of collection IDs to group.
	 */
	private Map<String, Group> getCollectionIDsToGroupsMap(PerunSession session,
														   PerunBl perun,
														   Group directoryGroup)
			throws WrongAttributeAssignmentException, AttributeNotExistsException
	{
		Map<String, Group> collectionIDsToGroupMap = new HashMap<>();

		List<Group> collectionGroups = perun.getGroupsManagerBl().getSubGroups(session, directoryGroup);
		if (collectionGroups == null || collectionGroups.isEmpty()) {
			log.debug("No collection groups found, returning empty map.");
			return collectionIDsToGroupMap;
		}

		for (Group collectionGroup : collectionGroups) {
			Attribute collectionIDAttr = perun.getAttributesManagerBl()
				.getAttribute(session, collectionGroup, COLLECTION_ID_ATTR_NAME);

			if (collectionIDAttr == null || Strings.isNullOrEmpty(collectionIDAttr.valueAsString())) {
				log.warn("Found collection group ({}) without value in attr {}: ({})",
					collectionGroup, COLLECTION_ID_ATTR_NAME, collectionIDAttr);
			} else {
				collectionIDsToGroupMap.put(collectionIDAttr.valueAsString(), collectionGroup);
			}
		}

		return collectionIDsToGroupMap;
	}

	private Set<String> getCollectionIDs(PerunSession session, PerunBl perun, Group collectionsGroup)
			throws WrongAttributeAssignmentException, AttributeNotExistsException
	{
		return getCollectionIDsToGroupsMap(session, perun, collectionsGroup).keySet();
	}

}
