package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.PerunBeanProcessingPool;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.blImpl.GroupsManagerBlImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Class used for scheduling group and group structure synchronizations.
 * It does not run any scheduler, it just provides the functionality for the scheduling of synchronizations.
 *
 * Methods in this class are thread safe
 */
public class SynchronizationPool {
	private final PerunBeanProcessingPool<Group> poolOfGroupsToBeSynchronized = new PerunBeanProcessingPool<>();
	private final PerunBeanProcessingPool<Group> poolOfGroupsStructuresToBeSynchronized = new PerunBeanProcessingPool<>();
	//Access lock to create concurrent access by any operation to any pool of this class.
	private final Lock poolAccessLock = new ReentrantLock(true);
	//Semaphore which takes care about emptiness of list of waiting groups (threads will wait for another group)
	//Counter in this semaphore counts number of waiting groups in the pool (0 means no groups are waiting to be processed)
	private final Semaphore notEmptyGroupsPoolSemaphore = new Semaphore(0, true);
	//Semaphore which takes care about emptiness of list of waiting groups structures (threads will wait for another group structure)
	//Counter in this semaphore counts number of waiting groups structures in the pool (0 means no groups structures are waiting to be processed)
	private final Semaphore notEmptyGroupsStructuresPoolSemaphore = new Semaphore(0, true);

	private final static Logger log = LoggerFactory.getLogger(SynchronizationPool.class);

	/**
	 * Put group to the pool of waiting groups structures.
	 *
	 * @param group which will be added to the pool of waiting group structures
	 * @param asFirst true if group will skip order and will be placed to the list as first (LIFO)
	 * @return
	 * @throws InternalErrorException
	 */
	public boolean putGroupStructureToPoolOfWaitingGroupsStructures(Group group, boolean asFirst) throws InternalErrorException {
		try {
			poolAccessLock.lock();
			if (poolOfGroupsStructuresToBeSynchronized.putJobIfAbsent(group, asFirst)) {
				notEmptyGroupsStructuresPoolSemaphore.release();
				return true;
			}
			return false;
		} finally {
			poolAccessLock.unlock();
		}
	}

	/**
	 * Put group to the pool of waiting groups.
	 *
	 * @param group which will be added to the pool of waiting groups
	 * @param asFirst true if group will skip order and will be placed to the list as first (LIFO)
	 * @return
	 * @throws InternalErrorException
	 */
	public boolean putGroupToPoolOfWaitingGroups(Group group, boolean asFirst) throws InternalErrorException {
		try {
			poolAccessLock.lock();
			if (poolOfGroupsToBeSynchronized.putJobIfAbsent(group, asFirst)) {
				notEmptyGroupsPoolSemaphore.release();
				return true;
			}
			return false;
		} finally {
			poolAccessLock.unlock();
		}
	}

	/**
	 * Put list of groups to the pool of waiting groups.
	 *
	 * @param groups which will be added to the pool of waiting groups
	 * @return
	 * @throws InternalErrorException
	 */
	public int putGroupsToPoolOfWaitingGroups(List<Group> groups) throws InternalErrorException {
		int numberOfAddedGroups = 0;
		try {
			poolAccessLock.lock();
			for (Group group: groups){
				if (poolOfGroupsToBeSynchronized.putJobIfAbsent(group, false)) {
					notEmptyGroupsPoolSemaphore.release();
					log.debug("Group {} was added to the pool of groups waiting for synchronization.", group);
					numberOfAddedGroups++;
				} else {
					log.debug("Group {} synchronization is already running.", group);
				}
			}
		} finally {
			poolAccessLock.unlock();
		}
		return numberOfAddedGroups;
	}

	/**
	 * Put list of groups to the pool of waiting groups structures.
	 *
	 * @param groups which will be added to the pool of waiting group structures
	 * @return
	 * @throws InternalErrorException
	 */
	public int putGroupsStructuresToPoolOfWaitingGroupsStructures(List<Group> groups) throws InternalErrorException {
		int numberOfAddedGroups = 0;
		try {
			poolAccessLock.lock();
			for (Group group: groups){
				if (poolOfGroupsStructuresToBeSynchronized.putJobIfAbsent(group, false)) {
					notEmptyGroupsStructuresPoolSemaphore.release();
					log.debug("Group structure {} was added to the pool of groups structures waiting for synchronization.", group);
					numberOfAddedGroups++;
				} else {
					log.debug("Group structure {} synchronization is already running.", group);
				}
			}
		} finally {
			poolAccessLock.unlock();
		}
		return numberOfAddedGroups;
	}

