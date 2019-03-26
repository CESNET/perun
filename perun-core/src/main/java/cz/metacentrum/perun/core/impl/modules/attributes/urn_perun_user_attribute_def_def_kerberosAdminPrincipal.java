package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleImplApi;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Checks specified kerberos admin principal (check existence of only one kerberos principal)
 *
 * @author Michal Šťava   <stava.michal@gmail.com>
 */
public class urn_perun_user_attribute_def_def_kerberosAdminPrincipal extends UserAttributesModuleAbstract implements UserAttributesModuleImplApi {

	private static final Pattern pattern = Pattern.compile("^[-/_.a-zA-Z0-9]+@[-_.A-z0-9]+$");

	@Override
	public void checkAttributeValue(PerunSessionImpl perunSession, User user, Attribute attribute) throws WrongAttributeValueException {
		if(attribute.getValue() == null) throw new WrongAttributeValueException(attribute, user, "Attribute's value can't be null");
		String value = (String) attribute.getValue();
		Matcher matcher = pattern.matcher(value);
		if(!matcher.matches()) throw new WrongAttributeValueException(attribute, user, "Attribute's value is not in correct format. format: login@realm");
	}

	@Override
	public Attribute fillAttribute(PerunSessionImpl perunSession, User user, AttributeDefinition attribute) {
		return new Attribute(attribute);
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
		attr.setFriendlyName("kerberosAdminPrincipal");
		attr.setDisplayName("KERBEROS admin principal");
		attr.setType(String.class.getName());
		attr.setDescription("Kerberos pricipal used for root access.");
		return attr;
	}

}
