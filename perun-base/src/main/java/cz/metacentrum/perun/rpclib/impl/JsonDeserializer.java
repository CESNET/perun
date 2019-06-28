package cz.metacentrum.perun.rpclib.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import cz.metacentrum.perun.cabinet.model.Author;
import cz.metacentrum.perun.cabinet.model.Category;
import cz.metacentrum.perun.cabinet.model.Publication;
import cz.metacentrum.perun.cabinet.model.PublicationForGUI;
import cz.metacentrum.perun.cabinet.model.PublicationSystem;
import cz.metacentrum.perun.cabinet.model.Thanks;
import cz.metacentrum.perun.cabinet.model.ThanksForGUI;
import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.registrar.model.Application;
import cz.metacentrum.perun.registrar.model.ApplicationForm;
import cz.metacentrum.perun.registrar.model.ApplicationFormItem;
import cz.metacentrum.perun.registrar.model.ApplicationFormItemWithPrefilledValue;
import cz.metacentrum.perun.registrar.model.ApplicationMail;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.exceptions.RpcException;
import cz.metacentrum.perun.rpclib.api.Deserializer;

/**
 * Deserializer that reads values from JSON content.
 *
 * @author Jan Klos <ddd@mail.muni.cz>
 * @since 0.1
 */
@SuppressWarnings("WeakerAccess")
public class JsonDeserializer extends Deserializer {

	@JsonIgnoreProperties({"name","baseFriendlyName", "friendlyNameParameter", "entity", "beanName"})
	public interface AttributeMixIn {}

	@JsonIgnoreProperties({"name", "value", "baseFriendlyName", "friendlyNameParameter", "entity", "beanName", "writable"})
	public interface AttributeDefinitionMixIn {}

	@JsonIgnoreProperties({"commonName", "displayName", "beanName", "specificUser", "majorSpecificType"})
	public interface UserMixIn {}

	@JsonIgnoreProperties({"uimessage"})
	public interface AuditMessageMixIn {}

	@JsonIgnoreProperties({"beanName"})
	public interface PerunBeanMixIn {}

	@JsonIgnoreProperties({"userExtSources"})
	public interface CandidateMixIn {}

	@JsonIgnoreProperties({"name"})
	public interface PerunExceptionMixIn {}

	@JsonIgnoreProperties({"hostNameFromDestination", "beanName"})
	public interface DestinationMixIn {}

	@JsonIgnoreProperties({"shortName", "beanName"})
	public interface GroupMixIn {}

	@JsonIgnoreProperties({"persistent","beanName"})
	public interface UserExtSourceMixIn {}

	@SuppressWarnings("unused")
	@JsonIgnoreProperties({"groupStatuses", "groupStatus", "beanName", "suspended"})
	public interface MemberMixIn {
		@JsonIgnore
		void setStatus(String status);

		@JsonDeserialize
		void setStatus(Status status);

		@JsonIgnore
		void setMembershipType(String type);

		@JsonDeserialize
		void setMembershipType(MembershipType type);

		@JsonIgnore
		void setGroupsStatuses(Map<Integer, MemberGroupStatus> groupsStatuses);

		@JsonIgnore
		void putGroupStatuses(Map<Integer, MemberGroupStatus> groupStatuses);

		@JsonIgnore
		void putGroupStatus(int groupId, MemberGroupStatus status);

	}

	private static final ObjectMapper mapper = new ObjectMapper();
	private static final Map<Class<?>,Class<?>> mixinMap = new HashMap<>();
	static {
		mixinMap.put(Attribute.class, AttributeMixIn.class);
		mixinMap.put(AttributeDefinition.class, AttributeDefinitionMixIn.class);
		mixinMap.put(User.class, UserMixIn.class);
		mixinMap.put(Member.class, MemberMixIn.class);
		mixinMap.put(AuditMessage.class, AuditMessageMixIn.class);
		mixinMap.put(PerunBean.class, PerunBeanMixIn.class);
		mixinMap.put(Candidate.class, CandidateMixIn.class);
		mixinMap.put(PerunException.class, PerunExceptionMixIn.class);
		mixinMap.put(Destination.class, DestinationMixIn.class);
		mixinMap.put(Group.class, GroupMixIn.class);
		mixinMap.put(UserExtSource.class, UserExtSourceMixIn.class);

		mixinMap.put(Application.class, PerunBeanMixIn.class);
		mixinMap.put(ApplicationForm.class, PerunBeanMixIn.class);
		mixinMap.put(ApplicationFormItem.class, PerunBeanMixIn.class);
		mixinMap.put(ApplicationFormItemWithPrefilledValue.class, PerunBeanMixIn.class);
		mixinMap.put(ApplicationMail.class, PerunBeanMixIn.class);

		mixinMap.put(Author.class, PerunBeanMixIn.class);
		mixinMap.put(Category.class, PerunBeanMixIn.class);
		mixinMap.put(Publication.class, PerunBeanMixIn.class);
		mixinMap.put(PublicationForGUI.class, PerunBeanMixIn.class);
		mixinMap.put(PublicationSystem.class, PerunBeanMixIn.class);
		mixinMap.put(Thanks.class, PerunBeanMixIn.class);
		mixinMap.put(ThanksForGUI.class, PerunBeanMixIn.class);

		mapper.setMixIns(mixinMap);
	}

