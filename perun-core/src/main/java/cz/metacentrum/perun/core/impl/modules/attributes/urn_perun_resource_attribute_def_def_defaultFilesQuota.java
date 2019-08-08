package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceAttributesModuleImplApi;

import java.util.Collections;
import java.util.List;

/**
 *
 * @author Michal Stava stavamichal@gmail.com
 */
public class urn_perun_resource_attribute_def_def_defaultFilesQuota extends ResourceAttributesModuleAbstract implements ResourceAttributesModuleImplApi {

	private static final String A_R_defaultFilesLimit = AttributesManager.NS_RESOURCE_ATTR_DEF + ":defaultFilesLimit";
	private static final String A_F_readyForNewQuotas = AttributesManager.NS_FACILITY_ATTR_DEF + ":readyForNewQuotas";

	@Override
	public void checkAttributeSemantics(PerunSessionImpl perunSession, Resource resource, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
		Attribute attrDefaultFilesLimit;
		Integer defaultFilesQuota = null;
		Integer defaultFilesLimit = null;

		//Get DefaultFilesLimit attribute
		try {
			attrDefaultFilesLimit = perunSession.getPerunBl().getAttributesManagerBl().getAttribute(perunSession, resource, A_R_defaultFilesLimit);
		} catch (AttributeNotExistsException ex) {
			throw new ConsistencyErrorException("Attribute with defaultFilesLimit from resource " + resource.getId() + " could not obtained.", ex);
		}

		//Get defaultFilesQuota value
		if(attribute.getValue() != null) {
			defaultFilesQuota = (Integer) attribute.getValue();
		}
		if(defaultFilesQuota != null && defaultFilesQuota < 0) throw new WrongAttributeValueException(attribute, resource, attribute + " cannot be less than 0.");

		//Get defaultFilesLimit value
		if(attrDefaultFilesLimit != null &&  attrDefaultFilesLimit.getValue() != null) {
			defaultFilesLimit = (Integer) attrDefaultFilesLimit.getValue();
		}
		if(defaultFilesLimit != null && defaultFilesLimit < 0) throw new ConsistencyErrorException(attrDefaultFilesLimit + " cannot be less than 0.");

		//Compare DefaultFilesQuota with DefaultFilesLimit
		if(defaultFilesQuota == null || defaultFilesQuota == 0) {
			if(defaultFilesLimit != null && defaultFilesLimit != 0) throw new WrongReferenceAttributeValueException(attribute, attrDefaultFilesLimit, resource, null, resource, null, "Try to set unlimited quota, but limit is still " + defaultFilesLimit);
		}else if(defaultFilesLimit != null && defaultFilesLimit != 0) {
			if(defaultFilesLimit < defaultFilesQuota) throw new WrongReferenceAttributeValueException(attribute, attrDefaultFilesLimit, resource, null, resource, null, attribute + " must be less than or equals " + attrDefaultFilesLimit);
		}
	}

	@Override
	public void changedAttributeHook(PerunSessionImpl session, Resource resource, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException {
		//if this is setting of the new attribute value, check if old quota attributes are supported on the facility
		if(attribute.getValue() != null) {
			try {
				Facility facility = session.getPerunBl().getResourcesManagerBl().getFacility(session, resource);
				Attribute readyForNewQuotasAttribute = session.getPerunBl().getAttributesManagerBl().getAttribute(session, facility, A_F_readyForNewQuotas);
				//You shouldn't be allowed to set old quota attributes if facility is set for new quotas attributes (to prevent wrong setting of quotas)
				if(readyForNewQuotasAttribute.getValue() != null && readyForNewQuotasAttribute.valueAsBoolean()) {
					throw new WrongReferenceAttributeValueException(attribute, readyForNewQuotasAttribute, resource, null, facility, null, "For this facility the new quotas attributes are used! You are trying to set the old ones.");
				}
			} catch (AttributeNotExistsException ex) {
				//if attribute not exists, it is the same like it was set on false, which is ok
			} catch (WrongAttributeAssignmentException ex) {
				throw new InternalErrorException(ex);
			}
		}
	}

	@Override
	public List<String> getDependencies() {
		return Collections.singletonList(A_R_defaultFilesLimit);
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_RESOURCE_ATTR_DEF);
		attr.setFriendlyName("defaultFilesQuota");
		attr.setDisplayName("Default files quota");
		attr.setType(Integer.class.getName());
		attr.setDescription("Soft quota for number of files.");
		return attr;
	}
}
