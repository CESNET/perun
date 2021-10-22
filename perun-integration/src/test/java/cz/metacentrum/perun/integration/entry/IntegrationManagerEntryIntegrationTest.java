package cz.metacentrum.perun.integration.entry;

import junit.framework.TestCase;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:perun-core.xml", "classpath:perun-integration.xml" } )
@Transactional(transactionManager = "perunTransactionManager")
public class IntegrationManagerEntryIntegrationTest {

}