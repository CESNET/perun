package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.audit.events.AttributesManagerEvents.AttributeChangedForUser;
import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.UserManagerEvents.UserExtSourceAddedToUser;
import cz.metacentrum.perun.audit.events.UserManagerEvents.UserExtSourceRemovedFromUser;
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
import cz.metacentrum.perun.core.impl.ExtSourceX509;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.SkipValueCheckDuringDependencyCheck;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleImplApi;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Get and set specified user certDNs in hashMap included all userExtSources which are type of X509
 *
 * @author Michal Šťava <stavamichal@gmail.com>
 */
@SkipValueCheckDuringDependencyCheck
public class urn_perun_user_attribute_def_virt_userCertDNs extends UserVirtualAttributesModuleAbstract implements UserVirtualAttributesModuleImplApi {

	private final static String A_U_V_USER_CERT_DNS = AttributesManager.NS_USER_ATTR_VIRT + ":userCertDNs";

	@Override
	public Attribute getAttributeValue(PerunSessionImpl sess, User user, AttributeDefinition attributeDefinition) throws InternalErrorException {
		Attribute attribute = new Attribute(attributeDefinition);
		Map<String, String> userCertDNs = new LinkedHashMap<>();

		List<UserExtSource> userExtSources = sess.getPerunBl().getUsersManagerBl().getUserExtSources(sess, user);
		//Sort user ext sources by their ids (biggest id go last)
		userExtSources.sort(Comparator.comparingInt(PerunBean::getId));

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
	public List<AuditEvent> resolveVirtualAttributeValueChange(PerunSessionImpl perunSession, AuditEvent message) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<AuditEvent> resolvingMessages = new ArrayList<>();
		if (message == null) return resolvingMessages;

		if (message instanceof UserExtSourceAddedToUser && ExtSourcesManager.EXTSOURCE_X509.equals(((UserExtSourceAddedToUser) message).getUserExtSource().getExtSource().getType())) {
			resolvingMessages.add(resolveEvent(perunSession, ((UserExtSourceAddedToUser) message).getUser()));
		} else if (message instanceof UserExtSourceRemovedFromUser && ExtSourcesManager.EXTSOURCE_X509.equals(((UserExtSourceRemovedFromUser) message).getUserExtSource().getExtSource().getType())) {
			resolvingMessages.add(resolveEvent(perunSession, ((UserExtSourceRemovedFromUser) message).getUser()));
		}

		return resolvingMessages;
	}

	private AuditEvent resolveEvent(PerunSessionImpl perunSession, User user) throws AttributeNotExistsException, InternalErrorException {
		AttributeDefinition attrVirtUserCertDNsDefinition = perunSession.getPerunBl().getAttributesManagerBl().getAttributeDefinition(perunSession, A_U_V_USER_CERT_DNS);
		return new AttributeChangedForUser(new Attribute(attrVirtUserCertDNsDefinition), user);
	}

	@Override
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
