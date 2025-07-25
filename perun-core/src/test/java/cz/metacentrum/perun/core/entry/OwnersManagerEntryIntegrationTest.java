package cz.metacentrum.perun.core.entry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.Owner;
import cz.metacentrum.perun.core.api.OwnerType;
import cz.metacentrum.perun.core.api.OwnersManager;
import cz.metacentrum.perun.core.api.exceptions.OwnerNotExistsException;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

/**
 * Integration tests of OwnersManager.
 *
 * @author Jiri Harazim <harazim@mail.muni.cz>
 */
public class OwnersManagerEntryIntegrationTest extends AbstractPerunIntegrationTest {

  private static final String CLASS_NAME = "OwnersManager.";
  private OwnersManager ownersManagerEntry;
  private Owner createdOwner;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    ownersManagerEntry = perun.getOwnersManager();
    createdOwner = ownersManagerEntry.createOwner(sess, new Owner(0, "napoleon", "bonaparte", OwnerType.technical));
  }

  /**
   * Test method for
   * {@link cz.metacentrum.perun.core.entry.OwnersManagerEntry#createOwner(cz.metacentrum.perun.core.api.PerunSession,
   * cz.metacentrum.perun.core.api.Owner)}.
   */
  @Test
  public void testCreateOwner() throws Exception {
    System.out.println(CLASS_NAME + "createOwner");

    final String umpalumpa = "Umpalumpa";
    final String contact = "umpalumpa@johny.depp";

    final Owner owner = new Owner(0, umpalumpa, contact, OwnerType.technical);

    final Owner result = ownersManagerEntry.createOwner(sess, owner);

    assertNotNull(result);
    assertTrue(result.getId() > 0);
    assertEquals(umpalumpa, result.getName());
    assertEquals(contact, result.getContact());
  }

  /**
   * Test method for
   * {@link cz.metacentrum.perun.core.entry.OwnersManagerEntry#deleteOwner(cz.metacentrum.perun.core.api.PerunSession,
   * cz.metacentrum.perun.core.api.Owner)}.
   */
  @Test(expected = OwnerNotExistsException.class)
  public void testDeleteOwner() throws Exception {
    System.out.println(CLASS_NAME + "deleteOwner");

    ownersManagerEntry.deleteOwner(sess, createdOwner, true);

    ownersManagerEntry.getOwnerById(sess, createdOwner.getId());
  }

  @Test()
  public void testDeleteOwners() throws Exception {
    System.out.println(CLASS_NAME + "testDeleteOwners");

    Owner createdOwner2 = ownersManagerEntry.createOwner(sess, new Owner(1234, "test", "test", OwnerType.technical));
    assertEquals(2, ownersManagerEntry.getOwners(sess).size());

    ownersManagerEntry.deleteOwners(sess, List.of(createdOwner, createdOwner2), true);
    assertEquals(0, ownersManagerEntry.getOwners(sess).size());
  }

  /**
   * Test method for
   * {@link cz.metacentrum.perun.core.entry.OwnersManagerEntry#getOwnerById(cz.metacentrum.perun.core.api.PerunSession,
   * int)}.
   */
  @Test
  public void testGetOwnerById() throws Exception {
    System.out.println(CLASS_NAME + "getOwnerById");

    final Owner result = ownersManagerEntry.getOwnerById(sess, createdOwner.getId());

    assertEquals(createdOwner, result);
  }

  /**
   * Test method for
   * {@link
   * cz.metacentrum.perun.core.entry.OwnersManagerEntry#getOwnerByName(cz.metacentrum.perun.core.api.PerunSession,
   * String)}.
   */
  @Test
  public void testGetOwnerByName() throws Exception {
    System.out.println(CLASS_NAME + "getOwnerByName");

    final Owner result = ownersManagerEntry.getOwnerByName(sess, createdOwner.getName());

    assertEquals(createdOwner, result);
  }

  /**
   * Test method for
   * {@link cz.metacentrum.perun.core.entry.OwnersManagerEntry#getOwners(cz.metacentrum.perun.core.api.PerunSession)}.
   */
  @Test
  public void testGetOwners() throws Exception {
    System.out.println(CLASS_NAME + "getOwners");

    final List<Owner> owners = ownersManagerEntry.getOwners(sess);

    assertTrue(owners.contains(createdOwner));
  }

}
