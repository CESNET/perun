-- database version 3.2.32 (don't forget to update insert statement at the end of file)
CREATE
EXTENSION IF NOT EXISTS "unaccent";
CREATE
EXTENSION IF NOT EXISTS "pgcrypto";

-- VOS - virtual organizations
create table vos
(
    id              integer                                 not null,
    uu_id           uuid                                    not null default gen_random_uuid(),
    name            varchar                                 not null, -- full name of VO
    short_name      varchar                                 not null, -- commonly used name
    created_at      timestamp default statement_timestamp() not null,
    created_by      varchar   default user                  not null,
    modified_at     timestamp default statement_timestamp() not null,
    modified_by     varchar   default user                  not null,
    created_by_uid  integer,
    modified_by_uid integer,
    constraint vo_pk primary key (id),
    constraint vo_u unique (short_name)
);

-- USERS - information about user as real person
create table users
(
    id              integer                                 not null,
    uu_id           uuid                                    not null default gen_random_uuid(),
    first_name      varchar,                                          -- christening name
    last_name       varchar,                                          -- family name
    middle_name     varchar,                                          -- second name
    title_before    varchar,                                          -- academic degree used before name
    title_after     varchar,                                          -- academic degree used after name
    created_at      timestamp default statement_timestamp() not null,
    created_by      varchar   default user                  not null,
    modified_at     timestamp default statement_timestamp() not null,
    modified_by     varchar   default user                  not null,
    service_acc     boolean   default false                 not null, --is it service account?
    sponsored_acc   boolean   default false                 not null, --is it sponsored account?
    anonymized      boolean   default false                 not null, --was user anonymized?
    created_by_uid  integer,
    modified_by_uid integer,
    constraint usr_pk primary key (id)
);

-- OWNERS - owners of resources and devices
create table owners
(
    id              integer                                 not null,
    name            varchar                                 not null, --name of owner
    contact         varchar,                                          --contact email or phone
    created_at      timestamp default statement_timestamp() not null,
    created_by      varchar   default user                  not null,
    modified_at     timestamp default statement_timestamp() not null,
    modified_by     varchar   default user                  not null,
    type            varchar                                 not null, --type of owner (for example IdP)
    created_by_uid  integer,
    modified_by_uid integer,
    constraint ow_pk primary key (id),
    constraint ow_u unique (name)
);

-- CABINET_CATEGORIES - possible categories of publications
create table cabinet_categories
(
    id              integer        not null,
    name            varchar        not null, --name of category
    rank            numeric(38, 1) not null, --coefficient for evaluation of publication in scope of this category
    created_by_uid  integer,
    modified_by_uid integer,
    constraint cab_cat_pk primary key (id)
);

-- CABINET_PUBLICATION_SYSTEMS - external publication systems. Contains information which allowes searching
create table cabinet_publication_systems
(
    id              integer not null,
    friendlyName    varchar not null, --name of publication system
    url             varchar not null, --address for searching at external system
    username        varchar,          --logname
    password        varchar,          -- and password for connection to external system
    loginNamespace  varchar not null, --namespace used for username
    type            varchar not null, --name of class of parser for received data (for example cz.metacentrum.perun.cabinet.strategy.impl.MUStrategy) *)
    created_by_uid  integer,
    modified_by_uid integer,
    constraint cab_pubsys_pk primary key (id)
);
--*) it have to include entry about internal publication system to create publication directly in Perun DB

-- CABINET_PUBLICATIONS - all publications stored in Perun DB
create table cabinet_publications
(
    id                  integer                      not null,
    externalId          integer                      not null, --identifier at externa pub. system
    publicationSystemId integer                      not null, --identifier of external pub. system (cabinet_publication_systems.id) *)
    title               varchar                      not null,
    year                integer                      not null, --short title of publication
    main                varchar,                               --full cite of publication
    isbn                varchar,
    categoryId          integer                      not null, --identifier of category (cabinet_categories.id)
    createdBy           varchar        default user  not null,
    createdDate         timestamp                    not null,
    rank                numeric(38, 1) default 0     not null,
    doi                 varchar,
    locked              boolean        default false not null,
    created_by_uid      integer,
    modified_by_uid     integer,
    constraint cab_pub_pk primary key (id),
    constraint catpub_sys_fk foreign key (publicationsystemid) references cabinet_publication_systems (id),
    constraint cabpub_cat_fk foreign key (categoryid) references cabinet_categories (id)
);
--*) if publication is created directly in Perun externalId=id and publicationSystemId is identifier of internal system

-- CABINET_AUTHORSHIPS - relation of user to publication (author,co-author)
create table cabinet_authorships
(
    id              integer              not null,
    publicationId   integer              not null, --identifier of publication (cabinet_publications.id)
    userId          integer              not null, -- identifier of user (users.id)
    createdBy       varchar default user not null,
    createdDate     timestamp            not null,
    created_by_uid  integer,
    modified_by_uid integer,
    constraint cab_au_pk primary key (id),
    constraint cabaut_pub_fk foreign key (publicationid) references cabinet_publications (id),
    constraint cabaut_usr_fk foreign key (userid) references users (id)
);

-- CABINET THANKS - list of institutions which are acnowledged at publication
create table cabinet_thanks
(
    id              integer              not null,
    publicationid   integer              not null, --identifier of publication (cabinet_publications.id)
    ownerId         integer              not null, --identifier of owner of used ources and devices (owners.id) - MetaCenter,CESNET...
    createdBy       varchar default user not null,
    createdDate     timestamp            not null,
    created_by_uid  integer,
    modified_by_uid integer,
    constraint cab_th_pk primary key (id),
    constraint cabthank_pub_fk foreign key (publicationid) references cabinet_publications (id)
);

-- FACILITIES - sources, devices - includes clusters,hosts,storages...
create table facilities
(
    id              integer                                 not null,
    uu_id           uuid                                    not null default gen_random_uuid(),
    name            varchar                                 not null, --unique name of facility
    dsc             varchar,
    created_at      timestamp default statement_timestamp() not null,
    created_by      varchar   default user                  not null,
    modified_at     timestamp default statement_timestamp() not null,
    modified_by     varchar   default user                  not null,
    created_by_uid  integer,
    modified_by_uid integer,
    constraint fac_pk primary key (id),
    constraint fac_name_u unique (name)
);

-- RESOURCES - facility assigned to VO
create table resources
(
    id              integer                                 not null,
    uu_id           uuid                                    not null default gen_random_uuid(),
    facility_id     integer                                 not null, --facility identifier (facility.id)
    name            varchar                                 not null, --name of resource
    dsc             varchar,                                          --purpose and description
    created_at      timestamp default statement_timestamp() not null,
    created_by      varchar   default user                  not null,
    modified_at     timestamp default statement_timestamp() not null,
    modified_by     varchar   default user                  not null,
    vo_id           integer                                 not null, --identifier of VO (vos.id)
    created_by_uid  integer,
    modified_by_uid integer,
    constraint rsrc_pk primary key (id),
    constraint rsrc_fac_fk foreign key (facility_id) references facilities (id),
    constraint rsrc_vo_fk foreign key (vo_id) references vos (id)
);

-- DESTINATIONS - targets of services
create table destinations
(
    id              integer                                 not null,
    destination     varchar                                 not null, --value of destination (hostname,URL...)
    type            varchar                                 not null, --type (host,URL...)
    created_at      timestamp default statement_timestamp() not null,
    created_by      varchar   default user                  not null,
    modified_at     timestamp default statement_timestamp() not null,
    modified_by     varchar   default user                  not null,
    created_by_uid  integer,
    modified_by_uid integer,
    constraint dest_pk primary key (id),
    constraint dest_u unique (destination, type)
);

-- FACILITY_OWNERS - one or more institutions which own the facility
create table facility_owners
(
    facility_id     integer                                 not null, --identifier of facility (facilities.id)
    owner_id        integer                                 not null, --identifier of owner (owners.id)
    created_at      timestamp default statement_timestamp() not null,
    created_by      varchar   default user                  not null,
    modified_at     timestamp default statement_timestamp() not null,
    modified_by     varchar   default user                  not null,
    created_by_uid  integer,
    modified_by_uid integer,
    constraint facow_pk primary key (facility_id, owner_id),
    constraint facow_fac_fk foreign key (facility_id) references facilities (id),
    constraint facow_ow_fk foreign key (owner_id) references owners (id)
);

-- GROUPS - groups of users
create table groups
(
    id              integer                                 not null,
    uu_id           uuid                                    not null default gen_random_uuid(),
    name            text                                    not null, --group name
    dsc             varchar,                                          --purpose and description
    vo_id           integer                                 not null, --identifier of VO (vos.id)
    created_at      timestamp default statement_timestamp() not null,
    created_by      varchar   default user                  not null,
    modified_at     timestamp default statement_timestamp() not null,
    modified_by     varchar   default user                  not null,
    parent_group_id integer,                                          --in case of subgroup identifier of parent group (groups.id)
    created_by_uid  integer,
    modified_by_uid integer,
    constraint grp_pk primary key (id),
    constraint grp_vos_fk foreign key (vo_id) references vos (id),
    constraint grp_grp_fk foreign key (parent_group_id) references groups (id)
);

-- MEMBERS - members of VO
create table members
(
    id              integer                                 not null,
    user_id         integer                                 not null, --user's identifier (users.id)
    vo_id           integer                                 not null, --identifier of VO (vos.id)
    created_at      timestamp default statement_timestamp() not null,
    created_by      varchar   default user                  not null,
    modified_at     timestamp default statement_timestamp() not null,
    modified_by     varchar   default user                  not null,
    status          integer   default 0                     not null, --status of membership
    sponsored       boolean   default false                 not null,
    created_by_uid  integer,
    modified_by_uid integer,
    constraint mem_pk primary key (id),
    constraint mem_user_fk foreign key (user_id) references users (id),
    constraint mem_vo_fk foreign key (vo_id) references vos (id),
    constraint mem_user_vo_u unique (vo_id, user_id)
);

-- ROLES - possible user's rolles - controle access of users to data in DB
create table roles
(
    id              integer not null,
    name            varchar not null, --name of role
    created_by_uid  integer,
    modified_by_uid integer,
    constraint roles_pk primary key (id),
    constraint roles_name_u unique (name)
);

-- MEMBERSHIP_TYPES - possible types of membership in group
create table membership_types
(
    id              integer not null,
    membership_type varchar not null, --type of memberships (DIRECT/INDIRECT...)
    description     varchar,          --description
    constraint MEMTYPE_PK primary key (id)
);

-- ATTR_NAMES - list of possible attributes
create table attr_names
(
    id              integer                                 not null,
    attr_name       varchar                                 not null, --full name of attribute
    friendly_name   varchar                                 not null, --short name of attribute
    namespace       varchar                                 not null, --access of attribute to the entity
    type            varchar                                 not null, --type o0f attribute data (strig,number,array...)
    dsc             varchar,                                          --purpose,description
    created_at      timestamp default statement_timestamp() not null,
    created_by      varchar   default user                  not null,
    modified_at     timestamp default statement_timestamp() not null,
    modified_by     varchar   default user                  not null,
    created_by_uid  integer,
    modified_by_uid integer,
    display_name    varchar,                                          --name of attr. displayed at GUI
    is_unique       boolean   DEFAULT FALSE                 NOT NULL,
    constraint attnam_pk primary key (id),
    constraint attnam_u unique (attr_name),
    constraint attfullnam_u unique (friendly_name, namespace)
);

create type attribute_action as enum (
	'READ',
	'WRITE'
	);

-- ATTRIBUTE_POLICY_COLLECTIONS - controls permissions for access to attributes
create table attribute_policy_collections
(
    id      integer          not null,
    attr_id integer          not null, --identifier of attribute (attr_names.id)
    action  attribute_action not null, --action on attribute (READ/WRITE)
    constraint attrpolcol_pk primary key (id),
    constraint attrpolcol_attr_fk foreign key (attr_id) references attr_names (id) on delete cascade
);

create type role_object as enum (
	'None',
	'Group',
	'Vo',
	'Facility',
	'Resource',
	'User',
	'Member'
	);

-- ATTRIBUTE_POLICIES - controls permissions for access to attributes
create table attribute_policies
(
    id                   integer     not null,
    role_id              integer     not null, --identifier of role (roles.id)
    object               role_object not null, --object upon which the role is set (e.g. Group)
    policy_collection_id integer     not null, --identifier of attribute policy collection (attribute_policy_collections.id)
    constraint attrpol_pk primary key (id),
    constraint attrpol_attr_fk foreign key (policy_collection_id) references attribute_policy_collections (id) on delete cascade,
    constraint attrpol_role_fk foreign key (role_id) references roles (id)
);

-- ATTRIBUTE_CRITICAL_ACTIONS - critical actions on attributes which may require additional authentication
create table attribute_critical_actions
(
    attr_id integer               not null, --identifier of attribute (attr_names.id)
    action  attribute_action      not null, --action on attribute (READ/WRITE)
    global  boolean default false not null, --action is critical globally for all objects
    constraint attrcritops_pk primary key (attr_id, action),
    constraint attrcritops_attr_fk foreign key (attr_id) references attr_names (id) on delete cascade
);

-- CONSENT_HUBS -- list of facilities with joint consent management
create table consent_hubs
(
    id               integer                                 not null,
    name             varchar                                 not null, --unique name of consent hub
    enforce_consents boolean                                 not null, --does this consent hub enforce consents for propagation?
    created_at       timestamp default statement_timestamp() not null,
    created_by       varchar   default user                  not null,
    modified_at      timestamp default statement_timestamp() not null,
    modified_by      varchar   default user                  not null,
    created_by_uid   integer,
    modified_by_uid  integer,
    constraint consent_hub_pk primary key (id),
    constraint consent_hub_name_u unique (name)
);

-- CONSENT_HUBS_FACILITIES -- consent hubs contain facilities
create table consent_hubs_facilities
(
    consent_hub_id  integer                                 not null, --identifier of consent_hub
    facility_id     integer                                 not null, --identifier of facility
    created_at      timestamp default statement_timestamp() not null,
    created_by      varchar   default user                  not null,
    modified_at     timestamp default statement_timestamp() not null,
    modified_by     varchar   default user                  not null,
    created_by_uid  integer,
    modified_by_uid integer,
    constraint conhubfac_ch_fk foreign key (consent_hub_id) references consent_hubs (id),
    constraint conhubfac_fac_fk foreign key (facility_id) references facilities (id),
    constraint facility_id_u unique (facility_id)
);

-- HOSTS - detail information about hosts and cluster nodes
create table hosts
(
    id              integer                                 not null,
    hostname        varchar                                 not null, --full name of machine
    facility_id     integer                                 not null, --identifier of facility containing the host (facilities.id)
    dsc             varchar,                                          --description
    created_at      timestamp default statement_timestamp() not null,
    created_by      varchar   default user                  not null,
    modified_at     timestamp default statement_timestamp() not null,
    modified_by     varchar   default user                  not null,
    created_by_uid  integer,
    modified_by_uid integer,
    constraint host_pk primary key (id),
    constraint host_fac_fk foreign key (facility_id) references facilities (id)
);

-- HOST_ATTR_VALUES - values of attributes assigned to hosts
create table host_attr_values
(
    host_id         integer                                 not null, --identifier of host (hosts.id)
    attr_id         integer                                 not null, --identifier of attributes (attr_names.id)
    attr_value      text,                                             --value of attribute
    created_at      timestamp default statement_timestamp() not null,
    created_by      varchar   default user                  not null,
    modified_at     timestamp default statement_timestamp() not null,
    modified_by     varchar   default user                  not null,
    created_by_uid  integer,
    modified_by_uid integer,
    constraint hostav_pk primary key (host_id, attr_id),
    constraint hostav_host_fk foreign key (host_id) references hosts (id),
    constraint hostav_attr_fk foreign key (attr_id) references attr_names (id)
);

-- HOST_ATTR_U_VALUES - unique attribute values
CREATE TABLE host_attr_u_values
(
    host_id    INT NOT NULL,
    attr_id    INT NOT NULL,
    attr_value text,
    UNIQUE (attr_id, attr_value),
    FOREIGN KEY (host_id, attr_id) REFERENCES host_attr_values ON DELETE CASCADE
);

-- AUDITER_CONSUMERS - registers recently processed events
create table auditer_consumers
(
    id                integer                                 not null,
    name              varchar                                 not null,
    last_processed_id integer,
    created_at        timestamp default statement_timestamp() not null,
    created_by        varchar   default user                  not null,
    modified_at       timestamp default statement_timestamp() not null,
    modified_by       varchar   default user                  not null,
    created_by_uid    integer,
    modified_by_uid   integer,
    constraint audcon_pk primary key (id),
    constraint audcon_u unique (name)
);

-- SERVICES - provided services, their atomic form
create table services
(
    id                     integer                                 not null,
    name                   varchar                                 not null, --name of service
    description            varchar,
    delay                  integer                                 not null default 10,
    recurrence             integer                                 not null default 2,
    enabled                boolean   default true                  not null,
    script                 varchar                                 not null,
    use_expired_members    boolean   default true                  not null,
    use_expired_vo_members boolean   default false                 not null,
    use_banned_members     boolean   default true                  not null,
    created_at             timestamp default statement_timestamp() not null,
    created_by             varchar   default user                  not null,
    modified_at            timestamp default statement_timestamp() not null,
    modified_by            varchar   default user                  not null,
    created_by_uid         integer,
    modified_by_uid        integer,
    constraint serv_pk primary key (id),
    constraint serv_u unique (name)
);

-- SERVICE_REQUIRED_ATTRS - list of attributes required by the service
create table service_required_attrs
(
    service_id      integer                                 not null, --identifier of service (services.id)
    attr_id         integer                                 not null, --identifier of attribute (attr_names.id)
    created_at      timestamp default statement_timestamp() not null,
    created_by      varchar   default user                  not null,
    modified_at     timestamp default statement_timestamp() not null,
    modified_by     varchar   default user                  not null,
    created_by_uid  integer,
    modified_by_uid integer,
    constraint srvreqattr_pk primary key (service_id, attr_id),
    constraint srvreqattr_srv_fk foreign key (service_id) references services (id),
    constraint srvreqattr_attr_fk foreign key (attr_id) references attr_names (id)
);

-- SPECIFIC_USER_USERS - relation between specific-users and real users
create table specific_user_users
(
    user_id          integer                                 not null, --identifier of real user (users.id)
    specific_user_id integer                                 not null, --identifier of specific user (users.id)
    created_by_uid   integer,
    modified_by_uid  integer,
    modified_at      timestamp default statement_timestamp() not null,
    type             varchar   default 'service'             not null,
    status           integer   default 0                     not null, -- 0=enabled or 1=disabled ownership
    constraint acc_specifu_u_pk primary key (user_id, specific_user_id),
    constraint acc_specifu_u_uid_fk foreign key (user_id) references users (id),
    constraint acc_specifu_u_suid_fk foreign key (specific_user_id) references users (id),
    constraint specifu_u_status_chk check (status in (0, 1))
);

-- SERVICE_DENIALS - services excluded from ussage
create table service_denials
(
    id              integer                                 not null,
    service_id      integer                                 not null, --identifier of service (services.id)
    facility_id     integer,                                          --identifier of facility (facilities.id)
    destination_id  integer,                                          --identifier of destination (destinations.id) if service is not excluded on whole service
    created_at      timestamp default statement_timestamp() not null,
    created_by      varchar   default user                  not null,
    modified_at     timestamp default statement_timestamp() not null,
    modified_by     varchar   default user                  not null,
    created_by_uid  integer,
    modified_by_uid integer,
    constraint srvden_pk primary key (id),
    constraint srvden_srv_fk foreign key (service_id) references services (id),
    constraint srvden_fac_fk foreign key (facility_id) references facilities (id),
    constraint srvden_dest_fk foreign key (destination_id) references destinations (id),
    constraint srvden_u check (service_id is not null and ((facility_id is not null and destination_id is null) or
                                                           (facility_id is null and destination_id is not null)))
);

-- RESOURCE_SERVICES - services assigned to resource
create table resource_services
(
    service_id      integer                                 not null, --identifier of service (services.id)
    resource_id     integer                                 not null, --identifier of resource (resources.id)
    created_at      timestamp default statement_timestamp() not null,
    created_by      varchar   default user                  not null,
    modified_at     timestamp default statement_timestamp() not null,
    modified_by     varchar   default user                  not null,
    created_by_uid  integer,
    modified_by_uid integer,
    constraint resrcsrv_pk primary key (service_id, resource_id),
    constraint resrcsrv_srv_fk foreign key (service_id) references services (id),
    constraint resrcsrv_rsrc_fk foreign key (resource_id) references resources (id)
);

-- APPLICATION - registration data
create table application
(
    id                 integer                                 not null,
    vo_id              integer                                 not null, --identifier of VO (vos.id)
    user_id            integer,                                          --identifier of user (users.id)
    apptype            varchar                                 not null, --type of application (initial/extension/embedded)
    extSourceName      varchar,                                          --name of external source of users
    extSourceType      varchar,                                          --type of external source of users (federation...)
    fed_info           text,                                             --data from federation or cert
    state              varchar,                                          --state of application (new/verified/approved/rejected)
    extSourceLoa       integer,                                          --level of assurance of user by external source
    group_id           integer,                                          --identifier of group (groups.id) if application is for group
    auto_approve_error varchar,                                          --error that occurred during automatic approval
    created_at         timestamp default statement_timestamp() not null,
    created_by         varchar   default user                  not null,
    modified_at        timestamp default statement_timestamp() not null,
    modified_by        varchar   default user                  not null,
    created_by_uid     integer,
    modified_by_uid    integer,
    constraint app_pk primary key (id),
    constraint app_vo_fk foreign key (vo_id) references vos (id) on delete cascade,
    constraint app_group_fk foreign key (group_id) references groups (id) on delete cascade,
    constraint app_user_fk foreign key (user_id) references users (id) on delete cascade,
    constraint app_state_chk check (state in ('REJECTED', 'NEW', 'VERIFIED', 'APPROVED'))
);

-- APPLICATION_FORM - form for application into VO or group
create table application_form
(
    id                           integer               not null,
    vo_id                        integer               not null, --identifier of VO (vos.id)
    automatic_approval           boolean default false not null, --approval of application is automatic
    automatic_approval_extension boolean default false not null, --approval of extension is automatic
    automatic_approval_embedded  boolean default false not null, --approval of embedded application is automatic
    module_names                 varchar,                        --name of modules (separated by comma) which are called when processing application
    group_id                     integer,                        --identifier of group (groups.id) if application is for group
    created_by_uid               integer,
    modified_by_uid              integer,
    constraint applform_pk primary key (id),
    constraint applform_vo_fk foreign key (vo_id) references vos (id) on delete cascade,
    constraint applform_group_fk foreign key (group_id) references groups (id) on delete cascade
);

create type app_item_disabled as enum (
	'NEVER',
	'ALWAYS',
	'IF_PREFILLED',
	'IF_EMPTY'
	);

create type app_item_hidden as enum (
	'NEVER',
	'ALWAYS',
	'IF_PREFILLED',
	'IF_EMPTY'
	);

-- APPLICATION_FORM_ITEMS - items of application form
create table application_form_items
(
    id                          integer           not null,
    form_id                     integer           not null,                        --identifier of form (application_form.id)
    hidden                      app_item_hidden   not null default 'NEVER',
    disabled                    app_item_disabled not null default 'NEVER',
    updatable                   boolean           not null default true,
    hidden_dependency_item_id   integer,
    disabled_dependency_item_id integer,
    ordnum                      integer           not null,                        --order of item
    shortname                   varchar           not null,                        --name of item
    required                    boolean                    default false not null, --value for item is mandatory
    type                        varchar,                                           --type of item
    fed_attr                    varchar,                                           --copied from federation attribute
    src_attr                    varchar,                                           --sourced from attribute
    dst_attr                    varchar,                                           --saved to attribute
    regex                       varchar,                                           --regular expression for checking of value
    created_by_uid              integer,
    modified_by_uid             integer,
    constraint applfrmit_pk primary key (id),
    constraint applfrmit_applform foreign key (form_id) references application_form (id) on delete cascade,
    constraint applfrmit_hd foreign key (hidden_dependency_item_id) references application_form_items (id) ON DELETE SET NULL,
    constraint applfrmit_dd foreign key (disabled_dependency_item_id) references application_form_items (id) ON DELETE SET NULL
);

-- APPLICATION_FORM_ITEM_APPTYPES - possible types of app. form items
create table application_form_item_apptypes
(
    item_id         integer not null, --identifier of form item (application_form_items.id)
    apptype         varchar not null, --type of item
    created_by_uid  integer,
    modified_by_uid integer,
    constraint applfrmittyp_applfrmit_fk foreign key (item_id) references application_form_items (id) on delete cascade
);

-- APPLICATION_FORM_ITEM_TEXTS - texts displayed with the items at app. form
create table application_form_item_texts
(
    item_id         integer not null, --identifier of form item (application_form_items.id)
    locale          varchar not null, --language for application
    label           text,             --label of item on app. form
    options         text,             --options for items with menu
    help            varchar,          --text of help
    error_message   varchar,          --text of error message
    created_by_uid  integer,
    modified_by_uid integer,
    constraint applfrmittxt_pk primary key (item_id, locale),
    constraint applfrmittxt_applfrmit_fk foreign key (item_id) references application_form_items (id) on delete cascade
);

-- APPLICATION_DATA - values of data entered by application form
create table application_data
(
    id              integer not null,
    app_id          integer not null, --identifier of application (application.id)
    item_id         integer,          --identifier of item (application_form_items.id)
    shortname       varchar,          --name of item
    value           varchar,          --value of item
    assurance_level varchar,          --level of assurance of item of newly registered user
    created_by_uid  integer,
    modified_by_uid integer,
    constraint appdata_pk primary key (id),
    constraint appdata_app_fk foreign key (app_id) references application (id) on delete cascade,
    constraint appdata_applfrmit_fk foreign key (item_id) references application_form_items (id) on delete cascade
);

-- APPLICATION_MAILS - notification mails sent together with application
create table application_mails
(
    id              integer               not null,
    form_id         integer               not null, --identifier of form (application_form.id)
    app_type        varchar               not null, --application type (initial/extension/embedded)
    mail_type       varchar               not null, --type of mail (user/administrator)
    send            boolean default false not null, --sent (Y/N)
    created_by_uid  integer,
    modified_by_uid integer,
    constraint appmails_pk primary key (id),
    constraint appmails_u unique (form_id, app_type, mail_type),
    constraint appmail_appform_fk foreign key (form_id) references application_form (id) on delete cascade
);

-- APPLICATION_MAIL_TEXTS - texts of notification mails
create table application_mail_texts
(
    mail_id         integer               not null, --identifier of mail (application_mails.id)
    locale          varchar               not null, --language for texts
    htmlFormat      boolean default false not null, --define if text is in html format or as a plain text
    subject         varchar,                        --subject of mail
    text            varchar,                        --text of mail
    created_by_uid  integer,
    modified_by_uid integer,
    constraint appmailtxt_pk primary key (mail_id, locale, htmlFormat),
    constraint appmailtxt_appmails_fk foreign key (mail_id) references application_mails (id) on delete cascade
);

-- APPLICATION_RESERVED_LOGINS - lognames reserved for new users who has not been saved at users table yet
create table application_reserved_logins
(
    login           varchar                                 not null, --logname
    namespace       varchar                                 not null, --namespace where logname is reserved
    user_id         integer,                                          --identifier of user (user.id)
    extsourcename   varchar                                 not null,
    created_by      varchar   default user                  not null,
    created_at      timestamp default statement_timestamp() not null,
    created_by_uid  integer,
    modified_by_uid integer,
    constraint app_logins_pk primary key (login, namespace),
    constraint applogin_userid_fk foreign key (user_id) references users (id)
);

create type invitations_status as enum (
    'ACCEPTED',
    'EXPIRED',
    'PENDING',
    'REVOKED',
    'UNSENT'
);

-- INVITATIONS - invitations sent out via email to users
create table invitations (
    id integer not null,
    token uuid not null default gen_random_uuid(),
    vo_id integer,
    group_id integer,
    application_id integer,
    sender_id integer not null,
    receiver_name varchar not null,
    receiver_email varchar not null,
    redirect_url varchar,
    language varchar not null,
    expiration timestamp,
    status invitations_status not null,
    created_at timestamp default statement_timestamp() not null,
	created_by varchar default user not null,
	modified_at timestamp default statement_timestamp() not null,
	modified_by varchar default user not null,
	created_by_uid integer,
	modified_by_uid integer,
    constraint invitations_pk primary key(id),
    constraint invitations_u unique(token),
    constraint invitations_user_fk foreign key(sender_id) references users(id),
    constraint invitations_app_fk foreign key(application_id) references application(id) on delete set null,
    constraint invitations_vo_fk foreign key(vo_id) references vos(id) on delete cascade,
    constraint invitations_group_fk foreign key(group_id) references groups(id) on delete cascade
);

-- BLOCKED_LOGINS - logins blocked for reservation or setting
create table blocked_logins
(
    id              integer                                 not null,
    login           varchar                                 not null, --login
    namespace       varchar,                                          --namespace where login is blocked
    related_user_id integer,                                          --id of user who was related to this login in the past
    created_at      timestamp default statement_timestamp() not null,
    created_by_uid  integer,
    modified_by_uid integer,
    constraint blocked_logins_pk primary key (id),
    constraint blocked_logins_u unique (login, namespace)
);

-- BLOCKED_ATTR_VALUES - values of attributes which have either been depleted or blocked and or not to be used again
create table blocked_attr_values (
    attr_id integer not null,
    attr_value varchar not null,
    created_at timestamp default statement_timestamp() not null,
    created_by_uid integer,
    modified_by_uid integer,
    constraint fk_attr_names foreign key (attr_id) references attr_names(id) ON DELETE CASCADE,
    constraint blocked_attr_values_u unique (attr_id, attr_value)
);

-- FACILITY_SERVICE_DESTINATIONS - destinations of services assigned to the facility
create table facility_service_destinations
(
    service_id       integer                                 not null, --identifier of service (services.id)
    facility_id      integer                                 not null, --identifier of facility (facilities.id)
    destination_id   integer                                 not null, --identifier of destination (destinations.id)
    created_at       timestamp default statement_timestamp() not null,
    created_by       varchar   default user                  not null,
    modified_at      timestamp default statement_timestamp() not null,
    modified_by      varchar   default user                  not null,
    created_by_uid   integer,
    modified_by_uid  integer,
    propagation_type varchar   default 'PARALLEL',
    constraint fac_srv_dest_pk primary key (facility_id, service_id, destination_id),
    constraint dest_srv_fk foreign key (service_id) references services (id),
    constraint dest_fac_fk foreign key (facility_id) references facilities (id),
    constraint dest_dest_fk foreign key (destination_id) references destinations (id)
);

-- ENTITYLESS_ATTR_VALUES - value of attributes which are not assigned to any entity
create table entityless_attr_values
(
    subject         varchar                                 not null, --indicator of subject assigned with attribute
    attr_id         integer                                 not null, --identifier of attribute (attr_names.id)
    attr_value      text,                                             --attribute value
    created_at      timestamp default statement_timestamp() not null,
    created_by      varchar   default user                  not null,
    modified_at     timestamp default statement_timestamp() not null,
    modified_by     varchar   default user                  not null,
    created_by_uid  integer,
    modified_by_uid integer,
    constraint entlatval_pk primary key (subject, attr_id),
    constraint entlatval_attr_fk foreign key (attr_id) references attr_names (id)
);

-- FACILITY_ATTR_VALUES - attribute values assigned to facility
create table facility_attr_values
(
    facility_id     integer                                 not null, --identifier of facility (facilities.id)
    attr_id         integer                                 not null, --identifier of attribute (attr_names.id)
    attr_value      text,                                             --attribute value
    created_at      timestamp default statement_timestamp() not null,
    created_by      varchar   default user                  not null,
    modified_at     timestamp default statement_timestamp() not null,
    modified_by     varchar   default user                  not null,
    created_by_uid  integer,
    modified_by_uid integer,
    constraint facattval_pk primary key (facility_id, attr_id),
    constraint facattval_nam_fk foreign key (attr_id) references attr_names (id),
    constraint facattval_fac_fk foreign key (facility_id) references facilities (id)
);

-- FACILITY_ATTR_U_VALUES - unique attribute values
CREATE TABLE facility_attr_u_values
(
    facility_id INT NOT NULL,
    attr_id     INT NOT NULL,
    attr_value  text,
    UNIQUE (attr_id, attr_value),
    FOREIGN KEY (facility_id, attr_id) REFERENCES facility_attr_values ON DELETE CASCADE
);


-- GROUP_ATTR_VALUES - attribute values assigned to groups
create table group_attr_values
(
    group_id        integer                                 not null, --identifier of group (groups.id)
    attr_id         integer                                 not null, --identifier of attribute (attr_names.id)
    attr_value      text,                                             --attribute value
    created_at      timestamp default statement_timestamp() not null,
    created_by      varchar   default user                  not null,
    modified_at     timestamp default statement_timestamp() not null,
    modified_by     varchar   default user                  not null,
    created_by_uid  integer,
    modified_by_uid integer,
    constraint grpattval_pk primary key (group_id, attr_id),
    constraint grpattval_grp_fk foreign key (group_id) references groups (id),
    constraint grpattval_attr_fk foreign key (attr_id) references attr_names (id)
);

