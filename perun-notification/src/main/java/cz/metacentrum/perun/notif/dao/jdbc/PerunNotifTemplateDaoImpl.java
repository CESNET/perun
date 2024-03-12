package cz.metacentrum.perun.notif.dao.jdbc;

import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.notif.dao.PerunNotifRegexDao;
import cz.metacentrum.perun.notif.dao.PerunNotifTemplateDao;
import cz.metacentrum.perun.notif.entities.PerunNotifReceiver;
import cz.metacentrum.perun.notif.entities.PerunNotifRegex;
import cz.metacentrum.perun.notif.entities.PerunNotifTemplate;
import cz.metacentrum.perun.notif.entities.PerunNotifTemplateMessage;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;

@Repository("perunNotifTemplateDao")
public class PerunNotifTemplateDaoImpl extends JdbcDaoSupport implements PerunNotifTemplateDao {

  public static final String DELIMITER = ";";
  @Autowired
  private PerunNotifRegexDao perunNotifRegexDao;

  public List<PerunNotifTemplate> getAllPerunNotifTemplates() {

    List<PerunNotifTemplate> result =
        this.getJdbcTemplate().query("SELECT * from pn_template", PerunNotifTemplate.PERUN_NOTIF_TEMPLATE);
    for (PerunNotifTemplate template : result) {

      // Gets all template ids which are connected to given regexIds
      Set<PerunNotifRegex> perunNotifRegexs = perunNotifRegexDao.getPerunNotifRegexForTemplateId(template.getId());
      template.setMatchingRegexs(perunNotifRegexs);

      List<PerunNotifReceiver> perunNotifReceiver = this.getJdbcTemplate()
          .query("SELECT * from pn_receiver where template_id = ?", PerunNotifReceiver.PERUN_NOTIF_RECEIVER,
              template.getId());
      template.setReceivers(perunNotifReceiver);

      List<PerunNotifTemplateMessage> perunNotifTemplateMessages = this.getJdbcTemplate()
          .query("SELECT * from pn_template_message where template_id = ?",
              PerunNotifTemplateMessage.PERUN_NOTIF_TEMPLATE_MESSAGE_ROW_MAPPER, template.getId());
      template.setPerunNotifTemplateMessages(perunNotifTemplateMessages);
    }

    return result;
  }

  @Override
  public PerunNotifTemplateMessage createPerunNotifTemplateMessage(PerunNotifTemplateMessage templateMessages) {

    int newPerunNotifTemplateMessageId = Utils.getNewId(this.getJdbcTemplate(), "pn_template_message_id_seq");
    this.getJdbcTemplate()
        .update("INSERT INTO pn_template_message(id, template_id, message, locale, subject) values(?,?,?,?,?)",
            newPerunNotifTemplateMessageId, templateMessages.getTemplateId(), templateMessages.getMessage(),
            templateMessages.getLocale().getLanguage(), templateMessages.getSubject());
    templateMessages.setId(newPerunNotifTemplateMessageId);

    return templateMessages;
  }

  @Override
  public PerunNotifTemplateMessage updatePerunNotifTemplateMessage(PerunNotifTemplateMessage templateMessage) {

    this.getJdbcTemplate()
        .update("update pn_template_message set template_id = ?, message = ?, locale = ?, subject = ? where id = ?",
            templateMessage.getTemplateId(), templateMessage.getMessage(), templateMessage.getLocale().getLanguage(),
            templateMessage.getSubject(), templateMessage.getId());

    return getPerunNotifTemplateMessageById(templateMessage.getId());
  }

  @Override
  public PerunNotifTemplate updatePerunNotifTemplateData(PerunNotifTemplate template) {

    this.getJdbcTemplate().update(
        "update pn_template set name = ?, notify_trigger = ?, oldest_message_time = ?, youngest_message_time=?, primary_properties=?, sender = ? where id = ?",
        template.getName(), template.getNotifyTrigger().getKey(), template.getOldestMessageTime(),
        template.getYoungestMessageTime(), template.getSerializedPrimaryProperties(), template.getSender(),
        template.getId());

    return getPerunNotifTemplateById(template.getId());
  }

  @Override
  public PerunNotifReceiver getPerunNotifReceiverById(int id) {

    try {
      PerunNotifReceiver object = this.getJdbcTemplate()
          .queryForObject("select * from pn_receiver where id = ?", PerunNotifReceiver.PERUN_NOTIF_RECEIVER, id);
      return object;
    } catch (EmptyResultDataAccessException ex) {
      return null;
    }
  }

  @Override
  public List<PerunNotifReceiver> getAllPerunNotifReceivers() {

    List<PerunNotifReceiver> list =
        this.getJdbcTemplate().query("select * from pn_receiver", PerunNotifReceiver.PERUN_NOTIF_RECEIVER);
    return list;
  }

