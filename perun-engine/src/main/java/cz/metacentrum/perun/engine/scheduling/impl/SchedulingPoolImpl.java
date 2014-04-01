package cz.metacentrum.perun.engine.scheduling.impl;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.core.task.TaskExecutor;

import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.engine.model.Pair;
import cz.metacentrum.perun.engine.scheduling.SchedulingPool;
import cz.metacentrum.perun.taskslib.model.ExecService;

@org.springframework.stereotype.Service(value = "schedulingPool")
// Spring 3.0 default...
@Scope(value = "singleton")
public class SchedulingPoolImpl implements SchedulingPool {

	private final static Logger log = LoggerFactory.getLogger(SchedulingPoolImpl.class);
	private Set<Pair<ExecService, Facility>> pool = Collections.newSetFromMap(new ConcurrentHashMap<Pair<ExecService, Facility>, Boolean>());
	private BufferedWriter out = null;
	private FileWriter fstream = null;
	@Autowired
	private TaskExecutor taskExecutorSchedulingPoolSerializer;
	private boolean writerInitialized = false;

	@Override
	public int addToPool(Pair<ExecService, Facility> pair) {
		if (!writerInitialized) {
			initializeWriter();
			writerInitialized = true;
		}
		pool.add(pair);
		serialize(pair);
		return pool.size();
	}

	@Override
	public List<Pair<ExecService, Facility>> emptyPool() {
		List<Pair<ExecService, Facility>> toBeReturned = new ArrayList<Pair<ExecService, Facility>>(pool);
		log.debug(toBeReturned.size() + " pairs to be returned");
		pool.clear();
		close();
		initializeWriter();
		return toBeReturned;
	}

	@Override
	public int getSize() {
		return pool.size();
	}

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

	@PreDestroy
	@Override
	public void close() {
		log.debug("Closing file writer...");
		try {
			if (out != null) {
				out.flush();
				out.close();
			}
		} catch (IOException e) {
			log.error(e.toString(), e);
		}
	}

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

	public TaskExecutor getTaskExecutorSchedulingPoolSerializer() {
		return taskExecutorSchedulingPoolSerializer;
	}

	public void setTaskExecutorSchedulingPoolSerializer(TaskExecutor taskExecutorSchedulingPoolSerializer) {
		this.taskExecutorSchedulingPoolSerializer = taskExecutorSchedulingPoolSerializer;
	}

}
