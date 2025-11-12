package com.mservice.fs.onboarding.model.common.offer;

import com.mservice.fs.onboarding.model.OnboardingRequest;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

/**
 * @author hoang.thai
 * on 12/1/2023
 */
@Getter
public class OfferRequest extends OnboardingRequest {

    private Set<String> unavailablePackageNames = new HashSet<>();
}
