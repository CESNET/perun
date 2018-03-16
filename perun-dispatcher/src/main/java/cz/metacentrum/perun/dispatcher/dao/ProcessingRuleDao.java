package cz.metacentrum.perun.dispatcher.dao;

import java.util.List;
import java.util.Map;

import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotExistsException;
import cz.metacentrum.perun.dispatcher.model.ProcessingRule;

/**
 * DAO layer for loading ProcessingRules.
 *
 * @author Michal Karm Babacek
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public interface ProcessingRuleDao {

	/**
	 * Load map of ProcessingRule to list of affected Services.
	 *
	 * @param perunSession
	 * @return
	 * @throws ServiceNotExistsException
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 */
	public Map<ProcessingRule, List<Service>> getRules(PerunSession perunSession) throws ServiceNotExistsException, InternalErrorException, PrivilegeException;

}
