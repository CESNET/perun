package cz.metacentrum.perun.cli;

import cz.metacentrum.perun.openapi.model.PerunBean;
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

	protected Integer getFacilityId(PerunCLI.CommandContext ctx, boolean required) {
		if (ctx.getCommandLine().hasOption("F")) {
			String facilityName = ctx.getCommandLine().getOptionValue("F");
			return ctx.getPerunRPC().getFacilitiesManager().getFacilityByName(facilityName).getId();
		} else if (ctx.getCommandLine().hasOption("f")) {
			return Integer.parseInt(ctx.getCommandLine().getOptionValue("f"));
		} else if(required){
			System.err.println("ERROR: facilityId or facilityName is required");
			System.exit(1);
			return 0;
		} else {
			return null;
		}
	}

	protected void addVoOptions(Options options) {
		options.addOption(Option.builder("v").required(false).hasArg(true).longOpt("voId").desc("VO id").build());
		options.addOption(Option.builder("V").required(false).hasArg(true).longOpt("voName").desc("VO short name").build());
	}

	protected Integer getVoId(PerunCLI.CommandContext ctx, boolean required) {
		if (ctx.getCommandLine().hasOption("V")) {
			String voShortName = ctx.getCommandLine().getOptionValue("V");
			return ctx.getPerunRPC().getVosManager().getVoByShortName(voShortName).getId();
		} else if (ctx.getCommandLine().hasOption("v")) {
			return Integer.parseInt(ctx.getCommandLine().getOptionValue("v"));
		} else if(required){
			System.err.println("ERROR: voId or voName is required");
			System.exit(1);
			return 0;
		} else {
			return null;
		}
	}

	protected void addServiceOptions(Options options) {
		options.addOption(Option.builder("s").required(false).hasArg(true).longOpt("serviceId").desc("service id").build());
		options.addOption(Option.builder("S").required(false).hasArg(true).longOpt("serviceName").desc("service name").build());
	}

	protected Integer getServiceId(PerunCLI.CommandContext ctx, boolean required) {
		if (ctx.getCommandLine().hasOption("S")) {
			String serviceName = ctx.getCommandLine().getOptionValue("S");
			return ctx.getPerunRPC().getServicesManager().getServiceByName(serviceName).getId();
		} else if (ctx.getCommandLine().hasOption("s")) {
			return Integer.parseInt(ctx.getCommandLine().getOptionValue("s"));
		} else if(required){
			System.err.println("ERROR: serviceId or serviceName is required");
			System.exit(1);
			return 0;
		} else {
			return null;
		}
	}

	protected void addSortingOptions(Options options, String orderByNameDescription) {
		options.addOption(Option.builder("n").required(false).hasArg(false).longOpt("orderByName").desc(orderByNameDescription).build());
		options.addOption(Option.builder("i").required(false).hasArg(false).longOpt("orderById").desc("order by id").build());
	}

	protected void sort(PerunCLI.CommandContext ctx, List<? extends PerunBean> perunBeans, Comparator comparator) {
		if (ctx.getCommandLine().hasOption("i")) {
			perunBeans.sort(Comparator.comparing(PerunBean::getId));
		} else if (ctx.getCommandLine().hasOption("n")) {
			perunBeans.sort(comparator);
		}
	}
}
