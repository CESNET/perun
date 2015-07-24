package cz.metacentrum.perun.cabinet.service.impl;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.Properties;

import cz.metacentrum.perun.core.api.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import cz.metacentrum.perun.cabinet.dao.IAuthorshipDao;
import cz.metacentrum.perun.cabinet.dao.ICategoryDao;
import cz.metacentrum.perun.cabinet.dao.IPublicationDao;
import cz.metacentrum.perun.cabinet.dao.IPublicationSystemDao;
import cz.metacentrum.perun.cabinet.model.Authorship;
import cz.metacentrum.perun.cabinet.model.Category;
import cz.metacentrum.perun.cabinet.model.Publication;
import cz.metacentrum.perun.cabinet.model.PublicationSystem;
import cz.metacentrum.perun.cabinet.service.IAuthorService;
import cz.metacentrum.perun.cabinet.service.IPerunService;
import cz.metacentrum.perun.core.bl.PerunBl;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:perun-cabinet.xml" })
@Transactional
@TransactionConfiguration(defaultRollback=true, transactionManager = "perunTransactionManager")
public class BaseIntegrationTest {

	public Authorship authorshipOne = null;
	public Authorship authorshipTwo = null;
	public Publication publicationOne = null;
	public Publication publicationTwo = null;
	public Category c1 = null;
	public static int USER_ID = 0;
	public static int USER_ID_2 = 1;
	public PublicationSystem pubSysZcu;
	public PublicationSystem pubSysMu;

	private boolean init = false;
	private int p1Id;

	@Autowired protected IAuthorshipDao authorshipDao;
	@Autowired protected ICategoryDao categoryDao;
	@Autowired protected IPublicationDao publicationDao;
	@Autowired protected IPublicationSystemDao publicationSystemDao;
	@Autowired protected IPerunService perunService;
	@Autowired protected IAuthorService authorService;
	@Autowired protected Properties cabinetProperties;
	@Autowired PerunBl perun;
	PerunSession sess;

	// setters -------------------------

	public void setPerunService(IPerunService perunService) {
		this.perunService = perunService;
	}

	public void setAuthorService(IAuthorService authorService) {
		this.authorService = authorService;
	}

	public void setReportDao(IAuthorshipDao reportDao) {
		this.authorshipDao = reportDao;
	}

	public void setCategoryDao(ICategoryDao categoryDao) {
		this.categoryDao = categoryDao;
	}

	public void setPublicationDao(IPublicationDao publicationDao) {
		this.publicationDao = publicationDao;
	}

	public void setPublicationSystemDao(IPublicationSystemDao publicationSystemDao) {
		this.publicationSystemDao = publicationSystemDao;
	}

	public void setCabinetProperties(Properties cabinetProperties) {
		this.cabinetProperties = cabinetProperties;
	}

	// test -------------------------------

	@Before
	public void beforeClass() throws Exception {

		if (init) return; //do only once for all tests

		// principal

		PerunPrincipal pp = new PerunPrincipal("perunTests", ExtSourcesManager.EXTSOURCE_NAME_INTERNAL, ExtSourcesManager.EXTSOURCE_INTERNAL);
		sess = perun.getPerunSession(pp);

		// setup world

		User user = new User();
		user.setLastName("cabinetTestUser");
		user.setServiceUser(false);
		user = perun.getUsersManagerBl().createUser(sess, user);
		USER_ID = user.getId();

		User user2 = new User();
		user2.setLastName("cabinetTestUser2");
		user2.setServiceUser(false);
		user2 = perun.getUsersManagerBl().createUser(sess, user2);
		USER_ID_2 = user2.getId();

		// category

		c1 = new Category(null, "patent", 3.9);
		int categoryId = categoryDao.createCategory(c1);
		c1.setId(categoryId);

		// publication systems

		PublicationSystem ps = new PublicationSystem();
		ps.setFriendlyName("OBD");
		ps.setLoginNamespace("zcu");
		ps.setUrl("http://obd.zcu.cz:6443/fcgi/verso.fpl?");
		ps.setType("cz.metacentrum.perun.cabinet.strategy.impl.OBD30Strategy");

		int id = publicationSystemDao.createPublicationSystem(ps);
		pubSysZcu = ps;

		assertTrue(id > 0);

		PublicationSystem ps2 = new PublicationSystem();
		ps2.setFriendlyName("Masarykova Univerzita - Prezentátor");
		ps2.setLoginNamespace("mu");
		ps2.setUrl("https://is.muni.cz/auth/prezentator/index.pl");
		ps2.setUsername(cabinetProperties.getProperty("perun.cabinet.mu.login"));
		ps2.setPassword(cabinetProperties.getProperty("perun.cabinet.mu.password"));
		ps2.setType("cz.metacentrum.perun.cabinet.strategy.impl.MUStrategy");

		int id2 = publicationSystemDao.createPublicationSystem(ps2);
		pubSysMu = ps2;

		assertTrue(id2 > 0);

		// create publication

		Publication p1 = new Publication();
		p1.setCategoryId(c1.getId());
		p1.setExternalId(666);
		p1.setPublicationSystemId(ps.getId());
		p1.setCreatedBy(sess.getPerunPrincipal().getActor());
		p1.setCreatedDate(new Date());
		p1.setTitle("Some title");
		p1.setIsbn("ISBN");
		p1.setMain("MAIN");
		p1.setYear(2020);
		p1.setRank(0.0);
		p1.setLocked(false);
		p1.setDoi("DOI1");

		Publication p2 = new Publication();
		p2.setCategoryId(c1.getId());
		p2.setExternalId(333);
		p2.setPublicationSystemId(ps.getId());
		p2.setCreatedBy(sess.getPerunPrincipal().getActor());
		p2.setCreatedDate(new Date());
		p2.setTitle("Some title vol. 2");
		p2.setIsbn("ISBN2");
		p2.setMain("MAIN2");
		p2.setYear(2025);
		p2.setRank(0.0);
		p2.setLocked(false);
		p2.setDoi("DOI2");

		p1Id = publicationDao.createPublication(sess, p1);
		p1.setId(p1Id);
		int p2Id = publicationDao.createPublication(sess, p2);
		p2.setId(p2Id);

		publicationOne = p1;
		publicationTwo = p2;

		Authorship a1 = new Authorship();
		a1.setCreatedBy(sess.getPerunPrincipal().getActor());
		a1.setCreatedDate(new Date());
		a1.setPublicationId(p1.getId()); // for PUB 1
		a1.setUserId(USER_ID);

		Authorship a2 = new Authorship();
		a2.setCreatedBy(sess.getPerunPrincipal().getActor());
		a2.setCreatedDate(new Date());
		a2.setPublicationId(p1.getId()); // for PUB 1
		a2.setUserId(USER_ID_2);

		int aId1 = authorshipDao.create(a1);
		a1.setId(aId1);
		int aId2 = authorshipDao.create(a2);
		a2.setId(aId2);

		authorshipOne = a1;
		authorshipTwo = a2;

		init = true;

	}

	@Test
	public void dummyTest() {
		// FIXME - Dummy test to prevent JUnit4 'No runnable methods' error.
	}

}
