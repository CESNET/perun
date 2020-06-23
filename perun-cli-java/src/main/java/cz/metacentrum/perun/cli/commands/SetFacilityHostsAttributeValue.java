package cz.metacentrum.perun.cli.commands;

import cz.metacentrum.perun.cli.PerunCLI;
import cz.metacentrum.perun.cli.PerunCommand;
import cz.metacentrum.perun.openapi.AttributesManagerApi;
import cz.metacentrum.perun.openapi.PerunRPC;
import cz.metacentrum.perun.openapi.model.Attribute;
import cz.metacentrum.perun.openapi.model.Facility;
import cz.metacentrum.perun.openapi.model.Host;
import cz.metacentrum.perun.openapi.model.InputSetHostAttribute;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.springframework.web.client.RestClientException;

import java.util.List;

/**
 * Sets attribute value on all hosts of a facility.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
@SuppressWarnings("unused")
public class SetFacilityHostsAttributeValue extends PerunCommand {

	@Override
	public String getCommandDescription() {
		return "sets attribute value on all hosts of a facility";
	}

	@Override
	public void addOptions(Options options) {
		this.addFacilityOptions(options);
		this.addAttributeDefinitionOptions(options);
		options.addOption(Option.builder("w").required(true).hasArg(true).longOpt("attributeValue").desc("attribute value").build());
	}

	@Override
	public void executeCommand(PerunCLI.CommandContext ctx) {
		int facilityId = this.getFacilityId(ctx, true);
		int attributeDefinitionId = this.getAttributeDefinitionId(ctx, true);
		String value = ctx.getCommandLine().getOptionValue("w");
		Object attributeValue;
		if(value.matches("\\d+")) {
			attributeValue = Integer.valueOf(value);
		} else {
			attributeValue = value;
		}

		PerunRPC perunRPC = ctx.getPerunRPC();
		AttributesManagerApi attributesManager = perunRPC.getAttributesManager();
		List<Host> hosts = perunRPC.getFacilitiesManager().getHosts(facilityId);
		for (Host host : hosts) {
			Attribute attribute = attributesManager.getHostAttributeById(host.getId(), attributeDefinitionId);
			attribute.setValue(attributeValue);
			System.out.println("setting attribute on host "+host.getHostname()+" to "+attributeValue+" ...");
			attributesManager.setHostAttribute(new InputSetHostAttribute().host(host.getId()).attribute(attribute));
		}
		System.out.println("OK");
	}

}
