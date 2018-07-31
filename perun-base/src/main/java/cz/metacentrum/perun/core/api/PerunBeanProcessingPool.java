package cz.metacentrum.perun.core.api;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Processing pool for processing PerunBeans as jobs.
 *
 * This implementation is thread-safe.
 *
 * You can put new waiting jobs in it and take waiting jobs to set them as running,
 * and then remove them after work with them is finished.
 * Jobs in processing pool are unique and can't be added if they are already
 * in list of waiting jobs or in set of running jobs.
 * New waiting job can skip order and be putted as first in the list of waiting jobs.
 *
 * Note: this class doesn't run any task scheduler, it just provides structures and functionality to manipulate
 * with these structures used for processing job from the pool of waiting jobs.
 *
 * @author Michal Stava <stavamichal@gmail.com>
 */
public class PerunBeanProcessingPool<T extends PerunBean> {

	//List of waiting jobs (they should be process as soon as possible), there is FIFO order
	private final LinkedList<T> waitingJobs = new LinkedList<>();
	//Set of already running jobs (we need to track them until they are finished)
	private final HashSet<T> runningJobs = new HashSet<>();
	//Semaphore which takes care about emptiness of list of waiting jobs (threads will wait for another job)
	//Counter in this semaphore counts number of waiting jobs in the pool (0 means no jobs are waiting to be processed)
	private final Semaphore notEmptyPoolSemaphore = new Semaphore(0, true);
	//Access lock to create concurrent access by any operation to any structure of this processing pool
	private final Lock jobsAccessLock = new ReentrantLock(true);

	/**
	 * Put new unique job to the list of waiting jobs.
	 *
	 * If job is already running, return false because there would be no change.
	 * If job is already waiting, check if asFirst is true. If yes, move it (remove and add) to
	 * the first place and return true. If not, return false.
	 * If there is no such job, add it and return true.
	 *
	 * Uniqueness of the job in pool is mandatory and it is no difference between waiting and running status of job in
	 * this case.
	 *
	 * @param job perunBean object which defines job
	 * @param asFirst true if job will skip order and will be placed to the list as first (LIFO)
	 *
	 * @return true if unique job was added or job was added to the beginning of the queue (asFirst is true), false if job exists and
	 * was not added to the beginning of the queue (asFirst is false) or it is already running
	 *
	 * @throws InternalErrorException if job in parameter is null
	 */
	public boolean putJobIfAbsent(T job, boolean asFirst) throws InternalErrorException {
		if(job == null) throw new InternalErrorException("Can't put null job to list of waiting jobs.");

		try {
			//get lock for any operation with structures of jobs
			jobsAccessLock.lock();

			//put only jobs which are not running yet
			if (runningJobs.contains(job)) return false;

			//If the job is already in waiting queue, we don't have to plan it again unless we want to move it
			// to the begging of the queue which is indicated by asFirst variable.
			if (waitingJobs.contains(job)) {
				if (!asFirst) {
					return false;
				} else {
					//remove it from the waiting queue before putting it back as first waiting job
					waitingJobs.remove(job);
				}
			}

			//add it as first or at last defined by parameter of method
			if (asFirst) {
				waitingJobs.addFirst(job);
			} else {
				waitingJobs.addLast(job);
			}

			//increase number of waiting jobs by one
			notEmptyPoolSemaphore.release();

		} finally {
			//unlock access lock
			jobsAccessLock.unlock();
		}

		//return true (new waiting job in list of waiting jobs)
		return true;
	}

	/**
	 * Take job from list of waiting jobs and add it to the list of running jobs.
	 *
	 * @return first waiting job from the list of waiting jobs
	 *
	 * @throws InterruptedException if acquiring of permit on semaphore was interrupted
	 */
	public T takeJob() throws InterruptedException {
		//I can take only if there is not empty list of waiting jobs
		notEmptyPoolSemaphore.acquire();

		T job;
		try {
			//If there is at least one job in the list of waiting jobs, get access to manipulate with it
			jobsAccessLock.lock();

			//We should always get not null job, because semaphore pool was not empty
			job = waitingJobs.pollFirst();

			runningJobs.add(job);
		} finally {
			//unlock access lock
			jobsAccessLock.unlock();

		}

		return job;
	}

	/**
	 * Remove jobs from set of running jobs. It means that we are done with this job.
	 *
	 * @param job perunBean object which defines job
	 *
	 * @return true if removing was successful, false if not (structure not contained the job)
	 */
	public boolean removeJob(T job) {
		//Can't remove null job
		if (job == null) return false;

		boolean removedSuccessfully;
		try {
			//get lock for any operation with structures of jobs
			jobsAccessLock.lock();

			//try to remove running job (because it should be done)
			removedSuccessfully = runningJobs.remove(job);
		} finally {
			//in any case unlock access lock
			jobsAccessLock.unlock();
		}

		return removedSuccessfully;
	}

	/**
	 * Get set of running jobs at this moment.
	 *
	 * @return set of running jobs at this moment
	 */
	public Set<T> getRunningJobs() {
		Set<T> runningJobs;
		try {
			//get lock for any operation with structures of jobs
			jobsAccessLock.lock();

			runningJobs = new HashSet<>(this.runningJobs);

		} finally {
			//in any case unlock access lock
			jobsAccessLock.unlock();
		}

		return runningJobs;
	}

	/**
	 * Get ordered list of waiting jobs at this moment.
	 *
	 * @return ordered list of waiting jobs at this moment
	 */
	public List<T> getWaitingJobs() {
		List<T> waitingJobs;
		try {
			//get lock for any operation with structures of jobs
			jobsAccessLock.lock();

			waitingJobs = new ArrayList<>(this.waitingJobs);
		} finally {
			//in any case unlock access lock
			jobsAccessLock.unlock();
		}

		return waitingJobs;
	}
}