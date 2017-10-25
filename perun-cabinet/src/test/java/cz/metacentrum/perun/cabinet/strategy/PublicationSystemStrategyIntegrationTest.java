package cz.metacentrum.perun.cabinet.strategy;

import cz.metacentrum.perun.cabinet.bl.CabinetException;
import cz.metacentrum.perun.cabinet.bl.ErrorCodes;
import cz.metacentrum.perun.cabinet.model.PublicationSystem;
import cz.metacentrum.perun.cabinet.CabinetBaseIntegrationTest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Integration tests for retrieving publications using different strategies.
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class PublicationSystemStrategyIntegrationTest extends CabinetBaseIntegrationTest {

	@Test
	public void contactPublicationSystemMUTest() throws Exception {
		System.out.println("PublicationSystemStrategyIntegrationTest.contactPublicationSystemMUTest");

		PublicationSystem publicationSystem = getCabinetManager().getPublicationSystemByNamespace("mu");
		assertNotNull(publicationSystem);

		PublicationSystemStrategy prezentator = (PublicationSystemStrategy) Class.forName(publicationSystem.getType()).newInstance();
		assertNotNull(prezentator);

		String authorId = "39700";
		int yearSince = 2009;
		int yearTill = 2010;
		HttpResponse result = prezentator.execute(prezentator.getHttpRequest(authorId, yearSince, yearTill, publicationSystem));

		assertNotNull(result);

	}

	@Test
	public void contactPublicationSystemOBDTest() throws Exception {
		System.out.println("PublicationSystemStrategyIntegrationTest.contactPublicationSystemOBDTest");

		PublicationSystem publicationSystem = getCabinetManager().getPublicationSystemByNamespace("zcu");
		assertNotNull(publicationSystem);

		PublicationSystemStrategy prezentator = (PublicationSystemStrategy) Class.forName(publicationSystem.getType()).newInstance();
		assertNotNull(prezentator);

		PublicationSystemStrategy obd = (PublicationSystemStrategy) Class.forName(publicationSystem.getType()).newInstance();
		assertNotNull(obd);

		String authorId = "Sitera,Jiří";
		int yearSince = 2006;
		int yearTill = 2009;
		HttpUriRequest request = obd.getHttpRequest(authorId, yearSince, yearTill, publicationSystem);

		try {
			HttpResponse response = obd.execute(request);
			assertNotNull(response);
		} catch (CabinetException ex) {
			if (!ex.getType().equals(ErrorCodes.HTTP_IO_EXCEPTION)) {
				fail("Different exception code, was: "+ex.getType() +", but expected: HTTP_IO_EXCEPTION.");
				// fail if different error
			} else {
				System.out.println("-- Test silently skipped because of HTTP_IO_EXCEPTION");
			}
		}

	}

	@Test
	public void contactPublicationSystemEuropePMCTest() throws Exception {
		System.out.println("PublicationSystemStrategyIntegrationTest.contactPublicationSystemEuropePMCTest");

		PublicationSystem publicationSystem = getCabinetManager().getPublicationSystemByNamespace("europepmc");
		assertNotNull(publicationSystem);

		PublicationSystemStrategy prezentator = (PublicationSystemStrategy) Class.forName(publicationSystem.getType()).newInstance();
		assertNotNull(prezentator);

		String authorId = "0000-0002- 1767-9318";
		int yearSince = 2017;
		int yearTill = 0;
		HttpResponse result = prezentator.execute(prezentator.getHttpRequest(authorId, yearSince, yearTill, publicationSystem));
		assertNotNull(result);

	}

}
