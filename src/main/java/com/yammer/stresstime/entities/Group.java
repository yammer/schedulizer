package com.yammer.stresstime.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.jaxrs.json.annotation.JSONP;
import com.google.common.collect.ImmutableSet;
import org.hibernate.annotations.*;
import org.hibernate.annotations.CascadeType;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.Set;

/* TODO: Collection handling methods */
@Entity
@Table(name = "groups")
public class Group {

    public Group(String name) {
        setName(name);
    }

    public Group() {
        // Required by Hibernate
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long mId;

    @Column(name = "name")
    @NotBlank
    private String mName;

    @OneToMany(mappedBy = "mGroup")
    @Cascade({CascadeType.DELETE})
    private Set<AssignableDay> mAssignableDays = new HashSet<>();

    @OneToMany(mappedBy = "mGroup")
    @Cascade({CascadeType.DELETE})
    private Set<AssignmentType> mAssignmentTypes = new HashSet<>();

    @OneToMany(mappedBy = "mGroup")
    @Cascade({CascadeType.DELETE})
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

    @JsonIgnore
    public Set<AssignableDay> getAssignableDays() {
        return ImmutableSet.copyOf(mAssignableDays);
    }

    public void setAssignableDays(Set<AssignableDay> assignableDays) {
        mAssignableDays = ImmutableSet.copyOf(mAssignableDays);
    }

    @JsonIgnore
    public Set<AssignmentType> getAssignmentTypes() {
        return ImmutableSet.copyOf(mAssignmentTypes);
    }

    public void setAssignmentTypes(Set<AssignmentType> assignmentTypes) {
        mAssignmentTypes = ImmutableSet.copyOf(assignmentTypes);
    }

    @JsonIgnore
    public Set<Membership> getMemberships() {
        return ImmutableSet.copyOf(mMemberships);
    }

    @JsonIgnore
    public Set<Employee> getEmployees() {
        ImmutableSet.Builder<Employee> employees = new ImmutableSet.Builder<>();
        for (Membership membership : mMemberships) {
            employees.add(membership.getEmployee());
        }
        return employees.build();
    }
}
