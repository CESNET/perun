package cz.metacentrum.perun.notif.dao.jdbc;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.notif.dao.PerunNotifRegexDao;
import cz.metacentrum.perun.notif.dao.PerunNotifTemplateDao;
import cz.metacentrum.perun.notif.entities.PerunNotifReceiver;
import cz.metacentrum.perun.notif.entities.PerunNotifRegex;
import cz.metacentrum.perun.notif.entities.PerunNotifTemplate;
import cz.metacentrum.perun.notif.entities.PerunNotifTemplateMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Set;

@Repository("perunNotifTemplateDao")
public class PerunNotifTemplateDaoImpl extends JdbcDaoSupport implements PerunNotifTemplateDao {

    @Autowired
    private PerunNotifRegexDao perunNotifRegexDao;

    public static final String DELIMITER = ";";

    public List<PerunNotifTemplate> getAllPerunNotifTemplates() throws InternalErrorException {

        List<PerunNotifTemplate> result = this.getJdbcTemplate().query("SELECT * from pn_template", PerunNotifTemplate.PERUN_NOTIF_TEMPLATE);
        for (PerunNotifTemplate template : result) {

            // Gets all template ids which are connected to given regexIds
            Set<PerunNotifRegex> perunNotifRegexs = perunNotifRegexDao.getPerunNotifRegexForTemplateId(template.getId());
            template.setMatchingRegexs(perunNotifRegexs);

            List<PerunNotifReceiver> perunNotifReceiver = this.getJdbcTemplate().query("SELECT * from pn_receiver where template_id = ?", new Object[]{template.getId()}, PerunNotifReceiver.PERUN_NOTIF_RECEIVER);
            template.setReceivers(perunNotifReceiver);

            List<PerunNotifTemplateMessage> perunNotifTemplateMessages = this.getJdbcTemplate().query("SELECT * from pn_template_message where template_id = ?", new Object[]{template.getId()}, PerunNotifTemplateMessage.PERUN_NOTIF_TEMPLATE_MESSAGE_ROW_MAPPER);
            template.setPerunNotifTemplateMessages(perunNotifTemplateMessages);
        }

        return result;
    }

    @Override
    public PerunNotifTemplateMessage savePerunNotifTemplateMessage(PerunNotifTemplateMessage templateMessages) throws InternalErrorException {

        int newPerunNotifTemplateMessageId = Utils.getNewId(this.getJdbcTemplate(), "pn_template_message_id_seq");
        this.getJdbcTemplate().update("INSERT INTO pn_template_message(id, template_id, message, locale, subject) values(?,?,?,?,?)", newPerunNotifTemplateMessageId, templateMessages.getTemplateId(), templateMessages.getMessage(), templateMessages.getLocale().toString(), templateMessages.getSubject());
        templateMessages.setId(newPerunNotifTemplateMessageId);
        
        return templateMessages;
    }

    @Override
    public PerunNotifTemplateMessage updatePerunNotifTemplateMessage(PerunNotifTemplateMessage templateMessage) throws InternalErrorException {

        this.getJdbcTemplate().update("update pn_template_message set template_id = ?, message = ?, locale = ? where id = ?", templateMessage.getTemplateId(), templateMessage.getMessage(), templateMessage.getLocale().toString(), templateMessage.getId());

        return getPerunNotifTemplateMessageById(templateMessage.getId());
    }

    @Override
    public PerunNotifTemplate updatePerunNotifTemplateData(PerunNotifTemplate template) throws InternalErrorException {

        this.getJdbcTemplate().update("update pn_template set oldest_message_time = ?, youngest_message_time=?, primary_properties=?, sender = ? where id = ?", template.getOldestMessageTime(), template.getYoungestMessageTime(), template.getSerializedPrimaryProperties(), template.getSender(), template.getId());

        return getPerunNotifTemplateById(template.getId());
    }