	private JsonNode root;

	/**
	 * @param in {@code InputStream} to read JSON data from
	 * @throws IOException if an IO error occurs
	 * @throws RpcException if content of {@code in} is wrongly formatted
	 */
	public JsonDeserializer(InputStream in) throws IOException, RpcException {
		try {
			root = mapper.readTree(in);
		} catch (JsonProcessingException ex) {
			throw new RpcException(RpcException.Type.WRONGLY_FORMATTED_CONTENT, "not correct JSON data", ex);
		}

		/*
			 if (!root.isObject()) {
			 throw new RpcException(RpcException.Type.WRONGLY_FORMATTED_CONTENT, "not a JSON Object");
			 }
			 */
	}

	@Override
	public boolean contains(String name) {
		return root.get(name) != null;
	}

	@Override
	public String readString(String name) throws RpcException {
		JsonNode node;

		if (name == null) {
			// The object is not under root, but directly in the response
			node = root;
		} else {
			node = root.get(name);
		}

		if (node.isNull()) {
			return null;
		}
		if (!node.isValueNode()) {
			throw new RpcException(RpcException.Type.CANNOT_DESERIALIZE_VALUE, node.toString() + " as String");
		}

		return node.asText();
	}

	@Override
	public String readString() throws RpcException {
		return readString(null);
	}

	@Override
	public int readInt(String name) throws RpcException {
		JsonNode node;

		if (name == null) {
			// The object is not under root, but directly in the response
			node = root;
			name = "root";
		} else {
			node = root.get(name);
		}

		if (node == null) {
			throw new RpcException(RpcException.Type.MISSING_VALUE, name);
		}

		if (!node.isInt()) {
			if (!node.isTextual()) {
				throw new RpcException(RpcException.Type.CANNOT_DESERIALIZE_VALUE, node.toString() + " as int");
			} else {
				try {
					return Integer.parseInt(node.textValue());
				} catch (NumberFormatException ex) {
					throw new RpcException(RpcException.Type.CANNOT_DESERIALIZE_VALUE, node.toString() + " as int", ex);
				}
			}
		}

		return node.intValue();
	}

	@Override
	public int readInt() throws RpcException {
		return readInt(null);
	}


	@Override
	public int[] readArrayOfInts(String name) throws RpcException {
		JsonNode node;

		if (name == null) {
			// The object is not under root, but directly in the response
			node = root;
		} else {
			node = root.get(name);
		}

		if (node.isNull()) {
			return null;
		}
		if (!node.isArray()) {
			throw new RpcException(RpcException.Type.CANNOT_DESERIALIZE_VALUE, node.toString() + " as int[] - not an array");
		}

		int[] array = new int[node.size()];

		for (int i = 0; i < node.size(); ++i) {
			JsonNode value = node.get(i);
			if (!value.isInt()) {
				throw new RpcException(RpcException.Type.CANNOT_DESERIALIZE_VALUE, node.toString() + " as int");
			}
			array[i] = node.get(i).intValue();
		}
		return array;
	}

	@Override
	public int[] readArrayOfInts() throws RpcException {
		return readArrayOfInts(null);
	}

	@Override
	public <T> T read(String name, Class<T> valueType) throws RpcException {
		JsonNode node;

		if (name == null) {
			// The object is not under root, but directly in the response
			node = root;
			name = "root";
		} else {
			node = root.get(name);
		}

		if (node == null) {
			throw new RpcException(RpcException.Type.MISSING_VALUE, name);
		}
		if (node.isNull()) {
			return null;
		}
		if (!node.isObject()) {
			throw new RpcException(RpcException.Type.CANNOT_DESERIALIZE_VALUE, node.toString() + " as " + valueType.getSimpleName());
		}

		try {
			return mapper.readValue(node.traverse(), valueType);
		} catch (IOException ex) {
			throw new RpcException(RpcException.Type.CANNOT_DESERIALIZE_VALUE, node.toString() + " as " + valueType.getSimpleName(), ex);
		}
	}

	@Override
	public <T> T read(Class<T> valueType) throws RpcException {
		return read(null, valueType);
	}

	@Override
	public <T> List<T> readList(String name, Class<T> valueType) throws RpcException {
		JsonNode node;

		if (name == null) {
			// The object is not under root, but directly in the response
			node = root;
			name = "root";
		} else {
			node = root.get(name);
		}

		if (node == null) {
			throw new RpcException(RpcException.Type.MISSING_VALUE, name);
		}
		if (node.isNull()) {
			return null;
		}
		if (!node.isArray()) {
			throw new RpcException(RpcException.Type.CANNOT_DESERIALIZE_VALUE, node.toString() + " as List<" + valueType.getSimpleName() + "> - not an array");
		}

		try {
			List<T> list = new ArrayList<>(node.size());
			for (JsonNode e : node) {
				list.add(mapper.readValue(e.traverse(), valueType));
			}
			return list;
		} catch (IOException ex) {
			throw new RpcException(RpcException.Type.CANNOT_DESERIALIZE_VALUE, node.toString() + " as List<" + valueType.getSimpleName() + ">", ex);
		}
	}

	@Override
	public <T> List<T> readList(Class<T> valueType) throws RpcException {
		return readList(null, valueType);
	}
}
