package com.yammer.stresstime.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

import static com.yammer.stresstime.utils.CoreUtils.convertOptional;

/* TODO: Collection handling methods */
@Entity
@Table(name = "groups")
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "name")
    @NotBlank
    private String name;

    @OneToMany(mappedBy = "group")
    @Cascade({CascadeType.DELETE})
    private Set<AssignableDay> assignableDays = new HashSet<>();

    @OneToMany(mappedBy = "group")
    @Cascade({CascadeType.DELETE})
    private Set<AssignmentType> assignmentTypes = new HashSet<>();

    @OneToMany(mappedBy = "group")
    @Cascade({CascadeType.DELETE})
    private Set<Membership> memberships = new HashSet<>();

    /* package private */ Group() {
        // Required by Hibernate
    }

    public Group(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonIgnore
    public Set<AssignableDay> getAssignableDays() {
        return ImmutableSet.copyOf(assignableDays);
    }

    public void setAssignableDays(Set<AssignableDay> assignableDays) {
        this.assignableDays = ImmutableSet.copyOf(this.assignableDays);
    }

    @JsonIgnore
    public Set<AssignmentType> getAssignmentTypes() {
        return ImmutableSet.copyOf(assignmentTypes);
    }

    public void setAssignmentTypes(Set<AssignmentType> assignmentTypes) {
        this.assignmentTypes = ImmutableSet.copyOf(assignmentTypes);
    }

    @JsonIgnore
    public Set<Membership> getMemberships() {
        return ImmutableSet.copyOf(memberships);
    }

    @JsonIgnore
    public Set<Employee> getEmployees() {
        ImmutableSet.Builder<Employee> employees = new ImmutableSet.Builder<>();
        for (Membership membership : memberships) {
            employees.add(membership.getEmployee());
        }
        return employees.build();
    }

    public boolean isMember(Employee employee) {
        return getMembership(employee).isPresent();
    }

    public boolean isAdmin(Employee employee) {
        Optional<Membership> membership = getMembership(employee);
        return membership.isPresent() && membership.get().isAdmin();
    }

    private Optional<Membership> getMembership(Employee employee) {
        return convertOptional(memberships.stream()
                .filter(m -> m.getEmployee().getId() == employee.getId())
                .findAny());
    }

}
