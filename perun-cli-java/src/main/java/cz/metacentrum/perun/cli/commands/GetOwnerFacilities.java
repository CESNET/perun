package cz.metacentrum.perun.cli.commands;

import cz.metacentrum.perun.cli.PerunCLI;
import cz.metacentrum.perun.cli.PerunCommand;
import cz.metacentrum.perun.openapi.model.Facility;
import cz.metacentrum.perun.openapi.model.Owner;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.springframework.web.client.RestClientException;

import java.util.List;

/**
 * Lists Facilites owned by an Owner.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
@SuppressWarnings("unused")
public class GetOwnerFacilities extends PerunCommand {

	@Override
	public String getCommandDescription() {
		return "lists Facilities owned by an Owner";
	}

	@Override
	public void addOptions(Options options) {
		options.addOption(Option.builder("o").required(true).hasArg(true).longOpt("ownerId").desc("owner id").build());
	}

	@Override
	public void executeCommand(PerunCLI.CommandContext ctx) {
		int ownerId = Integer.parseInt(ctx.getCommandLine().getOptionValue("o"));
		Owner owner = ctx.getPerunRPC().getOwnersManager().getOwnerById(ownerId);
		List<Facility> facilities = ctx.getPerunRPC().getFacilitiesManager().getOwnerFacilities(ownerId);
		System.out.println("Facilities owned by owner "+owner.getId()+" "+owner.getName()+" "+owner.getContact()+" "+owner.getType());
		System.out.println("=====================================================================================");
		for (Facility facility : facilities) {
			System.out.printf("%5d %40s %-40s%n", facility.getId(), facility.getName(), facility.getDescription());
		}
	}

}
