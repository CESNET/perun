package cz.metacentrum.perun.cabinet;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.Properties;

import cz.metacentrum.perun.cabinet.api.CabinetManager;
import cz.metacentrum.perun.core.api.*;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import cz.metacentrum.perun.cabinet.model.Authorship;
import cz.metacentrum.perun.cabinet.model.Category;
import cz.metacentrum.perun.cabinet.model.Publication;
import cz.metacentrum.perun.cabinet.model.PublicationSystem;
import cz.metacentrum.perun.core.bl.PerunBl;

/**
 * Base integration test class, all other tests should extend it.
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:perun-core.xml", "classpath:perun-cabinet.xml" })
@Transactional(transactionManager = "springTransactionManager")
public abstract class CabinetBaseIntegrationTest {

	public Authorship authorshipOne = null;
	public Authorship authorshipTwo = null;
	public Publication publicationOne = null;
	public Publication publicationTwo = null;
	public Owner owner;
	public Category c1 = null;
	public static int USER_ID = 0;
	public static int USER_ID_2 = 1;
	public PublicationSystem pubSysZcu;
	public PublicationSystem pubSysMu;
	public PublicationSystem pubSysEuropePMC;

	private boolean init = false;

	protected CabinetManager cabinetManager;
	@Autowired protected Properties cabinetProperties;
	@Autowired PerunBl perun;
	protected PerunSession sess;

	// setters -------------------------

	@Autowired
	public void setCabinetManager(CabinetManager cabinetManager) {
		this.cabinetManager = cabinetManager;
	}

	public CabinetManager getCabinetManager() {
		return cabinetManager;
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
		sess = perun.getPerunSession(pp, new PerunClient());

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

		// owner

		Owner owner = new Owner();
		owner.setName("PubOwner");
		owner.setContact("Call me");
		owner.setType(OwnerType.administrative);
		this.owner = perun.getOwnersManagerBl().createOwner(sess, owner);

		// category

		c1 = new Category(0, "patent", 3.9);
		c1 = getCabinetManager().createCategory(sess, c1);

		// publication systems

		PublicationSystem ps = new PublicationSystem();
		ps.setFriendlyName("OBD");
		ps.setLoginNamespace("zcu");
		ps.setUrl("http://obd.zcu.cz:6443/fcgi/verso.fpl?");
		ps.setType("cz.metacentrum.perun.cabinet.strategy.impl.OBD30Strategy");

		pubSysZcu = getCabinetManager().createPublicationSystem(sess, ps);
		assertTrue(pubSysZcu.getId() > 0);

		PublicationSystem ps2 = new PublicationSystem();
		ps2.setFriendlyName("Masarykova Univerzita - Prezentátor");
		ps2.setLoginNamespace("mu");
		ps2.setUrl("https://is.muni.cz/auth/prezentator/index.pl");
		ps2.setUsername(cabinetProperties.getProperty("perun.cabinet.mu.login"));
		ps2.setPassword(cabinetProperties.getProperty("perun.cabinet.mu.password"));
		ps2.setType("cz.metacentrum.perun.cabinet.strategy.impl.MUStrategy");

		pubSysMu = getCabinetManager().createPublicationSystem(sess, ps2);
		assertTrue(pubSysMu.getId() > 0);

		PublicationSystem ps3 = new PublicationSystem();
		ps3.setFriendlyName("Europe PMC");
		ps3.setLoginNamespace("europepmc");
		ps3.setUrl("https://www.ebi.ac.uk/europepmc/webservices/rest/search?");
		ps3.setType("cz.metacentrum.perun.cabinet.strategy.impl.EuropePMCStrategy");

		pubSysEuropePMC = getCabinetManager().createPublicationSystem(sess, ps3);
		assertTrue(pubSysEuropePMC.getId() > 0);

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

		publicationOne = getCabinetManager().createPublication(sess, p1);
		publicationTwo = getCabinetManager().createPublication(sess, p2);

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

		a1 = getCabinetManager().createAuthorship(sess, a1);
		a2 = getCabinetManager().createAuthorship(sess, a2);

		authorshipOne = a1;
		authorshipTwo = a2;

		init = true;

	}

}
