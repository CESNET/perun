package cz.metacentrum.perun.dispatcher.unit;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.util.Assert;

import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.dispatcher.TestBase;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.dispatcher.jms.DispatcherQueue;
import cz.metacentrum.perun.dispatcher.scheduling.SchedulingPool;
import cz.metacentrum.perun.taskslib.model.ExecService;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.model.Task.TaskStatus;

/**
 * @author Michal Karm Babacek
 */
public class SchedulingPoolTest extends TestBase {

    private final static Logger log = LoggerFactory.getLogger(SchedulingPoolTest.class);

    @Autowired
    private SchedulingPool schedulingPool;
    private DispatcherQueue dispatcherQueue;
    private List<Pair<ExecService, Facility>> testPairs = new ArrayList<Pair<ExecService, Facility>>();

    @Autowired
    private Task task1;
    @Autowired
    private Task task2;
    
    @Before
    public void setup() throws InternalErrorException {
    	task1.setStatus(TaskStatus.NONE);
    	task1.setSchedule(new Date(System.currentTimeMillis()));
    	dispatcherQueue = new DispatcherQueue(1, "test-queue");
    	schedulingPool.addToPool(task1, dispatcherQueue);
    }
    
    @After
    public void cleanup() {
    	schedulingPool.clear();
    }
    
    @IfProfileValue(name="perun.test.groups", values=("unit-tests"))
    @Test 
    public void addToPoolTest() throws InternalErrorException {
    	Assert.isTrue(schedulingPool.getSize() == 1, "original size is 1");
    	schedulingPool.addToPool(task1, dispatcherQueue);
    	Assert.isTrue(schedulingPool.getSize() == 1, "new size is 1"); // pool already contains this task
    	task2.setSchedule(new Date(System.currentTimeMillis()));
    	schedulingPool.addToPool(task2, dispatcherQueue);
    	Assert.isTrue(schedulingPool.getSize() == 2, "new size is 2");
    }
    
    @IfProfileValue(name="perun.test.groups", values=("unit-tests"))
    @Test
    public void getFromPoolTest() {
    	List<Task> tasks = schedulingPool.getDoneTasks();
    	Assert.isTrue(tasks.isEmpty(), "done list is empty");
    	tasks = schedulingPool.getWaitingTasks();
    	log.debug("new size: " + tasks.size());
    	Assert.isTrue(tasks.size() == 1, "new list has size 1");
    	Assert.isTrue(task1 == tasks.get(0), "task equals");
    }
    
    @IfProfileValue(name="perun.test.groups", values=("unit-tests"))
    @Test
    public void setTaskStatusTest() {
    	schedulingPool.setTaskStatus(task1, TaskStatus.PROCESSING);
    	List<Task> tasks = schedulingPool.getWaitingTasks();
    	Assert.isTrue(tasks.isEmpty());
    	tasks = schedulingPool.getProcessingTasks();
    	Assert.isTrue(tasks.size() == 1);
    	Assert.isTrue(task1 == tasks.get(0));
    }
    
    @IfProfileValue(name="perun.test.groups", values=("unit-tests"))
    @Test
    public void getTaskByIdTest() {
    	Task task = schedulingPool.getTaskById(task1.getId());
    	Assert.isTrue(task == task1);
    }
}
