package cz.metacentrum.perun.scim.api.entities;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;

import lombok.Data;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * Group resource type for SCIM protocol.
 *
 * @author Sona Mastrakova <sona.mastrakova@gmail.com>
 * @date 08.10.2016
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class GroupSCIM extends Resource {

    @JsonProperty
    private List<String> schemas;

    @JsonProperty
    private String displayName;

    @JsonProperty
    private List<MemberSCIM> members;
}
