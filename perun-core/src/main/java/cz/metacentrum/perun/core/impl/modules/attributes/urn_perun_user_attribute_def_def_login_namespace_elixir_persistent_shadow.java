package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserPersistentShadowAttribute;

/**
 * Class for checking logins uniqueness in the namespace and filling elixir-persistent id.
 * It is only storage! Use module login elixir_persistent for access the value.
 *
 * @author Sona Mastrakova <sona.mastrakova@gmail.com>
 * @author Ondrej Velisek <ondrejvelisek@gmail.com>
 *
 * @date 06.07.2015
 */
public class urn_perun_user_attribute_def_def_login_namespace_elixir_persistent_shadow
		extends UserPersistentShadowAttribute {

	private final static String extSourceNameElixir = "https://login.elixir-czech.org/idp/";
	private final static String domainNameElixir = "elixir-europe.org";
	private final static String attrNameElixir = "login-namespace:elixir-persistent-shadow";
	private final static String lifeScienceShadow = "urn:perun:user:attribute-def:def:login-namespace:lifescienceid-persistent-shadow";
	private final static String bbmriShadow = "urn:perun:user:attribute-def:def:login-namespace:bbmri-persistent-shadow";

	@Override
	public String getFriendlyName() {
		return attrNameElixir;
	}

	@Override
	public String getExtSourceName() {
		return extSourceNameElixir;
	}

	@Override
	public String getDomainName() {
		return domainNameElixir;
	}

	@Override
	public String getDescription() {
		return "Login to ELIXIR. Do not use it directly! " +
			   "Use \"user:virt:login-namespace:elixir-persistent\" attribute instead.";
	}

	@Override
	public String getDisplayName() {
		return "ELIXIR login";
	}

	@Override
	public String getFriendlyNameParameter() {
		return "elixir-persistent-shadow";
	}

	@Override
	public Attribute fillAttribute(PerunSessionImpl sess, User user, AttributeDefinition attribute) {
		// Check if user has login in namespace lifescienceid-persistent-shadow
		Attribute persistentShadow = null;
		try {
			persistentShadow = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, user, lifeScienceShadow);
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
			String attrValue = valueWithoutScope + "@" + domainNameElixir;
			filledAttribute.setValue(attrValue);
			return filledAttribute;
		}

		return super.fillAttribute(sess, user, attribute);
	}
}
