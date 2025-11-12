package com.mservice.fs.onboarding.model.crm;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mservice.fs.json.Json;
import com.mservice.fs.onboarding.model.ApplicationStatus;
import com.mservice.fs.onboarding.model.application.ScamStatus;
import com.mservice.fs.onboarding.utils.CrmUtils;
import com.mservice.fs.utils.DateUtil;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoanInfo {
    private String loanId;
    private String serviceId;
    private String partnerId;
    private String phoneNumber;
    private String createTime;
    private String lastModified;
    private String fullName;
    private String personalId;
    private ApplicationStatus status;
    private String statusMessage;
    @JsonProperty("isCancel")
    private boolean isCancel = false;
    private CrmType type;
    private String scamStatus;

    @Override
    public String toString() {
        return Json.encode(this);
    }

    public void setCreateTime(long createTime) {
        this.createTime = DateUtil.getDate(createTime, CrmUtils.FORMATTER);
    }

    public void setLastModified(long lastModified) {
        this.lastModified = DateUtil.getDate(lastModified, CrmUtils.FORMATTER);
    }


}
