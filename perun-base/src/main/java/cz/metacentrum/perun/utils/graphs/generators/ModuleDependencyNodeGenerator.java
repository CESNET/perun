package cz.metacentrum.perun.utils.graphs.generators;

import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.utils.graphs.Node;

/**
 * Implementation of {@link NodeGenerator} for nodes representing attribute moduels.
 * Label contains abbreviation for given attributeDefinition.
 *
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class ModuleDependencyNodeGenerator implements NodeGenerator<AttributeDefinition> {

	@Override
	public Node generate(AttributeDefinition entity, Long id) {
		Node n = new Node();

		n.setId(id);
		n.setLabel(BeansUtils.getAttributeDefinitionAbbreviation(entity));
		n.setFillColor(getNodeColorFromAttributeDefinition(entity));
		n.setStyle(getNodeStyleFromAttributeDefinition(entity));

		return n;
	}

	private static String getNodeStyleFromAttributeDefinition(AttributeDefinition ad) {
		if (ad.getNamespace().endsWith("virt")) {
			return "filled,dashed";
		} else if (ad.getNamespace().endsWith("def")) {
			return "filled";
		} else {
			return "filled";
		}
	}

	private static String getNodeColorFromAttributeDefinition(AttributeDefinition attributeDefinition) {
		switch (attributeDefinition.getEntity()) {
			case "user":
				return "gray";
			case "group":
				return "green";
			case "resource":
				return "cyan";
			case "facility":
				return "firebrick1";
			case "vo":
				return "pink";
			case "member":
				return "yellow";
			case "entityless":
				return "orange";
			case "group_resource":
				return "green:cyan";
			case "member_group":
				return "yellow:green";
			case "member_resource":
				return "yellow:cyan";
			case "user_facility":
				return "gray:firebrick1";
			default:
				return "white";
		}
	}
}
