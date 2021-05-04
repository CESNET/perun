package cz.metacentrum.perun.core.impl;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Class holding configuration of perun apps brandings and apps' domains.
 *
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class PerunAppsConfig {

	private static PerunAppsConfig instance;

	@JsonProperty("brands")
	private List<Brand> brands;

	public static PerunAppsConfig getInstance() {
		return instance;
	}

	public static void setInstance(PerunAppsConfig instance) {
		PerunAppsConfig.instance = instance;
	}

	public List<Brand> getBrands() {
		return brands;
	}

	public void setBrands(List<Brand> brands) {
		this.brands = brands;
	}

	/**
	 * Class holding data for a single branding.
	 */
	public static class Brand {

		@JsonProperty("name")
		private String name;

		@JsonProperty("old_gui_domain")
		private String oldGuiDomain;

		@JsonProperty("new_apps")
		private NewApps newApps;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public NewApps getNewApps() {
			return newApps;
		}

		public void setNewApps(NewApps newApps) {
			this.newApps = newApps;
		}

		public String getOldGuiDomain() {
			return oldGuiDomain;
		}

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
		@JsonProperty("admin")
		private String admin;

		@JsonProperty("profile")
		private String profile;

		@JsonProperty("pwd_reset")
		private String pwdReset;

		@JsonProperty("publications")
		private String publications;

		public String getAdmin() {
			return admin;
		}

		public void setAdmin(String admin) {
			this.admin = admin;
		}

		public String getProfile() {
			return profile;
		}

		public void setProfile(String profile) {
			this.profile = profile;
		}

		public String getPwdReset() {
			return pwdReset;
		}

		public void setPwdReset(String pwdReset) {
			this.pwdReset = pwdReset;
		}

		public String getPublications() {
			return publications;
		}

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
