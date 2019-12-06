package cz.metacentrum.perun.cli.commands;

import cz.metacentrum.perun.cli.PerunCLI;
import cz.metacentrum.perun.cli.PerunCommand;
import cz.metacentrum.perun.openapi.PerunRPC;
import cz.metacentrum.perun.openapi.model.Attribute;
import cz.metacentrum.perun.openapi.model.ExtSource;
import cz.metacentrum.perun.openapi.model.Member;
import cz.metacentrum.perun.openapi.model.User;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Prints expiration dates of users in a given VO.
 * <p>
 * E.g.
 * <code>
 * getExpirationByExtLogin --extSourceName META --voName meta -f users.txt
 * Martin Kuba;2020-02-02;makub@META
 * Tomáš Sapák;2020-02-02;sapakt@META
 * </code>
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
@SuppressWarnings("unused")
public class GetExpirationByExtLogin extends PerunCommand {

	@Override
	public String getCommandDescription() {
		return "outputs member expirations in given VO ";
	}

	@Override
	public void addOptions(Options options) {
		this.addVoOptions(options);
		this.addExtSourceOptions(options);
		options.addOption(Option.builder("f").required(true).hasArg().longOpt("file").desc("name of file with user login per line").build());
	}

	@Override
	public void executeCommand(PerunCLI.CommandContext ctx) {
		ExtSource extSource = getExtSource(ctx, true);
		String extSourceName = extSource.getName();
		Integer voId = getVoId(ctx, true);
		String filename = ctx.getCommandLine().getOptionValue("f");
		try {
			PerunRPC perunRPC = ctx.getPerunRPC();
			for (String login : Files.readAllLines(Paths.get(filename))) {
				User user = perunRPC.getUsersManager().getUserByExtSourceNameAndExtLogin(login, extSourceName);
				Member member = perunRPC.getMembersManager().getMemberByUser(voId, user.getId());
				Attribute attr = perunRPC.getAttributesManager().getMemberAttributeByName(member.getId(), "urn:perun:member:attribute-def:def:membershipExpiration");
				String expiration = (String) attr.getValue();
				System.out.println(
					user.getFirstName() + " " + user.getLastName()
						+ ";" + expiration
						+ ";" + login
				);
			}
		} catch (IOException e) {
			System.err.println("file " + filename + " cannot be read");
			System.exit(1);
		}
	}
}
