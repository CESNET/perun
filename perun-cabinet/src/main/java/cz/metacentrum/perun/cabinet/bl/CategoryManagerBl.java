package cz.metacentrum.perun.cabinet.bl;

import cz.metacentrum.perun.cabinet.model.Category;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import java.util.List;

/**
 * Interface for handling publication's categories
 *
 * @author Jiri Harazim <harazim@mail.muni.cz>
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public interface CategoryManagerBl {

  /**
   * Creates new Category for Publications with specified name and rank.
   *
   * @param sess     PerunSession
   * @param category new Category object
   * @return Created Category with ID set
   * @throws InternalErrorException When implementation fails
   */
  Category createCategory(PerunSession sess, Category category) throws CabinetException;

  /**
   * Delete category by its ID. If category contains any publications, it can't be deleted.
   *
   * @param sess     PerunSession
   * @param category Category to be deleted
   * @throws CabinetException       When Category doesn't exists or has publications
   * @throws InternalErrorException When implementation fails
   */
  void deleteCategory(PerunSession sess, Category category) throws CabinetException;

  /**
   * Return list of all Categories in Perun or empty list of none present.
   *
   * @return List of all categories
   * @throws InternalErrorException When implementation fails
   */
  List<Category> getCategories();

  /**
   * Get Category by its ID. Throws exception, if not exists.
   *
   * @param id ID of category to be found
   * @return Category by its ID.
   * @throws CabinetException       When Category doesn't exists
   * @throws InternalErrorException When implementation fails
   */
  Category getCategoryById(int id) throws CabinetException;

  /**
   * Updates publications category in Perun. Category to update is found by ID. When category rank is changed,
   * priorityCoefficient for all authors of books from this category, is recalculated.
   *
   * @param sess     PerunSession
   * @param category Category to update to
   * @return Updated category
   * @throws CabinetException       When Category doesn't exists
   * @throws InternalErrorException When implementation fails
   */
  Category updateCategory(PerunSession sess, Category category) throws CabinetException;

}
