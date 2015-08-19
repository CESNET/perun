package cz.metacentrum.perun.cabinet.service.impl;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import cz.metacentrum.perun.cabinet.dao.IAuthorshipDao;
import cz.metacentrum.perun.cabinet.model.Author;
import cz.metacentrum.perun.cabinet.model.Authorship;
import cz.metacentrum.perun.cabinet.service.CabinetException;
import cz.metacentrum.perun.cabinet.service.ErrorCodes;
import cz.metacentrum.perun.cabinet.service.IAuthorshipService;
import cz.metacentrum.perun.cabinet.service.SortParam;
import cz.metacentrum.perun.core.bl.PerunBl;

public class AuthorshipServiceImplTest extends BaseIntegrationTest {

	@Autowired
	private IAuthorshipService authorshipService;

	@Autowired
	PerunBl perun;

	// ------------- TESTS --------------------------------------------

	@Transactional
	@Rollback(true)
	@Test
	public void createAuthorshipTest() throws CabinetException {
		System.out.println("AuthorshipServiceImpl.createAuthorshipTest");

		Authorship r = new Authorship();
		r.setPublicationId(publicationTwo.getId()); // Because Pub 2 doesn't have authors yet
		r.setUserId(USER_ID); // ID from BaseIntegrationTest (Michal Procházka)
		r.setCreatedDate(new Date());
		r.setCreatedBy(sess.getPerunPrincipal().getActor());

		int id = authorshipService.createAuthorship(sess, r);

		assertTrue("New Authorship ID shouldn't be 0.", id > 0);
		assertTrue("Returned and Authorship ID doesn't match.", id == r.getId());

	}

	@Transactional
	@Rollback(true)
	@Test
	public void createAuthorshipWhenAlreadyExistsTest() throws CabinetException {
		System.out.println("AuthorshipServiceImpl.createAuthorshipWhenAlreadyExistsTest");

		Authorship r = new Authorship();
		r.setPublicationId(publicationOne.getId());
		r.setUserId(USER_ID); // ID from BaseIntegrationTest (Michal Procházka)
		r.setCreatedDate(new Date());
		r.setCreatedBy(sess.getPerunPrincipal().getActor());

		try {
			authorshipService.createAuthorship(sess, r);
		} catch (CabinetException ex) {
			if (!ex.getType().equals(ErrorCodes.AUTHORSHIP_ALREADY_EXISTS)) {
				fail("Different exception code, was: "+ex.getType() +", but expected: AUTHORSHIP_ALREADY_EXISTS.");
				// fail if different error
			}
		}

	}

	@Transactional
	@Rollback(true)
	@Test
	public void deleteAuthorshipByIdTest() throws CabinetException {
		System.out.println("AuthorshipServiceImpl.deleteAuthorshipByIdTest");

		// delete AuthorshipOne by Michal Procházka for PublicationOne
		int id = authorshipService.deleteAuthorshipById(sess, authorshipOne.getId());

		assertTrue("Number od deleted authorships should be exactly 1", id == 1);

		Authorship result = authorshipService.findAuthorshipById(authorshipOne.getId());
		assertTrue("Authorship ID: "+authorshipOne.getId()+" was not deleted!", result == null);

	}

	@Transactional
	@Rollback(true)
	@Test
	public void deleteAuthorshipByIdWhenNotExistsTest() throws CabinetException {
		System.out.println("AuthorshipServiceImpl.deleteAuthorshipByIdWhenNotExistsTest");

		// delete not existing authorship
		try {
			authorshipService.deleteAuthorshipById(sess, 0);
		} catch (CabinetException ex) {
			if (!ex.getType().equals(ErrorCodes.AUTHORSHIP_NOT_EXISTS)) {
				fail("Different exception code, was: "+ex.getType() +", but expected: AUTHORSHIP_NOT_EXISTS.");
				// fail if different error
			}
		}

	}

	@Transactional
	@Rollback(true)
	@Test
	public void updateAuthorshipTest() throws CabinetException {
		System.out.println("AuthorshipServiceImpl.updateAuthorshipTest");

		// transfer autohrshipOne from publicationOne to publicationTwo
		authorshipOne.setPublicationId(publicationTwo.getId());
		int result = authorshipService.updateAuthorship(sess, authorshipOne);

		assertTrue("There should be exactly 1 Authorship updated.", result == 1);

		// transfer autohrshipTwo from publicationOne to publicationTwo
		// and change also USER => such authorship should already exists !!
		authorshipTwo.setPublicationId(publicationTwo.getId());
		authorshipTwo.setUserId(USER_ID);

		try {
			authorshipService.updateAuthorship(sess, authorshipOne);
		} catch (CabinetException ex) {
			if (!ex.getType().equals(ErrorCodes.AUTHORSHIP_ALREADY_EXISTS)) {
				fail("Different exception code, was: "+ex.getType() +", but expected: AUTHORSHIP_ALREADY_EXISTS.");
				// fail if different error
			}
		}

	}

	@Test(expected=IllegalArgumentException.class)
	public void findAuthorshipsByFilterSPSQLInjectionTest() {
		System.out.println("AuthorshipServiceImpl.findAuthorshipsByFilterSPSQLInjectionTest");

		String s1 = "a0aSQLinjection_I;d";// semicolon is not allowed
		Authorship r = new Authorship();
		SortParam sp = new SortParam(0, 0, s1, false);

		authorshipService.findAuthorshipsByFilter(r, sp);
	}

