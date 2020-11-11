package cz.metacentrum.perun.rpclib.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import cz.metacentrum.perun.core.api.AuditMessage;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.RpcException;
import cz.metacentrum.perun.rpclib.api.Serializer;

/**
 * JSON serializer.
 *
 * @author Jan Klos <ddd@mail.muni.cz>
 * @since 0.1
 */
public final class JsonSerializer implements Serializer {

	@JsonIgnoreProperties({"name"})
	private interface AttributeMixIn {
	}

	@JsonIgnoreProperties({"name"})
	private interface AttributeDefinitionMixIn {}

	@JsonIgnoreProperties({"commonName", "displayName"})
	private interface UserMixIn {
	}

	private static final ObjectMapper mapper = new ObjectMapper();
	private static final Map<Class<?>,Class<?>> mixinMap = new HashMap<>();

	static {
		mixinMap.put(Attribute.class, AttributeMixIn.class);
		mixinMap.put(AttributeDefinition.class, AttributeDefinitionMixIn.class);
		mixinMap.put(User.class, UserMixIn.class);

		mapper.setMixIns(mixinMap);
	}
	private static final JsonFactory jsonFactory = new JsonFactory();

	static {
		//FIXME removed disable(JsonGenerator.Feature.FLUSH_PASSED_TO_STREAM)
		//jsonFactory.enable(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS);
		jsonFactory.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET).disable(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT).setCodec(mapper);
	}
	private OutputStream out;

	/**
	 * @param out {@code OutputStream} to output serialized data
	 * @throws IOException if an IO error occurs
	 */
	public JsonSerializer(OutputStream out) throws IOException {
		this.out = out;
	}

	@Override
	public void write(Object object) throws IOException {
		JsonGenerator gen = jsonFactory.createGenerator(out, JsonEncoding.UTF8);

		if (object instanceof Throwable) {
			throw new IllegalArgumentException("Tried to serialize a throwable object using write()", (Throwable) object);
		}
		try {
			gen.writeObject(object);
			gen.close();
		} catch (JsonProcessingException ex) {
			throw new RpcException(RpcException.Type.CANNOT_SERIALIZE_VALUE, ex);
		}
	}
}
