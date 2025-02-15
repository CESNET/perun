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
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Ban;
import cz.metacentrum.perun.core.api.Candidate;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.PerunRequest;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.exceptions.RpcException;
import cz.metacentrum.perun.core.api.exceptions.rt.PerunRuntimeException;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.model.TaskResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JSONP serializer.
 * <p>
 * This serializer strips all auditing data from PerunBean objects before sending an output with exception of
 * Authorship.class.
 * <p>
 * Please note, that many objects (especially from perun-registrar or perun-cabinet) aren't PerunBeans and contain
 * similar properties.
 * <p>
 * By default stripped properties are: createdAt, createdBy, modifiedAt, modifiedBy, createdByUid, modifiedByUid,
 * valueCreatedAt, valueCreatedBy, valueModifiedAt, valueModifiedBy.
 *
 * @author Michal Karm Babacek <michal.babacek@gmail.com>
 * @since 0.1
 */
public final class JsonSerializerJsonP implements Serializer {

  public static final String CONTENT_TYPE = "text/javascript; charset=utf-8";
  private static final ObjectMapper MAPPER = new ObjectMapper();
  private static final Map<Class<?>, Class<?>> MIXIN_MAP = new HashMap<>();
  private static final JsonFactory JSON_FACTORY = new JsonFactory();

  static {

    JavaTimeModule module = new JavaTimeModule();
    MAPPER.registerModule(module);
    // make mapper to serialize dates and timestamps like "YYYY-MM-DD" or "YYYY-MM-DDTHH:mm:ss.SSSSSS"
    MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    MIXIN_MAP.put(Attribute.class, AttributeMixIn.class);
    MIXIN_MAP.put(AttributeDefinition.class, AttributeDefinitionMixIn.class);
    MIXIN_MAP.put(User.class, UserMixIn.class);
    MIXIN_MAP.put(Candidate.class, CandidateMixIn.class);
    MIXIN_MAP.put(PerunException.class, ExceptionMixIn.class);
    MIXIN_MAP.put(PerunRuntimeException.class, ExceptionMixIn.class);
    MIXIN_MAP.put(PerunBean.class, PerunBeanMixIn.class);
    MIXIN_MAP.put(PerunRequest.class, PerunRequestMixIn.class);
    MIXIN_MAP.put(Authorship.class, CabinetMixIn.class);
    MIXIN_MAP.put(Author.class, CabinetMixIn.class);
    MIXIN_MAP.put(Category.class, CabinetMixIn.class);
    MIXIN_MAP.put(Publication.class, CabinetMixIn.class);
    MIXIN_MAP.put(Thanks.class, CabinetMixIn.class);
    MIXIN_MAP.put(Task.class, TaskMixIn.class);
    MIXIN_MAP.put(TaskResult.class, TaskResultMixIn.class);
    MIXIN_MAP.put(Ban.class, BanMixIn.class);

    MAPPER.setMixIns(MIXIN_MAP);
  }

  static {
    // FIXME removed disable(JsonGenerator.Feature.FLUSH_PASSED_TO_STREAM)
    JSON_FACTORY.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET).disable(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT)
        .setCodec(MAPPER);
  }

  private OutputStream out;
  private String callback;

  /**
   * @param out {@code OutputStream} to output serialized data
   * @throws IOException if an IO error occurs
   */
  public JsonSerializerJsonP(OutputStream out, HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    this.out = out;
    this.callback = request.getParameter("callback");
  }

  @Override
  public String getContentType() {
    return CONTENT_TYPE;
  }

  @Override
  public void write(Object object) throws IOException {
    JsonGenerator gen = JSON_FACTORY.createGenerator(out, JsonEncoding.UTF8);

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
    JsonGenerator gen = JSON_FACTORY.createGenerator(out, JsonEncoding.UTF8);

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
    JsonGenerator gen = JSON_FACTORY.createGenerator(out, JsonEncoding.UTF8);

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

  @JsonIgnoreProperties({"name", "createdAt", "createdBy", "modifiedAt", "modifiedBy", "createdByUid", "modifiedByUid",
      "valueCreatedAt", "valueCreatedBy", "valueModifiedAt", "valueModifiedBy"})
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

  @SuppressWarnings("unused")
  private interface TaskMixIn {
    @JsonIgnore
    LocalDateTime getEndTime();

    @JsonSerialize
    @JsonProperty(value = "endTime")
    Long getEndTimeAsLong();

    @JsonIgnore
    LocalDateTime getGenEndTime();

    @JsonSerialize
    @JsonProperty(value = "genEndTime")
    Long getGenEndTimeAsLong();

    @JsonIgnore
    LocalDateTime getGenStartTime();

    @JsonSerialize
    @JsonProperty(value = "genStartTime")
    Long getGenStartTimeAsLong();

    @JsonIgnore
    LocalDateTime getSchedule();

    @JsonSerialize
    @JsonProperty(value = "schedule")
    Long getScheduleAsLong();

    @JsonIgnore
    LocalDateTime getSendEndTime();

    @JsonSerialize
    @JsonProperty(value = "sendEndTime")
    Long getSendEndTimeAsLong();

    @JsonIgnore
    LocalDateTime getSendStartTime();

    @JsonSerialize
    @JsonProperty(value = "sendStartTime")
    Long getSendStartTimeAsLong();

    @JsonIgnore
    LocalDateTime getSentToEngine();

    @JsonSerialize
    @JsonProperty(value = "sentToEngine")
    Long getSentToEngineAsLong();

    @JsonIgnore
    LocalDateTime getStartTime();

    @JsonSerialize
    @JsonProperty(value = "startTime")
    Long getStartTimeAsLong();
  }

  @SuppressWarnings("unused")
  private interface TaskResultMixIn {

    @JsonIgnore
    Date getTimestamp();

    @JsonSerialize
    @JsonProperty(value = "timestamp")
    Long getTimestampAsLong();

  }

  @SuppressWarnings("unused")
  private interface BanMixIn {

    @JsonIgnore
    Date getValidityTo();

    @JsonSerialize
    @JsonProperty(value = "validityTo")
    Long getValidityToAsLong();

  }
}
