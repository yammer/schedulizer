package com.yammer.schedulizer.entities;

public abstract class BaseEntity extends JsonAnnotatedEntity {
    public abstract long getId();

    @Override
    public boolean equals(Object other) {
        return other.getClass().equals(this.getClass()) && getId() == ((BaseEntity) other).getId();
    }
}
