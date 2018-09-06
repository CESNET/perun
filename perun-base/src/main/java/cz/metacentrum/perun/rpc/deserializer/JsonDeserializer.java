package cz.metacentrum.perun.rpc.deserializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cz.metacentrum.perun.cabinet.model.*;

import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.registrar.model.*;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

import cz.metacentrum.perun.core.api.exceptions.RpcException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;

/**
 * Deserializer for JSON / JSONP data format.
 *
 * Reads parameters from body (InputStream) of request which is typically POST.
 * Doesn't read any parameters form URL!
 *
 * While deserializing objects from JSON some of their properties may be ignored.
 * They are not read from input and also not required to be present in input
 * in order to match JSON object to the right Java object.
 * They are set as NULL in Java object (if present as variable) or ignored at all.
 *
 * @author Jan Klos <ddd@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
@SuppressWarnings("Duplicates")
public class JsonDeserializer extends Deserializer {

	@JsonIgnoreProperties({"name","baseFriendlyName", "friendlyNameParameter", "entity", "beanName"})
	private interface AttributeMixIn {}

	@JsonIgnoreProperties({"name", "value", "baseFriendlyName", "friendlyNameParameter", "entity", "beanName", "writable"})
	private interface AttributeDefinitionMixIn {}

	@JsonIgnoreProperties({"commonName", "displayName", "beanName", "specificUser", "majorSpecificType"})
	private interface UserMixIn {}

	@JsonIgnoreProperties({"fullMessage"})
	private interface AuditMessageMixIn {}

	@JsonIgnoreProperties({"beanName"})
	private interface PerunBeanMixIn {}

	@JsonIgnoreProperties({"userExtSources"})
	private interface CandidateMixIn {}

	@JsonIgnoreProperties({"name"})
	private interface PerunExceptionMixIn {}

	@JsonIgnoreProperties({"hostNameFromDestination", "beanName"})
	private interface DestinationMixIn {}

	@JsonIgnoreProperties({"shortName", "beanName"})
	private interface GroupMixIn {}

	@JsonIgnoreProperties({"persistent","beanName"})
	private interface UserExtSourceMixIn {}

	@SuppressWarnings("unused")
	private interface MemberMixIn {
		@JsonIgnore
		void setStatus(String status);

		@JsonDeserialize
		void setStatus(Status status);

		@JsonIgnore
		void setMembershipType(String type);

		@JsonDeserialize
		void setMembershipType(MembershipType type);
	}

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
	private HttpServletRequest req;

	/**
	 * Create deserializer for JSON/JSONP data format.
	 *
	 * @param request HttpServletRequest this deserializer is about to process
	 *
	 * @throws IOException if an IO error occurs
	 * @throws RpcException if content of {@code in} is wrongly formatted
	 */
	public JsonDeserializer(HttpServletRequest request) throws IOException, RpcException {

		this.req = request;

		try {
			root = mapper.readTree(req.getInputStream());
		} catch (JsonProcessingException ex) {
			throw new RpcException(RpcException.Type.WRONGLY_FORMATTED_CONTENT, "not correct JSON data", ex);
		}

		if (!root.isObject()) {
			throw new RpcException(RpcException.Type.WRONGLY_FORMATTED_CONTENT, "not a JSON Object");
		}
	}

	public JsonDeserializer(InputStream in) throws IOException, RpcException {
		try {
			root = mapper.readTree(in);
		} catch (JsonProcessingException ex) {
			throw new RpcException(RpcException.Type.WRONGLY_FORMATTED_CONTENT, "not correct JSON data", ex);
		}
	}

	@Override
	public boolean contains(String name) {
		return root.get(name) != null;
	}

	@Override
	public String readString(String name) throws RpcException {
		JsonNode node = root.get(name);

		if (node == null) {
			throw new RpcException(RpcException.Type.MISSING_VALUE, name);
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
	public Boolean readBoolean(String name) throws RpcException {
		JsonNode node = root.get(name);

		if (node == null) {
			throw new RpcException(RpcException.Type.MISSING_VALUE, name);
		}
		if (node.isNull()) {
			return null;
		}
		if (!node.isValueNode()) {
			throw new RpcException(RpcException.Type.CANNOT_DESERIALIZE_VALUE, node.toString() + " as Boolean");
		}

		return node.asBoolean();
	}

	@Override
	public int readInt(String name) throws RpcException {
		JsonNode node = root.get(name);

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
	public int[] readArrayOfInts(String name) throws RpcException {
		JsonNode node = root.get(name);

		if (node == null) {
			throw new RpcException(RpcException.Type.MISSING_VALUE, name);
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
	public <T> T read(Class<T> valueType) throws RpcException {
		return this.read(null, valueType);
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
	public PerunBean readPerunBean(String name) throws RpcException {
		JsonNode node = root.get(name);

		if (node == null) {
			throw new RpcException(RpcException.Type.MISSING_VALUE, name);
		}
		if (node.isNull()) {
			return null;
		}
		if (!node.isObject()) {
			throw new RpcException(RpcException.Type.CANNOT_DESERIALIZE_VALUE, node.toString() + " as PerunBean.");
		}

		try {
			String beanName = node.get("beanName").getTextValue();
			if(beanName == null) {
				throw new RpcException(RpcException.Type.CANNOT_DESERIALIZE_VALUE, node.toString() + " as List<PerunBean> - missing beanName info");
			}
			return (PerunBean) mapper.readValue(node, Class.forName("cz.metacentrum.perun.core.api." + beanName));
		} catch (ClassNotFoundException ex) {
			throw new RpcException(RpcException.Type.CANNOT_DESERIALIZE_VALUE, node.toString() + " as List<PerunBean> - class not found");
		} catch (IOException ex) {
			throw new RpcException(RpcException.Type.CANNOT_DESERIALIZE_VALUE, node.toString() + " as PerunBean");
		}
	}

	@Override
	public List<PerunBean> readListPerunBeans(String name) throws RpcException {
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
			throw new RpcException(RpcException.Type.CANNOT_DESERIALIZE_VALUE, node.toString() + " as List<PerunBean> - not an array");
		}

		try {
			List<PerunBean> list = new ArrayList<>(node.size());
			for (JsonNode e : node) {
				String beanName = e.get("beanName").getTextValue();

				if(beanName == null) {
					throw new RpcException(RpcException.Type.CANNOT_DESERIALIZE_VALUE, node.toString() + " as List<PerunBean> - missing beanName info");
				}
				list.add((PerunBean) mapper.readValue(e, Class.forName("cz.metacentrum.perun.core.api." + beanName)));
			}
			return list;
		} catch (ClassNotFoundException ex) {
			throw new RpcException(RpcException.Type.CANNOT_DESERIALIZE_VALUE, node.toString() + " as List<PerunBean> - class not found");
		} catch (IOException ex) {
			throw new RpcException(RpcException.Type.CANNOT_DESERIALIZE_VALUE, node.toString() + " as List<PerunBean>", ex);
		}
	}

	@Override
	public <T> List<T> readList(Class<T> valueType) throws RpcException {
		return readList(null, valueType);
	}

	public String readAll() throws RpcException {
		return root.toString();
	}

	@Override
	public HttpServletRequest getServletRequest() {
		return this.req;
	}

}
