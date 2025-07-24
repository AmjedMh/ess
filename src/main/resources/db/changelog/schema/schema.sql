
-- DROP TABLE public.ess_user;

CREATE TABLE public.ess_user (
                                 id int8 NOT NULL,
                                 created_date timestamp(6) NULL,
                                 last_modified_date timestamp(6) NULL,
                                 "version" int8 NULL,
                                 actif bool NULL,
                                 date_status_change timestamp(6) NOT NULL,
                                 customer_account_id int8 NULL,
                                 user_type varchar(255) NULL,
                                 username varchar(255) NULL,
                                 created_by_id int8 NULL,
                                 last_modified_by_id int8 NULL,
                                 CONSTRAINT ess_user_pkey PRIMARY KEY (id),
                                 CONSTRAINT fkm6y0ppk0bpqmdcxbylr5vn4e3 FOREIGN KEY (created_by_id) REFERENCES public.ess_user(id),
                                 CONSTRAINT fkmaqpfwaj5xuxcqp6ybvwv3dcu FOREIGN KEY (last_modified_by_id) REFERENCES public.ess_user(id)
);

-- Drop table

-- DROP TABLE public.customer_account;

CREATE TABLE public.customer_account (
                                         id int8 NOT NULL,
                                         created_date timestamp(6) NULL,
                                         last_modified_date timestamp(6) NULL,
                                         "version" int8 NULL,
                                         actif bool NULL,
                                         date_status_change timestamp(6) NOT NULL,
                                         description varchar(255) NULL,
                                         master_user_id int8 NULL,
                                         "name" varchar(255) NOT NULL,
                                         status varchar(255) NULL,
                                         created_by_id int8 NULL,
                                         last_modified_by_id int8 NULL,
                                         CONSTRAINT customer_account_pkey PRIMARY KEY (id),
                                         CONSTRAINT fk3l6nay7hlguv3dfqtqicpswmy FOREIGN KEY (master_user_id) REFERENCES public.ess_user(id),
                                         CONSTRAINT fkmh4skaee542oju6apfavwv940 FOREIGN KEY (last_modified_by_id) REFERENCES public.ess_user(id),
                                         CONSTRAINT fkqe3d1shxxx5vupr83e4uifkd7 FOREIGN KEY (created_by_id) REFERENCES public.ess_user(id)
);

alter table public.ess_user add
CONSTRAINT fkorwuwukhvlx1knwp702qkcq25 FOREIGN KEY (customer_account_id) REFERENCES public.customer_account(id);

-- Drop table

-- DROP TABLE public.alert;

CREATE TABLE public.alert (
                              id int8 NOT NULL,
                              created_date timestamp(6) NULL,
                              last_modified_date timestamp(6) NULL,
                              "version" int8 NULL,
                              alert_description varchar(255) NULL,
                              code int8 NULL,
                              configuration_id varchar(255) NULL,
                              date_time varchar(255) NULL,
                              device_number int8 NULL,
                              device_type varchar(255) NULL,
                              state varchar(255) NULL,
                              status varchar(255) NULL,
                              created_by_id int8 NULL,
                              last_modified_by_id int8 NULL,
                              controller_pts_id int8 NULL,
                              CONSTRAINT alert_pkey PRIMARY KEY (id),
                              CONSTRAINT fk5yg9j3to8m3yxkkspnmkustfa FOREIGN KEY (last_modified_by_id) REFERENCES public.ess_user(id),
                              CONSTRAINT fkmvw1pfledekt6lqj4ebrenxoe FOREIGN KEY (created_by_id) REFERENCES public.ess_user(id)
);

-- Drop table

-- DROP TABLE public.controller_pts;

CREATE TABLE public.controller_pts (
                                       id int8 NOT NULL,
                                       created_date timestamp(6) NULL,
                                       last_modified_date timestamp(6) NULL,
                                       "version" int8 NULL,
                                       current_configuration_id int8 NULL,
                                       current_firmware_information_id int8 NULL,
                                       pts_id varchar(255) NOT NULL,
                                       station_id int8 NULL,
                                       user_controller_id int8 NULL,
                                       created_by_id int8 NULL,
                                       last_modified_by_id int8 NULL,
                                       CONSTRAINT controller_pts_pkey PRIMARY KEY (id),
                                       CONSTRAINT uk_dscoemr6xcq0tvicfrbhmv2e5 UNIQUE (pts_id),
                                       CONSTRAINT fk8ni7wov1x9glpm3cowkomrcir FOREIGN KEY (created_by_id) REFERENCES public.ess_user(id),
                                       CONSTRAINT fkekt4qpyypk873jsxxgdggeveb FOREIGN KEY (user_controller_id) REFERENCES public.ess_user(id),
                                       CONSTRAINT fkmtuomtpulhonnipvl5nsulbcd FOREIGN KEY (last_modified_by_id) REFERENCES public.ess_user(id)
);

alter table public.alert
add CONSTRAINT fkirvjcxdjv9ut2cifbc2yx77gk FOREIGN KEY (controller_pts_id) REFERENCES public.controller_pts(id);



-- Drop table

-- DROP TABLE public.controller_pts_configuration;

CREATE TABLE public.controller_pts_configuration (
                                                     id int8 NOT NULL,
                                                     created_date timestamp(6) NULL,
                                                     last_modified_date timestamp(6) NULL,
                                                     "version" int8 NULL,
                                                     configuration_id varchar(255) NOT NULL,
                                                     last_configuration_update timestamp(6) NULL,
                                                     pts_id varchar(255) NOT NULL,
                                                     created_by_id int8 NULL,
                                                     last_modified_by_id int8 NULL,
                                                     controller_pts_id int8 NULL,
                                                     CONSTRAINT controller_pts_configuration_pkey PRIMARY KEY (id),
                                                     CONSTRAINT uksvtgyxwiqfp3ep41kahp4pjrj UNIQUE (pts_id, configuration_id),
                                                     CONSTRAINT fkaqiyh3qbnntr6ijwwcae3es6v FOREIGN KEY (controller_pts_id) REFERENCES public.controller_pts(id),
                                                     CONSTRAINT fkbblv31b30hjmo6ihs7pn6wof FOREIGN KEY (created_by_id) REFERENCES public.ess_user(id),
                                                     CONSTRAINT fkiv691ueiwpu0exte4ymrdx6sb FOREIGN KEY (last_modified_by_id) REFERENCES public.ess_user(id)
);

-- Drop table

-- DROP TABLE public.country;

CREATE TABLE public.country (
                                id int8 NOT NULL,
                                created_date timestamp(6) NULL,
                                last_modified_date timestamp(6) NULL,
                                "version" int8 NULL,
                                code varchar(255) NULL,
                                "name" varchar(255) NULL,
                                created_by_id int8 NULL,
                                last_modified_by_id int8 NULL,
                                currency_id int8 NULL,
                                CONSTRAINT country_pkey PRIMARY KEY (id),
                                CONSTRAINT fk1p5sdn69hf3c2fexjf388x3ie FOREIGN KEY (last_modified_by_id) REFERENCES public.ess_user(id),
                                CONSTRAINT fk97cs794ht84l5hrej3rnj851v FOREIGN KEY (created_by_id) REFERENCES public.ess_user(id)
);


