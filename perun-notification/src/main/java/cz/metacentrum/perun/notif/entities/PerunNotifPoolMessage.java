package cz.metacentrum.perun.notif.entities;

import cz.metacentrum.perun.notif.dto.PoolMessage;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.*;

/**
 * Represents message in pool waiting to be sent. This message can be send based
 * on conditions which has to be met. These messages are aggregated together
 * with messages which has same keyAttributes, templateId.
 *
 * Table pn_pool_message
 *
 * @author tomas.tunkl
 */
public class PerunNotifPoolMessage {

	/**
	 * Delimiter used for map, divides keys+values from other pairs
	 */
	public static final String DELIMITER = ";";

	/**
	 * Unique generated Id attribute of message
	 *
	 * Sequence pn_pool_message_id_seq Column name id
	 */
	private Integer id;

	/**
	 * RegexId which recognized message
	 *
	 * Column name regex_id
	 */
	private Integer regexId;

	/**
	 * TemplateId for message
	 *
	 * Column name template_id
	 */
	private Integer templateId;

	/**
	 * Key attributes which are used to recognize two messages for same
	 * user, templateId must be same, and keyAttributes must be same
	 *
	 * Column key_attributes
	 */
	private Map<String, String> keyAttributes;

	/**
	 * Date of creation of pool message, this attribute can be reset after
	 * application restart
	 *
	 * Column name created
	 */
	private Instant created;

	/**
	 * Holds creating auditer message which was used for creation Will be
	 * used for parsing to retrieve of list of perun beans
	 *
	 * Column name notif_message
	 */
	private String notifMessage;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getRegexId() {
		return regexId;
	}

	public void setRegexId(Integer regexId) {
		this.regexId = regexId;
	}

	public Integer getTemplateId() {
		return templateId;
	}

	public void setTemplateId(Integer templateId) {
		this.templateId = templateId;
	}

	public Map<String, String> getKeyAttributes() {
		return keyAttributes;
	}

	public void setKeyAttributes(Map<String, String> keyAttributes) {
		this.keyAttributes = keyAttributes;
	}

	public String getSerializedKeyAttributes() throws UnsupportedEncodingException {

		return serializeMap(keyAttributes);
	}

	public Instant getCreated() {
		return created;
	}

	public void setCreated(Instant created) {
		this.created = created;
	}

	public String getNotifMessage() {
		return notifMessage;
	}

	public void setNotifMessage(String notifMessage) {
		this.notifMessage = notifMessage;
	}

	/**
	 * extracts from result all perunNotifPoolMessages which are in format:
	 * Integer is templateId, perunNotifPoolMessageProcessDto holds messages
	 * with same templateId and keyAttributes
	 *
	 * @author tomas.tunkl
	 */
	public static final class PERUN_NOTIF_POOL_MESSAGE_EXTRACTOR implements ResultSetExtractor<Map<Integer, List<PoolMessage>>> {

