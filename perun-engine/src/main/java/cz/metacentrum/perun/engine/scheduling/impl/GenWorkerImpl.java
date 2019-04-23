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
import java.time.LocalDateTime;

/**
 * Implementation of GenWorker, which is used for starting GEN scripts.
 * On completion, genEndTime is set.
 *
 * Workers are created by GenPlanner, done/error workers are collected by GenCollector.
 *
 * @see cz.metacentrum.perun.engine.runners.GenPlanner
 * @see cz.metacentrum.perun.engine.runners.GenCollector
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

		getTask().setGenStartTime(LocalDateTime.now());
		Service service = getTask().getService();

		log.info("[{}] Executing GEN worker for Task with Service ID: {} and Facility ID: {}.",
				getTask().getId(), getTask().getServiceId(), getTask().getFacilityId());

		ProcessBuilder pb = new ProcessBuilder(service.getScript(), "-f", String.valueOf(getTask().getFacilityId()));

		try {

			// start the script and wait for results
			super.execute(pb);

			// set gen end time
			getTask().setGenEndTime(LocalDateTime.now());

			if (getReturnCode() != 0) {

				log.error("[{}] GEN worker failed for Task. Ret code {}, STDOUT: {}, STDERR: {}",
						getTask().getId(), getReturnCode(), getStdout(), getStderr());

				throw new TaskExecutionException(task, getReturnCode(), getStdout(), getStderr());

			} else {

				log.info("[{}] GEN worker finished for Task. Ret code {}, STDOUT: {}, STDERR: {}",
						getTask().getId(), getReturnCode(), getStdout(), getStderr());

				return getTask();

			}

		} catch (IOException e) {
			log.error("[{}] GEN worker failed for Task. IOException: {}.",  task.getId(), e);
			throw new TaskExecutionException(task, e);
		} catch (InterruptedException e) {
			log.warn("[{}] GEN worker failed for Task. Execution was interrupted {}.", task.getId(), e);
			throw new TaskExecutionException(task, e);
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