-- Drop table

-- DROP TABLE public.currency;

CREATE TABLE public.currency (
                                 id int8 NOT NULL,
                                 created_date timestamp(6) NULL,
                                 last_modified_date timestamp(6) NULL,
                                 "version" int8 NULL,
                                 code varchar(255) NULL,
                                 locale varchar(255) NULL,
                                 "name" varchar(255) NULL,
                                 created_by_id int8 NULL,
                                 last_modified_by_id int8 NULL,
                                 CONSTRAINT currency_pkey PRIMARY KEY (id),
                                 CONSTRAINT fk1201v9tkeuru8ru9n60et82bc FOREIGN KEY (last_modified_by_id) REFERENCES public.ess_user(id),
                                 CONSTRAINT fkiksmllg7takf5uleinsl2mbw9 FOREIGN KEY (created_by_id) REFERENCES public.ess_user(id)
);

alter table public.country
    add CONSTRAINT fkfnsa0kdnu5cg50irup7h29tmj FOREIGN KEY (currency_id) REFERENCES public.currency(id);

-- Drop table

-- DROP TABLE public.custom_function_group;

CREATE TABLE public.custom_function_group (
                                              id int8 NOT NULL,
                                              created_date timestamp(6) NULL,
                                              last_modified_date timestamp(6) NULL,
                                              "version" int8 NULL,
                                              code varchar(255) NOT NULL,
                                              description varchar(255) NULL,
                                              related_user_id int8 NULL,
                                              "scope" varchar(255) NULL,
                                              created_by_id int8 NULL,
                                              last_modified_by_id int8 NULL,
                                              CONSTRAINT custom_function_group_pkey PRIMARY KEY (id),
                                              CONSTRAINT fk4d5kwn8pfigw7sckw5irkrmhr FOREIGN KEY (related_user_id) REFERENCES public.ess_user(id),
                                              CONSTRAINT fkbvikcudx72hc8wor1agkxurqx FOREIGN KEY (last_modified_by_id) REFERENCES public.ess_user(id),
                                              CONSTRAINT fksxysi8yylgw146dyrgiq9mtp9 FOREIGN KEY (created_by_id) REFERENCES public.ess_user(id)
);

-- Drop table

-- DROP TABLE public.custom_function_group_functions;

CREATE TABLE public.custom_function_group_functions (
                                                        user_id int8 NOT NULL,
                                                        function_id int8 NOT NULL,
                                                        CONSTRAINT custom_function_group_functions_pkey PRIMARY KEY (user_id, function_id),
                                                        CONSTRAINT fkera7v93dce6jsqicj2eal9syd FOREIGN KEY (user_id) REFERENCES public.custom_function_group(id)
);



-- Drop table



-- Drop table

-- DROP TABLE public.exchanged_packets;

CREATE TABLE public.exchanged_packets (
                                          id int8 NOT NULL,
                                          created_date timestamp(6) NULL,
                                          last_modified_date timestamp(6) NULL,
                                          "version" int8 NULL,
                                          packet_id int8 NULL,
                                          packet_type varchar(255) NULL,
                                          pts_id varchar(255) NULL,
                                          request_date timestamp(6) NULL,
                                          response_date timestamp(6) NULL,
                                          response_packet varchar(255) NULL,
                                          sent_packet varchar(255) NULL,
                                          created_by_id int8 NULL,
                                          last_modified_by_id int8 NULL,
                                          CONSTRAINT exchanged_packets_pkey PRIMARY KEY (id),
                                          CONSTRAINT ukax1ybnvysbeuikel2o3pwypnd UNIQUE (pts_id, packet_id),
                                          CONSTRAINT fk3kqn55gbb7hiinjqdybcwsk8c FOREIGN KEY (last_modified_by_id) REFERENCES public.ess_user(id),
                                          CONSTRAINT fkiqw7sra5y8ddp7q0utesd1jwx FOREIGN KEY (created_by_id) REFERENCES public.ess_user(id)
);

-- Drop table

-- DROP TABLE public.firmware_information;

CREATE TABLE public.firmware_information (
                                             id int8 NOT NULL,
                                             created_date timestamp(6) NULL,
                                             last_modified_date timestamp(6) NULL,
                                             "version" int8 NULL,
                                             date_time varchar(255) NULL,
                                             modification_date timestamp(6) NULL,
                                             pts_id varchar(255) NULL,
                                             version_state bool NOT NULL,
                                             created_by_id int8 NULL,
                                             last_modified_by_id int8 NULL,
                                             controller_pts_id int8 NULL,
                                             CONSTRAINT firmware_information_pkey PRIMARY KEY (id),
                                             CONSTRAINT ukmlt1k2c714nhd6u1or4wyo0je UNIQUE (pts_id, date_time),
                                             CONSTRAINT fk5jtlg5i41bnar5ocblpu1p4e FOREIGN KEY (controller_pts_id) REFERENCES public.controller_pts(id),
                                             CONSTRAINT fk5osetqcxrnxc29s4g4ac39s43 FOREIGN KEY (created_by_id) REFERENCES public.ess_user(id),
                                             CONSTRAINT fk7wlm7vak9kh34ky6jurqml4xf FOREIGN KEY (last_modified_by_id) REFERENCES public.ess_user(id)
);

-- Drop table

-- DROP TABLE public.fuel_grade;

CREATE TABLE public.fuel_grade (
                                   id int8 NOT NULL,
                                   created_date timestamp(6) NULL,
                                   last_modified_date timestamp(6) NULL,
                                   "version" int8 NULL,
                                   id_conf int8 NULL,
                                   expansion_coefficient float8 NULL,
                                   "name" varchar(255) NULL,
                                   price float8 NULL,
                                   created_by_id int8 NULL,
                                   last_modified_by_id int8 NULL,
                                   controller_pts_id int8 NULL,
                                   controller_pts_configuration_id int8 NULL,
                                   CONSTRAINT fuel_grade_pkey PRIMARY KEY (id),
                                   CONSTRAINT fk3lsqq4lk0ky9f5725x127y3k2 FOREIGN KEY (controller_pts_configuration_id) REFERENCES public.controller_pts_configuration(id),
                                   CONSTRAINT fk7dycf76u3u88fwqwp68y0fcs7 FOREIGN KEY (last_modified_by_id) REFERENCES public.ess_user(id),
                                   CONSTRAINT fkd3vrr325erhyhw1d9q4fm0cp FOREIGN KEY (created_by_id) REFERENCES public.ess_user(id),
                                   CONSTRAINT fkthantfcirs0pvhgbk49nbwesb FOREIGN KEY (controller_pts_id) REFERENCES public.controller_pts(id)
);

-- Drop table

