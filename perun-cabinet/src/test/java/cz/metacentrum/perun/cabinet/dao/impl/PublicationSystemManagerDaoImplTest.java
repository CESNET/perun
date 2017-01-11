package cz.metacentrum.perun.cabinet.dao.impl;

import static org.junit.Assert.assertNotNull;

import cz.metacentrum.perun.cabinet.dao.PublicationSystemManagerDao;
import cz.metacentrum.perun.cabinet.service.impl.CabinetBaseIntegrationTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import cz.metacentrum.perun.cabinet.model.PublicationSystem;
import cz.metacentrum.perun.cabinet.strategy.PublicationSystemStrategy;

public class PublicationSystemManagerDaoImplTest extends CabinetBaseIntegrationTest {

	private PublicationSystemManagerDao publicationSystemManagerDao;

	@Autowired
	public void setPublicationSystem(PublicationSystemManagerDao publicationSystemManagerDao) {
		this.publicationSystemManagerDao = publicationSystemManagerDao;
	}

	@Test
	public void getPublicationSystemByNamespace() throws Exception {
		System.out.println("PublicationSystemDao.getPublicationSystemByNamespace");

		PublicationSystem publicationSystem = publicationSystemManagerDao.getPublicationSystemByNamespace("mu");
		assertNotNull(publicationSystem);

		PublicationSystemStrategy prezentator = (PublicationSystemStrategy) Class.forName(publicationSystem.getType()).newInstance();
		assertNotNull(prezentator);
	}

}
