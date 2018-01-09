package cz.metacentrum.perun.cabinet.bl.impl;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import cz.metacentrum.perun.cabinet.bl.CabinetException;
import cz.metacentrum.perun.cabinet.bl.CabinetManagerBl;
import cz.metacentrum.perun.cabinet.bl.ErrorCodes;
import cz.metacentrum.perun.cabinet.bl.PublicationSystemManagerBl;
import cz.metacentrum.perun.cabinet.bl.ThanksManagerBl;
import cz.metacentrum.perun.cabinet.model.ThanksForGUI;
import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.bl.PerunBl;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.metacentrum.perun.cabinet.model.Publication;
import cz.metacentrum.perun.cabinet.model.PublicationSystem;
import cz.metacentrum.perun.cabinet.strategy.PublicationSystemStrategy;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Service class which provides Cabinet with ability to search through
 * external PS based on user's identity and PS namespace.
 *
 * @author Jiri Harazim <harazim@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class CabinetManagerBlImpl implements CabinetManagerBl {

	private PublicationSystemManagerBl publicationSystemManagerBl;
	private ThanksManagerBl thanksManagerBl;
	private PerunSession cabinetSession;
	private PerunBl perun;

	private static Logger log = LoggerFactory.getLogger(CabinetManagerBlImpl.class);

	private static final String ATTR_COEF_TYPE = "java.lang.String";
	private static final String ATTR_COEF_NAMESPACE = "urn:perun:user:attribute-def:def";
	private static final String ATTR_COEF_FRIENDLY_NAME = "priorityCoeficient";
	private static final String ATTR_COEF_DESCRIPTION = "Priority coefficient based on user's publications.";
	private static final String ATTR_COEF_DISPLAY_NAME = "Priority coefficient";

	private static final String ATTR_PUBS_TYPE = "java.util.LinkedHashMap";
	private static final String ATTR_PUBS_NAMESPACE = "urn:perun:user:attribute-def:def";
	private static final String ATTR_PUBS_FRIENDLY_NAME = "publications";
	private static final String ATTR_PUBS_DESCRIPTION = "Number of acknowledgements per resource provider.";
	private static final String ATTR_PUBS_DISPLAY_NAME = "Publications";

	// setter ----------------------------------------

	@Autowired
	public void setPublicationSystemManagerBl(PublicationSystemManagerBl publicationSystemManagerBl) {
		this.publicationSystemManagerBl = publicationSystemManagerBl;
	}

	@Autowired
	public void setThanksManagerBl(ThanksManagerBl thanksManagerBl) {
		this.thanksManagerBl = thanksManagerBl;
	}

	@Autowired
	public void setPerun(PerunBl perun) {
		this.perun = perun;
	}

	public PublicationSystemManagerBl getPublicationSystemManagerBl() {
		return publicationSystemManagerBl;
	}

	public ThanksManagerBl getThanksManagerBl() {
		return thanksManagerBl;
	}

	// methods --------------------------------------

	public List<Publication> findPublicationsInPubSys(String authorId, int yearSince, int yearTill, PublicationSystem ps) throws CabinetException {

		if (StringUtils.isBlank(authorId))
			throw new CabinetException("AuthorId cannot be empty while searching for publications");
		if (ps == null)
			throw new CabinetException("Publication system cannot be null while searching for publications");

		// authorId must be an publication system internal id i.e. UCO! not memberId, userId etc.
		PublicationSystemStrategy prezentator = null;
		try {
			log.debug("Attempting to instantiate class [{}]...", ps.getType());
			prezentator = (PublicationSystemStrategy) Class.forName(ps.getType()).newInstance();
			log.debug("Class [{}] successfully created.", ps.getType());
		} catch (Exception e) {
			throw new CabinetException(e);
		}

		HttpUriRequest request = prezentator.getHttpRequest(authorId, yearSince, yearTill, ps);
		HttpResponse response = prezentator.execute(request);

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
		PublicationSystem ps = getPublicationSystemManagerBl().getPublicationSystemByNamespace(pubSysNamespace);
		// get user
		User user;
		try {
			user = perun.getUsersManagerBl().getUserById(sess, userId);
		} catch (UserNotExistsException ex) {
			throw new CabinetException("User with ID: "+userId+" doesn't exists.", ErrorCodes.PERUN_EXCEPTION, ex);
		}

		// result list
		List<Publication> result = new ArrayList<Publication>();

		// PROCESS MU PUB SYS
		if (ps.getLoginNamespace().equalsIgnoreCase("mu")) {
			// get UCO
			List<UserExtSource> ues = perun.getUsersManagerBl().getUserExtSources(sess, user);
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
		} else if (ps.getLoginNamespace().equalsIgnoreCase("zcu") || ps.getLoginNamespace().equalsIgnoreCase("uk") ) {

			// search is based on "lastName,firstName"
			String authorId = user.getLastName()+","+user.getFirstName();

			result.addAll(findPublicationsInPubSys(authorId, yearSince, yearTill, ps));
			return result;

		} else if (ps.getLoginNamespace().equalsIgnoreCase("europepmc")) {

			try {
				Attribute attribute = perun.getAttributesManagerBl().getAttribute(sess, user, "urn:perun:user:attribute-def:virt:eduPersonORCID");
				ArrayList<String> attrValue = (ArrayList<String>)attribute.getValue();
				if (attrValue != null && !attrValue.isEmpty()) {
					// user shouldn't, but technically can have multiple orcid identities
					for (String singleValue : attrValue) {
						// get ID from OrcID identity: http://orcid.org/ID
						String orcid = StringUtils.substringAfter(singleValue, "http://orcid.org/");
						// iterate over all years since it can get only specific year
						for (int counter=yearSince; counter<=yearTill; counter++) {
							// get publications
							result.addAll(findPublicationsInPubSys(orcid, counter, 0, ps));
						}
					}
					return result;
				} else {
					throw new CabinetException("You don't have assigned ORCID identity in Perun for use in Europe PMC.", ErrorCodes.NO_IDENTITY_FOR_PUBLICATION_SYSTEM);
				}

			} catch (WrongAttributeAssignmentException e) {
				throw new InternalErrorException(e);
			} catch (AttributeNotExistsException e) {
				throw new InternalErrorException(e);
			}

		} else {
			log.error("Publication System with namespace: [{}] found but not supported for import.", pubSysNamespace);
			throw new CabinetException("PubSys namespace found but not supported for import.");
		}

	}

	public void updatePriorityCoefficient(PerunSession sess, Integer userId, Double rank) throws CabinetException {

		try {
			// get definition
			AttributeDefinition attrDef = perun.getAttributesManager().getAttributeDefinition(cabinetSession, ATTR_COEF_NAMESPACE+":"+ATTR_COEF_FRIENDLY_NAME);
			// Set attribute value
			Attribute attr = new Attribute(attrDef);
			DecimalFormat twoDForm = new DecimalFormat("#.##");
			attr.setValue(String.valueOf(twoDForm.format(rank)));
			// get user
			User user = perun.getUsersManager().getUserById(cabinetSession, userId);
			// assign or update user's attribute
			perun.getAttributesManager().setAttribute(cabinetSession, user, attr);
		} catch (PerunException e) {
			throw new CabinetException("Failed to update priority coefficient in Perun.",ErrorCodes.PERUN_EXCEPTION, e);
		}

	}

	@Override
	public void setThanksAttribute(int userId) throws CabinetException, InternalErrorException {

		List<ThanksForGUI> thanks = getThanksManagerBl().getRichThanksByUserId(userId);

		try {
			// get user
			User u = perun.getUsersManager().getUserById(cabinetSession, userId);
			// get attribute
			AttributeDefinition attrDef = perun.getAttributesManager().getAttributeDefinition(cabinetSession, ATTR_PUBS_NAMESPACE + ":" + ATTR_PUBS_FRIENDLY_NAME);
			Attribute attr = new Attribute(attrDef);
			// if there are thanks to set
			if (thanks != null && !thanks.isEmpty()) {
				// create new values map
				LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
				for (ThanksForGUI t : thanks) {
					Integer count = 1;
					if (map.containsKey(t.getOwnerName())) {
						// if contains value already, do +1
						String value = map.get(t.getOwnerName());
						count = Integer.parseInt(value);
						count = count + 1;
					}
					map.put(t.getOwnerName(), count.toString());
				}
				attr.setValue(map);
				perun.getAttributesManager().setAttribute(cabinetSession, u, attr);
			} else {
				// empty or null thanks - update to: remove
				perun.getAttributesManager().removeAttribute(cabinetSession, u, attrDef);
			}

		} catch (PerunException e) {
			throw new CabinetException("Failed to update " + ATTR_PUBS_NAMESPACE + ":" + ATTR_PUBS_FRIENDLY_NAME + " in Perun.", ErrorCodes.PERUN_EXCEPTION, e);
		}

	}

	/**
	 * Init method
	 *
	 * Checks if attribute priorityCoefficient exists in DB,
	 * if not, it's created.
	 *
	 * @throws PerunException
	 */
	protected void initialize() throws PerunException {

		// createCabinet
		final PerunPrincipal pp = new PerunPrincipal("perunCabinet", ExtSourcesManager.EXTSOURCE_NAME_INTERNAL, ExtSourcesManager.EXTSOURCE_INTERNAL);
		cabinetSession = perun.getPerunSession(pp, new PerunClient());

		AttributeDefinition attrDef;
		try {
			// check if attr exists
			attrDef = perun.getAttributesManager().getAttributeDefinition(cabinetSession,ATTR_COEF_NAMESPACE+":"+ATTR_COEF_FRIENDLY_NAME);
		} catch (AttributeNotExistsException e) {
			// if not - create it
			log.warn("Attribute "+ ATTR_COEF_NAMESPACE+":"+ATTR_COEF_FRIENDLY_NAME +" does not exist in Perun. Attempting to create it.");
			AttributeDefinition attributeDefinition = new AttributeDefinition();
			attributeDefinition.setDisplayName(ATTR_COEF_DISPLAY_NAME);
			attributeDefinition.setDescription(ATTR_COEF_DESCRIPTION);
			attributeDefinition.setFriendlyName(ATTR_COEF_FRIENDLY_NAME);
			attributeDefinition.setNamespace(ATTR_COEF_NAMESPACE);
			attributeDefinition.setType(ATTR_COEF_TYPE);
			try {
				// create attribute
				attrDef = perun.getAttributesManager().createAttribute(cabinetSession, attributeDefinition);
				// set attribute rights
				List<AttributeRights> rights = new ArrayList<AttributeRights>();
				rights.add(new AttributeRights(attrDef.getId(), Role.SELF, Arrays.asList(ActionType.READ)));
				perun.getAttributesManager().setAttributeRights(cabinetSession, rights);
			} catch (PerunException pe) {
				log.error("Failed to create attribute "+ ATTR_COEF_NAMESPACE+":"+ATTR_COEF_FRIENDLY_NAME +" in Perun.");
				throw new CabinetException("Failed to create attribute "+ ATTR_COEF_NAMESPACE+":"+ATTR_COEF_FRIENDLY_NAME +" in Perun.", ErrorCodes.PERUN_EXCEPTION, pe);
			}
			log.debug("Attribute "+ ATTR_COEF_NAMESPACE+":"+ATTR_COEF_FRIENDLY_NAME +" successfully created.");
		}
		AttributeDefinition attrDef2;
		try {
			// check if attr exists
			attrDef2 = perun.getAttributesManager().getAttributeDefinition(cabinetSession,ATTR_PUBS_NAMESPACE+":"+ATTR_PUBS_FRIENDLY_NAME);
		} catch (AttributeNotExistsException e) {
			// if not - create it
			log.warn("Attribute "+ ATTR_PUBS_NAMESPACE+":"+ATTR_PUBS_FRIENDLY_NAME +" does not exist in Perun. Attempting to create it.");
			AttributeDefinition attributeDefinition = new AttributeDefinition();
			attributeDefinition.setDisplayName(ATTR_PUBS_DISPLAY_NAME);
			attributeDefinition.setDescription(ATTR_PUBS_DESCRIPTION);
			attributeDefinition.setFriendlyName(ATTR_PUBS_FRIENDLY_NAME);
			attributeDefinition.setNamespace(ATTR_PUBS_NAMESPACE);
			attributeDefinition.setType(ATTR_PUBS_TYPE);
			try {
				attrDef2 = perun.getAttributesManager().createAttribute(cabinetSession, attributeDefinition);
				// set attribute rights
				List<AttributeRights> rights = new ArrayList<AttributeRights>();
				rights.add(new AttributeRights(attrDef2.getId(), Role.SELF, Arrays.asList(ActionType.READ)));
				perun.getAttributesManager().setAttributeRights(cabinetSession, rights);
			} catch (PerunException pe) {
				log.error("Failed to create attribute "+ ATTR_PUBS_NAMESPACE+":"+ATTR_PUBS_FRIENDLY_NAME +" in Perun.");
				throw new CabinetException("Failed to create attribute "+ ATTR_PUBS_NAMESPACE+":"+ATTR_PUBS_FRIENDLY_NAME +" in Perun.", ErrorCodes.PERUN_EXCEPTION, pe);
			}
			log.debug("Attribute "+ ATTR_PUBS_NAMESPACE+":"+ATTR_PUBS_FRIENDLY_NAME +" successfully created.");
		}
	}

}
