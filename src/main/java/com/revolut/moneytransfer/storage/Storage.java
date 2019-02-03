package com.revolut.moneytransfer.storage;

import com.revolut.moneytransfer.entity.Entity;

import java.util.List;
import java.util.Optional;

public interface Storage {
    <T extends Entity> List<T> getAll(EntityName entityName);

    <T extends Entity> Optional<T> getById(EntityName entityName, String id);

    <T extends Entity> T save(EntityName entityName, T entity);

    enum EntityName {
        ACCOUNT, TRANSFER
    }

}
