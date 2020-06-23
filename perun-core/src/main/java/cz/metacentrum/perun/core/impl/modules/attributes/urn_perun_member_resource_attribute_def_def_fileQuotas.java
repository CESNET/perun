package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Member;
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
import cz.metacentrum.perun.core.implApi.modules.attributes.MemberResourceAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.MemberResourceAttributesModuleImplApi;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Attribute for setting all member resource specific file quotas.
 *
 * @author Michal Stava stavamichal@gmail.com
 */
public class urn_perun_member_resource_attribute_def_def_fileQuotas extends MemberResourceAttributesModuleAbstract implements MemberResourceAttributesModuleImplApi {

	public static final String A_R_maxUserFileQuotas = AttributesManager.NS_RESOURCE_ATTR_DEF + ":maxUserFileQuotas";

	@Override
	public void checkAttributeSyntax(PerunSessionImpl perunSession, Member member, Resource resource, Attribute attribute) throws WrongAttributeValueException {
		if(attribute.getValue() == null) return;

		perunSession.getPerunBl().getModulesUtilsBl().checkAndTransferQuotas(attribute, resource, member, false);
	}

	@Override
	public void checkAttributeSemantics(PerunSessionImpl perunSession, Member member, Resource resource, Attribute attribute) throws WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
		//attribute can be null, it means there are no default settings on resource
		if(attribute.getValue() == null) {
			return;
		}

		//Check if every part of this map has the right pattern
		//And also check if every quota part has right settings (softQuota<=hardQuota)
		Map<String, Pair<BigDecimal, BigDecimal>> fileQuotasForMemberOnResource;
		try {
			fileQuotasForMemberOnResource = perunSession.getPerunBl().getModulesUtilsBl().checkAndTransferQuotas(attribute, resource, member, false);
		} catch (WrongAttributeValueException e) {
			throw new ConsistencyErrorException("Quotas on " + resource + " for member " + member + " are in bad format.", e);
		}

		//If there are no values after converting quota, we can skip testing against maxUserFileQuota attribute, because there is nothing to check
		if (fileQuotasForMemberOnResource == null || fileQuotasForMemberOnResource.isEmpty()) return;

		//Get maxUserFileQuotas value on this resource
		Attribute maxUserFileQuotasAttribute;
		try {
			maxUserFileQuotasAttribute = perunSession.getPerunBl().getAttributesManagerBl().getAttribute(perunSession, resource, A_R_maxUserFileQuotas);
		} catch (AttributeNotExistsException ex) {
			throw new ConsistencyErrorException(ex);
		}

		//Check and transfer maxUserFileQuotasForResource
		Map<String, Pair<BigDecimal, BigDecimal>> maxUserFileQuotasForResource;
		try {
			maxUserFileQuotasForResource = perunSession.getPerunBl().getModulesUtilsBl().checkAndTransferQuotas(maxUserFileQuotasAttribute, resource, null, false);
		} catch (WrongAttributeValueException ex) {
			throw new WrongReferenceAttributeValueException(attribute, maxUserFileQuotasAttribute, resource, member, resource, null,
					"Can't set fileQuotas for member on resource, because maxUserQuota is not in correct format. Please fix it first!", ex);
		}

		try {
			perunSession.getPerunBl().getModulesUtilsBl().checkIfQuotasIsInLimit(fileQuotasForMemberOnResource, maxUserFileQuotasForResource);
		} catch (QuotaNotInAllowedLimitException ex) {
			throw new WrongReferenceAttributeValueException(attribute, maxUserFileQuotasAttribute, member, resource, resource, null,
					"FileQuotas for member on resource is not in limit of maxUserQuota!", ex);
		}
	}

	@Override
	public List<String> getDependencies() {
		List<String> dependencies = new ArrayList<>();
		dependencies.add(A_R_maxUserFileQuotas);
		return dependencies;
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_MEMBER_RESOURCE_ATTR_DEF);
		attr.setFriendlyName("fileQuotas");
		attr.setDisplayName("File quotas for member on resource");
		attr.setType(LinkedHashMap.class.getName());
		attr.setDescription("Every record is the path (to volume) and the quota in format 'SoftQuota:HardQuota'. Example: '1000:2000'.");
		return attr;
	}
}
