package org.example.tradingaccountvalidation.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
@AllArgsConstructor
@Getter
public class ConditionMeta {

    private final String path;
    private final String expected;

}