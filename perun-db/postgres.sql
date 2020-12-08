-- database version 3.1.73 (don't forget to update insert statement at the end of file)

-- VOS - virtual organizations
create table vos (
	id integer not null,
	name varchar not null,   -- full name of VO
	short_name varchar not null, -- commonly used name
	created_at timestamp default statement_timestamp() not null,
	created_by varchar default user not null,
	modified_at timestamp default statement_timestamp() not null,
	modified_by varchar default user not null,
	created_by_uid integer,
	modified_by_uid integer,
	constraint vo_pk primary key (id),
	constraint vo_u unique (name)
);

-- USERS - information about user as real person
create table users (
	id integer not null,
	uu_id uuid not null default gen_random_uuid(),
	first_name varchar,   -- christening name
	last_name varchar,    -- family name
	middle_name varchar,   -- second name
	title_before varchar,  -- academic degree used before name
	title_after varchar,   -- academic degree used after name
	created_at timestamp default statement_timestamp() not null,
	created_by varchar default user not null,
	modified_at timestamp default statement_timestamp() not null,
	modified_by varchar default user not null,
	service_acc boolean default false not null, --is it service account?
	sponsored_acc boolean default false not null, --is it sponsored account?
	created_by_uid integer,
	modified_by_uid integer,
	constraint usr_pk primary key (id)
);

-- OWNERS - owners of resources and devices
create table owners (
	id integer not null,
	name varchar not null, --name of owner
	contact varchar,       --contact email or phone
	created_at timestamp default statement_timestamp() not null,
	created_by varchar default user not null,
	modified_at timestamp default statement_timestamp() not null,
	modified_by varchar default user not null,
	type varchar not null, --type of owner (for example IdP)
	created_by_uid integer,
	modified_by_uid integer,
	constraint ow_pk primary key (id),
  constraint ow_u unique (name)
);

-- CABINET_CATEGORIES - possible categories of publications
create table cabinet_categories (
	id integer not null,
	name varchar not null,  --name of category
	rank numeric(38,1) not null,  --coefficient for evaluation of publication in scope of this category
	created_by_uid integer,
	modified_by_uid integer,
	constraint cab_cat_pk primary key (id)
);

-- CABINET_PUBLICATION_SYSTEMS - external publication systems. Contains information which allowes searching
create table cabinet_publication_systems (
	id integer not null,
	friendlyName varchar not null, --name of publication system
	url varchar not null, --address for searching at external system
	username varchar,  --logname
	password varchar,  -- and password for connection to external system
	loginNamespace varchar not null, --namespace used for username
	type varchar not null,  --name of class of parser for received data (for example cz.metacentrum.perun.cabinet.strategy.impl.MUStrategy) *)
	created_by_uid integer,
	modified_by_uid integer,
	constraint cab_pubsys_pk primary key (id)
);
--*) it have to include entry about internal publication system to create publication directly in Perun DB

-- CABINET_PUBLICATIONS - all publications stored in Perun DB
create table cabinet_publications (
	id integer not null,
	externalId integer not null, --identifier at externa pub. system
	publicationSystemId integer not null, --identifier of external pub. system (cabinet_publication_systems.id) *)
	title varchar not null,
	year integer not null, --short title of publication
	main varchar, --full cite of publication
	isbn varchar,
	categoryId integer not null, --identifier of category (cabinet_categories.id)
	createdBy varchar default user not null,
	createdDate timestamp not null,
	rank numeric (38,1) default 0 not null,
	doi varchar,
	locked boolean default false not null,
	created_by_uid integer,
	modified_by_uid integer,
	constraint cab_pub_pk primary key (id),
  constraint catpub_sys_fk foreign key(publicationsystemid) references cabinet_publication_systems(id),
  constraint cabpub_cat_fk foreign key(categoryid) references cabinet_categories(id)
);
--*) if publication is created directly in Perun externalId=id and publicationSystemId is identifier of internal system

-- CABINET_AUTHORSHIPS - relation of user to publication (author,co-author)
create table cabinet_authorships (
	id integer not null,
	publicationId integer not null, --identifier of publication (cabinet_publications.id)
	userId integer not null, -- identifier of user (users.id)
	createdBy varchar default user not null,
	createdDate timestamp not null,
	created_by_uid integer,
	modified_by_uid integer,
	constraint cab_au_pk primary key (id),
  constraint cabaut_pub_fk foreign key(publicationid) references cabinet_publications(id),
  constraint cabaut_usr_fk foreign key(userid) references users(id)
);

-- CABINET THANKS - list of institutions which are acnowledged at publication
create table cabinet_thanks (
	id integer not null,
	publicationid integer not null, --identifier of publication (cabinet_publications.id)
	ownerId integer not null, --identifier of owner of used ources and devices (owners.id) - MetaCenter,CESNET...
	createdBy varchar default user not null,
	createdDate timestamp not null,
	created_by_uid integer,
	modified_by_uid integer,
	constraint cab_th_pk primary key (id),
  constraint cabthank_pub_fk foreign key(publicationid) references cabinet_publications(id)
);

-- FACILITIES - sources, devices - includes clusters,hosts,storages...
create table facilities (
	id integer not null,
	name varchar not null, --unique name of facility
	dsc varchar,
	created_at timestamp default statement_timestamp() not null,
	created_by varchar default user not null,
	modified_at timestamp default statement_timestamp() not null,
	modified_by varchar default user not null,
	created_by_uid integer,
	modified_by_uid integer,
	constraint fac_pk primary key(id),
  constraint fac_name_u unique (name)
);

-- RESOURCES - facility assigned to VO
create table resources (
	id integer not null,
	uu_id uuid not null default gen_random_uuid(),
	facility_id integer not null, --facility identifier (facility.id)
	name varchar not null,   --name of resource
	dsc varchar,            --purpose and description
	created_at timestamp default statement_timestamp() not null,
	created_by varchar default user not null,
	modified_at timestamp default statement_timestamp() not null,
	modified_by varchar default user not null,
	vo_id integer not null,   --identifier of VO (vos.id)
	created_by_uid integer,
	modified_by_uid integer,
	constraint rsrc_pk primary key (id),
  constraint rsrc_fac_fk foreign key (facility_id) references facilities(id),
  constraint rsrc_vo_fk foreign key (vo_id) references vos(id)
);

-- DESTINATIONS - targets of services
create table destinations (
	id integer not null,
	destination varchar not null, --value of destination (hostname,email,URL...)
	type varchar not null, --type (host,URL...)
	created_at timestamp default statement_timestamp() not null,
	created_by varchar default user not null,
	modified_at timestamp default statement_timestamp() not null,
	modified_by varchar default user not null,
	created_by_uid integer,
	modified_by_uid integer,
	constraint dest_pk primary key (id),
  constraint dest_u unique(destination,type)
);

-- FACILITY_OWNERS - one or more institutions which own the facility
create table facility_owners (
	facility_id integer not null, --identifier of facility (facilities.id)
	owner_id integer not null,   --identifier of owner (owners.id)
	created_at timestamp default statement_timestamp() not null,
	created_by varchar default user not null,
	modified_at timestamp default statement_timestamp() not null,
	modified_by varchar default user not null,
	created_by_uid integer,
	modified_by_uid integer,
	constraint facow_pk primary key (facility_id,owner_id),
  constraint facow_fac_fk foreign key (facility_id) references facilities(id),
  constraint facow_ow_fk foreign key (owner_id) references owners(id)
);

-- GROUPS - groups of users
create table groups (
	id integer not null,
	uu_id uuid not null default gen_random_uuid(),
	name text not null,         --group name
	dsc varchar,          --purpose and description
	vo_id integer not null,     --identifier of VO (vos.id)
	created_at timestamp default statement_timestamp() not null,
	created_by varchar default user not null,
	modified_at timestamp default statement_timestamp() not null,
	modified_by varchar default user not null,
	parent_group_id integer,    --in case of subgroup identifier of parent group (groups.id)
	created_by_uid integer,
	modified_by_uid integer,
	constraint grp_pk primary key (id),
  constraint grp_vos_fk foreign key (vo_id) references vos(id),
  constraint grp_grp_fk foreign key (parent_group_id) references groups(id)
);

-- FACILITIES_CONTACTS - all optional contacts for facility (owners, users or groups)
create table facility_contacts (
	name varchar not null, -- similar to tag of group of contacts
	facility_id integer not null, --facility identifier
	owner_id integer, --owner identifier
	user_id integer, --user identifier
	group_id integer, -- group identifier
	constraint faccont_fac_fk foreign key (facility_id) references facilities(id),
	constraint faccont_usr_fk foreign key (user_id) references users(id),
	constraint faccont_own_fk foreign key (owner_id) references owners(id),
	constraint faccont_grp_fk foreign key (group_id) references groups(id),
	constraint faccont_usr_own_grp_chk check
	((user_id is not null and owner_id is null and group_id is null)
	 or (user_id is null and owner_id is not null and group_id is null)
	 or (user_id is null and owner_id is null and group_id is not null))
);

