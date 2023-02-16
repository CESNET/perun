package cz.metacentrum.perun.cli.commands;

import cz.metacentrum.perun.cli.PerunCLI;
import cz.metacentrum.perun.cli.PerunCommand;
import cz.metacentrum.perun.openapi.model.FacilityWithAttributes;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.util.Arrays;
import java.util.List;

/**
 * Prints owners of facilities having the specified destination.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
@SuppressWarnings("unused")
public class GetFacilityByAttributeWithAttributes extends PerunCommand {

	@Override
	public String getCommandDescription() {
		return "prints attributes of facilities found by attribute value";
	}

	@Override
	public void addOptions(Options options) {
		options.addOption(Option.builder("a").required(true).hasArg(true).longOpt("attrName").desc("attribute name").build());
		options.addOption(Option.builder("v").required(true).hasArg(true).longOpt("attrValue").desc("attribute value").build());
		options.addOption(Option.builder("r").required(true).hasArg(true).longOpt("returnedAttributeNames").desc("names of returned attributes").build());
	}

	@Override
	public void executeCommand(PerunCLI.CommandContext ctx) {
		String attributeName = ctx.getCommandLine().getOptionValue("a");
		String attributeValue = ctx.getCommandLine().getOptionValue("v");
		List<String> attrNames = Arrays.asList(ctx.getCommandLine().getOptionValue("r").split(","));

		List<FacilityWithAttributes> facilities = ctx.getPerunRPC().getFacilitiesManager().getFacilitiesByAttributeWithAttributes(attributeName, attributeValue, attrNames);

		for (FacilityWithAttributes facility : facilities) {
			System.out.println(facility);
		}

	}

}
