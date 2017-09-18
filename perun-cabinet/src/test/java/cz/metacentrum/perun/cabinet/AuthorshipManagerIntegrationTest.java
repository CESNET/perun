package cz.metacentrum.perun.cabinet;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.junit.Test;

import cz.metacentrum.perun.cabinet.model.Author;
import cz.metacentrum.perun.cabinet.model.Authorship;
import cz.metacentrum.perun.cabinet.bl.CabinetException;
import cz.metacentrum.perun.cabinet.bl.ErrorCodes;

/**
 * Integration tests of AuthorshipManager
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class AuthorshipManagerIntegrationTest extends CabinetBaseIntegrationTest {

	@Test
	public void createAuthorshipTest() throws Exception {
		System.out.println("AuthorshipManagerIntegrationTest.createAuthorshipTest");

		Authorship authorship = new Authorship();
		authorship.setPublicationId(publicationTwo.getId());
		authorship.setUserId(USER_ID);
		authorship.setCreatedDate(new Date());
		authorship.setCreatedBy(sess.getPerunPrincipal().getActor());

		authorship = getCabinetManager().createAuthorship(sess, authorship);
		assertTrue(authorship != null);
		assertTrue("New Authorship ID shouldn't be 0.", authorship.getId() > 0);

		Authorship retrievedAuthorship = getCabinetManager().getAuthorshipById(authorship.getId());
		assertEquals(authorship, retrievedAuthorship);

	}

	@Test
	public void createAuthorshipWhenAlreadyExistsTest() throws Exception {
		System.out.println("AuthorshipManagerIntegrationTest.createAuthorshipWhenAlreadyExistsTest");

		Authorship authorship = new Authorship();
		authorship.setPublicationId(publicationOne.getId());
		authorship.setUserId(USER_ID);
		authorship.setCreatedDate(new Date());
		authorship.setCreatedBy(sess.getPerunPrincipal().getActor());

		try {
			getCabinetManager().createAuthorship(sess, authorship);
		} catch (CabinetException ex) {
			if (!ex.getType().equals(ErrorCodes.AUTHORSHIP_ALREADY_EXISTS)) {
				fail("Different exception code, was: "+ex.getType() +", but expected: AUTHORSHIP_ALREADY_EXISTS.");
				// fail if different error
			}
		}

	}

	@Test
	public void deleteAuthorshipByIdTest() throws Exception {
		System.out.println("AuthorshipManagerIntegrationTest.deleteAuthorshipByIdTest");

		// delete AuthorshipOne by Michal Procházka for PublicationOne
		getCabinetManager().deleteAuthorship(sess, authorshipOne);
		try {
			getCabinetManager().getAuthorshipById(authorshipOne.getId());
		} catch (CabinetException ex) {
			if (!Objects.equals(ErrorCodes.AUTHORSHIP_NOT_EXISTS, ex.getType())) {
				throw ex;
			}
		}

	}

	@Test
	public void authorshipExistsTest() throws Exception {
		System.out.println("AuthorshipManagerIntegrationTest.authorshipExistsTest");

		// authorshipOne always exists
		boolean result = getCabinetManager().authorshipExists(authorshipOne);
		assertTrue("Existing Authorship doesn't exists by checkMethod (by ID).", result);

		// new authorship shouldn't exists by ID
		Authorship a = new Authorship();
		a.setId(0);
		boolean result1 = getCabinetManager().authorshipExists(a);
		assertFalse("Authorship with ID: 0 shouldn't exists by checkMethod (by ID).", result1);

		// authorship with USER_ID for publicationOne should always exists
		Authorship a2 = new Authorship();
		a2.setPublicationId(publicationOne.getId());
		a2.setUserId(USER_ID);

		boolean result2 = getCabinetManager().authorshipExists(a2);
		assertTrue("Existing Authorship doesn't exists by checkMethod (by USER_ID, PUB_ID).", result2);

		// authorship with USER_ID for publication with ID=0 shouldn't exists
		a2.setPublicationId(0);
		boolean result3 = getCabinetManager().authorshipExists(a2);
		assertFalse("Authorship with PUB_ID: 0 shouldn't exists by checkMethod (by USER_ID, PUB_ID).", result3);

	}

	@Test
	public void getAuthorsByAuthorshipIdTest() throws Exception {
		System.out.println("AuthorshipManagerIntegrationTest.getAuthorsByAuthorshipIdTest");

		// store existing author of authorship one
		Author a = getCabinetManager().getAuthorById(USER_ID);

		List<Author> result = getCabinetManager().getAuthorsByAuthorshipId(sess, authorshipOne.getId());
		assertTrue("No authors found by authorship ID when there should be.", (result != null && result.size()>0));
		assertTrue("Original author should be between returned authors.", result.contains(a));

	}

	@Test
	public void getAuthorsByAuthorshipIdWhenNotExistsTest() throws Exception {
		System.out.println("AuthorshipManagerIntegrationTest.getAuthorsByAuthorshipIdWhenNotExistsTest");

		try {
			getCabinetManager().getAuthorsByAuthorshipId(sess, 0);
		} catch (CabinetException ex) {
			if (!ex.getType().equals(ErrorCodes.AUTHORSHIP_NOT_EXISTS)) {
				fail("Different exception code, was: "+ex.getType() +", but expected: AUTHORSHIP_NOT_EXISTS.");
				// fail if different error
			}
		}

	}

	@Test
	public void getAuthorshipsByPublicationIdTest() throws Exception {
		System.out.println("AuthorshipManagerIntegrationTest.getAuthorshipsByPublicationIdTest");

		List<Authorship> list = getCabinetManager().getAuthorshipsByPublicationId(publicationOne.getId());
		assertTrue("Returned authorships shouldn't be null.", list != null);
		assertTrue("There should be exactly 2 authors for publicationOne.", list.size() == 2);

	}

	@Test
	public void getAuthorshipsByUserIdTest() throws Exception {
		System.out.println("AuthorshipManagerIntegrationTest.getAuthorshipsByUserIdTest");

		List<Authorship> list = getCabinetManager().getAuthorshipsByUserId(USER_ID);
		assertTrue("Returned authorships shouldn't be null.", list != null);
		assertTrue("There should be some authorships for USER_ID.", list.size() > 0);

	}

	@Test
	public void getAuthorsByPublicationIdTest() throws Exception {
		System.out.println("AuthorshipManagerIntegrationTest.getAuthorsByPublicationIdTest");

		List<Author> list = getCabinetManager().getAuthorsByPublicationId(sess, publicationOne.getId());
		assertTrue("Returned authorships shouldn't be null.", list != null);
		assertTrue("There should be exactly 2 authors for publicationOne.", list.size() == 2);

	}

	@Test
	public void getRankTest() throws Exception {
		System.out.println("AuthorshipManagerIntegrationTest.getRankTest");

		// calculate rank based on DB constant
		Double rank = getCabinetManager().getRank(USER_ID);
		// calculate lowest possible rank based on TEST DATA in DB
		Double value = 1.0;
		value += publicationOne.getRank();
		value += c1.getRank();

		assertTrue("PriorityCoefficient is smaller than it should be!", rank >= value);

	}

}
