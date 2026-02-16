package org.example.tradingaccountvalidation.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.tradingaccountvalidation.service.TradingAccountValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountSnapshot {
    private static final Logger log = LoggerFactory.getLogger(TradingAccountValidationService.class);

    private String customerId;

    private String statusFrom;
    private String statusTo;

    private String equityOpenPosn;
    private String derivativeOpenPosn;
    private String mtfOpenPosn;

    private String marginPledge;
    private String cuspa;

    private String noOFSActivityLast24Months;
    private String noBuybackActivityLast24Months;
    private String noOpenOfferActivityLast24Months;
    private String noIPOSubscriptionLast24Months;

    private String noSGBTransactionLast24Months;
    private String noMutualFundTransactionLast24Months;
    private String noInsuranceTransactionLast24Months;
    private String noNCDBondActivityLast24Months;

    private String noNSDLOrCDSLActivityLast24Months;
    private String noApprovedPayInPayOutLast24Months;

    private String noKRAInitialOrModificationLast24Months;
    private String noCKYCUploadOrModificationLast24Months;

    private String noApprovedCRFLast24Months;

    private String noExchangeTradingLast24Months;

    private String notTradedOnCurrentDate;
    private String activeNACH;
    private String activeSIP;

    private String accountStatus;

    public void printStatus(String status) {
        log.info("Requested conversion: {}", status);
    }

    public void setAccountStatus(String status) {
        this.accountStatus = status;
        log.info("Allowed Account Status: {}", status);
    }
}