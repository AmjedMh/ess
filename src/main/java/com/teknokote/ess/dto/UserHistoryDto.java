package com.teknokote.ess.dto;

import com.teknokote.ess.core.model.EnumActivityType;
import com.teknokote.core.dto.ESSIdentifiedDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UserHistoryDto extends ESSIdentifiedDto<Long>{
    private Long userId;
    private EnumActivityType activityType;
    private String requestURI;
    private String ipAddress;
    private LocalDateTime activityDate;
    private String requestHandler;
    private Long controllerPtsId;
    private boolean impersonationMode;
    private String impersonatorUserName;
    private String action;
    private String userName;
    @Builder
    public UserHistoryDto(Long id,Long version,Long userId,EnumActivityType activityType,String requestURI,String ipAddress,LocalDateTime activityDate,String requestHandler,Long controllerPtsId,boolean impersonationMode,String impersonatorUserName,String userName,
                          String action)
    {
        super(id,version);
        this.userId=userId;
        this.activityType=activityType;
        this.requestURI=requestURI;
        this.ipAddress=ipAddress;
        this.activityDate=activityDate;
        this.requestHandler=requestHandler;
        this.controllerPtsId=controllerPtsId;
        this.impersonationMode=impersonationMode;
        this.impersonatorUserName=impersonatorUserName;
        this.userName=userName;
        this.action=action;
    }
}