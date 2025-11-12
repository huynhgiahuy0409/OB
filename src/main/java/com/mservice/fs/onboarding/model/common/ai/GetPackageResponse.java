package com.mservice.fs.onboarding.model.common.ai;

import com.mservice.fs.onboarding.model.OfferPackage;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author hoang.thai
 * on 9/12/2023
 */
@Getter
@Setter
public class GetPackageResponse {

    private String packageCode;
    private Double momoCreditScore;
    private String kycUpdateTime;
    private Boolean kycFlag;
    private String segmentUser;
    private String offerPackageType;
    private Integer responseCode;
    private List<OfferPackage> offerPackages;
    private String lenderLogic;
    private String lenderId;
    private String experimentTag;
    private List<MerchantInfoRecord> merchantInfoRecord;

}
