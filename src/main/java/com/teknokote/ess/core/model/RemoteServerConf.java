package com.teknokote.ess.core.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "RemoteServerConf")
public class RemoteServerConf {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;
    @Column(name = "IpAddress")
    private List<Integer> ipAddress;
    @Column(name = "DomainName")
    private String domainName;
    @Column(name = "UserId")
    private int userId;
    @Column(name = "ServerResponseTimeoutSeconds")
    private int serverResponseTimeoutSeconds;
    @Column(name = "UseDeviceIdentifierAsLogin")
    private Boolean useDeviceIdentifierAsLogin;
    @Column(name = "UploadPumpTransactions")
    private Boolean uploadPumpTransactions;
    @Column(name = "UploadTankMeasurements")
    private Boolean uploadTankMeasurements;
    @Column(name = "UploadGpsRecords")
    private Boolean uploadGpsRecords;
    @Column(name = "UseUploadTestRequests")
    private Boolean useUploadTestRequests;
    @Column(name = "UploadTestRequestsPeriodSeconds")
    private int uploadTestRequestsPeriodSeconds;
    @Column(name = "Uri")
    private String uri;
    @Column(name = "Port")
    private int port;
    @Column(name = "UseWebsocketsCommunication")
    private Boolean useWebsocketsCommunication;
    @Column(name = "WebsocketsUri")
    private String websocketsUri;
    @Column(name = "WebsocketsPort")
    private int websocketsPort;
    @Column(name = "WebsocketsReconnectPeriod")
    private int websocketsReconnectPeriod;
    @Column(name = "IsUploadSuccessful")
    private Boolean isUploadSuccessful;

}
