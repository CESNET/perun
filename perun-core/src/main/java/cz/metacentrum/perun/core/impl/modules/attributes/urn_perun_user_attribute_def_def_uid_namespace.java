package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleImplApi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Checks and fills at specified facility users UID.
 *
 * @date 22.4.2011 10:43:48
 * @author Lukáš Pravda   <luky.pravda@gmail.com>
 * @author Michal Prochazka  <michalp@ics.muni.cz>
 */
public class urn_perun_user_attribute_def_def_uid_namespace extends UserAttributesModuleAbstract implements UserAttributesModuleImplApi {

	private static final String A_E_namespace_minUID = AttributesManager.NS_ENTITYLESS_ATTR_DEF + ":namespace-minUID";
	private static final String A_E_namespace_maxUID = AttributesManager.NS_ENTITYLESS_ATTR_DEF + ":namespace-maxUID";
	private static final String A_E_namespace_namespace_uid_policy = AttributesManager.NS_ENTITYLESS_ATTR_DEF + ":namespace-uid-policy";
	private static final String UID_POLICY_INCREMENT = "increment";

	/**
	 * Checks the new UID of the user. The new UID must
	 * not be lower than the min UID or greater than the max UID. Also no collision between
	 * existing user and the new user is allowed.
	 */
	@Override
	public void checkAttributeSemantics(PerunSessionImpl sess, User user, Attribute attribute) throws WrongReferenceAttributeValueException, InternalErrorException, WrongAttributeAssignmentException {
		Integer uid = attribute.valueAsInteger();
		String uidNamespace = attribute.getFriendlyNameParameter();

		if (uid == null) {
			throw new WrongReferenceAttributeValueException(attribute, "Attribute was not filled, therefore there is nothing to be checked.");
		}

		Attribute minUidAttribute;
		Attribute maxUidAttribute;
		try {
			minUidAttribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, uidNamespace, A_E_namespace_minUID);
			maxUidAttribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, uidNamespace, A_E_namespace_maxUID);
		} catch (AttributeNotExistsException e) {
			throw new ConsistencyErrorException("minUid and maxUid attributes are required", e);
		}

		Integer min = minUidAttribute.valueAsInteger();
		Integer max = maxUidAttribute.valueAsInteger();
		if(min == null) {
			throw new WrongReferenceAttributeValueException(attribute, minUidAttribute);
		}
		if (max == null) {
			throw new WrongReferenceAttributeValueException(attribute, maxUidAttribute);
		}
		//uid is in proper range
		if (uid < min) {
			throw new WrongReferenceAttributeValueException(attribute, minUidAttribute, user, null, uidNamespace, null, "UID " + uid + " is lesser than min " + min);
		}

		if (uid > max) {
			throw new WrongReferenceAttributeValueException(attribute, maxUidAttribute, user, null, uidNamespace, null, "UID " + uid + " is higher than max " + max);
		}

		// Get all users who have set attribute urn:perun:member:attribute-def:def:uid-namespace:[uid-namespace], with the value.
		List<User> usersWithUid = sess.getPerunBl().getUsersManagerBl().getUsersByAttribute(sess, attribute);
		usersWithUid.remove(user); //remove self
		if (!usersWithUid.isEmpty()) {
			if(usersWithUid.size() > 1) throw new ConsistencyErrorException("FATAL ERROR: Duplicated UID detected." +  attribute + " " + usersWithUid);
			throw new WrongReferenceAttributeValueException(attribute, "UID " + attribute.getValue() + " is already occupied by " + usersWithUid.get(0)  + ". We can't set it for " + user + ".");
		}
	}

	/**
	 * Fills the new UID for the user at the specified facility. First empty slot
	 * in range (minUID, maxUID) is returned.
	 */
	@Override
	public Attribute fillAttribute(PerunSessionImpl sess, User user, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException {
		String uidNamespace = attribute.getFriendlyNameParameter();
		Attribute atr = new Attribute(attribute);

		Attribute minUidAttribute;
		Attribute maxUidAttribute;
		try {
			minUidAttribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, uidNamespace, A_E_namespace_minUID);
			maxUidAttribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, uidNamespace, A_E_namespace_maxUID);
		} catch (AttributeNotExistsException e) {
			throw new ConsistencyErrorException("minUid and maxUid attributes are required", e);
		}

		Integer min = (Integer) minUidAttribute.getValue();
		Integer max = (Integer) maxUidAttribute.getValue();
		if(min == null || max == null){
			return atr; //we couldnt determine necessary attributes for getting new uID
		}

		SortedSet<Integer> values = new TreeSet<>();

		// Get all attributes urn:perun:member:attribute-def:def:uid-namespace:[uid-namespace], then we can get the new UID
		List<Attribute> uidAttributes = sess.getPerunBl().getAttributesManagerBl().getAttributesByAttributeDefinition(sess, attribute);
		for (Attribute uidAttribute: uidAttributes) {
			if(uidAttribute.getValue() != null) values.add((Integer) uidAttribute.getValue());
		}


		String uidPolicy;
		try {
			uidPolicy = (String) sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, uidNamespace, A_E_namespace_namespace_uid_policy).getValue();
		} catch (AttributeNotExistsException e) {
			throw new InternalErrorException(e);
		}

		if (UID_POLICY_INCREMENT.equals(uidPolicy)) {
			// Only use +1 for max UID
			if(values.isEmpty()) {
				atr.setValue(min);
			} else {
				atr.setValue(Collections.max(values)+1);
			}
			return atr;
		} else {
			// Recycle UIDs

			if (values.size() == 0) {
				atr.setValue(min);
				return atr;
			} else {
				for (int i = min; i < max; i++) {
					if (!values.contains(i)) {
						atr.setValue(i);
						return atr;
					}
				}
				return atr;
			}
		}
	}

	@Override
	public List<String> getDependencies() {
		List<String> dependencies = new ArrayList<>();
		dependencies.add(A_E_namespace_maxUID);
		dependencies.add(A_E_namespace_minUID);
		return dependencies;
	}

	/*public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
		attr.setFriendlyName("uid-namespace");
		attr.setType(Integer.class.getName());
		attr.setDescription("Uid namespace.");
		return attr;
	}*/
}
