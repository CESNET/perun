package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserFacilityAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserFacilityAttributesModuleImplApi;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Slavek Licehammer &lt;glory@ics.muni.cz&gt;
 */
public class urn_perun_user_facility_attribute_def_def_shell_passwd_scp extends UserFacilityAttributesModuleAbstract implements UserFacilityAttributesModuleImplApi {

	private static final Pattern pattern = Pattern.compile("^(/[-_.a-zA-Z0-9]+)+$");

	@Override
	public void checkAttributeSyntax(PerunSessionImpl sess, User user, Facility facility, Attribute attribute) throws WrongAttributeValueException {
		String shell = attribute.valueAsString();

		if(shell == null) return;
		Matcher matcher = pattern.matcher(shell);
		if(!matcher.matches()) throw new WrongAttributeValueException(attribute, "Wrong format. ^(/[-_.A-z0-9]+)+$ expected");
	}

	@Override
	public void checkAttributeSemantics(PerunSessionImpl sess, User user, Facility facility, Attribute attribute) throws WrongReferenceAttributeValueException {
		if (attribute.valueAsString() == null) throw new WrongReferenceAttributeValueException(attribute, "Value can't be null");
	}

	@Override
	public Attribute fillAttribute(PerunSessionImpl sess, User user, Facility facility, AttributeDefinition attributeDefinition) throws WrongAttributeAssignmentException {
		Attribute attribute = new Attribute(attributeDefinition);
		try {
			Attribute shellOnFacilityAttribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, facility, AttributesManager.NS_FACILITY_ATTR_DEF + ":" + attributeDefinition.getFriendlyName());
			attribute.setValue(shellOnFacilityAttribute.getValue());
			return attribute;
		} catch(AttributeNotExistsException ex) {
			throw new ConsistencyErrorException(ex);
		}
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_FACILITY_ATTR_DEF);
		attr.setFriendlyName("shell_passwd_scp");
		attr.setDisplayName("Shell for passwd_scp");
		attr.setType(String.class.getName());
		attr.setDescription("Shell password.");
		return attr;
	}
}
