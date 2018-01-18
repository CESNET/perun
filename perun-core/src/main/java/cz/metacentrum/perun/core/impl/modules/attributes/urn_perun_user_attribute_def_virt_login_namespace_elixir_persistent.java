package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleAbstract;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class for access def:elixir-persistent-shadow attribute. It generates value if you call it for the first time.
 *
 * @author Ondrej Velisek <ondrejvelisek@gmail.com>
 */
public class urn_perun_user_attribute_def_virt_login_namespace_elixir_persistent extends UserVirtualAttributesModuleAbstract {

	public static final String SHADOW = "urn:perun:user:attribute-def:def:login-namespace:elixir-persistent-shadow";

	@Override
	public Attribute getAttributeValue(PerunSessionImpl sess, User user, AttributeDefinition attributeDefinition) throws InternalErrorException {
		Attribute elixirPersistent = new Attribute(attributeDefinition);

		try {
			Attribute elixirPersistentShadow = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, user, SHADOW);

			if (elixirPersistentShadow.getValue() == null) {

				elixirPersistentShadow = sess.getPerunBl().getAttributesManagerBl().fillAttribute(sess, user, elixirPersistentShadow);

				if (elixirPersistentShadow.getValue() == null) {
					throw new InternalErrorException("Elixir id couldn't be set automatically");
				}
				sess.getPerunBl().getAttributesManagerBl().setAttribute(sess, user, elixirPersistentShadow);
			}

			elixirPersistent.setValue(elixirPersistentShadow.getValue());
			return elixirPersistent;

		} catch (WrongAttributeAssignmentException e) {
			throw new InternalErrorException(e);
		} catch (AttributeNotExistsException e) {
			throw new InternalErrorException(e);
		} catch (WrongReferenceAttributeValueException e) {
			throw new InternalErrorException(e);
		} catch (WrongAttributeValueException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public List<String> getStrongDependencies() {
		return Collections.singletonList(SHADOW);
	}

	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_VIRT);
		attr.setFriendlyName("login-namespace:elixir-persistent");
		attr.setDisplayName("ELIXIR login");
		attr.setType(String.class.getName());
		attr.setDescription("Login to ELIXIR. It is set automatically with first call.");
		return attr;
	}
}
