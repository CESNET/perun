package cz.metacentrum.perun.engine.processing;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotExistsException;
import cz.metacentrum.perun.engine.exceptions.InvalidEventMessageException;
import cz.metacentrum.perun.taskslib.model.Task;

public interface EventParser {

    public Task parseEvent(String event) throws InvalidEventMessageException, ServiceNotExistsException, InternalErrorException, PrivilegeException;

}
