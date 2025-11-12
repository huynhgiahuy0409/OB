package com.mservice.fs.onboarding.model.application.submit;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * @author hoang.thai
 * on 11/21/2023
 */
@Getter
@Setter
public class SocialSellerResponse {

    private Data data;
    private String responseCode;
    private long responseTimestamp;

    @Getter
    @Setter
    public static class Data {

        private SocialSellerTransInfo socialSellerTransInfo;
        private SurveyInfo surveyInfo;
        private AiDetectInfo aiDetectInfo;
        private P2pInfo p2pInfo;
    }

    @Getter
    public static class SocialSellerTransInfo {

        private String numPayment;
        private String amount;
    }

    @Getter
    @Setter
    public static class SurveyInfo {

        @JsonProperty("isCompleteSurvey")
        private boolean completeSurvey;
    }

    @Getter
    @Setter
    public static class AiDetectInfo {

        @JsonProperty("isTop10Merchant")
        private boolean top10Merchant;
    }

    @Getter
    @Setter
    public static class P2pInfo {

        private String p2pAmount;
    }

}
