package cz.metacentrum.perun.core.blImpl;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.bl.SearcherBl;
import cz.metacentrum.perun.core.implApi.SearcherImplApi;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Searcher Class for searching objects by Map of Attributes
 *
 * @author Michal Stava <stavamichal@gmail.com>
 */
public class SearcherBlImpl implements SearcherBl {
  private static final Logger LOG = LoggerFactory.getLogger(SearcherBlImpl.class);

  private final SearcherImplApi searcherImpl;
  private PerunBl perunBl;

  public SearcherBlImpl(SearcherImplApi searcherImpl) {
    this.searcherImpl = searcherImpl;
  }

  @Override
  public List<Facility> getFacilities(PerunSession sess, Map<String, String> attributesWithSearchingValues)
      throws AttributeNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException {
    if (attributesWithSearchingValues == null || attributesWithSearchingValues.isEmpty()) {
      return perunBl.getFacilitiesManagerBl().getFacilities(sess);
    }

    Map<Attribute, String> mapOfAttrsWithValues = new HashMap<>();
    Map<AttributeDefinition, String> mapOfCoreAttributesWithValues = new HashMap<>();

    for (String name : attributesWithSearchingValues.keySet()) {
      if (name == null || name.equals("")) {
        throw new AttributeNotExistsException("There is no attribute with specified name!");
      }

      AttributeDefinition attrDef = perunBl.getAttributesManagerBl().getAttributeDefinition(sess, name);

      if (getPerunBl().getAttributesManagerBl().isCoreAttribute(sess, attrDef)) {
        mapOfCoreAttributesWithValues.put(attrDef, attributesWithSearchingValues.get(name));
      } else {
        mapOfAttrsWithValues.put(new Attribute(attrDef), attributesWithSearchingValues.get(name));
      }
    }

    List<Facility> facilitiesFromCoreAttributes =
        getFacilitiesForCoreAttributesByMapOfAttributes(sess, mapOfCoreAttributesWithValues);
    List<Facility> facilitiesFromAttributes = getSearcherImpl().getFacilities(sess, mapOfAttrsWithValues);
    facilitiesFromCoreAttributes.retainAll(facilitiesFromAttributes);
    return facilitiesFromCoreAttributes;
  }

  private List<Facility> getFacilitiesForCoreAttributesByMapOfAttributes(PerunSession sess,
                                               Map<AttributeDefinition, String> coreAttributesWithSearchingValues)
      throws AttributeNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException {
    List<Facility> facilities = getPerunBl().getFacilitiesManagerBl().getFacilities(sess);
    if (coreAttributesWithSearchingValues == null || coreAttributesWithSearchingValues.isEmpty()) {
      return facilities;
    }

    Set<AttributeDefinition> keys = coreAttributesWithSearchingValues.keySet();
    for (Iterator<Facility> facilityIter = facilities.iterator(); facilityIter.hasNext(); ) {
      Facility facilityFromIterator = facilityIter.next();

      //Compare all needed attributes and their value to the attributes of every facility. If he does not fit, remove
      // it from the array of returned facilities.
      for (AttributeDefinition attrDef : keys) {

        String value = coreAttributesWithSearchingValues.get(attrDef);
        Attribute attrForFacility =
            getPerunBl().getAttributesManagerBl().getAttribute(sess, facilityFromIterator, attrDef.getName());

        //One of attributes is not equal so remove him and continue with next facility
        if (!isAttributeValueMatching(attrForFacility, value, false)) {
          facilityIter.remove();
          break;
        }
      }
    }
    return facilities;
  }

  @Override
  public List<Group> getGroups(PerunSession sess, Vo vo, Map<String, String> attributesWithSearchingValues)
      throws AttributeNotExistsException, WrongAttributeValueException {
    return this.getGroups(sess, attributesWithSearchingValues).stream().filter(group -> group.getVoId() == vo.getId())
        .collect(Collectors.toList());
  }