-- DROP TABLE public."function";

CREATE TABLE public."function" (
                                   id int8 NOT NULL,
                                   created_date timestamp(6) NULL,
                                   last_modified_date timestamp(6) NULL,
                                   "version" int8 NULL,
                                   code varchar(255) NOT NULL,
                                   description varchar(255) NULL,
                                   "scope" varchar(255) NULL,
                                   created_by_id int8 NULL,
                                   last_modified_by_id int8 NULL,
                                   CONSTRAINT function_pkey PRIMARY KEY (id),
                                   CONSTRAINT fka1ycmlrb2soi77g8sjjxtrvqm FOREIGN KEY (last_modified_by_id) REFERENCES public.ess_user(id),
                                   CONSTRAINT fkga5c7gk8wbdllmfgom94o9rek FOREIGN KEY (created_by_id) REFERENCES public.ess_user(id)
);

alter table public.custom_function_group_functions
    add CONSTRAINT fku0cyco6cjsh3q31wd7qffhw9 FOREIGN KEY (function_id) REFERENCES public."function"(id);

-- Drop table

-- DROP TABLE public.message_pts_log;

CREATE TABLE public.message_pts_log (
                                        id int8 NOT NULL,
                                        created_date timestamp(6) NULL,
                                        last_modified_date timestamp(6) NULL,
                                        "version" int8 NULL,
                                        channel varchar(255) NULL,
                                        configuration_id varchar(255) NULL,
                                        fail_reason text NULL,
                                        firmware_version varchar(255) NULL,
                                        message text NULL,
                                        message_date timestamp(6) NULL,
                                        message_origin varchar(255) NULL,
                                        processing_state varchar(255) NULL,
                                        pts_id varchar(255) NULL,
                                        update_date timestamp(6) NULL,
                                        created_by_id int8 NULL,
                                        last_modified_by_id int8 NULL,
                                        CONSTRAINT message_pts_log_pkey PRIMARY KEY (id),
                                        CONSTRAINT fkaglyhji277urncv3wv3fforpu FOREIGN KEY (last_modified_by_id) REFERENCES public.ess_user(id),
                                        CONSTRAINT fke0atumx44exlxmm7v4ru3xbea FOREIGN KEY (created_by_id) REFERENCES public.ess_user(id)
);

-- Drop table

-- DROP TABLE public.nozzle;

CREATE TABLE public.nozzle (
                               id int8 NOT NULL,
                               created_date timestamp(6) NULL,
                               last_modified_date timestamp(6) NULL,
                               "version" int8 NULL,
                               id_conf int8 NULL,
                               description varchar(255) NULL,
                               created_by_id int8 NULL,
                               last_modified_by_id int8 NULL,
                               controller_pts_id int8 NULL,
                               controller_pts_configuration_id int8 NULL,
                               grade_id int8 NULL,
                               pump_id int8 NULL,
                               tank_id int8 NULL,
                               CONSTRAINT nozzle_pkey PRIMARY KEY (id),
                               CONSTRAINT fkb7nmr3iwpg454i7ayer9crtd2 FOREIGN KEY (controller_pts_configuration_id) REFERENCES public.controller_pts_configuration(id),
                               CONSTRAINT fke0mvj447ynhm2p50l51fwhabg FOREIGN KEY (created_by_id) REFERENCES public.ess_user(id),
                               CONSTRAINT fkglq6p87ijl7f55n4ruxjeifdv FOREIGN KEY (grade_id) REFERENCES public.fuel_grade(id),
                               CONSTRAINT fkl4xcn7fd718ou44cnjlotpggu FOREIGN KEY (controller_pts_id) REFERENCES public.controller_pts(id),
                               CONSTRAINT fkoraj8spqt4nxxr780vpwmi8a5 FOREIGN KEY (last_modified_by_id) REFERENCES public.ess_user(id)
);

-- Drop table

-- DROP TABLE public.payment;

CREATE TABLE public.payment (
                                id int8 NOT NULL,
                                created_date timestamp(6) NULL,
                                last_modified_date timestamp(6) NULL,
                                "version" int8 NULL,
                                payment_type varchar(255) NULL,
                                created_by_id int8 NULL,
                                last_modified_by_id int8 NULL,
                                CONSTRAINT payment_pkey PRIMARY KEY (id),
                                CONSTRAINT fk47tv525pq11jgw3kkwe1c9yqq FOREIGN KEY (created_by_id) REFERENCES public.ess_user(id),
                                CONSTRAINT fkecq355axl6hkkwxf63vp3dwtp FOREIGN KEY (last_modified_by_id) REFERENCES public.ess_user(id)
);

-- Drop table

-- DROP TABLE public."permission";

CREATE TABLE public."permission" (
                                     id int8 NOT NULL,
                                     created_date timestamp(6) NULL,
                                     last_modified_date timestamp(6) NULL,
                                     "version" int8 NULL,
                                     "configuration" bool NULL,
                                     "control" bool NULL,
                                     monitoring bool NULL,
                                     reports bool NULL,
                                     created_by_id int8 NULL,
                                     last_modified_by_id int8 NULL,
                                     CONSTRAINT permission_pkey PRIMARY KEY (id),
                                     CONSTRAINT fkfb5em9yg3nvb42gd1jlb4775l FOREIGN KEY (last_modified_by_id) REFERENCES public.ess_user(id),
                                     CONSTRAINT fksl2qscm1nvrkcrdtb2pfl93v9 FOREIGN KEY (created_by_id) REFERENCES public.ess_user(id)
);

-- Drop table

-- DROP TABLE public.port;

CREATE TABLE public.port (
                             "type" varchar(31) NOT NULL,
                             id int8 NOT NULL,
                             created_date timestamp(6) NULL,
                             last_modified_date timestamp(6) NULL,
                             "version" int8 NULL,
                             baud_rate int8 NULL,
                             protocol int8 NULL,
                             probe_port_id_configured varchar(255) NULL,
                             pump_port_id_configured int8 NULL,
                             created_by_id int8 NULL,
                             last_modified_by_id int8 NULL,
                             controller_pts_configuration_id int8 NULL,
                             CONSTRAINT port_pkey PRIMARY KEY (id),
                             CONSTRAINT fk9416ggon9svht8kkiri9hw2nj FOREIGN KEY (controller_pts_configuration_id) REFERENCES public.controller_pts_configuration(id),
                             CONSTRAINT fkaqjq88y4mh5xb46dft0t5917w FOREIGN KEY (created_by_id) REFERENCES public.ess_user(id),
                             CONSTRAINT fkta7e7dphff07bt3t92qi5fs28 FOREIGN KEY (last_modified_by_id) REFERENCES public.ess_user(id)
);

-- Drop table

-- DROP TABLE public.pricebord;

