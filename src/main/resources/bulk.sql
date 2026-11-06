insert into Farm (id, name, city, country, organic, ownerToken) select 1000 + g, 'Farm ' || g, (array['Lisboa','Porto','Campinas','Recife','Madrid'])[1 + g % 5], (array['PT','PT','BR','BR','ES'])[1 + g % 5], g % 2 = 0, md5(g::text) from generate_series(1, 1000) g;
insert into Fruit (id, name, color, weight, price, harvested, farm_id, code, createdAt, deliveredAt) select 1000 + g, (array['Apple','Banana','Orange','Lime','Pear','Grape','Mango','Kiwi'])[1 + g % 8] || ' ' || g, (array['RED','GREEN','YELLOW','ORANGE'])[1 + g % 4], 10 + g % 400, round(0.5 + (g % 1000) / 100.0, 2), case when g % 10 = 0 then null else date '2025-01-01' + g % 670 end, case when g % 20 = 0 then null else 1001 + g % 1000 end, gen_random_uuid(), timestamptz '2025-01-01 00:00:00+00' + g * interval '5 minutes', case when g % 3 = 0 then null else timestamptz '2025-01-01 00:00:00+00' + g * interval '7 minutes' end from generate_series(1, 200000) g;
insert into Fruit_tags (Fruit_id, tags) select 1000 + g, (array['fresh','promo','imported','organic','seasonal'])[1 + g % 5] from generate_series(1, 200000) g;
insert into Fruit_tags (Fruit_id, tags) select 1000 + g, 'bulk' from generate_series(1, 200000, 3) g;
alter sequence Farm_SEQ restart with 10000;
alter sequence Fruit_SEQ restart with 300000;
analyze;
