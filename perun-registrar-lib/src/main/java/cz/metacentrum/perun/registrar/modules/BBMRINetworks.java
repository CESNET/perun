package cz.metacentrum.perun.registrar.modules;

import com.google.common.base.Strings;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
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
 * Module for BBMRINetworks instance.
 * The module
 * 1. reads input with networks IDs and checks, whether the networks exist in Perun as groups
 * 2. adds users to the appropriate groups
 *
 * @author Jiri Mauritz <jirmaurtiz@gmail.com> (original)
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz> (modifications)
 */
public class BBMRINetworks extends DefaultRegistrarModule {

	private final static Logger log = LoggerFactory.getLogger(BBMRINetworks.class);
	private static final String NETWORK_IDS_FIELD = "networkIds";
	private static final String NETWORKS_GROUP_NAME = "networks";
	private static final String NETWORK_ID_ATTR_NAME = "urn:perun:group:attribute-def:def:networkID";

	/**
	 * Add users to the listed groups.
	 *
	 * @param session who approves the application
	 * @param app application
	 * @return unchanged application
	 * @throws PerunException in case of internal error in Perun
	 */
	public Application approveApplication(PerunSession session, Application app) throws VoNotExistsException, UserNotExistsException, PrivilegeException, MemberNotExistsException, RegistrarException, GroupNotExistsException, ExternallyManagedException, WrongAttributeAssignmentException, AttributeNotExistsException, WrongReferenceAttributeValueException, WrongAttributeValueException, NotGroupMemberException {
		// get perun and beans from session
		PerunBl perun = (PerunBl)session.getPerun();
		Vo vo = app.getVo();
		User user = app.getUser();
		Member member = perun.getMembersManagerBl().getMemberByUser(session, vo, user);

		// get the field of application with the collections
		Set<String> networkIDsInApplication = getNetworkIDsFromApplication(session, app);

		// get map of collection IDs to group from Perun
		Group networksGroup = perun.getGroupsManagerBl().getGroupByName(session, vo, NETWORKS_GROUP_NAME);
		Map<String, Group> networkIDsToGroupsMap = getNetworkIDsToGroupsMap(session, perun, networksGroup);

		// add user to all groups from the field on application
		for (String networkID : networkIDsInApplication) {
			Group group = networkIDsToGroupsMap.get(networkID);
			if (group == null) {
				log.debug("For network ID: " + networkID + " there is no group in Perun.");
			} else {
				try {
					perun.getGroupsManager().addMember(session, group, member);
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
	 * Checks whether all network IDs found in user input really exists in Perun.
	 * If not, CantBeApproved exception is thrown.
	 *
	 * @param session who approves the application
	 * @param app     unchanged application
	 * @throws CantBeApprovedException if at least one network ID does not exist in Perun
	 */
	public void canBeApproved(PerunSession session, Application app) throws PerunException {
		// get perun and beans from session
		PerunBl perun = (PerunBl)session.getPerun();
		Vo vo = app.getVo();

		// get all network IDs from Perun
		Group networksGroup = perun.getGroupsManagerBl().getGroupByName(session, vo, NETWORKS_GROUP_NAME);
		Set<String> neworksIDsInPerun = getNetworkIDs(session, perun, networksGroup);

		// get the field of application with the collections
		Set<String> networkIDsInApplication = getNetworkIDsFromApplication(session, app);

		// get non-existing collections
		networkIDsInApplication.removeAll(neworksIDsInPerun);

		// difference must be empty
		if (!networkIDsInApplication.isEmpty()) {
			throw new CantBeApprovedException("Networks with IDs: " + networkIDsInApplication + " do not exist." +
					"If you approve the application, these networks will be skipped.", "", "", "", true, app.getId());
		}
	}

	/**
	 * Gets network IDs from a field on the application form with short name.
	 *
	 * @return network IDs set
	 */
	private Set<String> getNetworkIDsFromApplication(PerunSession session, Application app) throws RegistrarException, PrivilegeException {
		String networksString = null;
		List<ApplicationFormItemData> formData = registrar.getApplicationDataById(session, app.getId());
		for (ApplicationFormItemData field : formData) {
			if (NETWORK_IDS_FIELD.equals(field.getShortname())) {
				networksString = field.getValue();
				break;
			}
		}

		if (networksString == null) {
			throw new InternalErrorException("There is no field with network IDs on the registration form.");
		}

		// get set of network IDs from application
		Set<String> networkIDsInApplication = new HashSet<>();
		for (String network : networksString.split("[,\n ]+")) {
			networkIDsInApplication.add(network.trim());
		}

		return networkIDsInApplication;
	}

	/**
	 * Gets collections as map of collectionID => Group.
	 *
	 * @return Map of collection IDs to group.
	 */
	private Map<String, Group> getNetworkIDsToGroupsMap (PerunSession session,
														 PerunBl perun,
														 Group networksGroup)
		throws WrongAttributeAssignmentException, AttributeNotExistsException
	{
		Map<String, Group> networkIDsToGroupMap = new HashMap<>();
		List<Group> networkGroups = perun.getGroupsManagerBl().getSubGroups(session, networksGroup);
		if (networkGroups == null || networkGroups.isEmpty()) {
			log.debug("No network groups found, returning empty map.");
			return networkIDsToGroupMap;
		}

		for (Group networkGroup: networkGroups) {
			Attribute networkIDAttr = perun.getAttributesManagerBl()
				.getAttribute(session, networkGroup, NETWORK_ID_ATTR_NAME);

			if (networkIDAttr == null || Strings.isNullOrEmpty(networkIDAttr.valueAsString())) {
				log.warn("Found collection group ({}) without value in attr {}: ({})",
					networkGroup, NETWORK_ID_ATTR_NAME, networkIDAttr);
			} else {
				networkIDsToGroupMap.put(networkIDAttr.valueAsString(), networkGroup);
			}
		}

		return networkIDsToGroupMap;
	}

	private Set<String> getNetworkIDs(PerunSession session, PerunBl perun, Group collectionsGroup)
		throws WrongAttributeAssignmentException, AttributeNotExistsException
	{
		return getNetworkIDsToGroupsMap(session, perun, collectionsGroup).keySet();
	}

}
