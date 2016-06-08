package cz.metacentrum.perun.rpc.methods;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.rpc.ApiCaller;
import cz.metacentrum.perun.rpc.ManagerMethod;
import cz.metacentrum.perun.rpc.deserializer.Deserializer;
import cz.metacentrum.perun.cabinet.model.Author;
import cz.metacentrum.perun.cabinet.model.Authorship;
import cz.metacentrum.perun.cabinet.model.Category;
import cz.metacentrum.perun.cabinet.model.Publication;
import cz.metacentrum.perun.cabinet.model.PublicationForGUI;
import cz.metacentrum.perun.cabinet.model.PublicationSystem;
import cz.metacentrum.perun.cabinet.model.Thanks;
import cz.metacentrum.perun.cabinet.model.ThanksForGUI;
import cz.metacentrum.perun.cabinet.service.CabinetException;

public enum CabinetManagerMethod implements ManagerMethod {

	// SEARCH METHODS
	/*#
	 * Finds publications of perun's user specified in param
	 * Search is done in external publication systems (MU, ZCU)
	 * All parameters are required.
	 *
	 * @param user int Perun user
	 * @param yearSince int Year since
	 * @param yearTill int Year till - must be equal or greater then yearSince
	 * @param pubSysNamespace String (MU or ZCU)
	 * @return List<Publication> Found publications
	 */
	findExternalPublications {
		public List<Publication> call(ApiCaller ac, Deserializer parms) throws PerunException, CabinetException {
			return ac.getCabinetManager().findExternalPublications(ac.getSession(), parms.readInt("user"), parms.readInt("yearSince"), parms.readInt("yearTill"), parms.readString("pubSysNamespace"));
		}
	},

	/*#
		* Finds publications according to provided instance. All set
		* properties are used with conjunction AND.
		*
		* @param publication Publication JSON object
		* @return List<Publication> Found publications
		*/
	findPublicationByFilter {
		public List<Publication> call(ApiCaller ac, Deserializer parms) throws PerunException, CabinetException {
			return ac.getCabinetManager().findPublicationsByFilter(parms.read("publication", Publication.class));
		}
	},

	/*#
		* Finds similar publications
		*
		* @param title String Title
		* @param isbn String ISBN
		* @param doi String DOI
		*
		* @return List<PublicationForGUI> Found publications
		*/
	/*#
		* Finds similar publications
		*
		* @param isbn String ISBN
		* @param doi String DOI
		*
		* @return List<PublicationForGUI> Found publications
		*/
	/*#
		* Finds similar publications
		*
		* @param doi String DOI
		*
		* @return List<PublicationForGUI> Found publications
		*/
	/*#
		* Finds similar publications
		*
		* @param title String Title
		* @param doi String DOI
		*
		* @return List<PublicationForGUI> Found publications
		*/
	/*#
		* Finds similar publications
		*
		* @param title String Title
		*
		* @return List<PublicationForGUI> Found publications
		*/
	/*#
		* Finds similar publications
		*
		* @param isbn String ISBN
		* @param doi String DOI
		*
		* @return List<PublicationForGUI> Found publications
		*/
	/*#
		* Finds similar publications
		*
		* @param isbn String ISBN
		*
		* @return List<PublicationForGUI> Found publications
		*/
	findSimilarPublications {
		public Set<PublicationForGUI> call(ApiCaller ac, Deserializer parms) throws PerunException, CabinetException {

			Set<PublicationForGUI> result = new HashSet<PublicationForGUI>();

			int yearSince = 0;
			int yearTill = 0;
			Integer userId = null;

			if (parms.contains("title")) {
				Publication filter = new Publication();
				filter.setTitle(parms.readString("title"));
				result.addAll(ac.getCabinetManager().findRichPublicationsByGUIFilter(filter, userId, yearSince, yearTill));
			}
			if (parms.contains("isbn")) {
				Publication filter = new Publication();
				filter.setIsbn(parms.readString("isbn"));
				result.addAll(ac.getCabinetManager().findRichPublicationsByGUIFilter(filter, userId, yearSince, yearTill));
			}
			if (parms.contains("doi")) {
				Publication filter = new Publication();
				filter.setDoi(parms.readString("doi"));
				result.addAll(ac.getCabinetManager().findRichPublicationsByGUIFilter(filter, userId, yearSince, yearTill));
			}
			return result;

		}
	},

