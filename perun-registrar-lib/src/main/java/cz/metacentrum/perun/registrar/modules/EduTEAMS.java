package cz.metacentrum.perun.registrar.modules;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.registrar.RegistrarManager;
import cz.metacentrum.perun.registrar.RegistrarModule;
import cz.metacentrum.perun.registrar.exceptions.RegistrarException;
import cz.metacentrum.perun.registrar.model.Application;
import cz.metacentrum.perun.registrar.model.ApplicationFormItemData;

import java.util.List;
import java.util.Map;

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
public class EduTEAMS implements RegistrarModule {

	private static final String A_U_D_EDUTEAMS_NICKNAME =
			"urn:perun:user:attribute-def:def:login-namespace:eduteams-nickname";
	private static final String APPLICATION_NICKNAME_ITEM = "nickname";

	private RegistrarManager registrar;

	@Override
	public void setRegistrar(RegistrarManager registrar) {
		this.registrar = registrar;
	}

	@Override
	public List<ApplicationFormItemData> createApplication(PerunSession session, Application application,
	                                                       List<ApplicationFormItemData> data) throws PerunException {
		return data;
	}

	@Override
	public Application approveApplication(PerunSession sess, Application application) throws RegistrarException, PrivilegeException, InternalErrorException, UserNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException, AttributeNotExistsException {

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
							sess.getPerun().getAttributesManager().getAttribute(sess, user, A_U_D_EDUTEAMS_NICKNAME);
				} catch (AttributeNotExistsException e) {
					// do not set the login if the attribute does not exist
					return application;
				}

				loginAttribute.setValue(nickName);

				sess.getPerun().getAttributesManager().setAttribute(sess, user, loginAttribute);
			}
		}

		return application;
	}

	@Override
	public Application rejectApplication(PerunSession session, Application app, String reason) throws PerunException {
		return app;
	}

	@Override
	public Application beforeApprove(PerunSession session, Application app) {
		return app;
	}

	@Override
	public void canBeApproved(PerunSession session, Application app) throws PerunException {
		// do nothing
	}

	@Override
	public void canBeSubmitted(PerunSession session, Map<String, String> params) throws PerunException {
		// do nothing
	}
}
