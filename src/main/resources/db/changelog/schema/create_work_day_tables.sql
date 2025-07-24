-- public.work_day_shift_planning definition

-- Drop table

-- DROP TABLE public.work_day_shift_planning;

CREATE TABLE public.work_day_shift_planning (
                                                "day" date NULL,
                                                created_by_id int8 NULL,
                                                created_date timestamp(6) NULL,
                                                id int8 NOT NULL,
                                                last_modified_by_id int8 NULL,
                                                last_modified_date timestamp(6) NULL,
                                                shift_rotation_id int8 NULL,
                                                station_id int8 NULL,
                                                "version" int8 NULL,
                                                CONSTRAINT work_day_shift_planning_pkey PRIMARY KEY (id)
);


-- public.work_day_shift_planning_execution definition

-- Drop table

-- DROP TABLE public.work_day_shift_planning_execution;

CREATE TABLE public.work_day_shift_planning_execution (
                                                          created_by_id int8 NULL,
                                                          created_date timestamp(6) NULL,
                                                          id int8 NOT NULL,
                                                          last_modified_by_id int8 NULL,
                                                          last_modified_date timestamp(6) NULL,
                                                          "version" int8 NULL,
                                                          work_day_shift_planning_id int8 NULL,
                                                          CONSTRAINT work_day_shift_planning_execution_pkey PRIMARY KEY (id)
);


-- public.work_day_shift_planning foreign keys

ALTER TABLE public.work_day_shift_planning ADD CONSTRAINT fk5jvl89t7l3ll8jooyv5geudyw FOREIGN KEY (station_id) REFERENCES public.station(id);
ALTER TABLE public.work_day_shift_planning ADD CONSTRAINT fka3e07j6ed0es8r1iilg45ppgx FOREIGN KEY (shift_rotation_id) REFERENCES public.shift_rotation(id);
ALTER TABLE public.work_day_shift_planning ADD CONSTRAINT fkc1g98d8yxbbo2ub2e3lf3so5f FOREIGN KEY (last_modified_by_id) REFERENCES public.ess_user(id);
ALTER TABLE public.work_day_shift_planning ADD CONSTRAINT fkiipcvxh0yff4d544vr7xil0le FOREIGN KEY (created_by_id) REFERENCES public.ess_user(id);


-- public.work_day_shift_planning_execution foreign keys

ALTER TABLE public.work_day_shift_planning_execution ADD CONSTRAINT fk4lqf58v6qfqfs5gl0hellb4xt FOREIGN KEY (work_day_shift_planning_id) REFERENCES public.work_day_shift_planning(id);
ALTER TABLE public.work_day_shift_planning_execution ADD CONSTRAINT fkc1jchpe0n85tovnx9ol4yal1e FOREIGN KEY (created_by_id) REFERENCES public.ess_user(id);
ALTER TABLE public.work_day_shift_planning_execution ADD CONSTRAINT fklx5ise2kwcw81ifkt4uyqfhdv FOREIGN KEY (last_modified_by_id) REFERENCES public.ess_user(id);

-- creation des sequences
-- public.work_day_shift_planning_execution_seq definition

-- DROP SEQUENCE public.work_day_shift_planning_execution_seq;

CREATE SEQUENCE public.work_day_shift_planning_execution_seq
    INCREMENT BY 50
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
	CACHE 1
	NO CYCLE;


-- public.work_day_shift_planning_seq definition

-- DROP SEQUENCE public.work_day_shift_planning_seq;

CREATE SEQUENCE public.work_day_shift_planning_seq
    INCREMENT BY 50
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
	CACHE 1
	NO CYCLE;
