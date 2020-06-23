package cz.metacentrum.perun.registrar.modules;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ExtendMembershipException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.registrar.model.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Module for ELIXIR-CZ VO at CESNET instance of Perun.
 *
 * By default all VO members get nearest 1.12. (loa 2) or +3m without possibility of extension.
 *
 * 1. For new VO registrations, if loa=2, manually set 1.1.9999
 * 2. For new Group registration, use VO rules
 * 3. For VO extension, user VO rules
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class Elixircz extends DefaultRegistrarModule {

	final static Logger log = LoggerFactory.getLogger(Elixircz.class);

	@Override
	public Application approveApplication(PerunSession session, Application app) throws MemberNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException, ExtendMembershipException {

		PerunBl perun = (PerunBl) session.getPerun();
		Member member = perun.getMembersManagerBl().getMemberByUser(session, app.getVo(), app.getUser());

		if (app.getGroup() == null && Objects.equals(app.getType(), Application.AppType.INITIAL)) {

			// IF VO INITIAL override VO rules to set unlimited (only to those with LoA = 2).
			Attribute loaAttr = perun.getAttributesManagerBl().getAttribute(session, app.getUser(), AttributesManager.NS_USER_ATTR_VIRT + ":loa");
			int loa = Integer.valueOf((String) loaAttr.getValue());

			if (loa == 2) {
				Attribute attr = perun.getAttributesManagerBl().getAttribute(session, member, AttributesManager.NS_MEMBER_ATTR_DEF + ":membershipExpiration");
				attr.setValue("9999-01-01"); // set distant future as never expires
				perun.getAttributesManagerBl().setAttribute(session, member, attr);
			}

		}

		if ((app.getGroup() != null && Objects.equals(app.getType(), Application.AppType.INITIAL)) ||
				(app.getGroup() == null && Objects.equals(app.getType(), Application.AppType.EXTENSION))) {

			// GROUP INITIAL OR VO EXTENSION -> set back standard expiration date based on VO rules
			Attribute attr = perun.getAttributesManagerBl().getAttribute(session, member, AttributesManager.NS_MEMBER_ATTR_DEF + ":membershipExpiration");
			perun.getAttributesManagerBl().removeAttribute(session, member, attr);
			perun.getMembersManagerBl().extendMembership(session, member);

		}

		return app;

	}

}
