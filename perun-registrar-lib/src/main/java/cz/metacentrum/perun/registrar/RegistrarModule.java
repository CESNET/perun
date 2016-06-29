package cz.metacentrum.perun.registrar;

import java.util.List;
import java.util.Map;

import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.registrar.exceptions.CantBeApprovedException;
import cz.metacentrum.perun.registrar.model.Application;
import cz.metacentrum.perun.registrar.model.ApplicationFormItemData;

/**
 * Interface for all registrar modules. They extend core registrar functionality and are
 * called after same functions in registrar.
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public interface RegistrarModule {

	/**
	 * Sets registrar manager for usage in a module code.
	 *
	 * @param registrar
	 */
	void setRegistrar(RegistrarManager registrar);

	/**
	 * Creates a new application.
	 *
	 * <p>The method triggers approval for VOs with auto-approved applications.
	 * @param user user present in session
	 * @param application application
	 * @param data data
	 * @return stored app data
	 */
	List<ApplicationFormItemData> createApplication(PerunSession user, Application application, List<ApplicationFormItemData> data) throws PerunException;

	/**
	 * Manually approves an application. Expected to be called as a result of direct VO administrator action in the web UI.
	 *
	 * @param session who approves the application
	 * @param app application
	 */
	Application approveApplication(PerunSession session, Application app) throws PerunException;

	/**
	 * Manually rejects an application. Expected to be called as a result of direct VO administrator action in the web UI.
	 *
	 * @param session who rejects the application
	 * @param app application
	 * @param reason optional reason of rejection displayed to user
	 */
	Application rejectApplication(PerunSession session, Application app, String reason) throws PerunException;

	/**
	 * Calls custom logic before approving of application starts -> e.g do not validate passwords when this fails
	 *
	 * @param session who approves the application
	 * @param app application
	 */
	Application beforeApprove(PerunSession session, Application app) throws PerunException;

	/**
	 * Custom logic for checking method before application approval from GUI
	 *
	 * @param session who approves the application
	 * @param app application
	 */
	void canBeApproved(PerunSession session, Application app) throws PerunException;

	/**
	 * Custom logic for checking method before application submission (retrieval of registration form) from GUI
	 *
	 * @param session who approves the application
	 * @param params custom params
	 */
	void canBeSubmitted(PerunSession session, Map<String, String> params) throws PerunException;

}
