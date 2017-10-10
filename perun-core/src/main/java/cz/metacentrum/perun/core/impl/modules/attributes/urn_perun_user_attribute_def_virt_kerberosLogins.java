package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.auditparser.AuditParser;
import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleImplApi;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Get and set specified user krb Principal Name in arrayList included all userExtSources which are type of KERBEROS
 *
 * @author Michal Šťava <stavamichal@gmail.com>
 */
public class urn_perun_user_attribute_def_virt_kerberosLogins extends UserVirtualAttributesModuleAbstract implements UserVirtualAttributesModuleImplApi {

	private final Pattern addUserExtSource = Pattern.compile("UserExtSource:\\[(.*)\\] added to User:\\[(.*)\\]");
	private final Pattern removeUserExtSource = Pattern.compile("UserExtSource:\\[(.*)\\] removed from User:\\[(.*)\\]");
	private final Pattern extSourceKerberos = Pattern.compile("cz.metacentrum.perun.core.impl.ExtSourceKerberos");

	@Override
	public Attribute getAttributeValue(PerunSessionImpl sess, User user, AttributeDefinition attributeDefinition) throws InternalErrorException {
		Attribute attribute = new Attribute(attributeDefinition);
		List<String> krbPrincipalName = new ArrayList<String>();
		Attribute krbLogins;

		try {
			krbLogins = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, user, AttributesManager.NS_USER_ATTR_DEF + ":kerberosLogins");
		} catch(AttributeNotExistsException ex) {
			throw new InternalErrorException("kerberosLogins attribute for " + user + " not exist.", ex);
		} catch(WrongAttributeAssignmentException ex) {
			throw new InternalErrorException("kerberos Logins attribute bad assignment.", ex);
		}

		if(krbLogins.getValue() != null) {
			krbPrincipalName.addAll((List<String>) krbLogins.getValue());
		}

		List<UserExtSource> userExtSources = sess.getPerunBl().getUsersManagerBl().getUserExtSources(sess, user);

		for(UserExtSource uES: userExtSources) {
			if(uES.getExtSource() != null) {
				String login = uES.getLogin();
				String type = uES.getExtSource().getType();

				if(type != null && login != null) {
					if(type.equals(ExtSourcesManager.EXTSOURCE_KERBEROS)) {
						if(!krbPrincipalName.contains(login)) krbPrincipalName.add(login);
					}
				}
			}
		}

		attribute = Utils.copyAttributeToViAttributeWithoutValue(krbLogins, attribute);
		attribute.setValue(krbPrincipalName);
		return attribute;
	}

	@Override
	public List<String> resolveVirtualAttributeValueChange(PerunSessionImpl perunSession, String message) throws InternalErrorException, WrongReferenceAttributeValueException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<String> resolvingMessages = new ArrayList<String>();
		if (message == null) return resolvingMessages;

		Matcher addUserExtSourceMatcher = addUserExtSource.matcher(message);
		Matcher removeUserExtSourceMatcher = removeUserExtSource.matcher(message);
		Matcher extSourceKerberosMatcher = extSourceKerberos.matcher(message);

		User user = null;
		Attribute attrVirtKerberosLogins = null;

		if(extSourceKerberosMatcher.find()) {
			if (addUserExtSourceMatcher.find() || removeUserExtSourceMatcher.find()) {
				user = perunSession.getPerunBl().getModulesUtilsBl().getUserFromMessage(perunSession, message);
				if (user != null) {
					attrVirtKerberosLogins = perunSession.getPerunBl().getAttributesManagerBl().getAttribute(perunSession, user, AttributesManager.NS_USER_ATTR_VIRT + ":kerberosLogins");
					String messageAttributeSet = attrVirtKerberosLogins.serializeToString() + " set for " + user.serializeToString() + ".";
					resolvingMessages.add(messageAttributeSet);
				}
			}
		}
		return resolvingMessages;
	}

	@Override
	public List<String> getStrongDependencies() {
		List<String> strongDependencies = new ArrayList<String>();
		strongDependencies.add(AttributesManager.NS_USER_ATTR_DEF + ":kerberosLogins");
		return strongDependencies;
	}

	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_VIRT);
		attr.setFriendlyName("krbPrincipalName");
		attr.setDisplayName("KERBEROS principals (full)");
		attr.setType(ArrayList.class.getName());
		attr.setDescription("Logins in kerberos (including realm and kerberos UserExtSources)");
		return attr;
	}
}
