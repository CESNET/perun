package cz.metacentrum.perun.core;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import cz.metacentrum.perun.core.api.PerunClient;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.Host;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.bl.PerunBl;

/**
 * This abstract class is intended for extending in integration tests in
 * Perun-core.
 *
 *
 * Extending this class brings you:
 * <ul>
 * <li>Reference for Perun instance from Spring's application context.</li>
 * <li>Automatical transaction support (via Spring ContextTest Framework) with
 * default rollback feature.
 * </ul>
 *
 * Your class can provide/overwrite it's own configuration of Spring's
 * application context
 *
 * @see <a href="http://static.springsource.org/spring/docs/current/spring-framework-reference/html/testing.html#testcontext-framework">testing</a>
 *
 *      You can use Mockito and PowerMock testing frameworks as well.
 *
 *      Keep in your mind that you can use AbstractJUnit4SpringContextTests for
 *      similar purpose. This alternative approach offers reference for
 *      JdbcTemplate which is useful for quering against database.
 *
 * @author Jiri Harazim <harazim@mail.muni.cz>
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:perun-base.xml", "classpath:perun-core.xml" })
@Transactional(transactionManager = "springTransactionManager")
public abstract class AbstractPerunIntegrationTest {

	@Autowired
	protected PerunBl perun;

	protected PerunSession sess;

	protected SortedSet<User> usersForDeletion = new TreeSet<User>();
	protected Set<Host> hostsForDeletion = new HashSet<Host>();

	public void setPerun(PerunBl p) {
		this.perun = p;
	}

	@Before
	public void setUpSess() throws Exception {
		final PerunPrincipal pp = new PerunPrincipal("perunTests", ExtSourcesManager.EXTSOURCE_NAME_INTERNAL, ExtSourcesManager.EXTSOURCE_INTERNAL);
		sess = perun.getPerunSession(pp, new PerunClient());
	}

}
