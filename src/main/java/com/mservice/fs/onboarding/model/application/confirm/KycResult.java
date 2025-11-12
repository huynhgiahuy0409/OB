package com.mservice.fs.onboarding.model.application.confirm;

import lombok.Getter;
import lombok.Setter;

/**
 * @author hoang.thai
 * on 1/8/2024
 */
@Getter
@Setter
public class KycResult {

    private FaceData faceData;
    private OcrData ocrData;
}
