#!/usr/bin/perl
use strict;
use warnings;
use Switch;
use Getopt::Long qw(:config no_ignore_case);

#constants
our $OUTPUT_DIR="/var/www/perun-web";

#INFO ABOUT STRUCTURE
#     %MANAGERS
#         |
#   {manager name}
#         |
#     @METHODS
#         |
#       {0..x}
#         |
#      %METHOD
#       |    |
#   {"name"}{javadocs}
#     |          |
# $methodName  @javadocs
#                |
#              {0..x}
#                |
#              %javadoc
#              | | | |
#   {"text","params","throws","return","deprecated","exampleResponse","exampleParams"}
#      |       |       |         |           |              |                |
#   @text    @params  @throws   $return  $deprecated $exampleResponse @exampleParams
#      |       |       |
#   {0..x}   {0..x}  {0..x}
#      |       |       |
#    $text  $param    $throw

#variables
my $managers = {};

our %objectExamples;

my $listPrepend = "[ ";
my $listAppend = " , {...} , {...} ]";

$objectExamples{"Attribute"} = "{ \"id\" : 2820 , \"friendlyName\" : \"createdAt\" , \"namespace\" : \"urn:perun:vo:attribute-def:core\" , \"value\" : \"2011-05-17 00:50:06.3\" , \"type\" : \"java.lang.String\" , \"entity\" : \"vo\" , \"writable\" : true , \"baseFriendlyName\" : \"createdAt\" , \"friendlyNameParameter\" : \"\" , \"displayName\" : \"VO created date\" , \"description\" : \"Date when VO was created.\" , \"beanName\" : \"Attribute\" }";
$objectExamples{"List&lt;Attribute&gt;"} = $listPrepend . $objectExamples{"Attribute"} . $listAppend;
$objectExamples{"List<Attribute>"} = $objectExamples{"List&lt;Attribute&gt;"};

$objectExamples{"Vo"} = "{ \"id\" : 123 , \"name\" : \"My testing VO\" , \"shortName\" : \"test_vo\" , \"beanName\" : \"Vo\" }";
$objectExamples{"List&lt;Vo&gt;"} = $listPrepend . $objectExamples{"Vo"} . $listAppend;
$objectExamples{"List<Vo>"} = $objectExamples{"List&lt;Vo&gt;"};

$objectExamples{"Facility"} = "{ \"id\" : 24 , \"name\" : \"host.facility.cz\" , \"description\" : \"is optional\" , \"beanName\" : \"Facility\" }";
$objectExamples{"List&lt;Facility&gt;"} = $listPrepend . $objectExamples{"Facility"} . $listAppend;
$objectExamples{"List<Facility>"} = $objectExamples{"List&lt;Facility&gt;"};

$objectExamples{"RichFacility"} = "{ \"id\" : 24 , \"name\" : \"host.facility.cz\" , , \"description\" : \"is optional\" , \"facilityOwners\" : [ { \"id\" : 183 , \"name\" : \"Some Body\" , \"type\" : \"technical\" , \"contact\" : \"mail\@mail.com\" , \"beanName\" : \"Owner\" } ] , \"beanName\" : \"RichFacility\" }";
$objectExamples{"List&lt;RichFacility&gt;"} = $listPrepend . $objectExamples{"RichFacility"} . $listAppend;
$objectExamples{"List<RichFacility>"} = $objectExamples{"List&lt;RichFacility&gt;"};

$objectExamples{"Resource"} = "{ \"id\" : 493 , \"name\" : \"host1.host.cz\" , \"description\" : \"ROOT access to host1.host.cz\" , \"facilityId\" : 24 , \"voId\" : 21 , \"beanName\" : \"Resource\" }";
$objectExamples{"List&lt;Resource&gt;"} = $listPrepend . $objectExamples{"Resource"} . $listAppend;
$objectExamples{"List<Resource>"} = $objectExamples{"List&lt;Resource&gt;"};

$objectExamples{"ResourceTag"} = "{ \"id\" : 3 , \"tagName\" : \"comp_cluster\" , \"voId\" : 123 , \"beanName\" : \"ResourceTag\" }";
$objectExamples{"List&lt;ResourceTag&gt;"} = $listPrepend . $objectExamples{"ResourceTag"} . $listAppend;
$objectExamples{"List<ResourceTag>"} = $objectExamples{"List&lt;ResourceTag&gt;"};

$objectExamples{"RichResource"} = "{ \"id\" : 493 , \"name\" : \"host1.host.cz\" , \"description\" : \"ROOT access to host1.host.cz\" , \"facilityId\" : 24 , \"voId\" : 123 , \"beanName\" : \"RichResource\" , \"vo\" : ". $objectExamples{"Vo"} . ", \"facility\" : ". $objectExamples{"Facility"} . " , \"resourceTags\" : ". $objectExamples{"List<ResourceTag>"} . " }";
$objectExamples{"List&lt;RichResource&gt;"} = $listPrepend . $objectExamples{"RichResource"} . $listAppend;
$objectExamples{"List<RichResource>"} = $objectExamples{"List&lt;RichResource&gt;"};

