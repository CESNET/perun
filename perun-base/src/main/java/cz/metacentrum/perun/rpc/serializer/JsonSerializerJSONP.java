package cz.metacentrum.perun.rpc.serializer;

import cz.metacentrum.perun.cabinet.model.Author;
import cz.metacentrum.perun.cabinet.model.Authorship;
import cz.metacentrum.perun.cabinet.model.Category;
import cz.metacentrum.perun.cabinet.model.Publication;
import cz.metacentrum.perun.cabinet.model.Thanks;
import cz.metacentrum.perun.core.api.*;
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
 * JSONP serializer.
 *
 * This serializer strips all auditing data from PerunBean objects
 * before sending an output with exception of Authorship.class.
 *
 * Please note, that many objects (especially from perun-registrar or
 * perun-cabinet) aren't PerunBeans and contain similar properties.
 *
 * By default stripped properties are: createdAt, createdBy,
 * modifiedAt, modifiedBy, createdByUid, modifiedByUid, valueCreatedAt,
 * valueCreatedBy, valueModifiedAt, valueModifiedBy.
 *
 * @author Michal Karm Babacek <michal.babacek@gmail.com>
 * @since 0.1
 */
public final class JsonSerializerJSONP implements Serializer {

	@JsonIgnoreProperties({"name", "createdAt", "createdBy", "modifiedAt", "modifiedBy", "createdByUid",
			"modifiedByUid", "valueCreatedAt", "valueCreatedBy", "valueModifiedAt", "valueModifiedBy"})
	private interface AttributeMixIn {
	}

	@JsonIgnoreProperties({"name", "createdAt", "createdBy", "modifiedAt", "modifiedBy", "createdByUid", "modifiedByUid"})
	private interface AttributeDefinitionMixIn {
	}

	@JsonIgnoreProperties({"commonName", "displayName", "createdAt", "createdBy", "modifiedAt", "modifiedBy",
			"createdByUid", "modifiedByUid"})
	private interface UserMixIn {
	}

	@JsonIgnoreProperties({"cause", "localizedMessage", "stackTrace"})
	private interface ExceptionMixIn {
	}

	@JsonIgnoreProperties({"userExtSources"})
	private interface CandidateMixIn {
	}

	@JsonIgnoreProperties({"createdAt", "createdBy", "modifiedAt", "modifiedBy", "createdByUid", "modifiedByUid"})
	private interface PerunBeanMixIn {
	}

	@JsonIgnoreProperties({"perunPrincipal"})
	private interface PerunRequestMixIn {
	}

	/* FOR Cabinet PerunBeans we need createdBy etc. data */
	@JsonIgnoreProperties({})
	private interface CabinetMixIn {
	}

	public static final String CONTENT_TYPE = "text/javascript; charset=utf-8";
	private static final ObjectMapper mapper = new ObjectMapper();

	static {
		mapper.getSerializationConfig().addMixInAnnotations(Attribute.class, AttributeMixIn.class);
		mapper.getSerializationConfig().addMixInAnnotations(AttributeDefinition.class, AttributeDefinitionMixIn.class);
		mapper.getSerializationConfig().addMixInAnnotations(User.class, UserMixIn.class);
		mapper.getSerializationConfig().addMixInAnnotations(Candidate.class, CandidateMixIn.class);
		mapper.getSerializationConfig().addMixInAnnotations(PerunException.class, ExceptionMixIn.class);
		mapper.getSerializationConfig().addMixInAnnotations(PerunRuntimeException.class, ExceptionMixIn.class);
		mapper.getSerializationConfig().addMixInAnnotations(PerunBean.class, PerunBeanMixIn.class);
		mapper.getSerializationConfig().addMixInAnnotations(PerunRequest.class, PerunRequestMixIn.class);
		mapper.getSerializationConfig().addMixInAnnotations(Authorship.class, CabinetMixIn.class);
		mapper.getSerializationConfig().addMixInAnnotations(Author.class, CabinetMixIn.class);
		mapper.getSerializationConfig().addMixInAnnotations(Category.class, CabinetMixIn.class);
		mapper.getSerializationConfig().addMixInAnnotations(Publication.class, CabinetMixIn.class);
		mapper.getSerializationConfig().addMixInAnnotations(Thanks.class, CabinetMixIn.class);
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
	public JsonSerializerJSONP(OutputStream out, HttpServletRequest request, HttpServletResponse response) throws IOException {
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
			gen.writeRaw(callback + "(");
			gen.writeObject(pex);
			gen.writeRaw(");");
			gen.flush();
		}
		gen.close();
	}

	@Override
	public void writePerunRuntimeException(PerunRuntimeException prex) throws IOException {
		JsonGenerator gen = jsonFactory.createJsonGenerator(out, JsonEncoding.UTF8);

		if (prex == null) {
			throw new IllegalArgumentException("prex is null");
		} else {
			gen.writeRaw(callback + "(");
			gen.writeObject(prex);
			gen.writeRaw(");");
			gen.flush();
		}
		gen.close();
	}
}
