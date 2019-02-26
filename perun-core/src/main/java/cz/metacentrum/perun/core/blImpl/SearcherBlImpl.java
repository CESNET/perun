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
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.bl.SearcherBl;
import cz.metacentrum.perun.core.implApi.SearcherImplApi;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Searcher Class for searching objects by Map of Attributes
 *
 * @author Michal Stava <stavamichal@gmail.com>
 */
public class SearcherBlImpl implements SearcherBl {
	private final static Logger log = LoggerFactory.getLogger(SearcherBlImpl.class);

	private final SearcherImplApi searcherImpl;
	private PerunBl perunBl;

	public SearcherBlImpl(SearcherImplApi searcherImpl) {
		this.searcherImpl = searcherImpl;
	}

	@Override
	public List<User> getUsers(PerunSession sess, Map<String, String> attributesWithSearchingValues) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		//If there is no attribute, so every user match
		if(attributesWithSearchingValues == null || attributesWithSearchingValues.isEmpty()) {
			return perunBl.getUsersManagerBl().getUsers(sess);
		}

		Map<Attribute, String> mapOfAttrsWithValues = new HashMap<Attribute, String>();
		Map<AttributeDefinition, String> mapOfCoreAttributesWithValues = new HashMap<AttributeDefinition, String>();
		for(String name: attributesWithSearchingValues.keySet()) {
			if(name == null || name.equals("")) throw new AttributeNotExistsException("There is attribute with no specific name!");
			AttributeDefinition attrDef = perunBl.getAttributesManagerBl().getAttributeDefinition(sess, name);
			if(getPerunBl().getAttributesManagerBl().isCoreAttribute(sess, attrDef)) {
				mapOfCoreAttributesWithValues.put(attrDef, attributesWithSearchingValues.get(name));
			} else {
				mapOfAttrsWithValues.put(new Attribute(attrDef), attributesWithSearchingValues.get(name));
			}
		}

