package cz.metacentrum.perun.dispatcher.scheduling;

import java.util.List;

import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.taskslib.model.ExecService;

/**
 * 
 * @author Michal Karm Babacek JavaDoc coming soon...
 * 
 */
public interface PropagationMaintainer {

    void checkResults();

    void setAllGenerateDependenciesToNone(List<ExecService> dependencies, Facility facility);

    void setAllGenerateDependenciesToNone(List<ExecService> dependencies, int facilityId);

	void closeTasksForEngine(int clientID);

	void onTaskComplete(int parseInt, int clientID, String status, String string);
}
