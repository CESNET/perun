package cz.metacentrum.perun.engine.unit;


import java.util.ArrayList;
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
import cz.metacentrum.perun.engine.TestBase;
import cz.metacentrum.perun.engine.model.Pair;
import cz.metacentrum.perun.engine.scheduling.SchedulingPool;
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
    private List<Pair<ExecService, Facility>> testPairs = new ArrayList<Pair<ExecService, Facility>>();

    @Autowired
    private Task task1;
    @Autowired
    private Task task2;
    
    @Before
    public void setup() {
    	task1.setStatus(TaskStatus.NONE);
    	task2.setStatus(TaskStatus.NONE);
    	schedulingPool.addToPool(task1);
    }
    
    @After
    public void cleanup() {
    	schedulingPool.removeTask(task1);
    	schedulingPool.removeTask(task2);
    }
    
    @IfProfileValue(name="perun.test.groups", values=("unit-tests"))
    @Test 
    public void addToPoolTest() {
    	Assert.isTrue(schedulingPool.getSize() == 1, "original size is 1");
    	schedulingPool.addToPool(task1);
    	Assert.isTrue(schedulingPool.getSize() == 1, "new size is 1"); // pool already contains this task
    	schedulingPool.addToPool(task2);
    	Assert.isTrue(schedulingPool.getSize() == 2, "new size is 2");
    }
    
    @IfProfileValue(name="perun.test.groups", values=("unit-tests"))
    @Test
    public void getFromPoolTest() {
    	List<Task> tasks = schedulingPool.getDoneTasks();
    	Assert.isTrue(tasks.isEmpty(), "done list is empty");
    	tasks = schedulingPool.getNewTasks();
    	log.debug("new size: " + tasks.size());
    	Assert.isTrue(tasks.size() == 1, "new list has size 1");
    	Assert.isTrue(task1 == tasks.get(0), "task equals");
    }
    
    @IfProfileValue(name="perun.test.groups", values=("unit-tests"))
    @Test
    public void setTaskStatusTest() {
    	schedulingPool.setTaskStatus(task1, TaskStatus.PROCESSING);
    	List<Task> tasks = schedulingPool.getNewTasks();
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
