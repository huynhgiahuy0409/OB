package com.mservice.fs.onboarding.model.common.config;


import com.mservice.fs.onboarding.model.PackageInfo;

import java.util.HashMap;
import java.util.Map;

public class PackageInfoConfig {

    private Map<String, PackageInfo> packageMap = new HashMap<>();

    public PackageInfo getPackage(String packageCode) {
        return packageMap.get(getKey(packageCode));
    }

    public void addPackage(PackageInfo packageInfo) {
        packageMap.put(getKey(packageInfo.getPackageCode()), packageInfo);
    }

    private String getKey(String packageCode) {
        return packageCode;
    }
}
