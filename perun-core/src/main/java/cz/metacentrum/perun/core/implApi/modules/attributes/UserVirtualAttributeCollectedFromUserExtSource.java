package cz.metacentrum.perun.core.implApi.modules.attributes;

import cz.metacentrum.perun.audit.events.AttributesManagerEvents.AllAttributesRemovedForUserExtSource;
import cz.metacentrum.perun.audit.events.AttributesManagerEvents.AttributeChangedForUser;
import cz.metacentrum.perun.audit.events.AttributesManagerEvents.AttributeRemovedForUes;
import cz.metacentrum.perun.audit.events.AttributesManagerEvents.AttributeSetForUes;
import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.AttributesManagerBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Common ancestor class for user virtual attributes that just collect values from userExtSource attributes.
 * <p>
 * For a given user, collects string values of userExtSource attributes with friendly name specified
 * by getSourceAttributeFriendlyName(), and splits them at character ';' which is used by mod_shib to join multiple values,
 * and stores all values into virtual user attribute with friendly name specified by
 * getDestinationAttributeFriendlyName().
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public abstract class UserVirtualAttributeCollectedFromUserExtSource<T extends UserVirtualAttributeCollectedFromUserExtSource.ModifyValueContext> extends UserVirtualAttributesModuleAbstract {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	/**
	 * Specifies friendly (short) name of attribute from namespace urn:perun:ues:attribute-def:def
	 * whose values are to be collected.
	 *
	 * @return short name of userExtSource attribute which is source of values
	 */
	public abstract String getSourceAttributeFriendlyName();

	/**
	 * Gets full URN of the UserExtSource attribute used for computing rhis attribute value.
	 * @return full source attribute URN
	 */
	public final String getSourceAttributeName() {
		return AttributesManager.NS_UES_ATTR_DEF + ":" + getSourceAttributeFriendlyName();
	}

	/**
	 * Specifies friendly (short) name of attribute from namespace urn:perun:user:attribute-def:virt
	 * where values will be stored
	 *
	 * @return short name of user attribute which is destination for collected values
	 */
	public abstract String getDestinationAttributeFriendlyName();

	/**
	 * Gets full URN of this virtual user attribute.
	 * @return full destination attribute URN
	 */
	public final String getDestinationAttributeName() {
		return AttributesManager.NS_USER_ATTR_VIRT + ":" + getDestinationAttributeFriendlyName();
	}

	public String getDestinationAttributeDisplayName() {
		return getDestinationAttributeFriendlyName();
	}

	public String getDestinationAttributeDescription() {
		return "Collected values of userExtSource attribute " + getDestinationAttributeFriendlyName();
	}

	/**
	 * Override this method if you need to modify the original values. The default implementation makes no modification.
	 * Return null if the value should be skipped.
	 *
	 * @param session PerunSession
	 * @param ctx context initialized in initModifyValueContext method
	 * @param ues UserExtSource
	 * @param value of userExtSource attribute
	 * @return modified value or null to skip the value
	 */
	public String modifyValue(PerunSession session, T ctx, UserExtSource ues, String value) {
		return value;
	}

	public static class ModifyValueContext {
		private final PerunSessionImpl session;
		private final User user;
		private final AttributeDefinition destinationAttributeDefinition;

		public ModifyValueContext(PerunSessionImpl session, User user, AttributeDefinition destinationAttributeDefinition) {
			this.session = session;
			this.user = user;
			this.destinationAttributeDefinition = destinationAttributeDefinition;
		}

		public PerunSessionImpl getSession() {
			return session;
		}

		public User getUser() {
			return user;
		}

		@SuppressWarnings("unused")
		public AttributeDefinition getDestinationAttributeDefinition() {
			return destinationAttributeDefinition;
		}
	}

	protected T initModifyValueContext(PerunSessionImpl sess, User user, AttributeDefinition destinationAttributeDefinition) {
		//noinspection unchecked
		return (T) new ModifyValueContext(sess, user, destinationAttributeDefinition);
	}

	@Override
	public Attribute getAttributeValue(PerunSessionImpl sess, User user, AttributeDefinition destinationAttributeDefinition) {
		T ctx = initModifyValueContext(sess, user, destinationAttributeDefinition);
		Attribute destinationAttribute = new Attribute(destinationAttributeDefinition);
		//for values use set because of avoiding duplicities
		Set<String> valuesWithoutDuplicities = new HashSet<>();

		String sourceAttributeFriendlyName = getSourceAttributeFriendlyName();
		List<UserExtSource> userExtSources = sess.getPerunBl().getUsersManagerBl().getUserExtSources(sess, user);
		AttributesManagerBl am = sess.getPerunBl().getAttributesManagerBl();

		for (UserExtSource userExtSource : userExtSources) {
			try {
				String sourceAttributeName = getSourceAttributeName();
				Attribute a = am.getAttribute(sess, userExtSource, sourceAttributeName);
				Object value = a.getValue();
				if (value != null && value instanceof String) {
					//Apache mod_shib joins multiple values with ';', split them again
					String[] rawValues = ((String) value).split(";");
					//add non-null values returned by modifyValue()
					Arrays.stream(rawValues).map(v -> modifyValue(sess, ctx, userExtSource, v)).filter(Objects::nonNull).forEachOrdered(valuesWithoutDuplicities::add);
				} else if (value != null && value instanceof ArrayList) {
					//If values are already separated to list of strings
					a.valueAsList().stream().map(v -> modifyValue(sess, ctx, userExtSource, v)).filter(Objects::nonNull).forEachOrdered(valuesWithoutDuplicities::add);
				}
			} catch (WrongAttributeAssignmentException | AttributeNotExistsException e) {
				log.error("cannot read " + sourceAttributeFriendlyName + " from userExtSource " + userExtSource.getId() + " of user " + user.getId(), e);
			}
		}

		//convert set to list (values in list will be without duplicities)
		destinationAttribute.setValue(new ArrayList<>(valuesWithoutDuplicities));
		return destinationAttribute;
	}

	/**
	 * Functional interface for controlling AuditEvents.
	 *
	 * Modules can overwrite method shouldBeEventHandled to change or add events that should
	 * handled. Events that should be handled are events which make modules to produce another
	 * AuditEvent.
	 */
	@FunctionalInterface
	public interface AttributeHandleIdentifier {

		/**
		 * Determines whether given auditEvent should be handled. If it should be the method
		 * returns userId of user from the auditEvent, otherwise returns null.
		 *
		 * @param auditEvent given auditEvent
		 * @return userId of user from auditEvent, otherwise null
		 */
		Integer shouldBeEventHandled(AuditEvent auditEvent);
	}

	public List<AttributeHandleIdentifier> getHandleIdentifiers() {
		List<AttributeHandleIdentifier> handleIdenfiers = new ArrayList<>();
		handleIdenfiers.add(auditEvent -> {
			if (auditEvent instanceof AllAttributesRemovedForUserExtSource) {
				return ((AllAttributesRemovedForUserExtSource) auditEvent).getUserExtSource().getUserId();
			} else {
				return null;
			}
		});
		handleIdenfiers.add(auditEvent -> {
			if (auditEvent instanceof AttributeRemovedForUes && ((AttributeRemovedForUes) auditEvent).getAttribute().getFriendlyName().equals(getSourceAttributeFriendlyName())) {
				return ((AttributeRemovedForUes) auditEvent).getUes().getUserId();
			} else {
				return null;
			}
		});
		handleIdenfiers.add(auditEvent -> {
			if (auditEvent instanceof AttributeSetForUes &&((AttributeSetForUes) auditEvent).getAttribute().getFriendlyName().equals(getSourceAttributeFriendlyName())) {
				return ((AttributeSetForUes) auditEvent).getUes().getUserId();
			} else {
				return null;
			}
		});
		return handleIdenfiers;
	}

	@Override
	public List<AuditEvent> resolveVirtualAttributeValueChange(PerunSessionImpl perunSession, AuditEvent message) throws WrongReferenceAttributeValueException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<AuditEvent> resolvingMessages = new ArrayList<>();
		if (message == null) return resolvingMessages;

		List<AttributeHandleIdentifier> list = getHandleIdentifiers();
		for (AttributeHandleIdentifier attributeHandleIdenfier : list) {
			Integer userId = attributeHandleIdenfier.shouldBeEventHandled(message);
			if (userId != null) {
				try {
					User user = perunSession.getPerunBl().getUsersManagerBl().getUserById(perunSession, userId);
					AttributeDefinition attributeDefinition = perunSession.getPerunBl().getAttributesManagerBl().getAttributeDefinition(perunSession, getDestinationAttributeName());
					resolvingMessages.add(new AttributeChangedForUser(new Attribute(attributeDefinition), user));
				} catch (UserNotExistsException e) {
					log.warn("User from UserExtSource doesn't exist in Perun. This occurred while parsing message: {}.", message);
				}
			}
		}

		return resolvingMessages;
	}

	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_VIRT);
		String friendlyName = getDestinationAttributeFriendlyName();
		attr.setFriendlyName(friendlyName);
		attr.setDisplayName(getDestinationAttributeDisplayName());
		attr.setType(ArrayList.class.getName());
		attr.setDescription(getDestinationAttributeDescription());
		return attr;
	}
}
