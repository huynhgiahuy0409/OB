package com.mservice.fs.onboarding.model.application.init;

import com.mservice.fs.generic.jdbc.DBColumn;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * @author hoang.thai
 * on 11/15/2023
 */
@Getter
@Setter
@Builder
public class AdditionalDataDB {

    @DBColumn(name = "CONTRACT_ID")
    private String contractId;
    @DBColumn(name = "KEY")
    private String key;
    @DBColumn(name = "VALUE")
    private String value;
}
