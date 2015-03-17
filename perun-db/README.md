# Perun DB #

This module holds all DB related utilities, which are necessary to install and run Perun.

### Supported DB types ###

* Oracle (>11)
* Postgres (>9.1)

> Data in Perun is stored using UTF-8 charset. When using Oracle DB, some columns are limited to 4000 of ASCII characters (1000 with full UTF-8 set).

## Installation ##

> Theses instructions doesn't cover creation of initial user (perun admin) and setting up other components like Apache and Tomcat. For complete installation instructions please refer to our wiki.

Empty schema of Perun's database can be created using _oracle.sql_ or _postgres.sql_ for each database type respectively. **Before running sql files please fill or edit username/schema/password in them to match your setting.**

### Postgres ###

* Create user _perun_ and grant it access from localhost.
* Create schema _perun_ and grant all privileges to user _perun_.
* Install official extension _unaccent_ and add it to schema _perun_ (must be performed by user _perun_ with temporary elevated privileges).
* Log-in as user _perun_ and run _postgres.sql_ to create empty tables.

### Oracle ###

* Connect as user with elevated privileges (to create other users etc.) to your Oracle DB.
* Run _oracle.sql_ which will create user _perunv3_ and perform all necessary setup.

## Migrating data between DBs ##

You can dump all data from one DB and put them to different DB using included export scripts. 

* Run ```./script_name.pl -h``` to see all required parameters and examples of usage.
* Export scripts require perl libraries to be installed, especially DBI (default + Oracle).
* Export scripts are using by default _table_order_ file with list of tables to export in correct order (for future import).

**Content of table: auditer_log is exported only for entries from last 10 days, since it may contain a lot of entries.**

**Content of tables: tasks and tasks_results is not exported.** They contain data about current state of services propagation and you might get stuck with _processing_ tasks, which are not actually running. Manual forced propagation of all services to all facilities is recommended after full migration.

**Content of pn_receiver is not exported** This is in order to prevent notification module from sending any message. You can add table to the list manually.

You can manually add missing tables names in _table_order_ file and modify script to get all data from auditer_log table.

### Oracle to Oracle ###

* You can use _export_oracle_to_oracle.pl_ perl script to get all your data.
* All dumped data are kept in Oracle compliant insert statements and sequence counters are set to match actual DB data.

### Oracle to Postgres ###

* You can use _export_oracle_to_postgres.pl_ perl script to get all your data. 
* All dumped data are automatically converted to Postgres compliant insert statements and sequence counters are set to match actual DB data.

### Postgres to Oracle ###

* There is no utility for that yet.

### Postgres to Postgres ###

* There is no utility for that yet. You can use native Postgres export/import features.