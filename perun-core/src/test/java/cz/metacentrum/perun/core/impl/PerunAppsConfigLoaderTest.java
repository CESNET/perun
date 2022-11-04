package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class PerunAppsConfigLoaderTest extends AbstractPerunIntegrationTest {

	private PerunAppsConfig.Brand expectedDefaultBrand;

	@Before
	public void setUp() {
		expectedDefaultBrand = new PerunAppsConfig.Brand();
		expectedDefaultBrand.setOldGuiDomain("perun-dev");
		expectedDefaultBrand.setName("default");
		expectedDefaultBrand.setOldGuiAlert("New GUI is <b><a href=\"http://www.google.com\">here</a></b>");

		var newApps = new PerunAppsConfig.NewApps();
		newApps.setApi("api");
		newApps.setAdmin("gui");
		newApps.setConsolidator("consolidator");
		newApps.setLinker("linker");
		newApps.setProfile("profile");
		newApps.setPwdReset("pwd-reset");
		newApps.setPublications("publications");

		expectedDefaultBrand.setNewApps(newApps);
	}

	@Test
	public void init() {
		PerunAppsConfig config = PerunAppsConfig.getInstance();

		assertThat(config)
				.isNotNull();
		assertThat(config.getBrands())
				.hasSize(2);
		assertThat(config.getBrands().get(0))
				.usingRecursiveComparison()
				.isEqualTo(expectedDefaultBrand);
		assertThat(config.getBrands().get(1).getOldGuiAlert())
			.isNull();
	}

	@Test
	public void vos() {
		PerunAppsConfig config = PerunAppsConfig.getInstance();

		assertThat(config)
			.isNotNull();
		assertThat(config.getBrands())
			.hasSize(2);
		assertThat(config.getBrands().get(1).getVoShortnames())
			.containsAll(List.of("test_vo", "other_vo"));
		assertThat(config.getBrands().get(0).getVoShortnames()).isNullOrEmpty();
	}
}
