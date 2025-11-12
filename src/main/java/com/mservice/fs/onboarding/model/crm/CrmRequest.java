package com.mservice.fs.onboarding.model.crm;


import com.mservice.fs.model.DefaultRequest;
import lombok.Getter;


@Getter
public class CrmRequest extends DefaultRequest {
    private String requestId;
    private String phoneNumber;
    private String beginDate;
    private String endDate;

}
