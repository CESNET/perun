# Perun services #

This module contains all scripts, which are used to manage end-services by Perun on requested destination. Please note, that Perun covers access management to such end-services and not their configuration as a whole.

Perun uses _push model_, so when configuration change occur in Perun, new config files are generated for affected services and sent to destination, where they are processed. Result of such operation is then reported back to Perun.

## Structure ##

* **gen/** - These perl scripts fetch data from Perun and generate new configuration files for each service and destination.
* **send/** - Scripts ensures transfer of configuration files to destination using SSH.
* **slave/** - These scripts process new files on destination machine and perform change itself.

Gen and send scripts are located on your Perun instance and are used by _perun-engine_ component. Slave scripts are then located on destination machines.

## Deployment on destinations ##

* Slave scripts can be automatically installed on destination machines as .deb or .rpm packages.
* You can generate packages from source by running ``make clean && make`` in _slave/_ folder.
* You must allow access on your destination from your Perun instance by SSH key as user with all necessary privileges (usually root).
* You can specify different user in Perun and it's good practice to restrict access on destination to run only command ``/opt/perun/bin/perun``.
* You can override default behavior of services on each destination by creating file ``/etc/perunv3.conf`` and put own config data here. As example, you can define white/black list of services, which you allow to configure on your destination. You can also set expected destination and facility names, so nobody can push own data to your destination from same Perun instance.

```
# syntax: (item1 item2 item3)
SERVICE_BLACKLIST=()	
SERVICE_WHITELIST=()
DNS_ALIAS_WHITELIST=( `hostname -f` )
FACILITY_WHITELIST=()
```