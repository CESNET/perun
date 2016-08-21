package cz.metacentrum.perun.engine.scheduling.impl;

import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.core.api.Pair;
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

		ProcessBuilder pb = new ProcessBuilder(
				service.getScript(),
				task.getFacility().getName(),
				sendTask.getDestination().getDestination(),
				sendTask.getDestination().getType()
		);

		try {

			if(sendTask.getDestination().getPropagationType().equals(Destination.PROPAGATIONTYPE_DUMMY)) {
				// set results
				sendTask.setStdout("");
				sendTask.setStderr("");
				sendTask.setReturnCode(0);
				sendTask.setEndTime(new Date(System.currentTimeMillis()));
				
				log.info("[{}] SEND worker skipped for dummy destination for Task.",
						new Object[]{sendTask.getTask().getId()});

				sendTask.setStatus(SENT);
				return sendTask;
			} 

			// start the script and wait for results
			super.execute(pb);

			// set results
			sendTask.setStdout(super.getStdout());
			sendTask.setStderr(super.getStderr());
			sendTask.setReturnCode(super.getReturnCode());
			sendTask.setEndTime(new Date(System.currentTimeMillis()));

			if (getReturnCode() != 0) {

				log.error("[{}] SEND worker failed for Task. Ret code {}, STDOUT: {}, STDERR: {}",
						new Object[]{task.getId(), getReturnCode(), getStdout(), getStderr()});

				sendTask.setStatus(ERROR);
				throw new TaskExecutionException(sendTask.getId(), getReturnCode(),
						getStdout(), getStderr());

			} else {

				log.info("[{}] SEND worker finished for Task. Ret code {}, STDOUT: {}, STDERR: {}",
						new Object[]{sendTask.getTask().getId(), getReturnCode(), getStdout(), getStderr()});

				sendTask.setStatus(SENT);
				return sendTask;

			}

		} catch (IOException e) {
			log.error("[{}] SEND worker failed for Task. IOException: {}.",  task.getId(), e);
			sendTask.setStatus(ERROR);
			throw new TaskExecutionException(new Pair<>(task.getId(), sendTask.getDestination()), 2, "", e.getMessage());
		} catch (InterruptedException e) {
			log.warn("[{}] SEND worker failed for Task. Execution was interrupted {}.", task.getId(), e);
			sendTask.setStatus(ERROR);
			throw new TaskExecutionException(new Pair<>(task.getId(), sendTask.getDestination()), 1, "", e.getMessage());
		}

	}

	public SendTask getSendTask() {
		return sendTask;
	}

}
