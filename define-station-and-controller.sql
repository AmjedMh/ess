insert into station (id,address,"name", user_login) values (nextval('station_seq'), 'test adress','TOTAL', 'admin');

select * from station

insert into controller_pts (id,pts_id, station_id) values (nextval('controller_pts_seq'), '0027003A3438510935383135',1);

insert into userctr(id, login,"password",id_cont) values (nextval('userctr_id_seq'),'admin','admin',1);
