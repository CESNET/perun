package cz.metacentrum.perun.core.impl.modules.pwdmgr;

import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.CoreConfig;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.rt.EmptyPasswordRuntimeException;
import cz.metacentrum.perun.core.api.exceptions.rt.LoginNotExistsRuntimeException;
import cz.metacentrum.perun.core.api.exceptions.rt.PasswordChangeFailedRuntimeException;
import cz.metacentrum.perun.core.api.exceptions.rt.PasswordCreationFailedRuntimeException;
import cz.metacentrum.perun.core.api.exceptions.rt.PasswordDeletionFailedRuntimeException;
import cz.metacentrum.perun.core.api.exceptions.rt.PasswordDoesntMatchRuntimeException;
import cz.metacentrum.perun.core.api.exceptions.rt.PasswordOperationTimeoutRuntimeException;
import cz.metacentrum.perun.core.api.exceptions.rt.PasswordStrengthFailedRuntimeException;
import cz.metacentrum.perun.core.implApi.modules.pwdmgr.PasswordManagerModule;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Map;

/**
 * Generic implementation of {@link PasswordManagerModule}. It runs generic password manger script
 * defined as perun config in {@link CoreConfig#getPasswordManagerProgram()}
 *
 * @author Pavel Zlamal <zlamal@cesnet.cz>
 */
public class GenericPasswordManagerModule implements PasswordManagerModule {

	protected static final String PASSWORD_VALIDATE = "validate";
	protected static final String PASSWORD_CREATE = "create";
	protected static final String PASSWORD_RESERVE = "reserve";
	protected static final String PASSWORD_RESERVE_RANDOM = "reserve_random";
	protected static final String PASSWORD_CHANGE = "change";
	protected static final String PASSWORD_CHECK = "check";
	protected static final String PASSWORD_DELETE = "delete";

	private String actualLoginNamespace = "generic";

	public String getActualLoginNamespace() {
		return actualLoginNamespace;
	}

	public void setActualLoginNamespace(String actualLoginNamespace) {
		this.actualLoginNamespace = actualLoginNamespace;
	}

	@Override
	public Map<String, String> generateAccount(PerunSession session, Map<String, String> parameters) throws InternalErrorException {
		// account generation is not supported
		return null;
	}

	@Override
	public void reservePassword(PerunSession session, String userLogin, String password) throws InternalErrorException {
		if (StringUtils.isBlank(password)) {
			throw new EmptyPasswordRuntimeException("Password for " + actualLoginNamespace + ":" + userLogin + " cannot be empty.");
		}
		Process process = createProcess(PASSWORD_RESERVE, actualLoginNamespace, userLogin);
		sendPassword(process, password);
		handleExit(process, actualLoginNamespace, userLogin);
	}

	@Override
	public void reserveRandomPassword(PerunSession session, String userLogin) throws InternalErrorException {
		Process process = createProcess(PASSWORD_RESERVE_RANDOM, actualLoginNamespace, userLogin);
		handleExit(process, actualLoginNamespace, userLogin);
	}

	@Override
	public void checkPassword(PerunSession sess, String userLogin, String password) {
		if (StringUtils.isBlank(password)) {
			throw new EmptyPasswordRuntimeException("Password for " + actualLoginNamespace + ":" + userLogin + " cannot be empty.");
		}
		Process process = createProcess(PASSWORD_CHECK, actualLoginNamespace, userLogin);
		sendPassword(process, password);
		handleExit(process, actualLoginNamespace, userLogin);
	}

	@Override
	public void changePassword(PerunSession sess, String userLogin, String newPassword) throws InternalErrorException {
		if (StringUtils.isBlank(newPassword)) {
			throw new EmptyPasswordRuntimeException("Password for " + actualLoginNamespace + ":" + userLogin + " cannot be empty.");
		}
		Process process = createProcess(PASSWORD_CHANGE, actualLoginNamespace, userLogin);
		sendPassword(process, newPassword);
		handleExit(process, actualLoginNamespace, userLogin);
	}

	@Override
	public void validatePassword(PerunSession sess, String userLogin) {
		Process process = createProcess(PASSWORD_VALIDATE, actualLoginNamespace, userLogin);
		handleExit(process, actualLoginNamespace, userLogin);
	}

	@Override
	public void deletePassword(PerunSession sess, String userLogin) throws InternalErrorException {
		Process process = createProcess(PASSWORD_DELETE, actualLoginNamespace, userLogin);
		handleExit(process, actualLoginNamespace, userLogin);
	}

	private Process createProcess(String operation, String loginNamespace, String login) {

		// Check validity of original password
		ProcessBuilder pb = new ProcessBuilder(BeansUtils.getCoreConfig().getPasswordManagerProgram(), operation, loginNamespace, login);

		Process process;
		try {
			process = pb.start();
		} catch (IOException e) {
			throw new InternalErrorException(e);
		}

		return process;

	}

	private void sendPassword(Process process, String password) {

		OutputStream os = process.getOutputStream();
		// Write password to the stdin of the program
		PrintWriter pw = new PrintWriter(os, true);
		pw.write(password);
		pw.close();

	}

	private void handleExit(Process process, String loginNamespace, String userLogin) {

		InputStream es = process.getErrorStream();

		// If non-zero exit code is returned, then try to read error output
		try {
			if (process.waitFor() != 0) {
				if (process.exitValue() == 1) {
					throw new PasswordDoesntMatchRuntimeException("Old password doesn't match for " + loginNamespace + ":" + userLogin + ".");
				} else if (process.exitValue() == 3) {
					throw new PasswordChangeFailedRuntimeException("Password change failed for " + loginNamespace + ":" + userLogin + ".");
				} else if (process.exitValue() == 4) {
					throw new PasswordCreationFailedRuntimeException("Password creation failed for " + loginNamespace + ":" + userLogin + ".");
				} else if (process.exitValue() == 5) {
					throw new PasswordDeletionFailedRuntimeException("Password deletion failed for " + loginNamespace + ":" + userLogin + ".");
				} else if (process.exitValue() == 6) {
					throw new LoginNotExistsRuntimeException("User login doesn't exists in underlying system for " + loginNamespace + ":" + userLogin + ".");
				} else if (process.exitValue() == 11) {
					throw new PasswordStrengthFailedRuntimeException("Password to set doesn't match expected restrictions for " + loginNamespace + ":" + userLogin + ".");
				} else if (process.exitValue() == 12) {
					throw new PasswordOperationTimeoutRuntimeException("Operation with password exceeded expected limit for " + loginNamespace + ":" + userLogin + ".");
				} else {
					// Some other error occurred
					BufferedReader inReader = new BufferedReader(new InputStreamReader(es));
					StringBuilder errorMsg = new StringBuilder();
					String line;
					try {
						while ((line = inReader.readLine()) != null) {
							errorMsg.append(line);
						}
					} catch (IOException e) {
						throw new InternalErrorException(e);
					}

					throw new InternalErrorException(errorMsg.toString());
				}
			}
		} catch (InterruptedException e) {
			throw new InternalErrorException(e);
		}

	}

}
