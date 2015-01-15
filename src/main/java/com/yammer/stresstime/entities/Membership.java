package com.yammer.stresstime.entities;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "memberships")
public class Membership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long mId;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee mEmployee;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group mGroup;

    @Column(name = "admin")
    @NotNull
    private boolean mAdmin = false;

    public long getId() {
        return mId;
    }

    public Employee getEmployee() {
        return mEmployee;
    }

    public void setEmployee(Employee employee) {
        mEmployee = employee;
    }

    public Group getGroup() {
        return mGroup;
    }

    public void setGroup(Group group) {
        mGroup = group;
    }

    public boolean isAdmin() {
        return mAdmin;
    }

    public void setAdmin(boolean admin) {
        mAdmin = admin;
    }
}
