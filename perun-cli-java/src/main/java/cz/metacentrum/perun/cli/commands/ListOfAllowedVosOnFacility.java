package cz.metacentrum.perun.cli.commands;

import cz.metacentrum.perun.cli.PerunCLI;
import cz.metacentrum.perun.cli.PerunCommand;
import cz.metacentrum.perun.openapi.model.Vo;
import org.apache.commons.cli.Options;
import org.springframework.web.client.RestClientException;

import java.util.Comparator;
import java.util.List;

/**
 * Prints list of VOs which are allowed to use Facility. Facility id or name and owner id are required.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
@SuppressWarnings("unused")
public class ListOfAllowedVosOnFacility extends PerunCommand {

	@Override
	public String getCommandDescription() {
		return "prints list of VOs which have a Resource on a Facility";
	}

	@Override
	public void addOptions(Options options) {
		this.addFacilityOptions(options);
		this.addSortingOptions(options, "order by vo short name");
	}

	@Override
	public void executeCommand(PerunCLI.CommandContext ctx) {
		int facilityId = this.getFacilityId(ctx, true);
		List<Vo> vos = ctx.getPerunRPC().getFacilitiesManager().getAllowedVos(facilityId);
		this.sort(ctx, vos, Comparator.comparing(Vo::getShortName));
		for (Vo vo : vos) {
			System.out.println(vo.getId() + "\t" + vo.getShortName());
		}
	}

}
