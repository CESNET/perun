package cz.metacentrum.perun.ldapc.initializer.api;


import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.ldapc.initializer.beans.PerunInitializer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;

/**
 * Interface for Utils methods
 *
 * @author Michal Stava <stavamichal@gmail.com>
 */
public interface UtilsApi {

	/**
	 * This method will generate all data from Perun in the form of LDIF and then will insert them into the LDAP.
	 * It also can change the last processed ID for LDAPc Consumer.
	 *
	 * It is running in special
	 *
	 * @param perunInitializer need to be loaded to get all needed dependencies
	 * @param updateLastProcessedId if processed ID should be updated or not
	 * @throws InternalErrorException
	 */
	void initializeLDAPFromPerun(PerunInitializer perunInitializer, Boolean updateLastProcessedId) throws InternalErrorException;

	/**
	 * Method to set last processed id for concrete consumer
	 *
	 * @param consumerName name of consumer to set
	 * @param lastProcessedId id to set
	 * @param perunPrincipal perunPrincipal for initializing RpcCaller
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 */
	void setLastProcessedId(PerunPrincipal perunPrincipal, String consumerName, int lastProcessedId) throws InternalErrorException, PrivilegeException;

	/**
	 * Method generate all Vos to the text for using in LDIF.
	 * Write all these information to writer in perunInitializer object.
	 *
	 * @param perunInitializer need to be loaded to get all needed dependencies
	 *
	 * @throws InternalErrorException if some problem with initializer or objects in perun-core
	 * @throws IOException if some problem with writer
	 */
	void generateAllVosToWriter(PerunInitializer perunInitializer) throws InternalErrorException, IOException;


	/**
	 * Method generate all Resources to the text for using in LDIF.
	 * Write all these information to writer in perunInitializer object.
	 *
	 * @param perunInitializer need to be loaded to get all needed dependencies
	 *
	 * @throws InternalErrorException if some problem with initializer or objects in perun-core
	 * @throws IOException if some problem with writer
	 */
	void generateAllResourcesToWriter(PerunInitializer perunInitializer) throws InternalErrorException, IOException;

	/**
	 * Method generate all Groups to the text for using in LDIF.
	 * Write all these information to writer in perunInitializer object.
	 *
	 * @param perunInitializer need to be loaded to get all needed dependencies
	 *
	 * @throws InternalErrorException if some problem with initializer or objects in perun-core
	 * @throws IOException if some problem with writer
	 */
	void generateAllGroupsToWriter(PerunInitializer perunInitializer) throws InternalErrorException, IOException;

	/**
	 * Method generate all Users to the text for using in LDIF.
	 * Write all these information to writer in perunInitializer object.
	 *
	 * @param perunInitializer need to be loaded to get all needed dependencies
	 *
	 * @throws InternalErrorException if some problem with initializer or objects in perun-core
	 * @throws IOException if some problem with writer
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	void generateAllUsersToWriter(PerunInitializer perunInitializer) throws IOException, InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException;
}
