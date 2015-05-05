package cz.metacentrum.perun.engine.scheduling;

import java.util.List;

import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.engine.jms.JMSQueueManager;
import cz.metacentrum.perun.engine.model.Statistics;
import cz.metacentrum.perun.taskslib.model.ExecService;

/**
 * 
 * @author Michal Karm Babacek JavaDoc coming soon...
 * 
 */
public interface PropagationMaintainer {

	void checkResults();

	Statistics getStatistics();

	void setAllGenerateDependenciesToNone(List<ExecService> dependencies,
			Facility facility);

	void setAllGenerateDependenciesToNone(List<ExecService> dependencies,
			int facilityId);

	void setJmsQueueManager(JMSQueueManager jmsQueueManager);
}
