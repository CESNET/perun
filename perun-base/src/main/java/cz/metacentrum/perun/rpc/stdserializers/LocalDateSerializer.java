package cz.metacentrum.perun.rpc.stdserializers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

/**
 * Serializer that can be used to serialize a LocalDate into format 'yyyy-MM-dd'
 *
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class LocalDateSerializer extends StdSerializer<LocalDate> {

  public LocalDateSerializer() {
    super(LocalDate.class);
  }

  @Override
  public void serialize(LocalDate value, JsonGenerator generator, SerializationContext context) {
    generator.writeString(value.format(DateTimeFormatter.ISO_LOCAL_DATE));
  }
}
