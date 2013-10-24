create table "application_form" (
    id integer not null,
    vo_id integer not null,
    group_id integer,
    automatic_approval char(1),
    automatic_approval_extension char(1),
    module_name varchar2(128)
 );

create table "application_form_items" (
    id integer not null,
    form_id integer not null,
    ordnum integer not null,
    shortname varchar(128) not null,
    required char(1),
    type varchar(128),
    fed_attr varchar(128),
    dst_attr varchar(384),
    regex varchar(4000)
 );

create table "application_form_item_apptypes" (
    item_id integer not null,
    apptype varchar(128) not null
 );

create table "application_form_item_texts" (
    item_id integer not null,
    locale varchar(128) not null,
    label varchar(4000),
    options varchar(4000),
    help varchar(4000),
    error_message varchar(4000)
 );

create table "application" (
    id integer not null,
    vo_id integer not null,
    group_id integer,
    user_id integer,
    apptype varchar(128) not null,
    extSourceName varchar(4000),
    extSourceType varchar(4000),
    fed_info varchar(4000),
    state varchar(128),
    extSourceLoa integer,
    created_at date  default 'now' not null,
    created_by varchar(1024) default user not null,
    modified_at date default 'now' not null,
    modified_by varchar(1024) default user not null
 );

create table "application_data" (
    id integer not null,
    app_id integer not null,
    item_id integer,
    shortname varchar(128),
    value varchar(4000),
    assurance_level varchar(128)
 );

create table "application_reserved_logins" ( 
 	login varchar(256) not null,
 	namespace varchar(30) not null,
	app_id integer not null,
	created_by varchar(1024) default user not null,
 	created_at date default 'now' not null
);

create table application_mails (
    id integer not null,
    form_id integer not null,
    app_type varchar(30) not null,
    mail_type varchar(30) not null,
    send varchar(1) not null
);

create table application_mail_texts (
    mail_id integer not null,
    locale varchar(10) not null,
    subject varchar(1024),
    text varchar(4000)
);

create sequence "APPLICATION_FORM_ID_SEQ" maxvalue 1.0000E+28;
create sequence "APPLICATION_FORM_ITEMS_ID_SEQ" maxvalue 1.0000E+28;
create sequence "APPLICATION_ID_SEQ" maxvalue 1.0000E+28;
create sequence "APPLICATION_DATA_ID_SEQ" maxvalue 1.0000E+28;
create sequence "APPLICATION_MAILS_ID_SEQ" maxvalue 1.0000E+28;

create index IDX_FK_APPLFORM_VO on application_form(vo_id);
create index IDX_FK_APPLFORM_GROUP on application_form(group_id);
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

alter table application_form add (
constraint APPLFORM_PK primary key (id),
constraint APPLFORM_U unique (vo_id,group_id),
constraint APPLFORM_VO_FK foreign key (vo_id) references vos(id) on delete cascade 
constraint APPLFORM_GROUP_FK foreign key (group_id) references groups(id) on delete cascade
);

alter table application_form_items add (
constraint APPLFRMIT_PK primary key (id),
constraint APPLFRMIT_APPLFORM foreign key (form_id) references application_form(id) on delete cascade
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
constraint APP_GROUP_FK foreign key (group_id) references groups(id) on delete cascade,
constraint APP_USER_FK foreign key (user_id) references users(id)  on delete cascade,
constraint APP_STATE_CHK check (state in ('DONE','REJECTED','NEW','VERIFIED','APPROVED'))
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