$objectExamples{"Owner"} = "{ \"id\" : 183 , \"name\" : \"Some Body\" , \"type\" : \"administrative\" , \"contact\" : \"mail\@mail.com\" , \"beanName\" : \"Owner\" }";
$objectExamples{"List&lt;Owner&gt;"} = $listPrepend . $objectExamples{"Owner"} . $listAppend;
$objectExamples{"List<Owner>"} = $objectExamples{"List&lt;Owner&gt;"};

$objectExamples{"Group"} = "{ \"id\" : 1061 , \"name\" : \"My group\" , \"shortName\" : \"My group\" , \"description\" : \"My testing group\" , \"parentGroupId\" : null , \"voId\" : 201 , \"beanName\" : \"Group\" }";
$objectExamples{"List&lt;Group&gt;"} = $listPrepend . $objectExamples{"Group"} . $listAppend;
$objectExamples{"List<Group>"} = $objectExamples{"List&lt;Group&gt;"};

$objectExamples{"RichGroup"} = "{ \"id\" : 1061 , \"name\" : \"My Group\" , \"shortName\" : \"My Group\" , \"description\" : \"My testing group\" , \"parentGroupId\" : null , \"voId\" : 201 , \"beanName\" : \"RichGroup\" , \"attributes\" : [ { \"value\" : null , \"type\" : \"java.lang.String\" , \"entity\" : \"group\" , \"namespace\" : \"urn:perun:group:attribute-def:def\" , \"friendlyName\" : \"synchronizationEnabled\" , \"writable\" : true , \"baseFriendlyName\" : \"synchronizationEnabled\" , \"friendlyNameParameter\" : \"\" , \"displayName\" : \"Synchronization enabled\" , \"description\" : \"Enables group synchronization from external source.\" , \"id\" : 103 , \"beanName\" : \"Attribute\" } ] }";
$objectExamples{"List&lt;RichGroup&gt;"} = $listPrepend . $objectExamples{"RichGroup"} . $listAppend;
$objectExamples{"List<RichGroup>"} = $objectExamples{"List&lt;RichGroup&gt;"};

$objectExamples{"Member"} = "{ \"id\" : 12 , \"userId\" : 34 , \"voId\" : 42 , \"membershipType\" : \"DIRECT\" , \"status\" : \"VALID\" , \"beanName\" : \"Member\" }";
$objectExamples{"List&lt;Member&gt;"} = $listPrepend . $objectExamples{"Member"} . $listAppend;
$objectExamples{"List<Member>"} = $objectExamples{"List&lt;Member&gt;"};

$objectExamples{"User"} = "{ \"firstName\" : \"Some\" , \"lastName\" : \"Body\" , \"middleName\" : null , \"titleBefore\" : \"Mgr.\" , \"titleAfter\" : null , \"serviceUser\" : false , \"id\" : 34 , \"beanName\" : \"User\" }";
$objectExamples{"List&lt;User&gt;"} = $listPrepend . $objectExamples{"User"} . $listAppend;
$objectExamples{"List<User>"} = $objectExamples{"List&lt;User&gt;"};

$objectExamples{"ExtSource"} = "{ \"name\" : \"PERUNPEOPLE\" , \"type\" : \"cz.metacentrum.perun.core.impl.ExtSourceSql\" , \"attributes\" : {} , \"id\" : 2 , \"beanName\" : \"ExtSource\" }";
$objectExamples{"List&lt;ExtSource&gt;"} = $listPrepend . $objectExamples{"ExtSource"} . $listAppend;
$objectExamples{"List<ExtSource>"} = $objectExamples{"List&lt;ExtSource&gt;"};

$objectExamples{"UserExtSource"} = "{ \"userId\": 34 , \"loa\" : 0 , \"extSource\" : " . $objectExamples{"ExtSource"} . " , \"login\" : \"my_login\" , \"id\" : 312 , \"beanName\" : \"UserExtSource\" }";
$objectExamples{"List&lt;UserExtSource&gt;"} = $listPrepend . $objectExamples{"UserExtSource"} . $listAppend;
$objectExamples{"List<UserExtSource>"} = $objectExamples{"List&lt;UserExtSource&gt;"};

