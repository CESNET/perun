package cz.metacentrum.perun.core.impl.modules.pwdmgr;

import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.bl.PerunBl;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class EinfraPasswordManagerModuleTest extends AbstractPerunIntegrationTest {

	private EinfraPasswordManagerModule module;

	@Before
	public void setUp() {
		this.module = (EinfraPasswordManagerModule) ((PerunBl) sess.getPerun()).getUsersManagerBl()
				.getPasswordManagerModule(sess, "einfra");
	}

	@Test
	public void testNotAllowedLogins() {
		List<String> notAllowedLogins = List.of(
				"open-suff",
				"dd-suff",
				"it4isuff",
				"pr0suff",
				"pr5suff"
		);

		assertThat(notAllowedLogins)
				.noneMatch(login -> module.isLoginPermitted(sess, login));
	}

	@Test
	public void testAllowedLogins() {
		List<String> allowedLogins = List.of(
				"vopen-suff",
				"it5i",
				"john"
		);

		assertThat(allowedLogins)
				.allMatch(login -> module.isLoginPermitted(sess, login));
	}
}
