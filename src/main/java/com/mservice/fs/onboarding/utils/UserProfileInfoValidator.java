package com.mservice.fs.onboarding.utils;

import com.mservice.fs.log.Log;
import com.mservice.fs.sof.queue.model.profile.UserProfileInfo;
import com.mservice.fs.utils.Utils;

import java.lang.reflect.Field;

/**
 * @author tuan.tran6
 * on 10/4/2024
 */
public class UserProfileInfoValidator {
    private static final String[] FIELDS_TO_CHECK = {
            "fullNameKyc", "addressKyc", "dobKyc", "expiredDateKyc", "genderKyc", "issueDateKyc",
            "personalIdKyc", "idCardTypeKyc", "issuePlaceKyc", "email", "agent", "identify",
            "passportKyc", "faceMatching", "idFrontImageKyc", "idBackImageKyc",
            "imageFaceMatching", "walletStatus", "timestampKyc", "faceMatchTimestamp",
            "dobKycOcr", "fullNameKycOcr"
    };

    public static boolean isValid(UserProfileInfo obj) {
        if (obj == null) {
            return false;
        }

        boolean hasValidField = false;

        for (String fieldName : FIELDS_TO_CHECK) {
            try {
                Field field = obj.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                Object value = field.get(obj);
                if (Utils.isNotEmpty(value)) {
                    hasValidField = true;
                    break;
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                Log.MAIN.info("[UserProfileInfoValidator] check field error", e);
            }
        }
        return hasValidField;
    }
}
