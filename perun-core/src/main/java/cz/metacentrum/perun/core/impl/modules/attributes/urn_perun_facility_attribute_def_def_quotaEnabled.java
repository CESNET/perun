package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.FacilityAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.FacilityAttributesModuleImplApi;

/**
 * Attribute with information if quota is enabled on facility
 *
 * @author Michal Šťava <stavamichal@gmail.com>
 */
public class urn_perun_facility_attribute_def_def_quotaEnabled extends FacilityAttributesModuleAbstract implements FacilityAttributesModuleImplApi  {

	@Override
	public void checkAttributeSyntax(PerunSessionImpl perunSession, Facility facility, Attribute attribute) throws WrongAttributeValueException {
		//Null means the same like 0 (not enabled)
		if(attribute.getValue() == null) return;
		
		Integer quotaEnabled = attribute.valueAsInteger();
		if(quotaEnabled > 1 || quotaEnabled < 0) throw new WrongAttributeValueException(attribute, facility, "Attribute has only two possible options for quota: 0 - means not enabled, 1 - means enabled.");
		 
	}

	@Override
	public Attribute fillAttribute(PerunSessionImpl session, Facility facility, AttributeDefinition attribute) {
		//Default is 0, it means quota is not enabled (null means the same)
		Attribute retAttr = new Attribute(attribute);
		retAttr.setValue(0);
		return retAttr;
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_FACILITY_ATTR_DEF);
		attr.setFriendlyName("quotaEnabled");
		attr.setDisplayName("Quota eanbled");
		attr.setType(Integer.class.getName());
		attr.setDescription("Attribute says if quota is enabled on facility.");
		return attr;
	}

}