    @Override
    public PerunNotifReceiver getPerunNotifReceiverById(int id) throws InternalErrorException {

        try {
            PerunNotifReceiver object = this.getJdbcTemplate().queryForObject("select * from pn_receiver where id = ?", new Object[]{id}, PerunNotifReceiver.PERUN_NOTIF_RECEIVER);
            return object;
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    @Override
    public PerunNotifReceiver savePerunNotifReceiver(PerunNotifReceiver receiver) throws InternalErrorException {

        int newPerunNotifReceiverId = Utils.getNewId(this.getJdbcTemplate(), "pn_receiver_id_seq");

        this.getJdbcTemplate().update("insert into pn_receiver (id, target, type_of_receiver, template_id) values (?, ?, ?, ?)", newPerunNotifReceiverId, receiver.getTarget(), receiver.getTypeOfReceiver().getKey(), receiver.getTemplateId());
        receiver.setId(newPerunNotifReceiverId);
        
        return receiver;
    }

    public PerunNotifReceiver updatePerunNotifReceiver(PerunNotifReceiver receiver) throws InternalErrorException {

        this.getJdbcTemplate().update("update pn_receiver set target = ?, type_of_receiver = ?, template_id = ? where id = ?", receiver.getTarget(), receiver.getTypeOfReceiver().getKey(), receiver.getTemplateId(), receiver.getId());

        return getPerunNotifReceiverById(receiver.getId());
    }

    @Override
    public PerunNotifTemplate getPerunNotifTemplateById(int id) throws InternalErrorException {

        PerunNotifTemplate template = null;
        try {
            template = this.getJdbcTemplate().queryForObject("SELECT * from pn_template where id = ?", new Object[]{id}, PerunNotifTemplate.PERUN_NOTIF_TEMPLATE);
        } catch (EmptyResultDataAccessException ex) {
            //This exception is thrown when object is not found
            return null;
        }

        Set<PerunNotifRegex> regexes = perunNotifRegexDao.getPerunNotifRegexForTemplateId(template.getId());
        template.setMatchingRegexs(regexes);

        List<PerunNotifReceiver> perunNotifReceiver = this.getJdbcTemplate().query("SELECT * from pn_receiver where template_id = ?", new Object[]{template.getId()}, PerunNotifReceiver.PERUN_NOTIF_RECEIVER);
        template.setReceivers(perunNotifReceiver);

        List<PerunNotifTemplateMessage> perunNotifTemplateMessages = this.getJdbcTemplate().query("SELECT * from pn_template_message where template_id = ?", new Object[]{template.getId()}, PerunNotifTemplateMessage.PERUN_NOTIF_TEMPLATE_MESSAGE_ROW_MAPPER);
        template.setPerunNotifTemplateMessages(perunNotifTemplateMessages);

        return template;
    }

    @Override
    public PerunNotifTemplate savePerunNotifTemplateInternals(PerunNotifTemplate template) throws InternalErrorException {

        int newPerunNotifTemplateId = Utils.getNewId(this.getJdbcTemplate(), "pn_template_id_seq");

        this.getJdbcTemplate().update("insert into pn_template(id, primary_properties, notify_trigger, youngest_message_time, oldest_message_time, locale, sender) values (?, ?, ?, ?, ?, ?, ?)", newPerunNotifTemplateId, template.getSerializedPrimaryProperties(), template.getNotifyTrigger().getKey(), template.getYoungestMessageTime(), template.getOldestMessageTime(), template.getLocale(), template.getSender());
        template.setId(newPerunNotifTemplateId);
        
        return template;
    }

    @Override
    public void removePerunNotifReceiverById(int id) throws InternalErrorException {

        this.getJdbcTemplate().update("delete from pn_receiver where id = ?", id);
    }

    @Override
    public PerunNotifTemplateMessage getPerunNotifTemplateMessageById(int id) throws InternalErrorException {

        try {
            return this.getJdbcTemplate().queryForObject("select * from pn_template_message where id = ?", new Object[]{id}, PerunNotifTemplateMessage.PERUN_NOTIF_TEMPLATE_MESSAGE_ROW_MAPPER);
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    @Override
    public void removePerunNotifTemplateMessage(int id) throws InternalErrorException {

        this.getJdbcTemplate().update("delete from pn_template_message where id = ?", id);
    }

    @Override
    public void removePerunNotifTemplateById(int id) {

        this.getJdbcTemplate().update("delete from pn_template_message where template_id = ?", id);

        this.getJdbcTemplate().update("delete from pn_template_regex where template_id = ?", id);

        this.getJdbcTemplate().update("delete from pn_pool_message where template_id = ?", id);

        this.getJdbcTemplate().update("delete from pn_template where id = ?", id);
    }
}
