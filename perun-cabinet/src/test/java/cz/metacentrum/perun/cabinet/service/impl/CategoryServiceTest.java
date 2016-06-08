package cz.metacentrum.perun.cabinet.service.impl;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import cz.metacentrum.perun.cabinet.model.Category;
import cz.metacentrum.perun.cabinet.service.ICategoryService;

public class CategoryServiceTest extends BaseIntegrationTest {

	private ICategoryService categoryService;

	@Autowired
	public void setCategoryService(ICategoryService categoryService) {
		this.categoryService = categoryService;
	}

	// ------------- TESTS --------------------------------------------

	@Test
	public void createCategoryTest() throws Exception {
		System.out.println("CategoryService.createCategoryTest");

		Category c = new Category(null, "Patent3", 7.0);
		int id = categoryService.createCategory(c);

		assertTrue("New Category ID shouldn't be 0.", id > 0);
		assertTrue("Returned and Category ID doesn't match.", id == c.getId());

	}

	@Test
	public void updateCategoryTest() throws Exception {
		System.out.println("CategoryService.updateCategoryTest");

		Double oldRank = c1.getRank();
		c1.setRank(2.0);
		int result = categoryService.updateCategoryById(sess, c1);

		assertTrue("Category with ID: "+c1.getId()+" not found for update or not updated.", result > 0);
		assertTrue("Category rank was not changed during updated.", oldRank != c1.getRank());

	}

	@Test
	public void findAllCategoriesTest() throws Exception {
		System.out.println("CategoryService.findAllCategoriesTest");

		List<Category> categories = categoryService.findAllCategories();
		assertTrue("There should be at least 1 category.",categories.size() > 0);

	}

	@Test
	public void findCategoryByIdTest() throws Exception {
		System.out.println("CategoryService.findCategoryByIdTest");

		Category c2 = categoryService.findCategoryById(c1.getId());
		assertEquals("Original and retrieved Category by ID: "+c1.getId()+" are not same.", c1, c2);

	}

	@Test
	public void getCountTest() throws Exception {
		System.out.println("CategoryService.getCountTest");

		int count = categoryService.getCount();
		assertTrue("There should be some Categories but was "+count, count > 0);

	}

	@Test (expected=DataIntegrityViolationException.class)
		public void deleteCategoryWhenPublicationExistsTest() throws Exception {
			System.out.println("CategoryService.deleteCategoryWhenPublicationExistsTest");

			categoryService.deleteCategoryById(c1.getId());
			// should throw exception

		}

	@Test
	public void deleteCategoryTest() throws Exception {
		System.out.println("CategoryService.deleteCategoryTest");

		Category c = new Category(null, "Patent4", 1.0);
		int id = categoryService.createCategory(c);
		int result = categoryService.deleteCategoryById(id);

		assertTrue("There should be exactly 1 category deleted, but was: "+result,result == 1);

	}

}