$objectExamples{"RichUser"} = "{ \"firstName\" : \"Some\" , \"lastName\" : \"Body\" , \"middleName\" : null , \"titleBefore\" : \"Mgr.\" , \"titleAfter\" : null , \"serviceUser\" : false , \"id\" : 34 , \"beanName\" : \"User\" , \"userExtSources\" : " . $objectExamples{"List<UserExtSource>"} . ", \"userAttributes\" : [ { \"value\" : \"my_login\" , \"type\" : \"java.lang.String\" , \"entity\" : \"user\" , \"namespace\" : \"urn:perun:user:attribute-def:def\" , \"friendlyName\" : \"login-namespace:perun\" , \"writable\" : true , \"baseFriendlyName\" : \"login-namespace\" , \"friendlyNameParameter\" : \"perun\" , \"displayName\" : \"Login in namespace: perun\" , \"description\" : \"Logname in namespace 'perun'.\" , \"id\" : 1905 , \"beanName\" : \"Attribute\" } ] }";
$objectExamples{"List&lt;RichUser&gt;"} = $listPrepend . $objectExamples{"RichUser"} . $listAppend;
$objectExamples{"List<RichUser>"} = $objectExamples{"List&lt;RichUser&gt;"};

$objectExamples{"RichMember"} = "{ \"id\" : 12 , \"userId\" : 34 , \"voId\" : 42 , \"membershipType\" : \"DIRECT\" , \"status\" : \"VALID\" , \"beanName\" : \"RichMember\" , \"user\" : " . $objectExamples{"User"} . " , \"userExtSources\" : " . $objectExamples{"List<UserExtSource>"} . " , \"memberAttributes\" : [ ] , \"userAttributes\" : [ { \"value\" : \"my_login\" , \"type\" : \"java.lang.String\" , \"entity\" : \"user\" , \"namespace\" : \"urn:perun:user:attribute-def:def\" , \"friendlyName\" : \"login-namespace:perun\" , \"writable\" : true , \"baseFriendlyName\" : \"login-namespace\" , \"friendlyNameParameter\" : \"perun\" , \"displayName\" : \"Login in namespace: perun\" , \"description\" : \"Logname in namespace 'perun'.\" , \"id\" : 1905 , \"beanName\" : \"Attribute\" } ] }";
$objectExamples{"List&lt;RichMember&gt;"} = $listPrepend . $objectExamples{"RichMember"} . $listAppend;
$objectExamples{"List<RichMember>"} = $objectExamples{"List&lt;RichMember&gt;"};

$objectExamples{"RTMessage"} = "{ \"ticketNumber\" : 32525 , \"memberPreferredEmail\" : \"mail\@mail.com\" }";

$objectExamples{"Service"} = "{ \"id\" : 290 , \"name\" : \"passwd\" }";
$objectExamples{"List&lt;Service&gt;"} = $listPrepend . $objectExamples{"Service"} . $listAppend;
$objectExamples{"List<Service>"} = $objectExamples{"List&lt;Service&gt;"};

$objectExamples{"ServicesPackage"} = "{ \"id\" : 50 , \"name\" : \"Unix account\" , \"description\" : \"Collection of services for managing unix accounts.\" }";
$objectExamples{"List&lt;ServicesPackage&gt;"} = $listPrepend . $objectExamples{"ServicesPackage"} . $listAppend;
$objectExamples{"List<ServicesPackage>"} = $objectExamples{"List&lt;ServicesPackage&gt;"};

$objectExamples{"Destination"} = "{ \"id\" : 99 , \"destination\" : \"host\@host.cz\" , \"type\" : \"HOST\" , \"propagationType\" : \"PARALLEL\" }";
$objectExamples{"List&lt;Destination&gt;"} = $listPrepend . $objectExamples{"Destination"} . $listAppend;
$objectExamples{"List<Destination>"} = $objectExamples{"List&lt;Destination&gt;"};

$objectExamples{"Host"} = "{ \"id\" : 523 , \"hostname\" : \"host1.host.cz\" }";
$objectExamples{"List&lt;Host&gt;"} = $listPrepend . $objectExamples{"Host"} . $listAppend;
$objectExamples{"List<Host>"} = $objectExamples{"List&lt;Host&gt;"};

$objectExamples{"AuditMessage"} = "{ \"id\" : 249053 , \"msg\" : \"Something happened.\" , \"actor\" : \"actor\@hostname.cz\" , \"createdAt\" : \"2015-03-16 16:00:40.449221\" , \"createdByUid\" : \"34\" , \"fullMessage\" : \"249053 \\\"2015-03-16 16:00:40.449221\\\" \\\"actor\@hostname.cz\\\" Something happened.\" }";
$objectExamples{"List&lt;AuditMessage&gt;"} = $listPrepend . $objectExamples{"AuditMessage"} . $listAppend;
$objectExamples{"List<AuditMessage>"} = $objectExamples{"List&lt;AuditMessage&gt;"};

$objectExamples{"String"} = "\"text\"";
$objectExamples{"boolean"} = "true";

