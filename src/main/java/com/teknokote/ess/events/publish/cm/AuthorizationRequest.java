package com.teknokote.ess.events.publish.cm;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthorizationRequest {
    private String reference;
    private String tag;
    private String productName;
    private String salePointName;
    private String salePointIdentifier;
    private String ptsId;
    private Long pump;
    @Builder
    public AuthorizationRequest(String reference, String tag, String productName, String salePointName,String salePointIdentifier,String ptsId,Long pump) {
        this.reference = reference;
        this.tag = tag;
        this.productName = productName;
        this.salePointName = salePointName;
        this.salePointIdentifier=salePointIdentifier;
        this.ptsId=ptsId;
        this.pump=pump;
    }
}