-- MEMBERS - members of VO
create table members (
	id integer not null,
	user_id integer not null,  --user's identifier (users.id)
	vo_id integer not null,    --identifier of VO (vos.id)
	created_at timestamp default statement_timestamp() not null,
	created_by varchar default user not null,
	modified_at timestamp default statement_timestamp() not null,
	modified_by varchar default user not null,
	status integer default 0 not null, --status of membership
	sponsored boolean default false not null,
	created_by_uid integer,
	modified_by_uid integer,
	constraint mem_pk primary key(id),
  constraint mem_user_fk foreign key(user_id) references users(id),
  constraint mem_vo_fk foreign key(vo_id) references vos(id),
  constraint mem_user_vo_u unique (vo_id, user_id)
);

-- ROLES - possible user's rolles - controle access of users to data in DB
create table roles (
	id integer not null,
	name varchar not null,  --name of role
	created_by_uid integer,
	modified_by_uid integer,
	constraint roles_pk primary key (id),
  constraint roles_name_u unique (name)
);

-- ACTION_TYPES - possible actions for attributes
create table action_types (
	id integer not null,
	action_type varchar not null,  --type of action (read/write...)
	description varchar,         --description
	constraint actiontyp_pk primary key (id),
  constraint actiontyp_u unique (action_type),
  constraint actiontyp_at_chk check (action_type in ('read', 'read_vo', 'read_public', 'write', 'write_vo', 'write_public'))
);

-- MEMBERSHIP_TYPES - possible types of membership in group
create table membership_types (
	id integer not null,
	membership_type varchar not null,  --type of memberships (DIRECT/INDIRECT...)
	description varchar,              --description
	constraint MEMTYPE_PK primary key (id)
);

-- ATTR_NAMES - list of possible attributes
create table attr_names (
	id integer not null,
	attr_name varchar not null,  --full name of attribute
	friendly_name varchar not null, --short name of attribute
	namespace varchar not null,  --access of attribute to the entity
	type varchar not null,       --type o0f attribute data (strig,number,array...)
	dsc varchar,                --purpose,description
	created_at timestamp default statement_timestamp() not null,
	created_by varchar default user not null,
	modified_at timestamp default statement_timestamp() not null,
	modified_by varchar default user not null,
	created_by_uid integer,
	modified_by_uid integer,
	display_name varchar,  --name of attr. displayed at GUI
	is_unique boolean DEFAULT FALSE NOT NULL,
	constraint attnam_pk primary key(id),
  constraint attnam_u unique (attr_name),
  constraint attfullnam_u unique (friendly_name,namespace)
);

-- ATTRIBUTES_AUTHZ - controles permissions for access to attributes
create table attributes_authz (
	attr_id integer not null,  --identifier of attribute (attr_names.id)
	role_id integer not null,  --identifier of role (roles.id)
	action_type_id integer not null,  --identifier of action (action_types.id)
	constraint attrauthz_pk primary key (attr_id,role_id,action_type_id),
  constraint attrauthz_attr_fk foreign key (attr_id) references attr_names (id),
  constraint attrauthz_role_fk foreign key (role_id) references roles(id),
  constraint attrauthz_actiontyp_fk foreign key (action_type_id) references action_types(id)
);


-- HOSTS - detail information about hosts and cluster nodes
create table hosts (
	id integer not null,
	hostname varchar not null,  --full name of machine
	facility_id integer not null,    --identifier of facility containing the host (facilities.id)
	dsc varchar,  --description
	created_at timestamp default statement_timestamp() not null,
	created_by varchar default user not null,
	modified_at timestamp default statement_timestamp() not null,
	modified_by varchar default user not null,
	created_by_uid integer,
	modified_by_uid integer,
	constraint host_pk primary key (id),
  constraint host_fac_fk foreign key(facility_id) references facilities(id)
);

-- HOST_ATTR_VALUES - values of attributes assigned to hosts
create table host_attr_values (
	host_id integer not null,  --identifier of host (hosts.id)
	attr_id integer not null,  --identifier of attributes (attr_names.id)
	attr_value text,  --value of attribute
	created_at timestamp default statement_timestamp() not null,
	created_by varchar default user not null,
	modified_at timestamp default statement_timestamp() not null,
	modified_by varchar default user not null,
	created_by_uid integer,
	modified_by_uid integer,
	constraint hostav_pk primary key (host_id,attr_id),
  constraint hostav_host_fk foreign key (host_id) references hosts(id),
  constraint hostav_attr_fk foreign key (attr_id) references attr_names(id)
);

-- HOST_ATTR_U_VALUES - unique attribute values
CREATE TABLE host_attr_u_values (
	host_id INT NOT NULL,
	attr_id INT NOT NULL,
	attr_value text,
	UNIQUE (attr_id, attr_value),
	FOREIGN KEY (host_id,attr_id) REFERENCES host_attr_values ON DELETE CASCADE
);

-- AUDITER_CONSUMERS - registers recently processed events
create table auditer_consumers (
	id integer not null,
	name varchar not null,
	last_processed_id integer,
	created_at timestamp default statement_timestamp() not null,
	created_by varchar default user not null,
	modified_at timestamp default statement_timestamp() not null,
	modified_by varchar default user not null,
	created_by_uid integer,
	modified_by_uid integer,
	constraint audcon_pk primary key (id),
  constraint audcon_u unique(name)
);

-- SERVICES - provided services, their atomic form
create table services (
	id integer not null,
	name varchar not null,    --name of service
	description varchar,
	delay integer not null default 10,
	recurrence integer not null default 2,
	enabled boolean default true not null,
	script varchar not null,
	created_at timestamp default statement_timestamp() not null,
	created_by varchar default user not null,
	modified_at timestamp default statement_timestamp() not null,
	modified_by varchar default user not null,
	created_by_uid integer,
	modified_by_uid integer,
	constraint serv_pk primary key(id),
  constraint serv_u unique(name)
);

-- SERVICE_REQUIRED_ATTRS - list of attributes required by the service
create table service_required_attrs (
	service_id integer not null,   --identifier of service (services.id)
	attr_id integer not null,      --identifier of attribute (attr_names.id)
	created_at timestamp default statement_timestamp() not null,
	created_by varchar default user not null,
	modified_at timestamp default statement_timestamp() not null,
	modified_by varchar default user not null,
	created_by_uid integer,
	modified_by_uid integer,
	constraint srvreqattr_pk primary key (service_id,attr_id),
  constraint srvreqattr_srv_fk foreign key(service_id) references services(id),
  constraint srvreqattr_attr_fk foreign key(attr_id) references attr_names(id)
);

-- SPECIFIC_USER_USERS - relation between specific-users and real users
create table specific_user_users (
	user_id integer not null,          --identifier of real user (users.id)
	specific_user_id integer not null,  --identifier of specific user (users.id)
	created_by_uid integer,
	modified_by_uid integer,
	modified_at timestamp default statement_timestamp() not null,
	type varchar default 'service' not null,
	status integer default 0 not null,   -- 0=enabled or 1=disabled ownership
	constraint acc_specifu_u_pk primary key (user_id,specific_user_id),
  constraint acc_specifu_u_uid_fk foreign key (user_id) references users(id),
  constraint acc_specifu_u_suid_fk foreign key (specific_user_id) references users(id),
  constraint specifu_u_status_chk check (status in (0,1))
);

-- SERVICE_DENIALS - services excluded from ussage
create table service_denials (
	id integer not null,
	service_id integer not null,       --identifier of service (services.id)
	facility_id integer,               --identifier of facility (facilities.id)
	destination_id integer,            --identifier of destination (destinations.id) if service is not excluded on whole service
	created_at timestamp default statement_timestamp() not null,
	created_by varchar default user not null,
	modified_at timestamp default statement_timestamp() not null,
	modified_by varchar default user not null,
	created_by_uid integer,
	modified_by_uid integer,
	constraint srvden_pk primary key (id),
  constraint srvden_srv_fk foreign key (service_id) references services(id),
  constraint srvden_fac_fk foreign key (facility_id) references facilities(id),
  constraint srvden_dest_fk foreign key (destination_id) references destinations(id),
  constraint srvden_u check(service_id is not null and ((facility_id is not null and destination_id is null) or (facility_id is null and destination_id is not null)))
);

-- RESOURCE_SERVICES - services assigned to resource
create table resource_services (
	service_id integer not null,   --identifier of service (services.id)
	resource_id integer not null,  --identifier of resource (resources.id)
	created_at timestamp default statement_timestamp() not null,
	created_by varchar default user not null,
	modified_at timestamp default statement_timestamp() not null,
	modified_by varchar default user not null,
	created_by_uid integer,
	modified_by_uid integer,
	constraint resrcsrv_pk primary key (service_id,resource_id),
  constraint resrcsrv_srv_fk foreign key (service_id) references services(id),
  constraint resrcsrv_rsrc_fk foreign key (resource_id) references resources(id)
);

