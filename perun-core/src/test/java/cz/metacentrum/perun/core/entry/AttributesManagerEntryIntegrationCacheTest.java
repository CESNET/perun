package cz.metacentrum.perun.core.entry;

import cz.metacentrum.perun.core.impl.CacheManager;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests of AttributesManager for cache.
 *
 * @author Simona Kruppova
 */
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional(transactionManager = "perunTestTransactionManager")
public class AttributesManagerEntryIntegrationCacheTest extends AttributesManagerEntryIntegrationTestAbstract {

	private final static String CLASS_NAME = "AttributesManagerCache.";

	@Before
	public void setUp() throws Exception {
		super.setUp();
		super.setClassName(CLASS_NAME);
		CacheManager.setCacheDisabled(false);
		perun.getCacheManager().newTopLevelTransaction();
	}

	@After
	public void rollback() {
		perun.getCacheManager().rollback();
	}
}
