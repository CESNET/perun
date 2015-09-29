package cz.metacentrum.perun.rpclib.impl;

import java.io.IOException;
import java.io.OutputStream;

import cz.metacentrum.perun.core.api.ExtSource;
import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.ObjectMapper;

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

	static {
		mapper.getSerializationConfig().addMixInAnnotations(Attribute.class, AttributeMixIn.class);
		mapper.getSerializationConfig().addMixInAnnotations(AttributeDefinition.class, AttributeDefinitionMixIn.class);
		mapper.getSerializationConfig().addMixInAnnotations(User.class, UserMixIn.class);
	}
	private static final JsonFactory jsonFactory = new JsonFactory();

	static {
		//FIXME odstraneno disable(JsonGenerator.Feature.FLUSH_PASSED_TO_STREAM)
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
	public void write(Object object) throws RpcException, IOException {
		JsonGenerator gen = jsonFactory.createJsonGenerator(out, JsonEncoding.UTF8);

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