		@Override
		public Map<Integer, List<PoolMessage>> extractData(ResultSet rs) throws SQLException {
			Map<String, List<PerunNotifPoolMessage>> keyMap = new HashMap<String, List<PerunNotifPoolMessage>>();

			//We get all perunNotifPoolMessages and put them in map, where in list are same keyAttributes
			//But different templateIds
			while (rs.next()) {
				PerunNotifPoolMessage poolMessage = new PerunNotifPoolMessage();
				try {
					poolMessage.setKeyAttributes(parseStringToMap(rs.getString("key_attributes")));
				} catch (UnsupportedEncodingException ex) {
					throw new SQLException("Unsupported encoding during decode of map.", ex);
				}
				poolMessage.setId(rs.getInt("id"));
				poolMessage.setCreated(Instant.ofEpochMilli(rs.getTimestamp("created").getTime()));
				poolMessage.setRegexId(rs.getInt("regex_id"));
				poolMessage.setTemplateId(rs.getInt("template_id"));
				poolMessage.setNotifMessage(rs.getString("notif_message"));

				if (keyMap.get(rs.getString("key_attributes")) != null) {
					keyMap.get(rs.getString("key_attributes")).add(poolMessage);
				} else {
					List<PerunNotifPoolMessage> list = new ArrayList<PerunNotifPoolMessage>();
					list.add(poolMessage);
					keyMap.put(rs.getString("key_attributes"), list);
				}
			}

			Map<Integer, List<PoolMessage>> resultMap = new HashMap<Integer, List<PoolMessage>>();
			for (List<PerunNotifPoolMessage> keyList : keyMap.values()) {
				//Holds perunNotifPoolMessageDao with same keyAttributes in values and for different templateIds
				Map<Integer, PoolMessage> subMap = new HashMap<Integer, PoolMessage>();
				//Holds information about messages with same keyAttributes and templateId
				PoolMessage messageDto = null;
				for (PerunNotifPoolMessage message : keyList) {
					//We list throw all messages, we know that every message has same keyAttributes and locale
					if (messageDto == null) {
						messageDto = new PoolMessage();
						messageDto.setKeyAttributes(message.getKeyAttributes());
						messageDto.setTemplateId(message.getTemplateId());
						messageDto.addToList(message);
					} else {
						if (messageDto.getTemplateId().equals(message.getTemplateId())) {
							//If we have same templateId we add to dto
							messageDto.addToList(message);
						} else {
							//We found new templateId, we dont have to check existing of same templateId in Map
							//It is based on ordering from db
							subMap.put(messageDto.getTemplateId(), messageDto);
							messageDto = new PoolMessage();
							messageDto.setKeyAttributes(message.getKeyAttributes());
							messageDto.setTemplateId(message.getTemplateId());
							messageDto.addToList(message);
						}
					}
				}

				if (messageDto != null) {
					subMap.put(messageDto.getTemplateId(), messageDto);
				}

				for (Integer templateId : subMap.keySet()) {

					PoolMessage dto = subMap.get(templateId);
					List<PoolMessage> list = resultMap.get(templateId);
					if (list == null) {
						list = new ArrayList<PoolMessage>();
						resultMap.put(templateId, list);
					}

					list.add(dto);
				}
			}

			return resultMap;
		}
	}

	private static Map<String, String> parseStringToMap(String string) throws UnsupportedEncodingException {

		if (string == null || string.isEmpty()) {
			return null;
		}

		String[] pairs = string.split(PerunNotifPoolMessage.DELIMITER);
		Map<String, String> result = new HashMap<String, String>();
		for (String pair : pairs) {
			String key = pair.substring(0, pair.indexOf("="));
			String value = pair.substring(pair.indexOf("=") + 1);
			String decodedValue = URLDecoder.decode(value, "utf-8");

			result.put(key, decodedValue);
		}

		return result;
	}

	private static String serializeMap(Map<String, String> map) throws UnsupportedEncodingException {
		StringBuffer serializedKeyAttributes = new StringBuffer();
		List<String> sortedList = new ArrayList<String>(map.keySet());
		Collections.sort(sortedList);
		for (String key : sortedList) {
			if (map.get(key) != null) {
				String encodedKey = URLEncoder.encode(map.get(key), "utf-8");
				serializedKeyAttributes.append(key).append("=").append(encodedKey).append(PerunNotifPoolMessage.DELIMITER);
			}
		}

		return serializedKeyAttributes.toString();
	}

	@Override
	public String toString() {
		try {
			return "id: " + getId() + " regex id: " + getRegexId() + " template id: " + getTemplateId()
				+ " key attributes:" + getSerializedKeyAttributes() + " created: " + getCreated()
				+ " notif message: " + getNotifMessage();
		} catch (UnsupportedEncodingException ex) {
			return "id: " + getId() + " regex id: " + getRegexId() + " template id: " + getTemplateId()
				+ " key attributes: CANNOT SERIALIZE !" + " created: " + getCreated()
				+ " notif message: " + getNotifMessage();
		}
	}

}
