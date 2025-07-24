package com.teknokote.ess.events.publish.cm;

import com.teknokote.core.dto.ESSActivatableDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
@Getter
@Setter
public class CMUser extends ESSActivatableDto<Long> {
    private String userIdentifier;
    private String username;
    private String reference;
    private LocalDateTime lastConnectionDate;

    @Builder
    public CMUser(Long id, Long version,boolean actif,LocalDateTime dateStatusChange, String username, LocalDateTime lastConnectionDate,String reference,String userIdentifier)
    {
        super(id, version,actif,dateStatusChange);
        this.userIdentifier=userIdentifier;
        this.reference=reference;
        this.username = username;
        this.lastConnectionDate=lastConnectionDate;
    }
}