-- APPLICATION - registration data
create table application (
	id integer not null,
	vo_id integer not null,  --identifier of VO (vos.id)
	user_id integer,         --identifier of user (users.id)
	apptype varchar not null,  --type of application (initial/extension)
	extSourceName varchar,  --name of external source of users
	extSourceType varchar,  --type of external source of users (federation...)
	fed_info text,               --data from federation or cert
	state varchar,           --state of application (new/verified/approved/rejected)
	extSourceLoa integer,  --level of assurance of user by external source
	group_id integer,      --identifier of group (groups.id) if application is for group
	created_at timestamp default statement_timestamp() not null,
	created_by varchar default user not null,
	modified_at timestamp default statement_timestamp() not null,
	modified_by varchar default user not null,
	created_by_uid integer,
	modified_by_uid integer,
	constraint app_pk primary key (id),
  constraint app_vo_fk foreign key (vo_id) references vos(id) on delete cascade,
  constraint app_group_fk foreign key (group_id) references groups(id) on delete cascade,
  constraint app_user_fk foreign key (user_id) references users(id) on delete cascade,
  constraint app_state_chk check (state in ('REJECTED','NEW','VERIFIED','APPROVED'))
);

-- APPLICATION_FORM - form for application into VO or group
create table application_form (
	id integer not null,
	vo_id integer not null,     --identifier of VO (vos.id)
	automatic_approval boolean default false not null, --approval of application is automatic
	automatic_approval_extension boolean default false not null, --approval of extension is automatic
	module_name varchar,  --name of module which processes application
	group_id integer,          --identifier of group (groups.id) if application is for group
	created_by_uid integer,
	modified_by_uid integer,
	constraint applform_pk primary key (id),
  constraint applform_vo_fk foreign key (vo_id) references vos(id) on delete cascade,
  constraint applform_group_fk foreign key (group_id) references groups(id) on delete cascade
);

-- APPLICATION_FORM_ITEMS - items of application form
create table application_form_items (
	id integer not null,
	form_id integer not null,  --identifier of form (application_form.id)
	ordnum integer not null,   --order of item
	shortname varchar not null,  --name of item
	required boolean default false not null,          --value for item is mandatory
	type varchar,         --type of item
	fed_attr varchar,     --copied from federation attribute
	src_attr varchar,     --sourced from attribute
	dst_attr varchar,     --saved to attribute
	regex varchar,       --regular expression for checking of value
	created_by_uid integer,
	modified_by_uid integer,
	constraint applfrmit_pk primary key (id),
  constraint applfrmit_applform foreign key (form_id) references application_form(id) on delete cascade
);

-- APPLICATION_FORM_ITEM_APPTYPES - possible types of app. form items
create table application_form_item_apptypes (
	item_id integer not null,  --identifier of form item (application_form_items.id)
	apptype varchar not null,  --type of item
	created_by_uid integer,
	modified_by_uid integer,
	constraint applfrmittyp_applfrmit_fk foreign key (item_id) references application_form_items(id) on delete cascade
);

-- APPLICATION_FORM_ITEM_TEXTS - texts displayed with the items at app. form
create table application_form_item_texts (
	item_id integer not null,     --identifier of form item (application_form_items.id)
	locale varchar not null, --language for application
	label text,    			      --label of item on app. form
	options text,      			  --options for items with menu
	help varchar,           --text of help
	error_message varchar,  --text of error message
	created_by_uid integer,
	modified_by_uid integer,
	constraint applfrmittxt_pk primary key(item_id,locale),
  constraint applfrmittxt_applfrmit_fk foreign key (item_id) references application_form_items(id) on delete cascade
);

-- APPLICATION_DATA - values of data entered by application form
create table application_data (
	id integer not null,
	app_id integer not null,  --identifier of application (application.id)
	item_id integer,          --identifier of item (application_form_items.id)
	shortname varchar,   --name of item
	value varchar,      --value of item
	assurance_level varchar, --level of assurance of item of newly registered user
	created_by_uid integer,
	modified_by_uid integer,
	constraint appdata_pk primary key (id),
  constraint appdata_app_fk foreign key (app_id) references application(id) on delete cascade,
  constraint appdata_applfrmit_fk foreign key (item_id) references application_form_items(id) on delete cascade
);

-- APPLICATION_MAILS - notification mails sent together with application
create table application_mails (
	id integer not null,
	form_id integer not null,       --identifier of form (application_form.id)
	app_type varchar not null,  --application type (initial/extension)
	mail_type varchar not null, --type of mail (user/administrator)
	send boolean default false not null,       --sent (Y/N)
	created_by_uid integer,
	modified_by_uid integer,
	constraint appmails_pk primary key (id),
  constraint appmails_u unique (form_id,app_type,mail_type),
  constraint appmail_appform_fk foreign key (form_id) references application_form(id) on delete cascade
);

-- APPLICATION_MAIL_TEXTS - texts of notification mails
create table application_mail_texts (
	mail_id integer not null,     --identifier of mail (application_mails.id)
	locale varchar not null,  --language for texts
	subject varchar,        --subject of mail
	text varchar,           --text of mail
	created_by_uid integer,
	modified_by_uid integer,
	constraint appmailtxt_pk primary key (mail_id, locale),
  constraint appmailtxt_appmails_fk foreign key (mail_id) references application_mails(id) on delete cascade
);

-- APPLICATION_RESERVED_LOGINS - lognames reserved for new users who has not been saved at users table yet
create table application_reserved_logins (
	login varchar not null,        --logname
	namespace varchar not null,     --namespace where logname is reserved
	app_id integer not null,            --identifier of application (application.id)
	created_by varchar default user not null,
	created_at timestamp default statement_timestamp() not null,
	created_by_uid integer,
	modified_by_uid integer,
	constraint app_logins_pk primary key(login, namespace),
  constraint applogin_appid_fk foreign key(app_id) references application(id)
);

-- FACILITY_SERVICE_DESTINATIONS - destinations of services assigned to the facility
create table facility_service_destinations (
	service_id integer not null,   --identifier of service (services.id)
	facility_id integer not null,  --identifier of facility (facilities.id)
	destination_id integer not null, --identifier of destination (destinations.id)
	created_at timestamp default statement_timestamp() not null,
	created_by varchar default user not null,
	modified_at timestamp default statement_timestamp() not null,
	modified_by varchar default user not null,
	created_by_uid integer,
	modified_by_uid integer,
	propagation_type varchar default 'PARALLEL',
	constraint dest_srv_fk foreign key (service_id) references services(id),
  constraint dest_fac_fk foreign key (facility_id) references facilities(id),
  constraint dest_dest_fk foreign key(destination_id) references destinations(id)
);

-- ENTITYLESS_ATTR_VALUES - value of attributes which are not assigned to any entity
create table entityless_attr_values (
	subject varchar not null,  --indicator of subject assigned with attribute
	attr_id integer not null,       --identifier of attribute (attr_names.id)
	attr_value text,       --attribute value
	created_at timestamp default statement_timestamp() not null,
	created_by varchar default user not null,
	modified_at timestamp default statement_timestamp() not null,
	modified_by varchar default user not null,
	created_by_uid integer,
	modified_by_uid integer,
	constraint entlatval_pk primary key(subject,attr_id),
  constraint entlatval_attr_fk foreign key (attr_id) references attr_names(id)
);

-- FACILITY_ATTR_VALUES - attribute values assigned to facility
create table facility_attr_values (
	facility_id integer not null,   --identifier of facility (facilities.id)
	attr_id integer not null,       --identifier of attribute (attr_names.id)
	attr_value text,       --attribute value
	created_at timestamp default statement_timestamp() not null,
	created_by varchar default user not null,
	modified_at timestamp default statement_timestamp() not null,
	modified_by varchar default user not null,
	created_by_uid integer,
	modified_by_uid integer,
	constraint facattval_pk primary key (facility_id,attr_id),
  constraint facattval_nam_fk foreign key (attr_id) references attr_names(id),
  constraint facattval_fac_fk foreign key (facility_id) references facilities (id)
);

-- FACILITY_ATTR_U_VALUES - unique attribute values
CREATE TABLE facility_attr_u_values (
	facility_id INT NOT NULL,
	attr_id INT NOT NULL,
	attr_value text,
	UNIQUE (attr_id, attr_value),
	FOREIGN KEY (facility_id,attr_id) REFERENCES facility_attr_values ON DELETE CASCADE
);


-- GROUP_ATTR_VALUES - attribute values assigned to groups
create table group_attr_values (
	group_id integer not null,     --identifier of group (groups.id)
	attr_id integer not null,      --identifier of attribute (attr_names.id)
	attr_value text,      --attribute value
	created_at timestamp default statement_timestamp() not null,
	created_by varchar default user not null,
	modified_at timestamp default statement_timestamp() not null,
	modified_by varchar default user not null,
	created_by_uid integer,
	modified_by_uid integer,
	constraint grpattval_pk primary key (group_id,attr_id),
  constraint grpattval_grp_fk foreign key (group_id) references groups(id),
  constraint grpattval_attr_fk foreign key (attr_id) references attr_names(id)
);

