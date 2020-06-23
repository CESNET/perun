package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleImplApi;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Slavek Licehammer &lt;glory@ics.muni.cz&gt;
 */
public class urn_perun_user_attribute_def_def_eduroamIdentities extends UserAttributesModuleAbstract implements UserAttributesModuleImplApi {

	private static final Pattern pattern = Pattern.compile("^[-/_.a-zA-Z0-9]+@[-_.A-z0-9]+$");

	@Override
	public void checkAttributeSyntax(PerunSessionImpl sess, User user, Attribute attribute) throws WrongAttributeValueException {
		if(attribute == null) return; //null is OK
		List<String> value = attribute.valueAsList();
		for(String login : value) {
			Matcher matcher = pattern.matcher(login);
			if(!matcher.matches()) throw new WrongAttributeValueException(attribute, "Value is not in correct format. format: login@organization");
		}
	}

	@Override
	public Attribute fillAttribute(PerunSessionImpl sess, User user, AttributeDefinition attributeDefinition) throws WrongAttributeAssignmentException {
		List<String> value = new ArrayList<>();
		try {
			String loginMU = (String) sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, user, "urn:perun:user:attribute-def:def:login-namespace:mu").getValue();
			if(loginMU != null) value.add(loginMU + "@eduroam.muni.cz");
			String loginCesnet = (String) sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, user, "urn:perun:user:attribute-def:def:login-namespace:cesnet").getValue();
			if(loginCesnet != null) value.add(loginCesnet + "@cesnet.cz");
		} catch(AttributeNotExistsException ex) {
			throw new ConsistencyErrorException(ex);
		}

		Attribute attribute = new Attribute(attributeDefinition);
		attribute.setValue(value);
		return attribute;
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
		attr.setFriendlyName("eduroamIdentities");
		attr.setDisplayName("EDUROAM identity");
		attr.setType(ArrayList.class.getName());
		attr.setDescription("List of eduroam identities.");
		return attr;
	}
}
