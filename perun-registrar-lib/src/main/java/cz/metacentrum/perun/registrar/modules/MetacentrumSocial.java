package cz.metacentrum.perun.registrar.modules;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.MemberGroupMismatchException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.registrar.model.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Module for "Social" group within the Metacentrum VO.
 *
 * @author Pavel Zlamal <zlamal@cesnet.cz>
 */
public class MetacentrumSocial extends DefaultRegistrarModule {

	private final static Logger log = LoggerFactory.getLogger(MetacentrumSocial.class);

	private final static String A_MEMBER_MEMBERSHIP_EXPIRATION = AttributesManager.NS_MEMBER_ATTR_DEF+":membershipExpiration";
	private final static String A_MEMBER_GROUP_MEMBERSHIP_EXPIRATION = AttributesManager.NS_MEMBER_GROUP_ATTR_DEF+":groupMembershipExpiration";

	/**
	 * Set GROUP MEMBERSHIP EXPIRATION based on the current VO MEMBERSHIP EXPIRATION
	 */
	@Override
	public Application approveApplication(PerunSession session, Application app) throws MemberNotExistsException, WrongAttributeAssignmentException, AttributeNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException {

		PerunBl perun = (PerunBl)session.getPerun();
		Vo vo = app.getVo();
		User user = app.getUser();
		Member member = perun.getMembersManagerBl().getMemberByUser(session, vo, user);
		Group group = app.getGroup();
		Attribute voExpiration = perun.getAttributesManagerBl().getAttribute(session, member, A_MEMBER_MEMBERSHIP_EXPIRATION);
		try {
			Attribute groupExpiration = perun.getAttributesManagerBl().getAttribute(session, member, group, A_MEMBER_GROUP_MEMBERSHIP_EXPIRATION);
			groupExpiration.setValue(voExpiration.getValue());
			perun.getAttributesManagerBl().setAttribute(session, member, group, groupExpiration);
			log.debug("{} expiration in Group {} aligned with the VO {} expiration: {}",
					member, group.getName(), vo.getName(), groupExpiration.valueAsString());
		} catch (MemberGroupMismatchException e) {
			log.error("Member and group should be from the same VO.", e);
			throw new ConsistencyErrorException("Member and group should be from the same VO.", e);
		}
		return app;

	}

}
