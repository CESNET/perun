package cz.metacentrum.perun.core.provisioning;


import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;

import java.util.List;
import java.util.Map;

/**
 * This component is used to efficiently load required attributes.
 *
 * Attributes, that can be hashed, must be loaded first via load* methods.
 * E.g.: to be able to call getResourceAttributesHashes, one must first call
 * loadResourceSpecificAttributes.
 *
 * IMPORTANT: this components has a STATE! The order of load methods is important.
 * E.g.: loadResourceSpecificAttributes will overwrite data for previously loaded resource.
 *
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public interface GenDataProvider {

	/**
	 * Return all hashes for facility attributes.
	 *
	 * @return list of hashes
	 */
	List<String> getFacilityAttributesHashes();

	/**
	 * Return all hashes for given resource attributes.
	 * If addVoAttributes is true, also adds a hash for resource's vo attributes,
	 * if they are not empty.
	 *
	 * @param resource resource
	 * @param addVoAttributes if true, add also vo attributes hash, if not empty
	 * @return list of hashes
	 */
	List<String> getResourceAttributesHashes(Resource resource, boolean addVoAttributes);

	/**
	 * Return all hashes relevant for given member.
	 * Member, User, Member-Resource, User-Facility.
	 *
	 * @param resource resource used to get member-resource attributes hash
	 * @param member given member
	 * @return list of hashes
	 */
	List<String> getMemberAttributesHashes(Resource resource, Member member);


	/**
	 * Return all hashes relevant for given member.
	 * Member, User, Member-Resource, User-Facility and Member-Group.
	 *
	 * @param resource resource used to get member-resource attributes hash
	 * @param member given member
	 * @param group group used to get member-group attributes
	 * @return list of hashes
	 */
	List<String> getMemberAttributesHashes(Resource resource, Member member, Group group);

	/**
	 * Returns all hashes relevant for given group.
	 * Group and Group-Resource attributes.
	 *
	 * @param resource resource used to get Group-Resource attributes.
	 * @param group group
	 * @return list of hashes
	 */
	List<String> getGroupAttributesHashes(Resource resource, Group group);

	/**
	 * Loads Facility attributes.
	 */
	void loadFacilityAttributes();

	/**
	 * Loads Resource and Member specific attributes.
	 * Resouce, Member, User, User-Facility (if not already loaded).
	 * Resource-Member (always)
	 * Vo - if specified by loadVoAttributes
	 *
	 * @param resource resource
	 * @param members members
	 * @param loadVoAttributes specifies, if the voAttributesShould be loaded as well.
	 */
	void loadResourceAttributes(Resource resource, List<Member> members, boolean loadVoAttributes);

	/**
	 * Loads Group and Group-Resource attributes.
	 * Group attributes are loaded only for groups that has not been already loaded.
	 *
	 * @param resource resource
	 * @param groups groups
	 */
	void loadGroupsAttributes(Resource resource, List<Group> groups);

	/**
	 * Loads Member-Group attributes.
	 *
	 * @param group groups
	 * @param members members
	 */
	void loadMemberGroupAttributes(Group group, List<Member> members);

	/**
	 * Returns map of all loaded attributes grouped by their hashes.
	 * Returns only non-empty lists, and only lists, for which their hashes has been returned,
	 * by some get.*attributesHashes method.
	 *
	 * @return map of hashes attributes
	 */
	Map<String, Map<String, Object>> getAllFetchedAttributes();
}
