package cz.metacentrum.perun.ldapc.processor.impl;

import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.NamingException;

import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.ldapc.model.PerunUser;
import cz.metacentrum.perun.ldapc.processor.EventDispatcher.MessageBeans;

public class UserAttributeProcessor extends AbstractAttributeProcessor {

	private final static Logger log = LoggerFactory.getLogger(UserAttributeProcessor.class);

	@Autowired
	protected PerunUser perunUser;
	
	private static Pattern userSetPattern = Pattern.compile(" set for User:\\[(.*)\\]");
	private static Pattern userRemovePattern = Pattern.compile(" removed for User:\\[(.*)\\]");
	private static Pattern userAllAttrsRemovedPattern = Pattern.compile("All attributes removed for User:\\[(.*)\\]");

	//UserExtSources patterns
	private Pattern addUserExtSourcePattern = Pattern.compile("UserExtSource:\\[(.*)\\] added to User:\\[(.*)\\]");
	private Pattern removeUserExtSourcePattern = Pattern.compile("UserExtSource:\\[(.*)\\] removed from User:\\[(.*)\\]");
	
	
	public UserAttributeProcessor() {
		super(MessageBeans.USER_F, userSetPattern, userRemovePattern, userAllAttrsRemovedPattern);
	}
	
	
	public void processAttributeSet(String msg, MessageBeans beans) {
		// ensure we have the correct beans available
		if(beans.getAttribute() == null || beans.getUser() == null) {
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
		if(beans.getAttributeDef() == null || beans.getUser() == null) {
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
		if(beans.getUser() == null) {
			return;
		}
		try {
			log.debug("Removing all attributes from user {}", beans.getUser());
			perunUser.removeAllAttributes(beans.getUser());
		} catch (NamingException e) {
			log.error("Error removing attributes from user {}: {}", beans.getUser().getId(), e);
		}
	}	

	public void processExtSourceSet(String msg, MessageBeans beans) {
		// ensure we have the correct beans available
		if(beans.getUser() == null || beans.getUserExtSource() == null) {
			return;
		}
		try {
			if(beans.getUserExtSource().getExtSource().getType().equals(ExtSourcesManager.EXTSOURCE_IDP)) {
				log.debug("Adding ExtSource {} for user {}", beans.getUserExtSource(), beans.getUser());
				perunUser.addPrincipal(beans.getUser(), beans.getUserExtSource().getLogin());
			}
		} catch (NamingException | InternalErrorException e) {
			log.error("Error adding ExtSource {} for user {}: {}", beans.getUserExtSource().getId(), beans.getUser().getId(), e);
		}
	}

	public void processExtSourceRemoved(String msg, MessageBeans beans) {
		// ensure we have the correct beans available
		if(beans.getUser() == null || beans.getUserExtSource() == null) {
			return;
		}
		try {
			if(beans.getUserExtSource().getExtSource().getType().equals(ExtSourcesManager.EXTSOURCE_IDP)) {
				log.debug("Removing ExtSource {} from user {}", beans.getUserExtSource(), beans.getUser());
				perunUser.removePrincipal(beans.getUser(), beans.getUserExtSource().getLogin());
			}
		} catch (NamingException | InternalErrorException e) {
			log.error("Error removing ExtSource {} from user {}: {}", beans.getUserExtSource().getId(), beans.getUser().getId(), e);
		}
	}

}
