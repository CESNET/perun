package cz.metacentrum.perun.scim.api.entities;

import lombok.Data;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * Member of group resource type.
 * 
 * @author Sona Mastrakova <sona.mastrakova@gmail.com>
 * @date 08.10.2016
 */
@Data
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class MemberSCIM {

    @JsonProperty
    private String value;

    @JsonProperty("$ref")
    private String ref;

    @JsonProperty
    private String display;
}
