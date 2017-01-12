package cz.metacentrum.perun.cabinet.dao.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import cz.metacentrum.perun.cabinet.dao.PublicationSystemManagerDao;
import cz.metacentrum.perun.cabinet.service.impl.CabinetBaseIntegrationTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import cz.metacentrum.perun.cabinet.model.PublicationSystem;
import cz.metacentrum.perun.cabinet.strategy.PublicationSystemStrategy;

import java.util.List;
import java.util.Objects;

public class PublicationSystemManagerDaoTest extends CabinetBaseIntegrationTest {

	private PublicationSystemManagerDao publicationSystemManagerDao;

	@Autowired
	public void setPublicationSystem(PublicationSystemManagerDao publicationSystemManagerDao) {
		this.publicationSystemManagerDao = publicationSystemManagerDao;
	}

	@Test
	public void createPublicationSystem() throws Exception {
		System.out.println("PublicationSystemDao.createPublicationSystem");
		// TODO - implement test
	}


	@Test
	public void updatePublicationSystem() throws Exception {
		System.out.println("PublicationSystemDao.updatePublicationSystem");
		// TODO - implement test
	}

	@Test
	public void deletePublicationSystem() throws Exception {
		System.out.println("PublicationSystemDao.deletePublicationSystem");
		// TODO - implement test
	}

	@Test
	public void getPublicationSystems() throws Exception {
		System.out.println("PublicationSystemDao.getPublicationSystems");
		List<PublicationSystem> systems = publicationSystemManagerDao.getPublicationSystems();

		assertNotNull(systems);
		assertTrue(!systems.isEmpty());
		assertTrue(systems.contains(pubSysMu));
		assertTrue(systems.contains(pubSysZcu));
		assertTrue(systems.size() == 3);

	}

	@Test
	public void getPublicationSystemByNamespace() throws Exception {
		System.out.println("PublicationSystemDao.getPublicationSystemByNamespace");

		PublicationSystem publicationSystem = publicationSystemManagerDao.getPublicationSystemByNamespace("mu");
		assertNotNull(publicationSystem);
		assertTrue(Objects.equals(publicationSystem, pubSysMu));

		PublicationSystemStrategy ps = (PublicationSystemStrategy) Class.forName(publicationSystem.getType()).newInstance();
		assertNotNull(ps);
	}

	@Test
	public void getPublicationSystemById() throws Exception {
		System.out.println("PublicationSystemDao.getPublicationSystemById");

		PublicationSystem publicationSystem = publicationSystemManagerDao.getPublicationSystemById(pubSysZcu.getId());
		assertNotNull(publicationSystem);
		assertTrue(Objects.equals(publicationSystem, pubSysZcu));

		PublicationSystemStrategy ps = (PublicationSystemStrategy) Class.forName(publicationSystem.getType()).newInstance();
		assertNotNull(ps);

	}

	@Test
	public void getPublicationSystemByName() throws Exception {
		System.out.println("PublicationSystemDao.getPublicationSystemByName");

		PublicationSystem publicationSystem = publicationSystemManagerDao.getPublicationSystemByName(pubSysZcu.getFriendlyName());
		assertNotNull(publicationSystem);
		assertTrue(Objects.equals(publicationSystem, pubSysZcu));

		PublicationSystemStrategy ps = (PublicationSystemStrategy) Class.forName(publicationSystem.getType()).newInstance();
		assertNotNull(ps);

	}

}
