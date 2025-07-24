package com.teknokote.ess.events.publish.cm;

import com.teknokote.core.dto.ESSIdentifiedDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CMSalePointDto extends ESSIdentifiedDto<Long> {
    private String identifier;
    private String name;
    private String city;
    private String area;
    private String phone;
    private Long supplierId;
    private String reference;
    private Long countryId;
    private boolean status;

    @Builder
    public CMSalePointDto(Long id, Long version, String name, String city, String area, Long supplierId, String phone, String reference, Long countryId, boolean status, String identifier) {
        super(id, version);
        this.identifier = identifier;
        this.name = name;
        this.city = city;
        this.area = area;
        this.supplierId = supplierId;
        this.phone = phone;
        this.reference = reference;
        this.countryId = countryId;
        this.status = status;
    }
}
