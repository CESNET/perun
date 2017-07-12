package cz.metacentrum.perun.core.implApi.modules.attributes;

import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.bl.AttributesManagerBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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
public abstract class UserVirtualAttributeCollectedFromUserExtSource extends UserVirtualAttributesModuleAbstract {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	/**
	 * Specifies friendly (short) name of attribute from namespace urn:perun:ues:attribute-def:def
	 * whose values are to be collected.
	 *
	 * @return short name of userExtSource attribute which is source of values
	 */
	public abstract String getSourceAttributeFriendlyName();

	/**
	 * Specifies friendly (short) name of attribute from namespace urn:perun:user:attribute-def:virt
	 * where values will be stored
	 *
	 * @return short name of user attribute which is destination for collected values
	 */
	public abstract String getDestinationAttributeFriendlyName();

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
	 * @param value of userExtSource attribute
	 * @return modified value or null to skip the value
	 */
	public String modifyValue(String value) {
		return value;
	}

	@Override
	public Attribute getAttributeValue(PerunSessionImpl sess, User user, AttributeDefinition destinationAttributeDefinition) throws InternalErrorException {

		Attribute destinationAttribute = new Attribute(destinationAttributeDefinition);
		List<String> values = new ArrayList<>();

		String sourceAttributeFriendlyName = getSourceAttributeFriendlyName();
		List<UserExtSource> userExtSources = sess.getPerunBl().getUsersManagerBl().getUserExtSources(sess, user);
		AttributesManagerBl am = sess.getPerunBl().getAttributesManagerBl();

		for (UserExtSource userExtSource : userExtSources) {
			try {
				Attribute a = am.getAttribute(sess, userExtSource, "urn:perun:ues:attribute-def:def:" + sourceAttributeFriendlyName);
				Object value = a.getValue();
				if (value != null && value instanceof String) {
					//Apache mod_shib joins multiple values with ';', split them again
					String[] rawValues = ((String) value).split(";");
					//add non-null values returned by modifyValue()
					Arrays.stream(rawValues).map(this::modifyValue).filter(Objects::nonNull).forEachOrdered(values::add);
				}
			} catch (WrongAttributeAssignmentException | AttributeNotExistsException e) {
				log.error("cannot read " + sourceAttributeFriendlyName + " from userExtSource " + userExtSource.getId() + " of user " + user.getId(), e);
			}
		}
		destinationAttribute.setValue(values);
		return destinationAttribute;
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