-- GROUP_ATTR_U_VALUES - unique attribute values
CREATE TABLE group_attr_u_values
(
    group_id   INT NOT NULL,
    attr_id    INT NOT NULL,
    attr_value text,
    UNIQUE (attr_id, attr_value),
    FOREIGN KEY (group_id, attr_id) REFERENCES group_attr_values ON DELETE CASCADE
);

-- RESOURCE_ATTR_VALUES - attribute values assigned to resources
create table resource_attr_values
(
    resource_id     integer                                 not null, --identifier of resource (resources.id)
    attr_id         integer                                 not null, --identifier of attribute (attr_names.id)
    attr_value      text,                                             --attribute value
    created_at      timestamp default statement_timestamp() not null,
    created_by      varchar   default user                  not null,
    modified_at     timestamp default statement_timestamp() not null,
    modified_by     varchar   default user                  not null,
    created_by_uid  integer,
    modified_by_uid integer,
    constraint resatval_pk primary key (resource_id, attr_id),
    constraint resatval_res_fk foreign key (resource_id) references resources (id),
    constraint resatval_resatnam_fk foreign key (attr_id) references attr_names (id)
);

-- RESOURCE_ATTR_U_VALUES - unique attribute values
CREATE TABLE resource_attr_u_values
(
    resource_id INT NOT NULL,
    attr_id     INT NOT NULL,
    attr_value  text,
    UNIQUE (attr_id, attr_value),
    FOREIGN KEY (resource_id, attr_id) REFERENCES resource_attr_values ON DELETE CASCADE
);

-- GROUP_RESOURCE_ATTR_VALUES - attribute values assigned to groups and resources
create table group_resource_attr_values
(
    group_id        integer                                 not null, --identifier of group (groups.id)
    resource_id     integer                                 not null, --identifier of resource (resources.id)
    attr_id         integer                                 not null, --identifier of attribute (attr_names.id)
    attr_value      text,                                             --attribute value
    created_at      timestamp default statement_timestamp() not null,
    created_by      varchar   default user                  not null,
    modified_at     timestamp default statement_timestamp() not null,
    modified_by     varchar   default user                  not null,
    created_by_uid  integer,
    modified_by_uid integer,
    constraint grpresav_pk primary key (group_id, resource_id, attr_id),
    constraint grpresav_grp_fk foreign key (group_id) references groups (id),
    constraint grpresav_res_fk foreign key (resource_id) references resources (id),
    constraint grpresav_attr_fk foreign key (attr_id) references attr_names (id)
);

-- GROUP_RESOURCE_ATTR_U_VALUES - unique attribute values
CREATE TABLE group_resource_attr_u_values
(
    group_id    INT NOT NULL,
    resource_id INT NOT NULL,
    attr_id     INT NOT NULL,
    attr_value  text,
    UNIQUE (attr_id, attr_value),
    FOREIGN KEY (group_id, resource_id, attr_id) REFERENCES group_resource_attr_values ON DELETE CASCADE
);

-- GROUPS_MEMBERS - members of groups
create table groups_members
(
    group_id            integer                                 not null, --identifier of group (groups.id)
    member_id           integer                                 not null, --identifier of member (members.id)
    created_at          timestamp default statement_timestamp() not null,
    created_by          varchar   default user                  not null,
    modified_at         timestamp default statement_timestamp() not null,
    modified_by         varchar   default user                  not null,
    source_group_status integer                                 not null default 0,
    created_by_uid      integer,
    modified_by_uid     integer,
    membership_type     integer                                 not null, --identifier of membership type (membersip_types.id)
    dual_membership     boolean   default false,                          -- whether user is both direct and indirect member
    source_group_id     integer                                 not null, --identifier of parent group (groups.id) if any
    constraint grpmem_pk primary key (member_id, group_id, source_group_id),
    constraint grpmem_gr_fk foreign key (group_id) references groups (id),
    constraint grpmem_mem_fk foreign key (member_id) references members (id),
    constraint grpmem_memtype_fk foreign key (membership_type) references membership_types (id)
);

create type group_resource_status as enum (
	'ACTIVE',
	'INACTIVE',
	'FAILED',
	'PROCESSING'
	);

-- GROUPS_RESOURCES - groups assigned to resource
create table groups_resources
(
    group_id              integer                                 not null, --identifier of group (groups.id)
    resource_id           integer                                 not null, --identifier of resource (resources.id)
    created_at            timestamp default statement_timestamp() not null,
    created_by            varchar   default user                  not null,
    modified_at           timestamp default statement_timestamp() not null,
    modified_by           varchar   default user                  not null,
    created_by_uid        integer,
    modified_by_uid       integer,
    auto_assign_subgroups boolean   default false                 not null,
    constraint grres_grp_res_u unique (group_id, resource_id),
    constraint grres_gr_fk foreign key (group_id) references groups (id),
    constraint grres_res_fk foreign key (resource_id) references resources (id)
);

-- GROUPS_RESOURCES_AUTOMATIC - groups automatically assigned to resource through source group
create table groups_resources_automatic
(
    group_id        integer                                 not null, --identifier of group (groups.id)
    resource_id     integer                                 not null, --identifier of resource (resources.id)
    source_group_id integer                                 not null, --identifier of source group (groups.id)
    created_at      timestamp default statement_timestamp() not null,
    created_by      varchar   default user                  not null,
    modified_at     timestamp default statement_timestamp() not null,
    modified_by     varchar   default user                  not null,
    created_by_uid  integer,
    modified_by_uid integer,
    constraint grresaut_grp_res_sgrp_u unique (group_id, resource_id, source_group_id),
    constraint grresaut_gr_fk foreign key (group_id) references groups (id),
    constraint grresaut_res_fk foreign key (resource_id) references resources (id),
    constraint grresaut_sgr_fk foreign key (source_group_id) references groups (id)
);

create function relation_group_resource_exist(integer, integer) returns integer
as 'select count(1)::integer from (SELECT group_id, resource_id FROM groups_resources UNION SELECT group_id, resource_id FROM groups_resources_automatic) gr_res where group_id=$1 and resource_id=$2;'
	language sql;

create table groups_resources_state
(
    group_id      integer               not null,
    resource_id   integer               not null,
    status        group_resource_status not null default 'PROCESSING',
    failure_cause varchar                        default null,
    constraint grres_s_grp_res_u unique (group_id, resource_id),
    constraint grres_s_gr_fk foreign key (group_id) references groups (id),
    constraint grres_s_res_fk foreign key (resource_id) references resources (id),
    check ( relation_group_resource_exist(group_id, resource_id) != 0
)
    );

create function delete_group_resource_status() returns trigger as '
	begin
		if relation_group_resource_exist(OLD.group_id, OLD.resource_id) = 0 then
			delete from groups_resources_state where group_id=OLD.group_id and resource_id=OLD.resource_id;
		end if;
	return OLD;
	end;
	' language plpgsql;

create trigger after_deleting_from_groups_resources
    after delete
    on groups_resources
    for each row execute procedure delete_group_resource_status();

create trigger after_deleting_from_groups_resources_automatic
    after delete
    on groups_resources_automatic
    for each row execute procedure delete_group_resource_status();

-- MEMBER_ATTR_VALUES - values of attributes assigned to members
create table member_attr_values
(
    member_id       integer                                 not null, --identifier of member (members.id)
    attr_id         integer                                 not null, --identifier of attribute (attr_names.id)
    attr_value      text,                                             --attribute value
    created_at      timestamp default statement_timestamp() not null,
    created_by      varchar   default user                  not null,
    modified_at     timestamp default statement_timestamp() not null,
    modified_by     varchar   default user                  not null,
    created_by_uid  integer,
    modified_by_uid integer,
    constraint memattval_pk primary key (member_id, attr_id),
    constraint memattval_mem_fk foreign key (member_id) references members (id),
    constraint memattval_attr_fk foreign key (attr_id) references attr_names (id)
);

-- MEMBER_ATTR_U_VALUES -- unique attribute values
CREATE TABLE member_attr_u_values
(
    member_id  INT NOT NULL,
    attr_id    INT NOT NULL,
    attr_value text,
    UNIQUE (attr_id, attr_value),
    FOREIGN KEY (member_id, attr_id) REFERENCES member_attr_values ON DELETE CASCADE
);

-- MEMBER_GROUP_ATTR_VALUES - values of attributes assigned to members in groups
create table member_group_attr_values
(
    member_id       integer                                 not null, --identifier of member (members.id)
    group_id        integer                                 not null, --identifier of group (groups.id)
    attr_id         integer                                 not null, --identifier of attribute (attr_names.id)
    attr_value      text,                                             --attribute value
    created_at      timestamp default statement_timestamp() not null,
    created_by      varchar   default user                  not null,
    modified_at     timestamp default statement_timestamp() not null,
    modified_by     varchar   default user                  not null,
    created_by_uid  integer,
    modified_by_uid integer,
    constraint memgav_pk primary key (member_id, group_id, attr_id),
    constraint memgav_mem_fk foreign key (member_id) references members (id),
    constraint memgav_grp_fk foreign key (group_id) references groups (id),
    constraint memgav_accattnam_fk foreign key (attr_id) references attr_names (id)
);

-- MEMBER_GROUP_ATTR_U_VALUES - unique attribute values
CREATE TABLE member_group_attr_u_values
(
    member_id  INT NOT NULL,
    group_id   INT NOT NULL,
    attr_id    INT NOT NULL,
    attr_value text,
    UNIQUE (attr_id, attr_value),
    FOREIGN KEY (member_id, group_id, attr_id) REFERENCES member_group_attr_values ON DELETE CASCADE
);

-- MEMBER_RESOURCE_ATTR_VALUES - values of attributes assigned to members on resources
create table member_resource_attr_values
(
    member_id       integer                                 not null, --identifier of member (members.id)
    resource_id     integer                                 not null, --identifier of resource (resources.id)
    attr_id         integer                                 not null, --identifier of attribute (attr_names.id)
    attr_value      text,                                             --attribute value
    created_at      timestamp default statement_timestamp() not null,
    created_by      varchar   default user                  not null,
    modified_at     timestamp default statement_timestamp() not null,
    modified_by     varchar   default user                  not null,
    created_by_uid  integer,
    modified_by_uid integer,
    constraint memrav_pk primary key (member_id, resource_id, attr_id),
    constraint memrav_mem_fk foreign key (member_id) references members (id),
    constraint memrav_rsrc_fk foreign key (resource_id) references resources (id),
    constraint memrav_accattnam_fk foreign key (attr_id) references attr_names (id)
);

-- MEMBER_RESOURCE_ATTR_U_VALUES - unique attribute values
CREATE TABLE member_resource_attr_u_values
(
    member_id   INT NOT NULL,
    resource_id INT NOT NULL,
    attr_id     INT NOT NULL,
    attr_value  text,
    UNIQUE (attr_id, attr_value),
    FOREIGN KEY (member_id, resource_id, attr_id) REFERENCES member_resource_attr_values ON DELETE CASCADE
);

-- USER_ATTR_VALUES - values of attributes assigned to users
create table user_attr_values
(
    user_id         integer                                 not null, --identifier of user (users.id)
    attr_id         integer                                 not null, --identifier of attribute (attr_names.id)
    attr_value      text,                                             --attribute value
    created_at      timestamp default statement_timestamp() not null,
    created_by      varchar   default user                  not null,
    modified_at     timestamp default statement_timestamp() not null,
    modified_by     varchar   default user                  not null,
    created_by_uid  integer,
    modified_by_uid integer,
    constraint usrav_pk primary key (user_id, attr_id),
    constraint usrav_usr_fk foreign key (user_id) references users (id),
    constraint usrav_accattnam_fk foreign key (attr_id) references attr_names (id)
);

-- USER_ATTR_U_VALUE - unique attribute values
CREATE TABLE user_attr_u_values
(
    user_id    INT NOT NULL,
    attr_id    INT NOT NULL,
    attr_value text,
    UNIQUE (attr_id, attr_value),
    FOREIGN KEY (user_id, attr_id) REFERENCES user_attr_values ON DELETE CASCADE
);

-- USER_FACILITY_ATTR_VALUES - values of attributes assigned to users on facilities
create table user_facility_attr_values
(
    user_id         integer                                 not null, --identifier of user (users.id)
    facility_id     integer                                 not null, --identifier of facility (facilities.id)
    attr_id         integer                                 not null, --identifier of attribute (attr_names.id)
    attr_value      text,                                             --attribute value
    created_at      timestamp default statement_timestamp() not null,
    created_by      varchar   default user                  not null,
    modified_at     timestamp default statement_timestamp() not null,
    modified_by     varchar   default user                  not null,
    created_by_uid  integer,
    modified_by_uid integer,
    constraint usrfacav_u primary key (user_id, facility_id, attr_id),
    constraint usrfacav_mem_fk foreign key (user_id) references users (id),
    constraint usrfacav_fac_fk foreign key (facility_id) references facilities (id),
    constraint usrfacav_accattnam_fk foreign key (attr_id) references attr_names (id)
);

-- USER_FACILITY_ATTR_U_VALUES - unique attribute values
CREATE TABLE user_facility_attr_u_values
(
    user_id     INT NOT NULL,
    facility_id INT NOT NULL,
    attr_id     INT NOT NULL,
    attr_value  text,
    UNIQUE (attr_id, attr_value),
    FOREIGN KEY (user_id, facility_id, attr_id) REFERENCES user_facility_attr_values ON DELETE CASCADE
);

-- VO_ATTR_VALUES - attributes specific for VO
create table vo_attr_values
(
    vo_id           integer                                 not null, --identifier of VO (vos.id)
    attr_id         integer                                 not null, --identifier of attribute (attr_names.id)
    attr_value      text,                                             --attribute value
    created_at      timestamp default statement_timestamp() not null,
    created_by      varchar   default user                  not null,
    modified_at     timestamp default statement_timestamp() not null,
    modified_by     varchar   default user                  not null,
    created_by_uid  integer,
    modified_by_uid integer,
    constraint voattval_pk primary key (vo_id, attr_id),
    constraint voattval_nam_fk foreign key (attr_id) references attr_names (id),
    constraint voattval_vo_fk foreign key (vo_id) references vos (id)
);

-- VO_ATTR_U_VALUES - unique attribute values
CREATE TABLE vo_attr_u_values
(
    vo_id      INT NOT NULL,
    attr_id    INT NOT NULL,
    attr_value text,
    UNIQUE (attr_id, attr_value),
    FOREIGN KEY (vo_id, attr_id) REFERENCES vo_attr_values ON DELETE CASCADE
);

-- EXT_SOURCES - external sources from which we can gain data about users
create table ext_sources
(
    id              integer                                 not null,
    name            varchar                                 not null, --name of source
    type            varchar,                                          --type of source (LDAP/IdP...)
    created_at      timestamp default statement_timestamp() not null,
    created_by      varchar   default user                  not null,
    modified_at     timestamp default statement_timestamp() not null,
    modified_by     varchar   default user                  not null,
    created_by_uid  integer,
    modified_by_uid integer,
    constraint usrsrc_pk primary key (id),
    constraint usrsrc_u unique (name)
);

-- EXT_SOURCES_ATTRIBUTES - values of attributes of external sources
create table ext_sources_attributes
(
    ext_sources_id  integer                                 not null, --identifier of ext. source (ext_sources.id)
    attr_name       varchar                                 not null, --name of attribute at ext. source
    attr_value      text,                                             --value of attribute
    created_at      timestamp default statement_timestamp() not null,
    created_by      varchar   default user                  not null,
    modified_at     timestamp default statement_timestamp() not null,
    modified_by     varchar   default user                  not null,
    created_by_uid  integer,
    modified_by_uid integer,
    constraint usrcatt_usrc_fk foreign key (ext_sources_id) references ext_sources (id)
);

-- VO_EXT_SOURCES - external sources assigned to VO
create table vo_ext_sources
(
    vo_id           integer                                 not null, --identifier of VO (vos.id)
    ext_sources_id  integer                                 not null, --identifier of ext. source (ext_sources.id)
    created_at      timestamp default statement_timestamp() not null,
    created_by      varchar   default user                  not null,
    modified_at     timestamp default statement_timestamp() not null,
    modified_by     varchar   default user                  not null,
    created_by_uid  integer,
    modified_by_uid integer,
    constraint vousrsrc_pk primary key (vo_id, ext_sources_id),
    constraint vousrsrc_usrsrc_fk foreign key (ext_sources_id) references ext_sources (id),
    constraint vousrsrc_vos_fk foreign key (vo_id) references vos (id)
);

-- GROUP_EXT_SOURCES - external source assigned to GROUP
create table group_ext_sources
(
    group_id        integer                                 not null,
    ext_source_id   integer                                 not null,
    created_at      timestamp default statement_timestamp() not null,
    created_by      varchar   default user                  not null,
    modified_at     timestamp default statement_timestamp() not null,
    modified_by     varchar   default user                  not null,
    created_by_uid  integer,
    modified_by_uid integer,
    constraint groupsrc_pk primary key (group_id, ext_source_id),
    constraint groupsrc_src_fk foreign key (ext_source_id) references ext_sources (id),
    constraint groupsrc_groups_fk foreign key (group_id) references groups (id)
);

-- USER_EXT_SOURCES - external source from which user come (identification of user in his home system)
create table user_ext_sources
(
    id              integer                                 not null,
    user_id         integer                                 not null, --identifier of user (users.id)
    login_ext       varchar                                 not null, --logname from his home system
    ext_sources_id  integer                                 not null, --identifier of ext. source (ext_sources.id)
    created_at      timestamp default statement_timestamp() not null,
    created_by      varchar   default user                  not null,
    modified_at     timestamp default statement_timestamp() not null,
    modified_by     varchar   default user                  not null,
    loa             integer,                                          --level of assurance
    last_access     timestamp default statement_timestamp() not null, --time of last user's access (to Perun) by using this external source
    created_by_uid  integer,
    modified_by_uid integer,
    constraint usrex_p primary key (id),
    constraint usrex_u unique (ext_sources_id, login_ext),
    constraint usrex_usr_fk foreign key (user_id) references users (id),
    constraint usrex_usersrc_fk foreign key (ext_sources_id) references ext_sources (id)
);

-- SERVICE_PACKAGES - possible groups of services
create table service_packages
(
    id              integer                                 not null,
    name            varchar                                 not null, --name of service package
    description     varchar,                                          --purpose,description
    created_at      timestamp default statement_timestamp() not null,
    created_by      varchar   default user                  not null,
    modified_at     timestamp default statement_timestamp() not null,
    modified_by     varchar   default user                  not null,
    created_by_uid  integer,
    modified_by_uid integer,
    constraint pkg_pk primary key (id),
    constraint pkg_name unique (name)
);

-- SERVICE_SERVICE_PACKAGES - groups of services which should to be executed together however at specific order
create table service_service_packages
(
    service_id      integer                                 not null, --identifier of service (services.id)
    package_id      integer                                 not null, --identifier of package (service_packages.id)
    created_at      timestamp default statement_timestamp() not null,
    created_by      varchar   default user                  not null,
    modified_at     timestamp default statement_timestamp() not null,
    modified_by     varchar   default user                  not null,
    created_by_uid  integer,
    modified_by_uid integer,
    constraint srvpkg_srv_pk primary key (service_id, package_id),
    constraint srvpkg_srv_fk foreign key (service_id) references services (id),
    constraint srvpkg_pkg_fk foreign key (package_id) references service_packages (id)
);

-- TASKS - contains planned services and services finished at near past
create table tasks
(
    id              integer                                 not null,
    service_id      integer                                 not null, --identifier of executed service (services.id)
    facility_id     integer                                 not null, --identifier of target facility (facilities.id)
    schedule        timestamp                               not null, --planned time for starting task
    recurrence      integer                                 not null, --number of repeating of task in case of error
    delay           integer                                 not null, --delay after next executing in case of error
    status          varchar                                 not null, --state of task
    start_time      timestamp,                                        --real start time of task
    end_time        timestamp,                                        --real end time of task
    engine_id       integer   default 1,                              --identifier of engine which is executing the task
    created_at      timestamp default statement_timestamp() not null,
    err_message     varchar,                                          --return message in case of error
    created_by_uid  integer,
    modified_by_uid integer,
    constraint task_pk primary key (id),
    constraint task_u unique (service_id, facility_id),
    constraint task_srv_fk foreign key (service_id) references services (id),
    constraint task_fac_fk foreign key (facility_id) references facilities (id),
    constraint task_stat_chk check (status in
                                    ('WAITING', 'PLANNED', 'GENERATING', 'GENERROR', 'GENERATED', 'SENDING', 'DONE',
                                     'SENDERROR', 'ERROR', 'WARNING'))
);

-- TASKS_RESULTS - contains partial results of tasks (executing, waiting and at near past finished)
create table tasks_results
(
    id              integer                                 not null,
    task_id         integer                                 not null, --identifier of task (tasks.id)
    task_run_id     integer  default 0                      not null, --identifier of specific task propagation for logging
    destination_id  integer                                 not null, --identifier of destination (destinations.id)
    status          varchar                                 not null, --status of task
    err_message     varchar,                                          --return message in case of error
    std_message     varchar,                                          --return message in case of success
    return_code     integer,                                          --returned value
    timestamp       timestamp,                                        --real time of executing
    engine_id       integer   default 1                     not null, --identifier of executing engine
    created_at      timestamp default statement_timestamp() not null,
    created_by      varchar   default user                  not null,
    modified_at     timestamp default statement_timestamp() not null,
    modified_by     varchar   default user                  not null,
    created_by_uid  integer,
    modified_by_uid integer,
    constraint taskres_pk primary key (id),
    constraint taskres_task_fk foreign key (task_id) references tasks (id),
    constraint taskres_dest_fk foreign key (destination_id) references destinations (id),
    constraint taskres_stat_chk check (status in ('DONE', 'ERROR', 'DENIED', 'WARNING'))
);

create table auditer_log
(
    id              integer                                 not null, --identifier of logged event
    msg             text                                    not null, --text of logging message
    actor           varchar                                 not null, --who causes the event
    created_at      timestamp default statement_timestamp() not null,
    created_by_uid  integer,
    modified_by_uid integer,
    constraint audlog_pk primary key (id)
);

-- PN_AUDIT_MESSAGE - Contains all messages retrieved from the auditer log, since the notification module is auditer consumer. These messages are waiting to be processed by the notification module
create table pn_audit_message
(
    message         text,
    id              integer NOT NULL,
    created_by_uid  integer,
    modified_by_uid integer,
    constraint pn_audmsg_pk primary key (id)
);

-- PN_OBJECT - Keeps names of the Perun beans and their properties, for the recognition in a regular expression in the notification module
create table pn_object
(
    id              integer NOT NULL,
    name            varchar, --arbitrary name
    properties      varchar, --set of names of methods divided by ';'
    class_name      varchar, --the whole java class name of the object, e.g. 'cz.metacentrum.perun.core.api.Vo'
    created_by_uid  integer,
    modified_by_uid integer,
    constraint pn_object_pk primary key (id)
);

-- PN_TEMPLATE - Contains templates for creating the message in the notification module
create table pn_template
(
    id                    integer NOT NULL,
    primary_properties    varchar NOT NULL, --important attributes, which messages will be grouped on, same as later PN_POOL_MESSAGE.KEY_ATTRIBUTES
    notify_trigger        varchar,          --configures two approaches to the grouping messages,
    --when equals 'ALL_REGEX_IDS', the pool messages are grouped and sent when all needed audit messages are collected
    --when equals 'STREAM', the related pool messages are waiting for the certain amount of time and then sent
    youngest_message_time integer,          --time limit for the youngest message
    oldest_message_time   integer,          --time limit for the oldest message
    name                  varchar,          --arbitrary name
    sender                varchar,          --email address that will be stated as sender
    reply_to              varchar,          --email address that will be used in the reply_to field
    created_by_uid        integer,
    modified_by_uid       integer,
    constraint pn_tmpl_pk primary key (id)
);

-- PN_POOL_MESSAGE - Contains all messages, which are already processed by the notification module. The mesages will be grouped in this table before sending
create table pn_pool_message
(
    id              integer                                 NOT NULL,
    regex_id        integer                                 NOT NULL, --references a regular expression binded to a message
    template_id     integer                                 NOT NULL, --references a template binded to a message
    key_attributes  varchar                                 NOT NULL, --contains all attributes extracted from audit message that are important for grouping messages
    created         timestamp default statement_timestamp() NOT NULL, --the time of the pool message creation, important when PN_TEMPLATE.NOTIFY_TRIGGER is set to 'stream'
    notif_message   text                                    NOT NULL, --contains original audit message, important when gaining attributes in template message
    created_by_uid  integer,
    modified_by_uid integer,
    constraint pn_poolmsg_pk primary key (id),
    constraint pn_poolmsg_tmpl_fk foreign key (template_id) references pn_template (id)
);

-- PN_RECEIVER - Keeps information about receiver of messages from notification module
create table pn_receiver
(
    id               integer NOT NULL,
    target           varchar NOT NULL, --the email address or jabber of the receiver
    type_of_receiver varchar NOT NULL, --available options are email_user/email_group/jabber
    template_id      integer NOT NULL, --reference to the pn_template
    created_by_uid   integer,
    modified_by_uid  integer,
    locale           varchar,          ----the message language and formating is based on locale
    constraint pn_receiver_pk primary key (id),
    constraint pn_receiver_tmpl_fk foreign key (template_id) references pn_template (id)
);

-- PN_REGEX - Keeps regular expressions, which are used to parse audit messages in the notification module
create table pn_regex
(
    id              integer NOT NULL,
    note            varchar,          --comment to the regex
    regex           varchar NOT NULL, --the regular expression
    created_by_uid  integer,
    modified_by_uid integer,
    constraint pn_regex_pk primary key (id)
);

-- PN_TEMPLATE_MESSAGE - Contains text messages that the notification module sends. The freemaker syntax can be used in the message, so the content is dynamic and is able to contain attributes from the audit message
create table pn_template_message
(
    id              integer NOT NULL,
    template_id     integer NOT NULL, --reference to the pn_template
    locale          varchar NOT NULL, --the message language and formating is based on locale
    message         varchar,          --text message
    created_by_uid  integer,
    modified_by_uid integer,
    subject         varchar not null, --text, which is used as subject of the message
    constraint pn_tmplmsg_pk primary key (id),
    constraint pn_tmplmsg_tmpl_fk foreign key (template_id) references pn_template (id) deferrable
);

-- PN_TEMPLATE_REGEX - Represents relation between pn_template and pn_regex in the notification module
create table pn_template_regex
(
    regex_id        integer NOT NULL, --referecnce to the regular expression
    template_id     integer NOT NULL, --reference to the pn_template
    id              integer NOT NULL,
    created_by_uid  integer,
    modified_by_uid integer,
    constraint pn_tmplrgx_pk primary key (id),
    constraint pn_tmplrgx_rgx_fk foreign key (regex_id) references pn_regex (id),
    constraint pn_tmplrgx_tmpl_fk foreign key (template_id) references pn_template (id)
);

-- PN_REGEX_OBJECT - Represents relation between pn_regex and pn_object in the notification module
create table pn_regex_object
(
    id              integer NOT NULL,
    regex_id        integer NOT NULL, --referecnce to the pn_regex
    object_id       integer NOT NULL, --reference to the pn_object
    created_by_uid  integer,
    modified_by_uid integer,
    constraint pn_rgxobj_pk primary key (id),
    constraint pn_rgxobj_rgx_fk foreign key (regex_id) references pn_regex (id),
    constraint pn_rgxobj_obj_fk foreign key (object_id) references pn_object (id)
);

-- GROUPS_GROUPS - Groups relations (union,subgroups)
create table groups_groups
(
    result_gid  integer                                 not null, --identifier of group
    operand_gid integer                                 not null, --identifier of operand group (unioned / parent group)
    parent_flag boolean   default false,
    created_at  timestamp default statement_timestamp() not null,
    created_by  varchar   default user                  not null,
    modified_at timestamp default statement_timestamp() not null,
    modified_by varchar   default user                  not null,
    constraint grp_grp_pk primary key (result_gid, operand_gid),
    constraint grp_grp_rgid_fk foreign key (result_gid) references groups (id),
    constraint grp_grp_ogid_fk foreign key (operand_gid) references groups (id)
);

-- VOS_VOS - Hierarchical structure of virtual organizations and their member organizations
create table vos_vos
(
    vo_id        integer                                 not null, -- identifier of VO
    member_vo_id integer                                 not null, -- identifier of its member vo
    created_at   timestamp default statement_timestamp() not null,
    created_by   varchar   default user                  not null,
    modified_at  timestamp default statement_timestamp() not null,
    modified_by  varchar   default user                  not null,
    constraint vos_vos_pk primary key (vo_id, member_vo_id),
    constraint vos_vos_void_fk foreign key (vo_id) references vos (id),
    constraint vos_vos_memid_fk foreign key (member_vo_id) references vos (id)
);

-- ALLOWED_GROUPS_TO_HIERARCHICAL_VO - Groups allowed to be included to parent vo's groups
create table allowed_groups_to_hierarchical_vo
(
    group_id       integer                                 not null, --identifier of group
    vo_id          integer                                 not null, --identifier of parent vo
    created_at     timestamp default statement_timestamp() not null,
    created_by     varchar   default user                  not null,
    created_by_uid integer,
    constraint alwd_grps_pk primary key (group_id, vo_id),
    constraint alwd_grps_gid_fk foreign key (group_id) references groups (id) on delete cascade,
    constraint alwd_grps_void_fk foreign key (vo_id) references vos (id)
);

-- RES_TAGS - possible resource tags in VO
create table res_tags
(
    id              integer                                 not null,
    vo_id           integer                                 not null, --identifier of VO
    tag_name        varchar                                 not null, --name of tag (computationl node/storage...)
    created_at      timestamp default statement_timestamp() not null,
    created_by      varchar   default user                  not null,
    modified_at     timestamp default statement_timestamp() not null,
    modified_by     varchar   default user                  not null,
    created_by_uid  integer,
    modified_by_uid integer,
    constraint restags_pk primary key (id),
    constraint restags_u unique (vo_id, tag_name),
    constraint restags_vos_fk foreign key (vo_id) references vos (id)
);

-- TAGS_RESOURCES - relation between tags and resources
create table tags_resources
(
    tag_id      integer not null, --identifier of tag (res_tags.id)
    resource_id integer not null, --identifier of resource
    constraint tags_res_pk primary key (tag_id, resource_id),
    constraint tags_res_tags_fk foreign key (tag_id) references res_tags (id),
    constraint tags_res_res_fk foreign key (resource_id) references resources (id)
);

-- CONFIGURATIONS - system Perun configuration
create table configurations
(
    property varchar not null, --property (for example database version)
    value    varchar not null, --value of configuration property
    constraint config_pk primary key (property)
);

-- MAILCHANGE - allow to user to change mail address, temporairly saved mails during change is in progress
create table mailchange
(
    id             integer                                 not null,
    uu_id          uuid                                    not null default gen_random_uuid(),
    value          text                                    not null, --
    user_id        integer                                 not null, --identifier of user (users.id)
    created_at     timestamp default statement_timestamp() not null,
    created_by     varchar   default user                  not null,
    created_by_uid integer,
    constraint mailchange_pk primary key (id),
    constraint mailchange_u_fk foreign key (user_id) references users (id)
);

--PWDRESET - allows to user to change passwd
create table pwdreset
(
    id             integer                                 not null,
    uu_id          uuid                                    not null default gen_random_uuid(),
    namespace      text                                    not null,
    mail           text,
    user_id        integer                                 not null,
    validity_to    timestamp                               not null,
    created_at     timestamp default statement_timestamp() not null,
    created_by     varchar   default user                  not null,
    created_by_uid integer,
    constraint pwdreset_pk primary key (id),
    constraint pwdreset_u_fk foreign key (user_id) references users (id)
);

create table vos_bans
(
    id              integer                                 not null,
    member_id       integer                                 not null,
    vo_id           integer                                 not null,
    description     varchar,
    banned_to       timestamp default '2999-01-01 00:00:00' not null,
    created_at      timestamp default statement_timestamp() not null,
    created_by      varchar   default user                  not null,
    modified_at     timestamp default statement_timestamp() not null,
    modified_by     varchar   default user                  not null,
    created_by_uid  integer,
    modified_by_uid integer,
    constraint vos_bans_pk primary key (id),
    constraint vos_bans_u unique (member_id),
    constraint vos_bans_mem_fk foreign key (member_id) references members (id),
    constraint vos_bans_vo_fk foreign key (vo_id) references vos (id)
);

create table resources_bans
(
    id              integer                                 not null,
    member_id       integer                                 not null,
    resource_id     integer                                 not null,
    description     varchar,
    banned_to       timestamp                               not null default '2999-01-01 00:00:00' not null,
    created_at      timestamp default statement_timestamp() not null,
    created_by      varchar   default user                  not null,
    modified_at     timestamp default statement_timestamp() not null,
    modified_by     varchar   default user                  not null,
    created_by_uid  integer,
    modified_by_uid integer,
    constraint res_bans_pk primary key (id),
    constraint res_bans_u unique (member_id, resource_id),
    constraint res_bans_mem_fk foreign key (member_id) references members (id),
    constraint res_bans_res_fk foreign key (resource_id) references resources (id)
);

