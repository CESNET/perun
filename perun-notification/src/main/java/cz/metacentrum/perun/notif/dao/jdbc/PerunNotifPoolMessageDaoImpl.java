package cz.metacentrum.perun.notif.dao.jdbc;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.bl.DatabaseManagerBl;
import cz.metacentrum.perun.core.impl.Compatibility;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.notif.dao.PerunNotifPoolMessageDao;
import cz.metacentrum.perun.notif.dto.PoolMessage;
import cz.metacentrum.perun.notif.entities.PerunNotifPoolMessage;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository("perunNotifPoolMessageDao")
@Transactional(propagation = Propagation.REQUIRED)
public class PerunNotifPoolMessageDaoImpl extends JdbcDaoSupport implements PerunNotifPoolMessageDao {

  private static final Logger logger = LoggerFactory.getLogger(PerunNotifPoolMessageDao.class);

  public void savePerunNotifPoolMessage(PerunNotifPoolMessage message) {

    logger.debug("Saving perunNotifPoolMessage to db, message: {}", message);
    int newMessageId = Utils.getNewId(this.getJdbcTemplate(), "pn_pool_message_id_seq");
    logger.debug("New id created for poolMessage: {}", newMessageId);

    String serializedKeyAttributes = null;
    try {
      logger.debug("Serializing keyAttributes to string, key: {}", message.getKeyAttributes());
      serializedKeyAttributes = message.getSerializedKeyAttributes();
      logger.debug("KeyAttributes serialized: {}", serializedKeyAttributes);
    } catch (UnsupportedEncodingException ex) {
      logger.error("Error during encoding map for perunNotifPoolMessage.", ex);
      throw new InternalErrorException(ex);
    }

    if (message.getCreated() == null) {
      message.setCreated(Instant.now());
    }
    this.getJdbcTemplate().update(
        "insert into pn_pool_message" + "(id, regex_id, template_id, key_attributes, notif_message, created) " +
            "values (?,?,?,?,?,?)",
        newMessageId, message.getRegexId(), message.getTemplateId(), serializedKeyAttributes, message.getNotifMessage(),
        new Timestamp(message.getCreated().toEpochMilli()));

    message.setId(newMessageId);

    logger.debug("PoolMessage saved: {}", message);
  }

  @Override
  public Map<Integer, List<PoolMessage>> getAllPoolMessagesForProcessing() {

    Duration days = Duration.ofDays(10);
    removeOldPoolMessages(days.toMillis());

    logger.debug("Getting all poolMessages from db.");
    Map<Integer, List<PoolMessage>> result = this.getJdbcTemplate()
        .query("SELECT * FROM pn_pool_message ORDER BY key_attributes ASC, template_id ASC, created ASC ",
            new PerunNotifPoolMessage.PERUN_NOTIF_POOL_MESSAGE_EXTRACTOR());
    logger.debug("Pool messages retrieved from db: {}", result);

    return result;
  }

  @Override
  public void setAllCreatedToNow() {

    logger.debug("Setting created for PoolMessages to now in db.");
    this.getJdbcTemplate()
        .update("update pn_pool_message set created = ? where 1=1", new Timestamp(Instant.now().toEpochMilli()));
  }

  @Override
  public void removeAllPoolMessages(Set<Integer> proccessedIds) {

    if (proccessedIds == null || proccessedIds.isEmpty()) {
      return;
    }

    logger.debug("Removing poolMessages from db with ids: {}", proccessedIds);
    this.getJdbcTemplate().update("delete from pn_pool_message where id " + Compatibility.getStructureForInClause(),
        preparedStatement ->
            preparedStatement.setArray(1,
                DatabaseManagerBl.prepareSQLArrayOfNumbersFromIntegers(new ArrayList<>(proccessedIds),
                    preparedStatement)));
    logger.debug("PoolMessages with id: {}, removed.", proccessedIds);
  }

  private void removeOldPoolMessages(long olderThan) {
    Set<Integer> proccessedIds = new HashSet<Integer>();
    long actualTimeInMillis = Instant.now().toEpochMilli();
    SqlRowSet srs = this.getJdbcTemplate().queryForRowSet("SELECT id,created FROM pn_pool_message");
    while (srs.next()) {
      Timestamp timeStamp = srs.getTimestamp("created");
      if (timeStamp.getTime() + olderThan < actualTimeInMillis) {
        proccessedIds.add(srs.getInt("id"));
      }
    }

    removeAllPoolMessages(proccessedIds);
  }
}