  @Override
  public List<Group> getGroups(PerunSession sess, Map<String, String> attributesWithSearchingValues)
      throws AttributeNotExistsException, WrongAttributeValueException {
    //If there is no attribute, so every group match
    if (attributesWithSearchingValues == null || attributesWithSearchingValues.isEmpty()) {
      return perunBl.getVosManagerBl().getVos(sess).stream()
          .flatMap(vo -> perunBl.getGroupsManagerBl().getAllGroups(sess, vo).stream()).collect(Collectors.toList());
    }

    Map<Attribute, String> mapOfAttrsWithValues = new HashMap<>();
    for (String name : attributesWithSearchingValues.keySet()) {
      if (name == null || name.isEmpty()) {
        throw new AttributeNotExistsException("There is attribute with no specific name!");
      }
      AttributeDefinition attrDef = perunBl.getAttributesManagerBl().getAttributeDefinition(sess, name);
      if (getPerunBl().getAttributesManagerBl().isCoreAttribute(sess, attrDef)) {
        //skip core attributes, there are unsupported at this moment
        continue;
      } else {
        mapOfAttrsWithValues.put(new Attribute(attrDef), attributesWithSearchingValues.get(name));
      }
    }

    return getSearcherImpl().getGroups(sess, mapOfAttrsWithValues);
  }

  @Override
  public List<Group> getGroupsByGroupResourceSetting(PerunSession sess, Attribute groupResourceAttribute,
                                                     Attribute resourceAttribute) {
    if (groupResourceAttribute == null || groupResourceAttribute.getValue() == null || resourceAttribute == null ||
        resourceAttribute.getValue() == null) {
      throw new InternalErrorException("Can't find groups by attributes with null value.");
    }
    if (!groupResourceAttribute.getNamespace().equals(AttributesManager.NS_GROUP_RESOURCE_ATTR_DEF)) {
      throw new InternalErrorException(
          "Group-resource attribute need to be in group-resource-def namespace! - " + groupResourceAttribute);
    }
    if (!resourceAttribute.getNamespace().equals(AttributesManager.NS_RESOURCE_ATTR_DEF)) {
      throw new InternalErrorException("Resource attribute need to be in resource-def namespace!" + resourceAttribute);
    }

    return getSearcherImpl().getGroupsByGroupResourceSetting(sess, groupResourceAttribute, resourceAttribute);
  }

  @Override
  public List<Integer> getGroupsIdsForAppAutoRejection() {
    return getSearcherImpl().getGroupsIdsForAppAutoRejection();
  }

  @Override
  public List<Member> getMembersByExpiration(PerunSession sess, String operator, int days) {
    return getSearcherImpl().getMembersByExpiration(sess, operator, null, days);
  }

  @Override
  public List<Member> getMembersByExpiration(PerunSession sess, String operator, LocalDate date) {
    return getSearcherImpl().getMembersByExpiration(sess, operator, date, 0);
  }

  @Override
  public List<Member> getMembersByGroupExpiration(PerunSession sess, Group group, String operator, LocalDate date) {
    return getSearcherImpl().getMembersByGroupExpiration(sess, group, operator, date, 0);
  }

  public PerunBl getPerunBl() {
    return this.perunBl;
  }

  @Override
  public List<Resource> getResources(PerunSession sess, Map<String, String> attributesWithSearchingValues,
                                     boolean allowPartialMatchForString)
      throws AttributeNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException {
    if (attributesWithSearchingValues == null || attributesWithSearchingValues.isEmpty()) {
      return perunBl.getResourcesManagerBl().getResources(sess);
    }

    Map<Attribute, String> mapOfAttrsWithValues = new HashMap<>();
    Map<AttributeDefinition, String> mapOfCoreAttributesWithValues = new HashMap<>();

    for (String name : attributesWithSearchingValues.keySet()) {
      if (name == null || name.isEmpty()) {
        throw new AttributeNotExistsException("There is no attribute with specified name!");
      }

      AttributeDefinition attrDef = perunBl.getAttributesManagerBl().getAttributeDefinition(sess, name);

      if (getPerunBl().getAttributesManagerBl().isCoreAttribute(sess, attrDef)) {
        mapOfCoreAttributesWithValues.put(attrDef, attributesWithSearchingValues.get(name));
      } else {
        mapOfAttrsWithValues.put(new Attribute(attrDef), attributesWithSearchingValues.get(name));
      }
    }

    List<Resource> resourcesFromCoreAttributes =
        getResourcesForCoreAttributesByMapOfAttributes(sess, mapOfCoreAttributesWithValues, allowPartialMatchForString);
    List<Resource> resourcesFromAttributes =
        getSearcherImpl().getResources(sess, mapOfAttrsWithValues, allowPartialMatchForString);
    resourcesFromCoreAttributes.retainAll(resourcesFromAttributes);
    return resourcesFromCoreAttributes;
  }

