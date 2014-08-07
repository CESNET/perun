# Perun DB #

This module holds all DB related utilities, which are necessary to install and run Perun.

## Supported DB types ##

* Oracle (>11)
* Postgres (>9.1)

## Instalation ##

Theses instructions doesn't cover creating initial user and setting up other components like Apache and Tomcat. For complete installation instructions please refer to our wiki.

Empty schema of Perun's database can be created using _oracle.sql_ or _postgres.sql_ for each database type respectively. 

**Before running sql files edit username/schema/password in them to match yours.**

### Postgres ###

* Create user _perun_ and grant it access from localhost.
* Create schema _perun_ and grant all privileges to user _perun_.
* Install official extension _unaccent_ and add it to schema _perun_ (must be performed by user _perun_ with temporary elevated privileges).
* Log-in as user _perun_ and run _postgres.sql_ to create empty tables.

### Oracle ###

* Connect as user with elevated privileges (to create other users etc.) to your Oracle DB.
* Run _oracle.sql_ which will create user _perunv3_ and perform all necessary setup.

## Migrating between DB types ##

You can dump all data from one DB and put them to different DB type. **Content of audit_log table is exported only for entries from last 10 days, since it may contain a lot of entries.** You are free to modify export script, if you need all data.

### Oracle to Postgres ###

* You can use _export_oracle_to_postgres.pl_ perl script to move all your data. If table name is passed to script, only one table with such name is exported.
* Before running script, _user_ and _pwd_ properties in it must be manually set to access your Oracle DB.
* All dumped data are automatically converted to Postgres compliant insert statements and sequence counters are set to match actual DB data.
* Script uses file _table_order_ to ensure, that inserts are in right order.
* You will get _DB_data.sql_ file containing all data as inserts for Postgres.

### Postgres to Oracle ###

* There is no utility for that yet.