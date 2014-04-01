<?php
/**
 * COMMAND LINE
 * RpcMethodsParser.php -s VosManagerMethod.java -d VosManager.java -v -r "https://perun.metacentrum.cz/perun-rpc-fed/jsonp/"
 */
$opts = "s:d:v::r:";
$options = getopt($opts);
$src = $options["s"];
$desc = $options["d"];
$serverurl = $options["r"];
$verbose = array_key_exists("v", $options);

if($src && $desc){
	$time_start = microtime(true);

	$parser = new RpcMethodsParser($src);
	$parser -> setRpcServerUrl($serverurl);
	$parser -> parse();

	file_put_contents($desc, $parser -> build());

	$time_end = microtime(true);
	$time = ceil(($time_end - $time_start) * 1000);

	print "RPC interface generated in {$time} ms.\n";
}


/**
 * Simple error & messages handler
 */
function log_message($text, $error = false)
{
	if($error){
		die($text . "\n");
	}else{
		if($GLOBALS["verbose"])
		{
			print $text . "\n";
		}
	}
}


/**
 * Parses the Perun ManagerMethods to an interface
 *
 * Usage:
 * $parser = new RpcMethodsParser(filename);
 * $parser -> setRpcServerUrl("https://.../jsonp/");
 * $output = $parser -> parse();
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
class RpcMethodsParser{

	// * whatever
	const JAVADOCLINE_FORMAT = "/^\* *(.*)$/";

	// @return TYPE DESCRIPTION
	const RETURN_FORMAT = "/^@return ([a-zA-Z0-9<>_]+)[ ]*(.*)$/";

	// @param PARAM TYPE DESCRIPTION
	const PARAM_FORMAT = "/^@param ([a-zA-Z0-9_]+) ([a-zA-Z0-9<>_]+)[ ]*(.*)$/";

	// @param METHOD NAME TYPE DESCRIPTION
	const ENUM_FORMAT = "/^([a-zA-Z0-9_]+)/";

	// public enum ManagerNameMethod implements ManagerMethod
	const MANAGER_NAME_FORMAT = "/enum (.*)Method implements ManagerMethod/";

	// rpc server url
	private $rpcServerUrl = "";

	// lines of the Java file
	private $lines = array();

	// methods parsed from the file
	private $rpcMethods = array();
	private $rpcMethodsWaitingForName = array();

	// manager name (interface name)
	private $managerName = "";

	/**
	 * Creates a new parser instance, with filename as parameter
	 */
	public function __construct($filename)
	{
		if(!file_exists($filename))
		{
			log_message("ERROR: File not found", true);
		}

		$this -> lines = explode("\n", file_get_contents($filename));
		foreach($this -> lines as &$line){
			$line = trim($line);
		}
	}

	/**
	 * Sets the RPC server URL
	 */
	public function setRpcServerUrl($url)
	{
		$this -> rpcServerUrl = $url;
	}

	/**
	 * Builds the final output
	 */
	public function build()
	{
		if($this -> managerName == "")
		{
			log_message("ERROR: Manager name not found", true);
		}

		$output = "package rpc;\n";
		$output .= "import java.util.*;\n";
		$output .= "\npublic interface " . $this -> managerName . " {\n\n";


		foreach($this -> rpcMethods as $method){
			$output .= $method -> build() . "\n\n";
		}
		$output .= "}";
		return $output;
	}

	/**
	 * Parses the input file
	 */
	public function parse()
	{
		if(!is_array($this -> lines) || count($this -> lines) == 0)
		{
			log_message("ERROR: No lines found", true);
		}

		$startFound = false;
		$rpcMethod = null;
		$managerNameFound = false;

		for($rowNum = 0; $rowNum < count($this -> lines); $rowNum++)
		{
			$line = $this -> lines[$rowNum];

			// finding manager name
			if(!$managerNameFound){
				if($this -> tryParseManagerName($line)){
					$managerNameFound = true;
				}
				continue;
			}
			// finding start
			if($line == "/*#"){

				log_message("Start found at line " . ($rowNum + 1));

				// already started
				if($startFound){
					// ignoring
					continue;

					//log_message("ERROR: /** not expected at line " . ($rowNum + 1), true);
				}
				// ok, starting now
				$startFound = true;

				// for URL generating
				$managerCallName = strtolower($this->managerName[0]) . substr($this -> managerName, 1);

				$rpcMethod = new RpcMethod($this -> rpcServerUrl, $managerCallName);
				$this -> rpcMethodsWaitingForName[] = $rpcMethod;

				continue;
			}

			// finding end
			if($line == "*/"){

				log_message("End found at line " . ($rowNum + 1));


				// already ended
				if(!$startFound){
					// log_message("ERROR: */ not expected (start not found) at line " . ($rowNum + 1), true);
					continue;
				}
				// wating for the enum name
				$startFound = false;
				$endFound = true;
				continue;
			}

			// finding enum name
			if(!$startFound && $endFound){

				// try find enum name
				if($this -> tryParseEnumName($line)){

					log_message("Method name found at line " . ($rowNum + 1));


					// save rpc method
					$this -> rpcMethods = array_merge($this -> rpcMethods, $this -> rpcMethodsWaitingForName);

					// clear methods waiting for name
					$this -> rpcMethodsWaitingForName = array();
					$endFound = false;

					continue;
				}


			}

			// start exists, parse lines?
			if($startFound){

				$matches = array();
				// javadoc line?
				if(preg_match(self :: JAVADOCLINE_FORMAT, $line , $matches) === 0){

					// javadoc line not found
					//log_message("ERROR: Expecting * at the start of line " . ($rowNum + 1), true);

					// ignoring
					continue;
				}

				// crop line (removes * from the begining);
				$line = trim($matches[1]);

				// the type is return
				if($this -> tryParseReturn($line, $rpcMethod)){
					log_message("Return found at line " . ($rowNum + 1));
					continue;
				}

				// the type is param
				if($this -> tryParseParam($line, $rpcMethod)){
					log_message("Param found at line " . ($rowNum + 1));
					continue;
				}

				// general param
				log_message("Another javadoc found at line " . ($rowNum + 1));
				$rpcMethod -> addGeneralJavaDocLine($line);

				// continue
				continue;
			}
		}
	}

	/**
	 * Try to find and parse @return part of JavaDoc
	 */
	private function tryParseReturn($line, $rpcMethod){
		$matches = array();

		// if not found, return false
		if(preg_match(self :: RETURN_FORMAT, $line , $matches) === 0){
			return false;
		}

		$rpcMethod -> setReturnType($matches[1]);
		$rpcMethod -> addReturn($matches[2]);
		return true;
	}

	/**
	 * Try to find and parse @param part of JavaDoc
	 */
	private function tryParseParam($line, $rpcMethod){
		$matches = array();

		// if not found, return false
		if(preg_match(self :: PARAM_FORMAT, $line, $matches) === 0){
			return false;
		}

		$rpcMethod -> addParameter($matches[1], $matches[2], $matches[3]);
		return true;
	}

	/**
	 * Try to find and parse ENUM NAME
	 */
	private function tryParseEnumName($line){
		$matches = array();

		// if not found, return false
		if(preg_match(self :: ENUM_FORMAT, $line, $matches) === 0){
			return false;
		}

		foreach($this -> rpcMethodsWaitingForName as &$rpcMethod)
		{
			$rpcMethod -> setMethodName($matches[1]);
		}
		return true;
	}

	/**
	 * Try to find and parse manager name
	 */
	private function tryParseManagerName($line){
		$matches = array();

		// if not found, return false
		if(preg_match(self :: MANAGER_NAME_FORMAT, $line, $matches) === 0){
			return false;
		}

		$this -> managerName = $matches[1];
		return true;
	}

}


