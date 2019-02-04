package com.revolut.moneytransfer.storage;

import com.revolut.moneytransfer.business.entity.AccountEntity;
import com.revolut.moneytransfer.business.entity.Entity;
import com.revolut.moneytransfer.business.entity.TransferEntity;

import java.math.BigDecimal;
import java.util.*;

import static com.revolut.moneytransfer.business.entity.AccountEntity.State.ACTIVE;
import static com.revolut.moneytransfer.business.entity.TransferEntity.State.COMPLETED;
import static com.revolut.moneytransfer.business.entity.TransferEntity.State.PENDING;

public class InMemoryStorage implements Storage {
    private Map<EntityName, Set<Entity>> data = new EnumMap<>(EntityName.class);

    public void dummyData() {
        Set<Entity> accountList = new HashSet<>();
        String account1Id = "/accounts/3fc6b414-cdb8-4b8f-beb5-fb08c2902f87";
        String account2Id = "/accounts/9aecab5d-3827-4624-97a9-11b1207c7a12";
        String account3Id = "/accounts/e9ccb93b-bded-41a3-8e7e-95c3a322a8ee";

        accountList.add(new AccountEntity(account1Id, "account1", new BigDecimal(9000), "EUR", ACTIVE));
        accountList.add(new AccountEntity(account2Id, "account2", new BigDecimal(7000), "EUR", ACTIVE));
        accountList.add(new AccountEntity(account3Id, "account3", new BigDecimal(5000), "EUR", ACTIVE));
        data.put(EntityName.ACCOUNT, accountList);

        Set<Entity> transferList = new HashSet<>();
        transferList.add(new TransferEntity("/transfers/467c34aa-cef8-bdef-8e7e-1er08c2901e90", account1Id, account2Id, new BigDecimal(1000), "EUR", PENDING));
        transferList.add(new TransferEntity("/transfers/816c2a88-7205-4a3b-905b-048af106847d", account1Id, account3Id, new BigDecimal(10000), "EUR", PENDING));
        data.put(EntityName.TRANSFER, transferList);
    }

    @Override
    public List<Entity> getAll(EntityName entityName) {
        return new ArrayList<>(data.get(entityName));
    }

    @Override
    public Optional<Entity> getById(EntityName entityName, String id) {
        return data.get(entityName).stream().filter(entity -> entity.matchId(id)).findAny();
    }

    @Override
    public Entity save(EntityName entityName, Entity entity) {
        data.get(entityName).remove(entity);
        data.get(entityName).add(entity);
        return entity;
    }
}
