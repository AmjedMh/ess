package com.teknokote.ess.core.model;

import com.teknokote.core.model.ESSEntity;
import com.teknokote.ess.core.model.organization.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class PtsNetwork extends ESSEntity<Long, User>
{
    private static final long serialVersionUID = -9153036939493111625L;

    @Column(name ="IpAddress")
    private String ipAddress;
    @Column(name ="NetMask")
    private String netMask;
    @Column(name ="Gateway")
    private String gateway;
    @Column(name ="HttpPort")
    private int httpPort;
    @Column(name ="HttpsPort")
    private int httpsPort;
    @Column(name ="Dns1")
    private String dns1;
    @Column(name ="Dns2")
    private String dns2;

}
