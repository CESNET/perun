set database sql syntax PGS true;


create table tasks (
    id integer not null,
    exec_service_id  integer not null,
    facility_id  integer not null,
    schedule date not null,
    recurrence integer not null,
    delay integer not null,
    status varchar(16) not null,
    start_time date,
    end_time date,
    engine_id integer not null,
    created_at date  default now not null,
    created_by varchar(1024) default user not null,
    modified_at date default now not null,
    modified_by varchar(1024) default user not null,
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
    timestamp date,
    engine_id integer not null,
    created_at date  default now not null,
    created_by varchar(1024) default user not null,
    modified_at date default now not null,
    modified_by varchar(1024) default user not null,
   created_by_uid integer,
   modified_by_uid integer
);

create table services (
   id integer not null,
   name varchar(128) not null,
   owner_id integer not null,
   created_at date  default now not null,
   created_by varchar(1024) default user not null,
   modified_at date default now not null,
   modified_by varchar(1024) default user not null,
   status char(1) default '0' not null,
   created_by_uid integer,
   modified_by_uid integer
);

create table exec_services (
   id integer not null,
   service_id integer not null,
   default_delay integer not null,
   enabled char(1) not null,
   default_recurrence integer not null,
   script varchar(256) not null,
   type varchar(10) not null,
   created_at date  default now not null,
   created_by varchar(1024) default user not null,
   modified_at date default now not null,
   modified_by varchar(1024) default user not null,
   status char(1) default '0' not null,
   created_by_uid integer,
   modified_by_uid integer
);

create table facilities (
    id integer not null,
    name varchar(128) not null,
    type varchar(32) not null,
    created_at date  default now not null,
    created_by varchar(1024) default user not null,
    modified_at date default now not null,
    modified_by varchar(1024) default user not null,
    status char(1) default '0' not null,
   created_by_uid integer,
   modified_by_uid integer
);


create sequence TASKS_ID_SEQ start with 10 increment by 1;
create sequence TASKS_RESULTS_ID_SEQ start with 10 increment by 1;
create sequence EXEC_SERVICES_ID_SEQ start with 20 increment by 1;
create sequence FACILITIES_ID_SEQ;
create sequence SERVICES_ID_SEQ;

create index idx_tasks_facility_id on tasks(facility_id);
create index idx_tasks_exec_service_id on tasks(exec_service_id);
create index idx_tasks_results_task_id on tasks_results(task_id);
create index IDX_FK_TASK_EXSRV on tasks(exec_service_id);
create index IDX_FK_TASK_FAC on tasks(facility_id);
create index IDX_FK_TASK_ENG on tasks(engine_id);
create index IDX_FK_TASKRES_TASK on tasks_results(task_id);
create index IDX_FK_TASKRES_DEST on tasks_results(destination_id);
create index IDX_FK_TASKRES_ENG on tasks_results(engine_id);
create index IDX_FK_EXSRV_SRV on exec_services(service_id);

alter table tasks add constraint TASK_PK primary key (id);
alter table tasks add constraint TASK_STAT_CHK check (status in ('NONE','OPEN','PLANNED','PROCESSING','DONE','ERROR'));
alter table tasks_results add constraint TASKS_RESULTS_PK primary key (id);
alter table tasks_results add constraint TASKRES_TASK_FK foreign key (task_id) references tasks(id);
alter table tasks_results add constraint TASKRES_STAT_CHK check (status in ('DONE','ERROR','FATAL_ERROR','DENIED'));
alter table services add  constraint SERV_PK primary key(id);
alter table services add constraint SERV_U unique(name);
alter table exec_services add  constraint EXSRV_PK primary key(id);
alter table exec_services add constraint EXSRV_SRV_FK foreign key (service_id) references services(id);
alter table exec_services add constraint EXSRV_TYPE_CHK check (type IN ('SEND','GENERATE'));
alter table facilities add constraint FAC_PK primary key(id); 
alter table facilities add constraint FAC_U unique (name,type);

