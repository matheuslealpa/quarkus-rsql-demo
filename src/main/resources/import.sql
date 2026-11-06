insert into Farm (id, name, city, country, organic, ownerToken) values (1, 'Sunny', 'Lisboa', 'PT', true, 'secret-aaa');
insert into Farm (id, name, city, country, organic, ownerToken) values (2, 'Green Valley', 'Porto', 'PT', false, 'secret-bbb');
insert into Farm (id, name, city, country, organic, ownerToken) values (3, 'Fazenda Boa Vista', 'Campinas', 'BR', true, 'secret-ccc');
alter sequence Farm_SEQ restart with 100;

insert into Fruit (id, name, color, weight, price, harvested, farm_id) values (1, 'Apple', 'RED', 150, 2.50, '2026-10-01', 1);
insert into Fruit (id, name, color, weight, price, harvested, farm_id) values (2, 'Green Apple', 'GREEN', 140, 2.80, '2026-10-05', 2);
insert into Fruit (id, name, color, weight, price, harvested, farm_id) values (3, 'Banana', 'YELLOW', 120, 1.20, '2026-09-20', 3);
insert into Fruit (id, name, color, weight, price, harvested, farm_id) values (4, 'Orange', 'ORANGE', 200, 3.10, '2026-10-15', 3);
insert into Fruit (id, name, color, weight, price, harvested, farm_id) values (5, 'Lime', 'GREEN', 60, 0.90, null, null);
insert into Fruit (id, name, color, weight, price, harvested, farm_id) values (6, 'Strawberry', 'RED', 20, 5.00, '2026-11-01', 1);
alter sequence Fruit_SEQ restart with 100;

insert into Fruit_tags (Fruit_id, tags) values (1, 'fresh');
insert into Fruit_tags (Fruit_id, tags) values (1, 'promo');
insert into Fruit_tags (Fruit_id, tags) values (2, 'fresh');
insert into Fruit_tags (Fruit_id, tags) values (4, 'promo');
insert into Fruit_tags (Fruit_id, tags) values (6, 'fresh');
insert into Fruit_tags (Fruit_id, tags) values (6, 'imported');
