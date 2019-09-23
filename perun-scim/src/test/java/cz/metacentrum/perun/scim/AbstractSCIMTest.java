package cz.metacentrum.perun.scim;

import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.PerunClient;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.bl.PerunBl;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Main test class defining context and common variables.
 * All other SCIM test classes are extending this class.
 *
 * @author Sona Mastrakova <sona.mastrakova@gmail.com>
 * @date 11.10.2016
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:perun-scim.xml" })
@Transactional(transactionManager = "springTransactionManager")
public abstract class AbstractSCIMTest {

	@Autowired
	private PerunBl perun;
	private User testUser;

	public PerunSession session;

	@Before
	public void setUpSession() throws Exception {
		session = perun.getPerunSession(new PerunPrincipal(
						"perunTests",
						ExtSourcesManager.EXTSOURCE_NAME_INTERNAL,
						ExtSourcesManager.EXTSOURCE_INTERNAL),
				new PerunClient());

		testUser = createTestUser();
		session.getPerunPrincipal().setUser(testUser);
	}

	private User createTestUser() throws InternalErrorException {
		User user = new User();
		user.setFirstName("James");
		user.setLastName("Bond");
		return perun.getUsersManagerBl().createUser(session, user);
	}

	public Vo createVo(int id, String name, String shortName) throws Exception {
		return perun.getVosManagerBl().createVo(session, new Vo(id, name, shortName));
	}

	public Group createGroup(Vo vo, String name, String desc) throws Exception {
		return perun.getGroupsManagerBl().createGroup(session, vo, new Group(name, desc));
	}

	public User createUser(int id, String firstName, String lastName) throws Exception {
		User user = new User(id, firstName, lastName, null, null, null);
		return perun.getUsersManagerBl().createUser(session, user);
	}

	public Member createMember(Vo vo, User user) throws Exception {
		return perun.getMembersManagerBl().createMember(session, vo, user);
	}

	public final void addMemberToGroup(Group group, Member member) throws Exception {
		perun.getGroupsManagerBl().addMember(session, group, member);
	}
}
