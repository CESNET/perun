package cz.metacentrum.perun.cli.commands;

import cz.metacentrum.perun.cli.PerunCLI;
import cz.metacentrum.perun.cli.PerunCommand;
import cz.metacentrum.perun.openapi.model.Attribute;
import cz.metacentrum.perun.openapi.model.Group;
import cz.metacentrum.perun.openapi.model.RichGroup;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.springframework.web.client.RestClientException;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Prints list of Groups allowed on Facility.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
@SuppressWarnings("unused")
public class ListOfFacilityGroups extends PerunCommand {

	@Override
	public String getCommandDescription() {
		return "prints list of Groups allowed on Facility";
	}

	@Override
	public void addOptions(Options options) {
		this.addFacilityOptions(options);
		this.addVoOptions(options);
		this.addServiceOptions(options);
		this.addSortingOptions(options, "order by group name");
		options.addOption(Option.builder("a").required(false).hasArgs().longOpt("attrNames").desc("multiple attribute names").build());
	}

	@Override
	public void executeCommand(PerunCLI.CommandContext ctx) {
		int facilityId = this.getFacilityId(ctx, true);
		Integer voId = this.getVoId(ctx, false);
		Integer serviceId = this.getServiceId(ctx, false);
		String[] optionA = ctx.getCommandLine().getOptionValues("a");
		if (optionA == null) {
			List<Group> groups = ctx.getPerunRPC().getFacilitiesManager().getAllowedGroups(facilityId, voId, serviceId);
			this.sort(ctx, groups, Comparator.comparing(Group::getName));
			for (Group group : groups) {
				System.out.println(group.getId() + "\t" + group.getName() + "\t" + "\"" + group.getDescription() + "\"\t voId: " + group.getVoId());
			}
		} else {
			List<String> attrNames = Arrays.asList(optionA);
			List<RichGroup> groups = ctx.getPerunRPC().getFacilitiesManager().getAllowedRichGroupsWithAttributes(facilityId, attrNames, voId, serviceId);
			this.sort(ctx, groups, Comparator.comparing(RichGroup::getName));
			for (RichGroup group : groups) {
				System.out.println(group.getId() + "\t" + group.getName() + "\t" + "\"" + group.getDescription() + "\"\t voId: " + group.getVoId());
				List<Attribute> groupAttributes = group.getAttributes();
				if (groupAttributes != null) {
					for (Attribute a : groupAttributes) {
						System.out.println("  " + a.getNamespace() + ":" + a.getFriendlyName() + " = " + a.getValue());
					}
				}
			}
		}
	}

}
