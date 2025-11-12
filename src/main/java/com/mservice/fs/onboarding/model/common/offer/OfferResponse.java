package com.mservice.fs.onboarding.model.common.offer;

import com.mservice.fs.onboarding.model.OnboardingResponse;
import com.mservice.fs.onboarding.model.PackageInfo;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author hoang.thai
 * on 12/1/2023
 */
@Getter
@Setter
public class OfferResponse extends OnboardingResponse {

    private List<PackageInfo> packages;
    private List<PackageInfo> zeroInterestPackages;

}
