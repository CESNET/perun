package cz.metacentrum.perun.core.implApi.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
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
import java.util.regex.Pattern;

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

	private final Pattern allAttributesRemovedForUserExtSource = Pattern.compile("All attributes removed for UserExtSource:\\[(.|\\s)*]", Pattern.MULTILINE);
	private final Pattern removeUserExtSourceAttribute = Pattern.compile("AttributeDefinition:\\[(.|\\s)*" + getSourceAttributeFriendlyName() + "(.|\\s)*] removed for UserExtSource:\\[(.|\\s)*]", Pattern.MULTILINE);
	private final Pattern setUserExtSourceAttribute = Pattern.compile("Attribute:\\[(.|\\s)*" + getSourceAttributeFriendlyName() + "(.|\\s)*] set for UserExtSource:\\[(.|\\s)*]", Pattern.MULTILINE);

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
		return "Collected values of userExtSource atribute " + getDestinationAttributeFriendlyName();
	}

	/**
	 * Override this method if you need to modify the original values. The default implementation makes no modification.
	 * Return null if the value should be skipped.
	 *
	 * @param ctx context initialized in initModifyValueContext method
	 * @param value of userExtSource attribute
	 * @return modified value or null to skip the value
	 */
	public String modifyValue(T ctx, String value) {
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

	protected T initModifyValueContext(PerunSessionImpl sess, User user, AttributeDefinition destinationAttributeDefinition) throws InternalErrorException {
		//noinspection unchecked
		return (T) new ModifyValueContext(sess, user, destinationAttributeDefinition);
	}

	@Override
	public Attribute getAttributeValue(PerunSessionImpl sess, User user, AttributeDefinition destinationAttributeDefinition) throws InternalErrorException {
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
					Arrays.stream(rawValues).map(v -> modifyValue(ctx, v)).filter(Objects::nonNull).forEachOrdered(valuesWithoutDuplicities::add);
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
	 * Get list of message patterns used for resolving attribute value change.
	 * @return List of Patterns
	 */
	public List<Pattern> getPatternsForMatch() {
		List<Pattern> patterns = new ArrayList<>();
		patterns.add(allAttributesRemovedForUserExtSource);
		patterns.add(setUserExtSourceAttribute);
		patterns.add(removeUserExtSourceAttribute);

		return patterns;
	}

	@Override
	public List<String> resolveVirtualAttributeValueChange(PerunSessionImpl perunSession, String message) throws InternalErrorException, WrongReferenceAttributeValueException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<String> resolvingMessages = new ArrayList<>();
		if (message == null) return resolvingMessages;

		if (messageNeedsResolve(message)) {
			log.debug("Resolving virtual attribute value change for message: " + message);
			User user = perunSession.getPerunBl().getModulesUtilsBl().getUserFromMessage(perunSession, message);
			if (user != null) {
				Attribute attribute = perunSession.getPerunBl().getAttributesManagerBl().getAttribute(perunSession, user, getDestinationAttributeName());
				@SuppressWarnings("unchecked") List<String> attributeValue = (ArrayList<String>) attribute.getValue();
				String messageAttributeSet;
				if (attributeValue == null || attributeValue.isEmpty()) {
					AttributeDefinition attributeDefinition = new AttributeDefinition(attribute);
					messageAttributeSet = attributeDefinition.serializeToString() + " removed for " + user.serializeToString() + ".";
				} else {
					messageAttributeSet = attribute.serializeToString() + " set for " + user.serializeToString() + ".";
				}
				resolvingMessages.add(messageAttributeSet);
			}
			if(!resolvingMessages.isEmpty()) log.debug("These new messages will be generated: " + resolvingMessages);
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

	private boolean messageNeedsResolve(String message) {
		List<Pattern> patterns = getPatternsForMatch();

		for (Pattern p: patterns) {
			if (p.matcher(message).find()) {
				return true;
			}
		}

		return false;
	}
}
