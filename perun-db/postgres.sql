-- database version 3.1.34 (don't forget to update insert statement at the end of file)

-- VOS - virtual organizations
create table "vos" (
	id integer not null,
	name varchar(128) not null,   -- full name of VO
	short_name varchar(32) not null, -- commonly used name
	created_at timestamp default now() not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now() not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer
);

-- USERS - information about user as real person
create table "users" (
	id integer not null,
	first_name varchar(64),   -- christening name
	last_name varchar(64),    -- family name
	middle_name varchar(64),   -- second name
	title_before varchar(40),  -- academic degree used before name
	title_after varchar(40),   -- academic degree used after name
	created_at timestamp  default now() not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now() not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	service_acc char(1) default '0' not null, --is it service account?
	sponsored_acc char(1) default '0' not null, --is it sponsored account?
	created_by_uid integer,
	modified_by_uid integer
);

-- OWNERS - owners of resources and devices
create table "owners" (
	id integer not null,
	name varchar(128) not null, --name of owner
	contact varchar(100),       --contact email or phone
	created_at timestamp default now() not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now() not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	type varchar(128) not null, --type of owner (for example IdP)
	created_by_uid integer,
	modified_by_uid integer
);

-- CABINET_CATEGORIES - possible categories of publications
create table "cabinet_categories" (
	id integer not null,
	name varchar(128) not null,  --name of category
	rank numeric(38,1) not null,  --coefficient for evaluation of publication in scope of this category
	created_by_uid integer,
	modified_by_uid integer
);

-- CABINET_PUBLICATION_SYSTEMS - external publication systems. Contains information which allowes searching
create table "cabinet_publication_systems" (
	id integer not null,
	friendlyName varchar(128) not null, --name of publication system
	url varchar(128) not null, --address for searching at external system
	username varchar(64),  --logname
	password varchar(64),  -- and password for connection to external system
	loginNamespace varchar(128) not null, --namespace used for username
	type varchar(128) not null,  --name of class of parser for received data (for example cz.metacentrum.perun.cabinet.strategy.impl.MUStrategy) *)
	created_by_uid integer,
	modified_by_uid integer
);
--*) it have to include entry about internal publication system to create publication directly in Perun DB

-- CABINET_PUBLICATIONS - all publications stored in Perun DB
create table "cabinet_publications" (
	id integer not null,
	externalId integer not null, --identifier at externa pub. system
	publicationSystemId integer not null, --identifier of external pub. system (cabinet_publication_systems.id) *)
	title varchar(1024) not null,
	year integer not null, --short title of publication
	main varchar(4000), --full cite of publication
	isbn varchar(32),
	categoryId integer not null, --identifier of category (cabinet_categories.id)
	createdBy varchar(1024) default user not null,
	createdDate timestamp not null,
	rank numeric (38,1) default 0 not null,
	doi varchar(256),
	locked varchar(1) default 0 not null,
	created_by_uid integer,
	modified_by_uid integer
);
--*) if publication is created directly in Perun externalId=id and publicationSystemId is identifier of internal system

-- CABINET_AUTHORSHIPS - relation of user to publication (author,co-author)
create table "cabinet_authorships" (
	id integer not null,
	publicationId integer not null, --identifier of publication (cabinet_publications.id)
	userId integer not null, -- identifier of user (users.id)
	createdBy varchar(1024) default user not null,
	createdDate timestamp not null,
	created_by_uid integer,
	modified_by_uid integer
);

-- CABINET THANKS - list of institutions which are acnowledged at publication
create table "cabinet_thanks" (
	id integer not null,
	publicationid integer not null, --identifier of publication (cabinet_publications.id)
	ownerId integer not null, --identifier of owner of used ources and devices (owners.id) - MetaCenter,CESNET...
	createdBy varchar(1024) default user not null,
	createdDate timestamp not null,
	created_by_uid integer,
	modified_by_uid integer
);

-- FACILITIES - sources, devices - includes clusters,hosts,storages...
create table "facilities" (
	id integer not null,
	name varchar(128) not null, --unique name of facility
	dsc varchar(1024),
	created_at timestamp default now() not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now() not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer
);

-- RESOURCES - facility assigned to VO
create table "resources" (
	id integer not null,
	facility_id integer not null, --facility identifier (facility.id)
	name varchar(128) not null,   --name of resource
	dsc varchar(1024),            --purpose and description
	created_at timestamp default now() not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now() not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	vo_id integer not null,   --identifier of VO (vos.id)
	created_by_uid integer,
	modified_by_uid integer
);

-- DESTINATIONS - targets of services
create table "destinations" (
	id integer not null,
	destination varchar(1024) not null, --value of destination (hostname,email,URL...)
	type varchar(20) not null, --type (host,URL...)
	created_at timestamp default now() not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now() not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer
);

-- FACILITY_OWNERS - one or more institutions which own the facility
create table "facility_owners" (
	facility_id integer not null, --identifier of facility (facilities.id)
	owner_id integer not null,   --identifier of owner (owners.id)
	created_at timestamp default now() not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now() not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer
);

-- FACILITIES_CONTACTS - all optional contacts for facility (owners, users or groups)
create table "facility_contacts" (
	name varchar(128) not null, -- similar to tag of group of contacts
	facility_id integer not null, --facility identifier
	owner_id integer, --owner identifier
	user_id integer, --user identifier
	group_id integer -- group identifier
);

-- GROUPS - groups of users
create table "groups" (
	id integer not null,
	name text not null,         --group name
	dsc varchar(1024),          --purpose and description
	vo_id integer not null,     --identifier of VO (vos.id)
	created_at timestamp default now() not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now() not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	parent_group_id integer,    --in case of subgroup identifier of parent group (groups.id)
	created_by_uid integer,
	modified_by_uid integer
);

-- MEMBERS - members of VO
create table "members" (
	id integer not null,
	user_id integer not null,  --user's identifier (users.id)
	vo_id integer not null,    --identifier of VO (vos.id)
	created_at timestamp default now() not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now() not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null, --status of membership
	created_by_uid integer,
	modified_by_uid integer
);

-- ROUTING_RULES - rules for assigning event to engine
create table "routing_rules" (
	id integer not null,
	routing_rule varchar(512) not null, --string for matching
	created_at timestamp default now() not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now() not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer
);

-- DISPATCHER_SETTINGS - settings of daemon-dispatcher of services
create table "dispatcher_settings" (
	ip_address varchar(40) not null, --IP address
	port integer not null,           -- port
	last_check_in timestamp default (now()), --time of last activation
	created_at timestamp default now() not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now() not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer
);

-- ENGINES - information for daemons controles services planning
create table "engines" (
	id integer not null, --identifier of daemon
	ip_address varchar(40) not null, --IP address
	port integer not null, --port
	last_check_in timestamp default (now()), --time of last activation
	created_at timestamp default now() not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now() not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer
);

-- ENGINE_ROUTING_RULE - relation between engines and rules
create table "engine_routing_rule" (
	engine_id integer not null,   --engine identifier (engines.id)
	routing_rule_id integer not null, --identifier of rule (routing_rules.id)
	created_at timestamp default now() not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now() not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer
);

-- PROCESSING_RULES - rules for assigning processing services to events 
create table "processing_rules" (
	id integer not null,
	processing_rule varchar(1024) not null, --string for matching
	created_at timestamp default now() not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now() not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer
);

-- ROLES - possible user's rolles - controle access of users to data in DB 
create table "roles" (
	id integer not null,
	name varchar (32) not null,  --name of role
	created_by_uid integer,
	modified_by_uid integer
);

-- ACTION_TYPES - possible actions for attributes
create table action_types (
	id integer not null,
	action_type varchar(20) not null,  --type of action (read/write...)
	description varchar(1024)          --description
);

-- MEMBERSHIP_TYPES - possible types of membership in group
create table membership_types (
	id integer not null,
	membership_type varchar(10) not null,  --type of memberships (DIRECT/INDIRECT...)
	description varchar(1024)              --description
);

-- ATTR_NAMES - list of possible attributes
create table "attr_names" (
	id integer not null,
	default_attr_id integer,  --identifier of attribute which can be substituted by this (by default)
	attr_name varchar(384) not null,  --full name of attribute
	friendly_name varchar(128) not null, --short name of attribute
	namespace varchar(256) not null,  --access of attribute to the entity
	type varchar(256) not null,       --type o0f attribute data (strig,number,array...)
	dsc varchar(1024),                --purpose,description
	created_at timestamp default now() not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now() not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer,
	display_name varchar(256)  --name of attr. displayed at GUI
);

-- ATTRIBUTES_AUTHZ - controles permissions for access to attributes 
create table attributes_authz (
	attr_id integer not null,  --identifier of attribute (attr_names.id)
	role_id integer not null,  --identifier of role (roles.id)
	action_type_id integer not null  --identifier of action (action_types.id)
);

-- AUTHZ - assigned roles to users/groups/VOs/other entities...
create table "authz" (
	user_id integer,          --identifier of user
	role_id integer not null, --identifier of role
	vo_id integer,            --identifier of VO
	facility_id integer,      --identifier of facility
	member_id integer,        --identifier of member
	group_id integer,         --identifier of group
	service_id integer,       --identifier of service
	resource_id integer,      --identifier of resource
	service_principal_id integer,  --identifier service principal
	sponsored_user_id integer, --identifier of sponsored user
	created_by_uid integer,
	modified_by_uid integer,
	authorized_group_id integer, --identifier of whole authorized group
	security_team_id integer	--identifier of security team
);

