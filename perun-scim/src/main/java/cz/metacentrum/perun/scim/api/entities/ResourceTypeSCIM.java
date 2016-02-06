package cz.metacentrum.perun.scim.api.entities;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;
import lombok.Data;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * SCIM resource type.
 *
 * @author Sona Mastrakova <sona.mastrakova@gmail.com>
 * @date 08.10.2016
 */
@Data
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class ResourceTypeSCIM {

    @JsonProperty
    private List<String> schemas;

    @JsonProperty
    private String id;

    @JsonProperty
    private String name;

    @JsonProperty
    private String description;

    @JsonProperty
    private String endpoint;

    @JsonProperty
    private String schema;

    @JsonProperty
    private Meta meta;
}
