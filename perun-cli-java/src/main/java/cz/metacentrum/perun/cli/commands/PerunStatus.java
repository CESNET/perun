package cz.metacentrum.perun.cli.commands;

import cz.metacentrum.perun.cli.PerunCLI;
import cz.metacentrum.perun.cli.PerunCommand;

/**
 * Prints status of the Perun instance.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
@SuppressWarnings("unused")
public class PerunStatus extends PerunCommand {

  @Override
  public String getCommandDescription() {
    return "prints status of the Perun instance";
  }

  @Override
  public void executeCommand(PerunCLI.CommandContext ctx) {
    for (String status : ctx.getPerunRPC().getUtils().getPerunStatus()) {
      System.out.println(status);
    }
  }
}
