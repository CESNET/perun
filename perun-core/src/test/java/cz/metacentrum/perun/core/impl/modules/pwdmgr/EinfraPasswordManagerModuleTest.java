package cz.metacentrum.perun.core.impl.modules.pwdmgr;

import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.bl.PerunBl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class EinfraPasswordManagerModuleTest extends AbstractPerunIntegrationTest {

	private EinfraPasswordManagerModule module;

	private final int randomPasswordLength = 12;
	private final Pattern EinfraPasswordContainsNotAllowedChars = Pattern.compile(".*[^ABCDEFGHJKLMNPQRSTUVWXabcdefghjkmnpqrstuvwx23456789,._-].*");

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

	@Test
	public void testGeneratedPasswordContainsOnlyAllowedChars() {

		// test that password does not contain any invalid character
		Assert.assertFalse(EinfraPasswordContainsNotAllowedChars.matcher(module.generateRandomPassword(sess, null))
			.matches());
	}

	@Test
	public void generatedPasswordHasValidLength() {
		Assert.assertEquals(module.generateRandomPassword(sess, null).length(), randomPasswordLength);
	}
}
