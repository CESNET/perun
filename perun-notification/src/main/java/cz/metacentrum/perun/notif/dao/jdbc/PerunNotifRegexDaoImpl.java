package cz.metacentrum.perun.notif.dao.jdbc;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.notif.dao.PerunNotifRegexDao;
import cz.metacentrum.perun.notif.entities.PerunNotifObject;
import cz.metacentrum.perun.notif.entities.PerunNotifRegex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Jdbc implementation of PerunNotifRegexDao
 */
@Repository("perunNotifRegexDao")
public class PerunNotifRegexDaoImpl extends JdbcDaoSupport implements
        PerunNotifRegexDao {

    private static final Logger logger = LoggerFactory.getLogger(PerunNotifRegexDao.class);

    @Override
    public List<PerunNotifRegex> getAll() {

        logger.debug("Getting all PerunNotifRegexes from db.");
        List<PerunNotifRegex> result = this.getJdbcTemplate().query("SELECT * from pn_regex ",
                PerunNotifRegex.PERUN_NOTIF_REGEX);

        logger.debug("Regexes loaded from db: {}, loading objects.", result);
        for (PerunNotifRegex regex : result) {
            List<PerunNotifObject> objects = this.getJdbcTemplate().query("SELECT * from pn_object object JOIN pn_regex_object bind ON object.id = bind.object_id WHERE bind.regex_id = ?", new Object[]{regex.getId()}, PerunNotifObject.PERUN_NOTIF_OBJECT);
            regex.addObjects(objects);
        }
        logger.debug("Objects loaded, resulting regexes: {}", result);

        return result;
    }

    @Override
    public PerunNotifRegex saveInternals(PerunNotifRegex regex) throws InternalErrorException {

        logger.debug("Saving regex internals to db: {}", regex);
        int newPerunNotifRegexId = Utils.getNewId(this.getJdbcTemplate(), "pn_regex_id_seq");
        this.getJdbcTemplate().update("INSERT INTO pn_regex(id, note, regex) values (?,?,?)", newPerunNotifRegexId, regex.getNote(), regex.getRegex());

        regex.setId(newPerunNotifRegexId);
        logger.debug("Regex saved to db, id created: {}", regex);
        
        return regex;
    }

    @Override
    public PerunNotifRegex getPerunNotifRegexById(int id) throws InternalErrorException {

        logger.debug("Loading regex from db by id: {}", id);
        PerunNotifRegex regex = null;
        try {
            regex = this.getJdbcTemplate().queryForObject("SELECT * from pn_regex where id = ?", new Object[]{id}, PerunNotifRegex.PERUN_NOTIF_REGEX);
        } catch (EmptyResultDataAccessException ex) {
            logger.debug("Regex with id: {}, not found.", id);
            return null;
        }
        logger.debug("Regex with id: {}, loaded from db. Loading objects.", id);

        List<PerunNotifObject> objects = this.getJdbcTemplate().query("SELECT * from pn_object object JOIN pn_regex_object bind ON object.id = bind.object_id WHERE bind.regex_id = ?", new Object[]{regex.getId()}, PerunNotifObject.PERUN_NOTIF_OBJECT);
        regex.addObjects(objects);

        logger.debug("Objects loaded result: {}", regex);
        return regex;
    }

    @Override
    public Set<PerunNotifRegex> getPerunNotifRegexForTemplateId(int id) throws InternalErrorException {

        logger.debug("Loading regexes for template with id: {}", id);
        List<PerunNotifRegex> regexes = this.getJdbcTemplate().query("SELECT * from pn_regex regex JOIN pn_template_regex bind ON regex.id=bind.regex_id WHERE bind.template_id = ?", new Object[]{id}, PerunNotifRegex.PERUN_NOTIF_REGEX);
        if (regexes != null) {
            for (PerunNotifRegex regex : regexes) {

                List<PerunNotifObject> objects = this.getJdbcTemplate().query("SELECT * from pn_object object JOIN pn_regex_object bind ON object.id = bind.object_id WHERE bind.regex_id = ?", new Object[]{regex.getId()}, PerunNotifObject.PERUN_NOTIF_OBJECT);
                regex.addObjects(objects);
            }
        }
        logger.debug("Regexes loaded with object for template id: {}, result: {}", Arrays.asList(id, regexes));

        return new HashSet<PerunNotifRegex>(regexes);
    }

    @Override
    public PerunNotifRegex updatePerunNotifRegexInternals(PerunNotifRegex regex) throws InternalErrorException {

        logger.debug("Updating regex internals in db: {}", regex);
        this.getJdbcTemplate().update("update pn_regex set note = ?, regex = ? where id = ?", regex.getNote(), regex.getRegex(), regex.getId());

        return getPerunNotifRegexById(regex.getId());
    }

    @Override
    public void removePerunNotifRegexById(int id) throws InternalErrorException {

        logger.debug("Removing relation between object and regex for regexId: {}", id);
        this.getJdbcTemplate().update("delete from pn_regex_object where regex_id = ?", id);

        logger.debug("Removing regex entity for id: {}", id);
        this.getJdbcTemplate().update("delete from pn_regex where id = ?", id);
    }

    @Override
    public List<Integer> getTemplateIdsForRegexId(int id) throws InternalErrorException {

        logger.debug("Loading templateIds for regexId: {}", id);
        return this.getJdbcTemplate().query("select template_id from pn_template_regex where regex_id = ?", new Object[]{id}, new RowMapper<Integer>() {
            @Override
            public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {

                return rs.getInt("template_id");
            }
        });
    }

    @Override
    public boolean isRegexRelation(int templateId, Integer regexId) {

        logger.debug("Trying to load relation between template: {}, and regex: {}", Arrays.asList(templateId, regexId));
        SqlRowSet rowSet = this.getJdbcTemplate().queryForRowSet("select * from pn_template_regex where template_id = ? AND regex_id = ?", templateId, regexId);
        return rowSet.next();
    }

    @Override
    public void saveRegexRelation(int templateId, Integer regexId) throws InternalErrorException {

        logger.debug("Saving relation between template: {} and regex: {}", Arrays.asList(templateId, regexId));
        int newId = Utils.getNewId(this.getJdbcTemplate(), "pn_template_regex_seq");

        this.getJdbcTemplate().update("insert into pn_template_regex(id, template_id, regex_id) values(?,?,?)", newId, templateId, regexId);
        logger.debug("Relation between template: {} and regex: {} saved with id: {}", Arrays.asList(templateId, regexId, newId));
    }

    @Override
    public void removePerunNotifTemplateRegexRelation(int templateId, int regexId) throws InternalErrorException {

        logger.debug("Removing relation between template: {} and regex: {}", Arrays.asList(templateId, regexId));
        this.getJdbcTemplate().update("delete from pn_template_regex where template_id = ? and regex_id = ? ", templateId, regexId);
    }
}