	/*#
		* Finds rich publications in Cabinet by GUI filter:
		*
		* id = exact match (used when search for publication of authors)
		* title = if "like" this substring
		* year = exact match
		* isbn = if "like" this substring
		* category = exact match
		* yearSince = if year >= yearSince
		* yearTill = if year <= yearTill
		*
		* If you don't want to filter by publication params, do not include the attribute in the query.
		*
		* @param id int Publication <code>id</code>
		* @param title String Title
		* @param isbn String ISBN
		* @param year int Year
		* @param category int Category
		* @param doi String DOI
		* @param locked boolean Publication locked
		* @param yearSince int Year since
		* @param yearTill int Year till
		* @param userId int User <code>id</code>
		* @return List<PublicationForGUI> Found publications
		*/
	findPublicationsByGUIFilter {
		public List<PublicationForGUI> call(ApiCaller ac, Deserializer parms) throws PerunException, CabinetException {

			// set filter
			Publication filter = new Publication();
			int yearSince = 0;
			int yearTill = 0;
			Integer userId = null;

			if (parms.contains("id")) {
				filter.setId(parms.readInt("id"));
			}
			if (parms.contains("title")) {
				filter.setTitle(parms.readString("title"));
			}
			if (parms.contains("isbn")) {
				filter.setIsbn(parms.readString("isbn"));
			}
			if (parms.contains("year")) {
				filter.setYear(parms.readInt("year"));
			}
			if (parms.contains("category")) {
				filter.setCategoryId(parms.readInt("category"));
			}
			if (parms.contains("doi")) {
				filter.setDoi(parms.readString("doi"));
			}
			if (parms.contains("locked")) {
				if (parms.readString("locked").equalsIgnoreCase("false")) {
					filter.setLocked(false);
				} else if (parms.readString("locked").equalsIgnoreCase("true")) {
					filter.setLocked(true);
				}
			}
			if (parms.contains("yearSince")) {
				yearSince = parms.readInt("yearSince");
			}
			if (parms.contains("yearTill")) {
				yearTill = parms.readInt("yearTill");
			}
			if (parms.contains("userId")) {
				// just to be safe
				if (parms.readInt("userId") != 0) {
					userId = parms.readInt("userId");
				}
			}

			// result list
			List<PublicationForGUI> result = new ArrayList<PublicationForGUI>();

			result = ac.getCabinetManager().findRichPublicationsByGUIFilter(filter, userId, yearSince, yearTill);

			return result;

		}
	},

	/*#
		* Returns a Publication by its <code>id</code>.
		* @param id int Publication <code>id</code>
		* @return PublicationForGUI found Publication
		*/
	findPublicationById {
		public PublicationForGUI call(ApiCaller ac, Deserializer parms) throws PerunException, CabinetException {
			return ac.getCabinetManager().findRichPublicationById(parms.readInt("id"));
		}
	},

	/*#
		* Returns all authorships.
		* @return List<Authorship> Authorships
		*/
	findAllAuthorships {
		public List<Authorship> call(ApiCaller ac, Deserializer parms) throws PerunException, CabinetException {
			return ac.getCabinetManager().findAllAuthorships();
		}
	},

	/*#
		* Returns all authorships according to a filter. Between filled properties is
		* used conjunction AND.
		*
		* @param authorship Authorship JSON object
		* @return List<Authorship> Authorships
		*/
	findAuthorshipsByFilter {
		public List<Authorship> call(ApiCaller ac, Deserializer parms) throws PerunException, CabinetException {
			return ac.getCabinetManager().findAuthorshipsByFilter(parms.read("authorship", Authorship.class));
		}
	},

	/*#
		* Returns an Authorship by its <code>id</code>.
		* @param id int Authorship <code>id</code>
		* @return Authorship found Authorship
		*/
	findAuthorshipById {
		public Authorship call(ApiCaller ac, Deserializer parms) throws PerunException, CabinetException {
			return ac.getCabinetManager().findAuthorshipById(parms.readInt("id"));
		}
	},

	/*#
		* Returns all authors.
		* @return List<Author> Authors
		*/
	findAllAuthors {
		public List<Author> call(ApiCaller ac, Deserializer parms) throws PerunException, CabinetException {
			return ac.getCabinetManager().findAllAuthors();
		}
	},

	/*#
		* Finds Authors of a Publication.
		* @param id int Publication <code>id</code>
		* @return List<Author> Authors
		*/
	findAuthorsByPublicationId {
		public List<Author> call(ApiCaller ac, Deserializer parms) throws PerunException, CabinetException {
			return ac.getCabinetManager().findAuthorsByPublicationId(parms.readInt("id"));
		}
	},

	/*#
		* Returns all Categories.
		* @return List<Category> Categories
		*/
	findAllCategories {
		public List<Category> call(ApiCaller ac, Deserializer parms) throws PerunException, CabinetException {
			return ac.getCabinetManager().findAllCategories();
		}
	},

