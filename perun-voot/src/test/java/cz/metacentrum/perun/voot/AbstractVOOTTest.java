package cz.metacentrum.perun.voot;

import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.PerunClient;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.*;
import cz.metacentrum.perun.core.bl.PerunBl;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Main test class defining context and common variables.
 * Any VOOT test class should extend this one.
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:perun-voot.xml"})
@Transactional(transactionManager = "perunTransactionManager")
public abstract class AbstractVOOTTest {

	@Autowired
	PerunBl perun;
	// session
	PerunSession session;
	// user in session
	User user1;

	/**
	 * Setup session for tests, it's called before each class
	 * @throws Exception
	 */
	@Before
	public void setUpSession() throws Exception {
		session = perun.getPerunSession(
				new PerunPrincipal("perunTests", ExtSourcesManager.EXTSOURCE_NAME_INTERNAL, ExtSourcesManager.EXTSOURCE_INTERNAL),
				new PerunClient());
		user1 = setUpUser1();
		session.getPerunPrincipal().setUser(user1);
		setUpBackground();
	}

	/**
	 * Each test can setup own background (environment)
	 * by implementing this method
	 * @throws PerunException
	 */
	abstract void setUpBackground() throws PerunException;

	User setUpUser1() throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		User user = new User();
		user.setFirstName("James");
		user.setMiddleName("");
		user.setLastName("Bond");
		user.setTitleBefore("");
		user.setTitleAfter("");
		return perun.getUsersManagerBl().createUser(session, user);
	}

}
