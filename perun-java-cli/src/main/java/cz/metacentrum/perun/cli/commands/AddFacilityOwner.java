package cz.metacentrum.perun.cli.commands;

import cz.metacentrum.perun.cli.PerunCLI;
import cz.metacentrum.perun.cli.PerunCommand;
import cz.metacentrum.perun.openapi.PerunException;
import cz.metacentrum.perun.openapi.model.Facility;
import cz.metacentrum.perun.openapi.model.Owner;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import java.io.IOException;

/**
 * Adds an Owner to a Facility.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
@SuppressWarnings("unused")
public class AddFacilityOwner extends PerunCommand {

	@Override
	public String getCommandDescription() {
		return "Adds an Owner to a Facility.";
	}

	@Override
	public void addOptions(Options options) {
		options.addOption(Option.builder("f").required(true).hasArg(true).longOpt("facilityId").desc("id of facility").build());
		options.addOption(Option.builder("o").required(true).hasArg(true).longOpt("ownerId").desc("id of owner").build());
	}

	@Override
	public void executeCommand(PerunCLI.CommandContext ctx) throws RestClientException {
		int facilityId = Integer.parseInt(ctx.getCommandLine().getOptionValue("f"));
		int ownerId = Integer.parseInt(ctx.getCommandLine().getOptionValue("o"));
    	ctx.getPerunRPC().getFacilitiesManager().addFacilityOwner(facilityId, ownerId);
		System.out.println("OK");
	}

}
