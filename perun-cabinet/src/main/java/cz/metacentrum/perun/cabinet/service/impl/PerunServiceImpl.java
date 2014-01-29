package cz.metacentrum.perun.cabinet.service.impl;

import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cz.metacentrum.perun.core.api.Owner;
import cz.metacentrum.perun.cabinet.dao.IThanksDao;
import cz.metacentrum.perun.cabinet.model.ThanksForGUI;
import cz.metacentrum.perun.cabinet.service.CabinetException;
import cz.metacentrum.perun.cabinet.service.ErrorCodes;
import cz.metacentrum.perun.cabinet.service.IPerunService;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.bl.PerunBl;

/**
 * Class which provides connection to the rest of Perun.
 * 
 * @author Jiri Harazim <harazim@mail.muni.cz>
 */
public class PerunServiceImpl implements IPerunService {

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

	// debug vars, delete it
	public static int CACHE_INVOKED_TOTAL_COUNT = 0;
	public static int CACHE_MISS_TOTAL_COUNT = 0;

	@Autowired
	private PerunBl perun;
	private PerunSession cabinetSession;
	private Logger log = LoggerFactory.getLogger(getClass());
	
	private IThanksDao thanksDao;

	// setters ------------------------
	
	public void setThanksDao(IThanksDao thanksDao) {
		this.thanksDao = thanksDao;
	}

	// methods ------------------------
		
	public Owner findOwnerById(PerunSession sess, Integer id) throws CabinetException {
		try {
			return perun.getOwnersManager().getOwnerById(sess, id);
		} catch (PerunException pe) {
			throw new CabinetException(ErrorCodes.PERUN_EXCEPTION, pe);
		}
	}

	public List<Owner> findAllOwners(PerunSession sess) throws CabinetException {
		try {
			return perun.getOwnersManager().getOwners(sess);
		} catch (PerunException pe){ 
			throw new CabinetException(ErrorCodes.PERUN_EXCEPTION, pe);
		}
	}
	
	public User findUserById(PerunSession sess, Integer userId) throws CabinetException {
		try {
			return perun.getUsersManager().getUserById(sess, userId);
		} catch (UserNotExistsException e) {
			// because of usage in authorExists method
			return null;
		} catch (PerunException pe) {
			throw new CabinetException(ErrorCodes.PERUN_EXCEPTION ,pe);
		}
	}
	
	public List<User> findAllUsers(PerunSession sess) throws CabinetException {
		try {
			return perun.getUsersManager().getUsers(sess);
		} catch (PerunException pe) {
			throw new CabinetException(ErrorCodes.PERUN_EXCEPTION , pe);
		}
	}
	
	public int getUsersCount(PerunSession sess) throws CabinetException {
		try {	
			return perun.getUsersManager().getUsers(sess).size();
		} catch (PerunException pe) {
			throw new CabinetException(ErrorCodes.PERUN_EXCEPTION , pe);
		}
	}

	public void updatePriorityCoeficient(PerunSession sess, Integer userId, Double rank) throws CabinetException {

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
			throw new CabinetException("Failed to update priority coeficient in Perun.",ErrorCodes.PERUN_EXCEPTION, e);
		}

	}
	

	@Override
	public synchronized void setThanksAttribute(int userId) throws CabinetException {

		List<ThanksForGUI> thanks = thanksDao.findAllRichThanksByUserId(userId);
		
		try {
			// get user
			User u = perun.getUsersManager().getUserById(cabinetSession, userId);
			// get attribute
			AttributeDefinition attrDef = perun.getAttributesManager().getAttributeDefinition(cabinetSession, ATTR_PUBS_NAMESPACE+":"+ATTR_PUBS_FRIENDLY_NAME);
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
			throw new CabinetException("Failed to update "+ATTR_PUBS_NAMESPACE+":"+ATTR_PUBS_FRIENDLY_NAME+" in Perun.",ErrorCodes.PERUN_EXCEPTION, e);
		}
		
		
	}

	
	public List<UserExtSource> getUsersLogins(PerunSession sess, User user) throws CabinetException {
		try {
			return perun.getUsersManager().getUserExtSources(sess, user);
		} catch (PerunException pe) {
			throw new CabinetException(ErrorCodes.PERUN_EXCEPTION, pe);
		}
	}
	
	/**
	 * Init method
	 * 
	 * Checks if attribute priorityCoeficient exists in DB,
	 * if not, it's created.
	 * 
	 * @throws PerunException
	 */
	protected void initialize() throws PerunException {
		
		// createCabinet
		final PerunPrincipal pp = new PerunPrincipal("perunCabinet", ExtSourcesManager.EXTSOURCE_INTERNAL, ExtSourcesManager.EXTSOURCE_INTERNAL);
		cabinetSession = perun.getPerunSession(pp);
		
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
				attrDef = perun.getAttributesManager().createAttribute(cabinetSession, attributeDefinition);
			} catch (PerunException pe) {
				log.error("Failed to create attribute "+ ATTR_COEF_NAMESPACE+":"+ATTR_COEF_FRIENDLY_NAME +" in Perun.");
				throw new CabinetException("Failed to create attribute "+ ATTR_COEF_NAMESPACE+":"+ATTR_COEF_FRIENDLY_NAME +" in Perun.", ErrorCodes.PERUN_EXCEPTION, pe);
			}
			log.debug("Attribute "+ ATTR_COEF_NAMESPACE+":"+ATTR_COEF_FRIENDLY_NAME +" successfully created.");
		}
		try {
			// check if attr exists
			attrDef = null;
			attrDef = perun.getAttributesManager().getAttributeDefinition(cabinetSession,ATTR_PUBS_NAMESPACE+":"+ATTR_PUBS_FRIENDLY_NAME);
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
				attrDef = perun.getAttributesManager().createAttribute(cabinetSession, attributeDefinition);
			} catch (PerunException pe) {
				log.error("Failed to create attribute "+ ATTR_PUBS_NAMESPACE+":"+ATTR_PUBS_FRIENDLY_NAME +" in Perun.");
				throw new CabinetException("Failed to create attribute "+ ATTR_PUBS_NAMESPACE+":"+ATTR_PUBS_FRIENDLY_NAME +" in Perun.", ErrorCodes.PERUN_EXCEPTION, pe);
			}
			log.debug("Attribute "+ ATTR_PUBS_NAMESPACE+":"+ATTR_PUBS_FRIENDLY_NAME +" successfully created.");
		}
	}

}