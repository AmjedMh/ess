package com.teknokote.ess.dto;

import com.teknokote.ess.core.model.organization.EnumAffectationMode;
import com.teknokote.core.dto.ESSActivatableDto;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
public class StationDto extends ESSActivatableDto<Long> {
    private String identifier;
    @NotEmpty
    private String name;
    private String address;
    private String city;
    // juste pour toDto() et toEntity() parce qu'il est créé en même temps
    private ControllerPtsDto controllerPts;
    private Long controllerPtsId;
    private String phone;
    private String cordonneesGps;
    private EnumAffectationMode modeAffectation;
    // juste pour toDto()
    private CountryDto country;
    @NotNull
    private Long countryId;
    @NotNull
    private Long customerAccountId;
    private Long creatorAccountId;
    private String creatorCustomerAccountName;
    private String customerAccountName;
    private LocalDateTime createdDate;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StationDto that = (StationDto) o;

        if (!Objects.equals(getId(), that.getId())) return false;
        if (!Objects.equals(name, that.name)) return false;
        if (!Objects.equals(identifier, that.identifier)) return false;
        return Objects.equals(controllerPts, that.controllerPts);
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (controllerPts != null ? controllerPts.hashCode() : 0);
        return result;
    }

    @Builder
    public StationDto(Long id, Long version, LocalDateTime createdDate, String name,String identifier, String address, Boolean actif,
                      LocalDateTime dateStatusChange, EnumAffectationMode modeAffectation, String cordonneesGps,
                      ControllerPtsDto controllerPts, String creatorCustomerAccountName, String customerAccountName,
                      CountryDto country, Long controllerPtsId, Long countryId, Long customerAccountId, Long creatorAccountId, String city, String phone) {
        super(id, version, actif, dateStatusChange);
        this.identifier=identifier;
        this.name = name;
        this.address = address;
        this.city = city;
        this.phone = phone;
        this.controllerPts = controllerPts;
        this.country = country;
        this.controllerPtsId = controllerPtsId;
        this.countryId = countryId;
        this.customerAccountId = customerAccountId;
        this.creatorAccountId = creatorAccountId;
        this.customerAccountName = customerAccountName;
        this.creatorCustomerAccountName = creatorCustomerAccountName;
        this.cordonneesGps = cordonneesGps;
        this.modeAffectation = modeAffectation;
        this.createdDate = createdDate;
    }
}
