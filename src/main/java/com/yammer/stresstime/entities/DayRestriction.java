package com.yammer.stresstime.entities;

import com.google.common.collect.ImmutableSet;
import org.hibernate.annotations.Type;
import org.joda.time.LocalDate;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "day_restrictions")
public class DayRestriction extends JsonAnnotatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "date")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentLocalDate")
    @NotNull
    private LocalDate date;

    @Column(name = "comment")
    private String comment;

    @ManyToMany
    @JoinTable(name = "day_restriction_assignment_type",
            joinColumns = { @JoinColumn(name = "day_restriction_id") },
            inverseJoinColumns = { @JoinColumn(name = "assignment_type_id") })
    private Set<AssignmentType> assignmentTypes = new HashSet<>();
    // ^ This represents the assignment types the employee cannot fulfill

    /* package private */ DayRestriction() {
        // Required by Hibernate
    }

    public DayRestriction(LocalDate date) {
        this.date = date;
    }

    public long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Set<AssignmentType> getAssignmentTypes() {
        return ImmutableSet.copyOf(assignmentTypes);
    }

    public void setAssignmentTypes(Set<AssignmentType> assignmentTypes) {
        this.assignmentTypes = ImmutableSet.copyOf(assignmentTypes);
    }
}
