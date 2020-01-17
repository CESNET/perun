package cz.metacentrum.perun.core.impl;

import static java.util.stream.Collectors.joining;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcPerunTemplate;
import org.springframework.jdbc.core.RowCountCallbackHandler;

import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;

/**
 * Methods and structures for working with locks on objects and actions.
 *
 * Created by Michal Stava stavamichal@gmail.com
 */
public class PerunLocksUtils {

	private final static Logger log = LoggerFactory.getLogger(PerunLocksUtils.class);

	private static JdbcPerunTemplate jdbc = null;

	//This empty object is used just for purpose of saving and identifying all locks for separate transaction
	public static final ThreadLocal<Object> uniqueKey = ThreadLocal.withInitial(Object::new);

	//Maps for saving and working with specific locks
	private static final ConcurrentHashMap<Group, ReadWriteLock> groupsLocks = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<Group, ConcurrentHashMap<Member, Lock>> groupsMembersLocks = new ConcurrentHashMap<>();

	private static PerunLocksUtils setDataSource(DataSource perunPool) {
		PerunLocksUtils.jdbc = new  JdbcPerunTemplate(perunPool);
		return new PerunLocksUtils();
	}
	
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
	public static void lockGroupMembership(Group group, List<Member> members) throws InternalErrorException {
		if(group == null) throw new InternalErrorException("Group can't be null when creating lock for group and list of members.");
		if(members == null) throw new InternalErrorException("Members can't be null or empty when creating lock for group and list of members.");

		lockGroupMembership(group);
		
		RowCountCallbackHandler countCallback = new RowCountCallbackHandler();  //
		log.debug("Trying to get lock on " + members.size() + " members" );
		jdbc.query("select * from  members where members.id in (" + 
				members.stream()
					.map(m -> String.valueOf(m.getId()))
					.collect(joining(",")) + ") for update", countCallback);
		if(countCallback.getRowCount() != members.size()) {
			throw new InternalErrorException("Error locking members - some members not found (expected " 
					+ members.size() + ", got " + countCallback.getRowCount() + ")");
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
	public static void lockGroupMembership(Group group) throws InternalErrorException {
		if(group == null) throw new InternalErrorException("Group can't be null when creating lock for group.");

		//jdbc.execute("select * from groups where id =  " + String.valueOf(group.getId()) + " for update");
		RowCountCallbackHandler countCallback = new RowCountCallbackHandler();  //
		log.debug("Trying to get lock on group " + group.getId());
		jdbc.query("select * from  groups where groups.id = " + String.valueOf(group.getId()) + " for update", countCallback);
		if(countCallback.getRowCount() == 0) {
			throw new InternalErrorException("Error locking group - no such group " + group.getId());
		}
		
	}

	/**
	 * Create transaction locks for list of Groups and bind them to the transaction (as resource by Object uniqueKey)
	 *
	 * @param groups list of groups
	 * @throws InternalErrorException
	 */
	public static void lockGroupMembership(List<Group> groups) throws InternalErrorException {
		if(groups != null) {
			for(Group group: groups) {
				lockGroupMembership(group);
			}
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
