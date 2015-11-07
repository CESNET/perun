package cz.metacentrum.perun.engine.scheduling.impl;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Class that switches some methods of ReentrantReadWriteLocks.
 * <p/>
 * The reason is to make operations in Monitoring Thread Pool Executor more understandable
 * - we want to give access to any number of writers but only one reader
 * <p/>
 * This class extends ReentrantReadWriteLock from package java.util.concurrent.locks.ReentrantReadWriteLock,
 * for information see JavaDoc of ReentrantReadWriteLock
 *
 * @author Jana Cechackova
 * @version 3
 */
public class WorkerLock extends ReentrantReadWriteLock {

	/**
	 * Returns writeLock instead of readLock.
	 * Now only one reader can hold this lock in one time.     *
	 */
	public WriteLock readingLock() {
		return super.writeLock();
	}

	/**
	 * Returns readLock instead of writeLock.
	 * Now any number of writers can hold this lock in one time.
	 */
	public ReadLock writingLock() {
		return super.readLock();
	}


}