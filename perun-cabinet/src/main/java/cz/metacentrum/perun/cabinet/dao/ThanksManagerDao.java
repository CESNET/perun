package cz.metacentrum.perun.cabinet.dao;

import cz.metacentrum.perun.cabinet.bl.CabinetException;
import cz.metacentrum.perun.cabinet.model.Thanks;
import cz.metacentrum.perun.cabinet.model.ThanksForGUI;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import java.util.List;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Interface of DAO layer for handling Thanks entity.
 *
 * @author Jiri Harazim <harazim@mail.muni.cz>
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public interface ThanksManagerDao {

  /**
   * Creates new Thanks for Publication
   *
   * @param sess   PerunSession
   * @param thanks new Thanks object
   * @return Created Thanks with ID set
   * @throws InternalErrorException When implementation fails
   */
  @Transactional(rollbackFor = Exception.class, propagation = Propagation.NESTED)
  Thanks createThanks(PerunSession sess, Thanks thanks) throws CabinetException;

  /**
   * Delete Thanks by its ID.
   *
   * @param sess   PerunSession
   * @param thanks Thanks to be deleted
   * @throws CabinetException       When Thanks doesn't exists
   * @throws InternalErrorException When implementation fails
   */
  @Transactional(rollbackFor = Exception.class, propagation = Propagation.NESTED)
  void deleteThanks(PerunSession sess, Thanks thanks) throws CabinetException;

  /**
   * Get ThanksForGUI of Publication specified by its ID or empty list.
   *
   * @param publicationId ID of Publication to get Thanks for
   * @return List of Publications Thanks
   * @throws InternalErrorException When implementation fails
   */
  List<ThanksForGUI> getRichThanksByPublicationId(int publicationId);

  /**
   * Get ThanksForGUI of User specified by its ID or empty list.
   *
   * @param userId ID of User to get Thanks for
   * @return List of Publications Thanks
   * @throws InternalErrorException When implementation fails
   */
  List<ThanksForGUI> getRichThanksByUserId(int userId);

  /**
   * Get Thanks by its ID. Throws exception, if not exists.
   *
   * @param id ID of Thanks to be found
   * @return Thanks by its ID.
   * @throws CabinetException       When Thanks doesn't exists
   * @throws InternalErrorException When implementation fails
   */
  Thanks getThanksById(int id) throws CabinetException;

  /**
   * Get Thanks of Publication specified by its ID or empty list.
   *
   * @param publicationId ID of Publication to get Thanks for
   * @return List of Publications Thanks
   * @throws InternalErrorException When implementation fails
   */
  List<Thanks> getThanksByPublicationId(int publicationId);

  /**
   * Check if same Thanks exists by ID or OwnerId,PublicationId combination.
   *
   * @param thanks Thanks to check by
   * @return TRUE = Thanks for same Owner and Publication or with same ID exists / FALSE = Same Thanks not found
   * @throws InternalErrorException When implementation fails
   */
  boolean thanksExist(Thanks thanks);

}