	/*#
		* Finds thanks by a filter.
		* @param thanks Thanks JSON object
		* @return List<Thanks> Found thanks
		*/
	findThanksByFilter {
		public List<Thanks> call(ApiCaller ac, Deserializer parms) throws PerunException, CabinetException {
			return ac.getCabinetManager().findThanksByFilter(parms.read("thanks", Thanks.class));
		}
	},

	/*#
		* Finds thanks by a Publication.
		* @param id int Publication <code>id</code>
		* @return List<ThanksForGUI> Found thanks
		*/
	findThanksByPublicationId {
		public List<ThanksForGUI> call(ApiCaller ac, Deserializer parms) throws PerunException, CabinetException {
			return ac.getCabinetManager().findRichThanksByPublicationId(parms.readInt("id"));
		}
	},

	/*#
		* Returns all PublicationSystems.
		* @return List<PublicationSystem> Publication systems
		*/
	findAllPublicationSystems {
		public 	List<PublicationSystem> call(ApiCaller ac, Deserializer parms) throws PerunException, CabinetException {
			return ac.getCabinetManager().findAllPublicationSystems();
		}
	},

	// CREATE / UPDATE / DELETE / CHECK METHODS

	/*#
		* Creates a category.
		* @param category Category JSON object
		* @return Category Created category
		*/
	createCategory {
		public Category call(ApiCaller ac, Deserializer parms) throws PerunException, CabinetException {
			ac.stateChangingCheck();
			int id = ac.getCabinetManager().createCategory(parms.read("category", Category.class));
			return ac.getCabinetManager().findCategoryById(id);
		}
	},

	/*#
		* Updates a category.
		* @param category Category JSON object
		* @return Category Updated category
		*/
	updateCategory {
		public Category call(ApiCaller ac, Deserializer parms) throws PerunException, CabinetException {
			ac.stateChangingCheck();
			Category cat = parms.read("category", Category.class);
			ac.getCabinetManager().updateCategoryById(ac.getSession(), cat);
			return ac.getCabinetManager().findCategoryById(cat.getId());
		}
	},

	/*#
		* Deletes a category.
		* @param id int Category <code>id</code>
		*/
	deleteCategory {
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException, CabinetException {
			ac.stateChangingCheck();
			ac.getCabinetManager().deleteCategoryById(parms.readInt("id"));
			return null;
		}
	},

	/*#
		* Creates an Authorship.
		* If the authorship already exists, it's returned.
		* @param authorship Authorship JSON object
		* @return Authorship Authorship
		*/
	createAuthorship {
		public Authorship call(ApiCaller ac, Deserializer parms) throws PerunException, CabinetException {
			ac.stateChangingCheck();
			Authorship auth = parms.read("authorship", Authorship.class);
			if (ac.getCabinetManager().authorshipExists(auth)) {
				// exists - return existing
				// we must take only unique params, when called multiple time from GUI and entry was created by somebody else
				Authorship filterAuthorship = new Authorship();
				filterAuthorship.setPublicationId(auth.getPublicationId());
				filterAuthorship.setUserId(auth.getUserId());
				return ac.getCabinetManager().findAuthorshipsByFilter(filterAuthorship).get(0);
				// pubId and userId are unique and checked before, so we can safely return first and only authorship.
			} else {
				int id = ac.getCabinetManager().createAuthorship(ac.getSession(), parms.read("authorship", Authorship.class));
				return ac.getCabinetManager().findAuthorshipById(id);
			}
		}
	},

	/*#
		* Updates an Authorship.
		* @param authorship Authorship JSON object
		* @return Authorship Updated Authorship
		*/
	updateAuthorship {
		public Authorship call(ApiCaller ac, Deserializer parms) throws PerunException, CabinetException {
			ac.stateChangingCheck();
			Authorship a = parms.read("authorship", Authorship.class);
			ac.getCabinetManager().updateAuthorship(ac.getSession(), a);
			return ac.getCabinetManager().findAuthorshipById(a.getId());
		}
	},

	/*#
		* Deletes an Authorship.
		* @param publicationId int Publication <code>id</code>
		* @param userId int User <code>id</code>
		*/
	deleteAuthorship {
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException, CabinetException {
			ac.stateChangingCheck();
			Authorship filter = new Authorship();
			filter.setPublicationId(parms.readInt("publicationId"));
			filter.setUserId(parms.readInt("userId"));
			// pubId and UserId are unique, so return of first is safe
			Authorship authorship = ac.getCabinetManager().findAuthorshipsByFilter(filter).get(0);
			// delete
			ac.getCabinetManager().deleteAuthorshipById(ac.getSession(), authorship.getId());
			return null;
		}
	},

