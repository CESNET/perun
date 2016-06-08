package cz.metacentrum.perun.ldapc.initializer.main;

import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.ldapc.initializer.beans.PerunInitializer;
import cz.metacentrum.perun.ldapc.initializer.utils.Utils;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main class for generating ldif data for initializing LDAP for perun-ldapc
 *
 * @author Michal Stava <stavamichal@gmail.com>
 */
public class Main {

	private final static Logger log = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) throws Exception {
		//Default is old version and stdout (null file)
		String fileName = null;

		if(args.length > 1 || args.length == 0) {
			System.out.println(help());
		} else if(args[0].equals("-h") || args[0].equals("--help")) {
			System.out.println(help());
		} else {
			fileName = args[0];
			Main main;
			main = new Main(fileName);
		}
	}

	/**
	 * Main class for purpose of generating LDIF
	 *
	 * GroupOfUniqueNames object class is not supported in new instances of perun LDAP
	 *
	 * @param fileName if not null, use file for generating. if null use stdout
	 * @param newLDAPversion if true, then do not use GroupOfUniqueNames object class
	 * 
	 * @throws InternalErrorException
	 */
	public Main(String fileName) throws InternalErrorException {
		PerunInitializer perunInitializer = null;
		try {
			try {
				perunInitializer = new PerunInitializer(fileName);
			} catch (InternalErrorException ex) {
				System.err.println("There is problem with Initializing of PerunInitializer. More info can be found in " + fileName);
				throw ex;
			} catch (FileNotFoundException ex) {
				System.err.println("There is problem with preparing writer to file " + fileName);
				throw new InternalErrorException(ex);
			}

			//get last message id before start of initializing
			int LastMessageBeforeInitializingData = perunInitializer.getPerunBl().getAuditer().getLastMessageId();
			System.err.println("Last message id before starting initializing: " + LastMessageBeforeInitializingData + '\n');

			try {
				Utils.generateAllVosToWriter(perunInitializer);
				Utils.generateAllGroupsToWriter(perunInitializer);
				Utils.generateAllResourcesToWriter(perunInitializer);
				Utils.generateAllUsersToWriter(perunInitializer);
			} catch (IOException ex) {
				System.err.println("Last message id before starting initializing: " + LastMessageBeforeInitializingData + '\n');
				throw new InternalErrorException(ex);
			} catch (AttributeNotExistsException | WrongAttributeAssignmentException ex) {
				System.err.println("Problem with initializing users, there is an attribute which probably not exists.");
				throw new InternalErrorException(ex);
			}

			//get last message id after initializing
			int LastMessageAfterInitializingData = perunInitializer.getPerunBl().getAuditer().getLastMessageId();
			System.err.println("Last message id after initializing: " + LastMessageAfterInitializingData + '\n');

			//This is the only operation of WRITING to the DB
			//Call RPC-LIB for this purpose
			try {
				Utils.setLastProcessedId(perunInitializer.getPerunPrincipal(), perunInitializer.getConsumerName(), LastMessageAfterInitializingData);
			} catch (InternalErrorException | PrivilegeException ex) {
				System.err.println("Can't set last processed ID because of lack of privileges or some Internal error.");
				throw new InternalErrorException(ex);
			}
		} finally {
			//Close writer if already opened
			if(perunInitializer != null) {
				try {
					perunInitializer.closeWriter();
				} catch(IOException ex) {
					System.err.println("Can't close writer by normal way.");
					throw new InternalErrorException(ex);
				}
			}
		}

		System.err.println("Generating of initializing LDIF done without error!");
	}

	/**
	 * Message with help about usage.
	 *
	 * @return message with help
	 */
	private static String help() {
		StringBuilder sb = new StringBuilder();
		sb.append("--------------HELP-------------");
		sb.append('\n');
		sb.append("-g           =>  generate ldif to stdout");
		sb.append('\n');
		sb.append("-gnew        =>  generate ldif without objectClass groupOfUniqueNames to stdout");
		sb.append('\n');
		sb.append("-g [file]    =>  generate ldif to file");
		sb.append('\n');
		sb.append("-gnew [file] =>  generate ldif without objectClass groupOfUniqueNames to file");
		sb.append('\n');
		return sb.toString();
	}
}
