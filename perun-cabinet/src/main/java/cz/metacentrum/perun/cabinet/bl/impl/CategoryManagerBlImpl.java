package cz.metacentrum.perun.cabinet.bl.impl;

import cz.metacentrum.perun.cabinet.bl.AuthorshipManagerBl;
import cz.metacentrum.perun.cabinet.bl.CabinetException;
import cz.metacentrum.perun.cabinet.bl.CabinetManagerBl;
import cz.metacentrum.perun.cabinet.bl.CategoryManagerBl;
import cz.metacentrum.perun.cabinet.bl.PublicationManagerBl;
import cz.metacentrum.perun.cabinet.dao.CategoryManagerDao;
import cz.metacentrum.perun.cabinet.model.Author;
import cz.metacentrum.perun.cabinet.model.Category;
import cz.metacentrum.perun.cabinet.model.Publication;
import cz.metacentrum.perun.core.api.PerunSession;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Class for handling Category entity in Cabinet.
 *
 * @author Jiri Harazim <harazim@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class CategoryManagerBlImpl implements CategoryManagerBl {

  private static Logger LOG = LoggerFactory.getLogger(CategoryManagerBlImpl.class);
  private CategoryManagerDao categoryManagerDao;
  private PublicationManagerBl publicationManagerBl;
  private CabinetManagerBl cabinetManagerBl;
  private AuthorshipManagerBl authorshipManagerBl;

  // setters ----------------------

  @Override
  public Category createCategory(PerunSession sess, Category category) throws CabinetException {
    Category newCategory = getCategoryManagerDao().createCategory(sess, category);
    LOG.debug("{} created.", newCategory);
    return newCategory;
  }

  @Override
  public void deleteCategory(PerunSession sess, Category category) throws CabinetException {
    getCategoryManagerDao().deleteCategory(sess, category);
    LOG.debug("{} deleted.", category);
  }

  public AuthorshipManagerBl getAuthorshipManagerBl() {
    return authorshipManagerBl;
  }

  public CabinetManagerBl getCabinetManagerBl() {
    return cabinetManagerBl;
  }

  @Override
  public List<Category> getCategories() {
    return getCategoryManagerDao().getCategories();
  }

  @Override
  public Category getCategoryById(int id) throws CabinetException {
    return getCategoryManagerDao().getCategoryById(id);
  }

  public CategoryManagerDao getCategoryManagerDao() {
    return categoryManagerDao;
  }

  public PublicationManagerBl getPublicationManagerBl() {
    return publicationManagerBl;
  }

  // methods ----------------------

  @Autowired
  public void setAuthorshipManagerBl(AuthorshipManagerBl authorshipManagerBl) {
    this.authorshipManagerBl = authorshipManagerBl;
  }

  @Autowired
  public void setCabinetManagerBl(CabinetManagerBl cabinetManagerBl) {
    this.cabinetManagerBl = cabinetManagerBl;
  }

  @Autowired
  public void setCategoryManagerDao(CategoryManagerDao categoryManagerDao) {
    this.categoryManagerDao = categoryManagerDao;
  }

  @Autowired
  public void setPublicationManagerBl(PublicationManagerBl publicationManagerBl) {
    this.publicationManagerBl = publicationManagerBl;
  }

  @Override
  public Category updateCategory(PerunSession sess, Category category) throws CabinetException {
    // save original category
    Category cat = getCategoryManagerDao().getCategoryById(category.getId());
    // update
    Category result = getCategoryManagerDao().updateCategory(sess, category);
    // was rank changed ?
    if (!Objects.equals(cat.getRank(), category.getRank())) {
      synchronized (CabinetManagerBlImpl.class) {
        // yes
        List<Publication> pubs = getPublicationManagerBl().getPublicationsByCategoryId(category.getId());

        // update coef for all authors of all publications in updated category
        Set<Author> authors = new HashSet<Author>();
        for (Publication p : pubs) {
          authors.addAll(getAuthorshipManagerBl().getAuthorsByPublicationId(p.getId()));
        }

        for (Author a : authors) {
          getCabinetManagerBl().updatePriorityCoefficient(sess, a.getId(),
              getAuthorshipManagerBl().calculateNewRank(a.getAuthorships()));
        }
      }
      LOG.debug("Category: [{}] updated to Category: [{}]", cat, category);
    }
    return result;
  }

}
