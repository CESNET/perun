package cz.metacentrum.perun.engine.scheduling.impl;

import org.springframework.beans.factory.annotation.Autowired;

import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.engine.scheduling.DenialsResolver;
import cz.metacentrum.perun.taskslib.dao.ExecServiceDenialDao;
import cz.metacentrum.perun.taskslib.model.ExecService;

/**
 * 
 * 
 */
@org.springframework.stereotype.Service(value = "denialsResolver")
public class DenialsResolverImpl implements DenialsResolver {

	@Autowired
	private ExecServiceDenialDao execServiceDenialDao;

	@Override
	public boolean isExecServiceDeniedOnFacility(ExecService execService,
			Facility facility)  {
		return execServiceDenialDao.isExecServiceDeniedOnFacility(
				execService.getId(), facility.getId());
	}

	@Override
	public boolean isExecServiceDeniedOnDestination(ExecService execService,
			int destination)  {
		return execServiceDenialDao.isExecServiceDeniedOnDestination(
				execService.getId(), destination);
	}

}
