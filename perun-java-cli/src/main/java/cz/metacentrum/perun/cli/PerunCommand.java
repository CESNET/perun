package cz.metacentrum.perun.cli;

import cz.metacentrum.perun.openapi.model.Vo;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.springframework.web.client.RestClientException;

import java.util.Comparator;
import java.util.List;

/**
 * Empty command.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public abstract class PerunCommand {

	String getName() {
		String s = getClass().getSimpleName();
		return s.substring(0, 1).toLowerCase() + s.substring(1);
	}

	public abstract String getCommandDescription();

	/**
	 * Adds command-line options.
	 *
	 * @param options options specific to this command
	 */
	public void addOptions(Options options) {
	}

	public abstract void executeCommand(PerunCLI.CommandContext ctx) throws RestClientException;

	protected void addFacilityOptions(Options options) {
		options.addOption(Option.builder("f").required(false).hasArg(true).longOpt("facilityId").desc("facility id").build());
		options.addOption(Option.builder("F").required(false).hasArg(true).longOpt("facilityName").desc("facility name").build());
	}

	protected int getFacilityId(PerunCLI.CommandContext ctx) {
		if (ctx.getCommandLine().hasOption("F")) {
			String facilityName = ctx.getCommandLine().getOptionValue("F");
			return ctx.getPerunRPC().getFacilitiesManager().getFacilityByName(facilityName).getId();
		} else if (ctx.getCommandLine().hasOption("f")) {
			return Integer.parseInt(ctx.getCommandLine().getOptionValue("f"));
		} else {
			System.err.println("ERROR: facilityId or facilityName is required");
			System.exit(1);
			return 0;
		}
	}

	protected void addVoSortingOptions(Options options) {
		options.addOption(Option.builder("n").required(false).hasArg(false).longOpt("orderByName").desc("order by short name").build());
		options.addOption(Option.builder("i").required(false).hasArg(false).longOpt("orderById").desc("order by id").build());
	}

	protected void sortVos(PerunCLI.CommandContext ctx, List<Vo> vos) {
		if (ctx.getCommandLine().hasOption("n")) {
			vos.sort(Comparator.comparing(Vo::getShortName));
		} else if (ctx.getCommandLine().hasOption("i")) {
			vos.sort(Comparator.comparing(Vo::getId));
		}
	}
}
