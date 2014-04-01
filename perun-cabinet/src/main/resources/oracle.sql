create table cabinet_categories (
   id integer not null,
   name varchar2(128) not null,
   rank number(38,1) not null
);

create table cabinet_publication_systems (
   id integer not null,
   friendlyName varchar2(128) not null,
   url varchar2(128) not null,
   username varchar2(64),
   password varchar2(64),
   loginNamespace varchar2(128) not null,
   type varchar2(128) not null
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
   locked varchar2(1) default 0 not null
);

create table cabinet_authorships (
   id integer not null,
   publicationId integer not null,
   userId integer not null,
   createdBy varchar2(1024) default user not null,
   createdDate date not null
);

create table cabinet_thanks (
   id integer not null,
   publicationid integer not null,
   ownerId integer not null,
   createdBy varchar2(1024) default user not null,
   createdDate date not null
);

create sequence CABINET_PUBLICATIONS_ID_SEQ maxvalue 1.0000E+28;
create sequence CABINET_PUB_SYS_ID_SEQ maxvalue 1.0000E+28;
create sequence CABINET_AUTHORSHIPS_ID_SEQ maxvalue 1.0000E+28;
create sequence CABINET_THANKS_ID_SEQ maxvalue 1.0000E+28;
create sequence CABINET_CATEGORIES_ID_SEQ maxvalue 1.0000E+28;

create index IDX_FK_CABINET_THANKS on cabinet_thanks(publicationid);
create index IDX_FK_CATPUB_SYS on cabinet_publications(publicationSystemid);
create index IDX_FK_CABPUB_CAT on cabinet_publications(categoryid);
create index IDX_FK_CABAUT_PUB on cabinet_authorships(publicationId);
create index IDX_FK_CABAUT_USR on cabinet_authorships(userId);

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
constraint CABTHANK_PUB_FK foreign key(publicationid) references cabinet_publications(id)
);