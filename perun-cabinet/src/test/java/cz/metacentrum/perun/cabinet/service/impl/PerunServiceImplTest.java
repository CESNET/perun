package cz.metacentrum.perun.cabinet.service.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import cz.metacentrum.perun.core.api.OwnerType;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import cz.metacentrum.perun.core.api.Owner;
import cz.metacentrum.perun.cabinet.model.Author;
import cz.metacentrum.perun.cabinet.service.CabinetException;
import cz.metacentrum.perun.cabinet.service.IAuthorService;
import cz.metacentrum.perun.cabinet.service.IPerunService;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.bl.PerunBl;

public class PerunServiceImplTest extends BaseIntegrationTest {

	@Autowired protected IPerunService perunService;
	@Autowired protected IAuthorService authorService;
	@Autowired protected PerunBl perun;

	@Test
	public void findOwnersIntegrationTest() throws InternalErrorException, PrivilegeException, CabinetException {
		System.out.println("PerunServiceImpl.findOwnersIntegrationTest");

		Owner owner = new Owner();
		owner.setName("cabinetOwner");
		owner.setContact("cabinet@owner.cz");
		owner.setType(OwnerType.administrative);
		owner = perun.getOwnersManager().createOwner(sess, owner);

		List<Owner> owners = perunService.findAllOwners(sess);

		assertNotNull(owners);
		assertTrue(owners.size() > 0);
		assertTrue(owners.contains(owner));

	}

	@Test
	public void findUserByNameIntegrationTest() throws Exception {
		System.out.println("PerunServiceImpl.findLoginNamespacesByUserIntegrationTest");

		List<User> users = perun.getUsersManager().findUsersByName(sess, "", "", "", "cabinetTestUser", "");
		assertNotNull(users);

	}

	@Test
	public void findAuthorById() throws Exception {
		System.out.println("PerunServiceImpl.findAuthorById");

		Author auth = authorService.findAuthorByUserId(USER_ID);
		assertNotNull(auth);

	}

	@Test
	public void getAuthorsCount() throws Exception {
		System.out.println("PerunServiceImpl.getAuthorsCount");

		int count = perunService.getUsersCount(sess);
		assertTrue("Authors count should be > 0", count > 0);

	}

	// setters -------------------------------------------

	public void setPerunService(IPerunService perunService) {
		this.perunService = perunService;
	}

	public void setAuthorService(IAuthorService authorService) {
		this.authorService = authorService;
	}

}
