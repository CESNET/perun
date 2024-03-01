package cz.metacentrum.perun.core.entry;

import cz.metacentrum.perun.core.api.AuthzResolver;
import cz.metacentrum.perun.core.api.Owner;
import cz.metacentrum.perun.core.api.OwnersManager;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.IllegalArgumentException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.OwnerAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.OwnerNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.RelationExistsException;
import cz.metacentrum.perun.core.bl.OwnersManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.core.implApi.OwnersManagerImplApi;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Slavek Licehammer glory@ics.muni.cz
 */
public class OwnersManagerEntry implements OwnersManager {

  static final Logger LOG = LoggerFactory.getLogger(OwnersManagerEntry.class);

  private OwnersManagerBl ownersManagerBl;
  private PerunBl perunBl;

  /**
   * Constructor.
   */
  public OwnersManagerEntry(PerunBl perunBl) {
    this.perunBl = perunBl;
    this.ownersManagerBl = perunBl.getOwnersManagerBl();
  }

  public OwnersManagerEntry() {
  }

  @Override
  public Owner createOwner(PerunSession sess, Owner owner) throws PrivilegeException {
    Utils.checkPerunSession(sess);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "createOwner_Owner_policy")) {
      throw new PrivilegeException(sess, "createOwner");
    }

    if (!owner.getName().equals(owner.getName().trim())) {
      throw new IllegalArgumentException("Owner name cannot contain leading or trailing spaces");
    }
    Utils.notNull(owner, "owner");

    return getOwnersManagerBl().createOwner(sess, owner);
  }

  @Override
  public void deleteOwner(PerunSession sess, Owner owner)
      throws OwnerNotExistsException, PrivilegeException, RelationExistsException, OwnerAlreadyRemovedException {
    Utils.checkPerunSession(sess);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "deleteOwner_Owner_policy", owner)) {
      throw new PrivilegeException(sess, "deleteOwner");
    }

    getOwnersManagerBl().checkOwnerExists(sess, owner);

    getOwnersManagerBl().deleteOwner(sess, owner);
  }

  @Override
  public void deleteOwner(PerunSession sess, Owner owner, boolean forceDelete)
      throws OwnerNotExistsException, PrivilegeException, RelationExistsException, OwnerAlreadyRemovedException {
    Utils.checkPerunSession(sess);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "deleteOwner_Owner_boolean_policy", owner)) {
      throw new PrivilegeException(sess, "deleteOwner");
    }

    getOwnersManagerBl().checkOwnerExists(sess, owner);

    getOwnersManagerBl().deleteOwner(sess, owner, forceDelete);
  }

  @Override
  public void deleteOwners(PerunSession sess, List<Owner> owners, boolean forceDelete)
      throws OwnerNotExistsException, PrivilegeException, RelationExistsException, OwnerAlreadyRemovedException {
    Utils.checkPerunSession(sess);

    for (Owner owner : owners) {
      getOwnersManagerBl().checkOwnerExists(sess, owner);

      // Authorization
      if (!AuthzResolver.authorizedInternal(sess, "deleteOwner_Owner_boolean_policy", owner)) {
        throw new PrivilegeException(sess, "deleteOwners");
      }

      getOwnersManagerBl().deleteOwner(sess, owner, forceDelete);
    }
  }

  @Override
  public Owner getOwnerById(PerunSession sess, int id) throws OwnerNotExistsException, PrivilegeException {
    Utils.checkPerunSession(sess);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "getOwnerById_int_policy")) {
      throw new PrivilegeException(sess, "getOwnerById");
    }

    return getOwnersManagerBl().getOwnerById(sess, id);
  }

  @Override
  public Owner getOwnerByName(PerunSession sess, String name) throws OwnerNotExistsException, PrivilegeException {
    Utils.checkPerunSession(sess);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "getOwnerByName_String_policy")) {
      throw new PrivilegeException(sess, "getOwnerByName");
    }

    return getOwnersManagerBl().getOwnerByName(sess, name);
  }

  @Override
  public List<Owner> getOwners(PerunSession sess) throws PrivilegeException {
    Utils.checkPerunSession(sess);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "getOwners_policy")) {
      throw new PrivilegeException(sess, "getOwners");
    }

    return getOwnersManagerBl().getOwners(sess);
  }

  /**
   * Gets the owners manager
   *
   * @return The ownersManagerBl.
   */
  public OwnersManagerBl getOwnersManagerBl() {
    return this.ownersManagerBl;
  }

  /*FIXME delete this method */
  public OwnersManagerImplApi getOwnersManagerImpl() {
    throw new InternalErrorException("Unsupported method!");
  }

  public PerunBl getPerunBl() {
    return this.perunBl;
  }

  /**
   * Sets the ownersManagerBl for this instance.
   *
   * @param ownersManagerBl The ownersManagerBl.
   */
  public void setOwnersManagerBl(OwnersManagerBl ownersManagerBl) {
    this.ownersManagerBl = ownersManagerBl;
  }

  /**
   * Sets the perunBl for this instance.
   *
   * @param perunBl The perunBl.
   */
  public void setPerunBl(PerunBl perunBl) {
    this.perunBl = perunBl;
  }


}
