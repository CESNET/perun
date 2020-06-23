package cz.metacentrum.perun.cli;

import cz.metacentrum.perun.openapi.model.ExtSource;
import cz.metacentrum.perun.openapi.model.Facility;
import cz.metacentrum.perun.openapi.model.PerunBean;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.springframework.web.client.RestClientException;

import java.util.Comparator;
import java.util.List;

/**
 * Empty command.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public abstract class PerunCommand {

	String getName() {
		String s = getClass().getSimpleName();
		return s.substring(0, 1).toLowerCase() + s.substring(1);
	}

	public abstract String getCommandDescription();

	/**
	 * Adds command-line options.
	 *
	 * @param options options specific to this command
	 */
	public void addOptions(Options options) {
	}

	public abstract void executeCommand(PerunCLI.CommandContext ctx);


	private void addEntityOptions(Options options, String entity, String idOption, String nameOption,  String nameType) {
		options.addOption(Option.builder(idOption).required(false).hasArg(true).longOpt(entity+"Id").desc(entity+" id").build());
		options.addOption(Option.builder(nameOption).required(false).hasArg(true).longOpt(entity+"Name").desc(entity+" "+nameType+" name").build());
	}

	@FunctionalInterface
	private interface EntityIdByNameGetter {
		Integer get(String entityName);
	}

	@FunctionalInterface
	private interface EntityByNameGetter<T> {
		T get(String entityName);
	}

	@FunctionalInterface
	private interface EntityByIdGetter<T> {
		T get(int entityId);
	}
	private Integer getEntityId(PerunCLI.CommandContext ctx, boolean required, String entity, String idOption, String nameOption, EntityIdByNameGetter eg) {
		if (ctx.getCommandLine().hasOption(nameOption)) {
			String entityName = ctx.getCommandLine().getOptionValue(nameOption);
			return eg.get(entityName);
		} else if (ctx.getCommandLine().hasOption(idOption)) {
			return Integer.parseInt(ctx.getCommandLine().getOptionValue(idOption));
		} else if(required){
			System.err.println("ERROR: "+entity+"Id or "+entity+"Name is required");
			System.exit(1);
			return 0;
		} else {
			return null;
		}
	}

	private <T> T getEntity(PerunCLI.CommandContext ctx, boolean required, String entity, String idOption, String nameOption, EntityByNameGetter<T> gn, EntityByIdGetter<T> gi) {
		if (ctx.getCommandLine().hasOption(nameOption)) {
			return gn.get(ctx.getCommandLine().getOptionValue(nameOption));
		} else if (ctx.getCommandLine().hasOption(idOption)) {
			return gi.get(Integer.parseInt(ctx.getCommandLine().getOptionValue(idOption)));
		} else if(required){
			System.err.println("ERROR: "+entity+"Id or "+entity+"Name is required");
			System.exit(1);
			return null;
		} else {
			return null;
		}
	}

	protected void addFacilityOptions(Options options) {
		addEntityOptions(options,  "facility","f", "F", "");
	}

	@SuppressWarnings("SameParameterValue")
	protected Integer getFacilityId(PerunCLI.CommandContext ctx, boolean required) {
		return getEntityId(ctx, required, "facility", "f", "F",
			facilityName -> ctx.getPerunRPC().getFacilitiesManager().getFacilityByName(facilityName).getId());
	}

	protected Facility getFacility(PerunCLI.CommandContext ctx, boolean required) {
		return getEntity(ctx, required, "facility", "f", "F",
			facilityName -> ctx.getPerunRPC().getFacilitiesManager().getFacilityByName(facilityName),
			facilityId -> ctx.getPerunRPC().getFacilitiesManager().getFacilityById(facilityId));
	}

	protected void addVoOptions(Options options) {
		addEntityOptions(options,  "vo","v", "V", "short");
	}

	@SuppressWarnings("SameParameterValue")
	protected Integer getVoId(PerunCLI.CommandContext ctx, boolean required) {
		return getEntityId(ctx, required, "vo", "v", "V",
			voShortName -> ctx.getPerunRPC().getVosManager().getVoByShortName(voShortName).getId());
	}

	protected void addAttributeDefinitionOptions(Options options) {
		addEntityOptions(options,  "attribute","a", "A", "");
	}

	protected Integer getAttributeDefinitionId(PerunCLI.CommandContext ctx, boolean required) {
		return getEntityId(ctx, required, "attribute", "a", "A",
			attributeName -> ctx.getPerunRPC().getAttributesManager().getAttributeDefinitionByName(attributeName).getId());
	}

	protected void addServiceOptions(Options options) {
		addEntityOptions(options,  "service","s", "S", "");
	}

	@SuppressWarnings("SameParameterValue")
	protected Integer getServiceId(PerunCLI.CommandContext ctx, boolean required) {
		return getEntityId(ctx, required, "service", "s", "S",
			serviceName -> ctx.getPerunRPC().getServicesManager().getServiceByName(serviceName).getId());
	}

	protected void addExtSourceOptions(Options options) {
		addEntityOptions(options,  "extSource","e", "E", "");
	}

	protected ExtSource getExtSource(PerunCLI.CommandContext ctx, boolean required) {
		return getEntity(ctx, required, "extSource", "e", "E",
			extSourceName -> ctx.getPerunRPC().getExtSourcesManager().getExtSourceByName(extSourceName),
			extSourceId -> ctx.getPerunRPC().getExtSourcesManager().getExtSourceById(extSourceId));
	}

	protected void addSortingOptions(Options options, String orderByNameDescription) {
		options.addOption(Option.builder("n").required(false).hasArg(false).longOpt("orderByName").desc(orderByNameDescription).build());
		options.addOption(Option.builder("i").required(false).hasArg(false).longOpt("orderById").desc("order by id").build());
	}

	protected void sort(PerunCLI.CommandContext ctx, List<? extends PerunBean> perunBeans, Comparator comparator) {
		if (ctx.getCommandLine().hasOption("i")) {
			perunBeans.sort(Comparator.comparing(PerunBean::getId));
		} else if (ctx.getCommandLine().hasOption("n")) {
			perunBeans.sort(comparator);
		}
	}
}
