package cz.metacentrum.perun.registrar.modules;

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
import cz.metacentrum.perun.registrar.RegistrarManager;
import cz.metacentrum.perun.registrar.RegistrarModule;
import cz.metacentrum.perun.registrar.exceptions.CantBeApprovedException;
import cz.metacentrum.perun.registrar.exceptions.RegistrarException;
import cz.metacentrum.perun.registrar.model.Application;
import cz.metacentrum.perun.registrar.model.ApplicationFormItemData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Module for BBMRICollections instance.
 * The module
 * 1. reads input with collection IDs and checks, whether the collections exist in Perun as groups
 * 2. adds users to the appropriate groups
 *
 * @author Jiri Mauritz <jirmaurtiz@gmail.com> (original)
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz> (modifications)
 */
public class BBMRICollections implements RegistrarModule {

	private final static Logger log = LoggerFactory.getLogger(BBMRICollections.class);
	private static final String BIOBANK_IDS_FIELD = "Comma or new-line separated list of IDs of collections you are representing:";
	private static final String COLLECTIONS_GROUP_NAME = "collections:bbmriEricDirectory";
	private static final String COLLECTION_ID_ATTR_NAME = "urn:perun:group:attribute-def:def:collectionID";
	private static final String REPRESENTATIVES_GROUP_NAME = "representatives";
	private static final String ADD_NEW_COLLECTIONS_GROUP_NAME = "addNewCollections";

	private RegistrarManager registrar;

	@Override
	public void setRegistrar(RegistrarManager registrar) {
		this.registrar = registrar;
	}

	@Override
	public List<ApplicationFormItemData> createApplication(PerunSession user, Application application, List<ApplicationFormItemData> data) {
		return data;
	}


	/**
	 * Add users to the listed groups.
	 *
	 * @param session who approves the application
	 * @param app application
	 * @return unchanged application
	 * @throws PerunException in case of internal error in Perun
	 */
	@Override
	public Application approveApplication(PerunSession session, Application app) throws VoNotExistsException, UserNotExistsException, PrivilegeException, MemberNotExistsException, InternalErrorException, RegistrarException, GroupNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException, ExternallyManagedException, WrongAttributeValueException, WrongReferenceAttributeValueException, NotGroupMemberException {
		// get perun and beans from session
		Perun perun = session.getPerun();
		Vo vo = app.getVo();
		User user = app.getUser();
		Member member = perun.getMembersManager().getMemberByUser(session, vo, user);

		// get the field of application with the collections
		Set<String> collectionIDsInApplication = getCollectionIDsFromApplication(session, app);

		// get map of collection IDs to group from Perun
		Group collectionsGroup = perun.getGroupsManager().getGroupByName(session, vo, COLLECTIONS_GROUP_NAME);
		Map<String, Group> collectionIDsToGroupsMap = getCollectionIDsToGroupsMap(session, perun, collectionsGroup);

		// add user to all groups from the field on application
		for (String collectionID : collectionIDsInApplication) {
			Group group = collectionIDsToGroupsMap.get(collectionID);
			if (group == null) {
				log.debug("For collection ID " + collectionID + " there is no group in Perun.");
			} else {
				// add user to the group
				try {
					perun.getGroupsManager().addMember(session, group, member);
				} catch (AlreadyMemberException ex) {
					// ignore
				}
			}
		}

		if (app.getGroup() != null && app.getGroup().getName().equals(ADD_NEW_COLLECTIONS_GROUP_NAME)) {
			perun.getGroupsManager().removeMember(session, app.getGroup(), member);
		}

		return app;
	}

	@Override
	public Application rejectApplication(PerunSession session, Application app, String reason) {
		return app;
	}


	@Override
	public Application beforeApprove(PerunSession session, Application app) {
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
		Perun perun = session.getPerun();
		Vo vo = app.getVo();

		// get all collection IDs from Perun
		Group collectionsGroup = perun.getGroupsManager().getGroupByName(session, vo, COLLECTIONS_GROUP_NAME);
		Set<String> collectionIDsInPerun = getCollectionIDs(session, perun, collectionsGroup);


		// get the field of application with the collections
		Set<String> collectionIDsInApplication = getCollectionIDsFromApplication(session, app);

		// get non-existing collections
		collectionIDsInApplication.removeAll(collectionIDsInPerun);

		// difference must be empty
		if (!collectionIDsInApplication.isEmpty()) {
			throw new CantBeApprovedException("Collections " + collectionIDsInApplication + " do not exist." +
					"If you approve the application, these collections will be skipped.", "", "", "", true);
		}
	}

	@Override
	public void canBeSubmitted(PerunSession session, Map<String, String> params) {

	}

	/**
	 * Gets collection IDs from a field on the application form with short name.
	 *
	 * @return collection IDs set
	 */
	private Set<String> getCollectionIDsFromApplication(PerunSession session, Application app) throws RegistrarException, PrivilegeException, InternalErrorException {
		String collectionsString = null;
		List<ApplicationFormItemData> formData = registrar.getApplicationDataById(session, app.getId());
		for (ApplicationFormItemData field : formData) {
			if (BIOBANK_IDS_FIELD.equals(field.getShortname())) {
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
	private Map<String, Group> getCollectionIDsToGroupsMap (PerunSession session, Perun perun, Group collectionsGroup) throws GroupNotExistsException, WrongAttributeAssignmentException, InternalErrorException, AttributeNotExistsException, PrivilegeException {
		Map<String, Group> collectionIDsToGroupMap = new HashMap<>();
		for (Group group : perun.getGroupsManager().getSubGroups(session, collectionsGroup)) {
			for (Group subgroup : perun.getGroupsManager().getSubGroups(session, group)) {
				if (REPRESENTATIVES_GROUP_NAME.equals(subgroup.getShortName())) {
					Attribute collectionID = perun.getAttributesManager().getAttribute(session, subgroup, COLLECTION_ID_ATTR_NAME);
					collectionIDsToGroupMap.put(collectionID.valueAsString(), subgroup);
				}
			}
		}

		return collectionIDsToGroupMap;
	}

	private Set<String> getCollectionIDs(PerunSession session, Perun perun, Group collectionsGroup) throws InternalErrorException, PrivilegeException, WrongAttributeAssignmentException, AttributeNotExistsException, GroupNotExistsException {
		return getCollectionIDsToGroupsMap(session, perun, collectionsGroup).keySet();
	}
}
