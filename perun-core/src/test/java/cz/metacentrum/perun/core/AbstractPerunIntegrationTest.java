package cz.metacentrum.perun.core;

import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.Host;
import cz.metacentrum.perun.core.api.PerunClient;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.bl.PerunBl;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * This abstract class is intended for extending in integration tests in
 * Perun-core.
 * <p>
 * <p>
 * Extending this class brings you:
 * <ul>
 * <li>Reference for Perun instance from Spring's application context.</li>
 * <li>Automatical transaction support (via Spring ContextTest Framework) with
 * default rollback feature.
 * </ul>
 * <p>
 * Your class can provide/overwrite it's own configuration of Spring's
 * application context
 *
 * @author Jiri Harazim <harazim@mail.muni.cz>
 * @see <a href="http://static.springsource.org/spring/docs/current/spring-framework-reference/html/testing.html#testcontext-framework">testing</a>
 * <p>
 * You can use Mockito and PowerMock testing frameworks as well.
 * <p>
 * Keep in your mind that you can use AbstractJUnit4SpringContextTests for
 * similar purpose. This alternative approach offers reference for
 * JdbcTemplate which is useful for quering against database.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextHierarchy({
    @ContextConfiguration(locations = {"classpath:perun-base.xml", "classpath:perun-core.xml"})
})
@Transactional(transactionManager = "springTransactionManager")
public abstract class AbstractPerunIntegrationTest {

  protected static PerunSession sess;
  protected final SortedSet<User> usersForDeletion = new TreeSet<>();
  protected final Set<Host> hostsForDeletion = new HashSet<>();
  @Autowired
  protected PerunBl perun;

  public void setPerun(PerunBl p) {
    this.perun = p;
  }

  @Before
  public void setUpSess() throws Exception {
    final PerunPrincipal pp = new PerunPrincipal("perunTests", ExtSourcesManager.EXTSOURCE_NAME_INTERNAL,
        ExtSourcesManager.EXTSOURCE_INTERNAL);
    sess = perun.getPerunSession(pp, new PerunClient());
    perun.getExtSourcesManagerBl().loadExtSourcesDefinitions(sess);
  }

}
