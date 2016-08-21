package cz.metacentrum.perun.engine.scheduling.impl;

import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.engine.exceptions.TaskExecutionException;
import cz.metacentrum.perun.engine.scheduling.GenWorker;
import cz.metacentrum.perun.taskslib.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import static cz.metacentrum.perun.taskslib.model.Task.TaskStatus.GENERATED;
import static cz.metacentrum.perun.taskslib.model.Task.TaskStatus.GENERROR;

/**
 * Implementation of GenWorker, which is used for starting GEN scripts.
 *
 * @author David Šarman
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
@Component("genWorker")
@Scope(value = "prototype")
public class GenWorkerImpl extends AbstractWorker<Task> implements GenWorker {

	private final static Logger log = LoggerFactory.getLogger(GenWorkerImpl.class);
	private Task task;

	public GenWorkerImpl(Task task, File directory) {
		if (task == null) throw new IllegalArgumentException("Task to execute can't be null.");
		this.task = task;
		setDirectory(directory);
	}

	@Override
	public Task call() throws TaskExecutionException {

		getTask().setGenStartTime(new Date(System.currentTimeMillis()));
		Service service = getTask().getService();

		log.info("[{}] Executing GEN worker for Task with Service ID: {} and Facility ID: {}.",
				new Object[]{getTask().getId(), getTask().getServiceId(), getTask().getFacilityId()});

		ProcessBuilder pb = new ProcessBuilder(service.getScript(), "-f", String.valueOf(getTask().getFacilityId()));

		try {

			// start the script and wait for results
			super.execute(pb);

			// set gen end time
			getTask().setGenEndTime(new Date(System.currentTimeMillis()));

			if (getReturnCode() != 0) {

				log.error("[{}] GEN worker failed for Task. Ret code {}, STDOUT: {}, STDERR: {}",
						new Object[]{getTask().getId(), getReturnCode(), getStdout(), getStderr()});

				getTask().setStatus(GENERROR);
				throw new TaskExecutionException(task.getId(), getReturnCode(), getStdout(), getStderr());

			} else {

				log.info("[{}] GEN worker finished for Task. Ret code {}, STDOUT: {}, STDERR: {}",
						new Object[]{getTask().getId(), getReturnCode(), getStdout(), getStderr()});

				getTask().setStatus(GENERATED);
				return getTask();

			}

		} catch (IOException e) {
			log.error("[{}] GEN worker failed for Task. IOException: {}.",  task.getId(), e);
			getTask().setStatus(GENERROR);
			throw new TaskExecutionException(task.getId(), e);
		} catch (InterruptedException e) {
			log.warn("[{}] GEN worker failed for Task. Execution was interrupted {}.", task.getId(), e);
			task.setStatus(GENERROR);
			throw new TaskExecutionException(task.getId(), e);
		}

	}

	@Override
	public Integer getTaskId() {
		return task.getId();
	}

	@Override
	public Task getTask() {
		return task;
	}

}
