package cz.metacentrum.perun.core.entry;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Searcher;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.bl.SearcherBl;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Slavek Licehammer glory@ics.muni.cz
 */
public class SearcherEntry implements Searcher {

	final static Logger log = LoggerFactory.getLogger(ResourcesManagerEntry.class);

	private SearcherBl searcherBl;
	private PerunBl perunBl;

	public SearcherEntry(PerunBl perunBl) {
		this.perunBl = perunBl;
		this.searcherBl = perunBl.getSearcherBl();
	}

	public SearcherEntry() {
	}

	public List<User> getUsers(PerunSession sess, Map<String, String> attributesWithSearchingValues) throws InternalErrorException, AttributeNotExistsException, PrivilegeException, WrongAttributeAssignmentException {
		return searcherBl.getUsers(sess, attributesWithSearchingValues);
	}

	public List<User> getUsersForCoreAttributes(PerunSession sess, Map<String, String> coreAttributesWithSearchingValues) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException, PrivilegeException {
		return searcherBl.getUsersForCoreAttributes(sess, coreAttributesWithSearchingValues);
	}

	public SearcherBl getSearcherBl() {
		return this.searcherBl;
	}

	public void setPerunBl(PerunBl perunBl) {
		this.perunBl = perunBl;
	}

	public void setSearcherBl(SearcherBl searcherBl) {
		this.searcherBl = searcherBl;
	}

	public PerunBl getPerunBl() {
		return this.perunBl;
	}


}
