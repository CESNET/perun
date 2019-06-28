package cz.metacentrum.perun.scim.api.entities;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * SCIM Service Provider Configuration
 *
 * @author Sona Mastrakova <sona.mastrakova@gmail.com>
 * @date 08.10.2016
 */

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

	public ServiceProviderConfiguration(List<String> schemas, String documentationUrl, Patch patch, Bulk bulk,
	                                    Filter filter, ChangePassword changePassword, Sort sort, Etag etag,
	                                    XmlDataFormat xmlDataFormat, List<AuthenticationSchemes> authenticationSchemes) {
		this.schemas = schemas;
		this.documentationUrl = documentationUrl;
		this.patch = patch;
		this.bulk = bulk;
		this.filter = filter;
		this.changePassword = changePassword;
		this.sort = sort;
		this.etag = etag;
		this.xmlDataFormat = xmlDataFormat;
		this.authenticationSchemes = authenticationSchemes;
	}

	public ServiceProviderConfiguration() {
	}

	public List<String> getSchemas() {
		return schemas;
	}

	public void setSchemas(List<String> schemas) {
		this.schemas = schemas;
	}

	public String getDocumentationUrl() {
		return documentationUrl;
	}

	public void setDocumentationUrl(String documentationUrl) {
		this.documentationUrl = documentationUrl;
	}

	public Patch getPatch() {
		return patch;
	}

	public void setPatch(Patch patch) {
		this.patch = patch;
	}

	public Bulk getBulk() {
		return bulk;
	}

	public void setBulk(Bulk bulk) {
		this.bulk = bulk;
	}

	public Filter getFilter() {
		return filter;
	}

	public void setFilter(Filter filter) {
		this.filter = filter;
	}

	public ChangePassword getChangePassword() {
		return changePassword;
	}

	public void setChangePassword(ChangePassword changePassword) {
		this.changePassword = changePassword;
	}

	public Sort getSort() {
		return sort;
	}

	public void setSort(Sort sort) {
		this.sort = sort;
	}

	public Etag getEtag() {
		return etag;
	}

	public void setEtag(Etag etag) {
		this.etag = etag;
	}

	public XmlDataFormat getXmlDataFormat() {
		return xmlDataFormat;
	}

	public void setXmlDataFormat(XmlDataFormat xmlDataFormat) {
		this.xmlDataFormat = xmlDataFormat;
	}

	public List<AuthenticationSchemes> getAuthenticationSchemes() {
		return authenticationSchemes;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ServiceProviderConfiguration)) return false;

		ServiceProviderConfiguration that = (ServiceProviderConfiguration) o;

		if (getSchemas() != null ? !getSchemas().equals(that.getSchemas()) : that.getSchemas() != null) return false;
		if (getDocumentationUrl() != null ? !getDocumentationUrl().equals(that.getDocumentationUrl()) : that.getDocumentationUrl() != null)
			return false;
		if (getPatch() != null ? !getPatch().equals(that.getPatch()) : that.getPatch() != null) return false;
		if (getBulk() != null ? !getBulk().equals(that.getBulk()) : that.getBulk() != null) return false;
		if (getFilter() != null ? !getFilter().equals(that.getFilter()) : that.getFilter() != null) return false;
		if (getChangePassword() != null ? !getChangePassword().equals(that.getChangePassword()) : that.getChangePassword() != null)
			return false;
		if (getSort() != null ? !getSort().equals(that.getSort()) : that.getSort() != null) return false;
		if (getEtag() != null ? !getEtag().equals(that.getEtag()) : that.getEtag() != null) return false;
		if (getXmlDataFormat() != null ? !getXmlDataFormat().equals(that.getXmlDataFormat()) : that.getXmlDataFormat() != null)
			return false;
		return getAuthenticationSchemes() != null ? getAuthenticationSchemes().equals(that.getAuthenticationSchemes()) : that.getAuthenticationSchemes() == null;

	}

	@Override
	public int hashCode() {
		int result = getSchemas() != null ? getSchemas().hashCode() : 0;
		result = 31 * result + (getDocumentationUrl() != null ? getDocumentationUrl().hashCode() : 0);
		result = 31 * result + (getPatch() != null ? getPatch().hashCode() : 0);
		result = 31 * result + (getBulk() != null ? getBulk().hashCode() : 0);
		result = 31 * result + (getFilter() != null ? getFilter().hashCode() : 0);
		result = 31 * result + (getChangePassword() != null ? getChangePassword().hashCode() : 0);
		result = 31 * result + (getSort() != null ? getSort().hashCode() : 0);
		result = 31 * result + (getEtag() != null ? getEtag().hashCode() : 0);
		result = 31 * result + (getXmlDataFormat() != null ? getXmlDataFormat().hashCode() : 0);
		result = 31 * result + (getAuthenticationSchemes() != null ? getAuthenticationSchemes().hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "ServiceProviderConfiguration{" +
				"schemas=" + schemas +
				", documentationUrl='" + documentationUrl + '\'' +
				", patch=" + patch +
				", bulk=" + bulk +
				", filter=" + filter +
				", changePassword=" + changePassword +
				", sort=" + sort +
				", etag=" + etag +
				", xmlDataFormat=" + xmlDataFormat +
				", authenticationSchemes=" + authenticationSchemes +
				'}';
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

	public void setAuthenticationSchemes(List<AuthenticationSchemes> authenticationSchemes) {
		this.authenticationSchemes = authenticationSchemes;
	}

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
}
