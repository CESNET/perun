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

/**
 *
 * @author Michal Stava <stavamichal@gmail.com>
 */
@Deprecated
public class urn_perun_entityless_attribute_def_def_namespace_maxGID extends EntitylessAttributesModuleAbstract implements EntitylessAttributesModuleImplApi {

	public void checkAttributeValue(PerunSessionImpl perunSession, String key, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
		Integer maxGID = (Integer) attribute.getValue();
		if(maxGID != null) {
			if(maxGID<1) throw new WrongAttributeValueException(attribute, "Attribute value must be min 1.");
			try {
				Attribute minGIDAttr = perunSession.getPerunBl().getAttributesManagerBl().getAttribute(perunSession, key, AttributesManager.NS_ENTITYLESS_ATTR_DEF + ":namespace-minGID");
				Integer minGID = (Integer) minGIDAttr.getValue();
				if(minGID != null) {
					if(maxGID < minGID) throw new WrongAttributeValueException(attribute, "Attribute value must be more than minGID. MinGID = " + minGID + ", and maxGID try to set = " + maxGID);
				}
			} catch (AttributeNotExistsException ex) {
				throw new ConsistencyErrorException("Attribute namespace-minGID is supposed to exist.",ex);
			}
		}
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_ENTITYLESS_ATTR_DEF);
		attr.setFriendlyName("namespace_maxGID");
		attr.setDisplayName("Max GID in namespace");
		attr.setType(Integer.class.getName());
		attr.setDescription("Maximal value of Group ID.");
		return attr;
	}
}
