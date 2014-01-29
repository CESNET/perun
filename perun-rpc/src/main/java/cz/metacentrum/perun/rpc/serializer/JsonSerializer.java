package cz.metacentrum.perun.rpc.serializer;

import java.io.IOException;
import java.io.OutputStream;

import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.ObjectMapper;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Candidate;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.exceptions.rt.PerunRuntimeException;
import cz.metacentrum.perun.rpc.RpcException;

/**
 * JSON serializer.
 *
 * @author Jan Klos <ddd@mail.muni.cz>
 * @since 0.1
 */
public final class JsonSerializer implements Serializer {

    @JsonIgnoreProperties({"name"})
    private interface AttributeMixIn {
    }

    @JsonIgnoreProperties({"name"})
    private interface AttributeDefinitionMixIn {}

    @JsonIgnoreProperties({"commonName", "displayName"})
    private interface UserMixIn {}
    
    @JsonIgnoreProperties({"cause"})
    private interface PerunExceptionMixIn {}
    
    public static final String CONTENT_TYPE = "application/json; charset=utf-8";
    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.getSerializationConfig().addMixInAnnotations(Attribute.class, AttributeMixIn.class);
        mapper.getSerializationConfig().addMixInAnnotations(AttributeDefinition.class, AttributeDefinitionMixIn.class);
        mapper.getSerializationConfig().addMixInAnnotations(User.class, UserMixIn.class);
        mapper.getSerializationConfig().addMixInAnnotations(PerunException.class, PerunExceptionMixIn.class);
        mapper.getSerializationConfig().addMixInAnnotations(PerunRuntimeException.class, PerunExceptionMixIn.class);
    }
    private static final JsonFactory jsonFactory = new JsonFactory();

    static {
//FIXME odstraneno disable(JsonGenerator.Feature.FLUSH_PASSED_TO_STREAM)
      jsonFactory.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET).disable(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT).setCodec(mapper);
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
    public String getContentType() {
        return CONTENT_TYPE;
    }

    @Override
    public void write(Object object) throws RpcException, IOException {
        JsonGenerator gen = jsonFactory.createJsonGenerator(out, JsonEncoding.UTF8);

        try {
            gen.writeObject(object);
            gen.close();
        } catch (JsonProcessingException ex) {
            throw new RpcException(RpcException.Type.CANNOT_SERIALIZE_VALUE, ex);
        }
    }

    @Override
    public void writePerunException(PerunException pex) throws IOException {
        if (pex == null) throw new IllegalArgumentException("pex is null");
        try { 
          this.write(pex);
        } catch(RpcException ex) {
          //TODO log
          JsonGenerator gen = jsonFactory.createJsonGenerator(out, JsonEncoding.UTF8);
          //FIXME transform exception to correct format.
          gen.writeString(ex.toString());
        }
    }

    @Override
    public void writePerunRuntimeException(PerunRuntimeException prex) throws IOException {
        if (prex == null)  throw new IllegalArgumentException("prex is null");
        try { 
          this.write(prex);
        } catch(RpcException ex) {
          //TODO log
          JsonGenerator gen = jsonFactory.createJsonGenerator(out, JsonEncoding.UTF8);
          //FIXME transform exception to correct format.
          gen.writeString(ex.toString());
        }
    }
}
