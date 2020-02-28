package cz.metacentrum.perun.ldapc.processor.impl;

import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.ldapc.model.PerunUser;
import cz.metacentrum.perun.ldapc.processor.EventDispatcher.MessageBeans;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.InvalidAttributeValueException;
import org.springframework.ldap.NamingException;

import java.util.regex.Pattern;

public class UserAttributeProcessor extends AbstractAttributeProcessor {

	private final static Logger log = LoggerFactory.getLogger(UserAttributeProcessor.class);

	@Autowired
	protected PerunUser perunUser;

	private static Pattern userSetPattern = Pattern.compile(" set for User:\\[(.*)\\]");
	private static Pattern userRemovePattern = Pattern.compile(" removed for User:\\[(.*)\\]");
	private static Pattern userAllAttrsRemovedPattern = Pattern.compile("All attributes removed for User:\\[(.*)\\]");
	private static Pattern userVirtualChangePattern = Pattern.compile(" changed for User:\\[(.*)\\]");

	//UserExtSources patterns
	private Pattern addUserExtSourcePattern = Pattern.compile("UserExtSource:\\[(.*)\\] added to User:\\[(.*)\\]");
	private Pattern removeUserExtSourcePattern = Pattern.compile("UserExtSource:\\[(.*)\\] removed from User:\\[(.*)\\]");


	public UserAttributeProcessor() {
		super(MessageBeans.USER_F, userSetPattern, userRemovePattern, userAllAttrsRemovedPattern, userVirtualChangePattern);
	}


	public void processAttributeSet(String msg, MessageBeans beans) {
		// ensure we have the correct beans available
		if (beans.getAttribute() == null || beans.getUser() == null) {
			return;
		}
		try {
			log.debug("Setting attribute {} for user {}", beans.getAttribute(), beans.getUser());
			perunUser.modifyEntry(beans.getUser(), beans.getAttribute());
		} catch (NamingException | InternalErrorException e) {
			log.error("Error setting attribute {} for user {}: {}", beans.getAttribute().getId(), beans.getUser().getId(), e);
		}
	}

	public void processAttributeRemoved(String msg, MessageBeans beans) {
		// ensure we have the correct beans available
		if (beans.getAttributeDef() == null || beans.getUser() == null) {
			return;
		}
		try {
			log.debug("Removing attribute {} for user {}", beans.getAttributeDef(), beans.getUser());
			perunUser.modifyEntry(beans.getUser(), beans.getAttributeDef());
		} catch (NamingException | InternalErrorException e) {
			log.error("Error removing attribute {} from user {}: {}", beans.getAttributeDef().getId(), beans.getUser().getId(), e);
		}
	}

	public void processAllAttributesRemoved(String msg, MessageBeans beans) {
		// ensure we have the correct beans available
		if (beans.getUser() == null) {
			return;
		}
		try {
			log.debug("Removing all attributes from user {}", beans.getUser());
			perunUser.removeAllAttributes(beans.getUser());
		} catch (NamingException e) {
			log.error("Error removing attributes from user {}: {}", beans.getUser().getId(), e);
		}
	}

	public void processVirtualAttributeChanged(String msg, MessageBeans beans) {
		if (beans.getAttribute() == null || beans.getUser() == null) {
			return;
		}
		try {
			log.debug("Changing virtual attribute {} for user {}", beans.getAttribute(), beans.getUser());
			perunUser.modifyEntry(beans.getUser(), ((PerunBl) ldapcManager.getPerunBl()).getAttributesManagerBl().
					getAttribute(ldapcManager.getPerunSession(), beans.getUser(), beans.getAttribute().getName()));
		} catch (WrongAttributeAssignmentException | InternalErrorException | AttributeNotExistsException | NamingException e) {
			log.error("Error changing virtual attribute {} for user {}: {}", beans.getAttribute().getId(), beans.getUser().getId(), e);
		}
	}

	public void processExtSourceAdded(String msg, MessageBeans beans) {
		// ensure we have the correct beans available
		if (beans.getUser() == null || beans.getUserExtSource() == null) {
			return;
		}
		try {
			if (beans.getUserExtSource().getExtSource().getType().equals(ExtSourcesManager.EXTSOURCE_IDP)) {
				log.debug("Adding ExtSource {} for user {}", beans.getUserExtSource(), beans.getUser());
				perunUser.addPrincipal(beans.getUser(), beans.getUserExtSource().getLogin());
			}
		} catch (NamingException | InternalErrorException e) {
			log.error("Error adding ExtSource {} for user {}: {}", beans.getUserExtSource().getId(), beans.getUser().getId(), e);
		}
	}

	public void processExtSourceRemoved(String msg, MessageBeans beans) {
		// ensure we have the correct beans available
		if (beans.getUser() == null || beans.getUserExtSource() == null) {
			return;
		}
		try {
			if (beans.getUserExtSource().getExtSource().getType().equals(ExtSourcesManager.EXTSOURCE_IDP)) {
				log.debug("Removing ExtSource {} from user {}", beans.getUserExtSource(), beans.getUser());
				perunUser.removePrincipal(beans.getUser(), beans.getUserExtSource().getLogin());
			}
		} catch (NamingException | InternalErrorException e) {
			log.error("Error removing ExtSource {} from user {}: {}", beans.getUserExtSource().getId(), beans.getUser().getId(), e);
		}
	}

	public void processAdminAdded(String msg, MessageBeans beans) {
		if (beans.getUser() == null) {
			return;
		}
		PerunBean admined = null;
		try {
			if (beans.getVo() != null) {
				admined = beans.getVo();
				perunUser.addAsVoAdmin(beans.getUser(), beans.getVo());
			} else if (beans.getGroup() != null) {
				admined = beans.getGroup();
				perunUser.addAsGroupAdmin(beans.getUser(), beans.getGroup());
			} else if (beans.getFacility() != null) {
				admined = beans.getFacility();
				perunUser.addAsFacilityAdmin(beans.getUser(), beans.getFacility());
			}
		} catch (NamingException | InternalErrorException e) {
			log.error("Error adding user {} as admin of {}: {}", beans.getUser().getId(), admined.getId(), ((InvalidAttributeValueException) e).getExplanation(), e);
		}
	}

	public void processAdminRemoved(String msg, MessageBeans beans) {
		if (beans.getUser() == null) {
			return;
		}
		PerunBean admined = null;
		try {
			if (beans.getVo() != null) {
				admined = beans.getVo();
				perunUser.removeFromVoAdmins(beans.getUser(), beans.getVo());
			} else if (beans.getGroup() != null) {
				admined = beans.getGroup();
				perunUser.removeFromGroupAdmins(beans.getUser(), beans.getGroup());
			} else if (beans.getFacility() != null) {
				admined = beans.getFacility();
				perunUser.removeFromFacilityAdmins(beans.getUser(), beans.getFacility());
			}
		} catch (NamingException | InternalErrorException e) {
			log.error("Error removing user {} from admins of {}: {}", beans.getUser().getId(), admined.getId(), e);
		}

	}
}
