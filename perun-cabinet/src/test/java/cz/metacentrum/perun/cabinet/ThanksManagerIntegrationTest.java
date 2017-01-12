package cz.metacentrum.perun.cabinet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Date;
import java.util.List;

import cz.metacentrum.perun.cabinet.CabinetBaseIntegrationTest;
import org.junit.Test;

import cz.metacentrum.perun.cabinet.model.Thanks;
import cz.metacentrum.perun.cabinet.bl.CabinetException;
import cz.metacentrum.perun.cabinet.bl.ErrorCodes;

public class ThanksManagerIntegrationTest extends CabinetBaseIntegrationTest {

	@Test
	public void createThanksTest() throws Exception {
		System.out.println("ThanksServiceImpl.createThanksTest");

		Thanks t = new Thanks();
		t.setCreatedBy(sess.getPerunPrincipal().getActor());
		t.setCreatedDate(new Date());
		t.setOwnerId(1);
		t.setPublicationId(publicationOne.getId());

		t = getCabinetManager().createThanks(sess, t);
		assertTrue(t != null);

		/* Test for getting by pub ID */

		List<Thanks> thanks = getCabinetManager().getThanksByPublicationId(publicationOne.getId());
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

		getCabinetManager().createThanks(sess, t);
		try {
			getCabinetManager().createThanks(sess, t);
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

		getCabinetManager().createThanks(sess, t);
		try {
			getCabinetManager().createThanks(sess, t);
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
		t = getCabinetManager().createThanks(sess, t);

		assertTrue(t != null);

		getCabinetManager().deleteThanks(sess, t);

		List<Thanks> thanks = getCabinetManager().getThanksByPublicationId(publicationOne.getId());
		assertTrue("Thanks was not deleted", thanks.size() == 0);

	}

}
