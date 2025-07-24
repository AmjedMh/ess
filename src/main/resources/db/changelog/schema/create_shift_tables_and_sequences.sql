-- public.pump_attendant definition

-- Drop table

-- DROP TABLE public.pump_attendant;

CREATE TABLE public.pump_attendant (
                                       id int8 NOT NULL,
                                       "version" int8 NULL,
                                       created_date timestamp(6) NULL,
                                       last_modified_date timestamp(6) NULL,
                                       actif bool NULL,
                                       date_status_change timestamp(6) NOT NULL,
                                       first_name varchar(255) NULL,
                                       last_name varchar(255) NULL,
                                       matricule varchar(255) NULL,
                                       phone varchar(255) NULL,
                                       photo varchar(255) NULL,
                                       station_id int8 NULL,
                                       tag varchar(255) NULL,
                                       created_by_id int8 NULL,
                                       last_modified_by_id int8 NULL,
                                       CONSTRAINT pump_attendant_pkey PRIMARY KEY (id)
);

-- public.pump_attendant_team definition

-- Drop table

-- DROP TABLE public.pump_attendant_team;

CREATE TABLE public.pump_attendant_team (
                                            id int8 NOT NULL,
                                            "version" int8 NULL,
                                            created_date timestamp(6) NULL,
                                            last_modified_date timestamp(6) NULL,
                                            "name" varchar(255) NULL,
                                            station_id int8 NULL,
                                            created_by_id int8 NULL,
                                            last_modified_by_id int8 NULL,
                                            CONSTRAINT pump_attendant_team_pkey PRIMARY KEY (id)
);

-- public.affected_pump_attendant definition

-- Drop table

-- DROP TABLE public.affected_pump_attendant;

CREATE TABLE public.affected_pump_attendant (
                                                id int8 NOT NULL,
                                                "version" int8 NULL,
                                                created_date timestamp(6) NULL,
                                                last_modified_date timestamp(6) NULL,
                                                pump_attendant_id int8 NULL,
                                                pump_attendant_team_id int8 NULL,
                                                pump_id int8 NULL,
                                                created_by_id int8 NULL,
                                                last_modified_by_id int8 NULL,
                                                CONSTRAINT affected_pump_attendant_pkey PRIMARY KEY (id)
);

-- DROP TABLE public.payment_mean;

CREATE TABLE public.payment_mean (
                                     id int8 NOT NULL,
                                     "version" int8 NULL,
                                     created_date timestamp(6) NULL,
                                     last_modified_date timestamp(6) NULL,
                                     code varchar(255) NULL,
                                     customer_account_id int8 NULL,
                                     description varchar(255) NULL,
                                     created_by_id int8 NULL,
                                     last_modified_by_id int8 NULL,
                                     CONSTRAINT payment_mean_pkey PRIMARY KEY (id)
);




-- public.pump_attendant_collection_sheet definition

-- Drop table

-- DROP TABLE public.pump_attendant_collection_sheet;

CREATE TABLE public.pump_attendant_collection_sheet (
                                                        id int8 NOT NULL,
                                                        "version" int8 NULL,
                                                        created_date timestamp(6) NULL,
                                                        last_modified_date timestamp(6) NULL,
                                                        amount numeric(38, 2) NULL,
                                                        payment_mean_id int8 NULL,
                                                        pump_attendant_id int8 NULL,
                                                        shift_planning_execution_detail_id int8 NULL,
                                                        created_by_id int8 NULL,
                                                        last_modified_by_id int8 NULL,
                                                        CONSTRAINT pump_attendant_collection_sheet_pkey PRIMARY KEY (id)
);



-- public.shift_rotation definition

-- Drop table

-- DROP TABLE public.shift_rotation;

CREATE TABLE public.shift_rotation (
                                       id int8 NOT NULL,
                                       "version" int8 NULL,
                                       created_date timestamp(6) NULL,
                                       last_modified_date timestamp(6) NULL,
                                       end_validity_date date NULL,
                                       "name" varchar(255) NULL,
                                       start_validity_date date NULL,
                                       station_id int8 NULL,
                                       created_by_id int8 NULL,
                                       last_modified_by_id int8 NULL,
                                       CONSTRAINT shift_rotation_pkey PRIMARY KEY (id)
);


-- public.shift definition

-- Drop table

-- DROP TABLE public.shift;

CREATE TABLE public.shift (
                              id int8 NOT NULL,
                              "version" int8 NULL,
                              created_date timestamp(6) NULL,
                              last_modified_date timestamp(6) NULL,
                              ending_time time NULL,
                              "index" int4 NOT NULL,
                              "name" varchar(255) NULL,
                              off_day bool NULL,
                              starting_time time NULL,
                              created_by_id int8 NULL,
                              last_modified_by_id int8 NULL,
                              shift_rotation_id int8 NULL,
                              CONSTRAINT shift_pkey PRIMARY KEY (id)
);


-- public.shift_planning definition

-- Drop table

-- DROP TABLE public.shift_planning;

