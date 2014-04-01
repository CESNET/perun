package cz.metacentrum.perun.registrar;

import java.util.List;

import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
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

}
