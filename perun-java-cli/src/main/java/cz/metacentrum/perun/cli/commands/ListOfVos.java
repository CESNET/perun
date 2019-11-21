package cz.metacentrum.perun.cli.commands;

import cz.metacentrum.perun.cli.PerunCLI;
import cz.metacentrum.perun.cli.PerunCommand;
import cz.metacentrum.perun.openapi.model.Vo;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.util.Comparator;
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
		options.addOption(Option.builder("n").required(false).hasArg(false).longOpt("orderByName").desc("order by short name").build());
		options.addOption(Option.builder("i").required(false).hasArg(false).longOpt("orderById").desc("order by id").build());
	}

	@Override
	public void executeCommand(PerunCLI.CommandContext ctx) {
		List<Vo> allVos = ctx.getPerunRPC().getVosManager().getAllVos();
		if (ctx.getCommandLine().hasOption("n")) {
			allVos.sort(Comparator.comparing(Vo::getShortName));
		} else if (ctx.getCommandLine().hasOption("i")) {
			allVos.sort(Comparator.comparing(Vo::getId));
		}
		for (Vo vo : allVos) {
			System.out.println(vo.getId() + "\t" + vo.getShortName() + "\tcreated: " + vo.getCreatedAt());
		}

	}

}
