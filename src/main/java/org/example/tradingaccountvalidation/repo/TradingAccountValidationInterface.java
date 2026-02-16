package org.example.tradingaccountvalidation.repo;

import org.example.tradingaccountvalidation.model.DynamicAccountSnapshot;

public interface TradingAccountValidationInterface {
    DynamicAccountSnapshot validateAccount(DynamicAccountSnapshot snapshot);
}
