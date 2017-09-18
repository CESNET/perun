package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.auditparser.AuditParser;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleImplApi;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Get and set specified user certDNs in hashMap included all userExtSources which are type of X509
 *
 * @author Michal Šťava <stavamichal@gmail.com>
 */
public class urn_perun_user_attribute_def_virt_userCertDNs extends UserVirtualAttributesModuleAbstract implements UserVirtualAttributesModuleImplApi {

	private final Pattern addUserExtSource = Pattern.compile("UserExtSource:\\[(.*)\\] added to User:\\[(.*)\\]");
	private final Pattern removeUserExtSource = Pattern.compile("UserExtSource:\\[(.*)\\] removed from User:\\[(.*)\\]");
	private final Pattern extSourceTypeX509 = Pattern.compile("cz.metacentrum.perun.core.impl.ExtSourceX509");

	@Override
	public Attribute getAttributeValue(PerunSessionImpl sess, User user, AttributeDefinition attributeDefinition) throws InternalErrorException {
		Attribute attribute = new Attribute(attributeDefinition);
		Map<String, String> userCertDNs = new LinkedHashMap<>();

		List<UserExtSource> userExtSources = sess.getPerunBl().getUsersManagerBl().getUserExtSources(sess, user);
		//Sort user ext sources by their ids (biggest id go last)
		Collections.sort(userExtSources, (ues1, ues2) -> ues1.getId() - ues2.getId());

		//Prepare also prefix number
		int i=1;
		for(UserExtSource uES: userExtSources) {
			if(uES.getExtSource() != null) {
				String login = uES.getLogin();
				String type = uES.getExtSource().getType();
				String name = uES.getExtSource().getName();

				if(type != null && login != null && name != null && type.equals(ExtSourcesManager.EXTSOURCE_X509)) {
					userCertDNs.put(i + ":" + login, name);
					i++;
				}
			}
		}

		attribute.setValue(userCertDNs);
		return attribute;
	}

	@Override
	public List<String> resolveVirtualAttributeValueChange(PerunSessionImpl perunSession, String message) throws InternalErrorException, WrongReferenceAttributeValueException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<String> resolvingMessages = new ArrayList<String>();
		if(message == null) return resolvingMessages;

		User user = null;
		Attribute attrVirtUserCertDNs = null;

		Matcher extSourceTypeX509Matcher = extSourceTypeX509.matcher(message);
		Matcher addUserExtSourceMatcher = addUserExtSource.matcher(message);
		Matcher removeUserExtSourceMatcher = removeUserExtSource.matcher(message);

		if(extSourceTypeX509Matcher.find()) {
			if(addUserExtSourceMatcher.find() || removeUserExtSourceMatcher.find()) {
				user = perunSession.getPerunBl().getModulesUtilsBl().getUserFromMessage(perunSession, message);
				if(user != null) {
					attrVirtUserCertDNs = perunSession.getPerunBl().getAttributesManagerBl().getAttribute(perunSession, user, AttributesManager.NS_USER_ATTR_VIRT + ":userCertDNs");
					String messageAttributeSet = attrVirtUserCertDNs.serializeToString() + " set for " + user.serializeToString() + ".";
					resolvingMessages.add(messageAttributeSet);
				}
			}
		}

		return resolvingMessages;
	}

	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_VIRT);
		attr.setFriendlyName("userCertDNs");
		attr.setDisplayName("DN of certificates");
		attr.setType(LinkedHashMap.class.getName());
		attr.setDescription("Hash map for all users certificates DN included userExtsources type of X509.");
		return attr;
	}
}
