package cz.metacentrum.perun.core.blImpl;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.BeansUtils;
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
			for(AttributeDefinition attrDef: keys) {
				boolean userIsAccepted = true;
				String value = coreAttributesWithSearchingValues.get(attrDef);
				Attribute attrForUser = getPerunBl().getAttributesManagerBl().getAttribute(sess, userFromIterator, attrDef.getName());
				if(attrForUser.getType().equals("java.lang.String")) {
					String attrValue = (String) attrForUser.getValue();
					if(!attrValue.equals(value)) userIsAccepted = false;
				} else if(attrForUser.getType().equals("java.lang.Integer")) {
					Integer attrValue = (Integer) attrForUser.getValue();
					Integer valueInInteger = Integer.valueOf(value);
					if(attrValue.intValue() != valueInInteger.intValue()) userIsAccepted = false;
				} else {
					throw new InternalErrorException("Core attribute: " + attrForUser + " is not type of String or Integer!");
				}
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
