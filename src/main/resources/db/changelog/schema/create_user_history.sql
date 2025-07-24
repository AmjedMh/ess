-- Drop table

DROP TABLE public.user_history;

CREATE TABLE public.user_history (
                                     id int8 NOT NULL,
                                     "version" int8 NULL,
                                     activity_date timestamp(6) NULL,
                                     activity_type varchar(255) NULL,
                                     controller_pts_id int8 NULL,
                                     ip_address varchar(255) NULL,
                                     request_handler varchar(255) NULL,
                                     requesturi varchar(255) NULL,
                                     user_id int8 NULL,
                                     CONSTRAINT user_history_pkey PRIMARY KEY (id)
);
