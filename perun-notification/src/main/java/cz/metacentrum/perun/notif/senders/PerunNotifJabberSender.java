package cz.metacentrum.perun.notif.senders;

import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.notif.dto.PerunNotifMessageDto;
import cz.metacentrum.perun.notif.dto.PoolMessage;
import cz.metacentrum.perun.notif.entities.PerunNotifReceiver;
import cz.metacentrum.perun.notif.enums.PerunNotifTypeOfReceiver;
import cz.metacentrum.perun.notif.utils.NotifUtils;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * Sender handles sending message to jabber of user User: tomastunkl Date:
 * 24.11.12 Time: 0:13
 */
public class PerunNotifJabberSender implements PerunNotifSender {

	private static final Logger logger = LoggerFactory.getLogger(PerunNotifJabberSender.class);

	@Autowired
	private PerunBl perun;

	@Autowired
	private Properties propertiesBean;

	private PerunSession session;

	private String jabberServer;
	private int port;
	private String serviceName;
	private String username;
	private String password;

	@PostConstruct
	public void init() throws Exception {
		session = NotifUtils.getPerunSession(perun);
		this.jabberServer = (String) propertiesBean.get("notif.jabber.jabberServer");
		this.port = Integer.valueOf((String) propertiesBean.get("notif.jabber.port"));
		this.serviceName = (String) propertiesBean.get("notif.jabber.serviceName");
		this.username = (String) propertiesBean.get("notif.jabber.username");
		this.password = (String) propertiesBean.get("notif.jabber.password");
	}

	@Override
	public boolean canHandle(PerunNotifTypeOfReceiver typeOfReceiver) {

		if (typeOfReceiver != null && typeOfReceiver.equals(PerunNotifTypeOfReceiver.JABBER)) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public Set<Integer> send(List<PerunNotifMessageDto> dtosToSend) {

		Set<Integer> usedPools = new HashSet<Integer>();
		try {
			ConnectionConfiguration config = new ConnectionConfiguration(jabberServer, port, serviceName);
			XMPPConnection connection = new XMPPConnection(config);

			connection.connect();
			SASLAuthentication.supportSASLMechanism("PLAIN", 0);
			connection.login(username, password);

			for (PerunNotifMessageDto messageDto : dtosToSend) {

				PerunNotifReceiver receiver = messageDto.getReceiver();
				PoolMessage dto = messageDto.getPoolMessage();

				Message message = new Message();
				message.setSubject(messageDto.getSubject());
				message.setBody(messageDto.getMessageToSend());
				message.setType(Message.Type.headline);

				String myReceiverId = dto.getKeyAttributes().get(receiver.getTarget());
				if (myReceiverId == null || myReceiverId.isEmpty()) {
					//Can be set one static account
					message.setTo(receiver.getTarget());
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
							Attribute emailAttribute = perun.getAttributesManagerBl().getAttribute(session, user, "urn:perun:user:attribute-def:def:jabber");
							if (emailAttribute != null && StringUtils.hasText(emailAttribute.toString())) {
								message.setTo((String) emailAttribute.getValue());
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

				connection.sendPacket(message);

				usedPools.addAll(messageDto.getUsedPoolIds());
			}

			connection.disconnect();
		} catch (XMPPException ex) {
			logger.error("Error during jabber establish connection.", ex);
		}

		return null;
	}
}
