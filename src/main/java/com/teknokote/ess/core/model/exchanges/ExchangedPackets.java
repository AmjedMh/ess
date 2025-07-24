package com.teknokote.ess.core.model.exchanges;


import com.teknokote.core.model.ESSEntity;
import com.teknokote.ess.core.model.organization.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "EXCHANGED_PACKETS", uniqueConstraints = {@UniqueConstraint(columnNames = {"ptsId","packetId"})})
public class ExchangedPackets extends ESSEntity<Long, User>
{

   private String ptsId;
   private Long packetId;
   @Enumerated(EnumType.STRING)
   private EnumPacketType packetType;
   private String sentPacket;
   private String responsePacket;
   private LocalDateTime requestDate;
   private LocalDateTime responseDate;

}
