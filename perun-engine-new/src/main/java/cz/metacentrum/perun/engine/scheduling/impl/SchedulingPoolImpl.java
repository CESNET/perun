package cz.metacentrum.perun.engine.scheduling.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;

import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.engine.model.Pair;
import cz.metacentrum.perun.engine.scheduling.SchedulingPool;
import cz.metacentrum.perun.engine.scheduling.TaskResultListener;
import cz.metacentrum.perun.taskslib.model.ExecService;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.model.Task.TaskStatus;
import cz.metacentrum.perun.taskslib.model.TaskResult;
import cz.metacentrum.perun.taskslib.service.TaskManager;

@org.springframework.stereotype.Service(value = "schedulingPool")
// Spring 3.0 default...
@Scope(value = "singleton")
public class SchedulingPoolImpl implements SchedulingPool, TaskResultListener {

    private final static Logger log = LoggerFactory.getLogger(SchedulingPoolImpl.class);

    private Map<TaskStatus, List<Task>> pool = new EnumMap<TaskStatus, List<Task>>(TaskStatus.class);
    private Map<Integer, Task> taskIdMap = new ConcurrentHashMap<Integer, Task>();

    @Autowired
    private TaskManager taskManager;
    
    /*
    private BufferedWriter out = null;
    private FileWriter fstream = null;
    @Autowired
    private TaskExecutor taskExecutorSchedulingPoolSerializer;
    private boolean writerInitialized = false;
*/
    
    public SchedulingPoolImpl() {
    	for(TaskStatus status : TaskStatus.class.getEnumConstants()) {
    		pool.put(status, new ArrayList<Task>());
    	}
    }
    
    @Override
	public int addToPool(Task task) {
    	synchronized(pool) {
    		if(taskIdMap.containsKey(task.getId())) {
    			log.warn("Task already is in the pool " + task.toString());
    			return this.getSize();
    		}
    		taskIdMap.put(task.getId(), task);
    		TaskStatus status = task.getStatus();
    		if(status == null) {
    			task.setStatus(TaskStatus.NONE);
    		}
    		if(!pool.get(task.getStatus()).contains(task.getId())) {
    			pool.get(task.getStatus()).add(task);
    		}
    	}
    	// XXX should this be synchronized too?
		task.setSchedule(new Date(System.currentTimeMillis()));
    	try {
			taskManager.insertTask(task, 0);
			log.debug("adding task " + task.toString() + " to database");
		} catch (InternalErrorException e) {
			log.error("Error storing task into database: " + e.getMessage());
		}
    	return this.getSize();
	}

	@Override
	public List<Task> getPlannedTasks() {
		return new ArrayList<Task>(pool.get(TaskStatus.PLANNED));
	}

	@Override
	public List<Task> getNewTasks() {
		return new ArrayList<Task>(pool.get(TaskStatus.NONE));
	}

	@Override
	public List<Task> getProcessingTasks() {
		return new ArrayList<Task>(pool.get(TaskStatus.PROCESSING));
	}
	
	@Override
	public List<Task> getErrorTasks() {
		return new ArrayList<Task>(pool.get(TaskStatus.ERROR));
	}
	
	@Override
	public List<Task> getDoneTasks() {
		return new ArrayList<Task>(pool.get(TaskStatus.DONE));
	}
	
	@Override
	public void setTaskStatus(Task task, TaskStatus status) {
		TaskStatus old = task.getStatus();
		task.setStatus(status);
		// move task to the appropriate place
		if(!old.equals(status)) {
			pool.get(old).remove(task);
			pool.get(status).add(task);
		}
		taskManager.updateTask(task, 0);
	}

	@Override
    public int getSize() {
/*
		int size = 0;
    	for(TaskStatus status : TaskStatus.class.getEnumConstants()) {
    		size += pool.get(status).size();
    	}
    	return size;
*/
		return taskIdMap.size();
    }

	@Override
	public Task getTaskById(int id) {
		return taskIdMap.get(id);
	}

