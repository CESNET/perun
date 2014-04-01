<?php
/**
 * Class for retrieving data from Perun RPC
 */
class PerunRpcClient{

	// request parameters
	private $postVars;

	// URL of the RPC
	private $requestUrl = "";

	// curl request
	private $curl;

	// settings
	const USERPWD = "perunv3-registrar:";
	const EXT_SRC_IDP = "cz.metacentrum.perun.core.impl.ExtSourceIdp";


	/**
	 * Creates a new request instance, with specified URL.
	 * Eg. $requestUrl = "https://perun.metacentrum.cz/perun-rpc-krb/json/"
	 */
	public function __construct($requestUrl)
	{
		$this -> requestUrl = $requestUrl;
		$this -> init();
	}

	/**
	 * Initializes the connection
	 */
	private function init()
	{
		// Setup KRB keytab location
		putenv("KRB5CCNAME=FILE:/tmp/krb5cc_perun_registrar");

		// Setup delegation
		#        $this -> postVars = array (
		#            'delegatedLogin' => $_SERVER['REMOTE_USER'],
		#            'delegatedExtSourceName' => $_SERVER['Shib-Identity-Provider'],
		#            'delegatedExtSourceType' => self :: EXT_SRC_IDP
		#        );
		#
		// Initialize CURL
		$this -> curl = curl_init();
		curl_setopt($this -> curl, CURLOPT_RETURNTRANSFER, true);
		curl_setopt($this -> curl, CURLOPT_HTTPAUTH, CURLAUTH_GSSNEGOTIATE);
		curl_setopt($this -> curl, CURLOPT_USERPWD, self :: USERPWD);
	}

	/**
	 * Adds a parameter to the request
	 */
	public function addParameter($name, $value)
	{
		$this -> postVars[$name] = $value;
	}

	/**
	 * Retrieves parsed JSON data or false if request fails.
	 */
	public function retrieveData()
	{
		// REQUEST
		curl_setopt($this -> curl, CURLOPT_URL, $this -> requestUrl);
		if ($this->postVars != null) {
			curl_setopt($this -> curl, CURLOPT_POSTFIELDS, json_encode($this -> postVars));
			curl_setopt($this -> curl, CURLOPT_POST, 1);
		}

		$response = curl_exec($this -> curl);
		curl_close($this -> curl);

		// IF REQUEST FAILS
		if($response === false){
			return false;
		}

		// DECODING RESPONSE
		$json = json_decode($response);

		// ERROR WHILE DECODING RESPONSE
		if($json === null){
			return false;
		}

		return $json;
	}

}
