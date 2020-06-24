# Perun DB #

This module holds all DB related utilities, which are necessary to install and run Perun.

### Supported DB types ###

* ~~Oracle (>11)~~ support for Oracle was dropped in 3.10.0
* Postgres (>9.1)

> ~~Data in Perun is stored using UTF-8 charset. When using Oracle DB, some columns are limited to 4000 of ASCII characters (1000 with full UTF-8 set).~~

## Installation ##

> Theses instructions doesn't cover creation of initial user (perun admin) and setting up other components like Apache and Tomcat. For complete installation instructions please refer to our wiki.

Empty schema of Perun's database can be created using _postgres.sql_. **Before running sql files please fill or edit username/schema/password in them to match your setting.**

* Create user _perun_ and grant it access from localhost.
* Create schema _perun_ and grant all privileges to user _perun_.
* Install official extension _unaccent_ and add it to schema _perun_ (must be performed by user _perun_ with temporary elevated privileges).
* Log-in as user _perun_ and run _postgres.sql_ to create empty tables.
