package com.teknokote.ess.events.publish.cm;

import com.teknokote.core.dto.ESSIdentifiedDto;
import com.teknokote.ess.core.model.organization.EnumCustomerAccountStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class CMSupplierDto extends ESSIdentifiedDto<Long> {
    private String identifier;
    private String reference;
    private String name;
    private String address;
    private String city;
    private String phone;
    private String email;
    private Set<CMSalePointDto> salePoints;
    private Set<CMUser> users;
    private Set<CMProduct> products;
    private EnumCustomerAccountStatus status;
    @Builder
    public CMSupplierDto(Long id,Long version,String reference, String name, String address, String phone, String city,String email,Set<CMUser> users,Set<CMSalePointDto> salePoints,Set<CMProduct> products,EnumCustomerAccountStatus status,String identifier)
    {
        super(id,version);
        this.identifier=identifier;
        this.reference = reference;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.city=city;
        this.email=email;
        this.salePoints=salePoints;
        this.products=products;
        this.users=users;
        this.status=status;
    }
}
