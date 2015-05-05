package cz.metacentrum.perun.engine.dao;

import java.util.List;
import java.util.Map;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotExistsException;
import cz.metacentrum.perun.engine.model.ProcessingRule;
import cz.metacentrum.perun.taskslib.model.ExecService;

/**
 * ProcessingRuleDao
 * 
 * @author Michal Karm Babacek
 * 
 */
public interface ProcessingRuleDao {

	public Map<ProcessingRule, List<ExecService>> getRules()
			throws ServiceNotExistsException, InternalErrorException,
			PrivilegeException;

}
