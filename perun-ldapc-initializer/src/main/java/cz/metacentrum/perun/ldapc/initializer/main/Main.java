package cz.metacentrum.perun.ldapc.initializer.main;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.ldapc.initializer.api.UtilsApi;
import cz.metacentrum.perun.ldapc.initializer.beans.PerunInitializer;
import java.io.FileNotFoundException;
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

		final PerunInitializer perunInitializer;
		try {
			perunInitializer = new PerunInitializer(outputFile);
		} catch (InternalErrorException ex) {
			System.err.println("There is problem with Initializing of PerunInitializer. More info can be found in " + outputFile);
			throw ex;
		} catch (FileNotFoundException ex) {
			System.err.println("There is problem with preparing writer to file " + outputFile);
			throw new InternalErrorException(ex);
		}

		UtilsApi utils = perunInitializer.getSpringCtx().getBean(UtilsApi.class);
		utils.initializeLDAPFromPerun(perunInitializer, commandLine.hasOption("c"));
	}
}
