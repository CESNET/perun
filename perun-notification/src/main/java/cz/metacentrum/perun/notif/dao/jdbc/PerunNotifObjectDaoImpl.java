package cz.metacentrum.perun.notif.dao.jdbc;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.notif.dao.PerunNotifObjectDao;
import cz.metacentrum.perun.notif.entities.PerunNotifObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

/**
 * Jdbc implementation of PerunNotifObjectDao
 *
 * User: tomastunkl
 * Date: 01.11.12
 * Time: 22:57
 */
@Repository("perunNotifObjectDao")
public class PerunNotifObjectDaoImpl extends JdbcDaoSupport implements PerunNotifObjectDao {

    private static final Logger logger = LoggerFactory.getLogger(PerunNotifObjectDao.class);

    @Override
    public PerunNotifObject savePerunNotifObject(PerunNotifObject object) throws InternalErrorException {

        logger.debug("Saving PerunNotifObject: {} to db.", object);
        int newPerunNotifObjectId = Utils.getNewId(this.getJdbcTemplate(), "pn_object_id_seq");
        this.getJdbcTemplate().update("INSERT INTO pn_object(id, name, properties, class_name) values(?,?,?,?)", newPerunNotifObjectId, object.getName(), object.getSerializedProperties(), object.getObjectClass().getName());

        object.setId(newPerunNotifObjectId);
        logger.debug("PerunNotifObject saved to db. Object: {}", object);
        return object;
    }

    @Override
    public PerunNotifObject updatePerunNotifObject(PerunNotifObject object) throws InternalErrorException {

        logger.debug("Updating object in db: {}", object);
        this.getJdbcTemplate().update("update pn_object set properties = ?, name = ?, class_name = ? where id = ?", object.getSerializedProperties(), object.getName(), object.getObjectClass().toString(), object.getId());

        PerunNotifObject result = getPerunNotifObjectById(object.getId());
        logger.debug("PerunNotifObject updated in db, returning object: {}", result);

        return result;
    }

    @Override
    public void removePerunNotifObjectById(int id) throws InternalErrorException {

        logger.debug("Removing relations of object and regex from db, object id: {}", id);
        this.getJdbcTemplate().update("delete from pn_regex_object where object_id = ?", id);

        logger.debug("Removing object with id: {} from db.", id);
        this.getJdbcTemplate().update("delete from pn_object where id = ?", id);
    }

    @Override
    public PerunNotifObject getPerunNotifObjectById(int id) throws InternalErrorException {

        logger.debug("Getting PerunNotifObject from db by id: {}", id);
        try {
            PerunNotifObject object = this.getJdbcTemplate().queryForObject("SELECT * FROM pn_object object where object.id = ?", new Object[]{id}, PerunNotifObject.PERUN_NOTIF_OBJECT);
            logger.debug("PerunNotifObject retrieved from db: {}", object);
            return object;
        } catch (EmptyResultDataAccessException ex) {
            logger.debug("PerunNotifObject with id: {} not found.", id);
            return null;
        }
    }

    @Override
    public boolean isObjectRelation(int templateId, Integer objectId) {

        logger.debug("IsObjectRelation for templateId: {}, objectId: {}", Arrays.asList(templateId, objectId));
        try {
            SqlRowSet rowSet = this.getJdbcTemplate().queryForRowSet("select * from pn_regex_object where regex_id = ? AND object_id = ?", templateId, objectId);
            logger.debug("Relation between templateId: {} and objectId: {}, found.", Arrays.asList(templateId, objectId));
            return rowSet.next();
        } catch (EmptyResultDataAccessException ex) {
            //This exception signals empty row
            logger.debug("Relation between templateId: {}, and objectId: {}, not found", Arrays.asList(templateId, objectId));
            return false;
        }
    }

    @Override
    public void saveObjectRelation(int templateId, Integer objectId) throws InternalErrorException {

        logger.debug("Saving relation bewteen templateId: {}, and objectId: {} to db.", Arrays.asList(templateId, objectId));
        int newId = Utils.getNewId(this.getJdbcTemplate(), "pn_regex_object_seq");

        this.getJdbcTemplate().update("insert into pn_regex_object(id, regex_id, object_id) values(?,?,?)", newId, templateId, objectId);
        logger.debug("Relation between templateId: {} and objectId: {} saved to db with id: {}", Arrays.asList(templateId, objectId, newId));
    }

    @Override
    public void removePerunNotifObjectRegexRelation(int regexId, int objectId) {

        logger.debug("Removing relation between object: {} and regex: {} from db.", Arrays.asList(objectId, regexId));
        this.getJdbcTemplate().update("delete from pn_regex_object where regex_id = ? and object_id = ?", regexId, objectId);
    }

    @Override
    public List<PerunNotifObject> getAll() {

        logger.debug("Getting all object from db.");
        return this.getJdbcTemplate().query("select * from pn_object", PerunNotifObject.PERUN_NOTIF_OBJECT);
    }
}
