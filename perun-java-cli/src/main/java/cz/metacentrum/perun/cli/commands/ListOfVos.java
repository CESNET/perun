package cz.metacentrum.perun.cli.commands;

import cz.metacentrum.perun.cli.PerunCLI;
import cz.metacentrum.perun.cli.PerunCommand;
import cz.metacentrum.perun.openapi.model.Vo;
import org.apache.commons.cli.Options;

import java.util.List;

/**
 * Prints list of all VOs.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
@SuppressWarnings("unused")
public class ListOfVos extends PerunCommand {

	@Override
	public String getCommandDescription() {
		return "prints list of all VOs";
	}

	@Override
	public void addOptions(Options options) {
		this.addVoSortingOptions(options);
	}

	@Override
	public void executeCommand(PerunCLI.CommandContext ctx) {
		List<Vo> vos = ctx.getPerunRPC().getVosManager().getAllVos();
		this.sortVos(ctx, vos);
		for (Vo vo : vos) {
			System.out.println(vo.getId() + "\t" + vo.getShortName() + "\tcreated: " + vo.getCreatedAt());
		}
	}

}
