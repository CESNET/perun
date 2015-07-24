package cz.metacentrum.perun.cabinet.service.impl;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import cz.metacentrum.perun.cabinet.model.Publication;
import cz.metacentrum.perun.cabinet.service.CabinetException;
import cz.metacentrum.perun.cabinet.service.ErrorCodes;
import cz.metacentrum.perun.cabinet.service.IPublicationService;

public class PublicationServiceImplTest extends BaseIntegrationTest {

	@Autowired
	private IPublicationService publicationService;

	public void setPublicationService(IPublicationService publicationService) {
		this.publicationService = publicationService;
	}

	// ------------- TESTS --------------------------------------------

	@Test
	public void createPublicationTest() throws CabinetException {
		System.out.println("PublicationServiceImpl.createPublicationTest");

		Publication p = new Publication();
		p.setCategoryId(publicationOne.getCategoryId());
		p.setCreatedBy(sess.getPerunPrincipal().getActor()); // Pepa id 10, userId 1
		p.setCreatedDate(new Date());
		p.setExternalId(999);
		p.setIsbn("isbn 123-4556-899");
		p.setMain("KERBEROS main zaznam.");
		p.setPublicationSystemId(publicationOne.getPublicationSystemId()); //MU
		p.setTitle("Kerberos");
		p.setYear(2010);
		p.setRank(0.0);
		p.setLocked(false);
		p.setDoi("DOI");
		p.setCreatedByUid(sess.getPerunPrincipal().getUserId());

		int id = publicationService.createPublication(sess, p);

		assertTrue("ID of stored and returned Publication doesn't match.", id == p.getId());
		assertTrue("Returned ID shouldn't be < 0.", id > 0);

	}

	@Test
	public void createInternalPublicationTest() throws CabinetException {
		System.out.println("PublicationServiceImpl.createInternalPublicationTest");

		Publication p = new Publication();
		p.setCategoryId(publicationOne.getCategoryId());
		p.setCreatedBy(sess.getPerunPrincipal().getActor()); // Pepa id 10, userId 1
		p.setCreatedDate(new Date());
		p.setExternalId(0); // INTERNAL
		p.setIsbn("isbn 123-4556-899");
		p.setMain("KERBEROS main zaznam.");
		p.setPublicationSystemId(0); // INTERNAL
		p.setTitle("Kerberos");
		p.setYear(2010);
		p.setRank(0.0);
		p.setLocked(false);
		p.setDoi("DOI");
		p.setCreatedByUid(sess.getPerunPrincipal().getUserId());

		int id = publicationService.createPublication(sess, p);
		assertTrue(id > 0);

		// must be reset, since test update object after creation
		p.setExternalId(0);
		p.setPublicationSystemId(0);

		// double-check existence (based on isbn)
		try {
			publicationService.createPublication(sess, p);
		} catch (CabinetException ex) {
			if (!ex.getType().equals(ErrorCodes.PUBLICATION_ALREADY_EXISTS)){
				fail("Different exception was thrown when creating \"same\" internal publication: "+ex);
			}
		}

	}

	@Test
	public void findPublicationsByFilterTest() throws Exception {
		System.out.println("PublicationServiceImpl.findPublicationsByFilterTest");

		// search base on publicationOne ID
		Publication pub = new Publication();
		pub.setId(publicationOne.getId());
		List<Publication> list = publicationService.findPublicationsByFilter(pub);
		assertTrue("Returned publications can't be null or empty.", (list != null && !list.isEmpty()));
		assertTrue("There should be exactly 1 publication returned.", list.size() == 1);
		assertTrue("Returned publication shoud be same as publicationOne.", list.contains(publicationOne));

		// search base on publicationOne EXT_ID, PUBSYS_ID
		pub = new Publication();
		pub.setExternalId(publicationOne.getExternalId());
		pub.setPublicationSystemId(publicationOne.getPublicationSystemId());
		list = publicationService.findPublicationsByFilter(pub);
		assertTrue("Returned publications can't be null or empty.", (list != null && !list.isEmpty()));
		assertTrue("There should be exactly 1 publication returned.", list.size() == 1);
		assertTrue("Returned publication should be same as publicationOne.", list.contains(publicationOne));

	}

	@Test
	public void getPublicationsCountTest() throws Exception {
		System.out.println("PublicationServiceImpl.getPublicationsCountTest");

		int result = publicationService.getPublicationsCount();
		assertTrue("There should be at least 2 testing publications!", result >= 2);


	}

	@Test
	public void deletePublicationTest() throws CabinetException {
		System.out.println("PublicationServiceImpl.deletePublicationTest");

		// publicationTwo can be deleted - doesn't have authors or thanks
		int id = publicationService.deletePublicationById(sess, publicationTwo.getId());
		assertTrue("There should be exactly 1 row deleted.",id == 1);

		// shouldn't find it after deletion
		Publication result = publicationService.findPublicationById(publicationTwo.getId());
		assertTrue("PublicationTwo was not deleted!", result == null);

	}

