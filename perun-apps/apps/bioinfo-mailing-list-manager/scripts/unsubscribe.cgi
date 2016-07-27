#!/usr/bin/perl

#enviromental variables
$ENV{KRB5CCNAME}='FILE:/tmp/krb5cc_perun-tools-webserver';
$ENV{PERUN_URL}='https://perun.metacentrum.cz/krb/rpc/';

use CGI qw(:standard);
use strict;
use warnings;
use Crypt::OpenSSL::AES;
use Crypt::CBC;
use MIME::Base64::URLSafe;
use JSON;
use lib '/opt/perun-cli/lib';
use Perun::Agent;

#header
my $q = CGI->new;
print $q->header(-type=>'text/javascript', -charset=>'UTF-8');

#used variables
my ($email, $timestamp);
my $key = "PerunRulezz";
my $secret = param("secret");
my $callback = param("callback");
my $mailingList = 'bioinfo-cz@elixir-czech.cz';

#if callback not exists in get parameter
if(!defined($callback)) {
 my $errorId = "1";
  my $message = "Callback must be defined!";
  my $name = "CallbackNotDefinedException";
  my %exception_hash = ('errorId'=>$errorId, 'message'=>$message, 'name'=>$name);
  my $json = encode_json \%exception_hash;
  print "null($json);";
  exit 0; 
}

#if secret not exists in get parameter
if(!defined($secret)) {
  my $errorId = "2";
  my $message = "String with secret is not defined!";
  my $name = "SecretNotDefinedException";
  my %exception_hash = ('errorId'=>$errorId, 'message'=>$message, 'name'=>$name);
  my $json = encode_json \%exception_hash;
  print "$callback($json);";
  exit 0;
}

#decrypt secret
my $cipher = Crypt::CBC->new(
              -key       => $key,
              -keylength => '256',
              -cipher    => "Crypt::OpenSSL::AES"
);

my $decodedEncryptedText;
eval {$decodedEncryptedText = urlsafe_b64decode($secret);}; 
if ($@) {
  my $message = "Problem with decoding secret: $@";
  my $errorId = "3";
  my $name = "DecodingFailedException";
  my %exception_hash = ('errorId'=>$errorId, 'message'=>$message, 'name'=>$name);
  my $json = encode_json \%exception_hash;
  print "$callback($json);";
  exit 0;
}

my $decryptedText;
eval {$decryptedText = $cipher->decrypt($decodedEncryptedText)};
if ($@) {
  my $message = "Problem with decrypting secret: $@";
  my $errorId = "4";
  my $name = "DecryptingFailedException";
  my %exception_hash = ('errorId'=>$errorId, 'message'=>$message, 'name'=>$name);
  my $json = encode_json \%exception_hash;
  print "$callback($json);";
  exit 0;
}

#test if decrypted secret is in correct format
my $parametersRegExp = '^([-_A-Za-z0-9+]+(\.[-_A-Za-z0-9]+)*@[-A-Za-z0-9]+(\.[-A-Za-z0-9]+)*(\.[A-Za-z]{2,})) bioinfo-cz@elixir-czech.cz ([0-9]+)$';

if($decryptedText !~ /$parametersRegExp/) {
  my $errorId = "5";
  my $message = "Decrepted secret is not in correct format!";
  my $name = "DecreptedSecretIsNotInCorrectFormatException";
  my %exception_hash = ('errorId'=>$errorId, 'message'=>$message, 'name'=>$name);
  my $json = encode_json \%exception_hash;
  print "$callback($json);";
  exit 0;
}

#get information from decrypted secret
my @listOfInformations = split(' ', $decryptedText);
$email = $listOfInformations[0];
$timestamp = $listOfInformations[2];

#test if timestamp is not older than 1 day
my $maxAgeOfTimestampInSec = 24 * 60 * 60; #sec
if (time - $timestamp > $maxAgeOfTimestampInSec) {
  my $errorId = "6";
  my $message = "Timestamp is older than 1 day!";
  my $name = "TimestampExceededMaxAgeException";
  my %exception_hash = ('errorId'=>$errorId, 'message'=>$message, 'name'=>$name);
  my $json = encode_json \%exception_hash;
  print "$callback($json);";
  exit 0;
}

