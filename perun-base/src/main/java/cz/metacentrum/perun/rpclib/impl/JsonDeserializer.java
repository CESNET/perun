package cz.metacentrum.perun.rpclib.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
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
import cz.metacentrum.perun.rpclib.api.Deserializer;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Deserializer that reads values from JSON content.
 *
 * @author Jan Klos <ddd@mail.muni.cz>
 * @since 0.1
 */
@SuppressWarnings("WeakerAccess")
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

  /**
   * @param in {@code InputStream} to read JSON data from
   * @throws IOException  if an IO error occurs
   * @throws RpcException if content of {@code in} is wrongly formatted
   */
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

  @Override
  public <T> T read(Class<T> valueType) {
    return read(null, valueType);
  }

  @Override
  public int[] readArrayOfInts(String name) {
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
  public int[] readArrayOfInts() {
    return readArrayOfInts(null);
  }

  @Override
  public int readInt(String name) {
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
  public int readInt() {
    return readInt(null);
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
  public <T> List<T> readList(Class<T> valueType) {
    return readList(null, valueType);
  }

  @Override
  public String readString(String name) {
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
      throw new RpcException(RpcException.Type.CANNOT_DESERIALIZE_VALUE, node + " as String");
    }

    return node.asText();
  }

  @Override
  public String readString() {
    return readString(null);
  }

  @JsonIgnoreProperties({"name", "baseFriendlyName", "friendlyNameParameter", "entity", "beanName"})
  public interface AttributeMixIn {
  }

  @JsonIgnoreProperties({"name", "value", "baseFriendlyName", "friendlyNameParameter", "entity", "beanName",
      "writable"})
  public interface AttributeDefinitionMixIn {
  }

  @JsonIgnoreProperties({"commonName", "displayName", "beanName", "specificUser", "majorSpecificType"})
  public interface UserMixIn {
  }

  @JsonIgnoreProperties({"beanName"})
  public interface PerunBeanMixIn {
  }


  @JsonIgnoreProperties({"userExtSources"})
  public interface CandidateMixIn {
  }

  @JsonIgnoreProperties({"name"})
  public interface PerunExceptionMixIn {
  }

  @JsonIgnoreProperties({"hostNameFromDestination", "beanName"})
  public interface DestinationMixIn {
  }

  @JsonIgnoreProperties({"shortName", "beanName"})
  public interface GroupMixIn {
  }

  @JsonIgnoreProperties({"persistent", "beanName"})
  public interface UserExtSourceMixIn {
  }

  @SuppressWarnings("unused")
  @JsonIgnoreProperties({"groupStatuses", "groupStatus", "beanName", "suspended"})
  public interface MemberMixIn {
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
