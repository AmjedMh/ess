package com.teknokote.ess.dto;

import com.teknokote.ess.core.model.organization.EnumCustomerAccountStatus;
import com.teknokote.core.dto.ESSActivatableDto;
import com.teknokote.ess.dto.shifts.PaymentMethodDto;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Getter
@Setter
public class CustomerAccountDto extends ESSActivatableDto<Long> {
    @NotEmpty
    private String identifier;
    @NotEmpty
    private String name;
    private String description;
    private EnumCustomerAccountStatus status;
    private String city;
    private String parentName;
    private Long parentId;
    private Long creatorAccountId;
    private String creatorCustomerAccountName;
    private int stationsCount;
    private Set<StationDto> stations;
    private Set<UserDto> attachedUsers;
    // Il reste parce qu'il est créé en même temps que le compte client. Il est exclut de l'update.
    private UserDto masterUser;
    private Long masterUserId;
    private UserDto creatorUser;
    private Long creatorUserId;
    private List<PaymentMethodDto> paymentMethods;
    private boolean resaleRight;
    private boolean cardManager;
    private boolean exported;
    private LocalDateTime plannedExportDate;
    private LocalDateTime scheduledDate;
    private String phone;
    private LocalDateTime createdDate;

    @Builder
    public CustomerAccountDto(Long id, Long version, LocalDateTime createdDate, String name, String description, boolean actif,
                              LocalDateTime dateStatusChange, EnumCustomerAccountStatus status, Set<StationDto> stations, Set<UserDto> attachedUsers,
                              String city, String phone, UserDto masterUser, Long masterUserId, Long creatorAccountId, String creatorCustomerAccountName,
                              UserDto creatorUser, String parentName, int stationsCount, Long parentId, Long creatorUserId,String identifier,
                              boolean resaleRight, List<PaymentMethodDto> paymentMethods, boolean cardManager, boolean exported, LocalDateTime plannedExportDate,LocalDateTime scheduledDate) {
        super(id, version, actif, dateStatusChange);
        this.identifier=identifier;
        this.name = name;
        this.description = description;
        this.status = status;
        this.city = city;
        this.masterUserId = masterUserId;
        this.masterUser = masterUser;
        this.stations = stations;
        this.stationsCount = stationsCount;
        this.attachedUsers = attachedUsers;
        this.parentName = parentName;
        this.parentId = parentId;
        this.creatorAccountId = creatorAccountId;
        this.creatorCustomerAccountName = creatorCustomerAccountName;
        this.creatorUser = creatorUser;
        this.creatorUserId = creatorUserId;
        this.resaleRight = resaleRight;
        this.paymentMethods = paymentMethods;
        this.cardManager = cardManager;
        this.phone = phone;
        this.exported = exported;
        this.plannedExportDate = plannedExportDate;
        this.createdDate = createdDate;
        this.scheduledDate=scheduledDate;
    }

    public void addStation(StationDto dto) {
        stations.add(dto);
    }

    public Optional<StationDto> getStationById(Long stationId) {
        return getStations().stream().filter(stationDto -> stationId.equals(stationDto.getId())).findFirst();
    }
}