-- HOSTS - detail information about hosts and cluster nodes
create table "hosts" (
	id integer not null,
	hostname varchar(128) not null,  --full name of machine
	facility_id integer not null,    --identifier of facility containing the host (facilities.id)
	dsc varchar(1024),  --description
	created_at timestamp default now() not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now() not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer
);

-- HOST_ATTR_VALUES - values of attributes assigned to hosts
create table "host_attr_values" (
	host_id integer not null,  --identifier of host (hosts.id)
	attr_id integer not null,  --identifier of attributes (attr_names.id)
	attr_value varchar(4000),  --value of attribute
	created_at timestamp default now() not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now() not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	attr_value_text text,   --value of attribute if it is very long text
	created_by_uid integer,
	modified_by_uid integer
);

-- AUDITER_CONSUMERS - registers recently processed events
create table "auditer_consumers" (
	id integer not null,
	name varchar(256) not null,
	last_processed_id integer,
	created_at timestamp default now() not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now() not null,
	modified_by varchar(1024) default user not null,
	created_by_uid integer,
	modified_by_uid integer
);

-- SERVICES - provided services, their atomic form
create table "services" (
	id integer not null,
	name varchar(128) not null,    --name of service
	created_at timestamp default now() not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now() not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer
);

-- SERVICE_PROCESSING_RULE - relation between services and processing rules
create table "service_processing_rule" (
	service_id integer not null,          --identifier of service (services.id)
	processing_rule_id integer not null,  --identifier of processing rule (processing_rules.id)
	created_at timestamp default now() not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now() not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer
);

-- SERVICE_REQUIRED_ATTRS - list of attributes required by the service
create table "service_required_attrs" (
	service_id integer not null,   --identifier of service (services.id)
	attr_id integer not null,      --identifier of attribute (attr_names.id)
	created_at timestamp default now() not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now() not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer
);

-- SERVICE_USER_USERS - relation between service-users and real users
create table specific_user_users (
	user_id integer not null,          --identifier of real user (users.id)
	specific_user_id integer not null,  --identifier of service user (users.id)
	created_by_uid integer,
	modified_by_uid integer,
	modified_at timestamp default now() not null,
	type varchar(20) default 'service' not null,
	status char(1) default '0' not null --is it service user?
);

-- EXEC_SERVICES - controlling data for service
create table "exec_services" (
	id integer not null,
	service_id integer not null,    --identifier of service (services.id)
	default_delay integer not null, --delay to repeating of service in case of error
	enabled char(1) not null,       --service is active/inactive
	default_recurrence integer not null,  --number of repeating in case of error
	script varchar(256) not null,   --name of executable service script
	type varchar(10) not null,      --part of service (SEND/GENERATE)
	created_at timestamp default now() not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now() not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer
);

-- SERVICE_DENIALS - services excluded from ussage
create table "service_denials" (
	id integer not null,
	exec_service_id integer not null,  --identifier of service (exec_services.id)
	facility_id integer,               --identifier of facility (facilities.id)
	destination_id integer,            --identifier of destination (destinations.id) if service is not excluded on whole facility
	created_at timestamp default now() not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now() not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer
);

-- SERVICE_DEPENDENCIES - dependency of executing one service on finishing of other service
create table "service_dependencies" (
	exec_service_id integer not null,  --identifier of service which must be finished finished first (exec_services.id)
	dependency_id integer not null,    --identifier of service which can be executed after finishing exec_service_id (exec_services.id)
	created_at timestamp default now() not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now() not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer,
	type varchar(16) default 'SERVICE' not null --type of dependency (SERVICE/DESTINATION)
);

-- RESOURCE_SERVICES - services assigned to resource
create table "resource_services" (
	service_id integer not null,   --identifier of service (services.id)
	resource_id integer not null,  --identifier of resource (resources.id)
	created_at timestamp default now() not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now() not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer
);

-- APPLICATION - registration data
create table "application" (
	id integer not null,
	vo_id integer not null,  --identifier of VO (vos.id)
	user_id integer,         --identifier of user (users.id)
	apptype varchar(128) not null,  --type of application (initial/extension)
	extSourceName varchar(4000),  --name of external source of users
	extSourceType varchar(4000),  --type of external source of users (federation...)
	fed_info varchar(4000),       --data from federation if any
	state varchar(128),           --state of application (new/verified/approveed/rejected)
	extSourceLoa integer,  --level of assurance of user by external source
	group_id integer,      --identifier of group (groups.id) if application is for group
	created_at timestamp default now() not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now() not null,
	modified_by varchar(1024) default user not null,
	created_by_uid integer,
	modified_by_uid integer
);

-- APPLICATION_FORM - form for application into VO or group
create table "application_form" (
	id integer not null,
	vo_id integer not null,     --identifier of VO (vos.id)
	automatic_approval char(1), --approval of application is automatic
	automatic_approval_extension char(1), --approval of extension is automatic
	module_name varchar(128),  --name of module which processes application
	group_id integer,          --identifier of group (groups.id) if application is for group
	created_by_uid integer,
	modified_by_uid integer
);

-- APPLICATION_FORM_ITEMS - items of application form
create table "application_form_items" (
	id integer not null,
	form_id integer not null,  --identifier of form (application_form.id)
	ordnum integer not null,   --order of item
	shortname varchar(128) not null,  --name of item
	required char(1),          --value for item is mandatory
	type varchar(128),         --type of item
	fed_attr varchar(128),     --copied from federation attribute
	dst_attr varchar(384),     --saved to attribute
	regex varchar(4000),       --regular expression for checking of value
	created_by_uid integer,
	modified_by_uid integer
);

-- APPLICATION_FORM_ITEM_APPTYPES - possible types of app. form items
create table "application_form_item_apptypes" (
	item_id integer not null,  --identifier of form item (application_form_items.id)
	apptype varchar(128) not null,  --type of item
	created_by_uid integer,
	modified_by_uid integer
);

-- APPLICATION_FORM_ITEM_TEXTS - texts displayed with the items at app. form
create table "application_form_item_texts" (
	item_id integer not null,     --identifier of form item (application_form_items.id)
	locale varchar(128) not null, --language for application
	label varchar(4000),          --label of item on app. form
	options varchar(4000),        --options for items with menu
	help varchar(4000),           --text of help
	error_message varchar(4000),  --text of error message
	created_by_uid integer,
	modified_by_uid integer
);

-- APPLICATION_DATA - values of data entered by application form
create table "application_data" (
	id integer not null,
	app_id integer not null,  --identifier of application (application.id)
	item_id integer,          --identifier of item (application_form_items.id)
	shortname varchar(128),   --name of item
	value varchar(4000),      --value of item
	assurance_level varchar(128), --level of assurance of item of newly registered user
	created_by_uid integer,
	modified_by_uid integer
);

-- APPLICATION_MAILS - notification mails sent together with application
create table "application_mails" (
	id integer not null,
	form_id integer not null,       --identifier of form (application_form.id)
	app_type varchar(30) not null,  --application type (initial/extension)
	mail_type varchar(30) not null, --type of mail (user/administrator)
	send varchar(1) not null,       --sent (Y/N)
	created_by_uid integer,
	modified_by_uid integer
);

-- APPLICATION_MAIL_TEXTS - texts of notification mails
create table "application_mail_texts" (
	mail_id integer not null,     --identifier of mail (application_mails.id)
	locale varchar(10) not null,  --language for texts
	subject varchar(1024),        --subject of mail
	text varchar(4000),           --text of mail
	created_by_uid integer,
	modified_by_uid integer
);

-- APPLICATION_RESERVED_LOGINS - lognames reserved for new users who has not been saved at users table yet                       
create table "application_reserved_logins" (
	login varchar(256) not null,        --logname
	namespace varchar(30) not null,     --namespace where logname is reserved
	app_id integer not null,            --identifier of application (application.id)
	created_by varchar(1024) default user not null,
	created_at timestamp default now() not null,
	created_by_uid integer,
	modified_by_uid integer
);

-- FACILITY_SERVICE_DESTINATIONS - destinations of services assigned to the facility
create table "facility_service_destinations" (
	service_id integer not null,   --identifier of service (services.id)
	facility_id integer not null,  --identifier of facility (facilities.id)
	destination_id integer not null, --identifier of destination (destinations.id)
	created_at timestamp default now() not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now() not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer,
	propagation_type varchar(10) default 'PARALLEL'
);

-- ENTITYLESS_ATTR_VALUES - value of attributes which are not assigned to any entity
create table "entityless_attr_values" (
	subject varchar(256) not null,  --indicator of subject assigned with attribute
	attr_id integer not null,       --identifier of attribute (attr_names.id)
	attr_value varchar(4000),       --attribute value
	created_at timestamp default now() not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now() not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	attr_value_text text,           --attribute value in case it is very long text
	created_by_uid integer,
	modified_by_uid integer
);

