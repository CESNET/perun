package cz.metacentrum.perun.cabinet;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

import cz.metacentrum.perun.cabinet.model.Publication;
import cz.metacentrum.perun.cabinet.bl.CabinetException;
import cz.metacentrum.perun.cabinet.bl.ErrorCodes;

public class PublicationManagerIntegrationTest extends CabinetBaseIntegrationTest {

	// ------------- TESTS --------------------------------------------

	@Test
	public void createPublicationTest() throws Exception {
		System.out.println("PublicationManagerIntegrationTest.createPublicationTest");

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

		p = getCabinetManager().createPublication(sess, p);
		assertTrue("Returned ID shouldn't be < 0.", p.getId() > 0);

	}

	@Test
	public void createInternalPublicationTest() throws Exception {
		System.out.println("PublicationManagerIntegrationTest.createInternalPublicationTest");

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

		p = getCabinetManager().createPublication(sess, p);
		assertTrue(p.getId() > 0);

		// must be reset, since test update object after creation
		p.setExternalId(0);
		p.setPublicationSystemId(0);

		// double-check existence (based on isbn)
		try {
			getCabinetManager().createPublication(sess, p);
		} catch (CabinetException ex) {
			if (!ex.getType().equals(ErrorCodes.PUBLICATION_ALREADY_EXISTS)){
				fail("Different exception was thrown when creating \"same\" internal publication: "+ex);
			}
		}

	}

	@Test
	public void getPublicationByIdOrExtIdTest() throws Exception {
		System.out.println("PublicationManagerIntegrationTest.getPublicationByIdOrExtIdTest");

		// search base on publicationOne ID
		Publication retrievedPub = getCabinetManager().getPublicationById(publicationOne.getId());
		assertTrue("Returned publications can't be null or empty.", (retrievedPub != null));
		assertTrue("Returned publication should be same as publicationOne.", Objects.equals(publicationOne, retrievedPub));

		// search base on publicationOne EXT_ID, PUB_SYS_ID
		retrievedPub = getCabinetManager().getPublicationByExternalId(publicationOne.getExternalId(), publicationOne.getPublicationSystemId());
		assertTrue("Returned publications can't be null or empty.", (retrievedPub != null));
		assertTrue("Returned publication should be same as publicationOne.", Objects.equals(publicationOne, retrievedPub));

	}

	@Test
	public void deletePublicationTest() throws Exception {
		System.out.println("PublicationManagerIntegrationTest.deletePublicationTest");

		// publicationTwo can be deleted - doesn't have authors or thanks
		getCabinetManager().deletePublication(sess, publicationTwo);

		// shouldn't find it after deletion
		try {
			getCabinetManager().getPublicationById(publicationTwo.getId());
		} catch (CabinetException ex) {
			if (!Objects.equals(ex.getType(), ErrorCodes.PUBLICATION_NOT_EXISTS)) {
				fail();
			}
		}

	}

	@Test
	public void deletePublicationWhenHaveAuthorsOrThanksTest() throws Exception {
		System.out.println("PublicationManagerIntegrationTest.deletePublicationWhenHaveAuthorsOrThanksTest");

		// publicationTwo can be deleted - doesn't have authors or thanks
		try {
			getCabinetManager().deletePublication(sess, publicationOne);
		} catch (CabinetException ex) {
			if (!ex.getType().equals(ErrorCodes.PUBLICATION_HAS_AUTHORS_OR_THANKS)) {
				fail("Different exception code, was: "+ex.getType() +", but expected: PUBLICATION_HAS_AUTHORS_OR_THANKS.");
				// fail if different error
			}
		}

	}

	@Test
	public void updatePublicationTest() throws Exception {
		System.out.println("PublicationManagerIntegrationTest.updatePublicationTest");

		publicationOne.setMain("NEW MAIN");
		getCabinetManager().updatePublication(sess, publicationOne);

		Publication pub = getCabinetManager().getPublicationById(publicationOne.getId());
		assertEquals("Returned publication should be updated.", pub, publicationOne);
		assertEquals("Returned publication should be updated.", pub.getMain(), publicationOne.getMain());

	}

	@Test
	public void updatePublicationWhenNotExistsTest() throws Exception {
		System.out.println("PublicationManagerIntegrationTest.updatePublicationWhenNotExistsTest");

		Publication pub = new Publication();
		try {
			getCabinetManager().updatePublication(sess, pub);
		} catch (CabinetException ex) {
			if (!ex.getType().equals(ErrorCodes.PUBLICATION_NOT_EXISTS)) {
				fail("Different exception code, was: "+ex.getType() +", but expected: PUBLICATION_NOT_EXISTS.");
				// fail if different error
			}
		}

	}

	@Test (expected=CabinetException.class)
		public void updatePublicationWhenCantUpdateTest() throws Exception {
			System.out.println("PublicationManagerIntegrationTest.updatePublicationWhenWhenCantUpdateTest");

			// make pub2 same as pub 1
			publicationTwo.setPublicationSystemId(publicationOne.getPublicationSystemId());
			publicationTwo.setExternalId(publicationOne.getExternalId());

			getCabinetManager().updatePublication(sess, publicationTwo);

		}

	@Test
	public void lockPublicationsTest() throws Exception {
		System.out.println("PublicationManagerIntegrationTest.lockPublicationsTest");

		List<Publication> pubs = new ArrayList<Publication>();
		pubs.add(publicationOne);
		pubs.add(publicationTwo);
		boolean lock = publicationOne.getLocked();
		getCabinetManager().lockPublications(sess, !lock, pubs); // switch lock to opposite
		Publication result = getCabinetManager().getPublicationById(publicationOne.getId());
		assertTrue("PublicationOne was not locked/unlocked", result.getLocked() != lock);
		Publication result2 = getCabinetManager().getPublicationById(publicationTwo.getId());
		assertTrue("PublicationTwo was not locked/unlocked", result2.getLocked() != lock);

	}

	@Test
	public void stripPublicationParams() throws Exception {
		System.out.println("PublicationManagerIntegrationTest.stripPublicationParams");

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

		p = getCabinetManager().createPublication(sess, p);

		// if stripping works, must have been created
		assertTrue("Returned ID shouldn't be < 0.", p.getId() > 0);

		assertTrue("Doi shouldn't be longer than 256.", p.getDoi().length()<=256);
		assertTrue("Isbn shouldn't be longer than 32.", p.getIsbn().length()<=32);
		assertTrue("Title shouldn't be longer than 1024.", p.getTitle().length()<=1024);
		assertTrue("Main shouldn't be longer than 4000.", p.getMain().length()<=4000);

	}

}
