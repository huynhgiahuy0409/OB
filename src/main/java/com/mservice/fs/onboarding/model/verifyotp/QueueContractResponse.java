package com.mservice.fs.onboarding.model.verifyotp;

import com.mservice.fs.json.Json;
import com.mservice.fs.model.DefaultResponse;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author hoang.thai
 * on 1/19/2024
 */
@Getter
@Setter
public class QueueContractResponse extends DefaultResponse {

    private List<AttachFileData> attachFiles;

    @Override
    public String toString() {
        return Json.encode(this);
    }

    @Getter
    @Setter
    public static class AttachFileData {

        private String name;
        private String path;
        private String link;
        private long expiredTime;
        private long size;
        private String ext;
        private String contentType;
    }

}