-- FACILITY_ATTR_VALUES - attribute values assigned to facility
create table "facility_attr_values" (
	facility_id integer not null,   --identifier of facility (facilities.id)
	attr_id integer not null,       --identifier of attribute (attr_names.id)
	attr_value varchar(4000),       --attribute value
	created_at timestamp default now() not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now() not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	attr_value_text text,           --attribute value in case it is very long text
	created_by_uid integer,
	modified_by_uid integer
);

-- GROUP_ATTR_VALUES - attribute values assigned to groups
create table "group_attr_values" (
	group_id integer not null,     --identifier of group (groups.id)
	attr_id integer not null,      --identifier of attribute (attr_names.id)
	attr_value varchar(4000),      --attribute value
	created_at timestamp default now() not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now() not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	attr_value_text text,          --attribute value in case it is very long text
	created_by_uid integer,
	modified_by_uid integer
);

-- RESOURCE_ATTR_VALUES - attribute values assigned to resources
create table "resource_attr_values" (
	resource_id integer not null,   --identifier of resource (resources.id)
	attr_id integer not null,       --identifier of attribute (attr_names.id)
	attr_value varchar(4000),       --attribute value
	created_at timestamp default now() not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now() not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	attr_value_text text,           --attribute value in case it is very long text
	created_by_uid integer,
	modified_by_uid integer
);

-- GROUP_RESOURCE_ATTR_VALUES - attribute values assigned to groups and resources
create table "group_resource_attr_values" (
	group_id integer not null,     --identifier of group (groups.id)
	resource_id integer not null,  --identifier of resource (resources.id)
	attr_id integer not null,      --identifier of attribute (attr_names.id)
	attr_value varchar(4000),      --attribute value
	created_at timestamp default now() not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now() not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	attr_value_text text,          --attribute value in case it is very long text
	created_by_uid integer,
	modified_by_uid integer
);

-- GROUPS_MEMBERS - members of groups
create table "groups_members" (
	group_id integer not null,   --identifier of group (groups.id)
	member_id integer not null,  --identifier of member (members.id)
	created_at timestamp default now() not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now() not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer,
	membership_type integer not null,  --identifier of membership type (membersip_types.id)
	source_group_id integer not null   --identifier of parent group (groups.id) if any
);

-- GROUPS_RESOURCES - groups assigned to resource
create table "groups_resources" (
	group_id integer not null,     --identifier of group (groups.id)
	resource_id integer not null,  --identifier of resource (resources.id)
	created_at timestamp default now() not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now() not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer
);

-- MEMBER_ATTR_VALUES - values of attributes assigned to members
create table "member_attr_values" (
	member_id integer not null,   --identifier of member (members.id)
	attr_id integer not null,     --identifier of attribute (attr_names.id)
	attr_value varchar(4000),     --attribute value
	created_at timestamp default now() not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now() not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	attr_value_text text,         --attribute value in case it is very long text
	created_by_uid integer,
	modified_by_uid integer
);

-- MEMBER_GROUP_ATTR_VALUES - values of attributes assigned to members in groups
create table "member_group_attr_values" (
	member_id integer not null,   --identifier of member (members.id)
	group_id integer not null, --identifier of group (groups.id)
	attr_id integer not null,     --identifier of attribute (attr_names.id)
	attr_value varchar(4000),     --attribute value
	created_at timestamp default now() not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now() not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	attr_value_text text,         --attribute value in case it is very long text
	created_by_uid integer,
	modified_by_uid integer
);

-- MEMBER_RESOURCE_ATTR_VALUES - values of attributes assigned to members on resources
create table "member_resource_attr_values" (
	member_id integer not null,   --identifier of member (members.id)
	resource_id integer not null, --identifier of resource (resources.id)
	attr_id integer not null,     --identifier of attribute (attr_names.id)
	attr_value varchar(4000),     --attribute value
	created_at timestamp default now() not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now() not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	attr_value_text text,         --attribute value in case it is very long text
	created_by_uid integer,
	modified_by_uid integer
);

-- USER_ATTR_VALUES - values of attributes assigned to users
create table "user_attr_values" (
	user_id integer not null,  --identifier of user (users.id)
	attr_id integer not null,  --identifier of attribute (attr_names.id)
	attr_value varchar(4000),  --attribute value
	created_at timestamp default now() not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now() not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	attr_value_text text,      --attribute value in case it is very long text
	created_by_uid integer,
	modified_by_uid integer
);

-- USER_FACILITY_ATTR_VALUES - values of attributes assigned to users on facilities
create table "user_facility_attr_values" (
	user_id integer not null,     --identifier of user (users.id)
	facility_id integer not null, --identifier of facility (facilities.id)
	attr_id integer not null,     --identifier of attribute (attr_names.id)
	attr_value varchar(4000),     --attribute value
	created_at timestamp default now() not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now() not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	attr_value_text text,         --attribute value in case it is very long text
	created_by_uid integer,
	modified_by_uid integer
);

-- VO_ATTR_VALUES - attributes specific for VO
create table "vo_attr_values" (
	vo_id integer not null,    --identifier of VO (vos.id)
	attr_id integer not null,  --identifier of attribute (attr_names.id)
	attr_value varchar(4000),  --attribute value
	created_at timestamp default now() not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now() not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	attr_value_text text,      --attribute value in case it is very long text
	created_by_uid integer,
	modified_by_uid integer
);

-- EXT_SOURCES - external sourcces from which we can gain data about users
create table "ext_sources" (
	id integer not null,
	name varchar(256) not null,    --name of source
	type varchar(64),              --type of source (LDAP/IdP...)
	created_at timestamp default now() not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now() not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer
);

-- EXT_SOURCES_ATTRIBUTES - values of attributes of external sources
create table "ext_sources_attributes" (
	ext_sources_id integer not null,   --identifier of ext. source (ext_sources.id)
	attr_name varchar(128) not null,   --name of attribute at ext. source
	attr_value varchar(4000),          --value of attribute
	created_at timestamp default now() not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now() not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer
);

-- VO_EXT_SOURCES - external sources assigned to VO 
create table "vo_ext_sources" (
	vo_id integer not null,          --identifier of VO (vos.id)
	ext_sources_id integer not null, --identifier of ext. source (ext_sources.id)
	created_at timestamp default now() not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now() not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer
);

-- GROUP_EXT_SOURCES - external source assigned to GROUP
create table "group_ext_sources" (
	group_id integer not null,
	ext_source_id integer not null,
	created_at timestamp default now() not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now() not null,
	modified_by varchar(1024) default user not null,
	created_by_uid integer,
	modified_by_uid integer
);

-- USER_EXT_SOURCES - external source from which user come (identification of user in his home system)
create table "user_ext_sources" (
	id integer not null,
	user_id integer not null,          --identifier of user (users.id)
	login_ext varchar(256) not null,   --logname from his home system
	ext_sources_id integer not null,   --identifier of ext. source (ext_sources.id)
	created_at timestamp default now() not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now() not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	loa integer,                       --level of assurance
	last_access timestamp default now() not null, --time of last user's access (to Perun) by using this external source
	created_by_uid integer,
	modified_by_uid integer
);

-- SERVICE_PACKAGES - possible groups of services 
create table "service_packages" (
	id integer not null,
	name varchar(128) not null,   --name of service package
	description varchar(512),     --purpose,description
	created_at timestamp default now() not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now() not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer
);

-- SERVICE_SERVICE_PACKAGES - groups of services which should to be executed together however at specific order
create table "service_service_packages" (
	service_id integer not null,   --identifier of service (services.id)
	package_id integer not null,   --identifier of package (service_packages.id)
	created_at timestamp default now() not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now() not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer
);

-- TASKS - contains planned services and services finished at near past
create table "tasks" (
	id integer not null,
	exec_service_id integer not null,  --identifier of executed service (exec_services.id)
	facility_id integer not null,      --identifier of target facility (facilities.id)
	schedule timestamp not null,        --planned time for starting task
	recurrence integer not null,        --number of repeating of task in case of error
	delay integer not null,             --delay after next executing in case of error
	status varchar(16) not null,        --state of task
	start_time timestamp,                    --real start time of task
	end_time timestamp,                      --real end time of task
	engine_id integer not null, --identifier of engine which executing the task (engines.id)
	created_at timestamp default now() not null,
	err_message varchar(4000),          --return message in case of error
	created_by_uid integer,
	modified_by_uid integer
);

-- TASKS_RESULTS - contains partial results of tasks (executing, waiting and at near past finished)
create table "tasks_results" (
	id integer not null,
	task_id integer not null,         --identifier of task (tasks.id)
	destination_id integer not null,  --identifier of destination (destinations.id)
	status varchar(16) not null,      --status of task
	err_message varchar(4000),        --return message in case of error
	std_message varchar(4000),        --return message in case of success
	return_code integer,              --returned value
	timestamp timestamp,                   --real time of executing
	engine_id integer not null,       --identifier of executing engine (engines.id)
	created_at timestamp default now() not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now() not null,
	modified_by varchar(1024) default user not null,
	created_by_uid integer,
	modified_by_uid integer
);

-- AUDITER_LOG - logging
create table "auditer_log" (
	id integer not null,         --identifier of logged event
	msg text not null,           --text of logging message
	actor varchar(256) not null, --who causes the event
	created_at timestamp default now() not null ,
	created_by_uid integer,
	modified_by_uid integer
);

