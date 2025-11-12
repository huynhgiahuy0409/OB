package com.mservice.fs.onboarding.connection.jdbc;

import com.mservice.fs.jdbc.processor.CallableInputParam;
import com.mservice.fs.jdbc.processor.CallableProcessor;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.model.PackageInfo;
import com.mservice.fs.utils.CommonConstant;
import com.mservice.fs.utils.Utils;
import org.apache.commons.lang3.StringUtils;

import java.sql.CallableStatement;
import java.sql.SQLException;

/**
 * @author hoang.thai
 * on 12/14/2023
 */
public class UpdatePackageProcessor extends CallableProcessor<Void> {

    @Override
    protected Void processWithStatement(CallableStatement callableStatement) throws SQLException, BaseException {
        return null;
    }

    public Void execute(PackageInfo chosenPackage, String applicationId) throws BaseException, Exception {
        return run(
                new CallableInputParam("P_APPLICATION_ID", applicationId),
                new CallableInputParam("P_LOAN_AMOUNT", chosenPackage.getLoanAmount()),
                new CallableInputParam("P_PACKAGE_CODE", chosenPackage.getPackageCode()),
                new CallableInputParam("P_DISBURSED_AMOUNT", chosenPackage.getDisbursedAmount()),
                new CallableInputParam("P_MIN_LOAN_AMOUNT", chosenPackage.getMinLoanAmount()),
                new CallableInputParam("P_MAX_LOAN_AMOUNT", chosenPackage.getMaxLoanAmount()),
                new CallableInputParam("P_AVAILABLE_AMOUNTS", Utils.isNotEmpty(chosenPackage.getAvailableLoanAmounts()) ? StringUtils.join(chosenPackage.getAvailableLoanAmounts(), CommonConstant.COMMA_SPLITTER) : CommonConstant.STRING_EMPTY)

        );
    }

}
