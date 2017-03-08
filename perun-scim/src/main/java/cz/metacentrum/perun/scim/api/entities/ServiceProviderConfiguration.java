package cz.metacentrum.perun.scim.api.entities;

import java.util.List;
import lombok.Data;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * SCIM Service Provider Configuration
 *
 * @author Sona Mastrakova <sona.mastrakova@gmail.com>
 * @date 08.10.2016
 */
@Data
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ServiceProviderConfiguration {

    private List<String> schemas;
    private String documentationUrl;
    private Patch patch;
    private Bulk bulk;
    private Filter filter;
    private ChangePassword changePassword;
    private Sort sort;
    private Etag etag;
    private XmlDataFormat xmlDataFormat;
    private List<AuthenticationSchemes> authenticationSchemes;

    public void setBulkSupport(Boolean value) {
        Bulk newBulk = new Bulk();
        newBulk.supported = value;
        this.bulk = newBulk;
    }

    public void setFilterSupport(Boolean value) {
        Filter newFilter = new Filter();
        newFilter.supported = value;
        this.filter = newFilter;
    }

    public void setPatchSupport(Boolean value) {
        Patch newPatch = new Patch();
        newPatch.supported = value;
        this.patch = newPatch;
    }

    public void setChangePasswordSupport(Boolean value) {
        ChangePassword newChangePassword = new ChangePassword();
        newChangePassword.supported = value;
        this.changePassword = newChangePassword;
    }

    public void setSortSupport(Boolean value) {
        Sort newSort = new Sort();
        newSort.supported = value;
        this.sort = newSort;
    }

    public void setEtagSupport(Boolean value) {
        Etag newEtag = new Etag();
        newEtag.supported = value;
        this.etag = newEtag;
    }

    public void setXmlDataFormatSupport(Boolean value) {
        XmlDataFormat newXmlDataFormat = new XmlDataFormat();
        newXmlDataFormat.supported = value;
        this.xmlDataFormat = newXmlDataFormat;
    }

    private static class Bulk {
        @JsonProperty
        private Boolean supported;
    }

    private static class Filter {
        @JsonProperty
        private Boolean supported;
    }

    private static class Patch {

        @JsonProperty
        private Boolean supported;
    }

    private static class ChangePassword {

        @JsonProperty
        private Boolean supported;
    }

    private static class Sort {

        @JsonProperty
        private Boolean supported;
    }

    private static class Etag {

        @JsonProperty
        private Boolean supported;
    }

    private static class XmlDataFormat {

        @JsonProperty
        private Boolean supported;
    }
}
