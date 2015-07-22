package cz.metacentrum.perun.rpclib.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.ObjectMapper;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AuditMessage;
import cz.metacentrum.perun.core.api.Candidate;
import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Group;

import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.exceptions.RpcException;
import cz.metacentrum.perun.rpclib.api.Deserializer;
import cz.metacentrum.perun.taskslib.model.ExecService;

/**
 * Deserializer that reads values from JSON content.
 *
 * @author Jan Klos <ddd@mail.muni.cz>
 * @since 0.1
 */
public class JsonDeserializer extends Deserializer {

	@JsonIgnoreProperties({"name", "baseFriendlyName", "friendlyNameParameter", "entity", "beanName"})
	private interface AttributeMixIn {}

	@JsonIgnoreProperties({"name", "value", "baseFriendlyName", "friendlyNameParameter", "entity", "beanName"})
	private interface AttributeDefinitionMixIn {}

	@JsonIgnoreProperties({"commonName", "displayName", "beanName"})
	private interface UserMixIn {}

	@JsonIgnoreProperties({"fullMessage"})
	private interface AuditMessageMixIn {}

	@JsonIgnoreProperties({"beanName"})
	private interface PerunBeanMixIn {}

	@JsonIgnoreProperties({"beanName"})
	private interface ExecServiceMixIn {}

	@JsonIgnoreProperties({"userExtSources"})
	private interface CandidateMixIn {}

	@JsonIgnoreProperties({"name"})
	private interface PerunExceptionMixIn {}

	@JsonIgnoreProperties({"hostNameFromDestination", "beanName"})
	private interface DestinationMixIn {}

	@JsonIgnoreProperties({"shortName", "beanName"})
	private interface GroupMixIn {}

	private interface MemberMixIn {
		@JsonIgnore
		public void setStatus(String status);
	}

	private static final ObjectMapper mapper = new ObjectMapper();
	static {
		mapper.getDeserializationConfig().addMixInAnnotations(Attribute.class, AttributeMixIn.class);
		mapper.getDeserializationConfig().addMixInAnnotations(AttributeDefinition.class, AttributeDefinitionMixIn.class);
		mapper.getDeserializationConfig().addMixInAnnotations(User.class, UserMixIn.class);
		mapper.getDeserializationConfig().addMixInAnnotations(Member.class, MemberMixIn.class);
		mapper.getDeserializationConfig().addMixInAnnotations(AuditMessage.class, AuditMessageMixIn.class);
		mapper.getDeserializationConfig().addMixInAnnotations(PerunBean.class, PerunBeanMixIn.class);
		mapper.getDeserializationConfig().addMixInAnnotations(ExecService.class, ExecServiceMixIn.class);
		mapper.getDeserializationConfig().addMixInAnnotations(Candidate.class, CandidateMixIn.class);
		mapper.getDeserializationConfig().addMixInAnnotations(PerunException.class, PerunExceptionMixIn.class);
		mapper.getDeserializationConfig().addMixInAnnotations(Destination.class, DestinationMixIn.class);
		mapper.getDeserializationConfig().addMixInAnnotations(Group.class, GroupMixIn.class);
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
		if (root.get(name) != null) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public String readString(String name) throws RpcException {
		JsonNode node;

		if (name == null) {
			// The object is not under root, but directly in the response
			node = root;
			name = "root";
		} else {
			node = root.get(name);
		}

		if (node.isNull()) {
			return null;
		}
		if (!node.isValueNode()) {
			throw new RpcException(RpcException.Type.CANNOT_DESERIALIZE_VALUE, node.toString() + " as String");
		}

		return node.getValueAsText();
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
			name = "root";
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
			T obj = mapper.readValue(node, valueType);
			return obj;
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
			List<T> list = new ArrayList<T>(node.size());
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
