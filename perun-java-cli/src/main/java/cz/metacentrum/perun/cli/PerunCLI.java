package cz.metacentrum.perun.cli;

import cz.metacentrum.perun.openapi.PerunRPC;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.kerberos.client.KerberosRestTemplate;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class PerunCLI {

	private static final Logger log = LoggerFactory.getLogger(PerunCLI.class);

	private static final String PERUN_URL_OPTION = "U";
	private static final String PERUN_URL_VARIABLE = "PERUN_URL";
	private static final String PERUN_USER_OPTION = "P";
	private static final String PERUN_USER_VARIABLE = "PERUN_USER";

	public static void main(String[] args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, ParseException {
		//find all classes implementing commands and put them into the "commands" variable
		log.debug("finding available commands...");
		Reflections reflections = new Reflections("cz.metacentrum.perun.cli.commands");
		List<Class<? extends PerunCommand>> classes = new ArrayList<>(reflections.getSubTypesOf(PerunCommand.class));
		classes.sort(Comparator.comparing(Class::getSimpleName));
		List<PerunCommand> commands = new ArrayList<>(classes.size());
		for (Class<? extends PerunCommand> aClass : classes) {
			commands.add(aClass.getDeclaredConstructor().newInstance());
		}

		//if no arguments specified, print list of available commands
		if (args.length == 0) {
			System.err.println();
			System.err.println("Usage: <command> <options>");
			System.err.println();
			System.err.println("run a command without options to see a list of its available options");
			System.err.println();
			System.err.println("available commands:");
			for (PerunCommand command : commands) {
				System.err.println("  " + command.getClass().getSimpleName() + " ... " + command.getCommandDescription());
			}
			System.exit(1);
		}
		//call the command from class specified as first argument
		String[] options = args.length == 1 ? new String[]{} : Arrays.copyOfRange(args, 1, args.length);
		for (PerunCommand command : commands) {
			if (command.getClass().getSimpleName().equals(args[0])) {
				call(command, options);
				return;
			}
		}
		System.err.println("Command not recognized: " + args[0]);
	}

	private static void call(PerunCommand command, String[] cliArgs) throws ParseException {
		//prepare CLI options
		//first options common to all commands
		Options options = new Options();
		options.addOption(Option.builder(PERUN_URL_OPTION).required(false).hasArg().longOpt(PERUN_URL_VARIABLE).desc("Perun base URL").build());
		options.addOption(Option.builder(PERUN_USER_OPTION).required(false).hasArg().longOpt(PERUN_USER_VARIABLE).desc("HTTP Basic Auth user/password").build());
		//then options specific to the command
		command.addOptions(options);

		//parse options
		CommandLine commandLine;
		try {
			commandLine = new DefaultParser().parse(options, cliArgs);
		} catch (MissingOptionException ex) {
			System.err.println();
			new HelpFormatter().printHelp(command.getClass().getSimpleName(), options);
			System.exit(1);
			return;
		}

		// find URL
		String perunUrl = System.getenv(PERUN_URL_VARIABLE);
		if (commandLine.hasOption(PERUN_URL_OPTION)) {
			perunUrl = commandLine.getOptionValue(PERUN_URL_OPTION);
		}
		if (perunUrl == null) perunUrl = "https://perun.cesnet.cz/krb/rpc";

		// find user and password
		String user = System.getenv(PERUN_USER_VARIABLE);
		if (commandLine.hasOption(PERUN_USER_OPTION)) {
			user = commandLine.getOptionValue(PERUN_USER_OPTION);
		}

		PerunRPC perunRPC;
		if (user == null) {
			perunRPC = new PerunRPC(perunUrl, null, null, new KerberosRestTemplate(null, "-"));
		} else {
			int slash = user.indexOf('/');
			if (slash == -1) {
				System.err.println("the username and password must be separated by the '/' character");
				System.exit(1);
			}
			String username = user.substring(0, slash);
			String password = user.substring(slash + 1);
			perunRPC = new PerunRPC(perunUrl, username, password);
		}

		//execute the command
		command.executeCommand(new CommandContext(perunRPC, commandLine));
	}

	public static class CommandContext {

		private final PerunRPC perunRPC;
		private final CommandLine commandLine;

		CommandContext(PerunRPC perunRPC, CommandLine commandLine) {
			this.perunRPC = perunRPC;
			this.commandLine = commandLine;
		}

		public CommandLine getCommandLine() {
			return commandLine;
		}

		public PerunRPC getPerunRPC() {
			return perunRPC;
		}
	}
}
