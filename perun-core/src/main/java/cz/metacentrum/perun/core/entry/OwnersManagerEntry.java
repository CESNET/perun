package cz.metacentrum.perun.core.entry;

import cz.metacentrum.perun.core.api.AuthzResolver;
import cz.metacentrum.perun.core.api.Owner;
import cz.metacentrum.perun.core.api.OwnersManager;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.OwnerAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.OwnerNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.RelationExistsException;
import cz.metacentrum.perun.core.bl.OwnersManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.core.implApi.OwnersManagerImplApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 *
 * @author Slavek Licehammer glory@ics.muni.cz
 */
public class OwnersManagerEntry implements OwnersManager {

	final static Logger log = LoggerFactory.getLogger(OwnersManagerEntry.class);

	private OwnersManagerBl ownersManagerBl;
	private PerunBl perunBl;

	/**
	 * Constructor.
	 *
	 */
	public OwnersManagerEntry(PerunBl perunBl) {
		this.perunBl = perunBl;
		this.ownersManagerBl = perunBl.getOwnersManagerBl();
	}

	public OwnersManagerEntry() {
	}

	/*FIXME delete this method */
	public OwnersManagerImplApi getOwnersManagerImpl() {
		throw new InternalErrorException("Unsupported method!");
	}

	@Override
	public Owner createOwner(PerunSession sess, Owner owner) throws InternalErrorException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
			throw new PrivilegeException(sess, "createOwner");
		}

		Utils.notNull(owner, "owner");

		return getOwnersManagerBl().createOwner(sess, owner);
	}

	@Override
	public void deleteOwner(PerunSession sess, Owner owner) throws OwnerNotExistsException, InternalErrorException, PrivilegeException, RelationExistsException, OwnerAlreadyRemovedException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
			throw new PrivilegeException(sess, "deleteOwner");
		}

		getOwnersManagerBl().checkOwnerExists(sess, owner);

		getOwnersManagerBl().deleteOwner(sess, owner);
	}

	@Override
	public void deleteOwner(PerunSession sess, Owner owner, boolean forceDelete) throws OwnerNotExistsException, InternalErrorException, PrivilegeException, RelationExistsException, OwnerAlreadyRemovedException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
			throw new PrivilegeException(sess, "deleteOwner");
		}

		getOwnersManagerBl().checkOwnerExists(sess, owner);

		getOwnersManagerBl().deleteOwner(sess, owner, forceDelete);
	}

	@Override
	public Owner getOwnerById(PerunSession sess, int id) throws OwnerNotExistsException, InternalErrorException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.SELF) &&
				!AuthzResolver.isAuthorized(sess, Role.VOADMIN) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN) &&
				!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN) &&
				!AuthzResolver.isAuthorized(sess, Role.RPC)) {
			throw new PrivilegeException(sess, "getOwnerById");
				}

		return getOwnersManagerBl().getOwnerById(sess, id);
	}

	@Override
	public List<Owner> getOwners(PerunSession sess) throws InternalErrorException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.SELF) &&
				!AuthzResolver.isAuthorized(sess, Role.VOADMIN) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN) &&
				!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN) &&
				!AuthzResolver.isAuthorized(sess, Role.ENGINE)) {
			throw new PrivilegeException(sess, "getOwners");
				}

		return getOwnersManagerBl().getOwners(sess);
	}

	/**
	 * Sets the ownersManagerBl for this instance.
	 *
	 * @param ownersManagerBl The ownersManagerBl.
	 */
	public void setOwnersManagerBl(OwnersManagerBl ownersManagerBl)
	{
		this.ownersManagerBl = ownersManagerBl;
	}

	public PerunBl getPerunBl() {
		return this.perunBl;
	}

	/**
	 * Gets the owners manager
	 *
	 * @return The ownersManagerBl.
	 */
	public OwnersManagerBl getOwnersManagerBl() {
		return this.ownersManagerBl;
	}

	/**
	 * Sets the perunBl for this instance.
	 *
	 * @param perunBl The perunBl.
	 */
	public void setPerunBl(PerunBl perunBl)
	{
		this.perunBl = perunBl;
	}


}
