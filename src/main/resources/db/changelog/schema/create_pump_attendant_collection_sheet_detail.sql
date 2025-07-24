-- public.pump_attendant_collection_sheet_detail definition

-- Drop table

-- DROP TABLE public.pump_attendant_collection_sheet_detail;

CREATE TABLE public.pump_attendant_collection_sheet_detail (
                                                               id int8 NOT NULL,
                                                               "version" int8 NULL,
                                                               created_date timestamp(6) NULL,
                                                               last_modified_date timestamp(6) NULL,
                                                               amount numeric(38, 2) NULL,
                                                               payment_mean_id int8 NULL,
                                                               pump_attendant_collection_sheet_id int8 NULL,
                                                               created_by_id int8 NULL,
                                                               last_modified_by_id int8 NULL,
                                                               CONSTRAINT pump_attendant_collection_sheet_detail_pkey PRIMARY KEY (id)
);


-- public.pump_attendant_collection_sheet_detail foreign keys

ALTER TABLE public.pump_attendant_collection_sheet_detail ADD CONSTRAINT fk7403vs0pbikyc8chaw3n5wwg FOREIGN KEY (pump_attendant_collection_sheet_id) REFERENCES public.pump_attendant_collection_sheet(id);
ALTER TABLE public.pump_attendant_collection_sheet_detail ADD CONSTRAINT fk7asc7ypsrd8ldggmqufcylnnf FOREIGN KEY (payment_mean_id) REFERENCES public.payment_mean(id);
ALTER TABLE public.pump_attendant_collection_sheet_detail ADD CONSTRAINT fkk5ycg8yjut66ks5pya37y6v0b FOREIGN KEY (last_modified_by_id) REFERENCES public.ess_user(id);
ALTER TABLE public.pump_attendant_collection_sheet_detail ADD CONSTRAINT fko6lldrfcsab84a11gc739i048 FOREIGN KEY (created_by_id) REFERENCES public.ess_user(id);


CREATE SEQUENCE public.pump_attendant_collection_sheet_detail_seq
    INCREMENT BY 50
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
	CACHE 1
	NO CYCLE;
