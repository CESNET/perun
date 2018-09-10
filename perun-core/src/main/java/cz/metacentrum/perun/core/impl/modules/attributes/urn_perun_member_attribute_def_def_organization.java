package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.MemberAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.MemberAttributesModuleImplApi;

/**
 * @author Simona Kruppova
 */
public class urn_perun_member_attribute_def_def_organization extends MemberAttributesModuleAbstract implements MemberAttributesModuleImplApi {

	private static final String A_U_organization = AttributesManager.NS_USER_ATTR_DEF + ":organization";

	@Override
	public void changedAttributeHook(PerunSessionImpl session, Member member, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException {
		User user = session.getPerunBl().getUsersManagerBl().getUserByMember(session, member);

		if(attribute.getValue() != null) {
			Attribute userOrganization = null;
			try {
				userOrganization = session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, A_U_organization);
				if(userOrganization.getValue() == null) {
					userOrganization.setValue(attribute.getValue());
					session.getPerunBl().getAttributesManagerBl().setAttribute(session, user, userOrganization);
				}
			} catch (WrongAttributeAssignmentException ex) {
				throw new InternalErrorException(ex);
			} catch (AttributeNotExistsException ex) {
				throw new ConsistencyErrorException(ex);
			} catch (WrongAttributeValueException ex) {
				throw new WrongReferenceAttributeValueException(attribute, userOrganization, "Mismatch in checking of member organization and user organization (different checking rules)", ex);
			}
		}
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_MEMBER_ATTR_DEF);
		attr.setFriendlyName("organization");
		attr.setDisplayName("Organization (for VO)");
		attr.setType(String.class.getName());
		attr.setDescription("Organization, from which user comes from.");
		return attr;
	}
}