/**
 * Auxilary class as a abstraction of a method
 */
class RpcMethod{

	/**
	 * serialised JavaDoc comments
	 */
	private $comment = "";

	/**
	 * serialised parameters
	 */
	private $parameters = "";

	/**
	 * return type
	 */
	private $returnType = "void";

	/**
	 * method name
	 */
	private $methodName = "";

	/**
	 * For generating URL
	 */
	private $rpcServerUrl = "";

	/**
	 * For generating URL
	 */
	private $managerName = "";

	private $urlGenerated = false;

	/**
	 * Constructor
	 */
	public function __construct($rpcServerUrl = "", $managerName = "")
	{
		$this -> rpcServerUrl = $rpcServerUrl;
		$this -> managerName = $managerName;
	}

	/**
	 * Adds a line to JavaDoc part
	 */
	public function addGeneralJavaDocLine($line)
	{
		$this -> comment .= "     * " . $line . "\n";
	}

	/**
	 * Adds a return part to JavaDoc
	 */
	public function addReturn($description)
	{
		$this -> buildUrl();

		if($this -> returnType != "void")
		{
			$this -> addGeneralJavaDocLine("@return " . $description);
		}
	}

	/**
	 * Adds a parameter part to JavaDoc
	 */
	public function addParameter($name, $type, $description)
	{
		$this -> buildUrl();

		// add comma if not empty
		if($this -> parameters != ""){
			$this -> parameters .= ", ";
		}

		// adds parameters
		$this -> parameters .= $type . " " . $name;

		// adds the comment
		$this -> addGeneralJavaDocLine("@param " . $name . " " . $description);
	}

	/**
	 * Sets method name
	 */
	public function setMethodName($name){
		$this -> methodName = $name;

	}

	/**
	 * Sets return type
	 */
	public function setReturnType($type){
		$this -> returnType = $type;
	}

	/**
	 * Builds url if not already buil
	 */
	private function buildUrl()
	{
		if($this -> urlGenerated){
			return;
		}

		if($this -> rpcServerUrl != "" && $this -> managerName != ""){
			$url = $this -> rpcServerUrl . $this -> managerName . "/METHOD_NAME";

			$output = "     * Available at: <a href=\"{$url}\">{$url}</a>\n";
			$output .= "     *\n";
			$this -> comment .= $output;
		}
		$this -> urlGenerated = true;
	}

	/**
	 * Builds and returns the method and javadoc
	 */
	public function build()
	{
		// replace method name
		$this -> comment = strtr($this->comment, array("METHOD_NAME" => $this -> methodName));

		$output = "    /**\n";
		$output .= $this -> comment;


		$this -> buildUrl();

		$output .= "     */\n";
		$output .= "    public " . $this -> returnType  . " " . $this -> methodName . " ( " . $this -> parameters . " );";
		return $output;
	}
}
