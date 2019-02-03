package com.revolut.moneytransfer.entity;

public class Entity {
    private String id;

    public Entity(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public boolean matchId(String externalId) {

        return this.id.equals(externalId) || this.id.contains(externalId);
    }
}
