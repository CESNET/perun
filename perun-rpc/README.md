# Perun RPC (server) #

This module wraps others into a single web application and represents single Perun instance. Application contains server side RPC API which you can use to manage your Perun instance. For this purpose, Perl CLI tools are also provided in *perun-cli*. See on bottom how to make it work. GUI is provided by *perun-web-gui* module, which is not packaged inside *perun-rpc* and must be built and deployed separately into some web server (Apache).

Application is expected to run inside Tomcat 7 container and receive all requests on AJP port (8009). Perun rely on Apache web server for passing requests, authentication of users and setting up environment variables. User credentials are passed to Perun to perform authorization. If approved, required action is performed and response returned to user through Apache web server.

You can find full documentation of Perun RPC API [on our web](https://perun.cesnet.cz/web/rpc-javadoc-howto.shtml).

### Build and local run ###

> Please note, that to actually run Perun, you must:
>
> * Setup DB and create initial user entry including roles and authz data.
> * Have a bunch of configuration files with proper settings in /etc/perun/ folder
> * Setup some authentication in Apache.
> * Pass requests and authentication data from Apache to Tomcat (AJP port) so Perun can locate user based on that.
>
> For now NON of these steps are covered on public wiki/web.
>
> **You can use [Perun ansible](https://github.com/CESNET/perun-ansible) scripts to install instance of Perun with default configuration and initial user with basic auth.**

To build production version of Perun RPC from sources use Maven command in a project root folder:

```
mvn clean install -pl perun-rpc -am -Dproduction -DskipTests
```

You can then deploy ``perun-rpc/target/perun-rpc.war`` into running Tomcat and it will be running under *http(s)://localhost:8009/perun-rpc* path. 

You must setup Apache to provide authentication or modify it to provide necessary data in HTTP headers. Otherwise all requests to the app will end with wrong authentication.

Also you must redirect all requests to the right URL (provided by tomcat), since GUI and CLI tools expect to find Perun on *http(s)://[hostname]/[krb/cert/fed]/rpc/[rest_of_request_path+params]* URL.

You can also run Perun locally (e.g. for some tests). Just run Maven with tomcat plugin in ``perun-rpc/`` folder:

```
# in memory version
mvn tomcat7:run-war

# against real DB with config in /etc/perun/
mvn tomcat7:run-war -Ddevel 
```

### CLI tools ###

Perun can be managed using CLI tools (Perl scripts). You can find them in *perun-cli* module.

In order to use them, you must install following Perl packages (example for Debian):

```bash
apt-get install libswitch-perl liblwp-authen-negotiate-perl libjson-any-perl libtext-asciitable-perl libterm-readkey-perl libwww-perl libcrypt-ssleay-perl libtext-unidecode-perl libdate-calc-perl
```

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
