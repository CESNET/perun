package cz.metacentrum.perun.rpclib.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.RpcException;
import cz.metacentrum.perun.rpclib.api.Serializer;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * JSON serializer.
 *
 * @author Jan Klos <ddd@mail.muni.cz>
 * @since 0.1
 */
public final class JsonSerializer implements Serializer {

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

    MAPPER.setMixIns(MIXIN_MAP);
  }

  static {
    //FIXME removed disable(JsonGenerator.Feature.FLUSH_PASSED_TO_STREAM)
    //jsonFactory.enable(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS);
    JSON_FACTORY.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET).disable(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT)
        .setCodec(MAPPER);
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
    JsonGenerator gen = JSON_FACTORY.createGenerator(out, JsonEncoding.UTF8);

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
