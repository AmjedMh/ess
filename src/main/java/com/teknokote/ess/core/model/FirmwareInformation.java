package com.teknokote.ess.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.teknokote.core.model.ESSEntity;
import com.teknokote.ess.core.model.configuration.ControllerPts;
import com.teknokote.ess.core.model.organization.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"ptsId","dateTime"}))
public class FirmwareInformation extends ESSEntity<Long, User>
{
    private static final long serialVersionUID = -4306030674467946016L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore // Break the circular reference here
    private ControllerPts controllerPts;
    private String ptsId;
    private String dateTime;
    private boolean versionState;
    private LocalDateTime modificationDate;
}
