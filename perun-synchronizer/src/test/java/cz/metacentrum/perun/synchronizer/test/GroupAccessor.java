package cz.metacentrum.perun.synchronizer.test;

import static cz.metacentrum.perun.core.impl.PerunLocksUtils.lockGroupMembership;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.Semaphore;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Component;

import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;

@Component
public class GroupAccessor {

	/**
	 * 
	 */
	private Group group;


	public GroupAccessor() {
	}
	
	public void accessGroupThread1(Semaphore starter) throws GroupNotExistsException, InternalErrorException, PrivilegeException, InterruptedException {
		lockGroupMembership(group);
		group.setDescription("failure");
		starter.release();
		// groupsManagerBl.getGroupsManagerImpl().updateGroup(sess, group);
		// allow the other thread time for mischief
		Thread.sleep(500);
		group.setDescription("success");
		// groupsManagerBl.updateGroup(sess, group);
	}

	public String accessGroupThread2(Semaphore starter) throws GroupNotExistsException, InternalErrorException, PrivilegeException, InterruptedException {
		starter.acquire();
		lockGroupMembership(group);
		// Group resultGroup = groupsManagerBl.getGroupsManagerImpl().getGroupById(sess, group.getId());
		String result = group.getDescription();
		return result;
	}

	public void accessGroupThread3(Semaphore starter) throws GroupNotExistsException, InternalErrorException, PrivilegeException, InterruptedException {
		group.setDescription("success2");
		starter.release();
		Thread.sleep(500);
		// groupsManagerBl.getGroupsManagerImpl().updateGroup(sess, group);
		// allow the other thread time for mischief
		group.setDescription("failure2");
		// groupsManagerBl.updateGroup(sess, group);
	}
	
	public Group getGroup() {
		return group;
	}

	public void setGroup(Group group) {
		this.group = group;
	}

}
