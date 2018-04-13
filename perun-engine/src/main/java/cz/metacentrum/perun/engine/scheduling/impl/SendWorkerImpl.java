package cz.metacentrum.perun.engine.scheduling.impl;

import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.engine.exceptions.TaskExecutionException;
import cz.metacentrum.perun.engine.scheduling.SendWorker;
import cz.metacentrum.perun.taskslib.model.SendTask;
import cz.metacentrum.perun.taskslib.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import static cz.metacentrum.perun.taskslib.model.SendTask.SendTaskStatus.ERROR;
import static cz.metacentrum.perun.taskslib.model.SendTask.SendTaskStatus.SENT;

/**
 * Implementation of SendWorker, which is used for starting SEND scripts.
 * On completion, SendTask endTime and status is set to either SEND or ERROR.
 * (beware, its SendTask.Status and not Task.Status).
 *
 * Workers are created by SendPlanner, done/error workers are collected by SendCollector.
 *
 * @see cz.metacentrum.perun.engine.runners.SendPlanner
 * @see cz.metacentrum.perun.engine.runners.SendCollector
 *
 * @author David Šarman
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class SendWorkerImpl extends AbstractWorker<SendTask> implements SendWorker {

	private final static Logger log = LoggerFactory.getLogger(SendWorkerImpl.class);

	private SendTask sendTask;

	public SendWorkerImpl(SendTask sendTask, File directory) {
		if (sendTask == null) throw new IllegalArgumentException("SendTask to execute can't be null.");
		this.sendTask = sendTask;
		setDirectory(directory);
	}

	@Override
	public SendTask call() throws TaskExecutionException {

		Task task = sendTask.getTask();
		Service service = task.getService();

		// we never actually run DUMMY destinations !!
		if (sendTask.getDestination().getPropagationType().equals(Destination.PROPAGATIONTYPE_DUMMY)) {

			log.info("[{}] Executing SEND worker skipped for dummy Destination: {}. Marked as SENT.",
					sendTask.getTask().getId(), sendTask.getDestination().getDestination());

			// set results
			sendTask.setStatus(SENT);
			sendTask.setStdout("");
			sendTask.setStderr("");
			sendTask.setReturnCode(0);
			sendTask.setEndTime(new Date(System.currentTimeMillis()));

			return sendTask;

		}

		log.info("[{}] Executing SEND worker for Task with Service ID: {} and Facility ID: {} and Destination: {}",
				sendTask.getTask().getId(), sendTask.getTask().getServiceId(), sendTask.getTask().getFacilityId(),
				sendTask.getDestination().getDestination());

		ProcessBuilder pb = new ProcessBuilder(
				service.getScript(),
				task.getFacility().getName(),
				sendTask.getDestination().getDestination(),
				sendTask.getDestination().getType()
		);

		try {

			// start the script and wait for results
			super.execute(pb);

			// set results
			sendTask.setStdout(super.getStdout());
			sendTask.setStderr(super.getStderr());
			sendTask.setReturnCode(super.getReturnCode());
			sendTask.setEndTime(new Date(System.currentTimeMillis()));

			if (getReturnCode() != 0) {

				log.error("[{}] SEND worker failed for Task. Ret code {}, STDOUT: {}, STDERR: {}",
						task.getId(), getReturnCode(), getStdout(), getStderr());

				sendTask.setStatus(ERROR);
				throw new TaskExecutionException(task, sendTask.getDestination(), getReturnCode(), getStdout(), getStderr());

			} else {

				log.info("[{}] SEND worker finished for Task. Ret code {}, STDOUT: {}, STDERR: {}",
						sendTask.getTask().getId(), getReturnCode(), getStdout(), getStderr());

				sendTask.setStatus(SENT);
				return sendTask;

			}

		} catch (IOException e) {
			log.error("[{}] SEND worker failed for Task. IOException: {}.",  task.getId(), e);
			sendTask.setStatus(ERROR);
			sendTask.setEndTime(new Date(System.currentTimeMillis()));
			throw new TaskExecutionException(task, sendTask.getDestination(), 2, "", e.getMessage());
		} catch (InterruptedException e) {
			log.warn("[{}] SEND worker failed for Task. Execution was interrupted {}.", task.getId(), e);
			sendTask.setStatus(ERROR);
			sendTask.setEndTime(new Date(System.currentTimeMillis()));
			throw new TaskExecutionException(task, sendTask.getDestination(), 1, "", e.getMessage());
		}

	}

	public SendTask getSendTask() {
		return sendTask;
	}

}