	@Test
	public void findAuthorshipsByFilterSPTest() {
		System.out.println("AuthorshipServiceImpl.findAuthorshipsByFilterSPTest");

		//do not use db
		IAuthorshipDao authorshipDao = Mockito.mock(IAuthorshipDao.class);
		AuthorshipServiceImpl authorshipService = new AuthorshipServiceImpl();
		authorshipService.setAuthorshipDao(authorshipDao);

		Authorship report = new Authorship();
		SortParam sortParam = new SortParam(0, 0, "userIdColumn", false);


		authorshipService.findAuthorshipsByFilter(report, sortParam);

		SortParam sortParam2 = new SortParam(0, 0, "userIdColumn_77", false);
		authorshipService.findAuthorshipsByFilter(report, sortParam2);
	}

	public void setAuthorshipService(IAuthorshipService authorshipService) {
		this.authorshipService = authorshipService;
	}

	@Test
	@Rollback(true)
	public void authorshipExistsTest() {
		System.out.println("AuthorshipServiceImpl.authorshipExistsTest");

		// authorshipOne always exists
		boolean result = authorshipService.authorshipExists(authorshipOne);
		assertTrue("Existing Authorship doesn't exists by checkMethod (by ID).", result);

		// new authorship shouldn't exists by ID
		Authorship a = new Authorship();
		a.setId(0);
		boolean result1 = authorshipService.authorshipExists(a);
		assertFalse("Authorship with ID: 0 shouldn't exists by checkMethod (by ID).", result1);

		// authorship with USER_ID for publicationOne should always exists
		Authorship a2 = new Authorship();
		a2.setPublicationId(publicationOne.getId());
		a2.setUserId(USER_ID);

		boolean result2 = authorshipService.authorshipExists(a2);
		assertTrue("Existing Authorship doesn't exists by checkMethod (by USER_ID, PUB_ID).", result2);

		// authorship with USER_ID for publication with ID=0 shouldn't exists
		a2.setPublicationId(0);
		boolean result3 = authorshipService.authorshipExists(a2);
		assertFalse("Authorship with PUB_ID: 0 shouldn't exists by checkMethod (by USER_ID, PUB_ID).", result3);

	}

	@Test
	public void findAuthorsByAuthorshipIdTest() throws Exception {
		System.out.println("AuthorshipServiceImpl.findAuthorsByAuthorshipIdTest");

		// store existing author of authorship one
		Author a = authorService.findAuthorByUserId(USER_ID);

		List<Author> result = authorshipService.findAuthorsByAuthorshipId(sess, authorshipOne.getId());
		assertTrue("No authors found by authorship ID when there should be.", (result != null && result.size()>0));
		assertTrue("Original author should be between returned authors.", result.contains(a));

	}

	@Test
	public void findAuthorsByAuthorshipIdWhenNotExistsTest() throws Exception {
		System.out.println("AuthorshipServiceImpl.findAuthorsByAuthorshipIdWhenNotExistsTest");

		try {
			authorshipService.findAuthorsByAuthorshipId(sess, 0);
		} catch (CabinetException ex) {
			if (!ex.getType().equals(ErrorCodes.AUTHORSHIP_NOT_EXISTS)) {
				fail("Different exception code, was: "+ex.getType() +", but expected: AUTHORSHIP_NOT_EXISTS.");
				// fail if different error
			}
		}

	}

	@Test
	public void findAllAuthorshipsANDgetAuthorshipCountTest() throws Exception {
		System.out.println("AuthorshipServiceImpl.findAllAuthorshipsANDgetAuthorshipCountTest");

		List<Authorship> list = authorshipService.findAllAuthorships();
		assertTrue("Returned authorships shouldn't be null.", list != null);
		assertTrue("Count of all authorships != size of list of all authorships!", list.size() == authorshipService.getAuthorshipsCount());

	}

	@Test
	public void findAuthorshipsByPublicationIdTest() throws Exception {
		System.out.println("AuthorshipServiceImpl.findAuthorshipsByPublicationIdTest");

		List<Authorship> list = authorshipService.findAuthorshipsByPublicationId(publicationOne.getId());
		assertTrue("Returned authorships shouldn't be null.", list != null);
		assertTrue("There should be exactly 2 authors for publicationOne.", list.size() == 2);

	}

	@Test
	public void findAuthorshipsByUserIdTest() throws Exception {
		System.out.println("AuthorshipServiceImpl.findAuthorshipsByUserIdTest");

		List<Authorship> list = authorshipService.findAuthorshipsByUserId(USER_ID);
		assertTrue("Returned authorships shouldn't be null.", list != null);
		assertTrue("There should be some authorships for USER_ID.", list.size() > 0);

	}

	@Test
	public void calculateNewRankTest() throws Exception {
		System.out.println("AuthorshipServiceImpl.calculateNewRankTest");

		// calculate rank based on DB contant
		Double rank = authorshipService.calculateNewRank(USER_ID);
		// calculate lowest possible rank based on TEST DATA in DB
		Double value = 1.0;
		value += publicationOne.getRank();
		value += c1.getRank();

		assertTrue("PriorityCoefficient is smaller than it should be!", rank >= value);

	}

	@Test
	public void findUniqueAuthorsIds() {
		System.out.println("AuthorshipServiceImpl.findUniqueAuthorsIds");

		List<Integer> list = authorService.findUniqueAuthorsIds();
		assertTrue("There must be some unique authors",!list.isEmpty());

	}

}