CREATE TABLE public.pricebord (
                                  id int8 NOT NULL,
                                  created_date timestamp(6) NULL,
                                  last_modified_date timestamp(6) NULL,
                                  "version" int8 NULL,
                                  error bool NULL,
                                  online bool NULL,
                                  price int8 NULL,
                                  created_by_id int8 NULL,
                                  last_modified_by_id int8 NULL,
                                  CONSTRAINT pricebord_pkey PRIMARY KEY (id),
                                  CONSTRAINT fk2g315y88dc5ivxdgoqao65fgf FOREIGN KEY (created_by_id) REFERENCES public.ess_user(id),
                                  CONSTRAINT fk3csap34emv4i2hjb7ho3cs26v FOREIGN KEY (last_modified_by_id) REFERENCES public.ess_user(id)
);

-- Drop table

-- DROP TABLE public.probe;

CREATE TABLE public.probe (
                              id int8 NOT NULL,
                              created_date timestamp(6) NULL,
                              last_modified_date timestamp(6) NULL,
                              "version" int8 NULL,
                              id_conf int8 NULL,
                              address int8 NULL,
                              created_by_id int8 NULL,
                              last_modified_by_id int8 NULL,
                              controller_pts_configuration_id int8 NULL,
                              port_id int8 NULL,
                              tank_id int8 NULL,
                              CONSTRAINT probe_pkey PRIMARY KEY (id),
                              CONSTRAINT fk56ww0mr8a8eqpylecdmmhpjc4 FOREIGN KEY (port_id) REFERENCES public.port(id),
                              CONSTRAINT fkcp2q36gputpng0qjcn8lawvdd FOREIGN KEY (controller_pts_configuration_id) REFERENCES public.controller_pts_configuration(id),
                              CONSTRAINT fkhafk8j6gsm6tcp2ivl7g91vf0 FOREIGN KEY (created_by_id) REFERENCES public.ess_user(id),
                              CONSTRAINT fkqbmnqvorigpyvobgqcgtkb29i FOREIGN KEY (last_modified_by_id) REFERENCES public.ess_user(id)
);


-- Drop table

-- DROP TABLE public.probe_measurements;

CREATE TABLE public.probe_measurements (
                                           id int8 NOT NULL,
                                           created_date timestamp(6) NULL,
                                           last_modified_date timestamp(6) NULL,
                                           "version" int8 NULL,
                                           fuel_grade_id int8 NULL,
                                           fuel_grade_name varchar(255) NULL,
                                           probe int8 NULL,
                                           product_density float4 NOT NULL,
                                           product_height float4 NOT NULL,
                                           product_mass float4 NOT NULL,
                                           producttcvolume float8 NULL,
                                           product_ullage float8 NULL,
                                           product_volume float8 NULL,
                                           status varchar(255) NULL,
                                           tank_filling_percentage int8 NULL,
                                           temperature float4 NOT NULL,
                                           water_height float4 NOT NULL,
                                           water_volume float8 NULL,
                                           created_by_id int8 NULL,
                                           last_modified_by_id int8 NULL,
                                           controller_pts_configuration_id int8 NULL,
                                           CONSTRAINT probe_measurements_pkey PRIMARY KEY (id),
                                           CONSTRAINT fk7qokkg38o05nff79apnml2vg8 FOREIGN KEY (controller_pts_configuration_id) REFERENCES public.controller_pts_configuration(id),
                                           CONSTRAINT fkc52293eg2lfh2wwyvvtnhwpmo FOREIGN KEY (last_modified_by_id) REFERENCES public.ess_user(id),
                                           CONSTRAINT fkokacls8w3a346h8s58u64ml07 FOREIGN KEY (created_by_id) REFERENCES public.ess_user(id)
);

-- Drop table

-- DROP TABLE public.probe_tank_volume_for_height;

CREATE TABLE public.probe_tank_volume_for_height (
                                                     id int8 NOT NULL,
                                                     created_date timestamp(6) NULL,
                                                     last_modified_date timestamp(6) NULL,
                                                     "version" int8 NULL,
                                                     height float4 NOT NULL,
                                                     probe int8 NULL,
                                                     volume float4 NOT NULL,
                                                     created_by_id int8 NULL,
                                                     last_modified_by_id int8 NULL,
                                                     id_cont int8 NULL,
                                                     CONSTRAINT probe_tank_volume_for_height_pkey PRIMARY KEY (id),
                                                     CONSTRAINT fk7lgefelc90s9ealthjabdt2q8 FOREIGN KEY (created_by_id) REFERENCES public.ess_user(id),
                                                     CONSTRAINT fkbi25nytlphrihogyw7ko84u61 FOREIGN KEY (id_cont) REFERENCES public.controller_pts(id),
                                                     CONSTRAINT fkpnt5y6fp1513q6j0s8wf8qkam FOREIGN KEY (last_modified_by_id) REFERENCES public.ess_user(id)
);

-- Drop table

-- DROP TABLE public.pts_network;

CREATE TABLE public.pts_network (
                                    id int8 NOT NULL,
                                    created_date timestamp(6) NULL,
                                    last_modified_date timestamp(6) NULL,
                                    "version" int8 NULL,
                                    dns1 varchar(255) NULL,
                                    dns2 varchar(255) NULL,
                                    gateway varchar(255) NULL,
                                    http_port int4 NULL,
                                    https_port int4 NULL,
                                    ip_address varchar(255) NULL,
                                    net_mask varchar(255) NULL,
                                    created_by_id int8 NULL,
                                    last_modified_by_id int8 NULL,
                                    CONSTRAINT pts_network_pkey PRIMARY KEY (id),
                                    CONSTRAINT fk24du0h602sro4of1eg7q5yqtg FOREIGN KEY (created_by_id) REFERENCES public.ess_user(id),
                                    CONSTRAINT fkjoaqthe3we3ppm8w89n4sveci FOREIGN KEY (last_modified_by_id) REFERENCES public.ess_user(id)
);

-- Drop table

-- DROP TABLE public.pump;

CREATE TABLE public.pump (
                             id int8 NOT NULL,
                             created_date timestamp(6) NULL,
                             last_modified_date timestamp(6) NULL,
                             "version" int8 NULL,
                             id_conf int8 NULL,
                             address int8 NULL,
                             created_by_id int8 NULL,
                             last_modified_by_id int8 NULL,
                             controller_pts_configuration_id int8 NULL,
                             port_id int8 NULL,
                             CONSTRAINT pump_pkey PRIMARY KEY (id),
                             CONSTRAINT fk5w0h4rhu2urumxbjvkryvk9u4 FOREIGN KEY (last_modified_by_id) REFERENCES public.ess_user(id),
                             CONSTRAINT fkg6600mxg2putckatxk2vrwinx FOREIGN KEY (created_by_id) REFERENCES public.ess_user(id),
                             CONSTRAINT fkm3rxlletmeklhslkung6qothr FOREIGN KEY (controller_pts_configuration_id) REFERENCES public.controller_pts_configuration(id),
                             CONSTRAINT fkr7m7oimch1chxxoaelc5gcrge FOREIGN KEY (port_id) REFERENCES public.port(id)
);

-- Drop table

