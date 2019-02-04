package com.revolut.moneytransfer.business.service;

import java.util.UUID;

public class IDGenerator {

    public static String generateAccountId() {
        return "/accounts/" + UUID.randomUUID().toString();
    }

    public static String generateTransferId() {
        return "/transfers/" + UUID.randomUUID().toString();
    }

}
