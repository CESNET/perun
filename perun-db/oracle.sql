-- database version 3.1.14

create user perunv3 identified by password;
grant create session to perunv3;
grant create sequence to perunv3;
grant create table to perunv3;
grant create view to perunv3;
grant unlimited tablespace to perunv3;

connect perunv3

create table vos (
	id integer not null,
	name varchar2(128) not null,
	short_name varchar2(32) not null,
	created_at date  default sysdate not null,
	created_by varchar2(1024) default user not null,
	modified_at date default sysdate not null,
	modified_by varchar2(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table users (
	id integer not null,
	first_name varchar2(64),
	last_name varchar2(64),
	middle_name varchar2(64),
	title_before varchar2(20),
	title_after varchar2(20),
	created_at date  default sysdate not null,
	created_by varchar2(1024) default user not null,
	modified_at date default sysdate not null,
	modified_by varchar2(1024) default user not null,
	status char(1) default '0' not null,
	service_acc char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table owners (
	id integer not null,
	name varchar2(128) not null,
	contact varchar2(100),
	created_at date  default sysdate not null,
	created_by varchar2(1024) default user not null,
	modified_at date default sysdate not null,
	modified_by varchar2(1024) default user not null,
	status char(1) default '0' not null,
	type varchar2(128) not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table cabinet_categories (
	id integer not null,
	name varchar2(128) not null,
	rank number(38,1) not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table cabinet_publication_systems (
	id integer not null,
	friendlyName varchar2(128) not null,
	url varchar2(128) not null,
	username varchar2(64),
	password varchar2(64),
	loginNamespace varchar2(128) not null,
	type varchar2(128) not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table cabinet_publications (
	id integer not null,
	externalId integer not null,
	publicationSystemId integer not null,
	title varchar2(1024) not null,
	year integer not null,
	main varchar2(4000),
	isbn varchar2(32),
	categoryId integer not null,
	createdBy varchar2(1024) default user not null,
	createdDate date not null,
	rank  number (38,1) default 0 not null,
	doi varchar2(256),
	locked varchar2(1) default 0 not null  ,
	created_by_uid integer,
	modified_by_uid integer
);

create table cabinet_authorships (
	id integer not null,
	publicationId integer not null,
	userId integer not null,
	createdBy varchar2(1024) default user not null,
	createdDate date not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table cabinet_thanks (
	id integer not null,
	publicationid integer not null,
	ownerId integer not null,
	createdBy varchar2(1024) default user not null,
	createdDate date not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table facilities (
	id integer not null,
	name varchar2(128) not null,
	created_at date  default sysdate not null,
	created_by varchar2(1024) default user not null,
	modified_at date default sysdate not null,
	modified_by varchar2(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table resources (
	id integer not null,
	facility_id integer not null,
	name varchar2(128) not null,
	dsc varchar2(1024),
	created_at date  default sysdate not null,
	created_by varchar2(1024) default user not null,
	modified_at date default sysdate not null,
	modified_by varchar2(1024) default user not null,
	status char(1) default '0' not null,
	vo_id integer not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table destinations (
	id integer not null,
	destination varchar2(1024) not null,
	type varchar2(20) not null,
	created_at date  default sysdate not null,
	created_by varchar2(1024) default user not null,
	modified_at date default sysdate not null,
	modified_by varchar2(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table facility_owners (
	facility_id integer not null,
	owner_id  integer not null,
	created_at date  default sysdate not null,
	created_by varchar2(1024) default user not null,
	modified_at date default sysdate not null,
	modified_by varchar2(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table groups (
	id integer not null,
	name varchar2(128) not null,
	dsc varchar2(1024),
	vo_id integer not null,
	created_at date  default sysdate not null,
	created_by varchar2(1024) default user not null,
	modified_at date default sysdate not null,
	modified_by varchar2(1024) default user not null,
	status char(1) default '0' not null,
	parent_group_id integer,
	created_by_uid integer,
	modified_by_uid integer
);

create table members (
	id integer not null,
	user_id integer not null,
	vo_id integer not null,
	created_at date  default sysdate not null,
	created_by varchar2(1024) default user not null,
	modified_at date default sysdate not null,
	modified_by varchar2(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table routing_rules (
	id integer not null,
	routing_rule varchar2(512) not null,
	created_at date  default sysdate not null,
	created_by varchar2(1024) default user not null,
	modified_at date default sysdate not null,
	modified_by varchar2(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table dispatcher_settings (
	ip_address varchar2(40) not null,
	port integer not null,
	last_check_in date default (sysdate),
	created_at date  default sysdate not null,
	created_by varchar2(1024) default user not null,
	modified_at date default sysdate not null,
	modified_by varchar2(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table engines (
	id integer not null,
	ip_address varchar2(40) not null,
	port integer not null,
	last_check_in date default (sysdate),
	created_at date  default sysdate not null,
	created_by varchar2(1024) default user not null,
	modified_at date default sysdate not null,
	modified_by varchar2(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table engine_routing_rule (
	engine_id integer not null,
	routing_rule_id integer not null,
	created_at date  default sysdate not null,
	created_by varchar2(1024) default user not null,
	modified_at date default sysdate not null,
	modified_by varchar2(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table processing_rules (
	id integer not null,
	processing_rule varchar2(1024) not null,
	created_at date  default sysdate not null,
	created_by varchar2(1024) default user not null,
	modified_at date default sysdate not null,
	modified_by varchar2(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table roles (
	id integer not null,
	name varchar2 (32) not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table action_types (
	id integer not null,
	action_type varchar2(20) not null,
	description varchar2(1024)
);

create table membership_types (
	id integer not null,
	membership_type varchar2(10) not null,
	description varchar2(1024)
);

create table attr_names (
	id integer not null,
	default_attr_id integer,
	attr_name varchar2(384) not null,
	friendly_name varchar2(128) not null,
	namespace varchar2(256) not null,
	type varchar2(256) not null,
	dsc varchar2(1024),
	created_at date  default sysdate not null,
	created_by varchar2(1024) default user not null,
	modified_at date default sysdate not null,
	modified_by varchar2(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer,
	display_name varchar2(256)
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
	hostname varchar2(128) not null,
	facility_id integer not null,
	dsc varchar2(1024),
	created_at date  default sysdate not null,
	created_by varchar2(1024) default user not null,
	modified_at date default sysdate not null,
	modified_by varchar2(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table host_attr_values (
	host_id integer not null,
	attr_id integer not null,
	attr_value varchar2(4000 char),
	created_at date  default sysdate not null,
	created_by varchar2(1024) default user not null,
	modified_at date default sysdate not null,
	modified_by varchar2(1024) default user not null,
	status char(1) default '0' not null,
	attr_value_text clob,
	created_by_uid integer,
	modified_by_uid integer
);

create table auditer_consumers (
	id integer not null,
	name varchar2(256) not null,
	last_processed_id integer,
	created_at date default sysdate not null,
	created_by varchar2(1024) default user not null,
	modified_at  date default sysdate not null,
	modified_by varchar2(1024) default user not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table services (
	id integer not null,
	name varchar2(128) not null,
	owner_id integer,
	created_at date  default sysdate not null,
	created_by varchar2(1024) default user not null,
	modified_at date default sysdate not null,
	modified_by varchar2(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table service_processing_rule (
	service_id integer not null,
	processing_rule_id integer not null,
	created_at date  default sysdate not null,
	created_by varchar2(1024) default user not null,
	modified_at date default sysdate not null,
	modified_by varchar2(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table service_required_attrs (
	service_id integer not null,
	attr_id integer not null,
	created_at date  default sysdate not null,
	created_by varchar2(1024) default user not null,
	modified_at date default sysdate not null,
	modified_by varchar2(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table service_user_users (
	user_id integer not null,
	service_user_id integer not null,
	created_by_uid integer,
	modified_by_uid integer,
	modified_at date default sysdate not null,
	status char(1) default '0' not null
);

create table exec_services (
	id integer not null,
	service_id integer not null,
	default_delay integer not null,
	enabled char(1) not null,
	default_recurrence integer not null,
	script varchar2(256) not null,
	type varchar2(10) not null,
	created_at date  default sysdate not null,
	created_by varchar2(1024) default user not null,
	modified_at date default sysdate not null,
	modified_by varchar2(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table service_denials (
	id integer not null,
	exec_service_id integer not null,
	facility_id integer,
	destination_id integer,
	created_at date  default sysdate not null,
	created_by varchar2(1024) default user not null,
	modified_at date default sysdate not null,
	modified_by varchar2(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table service_dependencies (
	exec_service_id integer not null,
	dependency_id integer not null,
	created_at date  default sysdate not null,
	created_by varchar2(1024) default user not null,
	modified_at date default sysdate not null,
	modified_by varchar2(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer,
	type varchar2(16) default 'SERVICE' not null
);

create table resource_services (
	service_id integer not null,
	resource_id integer not null,
	created_at date  default sysdate not null,
	created_by varchar2(1024) default user not null,
	modified_at date default sysdate not null,
	modified_by varchar2(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table application (
	id integer not null,
	vo_id integer not null,
	user_id integer,
	apptype varchar2(128) not null,
	extSourceName varchar2(4000),
	extSourceType varchar2(4000),
	fed_info varchar2(4000),
	state varchar2(128),
	extSourceLoa integer,
	group_id integer,
	created_at date  default sysdate not null,
	created_by varchar2(1024) default user not null,
	modified_at date default sysdate not null,
	modified_by varchar2(1024) default user not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table application_form (
	id integer not null,
	vo_id integer not null,
	automatic_approval char(1),
	automatic_approval_extension char(1),
	module_name varchar2(128),
	group_id integer,
	created_by_uid integer,
	modified_by_uid integer
);

create table application_form_items (
	id integer not null,
	form_id integer not null,
	ordnum integer not null,
	shortname varchar2(128) not null,
	required char(1),
	type varchar2(128),
	fed_attr varchar2(128),
	dst_attr varchar2(384),
	regex varchar2(4000),
	created_by_uid integer,
	modified_by_uid integer
);

create table application_form_item_apptypes (
	item_id integer not null,
	apptype varchar2(128) not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table application_form_item_texts (
	item_id integer not null,
	locale varchar2(128) not null,
	label varchar2(4000),
	options varchar2(4000),
	help varchar2(4000),
	error_message varchar2(4000),
	created_by_uid integer,
	modified_by_uid integer
);

create table application_data (
	id integer not null,
	app_id integer not null,
	item_id integer,
	shortname varchar2(128),
	value varchar2(4000 char),
	assurance_level varchar2(128),
	created_by_uid integer,
	modified_by_uid integer
);

create table application_mails (
	id integer not null,
	form_id integer not null,
	app_type varchar2(30) not null,
	mail_type varchar2(30) not null,
	send varchar2(1) not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table application_mail_texts (
	mail_id integer not null,
	locale varchar2(10) not null,
	subject varchar2(1024),
	text varchar2(4000),
	created_by_uid integer,
	modified_by_uid integer
);

create table application_reserved_logins (
	login varchar2(256) not null,
	namespace varchar2(30) not null,
	app_id integer not null,
	created_by varchar2(1024) default user not null,
	created_at date default sysdate not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table facility_service_destinations (
	service_id integer not null,
	facility_id integer not null,
	destination_id integer not null,
	created_at date  default sysdate not null,
	created_by varchar2(1024) default user not null,
	modified_at date default sysdate not null,
	modified_by varchar2(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer,
	propagation_type varchar2(10) default 'PARALLEL'
);

create table entityless_attr_values (
	subject varchar2(256) not null,
	attr_id integer not null,
	attr_value varchar2(4000 char),
	created_at date  default sysdate not null,
	created_by varchar2(1024) default user not null,
	modified_at date default sysdate not null,
	modified_by varchar2(1024) default user not null,
	status char(1) default '0' not null,
	attr_value_text clob,
	created_by_uid integer,
	modified_by_uid integer
);

create table facility_attr_values (
	facility_id integer not null,
	attr_id integer not null,
	attr_value varchar2(4000 char),
	created_at date  default sysdate not null,
	created_by varchar2(1024) default user not null,
	modified_at date default sysdate not null,
	modified_by varchar2(1024) default user not null,
	status char(1) default '0' not null,
	attr_value_text clob,
	created_by_uid integer,
	modified_by_uid integer
);

create table group_attr_values (
	group_id integer not null,
	attr_id integer not null,
	attr_value varchar2(4000 char),
	created_at date  default sysdate not null,
	created_by varchar2(1024) default user not null,
	modified_at date default sysdate not null,
	modified_by varchar2(1024) default user not null,
	status char(1) default '0' not null,
	attr_value_text clob,
	created_by_uid integer,
	modified_by_uid integer
);

create table resource_attr_values (
	resource_id integer not null,
	attr_id integer not null,
	attr_value varchar2(4000 char),
	created_at date  default sysdate not null,
	created_by varchar2(1024) default user not null,
	modified_at date default sysdate not null,
	modified_by varchar2(1024) default user not null,
	status char(1) default '0' not null,
	attr_value_text clob,
	created_by_uid integer,
	modified_by_uid integer
);

create table group_resource_attr_values (
	group_id integer not null,
	resource_id integer not null,
	attr_id integer not null,
	attr_value varchar2(4000 char),
	created_at date  default sysdate not null,
	created_by varchar2(1024) default user not null,
	modified_at date default sysdate not null,
	modified_by varchar2(1024) default user not null,
	status char(1) default '0' not null,
	attr_value_text clob,
	created_by_uid integer,
	modified_by_uid integer
);

create table groups_members (
	group_id integer not null,
	member_id integer not null,
	created_at  date default sysdate not null,
	created_by varchar2(1024) default user not null,
	modified_at  date default sysdate not null,
	modified_by varchar2(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer,
	membership_type integer not null,
	source_group_id integer not null
);

create table groups_resources (
	group_id integer not null,
	resource_id integer not null,
	created_at  date default sysdate not null,
	created_by varchar2(1024) default user not null,
	modified_at  date default sysdate not null,
	modified_by varchar2(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table member_attr_values (
	member_id integer not null,
	attr_id integer not null,
	attr_value varchar2(4000 char),
	created_at date  default sysdate not null,
	created_by varchar2(1024) default user not null,
	modified_at date default sysdate not null,
	modified_by varchar2(1024) default user not null,
	status char(1) default '0' not null,
	attr_value_text clob,
	created_by_uid integer,
	modified_by_uid integer
);

create table member_resource_attr_values (
	member_id integer not null,
	resource_id integer not null,
	attr_id integer not null,
	attr_value varchar2(4000 char),
	created_at date  default sysdate not null,
	created_by varchar2(1024) default user not null,
	modified_at date default sysdate not null,
	modified_by varchar2(1024) default user not null,
	status char(1) default '0' not null,
	attr_value_text clob,
	created_by_uid integer,
	modified_by_uid integer
);

create table user_attr_values (
	user_id integer not null,
	attr_id integer not null,
	attr_value varchar2(4000 char),
	created_at date  default sysdate not null,
	created_by varchar2(1024) default user not null,
	modified_at date default sysdate not null,
	modified_by varchar2(1024) default user not null,
	status char(1) default '0' not null,
	attr_value_text clob,
	created_by_uid integer,
	modified_by_uid integer
);

create table user_facility_attr_values (
	user_id integer not null,
	facility_id integer not null,
	attr_id integer not null,
	attr_value varchar2(4000 char),
	created_at date  default sysdate not null,
	created_by varchar2(1024) default user not null,
	modified_at date default sysdate not null,
	modified_by varchar2(1024) default user not null,
	status char(1) default '0' not null,
	attr_value_text clob,
	created_by_uid integer,
	modified_by_uid integer
);

create table vo_attr_values (
	vo_id integer not null,
	attr_id integer not null,
	attr_value varchar2(4000 char),
	created_at date  default sysdate not null,
	created_by varchar2(1024) default user not null,
	modified_at date default sysdate not null,
	modified_by varchar2(1024) default user not null,
	status char(1) default '0' not null,
	attr_value_text clob,
	created_by_uid integer,
	modified_by_uid integer
);

create table ext_sources (
	id integer not null,
	name varchar2(256) not null,
	type varchar2(64),
	created_at date  default sysdate not null,
	created_by varchar2(1024) default user not null,
	modified_at date default sysdate not null,
	modified_by varchar2(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table ext_sources_attributes (
	ext_sources_id integer not null,
	attr_name varchar2(128) not null,
	attr_value varchar2(4000 char),
	created_at date  default sysdate not null,
	created_by varchar2(1024) default user not null,
	modified_at date default sysdate not null,
	modified_by varchar2(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table vo_ext_sources (
	vo_id integer not null,
	ext_sources_id integer not null,
	created_at date  default sysdate not null,
	created_by varchar2(1024) default user not null,
	modified_at date default sysdate not null,
	modified_by varchar2(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table user_ext_sources (
	id integer not null,
	user_id integer not null,
	login_ext varchar2(256) not null,
	ext_sources_id integer not null,
	created_at date  default sysdate not null,
	created_by varchar2(1024) default user not null,
	modified_at date default sysdate not null,
	modified_by varchar2(1024) default user not null,
	status char(1) default '0' not null,
	loa integer,
	last_access date default sysdate not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table service_packages (
	id integer not null,
	name varchar2(128) not null,
	description varchar2(512),
	created_at date  default sysdate not null,
	created_by varchar2(1024) default user not null,
	modified_at date default sysdate not null,
	modified_by varchar2(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table service_service_packages (
	service_id integer not null,
	package_id integer not null,
	created_at date  default sysdate not null,
	created_by varchar2(1024) default user not null,
	modified_at date default sysdate not null,
	modified_by varchar2(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table tasks (
	id integer not null,
	exec_service_id  integer not null,
	facility_id  integer not null,
	schedule date not null,
	recurrence integer not null,
	delay integer not null,
	status varchar2(16) not null,
	start_time date,
	end_time date,
	engine_id integer not null,
	created_at date  default sysdate not null,
	err_message varchar2(4000),
	created_by_uid integer,
	modified_by_uid integer
);

create table tasks_results (
	id integer not null,
	task_id integer not null,
	destination_id integer not null,
	status varchar2(16) not null,
	err_message varchar2(4000),
	std_message varchar2(4000),
	return_code integer,
	timestamp date,
	engine_id integer not null,
	created_at date  default sysdate not null,
	created_by varchar2(1024) default user not null,
	modified_at date default sysdate not null,
	modified_by varchar2(1024) default user not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table auditer_log (
	id integer not null,
	msg clob not null,
	actor varchar2(256) not null,
	created_at date default sysdate not null ,
	created_by_uid integer,
	modified_by_uid integer
);


create table service_principals (
	id integer not null,
	description varchar2(1024),
	name varchar2(128) not null,
	created_at date  default sysdate not null,
	created_by varchar2(1024) default user not null,
	modified_at date default sysdate not null,
	modified_by varchar2(1024) default user not null,
	status char(1) default '0' not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table reserved_logins (
	login varchar2(256),
	namespace varchar2(128),
	application varchar2(256),
	id varchar2(1024),
	created_by_uid integer,
	modified_by_uid integer
);

create table pn_audit_message (
	message clob,
	id integer NOT NULL,
	created_by_uid integer,
	modified_by_uid integer
);

create table pn_object (
	id integer NOT NULL,
	name varchar2(256),
	properties varchar2(4000),
	class_name varchar2(512),
	created_by_uid integer,
	modified_by_uid integer
);

create table pn_pool_message (
	id integer NOT NULL,
	regex_id integer NOT NULL,
	template_id integer NOT NULL,
	key_attributes varchar2(4000) NOT NULL,
	created date default sysdate NOT NULL,
	notif_message varchar2(1000) NOT NULL,
	created_by_uid integer,
	modified_by_uid integer
);

create table pn_receiver (
	id integer NOT NULL,
	target varchar2(256) NOT NULL,
	type_of_receiver varchar2(256) NOT NULL,
	template_id integer NOT NULL,
	created_by_uid integer,
	modified_by_uid integer,
	locale varchar2(512)
);

create table pn_regex (
	id integer NOT NULL,
	note varchar2(256),
	regex varchar2(4000) NOT NULL,
	created_by_uid integer,
	modified_by_uid integer
);

create table pn_template (
	id integer NOT NULL,
	primary_properties varchar2(4000) NOT NULL,
	notify_trigger varchar2(100),
	youngest_message_time integer,
	oldest_message_time integer,
	name varchar2(512),
	sender varchar2(4000),
	created_by_uid integer,
	modified_by_uid integer
);

create table pn_template_message (
	id integer NOT NULL,
	template_id integer NOT NULL,
	locale varchar2(5) NOT NULL,
	message varchar2(4000),
	created_by_uid integer,
	modified_by_uid integer,
	subject varchar2(512)
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
	created_at date  default sysdate not null,
	created_by varchar2(1024) default user not null,
	modified_at date default sysdate not null,
	modified_by varchar2(1024) default user not null
);

create table res_tags (
	id integer not null,
	vo_id integer not null,
	tag_name varchar2 (1024) not null,
	created_at date  default sysdate not null,
	created_by varchar2(1024) default user not null,
	modified_at date default sysdate not null,
	modified_by varchar2(1024) default user not null,
	created_by_uid integer,
	modified_by_uid integer
);

create table tags_resources (
	tag_id integer not null,
	resource_id integer not null
);

create table configurations (
	property varchar2(32) not null,
	value varchar2(128) not null
);

create table mailchange (
	id integer not null,
	value clob not null,
	user_id integer not null,
	created_at date default sysdate not null,
	created_by varchar(1024) default user not null,
	created_by_uid integer
);

create table pwdreset (
	id integer not null,
	namespace varchar2(512) not null,
	user_id integer not null,
	created_at date default sysdate not null,
	created_by varchar(1024) default user not null,
	created_by_uid integer
);

create sequence ATTR_NAMES_ID_SEQ maxvalue 1.0000E+28;
create sequence AUDITER_CONSUMERS_ID_SEQ maxvalue 1.0000E+28;
create sequence AUDITER_LOG_ID_SEQ maxvalue 1.0000E+28;
create sequence DESTINATIONS_ID_SEQ maxvalue 1.0000E+28;
create sequence EXEC_SERVICES_ID_SEQ maxvalue 1.0000E+28;
create sequence EXT_SOURCES_ID_SEQ maxvalue 1.0000E+28;
create sequence FACILITIES_ID_SEQ maxvalue 1.0000E+28;
create sequence GROUPS_ID_SEQ maxvalue 1.0000E+28;
create sequence HOSTS_ID_SEQ maxvalue 1.0000E+28;
create sequence MEMBERS_ID_SEQ maxvalue 1.0000E+28;
create sequence OWNERS_ID_SEQ maxvalue 1.0000E+28;
create sequence PROCESSING_RULES_ID_SEQ  maxvalue 1.0000E+28;
create sequence RESOURCES_ID_SEQ maxvalue 1.0000E+28;
create sequence ROUTING_RULES_ID_SEQ maxvalue 1.0000E+28;
create sequence SERVICES_ID_SEQ maxvalue 1.0000E+28;
create sequence SERVICE_DENIALS_ID_SEQ maxvalue 1.0000E+28;
create sequence SERVICE_PACKAGES_ID_SEQ maxvalue 1.0000E+28;
create sequence TASKS_ID_SEQ maxvalue 1.0000E+28;
create sequence TASKS_RESULTS_ID_SEQ maxvalue 1.0000E+28;
create sequence USERS_ID_SEQ maxvalue 1.0000E+28;
create sequence USER_EXT_SOURCES_ID_SEQ maxvalue 1.0000E+28;
create sequence VOS_ID_SEQ maxvalue 1.0000E+28;
create sequence CABINET_PUBLICATIONS_ID_SEQ maxvalue 1.0000E+28;
create sequence CABINET_PUB_SYS_ID_SEQ maxvalue 1.0000E+28;
create sequence CABINET_AUTHORSHIPS_ID_SEQ maxvalue 1.0000E+28;
create sequence CABINET_THANKS_ID_SEQ maxvalue 1.0000E+28;
create sequence CABINET_CATEGORIES_ID_SEQ maxvalue 1.0000E+28;
create sequence ROLES_ID_SEQ maxvalue 1.0000E+28;
create sequence SERVICE_PRINCIPALS_ID_SEQ maxvalue 1.0000E+28;
create sequence APPLICATION_FORM_ID_SEQ maxvalue 1.0000E+28;
create sequence APPLICATION_FORM_ITEMS_ID_SEQ maxvalue 1.0000E+28;
create sequence APPLICATION_ID_SEQ maxvalue 1.0000E+28;
create sequence APPLICATION_DATA_ID_SEQ maxvalue 1.0000E+28;
create sequence APPLICATION_MAILS_ID_SEQ maxvalue 1.0000E+28;
create sequence PN_OBJECT_ID_SEQ maxvalue 1.0000E+28;
create sequence PN_POOL_MESSAGE_ID_SEQ maxvalue 1.0000E+28;
create sequence PN_RECEIVER_ID_SEQ maxvalue 1.0000E+28;
create sequence PN_REGEX_ID_SEQ maxvalue 1.0000E+28;
create sequence PN_TEMPLATE_ID_SEQ maxvalue 1.0000E+28;
create sequence PN_AUDIT_MESSAGE_ID_SEQ maxvalue 1.0000E+28;
create sequence PN_TEMPLATE_REGEX_SEQ maxvalue 1.0000E+28;
create sequence PN_TEMPLATE_MESSAGE_ID_SEQ maxvalue 1.0000E+28;
create sequence PN_REGEX_OBJECT_SEQ maxvalue 1.0000E+28;
create sequence ACTION_TYPES_SEQ maxvalue 1.0000E+28;
create sequence RES_TAGS_SEQ maxvalue 1.0000E+28;
create sequence MAILCHANGE_ID_SEQ maxvalue 1.0000E+28;
create sequence PWDRESET_ID_SEQ maxvalue 1.0000E+28;

create index idx_namespace on attr_names(namespace);
create index idx_authz_user_role_id on authz (user_id,role_id);
create index idx_authz_authz_group_role_id on authz (authorized_group_id,role_id);
create index IDX_FK_CABTHANK_PUB on cabinet_thanks(publicationid);
create index IDX_FK_USREX_USR on user_ext_sources(user_id);
create index IDX_FK_USREX_USERSRC on user_ext_sources(ext_sources_id);
create index IDX_FK_MEM_USER on members(user_id);
create index IDX_FK_MEM_VO on members(vo_id);
create index IDX_FK_HOST_FAC on hosts(facility_id);
create index IDX_FK_SERV_OW on services(owner_id);
create index IDX_FK_EXSRV_SRV on exec_services(service_id);
create index IDX_FK_DEST_SRV on facility_service_destinations(service_id);
create index IDX_FK_DEST_FAC on facility_service_destinations(facility_id);
create index IDX_FK_DEST_DESTC on facility_service_destinations(destination_id);
create index IDX_FK_VOUSRSRC_USRSRC on vo_ext_sources(ext_sources_id);
create index IDX_FK_VOUSRSRC_VOS on vo_ext_sources(vo_id);
create index IDX_FK_USRCATT_USRC on ext_sources_attributes(ext_sources_id);
create index IDX_FK_ATTNAM_ATTNAM on attr_names(default_attr_id);
create index IDX_FK_RSRC_FAC on resources(facility_id);
create index IDX_FK_RSRC_VO on resources(vo_id);
create index IDX_FK_RESATVAL_RES on resource_attr_values(resource_id);
create index IDX_FK_RESATVAL_RESATNAM on resource_attr_values(attr_id);
create index IDX_FK_USRAV_USR on user_attr_values(user_id);
create index IDX_FK_USRAV_ACCATTNAM on user_attr_values(attr_id);
create index IDX_FK_FACOW_FAC on facility_owners(facility_id);
create index IDX_FK_FACOW_OW on facility_owners(owner_id);
create index IDX_FK_FACATTVAL_NAM on facility_attr_values(attr_id);
create index IDX_FK_FACATTVAL_FAC on facility_attr_values(facility_id);
create index IDX_FK_VOATTVAL_NAM on vo_attr_values(attr_id);
create index IDX_FK_VOATTVAL_VO on vo_attr_values(vo_id);
create index IDX_FK_SRVPKG_SRV on service_service_packages(service_id);
create index IDX_FK_SRVPKG_PKG on service_service_packages(package_id);
create index IDX_FK_GRP_VOS on groups(vo_id);
create index IDX_FK_GRP_GRP on groups(parent_group_id);
create index IDX_FK_MEMRAV_MEM on member_resource_attr_values(member_id);
create index IDX_FK_MEMRAV_RSRC on member_resource_attr_values(resource_id);
create index IDX_FK_MEMRAV_ACCATTNAM on member_resource_attr_values(attr_id);
create index IDX_FK_USRFACAV_MEM on user_facility_attr_values(user_id);
create index IDX_FK_USRFACAV_FAC on user_facility_attr_values(facility_id);
create index IDX_FK_USRFACAV_ACCATTNAM on user_facility_attr_values(attr_id);
create index IDX_FK_TASK_EXSRV on tasks(exec_service_id);
create index IDX_FK_TASK_FAC on tasks(facility_id);
create index IDX_FK_TASK_ENG on tasks(engine_id);
create index IDX_FK_TASKRES_TASK on tasks_results(task_id);
create index IDX_FK_TASKRES_DEST on tasks_results(destination_id);
create index IDX_FK_TASKRES_ENG on tasks_results(engine_id);
create index IDX_FK_SRVDEN_EXSRV on service_denials(exec_service_id);
create index IDX_FK_SRVDEN_FAC on service_denials(facility_id);
create index IDX_FK_SRVDEN_DEST on service_denials(destination_id);
create index IDX_FK_SRVDEP_EXSRV on service_dependencies(exec_service_id);
create index IDX_FK_SRVDEP_DEPEXSRV on service_dependencies(dependency_id);
create index IDX_FK_SRVREQATTR_SRV on service_required_attrs(service_id);
create index IDX_FK_SRVREQATTR_ATTR on service_required_attrs(attr_id);
create index IDX_FK_RESRCSRV_SRV on resource_services(service_id);
create index IDX_FK_RESRCSRV_RSRC on resource_services(resource_id);
create index IDX_FK_ENGRR_ENG on engine_routing_rule(engine_id);
create index IDX_FK_ENGRR_RR on engine_routing_rule(routing_rule_id);
create index IDX_FK_SERVPR_SERV on service_processing_rule(service_id);
create index IDX_FK_SERVPR_PR on service_processing_rule(processing_rule_id);
create index IDX_FK_MEMATTVAL_MEM on member_attr_values(member_id);
create index IDX_FK_MEMATTVAL_ATTR on member_attr_values(attr_id);
create index IDX_FK_GRPATTVAL_GRP on group_attr_values(group_id);
create index IDX_FK_GRPATTVAL_ATTR on group_attr_values(attr_id);
create index IDX_FK_GRPRESAV_GRP on group_resource_attr_values(group_id);
create index IDX_FK_GRPRESAV_RES on group_resource_attr_values(resource_id);
create index IDX_FK_GRPRESAV_ATTR on group_resource_attr_values(attr_id);
create index IDX_FK_HOSTAV_HOST on host_attr_values(host_id);
create index IDX_FK_HOSTAV_ATTRT on host_attr_values(attr_id);
create index IDX_FK_ENTLATVAL_ATTR on entityless_attr_values(attr_id);
create index IDX_FK_CATPUB_SYS on cabinet_publications(publicationSystemid);
create index IDX_FK_CABPUB_CAT on cabinet_publications(categoryid);
create index IDX_FK_AUTHZ_ROLE on authz(role_id);
create index IDX_FK_AUTHZ_USER on authz(user_id);
create index IDX_FK_AUTHZ_AUTHZ_GROUP on authz(authorized_group_id);
create index IDX_FK_AUTHZ_VO on authz(vo_id);
create index IDX_FK_AUTHZ_FAC on authz(facility_id);
create index IDX_FK_AUTHZ_MEM on authz(member_id);
create index IDX_FK_AUTHZ_GROUP on authz(group_id);
create index IDX_FK_AUTHZ_SERVICE on authz(service_id);
create index IDX_FK_AUTHZ_RES on authz(resource_id);
create index IDX_FK_AUTHZ_SER_PRINC on authz(service_principal_id);
create index IDX_FK_GRRES_GR on groups_resources(group_id);
create index IDX_FK_GRRES_RES on groups_resources(resource_id);
create index IDX_FK_GRPMEM_GR on groups_members(group_id);
create index IDX_FK_GRPMEM_MEM on groups_members(member_id);
create index IDX_FK_GRPMEM_MEMTYPE on groups_members(membership_type);
create index IDX_FK_APPLFORM_VO on application_form(vo_id);
create index IDX_FK_APPLFORM_GROUP on application_form(group_id);
create index IDX_FK_APPLFRMIT_APPLFORM on application_form_items(form_id);
create index IDX_FK_APPLFRMITTYP_APPLFRMIT on application_form_item_apptypes(item_id);
create index IDX_FK_APPLFRMITTXT_APPLFRMIT on application_form_item_texts(item_id);
create index IDX_FK_APP_VO on application(vo_id);
create index IDX_FK_APP_USER on application(user_id);
create index IDX_FK_APP_GROUP on application(group_id);
create index IDX_FK_APPDATA_APP on application_data(app_id);
create index IDX_FK_APPDATA_APPLFRMIT on application_data(item_id);
create index IDX_FK_APPLOGIN_APPID on application_reserved_logins(app_id);
create index IDX_FK_APPMAIL_APPFORM on application_mails(form_id);
create index IDX_FK_APPMAILTXT_APPMAILS on application_mail_texts(mail_id);
create index IDX_FK_CABAUT_PUB on cabinet_authorships(publicationId);
create index IDX_FK_CABAUT_USR on cabinet_authorships(userId);
create index IDX_FK_PN_POOLMSG_TMPL on pn_pool_message(template_id);
create index IDX_FK_PN_RECEIVER_TMPL on pn_receiver(template_id);
create index IDX_FK_PN_TMPLMSG_TMPL on pn_template_message(template_id);
create index IDX_FK_PN_TMPLRGX_RGX on pn_template_regex(regex_id);
create index IDX_FK_PN_TMPLRGX_TMPL on pn_template_regex(template_id);
create index IDX_FK_PN_RGXOBJ_RGX on pn_regex_object(regex_id);
create index IDX_FK_PN_RGXOBJ_OBJ on pn_regex_object(object_id);
create index IDX_FK_SERVU_U_UI on service_user_users(user_id);
create index IDX_FK_SERVU_U_SUI on service_user_users(service_user_id);
create index IDX_FK_GRP_GRP_GID on groups_groups(group_id);
create index IDX_FK_GRP_GRP_PGID on groups_groups(parent_group_id);
create index IDX_FK_ATTRAUTHZ_ACTIONTYP on attributes_authz(action_type_id);
create index IDX_FK_ATTRAUTHZ_ROLE on attributes_authz(role_id);
create index IDX_FK_ATTRAUTHZ_ATTR on attributes_authz(attr_id);
create index IDX_FK_RESTAGS_VOS on res_tags(vo_id);
create index IDX_FK_TAGS_RES_TAGS on tags_resources(tag_id);
create index IDX_FK_TAGS_RES_RES on tags_resources(resource_id);
create index IDX_FK_MAILCHANGE_USER_ID on mailchange(user_id);
create index IDX_FK_PWDRESET_USER_ID on pwdreset(user_id);

alter table auditer_log add (constraint AUDLOG_PK primary key (id));
alter table auditer_consumers add (constraint AUDCON_PK primary key (id),
constraint AUDCON_U unique(name));
alter table users add (
constraint USR_PK primary key (id),
constraint USR_SRVACC_CHK check (service_acc in ('0','1'))
);
alter table vos add (constraint VO_PK primary key (id),
constraint VO_U unique (name)
);
alter table ext_sources add (
constraint USRSRC_PK primary key(id),
constraint USRSRC_U unique (name)
);
alter table facilities add (constraint FAC_PK primary key(id),
constraint FAC_NAME_U UNIQUE (name)
);
alter table user_ext_sources add (
constraint USREX_P primary key(id),
constraint USREX_U unique (ext_sources_id,login_ext),
constraint USREX_USR_FK foreign key (user_id) references users(id),
constraint USREX_USERSRC_FK foreign key(ext_sources_id) references ext_sources(id)
);
alter table members add (constraint MEM_PK primary key(id),
constraint MEM_USER_FK foreign key(user_id) references users(id),
constraint MEM_VO_FK foreign key(vo_id) references vos(id)
);
alter table owners add (constraint OW_PK primary key (id),
constraint OW_U unique (name)
);
alter table hosts add (constraint HOST_PK primary key (id),
constraint HOST_FAC_FK foreign key(facility_id) references facilities(id)
);
alter table services add (
constraint SERV_PK primary key(id),
constraint SERV_U unique(name),
constraint SERV_OW_FK foreign key (owner_id) references owners(id)
);
alter table exec_services add (
constraint EXSRV_PK primary key(id),
constraint EXSRV_SRV_FK foreign key (service_id) references services(id),
constraint EXSRV_TYPE_CHK check (type IN ('SEND','GENERATE'))
);
alter table destinations add(
constraint DEST_PK primary key (id),
constraint DEST_U unique (destination,type)
);
alter table facility_service_destinations add (
constraint DEST_SRV_FK foreign key (service_id) references services(id),
constraint DEST_FAC_FK foreign key (facility_id) references facilities(id),
constraint DEST_DEST_FK foreign key(destination_id) references destinations(id)
);
alter table vo_ext_sources add (
constraint VOUSRSRC_PK primary key (vo_id,ext_sources_id),
constraint VOUSRSRC_USRSRC_FK foreign key(ext_sources_id) references ext_sources(id),
constraint VOUSRSRC_VOS_FK foreign key(vo_id) references vos(id)
);
alter table ext_sources_attributes add (
constraint USRCATT_USRC_FK foreign key (ext_sources_id) references ext_sources(id)
);
alter table attr_names add (constraint ATTNAM_PK primary key(id),
constraint ATTNAM_U unique (attr_name),
constraint ATTFULLNAM_U unique (friendly_name,namespace),
constraint ATTNAM_ATTNAM_FK foreign key (default_attr_id) references attr_names(id)
);
alter table resources add (
constraint RSRC_PK primary key (id),
constraint RSRC_FAC_FK foreign key (facility_id) references facilities(id),
constraint RSRC_VO_FK foreign key (vo_id) references vos(id)
);
alter table resource_attr_values add (
constraint RESATVAL_PK primary key (resource_id,attr_id),
constraint RESATVAL_RES_FK foreign key(resource_id) references resources(id),
constraint RESATVAL_RESATNAM_FK foreign key(attr_id) references attr_names(id)
);
alter table user_attr_values add (
constraint USRAV_USR_FK foreign key (user_id) references users(id),
constraint USRAV_ACCATTNAM_FK foreign key (attr_id) references attr_names(id),
constraint USRAV_U unique(user_id,attr_id)
);
alter table facility_owners add (
constraint FACOW_PK primary key (facility_id,owner_id),
constraint FACOW_FAC_FK foreign key (facility_id) references facilities(id),
constraint FACOW_OW_FK foreign key (owner_id) references owners(id)
);
alter table facility_attr_values add (
constraint FACATTVAL_PK primary key (facility_id,attr_id),
constraint FACATTVAL_NAM_FK foreign key (attr_id) references attr_names(id),
constraint FACATTVAL_FAC_FK foreign key (facility_id) references facilities (id)
);
alter table vo_attr_values add (
constraint VOATTVAL_PK primary key (vo_id,attr_id),
constraint VOATTVAL_NAM_FK foreign key (attr_id) references attr_names(id),
constraint VOATTVAL_VO_FK foreign key (vo_id) references vos (id)
);
alter table service_packages add(
constraint PKG_PK primary key (id),
constraint PKG_NAME unique(name)
);
alter table service_service_packages add (
constraint SRVPKG_SRV_PK primary key(service_id,package_id),
constraint SRVPKG_SRV_FK foreign key(service_id) references services(id),
constraint SRVPKG_PKG_FK foreign key(package_id) references service_packages(id)
);
alter table groups add (
constraint GRP_PK primary key (id),
constraint GRP_NAM_VO_PARENTG_U unique (name,vo_id,parent_group_id),
constraint GRP_VOS_FK foreign key (vo_id) references vos(id),
constraint GRP_GRP_FK foreign key (parent_group_id) references groups(id)
);
alter table member_resource_attr_values add (
constraint MEMRAV_MEM_FK foreign key (member_id) references members(id),
constraint MEMRAV_RSRC_FK foreign key (resource_id) references resources(id),
constraint MEMRAV_ACCATTNAM_FK foreign key (attr_id) references attr_names(id),
constraint MEMRAV_U unique(member_id,resource_id,attr_id)
);
alter table user_facility_attr_values add (
constraint USRFACAV_MEM_FK foreign key (user_id) references users(id),
constraint USRFACAV_FAC_FK foreign key (facility_id) references facilities(id),
constraint USRFACAV_ACCATTNAM_FK foreign key (attr_id) references attr_names(id),
constraint USRFACAV_U unique(user_id,facility_id,attr_id)
);
alter table engines add (constraint ENG_PK primary key (id)
);
alter table tasks add (
constraint TASK_PK primary key (id),
constraint TASK_EXSRV_FK foreign key (exec_service_id) references exec_services(id),
constraint TASK_FAC_FK foreign key (facility_id) references facilities(id),
constraint TASK_ENG_FK foreign key (engine_id) references engines (id),
constraint TASK_STAT_CHK check (status in ('NONE','OPEN','PLANNED','PROCESSING','DONE','ERROR'))
);
alter table tasks_results add (
constraint TASKS_RESULTS_PK primary key (id),
constraint TASKRES_TASK_FK foreign key (task_id) references tasks(id),
constraint TASKRES_DEST_FK foreign key (destination_id) references destinations(id),
constraint TASKRES_ENG_FK foreign key (engine_id) references engines (id),
constraint TASKRES_STAT_CHK check (status in ('DONE','ERROR','FATAL_ERROR','DENIED'))
);
alter table service_denials add (
constraint SRVDEN_PK primary key (id),
constraint SRVDEN_EXSRV_FK foreign key (exec_service_id) references exec_services(id),
constraint SRVDEN_FAC_FK foreign key (facility_id) references facilities(id),
constraint SRVDEN_DEST_FK foreign key (destination_id) references destinations(id)
);
alter table service_dependencies add (
constraint SRVDEP_EXSRV_FK foreign key (exec_service_id) references exec_services(id),
constraint SRVDEP_DEPEXSRV_FK foreign key (dependency_id) references exec_services(id),
constraint SRVDEP_TYPE_CHK check (type in ('SERVICE','DESTINATION'))
);
alter table service_required_attrs add (
constraint SRVREQATTR_PK primary key (service_id,attr_id),
constraint SRVREQATTR_SRV_FK foreign key(service_id) references services(id),
constraint SRVREQATTR_ATTR_FK foreign key(attr_id) references attr_names(id)
);
alter table resource_services add(
constraint RESRCSRV_PK primary key (service_id,resource_id),
constraint RESRCSRV_SRV_FK foreign key (service_id) references services(id),
constraint RESRCSRV_RSRC_FK foreign key (resource_id) references resources(id)
);
alter table routing_rules add (
constraint ROUTRUL_PK primary key (id)
);
alter table engine_routing_rule add (
constraint ENGRR_ENG_FK foreign key (engine_id) references engines(id),
constraint ENGRR_RR_FK foreign key (routing_rule_id) references routing_rules(id)
);
alter table processing_rules add (
constraint PROCRUL_PK primary key (id)
);
alter table service_processing_rule add (
constraint SERVPR_SERV_FK foreign key (service_id) references services(id),
constraint SERVPR_PR_FK foreign key (processing_rule_id) references processing_rules(id)
);
alter table member_attr_values add (
constraint MEMATTVAL_PK primary key (member_id,attr_id),
constraint MEMATTVAL_MEM_FK foreign key (member_id) references members(id),
constraint MEMATTVAL_ATTR_FK foreign key (attr_id) references attr_names(id)
);
alter table group_attr_values add (
constraint GRPATTVAL_PK primary key (group_id,attr_id),
constraint GRPATTVAL_GRP_FK foreign key (group_id) references groups(id),
constraint GRPATTVAL_ATTR_FK foreign key (attr_id) references attr_names(id)
);
alter table group_resource_attr_values add (
constraint GRPRESAV_PK primary key (group_id,resource_id,attr_id),
constraint GRPRESAV_GRP_FK foreign key (group_id) references groups(id),
constraint GRPRESAV_RES_FK foreign key (resource_id) references resources(id),
constraint GRPRESAV_ATTR_FK foreign key (attr_id) references attr_names(id)
);
alter table host_attr_values add (
constraint HOSTAV_PK primary key (host_id,attr_id),
constraint HOSTAV_HOST_FK foreign key (host_id) references hosts(id),
constraint HOSTAV_ATTR_FK foreign key (attr_id) references attr_names(id)
);
alter table entityless_attr_values add (
constraint ENTLATVAL_PK primary key(subject,attr_id),
constraint ENTLATVAL_ATTR_FK foreign key (attr_id) references attr_names(id)
);

alter table cabinet_categories add (
constraint CAB_CAT_PK primary key (id)
);

alter table cabinet_publication_systems add (
constraint CAB_PUBSYS_PK primary key (id)
);

alter table cabinet_publications add (
constraint CAB_PUB_PK primary key (id),
constraint CATPUB_SYS_FK foreign key(publicationSystemId) references cabinet_publication_systems(id),
constraint CABPUB_CAT_FK foreign key(categoryId) references cabinet_categories(id)
);

alter table cabinet_authorships add (
constraint CAB_AU_PK primary key (id),
constraint CABAUT_PUB_FK foreign key(publicationId) references cabinet_publications(id),
constraint CABAUT_USR_FK foreign key(userId) references users(id)
);

alter table cabinet_thanks add (
constraint CAB_TH_PK primary key (id),
constraint CABTHANK_PUB_FK foreign key(publicationid) references cabinet_publications(id));

alter table roles add (
constraint ROLES_PK primary key (id),
constraint ROLES_NAME_U unique (name)
);

alter table service_principals add (
constraint SER_PRINC_PK primary key (id)
);

alter table authz add (
constraint AUTHZ_ROLE_FK foreign key (role_id) references roles(id),
constraint AUTHZ_USER_FK foreign key (user_id) references users(id),
constraint AUTHZ_AUTHZ_GROUP_FK foreign key (authorized_group_id) references groups(id),
constraint AUTHZ_VO_FK foreign key (vo_id) references vos(id),
constraint AUTHZ_FAC_FK foreign key (facility_id) references facilities(id),
constraint AUTHZ_MEM_FK foreign key (member_id) references members(id),
constraint AUTHZ_GROUP_FK foreign key (group_id) references groups(id),
constraint AUTHZ_SERVICE_FK foreign key (service_id) references services(id),
constraint AUTHZ_RES_FK foreign key (resource_id) references resources(id),
constraint AUTHZ_SER_PRINC_FK foreign key (service_principal_id) references service_principals(id),
constraint AUTHZ_USER_SERPRINC_AUTGRP_CHK check (decode(user_id,null,0,1)+decode(service_principal_id,null,0,1)+decode(authorized_group_id,null,0,1) = 1),
constraint AUTHZ_U2 unique (user_id,authorized_group_id,role_id,vo_id,facility_id,member_id,group_id,service_id,resource_id,service_principal_id)
);

alter table groups_resources add (
constraint GRRES_GRP_RES_U unique (group_id,resource_id),
constraint GRRES_GR_FK foreign key (group_id) references groups(id),
constraint GRRES_RES_FK foreign key (resource_id) references resources(id)
);

alter table membership_types add (
constraint MEMTYPE_PK primary key (id)
);
alter table groups_members add (
constraint GRPMEM_PK primary key (member_id,group_id, source_group_id),
constraint GRPMEM_GR_FK foreign key (group_id) references groups(id),
constraint GRPMEM_MEM_FK foreign key (member_id) references members(id),
constraint GRPMEM_MEMTYPE_FK foreign key (membership_type) references membership_types(id)
);

alter table application_form add (
constraint APPLFORM_PK primary key (id),
constraint APPLFORM_U unique (vo_id,group_id),
constraint APPLFORM_VO_FK foreign key (vo_id) references vos(id) on delete cascade,
constraint APPLFORM_GROUP_FK foreign key (group_id) references groups(id) on delete cascade
);

alter table application_form_items add (
constraint APPLFRMIT_PK primary key (id),
constraint APPLFRMIT_APPLFORM_FK foreign key (form_id) references application_form(id) on delete cascade
);

alter table application_form_item_apptypes add (
constraint APPLFRMITTYP_APPLFRMIT_FK foreign key (item_id) references application_form_items(id) on delete cascade
);

alter table application_form_item_texts add (
constraint APPLFRMITTXT_PK primary key(item_id,locale),
constraint APPLFRMITTXT_APPLFRMIT_FK foreign key (item_id) references application_form_items(id) on delete cascade
);


alter table application add (
constraint APP_PK primary key (id),
constraint APP_VO_FK foreign key (vo_id) references vos(id) on delete cascade,
constraint APP_USER_FK foreign key (user_id) references users(id) on delete cascade,
constraint APP_GROUP_FK foreign key (group_id) references groups(id) on delete cascade,
constraint APP_STATE_CHK check (state in ('REJECTED','NEW','VERIFIED','APPROVED'))
);

alter table application_data add (
constraint APPDATA_PK primary key (id),
constraint APPDATA_APP_FK foreign key (app_id) references application(id) on delete cascade,
constraint APPDATA_APPLFRMIT_FK foreign key (item_id) references application_form_items(id) on delete cascade
);

alter table application_reserved_logins add (
constraint APP_LOGINS_PK primary key(login, namespace),
constraint APPLOGIN_APPID_FK foreign key(app_id) references application(id)
);

alter table application_mails add (
constraint APPMAILS_PK primary key (id),
constraint APPMAILS_U unique (form_id,app_type,mail_type),
constraint APPMAIL_APPFORM_FK foreign key (form_id) references application_form(id) on delete cascade
);

alter table application_mail_texts add (
constraint APPMAILTXT_PK primary key (mail_id, locale),
constraint APPMAILTXT_APPMAILS_FK foreign key (mail_id) references application_mails(id) on delete cascade
);

alter table reserved_logins add (
constraint RESERVLOGINS_PK primary key (login,namespace)
);

alter table  pn_audit_message add (
constraint PN_AUDMSG_PK primary key (id)
);

alter table  pn_object add (
constraint PN_OBJECT_PK primary key (id)
);

alter table  pn_template add (
constraint PN_TMPL_PK primary key (id)
);

alter table  pn_pool_message add (
constraint PN_POOLMSG_PK primary key (id),
constraint PN_POOLMSG_TMPL_FK foreign key (template_id) references pn_template(id)
);

alter table  pn_receiver add (
constraint PN_RECEIVER_PK primary key (id),
constraint PN_RECEIVER_TMPL_FK foreign key (template_id) references pn_template(id)
);

alter table  pn_regex add (
constraint PN_REGEX_PK primary key (id)
);

alter table pn_template_message add (
constraint PN_TMPLMSG_PK primary key (id),
constraint PN_TMPLMSG_TMPL_FK foreign key (template_id) references pn_template(id) DEFERRABLE
);

alter table pn_template_regex add (
constraint PN_TMPLRGX_PK primary key (id),
constraint PN_TMPLRGX_RGX_FK foreign key (regex_id) references pn_regex(id),
constraint PN_TMPLRGX_TMPL_FK foreign key (template_id) references pn_template(id)
);

alter table pn_regex_object add (
constraint PN_RGXOBJ_PK primary key (id),
constraint PN_RGXOBJ_RGX_FK foreign key (regex_id) references pn_regex(id),
constraint PN_RGXOBJ_OBJ_FK foreign key (object_id) references pn_object(id)
);

alter table service_user_users add (
constraint ACC_SERVU_U_PK primary key (user_id,service_user_id),
constraint ACC_SERVU_U_UID_FK foreign key (user_id) references users(id),
constraint ACC_SERVU_U_SUID_FK foreign key (service_user_id) references users(id),
constraint SERVU_U_STATUS_CHK check (status in ('0','1'))
);

alter table groups_groups add (
constraint GRP_GRP_PK primary key (group_id,parent_group_id),
constraint GRP_GRP_GID_FK foreign key (group_id) references groups(id),
constraint GRP_GRP_PGID_FK foreign key (parent_group_id) references groups(id)
);

alter table action_types add (
constraint ACTIONTYP_PK primary key (id),
constraint ACTIONTYP_U unique (action_type),
constraint ACTIONTYP_AT_CHK check (action_type in ('read','write'))
);

alter table attributes_authz add (
constraint ATTRAUTHZ_PK primary key (attr_id,role_id,action_type_id),
constraint ATTRAUTHZ_ATTR_FK foreign key (attr_id) references attr_names (id),
constraint ATTRAUTHZ_ROLE_FK foreign key (role_id) references roles(id),
constraint ATTRAUTHZ_ACTIONTYP_FK foreign key (action_type_id) references action_types(id)
);

alter table res_tags add (
constraint RESTAGS_PK primary key (id),
constraint RESTAGS_U unique (vo_id, tag_name),
constraint RESTAGS_VOS_FK foreign key (vo_id) references vos(id)
);

alter table tags_resources add (
constraint TAGS_RES_PK primary key (tag_id,resource_id),
constraint TAGS_RES_TAGS_FK foreign key (tag_id) references res_tags(id),
constraint TAGS_RES_RES_FK  foreign key (resource_id) references resources(id)
);

alter table configurations add (
constraint CONFIG_PK primary key (property),
constraint CONFIG_PROP_CHK check (property in ('DATABASE VERSION'))
);

alter table mailchange add (
constraint mailchange_pk primary key (id),
constraint mailchange_u_fk foreign key (user_id) references users(id)
);

alter table pwdreset add (
constraint pwdreset_pk primary key (id),
constraint pwdreset_u_fk foreign key (user_id) references users(id)
);
