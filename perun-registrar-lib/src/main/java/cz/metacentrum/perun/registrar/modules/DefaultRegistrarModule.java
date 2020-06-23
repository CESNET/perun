package cz.metacentrum.perun.registrar.modules;

import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.AlreadyAdminException;
import cz.metacentrum.perun.core.api.exceptions.AlreadyMemberException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ExtendMembershipException;
import cz.metacentrum.perun.core.api.exceptions.ExternallyManagedException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.NotGroupMemberException;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.registrar.RegistrarManager;
import cz.metacentrum.perun.registrar.RegistrarModule;
import cz.metacentrum.perun.registrar.exceptions.CantBeApprovedException;
import cz.metacentrum.perun.registrar.exceptions.RegistrarException;
import cz.metacentrum.perun.registrar.model.Application;
import cz.metacentrum.perun.registrar.model.ApplicationForm;
import cz.metacentrum.perun.registrar.model.ApplicationFormItemData;
import cz.metacentrum.perun.registrar.model.ApplicationFormItemWithPrefilledValue;

import java.util.List;
import java.util.Map;

/**
 * Default implementation of registrar module, which does nothing, just sets RegistrarManager.
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class DefaultRegistrarModule implements RegistrarModule {

	protected RegistrarManager registrar;

	@Override
	public void setRegistrar(RegistrarManager registrar) {
		this.registrar = registrar;
	}

	@Override
	public List<ApplicationFormItemData> createApplication(PerunSession user, Application application, List<ApplicationFormItemData> data) throws PerunException {
		// return unmodified data
		return data;
	}

	@Override
	public Application approveApplication(PerunSession session, Application app) throws UserNotExistsException, PrivilegeException, AlreadyAdminException, GroupNotExistsException, VoNotExistsException, MemberNotExistsException, AlreadyMemberException, ExternallyManagedException, WrongAttributeValueException, WrongAttributeAssignmentException, AttributeNotExistsException, WrongReferenceAttributeValueException, RegistrarException, ExtendMembershipException, ExtSourceNotExistsException, NotGroupMemberException {
		// return unmodified data
		return app;
	}

	@Override
	public Application rejectApplication(PerunSession session, Application app, String reason) throws PerunException {
		// return unmodified data
		return app;
	}

	@Override
	public Application beforeApprove(PerunSession session, Application app) throws CantBeApprovedException, RegistrarException, PrivilegeException {
		// return unmodified data
		return app;
	}

	@Override
	public void canBeApproved(PerunSession session, Application app) throws PerunException {

	}

	@Override
	public void canBeSubmitted(PerunSession session, Map<String, String> params) throws PerunException {

	}

	@Override
	public void processFormItemsWithData(PerunSession session, Application.AppType appType, ApplicationForm form, List<ApplicationFormItemWithPrefilledValue> formItems) throws PerunException {

	}

}
