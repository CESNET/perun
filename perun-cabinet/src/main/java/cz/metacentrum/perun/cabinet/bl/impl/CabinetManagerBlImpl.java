package cz.metacentrum.perun.cabinet.bl.impl;

import java.util.ArrayList;
import java.util.List;

import cz.metacentrum.perun.cabinet.bl.CabinetException;
import cz.metacentrum.perun.cabinet.bl.CabinetManagerBl;
import cz.metacentrum.perun.cabinet.bl.ErrorCodes;
import cz.metacentrum.perun.cabinet.bl.HttpManagerBl;
import cz.metacentrum.perun.cabinet.bl.PerunManagerBl;
import cz.metacentrum.perun.cabinet.bl.PublicationSystemManagerBl;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.metacentrum.perun.cabinet.model.Publication;
import cz.metacentrum.perun.cabinet.model.PublicationSystem;
import cz.metacentrum.perun.cabinet.strategy.IFindPublicationsStrategy;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Service class which provides Cabinet with ability to search through
 * external PS based on user's identity and PS namespace.
 *
 * @author Jiri Harazim <harazim@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class CabinetManagerBlImpl implements CabinetManagerBl {

	private PerunManagerBl perunService;
	private PublicationSystemManagerBl publicationSystemManagerBl;
	private HttpManagerBl httpService;

	private static Logger log = LoggerFactory.getLogger(CabinetManagerBlImpl.class);

	// setter ----------------------------------------

	public void setPerunService(PerunManagerBl perunService) {
		this.perunService = perunService;
	}

	@Autowired
	public void setPublicationSystemManagerBl(PublicationSystemManagerBl publicationSystemManagerBl) {
		this.publicationSystemManagerBl = publicationSystemManagerBl;
	}

	public void setHttpService(HttpManagerBl httpService) {
		this.httpService = httpService;
	}

	// methods --------------------------------------

	public List<Publication> findPublicationsInPubSys(String authorId, int yearSince, int yearTill, PublicationSystem ps) throws CabinetException {

		if (StringUtils.isBlank(authorId))
			throw new CabinetException("AuthorId cannot be empty while searching for publications");
		if (ps == null)
			throw new CabinetException("Publication system cannot be null while searching for publications");

		// authorId must be an publication system internal id i.e. UCO! not memberId, userId etc.
		IFindPublicationsStrategy prezentator = null;
		try {
			log.debug("Attempting to instantiate class [{}]...", ps.getType());
			prezentator = (IFindPublicationsStrategy) Class.forName(ps.getType()).newInstance();
			log.debug("Class [{}] successfully created.", ps.getType());
		} catch (Exception e) {
			throw new CabinetException(e);
		}

		HttpUriRequest request = prezentator.getHttpRequest(authorId, yearSince, yearTill, ps);
		HttpResponse response = httpService.execute(request);

		if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
			throw new CabinetException("Can't contact publication system. HTTP error code: " + response.getStatusLine().getStatusCode(), ErrorCodes.HTTP_IO_EXCEPTION);
		}

		List<Publication> publications = prezentator.parseHttpResponse(response);

		for (Publication p : publications) {
			// set pub system for founded publications
			p.setPublicationSystemId(ps.getId());
		}

		return publications;

	}

	public List<Publication> findExternalPublicationsOfUser(PerunSession sess, int userId, int yearSince, int yearTill, String pubSysNamespace) throws CabinetException, InternalErrorException {

		// get PubSys
		PublicationSystem ps = publicationSystemManagerBl.getPublicationSystemByNamespace(pubSysNamespace);
		// get user
		User user = perunService.findUserById(sess, userId);
		// check - because of exception is not thrown when user not found
		if (user == null) {
			throw new CabinetException("User with ID: "+userId+" doesn't exists.", ErrorCodes.PERUN_EXCEPTION);
		}

		// result list
		List<Publication> result = new ArrayList<Publication>();

		// PROCESS MU PUB SYS
		if (ps.getLoginNamespace().equalsIgnoreCase("mu")) {
			// get UCO
			List<UserExtSource> ues = perunService.getUsersLogins(sess, user);
			String authorId = "";
			for (UserExtSource es : ues) {
				// get login from LDAP
				if (es.getExtSource().getName().equalsIgnoreCase("LDAPMU")) {
					authorId = es.getLogin(); // get only UCO
					break;
					// get login from IDP
				} else if (es.getExtSource().getName().equalsIgnoreCase("https://idp2.ics.muni.cz/idp/shibboleth")){
					authorId = es.getLogin().substring(0, es.getLogin().indexOf("@")); // get only UCO from UCO@mail.muni.cz
					break;
				}
			}
			// check
			if (authorId.isEmpty()) {
				throw new CabinetException("You don't have assigned identity in Perun for MU (LDAPMU / Shibboleth IDP).", ErrorCodes.NO_IDENTITY_FOR_PUBLICATION_SYSTEM);
			}
			// get publications
			result.addAll(findPublicationsInPubSys(authorId, yearSince, yearTill, ps));
			return result;

			// PROCESS ZCU 3.0 PUB SYS
		} else if (ps.getLoginNamespace().equalsIgnoreCase("zcu")) {

			// search is based on "lastName,firstName"
			String authorId = user.getLastName()+","+user.getFirstName();

			result.addAll(findPublicationsInPubSys(authorId, yearSince, yearTill, ps));
			return result;

		} else {
			log.error("Publication System with namespace: [{}] found but not supported for import.", pubSysNamespace);
			throw new CabinetException("PubSys namespace found but not supported for import.");
		}

	}

}
