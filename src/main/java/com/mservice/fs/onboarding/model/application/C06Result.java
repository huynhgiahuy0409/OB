package com.mservice.fs.onboarding.model.application;

import lombok.Getter;
import lombok.Setter;

/**
 * @author phat.duong
 * on 10/24/2024
 **/
@Getter
@Setter
public class C06Result {
    private String c06TimeVerify;
    private String kycSignature;
    private String kycChallenge;
    private String kycAAResult;
    private String kycEACCAResult;
    private String kycC06Partner;
}
