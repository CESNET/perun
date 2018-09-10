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
import cz.metacentrum.perun.core.api.exceptions.MemberResourceMismatchException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceMemberVirtualAttributesModuleAbstract;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import java.util.List;
import java.util.Map;

/**
 * Virtual attribute to count all data quotas for member on resource.
 *
 * For every volume count final value of data quotas for this member on this resource.
 *
 * @author Michal Stava stavamichal@gmail.com
 */
public class urn_perun_member_resource_attribute_def_virt_dataQuotas extends ResourceMemberVirtualAttributesModuleAbstract {
	public static final String A_R_defaultDataQuotas = AttributesManager.NS_RESOURCE_ATTR_DEF + ":defaultDataQuotas";
	public static final String A_MR_dataQuotas = AttributesManager.NS_MEMBER_RESOURCE_ATTR_DEF + ":dataQuotas";
	public static final String A_MR_dataQuotasOverride = AttributesManager.NS_MEMBER_RESOURCE_ATTR_DEF + ":dataQuotasOverride";

	@Override
	public Attribute getAttributeValue(PerunSessionImpl sess, Resource resource, Member member, AttributeDefinition attributeDefinition) throws InternalErrorException {
		Attribute attribute = new Attribute(attributeDefinition);

		//get resource quotas
		Map<String, Pair<BigDecimal, BigDecimal>> resourceTransferedQuotas;
		Attribute resourceQuotas;
		try {
			resourceQuotas = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, resource, A_R_defaultDataQuotas);
		} catch (AttributeNotExistsException ex) {
			throw new ConsistencyErrorException(ex);
		} catch (WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}

		if(resourceQuotas == null || resourceQuotas.getValue() == null) resourceTransferedQuotas = new HashMap<>();
		else {
			try {
				resourceTransferedQuotas = sess.getPerunBl().getModulesUtilsBl().checkAndTransferQuotas(resourceQuotas, resource, null, true);
			} catch (WrongAttributeValueException ex) {
				throw new ConsistencyErrorException("Quotas on resource " + resource + " are in bad format.", ex);
			}
		}

		//get members quotas
		Map<String, Pair<BigDecimal, BigDecimal>> memberTransferedQuotas;
		Attribute memberQuotas;
		try {
			memberQuotas = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, resource, member, A_MR_dataQuotas);
		} catch (AttributeNotExistsException ex) {
			throw new ConsistencyErrorException(ex);
		} catch (WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		} catch (MemberResourceMismatchException ex) {
			throw new InternalErrorException(ex);
		}

		if(memberQuotas == null || memberQuotas.getValue() == null) memberTransferedQuotas = new HashMap<>();
		else {
			try {
				memberTransferedQuotas = sess.getPerunBl().getModulesUtilsBl().checkAndTransferQuotas(memberQuotas, resource, member, true);
			} catch (WrongAttributeValueException ex) {
				throw new ConsistencyErrorException("Quotas on resource " + resource + " for member " + member + " are in bad format.", ex);
			}
		}

		//get members quotas override
		Map<String, Pair<BigDecimal, BigDecimal>> memberTransferedQuotasOverride;
		Attribute memberQuotasOverride;
		try {
			memberQuotasOverride = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, resource, member, A_MR_dataQuotasOverride);
		} catch (AttributeNotExistsException ex) {
			throw new ConsistencyErrorException(ex);
		} catch (WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		} catch (MemberResourceMismatchException ex) {
			throw new InternalErrorException(ex);
		}

		if(memberQuotasOverride == null || memberQuotasOverride.getValue() == null) memberTransferedQuotasOverride = new HashMap<>();
		else {
			try {
				memberTransferedQuotasOverride = sess.getPerunBl().getModulesUtilsBl().checkAndTransferQuotas(memberQuotasOverride, resource, member, true);
			} catch (WrongAttributeValueException ex) {
				throw new ConsistencyErrorException("Override quotas on resource " + resource + " for member " + member + " are in bad format.", ex);
			}
		}

		//Merge quotas for member on resource
		Map<String, Pair<BigDecimal, BigDecimal>> finalMemberResourceQuotas = sess.getPerunBl().getModulesUtilsBl().mergeMemberAndResourceTransferredQuotas(resourceTransferedQuotas, memberTransferedQuotas, memberTransferedQuotasOverride);

		//set final value to attribute
		attribute.setValue(sess.getPerunBl().getModulesUtilsBl().transferQuotasBackToAttributeValue(finalMemberResourceQuotas, true));

		//return attribute
		return attribute;
	}

	@Override
	public List<String> getStrongDependencies() {
		List<String> strongDependencies = new ArrayList<String>();
		strongDependencies.add(A_R_defaultDataQuotas);
		strongDependencies.add(A_MR_dataQuotas);
		strongDependencies.add(A_MR_dataQuotasOverride);
		return strongDependencies;
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_MEMBER_RESOURCE_ATTR_VIRT);
		attr.setFriendlyName("dataQuotas");
		attr.setDisplayName("Computed data quotas for a member on a resource");
		attr.setType(LinkedHashMap.class.getName());
		attr.setDescription("Every record is the path (to volume) and the quota in format 'SoftQuota:HardQuota' in (M, G, T, ...), G is default. Example: '10G:20T'. For every volume count final value of data quotas for this member on this resource.");
		return attr;
	}
}
