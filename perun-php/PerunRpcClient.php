<?php
/**
 * Class for retrieving data from Perun RPC
 */
class PerunRpcClient {

	// settings
	// PERUN RPC URL
	const RPC_URL = "https://perun.cesnet.cz/krb/rpc/json/";
	// Username
	const USER = "perun-client";
	// Password based authentication
	const PASSSWORD = "";
	// Kerberos based authentication, left empty if there is no Kerberos support
	const KERBEROS_CC = "";

	// DO NOT EDIT BELOW THIS LINE
	// ***************************

	// curl request
	private $curl;

	/**
	 * Initializes the connection
	 */
	public function __construct() {
		$this->curl = curl_init();
		curl_setopt($this->curl, CURLOPT_RETURNTRANSFER, true);

		// Setup KRB keytab location
		if (self::KERBEROS_CC != "") {
			putenv("KRB5CCNAME=FILE:" . self::KERBEROS_CC);
			curl_setopt($this->curl, CURLOPT_HTTPAUTH, CURLAUTH_GSSNEGOTIATE);
		}

		// Initialize CURL
		$userpwd = self::PASSSWORD != "" ? self::USER . ":" . self::PASSSWORD : self::USER . ":";
		curl_setopt($this->curl, CURLOPT_USERPWD, $userpwd);
	}

	/**
	 * Retrieves parsed JSON data or false if request fails.
	 */
	public function retrieveData($method, $vars = NULL) {
		// REQUEST
		curl_setopt($this->curl, CURLOPT_URL, self::RPC_URL . "/" . $method);
		if ($vars != NULL) {
			curl_setopt($this->curl, CURLOPT_POST, 1);
			curl_setopt($this->curl, CURLOPT_POSTFIELDS, json_encode($vars));
			// Allow correct processing of POST request and response
			curl_setopt($this->curl, CURLOPT_HTTPHEADER, array('Content-type: text/javascript;charset=utf-8'));
		}

		$response = curl_exec($this->curl);
		curl_close($this->curl);

		// IF REQUEST FAILS
		if ($response === false) {
			return false;
		}

		// DECODING RESPONSE
		$json = json_decode($response);

		// ERROR WHILE DECODING RESPONSE
		if ($json === null) {
			return false;
		}

		return $json;
	}

}
