package com.mservice.fs.onboarding.model.verifyotp;

import com.mservice.fs.onboarding.model.ApplicationData;
import lombok.Getter;
import lombok.Setter;

/**
 * @author hoang.thai
 * on 1/22/2024
 */
@Getter
@Setter
public class ContractData {

    private String day;
    private String month;
    private String year;
    private String hour;
    private String minute;
    private String second;
    private String otp;
    private ApplicationData applicationData;
    private String interestRate;

}
