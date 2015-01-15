package com.yammer.stresstime.entities;

import com.google.common.collect.ImmutableSet;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/* TODO: Collection handling methods */
@Entity
@Table(name = "groups")
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long mId;

    @Column(name = "name")
    @NotBlank
    private String mName;

    @OneToMany(mappedBy = "mGroup")
    private Set<AssignableDay> mAssignableDays = new HashSet<>();

    @OneToMany(mappedBy = "mGroup")
    private Set<AssignmentType> mAssignmentTypes = new HashSet<>();

    @OneToMany(mappedBy = "mGroup")
    private Set<Membership> mMemberships = new HashSet<>();

    public long getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public Set<AssignableDay> getAssignableDays() {
        return ImmutableSet.copyOf(mAssignableDays);
    }

    public void setAssignableDays(Set<AssignableDay> assignableDays) {
        mAssignableDays = ImmutableSet.copyOf(mAssignableDays);
    }

    public Set<AssignmentType> getAssignmentTypes() {
        return ImmutableSet.copyOf(mAssignmentTypes);
    }

    public void setAssignmentTypes(Set<AssignmentType> assignmentTypes) {
        mAssignmentTypes = ImmutableSet.copyOf(assignmentTypes);
    }

    public Set<Membership> getMemberships() {
        return ImmutableSet.copyOf(mMemberships);
    }

    public Set<Employee> getEmployees() {
        ImmutableSet.Builder<Employee> employees = new ImmutableSet.Builder<>();
        for (Membership membership : mMemberships) {
            employees.add(membership.getEmployee());
        }
        return employees.build();
    }
}
