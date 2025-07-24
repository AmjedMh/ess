-- public.payment_method definition

-- Drop table

-- DROP TABLE public.payment_method;

CREATE TABLE public.payment_method (
                                       id int8 NOT NULL,
                                       "version" int8 NULL,
                                       created_date timestamp(6) NULL,
                                       last_modified_date timestamp(6) NULL,
                                       code varchar(255) NULL,
                                       customer_account_id int8 NULL,
                                       description varchar(255) NULL,
                                       created_by_id int8 NULL,
                                       last_modified_by_id int8 NULL,
                                       CONSTRAINT payment_method_pkey PRIMARY KEY (id)
);


-- public.payment_method foreign keys

ALTER TABLE public.payment_method ADD CONSTRAINT fk5q1lmxs76xvectcpb8ubg72kg FOREIGN KEY (last_modified_by_id) REFERENCES public.ess_user(id);
ALTER TABLE public.payment_method ADD CONSTRAINT fk8dwncmnh603qa4m9ylpfmb7ka FOREIGN KEY (customer_account_id) REFERENCES public.customer_account(id);
ALTER TABLE public.payment_method ADD CONSTRAINT fkn3hhuhq4eqa78xsc76a6q8g6c FOREIGN KEY (created_by_id) REFERENCES public.ess_user(id);

-- public.payment_method_seq definition

-- DROP SEQUENCE public.payment_method_seq;

CREATE SEQUENCE public.payment_method_seq
    INCREMENT BY 50
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
	CACHE 1
	NO CYCLE;
