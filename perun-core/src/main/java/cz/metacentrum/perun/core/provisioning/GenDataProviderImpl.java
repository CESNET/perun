package cz.metacentrum.perun.core.provisioning;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.GroupResourceMismatchException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MemberGroupMismatchException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class GenDataProviderImpl implements GenDataProvider {

  private final PerunSessionImpl sess;
  private final Service service;
  private final Facility facility;

  private final Map<String, List<Attribute>> attributesByHash = new HashMap<>();
  /**
   * These maps are not overwritten, because they do not depend on currently loaded entities.
   */
  private final Map<Member, List<Attribute>> memberAttrs = new HashMap<>();
  private final Map<Group, List<Attribute>> groupAttrs = new HashMap<>();
  private final Map<User, List<Attribute>> userAttrs = new HashMap<>();
  private final Map<User, List<Attribute>> userFacilityAttrs = new HashMap<>();
  private final Map<Resource, List<Attribute>> resourceAttrs = new HashMap<>();
  /**
   * Vos has to contain only ids, or only vos loaded from DB. Because of the equals and hashCode implementations. If
   * they were mixed, it would cause data duplicity.
   */
  private final Map<Vo, List<Attribute>> voAttrs = new HashMap<>();
  private final Map<Integer, User> loadedUsersById = new HashMap<>();
  private final Set<Member> processedMembers = new HashSet<>();
  private final Set<Group> processedGroups = new HashSet<>();
  private final Hasher hasher = new IdHasher();
  private List<Attribute> facilityAttrs;
  /**
   * Map of Member-Resource attributes. This map is overwritten when data for new resource are loaded!!!
   */
  private Map<Member, List<Attribute>> memberResourceAttrs = new HashMap<>();
  /**
   * Map of Group-Resource attributes. This map is overwritten when data for new resource are loaded!!!
   */
  private Map<Group, List<Attribute>> groupResourceAttrs = new HashMap<>();
  /**
   * Map of Member-Group attributes. This map is overwritten when data for new group are loaded!!!
   */
  private Map<Member, List<Attribute>> memberGroupAttrs = new HashMap<>();
  private Group lastLoadedGroup;
  private Resource lastLoadedResource;

  public GenDataProviderImpl(PerunSessionImpl sess, Service service, Facility facility) {
    this.sess = sess;
    this.service = service;
    this.facility = facility;
  }

  private Map<String, Object> convertToMap(List<Attribute> attributes) {
    Map<String, Object> map = new HashMap<>();
    attributes.forEach(a -> map.put(a.getName(), a.getValue()));
    return map;
  }

  @Override
  public Map<String, Map<String, Object>> getAllFetchedAttributes() {
    return attributesByHash.entrySet().stream().filter(entry -> !entry.getValue().isEmpty())
        .collect(toMap(Map.Entry::getKey, entry -> convertToMap(entry.getValue())));
  }

  /**
   * Get a list of attributes for given hash, and loads appropriate entity attributes from given map into the map of all
   * processed attributes.
   * <p>
   * If the map doesn't contain attributes for the given entity, or the list is empty, this method returns an empty
   * list. Otherwise, it returns a List with the given hash.
   *
   * @param hash   entity hash
   * @param entity the entity which the hash belongs
   * @param map    map of attributes for the entity
   * @param <T>    the type of the entity User, Member, ...
   * @return List with the hash or empty list if the map doesn't contain any attributes for the given entity
   */
  private <T> List<String> getAndStoreHash(String hash, T entity, Map<T, List<Attribute>> map) {
    var hashes = new ArrayList<String>();
    if (!attributesByHash.containsKey(hash)) {
      if (!map.containsKey(entity)) {
        return hashes;
      }
      attributesByHash.put(hash, map.get(entity));
    }
    if (!attributesByHash.get(hash).isEmpty()) {
      hashes.add(hash);
    }
    return hashes;
  }

  @Override
  public List<String> getFacilityAttributesHashes() {
    String hash = hasher.hashFacility(facility);

    var hashes = new ArrayList<String>();

    if (!attributesByHash.containsKey(hash)) {
      if (facilityAttrs == null) {
        throw new IllegalStateException("Facility attributes need to be loaded first.");
      }
      attributesByHash.put(hash, facilityAttrs);
    }

    if (!attributesByHash.get(hash).isEmpty()) {
      hashes.add(hash);
    }

    return hashes;
  }

  @Override
  public List<String> getGroupAttributesHashes(Resource resource, Group group) {
    if (!resource.equals(lastLoadedResource)) {
      throw new IllegalStateException(
          "The last loaded resource is different than the required one. Required: " + resource + ", Last loaded: " +
          lastLoadedResource);
    }
    List<String> hashes = new ArrayList<>();

    hashes.addAll(getGroupAttributesHashes(group));
    hashes.addAll(getGroupResourceAttributesHashes(group, resource));

    return hashes;
  }

  private List<String> getGroupAttributesHashes(Group group) {
    String hash = hasher.hashGroup(group);

    return getAndStoreHash(hash, group, groupAttrs);
  }

  private List<String> getGroupResourceAttributesHashes(Group group, Resource resource) {
    if (!resource.equals(lastLoadedResource)) {
      throw new IllegalStateException(
          "The last loaded resource is different than the required one. Required: " + resource + ", Last loaded: " +
          lastLoadedResource);
    }

    // FIXME - need to make sure that data for all of the given groups were loaded

    String hash = hasher.hashGroupResource(group, resource);

    return getAndStoreHash(hash, group, groupResourceAttrs);
  }

  @Override
  public List<String> getMemberAttributesHashes(Resource resource, Member member) {
    if (!resource.equals(lastLoadedResource)) {
      throw new IllegalStateException(
          "The last loaded resource is different than the required one. Required: " + resource + ", Last loaded: " +
          lastLoadedResource);
    }
    List<String> hashes = new ArrayList<>();

    User user = loadedUsersById.get(member.getUserId());

    hashes.addAll(getMemberAttributesHashes(member));
    hashes.addAll(getUserAttributesHashes(user));
    hashes.addAll(getUserFacilityAttributesHashes(user, facility));
    hashes.addAll(getMemberResourceAttributesHashes(member, resource));

    return hashes;
  }

  @Override
  public List<String> getMemberAttributesHashes(Resource resource, Member member, Group group) {
    if (!group.equals(lastLoadedGroup)) {
      throw new IllegalStateException(
          "Cannot load member-group attributes for group " + group + ", because last" + "loaded group is: " +
          lastLoadedGroup);
    }
    List<String> hashes = getMemberAttributesHashes(resource, member);

    hashes.addAll(getMemberGroupAttributesHashes(member, group));

    return hashes;
  }

  private List<String> getMemberAttributesHashes(Member member) {
    String hash = hasher.hashMember(member);

    return getAndStoreHash(hash, member, memberAttrs);
  }

  private List<String> getMemberGroupAttributesHashes(Member member, Group group) {
    if (!group.equals(lastLoadedGroup)) {
      throw new IllegalStateException(
          "Cannot load member-group attributes for group " + group + ", because last" + "loaded group is: " +
          lastLoadedGroup);
    }
    String hash = hasher.hashMemberGroup(member, group);

    return getAndStoreHash(hash, member, memberGroupAttrs);
  }

  private List<String> getMemberResourceAttributesHashes(Member member, Resource resource) {
    if (!resource.equals(lastLoadedResource)) {
      throw new IllegalStateException(
          "The last loaded resource is different than the required one. Required: " + resource + ", Last loaded: " +
          lastLoadedResource);
    }
    String hash = hasher.hashMemberResource(member, resource);

    return getAndStoreHash(hash, member, memberResourceAttrs);
  }

  @Override
  public List<String> getResourceAttributesHashes(Resource resource, boolean addVoAttributes) {
    List<String> hashes = getResourceAttributesHashes(resource);

    if (addVoAttributes) {
      // create incomplete vo object with only vo id
      Vo vo = new Vo();
      vo.setId(resource.getVoId());
      hashes.addAll(getVoAttributesHashes(vo));
    }

    return hashes;
  }

  private List<String> getResourceAttributesHashes(Resource resource) {
    String hash = hasher.hashResource(resource);

    return getAndStoreHash(hash, resource, resourceAttrs);
  }

  private List<String> getUserAttributesHashes(User user) {
    String hash = hasher.hashUser(user);

    return getAndStoreHash(hash, user, userAttrs);
  }

  private List<String> getUserFacilityAttributesHashes(User user, Facility facility) {
    String hash = hasher.hashUserFacility(user, facility);

    return getAndStoreHash(hash, user, userFacilityAttrs);
  }

  private List<String> getVoAttributesHashes(Vo vo) {
    String hash = hasher.hashVo(vo);

    return getAndStoreHash(hash, vo, voAttrs);
  }

  @Override
  public void loadFacilityAttributes() {
    facilityAttrs = sess.getPerunBl().getAttributesManagerBl().getRequiredAttributes(sess, service, facility);
  }

  @Override
  public void loadGroupsAttributes(Resource resource, List<Group> groups) {
    groupResourceAttrs = new HashMap<>();
    lastLoadedResource = resource;

    for (Group group : groups) {
      try {
        // FIXME - attributes could be loaded at once to get a better performance
        groupResourceAttrs.put(group,
            sess.getPerunBl().getAttributesManagerBl().getRequiredAttributes(sess, service, resource, group));
      } catch (GroupResourceMismatchException e) {
        throw new InternalErrorException(e);
      }
    }

    List<Group> notYetProcessedGroups = new ArrayList<>(groups);
    notYetProcessedGroups.removeAll(processedGroups);
    processedGroups.addAll(notYetProcessedGroups);

    groupAttrs.putAll(sess.getPerunBl().getAttributesManagerBl()
        .getRequiredAttributesForGroups(sess, service, notYetProcessedGroups));
  }

  @Override
  public void loadMemberGroupAttributes(Group group, List<Member> members) {
    lastLoadedGroup = group;
    memberGroupAttrs = new HashMap<>();
    try {
      memberGroupAttrs.putAll(
          sess.getPerunBl().getAttributesManagerBl().getRequiredAttributes(sess, service, members, group));
    } catch (MemberGroupMismatchException e) {
      throw new InternalErrorException(e);
    }
  }

  private void loadMemberSpecificAttributes(List<Member> members) {
    memberAttrs.putAll(sess.getPerunBl().getAttributesManagerBl().getRequiredAttributes(sess, null, service, members));

    List<Integer> userIds = members.stream().map(Member::getUserId).collect(toList());

    List<User> users = sess.getPerunBl().getUsersManagerBl().getUsersByIds(sess, userIds);

    Map<Integer, User> usersById = users.stream().collect(toMap(User::getId, Function.identity()));
    loadedUsersById.putAll(usersById);

    loadUserSpecificAttributes(users);
  }

  @Override
  public void loadResourceAttributes(Resource resource, List<Member> members, boolean loadVoAttributes) {
    lastLoadedResource = resource;

    if (!resourceAttrs.containsKey(resource)) {
      resourceAttrs.put(resource,
          sess.getPerunBl().getAttributesManagerBl().getRequiredAttributes(sess, service, resource));
    }

    if (loadVoAttributes) {
      loadVoSpecificAttributes(resource);
    }

    memberResourceAttrs =
        sess.getPerunBl().getAttributesManagerBl().getRequiredAttributes(sess, service, resource, members);

    // we don't need to load again attributes for the already processed members
    List<Member> notYetProcessedMembers = new ArrayList<>(members);
    notYetProcessedMembers.removeAll(processedMembers);
    processedMembers.addAll(notYetProcessedMembers);

    loadMemberSpecificAttributes(notYetProcessedMembers);
  }

  private void loadUserSpecificAttributes(List<User> users) {
    userAttrs.putAll(sess.getPerunBl().getAttributesManagerBl().getRequiredAttributes(sess, service, users));

    userFacilityAttrs.putAll(
        sess.getPerunBl().getAttributesManagerBl().getRequiredAttributes(sess, service, facility, users));
  }

  private void loadVoSpecificAttributes(Resource resource) {
    // create incomplete vo object with only vo id
    Vo vo = new Vo();
    vo.setId(resource.getVoId());
    lastLoadedResource = resource;

    if (!voAttrs.containsKey(vo)) {
      Vo dbVo;

      try {
        dbVo = sess.getPerunBl().getVosManagerBl().getVoById(sess, resource.getVoId());
      } catch (VoNotExistsException e) {
        throw new InternalErrorException(e);
      }

      voAttrs.put(vo, sess.getPerunBl().getAttributesManagerBl().getRequiredAttributes(sess, service, dbVo));
    }
  }
}
