package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.ConsentHub;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Methods and structures for working with locks on objects and actions.
 *
 * Created by Michal Stava stavamichal@gmail.com
 */
public class PerunLocksUtils {

	private final static Logger log = LoggerFactory.getLogger(PerunLocksUtils.class);

	//This empty object is used just for purpose of saving and identifying all locks for separate transaction
	public static final ThreadLocal<Object> uniqueKey = ThreadLocal.withInitial(Object::new);

	//Maps for saving and working with specific locks
	private static final ConcurrentHashMap<Group, ReadWriteLock> groupsLocks = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<Group, ConcurrentHashMap<Member, Lock>> groupsMembersLocks = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<ConsentHub, ConcurrentHashMap<Integer, Lock>> consentHubsUsersLocks = new ConcurrentHashMap<>();

	/**
	 * Create transaction locks for combination of group and member (from list of members)
	 * and also bind them to the transaction (as resource by Object uniqueKey)
	 *
	 * @param group the group
	 * @param members list of members
	 * @throws InternalErrorException
	 * @throws InterruptedException
	 */
	@SuppressWarnings("ConstantConditions")
	public static void lockGroupMembership(Group group, List<Member> members) {
		if(group == null) throw new InternalErrorException("Group can't be null when creating lock for group and list of members.");
		if(members == null) throw new InternalErrorException("Members can't be null or empty when creating lock for group and list of members.");

		//Sort list of members strictly by ids (there is compareTo method in perunBean using ids for comparing)
		Collections.sort(members);

		List<Lock> returnedLocks = new ArrayList<>();

		try {
			try {
				//try to lock all needed locks there
				ReadWriteLock groupReadWriteLock = groupsLocks.computeIfAbsent(group, f -> new ReentrantReadWriteLock(true));

				if (!groupReadWriteLock.readLock().tryLock(4, TimeUnit.HOURS)) {
					throw new InternalErrorException("Can't acquire a lock in expected time.");
				}

				returnedLocks.add(groupReadWriteLock.readLock());
				for (Member member : members) {
					//Get members lock map by group if exists or create a new one
					ConcurrentHashMap<Member, Lock> membersLocks = groupsMembersLocks.get(group);
					if (membersLocks == null) {
						groupsMembersLocks.putIfAbsent(group, new ConcurrentHashMap<>());
						membersLocks = groupsMembersLocks.get(group);
					}

					//Get concrete member lock from members lock map or create a new one if not exists
					Lock memberLock = membersLocks.computeIfAbsent(member, f -> new ReentrantLock(true));

					//Lock the lock and return it
					if (!memberLock.tryLock(4, TimeUnit.HOURS)) {
						throw new InternalErrorException("Can't acquire a lock in expected time.");
					}
					returnedLocks.add(memberLock);
				}

				//bind these locks like transaction resource
				if (TransactionSynchronizationManager.getResource(uniqueKey.get()) == null) {
					TransactionSynchronizationManager.bindResource(uniqueKey.get(), returnedLocks);
				} else {
					// the returned resource can never be null because of the previous check
					((List<Lock>) TransactionSynchronizationManager.getResource(uniqueKey.get())).addAll(returnedLocks);
				}
			} catch (InterruptedException ex) {
				throw new InternalErrorException("Interrupted exception has been thrown while locking group " + group + " and list of members " + members, ex);
			}
		} catch (Exception ex) {
			//if some exception has been thrown, unlock all already locked locks
			unlockAll(returnedLocks);
			throw ex;
		}
	}

