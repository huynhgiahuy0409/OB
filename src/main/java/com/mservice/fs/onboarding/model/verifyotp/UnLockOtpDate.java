package com.mservice.fs.onboarding.model.verifyotp;


import lombok.Getter;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Getter
public class UnLockOtpDate {

    private String hour;
    private String minute;
    private String second;
    private String day;
    private String month;
    private String year;

    public UnLockOtpDate(long longTimeStamp) {
        Timestamp timestamp = new Timestamp(longTimeStamp);
        LocalDateTime localDateTime = timestamp.toLocalDateTime();
        hour = convertTimeDataToString(localDateTime.getHour());
        minute = convertTimeDataToString(localDateTime.getMinute());
        second = convertTimeDataToString(localDateTime.getSecond());
        day = convertTimeDataToString(localDateTime.getDayOfMonth());
        month = convertTimeDataToString(localDateTime.getMonthValue());
        year = convertTimeDataToString(localDateTime.getYear());
    }

    private String convertTimeDataToString(int hour) {
        if (hour < 10) {
            return "0" + hour;
        }
        return String.valueOf(hour);
    }
}
