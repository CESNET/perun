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
public class urn_perun_member_attribute_def_def_workplace extends MemberAttributesModuleAbstract implements MemberAttributesModuleImplApi {

	private static final String A_U_workplace = AttributesManager.NS_USER_ATTR_DEF + ":workplace";

	@Override
	public void changedAttributeHook(PerunSessionImpl session, Member member, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException {
		User user = session.getPerunBl().getUsersManagerBl().getUserByMember(session, member);

		if(attribute.getValue() != null) {
			Attribute userWorkplace = null;
			try {
				userWorkplace = session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, A_U_workplace);
				if(userWorkplace.getValue() == null) {
					userWorkplace.setValue(attribute.getValue());
					session.getPerunBl().getAttributesManagerBl().setAttribute(session, user, userWorkplace);
				}
			} catch (WrongAttributeAssignmentException ex) {
				throw new InternalErrorException(ex);
			} catch (AttributeNotExistsException ex) {
				throw new ConsistencyErrorException(ex);
			} catch (WrongAttributeValueException ex) {
				throw new WrongReferenceAttributeValueException(attribute, userWorkplace, "Mismatch in checking of member workplace and user workplace (different checking rules)", ex);
			}
		}
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_MEMBER_ATTR_DEF);
		attr.setFriendlyName("workplace");
		attr.setDisplayName("Workplace (for VO)");
		attr.setType(String.class.getName());
		attr.setDescription("Workplace at the member's organization.");
		return attr;
	}
}
