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
/**
 *
 * @author Michal Stava <stavamichal@gmail.com>
 */
public class  urn_perun_entityless_attribute_def_def_namespace_minUID extends EntitylessAttributesModuleAbstract implements EntitylessAttributesModuleImplApi {


	public void checkAttributeValue(PerunSessionImpl perunSession, String key, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
		Integer minUID = (Integer) attribute.getValue();
		if(minUID != null) {
			if(minUID<1) throw new WrongAttributeValueException(attribute, "Attribute value must be min 1.");
			try {
				Attribute maxUIDAttr = perunSession.getPerunBl().getAttributesManagerBl().getAttribute(perunSession, key, AttributesManager.NS_ENTITYLESS_ATTR_DEF + ":namespace-maxUID");
				Integer maxUID = (Integer) maxUIDAttr.getValue();
				if(maxUID != null) {
					if(minUID > maxUID) throw new WrongAttributeValueException(attribute, "Attribute value must be less than maxUID. MaxUID = " + maxUID + ", and minUID try to set = " + minUID);
				}
			} catch (AttributeNotExistsException ex) {
				throw new ConsistencyErrorException("Attribute namespace-maxUID is supposed to exist.",ex);
			}
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
}
