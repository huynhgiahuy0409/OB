package com.mservice.fs.onboarding.model.application;

import com.mservice.fs.json.CacheObject;
import com.mservice.fs.sof.queue.model.profile.UserProfileInfo;
import com.mservice.fs.utils.JsonUtil;
import lombok.Getter;
import lombok.Setter;

/**
 * @author phat.duong
 * on 10/16/2024
 **/
@Getter
@Setter
public class UserProfileInfoWrapper implements CacheObject {

    private static final String NAME = "APPLICATION_INFO";
    public static final long TIME_SAVE_USER_PROFILE = 1209600000l;

    private UserProfileInfo userProfileInfo;

    @Override
    public String toCacheString() throws Exception {
        return JsonUtil.toString(this);
    }
    public static String createKey(String serviceId, String applicationId) {
        return NAME + ":" + serviceId + "_" + applicationId;
    }
}
