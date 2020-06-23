package cz.metacentrum.perun.cli.commands;

import cz.metacentrum.perun.cli.PerunCLI;
import cz.metacentrum.perun.cli.PerunCommand;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.springframework.web.client.RestClientException;

/**
 * Removes selected owner from the facility. Facility id or name and owner id are required.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
@SuppressWarnings("unused")
public class RemoveFacilityOwner extends PerunCommand {

	@Override
	public String getCommandDescription() {
		return "removes an Owner from a Facility specified by id or name";
	}

	@Override
	public void addOptions(Options options) {
		this.addFacilityOptions(options);
		options.addOption(Option.builder("o").required(true).hasArg(true).longOpt("ownerId").desc("owner id").build());
	}

	@Override
	public void executeCommand(PerunCLI.CommandContext ctx) {
		int facilityId = this.getFacilityId(ctx, true);
		int ownerId = Integer.parseInt(ctx.getCommandLine().getOptionValue("o"));
		ctx.getPerunRPC().getFacilitiesManager().removeFacilityOwner(facilityId, ownerId);
		System.out.println("OK");
	}

}
