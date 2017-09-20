package cz.metacentrum.perun.core.blImpl;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.bl.SearcherBl;
import cz.metacentrum.perun.core.implApi.SearcherImplApi;

import java.util.ArrayList;
import java.util.Calendar;
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
	public List<Member> getMembersByExpiration(PerunSession sess, String operator, Calendar date) throws InternalErrorException {
		return getSearcherImpl().getMembersByExpiration(sess, operator, date, 0);
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
				boolean userIsAccepted = true;
				String value = coreAttributesWithSearchingValues.get(attrDef);
				Attribute attrForUser = getPerunBl().getAttributesManagerBl().getAttribute(sess, userFromIterator, attrDef.getName());

				if(attrForUser.getValue() == null) {
					//We are looking for users with null value in this core attribute
					if(value!=null && !value.isEmpty()) userIsAccepted = false;
				} else {
					//We need to compare those values, if they are equals,
					if (attrForUser.getValue() instanceof String) {
						String attrValue = (String) attrForUser.getValue();
						if (!attrValue.equals(value)) userIsAccepted = false;
					} else if (attrForUser.getValue() instanceof Integer) {
						Integer attrValue = (Integer) attrForUser.getValue();
						Integer valueInInteger = Integer.valueOf(value);
						if (attrValue.intValue() != valueInInteger.intValue()) userIsAccepted = false;
					} else {
						throw new InternalErrorException("Core attribute: " + attrForUser + " is not type of String or Integer!");
					}
				}

				//One of attributes is not equal so remove him and continue with next user
				if(!userIsAccepted) {
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
