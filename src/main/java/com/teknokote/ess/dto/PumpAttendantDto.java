package com.teknokote.ess.dto;

import com.teknokote.core.dto.ESSActivatableDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PumpAttendantDto extends ESSActivatableDto<Long> {
    private String firstName;
    private String lastName;
    private String address;
    private String tag;
    private Long stationId;
    private String matricule;
    private String photo;
    private String phone;

    @Builder
    public PumpAttendantDto(Long id, Long version, boolean actif, LocalDateTime dateStatusChange, String firstName, String lastName, String address
            , String tag, Long stationId, String matricule, String phone, String photo) {
        super(id, version, actif, dateStatusChange);
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.tag = tag;
        this.photo = photo;
        this.matricule = matricule;
        this.phone = phone;
        this.stationId = stationId;
    }
}