CREATE TABLE public.shift_planning (
                                       id int8 NOT NULL,
                                       "version" int8 NULL,
                                       created_date timestamp(6) NULL,
                                       last_modified_date timestamp(6) NULL,
                                       "day" date NULL,
                                       pump_attendant_team_id int8 NULL,
                                       shift_id int8 NULL,
                                       shift_rtotation_id int8 NULL,
                                       station_id int8 NULL,
                                       created_by_id int8 NULL,
                                       last_modified_by_id int8 NULL,
                                       shift_rotation_id int8 NULL,
                                       CONSTRAINT shift_planning_pkey PRIMARY KEY (id)
);


-- public.shift_planning_execution definition

-- Drop table

-- DROP TABLE public.shift_planning_execution;

CREATE TABLE public.shift_planning_execution (
                                                 id int8 NOT NULL,
                                                 "version" int8 NULL,
                                                 created_date timestamp(6) NULL,
                                                 last_modified_date timestamp(6) NULL,
                                                 end_date_time timestamp(6) NULL,
                                                 shift_id int8 NULL,
                                                 shift_planning_id int8 NULL,
                                                 start_date_time timestamp(6) NULL,
                                                 created_by_id int8 NULL,
                                                 last_modified_by_id int8 NULL,
                                                 CONSTRAINT shift_planning_execution_pkey PRIMARY KEY (id)
);


-- public.shift_planning_execution_detail definition

-- Drop table

-- DROP TABLE public.shift_planning_execution_detail;

CREATE TABLE public.shift_planning_execution_detail (
                                                        id int8 NOT NULL,
                                                        "version" int8 NULL,
                                                        created_date timestamp(6) NULL,
                                                        last_modified_date timestamp(6) NULL,
                                                        end_index numeric(38, 2) NULL,
                                                        nozzle_id int8 NULL,
                                                        pump_attendant_id int8 NULL,
                                                        pump_id int8 NULL,
                                                        shift_planning_execution_id int8 NULL,
                                                        start_index numeric(38, 2) NULL,
                                                        tank_return numeric(38, 2) NULL,
                                                        total_amount numeric(38, 2) NULL,
                                                        total_volume numeric(38, 2) NULL,
                                                        created_by_id int8 NULL,
                                                        last_modified_by_id int8 NULL,
                                                        CONSTRAINT shift_planning_execution_detail_pkey PRIMARY KEY (id)
);



-----------
-- SEQUENCES ---
----------------
-- public.affected_pump_attendant_seq definition

-- DROP SEQUENCE public.affected_pump_attendant_seq;

CREATE SEQUENCE public.affected_pump_attendant_seq
    INCREMENT BY 50
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
	CACHE 1
	NO CYCLE;

-- public.payment_mean_seq definition

-- DROP SEQUENCE public.payment_mean_seq;

CREATE SEQUENCE public.payment_mean_seq
    INCREMENT BY 50
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
	CACHE 1
	NO CYCLE;



-- public.pump_attendant_collection_sheet_seq definition

-- DROP SEQUENCE public.pump_attendant_collection_sheet_seq;

CREATE SEQUENCE public.pump_attendant_collection_sheet_seq
    INCREMENT BY 50
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
	CACHE 1
	NO CYCLE;


-- public.pump_attendant_seq definition

-- DROP SEQUENCE public.pump_attendant_seq;

CREATE SEQUENCE public.pump_attendant_seq
    INCREMENT BY 50
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
	CACHE 1
	NO CYCLE;


-- public.pump_attendant_team_seq definition

-- DROP SEQUENCE public.pump_attendant_team_seq;

CREATE SEQUENCE public.pump_attendant_team_seq
    INCREMENT BY 50
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
	CACHE 1
	NO CYCLE;



-- public.shift_planning_execution_detail_seq definition

-- DROP SEQUENCE public.shift_planning_execution_detail_seq;

CREATE SEQUENCE public.shift_planning_execution_detail_seq
    INCREMENT BY 50
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
	CACHE 1
	NO CYCLE;


-- public.shift_planning_execution_seq definition

-- DROP SEQUENCE public.shift_planning_execution_seq;

CREATE SEQUENCE public.shift_planning_execution_seq
    INCREMENT BY 50
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
	CACHE 1
	NO CYCLE;


-- public.shift_planning_seq definition

-- DROP SEQUENCE public.shift_planning_seq;

CREATE SEQUENCE public.shift_planning_seq
    INCREMENT BY 50
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
	CACHE 1
	NO CYCLE;


-- public.shift_rotation_seq definition

-- DROP SEQUENCE public.shift_rotation_seq;

CREATE SEQUENCE public.shift_rotation_seq
    INCREMENT BY 50
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
	CACHE 1
	NO CYCLE;


-- public.shift_seq definition

-- DROP SEQUENCE public.shift_seq;

CREATE SEQUENCE public.shift_seq
    INCREMENT BY 50
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
	CACHE 1
	NO CYCLE;
