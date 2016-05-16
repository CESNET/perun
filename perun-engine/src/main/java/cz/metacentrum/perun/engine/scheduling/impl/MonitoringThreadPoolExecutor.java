package cz.metacentrum.perun.engine.scheduling.impl;

import cz.metacentrum.perun.engine.scheduling.ExecutorEngineWorker;

import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * This class is used by MonitoringTaskExecutorImpl for organizing and executing Workers (instances of ExecutorEngineWorkerImpl).
 * It includes suitable methods to get information about its internal processes in any time.
 * <p/>
 * MonitoringThreadPoolExecutor extends class ThreadPoolExecutor from package java.util.concurrent
 * so for more information see JavaDoc of java.util.concurrent.ThreadPoolExecutor
 *
 * @author Jana Cechackova (396049@mail.muni.cz)
 * @version 3
 */
public class MonitoringThreadPoolExecutor extends ThreadPoolExecutor {

	/**
	 * @param runningWorkers    workers with state RUNNING
	 * @param completedWorkes   workers with state FINISHED
	 * @param changeLock        locks all possible changes when collecting information about Monitoring Thread Pool Executor
	 */
	private final CopyOnWriteArraySet<ExecutorEngineWorker> runningWorkers;
	private final CopyOnWriteArraySet<ExecutorEngineWorker> completedWorkers;
	private final WorkerLock changeLock = new WorkerLock();

	public MonitoringThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
		runningWorkers = new CopyOnWriteArraySet<>();
		completedWorkers = new CopyOnWriteArraySet<>();

	}

	public MonitoringThreadPoolExecutor(int corePoolSize, int maxPoolSize, int keepAliveSeconds, TimeUnit timeUnit, BlockingQueue<Runnable> queue, ThreadFactory threadFactory, RejectedExecutionHandler rejectedExecutionHandler) {
		super(corePoolSize, maxPoolSize, keepAliveSeconds, timeUnit, queue, threadFactory, rejectedExecutionHandler);
		runningWorkers = new CopyOnWriteArraySet<>();
		completedWorkers = new CopyOnWriteArraySet<>();
	}

	/**
	 * Method is called every time Worker is trying to access Monitoring Thread Pool Executor.
	 * Sets Worker's status to WAITING and calls method execute from its superclass
	 *
	 * @param r Worker, that is to be executed
	 */
	@Override
	public void execute(Runnable r) {
		try {
			changeLock.writingLock().lock();
			ExecutorEngineWorker worker = (ExecutorEngineWorker) r;
			worker.setWorkerStatus(WorkerStatus.WAITING);
		} finally {
			changeLock.writingLock().unlock();
		}
		super.execute(r);
	}

	/**
	 * Method is called right before every Worker is executed.
	 * Sets Worker's status to RUNNING and add Worker to collection runningWorkers
	 *
	 * @param t thread,that will run Worker r
	 * @param r Worker, that is to be executed
	 */
	@Override
	public void beforeExecute(Thread t, Runnable r) {
		try {
			changeLock.writingLock().lock(); // because we want to avoid mistake, that worker is not in any collection
			ExecutorEngineWorker worker = (ExecutorEngineWorker) r;
			worker.setWorkerStatus(WorkerStatus.RUNNING);
			runningWorkers.add(worker);
		} finally {
			changeLock.writingLock().unlock();
		}
		super.beforeExecute(t, r);
	}

	/**
	 * Method is called right after every Worker is executed.
	 * Sets Worker's status to FINISHED and add Worker to collection completedWorkers
	 *
	 * @param r Worker, that is to be executed
	 * @param t exception, if execution caused error, null if execution completed normally
	 */
	@Override
	public void afterExecute(Runnable r, Throwable t) {
		try {
			changeLock.writingLock().lock();
			ExecutorEngineWorker worker = (ExecutorEngineWorker) r;
			runningWorkers.remove((ExecutorEngineWorkerImpl) r);
			worker.setWorkerStatus(WorkerStatus.FINISHED);
			completedWorkers.add(worker);
		} finally {
			changeLock.writingLock().unlock();
		}
		super.afterExecute(r, t);
	}

	/**
	 * Method, that is called when someone wants to get information of all internal processes.
	 * Locks all possible changes in Monitoring Thread Pool Executor (new Workers, status changes, etc.)
	 * during collecting information.
	 *
	 * @return message      String that contains formatted info about Monitoring Thread Pool Executor
	 */
	@Override
	public String toString() {
		Date date = new Date();
		String message = "THREAD POOL EXECUTOR INFO\n actual time: " + date.toString() + "\n\n";
		try {
			changeLock.readingLock().lock();
			if (!runningWorkers.isEmpty()) {
				message += runningToString() + queueToString() + finishedToString();
				message += "ACTIVE COUNT: " + runningWorkers.size() + " COMPLETED TASK COUNT: " + completedWorkers.size() + " TOTAL TASK COUNT: " + super.getTaskCount();
				message += "\n--------------------------------------------------------------\n";
			} else {
				message += "ALL WORKERS ARE ALREADY FINISHED. SUMMARY:\n" + finishedToString();
			}
		} finally {
			changeLock.readingLock().unlock();
		}
		return message;
	}

	/**
	 * Method, that is called when someone wants to get information about finished Workers.
	 *
	 * @return message     String that contains formatted info about completed Workers
	 */
	public String finishedToString() {
		String message = "FINISHED WORKERS:\n";
		synchronized (completedWorkers) {
			for (ExecutorEngineWorker worker : completedWorkers) {
				message += worker.toString() + " TIME: " + (worker.getTask().getEndTime().getTime() - worker.getTask().getStartTime().getTime()) + "ms\n";
			}
			if (completedWorkers.isEmpty()) return "\nThere are no finished workers.\n";
		}
		return message;
	}

	/**
	 * Method, that is called when someone wants to get information about Workers waiting in the queue.
	 *
	 * @return message     String that contains formatted info about waiting Workers
	 */
	public String queueToString() {
		String message = "WORKERS IN QUEUE:\n";
		synchronized (super.getQueue()) {
			for (Object worker : super.getQueue()) {
				message += worker.toString() + "\n";
			}
			if (super.getQueue().isEmpty()) return "\nThere are no workers in the queue.\n";
		}
		return message;
	}

	/**
	 * Method, that is called when someone wants to get information about actually running Workers.
	 *
	 * @return message     String that contains formatted info about running Workers
	 */
	public String runningToString() {
		String message = "RUNNING WORKERS:\n";
		synchronized (runningWorkers) {
			for (Object worker : runningWorkers) {
				message += worker.toString() + "\n";
			}
			if (runningWorkers.isEmpty()) return "\nThere are no running workers.\n";
		}
		return message;
	}

	public CopyOnWriteArraySet<ExecutorEngineWorker> getRunningWorkers() {
		return runningWorkers;
	}

	public CopyOnWriteArraySet<ExecutorEngineWorker> getCompletedWorkers() {
		return completedWorkers;
	}
}
