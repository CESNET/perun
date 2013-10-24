package cz.metacentrum.perun.engine.processing;

import java.util.List;

import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotExistsException;
import cz.metacentrum.perun.engine.exceptions.InvalidEventMessageException;
import cz.metacentrum.perun.engine.model.Pair;
import cz.metacentrum.perun.taskslib.model.ExecService;

public interface EventExecServiceResolver {

    public List<Pair<List<ExecService>, Facility>> parseEvent(String event) throws InvalidEventMessageException, ServiceNotExistsException, InternalErrorException, PrivilegeException;

    public void refreshProcessingRules() throws ServiceNotExistsException, InternalErrorException, PrivilegeException;
}
