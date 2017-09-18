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
import cz.metacentrum.perun.cabinet.bl.CabinetException;

public enum CabinetManagerMethod implements ManagerMethod {

	/*#
	 * Get all PublicationSystems in Perun. If none, return empty list.
	 *
	 * @return List<PublicationSystem> List of all PublicationSystems or empty list.
	 */
	getPublicationSystems {
		public 	List<PublicationSystem> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getCabinetManager().getPublicationSystems();
		}
	},

	/*#
	 * Return list of all Categories in Perun or empty list of none present.
	 *
	 * @return List<Category> Categories
	 */
	getCategories {
		public List<Category> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getCabinetManager().getCategories();
		}
	},

	/*#
	 * Creates new Category for Publications with specified name and rank.
	 *
	 * @param category Category new Category object
	 * @return Category Created Category with ID set
	 */
	createCategory {
		public Category call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();
			return ac.getCabinetManager().createCategory(ac.getSession(), parms.read("category", Category.class));
		}
	},

	/*#
	 * Updates publications category in Perun. Category to update
	 * is found by ID. When category rank is changed, priorityCoefficient
	 * for all authors of books from this category, is recalculated.
	 *
	 * @param category Category to update to
	 * @return Category Updated category
	 * @throw CabinetException When Category doesn't exists
	 */
	updateCategory {
		public Category call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();
			return ac.getCabinetManager().updateCategory(ac.getSession(), parms.read("category", Category.class));
		}
	},

	/*#
	 * Delete category by its ID. If category contains any publications,
	 * it can't be deleted.
	 *
	 * @param id int Category <code>id</code>
	 * @throw CabinetException When Category doesn't exists or has publications
	 */
	deleteCategory {
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();
			ac.getCabinetManager().deleteCategory(ac.getSession(), ac.getCategoryById(parms.readInt("id")));
			return null;
		}
	},

	/*#
	 * Creates new Thanks for Publication
	 *
	 * @param thanks Thanks new Thanks object
	 * @return Thanks Created thanks
	 */
	createThanks {
		public Thanks call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();
			return ac.getCabinetManager().createThanks(ac.getSession(), parms.read("thanks", Thanks.class));
		}
	},

	/*#
	 * Delete Thanks by its ID.
	 *
	 * @param id int Thanks <code>id</code>
	 * @throw CabinetException When Thanks doesn't exists
	 */
	deleteThanks {
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();
			ac.getCabinetManager().deleteThanks(ac.getSession(), ac.getThanksById(parms.readInt("id")));
			return null;
		}
	},

	/*#
	 * Get ThanksForGUI of Publication specified by its ID or empty list.
	 *
	 * @param id int Publication <code>id</code>
	 * @return List<ThanksForGUI> Found thanks
	 */
	getRichThanksByPublicationId {
		public List<ThanksForGUI> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getCabinetManager().getRichThanksByPublicationId(parms.readInt("id"));
		}
	},

	/*#
	 * Creates Authorship. Everything except current date must be already set in Authorship object.
	 * Authorship is checked for existence before creation, if exists, existing object is returned.
	 * When authorship is successfully created, users priority coefficient is updated.
	 *
	 * @param authorship Authorship Authorship to be created
	 * @return Authorship Created authorship
	 */
	createAuthorship {
		public Authorship call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();
			Authorship auth = parms.read("authorship", Authorship.class);
			if (ac.getCabinetManager().authorshipExists(auth)) {
				// exists - return existing
				// we must take only unique params, when called multiple time from GUI and entry was created by somebody else
				return ac.getCabinetManager().getAuthorshipByUserAndPublicationId(auth.getUserId(), auth.getPublicationId());
				// pubId and userId are unique and checked before, so we can safely return first and only authorship.
			} else {
				return ac.getCabinetManager().createAuthorship(ac.getSession(), parms.read("authorship", Authorship.class));
			}
		}
	},

	/*#
	 * Delete Authorship by its userId and publicationId.
	 * @param publicationId int Publication <code>id</code>
	 * @param userId int User <code>id</code>
	 * @throw CabinetException When Authorship doesn't exists
	 */
	deleteAuthorship {
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();
			Authorship authorship = ac.getCabinetManager().getAuthorshipByUserAndPublicationId(parms.readInt("userId"),parms.readInt("publicationId"));
			ac.getCabinetManager().deleteAuthorship(ac.getSession(), authorship);
			return null;
		}
	},

	/*#
	 * Gets overall rank of given user as sum of all his publications Authorships.
	 *
	 * @param user int ID of user to get Rank for
	 * @return double Total rank of user or 1.0 if user has no Authorships yet (default rank).
	 */
	getRank {
		public Double call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getCabinetManager().getRank(parms.readInt("user"));
		}
	},

	/*#
	 * Return all Authors of Publication specified by its ID. Empty list of none found.
	 *
	 * @param id int ID of Publication to look by
	 * @return List<Author> List of Authors of Publication specified its ID. Empty list of none found.
	 */
	findAuthorsByPublicationId {
		public List<Author> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getCabinetManager().getAuthorsByPublicationId(ac.getSession(), parms.readInt("id"));
		}
	},

	/*#
	 * Return all Authors of Publications. Empty list of none found.
	 *
	 * @return List<Author> List of all Authors of Publications. Empty list of none found.
	 */
	findAllAuthors {
		public List<Author> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getCabinetManager().getAllAuthors(ac.getSession());
		}
	},

	/*#
	 * Find new Authors for Publication. Empty list of none found. Used by users to search for colleagues
	 * to add them as co-authors.
	 *
	 * @param searchString String Search string to find new Authors by
	 * @return List<Author> List of new possible Authors for Publication. Empty list of none found.
	 */
	findNewAuthors {
		public List<Author> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getCabinetManager().findNewAuthors(ac.getSession(), parms.readString("searchString"));
		}
	},

	/*#
	 * Create Publication. If exists by its ID or EXT_ID,PUB_SYS_ID then existing publication is returned.
	 *
	 * @param publication Publication Publication to create
	 * @return Publication Created publication with ID set
	 * @throw CabinetException
	 */
	createPublication {
		public Publication call(ApiCaller ac, Deserializer parms) throws PerunException, CabinetException {
			ac.stateChangingCheck();
			Publication pub = parms.read("publication", Publication.class);
			if (ac.getCabinetManager().publicationExists(pub)) {
				// get for external pubs
				if (pub.getExternalId() != 0 && pub.getPublicationSystemId() != 0) {
					// externalId and publicationSystemId are unique and checked before so we can safely return first and only publication.
					return ac.getCabinetManager().getRichPublicationByExternalId(pub.getExternalId(), pub.getPublicationSystemId());
					// for internal pubs
				}
			}
			// else create one
			Publication returnedPub = ac.getCabinetManager().createPublication(ac.getSession(), parms.read("publication", Publication.class));
			return ac.getCabinetManager().getRichPublicationById(returnedPub.getId());
		}
	},

	/*#
	 * Update existing publication by its ID.
	 *
	 * @param publication Publication Publication to update
	 * @return Publication Updated publication by its ID
	 * @throw CabinetException When same Publication already exists
	 */
	updatePublication {
		public PublicationForGUI call(ApiCaller ac, Deserializer parms) throws PerunException, CabinetException {
			ac.stateChangingCheck();
			Publication pub = parms.read("publication", Publication.class);
			ac.getCabinetManager().updatePublication(ac.getSession(), pub);
			return ac.getCabinetManager().getRichPublicationById(pub.getId());
		}
	},

	/*#
	 * Delete publication by its ID. Only Author of the record or PerunAdmin can do this.
	 *  - Author deletes Authorships and Thanks from publication.
	 *  - PerunAdmin also delete publication record.
	 *
	 * @param id int ID of Publication to delete
	 * @throw CabinetException When publication not exists
	 */
	deletePublication {
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();
			ac.getCabinetManager().deletePublication(ac.getSession(), ac.getPublicationById(parms.readInt("id")));
			return null;
		}
	},

	/*#
	 * Return Publication by its ID.
	 *
	 * @param id int ID of Publication
	 * @return PublicationForGUI by its ID
	 * @throw CabinetException When such Publication doesn't exists
	 */
	findPublicationById {
		public PublicationForGUI call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getCabinetManager().getRichPublicationById(parms.readInt("id"));
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
	 * userId = exact match or 0
	 *
	 * If you don't want to filter by publication params, do not include the attribute in the query.
	 *
	 * @param id int Publication <code>id</code>
	 * @param title String Title
	 * @param isbn String ISBN
	 * @param year int Year
	 * @param category int Category
	 * @param doi String DOI
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
			int userId = 0;

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
			if (parms.contains("yearSince")) {
				yearSince = parms.readInt("yearSince");
			}
			if (parms.contains("yearTill")) {
				yearTill = parms.readInt("yearTill");
			}
			if (parms.contains("userId")) {
				userId = parms.readInt("userId");
			}

			// result list
			List<PublicationForGUI> result = new ArrayList<PublicationForGUI>();

			result = ac.getCabinetManager().getRichPublicationsByFilter(filter, userId, yearSince, yearTill);

			return result;

		}
	},

	/*#
	 * (Un)Lock passed Publications for changes.
	 *
	 * @param lock boolean TRUE (lock) / FALSE (unlock)
	 * @param publications List<Publication> Publications to (un)lock
	 */
	lockPublications {
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();
			List<Publication> pubs = parms.readList("publications", Publication.class);
			boolean lock = parms.readBoolean("lock");
			ac.getCabinetManager().lockPublications(ac.getSession(), lock, pubs);
			return null;
		}
	},

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
			int userId = 0;

			if (parms.contains("title")) {
				Publication filter = new Publication();
				filter.setTitle(parms.readString("title"));
				result.addAll(ac.getCabinetManager().getRichPublicationsByFilter(filter, userId, yearSince, yearTill));
			}
			if (parms.contains("isbn")) {
				Publication filter = new Publication();
				filter.setIsbn(parms.readString("isbn"));
				result.addAll(ac.getCabinetManager().getRichPublicationsByFilter(filter, userId, yearSince, yearTill));
			}
			if (parms.contains("doi")) {
				Publication filter = new Publication();
				filter.setDoi(parms.readString("doi"));
				result.addAll(ac.getCabinetManager().getRichPublicationsByFilter(filter, userId, yearSince, yearTill));
			}
			return result;

		}
	},

}
