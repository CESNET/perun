package cz.metacentrum.perun.engine.scheduling.impl;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.engine.scheduling.ExecutorEngineWorker;
import cz.metacentrum.perun.engine.scheduling.TaskResultListener;
import cz.metacentrum.perun.taskslib.dao.TaskResultDao;
import cz.metacentrum.perun.taskslib.model.ExecService;
import cz.metacentrum.perun.taskslib.model.ExecService.ExecServiceType;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.model.TaskResult;
import cz.metacentrum.perun.taskslib.model.TaskResult.TaskResultStatus;

@Component("executorEngineWorker")
@Scope(value = "prototype")
public class ExecutorEngineWorkerImpl implements ExecutorEngineWorker {

	private final static Logger log = LoggerFactory.getLogger(ExecutorEngineWorkerImpl.class);

	private TaskResultListener resultListener;
	private Task task;
	private Facility facility;
	private ExecService execService;
	private Destination destination;
	@Autowired
	private Properties propertiesBean;
	private int engineId = -1;

	// where gen scripts are located (relative to engine working directory = where you started the jar)
	// value is taken from propertiesBean (see it's setter method)
	File genDirectory;
	File sendDirectory;

	@Override
	public void run() {

		if(destination.getPropagationType().equals(Destination.PROPAGATIONTYPE_DUMMY)) {
			log.info("EXECUTING(worker:" + this.hashCode() + "): Task ID:"
					+ task.getId() + ", Facility ID:" + task.getFacilityId()
					+ ", ExecService ID:" + task.getExecServiceId()
					+ ", ExecServiceType:" + execService.getExecServiceType());


			TaskResult taskResult = new TaskResult();
			taskResult.setTaskId(task.getId());
			taskResult.setDestinationId(destination.getId());
			taskResult.setErrorMessage("");
			taskResult.setStandardMessage("");
			taskResult.setReturnCode(0);
			taskResult.setStatus(TaskResultStatus.DONE);
			taskResult.setTimestamp(new Date(System.currentTimeMillis()));
			taskResult.setService(execService.getService());
				
			resultListener.onTaskDestinationDone(task, destination,	taskResult);
			log.info("SEND task for dummy destination done. Task: " + task.getId());
			return;
		}
		
		log.info("EXECUTING(worker:" + this.hashCode() + "): Task ID:"
				+ task.getId() + ", Facility ID:" + task.getFacilityId()
				+ ", ExecService ID:" + task.getExecServiceId()
				+ ", ExecServiceType:" + execService.getExecServiceType());

		String stdout = null;
		String stderr = null;
		int returnCode = -1;
		if (execService.getExecServiceType().equals(ExecServiceType.GENERATE)) {
			ProcessBuilder pb = new ProcessBuilder(execService.getScript(), "-f", String.valueOf(task.getFacilityId()));
			if (genDirectory != null) {
				// set path relative to current working dir
				pb.directory(genDirectory);
			}

			try {
				Process process = pb.start();

				StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream());
				StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream());

				errorGobbler.start();
				outputGobbler.start();

				returnCode = process.waitFor();

				while (errorGobbler.isAlive() || outputGobbler.isAlive()) Thread.sleep(50);

				stderr = errorGobbler.getSb();
				stdout = outputGobbler.getSb();

				// There is no point in writing into TaskResults. We switch TASK
				// status at once.
				// TODO: Put some logic in here :-)

				// task.setStatus(returnCode == 0 ? TaskStatus.DONE :
				// TaskStatus.ERROR);
				if (returnCode != 0) {
					log.info("GEN task failed. Ret code " + returnCode
							+ ". STDOUT: {}  STDERR: {}. Task: " + task.getId(),
							stdout, stderr);
					resultListener.onTaskDestinationError(task, destination, null);
				} else {
					resultListener.onTaskDestinationDone(task, destination, null);
					log.info("GEN task completed. Ret code " + returnCode
							+ ". STDOUT: {}  STDERR: {}. Task: " + task.getId(),
							stdout, stderr);
				}

			} catch (IOException e) {
				log.error(e.toString(), e);
				// task.setStatus(TaskStatus.ERROR);
				resultListener.onTaskDestinationError(task, destination, null);
			} catch (Exception e) {
				log.error(e.toString(), e);
				// task.setStatus(TaskStatus.ERROR);
				resultListener.onTaskDestinationError(task, destination, null);
			} finally {
				String ret = returnCode == -1 ? "unknown" : String
						.valueOf(returnCode);
				log.debug("GEN task ended. Ret code " + ret
						+ ". STDOUT: {}  STDERR: {}. Task: " + task.getId(), stdout,
						stderr);
				// taskManager.updateTask(task, getEngineId());
			}
		} else if (execService.getExecServiceType().equals(ExecServiceType.SEND)) {

			ProcessBuilder pb = new ProcessBuilder(execService.getScript(), facility.getName(), destination.getDestination(), destination.getType());
			if (sendDirectory != null) {
				// set path relative to current working dir
				pb.directory(sendDirectory);
			}

			try {

				Process process = pb.start();

				StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream());
				StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream());

				errorGobbler.start();
				outputGobbler.start();

				returnCode = process.waitFor();

				while (errorGobbler.isAlive() || outputGobbler.isAlive()) Thread.sleep(50);

				stderr = errorGobbler.getSb();
				stdout = outputGobbler.getSb();

				TaskResult taskResult = new TaskResult();
				taskResult.setTaskId(task.getId());
				taskResult.setDestinationId(destination.getId());
				taskResult.setErrorMessage(stderr);
				taskResult.setStandardMessage(stdout);
				taskResult.setReturnCode(returnCode);
				taskResult.setStatus(returnCode == 0 ? TaskResultStatus.DONE : TaskResultStatus.ERROR);
				taskResult.setTimestamp(new Date(System.currentTimeMillis()));
				taskResult.setService(execService.getService());
				
				if (taskResult.getStatus().equals(TaskResultStatus.ERROR)) {
					resultListener.onTaskDestinationError(task, destination,
							taskResult);
					log.info("SEND task failed. Ret code " + returnCode
							+ ". STDOUT: {}  STDERR: {}. Task: " + task.getId(),
							stdout, stderr);
				} else {
					resultListener.onTaskDestinationDone(task, destination,
							taskResult);
					log.info("SEND task completed. Ret code " + returnCode
							+ ". STDOUT: {}  STDERR: {}. Task: " + task.getId(),
							stdout, stderr);
				}

			} catch (Exception e) {
				log.info("SEND task ended. Ret code " + returnCode
						+ ". STDOUT: {}  STDERR: {}. Task: " + task.getId(), stdout,
						stderr);
				log.error("ERROR with TASK ID: " + task.getId()
						+ " , Exception:" + e.toString(), e);
				// If we are unable to switch to the ERROR state,
				// PropagationMaintainer would resolve
				// the Tasks status correctly anyhow (count Destinations x count
				// TaskResults)
				TaskResult taskResult = new TaskResult();
				taskResult.setTaskId(task.getId());
				taskResult.setDestinationId(destination.getId());
				taskResult.setStatus(TaskResultStatus.ERROR);
				taskResult.setTimestamp(new Date(System.currentTimeMillis()));
				taskResult.setService(execService.getService());

				resultListener.onTaskDestinationError(task, destination, taskResult);
			} finally {
				String ret = returnCode == -1 ? "unknown" : String.valueOf(returnCode);

			}

		} else {
			throw new IllegalArgumentException("Expected ExecService type is SEND or GENERATE.");
		}
	}

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}

	public Facility getFacility() {
		return facility;
	}

	public void setFacility(Facility facility) {
		this.facility = facility;
	}

	public ExecService getExecService() {
		return execService;
	}

	public void setExecService(ExecService execService) {
		this.execService = execService;
	}

	public Destination getDestination() {
		return destination;
	}

	public void setDestination(Destination destination) {
		this.destination = destination;
	}

	public Properties getPropertiesBean() {
		return propertiesBean;
	}

	@Autowired
	public void setPropertiesBean(Properties propertiesBean) {
		this.propertiesBean = propertiesBean;
		if (propertiesBean != null) {
			// here we can be sure, that properties bean is present
			genDirectory = new File(propertiesBean.getProperty("engine.genscript.path"));
			sendDirectory = new File(propertiesBean.getProperty("engine.sendscript.path"));
		}
	}

	public int getEngineId() {
		if (engineId == -1) {
			this.engineId = Integer.parseInt(propertiesBean.getProperty("engine.unique.id"));
		}
		return engineId;
	}

	public TaskResultListener getResultListener() {
		return resultListener;
	}

	public void setResultListener(TaskResultListener resultListener) {
		this.resultListener = resultListener;
	}

	public File getGenDirectory() {
		return genDirectory;
	}

	public void setGenDirectory(File genDirectory) {
		this.genDirectory = genDirectory;
	}

	public File getSendDirectory() {
		return sendDirectory;
	}

	public void setSendDirectory(File sendDirectory) {
		this.sendDirectory = sendDirectory;
	}

}