-- GROUP_ATTR_U_VALUES - unique attribute values
CREATE TABLE group_attr_u_values (
	group_id INT NOT NULL,
	attr_id INT NOT NULL,
	attr_value text,
	UNIQUE (attr_id, attr_value),
	FOREIGN KEY (group_id,attr_id) REFERENCES group_attr_values ON DELETE CASCADE
);

-- RESOURCE_ATTR_VALUES - attribute values assigned to resources
create table resource_attr_values (
	resource_id integer not null,   --identifier of resource (resources.id)
	attr_id integer not null,       --identifier of attribute (attr_names.id)
	attr_value text,       --attribute value
	created_at timestamp default statement_timestamp() not null,
	created_by varchar default user not null,
	modified_at timestamp default statement_timestamp() not null,
	modified_by varchar default user not null,
	created_by_uid integer,
	modified_by_uid integer,
	constraint resatval_pk primary key (resource_id,attr_id),
  constraint resatval_res_fk foreign key(resource_id) references resources(id),
  constraint resatval_resatnam_fk foreign key(attr_id) references attr_names(id)
);

-- RESOURCE_ATTR_U_VALUES - unique attribute values
CREATE TABLE resource_attr_u_values (
	resource_id INT NOT NULL,
	attr_id INT NOT NULL,
	attr_value text,
	UNIQUE (attr_id, attr_value),
	FOREIGN KEY (resource_id,attr_id) REFERENCES resource_attr_values ON DELETE CASCADE
);

-- GROUP_RESOURCE_ATTR_VALUES - attribute values assigned to groups and resources
create table group_resource_attr_values (
	group_id integer not null,     --identifier of group (groups.id)
	resource_id integer not null,  --identifier of resource (resources.id)
	attr_id integer not null,      --identifier of attribute (attr_names.id)
	attr_value text,      --attribute value
	created_at timestamp default statement_timestamp() not null,
	created_by varchar default user not null,
	modified_at timestamp default statement_timestamp() not null,
	modified_by varchar default user not null,
	created_by_uid integer,
	modified_by_uid integer,
	constraint grpresav_pk primary key (group_id,resource_id,attr_id),
  constraint grpresav_grp_fk foreign key (group_id) references groups(id),
  constraint grpresav_res_fk foreign key (resource_id) references resources(id),
  constraint grpresav_attr_fk foreign key (attr_id) references attr_names(id)
);

-- GROUP_RESOURCE_ATTR_U_VALUES - unique attribute values
CREATE TABLE group_resource_attr_u_values (
	group_id INT NOT NULL,
	resource_id INT NOT NULL,
	attr_id INT NOT NULL,
	attr_value text,
	UNIQUE (attr_id, attr_value),
	FOREIGN KEY (group_id,resource_id,attr_id) REFERENCES group_resource_attr_values ON DELETE CASCADE
);

-- GROUPS_MEMBERS - members of groups
create table groups_members (
	group_id integer not null,   --identifier of group (groups.id)
	member_id integer not null,  --identifier of member (members.id)
	created_at timestamp default statement_timestamp() not null,
	created_by varchar default user not null,
	modified_at timestamp default statement_timestamp() not null,
	modified_by varchar default user not null,
	source_group_status integer not null default 0,
	created_by_uid integer,
	modified_by_uid integer,
	membership_type integer not null,  --identifier of membership type (membersip_types.id)
	source_group_id integer not null,   --identifier of parent group (groups.id) if any
	constraint grpmem_pk primary key (member_id,group_id, source_group_id),
  constraint grpmem_gr_fk foreign key (group_id) references groups(id),
  constraint grpmem_mem_fk foreign key (member_id) references members(id),
  constraint grpmem_memtype_fk foreign key (membership_type) references membership_types(id)
);

-- GROUPS_RESOURCES - groups assigned to resource
create table groups_resources (
	group_id integer not null,     --identifier of group (groups.id)
	resource_id integer not null,  --identifier of resource (resources.id)
	created_at timestamp default statement_timestamp() not null,
	created_by varchar default user not null,
	modified_at timestamp default statement_timestamp() not null,
	modified_by varchar default user not null,
	created_by_uid integer,
	modified_by_uid integer,
	constraint grres_grp_res_u unique (group_id,resource_id),
  constraint grres_gr_fk foreign key (group_id) references groups(id),
  constraint grres_res_fk foreign key (resource_id) references resources(id)
);

-- MEMBER_ATTR_VALUES - values of attributes assigned to members
create table member_attr_values (
	member_id integer not null,   --identifier of member (members.id)
	attr_id integer not null,     --identifier of attribute (attr_names.id)
	attr_value text,     --attribute value
	created_at timestamp default statement_timestamp() not null,
	created_by varchar default user not null,
	modified_at timestamp default statement_timestamp() not null,
	modified_by varchar default user not null,
	created_by_uid integer,
	modified_by_uid integer,
	constraint memattval_pk primary key (member_id,attr_id),
  constraint memattval_mem_fk foreign key (member_id) references members(id),
  constraint memattval_attr_fk foreign key (attr_id) references attr_names(id)
);

-- MEMBER_ATTR_U_VALUES -- unique attribute values
CREATE TABLE member_attr_u_values (
	member_id INT NOT NULL,
	attr_id INT NOT NULL,
	attr_value text,
	UNIQUE (attr_id, attr_value),
	FOREIGN KEY (member_id,attr_id) REFERENCES member_attr_values ON DELETE CASCADE
);

-- MEMBER_GROUP_ATTR_VALUES - values of attributes assigned to members in groups
create table member_group_attr_values (
	member_id integer not null,   --identifier of member (members.id)
	group_id integer not null, --identifier of group (groups.id)
	attr_id integer not null,     --identifier of attribute (attr_names.id)
	attr_value text,     --attribute value
	created_at timestamp default statement_timestamp() not null,
	created_by varchar default user not null,
	modified_at timestamp default statement_timestamp() not null,
	modified_by varchar default user not null,
	created_by_uid integer,
	modified_by_uid integer,
	constraint memgav_pk primary key(member_id,group_id,attr_id),
  constraint memgav_mem_fk foreign key (member_id) references members(id),
  constraint memgav_grp_fk foreign key (group_id) references groups(id),
  constraint memgav_accattnam_fk foreign key (attr_id) references attr_names(id)
);

-- MEMBER_GROUP_ATTR_U_VALUES - unique attribute values
CREATE TABLE member_group_attr_u_values (
	member_id INT NOT NULL,
	group_id INT NOT NULL,
	attr_id INT NOT NULL,
	attr_value text,
	UNIQUE (attr_id, attr_value),
	FOREIGN KEY (member_id,group_id,attr_id) REFERENCES member_group_attr_values ON DELETE CASCADE
);

-- MEMBER_RESOURCE_ATTR_VALUES - values of attributes assigned to members on resources
create table member_resource_attr_values (
	member_id integer not null,   --identifier of member (members.id)
	resource_id integer not null, --identifier of resource (resources.id)
	attr_id integer not null,     --identifier of attribute (attr_names.id)
	attr_value text,     --attribute value
	created_at timestamp default statement_timestamp() not null,
	created_by varchar default user not null,
	modified_at timestamp default statement_timestamp() not null,
	modified_by varchar default user not null,
	created_by_uid integer,
	modified_by_uid integer,
	constraint memrav_pk primary key(member_id,resource_id,attr_id),
	constraint memrav_mem_fk foreign key (member_id) references members(id),
	constraint memrav_rsrc_fk foreign key (resource_id) references resources(id),
	constraint memrav_accattnam_fk foreign key (attr_id) references attr_names(id)
);

-- MEMBER_RESOURCE_ATTR_U_VALUES - unique attribute values
CREATE TABLE member_resource_attr_u_values (
	member_id INT NOT NULL,
	resource_id INT NOT NULL,
	attr_id INT NOT NULL,
	attr_value text,
	UNIQUE (attr_id, attr_value),
	FOREIGN KEY (member_id,resource_id,attr_id) REFERENCES member_resource_attr_values ON DELETE CASCADE
);

-- USER_ATTR_VALUES - values of attributes assigned to users
create table user_attr_values (
	user_id integer not null,  --identifier of user (users.id)
	attr_id integer not null,  --identifier of attribute (attr_names.id)
	attr_value text,  --attribute value
	created_at timestamp default statement_timestamp() not null,
	created_by varchar default user not null,
	modified_at timestamp default statement_timestamp() not null,
	modified_by varchar default user not null,
	created_by_uid integer,
	modified_by_uid integer,
	constraint usrav_pk primary key(user_id,attr_id),
	constraint usrav_usr_fk foreign key (user_id) references users(id),
  constraint usrav_accattnam_fk foreign key (attr_id) references attr_names(id)
);

-- USER_ATTR_U_VALUE - unique attribute values
CREATE TABLE user_attr_u_values (
	user_id  INT NOT NULL,
	attr_id  INT NOT NULL,
	attr_value text,
	UNIQUE (attr_id, attr_value),
	FOREIGN KEY (user_id,attr_id) REFERENCES user_attr_values ON DELETE CASCADE
);