	@Test
	public void deletePublicationWhenNotExistsTest() throws CabinetException {
		System.out.println("PublicationServiceImpl.deletePublicationWhenNotExistsTest");

		try {
			publicationService.deletePublicationById(sess, 0);
		} catch (CabinetException ex) {
			if (ex.getType() != ErrorCodes.PUBLICATION_NOT_EXISTS) {
				// fail if different error
				fail();
			}
		}

	}

	@Test
	public void deletePublicationWhenHaveAuthorsOrThanksTest() throws CabinetException {
		System.out.println("PublicationServiceImpl.deletePublicationWhenHaveAuthorsOrThanksTest");

		// publicationTwo can be deleted - doesn't have authors or thanks
		try {
			publicationService.deletePublicationById(sess, publicationOne.getId());
		} catch (CabinetException ex) {
			if (!ex.getType().equals(ErrorCodes.PUBLICATION_HAS_AUTHORS_OR_THANKS)) {
				fail("Different exception code, was: "+ex.getType() +", but expected: PUBLICATION_HAS_AUTHORS_OR_THANKS.");
				// fail if different error
			}
		}

	}

	@Test
	public void updatePublicationTest() throws CabinetException {
		System.out.println("PublicationServiceImpl.updatePublicationTest");

		publicationOne.setMain("NEW MAIN");
		int result = publicationService.updatePublicationById(sess, publicationOne);
		assertTrue("There should be exactly 1 publication updated.", result == 1);

		Publication pub = publicationService.findPublicationById(publicationOne.getId());
		assertEquals("Returned publication should be updated.", pub, publicationOne);

	}

	@Test
	public void updatePublicationWhenNotExistsTest() throws CabinetException {
		System.out.println("PublicationServiceImpl.updatePublicationWhenNotExistsTest");

		Publication pub = new Publication();
		try {
			publicationService.updatePublicationById(sess, pub);
		} catch (CabinetException ex) {
			if (!ex.getType().equals(ErrorCodes.PUBLICATION_NOT_EXISTS)) {
				fail("Different exception code, was: "+ex.getType() +", but expected: PUBLICATION_NOT_EXISTS.");
				// fail if different error
			}
		}

	}

	@Test (expected=CabinetException.class)
		public void updatePublicationWhenCantUpdateTest() throws CabinetException {
			System.out.println("PublicationServiceImpl.updatePublicationWhenWhenCantUpdateTest");

			// make pub2 same as pub 1
			publicationTwo.setPublicationSystemId(publicationOne.getPublicationSystemId());
			publicationTwo.setExternalId(publicationOne.getExternalId());

			publicationService.updatePublicationById(sess, publicationTwo);

		}

	@Test
	public void lockPublicationsTest() throws CabinetException {
		System.out.println("PublicationServiceImpl.lockPublicationsTest");

		List<Publication> pubs = new ArrayList<Publication>();
		pubs.add(publicationOne);
		pubs.add(publicationTwo);
		boolean lock = publicationOne.getLocked();
		publicationService.lockPublications(sess, !lock, pubs); // switch lock to opposite
		Publication result = publicationService.findPublicationById(publicationOne.getId());
		assertTrue("PublicationOne was not locked/unlocked", result.getLocked() != lock);
		Publication result2 = publicationService.findPublicationById(publicationTwo.getId());
		assertTrue("PublicationTwo was not locked/unlocked", result2.getLocked() != lock);

	}

	@Test
	public void stripPublicationParams() throws CabinetException {
		System.out.println("PublicationServiceImpl.stripPublicationParams");

		Publication p = new Publication();
		p.setCategoryId(publicationOne.getCategoryId());
		p.setCreatedBy(sess.getPerunPrincipal().getActor());
		p.setCreatedDate(new Date());
		p.setExternalId(999);
		p.setPublicationSystemId(publicationOne.getPublicationSystemId()); //MU
		p.setYear(2010);
		p.setRank(0.0);
		p.setLocked(false);

		// set long params

		String title = RandomStringUtils.random(1100, true, false);
		String main = RandomStringUtils.random(4020, true, false);
		String isbn = RandomStringUtils.random(40, true, false);
		String doi = RandomStringUtils.random(300, true, false);

		p.setIsbn(isbn);
		p.setMain(main);
		p.setTitle(title);
		p.setDoi(doi);
		p.setCreatedByUid(sess.getPerunPrincipal().getUserId());

		int id = publicationService.createPublication(sess, p);

		// if stripping works, must have been created
		assertTrue("ID of stored and returned Publication doesn't match.", id == p.getId());
		assertTrue("Returned ID shouldn't be < 0.", id > 0);

		assertTrue("Doi shouldn't be longer than 256.", p.getDoi().length()<=256);
		assertTrue("Isbn shouldn't be longer than 32.", p.getIsbn().length()<=32);
		assertTrue("Title shouldn't be longer than 1024.", p.getTitle().length()<=1024);
		assertTrue("Main shouldn't be longer than 4000.", p.getMain().length()<=4000);

	}

}