  /**
   * Find resources by core attribute values.
   *
   * @param sess                              session
   * @param coreAttributesWithSearchingValues attributes with values
   * @param allowPartialMatchForString        if true, we are looking for partial matches, if false, we are looking only
   *                                          for total matches (only for STRING type attributes)
   * @return list of resources
   * @throws InternalErrorException            internal error
   * @throws AttributeNotExistsException       attribute not exist
   * @throws WrongAttributeAssignmentException wrong attribute assignment
   * @throws WrongAttributeValueException      wrong attribute value
   */
  private List<Resource> getResourcesForCoreAttributesByMapOfAttributes(PerunSession sess,
                                                Map<AttributeDefinition, String> coreAttributesWithSearchingValues,
                                                boolean allowPartialMatchForString)
      throws AttributeNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException {
    List<Resource> resources = getPerunBl().getResourcesManagerBl().getResources(sess);
    if (coreAttributesWithSearchingValues == null || coreAttributesWithSearchingValues.isEmpty()) {
      return resources;
    }

    Set<AttributeDefinition> keys = coreAttributesWithSearchingValues.keySet();
    for (Iterator<Resource> resourceIterator = resources.iterator(); resourceIterator.hasNext(); ) {
      Resource resourceFromIterator = resourceIterator.next();

      //Compare all needed attributes and their value to the attributes of every resource. If he does not fit, remove
      // it from the array of returned resources.
      for (AttributeDefinition attrDef : keys) {

        String value = coreAttributesWithSearchingValues.get(attrDef);
        Attribute attrForResource =
            getPerunBl().getAttributesManagerBl().getAttribute(sess, resourceFromIterator, attrDef.getName());

        //One of attributes is not equal so remove it and continue with next resource
        if (!isAttributeValueMatching(attrForResource, value, allowPartialMatchForString)) {
          resourceIterator.remove();
          break;
        }
      }
    }

    return resources;
  }

  public SearcherImplApi getSearcherImpl() {
    return this.searcherImpl;
  }

  @Override
  public List<User> getUsers(PerunSession sess, Map<String, String> attributesWithSearchingValues)
      throws AttributeNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException {
    //If there is no attribute, so every user match
    if (attributesWithSearchingValues == null || attributesWithSearchingValues.isEmpty()) {
      return perunBl.getUsersManagerBl().getUsers(sess);
    }

    Map<Attribute, String> mapOfAttrsWithValues = new HashMap<>();
    Map<AttributeDefinition, String> mapOfCoreAttributesWithValues = new HashMap<>();
    for (String name : attributesWithSearchingValues.keySet()) {
      if (name == null || name.equals("")) {
        throw new AttributeNotExistsException("There is attribute with no specific name!");
      }
      AttributeDefinition attrDef = perunBl.getAttributesManagerBl().getAttributeDefinition(sess, name);
      if (getPerunBl().getAttributesManagerBl().isCoreAttribute(sess, attrDef)) {
        mapOfCoreAttributesWithValues.put(attrDef, attributesWithSearchingValues.get(name));
      } else {
        mapOfAttrsWithValues.put(new Attribute(attrDef), attributesWithSearchingValues.get(name));
      }
    }

    List<User> usersFromCoreAttributes =
        this.getUsersForCoreAttributesByMapOfAttributes(sess, mapOfCoreAttributesWithValues);
    List<User> usersFromAttributes = getSearcherImpl().getUsers(sess, mapOfAttrsWithValues);
    usersFromAttributes.retainAll(usersFromCoreAttributes);
    return usersFromAttributes;
  }

  @Override
  public List<User> getUsersForCoreAttributes(PerunSession sess, Map<String, String> coreAttributesWithSearchingValues)
      throws AttributeNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException {
    List<User> users = getPerunBl().getUsersManagerBl().getUsers(sess);
    if (coreAttributesWithSearchingValues == null || coreAttributesWithSearchingValues.isEmpty()) {
      return users;
    }

    Map<AttributeDefinition, String> mapOfCoreAttributesWithValues = new HashMap<>();
    Set<String> keys = coreAttributesWithSearchingValues.keySet();
    for (String name : keys) {
      if (name == null || name.equals("")) {
        throw new AttributeNotExistsException("There is attribute with no specific name!");
      }
      AttributeDefinition attrDef = perunBl.getAttributesManagerBl().getAttributeDefinition(sess, name);
      if (getPerunBl().getAttributesManagerBl().isCoreAttribute(sess, attrDef)) {
        mapOfCoreAttributesWithValues.put(attrDef, coreAttributesWithSearchingValues.get(name));
      } else {
        throw new InternalErrorException(
            "Attribute: " + attrDef + " is not core attribute! Can't be get for users by this method.");
      }
    }
    return this.getUsersForCoreAttributesByMapOfAttributes(sess, mapOfCoreAttributesWithValues);
  }