-- USER_FACILITY_ATTR_VALUES - values of attributes assigned to users on facilities
create table user_facility_attr_values (
	user_id integer not null,     --identifier of user (users.id)
	facility_id integer not null, --identifier of facility (facilities.id)
	attr_id integer not null,     --identifier of attribute (attr_names.id)
	attr_value text,     --attribute value
	created_at timestamp default statement_timestamp() not null,
	created_by varchar default user not null,
	modified_at timestamp default statement_timestamp() not null,
	modified_by varchar default user not null,
	created_by_uid integer,
	modified_by_uid integer,
	constraint usrfacav_u primary key(user_id,facility_id,attr_id),
	constraint usrfacav_mem_fk foreign key (user_id) references users(id),
  constraint usrfacav_fac_fk foreign key (facility_id) references facilities(id),
  constraint usrfacav_accattnam_fk foreign key (attr_id) references attr_names(id)
);

-- USER_FACILITY_ATTR_U_VALUES - unique attribute values
CREATE TABLE user_facility_attr_u_values (
	user_id INT NOT NULL,
	facility_id INT NOT NULL,
	attr_id INT NOT NULL,
	attr_value text,
	UNIQUE (attr_id, attr_value),
	FOREIGN KEY (user_id,facility_id,attr_id) REFERENCES user_facility_attr_values ON DELETE CASCADE
);

-- VO_ATTR_VALUES - attributes specific for VO
create table vo_attr_values (
	vo_id integer not null,    --identifier of VO (vos.id)
	attr_id integer not null,  --identifier of attribute (attr_names.id)
	attr_value text,  --attribute value
	created_at timestamp default statement_timestamp() not null,
	created_by varchar default user not null,
	modified_at timestamp default statement_timestamp() not null,
	modified_by varchar default user not null,
	created_by_uid integer,
	modified_by_uid integer,
	constraint voattval_pk primary key (vo_id,attr_id),
  constraint voattval_nam_fk foreign key (attr_id) references attr_names(id),
  constraint voattval_vo_fk foreign key (vo_id) references vos (id)
);

-- VO_ATTR_U_VALUES - unique attribute values
CREATE TABLE vo_attr_u_values (
	vo_id INT NOT NULL,
	attr_id INT NOT NULL,
	attr_value text,
	UNIQUE (attr_id, attr_value),
	FOREIGN KEY (vo_id,attr_id) REFERENCES vo_attr_values ON DELETE CASCADE
);

-- EXT_SOURCES - external sources from which we can gain data about users
create table ext_sources (
	id integer not null,
	name varchar not null,    --name of source
	type varchar,              --type of source (LDAP/IdP...)
	created_at timestamp default statement_timestamp() not null,
	created_by varchar default user not null,
	modified_at timestamp default statement_timestamp() not null,
	modified_by varchar default user not null,
	created_by_uid integer,
	modified_by_uid integer,
	constraint usrsrc_pk primary key(id),
  constraint usrsrc_u unique (name)
);

-- EXT_SOURCES_ATTRIBUTES - values of attributes of external sources
create table ext_sources_attributes (
	ext_sources_id integer not null,   --identifier of ext. source (ext_sources.id)
	attr_name varchar not null,   --name of attribute at ext. source
	attr_value text,          --value of attribute
	created_at timestamp default statement_timestamp() not null,
	created_by varchar default user not null,
	modified_at timestamp default statement_timestamp() not null,
	modified_by varchar default user not null,
	created_by_uid integer,
	modified_by_uid integer,
	constraint usrcatt_usrc_fk foreign key (ext_sources_id) references ext_sources(id)
);

-- VO_EXT_SOURCES - external sources assigned to VO
create table vo_ext_sources (
	vo_id integer not null,          --identifier of VO (vos.id)
	ext_sources_id integer not null, --identifier of ext. source (ext_sources.id)
	created_at timestamp default statement_timestamp() not null,
	created_by varchar default user not null,
	modified_at timestamp default statement_timestamp() not null,
	modified_by varchar default user not null,
	created_by_uid integer,
	modified_by_uid integer,
	constraint vousrsrc_pk primary key (vo_id,ext_sources_id),
  constraint vousrsrc_usrsrc_fk foreign key(ext_sources_id) references ext_sources(id),
  constraint vousrsrc_vos_fk foreign key(vo_id) references vos(id)
);

-- GROUP_EXT_SOURCES - external source assigned to GROUP
create table group_ext_sources (
	group_id integer not null,
	ext_source_id integer not null,
	created_at timestamp default statement_timestamp() not null,
	created_by varchar default user not null,
	modified_at timestamp default statement_timestamp() not null,
	modified_by varchar default user not null,
	created_by_uid integer,
	modified_by_uid integer,
	constraint groupsrc_pk primary key (group_id,ext_source_id),
  constraint groupsrc_src_fk foreign key(ext_source_id) references ext_sources(id),
  constraint groupsrc_groups_fk foreign key(group_id) references groups(id)
);

-- USER_EXT_SOURCES - external source from which user come (identification of user in his home system)
create table user_ext_sources (
	id integer not null,
	user_id integer not null,          --identifier of user (users.id)
	login_ext varchar not null,   --logname from his home system
	ext_sources_id integer not null,   --identifier of ext. source (ext_sources.id)
	created_at timestamp default statement_timestamp() not null,
	created_by varchar default user not null,
	modified_at timestamp default statement_timestamp() not null,
	modified_by varchar default user not null,
	loa integer,                       --level of assurance
	last_access timestamp default statement_timestamp() not null, --time of last user's access (to Perun) by using this external source
	created_by_uid integer,
	modified_by_uid integer,
	constraint usrex_p primary key(id),
  constraint usrex_u unique (ext_sources_id,login_ext),
  constraint usrex_usr_fk foreign key (user_id) references users(id),
  constraint usrex_usersrc_fk foreign key(ext_sources_id) references ext_sources(id)
);

-- SERVICE_PACKAGES - possible groups of services
create table service_packages (
	id integer not null,
	name varchar not null,   --name of service package
	description varchar,     --purpose,description
	created_at timestamp default statement_timestamp() not null,
	created_by varchar default user not null,
	modified_at timestamp default statement_timestamp() not null,
	modified_by varchar default user not null,
	created_by_uid integer,
	modified_by_uid integer,
	constraint pkg_pk primary key (id),
  constraint pkg_name unique(name)
);

-- SERVICE_SERVICE_PACKAGES - groups of services which should to be executed together however at specific order
create table service_service_packages (
	service_id integer not null,   --identifier of service (services.id)
	package_id integer not null,   --identifier of package (service_packages.id)
	created_at timestamp default statement_timestamp() not null,
	created_by varchar default user not null,
	modified_at timestamp default statement_timestamp() not null,
	modified_by varchar default user not null,
	created_by_uid integer,
	modified_by_uid integer,
	constraint srvpkg_srv_pk primary key(service_id,package_id),
  constraint srvpkg_srv_fk foreign key(service_id) references services(id),
  constraint srvpkg_pkg_fk foreign key(package_id) references service_packages(id)
);

-- TASKS - contains planned services and services finished at near past
create table tasks (
	id integer not null,
	service_id integer not null,        --identifier of executed service (services.id)
	facility_id integer not null,      --identifier of target facility (facilities.id)
	schedule timestamp not null,        --planned time for starting task
	recurrence integer not null,        --number of repeating of task in case of error
	delay integer not null,             --delay after next executing in case of error
	status varchar not null,        --state of task
	start_time timestamp,                    --real start time of task
	end_time timestamp,                      --real end time of task
	engine_id integer default 1,          --identifier of engine which is executing the task
	created_at timestamp default statement_timestamp() not null,
	err_message varchar,          --return message in case of error
	created_by_uid integer,
	modified_by_uid integer,
	constraint task_pk primary key (id),
  constraint task_u unique (service_id, facility_id),
  constraint task_srv_fk foreign key (service_id) references services(id),
  constraint task_fac_fk foreign key (facility_id) references facilities(id),
  constraint task_stat_chk check (status in ('WAITING', 'PLANNED', 'GENERATING', 'GENERROR', 'GENERATED', 'SENDING', 'DONE', 'SENDERROR', 'ERROR', 'WARNING'))
);

-- TASKS_RESULTS - contains partial results of tasks (executing, waiting and at near past finished)
create table tasks_results (
	id integer not null,
	task_id integer not null,         --identifier of task (tasks.id)
	destination_id integer not null,  --identifier of destination (destinations.id)
	status varchar not null,      --status of task
	err_message varchar,        --return message in case of error
	std_message varchar,        --return message in case of success
	return_code integer,              --returned value
	timestamp timestamp,                   --real time of executing
	engine_id integer default 1 not null,       --identifier of executing engine
	created_at timestamp default statement_timestamp() not null,
	created_by varchar default user not null,
	modified_at timestamp default statement_timestamp() not null,
	modified_by varchar default user not null,
	created_by_uid integer,
	modified_by_uid integer,
	constraint taskres_pk primary key (id),
	constraint taskres_task_fk foreign key (task_id) references tasks(id),
  constraint taskres_dest_fk foreign key (destination_id) references destinations(id),
  constraint taskres_stat_chk check (status in ('DONE','ERROR','DENIED', 'WARNING'))
);

