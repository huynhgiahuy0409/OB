package com.mservice.fs.onboarding.model.verifyotp;

import lombok.Getter;
import lombok.Setter;

/**
 * @author hoang.thai
 * on 1/19/2024
 */
@Getter
@Setter
public class AttachFile {

    private String base64;
    private String name;
    private String ext;
    private String contentType;
    private Boolean forceDownload; //download when click link if true
    private String path;
}
