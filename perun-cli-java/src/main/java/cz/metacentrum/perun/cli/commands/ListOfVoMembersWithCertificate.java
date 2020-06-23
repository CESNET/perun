package cz.metacentrum.perun.cli.commands;

import cz.metacentrum.perun.cli.PerunCLI;
import cz.metacentrum.perun.cli.PerunCommand;
import cz.metacentrum.perun.openapi.AttributesManagerApi;
import cz.metacentrum.perun.openapi.model.Attribute;
import cz.metacentrum.perun.openapi.model.AttributeDefinition;
import cz.metacentrum.perun.openapi.model.Member;
import cz.metacentrum.perun.openapi.model.Owner;
import cz.metacentrum.perun.openapi.model.User;
import org.apache.commons.cli.Options;
import org.springframework.web.client.RestClientException;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Prints list of users with certificate.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
@SuppressWarnings("unused")
public class ListOfVoMembersWithCertificate extends PerunCommand {

	@Override
	public String getCommandDescription() {
		return "prints list of VO members having a preferred X509 certificate";
	}

	@Override
	public void addOptions(Options options) {
		this.addVoOptions(options);
	}

	@Override
	public void executeCommand(PerunCLI.CommandContext ctx) {
		Integer voId = this.getVoId(ctx, true);
		AttributesManagerApi attributesManager = ctx.getPerunRPC().getAttributesManager();
		int prefDNid = attributesManager.getAttributeDefinitionByName("urn:perun:user:attribute-def:def:userPreferredCertDN").getId();

		for (Member member : ctx.getPerunRPC().getMembersManager().getMembers(voId, null)) {
			String  pdn = (String) attributesManager.getUserAttributeById(member.getUserId(), prefDNid).getValue();
			if(pdn != null) {
				User user = ctx.getPerunRPC().getUsersManager().getUserById(member.getUserId());
				System.out.println(user.getFirstName()+" "+user.getLastName()+" ("+member.getStatus()+") "+pdn);
			}
		}
	}

}