create table auditer_log (
	id integer not null,         --identifier of logged event
	msg text not null,           --text of logging message
	actor varchar not null, --who causes the event
	created_at timestamp default statement_timestamp() not null,
	created_by_uid integer,
	modified_by_uid integer,
	constraint audlog_pk primary key (id)
);

-- RESERVED_LOGINS - reserved lognames, actually is not used. Prepared for reservation by core.
create table reserved_logins (
	login varchar,        --logname
	namespace varchar,    --namespace in which is logname using
	application varchar,  --relation to application if any
	id varchar,
	created_by_uid integer,
	modified_by_uid integer,
	constraint reservlogins_pk primary key (login,namespace)
);

-- PN_AUDIT_MESSAGE - Contains all messages retrieved from the auditer log, since the notification module is auditer consumer. These messages are waiting to be processed by the notification module
create table pn_audit_message (
	message text,
	id integer NOT NULL,
	created_by_uid integer,
	modified_by_uid integer,
	constraint pn_audmsg_pk primary key (id)
);

-- PN_OBJECT - Keeps names of the Perun beans and their properties, for the recognition in a regular expression in the notification module
create table pn_object (
	id integer NOT NULL,
	name varchar,        --arbitrary name
	properties varchar, --set of names of methods divided by ';'
	class_name varchar,  --the whole java class name of the object, e.g. 'cz.metacentrum.perun.core.api.Vo'
	created_by_uid integer,
	modified_by_uid integer,
	constraint pn_object_pk primary key (id)
);

-- PN_TEMPLATE - Contains templates for creating the message in the notification module
create table pn_template (
	id integer NOT NULL,
	primary_properties varchar NOT NULL, --important attributes, which messages will be grouped on, same as later PN_POOL_MESSAGE.KEY_ATTRIBUTES
	notify_trigger varchar, --configures two approaches to the grouping messages,
	--when equals 'ALL_REGEX_IDS', the pool messages are grouped and sent when all needed audit messages are collected
	--when equals 'STREAM', the related pool messages are waiting for the certain amount of time and then sent
	youngest_message_time integer, --time limit for the youngest message
	oldest_message_time integer,   --time limit for the oldest message
	name varchar,       --arbitrary name
	sender varchar,    --email addres that will be stated as sender
	created_by_uid integer,
	modified_by_uid integer,
	constraint pn_tmpl_pk primary key (id)
);

-- PN_POOL_MESSAGE - Contains all messages, which are already processed by the notification module. The mesages will be grouped in this table before sending
create table pn_pool_message (
	id integer NOT NULL,
	regex_id integer NOT NULL,   --references a regular expression binded to a message
	template_id integer NOT NULL, --references a template binded to a message
	key_attributes varchar NOT NULL, --contains all attributes extracted from audit message that are important for grouping messages
	created timestamp default statement_timestamp() NOT NULL, --the time of the pool message creation, important when PN_TEMPLATE.NOTIFY_TRIGGER is set to 'stream'
	notif_message text NOT NULL, --contains original audit message, important when gaining attributes in template message
	created_by_uid integer,
	modified_by_uid integer,
	constraint pn_poolmsg_pk primary key (id),
  constraint pn_poolmsg_tmpl_fk foreign key (template_id) references pn_template(id)
);

-- PN_RECEIVER - Keeps information about receiver of messages from notification module
create table pn_receiver (
	id integer NOT NULL,
	target varchar NOT NULL, --the email address or jabber of the receiver
	type_of_receiver varchar NOT NULL, --available options are email_user/email_group/jabber
	template_id integer NOT NULL, --reference to the pn_template
	created_by_uid integer,
	modified_by_uid integer,
	locale varchar,           ----the message language and formating is based on locale
	constraint pn_receiver_pk primary key (id),
  constraint pn_receiver_tmpl_fk foreign key (template_id) references pn_template(id)
);

-- PN_REGEX - Keeps regular expressions, which are used to parse audit messages in the notification module
create table pn_regex (
	id integer NOT NULL,
	note varchar, --comment to the regex
	regex varchar NOT NULL, --the regular expression
	created_by_uid integer,
	modified_by_uid integer,
	constraint pn_regex_pk primary key (id)
);

-- PN_TEMPLATE_MESSAGE - Contains text messages that the notification module sends. The freemaker syntax can be used in the message, so the content is dynamic and is able to contain attributes from the audit message
create table pn_template_message (
	id integer NOT NULL,
	template_id integer NOT NULL,  --reference to the pn_template
	locale varchar NOT NULL,    --the message language and formating is based on locale
	message varchar,      --text message
	created_by_uid integer,
	modified_by_uid integer,
	subject varchar not null,  --text, which is used as subject of the message
	constraint pn_tmplmsg_pk primary key (id),
  constraint pn_tmplmsg_tmpl_fk foreign key (template_id) references pn_template(id) deferrable
);

-- PN_TEMPLATE_REGEX - Represents relation between pn_template and pn_regex in the notification module
create table pn_template_regex (
	regex_id integer NOT NULL, --referecnce to the regular expression
	template_id integer NOT NULL,  --reference to the pn_template
	id integer NOT NULL,
	created_by_uid integer,
	modified_by_uid integer,
	constraint pn_tmplrgx_pk primary key (id),
  constraint pn_tmplrgx_rgx_fk foreign key (regex_id) references pn_regex(id),
  constraint pn_tmplrgx_tmpl_fk foreign key (template_id) references pn_template(id)
);

-- PN_REGEX_OBJECT - Represents relation between pn_regex and pn_object in the notification module
create table pn_regex_object (
	id integer NOT NULL,
	regex_id integer NOT NULL,  --referecnce to the pn_regex
	object_id integer NOT NULL,  --reference to the pn_object
	created_by_uid integer,
	modified_by_uid integer,
	constraint pn_rgxobj_pk primary key (id),
  constraint pn_rgxobj_rgx_fk foreign key (regex_id) references pn_regex(id),
  constraint pn_rgxobj_obj_fk foreign key (object_id) references pn_object(id)
);

-- GROUPS_GROUPS - Groups relations (union,subgroups)
create table groups_groups (
	result_gid integer not null,         --identifier of group
	operand_gid integer not null,        --identifier of operand group (unioned / parent group)
	parent_flag boolean default false,
	created_at timestamp default statement_timestamp() not null,
	created_by varchar default user not null,
	modified_at timestamp default statement_timestamp() not null,
	modified_by varchar default user not null,
	constraint grp_grp_pk primary key (result_gid,operand_gid),
  constraint grp_grp_rgid_fk foreign key (result_gid) references groups(id),
  constraint grp_grp_ogid_fk foreign key (operand_gid) references groups(id)
);

-- RES_TAGS - possible resource tags in VO
create table res_tags (
	id integer not null,
	vo_id integer not null,            --identifier of VO
	tag_name varchar not null,  --name of tag (computationl node/storage...)
	created_at timestamp default statement_timestamp() not null,
	created_by varchar default user not null,
	modified_at timestamp default statement_timestamp() not null,
	modified_by varchar default user not null,
	created_by_uid integer,
	modified_by_uid integer,
	constraint restags_pk primary key (id),
  constraint restags_u unique (vo_id, tag_name),
  constraint restags_vos_fk foreign key (vo_id) references vos(id)
);

-- TAGS_RESOURCES - relation between tags and resources
create table tags_resources (
	tag_id integer not null,      --identifier of tag (res_tags.id)
	resource_id integer not null, --identifier of resource
	constraint tags_res_pk primary key (tag_id,resource_id),
  constraint tags_res_tags_fk foreign key (tag_id) references res_tags(id),
  constraint tags_res_res_fk foreign key (resource_id) references resources(id)
);

-- CONFIGURATIONS - system Perun configuration
create table configurations (
	property varchar not null,  --property (for example database version)
	value varchar not null,     --value of configuration property
	constraint config_pk primary key (property)
);

-- MAILCHANGE - allow to user to change mail address, temporairly saved mails during change is in progress
create table mailchange (
	id integer not null,
	value text not null,      --
	user_id integer not null, --identifier of user (users.id)
	created_at timestamp default statement_timestamp() not null,
	created_by varchar default user not null,
	created_by_uid integer,
	constraint mailchange_pk primary key (id),
  constraint mailchange_u_fk foreign key (user_id) references users(id)
);

--PWDRESET - allows to user to change passwd
create table pwdreset (
	id integer not null,
	namespace text not null,
	mail text,
	user_id integer not null,
	validity_to timestamp not null,
	created_at timestamp default statement_timestamp() not null,
	created_by varchar default user not null,
	created_by_uid integer,
	constraint pwdreset_pk primary key (id),
  constraint pwdreset_u_fk foreign key (user_id) references users(id)
);

create table security_teams (
	id integer not null,
	name varchar not null,
	description varchar,
	created_at timestamp default statement_timestamp() not null,
	created_by varchar default user not null,
	modified_at timestamp default statement_timestamp() not null,
	modified_by varchar default user not null,
	created_by_uid integer,
	modified_by_uid integer,
	constraint security_teams_pk primary key (id)
);

