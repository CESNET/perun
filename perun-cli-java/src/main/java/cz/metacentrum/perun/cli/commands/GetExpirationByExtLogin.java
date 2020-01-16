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
 * cat >users.txt <<"EOF"
 * makub@META
 * sapakt@META
 * EOF
 *
 * getExpirationByExtLogin --extSourceName META --voName meta -f users.txt
 *
 * makub@META;2020-02-02
 * sapakt@META;2020-02-02
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
		options.addOption(Option.builder("E").required(true).hasArg().longOpt("extSourceName").desc("extSource name").build());
		options.addOption(Option.builder("f").required(true).hasArg().longOpt("file").desc("name of file with user login per line").build());
	}

	@Override
	public void executeCommand(PerunCLI.CommandContext ctx) {
		String extSourceName = ctx.getCommandLine().getOptionValue("E");
		Integer voId = getVoId(ctx, true);
		String filename = ctx.getCommandLine().getOptionValue("f");
		try {
			PerunRPC perunRPC = ctx.getPerunRPC();
			for (String login : Files.readAllLines(Paths.get(filename))) {
				Member member = perunRPC.getMembersManager().getMemberByExtSourceNameAndExtLogin(voId, login, extSourceName);
				Attribute attr = perunRPC.getAttributesManager().getMemberAttributeByName(member.getId(), "urn:perun:member:attribute-def:def:membershipExpiration");
				String expiration = (String) attr.getValue();
				System.out.println(login + ";" + expiration);
			}
		} catch (IOException e) {
			System.err.println("file " + filename + " cannot be read");
			System.exit(1);
		}
	}
}
