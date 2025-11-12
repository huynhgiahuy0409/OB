package com.mservice.fs.onboarding.model.crm.getapplication;

import com.mservice.fs.generic.jdbc.DBColumn;
import com.mservice.fs.generic.jdbc.Transient;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.common.config.CrmConfig;
import com.mservice.fs.onboarding.utils.OnboardingUtils;
import com.mservice.fs.onboarding.utils.constant.Constant;
import com.mservice.fs.utils.Utils;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * @author muoi.nong
 */
@Getter
@Setter
public class CrmApplicationInfo {

    private String loanId;
    private String serviceCode;
    @Transient
    private String serviceName;
    private String partnerCode;
    private String phoneNumber;
    private String createTime;
    private String lastModified;
    private String fullName;
    private String personalId;
    private String loanAmount;
    @DBColumn(name = "new_status")
    private String status;
    @Transient
    private String statusMessage;
    @Transient
    private Boolean isCancel;
    private String type;

    public CrmApplicationInfo() {
    }

    public CrmApplicationInfo(String serviceName, ApplicationData applicationData, List<CrmConfig> crmConfig) {
        this.loanId = applicationData.getApplicationId();
        this.serviceCode = applicationData.getServiceId();
        this.partnerCode = applicationData.getPartnerId();
        this.phoneNumber = applicationData.getPhoneNumber();
        this.createTime = OnboardingUtils.convertLongToDateFormat(applicationData.getCreatedDate());
        this.lastModified = OnboardingUtils.convertLongToDateFormat(applicationData.getModifiedDateInMillis());
        this.fullName = applicationData.getFullName();
        this.personalId = applicationData.getIdNumber();
        this.loanAmount = Utils.isNotEmpty(applicationData.getChosenPackage().getLoanAmount()) ? String.valueOf(applicationData.getChosenPackage().getLoanAmount()) : Constant.UNKNOWN_VALUE;
        this.status = applicationData.getStatus().name();
        this.type = Constant.CACHE_TYPE_VALUE;
        this.updateCrmPackageInfo(serviceName, applicationData.getStatus().name(), this, crmConfig);
    }

    public void updateCrmPackageInfo(String serviceName, CrmApplicationInfo info, List<CrmConfig> crmConfig, String beginDate, String endDate, List<CrmApplicationInfo> result) {
        if (!isWithInDateRange(info.getCreateTime(), beginDate, endDate)) {
            return;
        }

        Optional<CrmConfig> config = crmConfig.stream().filter(a -> a.getStatus().equals(info.getStatus())).findAny();

        info.setStatusMessage(config.isPresent() ? config.get().getStatusMessage() : Constant.UNKNOWN_VALUE);
        info.setIsCancel(config.isPresent() && config.get().getIsAllowDelete() == 1 ? Boolean.TRUE : Boolean.FALSE);
        info.setServiceName(serviceName);

        result.add(info);
    }

    public void updateCrmPackageInfo(String serviceName, String status, CrmApplicationInfo info, List<CrmConfig> crmConfig) {
        Optional<CrmConfig> config = crmConfig.stream().filter(a -> a.getStatus().equals(status)).findAny();

        info.setStatusMessage(config.isPresent() ? config.get().getStatusMessage() : Constant.UNKNOWN_VALUE);
        info.setIsCancel(config.isPresent() && config.get().getIsAllowDelete() == 1 ? Boolean.TRUE : Boolean.FALSE);
        info.setServiceName(serviceName);
    }

    public boolean isWithInDateRange(String dateValid, String beginDate, String endDate) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(Constant.ONBOARDING_DATE_FORMAT);

        LocalDateTime validDate = LocalDateTime.parse(dateValid, dateTimeFormatter);
        LocalDateTime startDate = beginDate != null ? LocalDateTime.parse(beginDate, dateTimeFormatter) : null;
        LocalDateTime finishDate = endDate != null ? LocalDateTime.parse(endDate, dateTimeFormatter) : null;

        return (startDate == null || !validDate.isBefore(startDate))
                && (finishDate == null || !validDate.isAfter(finishDate));
    }
}
