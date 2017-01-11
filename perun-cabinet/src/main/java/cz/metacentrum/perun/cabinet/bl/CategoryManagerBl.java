package cz.metacentrum.perun.cabinet.bl;

import java.util.List;

import cz.metacentrum.perun.cabinet.bl.CabinetException;
import cz.metacentrum.perun.cabinet.model.Category;
import cz.metacentrum.perun.core.api.PerunSession;

/**
 * Interface for handling publication's categories
 *
 * @author Jiri Harazim <harazim@mail.muni.cz>
 */
public interface CategoryManagerBl {

	/**
	 * Creates new category for publication
	 *
	 * @param c new Category object
	 * @return ID of new category
	 */
	int createCategory(Category c);

	/**
	 * Return list of all categories in Perun
	 *
	 * @return list of all categories
	 */
	List<Category> findAllCategories();

	/**
	 * Find category by it's ID. Return null if
	 * not found.
	 *
	 * @param categoryId ID of category to be found
	 * @return category
	 */
	Category findCategoryById(Integer categoryId);

	/**
	 * Return count of all categories
	 *
	 * @return count of all categories
	 */
	int getCount();

	/**
	 * Updates publications category in Perun. Category to update
	 * is found by ID. When category's rank is changed, priorityCoeficient
	 * for all authors of books from this category, is recalculated.
	 *
	 * @param sess PerunSession
	 * @param category Category to update to
	 * @return number of updated rows (1 = ok / 0 = not found / other = consistency error)
	 * @throws CabinetException
	 */
	int updateCategoryById(PerunSession sess, Category category) throws CabinetException;

	/**
	 * Delete category by ID. If category contains any publications,
	 * it can't be deleted.
	 *
	 * @param id ID of category to be deleted
	 * @return number of deleted row (1 = ok / 0 = not found / other = consistency error)
	 */
	int deleteCategoryById(Integer id);

}
