package cz.metacentrum.perun.ldapc.beans;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.ldapc.model.AttributeValueTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;

/**
 * Value transformer, which converts attribute values from Perun into JSON formatted string.
 * Incoming multi-valued attributes are reduced to single string/value attributes.
 */
public class JsonValueTransformer extends ValueTransformerBase implements AttributeValueTransformer {

  private final static Logger log = LoggerFactory.getLogger(JsonValueTransformer.class);

  protected static ObjectMapper mapper = new ObjectMapper();

  @Override
  public String getValue(String value, Attribute attr) {
    try {
      return mapper.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      log.warn("Error converting {} to JSON", value, e);
      return null;
    }
  }

  @Override
  public String getValue(Collection<String> value, Attribute attr) {
    try {
      return mapper.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      log.warn("Error converting {} to JSON", value, e);
      return null;
    }
  }

  @Override
  public String getValue(Map<String, String> value, Attribute attr) {
    try {
      return mapper.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      log.warn("Error converting {} to JSON", value, e);
      return null;
    }
  }

  @Override
  public Boolean isMassTransformationPreferred() {
    return true;
  }

  @Override
  public Boolean isReduce() {
    return true;
  }

}
