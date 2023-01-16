package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.audit.events.AttributesManagerEvents.AttributeChangedForUser;
import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.UserManagerEvents.UserExtSourceAddedToUser;
import cz.metacentrum.perun.audit.events.UserManagerEvents.UserExtSourceRemovedFromUser;
import cz.metacentrum.perun.audit.events.UserManagerEvents.UserExtSourceUpdated;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleImplApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class urn_perun_user_attribute_def_virt_earliestActiveLastAccess extends UserVirtualAttributesModuleAbstract implements UserVirtualAttributesModuleImplApi {

	private static final String A_U_V_LAST_ACCESS = AttributesManager.NS_USER_ATTR_VIRT + ":" + "earliestActiveLastAccess";
	private static final Logger log = LoggerFactory.getLogger(urn_perun_user_attribute_def_virt_earliestActiveLastAccess.class);

	@Override
	public Attribute getAttributeValue(PerunSessionImpl sess, User user, AttributeDefinition attributeDefinition) {
		Attribute attribute = new Attribute(attributeDefinition);
		List<UserExtSource> ueses = sess.getPerunBl().getUsersManagerBl().getUserExtSources(sess, user);
		if (ueses == null || ueses.isEmpty()) {
			return attribute;
		}

		int validity = BeansUtils.getCoreConfig().getIdpLoginValidity();
		Optional<UserExtSource> value = ueses.stream()
			.filter(u -> ExtSourcesManager.EXTSOURCE_IDP.equals(u.getExtSource().getType()))
			.filter(u -> LocalDateTime.parse(u.getLastAccess(), Utils.lastAccessFormatter)
				.isAfter(LocalDateTime.now().minusMonths(validity)))
			.min(Comparator.comparing(UserExtSource::getLastAccess));

		attribute.setValue(value.isPresent() ? value.get().getLastAccess() : null);
		return attribute;
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_VIRT);
		attr.setFriendlyName("earliestActiveLastAccess");
		attr.setDisplayName("Earliest active last access");
		attr.setType(String.class.getName());
		attr.setDescription("Timestamp of the earliest active IdP extSource's lastAccess.");
		return attr;
	}

	@Override
	public List<AuditEvent> resolveVirtualAttributeValueChange(PerunSessionImpl sess, AuditEvent message) throws AttributeNotExistsException, WrongAttributeAssignmentException {

		List<AuditEvent> resolvingMessages = new ArrayList<>();
		if (message == null) return resolvingMessages;

		User user = null;
		try {
			if (message instanceof UserExtSourceAddedToUser) {
				if (!ExtSourcesManager.EXTSOURCE_IDP.equals(((UserExtSourceAddedToUser) message).getUserExtSource().getExtSource().getType())) {
					return resolvingMessages;
				}
				user = ((UserExtSourceAddedToUser) message).getUser();
				sess.getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
				resolvingMessages.add(resolveEvent(sess, user));
			} else if (message instanceof UserExtSourceRemovedFromUser) {
				if (!ExtSourcesManager.EXTSOURCE_IDP.equals(((UserExtSourceRemovedFromUser) message).getUserExtSource().getExtSource().getType())) {
					return resolvingMessages;
				}
				user = ((UserExtSourceRemovedFromUser) message).getUser();
				sess.getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
				resolvingMessages.add(resolveEvent(sess, user));
			} else if (message instanceof UserExtSourceUpdated) {
				if (!ExtSourcesManager.EXTSOURCE_IDP.equals(((UserExtSourceUpdated) message).getUserExtSource().getExtSource().getType())) {
					return resolvingMessages;
				}
				resolvingMessages.add(resolveEvent(sess, sess.getPerunBl().getUsersManagerBl().getUserById(
					sess, ((UserExtSourceUpdated) message).getUserExtSource().getUserId())));
			}
		} catch (UserNotExistsException e) {
			log.warn("User {} associated with event {} no longer exists while resolving virtual attribute value change for earliestActiveLastAccess.", user, message.getName());
		}

		return resolvingMessages;

	}

	/**
	 * Resolve and create new auditer message about earliestActiveLastAccess attribute change.
	 *
	 * @param sess PerunSession
	 * @param user User to resolve earliestActiveLastAccess messages
	 * @return List of new messages or empty list
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 */
	private AuditEvent resolveEvent(PerunSessionImpl sess, User user) throws AttributeNotExistsException {

		AttributeDefinition attributeDefinition = sess.getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, A_U_V_LAST_ACCESS);
		return new AttributeChangedForUser(new Attribute(attributeDefinition), user);

	}
}
