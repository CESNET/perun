package cz.metacentrum.perun.rpc.stdserializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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
  public void serialize(LocalDate value, JsonGenerator generator, SerializerProvider provider) throws IOException {
    generator.writeString(value.format(DateTimeFormatter.ISO_LOCAL_DATE));
  }
}
