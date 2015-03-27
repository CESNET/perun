# Perun PHP binding #

This module contains example PHP binding (for client or middleware) to Perun API. Example expect, that you connect to your Perun instance using plain login/password or Kerberos. You can make it work using personal certificate or Shibboleth IDP on your own. Please note, that this example uses fixed credentials. If you are about to use it for some kind of client app with user authentication, you must add current user credentials to the request.

If you are interested, have a look at more complex PHP project https://github.com/ivan-novakov/php-perun-api which aims to provide REST API on top of Perun RPC.

### How to make it work ###

* Download sources
* Install web server and PHP

```bash
sudo apt-get install apache2 libapache2-mod-php5 php5-curl
```

* Allow access to sources in your web server, e.g. like: http://localhost/php
* Enable support for PHP scripts including curl extension.

```
Options Indexes MultiViews Includes ExecCGI FollowSymLinks
AddHandler cgi-script .php
```

* Edit properties in **PerunRpcClient.php** to match your environment:

```
// URL to Perun RPC
const RPC_URL = "https://perun.cesnet.cz/krb/rpc/json/";

// Choose the authentication method:

// Username
const USER = "perun-client";
// Password based authentication, left empty if you are using Kerberos authz
const PASSSWORD = "";

// Kerberos based authentication, left empty if there is no Kerberos support
const KERBEROS_CC = "/tmp/krb5cc_perun-client";
```

You can optionally use plain login@REALM and password for Kerberos too, but it's not secured solution.

### More ###

More information about this library including RPC API documentation can be found here: https://wiki.metacentrum.cz/wiki/Perun_remote_interfaces#PHP_Library
