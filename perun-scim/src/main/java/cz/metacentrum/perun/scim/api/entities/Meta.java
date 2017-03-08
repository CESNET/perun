package cz.metacentrum.perun.scim.api.entities;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Date;
import lombok.Data;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * Metadata of the resource.
 * 
 * @author Sona Mastrakova <sona.mastrakova@gmail.com>
 * @date 08.10.2016
 */
@Data
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class Meta {

    @JsonProperty
    private String resourceType;

    @JsonProperty
    private Date created;

    @JsonProperty
    private Date lastModified;

    @JsonProperty
    private String location;

    @JsonProperty
    private String version;
}
