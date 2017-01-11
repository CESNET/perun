package cz.metacentrum.perun.cabinet.dao.impl;

import java.util.List;

import cz.metacentrum.perun.cabinet.dao.CategoryManagerDao;
import cz.metacentrum.perun.cabinet.dao.mybatis.CategoryExample;
import cz.metacentrum.perun.cabinet.dao.mybatis.CategoryMapper;
import cz.metacentrum.perun.cabinet.model.Category;

/**
 * Class of DAO layer for handling Category entity.
 * Provides connection to proper mapper.
 *
 * @author Jiri Harazim <harazim@mail.muni.cz>
 */
public class CategoryManagerDaoImpl implements CategoryManagerDao {

	private CategoryMapper categoryMapper;

	// setters ----------------------

	public void setCategoryMapper(CategoryMapper categoryMapper) {
		this.categoryMapper = categoryMapper;
	}

	// methods ----------------------

	public List<Category> findAllCategories() {
		return categoryMapper.selectByExample(new CategoryExample());
	}

	/**
	 * For returning generated id visit:
	 * Works on MySQL, for oracle you must add selectKey probably (useGeneratedKey I think does not work)
	 * @see http://stackoverflow.com/questions/1769688/howto-return-ids-on-inserts-with-ibatis-with-returning-keyword
	 */

	public int createCategory(Category c) {
		categoryMapper.insert(c);
		return c.getId();
	}



	public Category findCategoryById(Integer categoryId) {
		return categoryMapper.selectByPrimaryKey(categoryId);
	}



	public int getCount() {
		return categoryMapper.countByExample(new CategoryExample());
	}



	public int updateCategoryById(Category category) {
		return categoryMapper.updateByPrimaryKey(category);
	}



	public int deleteCategoryById(Integer id) {
		return categoryMapper.deleteByPrimaryKey(id) ;
	}

}