	/*#
		* Creates a new Publication.
		* If publication already exists, it's returned.
		*
		* @param publication Publication JSON object
		* @return Publication Publication
		*/
	createPublication {
		public Publication call(ApiCaller ac, Deserializer parms) throws PerunException, CabinetException {
			ac.stateChangingCheck();
			Publication pub = parms.read("publication", Publication.class);
			if (ac.getCabinetManager().publicationExists(pub)) {
				// if publication exists, do not create new
				Publication filter = new Publication();
				// get for external pubs
				if (pub.getExternalId() != 0 && pub.getPublicationSystemId() != 0) {
					filter.setExternalId(pub.getExternalId());
					filter.setPublicationSystemId(pub.getPublicationSystemId());
					// externalId and publicationSystemId are unique and checked before so we can safely return first and only publication.
					return ac.getCabinetManager().findRichPublicationsByFilter(filter, null).get(0);
					// for internal pubs
				}
			}
			// else create one
			int id = ac.getCabinetManager().createPublication(ac.getSession(), parms.read("publication", Publication.class));
			return ac.getCabinetManager().findRichPublicationById(id);
		}
	},

	/*#
		* Updates a Publication.
		*
		* @param publication Publication JSON object
		* @return Publication Updated Publication
		*/
	updatePublication {
		public PublicationForGUI call(ApiCaller ac, Deserializer parms) throws PerunException, CabinetException {
			ac.stateChangingCheck();
			Publication pub = parms.read("publication", Publication.class);
			ac.getCabinetManager().updatePublicationById(ac.getSession(), pub);
			return ac.getCabinetManager().findRichPublicationById(pub.getId());
		}
	},

	/*#
		* Deletes a Publication.
		* @param id int Publication <code>id</code>
		*/
	deletePublication {
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException, CabinetException {
			ac.stateChangingCheck();
			ac.getCabinetManager().deletePublicationById(ac.getSession(), parms.readInt("id"));
			return null;
		}
	},

	/*#
		* Checks whether a publication exists.
		* If you don't want to filter by a publication param, do not include the attribute in the query.
		*
		* @param externalId int External <code>id</code>
		* @param pubSysId int PubSys <code>id</code>
		* @param isbn String ISBN
		* @return boolean True if exists
		*/
	checkPublicationExists {
		public Boolean call(ApiCaller ac, Deserializer parms) throws PerunException, CabinetException {
			Publication pub = new Publication();
			if (parms.contains("externalId")) {
				pub.setExternalId(parms.readInt("externalId"));
			}
			if (parms.contains("pubSysId")) {
				pub.setPublicationSystemId(parms.readInt("pubSysId"));
			}
			if (parms.contains("isbn")) {
				pub.setIsbn(parms.readString("isbn"));
			}
			return ac.getCabinetManager().publicationExists(pub);
		}
	},

	/*#
	 * Locks and unlocks publications.
	 * @param publications List<Publication> Publications
	 * @param lock boolean true = lock, false = unlock
	 * @return int Number of updated rows
	 */
	lockPublications {
		public Integer call(ApiCaller ac, Deserializer parms) throws PerunException, CabinetException {
			ac.stateChangingCheck();

			List<Publication> pubs = parms.readList("publications", Publication.class);
			boolean lockState = parms.readBoolean("lock");
			return ac.getCabinetManager().lockPublications(ac.getSession(), lockState, pubs);

		}
	},

	/*#
		* Creates Thanks.
		* @param thanks Thanks JSON object
		* @return Thanks Created thanks
		*/
	createThanks {
		public Thanks call(ApiCaller ac, Deserializer parms) throws PerunException, CabinetException {
			ac.stateChangingCheck();
			int id = ac.getCabinetManager().createThanks(ac.getSession(), parms.read("thanks", Thanks.class));
			return ac.getCabinetManager().findThanksById(id);
		}
	},

	/*#
		* Deletes Thanks.
		* @param id int Thanks <code>id</code>
		*/
	deleteThanks {
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException, CabinetException {
			ac.stateChangingCheck();
			ac.getCabinetManager().deleteThanksById(ac.getSession(), parms.readInt("id"));
			return null;
		}
	},

	// OTHER METHODS

	/*#
		* Returns user's rank.
		*
		* @param user int User <code>id</code>
		* @return double User's rank
		*/
	getRank {
		public Double call(ApiCaller ac, Deserializer parms) throws PerunException, CabinetException {
			return ac.getCabinetManager().getRank(parms.readInt("user"));
		}
	},

	/*#
		* Recalculates "publications" attribute for
		* all users who reported any publication
		*/
	recalculateThanksAttribute {
		public Object call(ApiCaller ac, Deserializer parms) throws PerunException, CabinetException {
			ac.getCabinetManager().recalculateThanksAttribute(ac.getSession());
			return null;
		}
	};

}
