package cz.metacentrum.perun.registrar.modules;

import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.registrar.RegistrarManager;
import cz.metacentrum.perun.registrar.RegistrarModule;
import cz.metacentrum.perun.registrar.exceptions.CantBeApprovedException;
import cz.metacentrum.perun.registrar.model.Application;
import cz.metacentrum.perun.registrar.model.ApplicationFormItemData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Module for BBMRI instance.
 * The module
 * 1. reads input with collection IDs and checks, whether the collections exist in Perun as groups
 * 2. adds users to the appropriate groups
 *
 * @author Jiri Mauritz <jirmaurtiz@gmail.com>
 */
public class BBMRI implements RegistrarModule {

	final static Logger log = LoggerFactory.getLogger(BBMRI.class);
	public static final String BIOBANK_IDS_FIELD = "Comma or new-line separated list of collection IDs you are representing:";

	private RegistrarManager registrar;

	@Override
	public void setRegistrar(RegistrarManager registrar) {
		this.registrar = registrar;
	}

	@Override
	public List<ApplicationFormItemData> createApplication(PerunSession user, Application application, List<ApplicationFormItemData> data) throws PerunException {
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
	public Application approveApplication(PerunSession session, Application app) throws PerunException {
		// get perun and beans from session
		Perun perun = session.getPerun();
		Vo vo = app.getVo();
		User user = app.getUser();
		Member member = perun.getMembersManager().getMemberByUser(session, vo, user);

		// get the field of application with the collections
		Set<String> collectionIDsInApplication = getCollectionIDsFromApplication(session, app);

		// get map of collection IDs to group from Perun
		Map<String, Group> collectionIDsToGroupsMap = new HashMap<>();
		Group collectionsGroup = perun.getGroupsManager().getGroupByName(session, vo, "collections");
		for (Group group : perun.getGroupsManager().getSubGroups(session, collectionsGroup)) {
			for (Group subgroup : perun.getGroupsManager().getSubGroups(session, group)) {
				if ("representatives".equals(subgroup.getShortName())) {
					Attribute collectionID = perun.getAttributesManager().getAttribute(session, subgroup, "urn:perun:group:attribute-def:def:collectionID");
					collectionIDsToGroupsMap.put((String) collectionID.getValue(), subgroup);
				}
			}
		}

		// add user to all groups from the field on application
		for (String collectionID : collectionIDsInApplication) {
			Group group = collectionIDsToGroupsMap.get(collectionID);
			if (group == null) {
				log.debug("For collection ID " + collectionID + " is no group in Perun.");
			} else {
				// add user to the group
				perun.getGroupsManager().addMember(session, group, member);
			}
		}

		if (app.getGroup() != null && app.getGroup().getName().equals("addNewCollections")) {
			perun.getGroupsManager().removeMember(session, app.getGroup(), member);
		}

		return app;
	}

	@Override
	public Application rejectApplication(PerunSession session, Application app, String reason) throws PerunException {
		return app;
	}


	@Override
	public Application beforeApprove(PerunSession session, Application app) throws PerunException {
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
		Set<String> collectionIDsInPerun = new HashSet<>();
		Group collectionsGroup = perun.getGroupsManager().getGroupByName(session, vo, "collections");
		for (Group group : perun.getGroupsManager().getSubGroups(session, collectionsGroup)) {
			for (Group subgroup : perun.getGroupsManager().getSubGroups(session, group)) {
				if ("representatives".equals(subgroup.getShortName())) {
					Attribute collectionID = perun.getAttributesManager().getAttribute(session, subgroup, "urn:perun:group:attribute-def:def:collectionID");
					collectionIDsInPerun.add((String) collectionID.getValue());
				}
			}
		}

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
	public void canBeSubmitted(PerunSession session, Map<String, String> params) throws PerunException {

	}

	/**
	 * Gets collection IDs from a field on the application form with short name.
	 *
	 * @return collection IDs set
	 */
	private Set<String> getCollectionIDsFromApplication(PerunSession session, Application app) throws PerunException {
		String collectionsString = null;
		List<ApplicationFormItemData> formData = registrar.getApplicationDataById(session, app.getId());
		for (ApplicationFormItemData field : formData) {
			if (BIOBANK_IDS_FIELD.equals(field.getShortname())) {
				collectionsString = field.getValue();
				break;
			}
		}

		if (collectionsString == null) {
			throw new InternalErrorException("There is no field with biobank IDs on the registration form.");
		}

		// get set of collection IDs from application
		Set<String> collectionIDsInApplication = new HashSet<>();
		for (String collection : collectionsString.split("[,\n ]+")) {
			collectionIDsInApplication.add(collection.trim());
		}

		return collectionIDsInApplication;
	}
}
