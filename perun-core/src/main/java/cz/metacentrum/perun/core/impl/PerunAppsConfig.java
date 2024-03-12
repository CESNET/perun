package cz.metacentrum.perun.core.impl;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import java.util.ArrayList;
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

  /**
   * Returns configuration's brand in which the provided domain is specified
   *
   * @param domain Example: https://perun-dev.cz
   * @return brand or null if domain is not present in the configuration
   */
  public static Brand getBrandContainingDomain(String domain) {
    Utils.notNull(domain, "domain");
    for (Brand brand : instance.getBrands()) {
      PerunAppsConfig.NewApps newApps = brand.getNewApps();
      if (domain.equals(brand.getOldGuiDomain()) || domain.equals(newApps.getAdmin()) ||
          domain.equals(newApps.getProfile()) || domain.equals(newApps.getPublications()) ||
          domain.equals(newApps.getPwdReset()) || domain.equals(newApps.getApi()) ||
          domain.equals(newApps.getConsolidator()) || domain.equals(newApps.getLinker())) {
        return brand;
      }
    }
    return null;
  }

  /**
   * Iterates brands and searches for such that contains vo's shortname. If none found, returns default branding.
   */
  public static Brand getBrandContainingVo(String voShortname) {
    Brand defaultBrand =
        instance.getBrands().stream().filter(brand -> brand.getName().equals("default")).findFirst().orElse(null);
    return instance.getBrands().stream().filter(brand -> brand.getVoShortnames().contains(voShortname)).findFirst()
        .orElse(defaultBrand);
  }

  @JsonGetter("brands")
  public List<Brand> getBrands() {
    return brands;
  }

  @JsonSetter("brands")
  public void setBrands(List<Brand> brands) {
    this.brands = brands;
  }

  @Override
  public String toString() {
    return "PerunAppsConfig{" + "brands=" + brands + '}';
  }

  /**
   * Class holding data for a single branding.
   */
  public static class Brand {

    private String name;
    private String oldGuiDomain;
    private NewApps newApps;
    private List<String> voShortnames = new ArrayList<>();
    private String oldGuiAlert;

    @JsonGetter("name")
    public String getName() {
      return name;
    }

    @JsonGetter("newApps")
    public NewApps getNewApps() {
      return newApps;
    }

    @JsonGetter("oldGuiAlert")
    public String getOldGuiAlert() {
      return oldGuiAlert;
    }

    @JsonGetter("oldGuiDomain")
    public String getOldGuiDomain() {
      return oldGuiDomain;
    }

    @JsonGetter("voShortnames")
    public List<String> getVoShortnames() {
      return voShortnames;
    }

    @JsonSetter("name")
    public void setName(String name) {
      this.name = name;
    }

    @JsonSetter("new_apps")
    public void setNewApps(NewApps newApps) {
      this.newApps = newApps;
    }

    @JsonSetter("old_gui_alert")
    public void setOldGuiAlert(String oldGuiAlert) {
      this.oldGuiAlert = oldGuiAlert;
    }

    @JsonSetter("old_gui_domain")
    public void setOldGuiDomain(String oldGuiDomain) {
      this.oldGuiDomain = oldGuiDomain;
    }

    @JsonSetter("vos")
    public void setVoShortnames(List<String> voShortnames) {
      this.voShortnames = voShortnames;
    }

    @Override
    public String toString() {
      return "Brand{" + "name='" + name + '\'' + ", oldGuiDomain='" + oldGuiDomain + '\'' + ", oldGuiAlert='" +
             oldGuiAlert + '\'' + ", newApps=" + newApps + ", vos=" + voShortnames + '}';
    }
  }

  /**
   * Class holding domains of new gui applications.
   */
  public static class NewApps {

    private String api;

    private String admin;

    private String consolidator;

    private String linker;

    private String profile;

    private String pwdReset;

    private String publications;

    @JsonGetter("admin")
    public String getAdmin() {
      return admin;
    }

    @JsonGetter("api")
    public String getApi() {
      return api;
    }

    @JsonGetter("consolidator")
    public String getConsolidator() {
      return consolidator;
    }

    @JsonGetter("linker")
    public String getLinker() {
      return linker;
    }

    @JsonGetter("profile")
    public String getProfile() {
      return profile;
    }

    @JsonGetter("publications")
    public String getPublications() {
      return publications;
    }

    @JsonGetter("pwdReset")
    public String getPwdReset() {
      return pwdReset;
    }

    @JsonSetter("admin")
    public void setAdmin(String admin) {
      this.admin = admin;
    }

    @JsonSetter("api")
    public void setApi(String api) {
      this.api = api;
    }

    @JsonSetter("consolidator")
    public void setConsolidator(String consolidator) {
      this.consolidator = consolidator;
    }

    @JsonSetter("linker")
    public void setLinker(String linker) {
      this.linker = linker;
    }

    @JsonSetter("profile")
    public void setProfile(String profile) {
      this.profile = profile;
    }

    @JsonSetter("publications")
    public void setPublications(String publications) {
      this.publications = publications;
    }

    @JsonSetter("pwd_reset")
    public void setPwdReset(String pwdReset) {
      this.pwdReset = pwdReset;
    }

    @Override
    public String toString() {
      return "NewApps{" + "api='" + api + '\'' + ", admin='" + admin + '\'' + ", consolidator='" + consolidator + '\'' +
             ", linker='" + linker + '\'' + ", profile='" + profile + '\'' + ", pwdReset='" + pwdReset + '\'' +
             ", publications='" + publications + '\'' + '}';
    }
  }
}