-- DROP TABLE public.pump_price;

CREATE TABLE public.pump_price (
                                   id int8 NOT NULL,
                                   created_date timestamp(6) NULL,
                                   last_modified_date timestamp(6) NULL,
                                   "version" int8 NULL,
                                   price _float8 NULL,
                                   users varchar(255) NULL,
                                   created_by_id int8 NULL,
                                   last_modified_by_id int8 NULL,
                                   pump_id int8 NULL,
                                   CONSTRAINT pump_price_pkey PRIMARY KEY (id),
                                   CONSTRAINT fk8yym3xvd2tbcbdauyfetan2wq FOREIGN KEY (created_by_id) REFERENCES public.ess_user(id),
                                   CONSTRAINT fkh5d2q0y0sudp00cau9l0lwry3 FOREIGN KEY (last_modified_by_id) REFERENCES public.ess_user(id),
                                   CONSTRAINT fkqslp13g64jj5n3ne0bqaxqlcm FOREIGN KEY (pump_id) REFERENCES public.pump(id)
);

-- Drop table

-- DROP TABLE public.pump_transaction;

CREATE TABLE public.pump_transaction (
                                         id int8 NOT NULL,
                                         created_date timestamp(6) NULL,
                                         last_modified_date timestamp(6) NULL,
                                         "version" int8 NULL,
                                         amount float8 NULL,
                                         configuration_id varchar(255) NULL,
                                         date_time timestamp(6) NULL,
                                         date_time_start timestamp(6) NULL,
                                         price float4 NOT NULL,
                                         state varchar(255) NULL,
                                         tag varchar(255) NULL,
                                         tcvolume float4 NOT NULL,
                                         total_amount numeric(38, 2) NULL,
                                         total_volume numeric(38, 2) NULL,
                                         transaction_reference int8 NULL,
                                         "type" varchar(255) NULL,
                                         user_id int8 NULL,
                                         volume float8 NULL,
                                         created_by_id int8 NULL,
                                         last_modified_by_id int8 NULL,
                                         controller_pts_configuration_id int8 NULL,
                                         fuel_grade_id int8 NULL,
                                         nozzle_id int8 NULL,
                                         pump_id int8 NULL,
                                         CONSTRAINT pump_transaction_pkey PRIMARY KEY (id),
                                         CONSTRAINT fk2iywhfklatou6p3p2x1ojwgry FOREIGN KEY (controller_pts_configuration_id) REFERENCES public.controller_pts_configuration(id),
                                         CONSTRAINT fk4hsy3fto3ug6qoibddbp48lcw FOREIGN KEY (fuel_grade_id) REFERENCES public.fuel_grade(id),
                                         CONSTRAINT fkehybtunafpxodpdgk6a9d1mhb FOREIGN KEY (created_by_id) REFERENCES public.ess_user(id),
                                         CONSTRAINT fkhimuve4u0lq2q8tn0edblatm3 FOREIGN KEY (nozzle_id) REFERENCES public.nozzle(id),
                                         CONSTRAINT fkoxnte1piqw0th7v76fgxok56h FOREIGN KEY (last_modified_by_id) REFERENCES public.ess_user(id),
                                         CONSTRAINT fkoytbffks4hwf6dp34523ufxym FOREIGN KEY (pump_id) REFERENCES public.pump(id)
);

-- Drop table

-- DROP TABLE public.reader;

CREATE TABLE public.reader (
                               id int8 NOT NULL,
                               created_date timestamp(6) NULL,
                               last_modified_date timestamp(6) NULL,
                               "version" int8 NULL,
                               id_conf int8 NULL,
                               address int8 NULL,
                               error bool NULL,
                               online bool NULL,
                               pump_id int8 NULL,
                               readers int8 NULL,
                               tag varchar(255) NULL,
                               created_by_id int8 NULL,
                               last_modified_by_id int8 NULL,
                               controller_pts_configuration_id int8 NULL,
                               port_id int8 NULL,
                               CONSTRAINT reader_pkey PRIMARY KEY (id),
                               CONSTRAINT fk5hjc19hkpc3gkgx1guylg95qf FOREIGN KEY (controller_pts_configuration_id) REFERENCES public.controller_pts_configuration(id),
                               CONSTRAINT fken0uvljle0e5on0r48fkhah15 FOREIGN KEY (created_by_id) REFERENCES public.ess_user(id),
                               CONSTRAINT fkhpwuerer9gu379jnbcm14a34r FOREIGN KEY (port_id) REFERENCES public.port(id),
                               CONSTRAINT fkl2nj7joyxq9p96taevyx7b0q9 FOREIGN KEY (last_modified_by_id) REFERENCES public.ess_user(id)
);

-- Drop table

-- DROP TABLE public.remote_server_conf;

CREATE TABLE public.remote_server_conf (
                                           id int8 NOT NULL,
                                           domain_name varchar(255) NULL,
                                           ip_address _int4 NULL,
                                           is_upload_successful bool NULL,
                                           port int4 NULL,
                                           server_response_timeout_seconds int4 NULL,
                                           upload_gps_records bool NULL,
                                           upload_pump_transactions bool NULL,
                                           upload_tank_measurements bool NULL,
                                           upload_test_requests_period_seconds int4 NULL,
                                           uri varchar(255) NULL,
                                           use_device_identifier_as_login bool NULL,
                                           use_upload_test_requests bool NULL,
                                           use_websockets_communication bool NULL,
                                           user_id int4 NULL,
                                           websockets_port int4 NULL,
                                           websockets_reconnect_period int4 NULL,
                                           websockets_uri varchar(255) NULL,
                                           CONSTRAINT remote_server_conf_pkey PRIMARY KEY (id)
);

-- Drop table

-- DROP TABLE public.report_tank_measurement;

CREATE TABLE public.report_tank_measurement (
                                                id int8 NOT NULL,
                                                created_date timestamp(6) NULL,
                                                last_modified_date timestamp(6) NULL,
                                                "version" int8 NULL,
                                                alarms varchar(255) NULL,
                                                configuration_id varchar(255) NULL,
                                                date_time timestamp(6) NULL,
                                                fuel_grade_id int8 NULL,
                                                full_grade varchar(255) NULL,
                                                product_density float8 NOT NULL,
                                                product_height float8 NOT NULL,
                                                product_mass float8 NOT NULL,
                                                producttcvolume float8 NOT NULL,
                                                product_ullage float8 NOT NULL,
                                                product_volume float8 NOT NULL,
                                                status varchar(255) NULL,
                                                tank int8 NULL,
                                                temperature float8 NOT NULL,
                                                water_height float8 NOT NULL,
                                                water_volume float8 NOT NULL,
                                                created_by_id int8 NULL,
                                                last_modified_by_id int8 NULL,
                                                controller_pts_id int8 NULL,
                                                controller_pts_configuration_id int8 NULL,
                                                CONSTRAINT report_tank_measurement_pkey PRIMARY KEY (id),
                                                CONSTRAINT fk4qrd9bbxjtgi4rohsq5jbvyma FOREIGN KEY (last_modified_by_id) REFERENCES public.ess_user(id),
                                                CONSTRAINT fk9nrndpc7iv810sme13u8p0k83 FOREIGN KEY (controller_pts_id) REFERENCES public.controller_pts(id),
                                                CONSTRAINT fkr1iu2qnvmxb7d7v73ot1j3o8s FOREIGN KEY (controller_pts_configuration_id) REFERENCES public.controller_pts_configuration(id),
                                                CONSTRAINT fksd68mcp0htxdy3g0d99a9s5y3 FOREIGN KEY (created_by_id) REFERENCES public.ess_user(id)
);

