package com.revolut.moneytransfer.service;

import java.util.UUID;

public class IDGenerator {

    public String generateAccountId() {
        return "/accounts/" + UUID.randomUUID().toString();
    }

    public String generateTransferId() {
        return "/transfers/" + UUID.randomUUID().toString();
    }

}
