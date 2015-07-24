package cz.metacentrum.perun.cabinet.service.impl;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import cz.metacentrum.perun.cabinet.model.Publication;
import cz.metacentrum.perun.cabinet.service.CabinetException;
import cz.metacentrum.perun.cabinet.service.ErrorCodes;
import cz.metacentrum.perun.cabinet.service.ICabinetService;

public class CabinetServiceImplTest extends BaseIntegrationTest {

	private ICabinetService cabinetService;

	@Autowired
	public void setCabinetService(ICabinetService cabinetService) {
		this.cabinetService = cabinetService;
	}

	// ------------- TESTS --------------------------------------------

	@Test
	public void findPublicationsInPubSysMUTest() throws Exception {
		System.out.println("CabinetServiceImpl.findPublicationsInPubSysMUTest");

		List<Publication> pubs = new ArrayList<Publication>();
		try {
			pubs = cabinetService.findPublicationsInPubSys("39700", 2010, 2011, pubSysMu);
		} catch (CabinetException ex) {
			System.out.println(ex);
			if (!ex.getType().equals(ErrorCodes.HTTP_IO_EXCEPTION)) {
				fail("Different exception code, was: "+ex.getType() +", but expected: HTTP_IO_EXCEPTION.");
				// fail if different error
			} else {
				System.out.println("-- Test silently skipped because of HTTP_IO_EXCEPTION");
				return;
			}
		}
		assertTrue("There should be some publications returned", pubs != null && !pubs.isEmpty());

	}

	@Test
	public void findPublicationsInPubSysZCUTest() throws Exception {
		System.out.println("CabinetServiceImpl.findPublicationsInPubSysZCUTest");

		List<Publication> pubs = new ArrayList<Publication>();
		try {

			pubs = cabinetService.findPublicationsInPubSys("Sitera,Jiří", 2006, 2009, pubSysZcu);
		} catch (CabinetException ex) {
			if (!ex.getType().equals(ErrorCodes.HTTP_IO_EXCEPTION)) {
				fail("Different exception code, was: "+ex.getType() +", but expected: HTTP_IO_EXCEPTION.");
				// fail if different error
			} else {
				System.out.println("-- Test silently skipped because of HTTP_IO_EXCEPTION");
				return;
			}
		}
		assertTrue("There should be some publications returned", pubs != null && !pubs.isEmpty());

	}

}
