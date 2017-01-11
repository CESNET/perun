package cz.metacentrum.perun.cabinet.dao.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import cz.metacentrum.perun.cabinet.dao.PublicationSystemManagerDao;
import cz.metacentrum.perun.cabinet.bl.CabinetException;
import cz.metacentrum.perun.cabinet.bl.ErrorCodes;
import cz.metacentrum.perun.cabinet.service.impl.BaseIntegrationTest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import cz.metacentrum.perun.cabinet.model.PublicationSystem;
import cz.metacentrum.perun.cabinet.bl.HttpManagerBl;
import cz.metacentrum.perun.cabinet.strategy.IFindPublicationsStrategy;

public class PublicationSystemManagerDaoImplTest extends BaseIntegrationTest {

	private PublicationSystemManagerDao publicationSystemManagerDao;
	private HttpManagerBl httpService;

	@Autowired
	public void setHttpService(HttpManagerBl httpService) {
		this.httpService = httpService;
	}

	@Autowired
	public void setPublicationSystem(PublicationSystemManagerDao publicationSystemManagerDao) {
		this.publicationSystemManagerDao = publicationSystemManagerDao;
	}

	@Test
	public void getPublicationSystemByNamespace() throws Exception {
		System.out.println("PublicationSystemDao.getPublicationSystemByNamespace");

		PublicationSystem publicationSystem = publicationSystemManagerDao.getPublicationSystemByNamespace("mu");
		assertNotNull(publicationSystem);

		IFindPublicationsStrategy prezentator = (IFindPublicationsStrategy) Class.forName(publicationSystem.getType()).newInstance();
		assertNotNull(prezentator);
	}

	@Test
	public void contactPublicationSystemMUTest() throws Exception {
		System.out.println("PublicationSystemMapper.contactPublicationSystemMUTest");

		PublicationSystem publicationSystem = publicationSystemManagerDao.getPublicationSystemByNamespace("mu");
		assertNotNull(publicationSystem);

		IFindPublicationsStrategy prezentator = (IFindPublicationsStrategy) Class.forName(publicationSystem.getType()).newInstance();
		assertNotNull(prezentator);

		String authorId = "39700";
		int yearSince = 2009;
		int yearTill = 2010;
		HttpResponse result = httpService.execute(prezentator.getHttpRequest(authorId, yearSince, yearTill, publicationSystem));

		assertNotNull(result);

	}

	@Test
	public void contactPublicationSystemOBDTest() throws Exception {
		System.out.println("PublicationSystemMapper.contactPublicationSystemOBDTest");

		PublicationSystem publicationSystem = publicationSystemManagerDao.getPublicationSystemByNamespace("zcu");
		assertNotNull(publicationSystem);

		IFindPublicationsStrategy prezentator = (IFindPublicationsStrategy) Class.forName(publicationSystem.getType()).newInstance();
		assertNotNull(prezentator);

		IFindPublicationsStrategy obd = (IFindPublicationsStrategy) Class.forName(publicationSystem.getType()).newInstance();
		assertNotNull(obd);

		String authorId = "Sitera,Jiří";
		int yearSince = 2006;
		int yearTill = 2009;
		HttpUriRequest request = obd.getHttpRequest(authorId, yearSince, yearTill, publicationSystem);

		try {
			HttpResponse response = httpService.execute(request);
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

}