	/**
	 * Create transaction locks for group and bind them to the transaction (as resource by Object uniqueKey)
	 *
	 * @param group the group
	 * @throws InternalErrorException
	 * @throws InterruptedException
	 */
	@SuppressWarnings("ConstantConditions")
	public static void lockGroupMembership(Group group) {
		if(group == null) throw new InternalErrorException("Group can't be null when creating lock for group.");

		List<Lock> returnedLocks = new ArrayList<>();
		try {
			try {
				//Need to investigate if we have all needed locks already in the structure or we need to create them
				ReadWriteLock groupReadWriteLock = groupsLocks.computeIfAbsent(group, f -> new ReentrantReadWriteLock(true));

				if (!groupReadWriteLock.writeLock().tryLock(4, TimeUnit.HOURS)) {
					throw new InternalErrorException("Can't acquire a lock in expected time.");
				}
				returnedLocks.add(groupReadWriteLock.writeLock());

				//bind these locks like transaction resource
				if (TransactionSynchronizationManager.getResource(uniqueKey.get()) == null) {
					TransactionSynchronizationManager.bindResource(uniqueKey.get(), returnedLocks);
				} else {
					// the returned resource can never be null because of the previous check
					((List<Lock>) TransactionSynchronizationManager.getResource(uniqueKey.get())).addAll(returnedLocks);
				}
			} catch (InterruptedException ex) {
				throw new InternalErrorException("Interrupted exception has been thrown while locking group " + group, ex);
			}
		} catch (Exception ex) {
			//if some exception has been thrown, unlock all already locked locks
			unlockAll(returnedLocks);
			throw ex;
		}
	}

	/**
	 * Create transaction locks for list of Groups and bind them to the transaction (as resource by Object uniqueKey)
	 *
	 * @param groups list of groups
	 * @throws InternalErrorException
	 */
	public static void lockGroupMembership(List<Group> groups) {
		if(groups != null) {
			for(Group group: groups) {
				lockGroupMembership(group);
			}
		}
	}

	/**
	 * Create transaction lock for combination of consentHub and user and also
	 * bind it to the transaction (as resource by Object uniqueKey)
	 *
	 * @param consentHub the consentHub
	 * @param userId id of the User
	 */
	@SuppressWarnings("ConstantConditions")
	public static void lockUserInConsentHub(ConsentHub consentHub, int userId) {
		if(consentHub == null) throw new InternalErrorException("ConsentHub can't be null when creating lock for consent hub and user.");

		List<Lock> returnedLocks = new ArrayList<>();

		try {
			try {
				//Get user's lock map by consentHub if exists or create a new one
				ConcurrentHashMap<Integer, Lock> userLocks = consentHubsUsersLocks.get(consentHub);
				if (userLocks == null) {
					consentHubsUsersLocks.putIfAbsent(consentHub, new ConcurrentHashMap<>());
					userLocks = consentHubsUsersLocks.get(consentHub);
				}

				//Get concrete user lock from users lock map or create a new one if not exists
				Lock userLock = userLocks.computeIfAbsent(userId, f -> new ReentrantLock(true));

				//Lock the lock and return it
				if (!userLock.tryLock(4, TimeUnit.HOURS)) {
					throw new InternalErrorException("Can't acquire a lock in expected time.");
				}
				returnedLocks.add(userLock);

				//bind these locks like transaction resource
				if (TransactionSynchronizationManager.getResource(uniqueKey.get()) == null) {
					TransactionSynchronizationManager.bindResource(uniqueKey.get(), returnedLocks);
				} else {
					// the returned resource can never be null because of the previous check
					((List<Lock>) TransactionSynchronizationManager.getResource(uniqueKey.get())).addAll(returnedLocks);
				}
			} catch (InterruptedException ex) {
				throw new InternalErrorException("Interrupted exception has been thrown while locking consentHub " + consentHub + " and user with id " + userId, ex);
			}
		} catch (Exception ex) {
			//if some exception has been thrown, unlock all already locked locks
			unlockAll(returnedLocks);
			throw ex;
		}
	}

	/**
	 * Unlock all locks in argument list
	 *
	 * @param locks list of locks to unlock
	 */
	public static void unlockAll(List<Lock> locks) {
		if(locks != null) {
			for(Lock lock: locks) {
				lock.unlock();
			}
		}
	}
}
