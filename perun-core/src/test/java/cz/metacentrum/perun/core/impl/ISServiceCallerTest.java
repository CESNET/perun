package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.impl.modules.pwdmgr.ISServiceCallerImpl;
import cz.metacentrum.perun.core.implApi.modules.pwdmgr.ISResponseData;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import static cz.metacentrum.perun.core.implApi.modules.pwdmgr.ISServiceCaller.IS_ERROR_STATUS;
import static cz.metacentrum.perun.core.implApi.modules.pwdmgr.ISServiceCaller.IS_OK_STATUS;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class ISServiceCallerTest {

	private static final String TEST_ERR_RESPONSE_ERROR = "Navrhované heslo nelze použít, protože začíná nebo končí mezerou. Zvolte si prosím jiné.";
	private static final String TEST_ERR_RESPONSE = """
			<?xml version="1.0" encoding="utf-8"?>
			<response><resp reqid="297965"><stav>ERR</stav>
			    <error>""" + TEST_ERR_RESPONSE_ERROR + """
			    </error>
			    <uco></uco></resp>
			</response>
			""";

	private static final String TEST_OK_RESPONSE = """
			<?xml version="1.0" encoding="utf-8"?>
			<response>
			    <resp reqid="143002">
			        <stav>OK</stav>
			        <uco>9021006</uco>
			    </resp>
			</response>
			""";

	private final ISServiceCallerImpl isServiceCaller = ISServiceCallerImpl.getInstance();

	@Test
	public void testParseOkResponse() throws UnsupportedEncodingException {
		int reqId = 1;
		ISResponseData data = isServiceCaller
				.parseResponse(new ByteArrayInputStream(TEST_OK_RESPONSE.getBytes(StandardCharsets.UTF_8)), reqId);

		assertThat(data.getStatus()).isEqualTo(IS_OK_STATUS);
		assertThat(data.getError()).isNull();
		assertThat(data.getResponse()).isNotNull();
	}

	@Test
	public void testParseErrResponse() throws UnsupportedEncodingException {
		int reqId = 1;
		ISResponseData data = isServiceCaller
				.parseResponse(new ByteArrayInputStream(TEST_ERR_RESPONSE.getBytes(StandardCharsets.UTF_8)), reqId);

		assertThat(data.getStatus()).isEqualTo(IS_ERROR_STATUS);
		assertThat(data.getError()).isEqualTo(TEST_ERR_RESPONSE_ERROR);
		assertThat(data.getResponse()).isNotNull();
	}
}
