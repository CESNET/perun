package cz.metacentrum.perun.dispatcher.processing;

import java.util.List;
import java.util.Map;
import java.util.Set;

import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotExistsException;
import cz.metacentrum.perun.dispatcher.exceptions.InvalidEventMessageException;
import cz.metacentrum.perun.taskslib.model.ExecService;

public interface EventExecServiceResolver {

	public Map<Facility, Set<ExecService>> parseEvent(String event)
			throws InvalidEventMessageException, ServiceNotExistsException,
			InternalErrorException, PrivilegeException;

}
