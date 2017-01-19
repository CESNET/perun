package cz.metacentrum.perun.cabinet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Date;
import java.util.List;

import cz.metacentrum.perun.cabinet.model.ThanksForGUI;
import org.junit.Test;

import cz.metacentrum.perun.cabinet.model.Thanks;
import cz.metacentrum.perun.cabinet.bl.CabinetException;
import cz.metacentrum.perun.cabinet.bl.ErrorCodes;

/**
 * Integration tests of AuthorshipManager
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class ThanksManagerIntegrationTest extends CabinetBaseIntegrationTest {

	@Test
	public void createThanksTest() throws Exception {
		System.out.println("ThanksManagerIntegrationTest.createThanksTest");

		Thanks t = new Thanks();
		t.setCreatedBy(sess.getPerunPrincipal().getActor());
		t.setCreatedDate(new Date());
		t.setOwnerId(owner.getId());
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
		System.out.println("ThanksManagerIntegrationTest.createThanksWhenExistsByIDTest");

		Thanks t = new Thanks();
		t.setCreatedBy(sess.getPerunPrincipal().getActor());
		t.setCreatedDate(new Date());
		t.setOwnerId(owner.getId());
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
		System.out.println("ThanksManagerIntegrationTest.createThanksWhenExistsByOwnerAndPublicationTest");

		Thanks t = new Thanks();
		t.setCreatedBy(sess.getPerunPrincipal().getActor());
		t.setCreatedDate(new Date());
		t.setOwnerId(owner.getId());
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
		System.out.println("ThanksManagerIntegrationTest.deleteThanksTest");

		Thanks t = new Thanks();
		t.setCreatedBy(sess.getPerunPrincipal().getActor());
		t.setCreatedDate(new Date());
		t.setOwnerId(owner.getId());
		t.setPublicationId(publicationOne.getId());
		t = getCabinetManager().createThanks(sess, t);

		assertTrue(t != null);

		getCabinetManager().deleteThanks(sess, t);

		List<Thanks> thanks = getCabinetManager().getThanksByPublicationId(publicationOne.getId());
		assertTrue("Thanks was not deleted", thanks.size() == 0);

	}

	@Test
	public void getThanksByIdTest() throws Exception {
		System.out.println("ThanksManagerIntegrationTest.getThanksByIdTest");

		Thanks t = new Thanks();
		t.setCreatedBy(sess.getPerunPrincipal().getActor());
		t.setCreatedDate(new Date());
		t.setOwnerId(owner.getId());
		t.setPublicationId(publicationOne.getId());

		t = getCabinetManager().createThanks(sess, t);
		assertTrue(t != null);

		Thanks thanks = getCabinetManager().getThanksById(t.getId());
		assertNotNull("No thanks returned for ID", thanks);
		assertEquals("Stored and returned thanks should be equals", t, thanks);

	}

	@Test
	public void getThanksByPublicationIdTest() throws Exception {
		System.out.println("ThanksManagerIntegrationTest.getThanksByPublicationIdTest");

		Thanks t = new Thanks();
		t.setCreatedBy(sess.getPerunPrincipal().getActor());
		t.setCreatedDate(new Date());
		t.setOwnerId(owner.getId());
		t.setPublicationId(publicationOne.getId());

		t = getCabinetManager().createThanks(sess, t);
		assertTrue(t != null);

		List<Thanks> thanks = getCabinetManager().getThanksByPublicationId(publicationOne.getId());
		assertNotNull("No thanks returned for publicationOne", thanks);
		assertEquals("Stored and returned thanks should be equals", t, thanks.get(0));

	}

	@Test
	public void getRichThanksByPublicationIdTest() throws Exception {
		System.out.println("ThanksManagerIntegrationTest.getRichThanksByPublicationIdTest");

		Thanks t = new Thanks();
		t.setCreatedBy(sess.getPerunPrincipal().getActor());
		t.setCreatedDate(new Date());
		t.setOwnerId(owner.getId());
		t.setPublicationId(publicationOne.getId());

		t = getCabinetManager().createThanks(sess, t);
		assertTrue(t != null);

		List<ThanksForGUI> thanks = getCabinetManager().getRichThanksByPublicationId(publicationOne.getId());
		assertNotNull("No thanks returned for publicationOne", thanks);
		assertEquals("Stored and returned thanks should be equals", t.getId(), thanks.get(0).getId());

	}

	@Test
	public void getRichThanksByUserIdTest() throws Exception {
		System.out.println("ThanksManagerIntegrationTest.getRichThanksByUserIdTest");

		Thanks t = new Thanks();
		t.setCreatedBy(sess.getPerunPrincipal().getActor());
		t.setCreatedDate(new Date());
		t.setOwnerId(owner.getId());
		t.setPublicationId(publicationOne.getId());

		t = getCabinetManager().createThanks(sess, t);
		assertTrue(t != null);

		List<ThanksForGUI> thanks = getCabinetManager().getRichThanksByUserId(USER_ID);
		assertNotNull("No thanks returned for user "+USER_ID, thanks);
		assertEquals("Stored and returned thanks should be equals", t.getId(), thanks.get(0).getId());

	}

}