		List<User> usersFromCoreAttributes = this.getUsersForCoreAttributesByMapOfAttributes(sess, mapOfCoreAttributesWithValues);
		List<User> usersFromAttributes = getSearcherImpl().getUsers(sess, mapOfAttrsWithValues);
		usersFromAttributes.retainAll(usersFromCoreAttributes);
		return usersFromAttributes;
	}

	@Override
	public List<User> getUsersForCoreAttributes(PerunSession sess, Map<String, String> coreAttributesWithSearchingValues) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<User> users = getPerunBl().getUsersManagerBl().getUsers(sess);
		if(coreAttributesWithSearchingValues == null || coreAttributesWithSearchingValues.isEmpty()) return users;

		Map<AttributeDefinition, String> mapOfCoreAttributesWithValues = new HashMap<AttributeDefinition, String>();
		Set<String> keys = coreAttributesWithSearchingValues.keySet();
		for(String name: keys) {
			if(name == null || name.equals("")) throw new AttributeNotExistsException("There is attribute with no specific name!");
			AttributeDefinition attrDef = perunBl.getAttributesManagerBl().getAttributeDefinition(sess, name);
			if(getPerunBl().getAttributesManagerBl().isCoreAttribute(sess, attrDef)) {
				mapOfCoreAttributesWithValues.put(attrDef, coreAttributesWithSearchingValues.get(name));
			} else {
				throw new InternalErrorException("Attribute: " + attrDef + " is not core attribute! Can't be get for users by this method.");
			}
		}
		return this.getUsersForCoreAttributesByMapOfAttributes(sess, mapOfCoreAttributesWithValues);
	}

	@Override
	public List<Member> getMembersByExpiration(PerunSession sess, String operator, int days) throws InternalErrorException {
		return getSearcherImpl().getMembersByExpiration(sess, operator, null, days);
	}

	@Override
	public List<Member> getMembersByExpiration(PerunSession sess, String operator, LocalDate date) throws InternalErrorException {
		return getSearcherImpl().getMembersByExpiration(sess, operator, date, 0);
	}

	@Override
	public List<Member> getMembersByGroupExpiration(PerunSession sess, Group group, String operator, LocalDate date) throws InternalErrorException {
		return getSearcherImpl().getMembersByGroupExpiration(sess, group, operator, date, 0);
	}

	@Override
	public List<Group> getGroupsByGroupResourceSetting(PerunSession sess, Attribute groupResourceAttribute, Attribute resourceAttribute) throws InternalErrorException {
		if(groupResourceAttribute == null || groupResourceAttribute.getValue() == null || resourceAttribute == null || groupResourceAttribute == null) {
			throw new InternalErrorException("Can't find groups by attributes with null value.");
		}
		if(!groupResourceAttribute.getNamespace().equals(AttributesManager.NS_GROUP_RESOURCE_ATTR_DEF)) {
			throw new InternalErrorException("Group-resource attribute need to be in group-resource-def namespace! - " + groupResourceAttribute);
		}
		if(!resourceAttribute.getNamespace().equals(AttributesManager.NS_RESOURCE_ATTR_DEF)) {
			throw new InternalErrorException("Resource attribute need to be in resource-def namespace!" + resourceAttribute);
		}

		return getSearcherImpl().getGroupsByGroupResourceSetting(sess, groupResourceAttribute, resourceAttribute);
	}

	@Override
	public List<Facility> getFacilities(PerunSession sess, Map<String, String> attributesWithSearchingValues) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		if (attributesWithSearchingValues == null || attributesWithSearchingValues.isEmpty()) {
			return perunBl.getFacilitiesManagerBl().getFacilities(sess);
		}

		Map<Attribute, String> mapOfAttrsWithValues = new HashMap<>();
		Map<AttributeDefinition, String> mapOfCoreAttributesWithValues = new HashMap<>();

		for(String name: attributesWithSearchingValues.keySet()) {
			if(name == null || name.equals("")) {
				throw new AttributeNotExistsException("There is no attribute with specified name!");
			}

			AttributeDefinition attrDef = perunBl.getAttributesManagerBl().getAttributeDefinition(sess, name);

			if(getPerunBl().getAttributesManagerBl().isCoreAttribute(sess, attrDef)) {
				mapOfCoreAttributesWithValues.put(attrDef, attributesWithSearchingValues.get(name));
			} else {
				mapOfAttrsWithValues.put(new Attribute(attrDef), attributesWithSearchingValues.get(name));
			}
		}

		List<Facility> facilitiesFromCoreAttributes = getFacilitiesForCoreAttributesByMapOfAttributes(sess, mapOfCoreAttributesWithValues);
		List<Facility> facilitiesFromAttributes = getSearcherImpl().getFacilities(sess, mapOfAttrsWithValues);
		facilitiesFromCoreAttributes.retainAll(facilitiesFromAttributes);
		return facilitiesFromCoreAttributes;
	}

	@Override
	public List<Resource> getResources(PerunSession sess, Map<String, String> attributesWithSearchingValues) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		if (attributesWithSearchingValues == null || attributesWithSearchingValues.isEmpty()) {
			return perunBl.getResourcesManagerBl().getResources(sess);
		}

		Map<Attribute, String> mapOfAttrsWithValues = new HashMap<>();
		Map<AttributeDefinition, String> mapOfCoreAttributesWithValues = new HashMap<>();

		for(String name: attributesWithSearchingValues.keySet()) {
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

		List<Resource> resourcesFromCoreAttributes = getResourcesForCoreAttributesByMapOfAttributes(sess, mapOfCoreAttributesWithValues);
		List<Resource> resourcesFromAttributes = getSearcherImpl().getResources(sess, mapOfAttrsWithValues);
		resourcesFromCoreAttributes.retainAll(resourcesFromAttributes);
		return resourcesFromCoreAttributes;
	}

	private List<Facility> getFacilitiesForCoreAttributesByMapOfAttributes(PerunSession sess, Map<AttributeDefinition, String> coreAttributesWithSearchingValues) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<Facility> facilities = getPerunBl().getFacilitiesManagerBl().getFacilities(sess);
		if (coreAttributesWithSearchingValues == null || coreAttributesWithSearchingValues.isEmpty()) {
			return facilities;
		}

		Set<AttributeDefinition> keys = coreAttributesWithSearchingValues.keySet();
		for(Iterator<Facility> facilityIter = facilities.iterator(); facilityIter.hasNext();) {
			Facility facilityFromIterator = facilityIter.next();

			//Compare all needed attributes and their value to the attributes of every facility. If he does not fit, remove it from the array of returned facilities.
			for(AttributeDefinition attrDef: keys) {

				String value = coreAttributesWithSearchingValues.get(attrDef);
				Attribute attrForFacility = getPerunBl().getAttributesManagerBl().getAttribute(sess, facilityFromIterator, attrDef.getName());

				//One of attributes is not equal so remove him and continue with next facility
				if (!isAttributeValueMatching(attrForFacility, value)) {
					facilityIter.remove();
					break;
				}
			}
		}
		return facilities;
	}

	/**
	 * Find resources by core attribute values.
	 *
	 * @param sess session
	 * @param coreAttributesWithSearchingValues attributes with values
	 * @return list of resources
	 * @throws InternalErrorException internal error
	 * @throws AttributeNotExistsException attribute not exist
	 * @throws WrongAttributeAssignmentException wrong attribute assignment
	 */
	private List<Resource> getResourcesForCoreAttributesByMapOfAttributes(PerunSession sess, Map<AttributeDefinition, String> coreAttributesWithSearchingValues) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<Resource> resources = getPerunBl().getResourcesManagerBl().getResources(sess);
		if (coreAttributesWithSearchingValues == null || coreAttributesWithSearchingValues.isEmpty()) {
			return resources;
		}

		Set<AttributeDefinition> keys = coreAttributesWithSearchingValues.keySet();
		for (Iterator<Resource> resourceIterator = resources.iterator(); resourceIterator.hasNext();) {
			Resource resourceFromIterator = resourceIterator.next();

			//Compare all needed attributes and their value to the attributes of every resource. If he does not fit, remove it from the array of returned resources.
			for(AttributeDefinition attrDef: keys) {

				String value = coreAttributesWithSearchingValues.get(attrDef);
				Attribute attrForResource = getPerunBl().getAttributesManagerBl().getAttribute(sess, resourceFromIterator, attrDef.getName());

				//One of attributes is not equal so remove it and continue with next resource
				if (!isAttributeValueMatching(attrForResource, value)) {
					resourceIterator.remove();
					break;
				}
			}
		}

		return resources;
	}

	/**
	 * Returns true if the given value corresponds with value of given attribute.
	 *
	 * Accepted types of values are Integer and String. If given attribute has any other
	 * value type, exception is risen.
	 *
	 * @param entityAttribute attribute
	 * @param value value
	 * @return true, if the given value corresponds with value of given attribute
	 * @throws InternalErrorException internal error
	 */
	private boolean isAttributeValueMatching(Attribute entityAttribute, String value) throws InternalErrorException {
		boolean shouldBeAccepted = true;

		if(entityAttribute.getValue() == null) {
			//We are looking for entities with null value in this core attribute
			if(value!=null && !value.isEmpty()) {
				shouldBeAccepted = false;
			}
		} else {
			//We need to compare those values, if they are equals,
			if (entityAttribute.getValue() instanceof String) {
				String attrValue = entityAttribute.valueAsString();
				if (!attrValue.equals(value)) {
					shouldBeAccepted = false;
				}
			} else if (entityAttribute.getValue() instanceof Integer) {
				Integer attrValue = entityAttribute.valueAsInteger();
				Integer valueInInteger = Integer.valueOf(value);
				if (attrValue.intValue() != valueInInteger.intValue()) {
					shouldBeAccepted = false;
				}
			} else {
				throw new InternalErrorException("Core attribute: " + entityAttribute + " is not type of String or Integer!");
			}
		}

		return shouldBeAccepted;
	}

	/**
	 * This method take map of coreAttributes with search values and return all
	 * users who have the specific match for all of these core attributes.
	 *
	 * @param sess
	 * @param coreAttributesWithSearchingValues
	 * @return
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<User> getUsersForCoreAttributesByMapOfAttributes(PerunSession sess, Map<AttributeDefinition, String> coreAttributesWithSearchingValues) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<User> users = getPerunBl().getUsersManagerBl().getUsers(sess);
		if(coreAttributesWithSearchingValues == null || coreAttributesWithSearchingValues.isEmpty()) return users;

		Set<AttributeDefinition> keys = coreAttributesWithSearchingValues.keySet();
		for(Iterator<User> userIter = users.iterator(); userIter.hasNext();) {
			User userFromIterator = userIter.next();

			//Compare all needed attributes and their value to the attributes of every user. If he does not fit, remove him from the array of returned users.
			for(AttributeDefinition attrDef: keys) {
				String value = coreAttributesWithSearchingValues.get(attrDef);
				Attribute attrForUser = getPerunBl().getAttributesManagerBl().getAttribute(sess, userFromIterator, attrDef.getName());

				//One of attributes is not equal so remove him and continue with next user
				if(!isAttributeValueMatching(attrForUser, value)) {
					userIter.remove();
					break;
				}
			}
		}
		return users;
	}

	public SearcherImplApi getSearcherImpl() {
		return this.searcherImpl;
	}

	public PerunBl getPerunBl() {
		return this.perunBl;
	}

	public void setPerunBl(PerunBl perunBl) {
		this.perunBl = perunBl;
	}

}
