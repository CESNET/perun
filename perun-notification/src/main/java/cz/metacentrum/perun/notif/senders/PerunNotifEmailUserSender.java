package cz.metacentrum.perun.notif.senders;

import cz.metacentrum.perun.core.api.*;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * Sender handles sending message using email to user
 *
 * User: tomastunkl Date: 23.11.12 Time: 23:10
 */
public class PerunNotifEmailUserSender implements PerunNotifSender {

	private static final Logger logger = LoggerFactory.getLogger(PerunNotifEmailUserSender.class);

	@Autowired
	private PerunBl perun;

	@Autowired
	private PerunNotifEmailManager perunNotifEmailManager;

	private PerunSession session;

	@PostConstruct
	public void init() throws Exception {
		session = NotifUtils.getPerunSession(perun);
	}

	@Override
	public boolean canHandle(PerunNotifTypeOfReceiver typeOfReceiver) {

		if (typeOfReceiver != null && typeOfReceiver.equals(PerunNotifTypeOfReceiver.EMAIL_USER)) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public Set<Integer> send(List<PerunNotifMessageDto> dtosToSend) {

		Set<Integer> usedPools = new HashSet<Integer>();
		List<PerunNotifEmailMessageToSendDto> messagesToSend = new ArrayList<PerunNotifEmailMessageToSendDto>();
		for (PerunNotifMessageDto messageDto : dtosToSend) {

			PerunNotifReceiver receiver = messageDto.getReceiver();
			PoolMessage dto = messageDto.getPoolMessage();

			logger.debug("Creating email for user, receiver: {}", receiver.getId());
			PerunNotifEmailMessageToSendDto emailDto = new PerunNotifEmailMessageToSendDto();
			emailDto.setMessage(messageDto.getMessageToSend());
			emailDto.setSubject(messageDto.getSubject());
			usedPools.addAll(messageDto.getUsedPoolIds());

			String sender = messageDto.getSender();
			emailDto.setSender(sender);
			logger.debug("Calculated sender for receiver: {}, sender: {}", Arrays.asList(receiver.getId(), sender));

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
					logger.error("Cannot resolve id: {}, error: {}", Arrays.asList(id, ex.getMessage()));
					logger.debug("ST:", ex);
				}
				if (id != null) {
					try {
						User user = perun.getUsersManagerBl().getUserById(session, id);
						Attribute emailAttribute = perun.getAttributesManagerBl().getAttribute(session, user, "urn:perun:user:attribute-def:def:preferredMail");
						if (emailAttribute != null && StringUtils.hasText(emailAttribute.toString())) {
							emailDto.setReceiver((String) emailAttribute.getValue());
						}
					} catch (UserNotExistsException ex) {
						logger.error("Cannot found user with id: {}, ex: {}", Arrays.asList(id, ex.getMessage()));
						logger.debug("ST:", ex);
					} catch (AttributeNotExistsException ex) {
						logger.warn("Cannot found email for user with id: {}, ex: {}", Arrays.asList(id, ex.getMessage()));
						logger.debug("ST:", ex);
					} catch (Exception ex) {
						logger.error("Error during user email recognition, ex: {}", ex.getMessage());
						logger.debug("ST:", ex);
					}
				}
			}

			messagesToSend.add(emailDto);
		}

		perunNotifEmailManager.sendMessages(messagesToSend);

		return usedPools;
	}
}
