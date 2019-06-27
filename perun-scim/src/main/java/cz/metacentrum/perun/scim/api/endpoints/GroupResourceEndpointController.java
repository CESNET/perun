package cz.metacentrum.perun.scim.api.endpoints;

import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.scim.api.entities.GroupSCIM;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.bl.PerunBl;

import static cz.metacentrum.perun.scim.api.SCIMDefaults.BASE_PATH;
import static cz.metacentrum.perun.scim.api.SCIMDefaults.URN_GROUP;
import static cz.metacentrum.perun.scim.api.SCIMDefaults.USERS_PATH;

import cz.metacentrum.perun.scim.api.entities.MemberSCIM;
import cz.metacentrum.perun.scim.api.exceptions.SCIMException;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Endpoint controller, that returns all group resources.
 *
 * @author Sona Mastrakova <sona.mastrakova@gmail.com>
 * @date 08.10.2016
 */
public class GroupResourceEndpointController {

	private final Logger log = LoggerFactory.getLogger(GroupResourceEndpointController.class);
	private final PerunSession session;
	private final PerunBl perunBl;

	public GroupResourceEndpointController(PerunSession session) {
		this.session = session;
		this.perunBl = (PerunBl) session.getPerun();
	}

	public Response getGroup(String identifier) throws SCIMException {
		log.debug("Calling SCIM REST method getGroup by id {}", identifier);
		if (identifier == null) {
			throw new NullPointerException("identifier is null");
		}
		try {
			Group perunGroup = perunBl.getGroupsManagerBl().getGroupById(session, Integer.parseInt(identifier));
			ObjectMapper mapper = new ObjectMapper();
			return Response.ok(mapper.writeValueAsString(mapPerunGroupToScimGroup(perunGroup))).build();
		} catch (InternalErrorException ex) {
			log.warn("Internal exception occured while getting group with id {}.", identifier);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		} catch (GroupNotExistsException ex) {
			log.warn("Group with id {} does not exists.", identifier);
			return Response.status(Response.Status.NOT_FOUND).build();
		} catch (IOException ex) {
			throw new SCIMException("Cannot convert group resource to json string", ex);
		}
	}

	private GroupSCIM mapPerunGroupToScimGroup(Group perunGroup) {
		List<String> schemas = new ArrayList<>();
		schemas.add(URN_GROUP);
		GroupSCIM result = new GroupSCIM();
		result.setSchemas(schemas);
		result.setDisplayName(perunGroup.getName());
		result.setId(new Long(perunGroup.getId()));

		try {
			List<Member> perunGroupMembers = perunBl.getGroupsManagerBl().getGroupMembers(session, perunGroup);
			result.setMembers(mapPerunMembersToScimMembers(perunGroupMembers));
		} catch (InternalErrorException ex) {
			log.error("Cannot obtain members of group " + perunGroup.getId() + " in VO " + perunGroup.getVoId(), ex);
		}

		return result;
	}

	private List<MemberSCIM> mapPerunMembersToScimMembers(List<Member> perunMembers) {
		List<MemberSCIM> scimMembers = new ArrayList();

		for (Member perunMember : perunMembers) {
			User perunUser;
			try {
				perunUser = perunBl.getUsersManagerBl().getUserByMember(session, perunMember);
				MemberSCIM member = new MemberSCIM();
				String id = String.valueOf(perunMember.getId());
				member.setDisplay(perunUser.getDisplayName());
				member.setValue(id);
				member.setRef(BASE_PATH + USERS_PATH + id);
				scimMembers.add(member);
			} catch (InternalErrorException ex) {
				log.error("Cannot find user with id " + perunMember.getUserId(), ex);
			}
		}

		return scimMembers;
	}
}
