package cz.metacentrum.perun.cli.commands;

import cz.metacentrum.perun.cli.PerunCLI;
import cz.metacentrum.perun.cli.PerunCommand;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

/**
 * Adds an Owner to a Facility.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
@SuppressWarnings("unused")
public class AddFacilityOwner extends PerunCommand {

  @Override
  public String getCommandDescription() {
    return "adds an Owner to a Facility specified by id or name";
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
    ctx.getPerunRPC().getFacilitiesManager().addFacilityOwner(facilityId, ownerId);
    System.out.println("OK");
  }

}
