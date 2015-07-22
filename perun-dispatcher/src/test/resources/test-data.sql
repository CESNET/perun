insert into engines (id, ip_address, port, created_by_uid, modified_by_uid)
values (1, '127.0.0.1', 666, 1, 1);

insert into routing_rules (id, routing_rule, created_by_uid, modified_by_uid)
values (1, 'portishead', 1, 1);

insert into engine_routing_rule (engine_id, routing_rule_id, created_by_uid, modified_by_uid)
values (1, 1, 1, 1);