	@Override
	public void removeTask(Task task) {
		synchronized(pool) {
			List<Task> tasklist = pool.get(task.getStatus());
			if(tasklist != null) {
				tasklist.remove(task);
			}
			taskIdMap.remove(task.getId());
		}
		taskManager.removeTask(task.getId(), 0);
	}

	@Override
    @Deprecated
    public int addToPool(Pair<ExecService, Facility> pair) {
/*
		if (!writerInitialized) {
            initializeWriter();
            writerInitialized = true;
        }
        pool.add(pair);
        serialize(pair);
*/
		return this.getSize();
    }

    @Override
    @Deprecated
    public List<Pair<ExecService, Facility>> emptyPool() {
/*
    	List<Pair<ExecService, Facility>> toBeReturned = new ArrayList<Pair<ExecService, Facility>>(pool);
        log.debug(toBeReturned.size() + " pairs to be returned");
        pool.clear();
        close();
        initializeWriter();
        return toBeReturned;
*/
    	return null;
    }

/*    
    private void serialize(Pair<ExecService, Facility> pair) {
        taskExecutorSchedulingPoolSerializer.execute(new Serializator(pair));
    }

    private void initializeWriter() {
        try {
            //Do not append, truncate the file instead.
            //You just have to make sure the former content have been loaded...
            fstream = new FileWriter("SchedulingPool.txt", false);
        } catch (IOException e) {
            log.error(e.toString(), e);
        }
        out = new BufferedWriter(fstream);
    }
*/
    
    @PreDestroy
    @Override
    @Deprecated
    public void close() {
/*
    	log.debug("Closing file writer...");
        try {
            if (out != null) {
                out.flush();
                out.close();
            }
        } catch (IOException e) {
            log.error(e.toString(), e);
        }
*/        
    }

	@Override
	public void reloadTasks(int engineID) {
		this.clearPool();
		for(Task task : taskManager.listAllTasks(engineID)) {
    		TaskStatus status = task.getStatus();
    		if(status == null) {
    			task.setStatus(TaskStatus.NONE);
    		}
    		if(!pool.get(task.getStatus()).contains(task.getId())) {
    			pool.get(task.getStatus()).add(task);
    		}
    		// XXX should this be synchronized too?
    		taskIdMap.put(task.getId(), task);
		}
		
	}

	private void clearPool() {
		pool = new EnumMap<TaskStatus, List<Task>>(TaskStatus.class);
		taskIdMap = new ConcurrentHashMap<Integer, Task>();
    	for(TaskStatus status : TaskStatus.class.getEnumConstants()) {
    		pool.put(status, new ArrayList<Task>());
    	}
	}

	// implementation of TaskResultListener interface
	//   - meant for GEN tasks
	//   - does not take into account destinations, once the method is called, the task status is set accordingly
	@Override
	public void onTaskDestinationDone(Task task, Destination destination,
			TaskResult result) {
		this.setTaskStatus(task, TaskStatus.DONE);
	}

	@Override
	public void onTaskDestinationError(Task task, Destination destination,
			TaskResult result) {
		this.setTaskStatus(task, TaskStatus.ERROR);
	}
	
/*
    class Serializator implements Runnable {
        private Pair<ExecService, Facility> pair = null;

        public Serializator(Pair<ExecService, Facility> pair) {
            super();
            this.pair = pair;
        }

        public void run() {
            if (pair != null && pair.getLeft() != null && pair.getRight() != null) {
                try {
                    out.write(System.currentTimeMillis() + " " + pair.getLeft().getId() + " " + pair.getRight().getId() + "\n");
                    out.flush();
                } catch (IOException e) {
                    log.error(e.toString(), e);
                }
            }
        }
    }
*/
    
/*
    public TaskExecutor getTaskExecutorSchedulingPoolSerializer() {
        return taskExecutorSchedulingPoolSerializer;
    }

    public void setTaskExecutorSchedulingPoolSerializer(TaskExecutor taskExecutorSchedulingPoolSerializer) {
        this.taskExecutorSchedulingPoolSerializer = taskExecutorSchedulingPoolSerializer;
    }
*/
    
}
