package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.GroupResourceMismatchException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupResourceAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupResourceAttributesModuleImplApi;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Michal Stava email:&lt;stavamichal@gmail.com&gt;
 */
public class urn_perun_group_resource_attribute_def_def_isSystemUnixGroup extends GroupResourceAttributesModuleAbstract implements GroupResourceAttributesModuleImplApi {

	private static final String A_GR_systemUnixGroupName = AttributesManager.NS_GROUP_RESOURCE_ATTR_DEF + ":systemUnixGroupName";
	private static final String A_GR_systemUnixGID = AttributesManager.NS_GROUP_RESOURCE_ATTR_DEF + ":systemUnixGID";

	@Override
	public Attribute fillAttribute(PerunSessionImpl sess, Group group, Resource resource, AttributeDefinition attributeDefinition) {
		return new Attribute(attributeDefinition);
	}

	@Override
	public void checkAttributeSemantics(PerunSessionImpl sess, Group group, Resource resource, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException{

		Integer isSystemUnixGroup = (Integer) attribute.getValue();
		if(isSystemUnixGroup == null) return; //isSystemUnixGroup can be null. It is equivalent to 0.

		if(isSystemUnixGroup != 0 && isSystemUnixGroup != 1) throw new WrongAttributeValueException(attribute, "Attribute isSystemUnixGroup should not other number than 0 or 1.");

		Attribute sysUnixGroupName;
		Attribute sysUnixGID;

		if(isSystemUnixGroup == 1) {

			try {
				sysUnixGroupName = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, resource, group, A_GR_systemUnixGroupName);
				sysUnixGID = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, resource, group, A_GR_systemUnixGID);
			} catch (AttributeNotExistsException ex) {
				//if any of these attributes not exist, its wrong
				throw new ConsistencyErrorException("Attributes sysUnixGroupName or sysUnixGID not exist.",ex);
			} catch (GroupResourceMismatchException ex) {
				throw new InternalErrorException(ex);
			}

			try {
				sess.getPerunBl().getAttributesManagerBl().checkAttributeSemantics(sess, resource, group, sysUnixGroupName);
			} catch(WrongAttributeValueException ex) {
				throw new WrongReferenceAttributeValueException("Bad value in sysUnixGroupName attribute.",ex);
			} catch (GroupResourceMismatchException ex) {
				throw new InternalErrorException(ex);
			}

			try {
				sess.getPerunBl().getAttributesManagerBl().checkAttributeSemantics(sess, resource, group, sysUnixGID);
			} catch(WrongAttributeValueException ex) {
				throw new WrongReferenceAttributeValueException("Bad value in sysUnixGID.",ex);
			} catch (GroupResourceMismatchException ex) {
				throw new InternalErrorException(ex);
			}
		}
	}

	@Override
	public List<String> getDependencies() {
		List<String> dependencies = new ArrayList<>();
		dependencies.add(A_GR_systemUnixGroupName);
		dependencies.add(A_GR_systemUnixGID);
		return dependencies;
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_GROUP_RESOURCE_ATTR_DEF);
		attr.setFriendlyName("isSystemUnixGroup");
		attr.setDisplayName("Is system unix group");
		attr.setType(Integer.class.getName());
		attr.setDescription("The group is system unix group.");
		return attr;
	}
}
