package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupAttributesModuleImplApi;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class urn_perun_group_attribute_def_def_groupStructureResources extends GroupAttributesModuleAbstract implements GroupAttributesModuleImplApi {

	private static final Pattern resourceIdPattern = Pattern.compile("^[1-9][0-9]*$");
    private static final Pattern invalidEscapePattern =
      Pattern.compile("(" + "([^\\\\]|^)(\\\\\\\\)*)\\\\([^,\\\\]|$)");

	@Override
	public void checkAttributeSyntax(PerunSessionImpl sess, Group group, Attribute attribute) throws WrongAttributeValueException {
		//Null value is ok, means no settings for group
		if(attribute.getValue() == null) return;

		LinkedHashMap<String, String> attrValues = attribute.valueAsMap();

		boolean hasInvalidResourceName = attrValues.keySet().stream()
				.anyMatch(this::invalidResourceName);

		if (hasInvalidResourceName) {
			throw new WrongAttributeValueException(attribute, group, "Some of the specified resource ids has an invalid format.");
		}
		for (String rawGroupLogins : attrValues.values()) {
			// null or empty value means the whole group tree
			if (rawGroupLogins == null || rawGroupLogins.isEmpty()) {
				continue;
			}
			Matcher m = invalidEscapePattern.matcher(rawGroupLogins);
			if (m.find()) {
				throw new WrongAttributeValueException(attribute, group, "Group logins format contains invalid escape sequence.");
			}
			if (!rawGroupLogins.endsWith(",")) {
				throw new WrongAttributeValueException(attribute, group, "Each group login has to end with a comma ','.");
			}
		}
	}

	@Override
	public void checkAttributeSemantics(PerunSessionImpl sess, Group group, Attribute attribute) throws WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
		//Null value is ok, means no settings for group
		if(attribute.getValue() == null) return;

		LinkedHashMap<String, String> attrValues = attribute.valueAsMap();
		Vo vo;
		try {
			vo = sess.getPerunBl().getVosManagerBl().getVoById(sess, group.getVoId());
		} catch (VoNotExistsException e) {
			throw new InternalErrorException("Failed to find group's vo.", e);
		}
		List<Resource> voResources = sess.getPerunBl().getResourcesManagerBl().getResources(sess, vo);
		Set<Integer> voResourceIds = voResources.stream()
			.map(Resource::getId)
			.collect(Collectors.toSet());

		for (String rawId : attrValues.keySet()) {
			int id = Integer.parseInt(rawId);
			if (!voResourceIds.contains(id)) {
				throw new WrongReferenceAttributeValueException(attribute, "There is no resource with id '" + id + "' assigned to the groups vo: " + vo);
			}
		}
	}

	private boolean invalidResourceName(String value) {
		return !resourceIdPattern.matcher(value).matches();
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_GROUP_ATTR_DEF);
		attr.setType(LinkedHashMap.class.getName());
		attr.setFriendlyName("groupStructureResources");
		attr.setDisplayName("Group structure synchronization resources");
		attr.setDescription("Defines, which resources (map keys) should be auto assigned, and to which groups " +
			"(map values). Each group login should end with the `,` character (even the last one, eg: " +
			"`login1,login2,`). If some of the group logins contains a comma ',' or backslash '\\', you have to " +
			"escape it with the backslash '\\' character.");
		return attr;
	}
}