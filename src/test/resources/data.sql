INSERT INTO public.customer_account
(id, description, name, status, master_user_id,version,actif, date_status_change)
VALUES(1, 'mazraa acount', 'mazraa', 'ENABLED', null,0, true, now());
INSERT INTO ess_user(id, username, customer_account_id,user_type,version,actif, date_status_change) VALUES(1, 'ctr', 1,'CONTROLLER',0, true, now());
INSERT INTO ess_user(id, username, customer_account_id,user_type,version,actif, date_status_change) VALUES(2, 'mazraa', 1,'APPLICATION',0, true, now());

INSERT INTO public.currency
(id, code, locale, name)
VALUES(1, 'TND', 'ar_tn', 'Dinars Tunisien');

INSERT INTO public.currency
(id, code, locale, name)
VALUES(2, 'EUR', 'fr_FR', 'Euro');

INSERT INTO public.country
(id, code, name, currency_id)
VALUES(1, 'TN', 'Tunisie', 1);

INSERT INTO public.country
(id, code, name, currency_id)
VALUES(2, 'FR', 'France', 2);

insert into station (id,address,name,actif,DATE_STATUS_CHANGE,customer_account_id, version) values (nextval('station_seq'), 'test adress','TOTAL',true,now(),1,0);

insert into controller_pts (id,pts_id, station_id,version) values (nextval('controller_pts_seq'), '0027003A3438510935383135',1,0);



