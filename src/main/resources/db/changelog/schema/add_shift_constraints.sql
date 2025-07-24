-- public.affected_pump_attendant foreign keys

ALTER TABLE public.affected_pump_attendant ADD CONSTRAINT fk1cle8uabb3ult82d6evkyapre FOREIGN KEY (pump_attendant_team_id) REFERENCES public.pump_attendant_team(id);
ALTER TABLE public.affected_pump_attendant ADD CONSTRAINT fk8dbr11y4qby7y1pq5rew6e5jn FOREIGN KEY (last_modified_by_id) REFERENCES public.ess_user(id);
ALTER TABLE public.affected_pump_attendant ADD CONSTRAINT fk8qnxxj914e931mcjfq84e8o6w FOREIGN KEY (created_by_id) REFERENCES public.ess_user(id);
ALTER TABLE public.affected_pump_attendant ADD CONSTRAINT fkm5rrn4t09q1swkpf3m5qveri3 FOREIGN KEY (pump_attendant_id) REFERENCES public.pump_attendant(id);
ALTER TABLE public.affected_pump_attendant ADD CONSTRAINT fknv1d75kte5gud30egq2cej58r FOREIGN KEY (pump_id) REFERENCES public.pump(id);

ALTER TABLE public.customer_account ADD CONSTRAINT fkbyv8djva5l9ku8uq9mmrlo1on FOREIGN KEY (creator_user_id) REFERENCES public.ess_user(id);
ALTER TABLE public.customer_account ADD CONSTRAINT fkgwifrtyniap78secumyiy7oe3 FOREIGN KEY (creator_account_id) REFERENCES public.customer_account(id);
ALTER TABLE public.customer_account ADD CONSTRAINT fktek7eot958qlenhyclb3chyhj FOREIGN KEY (parent_id) REFERENCES public.customer_account(id);


-- public.ess_user foreign keys

ALTER TABLE public.ess_user ADD CONSTRAINT fkb12db9wrrkg8ymm5fq87svr1b FOREIGN KEY (creator_account_id) REFERENCES public.customer_account(id);
-- public.payment_mean foreign keys

ALTER TABLE public.payment_mean ADD CONSTRAINT fk43iqpysnekd4his3cmiiqo5f4 FOREIGN KEY (customer_account_id) REFERENCES public.customer_account(id);
ALTER TABLE public.payment_mean ADD CONSTRAINT fkb0bhnso5l4hrugtbyv7c8hf30 FOREIGN KEY (created_by_id) REFERENCES public.ess_user(id);
ALTER TABLE public.payment_mean ADD CONSTRAINT fkqr4c801r8lhur5h0tgw3i6inj FOREIGN KEY (last_modified_by_id) REFERENCES public.ess_user(id);
-- public.pump_attendant foreign keys

ALTER TABLE public.pump_attendant ADD CONSTRAINT fkcwdrqovga8w36pkdqfv0iplkp FOREIGN KEY (last_modified_by_id) REFERENCES public.ess_user(id);
ALTER TABLE public.pump_attendant ADD CONSTRAINT fkm5l6kxacqsv5kk6wj8horh0sn FOREIGN KEY (station_id) REFERENCES public.station(id);
ALTER TABLE public.pump_attendant ADD CONSTRAINT fkpqss667vo97i1f1p15yfslhcj FOREIGN KEY (created_by_id) REFERENCES public.ess_user(id);


-- public.pump_attendant_collection_sheet foreign keys

ALTER TABLE public.pump_attendant_collection_sheet ADD CONSTRAINT fk6kaouu3nrwjlcxkekfags35c2 FOREIGN KEY (payment_mean_id) REFERENCES public.payment_mean(id);
ALTER TABLE public.pump_attendant_collection_sheet ADD CONSTRAINT fkdoiasm33n59vivyjpyfs0deht FOREIGN KEY (shift_planning_execution_detail_id) REFERENCES public.shift_planning_execution_detail(id);
ALTER TABLE public.pump_attendant_collection_sheet ADD CONSTRAINT fkeagrbu7fdfwmpxtjjnafkmroh FOREIGN KEY (last_modified_by_id) REFERENCES public.ess_user(id);
ALTER TABLE public.pump_attendant_collection_sheet ADD CONSTRAINT fklqh6sgpc1g845j08ho62ciilj FOREIGN KEY (pump_attendant_id) REFERENCES public.pump_attendant(id);
ALTER TABLE public.pump_attendant_collection_sheet ADD CONSTRAINT fktnvlpbcjn3skiifr9n4e6umm5 FOREIGN KEY (created_by_id) REFERENCES public.ess_user(id);


-- public.pump_attendant_team foreign keys

ALTER TABLE public.pump_attendant_team ADD CONSTRAINT fkb7rb5jp26uho3gfhubcdjhgc9 FOREIGN KEY (station_id) REFERENCES public.station(id);
ALTER TABLE public.pump_attendant_team ADD CONSTRAINT fklkuse1vjn0lkuae706r79r1ll FOREIGN KEY (created_by_id) REFERENCES public.ess_user(id);
ALTER TABLE public.pump_attendant_team ADD CONSTRAINT fkpkqxkbpyxe3mtmydwhbiearag FOREIGN KEY (last_modified_by_id) REFERENCES public.ess_user(id);

