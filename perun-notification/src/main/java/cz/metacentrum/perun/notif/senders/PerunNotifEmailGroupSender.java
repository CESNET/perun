package cz.metacentrum.perun.notif.senders;

import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.notif.dto.PerunNotifEmailMessageToSendDto;
import cz.metacentrum.perun.notif.dto.PerunNotifMessageDto;
import cz.metacentrum.perun.notif.dto.PoolMessage;
import cz.metacentrum.perun.notif.entities.PerunNotifReceiver;
import cz.metacentrum.perun.notif.entities.PerunNotifTemplate;
import cz.metacentrum.perun.notif.enums.PerunNotifTypeOfReceiver;
import cz.metacentrum.perun.notif.managers.PerunNotifEmailManager;
import cz.metacentrum.perun.notif.utils.NotifUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Sender handles sending message to group of users
 *
 * User: tomastunkl Date: 23.11.12 Time: 23:35
 */
public class PerunNotifEmailGroupSender implements PerunNotifSender {

	private static final Logger logger = LoggerFactory.getLogger(PerunNotifEmailGroupSender.class);

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

		if (typeOfReceiver != null && typeOfReceiver.equals(PerunNotifTypeOfReceiver.EMAIL_GROUP)) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public Set<Integer> send(List<PerunNotifMessageDto> dtosToSend) {

		Set<Integer> usedPoolIds = new HashSet<Integer>();
		List<PerunNotifEmailMessageToSendDto> messagesToSend = new ArrayList<PerunNotifEmailMessageToSendDto>();

		for (PerunNotifMessageDto messageDto : dtosToSend) {
			PoolMessage dto = messageDto.getPoolMessage();
			PerunNotifTemplate template = messageDto.getTemplate();
			PerunNotifReceiver receiver = messageDto.getReceiver();

			try {
				String groupSender = dto.getKeyAttributes().get(template.getSender());
				if (groupSender == null || groupSender.isEmpty()) {
					groupSender = template.getSender();
				}
				logger.debug("Calculated sender : {}", groupSender);

				Integer groupId = Integer.valueOf(receiver.getTarget());
				Group group = perun.getGroupsManagerBl().getGroupById(session, groupId);
				List<Member> groupMembers = perun.getGroupsManagerBl().getGroupMembers(session, group);
				if (groupMembers != null) {
					for (Member member : groupMembers) {
						try {
							PerunNotifEmailMessageToSendDto memberEmailDto = new PerunNotifEmailMessageToSendDto();
							memberEmailDto.setMessage(messageDto.getMessageToSend());
							memberEmailDto.setSubject(messageDto.getSubject());
							memberEmailDto.setReceiver((String) perun.getAttributesManagerBl().getAttribute(session, perun.getUsersManager().getUserByMember(session, member), "urn:perun:user:attribute-def:def:preferredMail").getValue());
							memberEmailDto.setSender(groupSender);

							messagesToSend.add(memberEmailDto);
						} catch (Exception ex) {
							logger.error("PreferredEmail cannot be retrieved, userId: {}", member.getUserId(), ex);
						}
					}
				}
				usedPoolIds.addAll(messageDto.getUsedPoolIds());
			} catch (NumberFormatException ex) {
				logger.error("GroupId cannot be parsed: {}", receiver.getTarget());
			} catch (GroupNotExistsException ex) {
				logger.error("Group with id: {} does not exists.", receiver.getTarget());
			} catch (InternalErrorException ex) {
				logger.error("Error during processing messageDto.", ex);
			}
		}

		perunNotifEmailManager.sendMessages(messagesToSend);

		return usedPoolIds;
	}
}