create table security_teams_facilities (
	security_team_id integer not null,
	facility_id integer not null,
	created_at timestamp default statement_timestamp() not null,
	created_by varchar default user not null,
	modified_at timestamp default statement_timestamp() not null,
	modified_by varchar default user not null,
	created_by_uid integer,
	modified_by_uid integer,
	constraint security_teams_facilities_pk primary key (security_team_id, facility_id),
  constraint security_teams_facilities_security_team_fk foreign key (security_team_id) references security_teams(id),
  constraint security_teams_facilities_facilities_fk foreign key (facility_id) references facilities(id)
);

create table blacklists (
	security_team_id integer not null,
	user_id integer not null,
	description varchar,
	created_at timestamp default statement_timestamp() not null,
	created_by varchar default user not null,
	modified_at timestamp default statement_timestamp() not null,
	modified_by varchar default user not null,
	created_by_uid integer,
	modified_by_uid integer,
	constraint bllist_pk primary key (security_team_id,user_id),
  constraint bllist_secteam_fk foreign key (security_team_id) references security_teams (id),
  constraint bllist_user_fk foreign key (user_id) references users(id)
);

create table vos_bans (
	id integer not null,
	member_id integer not null,
	vo_id integer not null,
	description varchar,
	banned_to timestamp default '2999-01-01 00:00:00' not null,
	created_at timestamp default statement_timestamp() not null,
	created_by varchar default user not null,
	modified_at timestamp default statement_timestamp() not null,
	modified_by varchar default user not null,
	created_by_uid integer,
	modified_by_uid integer,
	constraint vos_bans_pk primary key (id),
	constraint vos_bans_u unique (member_id),
	constraint vos_bans_mem_fk foreign key (member_id) references members (id),
	constraint vos_bans_vo_fk foreign key (vo_id) references vos (id)
);

create table resources_bans (
	id integer not null,
	member_id integer not null,
	resource_id integer not null,
	description varchar,
	banned_to timestamp not null,
	created_at timestamp default statement_timestamp() not null,
	created_by varchar default user not null,
	modified_at timestamp default statement_timestamp() not null,
	modified_by varchar default user not null,
	created_by_uid integer,
	modified_by_uid integer,
	constraint res_bans_pk primary key (id),
  constraint res_bans_u unique (member_id, resource_id),
  constraint res_bans_mem_fk foreign key (member_id) references members (id),
  constraint res_bans_res_fk foreign key (resource_id) references resources (id)
);

create table facilities_bans (
	id integer not null,
	user_id integer not null,
	facility_id integer not null,
	description varchar,
	banned_to timestamp not null,
	created_at timestamp default statement_timestamp() not null,
	created_by varchar default user not null,
	modified_at timestamp default statement_timestamp() not null,
	modified_by varchar default user not null,
	created_by_uid integer,
	modified_by_uid integer,
	constraint fac_bans_pk primary key (id),
  constraint fac_bans_u unique (user_id, facility_id),
  constraint fac_bans_usr_fk foreign key (user_id) references users (id),
  constraint fac_bans_fac_fk foreign key (facility_id) references facilities (id)
);

create table user_ext_source_attr_values (
	user_ext_source_id integer not null,
	attr_id integer not null,
	attr_value text,
	created_at timestamp default statement_timestamp() not null,
	created_by varchar default user not null,
	modified_at timestamp default statement_timestamp() not null,
	modified_by varchar default user not null,
	created_by_uid integer,
	modified_by_uid integer,
	constraint uesattrval_pk primary key (user_ext_source_id, attr_id),
  constraint uesattrval_ues_fk foreign key (user_ext_source_id) references user_ext_sources(id),
  constraint uesattrval_attr_fk foreign key (attr_id) references attr_names(id)
);

-- USER_EXT_SOURCE_ATTR_U_VALUES - unique attribute values
CREATE TABLE user_ext_source_attr_u_values (
	user_ext_source_id INT NOT NULL,
	attr_id    INT NOT NULL,
	attr_value text,
	UNIQUE (attr_id, attr_value),
	FOREIGN KEY (user_ext_source_id,attr_id) REFERENCES user_ext_source_attr_values ON DELETE CASCADE
);

CREATE TABLE members_sponsored (
	active boolean default true not null,
	sponsored_id INTEGER NOT NULL,
	sponsor_id INTEGER NOT NULL,
	validity_to timestamp default null,
	created_at timestamp default statement_timestamp() not null,
	created_by varchar default user not null,
	created_by_uid integer,
	modified_at timestamp default statement_timestamp() not null,
	modified_by varchar default user not null,
	modified_by_uid integer,
	constraint memspons_mem_fk foreign key (sponsored_id) references members(id),
  constraint memspons_usr_fk foreign key (sponsor_id) references users(id),
  constraint memspons_mem_usr_u unique (sponsored_id, sponsor_id)
);

-- AUTHZ - assigned roles to users/groups/VOs/other entities...
create table authz (
	user_id integer,          --identifier of user
	role_id integer not null, --identifier of role
	vo_id integer,            --identifier of VO
	facility_id integer,      --identifier of facility
	member_id integer,        --identifier of member
	group_id integer,         --identifier of group
	service_id integer,       --identifier of service
	resource_id integer,      --identifier of resource
	sponsored_user_id integer, --identifier of sponsored user
	created_by_uid integer,
	modified_by_uid integer,
	authorized_group_id integer, --identifier of whole authorized group
	security_team_id integer,	--identifier of security team
	constraint authz_role_fk foreign key (role_id) references roles(id),
	constraint authz_user_fk foreign key (user_id) references users(id),
	constraint authz_authz_group_fk foreign key (authorized_group_id) references groups(id),
	constraint authz_vo_fk foreign key (vo_id) references vos(id),
	constraint authz_fac_fk foreign key (facility_id) references facilities(id),
	constraint authz_mem_fk foreign key (member_id) references members(id),
	constraint authz_group_fk foreign key (group_id) references groups(id),
	constraint authz_service_fk foreign key (service_id) references services(id),
	constraint authz_res_fk foreign key (resource_id) references resources(id),
	constraint authz_sponsu_fk foreign key (sponsored_user_id) references users(id),
	constraint authz_sec_team_fk foreign key (security_team_id) references security_teams(id),
	constraint authz_user_autgrp_chk check
	     ((user_id is not null and authorized_group_id is null) or (user_id is null and authorized_group_id is not null))
);


create sequence "attr_names_id_seq";
create sequence "auditer_consumers_id_seq";
create sequence "auditer_log_id_seq";
create sequence "destinations_id_seq";
create sequence "ext_sources_id_seq";
create sequence "facilities_id_seq";
create sequence "groups_id_seq";
create sequence "hosts_id_seq";
create sequence "members_id_seq";
create sequence "owners_id_seq";
create sequence "resources_id_seq";
create sequence "services_id_seq";
create sequence "service_denials_id_seq";
create sequence "service_packages_id_seq";
create sequence "tasks_id_seq";
create sequence "tasks_results_id_seq";
create sequence "users_id_seq";
create sequence "user_ext_sources_id_seq";
create sequence "vos_id_seq";
create sequence "cabinet_publications_id_seq";
create sequence "cabinet_pub_sys_id_seq";
create sequence "cabinet_authorships_id_seq";
create sequence "cabinet_thanks_id_seq";
create sequence "cabinet_categories_id_seq";
create sequence "roles_id_seq";
create sequence "application_form_id_seq";
create sequence "application_form_items_id_seq";
create sequence "application_id_seq";
create sequence "application_data_id_seq";
create sequence "application_mails_id_seq";
create sequence "pn_object_id_seq";
create sequence "pn_pool_message_id_seq";
create sequence "pn_receiver_id_seq";
create sequence "pn_regex_id_seq";
create sequence "pn_template_id_seq";
create sequence "pn_audit_message_id_seq";
create sequence "pn_template_regex_seq";
create sequence "pn_template_message_id_seq";
create sequence "pn_regex_object_seq";
create sequence "action_types_seq";
create sequence "res_tags_seq";
create sequence "mailchange_id_seq";
create sequence "pwdreset_id_seq";
create sequence "security_teams_id_seq";
create sequence "resources_bans_id_seq";
create sequence "facilities_bans_id_seq";
create sequence "vos_bans_id_seq";