-- public.shift foreign keys

ALTER TABLE public.shift ADD CONSTRAINT fk5rwls3nl1ccd95h4l8oln2ixt FOREIGN KEY (last_modified_by_id) REFERENCES public.ess_user(id);
ALTER TABLE public.shift ADD CONSTRAINT fkg6o37jbu2rh65yfmn8l6dxurq FOREIGN KEY (shift_rotation_id) REFERENCES public.shift_rotation(id);
ALTER TABLE public.shift ADD CONSTRAINT fkq8lyf5cy59ma3etukwlge1ycw FOREIGN KEY (created_by_id) REFERENCES public.ess_user(id);


-- public.shift_planning foreign keys

ALTER TABLE public.shift_planning ADD CONSTRAINT fk1qpboy0c2pjmfd6cou5blivxd FOREIGN KEY (created_by_id) REFERENCES public.ess_user(id);
ALTER TABLE public.shift_planning ADD CONSTRAINT fk1u7fvrer4t7isgi441giyaicv FOREIGN KEY (last_modified_by_id) REFERENCES public.ess_user(id);
ALTER TABLE public.shift_planning ADD CONSTRAINT fk78pqoif7544mvp986bnhh5r8c FOREIGN KEY (pump_attendant_team_id) REFERENCES public.pump_attendant_team(id);
ALTER TABLE public.shift_planning ADD CONSTRAINT fkdhyaqawk73apegg91e3v1g8sj FOREIGN KEY (station_id) REFERENCES public.station(id);
ALTER TABLE public.shift_planning ADD CONSTRAINT fkn7sk7o6gwco0jl7aitbog5n7t FOREIGN KEY (shift_id) REFERENCES public.shift(id);
ALTER TABLE public.shift_planning ADD CONSTRAINT fkrej9hxmjmb94b6yfgoh67iu5k FOREIGN KEY (shift_rotation_id) REFERENCES public.shift_rotation(id);


-- public.shift_planning_execution foreign keys

ALTER TABLE public.shift_planning_execution ADD CONSTRAINT fk8rwxqwv1e20f92qit86h4apwq FOREIGN KEY (last_modified_by_id) REFERENCES public.ess_user(id);
ALTER TABLE public.shift_planning_execution ADD CONSTRAINT fkcoklhq1lu4wv99f5uadmw63lw FOREIGN KEY (created_by_id) REFERENCES public.ess_user(id);
ALTER TABLE public.shift_planning_execution ADD CONSTRAINT fkh0vh9pus26vfk902ynn412v1k FOREIGN KEY (shift_id) REFERENCES public.shift(id);
ALTER TABLE public.shift_planning_execution ADD CONSTRAINT fktdiw9me1xvo097futxok7ml8 FOREIGN KEY (shift_planning_id) REFERENCES public.shift_planning(id);


-- public.shift_planning_execution_detail foreign keys

ALTER TABLE public.shift_planning_execution_detail ADD CONSTRAINT fk13uc8ju2wx1j4bg9w5gcgnttk FOREIGN KEY (pump_id) REFERENCES public.pump(id);
ALTER TABLE public.shift_planning_execution_detail ADD CONSTRAINT fk55hdsutrwe6mndu18h90bdhfn FOREIGN KEY (last_modified_by_id) REFERENCES public.ess_user(id);
ALTER TABLE public.shift_planning_execution_detail ADD CONSTRAINT fk6hg4uwaq2m11tnbjv8fyx8v2j FOREIGN KEY (pump_attendant_id) REFERENCES public.pump_attendant(id);
ALTER TABLE public.shift_planning_execution_detail ADD CONSTRAINT fkfnaw2c52sr1fh42duevijjldi FOREIGN KEY (created_by_id) REFERENCES public.ess_user(id);
ALTER TABLE public.shift_planning_execution_detail ADD CONSTRAINT fkieyv2akqjdjb26eggclhoupnm FOREIGN KEY (nozzle_id) REFERENCES public.nozzle(id);
ALTER TABLE public.shift_planning_execution_detail ADD CONSTRAINT fkqjejymkiaktkni2v14tfcr2d3 FOREIGN KEY (shift_planning_execution_id) REFERENCES public.shift_planning_execution(id);


-- public.shift_rotation foreign keys

ALTER TABLE public.shift_rotation ADD CONSTRAINT fkfm3hct0dxqh7beuj28170ct9p FOREIGN KEY (station_id) REFERENCES public.station(id);
ALTER TABLE public.shift_rotation ADD CONSTRAINT fkofktq1od0x9rwc9wl2v0q2iil FOREIGN KEY (last_modified_by_id) REFERENCES public.ess_user(id);
ALTER TABLE public.shift_rotation ADD CONSTRAINT fkpp8jiw6f90tqmjcmffbk905y7 FOREIGN KEY (created_by_id) REFERENCES public.ess_user(id);

ALTER TABLE public.station ADD CONSTRAINT fk4xnx9vio3cbd4q69nkebwudqa FOREIGN KEY (creator_account_id) REFERENCES public.customer_account(id);

