package cz.metacentrum.perun.registrar.modules;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AlreadyMemberException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.registrar.model.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * This module sorts new VO members into the groups based on their affiliations from ČVUT/UK IdPs
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class CvutFbmi extends DefaultRegistrarModule {

	private final static Logger log = LoggerFactory.getLogger(CvutFbmi.class);

	/**
	 * Add approved VO members into specific groups based on their affiliation from ČVUT or UK IdP
	 */
	@Override
	public Application approveApplication(PerunSession session, Application app) throws MemberNotExistsException, WrongAttributeAssignmentException, AttributeNotExistsException, GroupNotExistsException {

		PerunBl perun = (PerunBl)session.getPerun();
		User user = app.getUser();
		Vo vo = app.getVo();

		// For INITIAL VO APPLICATIONS
		if (Application.AppType.INITIAL.equals(app.getType()) && app.getGroup() == null) {

			Attribute a = perun.getAttributesManagerBl().getAttribute(session, user, AttributesManager.NS_USER_ATTR_VIRT + ":eduPersonScopedAffiliations");

			if (a.getValue() != null) {

				Member member = perun.getMembersManagerBl().getMemberByUser(session, vo, user);

				List<String> affiliations = a.valueAsList();

				if (affiliations.contains("employee@cvut.cz")) {
					categorizeMember(session, vo, member, "Osoby:CVUT:Zamestnanec");
				}
				if (affiliations.contains("student@cvut.cz")) {
					categorizeMember(session, vo, member, "Osoby:CVUT:Student");
				}
				if (affiliations.contains("employee@cuni.cz")) {
					categorizeMember(session, vo, member, "Osoby:UK:Zamestnanec");
				}
				if (affiliations.contains("student@cuni.cz")) {
					categorizeMember(session, vo, member, "Osoby:UK:Student");
				}

			}

		}

		return app;

	}

	private void categorizeMember(PerunSession session, Vo vo, Member member, String groupName) {

		PerunBl perun = (PerunBl)session.getPerun();
		Group group = null;
		try {
			group = perun.getGroupsManagerBl().getGroupByName(session, vo, groupName);
			perun.getGroupsManagerBl().addMember(session, group, member);
		} catch (GroupNotExistsException e) {
			log.warn("Destination group {} for ČVUT FBMI and UK members categorisation doesn't exists.", groupName, e);
		} catch (WrongReferenceAttributeValueException | WrongAttributeValueException e) {
			log.error("Member {} couldn't be added to expected group {}.", member, group, e);
		} catch (AlreadyMemberException e) {
			// IGNORE
		}

	}

}
