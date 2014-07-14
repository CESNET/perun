package cz.metacentrum.perun.core.impl.modules.attributes;

import java.lang.String;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AlreadyReservedLoginException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleImplApi;

/**
 * Class for checking login's uniqueness in the namespace
 *
 * @author Michal Prochazka  &lt;michalp@ics.muni.cz&gt;
 * @author Slavek Licehammer &lt;glory@ics.muni.cz&gt;
 * @date 3.6.2011 11:02:22
 */
public class urn_perun_user_attribute_def_def_login_namespace extends UserAttributesModuleAbstract implements UserAttributesModuleImplApi {

	private final static Logger log = LoggerFactory.getLogger(urn_perun_user_attribute_def_def_login_namespace.class);

	/**
	 * Checks if the user's login is unique in the namespace
	 * organization
	 */
	public void checkAttributeValue(PerunSessionImpl sess, User user, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException {

		String[] unpermittedLogins = {"arraysvcs", "at", "backup", "bin", "daemon", "Debian-exim", "flexlm", "ftp", "games",
		        "gdm", "glite", "gnats", "haldaemon", "identd", "irc", "libuuid", "list", "lp", "mail", "man",
		        "messagebus", "news", "nobody", "ntp", "openslp", "pcp", "polkituser", "postfix", "proxy",
		        "pulse", "puppet", "root", "saned", "smmsp", "smmta", "sshd", "statd", "suse-ncc", "sync",
		        "sys", "uucp", "uuidd", "www-data", "wwwrun", "zenssh", "oneadmin", "tomcat"};

		String userLogin = (String) attribute.getValue();
		if (userLogin == null) throw new WrongAttributeValueException(attribute, user, "Value can't be nugll");
		if(!userLogin.matches("^[a-zA-Z0-9][-A-z0-9_.@/]*$")) throw new WrongAttributeValueException(attribute, user, "Wrong format. ^[A-z0-9][-A-z0-9_.@/]*$ expected.");

		foreach(String login: unpermittedLogins) {
			if(userLogin.equals(login)) throw new WrongAttributeValueException(attribute, user, "Unpermitted login.");
		}

		// Get all users who have set attribute urn:perun:member:attribute-def:def:login-namespace:[login-namespace], with the value.
		List<User> usersWithSameLogin = sess.getPerunBl().getUsersManagerBl().getUsersByAttribute(sess, attribute);
		usersWithSameLogin.remove(user); //remove self
		if (!usersWithSameLogin.isEmpty()) {
			if(usersWithSameLogin.size() > 1) throw new ConsistencyErrorException("FATAL ERROR: Duplicated Login detected." +  attribute + " " + usersWithSameLogin);
			throw new WrongAttributeValueException(attribute, user, "This login " + attribute.getValue() + " is already occupied.");
		}

		try {
			sess.getPerunBl().getUsersManagerBl().checkReservedLogins(sess, attribute.getFriendlyNameParameter(), userLogin);
		} catch (AlreadyReservedLoginException ex) {
			throw new WrongAttributeValueException(attribute, user, "Login in specific namespace already reserved.", ex);
		}
	}

	@Override
	/**
	 * Not implemented yet
	 */
	public Attribute fillAttribute(PerunSessionImpl perunSession, User user, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException {
		return new Attribute(attribute);
	}

	/*public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
		attr.setFriendlyName("login-namespace");
		attr.setType(String.class.getName());
		attr.setDescription("Login namespace.");
		return attr;
	}*/
}
