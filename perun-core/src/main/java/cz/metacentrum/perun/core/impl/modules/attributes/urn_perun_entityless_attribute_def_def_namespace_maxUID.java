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
import cz.metacentrum.perun.core.implApi.modules.attributes.EntitylessAttributesModuleImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.EntitylessAttributesModuleAbstract;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Michal Stava <stavamichal@gmail.com>
 */
public class urn_perun_entityless_attribute_def_def_namespace_maxUID extends EntitylessAttributesModuleAbstract implements EntitylessAttributesModuleImplApi {

	private static final String A_E_namespaceMinUID = AttributesManager.NS_ENTITYLESS_ATTR_DEF + ":namespace-minUID";

	public void checkAttributeValue(PerunSessionImpl perunSession, String key, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
		Integer maxUID = (Integer) attribute.getValue();
		if(maxUID != null) {
			if(maxUID<1) throw new WrongAttributeValueException(attribute, "Attribute value must be min 1.");
			try {
				Attribute minUIDAttr = perunSession.getPerunBl().getAttributesManagerBl().getAttribute(perunSession, key, A_E_namespaceMinUID);
				Integer minUID = (Integer) minUIDAttr.getValue();
				if(minUID != null) {
					if(maxUID < minUID) throw new WrongAttributeValueException(attribute, "Attribute value must be more than minUID. MinUID = " + minUID + ", and maxUID try to set = " + maxUID);
				}
			} catch (AttributeNotExistsException ex) {
				throw new ConsistencyErrorException("Attribute namespace-minUID is supposed to exist.",ex);
			}
		}
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_ENTITYLESS_ATTR_DEF);
		attr.setFriendlyName("namespace_maxUID");
		attr.setDisplayName("Max UID in namespace");
		attr.setType(Integer.class.getName());
		attr.setDescription("Maximal value of User ID.");
		return attr;
	}

	@Override
	public List<String> getDependencies() {
		return Collections.singletonList(A_E_namespaceMinUID);
	}
}
