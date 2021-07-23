package cz.metacentrum.perun.core.impl;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.util.List;

/**
 * Class holding configuration of perun apps brandings and apps' domains.
 *
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class PerunAppsConfig {

	private static PerunAppsConfig instance;

	private List<Brand> brands;

	public static PerunAppsConfig getInstance() {
		return instance;
	}

	public static void setInstance(PerunAppsConfig instance) {
		PerunAppsConfig.instance = instance;
	}

	@JsonGetter("brands")
	public List<Brand> getBrands() {
		return brands;
	}

	@JsonSetter("brands")
	public void setBrands(List<Brand> brands) {
		this.brands = brands;
	}

	/**
	 * Returns configuration's brand in which the provided domain is specified
	 * @param domain Example: https://perun-dev.cz
	 * @return brand or null if domain is not present in the configuration
	 */
	public static Brand getBrandContainingDomain(String domain) {
		for (Brand brand : instance.getBrands()) {
			PerunAppsConfig.NewApps newApps = brand.getNewApps();
			if (brand.getOldGuiDomain().equals(domain) || newApps.getAdmin().equals(domain) || newApps.getProfile().equals(domain)
				|| newApps.getPublications().equals(domain) || newApps.getPwdReset().equals(domain)) {
				return brand;
			}
		}
		return null;
	}

	/**
	 * Class holding data for a single branding.
	 */
	public static class Brand {

		private String name;

		private String oldGuiDomain;

		private NewApps newApps;

		@JsonGetter("name")
		public String getName() {
			return name;
		}

		@JsonSetter("name")
		public void setName(String name) {
			this.name = name;
		}

		@JsonGetter("newApps")
		public NewApps getNewApps() {
			return newApps;
		}

		@JsonSetter("new_apps")
		public void setNewApps(NewApps newApps) {
			this.newApps = newApps;
		}

		@JsonGetter("oldGuiDomain")
		public String getOldGuiDomain() {
			return oldGuiDomain;
		}

		@JsonSetter("old_gui_domain")
		public void setOldGuiDomain(String oldGuiDomain) {
			this.oldGuiDomain = oldGuiDomain;
		}

		@Override
		public String toString() {
			return "Brand{" +
					"name='" + name + '\'' +
					", oldGuiDomain='" + oldGuiDomain + '\'' +
					", newApps=" + newApps +
					'}';
		}
	}

	/**
	 * Class holding domains of new gui applications.
	 */
	public static class NewApps {
		private String admin;

		private String profile;

		private String pwdReset;

		private String publications;

		@JsonGetter("admin")
		public String getAdmin() {
			return admin;
		}

		@JsonSetter("admin")
		public void setAdmin(String admin) {
			this.admin = admin;
		}

		@JsonGetter("profile")
		public String getProfile() {
			return profile;
		}

		@JsonSetter("profile")
		public void setProfile(String profile) {
			this.profile = profile;
		}

		@JsonGetter("pwdReset")
		public String getPwdReset() {
			return pwdReset;
		}

		@JsonSetter("pwd_reset")
		public void setPwdReset(String pwdReset) {
			this.pwdReset = pwdReset;
		}

		@JsonGetter("publications")
		public String getPublications() {
			return publications;
		}

		@JsonSetter("publications")
		public void setPublications(String publications) {
			this.publications = publications;
		}

		@Override
		public String toString() {
			return "NewApps{" +
					"admin='" + admin + '\'' +
					", profile='" + profile + '\'' +
					'}';
		}
	}

	@Override
	public String toString() {
		return "PerunAppsConfig{" +
				"brands=" + brands +
				'}';
	}
}
