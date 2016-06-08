package cz.metacentrum.perun.cabinet.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import cz.metacentrum.perun.cabinet.model.Thanks;
import cz.metacentrum.perun.cabinet.service.CabinetException;
import cz.metacentrum.perun.cabinet.service.ErrorCodes;
import cz.metacentrum.perun.cabinet.service.IThanksService;

public class ThanksServiceImplTest extends BaseIntegrationTest {

	@Autowired
	private IThanksService thanksService;

	public void setThanksService(IThanksService thanksService) {
		this.thanksService = thanksService;
	}

	// ------------- TESTS --------------------------------------------

	@Test
	public void createThanksTest() throws Exception {
		System.out.println("ThanksServiceImpl.createThanksTest");

		Thanks t = new Thanks();
		t.setCreatedBy(sess.getPerunPrincipal().getActor());
		t.setCreatedDate(new Date());
		t.setOwnerId(1);
		t.setPublicationId(publicationOne.getId());

		int id = thanksService.createThanks(sess, t);

		assertTrue("Returned ID is not > 0.", id > 0);
		assertTrue("Returned and stored ID are not same.", id == t.getId());

		/* Test for getting by pub ID */

		List<Thanks> thanks = thanksService.findThanksByPublicationId(publicationOne.getId());
		assertNotNull("No thanks returned for publicationOne", thanks);
		assertEquals("Stored and returned thanks should be equals", t, thanks.get(0));

	}

	@Test
	public void createThanksWhenExistsByIDTest() throws Exception {
		System.out.println("ThanksServiceImpl.createThanksWhenExistsByIDTest");

		Thanks t = new Thanks();
		t.setCreatedBy(sess.getPerunPrincipal().getActor());
		t.setCreatedDate(new Date());
		t.setOwnerId(1);
		t.setPublicationId(publicationOne.getId());

		thanksService.createThanks(sess, t);
		try {
			thanksService.createThanks(sess, t);
		} catch (CabinetException ex) {
			if (!ex.getType().equals(ErrorCodes.THANKS_ALREADY_EXISTS)){
				fail("Different exception was thrown when creating \"same\" Thanks: "+ex+", but expected: THANKS_ALREADY_EXISTS");
			}
		}

	}

	@Test
	public void createThanksWhenExistsByOwnerAndPublicationTest() throws Exception {
		System.out.println("ThanksServiceImpl.createThanksWhenExistsByOwnerAndPublicationTest");

		Thanks t = new Thanks();
		t.setCreatedBy(sess.getPerunPrincipal().getActor());
		t.setCreatedDate(new Date());
		t.setOwnerId(1);
		t.setPublicationId(publicationOne.getId());

		thanksService.createThanks(sess, t);
		try {
			t.setId(null); // force comparison on owner and publication
			thanksService.createThanks(sess, t);
		} catch (CabinetException ex) {
			if (!ex.getType().equals(ErrorCodes.THANKS_ALREADY_EXISTS)){
				fail("Different exception was thrown when creating \"same\" Thanks: "+ex+", but expected: THANKS_ALREADY_EXISTS");
			}
		}

	}

	@Test
	public void deleteThanksTest() throws Exception {
		System.out.println("ThanksServiceImpl.deleteThanksTest");

		Thanks t = new Thanks();
		t.setCreatedBy(sess.getPerunPrincipal().getActor());
		t.setCreatedDate(new Date());
		t.setOwnerId(1);
		t.setPublicationId(publicationOne.getId());
		int id = thanksService.createThanks(sess, t);
		assertTrue(id > 0);

		int result = thanksService.deleteThanksById(sess, t.getId());
		assertTrue("There should be exactly 1 Thanks deleted.", result == 1);

		List<Thanks> thanks = thanksService.findThanksByPublicationId(publicationOne.getId());
		assertTrue("Thanks was not deleted", thanks.size() == 0);

	}


}
