package cz.metacentrum.perun.core.entry;

import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.ConsentsManager;
import org.junit.Before;
import org.junit.Test;

/**
 * Integration tests of ConsentsManager.
 *
 * @author Jakub Hejda <Jakub.Hejda@cesnet.cz>
 */
public class ConsentsManagerEntryIntegrationTest extends AbstractPerunIntegrationTest {

	private ConsentsManager consentsManagerEntry;

	@Before
	public void setUp() throws Exception {
		consentsManagerEntry = perun.getConsentsManager();
	}

	@Test
	public void getAllConsentHubs() throws Exception {
		//TODO: test
	}

	@Test
	public void getConsentHubById() throws Exception {
		//TODO: test
	}

	@Test
	public void getConsentHubByName() throws Exception {
		//TODO: test
	}

}