  /**
   * This method take map of coreAttributes with search values and return all users who have the specific match for all
   * of these core attributes.
   *
   * @param sess
   * @param coreAttributesWithSearchingValues
   * @return
   * @throws InternalErrorException
   * @throws AttributeNotExistsException
   * @throws WrongAttributeAssignmentException
   * @throws WrongAttributeValueException
   */
  private List<User> getUsersForCoreAttributesByMapOfAttributes(PerunSession sess,
                                                  Map<AttributeDefinition, String> coreAttributesWithSearchingValues)
      throws AttributeNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException {
    List<User> users = getPerunBl().getUsersManagerBl().getUsers(sess);
    if (coreAttributesWithSearchingValues == null || coreAttributesWithSearchingValues.isEmpty()) {
      return users;
    }

    Set<AttributeDefinition> keys = coreAttributesWithSearchingValues.keySet();
    for (Iterator<User> userIter = users.iterator(); userIter.hasNext(); ) {
      User userFromIterator = userIter.next();

      //Compare all needed attributes and their value to the attributes of every user. If he does not fit, remove him
      // from the array of returned users.
      for (AttributeDefinition attrDef : keys) {
        String value = coreAttributesWithSearchingValues.get(attrDef);
        Attribute attrForUser =
            getPerunBl().getAttributesManagerBl().getAttribute(sess, userFromIterator, attrDef.getName());

        //One of attributes is not equal so remove him and continue with next user
        if (!isAttributeValueMatching(attrForUser, value, false)) {
          userIter.remove();
          break;
        }
      }
    }
    return users;
  }

  @Override
  public List<Integer> getVosIdsForAppAutoRejection() {
    return getSearcherImpl().getVosIdsForAppAutoRejection();
  }

  /**
   * Returns true if the given value corresponds with value of given attribute.
   * <p>
   * Accepted types of values are Integer and String. If given attribute has any other value type, exception is risen.
   *
   * @param entityAttribute            attribute
   * @param allowPartialMatchForString if true, we are looking for partial match without case sensitivity, if false, we
   *                                   are looking only for total matches with case sensitivity (only for STRING type
   *                                   attributes)
   * @param value                      value
   * @return true, if the given value corresponds with value of given attribute
   * @throws InternalErrorException internal error
   */
  private boolean isAttributeValueMatching(Attribute entityAttribute, String value,
                                           boolean allowPartialMatchForString) throws WrongAttributeValueException {
    boolean shouldBeAccepted = true;

    if (entityAttribute.getValue() == null) {
      //We are looking for entities with null value in this core attribute
      if (value != null && !value.isEmpty()) {
        shouldBeAccepted = false;
      }
    } else {
      //We need to compare those values, if they are equals,
      if (entityAttribute.getValue() instanceof String) {
        String attrValue = entityAttribute.valueAsString();
        if (allowPartialMatchForString) {
          if (!attrValue.toLowerCase().contains(value.toLowerCase())) {
            shouldBeAccepted = false;
          }
        } else {
          if (!attrValue.equals(value)) {
            shouldBeAccepted = false;
          }
        }
      } else if (entityAttribute.getValue() instanceof Integer) {
        try {
          Integer attrValue = entityAttribute.valueAsInteger();
          int valueInInteger = Integer.parseInt(value);
          if (attrValue != valueInInteger) {
            shouldBeAccepted = false;
          }
        } catch (NumberFormatException ex) {
          throw new WrongAttributeValueException(
              "Searched value for core attribute: " + entityAttribute + " should be type of Integer");
        }
      } else if (entityAttribute.getValue() instanceof Boolean) {
        if (!value.equals("false") && !value.equals("true")) {
          throw new WrongAttributeValueException(
              "Searched value for core attribute: " + entityAttribute + " should be 'true' or 'false'");
        }
        Boolean attrValue = entityAttribute.valueAsBoolean();
        boolean valueInBoolean = Boolean.parseBoolean(value);
        if (attrValue != valueInBoolean) {
          shouldBeAccepted = false;
        }
      } else {
        throw new InternalErrorException(
            "Core attribute: " + entityAttribute + " is not type of String, Integer or Boolean!");
      }
    }

    return shouldBeAccepted;
  }

  public void setPerunBl(PerunBl perunBl) {
    this.perunBl = perunBl;
  }

}