create unique index idx_grp_nam_vo_parentg_u on groups (name,vo_id,coalesce(parent_group_id,'0'));
create index idx_namespace on attr_names(namespace);
create index idx_authz_user_role_id on authz (user_id,role_id);
create index idx_authz_authz_group_role_id on authz (authorized_group_id,role_id);
create index idx_fk_cabthank_pub on cabinet_thanks(publicationid);
create index idx_fk_usrex_usr on user_ext_sources(user_id);
create index idx_fk_usrex_usersrc on user_ext_sources(ext_sources_id);
create index idx_fk_mem_user on members(user_id);
create index idx_fk_mem_vo on members(vo_id);
create index idx_fk_host_fac on hosts(facility_id);
create index idx_fk_dest_srv on facility_service_destinations(service_id);
create index idx_fk_dest_fac on facility_service_destinations(facility_id);
create index idx_fk_dest_destc on facility_service_destinations(destination_id);
create index idx_fk_vousrsrc_usrsrc on vo_ext_sources(ext_sources_id);
create index idx_fk_vousrsrc_vos on vo_ext_sources(vo_id);
create index idx_fk_groupsrc_src on group_ext_sources(ext_source_id);
create index idx_fk_groupsrc_group on group_ext_sources(group_id);
create index idx_fk_usrcatt_usrc on ext_sources_attributes(ext_sources_id);
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
create index idx_fk_task_srv on tasks(service_id);
create index idx_fk_task_fac on tasks(facility_id);
create index idx_fk_taskres_task on tasks_results(task_id);
create index idx_fk_taskres_dest on tasks_results(destination_id);
create index idx_fk_srvden_srv on service_denials(service_id);
create index idx_fk_srvden_fac on service_denials(facility_id);
create index idx_fk_srvden_dest on service_denials(destination_id);
create unique index idx_srvden_u ON service_denials(COALESCE(service_id, '0'), COALESCE(facility_id, '0'), COALESCE(destination_id, '0'));
create index idx_fk_srvreqattr_srv on service_required_attrs(service_id);
create index idx_fk_srvreqattr_attr on service_required_attrs(attr_id);
create index idx_fk_resrcsrv_srv on resource_services(service_id);
create index idx_fk_resrcsrv_rsrc on resource_services(resource_id);
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
create unique index idx_authz_u ON authz (COALESCE(user_id, '0'), COALESCE(authorized_group_id, '0'), role_id, COALESCE(group_id, '0'), COALESCE(vo_id, '0'), COALESCE(facility_id, '0'), COALESCE(member_id, '0'), COALESCE(resource_id, '0'), COALESCE(service_id, '0'), COALESCE(security_team_id, '0'), COALESCE(sponsored_user_id, '0'));
create index idx_fk_authz_role on authz(role_id);
create index idx_fk_authz_user on authz(user_id);
create index idx_fk_authz_authz_group on authz(authorized_group_id);
create index idx_fk_authz_vo on authz(vo_id);
create index idx_fk_authz_fac on authz(facility_id);
create index idx_fk_authz_mem on authz(member_id);
create index idx_fk_authz_group on authz(group_id);
create index idx_fk_authz_service on authz(service_id);
create index idx_fk_authz_res on authz(resource_id);
create index idx_fk_authz_sec_team on authz(security_team_id);
create index idx_fk_authz_sponsu on authz(sponsored_user_id);
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
create index idx_fk_specifu_u_ui on specific_user_users(user_id);
create index idx_fk_specifu_u_sui on specific_user_users(specific_user_id);
create index idx_fk_grp_grp_rgid on groups_groups(result_gid);
create index idx_fk_grp_grp_ogid on groups_groups(operand_gid);
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
create index idx_fk_res_ban_member on resources_bans (member_id);
create index idx_fk_res_ban_res on resources_bans (resource_id);
create index idx_fk_res_ban_member_res on resources_bans (member_id, resource_id);
create index idx_fk_fac_ban_user on facilities_bans (user_id);
create index idx_fk_fac_ban_fac on facilities_bans (facility_id);
create index idx_fk_fac_ban_user_fac on facilities_bans (user_id, facility_id);
create index idx_fk_vos_ban_member on vos_bans (member_id);
create index idx_fk_vos_ban_vos on vos_bans (vo_id);
create index idx_fk_ues_attr_values_ues on user_ext_source_attr_values (user_ext_source_id);
create index idx_fk_ues_attr_values_attr on user_ext_source_attr_values (attr_id);
create index idx_fk_memspons_mem ON members_sponsored(sponsored_id);
create index idx_fk_memspons_usr ON members_sponsored(sponsor_id);
CREATE INDEX fauv_idx ON facility_attr_u_values (facility_id, attr_id);
CREATE INDEX gauv_idx ON group_attr_u_values (group_id, attr_id);
CREATE INDEX grauv_idx ON group_resource_attr_u_values (group_id, resource_id, attr_id);
CREATE INDEX hauv_idx ON host_attr_u_values (host_id, attr_id);
CREATE INDEX mauv_idx ON member_attr_u_values (member_id, attr_id);
CREATE INDEX mgauv_idx ON member_group_attr_u_values (member_id, group_id, attr_id);
CREATE INDEX mrauv_idx ON member_resource_attr_u_values (member_id, resource_id, attr_id);
CREATE INDEX rauv_idx ON resource_attr_u_values (resource_id, attr_id);
CREATE INDEX uauv_idx ON user_attr_u_values (user_id, attr_id);
CREATE INDEX uesauv_idx ON user_ext_source_attr_u_values (user_ext_source_id, attr_id);
CREATE INDEX ufauv_idx ON user_facility_attr_u_values (user_id, facility_id, attr_id) ;
CREATE INDEX vauv_idx ON vo_attr_u_values (vo_id, attr_id) ;

grant all on users to perun;
grant all on vos to perun;
grant all on ext_sources to perun;
grant all on user_ext_sources to perun;
grant all on members to perun;
grant all on owners to perun;
grant all on hosts to perun;
grant all on services to perun;
grant all on destinations to perun;
grant all on facility_service_destinations to perun;
grant all on vo_ext_sources to perun;
grant all on group_ext_sources to perun;
grant all on ext_sources_attributes to perun;
grant all on attr_names to perun;
grant all on facilities to perun;
grant all on resources to perun;
grant all on resource_attr_values to perun;
grant all on resource_attr_u_values to perun;
grant all on facility_contacts to perun;
grant all on user_attr_values to perun;
grant all on user_attr_u_values to perun;
grant all on facility_owners to perun;
grant all on facility_attr_values to perun;
grant all on facility_attr_u_values to perun;
grant all on vo_attr_values to perun;
grant all on vo_attr_u_values to perun;
grant all on service_packages to perun;
grant all on service_service_packages to perun;
grant all on groups to perun;
grant all on member_resource_attr_values to perun;
grant all on member_resource_attr_u_values to perun;
grant all on member_group_attr_values to perun;
grant all on member_group_attr_u_values to perun;
grant all on user_facility_attr_values to perun;
grant all on user_facility_attr_u_values to perun;
grant all on tasks to perun;
grant all on tasks_results to perun;
grant all on service_denials to perun;
grant all on service_required_attrs to perun;
grant all on resource_services to perun;
grant all on member_attr_values to perun;
grant all on member_attr_u_values to perun;
grant all on group_attr_values to perun;
grant all on group_attr_u_values to perun;
grant all on group_resource_attr_values to perun;
grant all on group_resource_attr_u_values to perun;
grant all on host_attr_values to perun;
grant all on host_attr_u_values to perun;
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
grant all on specific_user_users to perun;
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
grant all on resources_bans to perun;
grant all on facilities_bans to perun;
grant all on vos_bans to perun;
grant all on membership_types to perun;
grant all on user_ext_source_attr_values to perun;
grant all on user_ext_source_attr_u_values to perun;
grant all on members_sponsored to perun;

-- set initial Perun DB version
insert into configurations values ('DATABASE VERSION','3.1.73');

-- insert membership types
insert into membership_types (id, membership_type, description) values (1, 'DIRECT', 'Member is directly added into group');
insert into membership_types (id, membership_type, description) values (2, 'INDIRECT', 'Member is added indirectly through UNION relation');

-- insert action types
insert into action_types (id, action_type, description) values (nextval('action_types_seq'), 'read', 'Can read value.');
insert into action_types (id, action_type, description) values (nextval('action_types_seq'), 'read_vo', 'Vo related can read value.');
insert into action_types (id, action_type, description) values (nextval('action_types_seq'), 'read_public', 'Anyone can read value.');
insert into action_types (id, action_type, description) values (nextval('action_types_seq'), 'write', 'Can write, rewrite and remove value.');
insert into action_types (id, action_type, description) values (nextval('action_types_seq'), 'write_vo', 'Vo related can write, rewrite and remove value.');
insert into action_types (id, action_type, description) values (nextval('action_types_seq'), 'write_public', 'Anyone can write, rewrite and remove value.');

-- init default auditer consumers
insert into auditer_consumers (id, name, last_processed_id) values (nextval('auditer_consumers_id_seq'), 'dispatcher', 0);
insert into auditer_consumers (id, name, last_processed_id) values (nextval('auditer_consumers_id_seq'), 'notifications', 0);

-- initial user, user_ext_source and internal ext_source
insert into ext_sources (id,name,type) values (nextval('ext_sources_id_seq'),'INTERNAL','cz.metacentrum.perun.core.impl.ExtSourceInternal');
insert into users (id, first_name, last_name) values (nextval('users_id_seq'),'Master','Perun');
insert into user_ext_sources (id, user_id, login_ext, ext_sources_id, loa) values (nextval('user_ext_sources_id_seq'), currval('users_id_seq'), 'perun', currval('ext_sources_id_seq'), 0);

