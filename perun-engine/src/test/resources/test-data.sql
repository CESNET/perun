insert into owners (id, name, type)
values (1, 'test_owner', 'test');

insert into services (id, name, owner_id)
values (1, 'test_service', 1);

insert into exec_services (id, service_id, default_delay, enabled, default_recurrence, script, type)
values (1, 1, 10, '1', 3, '/bin/true', 'SEND');

insert into exec_services (id, service_id, default_delay, enabled, default_recurrence, script, type)
values (2, 1, 10, '1', 3, '/bin/true', 'SEND');

insert into exec_services (id, service_id, default_delay, enabled, default_recurrence, script, type)
values (3, 1, 10, '1', 3, '/bin/true', 'GENERATE');

insert into facilities (id, name)
values (0, 'testFacility');

insert into engines (id, ip_address, port)
values (0, '127.0.0.1', 5560);

insert into destinations (id, destination, type) 
values (1, 'par_dest1', 'PARALLEL');

insert into destinations (id, destination, type) 
values (2, 'par_dest2', 'PARALLEL');

insert into destinations (id, destination, type) 
values (3, 'par_dest3', 'ONE');

insert into destinations (id, destination, type) 
values (4, 'par_dest4', 'ONE');

