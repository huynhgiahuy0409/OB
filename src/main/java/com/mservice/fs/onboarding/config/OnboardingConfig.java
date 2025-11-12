package com.mservice.fs.onboarding.config;

import com.mservice.fs.generic.Config;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


@Getter
public class OnboardingConfig implements Config {

    private String onboardingService;
    private Map<String, String> contentGchat;
    private ContractConfig contractConfig;
    private boolean enableBEForceFaceMatching;
    private boolean enableVerifyFMConfirmAction;
    private List<String> byPassBEForceFaceMatching;
    private List<String> whiteListPackageInterestRate;
    private List<String> initCurrentAddressService;
    private List<String> serviceAlertNotiMissUserProfile;
    private Map<String, Long> cacheUserProfileMap;
    private Map<String, Long> cacheProcessNameMap;
    private List<String> finalSubmitClearPendingFormPartners = new ArrayList<>();
    private List<String> updateCacheTimeWhenBannedPartners = new ArrayList<>();
    private List<String> verifyOtpUpdateLinkS3Partners = new ArrayList<>();
    private List<String> generateOtpUpdateLinkS3Partners = new ArrayList<>();
    private Map<String, List<String>> serviceIdMergedMap;
    private Set<String> byPassValidatePathImageByPartner = new HashSet<>();
    private Map<String, String> prefixSegmentMap = new HashMap<>();
    private Map<String, String> lendingServiceProductCodeMap;
    private Map<String, Object> replaceExtraRenderDataMap = new HashMap<>();
    private Map<String, List<String>> partnerFieldMap = new HashMap<>();
    private boolean enableReplaceRenderData;
    private Set<String> serviceApplyRecalculateDueDate;



}
