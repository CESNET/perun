package cz.metacentrum.perun.cli.commands;

import cz.metacentrum.perun.cli.PerunCLI;
import cz.metacentrum.perun.cli.PerunCommand;
import cz.metacentrum.perun.openapi.model.Facility;
import cz.metacentrum.perun.openapi.model.Owner;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

/**
 * Prints owners of facilities having the specified destination.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
@SuppressWarnings("unused")
public class GetDestinationOwners extends PerunCommand {

  @Override
  public String getCommandDescription() {
    return "prints owners of facilities having the specified destination";
  }

  @Override
  public void addOptions(Options options) {
    options.addOption(
        Option.builder("d").required(true).hasArg(true).longOpt("destination").desc("destination name").build());
    options.addOption(Option.builder("c").required(false).hasArg(false).longOpt("convertToLowerCase")
        .desc("convert destination name to lowercase").build());
  }

  @Override
  public void executeCommand(PerunCLI.CommandContext ctx) {
    String destination = ctx.getCommandLine().getOptionValue("d");
    if (ctx.getCommandLine().hasOption("c")) {
      destination = destination.toLowerCase();
    }
    System.out.println("Destination '" + destination + "' is used by facilities:");
    System.out.println();
    for (Facility facility : ctx.getPerunRPC().getFacilitiesManager().getFacilitiesByDestination(destination)) {
      String desc = facility.getDescription();
      System.out.println("facility: " + facility.getId() + " " + facility.getName() +
          (desc != null && !desc.trim().isEmpty() ? " \"" + desc + "\"" : ""));
      for (Owner owner : ctx.getPerunRPC().getFacilitiesManager().getFacilityOwners(facility.getId())) {
        System.out.println("  " + owner.getType() + " owner: " + owner.getName() + " " + owner.getContact());
      }
    }
  }

}
