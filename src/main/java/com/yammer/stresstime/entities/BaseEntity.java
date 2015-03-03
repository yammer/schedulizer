package com.yammer.stresstime.entities;

public abstract class BaseEntity extends JsonAnnotatedEntity {
    public abstract long getId();

    @Override
    public boolean equals(Object other) {
        if (!other.getClass().equals(this.getClass())) {
            return false;
        }
        return getId() == ((BaseEntity)other).getId();
    }
}
