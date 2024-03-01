package cz.metacentrum.perun.notif.senders;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.notif.dto.PerunNotifEmailMessageToSendDto;
import cz.metacentrum.perun.notif.dto.PerunNotifMessageDto;
import cz.metacentrum.perun.notif.dto.PoolMessage;
import cz.metacentrum.perun.notif.entities.PerunNotifReceiver;
import cz.metacentrum.perun.notif.enums.PerunNotifTypeOfReceiver;
import cz.metacentrum.perun.notif.managers.PerunNotifEmailManager;
import cz.metacentrum.perun.notif.utils.NotifUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

/**
 * Sender handles sending message using email to user
 * <p>
 * User: tomastunkl Date: 23.11.12 Time: 23:10
 */
public class PerunNotifEmailUserSender implements PerunNotifSender {

  private static final Logger LOGGER = LoggerFactory.getLogger(PerunNotifEmailUserSender.class);

  @Autowired
  private PerunBl perun;

  @Autowired
  private PerunNotifEmailManager perunNotifEmailManager;

  private PerunSession session;

  @Override
  public boolean canHandle(PerunNotifTypeOfReceiver typeOfReceiver) {

    return typeOfReceiver != null && typeOfReceiver.equals(PerunNotifTypeOfReceiver.EMAIL_USER);
  }

  @PostConstruct
  public void init() throws Exception {
    session = NotifUtils.getPerunSession(perun);
  }

  @Override
  public Set<Integer> send(List<PerunNotifMessageDto> dtosToSend) {

    Set<Integer> usedPools = new HashSet<Integer>();
    List<PerunNotifEmailMessageToSendDto> messagesToSend = new ArrayList<PerunNotifEmailMessageToSendDto>();
    for (PerunNotifMessageDto messageDto : dtosToSend) {

      PerunNotifReceiver receiver = messageDto.getReceiver();
      PoolMessage dto = messageDto.getPoolMessage();

      LOGGER.debug("Creating email for user, receiver: {}", receiver.getId());
      PerunNotifEmailMessageToSendDto emailDto = new PerunNotifEmailMessageToSendDto();
      emailDto.setMessage(messageDto.getMessageToSend());
      emailDto.setSubject(messageDto.getSubject());
      usedPools.addAll(messageDto.getUsedPoolIds());

      String sender = messageDto.getSender();
      emailDto.setSender(sender);
      LOGGER.debug("Calculated sender for receiver: {}, sender: {}", receiver.getId(), sender);

      String myReceiverId = dto.getKeyAttributes().get(receiver.getTarget());
      if (myReceiverId == null || myReceiverId.isEmpty()) {
        //Can be set one static account
        emailDto.setReceiver(receiver.getTarget());
      } else {
        //We try to resolve id
        Integer id = null;
        try {
          id = Integer.valueOf(myReceiverId);
        } catch (NumberFormatException ex) {
          LOGGER.error("Cannot resolve id: {}, error: {}", id, ex.getMessage());
          LOGGER.debug("ST:", ex);
        }
        if (id != null) {
          try {
            User user = perun.getUsersManagerBl().getUserById(session, id);
            Attribute emailAttribute = perun.getAttributesManagerBl()
                .getAttribute(session, user, "urn:perun:user:attribute-def:def:preferredMail");
            if (emailAttribute != null && StringUtils.hasText(emailAttribute.toString())) {
              emailDto.setReceiver((String) emailAttribute.getValue());
            }
          } catch (UserNotExistsException ex) {
            LOGGER.error("Cannot found user with id: {}, ex: {}", id, ex.getMessage());
            LOGGER.debug("ST:", ex);
          } catch (AttributeNotExistsException ex) {
            LOGGER.warn("Cannot found email for user with id: {}, ex: {}", id, ex.getMessage());
            LOGGER.debug("ST:", ex);
          } catch (Exception ex) {
            LOGGER.error("Error during user email recognition, ex: {}", ex.getMessage());
            LOGGER.debug("ST:", ex);
          }
        }
      }

      messagesToSend.add(emailDto);
    }

    perunNotifEmailManager.sendMessages(messagesToSend);

    return usedPools;
  }
}
