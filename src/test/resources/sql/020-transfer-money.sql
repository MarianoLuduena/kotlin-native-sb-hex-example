insert into account (id, currency, created_at) values (1, 'ARS', '2019-11-03 08:00:00.0');
insert into account (id, currency, created_at) values (2, 'ARS', '2020-08-01 08:00:00.0');

insert into activity (id, created_at, owner_account_id, source_account_id, target_account_id, amount)
values (1001, '2020-08-08 08:00:00.0', 1, 1, 2, 500);

insert into activity (id, created_at, owner_account_id, source_account_id, target_account_id, amount)
values (1002, '2020-08-08 08:00:00.0', 2, 1, 2, 500);

insert into activity (id, created_at, owner_account_id, source_account_id, target_account_id, amount)
values (1003, '2020-08-09 10:00:00.0', 1, 2, 1, 1000);

insert into activity (id, created_at, owner_account_id, source_account_id, target_account_id, amount)
values (1004, '2020-08-09 10:00:00.0', 2, 2, 1, 1000);

insert into activity (id, created_at, owner_account_id, source_account_id, target_account_id, amount)
values (1005, '2021-08-09 09:00:00.0', 1, 1, 2, 1000);

insert into activity (id, created_at, owner_account_id, source_account_id, target_account_id, amount)
values (1006, '2021-08-09 09:00:00.0', 2, 1, 2, 1000);

insert into activity (id, created_at, owner_account_id, source_account_id, target_account_id, amount)
values (1007, '2021-08-09 10:00:00.0', 1, 2, 1, 1000);

insert into activity (id, created_at, owner_account_id, source_account_id, target_account_id, amount)
values (1008, '2021-08-09 10:00:00.0', 2, 2, 1, 1000);
