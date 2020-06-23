package cz.metacentrum.perun.registrar.modules;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.registrar.exceptions.RegistrarException;
import cz.metacentrum.perun.registrar.model.Application;
import cz.metacentrum.perun.registrar.model.ApplicationFormItemData;

import java.util.List;

/**
 * Module for eduTEAMS instance.
 *
 * <p>
 *     If the application item 'nickname' contains a nonempty value, the value
 *     is set into 'eduteams-nickname' user attribute. If the attribute definition
 *     does not exist, this module does nothing.
 * </p>
 *
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class EduTEAMS extends DefaultRegistrarModule {

	private static final String A_U_D_EDUTEAMS_NICKNAME =
			"urn:perun:user:attribute-def:def:login-namespace:eduteams-nickname";
	private static final String APPLICATION_NICKNAME_ITEM = "nickname";

	@Override
	public Application approveApplication(PerunSession sess, Application application) throws RegistrarException, PrivilegeException, UserNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException, AttributeNotExistsException {

		if (application.getType() == Application.AppType.INITIAL) {

			List<ApplicationFormItemData> applicationData = registrar.getApplicationDataById(sess, application.getId());

			String nickName = null;

			for (ApplicationFormItemData appItem : applicationData) {
				if (APPLICATION_NICKNAME_ITEM.equals(appItem.getShortname())) {
					nickName = appItem.getValue();
				}
			}

			if (nickName != null && !nickName.trim().isEmpty()) {
				User user = application.getUser();

				Attribute loginAttribute;
				try {
					loginAttribute =
							((PerunBl)sess.getPerun()).getAttributesManagerBl().getAttribute(sess, user, A_U_D_EDUTEAMS_NICKNAME);
				} catch (AttributeNotExistsException e) {
					// do not set the login if the attribute does not exist
					return application;
				}

				loginAttribute.setValue(nickName);

				((PerunBl)sess.getPerun()).getAttributesManagerBl().setAttribute(sess, user, loginAttribute);
			}
		}

		return application;
	}

}
