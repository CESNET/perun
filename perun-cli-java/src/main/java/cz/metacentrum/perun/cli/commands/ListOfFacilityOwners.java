package cz.metacentrum.perun.cli.commands;

import cz.metacentrum.perun.cli.PerunCLI;
import cz.metacentrum.perun.cli.PerunCommand;
import cz.metacentrum.perun.openapi.model.Owner;
import org.apache.commons.cli.Options;
import org.springframework.web.client.RestClientException;

import java.util.Comparator;
import java.util.List;

/**
 * Prints list of facility Owners.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
@SuppressWarnings("unused")
public class ListOfFacilityOwners extends PerunCommand {

	@Override
	public String getCommandDescription() {
		return "prints list of facility Owners";
	}

	@Override
	public void addOptions(Options options) {
		this.addFacilityOptions(options);
		this.addSortingOptions(options,"order by owner name");
	}

	@Override
	public void executeCommand(PerunCLI.CommandContext ctx) {
		int facilityId = this.getFacilityId(ctx, true);
		List<Owner> owners = ctx.getPerunRPC().getFacilitiesManager().getFacilityOwners(facilityId);
		this.sort(ctx, owners, Comparator.comparing(Owner::getName));
		for (Owner owner : owners) {
			System.out.println(owner.getId() + "\t" + owner.getType() + "\t" + owner.getName() + "\t" + owner.getContact());
		}
	}

}
