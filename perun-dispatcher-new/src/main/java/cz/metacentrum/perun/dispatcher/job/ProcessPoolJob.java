package cz.metacentrum.perun.dispatcher.job;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.dispatcher.scheduling.TaskScheduler;

@org.springframework.stereotype.Service(value = "processPoolJob")
public class ProcessPoolJob extends QuartzJobBean {

    private final static Logger log = LoggerFactory.getLogger(ProcessPoolJob.class);

    @Autowired
    private TaskScheduler taskScheduler;


    public TaskScheduler getTaskScheduler() {
        return taskScheduler;
    }

    public void setTaskScheduler(TaskScheduler taskScheduler) {
        this.taskScheduler = taskScheduler;
    }

	@Override
	protected void executeInternal(JobExecutionContext arg0)
			throws JobExecutionException {
        log.info("Entering ProcessPoolJob: taskScheduler.processPool().");
        try {
            taskScheduler.processPool();
        } catch (InternalErrorException e) {
            log.error(e.toString(), e);
        }
        log.info("ProcessPoolJob: taskScheduler.processPool() has completed.");
		
	}

}
