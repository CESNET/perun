set database sql syntax PGS true;

create table vos (
	id integer not null,
	name varchar(128) not null,
	short_name varchar(32) not null,
	created_at timestamp default now not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table users (
	id integer not null,
	first_name varchar(64),
	last_name varchar(64),
	middle_name varchar(64),
	title_before varchar(20),
	title_after varchar(20),
	created_at timestamp  default now not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	service_acc char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table owners (
	id integer not null,
	name varchar(128) not null,
	contact varchar(100),
	created_at timestamp default now not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	type varchar(128) not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table cabinet_categories (
	id integer not null,
	name varchar(128) not null,
	rank numeric(38,1) not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table cabinet_publication_systems (
	id integer not null,
	friendlyName varchar(128) not null,
	url varchar(128) not null,
	username varchar(64),
	password varchar(64),
	loginNamespace varchar(128) not null,
	type varchar(128) not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table cabinet_publications (
	id integer not null,
	externalId integer not null,
	publicationSystemId integer not null,
	title varchar(1024) not null,
	year integer not null,
	main varchar(4000),
	isbn varchar(32),
	categoryId integer not null,
	createdBy varchar(1024) default user not null,
	createdDate timestamp not null,
	rank numeric (38,1) default 0 not null,
	doi varchar(256),
	locked varchar(1) default 0 not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table cabinet_authorships (
	id integer not null,
	publicationId integer not null,
	userId integer not null,
	createdBy varchar(1024) default user not null,
	createdDate timestamp not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table cabinet_thanks (
	id integer not null,
	publicationid integer not null,
	ownerId integer not null,
	createdBy varchar(1024) default user not null,
	createdDate timestamp not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table facilities (
	id integer not null,
	name varchar(128) not null,
	dsc varchar(1024),
	created_at timestamp default now not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table resources (
	id integer not null,
	facility_id integer not null,
	name varchar(128) not null,
	dsc varchar(1024),
	created_at timestamp default now not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	vo_id integer not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table destinations (
	id integer not null,
	destination varchar(1024) not null,
	type varchar(20) not null,
	created_at timestamp default now not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table facility_owners (
	facility_id integer not null,
	owner_id integer not null,
	created_at timestamp default now not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table facility_contacts (
	contact_group_name varchar(128) not null,
	facility_id integer not null,
	owner_id integer,
	user_id integer,
	group_id integer
);

create table groups (
	id integer not null,
	name longvarchar not null,
	dsc varchar(1024),
	vo_id integer not null,
	created_at timestamp default now not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	parent_group_id integer,
	created_by_uid integer,
	modified_by_uid integer
);

create table members (
	id integer not null,
	user_id integer not null,
	vo_id integer not null,
	created_at timestamp default now not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table routing_rules (
	id integer not null,
	routing_rule varchar(512) not null,
	created_at timestamp default now not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table dispatcher_settings (
	ip_address varchar(40) not null,
	port integer not null,
	last_check_in timestamp default now,
	created_at timestamp default now not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table engines (
	id integer not null,
	ip_address varchar(40) not null,
	port integer not null,
	last_check_in timestamp default now,
	created_at timestamp default now not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table engine_routing_rule (
	engine_id integer not null,
	routing_rule_id integer not null,
	created_at timestamp default now not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table processing_rules (
	id integer not null,
	processing_rule varchar(1024) not null,
	created_at timestamp default now not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table roles (
	id integer not null,
	name varchar(32) not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table action_types (
	id integer not null,
	action_type varchar(20) not null,
	description varchar(1024)
);

create table membership_types (
	id integer not null,
	membership_type varchar(10) not null,
	description varchar(1024)
);

create table attr_names (
	id integer not null,
	default_attr_id integer,
	attr_name varchar(384) not null,
	friendly_name varchar(128) not null,
	namespace varchar(256) not null,
	type varchar(256) not null,
	dsc varchar(1024),
	created_at timestamp default now not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer,
	display_name varchar(256)
);

create table attributes_authz (
	attr_id integer not null,
	role_id integer not null,
	action_type_id integer not null
);

create table authz (
	user_id integer,
	role_id integer not null,
	vo_id integer,
	facility_id integer,
	member_id integer,
	group_id integer,
	service_id integer,
	resource_id integer,
	service_principal_id integer,
	created_by_uid integer,
	modified_by_uid integer,
	authorized_group_id integer
);

create table hosts (
	id integer not null,
	hostname varchar(128) not null,
	facility_id integer not null,
	dsc varchar(1024),
	created_at timestamp default now not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table host_attr_values (
	host_id integer not null,
	attr_id integer not null,
	attr_value varchar(4000),
	created_at timestamp default now not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	attr_value_text longvarchar,
	created_by_uid integer,
	modified_by_uid integer
);

create table auditer_consumers (
	id integer not null,
	name varchar(256) not null,
	last_processed_id integer,
	created_at timestamp default now not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now not null,
	modified_by varchar(1024) default user not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table services (
	id integer not null,
	name varchar(128) not null,
	owner_id integer,
	created_at timestamp default now not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table service_processing_rule (
	service_id integer not null,
	processing_rule_id integer not null,
	created_at timestamp default now not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table service_required_attrs (
	service_id integer not null,
	attr_id integer not null,
	created_at timestamp default now not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table service_user_users (
	user_id integer not null,
	service_user_id integer not null,
	created_by_uid integer,
	modified_by_uid integer,
	modified_at timestamp default now not null,
	status char(1) default '0' not null
);

create table exec_services (
	id integer not null,
	service_id integer not null,
	default_delay integer not null,
	enabled char(1) not null,
	default_recurrence integer not null,
	script varchar(256) not null,
	type varchar(10) not null,
	created_at timestamp default now not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table service_denials (
	id integer not null,
	exec_service_id integer not null,
	facility_id integer,
	destination_id integer,
	created_at timestamp default now not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table service_dependencies (
	exec_service_id integer not null,
	dependency_id integer not null,
	created_at timestamp default now not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer,
	type varchar(16) default 'SERVICE' not null
);

create table resource_services (
	service_id integer not null,
	resource_id integer not null,
	created_at timestamp default now not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table application (
	id integer not null,
	vo_id integer not null,
	user_id integer,
	apptype varchar(128) not null,
	extSourceName varchar(4000),
	extSourceType varchar(4000),
	fed_info varchar(4000),
	state varchar(128),
	extSourceLoa integer,
	group_id integer,
	created_at timestamp default now not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now not null,
	modified_by varchar(1024) default user not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table application_form (
	id integer not null,
	vo_id integer not null,
	automatic_approval char(1),
	automatic_approval_extension char(1),
	module_name varchar(128),
	group_id integer,
	created_by_uid integer,
	modified_by_uid integer
);

create table application_form_items (
	id integer not null,
	form_id integer not null,
	ordnum integer not null,
	shortname varchar(128) not null,
	required char(1),
	type varchar(128),
	fed_attr varchar(128),
	dst_attr varchar(384),
	regex varchar(4000),
	created_by_uid integer,
	modified_by_uid integer
);

create table application_form_item_apptypes (
	item_id integer not null,
	apptype varchar(128) not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table application_form_item_texts (
	item_id integer not null,
	locale varchar(128) not null,
	label varchar(4000),
	options varchar(4000),
	help varchar(4000),
	error_message varchar(4000),
	created_by_uid integer,
	modified_by_uid integer
);

create table application_data (
	id integer not null,
	app_id integer not null,
	item_id integer,
	shortname varchar(128),
	value varchar(4000),
	assurance_level varchar(128),
	created_by_uid integer,
	modified_by_uid integer
);

create table application_mails (
	id integer not null,
	form_id integer not null,
	app_type varchar(30) not null,
	mail_type varchar(30) not null,
	send varchar(1) not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table application_mail_texts (
	mail_id integer not null,
	locale varchar(10) not null,
	subject varchar(1024),
	text varchar(4000),
	created_by_uid integer,
	modified_by_uid integer
);

create table application_reserved_logins (
	login varchar(256) not null,
	namespace varchar(30) not null,
	app_id integer not null,
	created_by varchar(1024) default user not null,
	created_at timestamp default now not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table facility_service_destinations (
	service_id integer not null,
	facility_id integer not null,
	destination_id integer not null,
	created_at timestamp default now not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer,
	propagation_type varchar(10) default 'PARALLEL'
);

create table entityless_attr_values (
	subject varchar(256) not null,
	attr_id integer not null,
	attr_value varchar(4000),
	created_at timestamp default now not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	attr_value_text longvarchar,
	created_by_uid integer,
	modified_by_uid integer
);

create table facility_attr_values (
	facility_id integer not null,
	attr_id integer not null,
	attr_value varchar(4000),
	created_at timestamp default now not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	attr_value_text longvarchar,
	created_by_uid integer,
	modified_by_uid integer
);

create table group_attr_values (
	group_id integer not null,
	attr_id integer not null,
	attr_value varchar(4000),
	created_at timestamp default now not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	attr_value_text longvarchar,
	created_by_uid integer,
	modified_by_uid integer
);

create table resource_attr_values (
	resource_id integer not null,
	attr_id integer not null,
	attr_value varchar(4000),
	created_at timestamp default now not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	attr_value_text longvarchar,
	created_by_uid integer,
	modified_by_uid integer
);

create table group_resource_attr_values (
	group_id integer not null,
	resource_id integer not null,
	attr_id integer not null,
	attr_value varchar(4000),
	created_at timestamp default now not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	attr_value_text longvarchar,
	created_by_uid integer,
	modified_by_uid integer
);

create table groups_members (
	group_id integer not null,
	member_id integer not null,
	created_at timestamp default now not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer,
	membership_type integer not null,
	source_group_id integer not null
);

create table groups_resources (
	group_id integer not null,
	resource_id integer not null,
	created_at timestamp default now not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table member_attr_values (
	member_id integer not null,
	attr_id integer not null,
	attr_value varchar(4000),
	created_at timestamp default now not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	attr_value_text longvarchar,
	created_by_uid integer,
	modified_by_uid integer
);

create table member_resource_attr_values (
	member_id integer not null,
	resource_id integer not null,
	attr_id integer not null,
	attr_value varchar(4000),
	created_at timestamp default now not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	attr_value_text longvarchar,
	created_by_uid integer,
	modified_by_uid integer
);

create table member_group_attr_values (
	member_id integer not null,
	group_id integer not null,
	attr_id integer not null,
	attr_value varchar(4000),
	created_at timestamp default now() not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now() not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	attr_value_text longvarchar,
	created_by_uid integer,
	modified_by_uid integer
);

create table user_attr_values (
	user_id integer not null,
	attr_id integer not null,
	attr_value varchar(4000),
	created_at timestamp default now not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	attr_value_text longvarchar,
	created_by_uid integer,
	modified_by_uid integer
);

create table user_facility_attr_values (
	user_id integer not null,
	facility_id integer not null,
	attr_id integer not null,
	attr_value varchar(4000),
	created_at timestamp default now not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	attr_value_text longvarchar,
	created_by_uid integer,
	modified_by_uid integer
);

create table vo_attr_values (
	vo_id integer not null,
	attr_id integer not null,
	attr_value varchar(4000),
	created_at timestamp default now not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	attr_value_text longvarchar,
	created_by_uid integer,
	modified_by_uid integer
);

create table ext_sources (
	id integer not null,
	name varchar(256) not null,
	type varchar(64),
	created_at timestamp default now not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table ext_sources_attributes (
	ext_sources_id integer not null,
	attr_name varchar(128) not null,
	attr_value varchar(4000),
	created_at timestamp default now not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table vo_ext_sources (
	vo_id integer not null,
	ext_sources_id integer not null,
	created_at timestamp default now not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table user_ext_sources (
	id integer not null,
	user_id integer not null,
	login_ext varchar(256) not null,
	ext_sources_id integer not null,
	created_at timestamp default now not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	loa integer,
	last_access timestamp default now not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table service_packages (
	id integer not null,
	name varchar(128) not null,
	description varchar(512),
	created_at timestamp default now not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table service_service_packages (
	service_id integer not null,
	package_id integer not null,
	created_at timestamp default now not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table tasks (
	id integer not null,
	exec_service_id integer not null,
	facility_id integer not null,
	schedule timestamp not null,
	recurrence integer not null,
	delay integer not null,
	status varchar(16) not null,
	start_time timestamp,
	end_time timestamp,
	engine_id integer not null,
	created_at timestamp default now not null,
	err_message varchar(4000),
	created_by_uid integer,
	modified_by_uid integer
);

create table tasks_results (
	id integer not null,
	task_id integer not null,
	destination_id integer not null,
	status varchar(16) not null,
	err_message varchar(4000),
	std_message varchar(4000),
	return_code integer,
	timestamp timestamp,
	engine_id integer not null,
	created_at timestamp default now not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now not null,
	modified_by varchar(1024) default user not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table auditer_log (
	id integer not null,
	msg longvarchar not null,
	actor varchar(256) not null,
	created_at timestamp default now not null ,
	created_by_uid integer,
	modified_by_uid integer
);

create table service_principals (
	id integer not null,
	description varchar(1024),
	name varchar(128) not null,
	created_at timestamp default now not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now not null,
	modified_by varchar(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table reserved_logins (
	login varchar(256),
	namespace varchar(128),
	application varchar(256),
	id varchar(1024),
	created_by_uid integer,
	modified_by_uid integer
);

create table pn_audit_message (
	message longvarchar,
	id integer NOT NULL,
	created_by_uid integer,
	modified_by_uid integer
);

create table pn_object (
	id integer NOT NULL,
	name varchar(256),
	properties varchar(4000),
	class_name varchar(512),
	created_by_uid integer,
	modified_by_uid integer
);

create table pn_pool_message (
	id integer NOT NULL,
	regex_id integer NOT NULL,
	template_id integer NOT NULL,
	key_attributes varchar(4000) NOT NULL,

	created timestamp default now NOT NULL,
	notif_message longvarchar NOT NULL,
	created_by_uid integer,
	modified_by_uid integer
);

create table pn_receiver (
	id integer NOT NULL,
	target varchar(256) NOT NULL,
	type_of_receiver varchar(256) NOT NULL,
	template_id integer NOT NULL,
	created_by_uid integer,
	modified_by_uid integer,
	locale varchar(512)
);

create table pn_regex (
	id integer NOT NULL,
	note varchar(256),
	regex varchar(4000) NOT NULL,
	created_by_uid integer,
	modified_by_uid integer
);

create table pn_template (
	id integer NOT NULL,
	primary_properties varchar(4000) NOT NULL,
	notify_trigger varchar(100),


	youngest_message_time integer,
	oldest_message_time integer,
	name varchar(512),
	sender varchar(4000),
	created_by_uid integer,
	modified_by_uid integer
);

create table pn_template_message (
	id integer NOT NULL,
	template_id integer NOT NULL,
	locale varchar(5) NOT NULL,
	message varchar(4000),
	created_by_uid integer,
	modified_by_uid integer,
	subject varchar(512) not null
);

create table pn_template_regex (
	regex_id integer NOT NULL,
	template_id integer NOT NULL,
	id integer NOT NULL,
	created_by_uid integer,
	modified_by_uid integer
);

create table pn_regex_object (
	id integer NOT NULL,
	regex_id integer NOT NULL,
	object_id integer NOT NULL,
	created_by_uid integer,
	modified_by_uid integer
);

create table groups_groups (
	group_id integer not null,
	parent_group_id integer not null,
	group_mode integer not null,
	created_at timestamp default now not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now not null,
	modified_by varchar(1024) default user not null
);

create table res_tags (
	id integer not null,
	vo_id integer not null,
	tag_name varchar (1024) not null,
	created_at timestamp default now not null,
	created_by varchar(1024) default user not null,
	modified_at timestamp default now not null,
	modified_by varchar(1024) default user not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table tags_resources (
	tag_id integer not null,
	resource_id integer not null
);

create table configurations (
	property varchar(32) not null,
	value varchar(128) not null
);

create table mailchange (
	id integer not null,
	value longvarchar not null,
	user_id integer not null,
	created_at timestamp default now not null,
	created_by varchar(1024) default user not null,
	created_by_uid integer
);

create table pwdreset (
	id integer not null,
	namespace longvarchar not null,
	user_id integer not null,
	created_at timestamp default now not null,
	created_by varchar(1024) default user not null,
	created_by_uid integer
);

create sequence attr_names_id_seq;
create sequence auditer_consumers_id_seq;
create sequence auditer_log_id_seq;
create sequence destinations_id_seq;
create sequence exec_services_id_seq start with 20 increment by 1;
create sequence ext_sources_id_seq;
create sequence facilities_id_seq;
create sequence groups_id_seq;
create sequence hosts_id_seq;
create sequence members_id_seq;
create sequence owners_id_seq;
create sequence processing_rules_id_seq start with 10 increment by 1;
create sequence resources_id_seq;
create sequence routing_rules_id_seq start with 10 increment by 1;
create sequence services_id_seq;
create sequence service_denials_id_seq start with 10 increment by 1;
create sequence service_packages_id_seq;
create sequence tasks_id_seq start with 10 increment by 1;
create sequence tasks_results_id_seq start with 10 increment by 1;
create sequence users_id_seq;
create sequence user_ext_sources_id_seq;
create sequence vos_id_seq;
create sequence cabinet_publications_id_seq;
create sequence cabinet_pub_sys_id_seq;
create sequence cabinet_authorships_id_seq;
create sequence cabinet_thanks_id_seq;
create sequence cabinet_categories_id_seq;
create sequence roles_id_seq;
create sequence service_principals_id_seq;
create sequence application_form_id_seq;
create sequence application_form_items_id_seq;
create sequence application_id_seq;
create sequence application_data_id_seq;
create sequence application_mails_id_seq;
create sequence pn_object_id_seq;
create sequence pn_pool_message_id_seq;
create sequence pn_receiver_id_seq;
create sequence pn_regex_id_seq;
create sequence pn_template_id_seq;
create sequence pn_audit_message_id_seq;
create sequence pn_template_regex_seq;
create sequence pn_template_message_id_seq;
create sequence pn_regex_object_seq;
create sequence action_types_seq;
create sequence res_tags_seq;
create sequence mailchange_id_seq;
create sequence pwdreset_id_seq;

create index idx_namespace on attr_names(namespace);
create index idx_authz_user_role_id on authz(user_id,role_id);
create index idx_authz_authz_group_role_id on authz(authorized_group_id,role_id);
create index idx_fk_cabthank_pub on cabinet_thanks(publicationid);
create index idx_fk_usrex_usr on user_ext_sources(user_id);
create index idx_fk_usrex_usersrc on user_ext_sources(ext_sources_id);
create index idx_fk_mem_user on members(user_id);
create index idx_fk_mem_vo on members(vo_id);
create index idx_fk_host_fac on hosts(facility_id);
create index idx_fk_serv_ow on services(owner_id);
create index idx_fk_exsrv_srv on exec_services(service_id);
create index idx_fk_dest_srv on facility_service_destinations(service_id);
create index idx_fk_dest_fac on facility_service_destinations(facility_id);
create index idx_fk_dest_destc on facility_service_destinations(destination_id);
create index idx_fk_vousrsrc_usrsrc on vo_ext_sources(ext_sources_id);
create index idx_fk_vousrsrc_vos on vo_ext_sources(vo_id);
create index idx_fk_usrcatt_usrc on ext_sources_attributes(ext_sources_id);
create index idx_fk_attnam_attnam on attr_names(default_attr_id);
create index idx_fk_rsrc_fac on resources(facility_id);
create index idx_fk_rsrc_vo on resources(vo_id);
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
create unique index idx_authz_u2 on authz (user_id, authorized_group_id, service_principal_id, role_id, group_id, vo_id, facility_id, member_id, resource_id, service_id);
create index idx_fk_faccont_fac on facility_contacts(facility_id);
create index idx_fk_faccont_usr on facility_contacts(user_id);
create index idx_fk_faccont_own on facility_contacts(owner_id);
create index idx_fk_faccont_grp on facility_contacts(group_id);
create unique index idx_faccont_u2 ON facility_contacts (user_id, owner_id, group_id, facility_id, contact_group_name);
create index idx_fk_grres_gr on groups_resources(group_id);
create index idx_fk_grres_res on groups_resources(resource_id);
create index idx_fk_grpmem_gr on groups_members(group_id);
create index idx_fk_grpmem_mem on groups_members(member_id);
create index idx_fk_grpmem_memtype on groups_members(membership_type);
create unique index applform_u1 on application_form (vo_id);
create unique index applform_u2 on application_form (vo_id, group_id);
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
create index idx_fk_servu_u_ui on service_user_users(user_id);
create index idx_fk_servu_u_sui on service_user_users(service_user_id);
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
alter table services add constraint serv_ow_fk foreign key (owner_id) references owners(id);

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
alter table groups add constraint grn_nam_vo_parentg_u unique (name,vo_id,parent_group_id);
alter table groups add constraint grp_vos_fk foreign key (vo_id) references vos(id);
alter table groups add constraint grp_grp_fk foreign key (parent_group_id) references groups(id);

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
alter table service_denials add constraint srvden_u unique(exec_service_id,facility_id,destination_id);

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

alter table groups_members add constraint GRPMEM_PK primary key (member_id,group_id, source_group_id);
alter table groups_members add constraint GRPMEM_GR_FK foreign key (group_id) references groups(id);
alter table groups_members add constraint GRPMEM_MEM_FK foreign key (member_id) references members(id);
alter table groups_members add constraint GRPMEM_MEMTYPE_FK foreign key (membership_type) references membership_types(id);

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
alter table pn_template_message add constraint pn_tmplmsg_tmpl_fk foreign key (template_id) references pn_template(id);

alter table pn_template_regex add constraint pn_tmplrgx_pk primary key (id);
alter table pn_template_regex add constraint pn_tmplrgx_rgx_fk foreign key (regex_id) references pn_regex(id);
alter table pn_template_regex add constraint pn_tmplrgx_tmpl_fk foreign key (template_id) references pn_template(id);

alter table pn_regex_object add constraint pn_rgxobj_pk primary key (id);
alter table pn_regex_object add constraint pn_rgxobj_rgx_fk foreign key (regex_id) references pn_regex(id);
alter table pn_regex_object add constraint pn_rgxobj_obj_fk foreign key (object_id) references pn_object(id);

alter table service_user_users add constraint acc_servu_u_pk primary key (user_id,service_user_id);
alter table service_user_users add constraint acc_servu_u_uid_fk foreign key (user_id) references users(id);
alter table service_user_users add constraint acc_servu_u_suid_fk foreign key (service_user_id) references users(id);
alter table service_user_users add constraint servu_u_status_chk check (status in ('0','1'));

alter table groups_groups add constraint grp_grp_pk primary key (group_id,parent_group_id);
alter table groups_groups add constraint grp_grp_gid_fk foreign key (group_id) references groups(id);
alter table groups_groups add constraint grp_grp_pgid_fk foreign key (parent_group_id) references groups(id);

alter table action_types add constraint actiontyp_pk primary key (id);
alter table action_types add constraint actiontyp_u unique (action_type);
alter table action_types add constraint actiontyp_at_chk check (action_type in ('read','write'));

alter table attributes_authz add constraint attrauthz_pk primary key (attr_id,role_id,action_type_id);
alter table attributes_authz add constraint attrauthz_attr_fk foreign key (attr_id) references attr_names(id);
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
alter table tasks add constraint task_eng_fk foreign key (engine_id) references engines(id);
alter table tasks add constraint task_stat_chk check (status in ('NONE','OPEN','PLANNED','PROCESSING','DONE','ERROR'));

alter table tasks_results add constraint taskres_task_fk foreign key (task_id) references tasks(id);
alter table tasks_results add constraint taskres_dest_fk foreign key (destination_id) references destinations(id);
alter table tasks_results add constraint taskres_eng_fk foreign key (engine_id) references engines(id);
alter table tasks_results add constraint taskres_stat_chk check (status in ('DONE','ERROR','FATAL_ERROR','DENIED'));

alter table facility_contacts add constraint faccont_fac_fk foreign key (facility_id) references facilities(id);
alter table facility_contacts add constraint faccont_usr_fk foreign key (user_id) references users(id);
alter table facility_contacts add constraint faccont_own_fk foreign key (owner_id) references owners(id);
alter table facility_contacts add constraint faccont_grp_fk foreign key (group_id) references groups(id);
alter table facility_contacts add constraint faccont_usr_own_grp_chk check ((user_id is not null and owner_id is null and group_id is null) or (user_id is null and owner_id is not null and group_id is null) or (user_id is null and owner_id is null and group_id is not null));

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
alter table authz add constraint authz_user_serprinc_autgrp_chk check ((user_id is not null and service_principal_id is null and authorized_group_id is null) or (user_id is null and service_principal_id is not null and authorized_group_id is null) or (user_id is null and service_principal_id is null and authorized_group_id is not null));
alter table configurations add constraint config_pk primary key (property);
alter table configurations add constraint config_prop_chk check (property in ('DATABASE VERSION'));
alter table mailchange add constraint mailchange_pk primary key (id);
alter table mailchange add constraint mailchange_u_fk foreign key (user_id) references users(id);
alter table pwdreset add constraint pwdreset_pk primary key (id);
alter table pwdreset add constraint pwdreset_u_fk foreign key (user_id) references users(id);

