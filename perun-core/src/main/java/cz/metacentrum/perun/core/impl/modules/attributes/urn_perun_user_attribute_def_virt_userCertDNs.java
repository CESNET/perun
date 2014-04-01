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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Get and set specified user certDNs in hashMap included all userExtSources which are type of X509
 *
 * @author Michal Šťava <stavamichal@gmail.com>
 */
public class urn_perun_user_attribute_def_virt_userCertDNs extends UserVirtualAttributesModuleAbstract implements UserVirtualAttributesModuleImplApi {

	private Pattern addUserExtSource = Pattern.compile("UserExtSource:\\[(.*)\\] added to User:\\[(.*)\\]");
	private Pattern removeUserExtSource = Pattern.compile("UserExtSource:\\[(.*)\\] removed from User:\\[(.*)\\]");
	private Pattern extSourceTypeX509 = Pattern.compile("cz.metacentrum.perun.core.impl.ExtSourceX509");
	private Pattern userAttributeSet = Pattern.compile("Attribute:\\[(.*)\\] set for User:\\[(.*)\\]");
	private Pattern userCertDNs = Pattern.compile("friendlyName=<userCertDNs>");
	private Pattern userAttributeRemoved = Pattern.compile("AttributeDefinition:\\[(.*)\\] removed for User:\\[(.*)\\]");

	@Override
	public Attribute getAttributeValue(PerunSessionImpl sess, User user, AttributeDefinition attributeDefinition) throws InternalErrorException {
		Attribute attribute = new Attribute(attributeDefinition);
		Map<String, String> userCertDNs = new LinkedHashMap<String, String>();

		Attribute attrUserCertDNs = null;
		try {
			attrUserCertDNs = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, user, AttributesManager.NS_USER_ATTR_DEF + ":userCertDNs");
		} catch(AttributeNotExistsException ex) {
			throw new InternalErrorException("userCertDNs attribute for " + user + " not exist.", ex);
		} catch(WrongAttributeAssignmentException ex) {
			throw new InternalErrorException("userCertDNs attribute bad assignment.", ex);
		}

		if(attrUserCertDNs.getValue() != null) {
			userCertDNs.putAll((LinkedHashMap<String,String>) attrUserCertDNs.getValue());
		}

		List<UserExtSource> userExtSources = sess.getPerunBl().getUsersManagerBl().getUserExtSources(sess, user);

		for(UserExtSource uES: userExtSources) {
			if(uES.getExtSource() != null) {
				String login = uES.getLogin();
				String type = uES.getExtSource().getType();
				String name = uES.getExtSource().getName();

				if(type != null && login != null && name != null) {
					if(type.equals(ExtSourcesManager.EXTSOURCE_X509)) {
						if(!userCertDNs.containsKey(login)) userCertDNs.put(login, name);
					}
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
		Matcher userAttributeSetMatcher = userAttributeSet.matcher(message);
		Matcher userAttributeRemovedMatcher = userAttributeRemoved.matcher(message);
		Matcher userCertDNsMatcher = userCertDNs.matcher(message);

		if(extSourceTypeX509Matcher.find()) {
			if(addUserExtSourceMatcher.find() || removeUserExtSourceMatcher.find()) {
				user = getUserFromMessage(message);
				if(user != null) {
					attrVirtUserCertDNs = perunSession.getPerunBl().getAttributesManagerBl().getAttribute(perunSession, user, AttributesManager.NS_USER_ATTR_VIRT + ":userCertDNs");
					String messageAttributeSet = attrVirtUserCertDNs.serializeToString() + " set for " + user.serializeToString() + ".";
					resolvingMessages.add(messageAttributeSet);
				}
			}
		} else if(userCertDNsMatcher.find()) {
			if(userAttributeSetMatcher.find()) {
				user = getUserFromMessage(message);
				if(user != null) {
					attrVirtUserCertDNs = perunSession.getPerunBl().getAttributesManagerBl().getAttribute(perunSession, user, AttributesManager.NS_USER_ATTR_VIRT + ":userCertDNs");
					String messageAttributeSet = attrVirtUserCertDNs.serializeToString() + " set for " + user.serializeToString() + ".";
					resolvingMessages.add(messageAttributeSet);
				}
			} else if(userAttributeRemovedMatcher.find()) {
				user = getUserFromMessage(message);
				if(user != null) {
					attrVirtUserCertDNs = perunSession.getPerunBl().getAttributesManagerBl().getAttribute(perunSession, user, AttributesManager.NS_USER_ATTR_VIRT + ":userCertDNs");
					AttributeDefinition attrVirtUserCertDNsDefinition = (AttributeDefinition) attrVirtUserCertDNs;
					String messageAttributeRemoved = attrVirtUserCertDNsDefinition + " removed for " + user.serializeToString() + ".";
					resolvingMessages.add(messageAttributeRemoved);
				}
			}
		}

		return resolvingMessages;
	}

	@Override
	public List<String> getStrongDependencies() {
		List<String> strongDependencies = new ArrayList<String>();
		strongDependencies.add(AttributesManager.NS_USER_ATTR_DEF + ":userCertDNs");
		return strongDependencies;
	}

	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_VIRT);
		attr.setFriendlyName("userCertDNs");
		attr.setType(LinkedHashMap.class.getName());
		attr.setDescription("Hash map for all users certificates DN included userExtsources type of X509.");
		return attr;
	}

	/**
	 * Get User from message if exists or if there is only one. In other case return null instead.
	 *
	 * @param message
	 * @return user or null
	 * @throws InternalErrorException
	 */
	private User getUserFromMessage(String message) throws InternalErrorException {
		User user = null;
		List<PerunBean> perunBeans = AuditParser.parseLog(message);

		for(PerunBean pb: perunBeans) {
			if(pb instanceof User) {
				if(user != null) {
					return null;
				} else {
					user = (User) pb;
				}
			}
		}
		return user;
	}
}
