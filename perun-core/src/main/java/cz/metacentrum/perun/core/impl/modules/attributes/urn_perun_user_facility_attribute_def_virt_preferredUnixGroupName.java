package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.core.implApi.modules.attributes.FacilityUserVirtualAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.FacilityUserVirtualAttributesModuleImplApi;
import java.util.ArrayList;
import java.util.List;

/**
 * Checks and fills at specified facility users preferred unix Group Name
 * Empty list if user has no preferrences.
 *
 * @date 12.8.2014
 * @author Michal Stava   <stavamichal@gmail.com>
 */
public class urn_perun_user_facility_attribute_def_virt_preferredUnixGroupName extends FacilityUserVirtualAttributesModuleAbstract implements FacilityUserVirtualAttributesModuleImplApi {

	private static final String A_FACILITY_DEF_UNIX_GROUPNAME_NAMESPACE = AttributesManager.NS_FACILITY_ATTR_DEF + ":unixGroupName-namespace";
	private static final String A_USER_DEF_PREFERRED_UNIX_GROUPNAME_NAMESPACE = AttributesManager.NS_USER_ATTR_DEF + ":preferredUnixGroupName-namespace:";

	@Override
	public void checkAttributeValue(PerunSessionImpl sess, Facility facility, User user, Attribute attribute) throws WrongAttributeValueException, WrongReferenceAttributeValueException, InternalErrorException, WrongAttributeAssignmentException {
		try {
			Attribute facilityGroupNameNamespaceAttr = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, facility, A_FACILITY_DEF_UNIX_GROUPNAME_NAMESPACE);
			if(facilityGroupNameNamespaceAttr.getValue() == null) {
				throw new WrongReferenceAttributeValueException(attribute, facilityGroupNameNamespaceAttr, user, facility, facility, null, "GroupName-namespace for racility cannot be null.");
			}
			String namespace = (String) facilityGroupNameNamespaceAttr.getValue();

			Attribute preferredUnixGroupNameAttr = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, user, A_USER_DEF_PREFERRED_UNIX_GROUPNAME_NAMESPACE + namespace);
			if(attribute.getValue() != null) {
				preferredUnixGroupNameAttr.setValue(attribute.getValue());
				sess.getPerunBl().getAttributesManagerBl().checkAttributeValue(sess, user, preferredUnixGroupNameAttr);
			} // Null is ok, can be empty
		} catch (AttributeNotExistsException e) {
			throw new ConsistencyErrorException(e);
		}
	}

	@Override
	public Attribute getAttributeValue(PerunSessionImpl sess, Facility facility, User user, AttributeDefinition attributeDefinition) throws InternalErrorException {
		Attribute attr = new Attribute(attributeDefinition);
		Attribute preferredGroupNameAttribute = null;

		try {
			Attribute facilityGroupNameNamespaceAttr = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, facility, A_FACILITY_DEF_UNIX_GROUPNAME_NAMESPACE);
			if(facilityGroupNameNamespaceAttr.getValue() != null) {
				String namespace = (String) facilityGroupNameNamespaceAttr.getValue();
				preferredGroupNameAttribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, user, A_USER_DEF_PREFERRED_UNIX_GROUPNAME_NAMESPACE + namespace);
				attr = Utils.copyAttributeToVirtualAttributeWithValue(preferredGroupNameAttribute, attr);
			} else {
				attr.setValue(null);
			}
		} catch (AttributeNotExistsException e) {
			throw new ConsistencyErrorException(e);
		} catch (WrongAttributeAssignmentException e) {
			throw new InternalErrorException(e);
		}

		return attr;
	}

	@Override
	public boolean setAttributeValue(PerunSessionImpl sess, Facility facility, User user, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException {
		AttributeDefinition userPreferredGroupNameDefinition;
		try {
			Attribute facilityGroupNameNamespaceAttr = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, facility, A_FACILITY_DEF_UNIX_GROUPNAME_NAMESPACE);
			if(facilityGroupNameNamespaceAttr.getValue() == null) {
				throw new WrongReferenceAttributeValueException(attribute, facilityGroupNameNamespaceAttr, facility, user, facility, null, "Facility need to have nonempty groupName-namespace attribute.");
			}
			String namespace = (String) facilityGroupNameNamespaceAttr.getValue();

			userPreferredGroupNameDefinition = sess.getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, A_USER_DEF_PREFERRED_UNIX_GROUPNAME_NAMESPACE + namespace);
		} catch (AttributeNotExistsException e) {
			throw new ConsistencyErrorException(e);
		} catch (WrongAttributeAssignmentException e) {
			throw new InternalErrorException(e);
		}

		Attribute userPreferredGroupName = new Attribute(userPreferredGroupNameDefinition);
		userPreferredGroupName.setValue(attribute.getValue());

		try {
			return sess.getPerunBl().getAttributesManagerBl().setAttributeWithoutCheck(sess, user, userPreferredGroupName);
		} catch (WrongAttributeValueException e) {
			throw new InternalErrorException(e);
		} catch (WrongAttributeAssignmentException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public List<String> getDependencies() {
		List<String> dependencies = new ArrayList<>();
		dependencies.add(A_FACILITY_DEF_UNIX_GROUPNAME_NAMESPACE);
		dependencies.add(A_USER_DEF_PREFERRED_UNIX_GROUPNAME_NAMESPACE + "*");
		return dependencies;
	}

	@Override
	public List<String> getStrongDependencies() {
		List<String> StrongDependencies = new ArrayList<String>();
		StrongDependencies.add(AttributesManager.NS_FACILITY_ATTR_DEF + ":unixGroupName-namespace");
		StrongDependencies.add(AttributesManager.NS_USER_ATTR_DEF + ":preferredUnixGroupName-namespace" + ":*");
		return StrongDependencies;
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_FACILITY_ATTR_VIRT);
		attr.setFriendlyName("preferredUnixGroupName");
		attr.setDisplayName("Preferred Unix GroupName");
		attr.setType(List.class.getName());
		attr.setDescription("Choosed users preferred unix groupNames for specific facility namespace.");
		return attr;
	}
}
