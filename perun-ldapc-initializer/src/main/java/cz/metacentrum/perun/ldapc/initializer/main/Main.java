package cz.metacentrum.perun.ldapc.initializer.main;

import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.ldapc.initializer.beans.PerunInitializer;
import cz.metacentrum.perun.ldapc.initializer.utils.Utils;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

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
		//no options means - output to stdout and without changing processed id
		String outputFile = null;
		Boolean changeProcessedId = false;

		//prepare optional command line options for ldapc initializer
		Options options = new Options();
		options.addOption(Option.builder("f").required(false).hasArg().longOpt("outputFile").desc("If set, then define output file to which will be perun ldif content file generated.").build());
		options.addOption(Option.builder("c").required(false).longOpt("changeProcessedId").desc("If set, then change processed id after finishing of content file generation.").build());
		options.addOption(Option.builder("h").required(false).longOpt("help").desc("If set, return help (always).").build());

		//parse options
		CommandLine commandLine;
		try {
			commandLine = new DefaultParser().parse(options, args);
		} catch (MissingOptionException ex) {
			System.out.println(ex.getMessage());
			System.exit(1);
			return;
		}

		//return help always if option "h" or "help" is set
		if (commandLine.hasOption("h")) {
			new HelpFormatter().printHelp("Perun ldapc-initializer:", options);
			System.exit(0);
			return;
		}

		//set path to file if option "f" is set
		if(commandLine.hasOption("f")) {
			outputFile = commandLine.getOptionValue("f");
		}

		//set changing processed id to true if option "c" is set
		if(commandLine.hasOption("c")) {
			changeProcessedId = true;
		}

		//call main with parsed options or with default settings
		new Main(outputFile, changeProcessedId);
	}


	/**
	 * Main class for purpose of generating LDIF
	 *
	 * GroupOfUniqueNames object class is not supported in new instances of perun LDAP
	 *
	 * @param fileName if not null, use file for generating. if null use stdout
	 * @param changeProcessedId if true, then change processed id for ldapc on the end of initialization, if false don't change it
	 *
	 * @throws InternalErrorException
	 */
	public Main(String fileName, Boolean changeProcessedId) throws InternalErrorException {
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
			int lastMessageBeforeInitializingData = perunInitializer.getPerunBl().getAuditer().getLastMessageId();
			System.err.println("Last message id before starting initializing: " + lastMessageBeforeInitializingData + '\n');

			try {
				Utils.generateAllVosToWriter(perunInitializer);
				Utils.generateAllGroupsToWriter(perunInitializer);
				Utils.generateAllResourcesToWriter(perunInitializer);
				Utils.generateAllUsersToWriter(perunInitializer);
			} catch (IOException ex) {
				System.err.println("Last message id before starting initializing: " + lastMessageBeforeInitializingData + '\n');
				throw new InternalErrorException(ex);
			} catch (AttributeNotExistsException | WrongAttributeAssignmentException ex) {
				System.err.println("Problem with initializing users, there is an attribute which probably not exists.");
				throw new InternalErrorException(ex);
			}

			//get last message id after initializing
			int lastMessageAfterInitializingData = perunInitializer.getPerunBl().getAuditer().getLastMessageId();
			System.err.println("Last message id after initializing: " + lastMessageAfterInitializingData + '\n');

			//This is the only operation of WRITING to the DB
			//Call RPC-LIB for this purpose
			if(changeProcessedId) {
				try {
					Utils.setLastProcessedId(perunInitializer.getPerunPrincipal(), perunInitializer.getConsumerName(), lastMessageAfterInitializingData);
				} catch (InternalErrorException | PrivilegeException ex) {
					System.err.println("Can't set last processed ID because of lack of privileges or some Internal error.");
					throw new InternalErrorException(ex);
				}
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
}