$objectExamples{"Candidate"} = "{ \"id\" : 0 , \"serviceUser\" : false , \"firstName\" : \"Random\" , \"lastName\" : \"Name\" , \"middleName\" : null , \"titleBefore\" : \"Dr.\" , \"titleAfter\" : null , userExtSource : " . $objectExamples{"UserExtSource"} . " , additionalUserExtSources : null , attributes : { \"urn:perun:member:attribute-def:def:organization\" : \"Masarykova univerzita\" , \"urn:perun:member:attribute-def:def:mail\" : \"random\@name.cz\" } }";
$objectExamples{"List&lt;Candidate&gt;"} = $listPrepend . $objectExamples{"Candidate"} . $listAppend;
$objectExamples{"List<Candidate>"} = $objectExamples{"List&lt;Candidate&gt;"};

$objectExamples{"SecurityTeam"} = "{ \"id\" : 924 , \"name\" : \"CSIRT\" , \"description\" : \"My CSIRT\" }";
$objectExamples{"List&lt;SecurityTeam&gt;"} = $listPrepend . $objectExamples{"SecurityTeam"} . $listAppend;
$objectExamples{"List<SecurityTeam>"} = $objectExamples{"List&lt;SecurityTeam&gt;"};

$objectExamples{"Pair<User,String>"} = "{ \"left\" : " . $objectExamples{"User"} ." , \"right\" : \"Some reason\" }";
$objectExamples{"List&lt;Pair&lt;User,String&gt;&gt;"} = $listPrepend . $objectExamples{"Pair<User,String>"} . $listAppend;
$objectExamples{"List<Pair<User,String>>"} = $objectExamples{"List&lt;Pair&lt;User,String&gt;&gt;"};

# SUB HELP
# help info
sub help {
	return qq{
  Generate HTML javadoc for Perun RPC
  ----------------------------------------
  Available options:
  --directory  | -d path to directory with rpc methods
  --output     | -o path to directory to write output files to
  --help       | -h prints this help

};
}