-- Drop table

-- DROP TABLE public.station;

CREATE TABLE public.station (
                                id int8 NOT NULL,
                                created_date timestamp(6) NULL,
                                last_modified_date timestamp(6) NULL,
                                "version" int8 NULL,
                                actif bool NULL,
                                date_status_change timestamp(6) NOT NULL,
                                address varchar(255) NULL,
                                country_id int8 NULL,
                                customer_account_id int8 NULL,
                                "name" varchar(255) NOT NULL,
                                created_by_id int8 NULL,
                                last_modified_by_id int8 NULL,
                                CONSTRAINT station_pkey PRIMARY KEY (id),
                                CONSTRAINT fk2yba3dar1y8aby4gp048e3cca FOREIGN KEY (customer_account_id) REFERENCES public.customer_account(id),
                                CONSTRAINT fke8re49nrraqoeemypu2p1r3mc FOREIGN KEY (country_id) REFERENCES public.country(id),
                                CONSTRAINT fkh1r5jpbhuhe1182yfw6myhslo FOREIGN KEY (last_modified_by_id) REFERENCES public.ess_user(id),
                                CONSTRAINT fks3o7vsgrxq423g6rqgnd3vh46 FOREIGN KEY (created_by_id) REFERENCES public.ess_user(id)
);

alter table public.controller_pts
    add CONSTRAINT fkbbn5ykb5lckd4vem1hhmunrrc FOREIGN KEY (current_configuration_id) REFERENCES public.controller_pts_configuration(id);
alter table public.controller_pts
    add CONSTRAINT fkg3gngbqaws6uxoqtldldnbxwl FOREIGN KEY (current_firmware_information_id) REFERENCES public.firmware_information(id);
alter table public.controller_pts
    add CONSTRAINT fktcvuvske97omtjqdhnr5rq8jr FOREIGN KEY (station_id) REFERENCES public.station(id);
-- Drop table

-- DROP TABLE public.status;

CREATE TABLE public.status (
                               id int8 NOT NULL,
                               created_date timestamp(6) NULL,
                               last_modified_date timestamp(6) NULL,
                               "version" int8 NULL,
                               battery_voltage int8 NULL,
                               configuration_id varchar(255) NULL,
                               cpu_temperature int8 NULL,
                               date_time varchar(255) NULL,
                               firmware_date_time varchar(255) NULL,
                               gps int8 NULL,
                               power_down_detected bool NULL,
                               price_boards int8 NULL,
                               probes int8 NULL,
                               pts_startup_seconds int8 NULL,
                               pumps int8 NULL,
                               readers int8 NULL,
                               sd_mounted bool NULL,
                               startup_seconds int8 NULL,
                               created_by_id int8 NULL,
                               last_modified_by_id int8 NULL,
                               CONSTRAINT status_pkey PRIMARY KEY (id),
                               CONSTRAINT fk1kbcbjc11bwua3g7g44qkmrdb FOREIGN KEY (last_modified_by_id) REFERENCES public.ess_user(id),
                               CONSTRAINT fkci2uuclcmrhn70dmwql9ev4o FOREIGN KEY (created_by_id) REFERENCES public.ess_user(id)
);

-- Drop table

-- DROP TABLE public.tag;

CREATE TABLE public.tag (
                            id int8 NOT NULL,
                            created_date timestamp(6) NULL,
                            last_modified_date timestamp(6) NULL,
                            "version" int8 NULL,
                            "name" varchar(255) NULL,
                            present bool NULL,
                            tags varchar(255) NULL,
                            "valid" bool NULL,
                            created_by_id int8 NULL,
                            last_modified_by_id int8 NULL,
                            nozzle_id int8 NULL,
                            pump_id int8 NULL,
                            CONSTRAINT tag_pkey PRIMARY KEY (id),
                            CONSTRAINT fk1qhvp386j4abxtqebueab4r23 FOREIGN KEY (nozzle_id) REFERENCES public.nozzle(id),
                            CONSTRAINT fk94th2q1nr26d6t006sufetir1 FOREIGN KEY (last_modified_by_id) REFERENCES public.ess_user(id),
                            CONSTRAINT fkels38d79a87jvlulw96her1bs FOREIGN KEY (created_by_id) REFERENCES public.ess_user(id),
                            CONSTRAINT fklkwoka0tqwjjntqf224qxpjer FOREIGN KEY (pump_id) REFERENCES public.pump(id)
);

-- Drop table

-- DROP TABLE public.tank;

CREATE TABLE public.tank (
                             id int8 NOT NULL,
                             created_date timestamp(6) NULL,
                             last_modified_date timestamp(6) NULL,
                             "version" int8 NULL,
                             id_conf int8 NULL,
                             critical_high_product_alarm int8 NOT NULL,
                             critical_low_product_alarm int8 NOT NULL,
                             height int8 NULL,
                             high_product_alarm int8 NOT NULL,
                             high_water_alarm_height int8 NOT NULL,
                             low_product_alarm_height int8 NULL,
                             set_stop_pumps_at_critical_low_product_height bool NOT NULL,
                             created_by_id int8 NULL,
                             last_modified_by_id int8 NULL,
                             controller_pts_configuration_id int8 NULL,
                             grade_id int8 NULL,
                             CONSTRAINT tank_pkey PRIMARY KEY (id),
                             CONSTRAINT fk11wq8l4j26c1lrq4ympeo8h87 FOREIGN KEY (controller_pts_configuration_id) REFERENCES public.controller_pts_configuration(id),
                             CONSTRAINT fkb8omx1e54k4la72br3e8eps FOREIGN KEY (grade_id) REFERENCES public.fuel_grade(id),
                             CONSTRAINT fkba9wjlasmk87a9f43u0hqsduj FOREIGN KEY (created_by_id) REFERENCES public.ess_user(id),
                             CONSTRAINT fko2i93m4npn7k9k0ftjgfrcn15 FOREIGN KEY (last_modified_by_id) REFERENCES public.ess_user(id)
);

alter table public.nozzle
    add CONSTRAINT fkfu5u3sigy6f61cydhkm0yxk2u FOREIGN KEY (tank_id) REFERENCES public.tank(id);
alter table public.nozzle
    add CONSTRAINT fkg70p5c0r1v2hm06w5o8xu46l2 FOREIGN KEY (pump_id) REFERENCES public.pump(id);

