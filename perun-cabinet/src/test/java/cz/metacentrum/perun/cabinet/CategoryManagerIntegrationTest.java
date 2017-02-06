package cz.metacentrum.perun.cabinet;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Objects;

import cz.metacentrum.perun.cabinet.bl.CabinetException;
import org.junit.Test;

import cz.metacentrum.perun.cabinet.model.Category;

/**
 * Integration tests of AuthorshipManager
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class CategoryManagerIntegrationTest extends CabinetBaseIntegrationTest {

	@Test
	public void createCategoryTest() throws Exception {
		System.out.println("CategoryManagerIntegrationTest.createCategoryTest");

		Category c = new Category(0, "Patent3", 7.0);
		c = getCabinetManager().createCategory(sess, c);

		Category retrievedCategory = getCabinetManager().getCategoryById(c.getId());
		assertTrue(Objects.equals(c, retrievedCategory));

	}

	@Test
	public void updateCategoryTest() throws Exception {
		System.out.println("CategoryManagerIntegrationTest.updateCategoryTest");

		Double oldRank = c1.getRank();
		c1.setRank(2.0);
		getCabinetManager().updateCategory(sess, c1);

		assertTrue("Category rank was not changed during updated.", !Objects.equals(oldRank, c1.getRank()));

	}

	@Test
	public void getCategoriesTest() throws Exception {
		System.out.println("CategoryManagerIntegrationTest.getCategoriesTest");

		List<Category> categories = getCabinetManager().getCategories();
		assertTrue("There should be at least 1 category.",categories.size() > 0);

	}

	@Test
	public void getCategoryByIdTest() throws Exception {
		System.out.println("CategoryManagerIntegrationTest.getCategoryByIdTest");

		Category c2 = getCabinetManager().getCategoryById(c1.getId());
		assertEquals("Original and retrieved Category by ID: "+c1.getId()+" are not same.", c1, c2);

	}

	@Test (expected=CabinetException.class)
	public void deleteCategoryWhenPublicationExistsTest() throws Exception {
		System.out.println("CategoryManagerIntegrationTest.deleteCategoryWhenPublicationExistsTest");

		getCabinetManager().deleteCategory(sess, c1);
		// should throw exception
	}

	@Test (expected=CabinetException.class)
	public void deleteCategoryWhenNotExistTest() throws Exception {
		System.out.println("CategoryManagerIntegrationTest.deleteCategoryWhenNotExistTest");

		getCabinetManager().deleteCategory(sess, new Category(0, "test", 1.0));
		// should throw exception
	}

	@Test
	public void deleteCategoryTest() throws Exception {
		System.out.println("CategoryManagerIntegrationTest.deleteCategoryTest");

		Category c = new Category(0, "Patent4", 1.0);
		c = getCabinetManager().createCategory(sess, c);
		getCabinetManager().deleteCategory(sess, c);

		List<Category> categories = getCabinetManager().getCategories();
		assertTrue(categories != null);
		assertTrue(!categories.isEmpty());
		assertTrue(!categories.contains(c));

	}

}