# SUB PROCESSFILE
# process every file in directory
sub processFile {
	my $file_name = $_[0];
	my $dir_path = $_[1];
	my $fullPath = $dir_path . "/" . $file_name;
	my $managerName;
	if($file_name =~ m/^(.*)Method\.java/) {
		$managerName = "$1";
	} elsif ($file_name =~ m/^(.*)\..*/) {
		$managerName = "$1";
	} else {
		$managerName = $file_name;
	}

	# open file
	print "PROCESSING: " . $managerName . " ($fullPath)\n";
	open my $handler, $fullPath or die "Could not open $fullPath";

	# phase of looking for method
	# 0 - looking for start of javadoc symbols /*# (if found -> 1)
	# 1 - looking for parts of one javadoc or end of this javadoc (if end found -> 2)
	# 2 - looking for another javadoc (if found -> 1) or name of method (if found -> 0)
	my $phase=0; #phase of looking in file
	my @methods = ();
	my $method = {};
	my @params = ();
	my @textLines = ();
	my @throws = ();
	my $deprecated = 0;
	my $exampleResponse;
	my @exampleParams = ();
	my $return;
	my @javadocs = (); #array with javadocs of one method
	while (my $line = <$handler>) {
		# skip every line which start // (one line comment)
		next if($line =~ m/^\s*\/\/.*/);

		# skip all comments which start /* without hash
		# !!! THIS IS NOT IMPLEMENTED, IF THERE IS SOME /* COMMENT ON IMPORTANT PLACE
		# IT CAN CREATE BAD DOCUMENTATION, NEED TO SOLVE OR DO NOT USE THIS TYPE OF COMMENTS

		switch ($phase) {
			case 0 {
				if($line =~ m/^\s*\/\*\#/) { $phase=1; }
			}
			case 1 {
				if($line =~ m/^\s*[*]\s*\@param\s*(.*)/) {
					push @params, $1;
				} elsif($line =~ m/^\s*\*\s*[@]return\s*(.*)/) {
					$return="$1";
				} elsif($line =~ m/^\s*\*\s*[@]exampleResponse\s*(.*)/) {
					$exampleResponse="$1";
				} elsif($line =~ m/^\s*\*\s*[@]exampleParam\s*(.*)/) {
					push @exampleParams, $1;
				} elsif($line =~ m/^\s*\*\s*[@]deprecated\s*(.*)/) {
					$deprecated=1;
				} elsif($line =~ m/^\s*\*\s*[@]throw\s*(.*)/) {
					push @throws, $1;
				} elsif($line =~ m/^\s*\*\//) {
					$phase=2;
					# local variables for purpose of saving information
					my $javadoc={};
					my @localParams = @params;
					my @localThrows = @throws;
					my @localTextLines = @textLines;
					my @localExampleParams = @exampleParams;
					# save one javadoc
					$javadoc->{'params'} = \@localParams;
					$javadoc->{'throws'} = \@localThrows;
					$javadoc->{'return'} = $return;
					$javadoc->{'exampleResponse'} = $exampleResponse;
					$javadoc->{'exampleParams'} = \@localExampleParams;
					if ($deprecated == 1) {
						$javadoc->{'deprecated'} = $deprecated;
					}
					$javadoc->{'text'} = \@localTextLines;
					push @javadocs, $javadoc;
					#reset all needed variables
					@params=();
					@textLines=();
					@throws=();
					@exampleParams=();
					undef $return;
					undef $exampleResponse;
					$deprecated=0;
					$javadoc=();
				} elsif($line =~ m/^\s*\*\s*(.*)/) {
					push @textLines, $1;
				} else {
					#skip this line, it is probably space or something nasty, we dont need it
				}
			}
			case 2 {
				if($line =~ m/^\s*\/[*]\#/) {
					$phase=1;
				} elsif($line =~ m/^\s*([a-zA-Z0-9]+)\s*\{.*/) {
					$phase=0;
					$method->{'name'}=$1;
					#local variable for saving all javadocs
					my @localJavadocs = @javadocs;
					$method->{'javadocs'}= \@localJavadocs;
					#local variable for saving one method
					my $localMethod = $method;
					push @methods, $localMethod;
					#reset all needed variables
					@javadocs = ();
					$method = {};
				} else {
					#skip this line, it is probably some code or empty line, we dont need it
				}
			}
		}
	}
	if($phase != 0) {
		die "Some phase was not ended correctly for file $file_name and phase $phase!";
	}

	#save all parsed methods
	$managers->{$managerName}=\@methods;

	close($handler);
}

#START OF MAIN PROGRAM

# default options
my $directory;
GetOptions ("help|h" => sub {print help(); exit 0;},
	"directory|d=s" => \$directory , "output|o=s" => \$OUTPUT_DIR) || die help();

#if directory with methods is not set
unless (defined($directory)) { die "ERROR: Path to directory where RPC methods are is required \n";}

#if directory with methods not exists
unless (-d $directory) { die "ERROR: Directory " . $directory . " not exists!"; }

#open input dir
opendir (DIR, $directory) or die "Cannot open directory with files (with methods)!";

#create output dir if not exists yet
unless (-d $OUTPUT_DIR) {
	mkdir $OUTPUT_DIR;
	print $OUTPUT_DIR . " was created. \n";
}

#process all files in dir
while (my $file = readdir(DIR)) {
	next if ($file =~ m/^\./);
	processFile($file, $directory)
}

# PRINT MAIN FILE

open FILE,">$OUTPUT_DIR/rpc-javadoc-howto.shtml" or die "Cannot open $OUTPUT_DIR/rpc-javadoc-howto.shtml: $! \n";

print FILE qq{
<!--#include virtual="header.shtml" -->

<div class="techspec content">

	<div class="push-under-menu"></div>

	<div class="container">

	<h2>RPC API documentation</h2>

	<div class="col-md-3 list-group">
		<a style="color: #005b99;" class="list-group-item" href="documentation.shtml">Back to Documentation<i style="margin-top: 3px; vertical-align: baseline;" class="icon-chevron-left pull-left"></i></a>
		<span class="list-group-item"><b><u>General</u></b></span>
		<a style="color: #005b99;" class="list-group-item" href="rpc-javadoc-howto.shtml"><b>How to use Perun RPC</b></a>
		<span class="list-group-item"><b><u>Managers</u></b></span>
};

foreach my $manager (sort(keys %{$managers})) {
	print FILE "<a class=\"list-group-item\" style=\"color: #005b99;\" href=\"rpc-javadoc-$manager.shtml\">$manager</a>"
}

print FILE "</div><div class=\"col-md-9 pull-right\">";


print FILE qq{
		<h2>How to use Perun RPC</h2>

		<p class="well warning">Perun RPC is <b>not</b> using traditional REST API, so please read properly, how are your requests handled and what are expected responses.</p>

		<h3>Authentication</h3>

		<p>Authentication of person / component making a request is done by Apache web server and depends on it’s current configuration. Perun can internally handle identity provided by Kerberos, Shibboleth IdP, Certificate or REMOTE_USER like Apache config. Identity info provided by Apache to Perun is used only to match identity to user object from Perun (if exists).</p>

		<h3>Authorization</h3>

		<p>Authorization is done on Perun side based on privileges associated with user, which are stored inside Perun. Few methods are accessible without authorization (e.g. in order to allow new users to register to Perun).</p>

		<h3>Request type GET / POST</h3>

		<p><strong>We recommend to use POST requests all the time.</strong> It’s most simple, since all parameters are transferred in a request body in JSON format and response is the same.</p>

		<p>You can optionally use GET requests, but then parameters must be present in a request URL (as a query) and you can call only listing methods (get, list). Methods changing state (create, delete, update, add, remove,…) must be POST.</p>

		<a id="url-structure"></a><h3>URL structure</h3>

		<pre><code>http(s)://[server]/[authentication]/rpc/[format]/[manager]/[method]?[params]</code></pre>

		<dl>
			<dt>[server]</dt>
			<dd>Is hostname of your Perun instance.</dd>

			<dt>[authentication]</dt>
			<dd>Is type of expected authentication which must be supported by Perun instance. Standard values are: <i>fed</i> (Shibboleth IDP), <i>krb</i> (Kerberos), <i>cert</i> (Certificate), <i>non</i> (without authorization).</dd>

			<dt>[format]</dt>
			<dd>Format of data for transfer. Possible values are: <i>json</i>, <i>jsonp</i> and <i>voot</i>.</dd>

			<dt>[manager]</dt>
			<dd>Name of manager to call method in (in a camel case).</dd>

			<dt>[method]</dt>
			<dd>Name of method to call from selected manager (in a camel case).</dd>

			<dt>[params]</dt>
			<dd>Query parameters passed in URL (for GET requests) in a following manner: <pre>?param1=value&amp;param2=value&amp;listParam[]=value1&amp;listParam[]=value2</pre></dd>

		</dl>

		<a id="passing-parameters"></a><h3>Passing parameters</h3>

		<p>When using GET requests, method parameters must be present in a URL (see above for full overview). <pre>URL:  ...vosManager/getVoById?id=123</pre>

		<p>When using POST, expected parameters are properties of JSON object in request body, eg.: <pre>URL:  ...vosManager/getVoById
Body: { "id": 123 }</pre>

		<p>If you are passing whole objects (e.g. on object creation), you usually omit not relevant properties and id is set to 0.

<pre>URL:  ...vosManager/createVo
Body: { "id": 0 , "name" : "My VO" , "shortName" : "myVo" }</pre>

		<p><i>Note: VO object is missing properties like: beanName, createdAt, createdBy etc.</i>

		<p>Perun API is using mostly only IDs of objects, so you don’t have to care about exact object properties values. Objects are retrieved only internally and must pass existence and authorization checks.

		<a id="http-return-codes"></a><h3>HTTP return codes</h3>

		<p><b>If OK, all requests to Perun API returns 200 return code.</b>
		<p>When processing of your request throws an exception in java code, <b>response is still 200, but it’s body is replaced with serialized Exception object.</b> Any other return code signalize problem with a server (not found, internal error etc.).

		<a id="return-values"></a><h3>Return values</h3>

		<p>If response of method call is an object or list of them, correct JSON representation is returned.

		<p>If response of method call is null or any primitive type (integer, string, boolean), returned value is simple string. So you will get: <code>null</code> and <b>NOT</b>: <code>{ "value": null }</code>

		<h3>Usage of JSON/JSONP formats</h3>

		<p>Perun can handle both formats. While <b>both consumes valid JSON as input</b>, second one produces response with padding:
<pre>Request:  someUrl?callback=call1&amp;param=value
Response: call1(response);
</pre>

		<p>If you omit <em>callback</em> query parameter, you will get: </p><pre>Response: null(response);</pre><p></p>

		<p>When using JSONP, returned objects are stripped of non relevant properties like <em>createdAt</em>, <em>createdBy</em>, <em>modifiedAt</em>, <em>modifiedBy</em> etc. You can get them when using standard JSON.</p>

	</div>
</div>
</div>
};

print FILE qq{
<script type="text/javascript">
	\$(document).ready(function() {
		\$("#nav-documentation").addClass('active');
	});
</script>

<!--#include virtual="footer.shtml" -->
};

close (FILE);

foreach my $manager (sort(keys %{$managers})) {

	open FILE,">$OUTPUT_DIR/rpc-javadoc-$manager.shtml" or die "Cannot open $OUTPUT_DIR/rpc-javadoc-$manager.shtml: $! \n";

	print FILE qq{
	<!--#include virtual="header.shtml" -->

	<div class="techspec content">

		<div class="push-under-menu"></div>

		<div class="container">

		<a id="$manager-title"></a><h2>RPC API documentation</h2>

		<div class="col-md-3 list-group">
		<a style="color: #005b99;" class="list-group-item" href="documentation.shtml">Back to Documentation<i style="margin-top: 3px; vertical-align: baseline;" class="icon-chevron-left pull-left"></i></a>
		<span class="list-group-item"><b><u>General</u></b></span>
		<a class="list-group-item" style="color: #005b99;" href="rpc-javadoc-howto.shtml">How to use RPC</a>
		<span class="list-group-item"><b><u>Managers</u></b></span>
	};

	foreach my $menuManager (sort(keys %{$managers})) {

		my $activeLink = "";
		if ($menuManager eq $manager) {
			$activeLink = "<b>" . $menuManager . "</b>";
		} else {
			$activeLink = $menuManager;
		}

		print FILE qq{<a class="list-group-item" style="color: #005b99;" href="rpc-javadoc-$menuManager.shtml">$activeLink</a>};

	}

	print FILE "</div>";

	#print FILE qq{<div class="panel-group" id="$manager">};
	print FILE qq{<div class="col-md-9 pull-right">};

	print FILE qq{<h2>$manager</h2>};

	my $methods = $managers->{$manager};
	my $sortedMethods={};

	#prepare sorted methods
	foreach my $notSortedMethod (@{$methods}) {
		#get names of methods
		my $methodName = $notSortedMethod->{'name'};
		my $javadocs = $notSortedMethod->{'javadocs'};
		$sortedMethods->{$methodName}=$notSortedMethod->{'javadocs'};
	}

	#print sorted methods
	foreach	my $sortedMethod (sort(keys %{$sortedMethods})) {
		my $javadocs = $sortedMethods->{$sortedMethod};

		#print info about javadocs
		my $counter = 0;
		foreach my $javadoc (@{$javadocs}) {

			$counter++;

			my $throws = $javadoc->{'throws'};
			my $return = $javadoc->{'return'};
			my $params = $javadoc->{'params'};
			my $texts = $javadoc->{'text'};
			my $deprecated = $javadoc->{'deprecated'};
			my $exampleResponse = $javadoc->{'exampleResponse'};
			my $exampleParamsLocal = $javadoc->{'exampleParams'};

			# FILL MAP with example params
			# $exampleParams{'param'}->"example_itself"
			my %exampleParams = ();
			foreach my $parameter (@$exampleParamsLocal) {
				if (defined $parameter && $parameter ne "") {
					my @rest = split(/ /, $parameter);
					splice(@rest, 0, 1);
					my $restPar = join(" ", @rest);
					$exampleParams{(split(/ /, $parameter))[0]} = $restPar;
				}
			}

			# CREATE ANNOTATION

			my $methodAnnotation = "";
			if (defined $params) {
				foreach my $par (@$params) {
					if (defined $par && $par ne "") {

						my $par1 = (split(/ /, $par))[1];
						$par1 =~ s/\Q<\E/&lt;/g;
						$par1 =~ s/\Q>\E/&gt;/g;

						$methodAnnotation .= $par1;
						$methodAnnotation .= " ";
						$methodAnnotation .= (split(/ /, $par))[0];
						$methodAnnotation .= ", "
					}
				}
			}
			if (length($methodAnnotation) >= 2) { $methodAnnotation = substr($methodAnnotation, 0, -2) }

			# is deprecated ?
			my $depr = "";
			if (defined $deprecated) {
				#$depr = "<span style=\"padding: 10px 20px; color: #005b99;\" class=\"pull-right\"><b>Deprecated</b></span>";
				$depr = '<abbr class="pull-right" title="Method is NOT recommended for use, it can be removed in any time."><b>Deprecated</b></abbr>';
			}

			# PRINT ANNOTATION

			print FILE qq{
				<div class="panel panel-default" style="margin-bottom: 5px;">
				<div class="panel-heading" style="background-color: white;">
					<span class="panel-title">
						$depr
						<a style="color: #005b99;" data-toggle="collapse" data-parent="#$manager" href="#$manager$sortedMethod$counter">
							$sortedMethod ( $methodAnnotation )
						</a>
					</span>
				</div>
				<div id="$manager$sortedMethod$counter" class="panel-collapse collapse">
                <div class="panel-body">
			};

			# <i class="icon-chevron-left" style="margin-top: 4px; transition: all 0.2s ease-out 0s;"></i>

			# PRINT MAIN TEXT
			print FILE "<p>";
			print FILE join(" " , @{$texts});

			# PRINT PARAM TABLE

			if (@{$params}) {

				print FILE "<table class=\"table\"><tr><th>Parameter name</th><th>Data type</th><th width=\"60%\">Description</th></tr>";

				#print params
				foreach my $param (@{$params}) {
					my @par = split(/ /, $param);

					$par[1] =~ s/\Q<\E/&lt;/g;
					$par[1] =~ s/\Q>\E/&gt;/g;

					print FILE '<tr><td>' . $par[0] . "</td><td>" . $par[1] . "</td><td>";
					splice(@par, 0, 2);
					print FILE join(" ", @par);
					print FILE "</td></tr>\n";
				}

				print FILE "</table>";
			}

			# PRINT THROWS TABLE

			print FILE "<table class=\"table\"><tr><th>Thrown exception</th><th width=\"60%\">Description</th></tr>";

			push (@{$throws}, "PrivilegeException When caller is not allowed to call this method. Result may vary based on caller identity and provided parameter values.");
			push (@{$throws}, "InternalErrorException When unspecified error occur. See exception param <code>message</code> for explanation.");
			push (@{$throws}, "RpcException Wrong usage of API (wrong url, missing param etc.). See exception params <code>message</code> and <code>type</code> for explanation.");

			foreach my $throw (sort @{$throws}) {

				my @tro = split(/ /, $throw);
				splice(@tro, 0, 1);
				my $restTro = join(" ", @tro);

				print FILE "<tr><td>" . (split(/ /, $throw))[0] . "</td><td>" . $restTro . "</td></tr>"
			}
			#print FILE '<tr><td>PrivilegeException</td><td>When caller is not allowed to call this method. Result may vary based on caller identity and provided parameter values.</td></tr>';
			#print FILE '<tr><td>InternalErrorException</td><td>When unspecified error occur. See exception <code>message</code> param for explanation.</td></tr>';
			#print FILE '<tr><td>RpcException</td><td>When caller is using API wrong way (wrong url, missing param etc.). See exception <code>message</code> and <code>type</code> params for explanation.</td></tr>';

			print FILE "</table>";

			# PRINT RETURN TABLE

			print FILE "<table class=\"table\"><tr><th>Return type</th><th width=\"60%\">Description</th></tr>";

			if(defined $return) {

				my @ret = split(/ /, $return);
				# escape <> in return type
				$ret[0] =~ s/\Q<\E/&lt;/g;
				$ret[0] =~ s/\Q>\E/&gt;/g;

				print FILE '<tr><td>' . $ret[0] . "</td><td>";
				splice(@ret, 0, 1);
				print FILE join(" ", @ret);
				print FILE "</td></tr>\n";
			} else {
				print FILE '<tr><td>void</td><td></td></tr>';
			}

			print FILE "</table>";

			# PRINT EXAMPLE URL

			my $managerUrl = lcfirst($manager);
			print FILE qq{
            	<p><b>Example URL</b><pre><code>https://[hostname]/krb/rpc/json/$managerUrl/$sortedMethod</code></pre>
            };

			print FILE "<ul><li><a href=\"rpc-javadoc-howto.shtml#url-structure\"><i>see URL structure</i></a></li></ul>";

			# PRINT EXAMPLE PARAMS

			if (@{$params}) {

				print FILE "<p><b>Example params</b><pre><code>{ ";

				#print params
				for (my $count = 0; $count < scalar @{$params}; $count++) {

					my $param = @{$params}[$count];
					my @par = split(/ /, $param);

					my $printPar = "{...}";
					# If we have fixed example for param, use it
					if (exists($exampleParams{$par[0]})) {
						$printPar = $exampleParams{$par[0]};
						# We don't have fixed example, use generic definition
					} elsif ($par[1] eq "int") {
						$printPar = int(rand(100));
					} elsif ($par[1] eq "List") {
						$printPar = "[ {...} , {...} ]";
					} elsif ($par[1] eq "String[]" || $par[1] eq "List<String>") {
						$printPar = "[ \"text\" , \"text\" ]";
					} elsif ($par[1] eq "int[]" || $par[1] eq "List<Integer>") {
						$printPar = "[ " . int(rand(100)) . " , " . int(rand(100)) ." ]";
					} elsif (exists($objectExamples{$par[1]})) {
						$printPar = $objectExamples{$par[1]};
					}

					print FILE "\"" . $par[0] . "\" : " . $printPar;

					if ($count < (scalar @{$params})-1) {
						print FILE " , ";
					}

				}

				print FILE " }</code></pre>";

				print FILE "<ul><li><a href=\"rpc-javadoc-howto.shtml#passing-parameters\"><i>see Passing params</i></a></li></ul>";

			}

			# PRINT EXAMPLE RESPONSE

			print FILE "<p><b>Example response</b><pre><code>";
			if(defined $return) {
				my @rt = split(/ /, $return);
				if (defined $exampleResponse) {
					print FILE $exampleResponse;
				} elsif (exists($objectExamples{$rt[0]})) {
					print FILE $objectExamples{$rt[0]};
				} elsif ($rt[0] eq "int") {
					print FILE int(rand(100));
				} else {
					print FILE "{ ... TODO ... }";
				}
			} else {
				print FILE "null";
			}
			print FILE "</code></pre>";

			print FILE "<ul><li><a href=\"rpc-javadoc-howto.shtml#return-values\"><i>see Return values</i></a></li><li><a href=\"rpc-javadoc-howto.shtml#http-return-codes\"><i>see HTTP return codes</i></a></li></ul>";

			print FILE "</p></div></div></div>";

		}

	}

	print FILE qq{</div>};

	print FILE qq{
    <script type="text/javascript">
    	\$(document).ready(function() {
    		\$("#nav-documentation").addClass('active');
    	});
    </script>

    <!--#include virtual="footer.shtml" -->
    };

	close (FILE);
}

#END OF MAIN PROGRAM

#closing DIR
closedir(DIR);
exit 0;