alter table public.probe
    add CONSTRAINT fkjcldy5c2is8do5ihwy3supsu6 FOREIGN KEY (tank_id) REFERENCES public.tank(id);
-- Drop table

-- DROP TABLE public.tank_delivery;

CREATE TABLE public.tank_delivery (
                                      id int8 NOT NULL,
                                      created_date timestamp(6) NULL,
                                      last_modified_date timestamp(6) NULL,
                                      "version" int8 NULL,
                                      configuration_id varchar(255) NULL,
                                      date_time timestamp(6) NULL,
                                      product_height numeric(38, 2) NULL,
                                      producttcvolume float8 NOT NULL,
                                      product_volume float8 NOT NULL,
                                      pumps_dispensed_volume float8 NOT NULL,
                                      temperature numeric(38, 2) NULL,
                                      water_height float8 NOT NULL,
                                      created_by_id int8 NULL,
                                      last_modified_by_id int8 NULL,
                                      controller_pts_id int8 NULL,
                                      tank_id int8 NULL,
                                      CONSTRAINT tank_delivery_pkey PRIMARY KEY (id),
                                      CONSTRAINT fk2nylpaum6r17aebtl8tt4lxcm FOREIGN KEY (created_by_id) REFERENCES public.ess_user(id),
                                      CONSTRAINT fk9nygi7xckwqb276j3mwr1ussy FOREIGN KEY (last_modified_by_id) REFERENCES public.ess_user(id),
                                      CONSTRAINT fkb3tjg15sbfaniluphknnm839e FOREIGN KEY (controller_pts_id) REFERENCES public.controller_pts(id),
                                      CONSTRAINT fklhtjcmrpl00x5d0v89dryaxr1 FOREIGN KEY (tank_id) REFERENCES public.tank(id)
);

-- Drop table

-- DROP TABLE public.tank_level_per_sales;

CREATE TABLE public.tank_level_per_sales (
                                             id int8 NOT NULL,
                                             created_date timestamp(6) NULL,
                                             last_modified_date timestamp(6) NULL,
                                             "version" int8 NULL,
                                             changed_volume float8 NOT NULL,
                                             date_time timestamp(6) NULL,
                                             fuel_grade varchar(255) NULL,
                                             pump_transaction_id int8 NULL,
                                             sales_volume float8 NOT NULL,
                                             tank int8 NULL,
                                             tank_volume_changes float8 NOT NULL,
                                             created_by_id int8 NULL,
                                             last_modified_by_id int8 NULL,
                                             controller_pts_id int8 NULL,
                                             controller_pts_configuration_id int8 NULL,
                                             CONSTRAINT tank_level_per_sales_pkey PRIMARY KEY (id),
                                             CONSTRAINT fkf3tsb63kh6ewwt272sy4yugit FOREIGN KEY (controller_pts_configuration_id) REFERENCES public.controller_pts_configuration(id),
                                             CONSTRAINT fkjblu3jv1onmo0vqhc5d3vu5ie FOREIGN KEY (last_modified_by_id) REFERENCES public.ess_user(id),
                                             CONSTRAINT fkmf246a0275ipa6cuyu2xlt1sk FOREIGN KEY (created_by_id) REFERENCES public.ess_user(id),
                                             CONSTRAINT fko7vompkw753v0qkqdpe6s60m7 FOREIGN KEY (controller_pts_id) REFERENCES public.controller_pts(id)
);

-- Drop table

-- DROP TABLE public.user_history;

CREATE TABLE public.user_history (
                                     id int8 NOT NULL,
                                     created_date timestamp(6) NULL,
                                     last_modified_date timestamp(6) NULL,
                                     "version" int8 NULL,
                                     "action" varchar(255) NULL,
                                     action_type varchar(255) NULL,
                                     "date" timestamp(6) NULL,
                                     id_action int8 NULL,
                                     ip_address varchar(255) NULL,
                                     pts_id varchar(255) NULL,
                                     requesturi varchar(255) NULL,
                                     user_name varchar(255) NULL,
                                     created_by_id int8 NULL,
                                     last_modified_by_id int8 NULL,
                                     controller_pts_id int8 NULL,
                                     CONSTRAINT user_history_pkey PRIMARY KEY (id),
                                     CONSTRAINT fka2yd3ydsqp5gbjfxlwhcbb5mo FOREIGN KEY (last_modified_by_id) REFERENCES public.ess_user(id),
                                     CONSTRAINT fkd1gfmd5h5oq4k2axa2n5pj2gk FOREIGN KEY (created_by_id) REFERENCES public.ess_user(id),
                                     CONSTRAINT fkltte3y45lni0u6t9vcv649aas FOREIGN KEY (controller_pts_id) REFERENCES public.controller_pts(id)
);

-- Drop table

-- DROP TABLE public.user_scope;

CREATE TABLE public.user_scope (
                                   id int8 NOT NULL,
                                   created_date timestamp(6) NULL,
                                   last_modified_date timestamp(6) NULL,
                                   "version" int8 NULL,
                                   customer_account_id int8 NULL,
                                   pump_id int8 NULL,
                                   related_user_id int8 NULL,
                                   "scope" varchar(255) NULL,
                                   station_id int8 NULL,
                                   created_by_id int8 NULL,
                                   last_modified_by_id int8 NULL,
                                   CONSTRAINT user_scope_pkey PRIMARY KEY (id),
                                   CONSTRAINT fk5b3gshqv5aqe2ttytw6r49r3f FOREIGN KEY (customer_account_id) REFERENCES public.customer_account(id),
                                   CONSTRAINT fk68vfrphoneg0y8luhgl8fi8eh FOREIGN KEY (last_modified_by_id) REFERENCES public.ess_user(id),
                                   CONSTRAINT fk71tyrp3awux48557bxcc47q7n FOREIGN KEY (created_by_id) REFERENCES public.ess_user(id),
                                   CONSTRAINT fkde4f19s2ti8jug229fjovq5cd FOREIGN KEY (station_id) REFERENCES public.station(id),
                                   CONSTRAINT fkdpv5g2yj63gpxh4boxfpepx1u FOREIGN KEY (related_user_id) REFERENCES public.ess_user(id),
                                   CONSTRAINT fkfrdcukpw6niy9n7gtbw31c49v FOREIGN KEY (pump_id) REFERENCES public.pump(id)
);

-- Drop table

-- DROP TABLE public.user_scope_functions;

CREATE TABLE public.user_scope_functions (
                                             user_scope_id int8 NOT NULL,
                                             function_id int8 NOT NULL,
                                             CONSTRAINT user_scope_functions_pkey PRIMARY KEY (user_scope_id, function_id),
                                             CONSTRAINT fke452n5ggqwfvqob3xc9tukpir FOREIGN KEY (user_scope_id) REFERENCES public.user_scope(id),
                                             CONSTRAINT fkq6g21wgvq6gn02v98k3i2hdxm FOREIGN KEY (function_id) REFERENCES public."function"(id)
);


