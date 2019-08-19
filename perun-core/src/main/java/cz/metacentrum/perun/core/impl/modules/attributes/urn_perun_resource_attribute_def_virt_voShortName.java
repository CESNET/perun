package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceVirtualAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceVirtualAttributesModuleImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.SkipValueCheckDuringDependencyCheck;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Slavek Licehammer &lt;glory@ics.muni.cz&gt;
 */
@SkipValueCheckDuringDependencyCheck
public class urn_perun_resource_attribute_def_virt_voShortName extends ResourceVirtualAttributesModuleAbstract implements ResourceVirtualAttributesModuleImplApi {

	@Override
	public Attribute getAttributeValue(PerunSessionImpl sess, Resource resource, AttributeDefinition attributeDefinition) throws InternalErrorException {
		Attribute attribute = new Attribute(attributeDefinition);

		Vo vo = sess.getPerunBl().getResourcesManagerBl().getVo(sess, resource);
		attribute.setValue(vo.getShortName());

		return attribute;

	}

	@Override
	public boolean setAttributeValue(PerunSessionImpl sess, Resource resource, Attribute attribute) throws InternalErrorException {
		throw new InternalErrorException("Can't set value of this virtual attribute this way. " + attribute);
	}

	@Override
	public boolean removeAttributeValue(PerunSessionImpl sess, Resource resource, AttributeDefinition attribute) throws InternalErrorException {
		throw new InternalErrorException("Can't remove value of this virtual attribute this way. " + attribute);
	}

	@Override
	public List<String> getStrongDependencies() {
		List<String> strongDependencies = new ArrayList<>();
		strongDependencies.add(AttributesManager.NS_VO_ATTR_CORE + ":shortName");
		return strongDependencies;
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_VO_ATTR_VIRT);
		attr.setFriendlyName("voShortName");
		attr.setDisplayName("Short name of VO");
		attr.setType(String.class.getName());
		attr.setDescription("Short name of VO where this resource is assigned");
		return attr;
	}
}
