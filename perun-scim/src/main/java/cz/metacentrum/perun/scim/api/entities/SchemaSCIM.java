package cz.metacentrum.perun.scim.api.entities;

import org.codehaus.jackson.annotate.JsonProperty;

import lombok.Data;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * Schema of the resource.
 * 
 * @author Sona Mastrakova <sona.mastrakova@gmail.com>
 * @date 08.10.2016
 */
@Data
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class SchemaSCIM {

    @JsonProperty
    private String id;

    @JsonProperty
    private String name;

    @JsonProperty
    private String description;
}