  @Override
  public PerunNotifReceiver createPerunNotifReceiver(PerunNotifReceiver receiver) {

    int newPerunNotifReceiverId = Utils.getNewId(this.getJdbcTemplate(), "pn_receiver_id_seq");

    this.getJdbcTemplate()
        .update("insert into pn_receiver (id, target, type_of_receiver, template_id, locale) values (?, ?, ?, ?, ?)",
            newPerunNotifReceiverId, receiver.getTarget(), receiver.getTypeOfReceiver().getKey(),
            receiver.getTemplateId(), receiver.getLocale());
    receiver.setId(newPerunNotifReceiverId);

    return receiver;
  }

  @Override
  public PerunNotifReceiver updatePerunNotifReceiver(PerunNotifReceiver receiver) {

    this.getJdbcTemplate()
        .update("update pn_receiver set target = ?, type_of_receiver = ?, template_id = ?, locale = ? where id = ?",
            receiver.getTarget(), receiver.getTypeOfReceiver().getKey(), receiver.getTemplateId(), receiver.getLocale(),
            receiver.getId());

    return getPerunNotifReceiverById(receiver.getId());
  }

  @Override
  public PerunNotifTemplate getPerunNotifTemplateById(int id) {

    PerunNotifTemplate template = null;
    try {
      template = this.getJdbcTemplate()
          .queryForObject("SELECT * from pn_template where id = ?", PerunNotifTemplate.PERUN_NOTIF_TEMPLATE, id);
    } catch (EmptyResultDataAccessException ex) {
      //This exception is thrown when object is not found
      return null;
    }

    Set<PerunNotifRegex> regexes = perunNotifRegexDao.getPerunNotifRegexForTemplateId(template.getId());
    template.setMatchingRegexs(regexes);

    List<PerunNotifReceiver> perunNotifReceiver = this.getJdbcTemplate()
        .query("SELECT * from pn_receiver where template_id = ?", PerunNotifReceiver.PERUN_NOTIF_RECEIVER,
            template.getId());
    template.setReceivers(perunNotifReceiver);

    List<PerunNotifTemplateMessage> perunNotifTemplateMessages = this.getJdbcTemplate()
        .query("SELECT * from pn_template_message where template_id = ?",
            PerunNotifTemplateMessage.PERUN_NOTIF_TEMPLATE_MESSAGE_ROW_MAPPER, template.getId());
    template.setPerunNotifTemplateMessages(perunNotifTemplateMessages);

    return template;
  }

  @Override
  public PerunNotifTemplate savePerunNotifTemplateInternals(PerunNotifTemplate template) {

    int newPerunNotifTemplateId = Utils.getNewId(this.getJdbcTemplate(), "pn_template_id_seq");

    this.getJdbcTemplate().update(
        "insert into pn_template(id, name, primary_properties, notify_trigger, youngest_message_time, oldest_message_time, sender) values (?, ?, ?, ?, ?, ?, ?)",
        newPerunNotifTemplateId, template.getName(), template.getSerializedPrimaryProperties(),
        template.getNotifyTrigger().getKey(), template.getYoungestMessageTime(), template.getOldestMessageTime(),
        template.getSender());
    template.setId(newPerunNotifTemplateId);

    return template;
  }

  @Override
  public void removePerunNotifReceiverById(int id) {

    this.getJdbcTemplate().update("delete from pn_receiver where id = ?", id);
  }

  @Override
  public PerunNotifTemplateMessage getPerunNotifTemplateMessageById(int id) {

    try {
      return this.getJdbcTemplate().queryForObject("select * from pn_template_message where id = ?",
          PerunNotifTemplateMessage.PERUN_NOTIF_TEMPLATE_MESSAGE_ROW_MAPPER, id);
    } catch (EmptyResultDataAccessException ex) {
      return null;
    }
  }

  @Override
  public List<PerunNotifTemplateMessage> getAllPerunNotifTemplateMessages() {
    return this.getJdbcTemplate()
        .query("select * from pn_template_message", PerunNotifTemplateMessage.PERUN_NOTIF_TEMPLATE_MESSAGE_ROW_MAPPER);
  }

  @Override
  public void removePerunNotifTemplateMessage(int id) {

    this.getJdbcTemplate().update("delete from pn_template_message where id = ?", id);
  }

  @Override
  public void removePerunNotifTemplateById(int id) {

    this.getJdbcTemplate().update("delete from pn_template_message where template_id = ?", id);

    this.getJdbcTemplate().update("delete from pn_template_regex where template_id = ?", id);

    this.getJdbcTemplate().update("delete from pn_pool_message where template_id = ?", id);

    this.getJdbcTemplate().update("delete from pn_template where id = ?", id);
  }

  @Override
  public void saveTemplateRegexRelation(int templateId, Integer regexId) {
    if (perunNotifRegexDao.isRegexRelation(templateId, regexId)) {
      //Relation exists
      return;
    } else {
      perunNotifRegexDao.saveTemplateRegexRelation(templateId, regexId);
      PerunNotifTemplate template = getPerunNotifTemplateById(templateId);
      template.addPerunNotifRegex(perunNotifRegexDao.getPerunNotifRegexById(regexId));
    }
  }
}
