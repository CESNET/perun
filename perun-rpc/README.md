# Perun RPC (server) #

This module wraps others into a single java web application and represent single Perun instance. This module also provides REST API to work with Perun itslef. Application is expected to run inside Tomcat 7 container and receive all requests on AJP port. Perun rely on Apache web server for authentication of users.

You can find REST API documentation here:

* [How to use RPC interface](https://wiki.metacentrum.cz/wiki/Perun_remote_interfaces#RPC)
* [List of available methods (javadoc)](http://perun.cesnet.cz/javadoc-rpc/)

### Build and local run ###

> Please note, that to actually run Perun, you must:
>
> * Setup DB and create initial user entry including roles and authz data.
> * Have a bunch of configuration files with proper setting in /etc/perun/ folder
> * Setup some authentication in Apache server.
> * Pass authentication data from Apache to Tomcat (AJP port) so Perun can locate user based on that.
>
> For complete installation instructions, please refer to our wiki.

To build production version of Perun RPC from sources use Maven command in repository root folder:

``mvn clean package -pl perun-rpc -am -Pproduction -DskipTests``

You can then deploy ``perun-rpc.war`` into running Tomcat. You can also run Perun locally (e.g. for some tests). Just run Maven with jetty plugin in ``perun-rpc/`` folder:

``mvn jetty:run-exploded``

You still must setup Apache to provide authentication or modify ``jetty.xml`` to provide necessary data in HTTP request attributes.

### CLI tools ###

Perun can be also managed using CLI tools (Perl scripts). You can find them in ``/src/main/perl`` folder.

In order to use them, you must install following Perl packages (example for Debian):

```apt-get install libswitch-perl liblwp-authen-negotiate-perl libjson-any-perl libtext-asciitable-perl libterm-readkey-perl libwww-perl libcrypt-ssleay-perl libtext-unidecode-perl libdate-calc-perl```

Then you must setup environmental variables to locate your Perun instance:

```
# URL to your Perun instance, pick krb for Kerberos authz, cert for certificate and fed for IDP federation
# It all depends on your Apache setup
export PERUN_URL="https://[instance_url]/[krb/cert/fed]/rpc/" 

# Your login and password
export PERUN_USER="[login]/[password]" 

# Setup path for Perl to locat Perun modules
export PERL5LIB="[folder_with_cli_tools]" 

# Keep empty or setup to match REALM returned by Apache (e.g. for Kerberos)
export PERUN_RPC_TYPE="Perun RPC"
```

You can then test connection by listing VOs like: ``./listOfVos``