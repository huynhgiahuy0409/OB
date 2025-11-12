package com.mservice.fs.onboarding.model.verifyotp;

import com.mservice.fs.model.DefaultRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author hoang.thai
 * on 1/19/2024
 */
@Getter
@Setter
public class QueueContractRequest extends DefaultRequest {

    private List<AttachFile> attachFiles;
    private String partnerId;
    private String walletId;
    private String moduleName;
    private Boolean fromOldServer;

}
