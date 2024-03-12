package cz.metacentrum.perun.cli.commands;

import cz.metacentrum.perun.cli.PerunCLI;
import cz.metacentrum.perun.cli.PerunCommand;
import cz.metacentrum.perun.openapi.model.Attribute;
import cz.metacentrum.perun.openapi.model.RichUser;
import cz.metacentrum.perun.openapi.model.UserExtSource;
import java.util.Comparator;
import java.util.List;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

/**
 * Gets user with its attributes.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
@SuppressWarnings("unused")
public class GetRichUser extends PerunCommand {

  @Override
  public String getCommandDescription() {
    return "gets user with its attributes";
  }

  @Override
  public void addOptions(Options options) {
    options.addOption(
        Option.builder("id").required(true).hasArg(true).longOpt("user").desc("user's identifier").build());
  }

  @Override
  public void executeCommand(PerunCLI.CommandContext ctx) {
    int id = Integer.parseInt(ctx.getCommandLine().getOptionValue("id"));
    RichUser richUser = ctx.getPerunRPC().getUsersManager().getRichUserWithAttributes(id);
    System.out.println("         id: " + richUser.getId());
    System.out.println("titleBefore: " + richUser.getTitleBefore());
    System.out.println("  firstName: " + richUser.getFirstName());
    System.out.println(" middleName: " + richUser.getMiddleName());
    System.out.println("   lastName: " + richUser.getLastName());
    System.out.println(" titleAfter: " + richUser.getTitleAfter());
    System.out.println("  createdAt: " + richUser.getCreatedAt());
    System.out.println("  createdBy: " + richUser.getCreatedBy());
    System.out.println(" modifiedAt: " + richUser.getModifiedAt());
    System.out.println(" modifiedBy: " + richUser.getModifiedBy());
    System.out.println();
    System.out.println(" UserExtSources:");
    System.out.println();
    List<UserExtSource> userExtSources = richUser.getUserExtSources();
    userExtSources.sort(Comparator.comparing(UserExtSource::getLastAccess));
    for (UserExtSource ues : userExtSources) {
      System.out.println("(" + ues.getLastAccess() + ") " + ues.getLogin() + " " + ues.getExtSource().getName());
    }
    System.out.println();
    System.out.println(" user attributes:");
    System.out.println();
    List<Attribute> userAttributes = richUser.getUserAttributes();
    for (Attribute a : userAttributes) {
      System.out.println("attribute " + a.getNamespace() + ":" + a.getFriendlyName() + " = " + a.getValue());
    }
  }

}
