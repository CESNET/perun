package cz.metacentrum.perun.rpc.serializer;

import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.api.exceptions.ExtendMembershipException;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.exceptions.rt.PerunRuntimeException;
import cz.metacentrum.perun.core.api.exceptions.RpcException;
import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.ObjectMapper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

/**
 * JSON serializer.
 *
 * @author Michal Karm Babacek <michal.babacek@gmail.com>
 * @since 0.1
 */
public final class JsonSerializerGWT implements Serializer {

	@JsonIgnoreProperties({"name"})
	private interface AttributeMixIn {
	}

	@JsonIgnoreProperties({"name"})
	private interface AttributeDefinitionMixIn {
	}

	@JsonIgnoreProperties({"commonName", "displayName"})
	private interface UserMixIn {
	}

	@JsonIgnoreProperties({"userExtSources"})
	private interface CandidateMixIn {
	}

	public static final String CONTENT_TYPE = "text/javascript; charset=utf-8";
	private static final ObjectMapper mapper = new ObjectMapper();

	static {
		mapper.getSerializationConfig().addMixInAnnotations(Attribute.class, AttributeMixIn.class);
		mapper.getSerializationConfig().addMixInAnnotations(AttributeDefinition.class, AttributeDefinitionMixIn.class);
		mapper.getSerializationConfig().addMixInAnnotations(User.class, UserMixIn.class);
		mapper.getSerializationConfig().addMixInAnnotations(Candidate.class, CandidateMixIn.class);
	}

	private static final JsonFactory jsonFactory = new JsonFactory();

	static {
		// FIXME odstraneno disable(JsonGenerator.Feature.FLUSH_PASSED_TO_STREAM)
		jsonFactory.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET).disable(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT).setCodec(mapper);
	}

	private OutputStream out;
	private String callback;

	/**
	 * @param out {@code OutputStream} to output serialized data
	 * @throws IOException if an IO error occurs
	 */
	public JsonSerializerGWT(OutputStream out, HttpServletRequest request, HttpServletResponse response) throws IOException {
		this.out = out;
		this.callback = request.getParameter("callback");
	}

	@Override
	public String getContentType() {
		return CONTENT_TYPE;
	}

	@Override
	public void write(Object object) throws RpcException, IOException {
		JsonGenerator gen = jsonFactory.createJsonGenerator(out, JsonEncoding.UTF8);

		if (object instanceof Throwable) {
			throw new IllegalArgumentException("Tried to serialize a throwable object using write()", (Throwable) object);
		}
		try {
			gen.writeRaw(callback + "(");
			gen.writeObject(object);
			gen.writeRaw(");");
			gen.flush();
			gen.close();
		} catch (JsonProcessingException ex) {
			throw new RpcException(RpcException.Type.CANNOT_SERIALIZE_VALUE, ex);
		}
	}

	@Override
	public void writePerunException(PerunException pex) throws IOException {
		JsonGenerator gen = jsonFactory.createJsonGenerator(out, JsonEncoding.UTF8);

		if (pex == null) {
			throw new IllegalArgumentException("pex is null");
		} else {
			gen.writeStartObject();

			gen.writeStringField("errorId", pex.getErrorId());

			if (pex instanceof RpcException) {
				gen.writeStringField("type", ((RpcException) pex).getType());
				gen.writeStringField("errorInfo", ((RpcException) pex).getErrorInfo());
			} else {
				gen.writeStringField("type", pex.getClass().getSimpleName());
				gen.writeStringField("errorInfo", pex.getMessage());
			}

			// write reason param for this case
			if (pex instanceof ExtendMembershipException) {
				gen.writeStringField("reason", ((ExtendMembershipException) pex).getReason().toString());
			}

			gen.writeEndObject();
		}

		gen.close();
	}

	@Override
	public void writePerunRuntimeException(PerunRuntimeException prex) throws IOException {
		JsonGenerator gen = jsonFactory.createJsonGenerator(out, JsonEncoding.UTF8);

		if (prex == null) {
			throw new IllegalArgumentException("prex is null");
		} else {
			gen.writeStartObject();

			gen.writeStringField("errorId", prex.getErrorId());
			gen.writeStringField("type", prex.getClass().getSimpleName());
			gen.writeStringField("errorInfo", prex.getMessage());

			gen.writeEndObject();
		}

		gen.close();
	}
}