create table facilities_bans
(
    id              integer                                 not null,
    user_id         integer                                 not null,
    facility_id     integer                                 not null,
    description     varchar,
    banned_to       timestamp                               not null default '2999-01-01 00:00:00' not null,
    created_at      timestamp default statement_timestamp() not null,
    created_by      varchar   default user                  not null,
    modified_at     timestamp default statement_timestamp() not null,
    modified_by     varchar   default user                  not null,
    created_by_uid  integer,
    modified_by_uid integer,
    constraint fac_bans_pk primary key (id),
    constraint fac_bans_u unique (user_id, facility_id),
    constraint fac_bans_usr_fk foreign key (user_id) references users (id),
    constraint fac_bans_fac_fk foreign key (facility_id) references facilities (id)
);

create table user_ext_source_attr_values
(
    user_ext_source_id integer                                 not null,
    attr_id            integer                                 not null,
    attr_value         text,
    created_at         timestamp default statement_timestamp() not null,
    created_by         varchar   default user                  not null,
    modified_at        timestamp default statement_timestamp() not null,
    modified_by        varchar   default user                  not null,
    created_by_uid     integer,
    modified_by_uid    integer,
    constraint uesattrval_pk primary key (user_ext_source_id, attr_id),
    constraint uesattrval_ues_fk foreign key (user_ext_source_id) references user_ext_sources (id),
    constraint uesattrval_attr_fk foreign key (attr_id) references attr_names (id)
);

-- USER_EXT_SOURCE_ATTR_U_VALUES - unique attribute values
CREATE TABLE user_ext_source_attr_u_values
(
    user_ext_source_id INT NOT NULL,
    attr_id            INT NOT NULL,
    attr_value         text,
    UNIQUE (attr_id, attr_value),
    FOREIGN KEY (user_ext_source_id, attr_id) REFERENCES user_ext_source_attr_values ON DELETE CASCADE
);

CREATE TABLE members_sponsored
(
    active          boolean   default true                  not null,
    sponsored_id    INTEGER                                 NOT NULL,
    sponsor_id      INTEGER                                 NOT NULL,
    validity_to     timestamp default null,
    created_at      timestamp default statement_timestamp() not null,
    created_by      varchar   default user                  not null,
    created_by_uid  integer,
    modified_at     timestamp default statement_timestamp() not null,
    modified_by     varchar   default user                  not null,
    modified_by_uid integer,
    constraint memspons_mem_fk foreign key (sponsored_id) references members (id),
    constraint memspons_usr_fk foreign key (sponsor_id) references users (id),
    constraint memspons_mem_usr_u unique (sponsored_id, sponsor_id)
);

-- AUTHZ - assigned roles to users/groups/VOs/other entities...
create table authz
(
    user_id             integer,                                          --identifier of user
    role_id             integer                                 not null, --identifier of role
    vo_id               integer,                                          --identifier of VO
    facility_id         integer,                                          --identifier of facility
    member_id           integer,                                          --identifier of member
    group_id            integer,                                          --identifier of group
    service_id          integer,                                          --identifier of service
    resource_id         integer,                                          --identifier of resource
    sponsored_user_id   integer,                                          --identifier of sponsored user
    created_by_uid      integer,
    modified_by_uid     integer,
    authorized_group_id integer,                                          --identifier of whole authorized group
    created_at          timestamp default statement_timestamp() not null,
    created_by          varchar   default user                  not null,
    constraint authz_role_fk foreign key (role_id) references roles (id),
    constraint authz_user_fk foreign key (user_id) references users (id),
    constraint authz_authz_group_fk foreign key (authorized_group_id) references groups (id),
    constraint authz_vo_fk foreign key (vo_id) references vos (id),
    constraint authz_fac_fk foreign key (facility_id) references facilities (id),
    constraint authz_mem_fk foreign key (member_id) references members (id),
    constraint authz_group_fk foreign key (group_id) references groups (id),
    constraint authz_service_fk foreign key (service_id) references services (id),
    constraint authz_res_fk foreign key (resource_id) references resources (id),
    constraint authz_sponsu_fk foreign key (sponsored_user_id) references users (id),
    constraint authz_user_autgrp_chk check
        ((user_id is not null and authorized_group_id is null) or (user_id is null and authorized_group_id is not null))
);

create table groups_to_register
(
    group_id integer,
    constraint grpreg_group_fk foreign key (group_id) references groups (id) on delete cascade
);

create table auto_registration_groups
(
    group_id                 integer not null,
    application_form_item_id integer not null,
    constraint auto_reg_grps_group_fk foreign key (group_id) references groups (id) on delete cascade,
    constraint auto_reg_grps_app_forms_fk foreign key (application_form_item_id) references application_form_items (id) on delete cascade,
    constraint auto_reg_grps_grp_app_form_u unique (group_id, application_form_item_id)
);

create type consent_status as enum (
	'UNSIGNED',
	'GRANTED',
	'REVOKED'
);

-- CONSENTS - consents of users
create table consents
(
    id              integer                                 not null,
    user_id         integer                                 not null,
    consent_hub_id  integer                                 not null,
    status          consent_status                          not null default 'UNSIGNED',
    created_at      timestamp default statement_timestamp() not null,
    created_by      varchar   default user                  not null,
    modified_at     timestamp default statement_timestamp() not null,
    modified_by     varchar   default user                  not null,
    created_by_uid  integer,
    modified_by_uid integer,
    constraint consents_pk primary key (id),
    constraint consents_user_fk foreign key (user_id) references users (id),
    constraint consents_cons_hub_fk foreign key (consent_hub_id) references consent_hubs (id),
    constraint consents_user_hub_status_u unique (user_id, consent_hub_id, status)
);

-- CONSENT_ATTR_DEFs - attributes associated with consents
create table consent_attr_defs
(
    consent_id INT NOT NULL,
    attr_id    INT NOT NULL,
    constraint consentatt_pk primary key (consent_id, attr_id),
    constraint consentatt_nam_fk foreign key (attr_id) references attr_names (id),
    constraint consentatt_consent_fk foreign key (consent_id) references consents (id)
);

create type mail_type as enum (
	'APP_CREATED_USER',
	'APPROVABLE_GROUP_APP_USER',
	'APP_CREATED_VO_ADMIN',
	'MAIL_VALIDATION',
	'APP_APPROVED_USER',
	'APP_REJECTED_USER',
	'APP_ERROR_VO_ADMIN',
	'USER_INVITE',
    'USER_PRE_APPROVED_INVITE'
);

-- APP_NOTIFICATIONS_SENT - sent applications notifications, used only for APP_CREATED_VO_ADMIN type for now
create table app_notifications_sent
(
    app_id            int                                     not null,
    notification_type mail_type                               not null,
    created_at        timestamp default statement_timestamp() not null,
    created_by        varchar   default user                  not null,
    constraint appnotifsent_pk primary key (app_id, notification_type),
    constraint appnotifsent_app_fk foreign key (app_id) references application (id) on delete cascade
);

create sequence "attr_names_id_seq";
create sequence "attribute_policies_id_seq";
create sequence "attribute_policy_collections_id_seq";
create sequence "auditer_consumers_id_seq";
create sequence "auditer_log_id_seq";
create sequence "consent_hubs_id_seq";
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
create sequence "res_tags_seq";
create sequence "mailchange_id_seq";
create sequence "pwdreset_id_seq";
create sequence "resources_bans_id_seq";
create sequence "facilities_bans_id_seq";
create sequence "vos_bans_id_seq";
create sequence "consents_id_seq";
create sequence "blocked_logins_id_seq";
create sequence "tasks_run_id_seq";
create sequence "invitations_id_seq";


create unique index idx_grp_nam_vo_parentg_u on groups (name, vo_id, coalesce(parent_group_id,'0'));
create index idx_namespace on attr_names (namespace);
create index idx_authz_user_role_id on authz (user_id, role_id);
create index idx_authz_authz_group_role_id on authz (authorized_group_id, role_id);
create index idx_auto_reg_grps_grp_app_form on auto_registration_groups (group_id, application_form_item_id);
create index idx_fk_cabthank_pub on cabinet_thanks (publicationid);
create index idx_fk_conhubfac_ch on consent_hubs_facilities (consent_hub_id);
create index idx_fk_conhubfac_fac on consent_hubs_facilities (facility_id);
create index idx_fk_usrex_usr on user_ext_sources (user_id);
create index idx_fk_usrex_usersrc on user_ext_sources (ext_sources_id);
create index idx_fk_mem_user on members (user_id);
create index idx_fk_mem_vo on members (vo_id);
create index idx_fk_host_fac on hosts (facility_id);
create index idx_fk_dest_srv on facility_service_destinations (service_id);
create index idx_fk_dest_fac on facility_service_destinations (facility_id);
create index idx_fk_dest_destc on facility_service_destinations (destination_id);
create index idx_fk_vousrsrc_usrsrc on vo_ext_sources (ext_sources_id);
create index idx_fk_vousrsrc_vos on vo_ext_sources (vo_id);
create index idx_fk_groupsrc_src on group_ext_sources (ext_source_id);
create index idx_fk_groupsrc_group on group_ext_sources (group_id);
create index idx_fk_usrcatt_usrc on ext_sources_attributes (ext_sources_id);
create index idx_fk_rsrc_fac on resources (facility_id);
create index idx_fk_rsrc_vo on resources (vo_id);
create index idx_fk_resatval_res on resource_attr_values (resource_id);
create index idx_fk_resatval_resatnam on resource_attr_values (attr_id);
create index idx_fk_usrav_usr on user_attr_values (user_id);
create index idx_fk_usrav_accattnam on user_attr_values (attr_id);
create index idx_fk_facow_fac on facility_owners (facility_id);
create index idx_fk_facow_ow on facility_owners (owner_id);
create index idx_fk_facattval_nam on facility_attr_values (attr_id);
create index idx_fk_facattval_fac on facility_attr_values (facility_id);
create index idx_fk_voattval_nam on vo_attr_values (attr_id);
create index idx_fk_voattval_vo on vo_attr_values (vo_id);
create index idx_fk_srvpkg_srv on service_service_packages (service_id);
create index idx_fk_srvpkg_pkg on service_service_packages (package_id);
create index idx_fk_grp_vos on groups (vo_id);
create index idx_fk_grp_grp on groups (parent_group_id);
create index idx_fk_memrav_mem on member_resource_attr_values (member_id);
create index idx_fk_memrav_rsrc on member_resource_attr_values (resource_id);
create index idx_fk_memrav_accattnam on member_resource_attr_values (attr_id);
create index idx_fk_memgav_mem on member_group_attr_values (member_id);
create index idx_fk_memgav_grp on member_group_attr_values (group_id);
create index idx_fk_memgav_accattnam on member_group_attr_values (attr_id);
create index idx_fk_usrfacav_mem on user_facility_attr_values (user_id);
create index idx_fk_usrfacav_fac on user_facility_attr_values (facility_id);
create index idx_fk_usrfacav_accattnam on user_facility_attr_values (attr_id);
create index idx_fk_task_srv on tasks (service_id);
create index idx_fk_task_fac on tasks (facility_id);
create index idx_fk_taskres_task on tasks_results (task_id);
create index idx_fk_taskres_dest on tasks_results (destination_id);
create index idx_fk_srvden_srv on service_denials (service_id);
create index idx_fk_srvden_fac on service_denials (facility_id);
create index idx_fk_srvden_dest on service_denials (destination_id);
create unique index idx_srvden_u ON service_denials (COALESCE(service_id, '0'), COALESCE(facility_id, '0'), COALESCE(destination_id, '0'));
create index idx_fk_srvreqattr_srv on service_required_attrs (service_id);
create index idx_fk_srvreqattr_attr on service_required_attrs (attr_id);
create index idx_fk_resrcsrv_srv on resource_services (service_id);
create index idx_fk_resrcsrv_rsrc on resource_services (resource_id);
create index idx_fk_memattval_mem on member_attr_values (member_id);
create index idx_fk_memattval_attr on member_attr_values (attr_id);
create index idx_fk_grpattval_grp on group_attr_values (group_id);
create index idx_fk_grpattval_attr on group_attr_values (attr_id);
create index idx_fk_grpresav_grp on group_resource_attr_values (group_id);
create index idx_fk_grpresav_res on group_resource_attr_values (resource_id);
create index idx_fk_grpresav_attr on group_resource_attr_values (attr_id);
create index idx_fk_hostav_host on host_attr_values (host_id);
create index idx_fk_hostav_attrt on host_attr_values (attr_id);
create index idx_fk_entlatval_attr on entityless_attr_values (attr_id);
create index idx_fk_catpub_sys on cabinet_publications (publicationsystemid);
create index idx_fk_cabpub_cat on cabinet_publications (categoryid);
create unique index idx_authz_u ON authz (COALESCE(user_id, '0'), COALESCE(authorized_group_id, '0'), role_id,
                                          COALESCE(group_id, '0'), COALESCE(vo_id, '0'), COALESCE(facility_id, '0'),
                                          COALESCE(member_id, '0'), COALESCE(resource_id, '0'),
                                          COALESCE(service_id, '0'),
                                          COALESCE(sponsored_user_id, '0'));
create index idx_fk_authz_role on authz (role_id);
create index idx_fk_authz_user on authz (user_id);
create index idx_fk_authz_authz_group on authz (authorized_group_id);
create index idx_fk_authz_vo on authz (vo_id);
create index idx_fk_authz_fac on authz (facility_id);
create index idx_fk_authz_mem on authz (member_id);
create index idx_fk_authz_group on authz (group_id);
create index idx_fk_authz_service on authz (service_id);
create index idx_fk_authz_res on authz (resource_id);
create index idx_fk_authz_sponsu on authz (sponsored_user_id);
create index idx_fk_grres_gr on groups_resources (group_id);
create index idx_fk_grres_res on groups_resources (resource_id);
create index idx_fk_grres_s_gr on groups_resources_state (group_id);
create index idx_fk_grres_s_res on groups_resources_state (resource_id);
create index idx_fk_grres_a_gr on groups_resources_automatic (group_id);
create index idx_fk_grres_a_res on groups_resources_automatic (resource_id);
create index idx_fk_grres_a_sgr on groups_resources_automatic (source_group_id);
create index idx_fk_grpmem_gr on groups_members (group_id);
create index idx_fk_grpmem_mem on groups_members (member_id);
create index idx_fk_grpmem_memtype on groups_members (membership_type);
create unique index applform_u1 on application_form (vo_id) where group_id is null;
create unique index applform_u2 on application_form (vo_id, group_id) where group_id is not null;
create index idx_fk_applform_vo on application_form (vo_id);
create index idx_fk_applform_group on application_form (group_id);
create index idx_fk_applfrmit_applform on application_form_items (form_id);
create index idx_fk_applfrmittyp_applfrmit on application_form_item_apptypes (item_id);
create index idx_fk_applfrmittxt_applfrmit on application_form_item_texts (item_id);
create index idx_fk_app_vo on application (vo_id);
create index idx_fk_app_user on application (user_id);
create index idx_fk_app_group on application (group_id);
create index idx_fk_appdata_app on application_data (app_id);
create index idx_fk_appdata_applfrmit on application_data (item_id);
create index idx_fk_applogin_userid on application_reserved_logins (user_id);
create index idx_fk_appmail_appform on application_mails (form_id);
create index idx_fk_appmailtxt_appmails on application_mail_texts (mail_id);
create index idx_fk_cabaut_pub on cabinet_authorships (publicationid);
create index idx_fk_cabaut_usr on cabinet_authorships (userid);
create index idx_fk_pn_poolmsg_tmpl on pn_pool_message (template_id);
create index idx_fk_pn_receiver_tmpl on pn_receiver (template_id);
create index idx_fk_pn_tmplmsg_tmpl on pn_template_message (template_id);
create index idx_fk_pn_tmplrgx_rgx on pn_template_regex (regex_id);
create index idx_fk_pn_tmplrgx_tmpl on pn_template_regex (template_id);
create index idx_fk_pn_rgxobj_rgx on pn_regex_object (regex_id);
create index idx_fk_pn_rgxobj_obj on pn_regex_object (object_id);
create index idx_fk_specifu_u_ui on specific_user_users (user_id);
create index idx_fk_specifu_u_sui on specific_user_users (specific_user_id);
create index idx_fk_grp_grp_rgid on groups_groups (result_gid);
create index idx_fk_grp_grp_ogid on groups_groups (operand_gid);
create index idx_fk_attrpol_role on attribute_policies (role_id);
create index idx_fk_attrpol_colid on attribute_policies (policy_collection_id);
create index idx_fk_attrpolcol_attr on attribute_policy_collections (attr_id);
create index idx_fk_restags_vos on res_tags (vo_id);
create index idx_fk_tags_res_tags on tags_resources (tag_id);
create index idx_fk_tags_res_res on tags_resources (resource_id);
create index idx_fk_mailchange_user_id on mailchange (user_id);
create index idx_fk_pwdreset_user_id on pwdreset (user_id);
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
create index idx_fk_memspons_mem ON members_sponsored (sponsored_id);
create index idx_fk_memspons_usr ON members_sponsored (sponsor_id);
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
CREATE INDEX ufauv_idx ON user_facility_attr_u_values (user_id, facility_id, attr_id);
CREATE INDEX vauv_idx ON vo_attr_u_values (vo_id, attr_id);
create index idx_fk_cons_usr ON consents (user_id);
create index idx_fk_cons_cons_hub ON consents (consent_hub_id);
create index idx_fk_attr_cons_cons ON consent_attr_defs (consent_id);
create index idx_fk_attr_cons_attr ON consent_attr_defs (attr_id);
create index idx_fk_alwd_grps_group ON allowed_groups_to_hierarchical_vo (group_id);
create index idx_fk_alwd_grps_vo ON allowed_groups_to_hierarchical_vo (vo_id);
create index idx_fk_attr_critops ON attribute_critical_actions (attr_id);
create index app_state_idx ON application (state);
create index idx_fk_inv_grps on invitations(group_id);
create index idx_fk_inv_vos on invitations(vo_id);
create index idx_fk_inv_usr on invitations(sender_id);
create index idx_fk_blk_attr_attr_names on blocked_attr_values(attr_id);


-- set initial Perun DB version
insert into configurations
values ('DATABASE VERSION', '3.2.32');
insert into configurations
values ('suspendGroupSync', 'false');
insert into configurations
values ('suspendTasksProp', 'false');
-- insert membership types
insert into membership_types (id, membership_type, description)
values (1, 'DIRECT', 'Member is directly added into group');
insert into membership_types (id, membership_type, description)
values (2, 'INDIRECT', 'Member is added indirectly through UNION relation');

-- init default auditer consumers
insert into auditer_consumers (id, name, last_processed_id)
values (nextval('auditer_consumers_id_seq'), 'dispatcher', 0);
insert into auditer_consumers (id, name, last_processed_id)
values (nextval('auditer_consumers_id_seq'), 'notifications', 0);

-- initial user, user_ext_source and internal ext_source
/*
insert into ext_sources (id,name,type) values (nextval('ext_sources_id_seq'),'INTERNAL','cz.metacentrum.perun.core.impl.ExtSourceInternal');
insert into users (id, first_name, last_name) values (nextval('users_id_seq'),'Master','Perun');
insert into user_ext_sources (id, user_id, login_ext, ext_sources_id, loa) values (nextval('user_ext_sources_id_seq'), currval('users_id_seq'), 'perun', currval('ext_sources_id_seq'), 0);
*/

