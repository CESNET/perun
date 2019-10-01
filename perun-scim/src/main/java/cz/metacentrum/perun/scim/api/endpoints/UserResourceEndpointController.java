package cz.metacentrum.perun.scim.api.endpoints;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.scim.api.entities.UserSCIM;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.scim.api.entities.EmailSCIM;

import java.util.ArrayList;
import java.util.List;

import cz.metacentrum.perun.scim.api.exceptions.SCIMException;

import java.io.IOException;

import static cz.metacentrum.perun.scim.api.SCIMDefaults.URN_USER;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Endpoint controller, that returns all user resources.
 *
 * @author Sona Mastrakova <sona.mastrakova@gmail.com>
 * @date 08.10.2016
 */
public class UserResourceEndpointController {

	private final Logger log = LoggerFactory.getLogger(UserResourceEndpointController.class);
	private final PerunSession session;
	private final PerunBl perunBl;

	public UserResourceEndpointController(PerunSession session) {
		this.session = session;
		this.perunBl = (PerunBl) session.getPerun();
	}

	public Response getUser(String identifier) throws SCIMException {
		log.debug("Calling SCIM REST method  getUser by id {}", identifier);
		if (identifier == null) {
			throw new NullPointerException("identifier is null");
		}
		try {
			User perunUser = perunBl.getUsersManagerBl().getUserById(session, Integer.parseInt(identifier));
			ObjectMapper mapper = new ObjectMapper();
			return Response.ok(mapper.writeValueAsString(mapPerunUserToScimUser(perunUser))).build();
		} catch (InternalErrorException ex) {
			log.warn("Internal exception occured while getting user with id {}.", identifier);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		} catch (UserNotExistsException ex) {
			log.warn("User with id {} does not exists.", identifier);
			return Response.status(Response.Status.NOT_FOUND).build();
		} catch (IOException ex) {
			throw new SCIMException("Cannot convert user resource to json string", ex);
		}
	}

	private UserSCIM mapPerunUserToScimUser(User perunUser) {
		Long userId = new Long(perunUser.getId());

		UserSCIM result = new UserSCIM();
		EmailSCIM email = getEmail(perunUser);
		if (email != null) {
			List emails = new ArrayList<>();
			emails.add(email);
			result.setEmails(emails);
		}

		result.setId(userId);
		result.setUserName(userId.toString());
		result.setName(perunUser.getDisplayName());
		result.setDisplayName(perunUser.getDisplayName());

		List<String> schemas = new ArrayList<>();
		schemas.add(URN_USER);
		result.setSchemas(schemas);

		return result;
	}

	private EmailSCIM getEmail(User perunUser) {
		Attribute preferredEmailAttribute = new Attribute();
		EmailSCIM email = new EmailSCIM();

		try {
			preferredEmailAttribute = perunBl.getAttributesManagerBl().getAttribute(session, perunUser, AttributesManager.NS_USER_ATTR_DEF + ":preferredMail");
			if (preferredEmailAttribute.getValue() != null) {
				email.setValue(preferredEmailAttribute.getValue().toString());
				email.setPrimary(true);
				email.setType("preferred email");
				return email;
			}
		} catch (InternalErrorException ex) {
			log.error("Internal exception occured while getting preferred email of user " + perunUser.getId(), ex);
		} catch (AttributeNotExistsException ex) {
			log.error("Attribute preferredMail doesn't exist for user " + perunUser.getId(), ex);
		} catch (WrongAttributeAssignmentException ex) {
			log.error("Trying to assign attribute to the wrong entity while getting preferred email of user " + perunUser.getId(), ex);
		}
		return null;
	}
}
