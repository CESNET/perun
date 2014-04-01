<?php

include ("conf.php");

header('Content-Type: text/html; charset=utf-8');

$applicationURLAttr = "urn:perun:vo:attribute-def:def:applicationURL";
// REQUEST URL & PARAMETERS
define("VO_URL", RPC_URL . "vosManager/getVos");

// CLIENT
$client = new PerunRpcClient(VO_URL);
$vos = $client -> retrieveData();

// ERROR WHILE DECODING RESPONSE
if($vos === false){
    print "Error while getting data.";
    exit;
}
// REQUEST URL & PARAMETERS
define("VO_ATTRS_URL", RPC_URL . "attributesManager/getAttribute");

// PRINTING DATA
virtual('header.shtml');

print '
            <div class="bigger-spaces">
              <h4>Access to CESNET Perun GUI, select authentication type:</h4>
              <a href="https://einfra.cesnet.cz/perun-gui/"><button class="btn btn-primary" type="button">eduID.cz</button></a>
              <a href="https://perun.metacentrum.cz/perun-gui-krb/"><button class="btn btn-primary" type="button">MetaCentrum login</button></a>
              <a href="https://perun.metacentrum.cz/perun-gui-cert/"><button class="btn btn-primary" type="button">IGTF Digital Certificate</button></a>
              <a href="https://perun.cesnet.cz/Shibboleth.sso/extidp?target=https://perun.cesnet.cz/perun-gui/"><button class="btn btn-primary" type="button">External IdP (Google, Facebook, ...)</button></a>
            </div>
            <br/>
            <div>
             <h4>Applications for projects/virtual organizations:</h4>
             <table class="table table-striped">
';
function toASCII( $str )
{
    return strtr(utf8_decode($str),
        utf8_decode(
        'ŠŒŽšœžŸ¥µÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖØÙÚÛÜÝßàáâãäåæçèéêëìíîïðñòóôõöøùúûüýÿ'),
        'SOZsozYYuAAAAAAACEEEEIIIIDNOOOOOOUUUUYsaaaaaaaceeeeiiiionoooooouuuuyy');
}

function cmp($a, $b)
{
    #return strcmp(iconv('UTF-8', 'ASCII//TRANSLIT', strtolower(trim($a->name))), iconv('UTF-8', 'ASCII//TRANSLIT', strtolower(trim($b->name))));
    return strcasecmp(toASCII($a->name), toASCII($b->name));
}
usort($vos, "cmp");


foreach ($vos as $vo) {
  // CLIENT
  $client = new PerunRpcClient(VO_ATTRS_URL);
  $client -> addParameter('vo', $vo->id);
  $client -> addParameter('attributeName', $applicationURLAttr);
  $attrs[$vo->shortName] = $client -> retrieveData();

  if (!empty($attrs[$vo->shortName]->value)) {
    print "<tr><td>" . $vo->name . " </td><td><a href=\"" . $attrs[$vo->shortName]->value . "\"><button class=\"btn\" type=\"button\"><i class=\"icon-chevron-right\"></i>&nbsp;Registration</button></a></td></tr>\n";
  }
  // ERROR WHILE DECODING RESPONSE
  if($attrs === false){
    print "Error while getting response.";
    exit;
  }
}

print '
	     </table>
             <h4>Reset user password in VO:</h4>
             <table class="table table-striped">
               <tr><td>MetaCentrum, Storage, ČEZtest</td><td><a href="https://perun.metacentrum.cz/perun-password-reset/?login-namespace=einfra"><button class="btn btn-primary" type="button"><i class="icon-chevron-right"></i>&nbsp;Password reset</button></a></dd>
               <tr><td>AUGER, VOCE, MPI, fedcloud.egi.eu</td><td><a href="https://perun.metacentrum.cz/perun-password-reset/?login-namespace=egi-ui"><button class="btn btn-primary" type="button"><i class="icon-chevron-right"></i>&nbsp;Password reset</button></a></dd>
             </table>

            </div>

';
virtual('footer.shtml');