insert into roles (created_by_uid, modified_by_uid, id, name)
values (null, null, 21, 'perunadmin');
insert into roles (created_by_uid, modified_by_uid, id, name)
values (null, null, 22, 'voadmin');
insert into roles (created_by_uid, modified_by_uid, id, name)
values (null, null, 23, 'groupadmin');
insert into roles (created_by_uid, modified_by_uid, id, name)
values (null, null, 24, 'self');
insert into roles (created_by_uid, modified_by_uid, id, name)
values (null, null, 25, 'authzresolver');
insert into roles (created_by_uid, modified_by_uid, id, name)
values (null, null, 26, 'facilityadmin');
insert into roles (created_by_uid, modified_by_uid, id, name)
values (null, null, 29, 'registrar');
insert into roles (created_by_uid, modified_by_uid, id, name)
values (null, null, 30, 'engine');
insert into roles (created_by_uid, modified_by_uid, id, name)
values (null, null, 31, 'rpc');
insert into roles (created_by_uid, modified_by_uid, id, name)
values (null, null, 32, 'unknown');
insert into roles (created_by_uid, modified_by_uid, id, name)
values (null, null, 81, 'voobserver');
insert into roles (created_by_uid, modified_by_uid, id, name)
values (null, null, 101, 'topgroupcreator');
insert into roles (created_by_uid, modified_by_uid, id, name)
values (null, null, 41, 'notifications');
insert into roles (created_by_uid, modified_by_uid, id, name)
values (null, null, 61, 'serviceuser');
insert into roles (created_by_uid, modified_by_uid, id, name)
values (null, null, 19, 'sponsor');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (3255, 3261, 'Shibboleth external sources', 2380, 'urn:perun:user:attribute-def:virt:shibbolethExtSources',
        'shibbolethExtSources', 'urn:perun:user:attribute-def:virt', 'java.util.LinkedHashMap',
        'Pairs of IdP indentifier and user''s EPPN.', timestamp '2013-06-27 10:02:15.5', 'glory@META',
        timestamp '2013-10-29 11:28:02.3', '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Home mount points', 14, 'urn:perun:facility:attribute-def:def:homeMountPoints', 'homeMountPoints',
        'urn:perun:facility:attribute-def:def', 'java.util.ArrayList', 'Available mount points for home on Facility.',
        timestamp '2011-06-01 10:57:48.4', 'PERUNV3', timestamp '2013-10-24 14:03:50.5',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (6701, 3261, 'DN of certificates', 2400, 'urn:perun:user:attribute-def:virt:userCertDNs', 'userCertDNs',
        'urn:perun:user:attribute-def:virt', 'java.util.LinkedHashMap',
        'All DNs of user''s certificates including external identities X509.', timestamp '2013-07-03 13:35:25.4',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739',
        timestamp '2013-10-24 13:09:53.5', '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'KERBEROS principals', 800, 'urn:perun:user:attribute-def:def:kerberosLogins', 'kerberosLogins',
        'urn:perun:user:attribute-def:def', 'java.util.ArrayList', 'Logins in KERBEROS  (including realm).',
        timestamp '2012-02-15 13:43:26.4', 'PERUNV3', timestamp '2013-10-23 14:27:19.4',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'UID in cerit', 1040, 'urn:perun:user:attribute-def:def:uid-namespace:cerit', 'uid-namespace:cerit',
        'urn:perun:user:attribute-def:def', 'java.lang.Integer', 'UID in namespace ''cerit''.',
        timestamp '2012-03-14 16:05:00.4', 'michalp@META', timestamp '2013-10-24 13:14:53.5',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Default shell', 21, 'urn:perun:resource:attribute-def:def:defaultShell', 'defaultShell',
        'urn:perun:resource:attribute-def:def', 'java.lang.String',
        'Default shell applied to all users on Resource. Selection is limited by available shells on Resource and Facility.',
        timestamp '2011-06-01 10:57:48.4', 'PERUNV3', timestamp '2013-10-25 10:28:29.6',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Available shells', 19, 'urn:perun:resource:attribute-def:def:shells', 'shells',
        'urn:perun:resource:attribute-def:def', 'java.util.ArrayList',
        'Available shells for users on Resource. Selection is limited by available shells on Facility.',
        timestamp '2011-06-01 10:57:48.4', 'PERUNV3', timestamp '2013-10-25 10:29:29.6',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'ID', 32, 'urn:perun:member:attribute-def:core:id', 'id', 'urn:perun:member:attribute-def:core',
        'java.lang.Integer', 'Identifier of member of VO at DB.', timestamp '2011-06-01 10:57:48.4', 'PERUNV3',
        timestamp '2013-10-25 12:42:39.6', '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Shell', 22, 'urn:perun:user_facility:attribute-def:def:shell', 'shell',
        'urn:perun:user_facility:attribute-def:def', 'java.lang.String', 'User''s selected shell on Facility.',
        timestamp '2011-06-01 10:57:48.4', 'PERUNV3', timestamp '2013-10-24 13:24:11.5',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Default home mount point', 23, 'urn:perun:resource:attribute-def:def:defaultHomeMountPoint',
        'defaultHomeMountPoint', 'urn:perun:resource:attribute-def:def', 'java.lang.String',
        'Default home mount point for users on Resource. Selection is limited by available home mount points on Resource and Facility.',
        timestamp '2011-06-01 10:57:48.4', 'PERUNV3', timestamp '2013-10-25 10:30:50.6',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'ID', 38, 'urn:perun:resource:attribute-def:core:id', 'id', 'urn:perun:resource:attribute-def:core',
        'java.lang.Integer', 'Identifier of resource at DB.', timestamp '2011-06-01 10:57:48.4', 'PERUNV3',
        timestamp '2013-10-25 10:21:38.6', '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Name', 39, 'urn:perun:resource:attribute-def:core:name', 'name',
        'urn:perun:resource:attribute-def:core', 'java.lang.String', 'Resource name.',
        timestamp '2011-06-01 10:57:48.4', 'PERUNV3', timestamp '2013-10-25 10:26:58.6',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Description', 40, 'urn:perun:resource:attribute-def:core:description', 'description',
        'urn:perun:resource:attribute-def:core', 'java.lang.String',
        'Description of resource, purpose of it''s creating.', timestamp '2011-06-01 10:57:48.4', 'PERUNV3',
        timestamp '2013-10-25 10:27:32.6', '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'ID', 43, 'urn:perun:vo:attribute-def:core:id', 'id', 'urn:perun:vo:attribute-def:core',
        'java.lang.Integer', 'Identifier of VO in DB.', timestamp '2011-06-01 10:57:48.4', 'PERUNV3',
        timestamp '2013-10-25 13:03:50.6', '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Name', 44, 'urn:perun:vo:attribute-def:core:name', 'name', 'urn:perun:vo:attribute-def:core',
        'java.lang.String', 'Full name of  Virtual organization.', timestamp '2011-06-01 10:57:48.4', 'PERUNV3',
        timestamp '2013-10-25 13:04:32.6', '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Short name', 45, 'urn:perun:vo:attribute-def:core:shortName', 'shortName',
        'urn:perun:vo:attribute-def:core', 'java.lang.String', 'Short name of Virtual organization.',
        timestamp '2011-06-01 10:57:48.4', 'PERUNV3', timestamp '2013-10-25 13:05:13.6',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'ID', 49, 'urn:perun:user:attribute-def:core:id', 'id', 'urn:perun:user:attribute-def:core',
        'java.lang.Integer', 'Identifier of user at DB.', timestamp '2011-06-01 10:57:48.4', 'PERUNV3',
        timestamp '2013-10-22 13:47:53.3', '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'First name', 50, 'urn:perun:user:attribute-def:core:firstName', 'firstName',
        'urn:perun:user:attribute-def:core', 'java.lang.String', 'User''s first (christening) name.',
        timestamp '2011-06-01 10:57:48.4', 'PERUNV3', timestamp '2013-10-22 13:50:11.3',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Last name', 51, 'urn:perun:user:attribute-def:core:lastName', 'lastName',
        'urn:perun:user:attribute-def:core', 'java.lang.String', 'User''s last name (family name).',
        timestamp '2011-06-01 10:57:48.4', 'PERUNV3', timestamp '2013-10-22 13:50:41.3',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Middle name', 52, 'urn:perun:user:attribute-def:core:middleName', 'middleName',
        'urn:perun:user:attribute-def:core', 'java.lang.String', 'User''s middle name.',
        timestamp '2011-06-01 10:57:48.4', 'PERUNV3', timestamp '2013-10-22 13:57:32.3',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Title before', 53, 'urn:perun:user:attribute-def:core:titleBefore', 'titleBefore',
        'urn:perun:user:attribute-def:core', 'java.lang.String', 'Scientific degree before of user''s name.',
        timestamp '2011-06-01 10:57:48.4', 'PERUNV3', timestamp '2013-10-22 13:58:06.3',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Title after', 54, 'urn:perun:user:attribute-def:core:titleAfter', 'titleAfter',
        'urn:perun:user:attribute-def:core', 'java.lang.String', 'Scientific degree behind of user''s name.',
        timestamp '2011-06-01 10:57:48.4', 'PERUNV3', timestamp '2013-10-22 13:58:30.3',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'UID in ruk', 221, 'urn:perun:user:attribute-def:def:uid-namespace:ruk', 'uid-namespace:ruk',
        'urn:perun:user:attribute-def:def', 'java.lang.Integer', 'UID in namespace ''ruk''.',
        timestamp '2011-11-28 12:50:38.2', 'PERUNV3', timestamp '2013-10-24 13:17:17.5',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'UID in ics', 222, 'urn:perun:user:attribute-def:def:uid-namespace:ics', 'uid-namespace:ics',
        'urn:perun:user:attribute-def:def', 'java.lang.Integer', 'UID in namespace ''ics''.',
        timestamp '2011-11-28 12:50:59.2', 'PERUNV3', timestamp '2013-10-24 13:16:51.5',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'UID namespace', 263, 'urn:perun:facility:attribute-def:def:uid-namespace', 'uid-namespace',
        'urn:perun:facility:attribute-def:def', 'java.lang.String', 'Define namespace for user''s UIDs on Facility.',
        timestamp '2011-12-01 13:59:30.5', 'PERUNV3', timestamp '2013-10-24 13:46:08.5',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'GID namespace', 280, 'urn:perun:facility:attribute-def:def:unixGID-namespace', 'unixGID-namespace',
        'urn:perun:facility:attribute-def:def', 'java.lang.String',
        'Define namespace for unix groups GIDs on Facility.', timestamp '2011-12-01 14:39:05.5', 'PERUNV3',
        timestamp '2013-10-24 13:49:27.5', '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Group''s external source', 100, 'urn:perun:group:attribute-def:def:groupExtSource',
        'groupExtSource', 'urn:perun:group:attribute-def:def', 'java.lang.String',
        'External source from which group comes from. Used for groups synchronization.',
        timestamp '2011-09-20 12:33:19.3', 'PERUNV3', timestamp '2013-10-25 11:18:32.6',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Member''s external source', 101, 'urn:perun:group:attribute-def:def:groupMembersExtSource',
        'groupMembersExtSource', 'urn:perun:group:attribute-def:def', 'java.lang.String',
        'External source from which group members comes from. Used for group synchronization.',
        timestamp '2011-09-20 12:33:19.3', 'PERUNV3', timestamp '2013-10-25 11:19:25.6',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Member''s query', 102, 'urn:perun:group:attribute-def:def:groupMembersQuery', 'groupMembersQuery',
        'urn:perun:group:attribute-def:def', 'java.lang.String',
        'Query (SQL) on external source which retrieves list of it''s members.', timestamp '2011-09-20 12:33:19.3',
        'PERUNV3', timestamp '2013-10-25 11:20:14.6',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Synchronization enabled', 103, 'urn:perun:group:attribute-def:def:synchronizationEnabled',
        'synchronizationEnabled', 'urn:perun:group:attribute-def:def', 'java.lang.String',
        'Enables group synchronization from external source.', timestamp '2011-09-20 12:33:21.3', 'PERUNV3',
        timestamp '2013-10-25 11:21:16.6', '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3411, 'Synchronization interval', 104, 'urn:perun:group:attribute-def:def:synchronizationInterval',
        'synchronizationInterval', 'urn:perun:group:attribute-def:def', 'java.lang.String',
        'Time between two successful synchronizations in (value * 5) minutes.', timestamp '2011-09-20 12:33:21.3',
        'PERUNV3', timestamp '2014-12-04 10:41:31.5',
        '/C=CZ/O=Masarykova univerzita/CN=Pavel Zl\xC3\xA1mal/unstructuredName=256627');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Min UID in namespace', 300, 'urn:perun:entityless:attribute-def:def:namespace-minUID',
        'namespace-minUID', 'urn:perun:entityless:attribute-def:def', 'java.lang.Integer',
        'Minimal value of UID in specific namespace.', timestamp '2011-12-02 11:25:33.6', 'PERUNV3',
        timestamp '2013-10-25 13:50:04.6', '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Max UID in namespace', 301, 'urn:perun:entityless:attribute-def:def:namespace-maxUID',
        'namespace-maxUID', 'urn:perun:entityless:attribute-def:def', 'java.lang.Integer',
        'Maximal value of UID in specific namespace.', timestamp '2011-12-02 11:25:40.6', 'PERUNV3',
        timestamp '2013-10-25 13:50:04.6', '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Membership expiration rules', 1540, 'urn:perun:vo:attribute-def:def:membershipExpirationRules',
        'membershipExpirationRules', 'urn:perun:vo:attribute-def:def', 'java.util.LinkedHashMap',
        'Set of rules to determine date of membership expiration. If not set, membership is not limited.',
        timestamp '2012-09-27 13:22:58.5', 'michalp@META', timestamp '2013-10-25 13:17:17.6',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (6701, 3261, 'Unix group name namespace', 2340, 'urn:perun:facility:attribute-def:def:unixGroupName-namespace',
        'unixGroupName-namespace', 'urn:perun:facility:attribute-def:def', 'java.lang.String',
        'Define namespace for unix groups names on Facility.', timestamp '2013-06-17 14:24:30.2',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739',
        timestamp '2013-10-24 13:52:19.5', '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Workplace (for VO)', 343, 'urn:perun:member:attribute-def:def:workplace', 'workplace',
        'urn:perun:member:attribute-def:def', 'java.lang.String', 'Workplace at the member''s organization.',
        timestamp '2011-12-06 15:07:32.3', 'PERUNV3', timestamp '2013-10-25 12:44:08.6',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Hostname', 380, 'urn:perun:facility:attribute-def:def:hostName', 'hostName',
        'urn:perun:facility:attribute-def:def', 'java.lang.String', 'Hostname for Facility',
        timestamp '2011-12-07 12:23:03.4', 'PERUNV3', timestamp '2013-10-24 14:26:07.5',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Number of cores', 401, 'urn:perun:facility:attribute-def:def:hostCoresNumber', 'hostCoresNumber',
        'urn:perun:facility:attribute-def:def', 'java.lang.Integer', 'Number of cores on Host Facility.',
        timestamp '2011-12-08 09:16:57.5', 'PERUNV3', timestamp '2013-10-24 14:27:16.5',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Number of cores', 400, 'urn:perun:host:attribute-def:def:coresNumber', 'coresNumber',
        'urn:perun:host:attribute-def:def', 'java.lang.Integer', 'Number of cores on hosts inside cluster.',
        timestamp '2011-12-08 09:16:00.5', 'PERUNV3', timestamp '2013-10-24 14:17:38.5',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'FS home mount point', 500, 'urn:perun:resource:attribute-def:def:fsHomeMountPoint',
        'fsHomeMountPoint', 'urn:perun:resource:attribute-def:def', 'java.lang.String',
        'Home mount point for resource.', timestamp '2011-12-19 22:12:00.2', 'PERUNV3',
        timestamp '2013-10-25 10:48:13.6', '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Default data limit', 520, 'urn:perun:resource:attribute-def:def:defaultDataLimit',
        'defaultDataLimit', 'urn:perun:resource:attribute-def:def', 'java.lang.String',
        'Default hard quota for user''s data, including units (M,G,T, ...), G is default.',
        timestamp '2011-12-21 13:52:26.4', 'PERUNV3', timestamp '2013-10-25 10:38:26.6',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Default data quota', 521, 'urn:perun:resource:attribute-def:def:defaultDataQuota',
        'defaultDataQuota', 'urn:perun:resource:attribute-def:def', 'java.lang.String',
        'Default soft quota for user''s data including units (M, G, T, ...), G is default.',
        timestamp '2011-12-21 13:54:07.4', 'PERUNV3', timestamp '2013-10-25 10:37:12.6',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Shell for passwd_scp', 601, 'urn:perun:facility:attribute-def:def:shell_passwd-scp',
        'shell_passwd-scp', 'urn:perun:facility:attribute-def:def', 'java.lang.String',
        'Shell used for passwd_scp access on Facility.', timestamp '2012-01-12 12:36:15.5', 'PERUNV3',
        timestamp '2013-10-24 14:06:52.5', '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Default files quota', 760, 'urn:perun:resource:attribute-def:def:defaultFilesQuota',
        'defaultFilesQuota', 'urn:perun:resource:attribute-def:def', 'java.lang.Integer',
        'Default soft quota for max. number of user''s files.', timestamp '2012-02-08 10:16:41.4', 'PERUNV3',
        timestamp '2013-10-25 10:39:47.6', '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Default files limit', 761, 'urn:perun:resource:attribute-def:def:defaultFilesLimit',
        'defaultFilesLimit', 'urn:perun:resource:attribute-def:def', 'java.lang.Integer',
        'Default hard quota for max. number of user''s files.', timestamp '2012-02-08 10:17:12.4', 'PERUNV3',
        timestamp '2013-10-25 10:40:50.6', '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Membership expiration', 860, 'urn:perun:member:attribute-def:def:membershipExpiration',
        'membershipExpiration', 'urn:perun:member:attribute-def:def', 'java.lang.String',
        'Date of VO membership expiration.', timestamp '2012-03-02 14:03:09.6', 'michalp@META',
        timestamp '2013-10-25 12:52:15.6', '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Sponzored member', 900, 'urn:perun:member:attribute-def:def:sponzoredMember', 'sponzoredMember',
        'urn:perun:member:attribute-def:def', 'java.lang.String',
        'RT ticket number, which describes why the membership is sponzored.', timestamp '2012-03-08 17:03:42.5',
        'michalp@META', timestamp '2013-10-25 12:53:21.6',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Display name', 1140, 'urn:perun:user:attribute-def:core:displayName', 'displayName',
        'urn:perun:user:attribute-def:core', 'java.lang.String', 'Displayed user''s name.',
        timestamp '2012-04-24 15:00:01.3', 'michalp@META', timestamp '2013-10-23 14:07:18.4',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Umask for home', 1200, 'urn:perun:facility:attribute-def:def:homeDirUmask', 'homeDirUmask',
        'urn:perun:facility:attribute-def:def', 'java.lang.String',
        'Unix umask, which will be applied when new home folder is created.', timestamp '2012-05-13 17:15:20.1',
        'glory@META', timestamp '2013-10-24 14:11:09.5',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Workplace', 1366, 'urn:perun:user:attribute-def:def:workplace', 'workplace',
        'urn:perun:user:attribute-def:def', 'java.lang.String', 'Workplace in organization. Provided by IDP.',
        timestamp '2012-08-17 12:34:21.6', 'michalp@META', timestamp '2013-10-22 14:00:34.3',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'ID', 80, 'urn:perun:group:attribute-def:core:id', 'id', 'urn:perun:group:attribute-def:core',
        'java.lang.Integer', 'Identifier of group at DB.', timestamp '2011-07-01 15:10:18.6', 'PERUNV3',
        timestamp '2013-10-25 11:13:01.6', '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Name', 81, 'urn:perun:group:attribute-def:core:name', 'name', 'urn:perun:group:attribute-def:core',
        'java.lang.String', 'Group name.', timestamp '2011-07-01 15:10:18.6', 'PERUNV3',
        timestamp '2013-10-25 11:13:32.6', '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Description', 82, 'urn:perun:group:attribute-def:core:description', 'description',
        'urn:perun:group:attribute-def:core', 'java.lang.String', 'Description of group, for what purpose it is used.',
        timestamp '2011-07-01 15:10:18.6', 'PERUNV3', timestamp '2013-10-25 11:14:06.6',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Login namespace', 261, 'urn:perun:facility:attribute-def:def:login-namespace', 'login-namespace',
        'urn:perun:facility:attribute-def:def', 'java.lang.String',
        'Define namespace for all user''s logins on Facility.', timestamp '2011-12-01 13:27:59.5', 'PERUNV3',
        timestamp '2013-10-24 13:45:13.5', '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Max UID', 360, 'urn:perun:facility:attribute-def:virt:maxUID', 'maxUID',
        'urn:perun:facility:attribute-def:virt', 'java.lang.Integer',
        'Maximal UID. Value is determined automatically based on Facility''s namespace.',
        timestamp '2011-12-07 10:36:52.4', 'PERUNV3', timestamp '2013-10-24 13:54:20.5',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Min UID', 361, 'urn:perun:facility:attribute-def:virt:minUID', 'minUID',
        'urn:perun:facility:attribute-def:virt', 'java.lang.Integer',
        'Minimal UID. Value is determined automatically based on Facility''s namespace.',
        timestamp '2011-12-07 10:36:57.4', 'PERUNV3', timestamp '2013-10-24 13:56:24.5',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Mount point for fs_scratch', 460, 'urn:perun:facility:attribute-def:def:fsScratchMountPoint',
        'fsScratchMountPoint', 'urn:perun:facility:attribute-def:def', 'java.lang.String',
        'Mount point where the scratch is located.', timestamp '2011-12-16 15:21:32.6', 'PERUNV3',
        timestamp '2013-10-24 14:08:25.5', '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3411, 'Login in namespace: cesnet', 660, 'urn:perun:user:attribute-def:def:login-namespace:cesnet',
        'login-namespace:cesnet', 'urn:perun:user:attribute-def:def', 'java.lang.String',
        'Logname in namespace ''cesnet''.', timestamp '2012-01-17 10:35:00.3', 'PERUNV3',
        timestamp '2014-09-24 14:39:44.4',
        '/C=CZ/O=Masarykova univerzita/CN=Pavel Zl\xC3\xA1mal/unstructuredName=256627');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Status', 880, 'urn:perun:member:attribute-def:core:status', 'status',
        'urn:perun:member:attribute-def:core', 'java.lang.String',
        'Status of member (VALID,INVALID,EXPIRED,SUSPENDED,DISABLED).', timestamp '2012-03-07 14:24:01.4', 'glory@META',
        timestamp '2013-10-25 12:54:42.6', '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Registration note', 1180, 'urn:perun:member:attribute-def:opt:registrationNote',
        'registrationNote', 'urn:perun:member:attribute-def:opt', 'java.lang.String',
        'Note entered on registration form. Usually reason of registration.', timestamp '2012-05-09 14:15:20.4',
        'michalp@META', timestamp '2013-10-25 12:55:32.6',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Data quota', 1241, 'urn:perun:member_resource:attribute-def:def:dataQuota', 'dataQuota',
        'urn:perun:member_resource:attribute-def:def', 'java.lang.String',
        'Soft quota for user''s data, including units (M,G,T, ...), G is default. Standard is empty, Default data quota is used. Filled value defines exception from Resource''s default.',
        timestamp '2012-05-22 10:41:23.3', 'zora@META', timestamp '2013-10-25 10:42:57.6',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Files quota', 1242, 'urn:perun:member_resource:attribute-def:def:filesQuota', 'filesQuota',
        'urn:perun:member_resource:attribute-def:def', 'java.lang.Integer',
        'Soft quota for max. number of user''s files. Standard is empty, Default files quota is used.Filled value defines exception from Resource''s default.',
        timestamp '2012-05-22 10:41:56.3', 'zora@META', timestamp '2013-10-25 10:45:38.6',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Preferred language (for VO)', 1300, 'urn:perun:member:attribute-def:def:preferredLanguage',
        'preferredLanguage', 'urn:perun:member:attribute-def:def', 'java.lang.String',
        'Language prefferred for communication (notification) for VO.', timestamp '2012-06-18 11:53:54.2',
        '39700@muni.cz', timestamp '2013-10-25 12:47:29.6',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Unix group name in cerit', 1320, 'urn:perun:group:attribute-def:def:unixGroupName-namespace:cerit',
        'unixGroupName-namespace:cerit', 'urn:perun:group:attribute-def:def', 'java.lang.String',
        'Unix group name in namespace ''cerit'' .', timestamp '2012-06-28 13:40:03.5', '255739@muni.cz',
        timestamp '2013-10-25 12:32:08.6', '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Link to user''s dashboard', 1341, 'urn:perun:vo:attribute-def:def:dashboardLink', 'dashboardLink',
        'urn:perun:vo:attribute-def:def', 'java.lang.String',
        'Link to the user''s dashboard. URL must start with http://.', timestamp '2012-08-15 11:23:11.4',
        'michalp@META', timestamp '2013-10-25 13:07:35.6',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'AFS server', 1370, 'urn:perun:facility:attribute-def:def:afsServer', 'afsServer',
        'urn:perun:facility:attribute-def:def', 'java.lang.String', 'AFS server for Facility.',
        timestamp '2012-08-17 14:26:00.6', 'michalp@META', timestamp '2013-10-24 14:32:00.5',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'AFS users volume', 1373, 'urn:perun:resource:attribute-def:def:afsUsersVolume', 'afsUsersVolume',
        'urn:perun:resource:attribute-def:def', 'java.lang.String',
        'Prefix of the AFS volme, where users have theirs directories.', timestamp '2012-08-17 14:43:17.6',
        'michalp@META', timestamp '2013-10-25 10:56:23.6',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'AFS mountpoint', 1377, 'urn:perun:resource:attribute-def:def:afsUsersMountPoint',
        'afsUsersMountPoint', 'urn:perun:resource:attribute-def:def', 'java.lang.String', 'AFS user''s mountpoint.',
        timestamp '2012-08-17 14:46:46.6', 'michalp@META', timestamp '2013-10-25 10:55:00.6',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'AFS user''s quota', 1376, 'urn:perun:user_facility:attribute-def:def:afsUserQuota', 'afsUserQuota',
        'urn:perun:user_facility:attribute-def:def', 'java.lang.String',
        'User''s quota for AFS. Filled value define exception from default.', timestamp '2012-08-17 14:45:47.6',
        'michalp@META', timestamp '2013-10-24 13:32:42.5',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'AFS volume', 1378, 'urn:perun:resource:attribute-def:def:afsVolume', 'afsVolume',
        'urn:perun:resource:attribute-def:def', 'java.lang.String', 'AFS top-level volume.',
        timestamp '2012-08-17 14:47:54.6', 'michalp@META', timestamp '2013-10-25 10:53:56.6',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Description for user''s settings page in GUI', 1400,
        'urn:perun:resource:attribute-def:def:userSettingsDescription', 'userSettingsDescription',
        'urn:perun:resource:attribute-def:def', 'java.lang.String',
        'Description, which is shown to users when changing shell or quotas in GUI.', timestamp '2012-08-27 12:38:52.2',
        'glory@META', timestamp '2013-10-25 10:33:47.6',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Spec description', 1480, 'urn:perun:facility:attribute-def:def:spec', 'spec',
        'urn:perun:facility:attribute-def:def', 'java.util.LinkedHashMap',
        'Detailed description of facility hardware (in CS and EN).', timestamp '2012-09-13 10:04:13.5', 'makub@META',
        timestamp '2013-10-24 14:36:03.5', '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Reserved', 1424, 'urn:perun:host:attribute-def:def:reserved', 'reserved',
        'urn:perun:host:attribute-def:def', 'java.lang.String', 'Is this Host permanently reserved?',
        timestamp '2012-09-03 16:29:52.2', 'glory@META', timestamp '2013-10-24 14:28:49.5',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Mail', 60, 'urn:perun:member:attribute-def:def:mail', 'mail', 'urn:perun:member:attribute-def:def',
        'java.lang.String', 'E-mail address in organization (VO wide).', timestamp '2011-06-01 13:36:44.4', 'PERUNV3',
        timestamp '2013-10-25 12:48:40.6', '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3411, 'Login in namespace: mu', 61, 'urn:perun:user:attribute-def:def:login-namespace:mu',
        'login-namespace:mu', 'urn:perun:user:attribute-def:def', 'java.lang.String', 'Logname in namespace ''mu''.',
        timestamp '2011-06-01 13:37:01.4', 'PERUNV3', timestamp '2014-09-24 14:39:44.4',
        '/C=CZ/O=Masarykova univerzita/CN=Pavel Zl\xC3\xA1mal/unstructuredName=256627');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Address (for VO)', 62, 'urn:perun:member:attribute-def:def:address', 'address',
        'urn:perun:member:attribute-def:def', 'java.lang.String',
        'Member''s address in organization (can be different from user''s address).', timestamp '2011-06-01 13:37:14.4',
        'PERUNV3', timestamp '2013-10-25 12:45:46.6',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'ID', 120, 'urn:perun:host:attribute-def:core:id', 'id', 'urn:perun:host:attribute-def:core',
        'java.lang.Integer', 'Identifier of host at DB.', timestamp '2011-10-24 14:30:58.2', 'PERUNV3',
        timestamp '2013-10-24 14:15:17.5', '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Hostname', 121, 'urn:perun:host:attribute-def:core:hostname', 'hostname',
        'urn:perun:host:attribute-def:core', 'java.lang.String', 'Full name of Host.',
        timestamp '2011-10-24 14:30:58.2', 'PERUNV3', timestamp '2013-10-24 14:16:39.5',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Organization (for VO)', 123, 'urn:perun:member:attribute-def:def:organization', 'organization',
        'urn:perun:member:attribute-def:def', 'java.lang.String', 'Organization, from which user comes from.',
        timestamp '2011-10-24 14:47:04.2', 'PERUNV3', timestamp '2013-10-25 12:50:43.6',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'EDUROAM identity', 1060, 'urn:perun:user:attribute-def:def:eduroamIdentities', 'eduroamIdentities',
        'urn:perun:user:attribute-def:def', 'java.util.ArrayList', 'List of  identities in EDUROAM.',
        timestamp '2012-03-21 09:23:14.4', 'michalp@META', timestamp '2013-10-23 14:25:42.4',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Phone', 1362, 'urn:perun:user:attribute-def:def:phone', 'phone',
        'urn:perun:user:attribute-def:def', 'java.lang.String', 'Phone number in organization. Provided by IDP.',
        timestamp '2012-08-17 12:32:30.6', 'michalp@META', timestamp '2013-10-23 14:10:23.4',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Unix group name in einfra', 1321,
        'urn:perun:group:attribute-def:def:unixGroupName-namespace:einfra', 'unixGroupName-namespace:einfra',
        'urn:perun:group:attribute-def:def', 'java.lang.String', 'Unix group name in namespace ''einfra''.',
        timestamp '2012-06-28 13:40:27.5', '255739@muni.cz', timestamp '2013-10-25 12:33:54.6',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Unix group name in storage', 1322,
        'urn:perun:group:attribute-def:def:unixGroupName-namespace:storage', 'unixGroupName-namespace:storage',
        'urn:perun:group:attribute-def:def', 'java.lang.String', 'Unix group name in namespace ''storage''.',
        timestamp '2012-06-28 13:40:48.5', '255739@muni.cz', timestamp '2013-10-25 12:35:17.6',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Unix GID in storage', 1325, 'urn:perun:group:attribute-def:def:unixGID-namespace:storage',
        'unixGID-namespace:storage', 'urn:perun:group:attribute-def:def', 'java.lang.Integer',
        'Unix GID in namespace ''storage''.', timestamp '2012-06-28 13:42:02.5', '255739@muni.cz',
        timestamp '2013-10-25 12:38:34.6', '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Preferred mail', 1361, 'urn:perun:user:attribute-def:def:preferredMail', 'preferredMail',
        'urn:perun:user:attribute-def:def', 'java.lang.String', 'E-mail address preferred for communication.',
        timestamp '2012-08-17 12:32:16.6', 'michalp@META', timestamp '2013-10-23 14:11:57.4',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Organization', 1363, 'urn:perun:user:attribute-def:def:organization', 'organization',
        'urn:perun:user:attribute-def:def', 'java.lang.String',
        'Organization, from which user comes from. Provided by IDP.', timestamp '2012-08-17 12:32:44.6', 'michalp@META',
        timestamp '2013-10-23 14:11:05.4', '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Address', 1364, 'urn:perun:user:attribute-def:def:address', 'address',
        'urn:perun:user:attribute-def:def', 'java.lang.String', 'Address in organization. Provided by IDP.',
        timestamp '2012-08-17 12:32:59.6', 'michalp@META', timestamp '2013-10-23 14:08:35.4',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Frontend', 1425, 'urn:perun:host:attribute-def:def:frontend', 'frontend',
        'urn:perun:host:attribute-def:def', 'java.lang.String', 'Is this host a frontend?',
        timestamp '2012-09-03 16:30:44.2', 'glory@META', timestamp '2013-10-24 14:28:11.5',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3411, 'VO''s application URL', 1460, 'urn:perun:vo:attribute-def:def:applicationURL', 'applicationURL',
        'urn:perun:vo:attribute-def:def', 'java.lang.String',
        'Custom link to VO''s application form used in e-mail invitations.', timestamp '2012-09-10 11:35:22.2',
        'michalp@META', timestamp '2014-09-03 09:02:36.4',
        '/C=CZ/O=Masarykova univerzita/CN=Pavel Zl\xC3\xA1mal/unstructuredName=256627');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3411, 'VO logo''s URL', 1560, 'urn:perun:vo:attribute-def:def:voLogoURL', 'voLogoURL',
        'urn:perun:vo:attribute-def:def', 'java.lang.String', 'Full URL of the VO''s logo image (including http://).',
        timestamp '2012-10-01 13:23:20.2', 'michalp@META', timestamp '2014-11-04 10:16:53.3',
        '/C=CZ/O=Masarykova univerzita/CN=Pavel Zl\xC3\xA1mal/unstructuredName=256627');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'HW description URL', 1482, 'urn:perun:facility:attribute-def:def:url', 'url',
        'urn:perun:facility:attribute-def:def', 'java.util.LinkedHashMap', 'URL to HW description (in CS and EN).',
        timestamp '2012-09-13 10:04:51.5', 'makub@META', timestamp '2013-10-24 14:37:14.5',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Contact e-mails', 1500, 'urn:perun:vo:attribute-def:def:contactEmail', 'contactEmail',
        'urn:perun:vo:attribute-def:def', 'java.util.ArrayList', 'Contact emails for the users ( to User support).',
        timestamp '2012-09-25 16:24:25.3', 'zlamalp@META', timestamp '2013-10-25 13:09:34.6',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Public ssh Key', 1520, 'urn:perun:user:attribute-def:def:sshPublicKey', 'sshPublicKey',
        'urn:perun:user:attribute-def:def', 'java.util.ArrayList', 'User''s public SSH key.',
        timestamp '2012-09-26 09:45:04.4', 'michalp@META', timestamp '2013-10-24 12:43:25.5',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Discs', 1605, 'urn:perun:facility:attribute-def:def:disk', 'disk',
        'urn:perun:facility:attribute-def:def', 'java.util.LinkedHashMap',
        'Description of installed discs (in CS and EN).', timestamp '2012-10-05 12:30:52.6', 'makub@META',
        timestamp '2013-10-24 14:38:28.5', '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Network interfaces', 1606, 'urn:perun:facility:attribute-def:def:network', 'network',
        'urn:perun:facility:attribute-def:def', 'java.util.LinkedHashMap',
        'Description of network interfaces (Ethernet,Infiniband etc) in CS and EN', timestamp '2012-10-05 12:32:29.6',
        'makub@META', timestamp '2013-10-24 14:39:45.5',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Comment', 1607, 'urn:perun:facility:attribute-def:def:comment', 'comment',
        'urn:perun:facility:attribute-def:def', 'java.util.LinkedHashMap',
        'Comment of hardware description (in CS and EN).', timestamp '2012-10-05 12:33:25.6', 'makub@META',
        timestamp '2013-10-24 14:41:01.5', '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Owner', 1608, 'urn:perun:facility:attribute-def:def:owner', 'owner',
        'urn:perun:facility:attribute-def:def', 'java.util.LinkedHashMap',
        'Organization that funded the hardware (in CS and EN).', timestamp '2012-10-05 12:35:09.6', 'makub@META',
        timestamp '2013-10-24 14:42:51.5', '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Listing priority', 1660, 'urn:perun:facility:attribute-def:def:listingPriority', 'listingPriority',
        'urn:perun:facility:attribute-def:def', 'java.lang.Integer',
        'Priority in lists of facilities, ordering higher numbers first.', timestamp '2012-10-17 13:04:50.4',
        'makub@META', timestamp '2013-10-24 14:43:45.5',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'IP for Flexlm', 1700, 'urn:perun:user:attribute-def:def:IPsForFlexlm', 'IPsForFlexlm',
        'urn:perun:user:attribute-def:def', 'java.util.ArrayList', 'IP adresses for IPtables on Flexlm server.',
        timestamp '2012-10-22 16:16:07.2', 'glory@META', timestamp '2013-10-24 13:38:26.5',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'PBSmon server', 1580, 'urn:perun:facility:attribute-def:def:pbsmonServer', 'pbsmonServer',
        'urn:perun:facility:attribute-def:def', 'java.lang.String', 'Server for PBS monitor.',
        timestamp '2012-10-04 12:59:50.5', 'makub@META', timestamp '2013-10-29 11:25:36.3',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'UID in zcu', 220, 'urn:perun:user:attribute-def:def:uid-namespace:zcu', 'uid-namespace:zcu',
        'urn:perun:user:attribute-def:def', 'java.lang.Integer', 'UID in namespace ''zcu''.',
        timestamp '2011-11-28 12:49:34.2', 'PERUNV3', timestamp '2013-10-24 13:19:18.5',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Is unix group', 145, 'urn:perun:group_resource:attribute-def:def:isUnixGroup', 'isUnixGroup',
        'urn:perun:group_resource:attribute-def:def', 'java.lang.Integer',
        'Group on Resource creates unix group on Facility.', timestamp '2011-11-08 12:03:32.3', 'PERUNV3',
        timestamp '2013-10-25 11:23:46.6', '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Photo', 1601, 'urn:perun:facility:attribute-def:def:photo', 'photo',
        'urn:perun:facility:attribute-def:def', 'java.lang.String', 'URL of Facility hw photo.',
        timestamp '2012-10-05 12:25:17.6', 'makub@META', timestamp '2013-10-24 14:45:15.5',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'GID', 341, 'urn:perun:group_resource:attribute-def:virt:unixGID', 'unixGID',
        'urn:perun:group_resource:attribute-def:virt', 'java.lang.Integer',
        'GID of unix group on Facility to which is this group mapped via Resource. Value is determined automatically from all group''s GID based on Facility''s namespace.',
        timestamp '2011-12-06 13:26:58.3', 'PERUNV3', timestamp '2013-10-25 11:28:37.6',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Default unix GID', 342, 'urn:perun:user_facility:attribute-def:def:defaultUnixGID',
        'defaultUnixGID', 'urn:perun:user_facility:attribute-def:def', 'java.lang.Integer',
        'Unix GID of default user''s Group on Facility.', timestamp '2011-12-06 13:28:37.3', 'PERUNV3',
        timestamp '2013-10-29 11:19:23.3', '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Research group (for VO)', 421, 'urn:perun:member:attribute-def:opt:researchGroup', 'researchGroup',
        'urn:perun:member:attribute-def:opt', 'java.lang.String', 'Membership in research group.',
        timestamp '2011-12-08 10:30:04.5', 'PERUNV3', timestamp '2013-10-25 12:51:22.6',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'DN of certificates', 441, 'urn:perun:user:attribute-def:def:userCertDNs', 'userCertDNs',
        'urn:perun:user:attribute-def:def', 'java.util.LinkedHashMap', 'All DNs of user''s certificates.',
        timestamp '2011-12-13 12:52:09.3', 'PERUNV3', timestamp '2013-10-24 13:08:53.5',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Unix group name', 580, 'urn:perun:resource:attribute-def:virt:unixGroupName', 'unixGroupName',
        'urn:perun:resource:attribute-def:virt', 'java.lang.String',
        'Unix group name used when whole Resource is group on Facility. Value is determined automatically based on Facility namespace.',
        timestamp '2012-01-11 11:51:56.4', 'PERUNV3', timestamp '2013-10-25 10:58:40.6',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Unix group name', 582, 'urn:perun:group_resource:attribute-def:virt:unixGroupName',
        'unixGroupName', 'urn:perun:group_resource:attribute-def:virt', 'java.lang.String',
        'Name of unix group on Facility to which is this group mapped via Resource. Value is determined automatically from all group''s unix group names based on Facility''s namespace.',
        timestamp '2012-01-11 11:52:22.4', 'PERUNV3', timestamp '2013-10-25 11:27:42.6',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Thumbnail', 1602, 'urn:perun:facility:attribute-def:def:thumbnail', 'thumbnail',
        'urn:perun:facility:attribute-def:def', 'java.lang.String', 'URL of facility hw photo''s thumbnail.',
        timestamp '2012-10-05 12:25:45.6', 'makub@META', timestamp '2013-10-24 14:46:24.5',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'CPU', 1603, 'urn:perun:facility:attribute-def:def:cpu', 'cpu',
        'urn:perun:facility:attribute-def:def', 'java.lang.String',
        'Dsecription of CPUs (number, cores, vendor, type, frequency).', timestamp '2012-10-05 12:28:52.6',
        'makub@META', timestamp '2013-10-24 14:47:08.5',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Unix group name in einfra', 588,
        'urn:perun:resource:attribute-def:def:unixGroupName-namespace:einfra', 'unixGroupName-namespace:einfra',
        'urn:perun:resource:attribute-def:def', 'java.lang.String', 'Unix group name in  namespace ''einfra''.',
        timestamp '2012-01-11 12:22:11.4', 'PERUNV3', timestamp '2013-10-25 13:35:22.6',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Unix GID in cerit', 589, 'urn:perun:resource:attribute-def:def:unixGID-namespace:cerit',
        'unixGID-namespace:cerit', 'urn:perun:resource:attribute-def:def', 'java.lang.Integer',
        'Unix GID of group which represented group for whole resource at namespace ''cerit''.',
        timestamp '2012-01-11 12:22:41.4', 'PERUNV3', timestamp '2013-10-25 11:02:29.6',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Unix GID in einfra', 590, 'urn:perun:resource:attribute-def:def:unixGID-namespace:einfra',
        'unixGID-namespace:einfra', 'urn:perun:resource:attribute-def:def', 'java.lang.Integer',
        'Unix GID of group which represented group for whole resource at namespace ''einfra''.',
        timestamp '2012-01-11 12:22:47.4', 'PERUNV3', timestamp '2013-10-25 11:03:48.6',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Unix group name in storage', 662,
        'urn:perun:resource:attribute-def:def:unixGroupName-namespace:storage', 'unixGroupName-namespace:storage',
        'urn:perun:resource:attribute-def:def', 'java.lang.String', 'Unix group name in  namespace ''storage''.',
        timestamp '2012-01-17 11:11:59.3', 'PERUNV3', timestamp '2013-10-25 13:36:54.6',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Unix GID in storage', 661, 'urn:perun:resource:attribute-def:def:unixGID-namespace:storage',
        'unixGID-namespace:storage', 'urn:perun:resource:attribute-def:def', 'java.lang.Integer',
        'Unix GID of group which represented group for whole resource at namespace ''storage''.',
        timestamp '2012-01-17 11:11:08.3', 'PERUNV3', timestamp '2013-10-25 11:05:05.6',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Memory (RAM)', 1604, 'urn:perun:facility:attribute-def:def:memory', 'memory',
        'urn:perun:facility:attribute-def:def', 'java.lang.String', 'Size of installed RAM.',
        timestamp '2012-10-05 12:29:51.6', 'makub@META', timestamp '2013-10-24 14:47:45.5',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'UID in storage', 667, 'urn:perun:user:attribute-def:def:uid-namespace:storage',
        'uid-namespace:storage', 'urn:perun:user:attribute-def:def', 'java.lang.Integer',
        'UID in namespace ''storage''.', timestamp '2012-01-17 11:17:05.3', 'PERUNV3',
        timestamp '2013-10-24 13:18:55.5', '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3411, 'Login in namespace: egi-ui', 780, 'urn:perun:user:attribute-def:def:login-namespace:egi-ui',
        'login-namespace:egi-ui', 'urn:perun:user:attribute-def:def', 'java.lang.String',
        'Logname in namespace ''egi-ui.', timestamp '2012-02-08 14:11:33.4', 'PERUNV3',
        timestamp '2014-09-24 14:39:44.4',
        '/C=CZ/O=Masarykova univerzita/CN=Pavel Zl\xC3\xA1mal/unstructuredName=256627');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Unix GID in cerit', 1323, 'urn:perun:group:attribute-def:def:unixGID-namespace:cerit',
        'unixGID-namespace:cerit', 'urn:perun:group:attribute-def:def', 'java.lang.Integer',
        'Unix GID in namespace ''cerit''.', timestamp '2012-06-28 13:41:27.5', '255739@muni.cz',
        timestamp '2013-10-25 12:36:13.6', '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Unix GID in einfra', 1324, 'urn:perun:group:attribute-def:def:unixGID-namespace:einfra',
        'unixGID-namespace:einfra', 'urn:perun:group:attribute-def:def', 'java.lang.Integer',
        'Unix GID in namespace ''einfra''.', timestamp '2012-06-28 13:41:46.5', '255739@muni.cz',
        timestamp '2013-10-25 12:37:40.6', '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Preferred language', 1365, 'urn:perun:user:attribute-def:def:preferredLanguage',
        'preferredLanguage', 'urn:perun:user:attribute-def:def', 'java.lang.String',
        'Language preferred in communication (notifications).', timestamp '2012-08-17 12:34:08.6', 'michalp@META',
        timestamp '2013-10-23 14:09:39.4', '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Unix GID in egi-ui', 1826, 'urn:perun:group:attribute-def:def:unixGID-namespace:egi-ui',
        'unixGID-namespace:egi-ui', 'urn:perun:group:attribute-def:def', 'java.lang.Integer',
        'Unix GID in namespace ''egi-ui''.', timestamp '2012-11-15 22:49:35.5', 'vlasta@META',
        timestamp '2013-10-25 12:36:42.6', '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Mailing list uses lang variants', 1860,
        'urn:perun:resource:attribute-def:def:mailingListUsesLangVariants', 'mailingListUsesLangVariants',
        'urn:perun:resource:attribute-def:def', 'java.lang.String',
        'Atribute has value ''true'' if separate mailing lists for each language are used.',
        timestamp '2012-11-20 08:56:38.3', 'makub@META', timestamp '2013-12-03 10:14:09.3',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (3255, 3261, 'Cloud platform destination map', 2200,
        'urn:perun:facility:attribute-def:def:cloudPlatformDestinationMap', 'cloudPlatformDestinationMap',
        'urn:perun:facility:attribute-def:def', 'java.util.LinkedHashMap',
        'Maps cloud platform (opennebula, openstack, ..) to destinations.', timestamp '2013-03-11 12:57:08.2',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2013-10-24 14:48:46.5',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (-1, 3261, 'Priority coeficient', 2240, 'urn:perun:user:attribute-def:def:priorityCoeficient',
        'priorityCoeficient', 'urn:perun:user:attribute-def:def', 'java.lang.String',
        'User''s priority coeficient based on reported Publications.', timestamp '2013-04-09 13:47:45.3',
        'perunCabinet', timestamp '2013-10-24 13:34:47.5',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3411, 'Login in namespace: einfra', 146, 'urn:perun:user:attribute-def:def:login-namespace:einfra',
        'login-namespace:einfra', 'urn:perun:user:attribute-def:def', 'java.lang.String',
        'Logname in namespace ''einfra''.', timestamp '2011-11-08 12:09:16.3', 'PERUNV3',
        timestamp '2014-09-24 14:39:44.4',
        '/C=CZ/O=Masarykova univerzita/CN=Pavel Zl\xC3\xA1mal/unstructuredName=256627');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'AFS cell name', 1368, 'urn:perun:facility:attribute-def:def:afsCell', 'afsCell',
        'urn:perun:facility:attribute-def:def', 'java.lang.String', 'AFS cell name for Facility.',
        timestamp '2012-08-17 13:09:40.6', 'michalp@META', timestamp '2013-10-24 14:31:16.5',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Quota FS name', 820, 'urn:perun:resource:attribute-def:def:quotaFileSystem', 'quotaFileSystem',
        'urn:perun:resource:attribute-def:def', 'java.lang.String',
        'Name of file system, which has files/data quota applied by Resource.', timestamp '2012-02-17 11:16:19.6',
        'PERUNV3', timestamp '2013-10-25 10:35:33.6',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Certificates', 440, 'urn:perun:user:attribute-def:def:userCertificates', 'userCertificates',
        'urn:perun:user:attribute-def:def', 'java.util.LinkedHashMap', 'All user''s full certificates.',
        timestamp '2011-12-13 12:49:59.3', 'PERUNV3', timestamp '2013-10-24 12:55:44.5',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Short name of VO', 1720, 'urn:perun:resource:attribute-def:virt:voShortName', 'voShortName',
        'urn:perun:resource:attribute-def:virt', 'java.lang.String',
        'Short name of VO where this resource is assigned.', timestamp '2012-10-30 16:08:25.3', 'glory@META',
        timestamp '2013-10-29 11:29:47.3', '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Description', 1481, 'urn:perun:facility:attribute-def:def:desc', 'desc',
        'urn:perun:facility:attribute-def:def', 'java.util.LinkedHashMap',
        'Short description of facility (in CS and EN).', timestamp '2012-09-13 10:04:33.5', 'makub@META',
        timestamp '2013-10-25 13:51:59.6', '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'GID of system unix group', 1161, 'urn:perun:group_resource:attribute-def:def:systemUnixGID',
        'systemUnixGID', 'urn:perun:group_resource:attribute-def:def', 'java.lang.Integer',
        'GID of unix system group to which is this group mapped.', timestamp '2012-05-04 12:39:00.6', 'michalp@META',
        timestamp '2013-10-25 11:25:26.6', '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Link to user''s manuals', 1340, 'urn:perun:vo:attribute-def:def:userManualsLink',
        'userManualsLink', 'urn:perun:vo:attribute-def:def', 'java.lang.String',
        'Link to user''s manuals of VO members. URL must start with http://.', timestamp '2012-08-15 11:21:55.4',
        'michalp@META', timestamp '2013-10-25 13:06:16.6',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (6701, 3411, 'Quota enabled', 2840, 'urn:perun:facility:attribute-def:def:quotaEnabled', 'quotaEnabled',
        'urn:perun:facility:attribute-def:def', 'java.lang.Integer', 'Attribute says if quota is enabled on facility.',
        timestamp '2014-07-09 11:44:35.4', 'stava@META', timestamp '2014-07-09 12:33:53.4',
        '/C=CZ/O=Masarykova univerzita/CN=Pavel Zl\xC3\xA1mal/unstructuredName=256627');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (6701, 6701, 'Project hard data quota limit', 2841,
        'urn:perun:group_resource:attribute-def:def:projectDataLimit', 'projectDataLimit',
        'urn:perun:group_resource:attribute-def:def', 'java.lang.String',
        'Project hard quota including units (M,G,T, ...), G is default.', timestamp '2014-07-09 11:54:02.4',
        'stava@META', timestamp '2014-07-09 11:55:22.4', 'stava@META');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (-1, 3411, 'Group''s appliaction URL', 2920, 'urn:perun:group:attribute-def:def:applicationURL',
        'applicationURL', 'urn:perun:group:attribute-def:def', 'java.lang.String',
        'Custom link to group''s application form used in e-mail invitations.', timestamp '2014-08-26 15:05:15.3',
        'perunRegistrar', timestamp '2014-09-03 09:02:36.4',
        '/C=CZ/O=Masarykova univerzita/CN=Pavel Zl\xC3\xA1mal/unstructuredName=256627');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (6701, 3411, 'Virtual default unix GID', 2988, 'urn:perun:user_facility:attribute-def:virt:defaultUnixGID',
        'defaultUnixGID', 'urn:perun:user_facility:attribute-def:virt', 'java.lang.Integer',
        'Computed unix group id from user preferrences.', timestamp '2014-09-09 14:31:15.3',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739',
        timestamp '2014-09-24 12:55:24.4',
        '/C=CZ/O=Masarykova univerzita/CN=Pavel Zl\xC3\xA1mal/unstructuredName=256627');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3411, 'Last synchronization timestamp', 2941,
        'urn:perun:group:attribute-def:def:lastSynchronizationTimestamp', 'lastSynchronizationTimestamp',
        'urn:perun:group:attribute-def:def', 'java.lang.String',
        'If group is synchronized, there will be the last timestamp of group synchronization.',
        timestamp '2014-08-27 13:34:18.4', 'PERUNV3', timestamp '2014-08-27 18:59:27.4',
        '/C=CZ/O=Masarykova univerzita/CN=Pavel Zl\xC3\xA1mal/unstructuredName=256627');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, null, 'Last synchronization state', 2960, 'urn:perun:group:attribute-def:def:lastSynchronizationState',
        'lastSynchronizationState', 'urn:perun:group:attribute-def:def', 'java.lang.String',
        'If group is synchronized, there will be information about state of last synchronization.',
        timestamp '2014-08-28 17:49:29.5', 'PERUNV3', timestamp '2014-08-28 17:49:29.5', 'PERUNV3');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Unix group name in egi-ui', 1824,
        'urn:perun:resource:attribute-def:def:unixGroupName-namespace:egi-ui', 'unixGroupName-namespace:egi-ui',
        'urn:perun:resource:attribute-def:def', 'java.lang.String', 'Unix group name in namespace ''egi-ui''.',
        timestamp '2012-11-15 22:46:11.5', 'vlasta@META', timestamp '2013-10-25 13:33:48.6',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Unix group name in egi-ui', 1825,
        'urn:perun:group:attribute-def:def:unixGroupName-namespace:egi-ui', 'unixGroupName-namespace:egi-ui',
        'urn:perun:group:attribute-def:def', 'java.lang.String', 'Unix group name in namespace ''egi-ui''.',
        timestamp '2012-11-15 22:46:51.5', 'vlasta@META', timestamp '2013-10-25 12:33:13.6',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'AFS group''s name', 1980, 'urn:perun:group_resource:attribute-def:def:afsGroupName',
        'afsGroupName', 'urn:perun:group_resource:attribute-def:def', 'java.lang.String', 'Name of group in AFS.',
        timestamp '2013-01-14 13:24:13.2', '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920',
        timestamp '2013-10-29 11:14:16.3', '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Mount point for locale scratch', 2000,
        'urn:perun:facility:attribute-def:def:fsScratchLocalMountPoint', 'fsScratchLocalMountPoint',
        'urn:perun:facility:attribute-def:def', 'java.lang.String', 'Mountpoint where the local scratch is located.',
        timestamp '2013-01-17 15:17:40.5', '/DC=org/DC=terena/DC=tcs/C=CZ/O=CESNET/CN=Michal Prochazka 8497',
        timestamp '2013-10-29 11:19:23.3', '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'To email addresses', 2020, 'urn:perun:group:attribute-def:def:toEmail', 'toEmail',
        'urn:perun:group:attribute-def:def', 'java.util.ArrayList',
        'Email addresses (of Group managers) used as To in mail notifications.', timestamp '2013-01-22 13:45:39.3',
        'perunRegistrar', timestamp '2013-10-25 11:16:52.6',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (3354, 3261, 'Certificates expirations', 2061, 'urn:perun:user:attribute-def:virt:userCertExpirations',
        'userCertExpirations', 'urn:perun:user:attribute-def:virt', 'java.util.LinkedHashMap',
        'Expirations of user''s certificates.', timestamp '2013-01-29 16:23:08.3',
        '/DC=org/DC=terena/DC=tcs/C=CZ/O=CESNET/CN=Michal Prochazka 8497', timestamp '2013-10-24 13:10:57.5',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (6701, 3261, 'DN of preferred certificate', 2120, 'urn:perun:user:attribute-def:def:userPreferredCertDN',
        'userPreferredCertDN', 'urn:perun:user:attribute-def:def', 'java.lang.String',
        'One user-preferred certificate DN without certificate authority.', timestamp '2013-02-13 10:58:56.4',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739',
        timestamp '2013-10-24 13:12:24.5', '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (6701, 3261, 'EPPN', 2300, 'urn:perun:user:attribute-def:virt:eduPersonPrincipalNames',
        'eduPersonPrincipalNames', 'urn:perun:user:attribute-def:virt', 'java.util.ArrayList',
        'Extsource logins from IDP.', timestamp '2013-04-29 10:26:08.2',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739',
        timestamp '2013-10-24 13:13:29.5', '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Shell for passwd_scp', 622, 'urn:perun:user_facility:attribute-def:def:shell_passwd-scp',
        'shell_passwd-scp', 'urn:perun:user_facility:attribute-def:def', 'java.lang.String',
        'User''s shell used for passwd_scp access on Facility.', timestamp '2012-01-12 13:19:30.5', 'PERUNV3',
        timestamp '2013-10-24 13:30:34.5', '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (3197, 3197, 'Big projects', 2580, 'urn:perun:user:attribute-def:opt:bigProjects', 'bigProjects',
        'urn:perun:user:attribute-def:opt', 'java.lang.String', 'ESFRI and other big projects',
        timestamp '2013-12-13 12:44:05.6', '/DC=org/DC=terena/DC=tcs/C=CZ/O=Masaryk University/CN=Martin Kuba 3988',
        timestamp '2013-12-13 12:44:05.6', '/DC=org/DC=terena/DC=tcs/C=CZ/O=Masaryk University/CN=Martin Kuba 3988');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (3255, 3411, 'Login in namespace: cloudidp', 2600, 'urn:perun:user:attribute-def:def:login-namespace:cloudidp',
        'login-namespace:cloudidp', 'urn:perun:user:attribute-def:def', 'java.lang.String',
        'Logname in namespace: ''cloudidp''.', timestamp '2013-12-20 14:38:36.6',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2014-09-24 14:39:44.4',
        '/C=CZ/O=Masarykova univerzita/CN=Pavel Zl\xC3\xA1mal/unstructuredName=256627');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (3255, 3255, 'Computed shell from user''s preferrences', 2620,
        'urn:perun:user_facility:attribute-def:virt:shell', 'shell', 'urn:perun:user_facility:attribute-def:virt',
        'java.lang.String', 'Computed shell from user preferences', timestamp '2014-01-09 11:18:50.5',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2014-04-03 15:55:16.5',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (3255, 3411, 'VO created date', 2820, 'urn:perun:vo:attribute-def:core:createdAt', 'createdAt',
        'urn:perun:vo:attribute-def:core', 'java.lang.String', 'Date when VO was created.',
        timestamp '2014-07-07 15:48:26.2', 'glory@META', timestamp '2014-08-13 14:43:03.4',
        '/C=CZ/O=Masarykova univerzita/CN=Pavel Zl\xC3\xA1mal/unstructuredName=256627');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (3261, 3261, 'Facility displayed name', 2640, 'urn:perun:facility:attribute-def:def:displayName', 'displayName',
        'urn:perun:facility:attribute-def:def', 'java.lang.String', 'Name of facility displayed at PBSMON',
        timestamp '2014-01-21 13:52:12.3', '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378',
        timestamp '2014-01-21 13:52:12.3', '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (6701, 6701, 'Project owner login', 2703, 'urn:perun:group_resource:attribute-def:def:projectOwnerLogin',
        'projectOwnerLogin', 'urn:perun:group_resource:attribute-def:def', 'java.lang.String',
        'Login of user, who is owner of project directory.', timestamp '2014-03-10 16:47:52.2', 'stava@META',
        timestamp '2014-05-12 17:01:19.2',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (6701, 6701, 'Basic default GID', 2980, 'urn:perun:user_facility:attribute-def:def:basicDefaultGID',
        'basicDefaultGID', 'urn:perun:user_facility:attribute-def:def', 'java.lang.Integer',
        'Pregenerated primary unix gid which is used if user doesn''t have other preferencies.',
        timestamp '2014-09-09 13:53:35.3',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739',
        timestamp '2014-09-09 13:53:35.3',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (3255, 3255, 'VO description', 2821, 'urn:perun:vo:attribute-def:def:description', 'description',
        'urn:perun:vo:attribute-def:def', 'java.lang.String', 'Description of the VO',
        timestamp '2014-07-07 15:49:16.2', '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920',
        timestamp '2014-07-07 15:49:16.2', '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (6701, 6701, 'Authoritative Group', 2900, 'urn:perun:group:attribute-def:def:authoritativeGroup',
        'authoritativeGroup', 'urn:perun:group:attribute-def:def', 'java.lang.Integer',
        'If group is authoritative for member. (for synchronization)', timestamp '2014-07-28 13:28:46.2',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739',
        timestamp '2014-07-28 13:28:46.2',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'KERBEROS principals (full)', 1941, 'urn:perun:user:attribute-def:virt:kerberosLogins',
        'kerberosLogins', 'urn:perun:user:attribute-def:virt', 'java.util.ArrayList',
        'Logins in KERBEROS (including realm and kerberos UserExtSources).', timestamp '2012-12-05 21:02:48.4',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739',
        timestamp '2013-10-23 14:28:50.4', '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (3354, 3261, 'UID namespace policy', 2080, 'urn:perun:entityless:attribute-def:def:namespace-uid-policy',
        'namespace-uid-policy', 'urn:perun:entityless:attribute-def:def', 'java.lang.String',
        'Policy for generating new UID number: recycle - use first available UID, increment - add 1 to last used UID (one of them).',
        timestamp '2013-01-30 14:19:41.4', 'michalp@META', timestamp '2013-10-25 13:21:06.6',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Home mount point', 24, 'urn:perun:user_facility:attribute-def:def:homeMountPoint',
        'homeMountPoint', 'urn:perun:user_facility:attribute-def:def', 'java.lang.String',
        'Where user''s home is mounted on Facility.', timestamp '2011-06-01 10:57:48.4', 'PERUNV3',
        timestamp '2013-10-24 13:25:02.5', '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Files limit', 1244, 'urn:perun:member_resource:attribute-def:def:filesLimit', 'filesLimit',
        'urn:perun:member_resource:attribute-def:def', 'java.lang.Integer',
        'Hard quota for max. number of user''s files. Standard is empty, Default files limit is used. Filled value defines exception from Resource''s default.',
        timestamp '2012-05-22 10:43:29.3', 'zora@META', timestamp '2013-10-25 10:46:21.6',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Phone (for VO)', 122, 'urn:perun:member:attribute-def:def:phone', 'phone',
        'urn:perun:member:attribute-def:def', 'java.lang.String', 'Phone number in organization.',
        timestamp '2011-10-24 14:46:44.2', 'PERUNV3', timestamp '2013-10-25 12:49:43.6',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (3255, 3261, 'UID in sagrid', 2501, 'urn:perun:user:attribute-def:def:uid-namespace:sagrid',
        'uid-namespace:sagrid', 'urn:perun:user:attribute-def:def', 'java.lang.Integer', 'UID in namespace ''sagrid''',
        timestamp '2013-11-14 16:05:44.5', 'glory@META', timestamp '2013-12-03 10:18:35.3',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (3411, 3411, 'Notification default language', 2540, 'urn:perun:vo:attribute-def:def:notificationsDefLang',
        'notificationsDefLang', 'urn:perun:vo:attribute-def:def', 'java.lang.String',
        'Default language used for application notifications to VO managers.', timestamp '2013-12-02 13:56:10.2',
        'zlamalp@META', timestamp '2013-12-02 13:56:10.2', 'zlamalp@META');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (6701, 6701, 'Project name', 2702, 'urn:perun:group_resource:attribute-def:def:projectName', 'projectName',
        'urn:perun:group_resource:attribute-def:def', 'java.lang.String',
        'Name of project, directory where the project exists.', timestamp '2014-03-10 16:47:30.2', 'stava@META',
        timestamp '2014-05-12 17:01:19.2',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (3255, 3255, 'Contact role for appDB', 2822, 'urn:perun:resource:attribute-def:def:appDBContactRole',
        'appDBContactRole', 'urn:perun:resource:attribute-def:def', 'java.lang.String', 'Contact role for appDB',
        timestamp '2014-07-07 16:22:09.2', '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920',
        timestamp '2014-07-07 16:22:09.2', '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (6701, 6701, 'Project soft data quota.', 2842, 'urn:perun:group_resource:attribute-def:def:projectDataQuota',
        'projectDataQuota', 'urn:perun:group_resource:attribute-def:def', 'java.lang.String',
        'Project soft quota including units (M, G, T, ...), G is default.', timestamp '2014-07-09 11:54:59.4',
        'stava@META', timestamp '2014-07-09 11:54:59.4', 'stava@META');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (3255, 3411, 'Target user for k5login', 2860, 'urn:perun:resource:attribute-def:def:k5loginTargetUser',
        'k5loginTargetUser', 'urn:perun:resource:attribute-def:def', 'java.lang.String',
        'Target user for .k5login settings.', timestamp '2014-07-15 14:38:34.3', 'glory@META',
        timestamp '2014-08-13 14:42:35.4',
        '/C=CZ/O=Masarykova univerzita/CN=Pavel Zl\xC3\xA1mal/unstructuredName=256627');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (6701, 6701, 'Preferred Unix GroupName for cerit', 2981,
        'urn:perun:user:attribute-def:def:preferredUnixGroupName-namespace:cerit',
        'preferredUnixGroupName-namespace:cerit', 'urn:perun:user:attribute-def:def', 'java.util.ArrayList',
        'User preferred unix group name, ordered by user''s personal preferrences.', timestamp '2014-09-09 13:55:52.3',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739',
        timestamp '2014-09-09 13:55:52.3',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (6701, 6701, 'Preferred Unix Group Name', 2989,
        'urn:perun:user_facility:attribute-def:virt:preferredUnixGroupName', 'preferredUnixGroupName',
        'urn:perun:user_facility:attribute-def:virt', 'java.util.ArrayList',
        'Choosed users preferred unix groupNames for specific facility namespace.', timestamp '2014-09-09 14:32:04.3',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739',
        timestamp '2014-09-09 14:32:04.3',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (3354, 3354, 'Heslo pro službu SAMBA DÚ', 3200, 'urn:perun:user:attribute-def:def:altPasswords:samba-du',
        'altPasswords:samba-du', 'urn:perun:user:attribute-def:def', 'java.util.LinkedHashMap',
        'Heslo pro službu SAMBA DÚ, uloženo na SAMBA serveru.', timestamp '2014-12-04 20:17:13.5',
        '/DC=cz/DC=cesnet-ca/O=Masaryk University/CN=Michal Prochazka', timestamp '2014-12-04 20:17:13.5',
        '/DC=cz/DC=cesnet-ca/O=Masaryk University/CN=Michal Prochazka');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Name of mailing list', 1840, 'urn:perun:resource:attribute-def:def:mailingListName',
        'mailingListName', 'urn:perun:resource:attribute-def:def', 'java.lang.String',
        'Name of the mailing list which is represented by this resource.', timestamp '2012-11-16 12:58:02.6',
        'glory@META', timestamp '2013-12-03 10:14:09.3',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Name', 1, 'urn:perun:facility:attribute-def:core:name', 'name',
        'urn:perun:facility:attribute-def:core', 'java.lang.String', 'Facility name.',
        timestamp '2011-06-01 10:57:48.4', 'PERUNV3', timestamp '2013-10-24 13:42:48.5',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'UID', 262, 'urn:perun:user_facility:attribute-def:virt:UID', 'UID',
        'urn:perun:user_facility:attribute-def:virt', 'java.lang.Integer',
        'User''s UID used at facility. Value is determined automatically from all user''s UIDs by Facility''s namespace.',
        timestamp '2011-12-01 13:58:28.5', 'PERUNV3', timestamp '2013-10-24 13:23:30.5',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Home mount points', 20, 'urn:perun:resource:attribute-def:def:homeMountPoints', 'homeMountPoints',
        'urn:perun:resource:attribute-def:def', 'java.util.ArrayList',
        'Available mount points for users on Resource. Selection is limited by available home mount points on Facility.',
        timestamp '2011-06-01 10:57:48.4', 'PERUNV3', timestamp '2013-10-25 10:32:36.6',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'PBS server', 1080, 'urn:perun:facility:attribute-def:def:pbsServer', 'pbsServer',
        'urn:perun:facility:attribute-def:def', 'java.lang.String', 'PBS server which controls this facility.',
        timestamp '2012-03-23 11:10:21.6', 'glory@META', timestamp '2013-10-24 14:13:56.5',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'SSH host key', 700, 'urn:perun:host:attribute-def:def:sshHostKey', 'sshHostKey',
        'urn:perun:host:attribute-def:def', 'java.lang.String', 'Host''s SSH key.', timestamp '2012-02-07 15:11:45.3',
        'PERUNV3', timestamp '2013-10-24 14:24:52.5',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Email footer', 1680, 'urn:perun:vo:attribute-def:def:mailFooter', 'mailFooter',
        'urn:perun:vo:attribute-def:def', 'java.lang.String',
        'Email footer used in mail notifications by tag {mailFooter}. To edit text whithout loose of formatting, please use notification''s GUI!!',
        timestamp '2012-10-19 10:52:35.6', 'zlamalp@META', timestamp '2013-10-25 13:16:06.6',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (3255, 3411, 'Login in namespace: sagrid', 2500, 'urn:perun:user:attribute-def:def:login-namespace:sagrid',
        'login-namespace:sagrid', 'urn:perun:user:attribute-def:def', 'java.lang.String',
        'Logname in namespace ''sagrid''.', timestamp '2013-11-14 15:58:43.5', 'glory@META',
        timestamp '2014-09-24 14:39:44.4',
        '/C=CZ/O=Masarykova univerzita/CN=Pavel Zl\xC3\xA1mal/unstructuredName=256627');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (3255, 3255, 'User preferred shells', 2680, 'urn:perun:user:attribute-def:def:preferredShells',
        'preferredShells', 'urn:perun:user:attribute-def:def', 'java.util.ArrayList',
        'User preferred shells, ordered by user''s personal preferences', timestamp '2014-02-12 14:23:22.4',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2014-04-03 15:54:05.5',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (6701, 6701, 'Projects base path', 2700, 'urn:perun:resource:attribute-def:def:projectsBasePath',
        'projectsBasePath', 'urn:perun:resource:attribute-def:def', 'java.lang.String',
        'Path to base directory of projects.', timestamp '2014-03-10 16:46:06.2', 'stava@META',
        timestamp '2014-05-12 17:01:19.2',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (-1, -1, 'Registrar URL', 3160, 'urn:perun:vo:attribute-def:def:registrarURL', 'registrarURL',
        'urn:perun:vo:attribute-def:def', 'java.lang.String',
        'Custom URL used in registration notifications (hostname without any parameters like: https://hostname.domain/). If not set, default hostname of Perun instance is used.',
        timestamp '2014-11-10 12:23:17.2', 'perunRegistrar', timestamp '2014-11-10 12:23:17.2', 'perunRegistrar');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (-1, -1, 'Registrar URL', 3161, 'urn:perun:group:attribute-def:def:registrarURL', 'registrarURL',
        'urn:perun:group:attribute-def:def', 'java.lang.String',
        'Custom URL used in registration notifications (hostname without any parameters like: https://hostname.domain/). This value override same VO setting. If not set, default hostname of Perun instance is used.',
        timestamp '2014-11-10 12:23:18.2', 'perunRegistrar', timestamp '2014-11-10 12:23:18.2', 'perunRegistrar');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (3354, 3354, 'Login on DU SAMBA server', 3180, 'urn:perun:user:attribute-def:def:login-namespace:du-samba',
        'login-namespace:du-samba', 'urn:perun:user:attribute-def:def', 'java.lang.String', 'Login on DU SAMBA server',
        timestamp '2014-11-25 12:07:59.3', 'michalp@META', timestamp '2014-11-25 12:07:59.3', 'michalp@META');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'From email address', 1740, 'urn:perun:vo:attribute-def:def:fromEmail', 'fromEmail',
        'urn:perun:vo:attribute-def:def', 'java.lang.String', 'Email address used as From in mail notifications.',
        timestamp '2012-11-05 12:04:18.2', 'perunRegistrar', timestamp '2013-10-25 13:10:45.6',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Available shells', 13, 'urn:perun:facility:attribute-def:def:shells', 'shells',
        'urn:perun:facility:attribute-def:def', 'java.util.ArrayList',
        'Available shells at Facility, which can be selected by users. Selection can be shortened or default shell set per Resource.',
        timestamp '2011-06-01 10:57:48.4', 'PERUNV3', timestamp '2013-10-24 14:02:38.5',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Data limit', 1243, 'urn:perun:member_resource:attribute-def:def:dataLimit', 'dataLimit',
        'urn:perun:member_resource:attribute-def:def', 'java.lang.String',
        'Hard quota for user''s data, including units (M,G,T, ...), G is default. Standard is empty, Default data limit is used. Filled value defines exception from Resource''s default.',
        timestamp '2012-05-22 10:43:11.3', 'zora@META', timestamp '2013-10-25 10:44:44.6',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (3255, 3261, 'Unix group name in sagrid', 2502,
        'urn:perun:resource:attribute-def:def:unixGroupName-namespace:sagrid', 'unixGroupName-namespace:sagrid',
        'urn:perun:resource:attribute-def:def', 'java.lang.String', 'Unix group name in  namespace ''sagrid''',
        timestamp '2013-11-14 16:06:39.5', 'glory@META', timestamp '2013-12-03 10:18:35.3',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (3255, 3261, 'Maling list manager''s e-mail', 2520,
        'urn:perun:resource:attribute-def:def:mailingListManagerMail', 'mailingListManagerMail',
        'urn:perun:resource:attribute-def:def', 'java.lang.String', 'Maling list manager''s e-mail.',
        timestamp '2013-11-15 09:48:31.6', 'glory@META', timestamp '2013-12-03 10:14:09.3',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (3411, 3411, 'Notification default language', 2541, 'urn:perun:group:attribute-def:def:notificationsDefLang',
        'notificationsDefLang', 'urn:perun:group:attribute-def:def', 'java.lang.String',
        'Default language used for application notifications to Group managers.', timestamp '2013-12-02 13:57:27.2',
        'zlamalp@META', timestamp '2013-12-02 13:57:27.2', 'zlamalp@META');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (3255, 3255, 'Unix permissions for scratch', 2720, 'urn:perun:facility:attribute-def:def:scratchDirPermissions',
        'scratchDirPermissions', 'urn:perun:facility:attribute-def:def', 'java.lang.String',
        'Unix permissions, which will be applied when new scratch folder is created.',
        timestamp '2014-05-05 14:54:28.2', '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920',
        timestamp '2014-05-05 14:54:28.2', '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (6701, 6701, 'Preferred Unix GroupName for einfra', 2983,
        'urn:perun:user:attribute-def:def:preferredUnixGroupName-namespace:einfra',
        'preferredUnixGroupName-namespace:einfra', 'urn:perun:user:attribute-def:def', 'java.util.ArrayList',
        'User preferred unix group name, ordered by user''s personal preferrences.', timestamp '2014-09-09 13:57:59.3',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739',
        timestamp '2014-09-09 13:57:59.3',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (6701, 6701, 'Preferred Unix GroupName for sitola', 2984,
        'urn:perun:user:attribute-def:def:preferredUnixGroupName-namespace:sitola',
        'preferredUnixGroupName-namespace:sitola', 'urn:perun:user:attribute-def:def', 'java.util.ArrayList',
        'User preferred unix group name, ordered by user''s personal preferrences.', timestamp '2014-09-09 14:10:16.3',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739',
        timestamp '2014-09-09 14:10:16.3',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'To email addresses', 1743, 'urn:perun:vo:attribute-def:def:toEmail', 'toEmail',
        'urn:perun:vo:attribute-def:def', 'java.util.ArrayList',
        'Email addresses (of VO managers) used as To in mail notifications.', timestamp '2012-11-05 12:16:55.2',
        'perunRegistrar', timestamp '2013-10-25 13:11:35.6',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Unix GID in sitola', 1900, 'urn:perun:group:attribute-def:def:unixGID-namespace:sitola',
        'unixGID-namespace:sitola', 'urn:perun:group:attribute-def:def', 'java.lang.Integer',
        'Unix GID in namespace ''sitola''.', timestamp '2012-11-22 14:35:07.5', 'glory@META',
        timestamp '2013-10-25 12:38:07.6', '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Unix group name in sitola', 1901,
        'urn:perun:group:attribute-def:def:unixGroupName-namespace:sitola', 'unixGroupName-namespace:sitola',
        'urn:perun:group:attribute-def:def', 'java.lang.String', 'Unix group name in namespace ''sitola''.',
        timestamp '2012-11-22 14:35:27.5', 'glory@META', timestamp '2013-10-25 12:34:36.6',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Unix GID in sitola', 1902, 'urn:perun:resource:attribute-def:def:unixGID-namespace:sitola',
        'unixGID-namespace:sitola', 'urn:perun:resource:attribute-def:def', 'java.lang.Integer',
        'Unix GID of group which represented group for whole resource at namespace ''sitola''.',
        timestamp '2012-11-22 14:36:02.5', 'glory@META', timestamp '2013-10-25 11:04:32.6',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'KERBEROS admin principal', 2040, 'urn:perun:user:attribute-def:def:kerberosAdminPrincipal',
        'kerberosAdminPrincipal', 'urn:perun:user:attribute-def:def', 'java.lang.String',
        'KERBEROS admin principal (used for root access).', timestamp '2013-01-28 10:40:16.2',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739',
        timestamp '2013-10-23 14:21:18.4', '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (3255, 3261, 'UID in fedcloud', 2100, 'urn:perun:user:attribute-def:def:uid-namespace:fedcloud',
        'uid-namespace:fedcloud', 'urn:perun:user:attribute-def:def', 'java.lang.Integer',
        'UID in namespace ''fedcloud'' for opennebula_fedcloud.', timestamp '2013-02-04 15:46:24.2',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2013-10-24 13:16:22.5',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (3255, 3261, 'FS volume', 2160, 'urn:perun:resource:attribute-def:def:fsVolume', 'fsVolume',
        'urn:perun:resource:attribute-def:def', 'java.lang.String',
        'Volume on which users''s homes dirs are created. (It''s not mount point!).', timestamp '2013-02-27 13:06:34.4',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2013-10-25 10:51:29.6',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (3354, 3261, 'UID in cesnet', 2221, 'urn:perun:user:attribute-def:def:uid-namespace:cesnet',
        'uid-namespace:cesnet', 'urn:perun:user:attribute-def:def', 'java.lang.Integer', 'UID in namespace ''cesnet''.',
        timestamp '2013-04-02 11:35:06.3', '/DC=org/DC=terena/DC=tcs/C=CZ/O=CESNET/CN=Michal Prochazka 8497',
        timestamp '2013-10-24 13:15:29.5', '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (3354, 3261, 'Apache SSL authz dir', 2260, 'urn:perun:resource:attribute-def:def:apacheSSLAuthzDir',
        'apacheSSLAuthzDir', 'urn:perun:resource:attribute-def:def', 'java.lang.String',
        'Path to the directory, which will contain list of allowed certificates.', timestamp '2013-04-24 13:56:46.4',
        '/DC=org/DC=terena/DC=tcs/C=CZ/O=CESNET/CN=Michal Prochazka 8497', timestamp '2013-10-25 10:50:34.6',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Destination file for passwd_scp', 1120,
        'urn:perun:facility:attribute-def:def:passwdScpDestinationFile', 'passwdScpDestinationFile',
        'urn:perun:facility:attribute-def:def', 'java.lang.String',
        'Path where passwd file (for passwd_scp service) is stored (Typically: /etc/passwd).',
        timestamp '2012-04-17 15:19:10.3', 'glory@META', timestamp '2013-10-24 14:13:11.5',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'AFS partition', 1371, 'urn:perun:facility:attribute-def:def:afsPartition', 'afsPartition',
        'urn:perun:facility:attribute-def:def', 'java.lang.String', 'AFS partition.', timestamp '2012-08-17 14:26:16.6',
        'michalp@META', timestamp '2013-10-24 14:32:27.5',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (3255, 3261, 'Unix GID in sagrid', 2503, 'urn:perun:resource:attribute-def:def:unixGID-namespace:sagrid',
        'unixGID-namespace:sagrid', 'urn:perun:resource:attribute-def:def', 'java.lang.Integer',
        'Unix GID of group which represented group for whole resource at namespace ''sagrid''.',
        timestamp '2013-11-14 16:07:46.5', 'glory@META', timestamp '2013-12-03 10:18:35.3',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (3255, 3261, 'Unix group name in sagrid', 2504,
        'urn:perun:group:attribute-def:def:unixGroupName-namespace:sagrid', 'unixGroupName-namespace:sagrid',
        'urn:perun:group:attribute-def:def', 'java.lang.String', 'Unix group name in  namespace ''sagrid''',
        timestamp '2013-11-14 16:08:42.5', 'glory@META', timestamp '2013-12-03 10:18:35.3',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (3255, 3261, 'Unix GID in sagrid', 2505, 'urn:perun:group:attribute-def:def:unixGID-namespace:sagrid',
        'unixGID-namespace:sagrid', 'urn:perun:group:attribute-def:def', 'java.lang.Integer',
        'Unix GID in namespace ''sagrid''', timestamp '2013-11-14 16:09:11.5', 'glory@META',
        timestamp '2013-12-03 10:18:35.3', '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (6701, 6701, 'Project directory permissions', 2701,
        'urn:perun:group_resource:attribute-def:def:projectDirPermissions', 'projectDirPermissions',
        'urn:perun:group_resource:attribute-def:def', 'java.lang.Integer',
        'Permissions (ACL) to directory, where the project exists. Standard unix file system permissions in numeric format.',
        timestamp '2014-03-10 16:47:01.2', 'stava@META', timestamp '2014-05-12 17:01:19.2',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (3354, 3354, 'Alternative passwords for eInfra', 2780, 'urn:perun:user:attribute-def:def:altPasswords:einfra',
        'altPasswords:einfra', 'urn:perun:user:attribute-def:def', 'java.util.LinkedHashMap',
        'List containing mapping between alt. password description and its ID in external authN system limited to eInfra',
        timestamp '2014-06-20 16:57:10.6', '/C=CZ/O=Masarykova univerzita/CN=Michal Prochazka/unstructuredName=39700',
        timestamp '2014-06-20 16:57:10.6', '/C=CZ/O=Masarykova univerzita/CN=Michal Prochazka/unstructuredName=39700');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (6701, 6701, 'Preferred Unix GroupName for storage', 2985,
        'urn:perun:user:attribute-def:def:preferredUnixGroupName-namespace:storage',
        'preferredUnixGroupName-namespace:storage', 'urn:perun:user:attribute-def:def', 'java.util.ArrayList',
        'User preferred unix group name, ordered by user''s personal preferrences.', timestamp '2014-09-09 14:10:54.3',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739',
        timestamp '2014-09-09 14:10:54.3',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (6701, 6701, 'Preferred Unix Group Name for egi-ui', 3000,
        'urn:perun:user:attribute-def:def:preferredUnixGroupName-namespace:egi-ui',
        'preferredUnixGroupName-namespace:egi-ui', 'urn:perun:user:attribute-def:def', 'java.util.ArrayList',
        'User preferred unix group name, ordered by user''s personal preferrences.', timestamp '2014-09-10 09:52:34.4',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739',
        timestamp '2014-09-10 09:52:34.4',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (3354, 3354, 'Is facility monitored?', 3080, 'urn:perun:facility:attribute-def:opt:metaIsMonitored',
        'metaIsMonitored', 'urn:perun:facility:attribute-def:opt', 'java.lang.String',
        'Is facility monitored by MetaCentrum Nagios', timestamp '2014-10-02 22:26:00.5',
        '/C=CZ/O=Masarykova univerzita/CN=Michal Prochazka/unstructuredName=39700', timestamp '2014-10-02 22:42:11.5',
        '/C=CZ/O=Masarykova univerzita/CN=Michal Prochazka/unstructuredName=39700');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Publications', 1800, 'urn:perun:user:attribute-def:def:publications', 'publications',
        'urn:perun:user:attribute-def:def', 'java.util.LinkedHashMap',
        'Number of publications with acknowledgement per resource provider.', timestamp '2012-11-15 10:08:53.5',
        'michalp@META', timestamp '2013-10-24 13:37:00.5',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'UID in egi-ui', 1821, 'urn:perun:user:attribute-def:def:uid-namespace:egi-ui',
        'uid-namespace:egi-ui', 'urn:perun:user:attribute-def:def', 'java.lang.Integer', 'UID in namespace ''egi-ui''.',
        timestamp '2012-11-15 22:32:11.5', 'vlasta@META', timestamp '2013-10-24 13:15:56.5',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Unix GID in egi-ui', 1822, 'urn:perun:resource:attribute-def:def:unixGID-namespace:egi-ui',
        'unixGID-namespace:egi-ui', 'urn:perun:resource:attribute-def:def', 'java.lang.Integer',
        'Unix GID of group which represented group for whole resource at namespace ''egi-ui''.',
        timestamp '2012-11-15 22:37:23.5', 'vlasta@META', timestamp '2013-10-25 11:03:05.6',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Mailing-list exclusion', 1841, 'urn:perun:member_resource:attribute-def:def:optOutMailingList',
        'optOutMailingList', 'urn:perun:member_resource:attribute-def:def', 'java.lang.String',
        'By default empty. If any value is set - it excludes member from mailing list represented by Resource (with service mailman).',
        timestamp '2012-11-16 13:05:32.6', 'glory@META', timestamp '2013-10-25 10:53:04.6',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'UID in shongo', 1960, 'urn:perun:user:attribute-def:def:uid-namespace:shongo',
        'uid-namespace:shongo', 'urn:perun:user:attribute-def:def', 'java.lang.Integer', 'UID in namespace ''shongo''.',
        timestamp '2012-12-11 15:47:29.3', 'zlamalp@META', timestamp '2013-10-24 13:17:40.5',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (3354, 3261, 'Login in namespace: einfra-services', 2060,
        'urn:perun:user:attribute-def:def:login-namespace:einfra-services', 'login-namespace:einfra-services',
        'urn:perun:user:attribute-def:def', 'java.lang.String', 'Logname in namespace ''einfra-services''.',
        timestamp '2013-01-29 12:01:23.3', '/DC=org/DC=terena/DC=tcs/C=CZ/O=CESNET/CN=Michal Prochazka 8497',
        timestamp '2013-10-23 14:17:26.4', '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (3411, 3261, 'From email address', 2140, 'urn:perun:group:attribute-def:def:fromEmail', 'fromEmail',
        'urn:perun:group:attribute-def:def', 'java.lang.String', 'Email address used as From in mail notifications.',
        timestamp '2013-02-20 15:14:44.4', 'zlamalp@META', timestamp '2013-10-25 11:17:34.6',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (3255, 3261, 'Administrative resource for owencloud', 2420,
        'urn:perun:resource:attribute-def:def:owncloudAdminResource', 'owncloudAdminResource',
        'urn:perun:resource:attribute-def:def', 'java.lang.String',
        'Users asigned to this resource will be propageted to each VO in owncloud (to be able to be set as managers). (values: true/false or nothing).',
        timestamp '2013-07-10 15:40:02.4', '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920',
        timestamp '2013-10-29 11:19:23.3', '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (6701, 6701, 'Used Gids', 2880, 'urn:perun:entityless:attribute-def:def:usedGids', 'usedGids',
        'urn:perun:entityless:attribute-def:def', 'java.util.LinkedHashMap',
        'All used and already depleted gids. Depleted mean gids, which are not used now.',
        timestamp '2014-07-24 13:15:28.5', 'stava@META', timestamp '2014-07-24 13:15:28.5', 'stava@META');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'AFS default users quota', 1375, 'urn:perun:resource:attribute-def:def:afsDefaultUsersQuota',
        'afsDefaultUsersQuota', 'urn:perun:resource:attribute-def:def', 'java.lang.String',
        'Default AFS user''s quota.', timestamp '2012-08-17 14:44:53.6', 'michalp@META',
        timestamp '2013-10-25 10:55:37.6', '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'ID', 41, 'urn:perun:facility:attribute-def:core:id', 'id', 'urn:perun:facility:attribute-def:core',
        'java.lang.Integer', 'Identifier of facility at DB.', timestamp '2011-06-01 10:57:48.4', 'PERUNV3',
        timestamp '2013-10-24 13:43:21.5', '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Name of system unix group', 1162, 'urn:perun:group_resource:attribute-def:def:systemUnixGroupName',
        'systemUnixGroupName', 'urn:perun:group_resource:attribute-def:def', 'java.lang.String',
        'Name of the system unix group to which is this group mapped.', timestamp '2012-05-04 12:39:15.6',
        'michalp@META', timestamp '2013-10-25 11:26:47.6',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Is system unix group', 1160, 'urn:perun:group_resource:attribute-def:def:isSystemUnixGroup',
        'isSystemUnixGroup', 'urn:perun:group_resource:attribute-def:def', 'java.lang.Integer',
        'Group on Resource creates system unix group on Facility.', timestamp '2012-05-04 12:37:12.6', 'michalp@META',
        timestamp '2013-10-25 11:24:31.6', '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Home mount point for passwd_scp', 623,
        'urn:perun:facility:attribute-def:def:homeMountPoint_passwd-scp', 'homeMountPoint_passwd-scp',
        'urn:perun:facility:attribute-def:def', 'java.lang.String', 'Home mount point for service passwd_scp.',
        timestamp '2012-01-12 14:01:06.5', 'PERUNV3', timestamp '2013-10-24 14:09:51.5',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Apache authz file', 1380, 'urn:perun:resource:attribute-def:def:apacheAuthzFile',
        'apacheAuthzFile', 'urn:perun:resource:attribute-def:def', 'java.lang.String',
        'File containing authz entries for Apache.', timestamp '2012-08-24 15:34:29.6', 'michalp@META',
        timestamp '2013-10-25 10:49:42.6', '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Unix group name in cerit', 586,
        'urn:perun:resource:attribute-def:def:unixGroupName-namespace:cerit', 'unixGroupName-namespace:cerit',
        'urn:perun:resource:attribute-def:def', 'java.lang.String', 'Unix group name in namespace ''cerit''.',
        timestamp '2012-01-11 12:22:02.4', 'PERUNV3', timestamp '2013-10-25 13:34:09.6',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (6701, 3261, 'Timezone', 2460, 'urn:perun:user:attribute-def:def:timezone', 'timezone',
        'urn:perun:user:attribute-def:def', 'java.lang.String', 'User''s timezone described by ±[hh] (ISO 8601).',
        timestamp '2013-10-09 09:21:13.4',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739',
        timestamp '2013-10-29 11:28:02.3', '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (3261, 3261, 'Is cluster', 2660, 'urn:perun:facility:attribute-def:def:isCluster', 'isCluster',
        'urn:perun:facility:attribute-def:def', 'java.lang.Integer',
        'Value of attribute 0 or empty means it is not cluster, value 1 means it is cluster',
        timestamp '2014-01-24 11:12:44.6', '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378',
        timestamp '2014-01-24 11:12:44.6', '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Level of assurance', 3461, 'urn:perun:user:attribute-def:virt:loa', 'loa',
        'urn:perun:user:attribute-def:virt', 'java.lang.Integer',
        'The highest value of LoA from all users userExtSources.', timestamp '2012-11-06 14:05:52.3', 'zlamalp@META',
        timestamp '2013-10-25 12:57:17.6', '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'RT queue for VO', 1780, 'urn:perun:vo:attribute-def:def:RTVoQueue', 'RTVoQueue',
        'urn:perun:vo:attribute-def:def', 'java.lang.String', 'Definition of VO''s RT queue.',
        timestamp '2012-11-13 14:19:31.3', 'stava@META', timestamp '2013-10-25 13:18:10.6',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Unix group name in sitola', 1903,
        'urn:perun:resource:attribute-def:def:unixGroupName-namespace:sitola', 'unixGroupName-namespace:sitola',
        'urn:perun:resource:attribute-def:def', 'java.lang.String', 'Unix group name in  namespace ''sitola''.',
        timestamp '2012-11-22 14:36:27.5', 'glory@META', timestamp '2013-10-25 13:36:09.6',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Mailing list allow invalid users', 1880,
        'urn:perun:resource:attribute-def:def:mailingListAllowInvalidUsers', 'mailingListAllowInvalidUsers',
        'urn:perun:resource:attribute-def:def', 'java.lang.String',
        'Atribute has value ''true'' if users with status other than VALID should be included.',
        timestamp '2012-11-20 13:57:53.3', 'makub@META', timestamp '2013-12-03 10:14:09.3',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'UID in sitola', 1904, 'urn:perun:user:attribute-def:def:uid-namespace:sitola',
        'uid-namespace:sitola', 'urn:perun:user:attribute-def:def', 'java.lang.Integer', 'UID in namespace ''sitola''.',
        timestamp '2012-11-22 14:37:08.5', 'glory@META', timestamp '2013-10-24 13:18:06.5',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3411, 'Login in namespace: sitola', 1905, 'urn:perun:user:attribute-def:def:login-namespace:sitola',
        'login-namespace:sitola', 'urn:perun:user:attribute-def:def', 'java.lang.String',
        'Logname in namespace ''sitola''.', timestamp '2012-11-22 14:38:03.5', 'glory@META',
        timestamp '2014-09-24 14:39:45.4',
        '/C=CZ/O=Masarykova univerzita/CN=Pavel Zl\xC3\xA1mal/unstructuredName=256627');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Research group', 1369, 'urn:perun:user:attribute-def:opt:researchGroup', 'researchGroup',
        'urn:perun:user:attribute-def:opt', 'java.lang.String', 'Name of the research group where the user works.',
        timestamp '2012-08-17 13:48:03.6', 'michalp@META', timestamp '2013-10-23 14:12:57.4',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'AFS default realm', 1372, 'urn:perun:resource:attribute-def:def:afsDefaultUsersRealm',
        'afsDefaultUsersRealm', 'urn:perun:resource:attribute-def:def', 'java.lang.String',
        'AFS default user''s realm (lower case).', timestamp '2012-08-17 14:40:13.6', 'michalp@META',
        timestamp '2013-10-25 10:57:22.6', '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (3255, 3261, 'ID of VO', 2480, 'urn:perun:resource:attribute-def:core:voId', 'voId',
        'urn:perun:resource:attribute-def:core', 'java.lang.Integer', 'ID of VO where this resource is assigned',
        timestamp '2013-10-14 14:50:01.2', 'glory@META', timestamp '2013-10-29 11:29:47.3',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Login', 260, 'urn:perun:user_facility:attribute-def:virt:login', 'login',
        'urn:perun:user_facility:attribute-def:virt', 'java.lang.String',
        'User''s logname at facility. Value is determined automatically from all user''s logins by Facility''s namespace.',
        timestamp '2011-12-01 13:26:26.5', 'PERUNV3', timestamp '2013-10-24 13:20:41.5',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (null, 3261, 'Unix GID', 581, 'urn:perun:resource:attribute-def:virt:unixGID', 'unixGID',
        'urn:perun:resource:attribute-def:virt', 'java.lang.Integer',
        'Unix GID used when whole Resource is group on Facility. Value is taken from resource''s GIDs based on Facility namespace.',
        timestamp '2012-01-11 11:52:01.4', 'PERUNV3', timestamp '2013-10-25 10:59:40.6',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (6701, 6701, 'Preferred Unix GroupName for sagrid', 2987,
        'urn:perun:user:attribute-def:def:preferredUnixGroupName-namespace:sagrid',
        'preferredUnixGroupName-namespace:sagrid', 'urn:perun:user:attribute-def:def', 'java.util.ArrayList',
        'User preferred unix group name, ordered by user''s personal preferrences.', timestamp '2014-09-09 14:11:57.3',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739',
        timestamp '2014-09-09 14:11:57.3',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (3255, 3255, 'Unix permissions for scratch_local', 3020,
        'urn:perun:facility:attribute-def:def:scratchLocalDirPermissions', 'scratchLocalDirPermissions',
        'urn:perun:facility:attribute-def:def', 'java.lang.String',
        'Unix permissions, which will be applied when new scratch folder is created.',
        timestamp '2014-09-17 10:17:09.4', '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920',
        timestamp '2014-09-17 10:17:09.4', '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (3354, 3354, 'Is host managed?', 3140, 'urn:perun:host:attribute-def:opt:metaIsManaged', 'metaIsManaged',
        'urn:perun:host:attribute-def:opt', 'java.lang.String', 'Is facility managed by MetaCentrum Puppet',
        timestamp '2014-10-18 07:44:44.7', '/C=CZ/O=CESNET/CN=Michal Prochazka/unstructuredName=8497',
        timestamp '2014-10-18 07:44:44.7', '/C=CZ/O=CESNET/CN=Michal Prochazka/unstructuredName=8497');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (3354, 3354, 'Is facility managed?', 3081, 'urn:perun:facility:attribute-def:opt:metaIsManaged', 'metaIsManaged',
        'urn:perun:facility:attribute-def:opt', 'java.lang.String', 'Is facility managed by MetaCentrum Puppet',
        timestamp '2014-10-02 22:42:00.5', '/C=CZ/O=Masarykova univerzita/CN=Michal Prochazka/unstructuredName=39700',
        timestamp '2014-10-02 22:42:00.5', '/C=CZ/O=Masarykova univerzita/CN=Michal Prochazka/unstructuredName=39700');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (3354, 3354, 'Is host monitored?', 3141, 'urn:perun:host:attribute-def:opt:metaIsMonitored', 'metaIsMonitored',
        'urn:perun:host:attribute-def:opt', 'java.lang.String', 'Is host monitored by MetaCentrum Nagios',
        timestamp '2014-10-18 07:45:39.7', '/C=CZ/O=CESNET/CN=Michal Prochazka/unstructuredName=8497',
        timestamp '2014-10-18 07:45:39.7', '/C=CZ/O=CESNET/CN=Michal Prochazka/unstructuredName=8497');
insert into attr_names (created_by_uid, modified_by_uid, display_name, id, attr_name, friendly_name, namespace, type,
                        dsc, created_at, created_by, modified_at, modified_by)
values (6701, 6701, 'Exclude non-valid users from unix groups`', 3220,
        'urn:perun:facility:attribute-def:def:excludeNonValidUsersFromUnixGroups', 'excludeNonValidUsersFromUnixGroups',
        'urn:perun:facility:attribute-def:def', 'java.lang.String',
        'Exclude non-valid users from unix groups if true is set.', timestamp '2015-01-20 13:13:54.3', 'stava@META',
        timestamp '2015-01-20 13:13:54.3', 'stava@META');
insert into services (created_by_uid, modified_by_uid, id, name, description, delay, recurrence, enabled, script,
                      created_at, created_by, modified_at, modified_by)
values (null, null, 1, 'eduroam_radius', null, 10, 2, true, './eduroam_radius', timestamp '2011-05-30 22:51:00.2',
        'PERUNV3', timestamp '2011-05-30 22:51:00.2', 'PERUNV3');
insert into services (created_by_uid, modified_by_uid, id, name, description, delay, recurrence, enabled, script,
                      created_at, created_by, modified_at, modified_by)
values (null, null, 161, 'passwd_nfs4', null, 10, 2, true, './passwd_nfs4', timestamp '2012-01-10 12:47:59.3',
        'PERUNV3', timestamp '2012-01-10 12:47:59.3', 'PERUNV3');
insert into services (created_by_uid, modified_by_uid, id, name, description, delay, recurrence, enabled, script,
                      created_at, created_by, modified_at, modified_by)
values (null, null, 162, 'group_nfs4', null, 10, 2, true, './group_nfs4', timestamp '2012-01-10 12:48:08.3', 'PERUNV3',
        timestamp '2012-01-10 12:48:08.3', 'PERUNV3');
insert into services (created_by_uid, modified_by_uid, id, name, description, delay, recurrence, enabled, script,
                      created_at, created_by, modified_at, modified_by)
values (null, null, 163, 'passwd_scp', null, 10, 2, true, './passwd_scp', timestamp '2012-01-10 12:51:04.3', 'PERUNV3',
        timestamp '2012-01-10 12:51:04.3', 'PERUNV3');
insert into services (created_by_uid, modified_by_uid, id, name, description, delay, recurrence, enabled, script,
                      created_at, created_by, modified_at, modified_by)
values (null, null, 302, 'afs', null, 10, 2, true, './afs', timestamp '2012-08-17 13:10:44.6', 'michalp@META',
        timestamp '2012-08-17 13:10:44.6', 'michalp@META');
insert into services (created_by_uid, modified_by_uid, id, name, description, delay, recurrence, enabled, script,
                      created_at, created_by, modified_at, modified_by)
values (null, null, 422, 'pbsmon_users', null, 10, 2, true, './pbsmon_users', timestamp '2012-11-15 09:35:35.5',
        'michalp@META', timestamp '2012-11-15 09:35:35.5', 'michalp@META');
insert into services (created_by_uid, modified_by_uid, id, name, description, delay, recurrence, enabled, script,
                      created_at, created_by, modified_at, modified_by)
values (3354, 3354, 762, 'redmine-MU', null, 10, 2, true, './redmine', timestamp '2014-05-23 13:03:36.6',
        '39700@muni.cz', timestamp '2014-05-23 13:03:36.6', '39700@muni.cz');
insert into services (created_by_uid, modified_by_uid, id, name, description, delay, recurrence, enabled, script,
                      created_at, created_by, modified_at, modified_by)
values (3255, 3255, 561, 'docdb', null, 10, 2, true, './docdb', timestamp '2013-05-22 14:53:23.4',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2013-05-22 14:53:23.4',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into services (created_by_uid, modified_by_uid, id, name, description, delay, recurrence, enabled, script,
                      created_at, created_by, modified_at, modified_by)
values (3197, 3197, 581, 'mailman_owners', null, 10, 2, true, './mailman_owners', timestamp '2013-05-23 10:23:50.5',
        '/DC=org/DC=terena/DC=tcs/C=CZ/O=Masaryk University/CN=Martin Kuba 3988', timestamp '2013-05-23 10:23:50.5',
        '/DC=org/DC=terena/DC=tcs/C=CZ/O=Masaryk University/CN=Martin Kuba 3988');
insert into services (created_by_uid, modified_by_uid, id, name, description, delay, recurrence, enabled, script,
                      created_at, created_by, modified_at, modified_by)
values (3354, 3354, 661, 'mailman', null, 10, 2, true, './mailman', timestamp '2013-11-15 21:40:26.6',
        '/C=CZ/O=Masarykova univerzita/CN=Michal Proch\xC3\xA1zka/unstructuredName=39700',
        timestamp '2013-11-15 21:40:26.6',
        '/C=CZ/O=Masarykova univerzita/CN=Michal Proch\xC3\xA1zka/unstructuredName=39700');
insert into services (created_by_uid, modified_by_uid, id, name, description, delay, recurrence, enabled, script,
                      created_at, created_by, modified_at, modified_by)
values (3255, 3255, 822, '_require_login_einfra', null, 10, 2, true, './_require_login_einfra',
        timestamp '2014-09-11 11:38:15.5', '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920',
        timestamp '2014-09-11 11:38:15.5', '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into services (created_by_uid, modified_by_uid, id, name, description, delay, recurrence, enabled, script,
                      created_at, created_by, modified_at, modified_by)
values (null, null, 81, 'k5login_root', null, 10, 2, true, './k5login_root', timestamp '2011-12-07 12:13:48.4',
        'PERUNV3', timestamp '2011-12-07 12:13:48.4', 'PERUNV3');
insert into services (created_by_uid, modified_by_uid, id, name, description, delay, recurrence, enabled, script,
                      created_at, created_by, modified_at, modified_by)
values (null, null, 421, 'mailman_meta', null, 10, 2, true, './mailman_meta', timestamp '2012-11-15 08:43:05.5',
        'michalp@META', timestamp '2012-11-15 08:43:05.5', 'michalp@META');
insert into services (created_by_uid, modified_by_uid, id, name, description, delay, recurrence, enabled, script,
                      created_at, created_by, modified_at, modified_by)
values (null, null, 401, 'sshkeys', null, 10, 2, true, './sshkeys', timestamp '2012-10-30 13:19:00.3', 'michalp@META',
        timestamp '2012-10-30 13:19:00.3', 'michalp@META');
insert into services (created_by_uid, modified_by_uid, id, name, description, delay, recurrence, enabled, script,
                      created_at, created_by, modified_at, modified_by)
values (null, null, 402, 'sshkeys_root', null, 10, 2, true, './sshkeys_root', timestamp '2012-10-30 13:28:30.3',
        'michalp@META', timestamp '2012-10-30 13:28:30.3', 'michalp@META');
insert into services (created_by_uid, modified_by_uid, id, name, description, delay, recurrence, enabled, script,
                      created_at, created_by, modified_at, modified_by)
values (3255, 3255, 601, 'apache_basic_auth', null, 10, 2, true, './apache_basic_auth',
        timestamp '2013-06-18 10:58:06.3', '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920',
        timestamp '2013-06-18 10:58:06.3', '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into services (created_by_uid, modified_by_uid, id, name, description, delay, recurrence, enabled, script,
                      created_at, created_by, modified_at, modified_by)
values (3255, 3255, 641, 'owncloud_vo_mapping', null, 10, 2, true, './owncloud_vo_mapping',
        timestamp '2013-07-09 11:33:21.3', '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920',
        timestamp '2013-07-09 11:33:21.3', '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into services (created_by_uid, modified_by_uid, id, name, description, delay, recurrence, enabled, script,
                      created_at, created_by, modified_at, modified_by)
values (3354, 3354, 701, 'cloudidp', null, 10, 2, true, './cloudidp', timestamp '2013-12-20 10:14:46.6',
        '/C=CZ/O=Masarykova univerzita/CN=Michal Proch\xC3\xA1zka/unstructuredName=39700',
        timestamp '2013-12-20 10:14:46.6',
        '/C=CZ/O=Masarykova univerzita/CN=Michal Proch\xC3\xA1zka/unstructuredName=39700');
insert into services (created_by_uid, modified_by_uid, id, name, description, delay, recurrence, enabled, script,
                      created_at, created_by, modified_at, modified_by)
values (6701, 6701, 741, 'fs_project', null, 10, 2, true, './fs_project', timestamp '2014-03-12 09:35:39.4',
        'stava@META', timestamp '2014-03-12 09:35:39.4', 'stava@META');
insert into services (created_by_uid, modified_by_uid, id, name, description, delay, recurrence, enabled, script,
                      created_at, created_by, modified_at, modified_by)
values (6701, 6701, 841, 'samba_du', null, 10, 2, true, './samba_du', timestamp '2014-10-08 15:51:25.4',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739',
        timestamp '2014-10-08 15:51:25.4',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739');
insert into services (created_by_uid, modified_by_uid, id, name, description, delay, recurrence, enabled, script,
                      created_at, created_by, modified_at, modified_by)
values (null, null, 101, 'fs_scratch', null, 10, 2, true, './fs_scratch', timestamp '2011-12-16 15:20:31.6', 'PERUNV3',
        timestamp '2011-12-16 15:20:31.6', 'PERUNV3');
insert into services (created_by_uid, modified_by_uid, id, name, description, delay, recurrence, enabled, script,
                      created_at, created_by, modified_at, modified_by)
values (null, null, 141, 'pbs_phys_cluster', null, 10, 2, true, './pbs_phys_cluster', timestamp '2011-12-16 20:26:10.6',
        'PERUNV3', timestamp '2011-12-16 20:26:10.6', 'PERUNV3');
insert into services (created_by_uid, modified_by_uid, id, name, description, delay, recurrence, enabled, script,
                      created_at, created_by, modified_at, modified_by)
values (3255, 3255, 462, 'fedcloud_export', null, 10, 2, true, './fedcloud_export', timestamp '2013-03-07 19:02:36.5',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2013-03-07 19:02:36.5',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into services (created_by_uid, modified_by_uid, id, name, description, delay, recurrence, enabled, script,
                      created_at, created_by, modified_at, modified_by)
values (null, null, 281, 'voms', null, 10, 2, true, './voms', timestamp '2012-05-28 17:48:38.2', '39700@muni.cz',
        timestamp '2012-05-28 17:48:38.2', '39700@muni.cz');
insert into services (created_by_uid, modified_by_uid, id, name, description, delay, recurrence, enabled, script,
                      created_at, created_by, modified_at, modified_by)
values (null, null, 341, 'pbsmon_json', null, 10, 2, true, './pbsmon_json', timestamp '2012-09-03 16:31:26.2',
        'glory@META', timestamp '2012-09-03 16:31:26.2', 'glory@META');
insert into services (created_by_uid, modified_by_uid, id, name, description, delay, recurrence, enabled, script,
                      created_at, created_by, modified_at, modified_by)
values (null, null, 361, 'pkinit', null, 10, 2, true, './pkinit', timestamp '2012-10-19 19:43:35.6', 'michalp@META',
        timestamp '2012-10-19 19:43:35.6', 'michalp@META');
insert into services (created_by_uid, modified_by_uid, id, name, description, delay, recurrence, enabled, script,
                      created_at, created_by, modified_at, modified_by)
values (3255, 3255, 481, 'pbs_publication_fairshare', null, 10, 2, true, './pbs_publication_fairshare',
        timestamp '2013-04-08 17:17:53.2', '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920',
        timestamp '2013-04-08 17:17:53.2', '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into services (created_by_uid, modified_by_uid, id, name, description, delay, recurrence, enabled, script,
                      created_at, created_by, modified_at, modified_by)
values (3255, 3255, 721, 'afs_group', null, 10, 2, true, './afs_group', timestamp '2014-03-11 23:42:00.3',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2014-03-11 23:42:00.3',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into services (created_by_uid, modified_by_uid, id, name, description, delay, recurrence, enabled, script,
                      created_at, created_by, modified_at, modified_by)
values (null, null, 21, 'passwd', null, 10, 2, true, './passwd', timestamp '2011-09-23 13:35:51.6', 'PERUNV3',
        timestamp '2011-09-23 13:35:51.6', 'PERUNV3');
insert into services (created_by_uid, modified_by_uid, id, name, description, delay, recurrence, enabled, script,
                      created_at, created_by, modified_at, modified_by)
values (null, null, 61, 'pbs_pre', null, 10, 2, true, './pbs_pre', timestamp '2011-11-30 11:10:35.4', 'PERUNV3',
        timestamp '2011-11-30 11:10:35.4', 'PERUNV3');
insert into services (created_by_uid, modified_by_uid, id, name, description, delay, recurrence, enabled, script,
                      created_at, created_by, modified_at, modified_by)
values (null, null, 121, 'fs_home', null, 10, 2, true, './fs_home', timestamp '2011-12-16 19:56:22.6', 'PERUNV3',
        timestamp '2011-12-16 19:56:22.6', 'PERUNV3');
insert into services (created_by_uid, modified_by_uid, id, name, description, delay, recurrence, enabled, script,
                      created_at, created_by, modified_at, modified_by)
values (null, null, 261, 'users_export', null, 10, 2, true, './users_export', timestamp '2012-04-24 14:56:04.3',
        'michalp@META', timestamp '2012-04-24 14:56:04.3', 'michalp@META');
insert into services (created_by_uid, modified_by_uid, id, name, description, delay, recurrence, enabled, script,
                      created_at, created_by, modified_at, modified_by)
values (null, null, 321, 'apache_ssl', null, 10, 2, true, './apache_ssl', timestamp '2012-08-24 15:29:44.6',
        'michalp@META', timestamp '2012-08-24 15:29:44.6', 'michalp@META');
insert into services (created_by_uid, modified_by_uid, id, name, description, delay, recurrence, enabled, script,
                      created_at, created_by, modified_at, modified_by)
values (3255, 3255, 781, 'appDB', null, 10, 2, true, './appDB', timestamp '2014-06-30 14:41:27.2',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2014-06-30 14:41:27.2',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into services (created_by_uid, modified_by_uid, id, name, description, delay, recurrence, enabled, script,
                      created_at, created_by, modified_at, modified_by)
values (3255, 3255, 801, 'k5login_generic', null, 10, 2, true, './k5login_generic', timestamp '2014-07-15 14:34:49.3',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2014-07-15 14:34:49.3',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into services (created_by_uid, modified_by_uid, id, name, description, delay, recurrence, enabled, script,
                      created_at, created_by, modified_at, modified_by)
values (null, null, 41, 'k5login', null, 10, 2, true, './k5login', timestamp '2011-11-08 11:34:50.3', 'PERUNV3',
        timestamp '2011-11-08 11:34:50.3', 'PERUNV3');
insert into services (created_by_uid, modified_by_uid, id, name, description, delay, recurrence, enabled, script,
                      created_at, created_by, modified_at, modified_by)
values (null, null, 42, 'group', null, 10, 2, true, './group', timestamp '2011-11-08 11:34:54.3', 'PERUNV3',
        timestamp '2011-11-08 11:34:54.3', 'PERUNV3');
insert into services (created_by_uid, modified_by_uid, id, name, description, delay, recurrence, enabled, script,
                      created_at, created_by, modified_at, modified_by)
values (null, null, 43, 'mailaliases', null, 10, 2, true, './mailaliases', timestamp '2011-11-08 11:34:59.3', 'PERUNV3',
        timestamp '2011-11-08 11:34:59.3', 'PERUNV3');
insert into services (created_by_uid, modified_by_uid, id, name, description, delay, recurrence, enabled, script,
                      created_at, created_by, modified_at, modified_by)
values (null, null, 221, 'fs_scratch_local', null, 10, 2, true, './fs_scratch_local', timestamp '2012-03-30 09:34:08.6',
        'michalp@META', timestamp '2012-03-30 09:34:08.6', 'michalp@META');
insert into services (created_by_uid, modified_by_uid, id, name, description, delay, recurrence, enabled, script,
                      created_at, created_by, modified_at, modified_by)
values (3255, 3255, 541, 'du_users_export', null, 10, 2, true, './du_users_export', timestamp '2013-05-16 15:05:16.5',
        'glory@META', timestamp '2013-05-16 15:05:16.5', 'glory@META');
insert into services (created_by_uid, modified_by_uid, id, name, description, delay, recurrence, enabled, script,
                      created_at, created_by, modified_at, modified_by)
values (null, null, 381, 'flexlm_iptables', null, 10, 2, true, './flexlm_iptables', timestamp '2012-10-22 16:17:03.2',
        'glory@META', timestamp '2012-10-22 16:17:03.2', 'glory@META');
insert into services (created_by_uid, modified_by_uid, id, name, description, delay, recurrence, enabled, script,
                      created_at, created_by, modified_at, modified_by)
values (3354, 3354, 501, 'openvpn', null, 10, 2, true, './openvpn', timestamp '2013-04-10 16:32:46.4',
        '/DC=org/DC=terena/DC=tcs/C=CZ/O=CESNET/CN=Michal Prochazka 8497', timestamp '2013-04-10 16:32:46.4',
        '/DC=org/DC=terena/DC=tcs/C=CZ/O=CESNET/CN=Michal Prochazka 8497');
insert into services (created_by_uid, modified_by_uid, id, name, description, delay, recurrence, enabled, script,
                      created_at, created_by, modified_at, modified_by)
values (3354, 3354, 861, 'labkey', null, 10, 2, true, './labkey', timestamp '2014-11-24 12:38:08.2',
        '/C=CZ/O=Masarykova univerzita/CN=Michal Prochazka/unstructuredName=39700', timestamp '2014-11-24 12:38:08.2',
        '/C=CZ/O=Masarykova univerzita/CN=Michal Prochazka/unstructuredName=39700');
insert into services (created_by_uid, modified_by_uid, id, name, description, delay, recurrence, enabled, script,
                      created_at, created_by, modified_at, modified_by)
values (3354, 3354, 621, 'gridmap', null, 10, 2, true, './gridmap', timestamp '2013-06-20 10:43:18.5',
        '/DC=org/DC=terena/DC=tcs/C=CZ/O=CESNET/CN=Michal Prochazka 8497', timestamp '2013-06-20 10:43:18.5',
        '/DC=org/DC=terena/DC=tcs/C=CZ/O=CESNET/CN=Michal Prochazka 8497');
insert into services (created_by_uid, modified_by_uid, id, name, description, delay, recurrence, enabled, script,
                      created_at, created_by, modified_at, modified_by)
values (3255, 3255, 681, 'metacloud_export', null, 10, 2, true, './metacloud_export', timestamp '2013-12-10 18:12:16.3',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2013-12-10 18:12:16.3',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 541, 582, timestamp '2013-10-09 15:07:15.4',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2013-10-09 15:07:15.4',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 42, 341, timestamp '2011-12-06 13:35:06.3', 'PERUNV3', timestamp '2011-12-06 13:35:06.3',
        'PERUNV3');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 21, 361, timestamp '2011-12-07 10:47:41.4', 'PERUNV3', timestamp '2011-12-07 10:47:41.4',
        'PERUNV3');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 42, 260, timestamp '2011-12-06 13:36:04.3', 'PERUNV3', timestamp '2011-12-06 13:36:04.3',
        'PERUNV3');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 42, 261, timestamp '2011-12-06 13:42:32.3', 'PERUNV3', timestamp '2011-12-06 13:42:32.3',
        'PERUNV3');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 21, 360, timestamp '2011-12-07 10:47:33.4', 'PERUNV3', timestamp '2011-12-07 10:47:33.4',
        'PERUNV3');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 61, 1080, timestamp '2012-03-23 11:11:30.6', 'glory@META', timestamp '2012-03-23 11:11:30.6',
        'glory@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 781, 2300, timestamp '2014-06-30 14:42:27.2',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2014-06-30 14:42:27.2',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 61, 880, timestamp '2012-03-23 11:13:12.6', 'glory@META', timestamp '2012-03-23 11:13:12.6',
        'glory@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 161, 262, timestamp '2012-01-10 12:55:08.3', 'PERUNV3', timestamp '2012-01-10 12:55:08.3',
        'PERUNV3');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 161, 342, timestamp '2012-01-10 12:55:10.3', 'PERUNV3', timestamp '2012-01-10 12:55:10.3',
        'PERUNV3');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 161, 360, timestamp '2012-01-10 12:55:46.3', 'PERUNV3', timestamp '2012-01-10 12:55:46.3',
        'PERUNV3');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 161, 361, timestamp '2012-01-10 12:55:46.3', 'PERUNV3', timestamp '2012-01-10 12:55:46.3',
        'PERUNV3');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 21, 2340, timestamp '2013-08-07 11:05:16.4',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2013-08-07 11:05:16.4',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 481, 262, timestamp '2013-04-16 13:00:25.3',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2013-04-16 13:00:25.3',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 162, 341, timestamp '2012-01-10 12:59:11.3', 'PERUNV3', timestamp '2012-01-10 12:59:11.3',
        'PERUNV3');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 481, 261, timestamp '2013-04-16 13:00:43.3',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2013-04-16 13:00:43.3',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3354, 3354, 321, 2400, timestamp '2013-07-11 09:48:37.5',
        '/DC=org/DC=terena/DC=tcs/C=CZ/O=CESNET/CN=Michal Prochazka 8497', timestamp '2013-07-11 09:48:37.5',
        '/DC=org/DC=terena/DC=tcs/C=CZ/O=CESNET/CN=Michal Prochazka 8497');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 421, 1361, timestamp '2013-06-25 14:46:33.3', 'glory@META', timestamp '2013-06-25 14:46:33.3',
        'glory@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 21, 261, timestamp '2012-01-10 13:01:45.3', 'PERUNV3', timestamp '2012-01-10 13:01:45.3',
        'PERUNV3');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 121, 2340, timestamp '2013-08-02 17:11:08.6',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2013-08-02 17:11:08.6',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 341, 400, timestamp '2012-09-12 14:58:47.4', 'michalp@META', timestamp '2012-09-12 14:58:47.4',
        'michalp@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 341, 1606, timestamp '2012-10-05 13:04:28.6', 'makub@META', timestamp '2012-10-05 13:04:28.6',
        'makub@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 361, 800, timestamp '2012-10-19 19:44:36.6', 'michalp@META', timestamp '2012-10-19 19:44:36.6',
        'michalp@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 422, 1363, timestamp '2012-11-15 09:58:01.5', 'michalp@META', timestamp '2012-11-15 09:58:01.5',
        'michalp@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 101, 280, timestamp '2013-09-02 09:43:22.2',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2013-09-02 09:43:22.2',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 422, 261, timestamp '2012-11-15 10:10:50.5', 'michalp@META', timestamp '2012-11-15 10:10:50.5',
        'michalp@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 421, 1840, timestamp '2012-11-16 12:59:38.6', 'glory@META', timestamp '2012-11-16 12:59:38.6',
        'glory@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 421, 1860, timestamp '2012-11-20 08:57:16.3', 'makub@META', timestamp '2012-11-20 08:57:16.3',
        'makub@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 221, 2000, timestamp '2013-01-17 15:18:02.5',
        '/DC=org/DC=terena/DC=tcs/C=CZ/O=CESNET/CN=Michal Prochazka 8497', timestamp '2013-01-17 15:18:02.5',
        '/DC=org/DC=terena/DC=tcs/C=CZ/O=CESNET/CN=Michal Prochazka 8497');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 163, 24, timestamp '2013-01-10 14:20:37.5', 'glory@META', timestamp '2013-01-10 14:20:37.5',
        'glory@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 21, 1720, timestamp '2013-02-06 12:37:32.4', 'glory@META', timestamp '2013-02-06 12:37:32.4',
        'glory@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 541, 1243, timestamp '2013-05-16 15:09:12.5', 'glory@META', timestamp '2013-05-16 15:09:12.5',
        'glory@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 221, 2340, timestamp '2013-09-02 09:44:38.2',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2013-09-02 09:44:38.2',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3197, 3197, 581, 880, timestamp '2013-05-23 10:27:06.5',
        '/DC=org/DC=terena/DC=tcs/C=CZ/O=Masaryk University/CN=Martin Kuba 3988', timestamp '2013-05-23 10:27:06.5',
        '/DC=org/DC=terena/DC=tcs/C=CZ/O=Masaryk University/CN=Martin Kuba 3988');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 601, 260, timestamp '2013-06-18 10:58:54.3', 'glory@META', timestamp '2013-06-18 10:58:54.3',
        'glory@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 161, 280, timestamp '2013-09-02 09:45:22.2',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2013-09-02 09:45:22.2',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3197, 3197, 561, 81, timestamp '2013-05-24 11:21:49.6',
        '/DC=org/DC=terena/DC=tcs/C=CZ/O=Masaryk University/CN=Martin Kuba 3988', timestamp '2013-05-24 11:21:49.6',
        '/DC=org/DC=terena/DC=tcs/C=CZ/O=Masaryk University/CN=Martin Kuba 3988');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 422, 1361, timestamp '2013-06-25 14:46:36.3', 'glory@META', timestamp '2013-06-25 14:46:36.3',
        'glory@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 581, 1361, timestamp '2013-06-25 14:46:35.3', 'glory@META', timestamp '2013-06-25 14:46:35.3',
        'glory@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 281, 1361, timestamp '2013-06-25 14:46:38.3', 'glory@META', timestamp '2013-06-25 14:46:38.3',
        'glory@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 163, 2340, timestamp '2013-09-02 09:45:46.2',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2013-09-02 09:45:46.2',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 541, 280, timestamp '2013-10-09 15:07:15.4',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2013-10-09 15:07:15.4',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 541, 341, timestamp '2013-10-09 15:07:15.4',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2013-10-09 15:07:15.4',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 541, 38, timestamp '2013-10-11 11:59:49.6',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2013-10-11 11:59:49.6',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 681, 580, timestamp '2013-12-10 18:13:00.3',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2013-12-10 18:13:00.3',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3354, 3354, 701, 51, timestamp '2013-12-20 11:23:18.6',
        '/C=CZ/O=Masarykova univerzita/CN=Michal Proch\xC3\xA1zka/unstructuredName=39700',
        timestamp '2013-12-20 11:23:18.6',
        '/C=CZ/O=Masarykova univerzita/CN=Michal Proch\xC3\xA1zka/unstructuredName=39700');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (6701, 6701, 741, 2703, timestamp '2014-03-12 09:39:57.4', 'stava@META', timestamp '2014-03-12 09:39:57.4',
        'stava@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (6701, 6701, 741, 582, timestamp '2014-03-12 09:39:57.4', 'stava@META', timestamp '2014-03-12 09:39:57.4',
        'stava@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (6701, 6701, 741, 2701, timestamp '2014-03-12 09:39:57.4', 'stava@META', timestamp '2014-03-12 09:39:57.4',
        'stava@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (6701, 6701, 741, 2700, timestamp '2014-03-12 09:39:57.4', 'stava@META', timestamp '2014-03-12 09:39:57.4',
        'stava@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3354, 3354, 762, 50, timestamp '2014-05-23 13:04:02.6', '39700@muni.cz', timestamp '2014-05-23 13:04:02.6',
        '39700@muni.cz');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3354, 3354, 762, 1361, timestamp '2014-05-23 13:04:30.6', '39700@muni.cz', timestamp '2014-05-23 13:04:30.6',
        '39700@muni.cz');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 781, 1361, timestamp '2014-06-30 14:42:11.2',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2014-06-30 14:42:11.2',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 781, 2400, timestamp '2014-06-30 14:42:20.2',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2014-06-30 14:42:20.2',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 801, 880, timestamp '2014-07-15 14:35:39.3',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2014-07-15 14:35:39.3',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 801, 2860, timestamp '2014-07-15 14:39:01.3',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2014-07-15 14:39:01.3',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 801, 1941, timestamp '2014-08-12 13:17:42.3',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2014-08-12 13:17:42.3',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 43, 261, timestamp '2011-12-07 11:17:51.4', 'PERUNV3', timestamp '2011-12-07 11:17:51.4',
        'PERUNV3');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 21, 50, timestamp '2011-11-09 11:23:22.4', 'PERUNV3', timestamp '2011-11-09 11:23:22.4', 'PERUNV3');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 21, 51, timestamp '2011-11-09 11:23:34.4', 'PERUNV3', timestamp '2011-11-09 11:23:34.4', 'PERUNV3');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 21, 260, timestamp '2011-12-06 19:59:42.3', 'PERUNV3', timestamp '2011-12-06 19:59:42.3',
        'PERUNV3');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 121, 500, timestamp '2011-12-19 22:12:27.2', 'PERUNV3', timestamp '2011-12-19 22:12:27.2',
        'PERUNV3');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 41, 260, timestamp '2011-12-07 11:16:31.4', 'PERUNV3', timestamp '2011-12-07 11:16:31.4',
        'PERUNV3');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 21, 342, timestamp '2011-12-06 19:53:48.3', 'PERUNV3', timestamp '2011-12-06 19:53:48.3',
        'PERUNV3');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 21, 262, timestamp '2011-12-06 19:59:50.3', 'PERUNV3', timestamp '2011-12-06 19:59:50.3',
        'PERUNV3');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 41, 261, timestamp '2011-12-07 11:16:41.4', 'PERUNV3', timestamp '2011-12-07 11:16:41.4',
        'PERUNV3');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 43, 260, timestamp '2011-12-07 11:17:49.4', 'PERUNV3', timestamp '2011-12-07 11:17:49.4',
        'PERUNV3');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 121, 880, timestamp '2012-07-18 12:29:59.4', 'glory@META', timestamp '2012-07-18 12:29:59.4',
        'glory@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 121, 761, timestamp '2012-02-08 10:28:56.4', 'PERUNV3', timestamp '2012-02-08 10:28:56.4',
        'PERUNV3');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 101, 300, timestamp '2011-12-20 15:08:44.3', 'PERUNV3', timestamp '2011-12-20 15:08:44.3',
        'PERUNV3');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 101, 260, timestamp '2011-12-20 15:10:47.3', 'PERUNV3', timestamp '2011-12-20 15:10:47.3',
        'PERUNV3');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 163, 23, timestamp '2013-02-27 18:22:16.4', 'glory@META', timestamp '2013-02-27 18:22:16.4',
        'glory@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 121, 262, timestamp '2011-12-21 15:33:50.4', 'PERUNV3', timestamp '2011-12-21 15:33:50.4',
        'PERUNV3');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 121, 261, timestamp '2011-12-21 14:52:37.4', 'PERUNV3', timestamp '2011-12-21 14:52:37.4',
        'PERUNV3');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 121, 520, timestamp '2011-12-21 14:56:06.4', 'PERUNV3', timestamp '2011-12-21 14:56:06.4',
        'PERUNV3');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 121, 521, timestamp '2011-12-21 14:56:09.4', 'PERUNV3', timestamp '2011-12-21 14:56:09.4',
        'PERUNV3');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 121, 263, timestamp '2011-12-21 15:33:50.4', 'PERUNV3', timestamp '2011-12-21 15:33:50.4',
        'PERUNV3');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 121, 360, timestamp '2011-12-21 15:33:50.4', 'PERUNV3', timestamp '2011-12-21 15:33:50.4',
        'PERUNV3');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 121, 361, timestamp '2011-12-21 15:33:51.4', 'PERUNV3', timestamp '2011-12-21 15:33:51.4',
        'PERUNV3');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 121, 760, timestamp '2012-02-08 10:28:54.4', 'PERUNV3', timestamp '2012-02-08 10:28:54.4',
        'PERUNV3');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 161, 2340, timestamp '2013-09-02 09:45:22.2',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2013-09-02 09:45:22.2',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 163, 51, timestamp '2012-01-10 13:19:29.3', 'PERUNV3', timestamp '2012-01-10 13:19:29.3',
        'PERUNV3');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 163, 260, timestamp '2012-01-10 13:19:29.3', 'PERUNV3', timestamp '2012-01-10 13:19:29.3',
        'PERUNV3');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 163, 261, timestamp '2012-01-10 13:19:30.3', 'PERUNV3', timestamp '2012-01-10 13:19:30.3',
        'PERUNV3');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 161, 800, timestamp '2012-02-15 15:00:47.4', 'glory@META', timestamp '2012-02-15 15:00:47.4',
        'glory@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 162, 800, timestamp '2012-02-15 15:00:50.4', 'glory@META', timestamp '2012-02-15 15:00:50.4',
        'glory@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 141, 1080, timestamp '2012-03-23 11:13:12.6', 'glory@META', timestamp '2012-03-23 11:13:12.6',
        'glory@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 221, 262, timestamp '2012-03-30 09:39:28.6', 'michalp@META', timestamp '2012-03-30 09:39:28.6',
        'michalp@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 401, 260, timestamp '2012-10-30 13:24:47.3', 'michalp@META', timestamp '2012-10-30 13:24:47.3',
        'michalp@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 341, 1660, timestamp '2012-10-17 13:06:18.4', 'makub@META', timestamp '2012-10-17 13:06:18.4',
        'makub@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 221, 342, timestamp '2012-03-30 09:39:30.6', 'michalp@META', timestamp '2012-03-30 09:39:30.6',
        'michalp@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 541, 263, timestamp '2013-05-16 15:08:21.5', 'glory@META', timestamp '2013-05-16 15:08:21.5',
        'glory@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 221, 260, timestamp '2012-03-30 09:39:30.6', 'michalp@META', timestamp '2012-03-30 09:39:30.6',
        'michalp@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 221, 301, timestamp '2012-03-30 09:39:31.6', 'michalp@META', timestamp '2012-03-30 09:39:31.6',
        'michalp@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 221, 300, timestamp '2012-03-30 09:39:31.6', 'michalp@META', timestamp '2012-03-30 09:39:31.6',
        'michalp@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 341, 1424, timestamp '2012-09-03 16:33:52.2', 'glory@META', timestamp '2012-09-03 16:33:52.2',
        'glory@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 221, 263, timestamp '2012-03-30 09:39:33.6', 'michalp@META', timestamp '2012-03-30 09:39:33.6',
        'michalp@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 221, 581, timestamp '2012-03-30 09:39:33.6', 'michalp@META', timestamp '2012-03-30 09:39:33.6',
        'michalp@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3354, 3354, 361, 2400, timestamp '2013-09-06 10:37:08.6',
        '/DC=org/DC=terena/DC=tcs/C=CZ/O=CESNET/CN=Michal Prochazka 8497', timestamp '2013-09-06 10:37:08.6',
        '/DC=org/DC=terena/DC=tcs/C=CZ/O=CESNET/CN=Michal Prochazka 8497');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 221, 580, timestamp '2012-03-30 09:39:34.6', 'michalp@META', timestamp '2012-03-30 09:39:34.6',
        'michalp@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 341, 1481, timestamp '2012-09-13 10:06:22.5', 'makub@META', timestamp '2012-09-13 10:06:22.5',
        'makub@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 163, 280, timestamp '2013-09-02 09:45:46.2',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2013-09-02 09:45:46.2',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 401, 1520, timestamp '2012-10-30 13:19:25.3', 'michalp@META', timestamp '2012-10-30 13:19:25.3',
        'michalp@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 402, 1520, timestamp '2012-10-30 13:28:46.3', 'michalp@META', timestamp '2012-10-30 13:28:46.3',
        'michalp@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 341, 121, timestamp '2012-09-03 16:33:55.2', 'glory@META', timestamp '2012-09-03 16:33:55.2',
        'glory@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 341, 1425, timestamp '2012-09-03 16:33:56.2', 'glory@META', timestamp '2012-09-03 16:33:56.2',
        'glory@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 101, 880, timestamp '2012-11-08 00:18:42.5', 'glory@META', timestamp '2012-11-08 00:18:42.5',
        'glory@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3354, 3354, 281, 2400, timestamp '2013-07-09 13:47:42.3',
        '/DC=org/DC=terena/DC=tcs/C=CZ/O=CESNET/CN=Michal Prochazka 8497', timestamp '2013-07-09 13:47:42.3',
        '/DC=org/DC=terena/DC=tcs/C=CZ/O=CESNET/CN=Michal Prochazka 8497');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 421, 1140, timestamp '2012-11-15 08:43:46.5', 'michalp@META', timestamp '2012-11-15 08:43:46.5',
        'michalp@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 421, 880, timestamp '2012-11-15 08:56:31.5', 'michalp@META', timestamp '2012-11-15 08:56:31.5',
        'michalp@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 421, 1365, timestamp '2012-11-15 09:24:02.5', 'michalp@META', timestamp '2012-11-15 09:24:02.5',
        'michalp@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 422, 1140, timestamp '2012-11-15 09:58:01.5', 'michalp@META', timestamp '2012-11-15 09:58:01.5',
        'michalp@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 422, 1800, timestamp '2012-11-15 10:09:16.5', 'michalp@META', timestamp '2012-11-15 10:09:16.5',
        'michalp@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 422, 1300, timestamp '2012-12-03 11:04:35.2', 'makub@meta.cesnet.cz',
        timestamp '2012-12-03 11:04:35.2', 'makub@meta.cesnet.cz');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 422, 860, timestamp '2012-12-03 11:04:35.2', 'makub@meta.cesnet.cz',
        timestamp '2012-12-03 11:04:35.2', 'makub@meta.cesnet.cz');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 42, 80, timestamp '2012-12-19 15:03:35.4', 'glory@META', timestamp '2012-12-19 15:03:35.4',
        'glory@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 421, 860, timestamp '2012-12-21 11:07:32.6',
        '/DC=org/DC=terena/DC=tcs/C=CZ/O=Masaryk University/CN=Martin Kuba 3988', timestamp '2012-12-21 11:07:32.6',
        '/DC=org/DC=terena/DC=tcs/C=CZ/O=Masaryk University/CN=Martin Kuba 3988');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 162, 1161, timestamp '2013-01-15 13:49:49.3', 'glory@META', timestamp '2013-01-15 13:49:49.3',
        'glory@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 162, 1162, timestamp '2013-01-15 13:49:52.3', 'glory@META', timestamp '2013-01-15 13:49:52.3',
        'glory@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 163, 20, timestamp '2013-02-27 18:22:13.4', 'glory@META', timestamp '2013-02-27 18:22:13.4',
        'glory@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 541, 360, timestamp '2013-05-16 15:08:30.5', 'glory@META', timestamp '2013-05-16 15:08:30.5',
        'glory@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 541, 1241, timestamp '2013-05-16 15:09:17.5', 'glory@META', timestamp '2013-05-16 15:09:17.5',
        'glory@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 541, 1244, timestamp '2013-05-16 15:09:27.5', 'glory@META', timestamp '2013-05-16 15:09:27.5',
        'glory@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 541, 1242, timestamp '2013-05-16 15:09:35.5', 'glory@META', timestamp '2013-05-16 15:09:35.5',
        'glory@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 481, 263, timestamp '2013-04-16 13:00:25.3',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2013-04-16 13:00:25.3',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 541, 1361, timestamp '2013-05-16 15:06:22.5', 'glory@META', timestamp '2013-05-16 15:06:22.5',
        'glory@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 541, 520, timestamp '2013-05-16 15:09:42.5', 'glory@META', timestamp '2013-05-16 15:09:42.5',
        'glory@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3197, 3197, 581, 1840, timestamp '2013-05-23 10:27:29.5',
        '/DC=org/DC=terena/DC=tcs/C=CZ/O=Masaryk University/CN=Martin Kuba 3988', timestamp '2013-05-23 10:27:29.5',
        '/DC=org/DC=terena/DC=tcs/C=CZ/O=Masaryk University/CN=Martin Kuba 3988');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3197, 3197, 581, 1860, timestamp '2013-05-23 10:27:43.5',
        '/DC=org/DC=terena/DC=tcs/C=CZ/O=Masaryk University/CN=Martin Kuba 3988', timestamp '2013-05-23 10:27:43.5',
        '/DC=org/DC=terena/DC=tcs/C=CZ/O=Masaryk University/CN=Martin Kuba 3988');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 641, 262, timestamp '2013-07-09 11:34:01.3',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2013-07-09 11:34:01.3',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3354, 3354, 321, 1380, timestamp '2013-09-23 22:50:43.2',
        '/DC=org/DC=terena/DC=tcs/C=CZ/O=CESNET/CN=Michal Prochazka 8497', timestamp '2013-09-23 22:50:43.2',
        '/DC=org/DC=terena/DC=tcs/C=CZ/O=CESNET/CN=Michal Prochazka 8497');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3354, 3354, 661, 1361, timestamp '2013-11-15 22:04:04.6',
        '/C=CZ/O=Masarykova univerzita/CN=Michal Proch\xC3\xA1zka/unstructuredName=39700',
        timestamp '2013-11-15 22:04:04.6',
        '/C=CZ/O=Masarykova univerzita/CN=Michal Proch\xC3\xA1zka/unstructuredName=39700');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3354, 3354, 661, 2520, timestamp '2013-11-15 22:04:55.6',
        '/C=CZ/O=Masarykova univerzita/CN=Michal Proch\xC3\xA1zka/unstructuredName=39700',
        timestamp '2013-11-15 22:04:55.6',
        '/C=CZ/O=Masarykova univerzita/CN=Michal Proch\xC3\xA1zka/unstructuredName=39700');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 681, 880, timestamp '2013-12-16 16:48:11.2',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2013-12-16 16:48:11.2',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 541, 1, timestamp '2014-01-23 16:04:57.5',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2014-01-23 16:04:57.5',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 101, 262, timestamp '2011-12-20 15:06:04.3', 'PERUNV3', timestamp '2011-12-20 15:06:04.3',
        'PERUNV3');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 21, 24, timestamp '2011-11-10 10:00:46.5', 'PERUNV3', timestamp '2011-11-10 10:00:46.5', 'PERUNV3');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 21, 22, timestamp '2011-11-10 10:01:13.5', 'PERUNV3', timestamp '2011-11-10 10:01:13.5', 'PERUNV3');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 101, 460, timestamp '2011-12-16 15:22:48.6', 'PERUNV3', timestamp '2011-12-16 15:22:48.6',
        'PERUNV3');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 163, 622, timestamp '2012-01-12 14:29:33.5', 'PERUNV3', timestamp '2012-01-12 14:29:33.5',
        'PERUNV3');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 101, 263, timestamp '2011-12-20 15:08:42.3', 'PERUNV3', timestamp '2011-12-20 15:08:42.3',
        'PERUNV3');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 462, 361, timestamp '2013-03-07 19:02:59.5', 'glory@META', timestamp '2013-03-07 19:02:59.5',
        'glory@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 101, 301, timestamp '2011-12-20 15:08:43.3', 'PERUNV3', timestamp '2011-12-20 15:08:43.3',
        'PERUNV3');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 43, 1361, timestamp '2013-06-25 14:46:31.3', 'glory@META', timestamp '2013-06-25 14:46:31.3',
        'glory@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 163, 360, timestamp '2012-01-10 13:19:31.3', 'PERUNV3', timestamp '2012-01-10 13:19:31.3',
        'PERUNV3');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 163, 601, timestamp '2012-01-12 14:29:31.5', 'PERUNV3', timestamp '2012-01-12 14:29:31.5',
        'PERUNV3');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 21, 263, timestamp '2012-01-17 11:15:45.3', 'PERUNV3', timestamp '2012-01-17 11:15:45.3',
        'PERUNV3');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3354, 3354, 601, 1380, timestamp '2013-06-18 11:31:54.3',
        '/DC=org/DC=terena/DC=tcs/C=CZ/O=CESNET/CN=Michal Prochazka 8497', timestamp '2013-06-18 11:31:54.3',
        '/DC=org/DC=terena/DC=tcs/C=CZ/O=CESNET/CN=Michal Prochazka 8497');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 341, 1603, timestamp '2012-10-05 13:04:28.6', 'makub@META', timestamp '2012-10-05 13:04:28.6',
        'makub@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 61, 1, timestamp '2012-04-10 14:02:34.3', 'glory@META', timestamp '2012-04-10 14:02:34.3',
        'glory@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 141, 1, timestamp '2012-04-11 08:25:09.4', 'glory@META', timestamp '2012-04-11 08:25:09.4',
        'glory@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 481, 49, timestamp '2013-04-09 16:19:53.3',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2013-04-09 16:19:53.3',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 481, 260, timestamp '2013-04-09 16:19:53.3',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2013-04-09 16:19:53.3',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 541, 146, timestamp '2013-05-16 15:06:02.5', 'glory@META', timestamp '2013-05-16 15:06:02.5',
        'glory@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3197, 3197, 561, 261, timestamp '2013-05-23 15:52:27.5',
        '/DC=org/DC=terena/DC=tcs/C=CZ/O=Masaryk University/CN=Martin Kuba 3988', timestamp '2013-05-23 15:52:27.5',
        '/DC=org/DC=terena/DC=tcs/C=CZ/O=Masaryk University/CN=Martin Kuba 3988');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3354, 3354, 601, 261, timestamp '2013-06-20 14:01:00.5',
        '/DC=org/DC=terena/DC=tcs/C=CZ/O=CESNET/CN=Michal Prochazka 8497', timestamp '2013-06-20 14:01:00.5',
        '/DC=org/DC=terena/DC=tcs/C=CZ/O=CESNET/CN=Michal Prochazka 8497');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 561, 1361, timestamp '2013-06-25 14:46:27.3', 'glory@META', timestamp '2013-06-25 14:46:27.3',
        'glory@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 462, 1361, timestamp '2013-06-25 14:46:29.3', 'glory@META', timestamp '2013-06-25 14:46:29.3',
        'glory@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3354, 3354, 621, 1941, timestamp '2013-06-26 13:52:28.4',
        '/DC=org/DC=terena/DC=tcs/C=CZ/O=CESNET/CN=Michal Prochazka 8497', timestamp '2013-06-26 13:52:28.4',
        '/DC=org/DC=terena/DC=tcs/C=CZ/O=CESNET/CN=Michal Prochazka 8497');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 21, 581, timestamp '2013-01-24 13:42:46.5', 'glory@META', timestamp '2013-01-24 13:42:46.5',
        'glory@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 121, 81, timestamp '2013-09-16 16:11:18.2',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2013-09-16 16:11:18.2',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 541, 2380, timestamp '2013-06-27 10:03:25.5', 'glory@META', timestamp '2013-06-27 10:03:25.5',
        'glory@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 163, 1120, timestamp '2012-04-17 15:19:40.3', 'glory@META', timestamp '2012-04-17 15:19:40.3',
        'glory@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 261, 1140, timestamp '2012-04-24 15:01:02.3', 'michalp@META', timestamp '2012-04-24 15:01:02.3',
        'michalp@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 261, 146, timestamp '2012-04-24 15:01:03.3', 'michalp@META', timestamp '2012-04-24 15:01:03.3',
        'michalp@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 121, 2160, timestamp '2013-02-27 13:12:05.4',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2013-02-27 13:12:05.4',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 121, 1200, timestamp '2012-05-13 17:23:08.1', 'glory@META', timestamp '2012-05-13 17:23:08.1',
        'glory@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 121, 1241, timestamp '2012-05-22 10:45:50.3', 'zora@META', timestamp '2012-05-22 10:45:50.3',
        'zora@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 121, 1242, timestamp '2012-05-22 10:45:51.3', 'zora@META', timestamp '2012-05-22 10:45:51.3',
        'zora@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 121, 1243, timestamp '2012-05-22 10:45:51.3', 'zora@META', timestamp '2012-05-22 10:45:51.3',
        'zora@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 121, 1244, timestamp '2012-05-22 10:45:52.3', 'zora@META', timestamp '2012-05-22 10:45:52.3',
        'zora@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 281, 45, timestamp '2012-10-30 15:27:56.3', 'michalp@META', timestamp '2012-10-30 15:27:56.3',
        'michalp@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 101, 2340, timestamp '2013-09-02 09:43:22.2',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2013-09-02 09:43:22.2',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 422, 260, timestamp '2012-11-15 09:58:01.5', 'michalp@META', timestamp '2012-11-15 09:58:01.5',
        'michalp@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 81, 2040, timestamp '2013-01-29 15:00:38.3', 'glory@META', timestamp '2013-01-29 15:00:38.3',
        'glory@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 541, 2480, timestamp '2013-10-14 16:08:56.2',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2013-10-14 16:08:56.2',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 42, 2340, timestamp '2013-07-04 10:18:38.5',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2013-07-04 10:18:38.5',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 42, 280, timestamp '2013-07-04 10:18:39.5',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2013-07-04 10:18:39.5',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 162, 280, timestamp '2013-07-04 10:19:32.5',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2013-07-04 10:19:32.5',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 462, 1720, timestamp '2013-03-07 19:03:02.5', 'glory@META', timestamp '2013-03-07 19:03:02.5',
        'glory@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 462, 1520, timestamp '2013-03-07 19:03:04.5', 'glory@META', timestamp '2013-03-07 19:03:04.5',
        'glory@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 162, 2340, timestamp '2013-07-04 10:19:49.5',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2013-07-04 10:19:49.5',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3354, 3354, 462, 2400, timestamp '2013-09-06 10:36:22.6',
        '/DC=org/DC=terena/DC=tcs/C=CZ/O=CESNET/CN=Michal Prochazka 8497', timestamp '2013-09-06 10:36:22.6',
        '/DC=org/DC=terena/DC=tcs/C=CZ/O=CESNET/CN=Michal Prochazka 8497');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 641, 1720, timestamp '2013-07-09 11:35:42.3',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2013-07-09 11:35:42.3',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 541, 1720, timestamp '2013-10-09 14:44:16.4',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2013-10-09 14:44:16.4',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 121, 582, timestamp '2013-09-16 15:19:06.2',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2013-09-16 15:19:06.2',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 541, 81, timestamp '2013-10-09 15:17:50.4',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2013-10-09 15:17:50.4',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 541, 49, timestamp '2013-10-14 16:09:05.2',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2013-10-14 16:09:05.2',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 681, 261, timestamp '2013-12-10 18:13:49.3',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2013-12-10 18:13:49.3',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 681, 2400, timestamp '2013-12-10 18:15:23.3',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2013-12-10 18:15:23.3',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 681, 1140, timestamp '2013-12-10 18:15:42.3',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2013-12-10 18:15:42.3',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 681, 1361, timestamp '2013-12-10 18:15:53.3',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2013-12-10 18:15:53.3',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3354, 3354, 701, 1140, timestamp '2013-12-20 11:23:18.6',
        '/C=CZ/O=Masarykova univerzita/CN=Michal Proch\xC3\xA1zka/unstructuredName=39700',
        timestamp '2013-12-20 11:23:18.6',
        '/C=CZ/O=Masarykova univerzita/CN=Michal Proch\xC3\xA1zka/unstructuredName=39700');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 721, 1980, timestamp '2014-03-12 00:00:20.4',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2014-03-12 00:00:20.4',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (6701, 6701, 741, 261, timestamp '2014-03-12 09:39:57.4', 'stava@META', timestamp '2014-03-12 09:39:57.4',
        'stava@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (6701, 6701, 741, 341, timestamp '2014-03-12 09:39:57.4', 'stava@META', timestamp '2014-03-12 09:39:57.4',
        'stava@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 721, 261, timestamp '2014-03-12 14:44:19.4',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2014-03-12 14:44:19.4',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 21, 14, timestamp '2011-10-13 11:13:21.5', 'PERUNV3', timestamp '2011-10-13 11:13:21.5', 'PERUNV3');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 101, 342, timestamp '2011-12-16 16:14:50.6', 'PERUNV3', timestamp '2011-12-16 16:14:50.6',
        'PERUNV3');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 121, 260, timestamp '2011-12-16 19:58:45.6', 'PERUNV3', timestamp '2011-12-16 19:58:45.6',
        'PERUNV3');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 21, 13, timestamp '2011-10-13 11:18:38.5', 'PERUNV3', timestamp '2011-10-13 11:18:38.5', 'PERUNV3');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 541, 580, timestamp '2013-10-09 15:07:15.4',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2013-10-09 15:07:15.4',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 21, 23, timestamp '2011-10-13 11:22:25.5', 'PERUNV3', timestamp '2011-10-13 11:22:25.5', 'PERUNV3');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 21, 21, timestamp '2011-10-13 11:22:34.5', 'PERUNV3', timestamp '2011-10-13 11:22:34.5', 'PERUNV3');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 21, 20, timestamp '2011-10-13 11:22:57.5', 'PERUNV3', timestamp '2011-10-13 11:22:57.5', 'PERUNV3');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 21, 19, timestamp '2011-10-13 11:23:09.5', 'PERUNV3', timestamp '2011-10-13 11:23:09.5', 'PERUNV3');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 61, 260, timestamp '2011-12-01 14:55:49.5', 'PERUNV3', timestamp '2011-12-01 14:55:49.5',
        'PERUNV3');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3197, 3197, 561, 260, timestamp '2013-05-23 15:52:27.5',
        '/DC=org/DC=terena/DC=tcs/C=CZ/O=Masaryk University/CN=Martin Kuba 3988', timestamp '2013-05-23 15:52:27.5',
        '/DC=org/DC=terena/DC=tcs/C=CZ/O=Masaryk University/CN=Martin Kuba 3988');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 163, 262, timestamp '2012-01-10 13:19:27.3', 'PERUNV3', timestamp '2012-01-10 13:19:27.3',
        'PERUNV3');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 341, 1580, timestamp '2012-10-04 13:00:21.5', 'makub@META', timestamp '2012-10-04 13:00:21.5',
        'makub@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 341, 1607, timestamp '2012-10-05 13:04:28.6', 'makub@META', timestamp '2012-10-05 13:04:28.6',
        'makub@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 341, 1608, timestamp '2012-10-05 13:04:28.6', 'makub@META', timestamp '2012-10-05 13:04:28.6',
        'makub@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 1, 1060, timestamp '2012-03-21 09:23:28.4', 'michalp@META', timestamp '2012-03-21 09:23:28.4',
        'michalp@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 163, 342, timestamp '2012-01-10 13:19:28.3', 'PERUNV3', timestamp '2012-01-10 13:19:28.3',
        'PERUNV3');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 163, 50, timestamp '2012-01-10 13:19:28.3', 'PERUNV3', timestamp '2012-01-10 13:19:28.3',
        'PERUNV3');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 163, 361, timestamp '2012-01-10 13:19:32.3', 'PERUNV3', timestamp '2012-01-10 13:19:32.3',
        'PERUNV3');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 341, 1601, timestamp '2012-10-05 13:04:28.6', 'makub@META', timestamp '2012-10-05 13:04:28.6',
        'makub@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 341, 1602, timestamp '2012-10-05 13:04:28.6', 'makub@META', timestamp '2012-10-05 13:04:28.6',
        'makub@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 641, 360, timestamp '2013-07-09 11:34:25.3',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2013-07-09 11:34:25.3',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 162, 1160, timestamp '2013-01-15 13:48:59.3', 'glory@META', timestamp '2013-01-15 13:48:59.3',
        'glory@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 281, 1720, timestamp '2012-10-30 16:36:26.3', 'glory@META', timestamp '2012-10-30 16:36:26.3',
        'glory@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 261, 1363, timestamp '2013-01-24 23:22:36.5',
        '/DC=org/DC=terena/DC=tcs/C=CZ/O=CESNET/CN=Michal Prochazka 8497', timestamp '2013-01-24 23:22:36.5',
        '/DC=org/DC=terena/DC=tcs/C=CZ/O=CESNET/CN=Michal Prochazka 8497');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 261, 880, timestamp '2013-01-24 23:51:39.5',
        '/DC=org/DC=terena/DC=tcs/C=CZ/O=CESNET/CN=Michal Prochazka 8497', timestamp '2013-01-24 23:51:39.5',
        '/DC=org/DC=terena/DC=tcs/C=CZ/O=CESNET/CN=Michal Prochazka 8497');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 261, 1720, timestamp '2013-01-25 00:07:11.6',
        '/DC=org/DC=terena/DC=tcs/C=CZ/O=CESNET/CN=Michal Prochazka 8497', timestamp '2013-01-25 00:07:11.6',
        '/DC=org/DC=terena/DC=tcs/C=CZ/O=CESNET/CN=Michal Prochazka 8497');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3354, 3354, 481, 1140, timestamp '2013-04-12 13:05:57.6',
        '/DC=org/DC=terena/DC=tcs/C=CZ/O=CESNET/CN=Michal Prochazka 8497', timestamp '2013-04-12 13:05:57.6',
        '/DC=org/DC=terena/DC=tcs/C=CZ/O=CESNET/CN=Michal Prochazka 8497');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3197, 3197, 561, 1140, timestamp '2013-05-23 15:51:42.5',
        '/DC=org/DC=terena/DC=tcs/C=CZ/O=Masaryk University/CN=Martin Kuba 3988', timestamp '2013-05-23 15:51:42.5',
        '/DC=org/DC=terena/DC=tcs/C=CZ/O=Masaryk University/CN=Martin Kuba 3988');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3197, 3197, 561, 82, timestamp '2013-05-24 11:21:49.6',
        '/DC=org/DC=terena/DC=tcs/C=CZ/O=Masaryk University/CN=Martin Kuba 3988', timestamp '2013-05-24 11:21:49.6',
        '/DC=org/DC=terena/DC=tcs/C=CZ/O=Masaryk University/CN=Martin Kuba 3988');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 121, 280, timestamp '2013-08-02 17:11:09.6',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2013-08-02 17:11:09.6',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 641, 361, timestamp '2013-07-09 11:34:14.3',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2013-07-09 11:34:14.3',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 21, 280, timestamp '2013-08-07 11:05:16.4',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2013-08-07 11:05:16.4',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 121, 1720, timestamp '2013-09-16 16:53:33.2',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2013-09-16 16:53:33.2',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 541, 2340, timestamp '2013-10-09 15:07:15.4',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2013-10-09 15:07:15.4',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 541, 581, timestamp '2013-10-09 15:07:16.4',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2013-10-09 15:07:16.4',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 462, 263, timestamp '2013-03-07 19:02:55.5', 'glory@META', timestamp '2013-03-07 19:02:55.5',
        'glory@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 462, 262, timestamp '2013-03-07 19:03:07.5', 'glory@META', timestamp '2013-03-07 19:03:07.5',
        'glory@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3354, 3354, 661, 880, timestamp '2013-11-15 22:04:19.6',
        '/C=CZ/O=Masarykova univerzita/CN=Michal Proch\xC3\xA1zka/unstructuredName=39700',
        timestamp '2013-11-15 22:04:19.6',
        '/C=CZ/O=Masarykova univerzita/CN=Michal Proch\xC3\xA1zka/unstructuredName=39700');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 681, 2340, timestamp '2013-12-10 18:13:00.3',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2013-12-10 18:13:00.3',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3354, 3354, 701, 146, timestamp '2013-12-20 11:23:18.6',
        '/C=CZ/O=Masarykova univerzita/CN=Michal Proch\xC3\xA1zka/unstructuredName=39700',
        timestamp '2013-12-20 11:23:18.6',
        '/C=CZ/O=Masarykova univerzita/CN=Michal Proch\xC3\xA1zka/unstructuredName=39700');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3354, 3354, 701, 1361, timestamp '2013-12-20 12:05:57.6',
        '/C=CZ/O=Masarykova univerzita/CN=Michal Proch\xC3\xA1zka/unstructuredName=39700',
        timestamp '2013-12-20 12:05:57.6',
        '/C=CZ/O=Masarykova univerzita/CN=Michal Proch\xC3\xA1zka/unstructuredName=39700');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 341, 2640, timestamp '2014-01-23 13:02:19.5',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2014-01-23 13:02:19.5',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3261, 3261, 61, 2660, timestamp '2014-01-24 11:15:23.6',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378', timestamp '2014-01-24 11:15:23.6',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3354, 3354, 701, 1363, timestamp '2013-12-20 12:30:25.6',
        '/C=CZ/O=Masarykova univerzita/CN=Michal Proch\xC3\xA1zka/unstructuredName=39700',
        timestamp '2013-12-20 12:30:25.6',
        '/C=CZ/O=Masarykova univerzita/CN=Michal Proch\xC3\xA1zka/unstructuredName=39700');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3261, 3261, 341, 2660, timestamp '2014-01-24 11:14:32.6',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378', timestamp '2014-01-24 11:14:32.6',
        '/C=CZ/O=Masaryk University/CN=Zora Sebestianova/unstructuredName=71378');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (6701, 6701, 741, 2340, timestamp '2014-03-12 09:39:57.4', 'stava@META', timestamp '2014-03-12 09:39:57.4',
        'stava@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (6701, 6701, 741, 2702, timestamp '2014-03-12 09:39:57.4', 'stava@META', timestamp '2014-03-12 09:39:57.4',
        'stava@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 721, 260, timestamp '2014-03-12 14:44:19.4',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2014-03-12 14:44:19.4',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 721, 1372, timestamp '2014-03-12 14:44:30.4',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2014-03-12 14:44:30.4',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 101, 2720, timestamp '2014-05-05 14:54:53.2',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2014-05-05 14:54:53.2',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3354, 3354, 762, 51, timestamp '2014-05-23 13:04:08.6', '39700@muni.cz', timestamp '2014-05-23 13:04:08.6',
        '39700@muni.cz');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3354, 3354, 762, 260, timestamp '2014-05-23 13:04:15.6', '39700@muni.cz', timestamp '2014-05-23 13:04:15.6',
        '39700@muni.cz');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 281, 880, timestamp '2014-06-06 16:59:54.6',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2014-06-06 16:59:54.6',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 341, 1605, timestamp '2012-10-05 13:04:28.6', 'makub@META', timestamp '2012-10-05 13:04:28.6',
        'makub@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 162, 581, timestamp '2012-01-11 12:19:19.4', 'PERUNV3', timestamp '2012-01-11 12:19:19.4',
        'PERUNV3');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 641, 263, timestamp '2013-07-09 11:34:01.3',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2013-07-09 11:34:01.3',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 42, 581, timestamp '2012-01-11 12:19:17.4', 'PERUNV3', timestamp '2012-01-11 12:19:17.4',
        'PERUNV3');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3354, 3354, 501, 2400, timestamp '2013-09-06 10:36:50.6',
        '/DC=org/DC=terena/DC=tcs/C=CZ/O=CESNET/CN=Michal Prochazka 8497', timestamp '2013-09-06 10:36:50.6',
        '/DC=org/DC=terena/DC=tcs/C=CZ/O=CESNET/CN=Michal Prochazka 8497');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 41, 500, timestamp '2011-12-22 13:50:05.5', 'PERUNV3', timestamp '2011-12-22 13:50:05.5',
        'PERUNV3');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3354, 3354, 621, 2400, timestamp '2013-07-09 13:48:39.3',
        '/DC=org/DC=terena/DC=tcs/C=CZ/O=CESNET/CN=Michal Prochazka 8497', timestamp '2013-07-09 13:48:39.3',
        '/DC=org/DC=terena/DC=tcs/C=CZ/O=CESNET/CN=Michal Prochazka 8497');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 641, 2420, timestamp '2013-07-10 15:40:20.4',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2013-07-10 15:40:20.4',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 42, 582, timestamp '2012-01-11 12:17:38.4', 'PERUNV3', timestamp '2012-01-11 12:17:38.4',
        'PERUNV3');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 162, 582, timestamp '2012-01-11 12:17:51.4', 'PERUNV3', timestamp '2012-01-11 12:17:51.4',
        'PERUNV3');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 42, 580, timestamp '2012-01-11 12:20:03.4', 'PERUNV3', timestamp '2012-01-11 12:20:03.4',
        'PERUNV3');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 162, 580, timestamp '2012-01-11 12:20:04.4', 'PERUNV3', timestamp '2012-01-11 12:20:04.4',
        'PERUNV3');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 221, 280, timestamp '2013-09-02 09:44:38.2',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2013-09-02 09:44:38.2',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 541, 880, timestamp '2013-10-11 12:04:07.6',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2013-10-11 12:04:07.6',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 42, 1161, timestamp '2012-05-07 10:17:57.2', 'glory@META', timestamp '2012-05-07 10:17:57.2',
        'glory@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 121, 580, timestamp '2012-02-16 11:09:47.5', 'glory@META', timestamp '2012-02-16 11:09:47.5',
        'glory@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 101, 580, timestamp '2012-02-16 11:09:58.5', 'glory@META', timestamp '2012-02-16 11:09:58.5',
        'glory@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 161, 263, timestamp '2012-01-12 12:09:15.5', 'PERUNV3', timestamp '2012-01-12 12:09:15.5',
        'PERUNV3');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 121, 581, timestamp '2012-02-16 11:10:11.5', 'glory@META', timestamp '2012-02-16 11:10:11.5',
        'glory@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 163, 263, timestamp '2012-01-17 11:19:29.3', 'PERUNV3', timestamp '2012-01-17 11:19:29.3',
        'PERUNV3');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 101, 581, timestamp '2012-02-16 11:10:15.5', 'glory@META', timestamp '2012-02-16 11:10:15.5',
        'glory@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 341, 1604, timestamp '2012-10-05 13:04:28.6', 'makub@META', timestamp '2012-10-05 13:04:28.6',
        'makub@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 381, 1700, timestamp '2012-10-22 16:17:34.2', 'glory@META', timestamp '2012-10-22 16:17:34.2',
        'glory@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 422, 880, timestamp '2012-11-15 09:58:01.5', 'michalp@META', timestamp '2012-11-15 09:58:01.5',
        'michalp@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 421, 1841, timestamp '2012-11-16 13:05:52.6', 'glory@META', timestamp '2012-11-16 13:05:52.6',
        'glory@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 21, 880, timestamp '2012-03-20 13:43:49.3', 'glory@META', timestamp '2012-03-20 13:43:49.3',
        'glory@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 81, 880, timestamp '2012-03-20 13:44:04.3', 'glory@META', timestamp '2012-03-20 13:44:04.3',
        'glory@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 21, 860, timestamp '2012-03-20 14:35:37.3', 'glory@META', timestamp '2012-03-20 14:35:37.3',
        'glory@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 41, 800, timestamp '2012-03-23 09:34:56.6', 'michalp@META', timestamp '2012-03-23 09:34:56.6',
        'michalp@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 42, 1160, timestamp '2012-05-07 10:17:55.2', 'glory@META', timestamp '2012-05-07 10:17:55.2',
        'glory@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 42, 1162, timestamp '2012-05-07 10:17:58.2', 'glory@META', timestamp '2012-05-07 10:17:58.2',
        'glory@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 341, 1480, timestamp '2012-09-13 10:06:22.5', 'makub@META', timestamp '2012-09-13 10:06:22.5',
        'makub@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 341, 1, timestamp '2012-09-13 10:06:22.5', 'makub@META', timestamp '2012-09-13 10:06:22.5',
        'makub@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 421, 1880, timestamp '2012-11-20 13:58:21.3', 'makub@META', timestamp '2012-11-20 13:58:21.3',
        'makub@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 422, 1369, timestamp '2012-12-06 08:50:21.5',
        '/DC=org/DC=terena/DC=tcs/C=CZ/O=Masaryk University/CN=Martin Kuba 3988', timestamp '2012-12-06 08:50:21.5',
        '/DC=org/DC=terena/DC=tcs/C=CZ/O=Masaryk University/CN=Martin Kuba 3988');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 462, 2200, timestamp '2013-03-11 12:58:43.2',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2013-03-11 12:58:43.2',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (null, null, 21, 580, timestamp '2013-01-24 13:42:49.5', 'glory@META', timestamp '2013-01-24 13:42:49.5',
        'glory@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 462, 360, timestamp '2013-03-07 19:02:57.5', 'glory@META', timestamp '2013-03-07 19:02:57.5',
        'glory@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 541, 262, timestamp '2013-05-16 15:07:09.5', 'glory@META', timestamp '2013-05-16 15:07:09.5',
        'glory@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3354, 3354, 661, 1140, timestamp '2013-11-15 22:03:47.6',
        '/C=CZ/O=Masarykova univerzita/CN=Michal Proch\xC3\xA1zka/unstructuredName=39700',
        timestamp '2013-11-15 22:03:47.6',
        '/C=CZ/O=Masarykova univerzita/CN=Michal Proch\xC3\xA1zka/unstructuredName=39700');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 541, 39, timestamp '2013-05-16 15:06:45.5', 'glory@META', timestamp '2013-05-16 15:06:45.5',
        'glory@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 541, 760, timestamp '2013-05-16 15:09:56.5', 'glory@META', timestamp '2013-05-16 15:09:56.5',
        'glory@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 541, 361, timestamp '2013-05-16 15:08:36.5', 'glory@META', timestamp '2013-05-16 15:08:36.5',
        'glory@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 541, 521, timestamp '2013-05-16 15:09:47.5', 'glory@META', timestamp '2013-05-16 15:09:47.5',
        'glory@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 541, 761, timestamp '2013-05-16 15:10:02.5', 'glory@META', timestamp '2013-05-16 15:10:02.5',
        'glory@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 541, 1941, timestamp '2013-06-27 09:29:04.5', 'glory@META', timestamp '2013-06-27 09:29:04.5',
        'glory@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3354, 3354, 661, 1840, timestamp '2013-11-15 22:04:40.6',
        '/C=CZ/O=Masarykova univerzita/CN=Michal Proch\xC3\xA1zka/unstructuredName=39700',
        timestamp '2013-11-15 22:04:40.6',
        '/C=CZ/O=Masarykova univerzita/CN=Michal Proch\xC3\xA1zka/unstructuredName=39700');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 681, 582, timestamp '2013-12-10 18:13:00.3',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2013-12-10 18:13:00.3',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 681, 260, timestamp '2013-12-10 18:13:50.3',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2013-12-10 18:13:50.3',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (6701, 6701, 741, 2840, timestamp '2014-07-09 11:56:47.4', 'stava@META', timestamp '2014-07-09 11:56:47.4',
        'stava@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3354, 3354, 701, 50, timestamp '2013-12-20 11:23:18.6',
        '/C=CZ/O=Masarykova univerzita/CN=Michal Proch\xC3\xA1zka/unstructuredName=39700',
        timestamp '2013-12-20 11:23:18.6',
        '/C=CZ/O=Masarykova univerzita/CN=Michal Proch\xC3\xA1zka/unstructuredName=39700');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3354, 3354, 701, 1364, timestamp '2013-12-20 11:23:18.6',
        '/C=CZ/O=Masarykova univerzita/CN=Michal Proch\xC3\xA1zka/unstructuredName=39700',
        timestamp '2013-12-20 11:23:18.6',
        '/C=CZ/O=Masarykova univerzita/CN=Michal Proch\xC3\xA1zka/unstructuredName=39700');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 21, 2620, timestamp '2014-01-09 11:19:52.5',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2014-01-09 11:19:52.5',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 21, 2680, timestamp '2014-02-26 21:45:55.4',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2014-02-26 21:45:55.4',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (6701, 6701, 741, 280, timestamp '2014-03-12 09:39:57.4', 'stava@META', timestamp '2014-03-12 09:39:57.4',
        'stava@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 781, 1341, timestamp '2014-07-07 15:44:48.2',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2014-07-07 15:44:48.2',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 781, 2820, timestamp '2014-07-07 15:48:38.2',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2014-07-07 15:48:38.2',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 781, 880, timestamp '2014-06-30 14:42:33.2',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2014-06-30 14:42:33.2',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 781, 50, timestamp '2014-06-30 14:42:38.2',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2014-06-30 14:42:38.2',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 781, 51, timestamp '2014-06-30 14:42:43.2',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2014-06-30 14:42:43.2',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 781, 1460, timestamp '2014-07-07 15:44:48.2',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2014-07-07 15:44:48.2',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 781, 2821, timestamp '2014-07-07 15:49:33.2',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2014-07-07 15:49:33.2',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 781, 45, timestamp '2014-07-07 15:52:24.2',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2014-07-07 15:52:24.2',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 781, 2822, timestamp '2014-07-07 16:22:58.2',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2014-07-07 16:22:58.2',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 781, 49, timestamp '2014-07-07 18:14:55.2',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2014-07-07 18:14:55.2',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (6701, 6701, 741, 2842, timestamp '2014-07-09 11:57:00.4', 'stava@META', timestamp '2014-07-09 11:57:00.4',
        'stava@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (6701, 6701, 741, 2841, timestamp '2014-07-09 11:57:00.4', 'stava@META', timestamp '2014-07-09 11:57:00.4',
        'stava@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (6701, 6701, 21, 2980, timestamp '2014-09-09 14:34:05.3',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739',
        timestamp '2014-09-09 14:34:05.3',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (6701, 6701, 161, 2988, timestamp '2014-09-09 14:34:14.3',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739',
        timestamp '2014-09-09 14:34:14.3',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (6701, 6701, 163, 2980, timestamp '2014-09-09 14:34:39.3',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739',
        timestamp '2014-09-09 14:34:39.3',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (6701, 6701, 101, 2989, timestamp '2014-09-09 14:35:04.3',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739',
        timestamp '2014-09-09 14:35:04.3',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (6701, 6701, 841, 341, timestamp '2014-10-08 16:08:38.4',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739',
        timestamp '2014-10-08 16:08:38.4',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (6701, 6701, 121, 2840, timestamp '2014-09-03 10:38:38.4',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739',
        timestamp '2014-09-03 10:38:38.4',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (6701, 6701, 21, 2988, timestamp '2014-09-09 14:34:05.3',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739',
        timestamp '2014-09-09 14:34:05.3',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (6701, 6701, 161, 2980, timestamp '2014-09-09 14:34:14.3',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739',
        timestamp '2014-09-09 14:34:14.3',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (6701, 6701, 163, 2988, timestamp '2014-09-09 14:34:39.3',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739',
        timestamp '2014-09-09 14:34:39.3',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (6701, 6701, 101, 2988, timestamp '2014-09-09 14:35:04.3',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739',
        timestamp '2014-09-09 14:35:04.3',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (6701, 6701, 841, 263, timestamp '2014-10-08 15:53:12.4',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739',
        timestamp '2014-10-08 15:53:12.4',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (6701, 6701, 841, 261, timestamp '2014-10-08 15:52:53.4',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739',
        timestamp '2014-10-08 15:52:53.4',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (6701, 6701, 841, 262, timestamp '2014-10-08 15:53:12.4',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739',
        timestamp '2014-10-08 15:53:12.4',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (6701, 6701, 42, 880, timestamp '2015-01-20 14:15:53.3', 'stava@META', timestamp '2015-01-20 14:15:53.3',
        'stava@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (6701, 6701, 161, 2989, timestamp '2014-09-09 14:34:14.3',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739',
        timestamp '2014-09-09 14:34:14.3',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (6701, 6701, 163, 2989, timestamp '2014-09-09 14:34:39.3',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739',
        timestamp '2014-09-09 14:34:39.3',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (6701, 6701, 101, 2980, timestamp '2014-09-09 14:35:04.3',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739',
        timestamp '2014-09-09 14:35:04.3',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (6701, 6701, 221, 2980, timestamp '2014-09-09 14:35:28.3',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739',
        timestamp '2014-09-09 14:35:28.3',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3354, 3354, 861, 1140, timestamp '2014-11-24 12:38:32.2',
        '/C=CZ/O=Masarykova univerzita/CN=Michal Prochazka/unstructuredName=39700', timestamp '2014-11-24 12:38:32.2',
        '/C=CZ/O=Masarykova univerzita/CN=Michal Prochazka/unstructuredName=39700');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (6701, 6701, 21, 2989, timestamp '2014-09-09 14:34:05.3',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739',
        timestamp '2014-09-09 14:34:05.3',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 462, 880, timestamp '2014-11-26 14:45:59.4',
        '/C=CZ/O=Masarykova univerzita/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2014-11-26 14:45:59.4',
        '/C=CZ/O=Masarykova univerzita/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (6701, 6701, 221, 2989, timestamp '2014-09-09 14:35:28.3',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739',
        timestamp '2014-09-09 14:35:28.3',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3354, 3354, 861, 123, timestamp '2014-11-24 12:39:18.2',
        '/C=CZ/O=Masarykova univerzita/CN=Michal Prochazka/unstructuredName=39700', timestamp '2014-11-24 12:39:18.2',
        '/C=CZ/O=Masarykova univerzita/CN=Michal Prochazka/unstructuredName=39700');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (6701, 6701, 221, 2988, timestamp '2014-09-09 14:35:28.3',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739',
        timestamp '2014-09-09 14:35:28.3',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3354, 3354, 861, 1361, timestamp '2014-11-24 12:38:41.2',
        '/C=CZ/O=Masarykova univerzita/CN=Michal Prochazka/unstructuredName=39700', timestamp '2014-11-24 12:38:41.2',
        '/C=CZ/O=Masarykova univerzita/CN=Michal Prochazka/unstructuredName=39700');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (6701, 6701, 42, 3220, timestamp '2015-01-20 14:16:00.3', 'stava@META', timestamp '2015-01-20 14:16:00.3',
        'stava@META');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (6701, 6701, 841, 260, timestamp '2014-10-08 15:52:53.4',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739',
        timestamp '2014-10-08 15:52:53.4',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 822, 146, timestamp '2014-09-11 11:38:28.5',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2014-09-11 11:38:28.5',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (3255, 3255, 221, 3020, timestamp '2014-09-17 10:18:34.4',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920', timestamp '2014-09-17 10:18:34.4',
        '/C=CZ/O=Masaryk University/CN=Slavek Licehammer/unstructuredName=255920');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (6701, 6701, 841, 581, timestamp '2014-10-08 15:54:14.4',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739',
        timestamp '2014-10-08 15:54:14.4',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (6701, 6701, 841, 2340, timestamp '2014-10-08 16:08:10.4',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739',
        timestamp '2014-10-08 16:08:10.4',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (6701, 6701, 841, 582, timestamp '2014-10-08 16:07:52.4',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739',
        timestamp '2014-10-08 16:07:52.4',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (6701, 6701, 841, 280, timestamp '2014-10-08 15:57:03.4',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739',
        timestamp '2014-10-08 15:57:03.4',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (6701, 6701, 841, 361, timestamp '2014-10-08 15:57:45.4',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739',
        timestamp '2014-10-08 15:57:45.4',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (6701, 6701, 841, 360, timestamp '2014-10-08 15:57:57.4',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739',
        timestamp '2014-10-08 15:57:57.4',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (6701, 6701, 841, 580, timestamp '2014-10-08 15:54:14.4',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739',
        timestamp '2014-10-08 15:54:14.4',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739');
insert into service_required_attrs (created_by_uid, modified_by_uid, service_id, attr_id, created_at, created_by,
                                    modified_at, modified_by)
values (6701, 6701, 341, 1720, timestamp '2014-11-26 17:23:01.4',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739',
        timestamp '2014-11-26 17:23:01.4',
        '/C=CZ/O=Masarykova univerzita/CN=Michal \xC5\xA0\xC5\xA5ava/unstructuredName=255739');

drop sequence pn_template_message_id_seq;
create sequence pn_template_message_id_seq start with 162;
drop sequence users_id_seq;
create sequence users_id_seq start with 45338;
drop sequence user_ext_sources_id_seq;
create sequence user_ext_sources_id_seq start with 104094;
drop sequence pn_template_id_seq;
create sequence pn_template_id_seq start with 143;
drop sequence hosts_id_seq;
create sequence hosts_id_seq start with 5247;
drop sequence service_denials_id_seq;
create sequence service_denials_id_seq start with 1236;
drop sequence pn_regex_object_seq;
create sequence pn_regex_object_seq start with 1;
drop sequence mailchange_id_seq;
create sequence mailchange_id_seq start with 443;
drop sequence tasks_results_id_seq;
create sequence tasks_results_id_seq start with 85745269;
drop sequence vos_id_seq;
create sequence vos_id_seq start with 3322;
drop sequence pn_audit_message_id_seq;
create sequence pn_audit_message_id_seq start with 1;
drop sequence ext_sources_id_seq;
create sequence ext_sources_id_seq start with 2402;
drop sequence pwdreset_id_seq;
create sequence pwdreset_id_seq start with 422;
drop sequence cabinet_publications_id_seq;
create sequence cabinet_publications_id_seq start with 5024;
drop sequence pn_object_id_seq;
create sequence pn_object_id_seq start with 1;
drop sequence facilities_id_seq;
create sequence facilities_id_seq start with 3323;
drop sequence tasks_id_seq;
create sequence tasks_id_seq start with 19049;
drop sequence application_mails_id_seq;
create sequence application_mails_id_seq start with 3967;
drop sequence cabinet_categories_id_seq;
create sequence cabinet_categories_id_seq start with 62;
drop sequence pn_pool_message_id_seq;
create sequence pn_pool_message_id_seq start with 1;
drop sequence pn_receiver_id_seq;
create sequence pn_receiver_id_seq start with 182;
drop sequence services_id_seq;
create sequence services_id_seq start with 862;
drop sequence service_packages_id_seq;
create sequence service_packages_id_seq start with 45;
drop sequence application_id_seq;
create sequence application_id_seq start with 18420;
drop sequence destinations_id_seq;
create sequence destinations_id_seq start with 4624;
drop sequence pn_regex_id_seq;
create sequence pn_regex_id_seq start with 102;
drop sequence cabinet_thanks_id_seq;
create sequence cabinet_thanks_id_seq start with 4602;
drop sequence members_id_seq;
create sequence members_id_seq start with 54032;
drop sequence attr_names_id_seq;
create sequence attr_names_id_seq start with 3521;
drop sequence application_form_id_seq;
create sequence application_form_id_seq start with 3502;
drop sequence resources_id_seq;
create sequence resources_id_seq start with 8306;
drop sequence cabinet_pub_sys_id_seq;
create sequence cabinet_pub_sys_id_seq start with 62;
drop sequence auditer_log_id_seq;
create sequence auditer_log_id_seq start with 3199217;
drop sequence pn_template_regex_seq;
create sequence pn_template_regex_seq start with 142;
drop sequence application_data_id_seq;
create sequence application_data_id_seq start with 54230;
drop sequence groups_id_seq;
create sequence groups_id_seq start with 9643;
drop sequence application_form_items_id_seq;
create sequence application_form_items_id_seq start with 7609;
drop sequence res_tags_seq;
create sequence res_tags_seq start with 22;
drop sequence roles_id_seq;
create sequence roles_id_seq start with 102;
drop sequence cabinet_authorships_id_seq;
create sequence cabinet_authorships_id_seq start with 5884;

-- insert default perun admin user
insert into users (id, first_name, last_name)
values (1, 'John', 'Doe');
insert into ext_sources (id, name, type)
values (1, 'BA', 'cz.metacentrum.perun.core.impl.ExtSourceKerberos');
insert into user_ext_sources (id, user_id, login_ext, ext_sources_id, loa)
values (1, 1, 'perun@BA', 1, 2);
insert into authz (user_id, role_id)
values (1, 21);