#------------------------ PERUN CALLS ----------------------------------
eval {
  #get perun agent
  my $agent = Perun::Agent->new();

  #get vo agent and voId by vo shortName
  my $vosAgent = $agent->getVosAgent;
  my $voName = "elixir-project";
  my $vo = $vosAgent->getVoByShortName(shortName => $voName);

  #get facility by name
  my $facilityAgent = $agent->getFacilitiesAgent;
  my $facilityName = "projects.cesnet.cz";
  my $facility = $facilityAgent->getFacilityByName(name => $facilityName);

  #get resource by facility and name
  my $resourcesAgent = $agent->getResourcesAgent;
  my $resourceName = $mailingList;
  my $resource = $resourcesAgent->getResourceByName(vo => $vo->getId(), facility => $facility->getId(), name => $resourceName);

  #get user by email
  my $attrEmailName = "urn:perun:user:attribute-def:def:preferredMail";
  my %attrs_hash = ($attrEmailName=>$email);
  my $searcherAgent = $agent->getSearcherAgent;
  my @users = $searcherAgent->getUsers(attributesWithSearchingValues => \%attrs_hash);

  unless(@users) {
    my $errorId = "7";
    my $message = "User with this preferred email: $email not found in Perun.!";
    my $name = "UserNotFoundByEmailException";                                              
    my %exception_hash = ('errorId'=>$errorId, 'message'=>$message, 'name'=>$name);
    my $json = encode_json \%exception_hash;
    print "$callback($json);";
    exit 0; 
  }

  if (scalar(@users)>1) {
    my $errorId = "8";
    my $message = "Found more than 1 user with this preferred email: $email!";
    my $name = "ConsistencyErrorException";                                              
    my %exception_hash = ('errorId'=>$errorId, 'message'=>$message, 'name'=>$name);
    my $json = encode_json \%exception_hash;
    print "$callback($json);";
    exit 0;  
  }

  my $user = $users[0];

  #get member from user
  my $membersAgent = $agent->getMembersAgent;
  my $member = $membersAgent->getMemberByUser(vo => $vo->getId(), user => $user->getId());

  #get group by Id (where user must be added)
  my $groupsAgent = $agent->getGroupsAgent;
  my $groupId = "10815";

  $groupsAgent->removeMember(group=>$groupId, member=>$member->getId());
};

if($@) {
  my $perunException = $@;
  my $name = "InternalErrorException";
  my $message = "Uknown error";
  my $errorId = 9;
  if ($perunException =~ /Authentication failed/) {
    $message = "Authentication Failed";
    $errorId = 10;
  } elsif ($perunException->can("getErrorId")) {
    $name = $perunException->getName();
    $message = $perunException->getErrorInfo();
    $errorId = $perunException->getErrorId();
  }                                                                                           
  my %exception_hash = ('errorId'=>$errorId, 'message'=>$message, 'name'=>$name);
  my $json = encode_json \%exception_hash;
  print "$callback($json);";
  exit 0;
}

#send email with informations
open(MAIL, "|/usr/sbin/sendmail -t");
#Email header
my $from = 'perun@cesnet.cz';
my $replyTo = 'support@elixir-czech.cz';
print MAIL "To: $email\n";
print MAIL "Reply-To: $replyTo\n";
print MAIL "From: $from\n";
print MAIL "Subject: Mailing list BIOINFO--ELIXIR CZ\n\n";
#Email Body
print MAIL qq{Dear user,

You have been unsubscribed as a receiver of the information from bioinfo group mailing list of the Czech ELIXIR node.


ELIXIR Project Czech Node <http://www.elixir-czech.org> support\@elixir-czech.org

---------------------------------------------------------------------------
If you want to receive this newsletter again, subscribe using
https://einfra.cesnet.cz/a/bioinfo-mailing-list-manager/non/subscribe.html
};

#Send Mail
if(!close(MAIL)) {
  my $errorId = "20";
  my $message = "Sendmail was not able to send email!";
  my $name = "EmailNotSendException";
  my %exception_hash = ('errorId'=>$errorId, 'message'=>$message, 'name'=>$name);
  my $json = encode_json \%exception_hash;
  print "null($json);";
  exit 0; 
}


print "$callback({});";
exit 0;

