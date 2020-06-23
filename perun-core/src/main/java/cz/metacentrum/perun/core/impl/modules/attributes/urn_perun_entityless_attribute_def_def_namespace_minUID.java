package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.EntitylessAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.EntitylessAttributesModuleImplApi;

import java.util.Collections;
import java.util.List;

/**
 *
 * @author Michal Stava <stavamichal@gmail.com>
 */
public class  urn_perun_entityless_attribute_def_def_namespace_minUID extends EntitylessAttributesModuleAbstract implements EntitylessAttributesModuleImplApi {

	private static final String A_E_namespaceMaxUID = AttributesManager.NS_ENTITYLESS_ATTR_DEF + ":namespace-maxUID";

	@Override
	public void checkAttributeSyntax(PerunSessionImpl perunSession, String key, Attribute attribute) throws WrongAttributeValueException {
		Integer minUID = attribute.valueAsInteger();
		if(minUID != null && minUID<1) throw new WrongAttributeValueException(attribute, "Attribute value must be bigger than 0.");
	}

	@Override
	public void checkAttributeSemantics(PerunSessionImpl perunSession, String key, Attribute attribute) throws WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
		Integer minUID = attribute.valueAsInteger();
		if (minUID == null) return;
		try {
			Attribute maxUIDAttr = perunSession.getPerunBl().getAttributesManagerBl().getAttribute(perunSession, key, A_E_namespaceMaxUID);
			Integer maxUID = (Integer) maxUIDAttr.getValue();
			if(maxUID != null) {
				if(minUID > maxUID) throw new WrongReferenceAttributeValueException(attribute, maxUIDAttr, key, null, key, null, "Attribute value must be less than maxUID. MaxUID = " + maxUID + ", and minUID try to set = " + minUID);
			}
		} catch (AttributeNotExistsException ex) {
			throw new ConsistencyErrorException("Attribute namespace-maxUID is supposed to exist.",ex);
		}
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_ENTITYLESS_ATTR_DEF);
		attr.setFriendlyName("namespace_minUID");
		attr.setDisplayName("Min UID in namespace");
		attr.setType(Integer.class.getName());
		attr.setDescription("Minimal value of User ID.");
		return attr;
	}

	@Override
	public List<String> getDependencies() {
		return Collections.singletonList(A_E_namespaceMaxUID);
	}
}