-- SERVICE_PRINCIPALS - principals for executing of services by engine, actually is not used
create table "service_principals" (
	id integer not null,
	description varchar(1024),    --description
	name varchar(128) not null,   --name of principal
	created_at timestamp default now() not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now() not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer
);

-- RESERVED_LOGINS - reserved lognames, actually is not used. Prepared for reservation by core. 
create table "reserved_logins" (
	login varchar(256),        --logname
	namespace varchar(128),    --namespace in which is logname using
	application varchar(256),  --relation to application if any
	id varchar(1024),
	created_by_uid integer,
	modified_by_uid integer
);

-- PN_AUDIT_MESSAGE - Contains all messages retrieved from the auditer log, since the notification module is auditer consumer. These messages are waiting to be processed by the notification module
create table pn_audit_message (
	message text,
	id integer NOT NULL,
	created_by_uid integer,
	modified_by_uid integer
);

-- PN_OBJECT - Keeps names of the Perun beans and their properties, for the recognition in a regular expression in the notification module
create table pn_object (
	id integer NOT NULL,
	name varchar(256),        --arbitrary name
	properties varchar(4000), --set of names of methods divided by ';'
	class_name varchar(512),  --the whole java class name of the object, e.g. 'cz.metacentrum.perun.core.api.Vo'
	created_by_uid integer,
	modified_by_uid integer
);

-- PN_POOL_MESSAGE - Contains all messages, which are already processed by the notification module. The mesages will be grouped in this table before sending
create table pn_pool_message (
	id integer NOT NULL,
	regex_id integer NOT NULL,   --references a regular expression binded to a message
	template_id integer NOT NULL, --references a template binded to a message
	key_attributes varchar(4000) NOT NULL, --contains all attributes extracted from audit message that are important for grouping messages

	created timestamp default now() NOT NULL, --the time of the pool message creation, important when PN_TEMPLATE.NOTIFY_TRIGGER is set to 'stream'
	notif_message text NOT NULL, --contains original audit message, important when gaining attributes in template message
	created_by_uid integer,
	modified_by_uid integer
);

-- PN_RECEIVER - Keeps information about receiver of messages from notification module
create table pn_receiver (
	id integer NOT NULL,
	target varchar(256) NOT NULL, --the email address or jabber of the receiver
	type_of_receiver varchar(256) NOT NULL, --available options are email_user/email_group/jabber
	template_id integer NOT NULL, --reference to the pn_template
	created_by_uid integer,
	modified_by_uid integer,
	locale varchar(512)           ----the message language and formating is based on locale
);

-- PN_REGEX - Keeps regular expressions, which are used to parse audit messages in the notification module
create table pn_regex (
	id integer NOT NULL,
	note varchar(256), --comment to the regex
	regex varchar(4000) NOT NULL, --the regular expression
	created_by_uid integer,
	modified_by_uid integer
);

-- PN_TEMPLATE - Contains templates for creating the message in the notification module
create table pn_template (
	id integer NOT NULL,
	primary_properties varchar(4000) NOT NULL, --important attributes, which messages will be grouped on, same as later PN_POOL_MESSAGE.KEY_ATTRIBUTES
	notify_trigger varchar(100), --configures two approaches to the grouping messages,
                                 --when equals 'ALL_REGEX_IDS', the pool messages are grouped and sent when all needed audit messages are collected
                                 --when equals 'STREAM', the related pool messages are waiting for the certain amount of time and then sent
	youngest_message_time integer, --time limit for the youngest message
	oldest_message_time integer,   --time limit for the oldest message
	name varchar(512),       --arbitrary name
	sender varchar(4000),    --email addres that will be stated as sender
	created_by_uid integer,
	modified_by_uid integer
);

-- PN_TEMPLATE_MESSAGE - Contains text messages that the notification module sends. The freemaker syntax can be used in the message, so the content is dynamic and is able to contain attributes from the audit message
create table pn_template_message (
	id integer NOT NULL,
	template_id integer NOT NULL,  --reference to the pn_template
	locale varchar(5) NOT NULL,    --the message language and formating is based on locale
	message varchar(4000),      --text message
	created_by_uid integer,
	modified_by_uid integer,
	subject varchar(512) not null  --text, which is used as subject of the message
);

-- PN_TEMPLATE_REGEX - Represents relation between pn_template and pn_regex in the notification module
create table pn_template_regex (
	regex_id integer NOT NULL, --referecnce to the regular expression
	template_id integer NOT NULL,  --reference to the pn_template
	id integer NOT NULL,
	created_by_uid integer,
	modified_by_uid integer
);

-- PN_REGEX_OBJECT - Represents relation between pn_regex and pn_object in the notification module
create table pn_regex_object (
	id integer NOT NULL,
	regex_id integer NOT NULL,  --referecnce to the pn_regex
	object_id integer NOT NULL,  --reference to the pn_object
	created_by_uid integer,
	modified_by_uid integer
);

-- GROUPS_GROUPS - subgroups in groups -  actually is not used. Prepared fore more sofisticated structure of groups.
create table groups_groups (
	group_id integer not null,         --identifier of group
	parent_group_id integer not null,  --identifier of parent group
	group_mode integer not null,
	created_at timestamp default now() not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now() not null,
	modified_by varchar(1024) default user not null
);

-- RES_TAGS - possible resource tags in VO
create table res_tags (
	id integer not null,
	vo_id integer not null,            --identifier of VO
	tag_name varchar (1024) not null,  --name of tag (computationl node/storage...)
	created_at timestamp default now() not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now() not null,
	modified_by varchar(1024) default user not null,
	created_by_uid integer,
	modified_by_uid integer
);

-- TAGS_RESOURCES - relation between tags and resources
create table tags_resources (
	tag_id integer not null,      --identifier of tag (res_tags.id)
	resource_id integer not null  --identifier of resource
);

-- CONFIGURATIONS - system Perun configuration
create table configurations (
	property varchar(32) not null,  --property (for example database version)
	value varchar(128) not null     --value of configuration property
);

-- MAILCHANGE - allow to user to change mail address, temporairly saved mails during change is in progress
create table "mailchange" (
	id integer not null,
	value text not null,      --
	user_id integer not null, --identifier of user (users.id)
	created_at timestamp default now() not null,
	created_by varchar(1024) default user not null,
	created_by_uid integer
);

--PWDRESET - allows to user to change passwd
create table "pwdreset" (
	id integer not null,
	namespace text not null,
	user_id integer not null,
	created_at timestamp default now() not null,
	created_by varchar(1024) default user not null,
	created_by_uid integer
);

