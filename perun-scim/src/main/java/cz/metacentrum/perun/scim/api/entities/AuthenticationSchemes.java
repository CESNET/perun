package cz.metacentrum.perun.scim.api.entities;

import lombok.Data;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Authentication Schemes for ServiceProviderConfigs endpoint.
 *
 * @author Sona Mastrakova <sona.mastrakova@gmail.com>
 * @date 08.10.2016
 */
@Data
public class AuthenticationSchemes {

    @JsonProperty
    private String name;

    @JsonProperty
    private String description;
}
