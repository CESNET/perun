package cz.metacentrum.perun.synchronizer.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.sql.DataSource;

import org.hsqldb.jdbcDriver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Candidate;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.GroupsManager;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.bl.GroupsManagerBl;
import cz.metacentrum.perun.core.bl.UsersManagerBl;

public class PerunLocksUtilsTest extends AbstractSynchronizerTest {

	// these must be setUp"type" before every method to be in DB
	final ExtSource extSource = new ExtSource(0, "testExtSource", "cz.metacentrum.perun.core.impl.ExtSourceInternal");
	final Group group = new Group("GroupsManagerTestGroup1","testovaci1");
	final Group group2 = new Group("GroupsManagerTestGroup2","testovaci2");
	final Group group21 = new Group("GroupsManagerTestGroup21","testovaci21");
	final Group group3 = new Group("GroupsManagerTestGroup3","testovaci3");
	final Group group4 = new Group("GroupsManagerTestGroup4","testovaci4");
	final Group group5 = new Group("GroupsManagerTestGroup5","testovaci5");
	final Group group6 = new Group("GroupsManagerTestGroup6","testovaci6");
	final Group group7 = new Group("GroupsManagerTestGroup7","testovaci7");
	final Group group8 = new Group("GroupsManagerTestGroup8","testovaci8");

	private Vo vo;
	private List<Attribute> attributesList = new ArrayList<>();

	// exists before every method
	private GroupsManager groupsManager;
	private GroupsManagerBl groupsManagerBl;
	private AttributesManager attributesManager;
	private UsersManagerBl usersManagerBl;

	private String result;
	
	@Autowired
	private DataSource dataSource; 
	
	@Autowired 
	public GroupAccessor testAccess;
	
	@Before
	public void setUp() throws Exception {

		groupsManager = perun.getGroupsManager();
		groupsManagerBl = perun.getGroupsManagerBl();
		attributesManager = perun.getAttributesManager();
		usersManagerBl = perun.getUsersManagerBl();
		//vo = setUpVo();
		//setUpGroup(vo);
		// moved to every method to save testing time

	}

	@After
	public void tearDown() throws Exception {
		cleanUp();
	}

	@Test
	public void testLockGroupMembershipGroupListOfMember() {
		fail("Not yet implemented");
	}


	@Test
	public void testLockGroupMembershipGroup() throws Exception {

		JdbcTemplate jdbc = new JdbcTemplate(dataSource);

		Connection conn = DataSourceUtils.getConnection(dataSource);
		conn.setAutoCommit(false);
		vo = setUpVo();
		setUpGroup(vo);
		testAccess.setGroup(group);
		conn.commit();

		final Semaphore starter = new Semaphore(0); 

		Thread thread1 = new Thread(new Runnable() {

			@Override
			public void run() {

				try {
					testAccess.accessGroupThread1(starter);
				} catch (Exception e) {
					result = e.getMessage();
				}
			}
			
		});
		Thread thread2 = new Thread(new Runnable() {

			@Override
			public void run() {

				try {
					result = testAccess.accessGroupThread2(starter);
				} catch (Exception e) {
					result = e.getMessage();
				}
			}
			
		});

		result = "undetermined";
		
		thread1.start();
		thread2.start();
		
		// wait for both threads exit
		thread1.join(1000);
		thread2.join(1000);

		assertEquals("success", result);

		thread1 = new Thread(new Runnable() {

			@Override
			public void run() {

				try {
					testAccess.accessGroupThread3(starter);
				} catch (Exception e) {
					result = e.getMessage();
				}
			}
			
		});

		thread2 = new Thread(new Runnable() {

			@Override
			public void run() {

				try {
					result = testAccess.accessGroupThread2(starter);
				} catch (Exception e) {
					result = e.getMessage();
				}
			}
			
		});
		
		result = "undetermined";

		thread1.start();
		thread2.start();

		// wait for both threads exit
		thread1.join(1000);
		thread2.join(1000);

		assertEquals("success2", result);
	}

	@Test
	public void testLockGroupMembershipListOfGroup() {
		fail("Not yet implemented");
	}

	// PRIVATE METHODS -------------------------------------------------------------

	private Vo setUpVo() throws Exception {

		Vo newVo = new Vo(0, "SynchronizerTestVo", "SyncTestVo");
		Vo returnedVo = perun.getVosManager().createVo(sess, newVo);
		// create test VO in database
		assertNotNull("unable to create testing Vo",returnedVo);

		//ExtSource es = perun.getExtSourcesManager().getExtSourceByName(sess, "LDAPMETA");
		// get real external source from DB
		//perun.getExtSourcesManager().addExtSource(sess, returnedVo, es);
		// add real ext source to our VO

		return returnedVo;

	}

	private Member setUpMember(Vo vo) throws Exception {

		// List<Candidate> candidates = perun.getVosManager().findCandidates(sess, vo, extLogin);
		// find candidates from ext source based on extLogin
		// assertTrue(candidates.size() > 0);

		Candidate candidate = setUpCandidate(0);
		Member member = perun.getMembersManagerBl().createMemberSync(sess, vo, candidate); // candidates.get(0)
		// set first candidate as member of test VO
		assertNotNull("No member created", member);
		usersForDeletion.add(perun.getUsersManager().getUserByMember(sess, member));
		// save user for deletion after test
		return member;

	}

	private Candidate setUpCandidate(int i) {

		String userFirstName = Long.toHexString(Double.doubleToLongBits(Math.random()));
		String userLastName = Long.toHexString(Double.doubleToLongBits(Math.random()));
		String extLogin = Long.toHexString(Double.doubleToLongBits(Math.random()));              // his login in external source

		Candidate candidate = new Candidate();  //Mockito.mock(Candidate.class);
		candidate.setFirstName(userFirstName);
		candidate.setId(0+i);
		candidate.setMiddleName("");
		candidate.setLastName(userLastName);
		candidate.setTitleBefore("");
		candidate.setTitleAfter("");
		final UserExtSource userExtSource = new UserExtSource(extSource, extLogin);
		candidate.setUserExtSource(userExtSource);
		candidate.setAttributes(new HashMap<>());
		return candidate;

	}

	private void setUpGroup(Vo vo) throws Exception {

		Group returnedGroup = groupsManager.createGroup(sess, vo, group);
		assertNotNull("unable to create a group",returnedGroup);
		returnedGroup = groupsManager.getGroupById(sess, group.getId()); 
		assertEquals("created group should be same as returned group",group,returnedGroup);

	}

	private List<Group> setUpGroups(Vo vo) throws Exception {
		List<Group> groups = new ArrayList<>();
		groups.add(groupsManager.createGroup(sess, vo, group3));
		groups.add(groupsManager.createGroup(sess, vo, group4));
		return groups;
	}

	private Resource setUpResource(Vo vo, Facility facility) throws Exception {

		Resource resource = new Resource();
		resource.setName("GroupsManagerTestResource");
		resource.setDescription("testing resource");
		assertNotNull("unable to create resource",perun.getResourcesManager().createResource(sess, resource, vo, facility));

		return resource;

	}

	private void cleanUp() throws Exception 
	{
		JdbcTemplate jdbc = new JdbcTemplate(dataSource);
		
		Connection conn = DataSourceUtils.getConnection(dataSource);
                conn.setAutoCommit(false);
		jdbc.execute("delete from groups where vo_id = (select id from vos where short_name = 'SyncTestVo')");
		jdbc.execute("delete from vos where short_name = 'SyncTestVo'");
		conn.commit();
	}
	
}
