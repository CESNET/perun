package cz.metacentrum.perun.cabinet.dao;

import java.util.List;

import cz.metacentrum.perun.cabinet.model.Category;

/**
 * Interface of DAO layer for handling Category entity.
 *
 * @author Jiri Harazim <harazim@mail.muni.cz>
 */
public interface ICategoryDao {

	List<Category> findAllCategories();

	int createCategory(Category c);

	Category findCategoryById(Integer categoryId);

	int getCount();

	int updateCategoryById(Category category);

	int deleteCategoryById(Integer id);

}