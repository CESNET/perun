begin;

alter table pn_reciever rename to pn_receiver;

alter table pn_receiver rename type_of_reciever to type_of_receiver;

alter table pn_receiver drop constraint perun_notif_reciever_id;

alter table pn_receiver drop constraint perun_notif_reciever_template_id;

alter table pn_receiver add constraint pn_receiver_id primary key (id);

alter table pn_receiver add constraint pn_receiver_template_id foreign key (template_id)
references pn_template (id) match simple on update no action on delete no action;

alter table pn_reciever_id_seq rename to pn_receiver_id_seq;

end;