package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.RichResource;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.FacilityVirtualAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.FacilityVirtualAttributesModuleImplApi;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * List of VO short names, which have resources on this facility.
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class urn_perun_facility_attribute_def_virt_voShortNames extends FacilityVirtualAttributesModuleAbstract implements FacilityVirtualAttributesModuleImplApi {

	@Override
	public Attribute getAttributeValue(PerunSessionImpl sess, Facility facility, AttributeDefinition attributeDefinition) throws InternalErrorException {

		Attribute attribute = new Attribute(attributeDefinition);

		Set<String> result = new HashSet<>();
		List<RichResource> resources = sess.getPerunBl().getFacilitiesManagerBl().getAssignedRichResources(sess, facility);
		for (RichResource resource : resources) {
			result.add(resource.getVo().getShortName());
		}

		if (result.isEmpty()) return attribute; // no resource = no vo short names

		attribute.setValue(new ArrayList<>(result)); // found resource = vo short names
		return attribute;

	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_FACILITY_ATTR_VIRT);
		attr.setFriendlyName("voShortNames");
		attr.setDisplayName("VO shortNames");
		attr.setType(ArrayList.class.getName());
		attr.setDescription("List of VOs short names, which have resources on this facility.");
		return attr;
	}

}
