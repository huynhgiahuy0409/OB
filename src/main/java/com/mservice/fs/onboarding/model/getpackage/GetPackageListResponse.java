package com.mservice.fs.onboarding.model.getpackage;

import com.mservice.fs.model.PlatformDefaultResponse;
import com.mservice.fs.onboarding.model.PackageInfo;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GetPackageListResponse  extends PlatformDefaultResponse {
    private List<PackageInfo> data;
    private Integer total;
}
