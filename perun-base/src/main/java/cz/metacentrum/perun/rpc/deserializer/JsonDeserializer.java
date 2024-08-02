package cz.metacentrum.perun.rpc.deserializer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import cz.metacentrum.perun.cabinet.model.Author;
import cz.metacentrum.perun.cabinet.model.Category;
import cz.metacentrum.perun.cabinet.model.Publication;
import cz.metacentrum.perun.cabinet.model.PublicationForGUI;
import cz.metacentrum.perun.cabinet.model.PublicationSystem;
import cz.metacentrum.perun.cabinet.model.Thanks;
import cz.metacentrum.perun.cabinet.model.ThanksForGUI;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Candidate;
import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.MemberGroupStatus;
import cz.metacentrum.perun.core.api.MembershipType;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.Status;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.exceptions.RpcException;
import cz.metacentrum.perun.registrar.model.Application;
import cz.metacentrum.perun.registrar.model.ApplicationForm;
import cz.metacentrum.perun.registrar.model.ApplicationFormItem;
import cz.metacentrum.perun.registrar.model.ApplicationFormItemWithPrefilledValue;
import cz.metacentrum.perun.registrar.model.ApplicationMail;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;

/**
 * Deserializer for JSON / JSONP data format.
 * <p>
 * Reads parameters from body (InputStream) of request which is typically POST. Doesn't read any parameters form URL!
 * <p>
 * While deserializing objects from JSON some of their properties may be ignored. They are not read from input and also
 * not required to be present in input in order to match JSON object to the right Java object. They are set as NULL in
 * Java object (if present as variable) or ignored at all.
 *
 * @author Jan Klos <ddd@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
@SuppressWarnings("Duplicates")
public class JsonDeserializer extends Deserializer {

  private static final ObjectMapper MAPPER = new ObjectMapper();
  private static final Map<Class<?>, Class<?>> MIXIN_MAP = new HashMap<>();

  static {
    MIXIN_MAP.put(Attribute.class, AttributeMixIn.class);
    MIXIN_MAP.put(AttributeDefinition.class, AttributeDefinitionMixIn.class);
    MIXIN_MAP.put(User.class, UserMixIn.class);
    MIXIN_MAP.put(Member.class, MemberMixIn.class);
    MIXIN_MAP.put(PerunBean.class, PerunBeanMixIn.class);
    MIXIN_MAP.put(Candidate.class, CandidateMixIn.class);
    MIXIN_MAP.put(PerunException.class, PerunExceptionMixIn.class);
    MIXIN_MAP.put(Destination.class, DestinationMixIn.class);
    MIXIN_MAP.put(Group.class, GroupMixIn.class);
    MIXIN_MAP.put(Resource.class, ResourceMixIn.class);
    MIXIN_MAP.put(UserExtSource.class, UserExtSourceMixIn.class);

    MIXIN_MAP.put(Application.class, PerunBeanMixIn.class);
    MIXIN_MAP.put(ApplicationForm.class, PerunBeanMixIn.class);
    MIXIN_MAP.put(ApplicationFormItem.class, PerunBeanMixIn.class);
    MIXIN_MAP.put(ApplicationFormItemWithPrefilledValue.class, PerunBeanMixIn.class);
    MIXIN_MAP.put(ApplicationMail.class, PerunBeanMixIn.class);

    MIXIN_MAP.put(Author.class, PerunBeanMixIn.class);
    MIXIN_MAP.put(Category.class, PerunBeanMixIn.class);
    MIXIN_MAP.put(Publication.class, PerunBeanMixIn.class);
    MIXIN_MAP.put(PublicationForGUI.class, PerunBeanMixIn.class);
    MIXIN_MAP.put(PublicationSystem.class, PerunBeanMixIn.class);
    MIXIN_MAP.put(Thanks.class, PerunBeanMixIn.class);
    MIXIN_MAP.put(ThanksForGUI.class, PerunBeanMixIn.class);

    MAPPER.setMixIns(MIXIN_MAP);

  }

  private final JsonNode root;
  private HttpServletRequest req;

  /**
   * Create deserializer for JSON/JSONP data format.
   *
   * @param request HttpServletRequest this deserializer is about to process
   * @throws IOException  if an IO error occurs
   * @throws RpcException if content of {@code in} is wrongly formatted
   */
  public JsonDeserializer(HttpServletRequest request) throws IOException {

    this.req = request;

    MAPPER.registerModule(new JavaTimeModule());
    MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    try {
      root = MAPPER.readTree(req.getInputStream());
    } catch (JsonProcessingException ex) {
      throw new RpcException(RpcException.Type.WRONGLY_FORMATTED_CONTENT, "not correct JSON data", ex);
    }

    if (!root.isObject()) {
      throw new RpcException(RpcException.Type.WRONGLY_FORMATTED_CONTENT, "not a JSON Object");
    }
  }

  public JsonDeserializer(InputStream in) throws IOException {
    try {
      root = MAPPER.readTree(in);
    } catch (JsonProcessingException ex) {
      throw new RpcException(RpcException.Type.WRONGLY_FORMATTED_CONTENT, "not correct JSON data", ex);
    }
  }

  @Override
  public boolean contains(String name) {
    return root.get(name) != null;
  }

  @Override
  public HttpServletRequest getServletRequest() {
    return this.req;
  }

  @Override
  public <T> T read(Class<T> valueType) {
    return this.read(null, valueType);
  }

  @Override
  public <T> T read(String name, Class<T> valueType) {
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
      throw new RpcException(RpcException.Type.CANNOT_DESERIALIZE_VALUE, node + " as " + valueType.getSimpleName());
    }

    try {
      return MAPPER.readValue(node.traverse(), valueType);
    } catch (IOException ex) {
      throw new RpcException(RpcException.Type.CANNOT_DESERIALIZE_VALUE, node + " as " + valueType.getSimpleName(), ex);
    }
  }

  public String readAll() {
    return root.toString();
  }

  @Override
  public int[] readArrayOfInts(String name) {
    JsonNode node = root.get(name);

    if (node == null) {
      throw new RpcException(RpcException.Type.MISSING_VALUE, name);
    }
    if (node.isNull()) {
      return null;
    }
    if (!node.isArray()) {
      throw new RpcException(RpcException.Type.CANNOT_DESERIALIZE_VALUE, node + " as int[] - not an array");
    }

    int[] array = new int[node.size()];

    for (int i = 0; i < node.size(); ++i) {
      JsonNode value = node.get(i);
      if (!value.isInt()) {
        throw new RpcException(RpcException.Type.CANNOT_DESERIALIZE_VALUE, node + " as int");
      }
      array[i] = node.get(i).intValue();
    }
    return array;
  }

  @Override
  public Boolean readBoolean(String name) {
    JsonNode node = root.get(name);

    if (node == null) {
      throw new RpcException(RpcException.Type.MISSING_VALUE, name);
    }
    if (node.isNull()) {
      return null;
    }
    if (!node.isValueNode()) {
      throw new RpcException(RpcException.Type.CANNOT_DESERIALIZE_VALUE, node + " as Boolean");
    }

    return node.asBoolean();
  }

  @Override
  public int readInt(String name) {
    JsonNode node = root.get(name);

    if (node == null) {
      throw new RpcException(RpcException.Type.MISSING_VALUE, name);
    }
    if (!node.isInt()) {
      if (!node.isTextual()) {
        throw new RpcException(RpcException.Type.CANNOT_DESERIALIZE_VALUE, node + " as int");
      } else {
        try {
          return Integer.parseInt(node.textValue());
        } catch (NumberFormatException ex) {
          throw new RpcException(RpcException.Type.CANNOT_DESERIALIZE_VALUE, node + " as int", ex);
        }
      }
    }
    return node.intValue();
  }

  @Override
  public <T> List<T> readList(Class<T> valueType) {
    return readList(null, valueType);
  }

  @Override
  public <T> List<T> readList(String name, Class<T> valueType) {
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
      throw new RpcException(RpcException.Type.CANNOT_DESERIALIZE_VALUE,
          node + " as List<" + valueType.getSimpleName() + "> - not an array");
    }

    try {
      List<T> list = new ArrayList<>(node.size());
      for (JsonNode e : node) {
        list.add(MAPPER.readValue(e.traverse(), valueType));
      }
      return list;
    } catch (IOException ex) {
      throw new RpcException(RpcException.Type.CANNOT_DESERIALIZE_VALUE,
          node + " as List<" + valueType.getSimpleName() + ">", ex);
    }
  }

  @Override
  public List<PerunBean> readListPerunBeans(String name) {
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
      throw new RpcException(RpcException.Type.CANNOT_DESERIALIZE_VALUE, node + " as List<PerunBean> - not an array");
    }

    try {
      List<PerunBean> list = new ArrayList<>(node.size());
      for (JsonNode e : node) {
        String beanName = e.get("beanName").textValue();

        if (beanName == null) {
          throw new RpcException(RpcException.Type.CANNOT_DESERIALIZE_VALUE,
              node + " as List<PerunBean> - missing beanName info");
        }
        list.add(
            (PerunBean) MAPPER.readValue(e.traverse(), Class.forName("cz.metacentrum.perun.core.api." + beanName)));
      }
      return list;
    } catch (ClassNotFoundException ex) {
      throw new RpcException(RpcException.Type.CANNOT_DESERIALIZE_VALUE,
          node + " as List<PerunBean> - class not found");
    } catch (IOException ex) {
      throw new RpcException(RpcException.Type.CANNOT_DESERIALIZE_VALUE, node + " as List<PerunBean>", ex);
    }
  }

  @Override
  public PerunBean readPerunBean(String name) {
    JsonNode node = root.get(name);

    if (node == null) {
      throw new RpcException(RpcException.Type.MISSING_VALUE, name);
    }
    if (node.isNull()) {
      return null;
    }
    if (!node.isObject()) {
      throw new RpcException(RpcException.Type.CANNOT_DESERIALIZE_VALUE, node + " as PerunBean.");
    }

    try {
      String beanName = node.get("beanName").textValue();
      if (beanName == null) {
        throw new RpcException(RpcException.Type.CANNOT_DESERIALIZE_VALUE,
            node + " as List<PerunBean> - missing beanName info");
      }
      return (PerunBean) MAPPER.readValue(node.traverse(), Class.forName("cz.metacentrum.perun.core.api." + beanName));
    } catch (ClassNotFoundException ex) {
      throw new RpcException(RpcException.Type.CANNOT_DESERIALIZE_VALUE,
          node + " as List<PerunBean> - class not found");
    } catch (IOException ex) {
      throw new RpcException(RpcException.Type.CANNOT_DESERIALIZE_VALUE, node + " as PerunBean");
    }
  }

  @Override
  public String readString(String name) {
    JsonNode node = root.get(name);

    if (node == null) {
      throw new RpcException(RpcException.Type.MISSING_VALUE, name);
    }
    if (node.isNull()) {
      return null;
    }
    if (!node.isValueNode()) {
      throw new RpcException(RpcException.Type.CANNOT_DESERIALIZE_VALUE, node + " as String");
    }

    return node.asText();
  }

  @Override
  public UUID readUUID(String name) {
    JsonNode node = root.get(name);

    if (node == null) {
      throw new RpcException(RpcException.Type.MISSING_VALUE, name);
    }
    if (node.isNull()) {
      return null;
    }
    if (!node.isValueNode()) {
      throw new RpcException(RpcException.Type.CANNOT_DESERIALIZE_VALUE, node + " as UUID");
    }

    UUID uuid;
    try {
      uuid = UUID.fromString(node.asText());
    } catch (IllegalArgumentException e) {
      throw new RpcException(RpcException.Type.CANNOT_DESERIALIZE_VALUE, node + " as UUID");
    }

    return uuid;
  }

  @JsonIgnoreProperties({"name", "baseFriendlyName", "friendlyNameParameter", "entity", "beanName"})
  private interface AttributeMixIn {
  }

  @JsonIgnoreProperties({"name", "value", "baseFriendlyName", "friendlyNameParameter", "entity", "beanName",
      "writable"})
  private interface AttributeDefinitionMixIn {
  }

  @JsonIgnoreProperties({"commonName", "displayName", "beanName", "specificUser", "majorSpecificType", "uuid"})
  private interface UserMixIn {
  }

  @JsonIgnoreProperties({"beanName", "uuid"})
  private interface PerunBeanMixIn {
  }

  @JsonIgnoreProperties({"userExtSources", "commonName", "displayName", "specificUser", "majorSpecificType", "beanName",
      "uuid"})
  private interface CandidateMixIn {
  }

  @JsonIgnoreProperties({"name"})
  private interface PerunExceptionMixIn {
  }

  @JsonIgnoreProperties({"hostNameFromDestination", "beanName"})
  private interface DestinationMixIn {
  }

  @JsonIgnoreProperties({"shortName", "beanName", "uuid"})
  private interface GroupMixIn {
  }

  @JsonIgnoreProperties({"beanName", "uuid"})
  private interface ResourceMixIn {
  }

  @JsonIgnoreProperties({"persistent", "beanName"})
  private interface UserExtSourceMixIn {
  }

  @SuppressWarnings("unused")
  @JsonIgnoreProperties({"groupStatuses", "groupStatus", "beanName", "suspended", "suspendedTo"})
  private interface MemberMixIn {
    @JsonIgnore
    void putGroupStatus(int groupId, MemberGroupStatus status);

    @JsonIgnore
    void putGroupStatuses(Map<Integer, MemberGroupStatus> groupStatuses);

    @JsonIgnore
    void setGroupsStatuses(Map<Integer, MemberGroupStatus> groupsStatuses);

    @JsonDeserialize
    void setMembershipType(MembershipType type);

    @JsonIgnore
    void setMembershipType(String type);

    @JsonDeserialize
    void setStatus(Status status);

    @JsonIgnore
    void setStatus(String status);

  }

}
