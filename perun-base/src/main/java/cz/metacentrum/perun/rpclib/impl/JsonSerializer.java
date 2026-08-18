package cz.metacentrum.perun.rpclib.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.RpcException;
import cz.metacentrum.perun.rpclib.api.Serializer;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import tools.jackson.core.JacksonException;
import tools.jackson.core.StreamWriteFeature;
import tools.jackson.core.json.JsonFactory;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;

/**
 * JSON serializer.
 *
 * @author Jan Klos <ddd@mail.muni.cz>
 * @since 0.1
 */
public final class JsonSerializer implements Serializer {

  private static final JsonMapper MAPPER;
  private static final Map<Class<?>, Class<?>> MIXIN_MAP = new HashMap<>();
  private static final JsonFactory JSON_FACTORY = JsonFactory.builder()
        .disable(StreamWriteFeature.AUTO_CLOSE_TARGET)
        .disable(StreamWriteFeature.AUTO_CLOSE_CONTENT)
        .build();

  static {

    MIXIN_MAP.put(Attribute.class, AttributeMixIn.class);
    MIXIN_MAP.put(AttributeDefinition.class, AttributeDefinitionMixIn.class);
    MIXIN_MAP.put(User.class, UserMixIn.class);

    MAPPER = JsonMapper.builder(JSON_FACTORY)
        .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
        .addMixIns(MIXIN_MAP)
        .build();
  }

  private OutputStream out;

  /**
   * @param out {@code OutputStream} to output serialized data
   */
  public JsonSerializer(OutputStream out) {
    this.out = out;
  }

  @Override
  public void write(Object object) {
    if (object instanceof Throwable) {
      throw new IllegalArgumentException("Tried to serialize a throwable object using write()", (Throwable) object);
    }
    try {
      MAPPER.writeValue(out, object);
    } catch (JacksonException ex) {
      throw new RpcException(RpcException.Type.CANNOT_SERIALIZE_VALUE, ex);
    }
  }

  @JsonIgnoreProperties({"name"})
  private interface AttributeMixIn {
  }

  @JsonIgnoreProperties({"name"})
  private interface AttributeDefinitionMixIn {
  }

  @JsonIgnoreProperties({"commonName", "displayName"})
  private interface UserMixIn {
  }
}
