package cz.metacentrum.perun.scim.api.entities;

import java.util.List;
import lombok.Data;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * ListResponse type containing list of some resources, number of resources
 * and type of resource schema.
 * 
 * @author Sona Mastrakova <sona.mastrakova@gmail.com>
 * @date 08.10.2016
 */
@Data
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
@JsonAutoDetect(getterVisibility = Visibility.NONE)
public class ListResponseSCIM {

    @JsonProperty
    private Long totalResults;
    
    @JsonProperty
    private String schemas;
    
    @JsonProperty(value="Resources")
    private List resources;
}
