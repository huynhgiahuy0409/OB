package com.mservice.fs.onboarding.model.status;

import com.mservice.fs.json.CacheObject;
import com.mservice.fs.onboarding.model.OfferPackage;
import com.mservice.fs.onboarding.model.PackageInfo;
import com.mservice.fs.onboarding.model.common.ai.GetPackageResponse;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author hoang.thai
 * on 11/23/2023
 */
@Getter
@Setter
public class PackageCache implements CacheObject {

    private static final String NAME = "PACKAGE";
    public static final long TIME_SAVE_PACKAGE = 3600000l;


    private Double momoCreditScore;
    LinkedHashMap<String, PackageInfo> packageInfoMap;
    private GetPackageResponse getPackageResponse;
    private List<OfferPackage> offerPackagesNotConfiguredInDb;

    public static String createKey(String serviceId, String initiatorId) {
        return NAME + ":" + serviceId + "_" + initiatorId;
    }
}
