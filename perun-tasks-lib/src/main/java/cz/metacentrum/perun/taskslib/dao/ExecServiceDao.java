package cz.metacentrum.perun.taskslib.dao;

import java.util.List;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.taskslib.model.ExecService;

/**
 * ExecService Data Access Object
 * 
 * @author Michal Karm Babacek
 * @version draft
 */
public interface ExecServiceDao {

	public List<ExecService> listExecServices();
	
	/**
	 * List all execServices tied to a certain Service
	 * @param serviceId
	 * @return
	 */
	public List<ExecService> listExecServices(int serviceId);

	public int countExecServices();

	/**
	 * Insert ExecService This method persists all the ExecService attributes,
	 * however it does not save the "name" attribute for it belongs to the
	 * "Service", not to the "ExecService".
	 * 
	 * @param execService
	 * @return a new ExecService
	 * @throws InternalErrorException 
	 */
	public int insertExecService(ExecService execService) throws InternalErrorException;

	/**
	 * Update ExecService This methos updates all the ExecServie attributes
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
	 * @param execService
	 */
	public void deleteExecService(int execServiceId);

	/**
	 * Delete all the ExecServices by Service
	 * 
	 * Deletes all the ExecServices that belongs to the given Service.
	 * 
	 * @param serviceId
	 */
	public void deleteAllExecServicesByService(int serviceId);

	public ExecService getExecService(int execServiceId);
}
