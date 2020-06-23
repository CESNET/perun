package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupAttributesModuleImplApi;

import java.util.List;

/**
 * Module for attribute groupExtSource
 *
 * @author Michal Stava  &lt;stavamichal@gmail.com&gt;
 */
public class urn_perun_group_attribute_def_def_groupExtSource extends GroupAttributesModuleAbstract implements GroupAttributesModuleImplApi {

	@Override
	public void checkAttributeSemantics(PerunSessionImpl sess, Group group, Attribute attribute) throws WrongReferenceAttributeValueException {
		//prepare groupName value variable
		String extSourceName = null;
		if(attribute.getValue() != null) extSourceName = attribute.valueAsString();

		//if extSourceName is null, attribute can be removed
		if (extSourceName != null) {
			try {
				Vo groupVo = sess.getPerunBl().getVosManagerBl().getVoById(sess, group.getVoId());
				List<ExtSource> allowedExtSources = sess.getPerunBl().getExtSourcesManagerBl().getVoExtSources(sess, groupVo);
				for(ExtSource es: allowedExtSources) {
					if(extSourceName.equals(es.getName())) return;
				}
				throw new WrongReferenceAttributeValueException(attribute, null, group, null, "ExtSourceName " + extSourceName + " is not valid, because VO " + groupVo + " of this group has no such extSource assigned.");
			} catch (VoNotExistsException ex) {
				throw new ConsistencyErrorException("Vo of this group " + group + " not exists!");
			}
		}
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_GROUP_ATTR_DEF);
		attr.setFriendlyName("groupExtSource");
		attr.setDisplayName("Group's external source");
		attr.setType(String.class.getName());
		attr.setDescription("External source from which group comes from. Used for groups synchronization.");
		return attr;
	}
}
