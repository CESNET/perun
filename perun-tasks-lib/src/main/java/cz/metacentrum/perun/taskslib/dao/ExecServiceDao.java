package cz.metacentrum.perun.taskslib.dao;

import java.util.List;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.taskslib.model.ExecService;

/**
 * ExecService Data Access Object
 *
 * @author Michal Karm Babacek
 */
public interface ExecServiceDao {

	/**
	 * List all execServices.
	 *
	 * @return list of all execServices
	 */
	public List<ExecService> listExecServices();

	/**
	 * List all execServices tied to a certain Service
	 *
	 * @param serviceId
	 * @return list of execServices tied to a certain service
	 */
	public List<ExecService> listExecServices(int serviceId);

	/**
	 * Count execServices
	 *
	 * @return amount of execServices stored in the DB
	 */
	public int countExecServices();

	/**
	 * Inserts an ExecService.
	 * This method persists all the ExecService attributes, however it does not save the "name" attribute for it belongs to the "Service", not to the "ExecService".
	 *
	 * @param execService execService to insert
	 * @return a new ExecService id
	 * @throws InternalErrorException
	 */
	public int insertExecService(ExecService execService) throws InternalErrorException;

	/**
	 * Update ExecService This methods updates all the ExecService attributes
	 * except the id and name. The name must be updated via Service manager for
	 * it actually belongs to the "Service" not to the "ExecService" :-)
	 *
	 * @param execService
	 */
	public void updateExecService(ExecService execService);

	/**
	 * Delete ExecService
	 * Deletes a "child" of the Service.
	 *
	 * @param execServiceId
	 */
	public void deleteExecService(int execServiceId);

	/**
	 * Deletes all the ExecServices that belongs to the given Service.
	 *
	 * @param serviceId
	 */
	public void deleteAllExecServicesByService(int serviceId);

	/**
	 * Get execService by ID
	 *
	 * @param execServiceId
	 * @return execService
	 */
	public ExecService getExecService(int execServiceId);
}
