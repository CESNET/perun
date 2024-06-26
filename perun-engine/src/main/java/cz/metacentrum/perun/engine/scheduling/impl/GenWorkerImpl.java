package cz.metacentrum.perun.engine.scheduling.impl;

import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.engine.exceptions.TaskExecutionException;
import cz.metacentrum.perun.engine.scheduling.GenWorker;
import cz.metacentrum.perun.taskslib.model.Task;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Implementation of GenWorker, which is used for starting GEN scripts. On completion, genEndTime is set.
 * <p>
 * Workers are created by GenPlanner, done/error workers are collected by GenCollector.
 *
 * @author David Šarman
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 * @see cz.metacentrum.perun.engine.runners.GenPlanner
 * @see cz.metacentrum.perun.engine.runners.GenCollector
 */
@Component("genWorker")
@Scope(value = "prototype")
public class GenWorkerImpl extends AbstractWorker<Task> implements GenWorker {

  private static final Logger LOG = LoggerFactory.getLogger(GenWorkerImpl.class);
  private Task task;

  public GenWorkerImpl(Task task, File directory) {
    if (task == null) {
      throw new IllegalArgumentException("Task to execute can't be null.");
    }
    this.task = task;
    setDirectory(directory);
  }

  @Override
  public Task call() throws TaskExecutionException {

    getTask().setGenStartTime(LocalDateTime.now());
    Service service = getTask().getService();

    LOG.info("[{}, {}] Executing GEN worker for Task with Service ID: {} and Facility ID: {}",
        getTask().getId(),
        getTask().getRunId(),
        getTask().getServiceId(), getTask().getFacilityId());
    ProcessBuilder pb;
    if (service.getScript().equals("./" + service.getName())) {
      pb = new ProcessBuilder(service.getScript(), "-c", "-f", String.valueOf(getTask().getFacilityId()), "-r",
          String.valueOf(getTask().getRunId()));
    } else {
      // if calling some generic script, also pass name of the service to avoid gen folder collision
      pb = new ProcessBuilder(service.getScript(), "-c", "-f", String.valueOf(getTask().getFacilityId()), "-s",
          service.getName(), "-r", String.valueOf(getTask().getRunId()));
    }

    try {

      // start the script and wait for results
      super.execute(pb);

      // set gen end time
      getTask().setGenEndTime(LocalDateTime.now());

      if (getReturnCode() != 0) {

        LOG.error("[{}, {}] GEN worker failed for Task. Ret code {}, STDOUT: {}, STDERR: {}", getTask().getId(),
            getTask().getRunId(),
            getReturnCode(), getStdout(), getStderr());

        throw new TaskExecutionException(task, getReturnCode(), getStdout(), getStderr());

      } else {

        LOG.info("[{}, {}] GEN worker finished for Task. Ret code {}, STDOUT: {}, STDERR: {}", getTask().getId(),
            getTask().getRunId(),
            getReturnCode(), getStdout(), getStderr());

        return getTask();

      }

    } catch (IOException e) {
      LOG.error("[{}, {}] GEN worker failed for Task. IOException: {}.", task.getId(), task.getRunId(), e);
      throw new TaskExecutionException(task, 2, "", e.getMessage());
    } catch (InterruptedException e) {
      LOG.warn("[{}, {}] GEN worker failed for Task. Execution was interrupted {}.", task.getId(), task.getRunId(), e);
      throw new TaskExecutionException(task, 1, "", e.getMessage());
    }

  }

  @Override
  public Task getTask() {
    return task;
  }

  @Override
  public Integer getTaskId() {
    return task.getId();
  }

}
