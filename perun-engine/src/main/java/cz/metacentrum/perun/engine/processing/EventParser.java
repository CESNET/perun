package cz.metacentrum.perun.engine.processing;

import cz.metacentrum.perun.engine.exceptions.InvalidEventMessageException;
import cz.metacentrum.perun.taskslib.model.Task;

/**
 * Takes care of parsing the string event into a Task.
 *
 * @author Michal Karm Babacek
 */
public interface EventParser {

  Task parseEvent(String event) throws InvalidEventMessageException;

}
