package com.teknokote.ess.events.publish.cm;

import com.teknokote.core.dto.ESSIdentifiedDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CMProduct extends ESSIdentifiedDto<Long> {
    private String code;
    private String name;
    private Double price;
    private Long supplierId;
    private String reference;

    @Builder
    public CMProduct(Long id, Long version, String code, String name, Double price, Long supplierId, String reference) {
        super(id, version);
        this.code = code;
        this.name = name;
        this.price = price;
        this.supplierId = supplierId;
        this.reference = reference;
    }
}