	/**
	 * Take a first group, which can be safely synchronized, from the pool of waiting groups and add it to the pool of running groups.
	 * If the group does not exists anymore, remove it from the pool of waiting groups, wait 10 seconds and try the whole process again.
	 * If none of the waiting groups can be synchronized, wait 10 seconds and try the whole process again.
	 *
	 * @param sess
	 * @return
	 * @throws InterruptedException
	 * @throws InternalErrorException
	 */
	public Group takeGroup(PerunSessionImpl sess) throws InterruptedException, InternalErrorException {
		while(true) {
			//I can take only if there is not empty list of waiting jobs
			notEmptyGroupsPoolSemaphore.acquire();

			try {
				poolAccessLock.lock();
				boolean removedGroup = false;
				//Take group which is not in all subGroups of group, which is in the pool of waiting or running group structures.
				for (Group group : poolOfGroupsToBeSynchronized.getWaitingJobs()) {
					boolean allowed = true;
					List<Group> groupStructureJobs = poolOfGroupsStructuresToBeSynchronized.getWaitingJobs();
					groupStructureJobs.addAll(poolOfGroupsStructuresToBeSynchronized.getRunningJobs());
					for (Group groupStructure : groupStructureJobs) {
						List<Group> allSubGroups = sess.getPerunBl().getGroupsManagerBl().getAllSubGroups(sess, groupStructure);
						if (allSubGroups.contains(group)) {
							allowed = false;
							break;
						}
					}
					if (allowed) {
						//Set the group to the first place in the pool of waiting groups.
						poolOfGroupsToBeSynchronized.putJobIfAbsent(group, true);
						try {
							//Group could be removed during some group structure synchronization, so there is no need to synchronize it anymore.
							sess.getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
						} catch (GroupNotExistsException e) {
							log.warn("Group {} was removed from a Vo while it was waiting for a synchronization.", group);
							poolOfGroupsToBeSynchronized.removeJob(poolOfGroupsToBeSynchronized.takeJob());
							removedGroup = true;
							break;
						}
						//Put the first group in the pool of waiting groups to the pool of running groups
						return poolOfGroupsToBeSynchronized.takeJob();
					}
				}
				//If no group was removed or taken we have to increase the semaphore.
				if (!removedGroup) notEmptyGroupsPoolSemaphore.release();
			} finally {
				poolAccessLock.unlock();
			}
			Thread.sleep(10000);
		}
	}

	/**
	 * Take a first group, which can be safely synchronized, from the pool of waiting groups structures and add it to the pool of running groups structures.
	 * If none of the waiting groups can be taken, wait 10 seconds and try again.
	 *
	 * @param sess
	 * @return
	 * @throws InterruptedException
	 * @throws InternalErrorException
	 */
	public Group takeGroupStructure(PerunSessionImpl sess) throws InterruptedException, InternalErrorException {
		while(true) {
			//I can take only if there is not empty list of waiting jobs
			notEmptyGroupsStructuresPoolSemaphore.acquire();

			try{
				poolAccessLock.lock();
				//Take the group which does not have any subGroup in the pool of running groups.
				for (Group groupStructure : poolOfGroupsStructuresToBeSynchronized.getWaitingJobs()) {
					boolean allowed = true;
					List<Group> allSubGroups = sess.getPerunBl().getGroupsManagerBl().getAllSubGroups(sess, groupStructure);
					for (Group runningGroup : poolOfGroupsToBeSynchronized.getRunningJobs()) {
						if (allSubGroups.contains(runningGroup)) {
							allowed = false;
							break;
						}
					}
					if (allowed) {
						//Set the group to the first place in the pool of waiting groups structures.
						poolOfGroupsStructuresToBeSynchronized.putJobIfAbsent(groupStructure, true);
						//Put the first group in the pool of waiting groups structures to the pool of running groups structures.
						return poolOfGroupsStructuresToBeSynchronized.takeJob();
					}
				}
				//None of the groups was taken so we have to increase the semaphore.
				notEmptyGroupsStructuresPoolSemaphore.release();
			} finally {
				poolAccessLock.unlock();
			}
			Thread.sleep(10000);
		}
	}

	/**
	 * Remove group from the pool of running groups structures
	 *
	 * @param group which will be removed from the pool of running groups structures
	 * @return
	 */
	public boolean removeGroupStructure(Group group) {
		try {
			poolAccessLock.lock();
			return poolOfGroupsStructuresToBeSynchronized.removeJob(group);
		} finally {
			poolAccessLock.unlock();
		}
	}

	/**
	 * Remove group from the pool of running groups
	 *
	 * @param group which will be removed from the pool of running groups structures
	 * @return
	 */
	public boolean removeGroup(Group group) {
		try {
			poolAccessLock.lock();
			return poolOfGroupsToBeSynchronized.removeJob(group);
		} finally {
			poolAccessLock.unlock();
		}
	}

	/**
	 * Method for accessing the pool of groups structures
	 *
	 * @return groups processing pool
	 */
	public PerunBeanProcessingPool<Group> asPoolOfGroupsToBeSynchronized() {
		try {
			poolAccessLock.lock();
			return poolOfGroupsToBeSynchronized;
		} finally {
			poolAccessLock.unlock();
		}
	}

	/**
	 * Method for accessing the pool of groups structures
	 *
	 * @return groups structures processing pool
	 */
	public PerunBeanProcessingPool<Group> asPoolOfGroupsStructuresToBeSynchronized() {
		try {
			poolAccessLock.lock();
			return poolOfGroupsStructuresToBeSynchronized;
		} finally {
			poolAccessLock.unlock();
		}
	}
}
