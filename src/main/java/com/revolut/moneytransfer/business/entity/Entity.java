package com.revolut.moneytransfer.business.entity;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Entity otherEntity = (Entity) o;
        return this.matchId(otherEntity.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