create table "security_teams" (
	id integer not null,
	name varchar(128) not null,
	description varchar(1024),
	created_at timestamp default now() not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now() not null,
	modified_by varchar(1024) default user not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table "security_teams_facilities" (
	security_team_id integer not null,
	facility_id integer not null,
	created_at timestamp default now() not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now() not null,
	modified_by varchar(1024) default user not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table "blacklists" (
	security_team_id integer not null,
	user_id integer not null,
	description varchar(1024),
	created_at timestamp default now() not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now() not null,
	modified_by varchar(1024) default user not null,
	created_by_uid integer,
	modified_by_uid integer
);

create sequence "attr_names_id_seq" maxvalue 9223372036854775807;
create sequence "auditer_consumers_id_seq" maxvalue 9223372036854775807;
create sequence "auditer_log_id_seq" maxvalue 9223372036854775807;
create sequence "destinations_id_seq" maxvalue 9223372036854775807;
create sequence "exec_services_id_seq" maxvalue 9223372036854775807;
create sequence "ext_sources_id_seq" maxvalue 9223372036854775807;
create sequence "facilities_id_seq" maxvalue 9223372036854775807;
create sequence "groups_id_seq" maxvalue 9223372036854775807;
create sequence "hosts_id_seq" maxvalue 9223372036854775807;
create sequence "members_id_seq" maxvalue 9223372036854775807;
create sequence "owners_id_seq" maxvalue 9223372036854775807;
create sequence "processing_rules_id_seq" maxvalue 9223372036854775807;
create sequence "resources_id_seq" maxvalue 9223372036854775807;
create sequence "routing_rules_id_seq" maxvalue 9223372036854775807;
create sequence "services_id_seq" maxvalue 9223372036854775807;
create sequence "service_denials_id_seq" maxvalue 9223372036854775807;
create sequence "service_packages_id_seq" maxvalue 9223372036854775807;
create sequence "tasks_id_seq" maxvalue 9223372036854775807;
create sequence "tasks_results_id_seq" maxvalue 9223372036854775807;
create sequence "users_id_seq" maxvalue 9223372036854775807;
create sequence "user_ext_sources_id_seq" maxvalue 9223372036854775807;
create sequence "vos_id_seq" maxvalue 9223372036854775807;
create sequence "cabinet_publications_id_seq" maxvalue 9223372036854775807;
create sequence "cabinet_pub_sys_id_seq" maxvalue 9223372036854775807;
create sequence "cabinet_authorships_id_seq" maxvalue 9223372036854775807;
create sequence "cabinet_thanks_id_seq" maxvalue 9223372036854775807;
create sequence "cabinet_categories_id_seq" maxvalue 9223372036854775807;
create sequence "roles_id_seq" maxvalue 9223372036854775807;
create sequence "service_principals_id_seq" maxvalue 9223372036854775807;
create sequence "application_form_id_seq" maxvalue 9223372036854775807;
create sequence "application_form_items_id_seq" maxvalue 9223372036854775807;
create sequence "application_id_seq" maxvalue 9223372036854775807;
create sequence "application_data_id_seq" maxvalue 9223372036854775807;
create sequence "application_mails_id_seq" maxvalue 9223372036854775807;
create sequence "pn_object_id_seq" maxvalue 9223372036854775807;
create sequence "pn_pool_message_id_seq" maxvalue 9223372036854775807;
create sequence "pn_receiver_id_seq" maxvalue 9223372036854775807;
create sequence "pn_regex_id_seq" maxvalue 9223372036854775807;
create sequence "pn_template_id_seq" maxvalue 9223372036854775807;
create sequence "pn_audit_message_id_seq" maxvalue 9223372036854775807;
create sequence "pn_template_regex_seq" maxvalue 9223372036854775807;
create sequence "pn_template_message_id_seq" maxvalue 9223372036854775807;
create sequence "pn_regex_object_seq" maxvalue 9223372036854775807;
create sequence "action_types_seq" maxvalue 9223372036854775807;
create sequence "res_tags_seq" maxvalue 9223372036854775807;
create sequence "mailchange_id_seq" maxvalue 9223372036854775807;
create sequence "pwdreset_id_seq" maxvalue 9223372036854775807;
create sequence "security_teams_id_seq" maxvalue 9223372036854775807;

create index idx_namespace on attr_names(namespace);
create index idx_authz_user_role_id on authz (user_id,role_id);
create index idx_authz_authz_group_role_id on authz (authorized_group_id,role_id);
create index idx_fk_cabthank_pub on cabinet_thanks(publicationid);
create index idx_fk_usrex_usr on user_ext_sources(user_id);
create index idx_fk_usrex_usersrc on user_ext_sources(ext_sources_id);
create index idx_fk_mem_user on members(user_id);
create index idx_fk_mem_vo on members(vo_id);
create index idx_fk_host_fac on hosts(facility_id);
create index idx_fk_exsrv_srv on exec_services(service_id);
create index idx_fk_dest_srv on facility_service_destinations(service_id);
create index idx_fk_dest_fac on facility_service_destinations(facility_id);
create index idx_fk_dest_destc on facility_service_destinations(destination_id);
create index idx_fk_vousrsrc_usrsrc on vo_ext_sources(ext_sources_id);
create index idx_fk_vousrsrc_vos on vo_ext_sources(vo_id);
create index idx_fk_groupsrc_src on group_ext_sources(ext_source_id);
create index idx_fk_groupsrc_group on group_ext_sources(group_id);
create index idx_fk_usrcatt_usrc on ext_sources_attributes(ext_sources_id);
create index idx_fk_attnam_attnam on attr_names(default_attr_id);
create index idx_fk_rsrc_fac on resources(facility_id);
create index idx_fk_rsrc_vo on resources(vo_id);
create index idx_fk_faccont_fac on facility_contacts(facility_id);
create index idx_fk_faccont_usr on facility_contacts(user_id);
create index idx_fk_faccont_own on facility_contacts(owner_id);
create index idx_fk_faccont_grp on facility_contacts(group_id);
create index idx_fk_resatval_res on resource_attr_values(resource_id);
create index idx_fk_resatval_resatnam on resource_attr_values(attr_id);
create index idx_fk_usrav_usr on user_attr_values(user_id);
create index idx_fk_usrav_accattnam on user_attr_values(attr_id);
create index idx_fk_facow_fac on facility_owners(facility_id);
create index idx_fk_facow_ow on facility_owners(owner_id);
create index idx_fk_facattval_nam on facility_attr_values(attr_id);
create index idx_fk_facattval_fac on facility_attr_values(facility_id);
create index idx_fk_voattval_nam on vo_attr_values(attr_id);
create index idx_fk_voattval_vo on vo_attr_values(vo_id);
create index idx_fk_srvpkg_srv on service_service_packages(service_id);
create index idx_fk_srvpkg_pkg on service_service_packages(package_id);
create index idx_fk_grp_vos on groups(vo_id);
create index idx_fk_grp_grp on groups(parent_group_id);
create index idx_fk_memrav_mem on member_resource_attr_values(member_id);
create index idx_fk_memrav_rsrc on member_resource_attr_values(resource_id);
create index idx_fk_memrav_accattnam on member_resource_attr_values(attr_id);
create index idx_fk_memgav_mem on member_group_attr_values(member_id);
create index idx_fk_memgav_grp on member_group_attr_values(group_id);
create index idx_fk_memgav_accattnam on member_group_attr_values(attr_id);
create index idx_fk_usrfacav_mem on user_facility_attr_values(user_id);
create index idx_fk_usrfacav_fac on user_facility_attr_values(facility_id);
create index idx_fk_usrfacav_accattnam on user_facility_attr_values(attr_id);
create index idx_fk_task_exsrv on tasks(exec_service_id);
create index idx_fk_task_fac on tasks(facility_id);
create index idx_fk_task_eng on tasks(engine_id);
create index idx_fk_taskres_task on tasks_results(task_id);
create index idx_fk_taskres_dest on tasks_results(destination_id);
create index idx_fk_taskres_eng on tasks_results(engine_id);
create index idx_fk_srvden_exsrv on service_denials(exec_service_id);
create index idx_fk_srvden_fac on service_denials(facility_id);
create index idx_fk_srvden_dest on service_denials(destination_id);
create unique index idx_srvden_u ON service_denials(COALESCE(exec_service_id, '0'), COALESCE(facility_id, '0'), COALESCE(destination_id, '0'));
create index idx_fk_srvdep_exsrv on service_dependencies(exec_service_id);
create index idx_fk_srvdep_depexsrv on service_dependencies(dependency_id);
create index idx_fk_srvreqattr_srv on service_required_attrs(service_id);
create index idx_fk_srvreqattr_attr on service_required_attrs(attr_id);
create index idx_fk_resrcsrv_srv on resource_services(service_id);
create index idx_fk_resrcsrv_rsrc on resource_services(resource_id);
create index idx_fk_engrr_eng on engine_routing_rule(engine_id);
create index idx_fk_engrr_rr on engine_routing_rule(routing_rule_id);
create index idx_fk_servpr_serv on service_processing_rule(service_id);
create index idx_fk_servpr_pr on service_processing_rule(processing_rule_id);
create index idx_fk_memattval_mem on member_attr_values(member_id);
create index idx_fk_memattval_attr on member_attr_values(attr_id);
create index idx_fk_grpattval_grp on group_attr_values(group_id);
create index idx_fk_grpattval_attr on group_attr_values(attr_id);
create index idx_fk_grpresav_grp on group_resource_attr_values(group_id);
create index idx_fk_grpresav_res on group_resource_attr_values(resource_id);
create index idx_fk_grpresav_attr on group_resource_attr_values(attr_id);
create index idx_fk_hostav_host on host_attr_values(host_id);
create index idx_fk_hostav_attrt on host_attr_values(attr_id);
create index idx_fk_entlatval_attr on entityless_attr_values(attr_id);
create index idx_fk_catpub_sys on cabinet_publications(publicationsystemid);
create index idx_fk_cabpub_cat on cabinet_publications(categoryid);
create unique index idx_faccont_u2 ON facility_contacts (COALESCE(user_id, '0'), COALESCE(owner_id, '0'), COALESCE(group_id, '0'), facility_id, name);
create unique index idx_authz_u2 ON authz (COALESCE(user_id, '0'), COALESCE(authorized_group_id, '0'), COALESCE(service_principal_id, '0'), role_id, COALESCE(group_id, '0'), COALESCE(vo_id, '0'), COALESCE(facility_id, '0'), COALESCE(member_id, '0'), COALESCE(resource_id, '0'), COALESCE(service_id, '0'), COALESCE(security_team_id, '0'));
create index idx_fk_authz_role on authz(role_id);
create index idx_fk_authz_user on authz(user_id);
create index idx_fk_authz_authz_group on authz(authorized_group_id);
create index idx_fk_authz_vo on authz(vo_id);
create index idx_fk_authz_fac on authz(facility_id);
create index idx_fk_authz_mem on authz(member_id);
create index idx_fk_authz_group on authz(group_id);
create index idx_fk_authz_service on authz(service_id);
create index idx_fk_authz_res on authz(resource_id);
create index idx_fk_authz_ser_princ on authz(service_principal_id);
create index idx_fk_authz_sec_team on authz(security_team_id);
create index idx_fk_authz_sponsoru_team on authz(sponsored_user_id);
create index idx_fk_grres_gr on groups_resources(group_id);
create index idx_fk_grres_res on groups_resources(resource_id);
create index idx_fk_grpmem_gr on groups_members(group_id);
create index idx_fk_grpmem_mem on groups_members(member_id);
create index idx_fk_grpmem_memtype on groups_members(membership_type);
create unique index applform_u1 on application_form (vo_id) where group_id is null;
create unique index applform_u2 on application_form (vo_id, group_id) where group_id is not null;
create index idx_fk_applform_vo on application_form(vo_id);
create index idx_fk_applform_group on application_form(group_id);
create index idx_fk_applfrmit_applform on application_form_items(form_id);
create index idx_fk_applfrmittyp_applfrmit on application_form_item_apptypes(item_id);
create index idx_fk_applfrmittxt_applfrmit on application_form_item_texts(item_id);
create index idx_fk_app_vo on application(vo_id);
create index idx_fk_app_user on application(user_id);
create index idx_fk_app_group on application(group_id);
create index idx_fk_appdata_app on application_data(app_id);
create index idx_fk_appdata_applfrmit on application_data(item_id);
create index idx_fk_applogin_appid on application_reserved_logins(app_id);
create index idx_fk_appmail_appform on application_mails(form_id);
create index idx_fk_appmailtxt_appmails on application_mail_texts(mail_id);
create index idx_fk_cabaut_pub on cabinet_authorships(publicationid);
create index idx_fk_cabaut_usr on cabinet_authorships(userid);
create index idx_fk_pn_poolmsg_tmpl on pn_pool_message(template_id);
create index idx_fk_pn_receiver_tmpl on pn_receiver(template_id);
create index idx_fk_pn_tmplmsg_tmpl on pn_template_message(template_id);
create index idx_fk_pn_tmplrgx_rgx on pn_template_regex(regex_id);
create index idx_fk_pn_tmplrgx_tmpl on pn_template_regex(template_id);
create index idx_fk_pn_rgxobj_rgx on pn_regex_object(regex_id);
create index idx_fk_pn_rgxobj_obj on pn_regex_object(object_id);
create index idx_fk_specifu_u_ui on service_user_users(user_id);
create index idx_fk_specifu_u_sui on service_user_users(specific_user_id);
create index idx_fk_grp_grp_gid on groups_groups(group_id);
create index idx_fk_grp_grp_pgid on groups_groups(parent_group_id);
create index idx_fk_attrauthz_actiontyp on attributes_authz(action_type_id);
create index idx_fk_attrauthz_role on attributes_authz(role_id);
create index idx_fk_attrauthz_attr on attributes_authz(attr_id);
create index idx_fk_restags_vos on res_tags(vo_id);
create index idx_fk_tags_res_tags on tags_resources(tag_id);
create index idx_fk_tags_res_res on tags_resources(resource_id);
create index idx_fk_mailchange_user_id on mailchange(user_id);
create index idx_fk_pwdreset_user_id on pwdreset(user_id);
create index idx_fk_security_teams_facilities_security_team on security_teams_facilities (security_team_id);
create index idx_fk_security_teams_facilities_facilities on security_teams_facilities (facility_id);
create index idx_fk_bllist_user on blacklists (user_id);
create index idx_fk_bllist_secteam on blacklists (security_team_id);


alter table auditer_log add constraint audlog_pk primary key (id);

alter table auditer_consumers add constraint audcon_pk primary key (id);
alter table auditer_consumers add constraint audcon_u unique(name);

alter table users add constraint usr_pk primary key (id);
alter table users add constraint usr_srvacc_chk check (service_acc in ('0','1'));

alter table vos add constraint vo_pk primary key (id);
alter table vos add constraint vo_u unique (name);

alter table ext_sources add constraint usrsrc_pk primary key(id);
alter table ext_sources add constraint usrsrc_u unique (name);

alter table user_ext_sources add constraint usrex_p primary key(id);
alter table user_ext_sources add constraint usrex_u unique (ext_sources_id,login_ext);
alter table user_ext_sources add constraint usrex_usr_fk foreign key (user_id) references users(id);
alter table user_ext_sources add constraint usrex_usersrc_fk foreign key(ext_sources_id) references ext_sources(id);

alter table members add constraint mem_pk primary key(id);
alter table members add constraint mem_user_fk foreign key(user_id) references users(id);
alter table members add constraint mem_vo_fk foreign key(vo_id) references vos(id);

alter table owners add constraint ow_pk primary key (id);
alter table owners add constraint ow_u unique (name);

alter table facilities add constraint fac_pk primary key(id);
alter table facilities add constraint fac_name_u unique (name);

alter table hosts add constraint host_pk primary key (id);
alter table hosts add constraint host_fac_fk foreign key(facility_id) references facilities(id);

alter table services add constraint serv_pk primary key(id);
alter table services add constraint serv_u unique(name);

alter table exec_services add constraint exsrv_pk primary key(id);
alter table exec_services add constraint exsrv_srv_fk foreign key (service_id) references services(id);
alter table exec_services add constraint exsrv_type_chk check (type in ('SEND','GENERATE'));

alter table destinations add constraint dest_pk primary key (id);
alter table destinations add constraint dest_u unique(destination,type);

alter table facility_service_destinations add constraint dest_srv_fk foreign key (service_id) references services(id);
alter table facility_service_destinations add constraint dest_fac_fk foreign key (facility_id) references facilities(id);
alter table facility_service_destinations add constraint dest_dest_fk foreign key(destination_id) references destinations(id);

alter table vo_ext_sources add constraint vousrsrc_pk primary key (vo_id,ext_sources_id);
alter table vo_ext_sources add constraint vousrsrc_usrsrc_fk foreign key(ext_sources_id) references ext_sources(id);
alter table vo_ext_sources add constraint vousrsrc_vos_fk foreign key(vo_id) references vos(id);

alter table ext_sources_attributes add constraint usrcatt_usrc_fk foreign key (ext_sources_id) references ext_sources(id);

alter table attr_names add constraint attnam_pk primary key(id);
alter table attr_names add constraint attnam_u unique (attr_name);
alter table attr_names add constraint attfullnam_u unique (friendly_name,namespace);
alter table attr_names add constraint attnam_attnam_fk foreign key (default_attr_id) references attr_names(id);

alter table resources add constraint rsrc_pk primary key (id);
alter table resources add constraint rsrc_fac_fk foreign key (facility_id) references facilities(id);
alter table resources add constraint rsrc_vo_fk foreign key (vo_id) references vos(id);

alter table resource_attr_values add constraint resatval_pk primary key (resource_id,attr_id);
alter table resource_attr_values add constraint resatval_res_fk foreign key(resource_id) references resources(id);
alter table resource_attr_values add constraint resatval_resatnam_fk foreign key(attr_id) references attr_names(id);

alter table user_attr_values add constraint usrav_usr_fk foreign key (user_id) references users(id);
alter table user_attr_values add constraint usrav_accattnam_fk foreign key (attr_id) references attr_names(id);
alter table user_attr_values add constraint usrav_u unique(user_id,attr_id);

alter table facility_owners add constraint facow_pk primary key (facility_id,owner_id);
alter table facility_owners add constraint facow_fac_fk foreign key (facility_id) references facilities(id);
alter table facility_owners add constraint facow_ow_fk foreign key (owner_id) references owners(id);

alter table facility_attr_values add constraint facattval_pk primary key (facility_id,attr_id);
alter table facility_attr_values add constraint facattval_nam_fk foreign key (attr_id) references attr_names(id);
alter table facility_attr_values add constraint facattval_fac_fk foreign key (facility_id) references facilities (id);

alter table vo_attr_values add constraint voattval_pk primary key (vo_id,attr_id);
alter table vo_attr_values add constraint voattval_nam_fk foreign key (attr_id) references attr_names(id);
alter table vo_attr_values add constraint voattval_vo_fk foreign key (vo_id) references vos (id);

alter table service_packages add constraint pkg_pk primary key (id);
alter table service_packages add constraint pkg_name unique(name);

alter table service_service_packages add constraint srvpkg_srv_pk primary key(service_id,package_id);
alter table service_service_packages add constraint srvpkg_srv_fk foreign key(service_id) references services(id);
alter table service_service_packages add constraint srvpkg_pkg_fk foreign key(package_id) references service_packages(id);

alter table groups add constraint grp_pk primary key (id);
alter table groups add constraint grp_nam_vo_parentg_u unique (name,vo_id,parent_group_id);
alter table groups add constraint grp_vos_fk foreign key (vo_id) references vos(id);
alter table groups add constraint grp_grp_fk foreign key (parent_group_id) references groups(id);

alter table group_ext_sources add constraint groupsrc_pk primary key (group_id,ext_source_id);
alter table group_ext_sources add constraint groupsrc_src_fk foreign key(ext_source_id) references ext_sources(id);
alter table group_ext_sources add constraint groupsrc_groups_fk foreign key(group_id) references groups(id);

alter table member_resource_attr_values add constraint memrav_mem_fk foreign key (member_id) references members(id);
alter table member_resource_attr_values add constraint memrav_rsrc_fk foreign key (resource_id) references resources(id);
alter table member_resource_attr_values add constraint memrav_accattnam_fk foreign key (attr_id) references attr_names(id);
alter table member_resource_attr_values add constraint memrav_u unique(member_id,resource_id,attr_id);

alter table member_group_attr_values add constraint memgav_mem_fk foreign key (member_id) references members(id);
alter table member_group_attr_values add constraint memgav_grp_fk foreign key (group_id) references groups(id);
alter table member_group_attr_values add constraint memgav_accattnam_fk foreign key (attr_id) references attr_names(id);
alter table member_group_attr_values add constraint memgav_u unique(member_id,group_id,attr_id);

alter table user_facility_attr_values add constraint usrfacav_mem_fk foreign key (user_id) references users(id);
alter table user_facility_attr_values add constraint usrfacav_fac_fk foreign key (facility_id) references facilities(id);
alter table user_facility_attr_values add constraint usrfacav_accattnam_fk foreign key (attr_id) references attr_names(id);
alter table user_facility_attr_values add constraint usrfacav_u unique(user_id,facility_id,attr_id);

alter table service_denials add constraint srvden_pk primary key (id);
alter table service_denials add constraint srvden_exsrv_fk foreign key (exec_service_id) references exec_services(id);
alter table service_denials add constraint srvden_fac_fk foreign key (facility_id) references facilities(id);
alter table service_denials add constraint srvden_dest_fk foreign key (destination_id) references destinations(id);
alter table service_denials add constraint srvden_u check(exec_service_id is not null and ((facility_id is not null and destination_id is null) or (facility_id is null and destination_id is not null)));

alter table service_dependencies add constraint srvdep_exsrv_fk foreign key (exec_service_id) references exec_services(id);
alter table service_dependencies add constraint srvdep_depexsrv_fk foreign key (dependency_id) references exec_services(id);
alter table service_dependencies add constraint srvdep_type_chk check (type in ('SERVICE','DESTINATION'));
alter table service_dependencies add constraint srvdep_u unique(exec_service_id,dependency_id);

alter table engines add constraint eng_pk primary key (id);

alter table service_required_attrs add constraint srvreqattr_pk primary key (service_id,attr_id);
alter table service_required_attrs add constraint srvreqattr_srv_fk foreign key(service_id) references services(id);
alter table service_required_attrs add constraint srvreqattr_attr_fk foreign key(attr_id) references attr_names(id);

alter table resource_services add constraint resrcsrv_pk primary key (service_id,resource_id);
alter table resource_services add constraint resrcsrv_srv_fk foreign key (service_id) references services(id);
alter table resource_services add constraint resrcsrv_rsrc_fk foreign key (resource_id) references resources(id);

alter table routing_rules add constraint routrul_pk primary key (id);

alter table engine_routing_rule add constraint engrr_eng_fk foreign key (engine_id) references engines(id);
alter table engine_routing_rule add constraint engrr_rr_fk foreign key (routing_rule_id) references routing_rules(id);

alter table processing_rules add constraint procrul_pk primary key (id);

alter table service_processing_rule add constraint servpr_serv_fk foreign key (service_id) references services(id);
alter table service_processing_rule add constraint servpr_pr_fk foreign key (processing_rule_id) references processing_rules(id);

alter table member_attr_values add constraint memattval_pk primary key (member_id,attr_id);
alter table member_attr_values add constraint memattval_mem_fk foreign key (member_id) references members(id);
alter table member_attr_values add constraint memattval_attr_fk foreign key (attr_id) references attr_names(id);

alter table group_attr_values add constraint grpattval_pk primary key (group_id,attr_id);
alter table group_attr_values add constraint grpattval_grp_fk foreign key (group_id) references groups(id);
alter table group_attr_values add constraint grpattval_attr_fk foreign key (attr_id) references attr_names(id);

alter table group_resource_attr_values add constraint grpresav_pk primary key (group_id,resource_id,attr_id);
alter table group_resource_attr_values add constraint grpresav_grp_fk foreign key (group_id) references groups(id);
alter table group_resource_attr_values add constraint grpresav_res_fk foreign key (resource_id) references resources(id);
alter table group_resource_attr_values add constraint grpresav_attr_fk foreign key (attr_id) references attr_names(id);

alter table host_attr_values add constraint hostav_pk primary key (host_id,attr_id);
alter table host_attr_values add constraint hostav_host_fk foreign key (host_id) references hosts(id);
alter table host_attr_values add constraint hostav_attr_fk foreign key (attr_id) references attr_names(id);

alter table entityless_attr_values add constraint entlatval_pk primary key(subject,attr_id);
alter table entityless_attr_values add constraint entlatval_attr_fk foreign key (attr_id) references attr_names(id);

alter table cabinet_categories add constraint cab_cat_pk primary key (id);

alter table cabinet_publication_systems add constraint cab_pubsys_pk primary key (id);

alter table cabinet_publications add constraint cab_pub_pk primary key (id);
alter table cabinet_publications add constraint catpub_sys_fk foreign key(publicationsystemid) references cabinet_publication_systems(id);
alter table cabinet_publications add constraint cabpub_cat_fk foreign key(categoryid) references cabinet_categories(id);

alter table cabinet_authorships add constraint cab_au_pk primary key (id);
alter table cabinet_authorships add constraint cabaut_pub_fk foreign key(publicationid) references cabinet_publications(id);
alter table cabinet_authorships add constraint cabaut_usr_fk foreign key(userid) references users(id);

alter table cabinet_thanks add constraint cab_th_pk primary key (id);
alter table cabinet_thanks add constraint cabthank_pub_fk foreign key(publicationid) references cabinet_publications(id);

alter table roles add constraint roles_pk primary key (id);
alter table roles add constraint roles_name_u unique (name);

alter table groups_resources add constraint grres_grp_res_u unique (group_id,resource_id);
alter table groups_resources add constraint grres_gr_fk foreign key (group_id) references groups(id);
alter table groups_resources add constraint grres_res_fk foreign key (resource_id) references resources(id);

alter table service_principals add constraint ser_princ_pk primary key (id);

alter table membership_types add constraint MEMTYPE_PK primary key (id);

alter table groups_members add constraint grpmem_pk primary key (member_id,group_id, source_group_id);
alter table groups_members add constraint grpmem_gr_fk foreign key (group_id) references groups(id);
alter table groups_members add constraint grpmem_mem_fk foreign key (member_id) references members(id);
alter table groups_members add constraint grpmem_memtype_fk foreign key (membership_type) references membership_types(id);

alter table application_form add constraint applform_pk primary key (id);
alter table application_form add constraint applform_vo_fk foreign key (vo_id) references vos(id) on delete cascade;
alter table application_form add constraint applform_group_fk foreign key (group_id) references groups(id) on delete cascade;

alter table application_form_items add constraint applfrmit_pk primary key (id);
alter table application_form_items add constraint applfrmit_applform foreign key (form_id) references application_form(id) on delete cascade;

alter table application_form_item_apptypes add constraint applfrmittyp_applfrmit_fk foreign key (item_id) references application_form_items(id) on delete cascade;

alter table application_form_item_texts add constraint applfrmittxt_pk primary key(item_id,locale);
alter table application_form_item_texts add constraint applfrmittxt_applfrmit_fk foreign key (item_id) references application_form_items(id) on delete cascade;

alter table application add constraint app_pk primary key (id);
alter table application add constraint app_vo_fk foreign key (vo_id) references vos(id) on delete cascade;
alter table application add constraint app_group_fk foreign key (group_id) references groups(id) on delete cascade;
alter table application add constraint app_user_fk foreign key (user_id) references users(id) on delete cascade;
alter table application add constraint app_state_chk check (state in ('REJECTED','NEW','VERIFIED','APPROVED'));

alter table application_data add constraint appdata_pk primary key (id);
alter table application_data add constraint appdata_app_fk foreign key (app_id) references application(id) on delete cascade;
alter table application_data add constraint appdata_applfrmit_fk foreign key (item_id) references application_form_items(id) on delete cascade;

alter table application_reserved_logins add constraint app_logins_pk primary key(login, namespace);
alter table application_reserved_logins add constraint applogin_appid_fk foreign key(app_id) references application(id);

alter table application_mails add constraint appmails_pk primary key (id);
alter table application_mails add constraint appmails_u unique (form_id,app_type,mail_type);
alter table application_mails add constraint appmail_appform_fk foreign key (form_id) references application_form(id) on delete cascade;

alter table application_mail_texts add constraint appmailtxt_pk primary key (mail_id, locale);
alter table application_mail_texts add constraint appmailtxt_appmails_fk foreign key (mail_id) references application_mails(id) on delete cascade;

alter table reserved_logins add constraint reservlogins_pk primary key (login,namespace);

alter table pn_audit_message add constraint pn_audmsg_pk primary key (id);

alter table pn_object add constraint pn_object_pk primary key (id);

alter table pn_template add constraint pn_tmpl_pk primary key (id);

alter table pn_pool_message add constraint pn_poolmsg_pk primary key (id);
alter table pn_pool_message add constraint pn_poolmsg_tmpl_fk foreign key (template_id) references pn_template(id);

alter table pn_receiver add constraint pn_receiver_pk primary key (id);
alter table pn_receiver add constraint pn_receiver_tmpl_fk foreign key (template_id) references pn_template(id);

alter table pn_regex add constraint pn_regex_pk primary key (id);

alter table pn_template_message add constraint pn_tmplmsg_pk primary key (id);
alter table pn_template_message add constraint pn_tmplmsg_tmpl_fk foreign key (template_id) references pn_template(id) deferrable;

alter table pn_template_regex add constraint pn_tmplrgx_pk primary key (id);
alter table pn_template_regex add constraint pn_tmplrgx_rgx_fk foreign key (regex_id) references pn_regex(id);
alter table pn_template_regex add constraint pn_tmplrgx_tmpl_fk foreign key (template_id) references pn_template(id);

alter table pn_regex_object add constraint pn_rgxobj_pk primary key (id);
alter table pn_regex_object add constraint pn_rgxobj_rgx_fk foreign key (regex_id) references pn_regex(id);
alter table pn_regex_object add constraint pn_rgxobj_obj_fk foreign key (object_id) references pn_object(id);

alter table specific_user_users add constraint acc_specifu_u_pk primary key (user_id,specific_user_id);
alter table specific_user_users add constraint acc_specifu_u_uid_fk foreign key (user_id) references users(id);
alter table specific_user_users add constraint acc_specifu_u_suid_fk foreign key (specific_user_id) references users(id);
alter table specific_user_users add constraint specifu_u_status_chk check (status in ('0','1'));

alter table groups_groups add constraint grp_grp_pk primary key (group_id,parent_group_id);
alter table groups_groups add constraint grp_grp_gid_fk foreign key (group_id) references groups(id);
alter table groups_groups add constraint grp_grp_pgid_fk foreign key (parent_group_id) references groups(id);

alter table action_types add constraint actiontyp_pk primary key (id);
alter table action_types add constraint actiontyp_u unique (action_type);
alter table action_types add constraint actiontyp_at_chk check (action_type in ('read','write'));

alter table attributes_authz add constraint attrauthz_pk primary key (attr_id,role_id,action_type_id);
alter table attributes_authz add constraint attrauthz_attr_fk foreign key (attr_id) references attr_names (id);
alter table attributes_authz add constraint attrauthz_role_fk foreign key (role_id) references roles(id);
alter table attributes_authz add constraint attrauthz_actiontyp_fk foreign key (action_type_id) references action_types(id);

alter table res_tags add constraint restags_pk primary key (id);
alter table res_tags add constraint restags_u unique (vo_id, tag_name);
alter table res_tags add constraint restags_vos_fk foreign key (vo_id) references vos(id);

alter table tags_resources add constraint tags_res_pk primary key (tag_id,resource_id);
alter table tags_resources add constraint tags_res_tags_fk foreign key (tag_id) references res_tags(id);
alter table tags_resources add constraint tags_res_res_fk foreign key (resource_id) references resources(id);

alter table tasks add constraint task_pk primary key (id);
alter table tasks add constraint task_exsrv_fk foreign key (exec_service_id) references exec_services(id);
alter table tasks add constraint task_fac_fk foreign key (facility_id) references facilities(id);
alter table tasks add constraint task_eng_fk foreign key (engine_id) references engines (id);
alter table tasks add constraint task_stat_chk check (status in ('NONE','OPEN','PLANNED','PROCESSING','DONE','ERROR'));

alter table tasks_results add constraint taskres_task_fk foreign key (task_id) references tasks(id);
alter table tasks_results add constraint taskres_dest_fk foreign key (destination_id) references destinations(id);
alter table tasks_results add constraint taskres_eng_fk foreign key (engine_id) references engines (id);
alter table tasks_results add constraint taskres_stat_chk check (status in ('DONE','ERROR','FATAL_ERROR','DENIED'));

alter table security_teams add constraint security_teams_pk primary key (id);
alter table security_teams_facilities add constraint security_teams_facilities_pk primary key (security_team_id, facility_id);
alter table security_teams_facilities add constraint security_teams_facilities_security_team_fk foreign key (security_team_id) references security_teams(id);
alter table security_teams_facilities add constraint security_teams_facilities_facilities_fk foreign key (facility_id) references facilities(id);

alter table blacklists add constraint bllist_pk primary key (security_team_id,user_id);
alter table blacklists add constraint bllist_secteam_fk foreign key (security_team_id) references security_teams (id);
alter table blacklists add constraint bllist_user_fk foreign key (user_id) references users(id);

alter table authz add constraint authz_role_fk foreign key (role_id) references roles(id);
alter table authz add constraint authz_user_fk foreign key (user_id) references users(id);
alter table authz add constraint authz_authz_group_fk foreign key (authorized_group_id) references groups(id);
alter table authz add constraint authz_vo_fk foreign key (vo_id) references vos(id);
alter table authz add constraint authz_fac_fk foreign key (facility_id) references facilities(id);
alter table authz add constraint authz_mem_fk foreign key (member_id) references members(id);
alter table authz add constraint authz_group_fk foreign key (group_id) references groups(id);
alter table authz add constraint authz_service_fk foreign key (service_id) references services(id);
alter table authz add constraint authz_res_fk foreign key (resource_id) references resources(id);
alter table authz add constraint authz_ser_princ_fk foreign key (service_principal_id) references service_principals(id);
alter table authz add constraint authz_sec_team_fk foreign key (security_team_id) references security_teams(id);
alter table authz add constraint authz_user_serprinc_autgrp_chk check ((user_id is not null and service_principal_id is null and authorized_group_id is null) or (user_id is null and service_principal_id is not null and authorized_group_id is null) or (user_id is null and service_principal_id is null and authorized_group_id is not null));
alter table configurations add constraint config_pk primary key (property);
alter table configurations add constraint config_prop_chk check (property in ('DATABASE VERSION'));
alter table mailchange add constraint mailchange_pk primary key (id);
alter table mailchange add constraint mailchange_u_fk foreign key (user_id) references users(id);
alter table pwdreset add constraint pwdreset_pk primary key (id);
alter table pwdreset add constraint pwdreset_u_fk foreign key (user_id) references users(id);

alter table facility_contacts add constraint faccont_fac_fk foreign key (facility_id) references facilities(id);
alter table facility_contacts add constraint faccont_usr_fk foreign key (user_id) references users(id);
alter table facility_contacts add constraint faccont_own_fk foreign key (owner_id) references owners(id);
alter table facility_contacts add constraint faccont_grp_fk foreign key (group_id) references groups(id);
alter table facility_contacts add constraint faccont_usr_own_grp_chk check ((user_id is not null and owner_id is null and group_id is null) or (user_id is null and owner_id is not null and group_id is null) or (user_id is null and owner_id is null and group_id is not null));

grant all on users to perun;
grant all on vos to perun;
grant all on ext_sources to perun;
grant all on user_ext_sources to perun;
grant all on members to perun;
grant all on owners to perun;
grant all on hosts to perun;
grant all on services to perun;
grant all on exec_services to perun;
grant all on destinations to perun;
grant all on facility_service_destinations to perun;
grant all on vo_ext_sources to perun;
grant all on group_ext_sources to perun;
grant all on ext_sources_attributes to perun;
grant all on attr_names to perun;
grant all on facilities to perun;
grant all on resources to perun;
grant all on resource_attr_values to perun;
grant all on facility_contacts to perun;
grant all on user_attr_values to perun;
grant all on facility_owners to perun;
grant all on facility_attr_values to perun;
grant all on vo_attr_values to perun;
grant all on service_packages to perun;
grant all on service_service_packages to perun;
grant all on groups to perun;
grant all on member_resource_attr_values to perun;
grant all on member_group_attr_values to perun;
grant all on user_facility_attr_values to perun;
grant all on tasks to perun;
grant all on tasks_results to perun;
grant all on service_denials to perun;
grant all on service_dependencies to perun;
grant all on engines to perun;
grant all on dispatcher_settings to perun;
grant all on service_required_attrs to perun;
grant all on resource_services to perun;
grant all on routing_rules to perun;
grant all on engine_routing_rule to perun;
grant all on processing_rules to perun;
grant all on service_processing_rule to perun;
grant all on member_attr_values to perun;
grant all on group_attr_values to perun;
grant all on group_resource_attr_values to perun;
grant all on host_attr_values to perun;
grant all on application_form to perun;
grant all on application_form_items to perun;
grant all on application_form_item_apptypes to perun;
grant all on application_form_item_texts to perun;
grant all on application to perun;
grant all on application_data to perun;
grant all on application_reserved_logins to perun;
grant all on auditer_log to perun;
grant all on auditer_consumers to perun;
grant all on entityless_attr_values to perun;
grant all on cabinet_categories to perun;
grant all on cabinet_publication_systems to perun;
grant all on cabinet_publications to perun;
grant all on cabinet_authorships to perun;
grant all on cabinet_thanks to perun;
grant all on roles to perun;
grant all on authz to perun;
grant all on groups_resources to perun;
grant all on groups_members to perun;
grant all on service_principals to perun;
grant all on application_mails to perun;
grant all on application_mail_texts to perun;
grant all on reserved_logins to perun;
grant all on pn_audit_message to perun;
grant all on pn_object to perun;
grant all on pn_pool_message to perun;
grant all on pn_receiver to perun;
grant all on pn_regex to perun;
grant all on pn_template to perun;
grant all on pn_template_message to perun;
grant all on pn_template_regex to perun;
grant all on pn_regex_object to perun;
grant all on service_user_users to perun;
grant all on groups_groups to perun;
grant all on action_types to perun;
grant all on attributes_authz to perun;
grant all on res_tags to perun;
grant all on tags_resources to perun;
grant all on configurations to perun;
grant all on mailchange to perun;
grant all on pwdreset to perun;
grant all on security_teams to perun;
grant all on security_teams_facilities to perun;
grant all on blacklists to perun;

-- set initial Perun DB version
insert into configurations values ('DATABASE VERSION','3.1.35');
