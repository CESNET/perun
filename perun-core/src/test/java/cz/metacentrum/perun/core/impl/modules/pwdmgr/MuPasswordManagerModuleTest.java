package cz.metacentrum.perun.core.impl.modules.pwdmgr;

import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.exceptions.PasswordStrengthException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.implApi.modules.pwdmgr.ISResponseData;
import cz.metacentrum.perun.core.implApi.modules.pwdmgr.ISServiceCaller;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.regex.Pattern;

import static cz.metacentrum.perun.core.implApi.modules.pwdmgr.ISServiceCaller.IS_ERROR_STATUS;
import static cz.metacentrum.perun.core.implApi.modules.pwdmgr.ISServiceCaller.IS_OK_STATUS;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class MuPasswordManagerModuleTest extends AbstractPerunIntegrationTest {

	private MuPasswordManagerModule module;
	private final ISServiceCaller isServiceCallerMock = mock(ISServiceCaller.class);

	private final int randomPasswordLength = 12;
	private final Pattern MUPasswordContainsNotAllowedChars = Pattern.compile(".*[^ABCDEFGHJKLMNPQRSTUVWXabcdefghjkmnpqrstuvwx23456789,._-].*");

	@Before
	public void setUp() throws Exception {
		this.module = (MuPasswordManagerModule)((PerunBl)sess.getPerun()).getUsersManagerBl().getPasswordManagerModule(sess, "mu");
		this.module = spy(this.module);

		doReturn("999809098")
				.when(this.module)
				.getPasswordTestUco();

		this.module.setIsServiceCaller(isServiceCallerMock);
	}

	@After
	public void tearDown() {
		Mockito.reset(isServiceCallerMock);
	}

	@Test
	public void generatedPasswordContainsOnlyAllowedChars() throws Exception {
		ISResponseData okResponseData = new ISResponseData();
		okResponseData.setStatus(IS_OK_STATUS);

		when(isServiceCallerMock.call(anyString(), anyInt()))
			.thenReturn(okResponseData);

		// test that password does not contain any invalid character
		Assert.assertFalse(MUPasswordContainsNotAllowedChars.matcher(module.generateRandomPassword(sess, null))
			.matches());
	}

	@Test
	public void generatedPasswordHasValidLength() throws Exception {
		ISResponseData okResponseData = new ISResponseData();
		okResponseData.setStatus(IS_OK_STATUS);

		when(isServiceCallerMock.call(anyString(), anyInt()))
			.thenReturn(okResponseData);

		Assert.assertEquals(module.generateRandomPassword(sess, null).length(), randomPasswordLength);
	}

	@Test
	public void changePassword() throws Exception {
		ISResponseData okResponseData = new ISResponseData();
		okResponseData.setStatus(IS_OK_STATUS);

		when(isServiceCallerMock.call(anyString(), anyInt()))
				.thenReturn(okResponseData);

		module.checkPasswordStrength(sess, null, "randomPassword2.");
	}

	@Test
	public void changePasswordExceptionIsThrownIfIsReturnsAnError() throws Exception {
		String errorMessage = "Invalid password";

		ISResponseData errResponseData = new ISResponseData();
		errResponseData.setStatus(IS_ERROR_STATUS);
		errResponseData.setError(errorMessage);

		when(isServiceCallerMock.call(anyString(), anyInt()))
				.thenReturn(errResponseData);

		assertThatExceptionOfType(PasswordStrengthException.class)
				.isThrownBy(() -> module.checkPasswordStrength(sess, null, "Adf.,.2;..df,"))
				.withMessageEndingWith(errorMessage);
	}
}
