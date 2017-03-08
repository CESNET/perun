package cz.metacentrum.perun.scim.api.entities;

import lombok.Data;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * Email for user resources.
 * 
 * @author Sona Mastrakova <sona.mastrakova@gmail.com>
 * @date 08.10.2016
 */
@Data
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class EmailSCIM {

    @JsonProperty
    private String type;

    @JsonProperty
    private String value;

    @JsonProperty
    private Boolean primary;
}
