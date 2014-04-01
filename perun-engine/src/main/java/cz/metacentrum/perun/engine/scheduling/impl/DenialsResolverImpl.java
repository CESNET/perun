package cz.metacentrum.perun.engine.scheduling.impl;

import org.springframework.beans.factory.annotation.Autowired;

import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.engine.scheduling.DenialsResolver;
import cz.metacentrum.perun.engine.service.EngineManager;
import cz.metacentrum.perun.rpclib.Rpc;
import cz.metacentrum.perun.taskslib.model.ExecService;

/**
 *
 * @author Michal Karm Babacek JavaDoc coming soon...
 *
 */
@org.springframework.stereotype.Service(value = "denialsResolver")
public class DenialsResolverImpl implements DenialsResolver {

	@Autowired
	private EngineManager engineManager;

	@Override
	public boolean isExecServiceDeniedOnFacility(ExecService execService, Facility facility) throws InternalErrorException {
		try {
			return Rpc.GeneralServiceManager.isExecServiceDeniedOnFacility(engineManager.getRpcCaller(), execService, facility);
		} catch (PrivilegeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public boolean isExecServiceDeniedOnDestination(ExecService execService, int destination) throws InternalErrorException {
		try {
			if (0 == Rpc.GeneralServiceManager.isExecServiceDeniedOnDestination(engineManager.getRpcCaller(), execService, destination)) {
				return true;
			} else
				return false;
		} catch (PrivilegeException e) {
			throw new InternalErrorException(e);
		}
	}

	public EngineManager getEngineManager() {
		return engineManager;
	}

	public void setEngineManager(EngineManager engineManager) {
		this.engineManager = engineManager;
	}

}
