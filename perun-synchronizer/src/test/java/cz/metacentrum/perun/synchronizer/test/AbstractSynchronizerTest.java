package cz.metacentrum.perun.synchronizer.test;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.Host;
import cz.metacentrum.perun.core.api.PerunClient;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.CacheManager;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextHierarchy({
		@ContextConfiguration(locations = {
					"classpath:perun-base.xml", 
					"classpath:perun-core.xml", 
					"classpath:perun-synchronizer.xml"})
})
@Transactional(transactionManager = "perunTestTransactionManager")
public abstract class AbstractSynchronizerTest {
	
	@Autowired
	protected PerunBl perun;

	protected static PerunSession sess;

	protected final SortedSet<User> usersForDeletion = new TreeSet<>();
	protected final Set<Host> hostsForDeletion = new HashSet<>();

	public void setPerun(PerunBl p) {
		this.perun = p;
	}

	@Before
	public void setUpSessAndCache() throws Exception {
		final PerunPrincipal pp = new PerunPrincipal("perunTests", ExtSourcesManager.EXTSOURCE_NAME_INTERNAL, ExtSourcesManager.EXTSOURCE_INTERNAL);
		sess = perun.getPerunSession(pp, new PerunClient());
		CacheManager.setCacheDisabled(true);
	}

	@After
	public void tearDownCache() {
		CacheManager.setCacheDisabled(false);
	}

}
