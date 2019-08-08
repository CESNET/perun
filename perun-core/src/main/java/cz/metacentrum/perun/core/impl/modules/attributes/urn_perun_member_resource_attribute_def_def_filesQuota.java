package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MemberResourceMismatchException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.MemberResourceAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.MemberResourceAttributesModuleImplApi;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Michal Stava stavamichal@gmail.com
 */
public class urn_perun_member_resource_attribute_def_def_filesQuota extends MemberResourceAttributesModuleAbstract implements MemberResourceAttributesModuleImplApi {

	private static final String A_R_defaultFilesLimit = AttributesManager.NS_RESOURCE_ATTR_DEF + ":defaultFilesLimit";
	private static final String A_R_defaultFilesQuota = AttributesManager.NS_RESOURCE_ATTR_DEF + ":defaultFilesQuota";
	private static final String A_MR_filesLimit = AttributesManager.NS_MEMBER_RESOURCE_ATTR_DEF + ":filesLimit";
	private static final String A_F_readyForNewQuotas = AttributesManager.NS_FACILITY_ATTR_DEF + ":readyForNewQuotas";

	@Override
	public void checkAttributeSemantics(PerunSessionImpl perunSession, Member member, Resource resource, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
		Attribute attrFilesLimit;
		Integer filesQuota = null;
		Integer filesLimit = null;

		//Get FilesLimit attribute
		try {
			attrFilesLimit = perunSession.getPerunBl().getAttributesManagerBl().getAttribute(perunSession, member, resource, A_MR_filesLimit);
		} catch (AttributeNotExistsException ex) {
			throw new ConsistencyErrorException(attribute + " from member " + member.getId() + " and resource " + resource.getId() + " could not obtained.", ex);
		} catch (MemberResourceMismatchException ex) {
			throw new InternalErrorException(ex);
		}

		//Get FilesQuota value
		if(attribute.getValue() != null) {
			filesQuota = (Integer) attribute.getValue();
		} else {
			try {
				attribute = perunSession.getPerunBl().getAttributesManagerBl().getAttribute(perunSession, resource, A_R_defaultFilesQuota);
			} catch (AttributeNotExistsException ex) {
				throw new ConsistencyErrorException("Attribute with defaultFilesQuota from resource " + resource.getId() + " could not obtained.", ex);
			}
			if(attribute != null && attribute.getValue() != null) {
				filesQuota = (Integer) attribute.getValue();
			}
		}
		if(filesQuota != null && filesQuota < 0) throw new WrongAttributeValueException(attribute, resource, member, attribute + " cannot be less than 0.");

		//Get FilesLimit value
		if(attrFilesLimit != null &&  attrFilesLimit.getValue() != null) {
			filesLimit = (Integer) attrFilesLimit.getValue();
		} else {
			try {
				attrFilesLimit = perunSession.getPerunBl().getAttributesManagerBl().getAttribute(perunSession, resource, A_R_defaultFilesLimit);
			} catch (AttributeNotExistsException ex) {
				throw new ConsistencyErrorException("Attribute with defaultFilesLimit from resource " + resource.getId() + " could not obtained.", ex);
			}
			if(attrFilesLimit != null && attrFilesLimit.getValue() != null) {
				filesLimit = (Integer) attrFilesLimit.getValue();
			}
		}
		if(filesLimit != null && filesLimit < 0) throw new ConsistencyErrorException(attrFilesLimit + " cannot be less than 0.");

		//Compare filesQuota with filesLimit
		if(filesQuota == null || filesQuota == 0) {
			if(filesLimit != null && filesLimit != 0) throw new WrongReferenceAttributeValueException(attribute, attrFilesLimit, resource, member, resource, null, "Try to set unlimited quota, but limit is still " + filesLimit);
		} else if(filesLimit != null && filesLimit != 0) {
			if(filesLimit < filesQuota) throw new WrongReferenceAttributeValueException(attribute, attrFilesLimit, resource, member, resource, null, attribute + " must be less than or equal to " + attrFilesLimit);
		}
	}

	@Override
	public void changedAttributeHook(PerunSessionImpl session, Member member, Resource resource, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException {
		//if this is setting of the new attribute value, check if old quota attributes are supported on the facility
		if(attribute.getValue() != null) {
			try {
				Facility facility = session.getPerunBl().getResourcesManagerBl().getFacility(session, resource);
				Attribute readyForNewQuotasAttribute = session.getPerunBl().getAttributesManagerBl().getAttribute(session, facility, A_F_readyForNewQuotas);
				//You shouldn't be allowed to set old quota attributes if facility is set for new quotas attributes (to prevent wrong setting of quotas)
				if(readyForNewQuotasAttribute.getValue() != null && readyForNewQuotasAttribute.valueAsBoolean()) {
					throw new WrongReferenceAttributeValueException(attribute, readyForNewQuotasAttribute, member, resource, facility, null, "For this facility the new quotas attributes are used! You are trying to set the old ones.");
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
		List<String> dependecies = new ArrayList<>();
		dependecies.add(A_MR_filesLimit);
		dependecies.add(A_R_defaultFilesLimit);
		dependecies.add(A_R_defaultFilesQuota);
		return dependecies;
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_MEMBER_RESOURCE_ATTR_DEF);
		attr.setFriendlyName("filesQuota");
		attr.setDisplayName("Files quota");
		attr.setType(Integer.class.getName());
		attr.setDescription("Soft quota for number of files.");
		return attr;
	}
}
