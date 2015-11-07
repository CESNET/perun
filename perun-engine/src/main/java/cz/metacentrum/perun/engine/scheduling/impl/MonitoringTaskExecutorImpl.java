package cz.metacentrum.perun.engine.scheduling.impl;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import cz.metacentrum.perun.engine.scheduling.MonitoringTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.Assert;

/**
 * This class is used as replacement for Thread Pool Task Executor in purpose to use its own Thread Pool Executor.
 * Original implementation of Thread Pool Task Executor holds its Thread Pool Executor as private attribute and
 * uses it for Worker execution.
 * <p/>
 * We want to replace that Thread Pool Executor with our own in order implement new methods to get
 * information about internal processes of Worker execution.
 * <p/>
 * This class extends ThreadPoolTaskExecutor from package org.springframework.scheduling.concurrent,
 * for more information see JavaDoc of ThreadPoolTaskExecutor.
 *
 * @author Jana Cechackova (396049@mail.muni.cz)
 * @version 3
 */
public class MonitoringTaskExecutorImpl extends ThreadPoolTaskExecutor implements MonitoringTaskExecutor {
	private static final int queueCapacity = 200000;
	private MonitoringThreadPoolExecutor executor;

	/**
	 * Method, that initializes new instance of our own Thread Pool Executor.
	 *
	 * @param threadFactory            see JavaDoc of superclass
	 * @param rejectedExecutionHandler see JavaDoc of superclass
	 * @return executor                 initialized executor
	 */
	@Override
	protected ExecutorService initializeExecutor(ThreadFactory threadFactory, RejectedExecutionHandler rejectedExecutionHandler) {
		BlockingQueue<Runnable> queue = super.createQueue(queueCapacity);
		executor = new MonitoringThreadPoolExecutor(super.getCorePoolSize(), super.getMaxPoolSize(),
				super.getKeepAliveSeconds(), TimeUnit.SECONDS, queue, threadFactory, rejectedExecutionHandler);
		return executor;
	}

	@Override
	public ThreadPoolExecutor getThreadPoolExecutor() throws IllegalStateException {

		Assert.state(executor != null, "ThreadPoolTaskExecutor not initialized.");
		return executor;
	}

	/**
	 * Method, that make it easy to print information about executor.
	 *
	 * @param howManyTimes how many prints do we want
	 * @param delayTime    how long should be delay between each print in milliseconds
	 */
	public void printAndWait(int howManyTimes, int delayTime) {
		try {
			for (int i = 0; i < howManyTimes; i++) {
				System.out.println(getThreadPoolExecutor().toString());
				Thread.sleep(delayTime);
			}
		} catch (InterruptedException ex) {
			System.err.println(ex.toString());
		}
	}
}
