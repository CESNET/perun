package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.QuotaNotInAllowedLimitException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceAttributesModuleImplApi;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Attribute for setting all default resource data quotas.
 *
 * @author Michal Stava stavamichal@gmail.com
 */
public class urn_perun_resource_attribute_def_def_defaultDataQuotas extends ResourceAttributesModuleAbstract implements ResourceAttributesModuleImplApi {

	public static final String A_R_maxUserDataQuotas = AttributesManager.NS_RESOURCE_ATTR_DEF + ":maxUserDataQuotas";

	@Override
	public void checkAttributeSyntax(PerunSessionImpl perunSession, Resource resource, Attribute attribute) throws WrongAttributeValueException {
		if (attribute.getValue() == null) return;

		perunSession.getPerunBl().getModulesUtilsBl().checkAndTransferQuotas(attribute, resource, null, true);
	}

	@Override
	public void checkAttributeSemantics(PerunSessionImpl perunSession, Resource resource, Attribute attribute) throws WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
		//attribute can be null, it means there are no default settings on resource
		if (attribute.getValue() == null) {
			return;
		}

		//Check if every part of this map has the right pattern
		//And also check if every quota part has right settings (softQuota<=hardQuota)
		Map<String, Pair<BigDecimal, BigDecimal>> defaultDataQuotasForResource;
		try {
			defaultDataQuotasForResource = perunSession.getPerunBl().getModulesUtilsBl().checkAndTransferQuotas(attribute, resource, null, true);
		} catch (WrongAttributeValueException e) {
			throw new ConsistencyErrorException("Final counted quotas on " + resource + " are in bad format.", e);
		}

		//If there are no values after converting quota, we can skip testing against maxUserDataQuota attribute, because there is nothing to check
		if (defaultDataQuotasForResource == null || defaultDataQuotasForResource.isEmpty()) return;

		//Get maxUserDataQuotas value on this resource
		Attribute maxUserDataQuotasAttribute;
		try {
			maxUserDataQuotasAttribute = perunSession.getPerunBl().getAttributesManagerBl().getAttribute(perunSession, resource, A_R_maxUserDataQuotas);
		} catch (AttributeNotExistsException ex) {
			throw new ConsistencyErrorException(ex);
		}

		//Check and transfer maxUserDataQuotasForResource
		Map<String, Pair<BigDecimal, BigDecimal>> maxUserDataQuotasForResource;
		try {
			maxUserDataQuotasForResource = perunSession.getPerunBl().getModulesUtilsBl().checkAndTransferQuotas(maxUserDataQuotasAttribute, resource, null, true);
		} catch (WrongAttributeValueException ex) {
			throw new WrongReferenceAttributeValueException(attribute, maxUserDataQuotasAttribute, resource, null, resource, null,
					"Can't set defaultDataQuotas for resource, because maxUserQuota is not in correct format. Please fix it first!", ex);
		}

		try {
			perunSession.getPerunBl().getModulesUtilsBl().checkIfQuotasIsInLimit(defaultDataQuotasForResource, maxUserDataQuotasForResource);
		} catch (QuotaNotInAllowedLimitException ex) {
			throw new WrongReferenceAttributeValueException(attribute, maxUserDataQuotasAttribute, resource, null, resource, null,
					"DefaultDataQuotas for resource is not in limit of maxUserQuota!", ex);
		}
	}

	@Override
	public List<String> getDependencies() {
		List<String> dependencies = new ArrayList<>();
		dependencies.add(A_R_maxUserDataQuotas);
		return dependencies;
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_RESOURCE_ATTR_DEF);
		attr.setFriendlyName("defaultDataQuotas");
		attr.setDisplayName("Default data quotas on any volumes.");
		attr.setType(LinkedHashMap.class.getName());
		attr.setDescription("Every record is the path (to volume) and the quota in format 'SoftQuota:HardQuota' in (M, G, T, ...), G is default. Example: '10G:20T'.");
		return attr;
	}
}
