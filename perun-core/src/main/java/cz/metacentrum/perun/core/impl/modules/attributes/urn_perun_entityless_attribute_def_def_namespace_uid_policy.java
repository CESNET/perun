package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.EntitylessAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.EntitylessAttributesModuleImplApi;
/**
 * Namespace_uid_policy attribute specifies how IDs are generated in the namespace. Possible value is "recycle" or "increment". <br />
 * <b>Recycle</b> - Always use first free id within allowed range.
 * <b>Increment</b> - Each new generated ID is one grater than previously generated ID.
 *
 *
 * @author Slavek Licehammer &lt;glory@ics.muni.cz&gt;
 */
public class urn_perun_entityless_attribute_def_def_namespace_uid_policy extends EntitylessAttributesModuleAbstract implements EntitylessAttributesModuleImplApi {

	public static final String RECYCLE_POLICY = "recycle";
	public static final String INCREMENT_POLICY = "increment";

	@Override
	public void checkAttributeValue(PerunSessionImpl perunSession, String key, Attribute attribute) throws WrongAttributeValueException {
		if(attribute.getValue() == null) return;
		if(!(RECYCLE_POLICY.equals(attribute.getValue()) || INCREMENT_POLICY.equals(attribute.getValue()))) throw new WrongAttributeValueException(attribute, key, "Posible values for this attribute are " + RECYCLE_POLICY + " or " + INCREMENT_POLICY);
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_ENTITYLESS_ATTR_DEF);
		attr.setFriendlyName("namespace-uid-policy");
		attr.setDisplayName("UID namespace policy");
		attr.setType(String.class.getName());
		attr.setDescription("Policy for generating new UID number. recycle - use first available UID, increment - add 1 to last used UID (maximal one)");
		return attr;
	}
}
