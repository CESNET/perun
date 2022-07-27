package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserPersistentShadowAttributeWithConfig;

/**
 * Class for checking logins uniqueness in the namespace and filling lifescienceid-persistent id.
 * It is only storage! Use module login lifescienceid_persistent for access the value.
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class urn_perun_user_attribute_def_def_login_namespace_lifescienceid_persistent_shadow
		extends UserPersistentShadowAttributeWithConfig {

	private final static String attrNameLifeScience = "login-namespace:lifescienceid-persistent-shadow";
	private final static String elixirShadow = "urn:perun:user:attribute-def:def:login-namespace:elixir-persistent-shadow";
	private final static String bbmriShadow = "urn:perun:user:attribute-def:def:login-namespace:bbmri-persistent-shadow";

	private final static String CONFIG_EXT_SOURCE_NAME_LIFESCIENCE = "extSourceNameLifeScience";
	private final static String CONFIG_DOMAIN_NAME_LIFESCIENCE = "domainNameLifeScience";

	@Override
	public String getExtSourceConfigName() {
		return CONFIG_EXT_SOURCE_NAME_LIFESCIENCE;
	}

	@Override
	public String getDomainConfigName() {
		return CONFIG_DOMAIN_NAME_LIFESCIENCE;
	}

	@Override
	public String getFriendlyName() {
		return attrNameLifeScience;
	}

	@Override
	public String getDescription() {
		return "Login to Lifescienceid. Do not use it directly! " +
			"Use \"user:virt:login-namespace:lifescienceid-persistent\" attribute instead.";
	}

	@Override
	public String getFriendlyNameParameter() {
		return "lifescienceid-persistent-shadow";
	}

	@Override
	public String getDisplayName() {
		return "Lifescienceid login";
	}

	@Override
	public Attribute fillAttribute(PerunSessionImpl sess, User user, AttributeDefinition attribute) {
		// Check if user has login in namespace elixir-persistent-shadow
		Attribute persistentShadow = null;
		try {
			persistentShadow = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, user, elixirShadow);
		} catch (WrongAttributeAssignmentException | AttributeNotExistsException ignored) {
		}

		if (persistentShadow == null || persistentShadow.getValue() == null) {
			// Check if user has login in namespace bbmri-persistent-shadow
			try {
				persistentShadow = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, user, bbmriShadow);
			} catch (WrongAttributeAssignmentException | AttributeNotExistsException ignored) {
			}
		}

		if (persistentShadow != null && persistentShadow.getValue() != null) {
			Attribute filledAttribute = new Attribute(attribute);
			String value = persistentShadow.getValue().toString();
			String valueWithoutScope = value.split("@", 2) [0];
			String attrValue = valueWithoutScope + "@" + getDomainName();
			filledAttribute.setValue(attrValue);
			return filledAttribute;
		}

		return super.fillAttribute(sess, user, attribute);
	}
}
