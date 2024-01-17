#!/usr/bin/php
<?php

use OpenAPI\Client\ApiException;
use OpenAPI\Client\Api\UsersManagerApi;

require __DIR__ . '/vendor/autoload.php';

$perun_user = getenv('PERUN_USER');
$options = getopt("", array("PERUN_USER:", "user:"));
if (array_key_exists("PERUN_USER", $options)) {
    $perun_user = $options["PERUN_USER"];
}
$sa = explode('/', $perun_user, 2);

$usersManager = new UsersManagerApi();
$config = $usersManager->getConfig();
$config->setAccessToken(null);
$config->setUsername($sa[0]);
$config->setPassword($sa[1]);
$config->setHost("https://perun.cesnet.cz/krb/rpc");
$config->setUserAgent("Perun OpenAPI PHP CLI");
$config->setDebug(false);
try {
    $userId = $options["user"];
    $user = $usersManager->getUserById($userId);
    print $user;
} catch (ApiException $e) {
    print $e;
}
?>
