package cz.metacentrum.perun.core.implApi.modules.pwdmgr;

import java.io.IOException;

/**
 * Service used to communicate with the IS MU.
 *
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public interface ISServiceCaller {
  String IS_ERROR_STATUS = "ERR";
  String IS_OK_STATUS = "OK";

  /**
   * Sends a request to the IS with the given body. The received response is returned, with the status code and an error
   * (if some has occurred).
   *
   * @param requestBody body of the http request, that will be send (xml format)
   * @param requestId   id of the request
   * @return response data
   * @throws IOException if the connection to the IS failed
   */
  ISResponseData call(String requestBody, int requestId) throws IOException;
}
