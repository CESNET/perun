package cz.metacentrum.perun.scim.api.entities;

import lombok.Data;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * All resources (user, group, ..) extend from this class, that contains resource
 * id, resource externalId and resource metadata.
 * 
 * @author Sona Mastrakova <sona.mastrakova@gmail.com>
 * @date 08.10.2016
 */
@Data
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class Resource {

    @JsonProperty
    private Long id;

    @JsonProperty
    private Long externalId;

    @JsonProperty
    private Meta meta;
}