-- DROP SEQUENCE public.alert_seq;

CREATE SEQUENCE public.alert_seq
    INCREMENT BY 50
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
	CACHE 1
	NO CYCLE;

-- DROP SEQUENCE public.controller_pts_configuration_seq;

CREATE SEQUENCE public.controller_pts_configuration_seq
    INCREMENT BY 50
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
	CACHE 1
	NO CYCLE;

-- DROP SEQUENCE public.controller_pts_seq;

CREATE SEQUENCE public.controller_pts_seq
    INCREMENT BY 50
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
	CACHE 1
	NO CYCLE;

-- DROP SEQUENCE public.country_seq;

CREATE SEQUENCE public.country_seq
    INCREMENT BY 50
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
	CACHE 1
	NO CYCLE;

-- DROP SEQUENCE public.currency_seq;

CREATE SEQUENCE public.currency_seq
    INCREMENT BY 50
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
	CACHE 1
	NO CYCLE;

-- DROP SEQUENCE public.custom_function_group_seq;

CREATE SEQUENCE public.custom_function_group_seq
    INCREMENT BY 50
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
	CACHE 1
	NO CYCLE;

-- DROP SEQUENCE public.customer_account_seq;

CREATE SEQUENCE public.customer_account_seq
    INCREMENT BY 50
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
	CACHE 1
	NO CYCLE;

-- DROP SEQUENCE public.ess_user_seq;

CREATE SEQUENCE public.ess_user_seq
    INCREMENT BY 50
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
	CACHE 1
	NO CYCLE;

-- DROP SEQUENCE public.exchanged_packets_seq;

CREATE SEQUENCE public.exchanged_packets_seq
    INCREMENT BY 50
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
	CACHE 1
	NO CYCLE;

-- DROP SEQUENCE public.firmware_information_seq;

CREATE SEQUENCE public.firmware_information_seq
    INCREMENT BY 50
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
	CACHE 1
	NO CYCLE;

-- DROP SEQUENCE public.fuel_grade_seq;

CREATE SEQUENCE public.fuel_grade_seq
    INCREMENT BY 50
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
	CACHE 1
	NO CYCLE;

-- DROP SEQUENCE public.function_seq;

CREATE SEQUENCE public.function_seq
    INCREMENT BY 50
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
	CACHE 1
	NO CYCLE;

-- DROP SEQUENCE public.message_pts_log_seq;

CREATE SEQUENCE public.message_pts_log_seq
    INCREMENT BY 50
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
	CACHE 1
	NO CYCLE;

-- DROP SEQUENCE public.nozzle_seq;

CREATE SEQUENCE public.nozzle_seq
    INCREMENT BY 50
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
	CACHE 1
	NO CYCLE;

-- DROP SEQUENCE public.payment_seq;

CREATE SEQUENCE public.payment_seq
    INCREMENT BY 50
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
	CACHE 1
	NO CYCLE;

-- DROP SEQUENCE public.permission_seq;

CREATE SEQUENCE public.permission_seq
    INCREMENT BY 50
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
	CACHE 1
	NO CYCLE;

-- DROP SEQUENCE public.port_seq;

CREATE SEQUENCE public.port_seq
    INCREMENT BY 50
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
	CACHE 1
	NO CYCLE;

-- DROP SEQUENCE public.pricebord_seq;

CREATE SEQUENCE public.pricebord_seq
    INCREMENT BY 50
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
	CACHE 1
	NO CYCLE;

-- DROP SEQUENCE public.probe_measurements_seq;

CREATE SEQUENCE public.probe_measurements_seq
    INCREMENT BY 50
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
	CACHE 1
	NO CYCLE;

-- DROP SEQUENCE public.probe_seq;

CREATE SEQUENCE public.probe_seq
    INCREMENT BY 50
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
	CACHE 1
	NO CYCLE;

-- DROP SEQUENCE public.probe_tank_volume_for_height_seq;

CREATE SEQUENCE public.probe_tank_volume_for_height_seq
    INCREMENT BY 50
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
	CACHE 1
	NO CYCLE;

-- DROP SEQUENCE public.pts_network_seq;

CREATE SEQUENCE public.pts_network_seq
    INCREMENT BY 50
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
	CACHE 1
	NO CYCLE;

-- DROP SEQUENCE public.pump_price_seq;

CREATE SEQUENCE public.pump_price_seq
    INCREMENT BY 50
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
	CACHE 1
	NO CYCLE;

-- DROP SEQUENCE public.pump_seq;

CREATE SEQUENCE public.pump_seq
    INCREMENT BY 50
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
	CACHE 1
	NO CYCLE;

-- DROP SEQUENCE public.pump_transaction_seq;

CREATE SEQUENCE public.pump_transaction_seq
    INCREMENT BY 50
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
	CACHE 1
	NO CYCLE;

-- DROP SEQUENCE public.reader_seq;

CREATE SEQUENCE public.reader_seq
    INCREMENT BY 50
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
	CACHE 1
	NO CYCLE;

-- DROP SEQUENCE public.remote_server_conf_seq;

CREATE SEQUENCE public.remote_server_conf_seq
    INCREMENT BY 50
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
	CACHE 1
	NO CYCLE;

-- DROP SEQUENCE public.report_tank_measurement_seq;

CREATE SEQUENCE public.report_tank_measurement_seq
    INCREMENT BY 50
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
	CACHE 1
	NO CYCLE;

-- DROP SEQUENCE public.station_seq;

CREATE SEQUENCE public.station_seq
    INCREMENT BY 50
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
	CACHE 1
	NO CYCLE;

-- DROP SEQUENCE public.status_seq;

CREATE SEQUENCE public.status_seq
    INCREMENT BY 50
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
	CACHE 1
	NO CYCLE;

-- DROP SEQUENCE public.tag_seq;

CREATE SEQUENCE public.tag_seq
    INCREMENT BY 50
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
	CACHE 1
	NO CYCLE;

-- DROP SEQUENCE public.tank_delivery_seq;

CREATE SEQUENCE public.tank_delivery_seq
    INCREMENT BY 50
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
	CACHE 1
	NO CYCLE;

-- DROP SEQUENCE public.tank_level_per_sales_seq;

CREATE SEQUENCE public.tank_level_per_sales_seq
    INCREMENT BY 50
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
	CACHE 1
	NO CYCLE;

-- DROP SEQUENCE public.tank_seq;

CREATE SEQUENCE public.tank_seq
    INCREMENT BY 50
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
	CACHE 1
	NO CYCLE;

-- DROP SEQUENCE public.user_history_seq;

CREATE SEQUENCE public.user_history_seq
    INCREMENT BY 50
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
	CACHE 1
	NO CYCLE;

-- DROP SEQUENCE public.user_scope_seq;

CREATE SEQUENCE public.user_scope_seq
    INCREMENT BY 50
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
	CACHE 1
	NO CYCLE;


