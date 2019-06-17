package cz.metacentrum.perun.rpclib.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.ObjectMapper;

import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.exceptions.RpcException;
import cz.metacentrum.perun.rpclib.api.Deserializer;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

/**
 * Deserializer that reads values from JSON content.
 *
 * @author Jan Klos <ddd@mail.muni.cz>
 * @since 0.1
 */
@SuppressWarnings("WeakerAccess")
public class JsonDeserializer extends Deserializer {

	@JsonIgnoreProperties({"name", "baseFriendlyName", "friendlyNameParameter", "entity", "beanName"})
	public interface AttributeMixIn {}

	@JsonIgnoreProperties({"name", "value", "baseFriendlyName", "friendlyNameParameter", "entity", "beanName"})
	public interface AttributeDefinitionMixIn {}

	@JsonIgnoreProperties({"commonName", "displayName", "beanName"})
	public interface UserMixIn {}

	@JsonIgnoreProperties({"fullMessage"})
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

	@JsonIgnoreProperties({"persistent","beanName"})
	public interface UserExtSourceMixIn {}

	private static final ObjectMapper mapper = new ObjectMapper();
	static {
		mapper.getDeserializationConfig().addMixInAnnotations(Attribute.class, AttributeMixIn.class);
		mapper.getDeserializationConfig().addMixInAnnotations(AttributeDefinition.class, AttributeDefinitionMixIn.class);
		mapper.getDeserializationConfig().addMixInAnnotations(User.class, UserMixIn.class);
		mapper.getDeserializationConfig().addMixInAnnotations(Member.class, MemberMixIn.class);
		mapper.getDeserializationConfig().addMixInAnnotations(AuditMessage.class, AuditMessageMixIn.class);
		mapper.getDeserializationConfig().addMixInAnnotations(PerunBean.class, PerunBeanMixIn.class);
		mapper.getDeserializationConfig().addMixInAnnotations(Candidate.class, CandidateMixIn.class);
		mapper.getDeserializationConfig().addMixInAnnotations(PerunException.class, PerunExceptionMixIn.class);
		mapper.getDeserializationConfig().addMixInAnnotations(Destination.class, DestinationMixIn.class);
		mapper.getDeserializationConfig().addMixInAnnotations(Group.class, GroupMixIn.class);
		mapper.getDeserializationConfig().addMixInAnnotations(UserExtSource.class, UserExtSourceMixIn.class);

		mapper.getDeserializationConfig().addMixInAnnotations(Application.class, PerunBeanMixIn.class);
		mapper.getDeserializationConfig().addMixInAnnotations(ApplicationForm.class, PerunBeanMixIn.class);
		mapper.getDeserializationConfig().addMixInAnnotations(ApplicationFormItem.class, PerunBeanMixIn.class);
		mapper.getDeserializationConfig().addMixInAnnotations(ApplicationFormItemWithPrefilledValue.class, PerunBeanMixIn.class);
		mapper.getDeserializationConfig().addMixInAnnotations(ApplicationMail.class, PerunBeanMixIn.class);

		mapper.getDeserializationConfig().addMixInAnnotations(Author.class, PerunBeanMixIn.class);
		mapper.getDeserializationConfig().addMixInAnnotations(Category.class, PerunBeanMixIn.class);
		mapper.getDeserializationConfig().addMixInAnnotations(Publication.class, PerunBeanMixIn.class);
		mapper.getDeserializationConfig().addMixInAnnotations(PublicationForGUI.class, PerunBeanMixIn.class);
		mapper.getDeserializationConfig().addMixInAnnotations(PublicationSystem.class, PerunBeanMixIn.class);
		mapper.getDeserializationConfig().addMixInAnnotations(Thanks.class, PerunBeanMixIn.class);
		mapper.getDeserializationConfig().addMixInAnnotations(ThanksForGUI.class, PerunBeanMixIn.class);

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
					return Integer.parseInt(node.getTextValue());
				} catch (NumberFormatException ex) {
					throw new RpcException(RpcException.Type.CANNOT_DESERIALIZE_VALUE, node.toString() + " as int", ex);
				}
			}
		}

		return node.getIntValue();
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
			array[i] = node.get(i).getIntValue();
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
			return mapper.readValue(node, valueType);
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
				list.add(mapper.readValue(e, valueType));
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
