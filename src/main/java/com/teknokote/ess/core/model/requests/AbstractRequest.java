package com.teknokote.ess.core.model.requests;

import com.teknokote.core.model.ESSEntity;
import com.teknokote.ess.core.model.configuration.Station;
import com.teknokote.ess.core.model.organization.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
@DiscriminatorColumn(name = "request_type")
public abstract class AbstractRequest extends ESSEntity<Long,User>
{
   @Serial
   private static final long serialVersionUID = 1324005170960908413L;
   @ManyToOne
   private Station station;
   @Column(name = "station_id",insertable = false, updatable = false)
   private Long stationId;
   @Enumerated(EnumType.STRING)
   private EnumRequestType requestType;
   private LocalDateTime scheduledDate;
   private LocalDateTime plannedDate;
   private LocalDateTime executionDate;
   private LocalDateTime lastTrialDate;
   @ManyToOne
   private User requester;
   @Column(name = "requester_id",insertable = false, updatable = false)
   private Long requesterId;
   @Enumerated(EnumType.STRING)
   private EnumRequestStatus status;
}
