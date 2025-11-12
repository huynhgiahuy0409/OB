package com.mservice.fs.onboarding.model.verifyotp;

import com.mservice.fs.model.DefaultResponse;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class QueueGenLinkResponse extends DefaultResponse {

    private List<LinkFileData> attachFiles;

    @Getter
    @Setter
    public static class LinkFileData {

        private String base64;
        private String ext;
        private String contentType;
        private boolean forceDownload;
        private String path;
        private String link;
        private long size;
        private long expiredTime;
    }

}
