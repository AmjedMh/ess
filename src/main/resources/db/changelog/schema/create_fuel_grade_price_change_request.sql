-- public.fuel_grade_price_change_request definition

-- Drop table

-- DROP TABLE public.fuel_grade_price_change_request;

CREATE TABLE public.fuel_grade_price_change_request (
                                                        id int8 NOT NULL,
                                                        "version" int8 NULL,
                                                        created_date timestamp(6) NULL,
                                                        last_modified_date timestamp(6) NULL,
                                                        execution_date timestamp(6) NULL,
                                                        last_trial_date timestamp(6) NULL,
                                                        planned_date timestamp(6) NULL,
                                                        scheduled_date timestamp(6) NULL,
                                                        request_type varchar(255) NULL,
                                                        requester_id int8 NULL,
                                                        station_id int8 NULL,
                                                        status varchar(255) NULL,
                                                        old_price numeric(38, 2) NULL,
                                                        new_price numeric(38, 2) NULL,
                                                        created_by_id int8 NULL,
                                                        last_modified_by_id int8 NULL,
                                                        fuel_grade_id int8 NULL,
                                                        CONSTRAINT fuel_grade_price_change_request_pkey PRIMARY KEY (id)
);


-- public.fuel_grade_price_change_request foreign keys

ALTER TABLE public.fuel_grade_price_change_request ADD CONSTRAINT fk26f6y13onfhwpt3rfagljw54i FOREIGN KEY (fuel_grade_id) REFERENCES public.fuel_grade(id);
ALTER TABLE public.fuel_grade_price_change_request ADD CONSTRAINT fk6mk157qnrajojghji7p8ajk1e FOREIGN KEY (station_id) REFERENCES public.station(id);
ALTER TABLE public.fuel_grade_price_change_request ADD CONSTRAINT fk79hj6y2i38jgrdqebwckqdo44 FOREIGN KEY (last_modified_by_id) REFERENCES public.ess_user(id);
ALTER TABLE public.fuel_grade_price_change_request ADD CONSTRAINT fkiy2vt1evfrq1jtwo0vt84gfgm FOREIGN KEY (created_by_id) REFERENCES public.ess_user(id);
ALTER TABLE public.fuel_grade_price_change_request ADD CONSTRAINT fkqo50i064lrpbon1jq8u3vgheq FOREIGN KEY (requester_id) REFERENCES public.ess_user(id);
