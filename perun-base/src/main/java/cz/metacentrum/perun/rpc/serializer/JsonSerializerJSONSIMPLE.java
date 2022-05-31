package cz.metacentrum.perun.rpc.serializer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import cz.metacentrum.perun.cabinet.model.Author;
import cz.metacentrum.perun.cabinet.model.Authorship;
import cz.metacentrum.perun.cabinet.model.Category;
import cz.metacentrum.perun.cabinet.model.Publication;
import cz.metacentrum.perun.cabinet.model.Thanks;
import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.exceptions.rt.PerunRuntimeException;
import cz.metacentrum.perun.core.api.exceptions.RpcException;
import cz.metacentrum.perun.taskslib.model.Task;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * JSON simple serializer which strips a lot of object params (used by engine).
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public final class JsonSerializerJSONSIMPLE implements Serializer {

	@JsonIgnoreProperties({"name", "createdAt", "createdBy", "modifiedAt", "modifiedBy", "createdByUid",
			"modifiedByUid", "valueCreatedAt", "valueCreatedBy", "valueModifiedAt", "valueModifiedBy", "beanName",
			"writable", "displayName", "description", "entity", "baseFriendlyName", "friendlyNameParameter", "id", "type"})
	private interface AttributeMixIn {
	}

	@JsonIgnoreProperties({"name", "createdAt", "createdBy", "modifiedAt", "modifiedBy", "createdByUid",
			"modifiedByUid", "beanName", "writable", "displayName", "description", "entity", "baseFriendlyName",
			"friendlyNameParameter", "id", "type"})
	private interface AttributeDefinitionMixIn {
	}

	@JsonIgnoreProperties({"commonName", "displayName", "createdAt", "createdBy", "modifiedAt", "modifiedBy",
			"createdByUid", "modifiedByUid", "beanName"})
	private interface UserMixIn {
	}

	@JsonIgnoreProperties({"cause", "localizedMessage", "stackTrace", "beanName"})
	private interface ExceptionMixIn {
	}

	@JsonIgnoreProperties({"userExtSources", "beanName"})
	private interface CandidateMixIn {
	}

	@JsonIgnoreProperties({"createdAt", "createdBy", "modifiedAt", "modifiedBy", "createdByUid", "modifiedByUid", "beanName"})
	private interface PerunBeanMixIn {
	}

	@JsonIgnoreProperties({"perunPrincipal", "beanName"})
	private interface PerunRequestMixIn {
	}

	/* FOR Cabinet PerunBeans we need createdBy etc. data */
	@JsonIgnoreProperties({})
	private interface CabinetMixIn {
	}

	@SuppressWarnings("unused")
	private interface TaskMixIn {
		@JsonSerialize
		@JsonProperty(value = "startTime")
		Long getStartTimeAsLong();

		@JsonIgnore
		LocalDateTime getStartTime();

		@JsonSerialize
		@JsonProperty(value = "schedule")
		Long getScheduleAsLong();

		@JsonIgnore
		LocalDateTime getSchedule();

		@JsonSerialize
		@JsonProperty(value = "genEndTime")
		Long getGenEndTimeAsLong();

		@JsonIgnore
		LocalDateTime getGenEndTime();

		@JsonSerialize
		@JsonProperty(value = "sendEndTime")
		Long getSendEndTimeAsLong();

		@JsonIgnore
		LocalDateTime getSendEndTime();

		@JsonSerialize
		@JsonProperty(value = "sendStartTime")
		Long getSendStartTimeAsLong();

		@JsonIgnore
		LocalDateTime getSendStartTime();

		@JsonSerialize
		@JsonProperty(value = "genStartTime")
		Long getGenStartTimeAsLong();

		@JsonIgnore
		LocalDateTime getGenStartTime();

		@JsonSerialize
		@JsonProperty(value = "sentToEngine")
		Long getSentToEngineAsLong();

		@JsonIgnore
		LocalDateTime getSentToEngine();

		@JsonSerialize
		@JsonProperty(value = "endTime")
		Long getEndTimeAsLong();

		@JsonIgnore
		LocalDateTime getEndTime();
	}

	public static final String CONTENT_TYPE = "application/json; charset=utf-8";
	private static final ObjectMapper mapper = new ObjectMapper();
	private static final Map<Class<?>,Class<?>> mixinMap = new HashMap<>();

	static {

		JavaTimeModule module = new JavaTimeModule();
		mapper.registerModule(module);
		// make mapper to serialize dates and timestamps like "YYYY-MM-DD" or "YYYY-MM-DDTHH:mm:ss.SSSSSS"
		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

		mixinMap.put(Attribute.class, AttributeMixIn.class);
		mixinMap.put(AttributeDefinition.class, AttributeDefinitionMixIn.class);
		mixinMap.put(User.class, UserMixIn.class);
		mixinMap.put(Candidate.class, CandidateMixIn.class);
		mixinMap.put(PerunException.class, ExceptionMixIn.class);
		mixinMap.put(PerunRuntimeException.class, ExceptionMixIn.class);
		mixinMap.put(PerunBean.class, PerunBeanMixIn.class);
		mixinMap.put(PerunRequest.class, PerunRequestMixIn.class);
		mixinMap.put(Authorship.class, CabinetMixIn.class);
		mixinMap.put(Author.class, CabinetMixIn.class);
		mixinMap.put(Category.class, CabinetMixIn.class);
		mixinMap.put(Publication.class, CabinetMixIn.class);
		mixinMap.put(Thanks.class, CabinetMixIn.class);
		mixinMap.put(Task.class, TaskMixIn.class);

		mapper.setMixIns(mixinMap);
	}

	private static final JsonFactory jsonFactory = new JsonFactory();

	static {
		//FIXME removed disable(JsonGenerator.Feature.FLUSH_PASSED_TO_STREAM)
		jsonFactory.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET).disable(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT).setCodec(mapper);
	}

	private OutputStream out;

	/**
	 * @param out {@code OutputStream} to output serialized data
	 * @throws IOException if an IO error occurs
	 */
	public JsonSerializerJSONSIMPLE(OutputStream out) throws IOException {
		this.out = out;
	}

	@Override
	public String getContentType() {
		return CONTENT_TYPE;
	}

	@Override
	public void write(Object object) throws IOException {
		JsonGenerator gen = jsonFactory.createGenerator(out, JsonEncoding.UTF8);

		try {
			gen.writeObject(object);
			gen.flush();
			gen.close();
		} catch (JsonProcessingException ex) {
			throw new RpcException(RpcException.Type.CANNOT_SERIALIZE_VALUE, ex);
		}
	}

	@Override
	public void writePerunException(PerunException pex) throws IOException {

		JsonGenerator gen = jsonFactory.createGenerator(out, JsonEncoding.UTF8);
		if (pex == null) {
			throw new IllegalArgumentException("pex is null");
		} else {
			gen.writeObject(pex);
			gen.flush();
		}
		gen.close();

	}

	@Override
	public void writePerunRuntimeException(PerunRuntimeException prex) throws IOException {

		JsonGenerator gen = jsonFactory.createGenerator(out, JsonEncoding.UTF8);
		if (prex == null) {
			throw new IllegalArgumentException("prex is null");
		} else {
			gen.writeObject(prex);
			gen.flush();
		}
		gen.close();

	}
}
