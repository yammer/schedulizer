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
public class DayRestriction {

    public DayRestriction() {
        // Required by Hibernate
    }

    public DayRestriction(LocalDate date) {
        setDate(date);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long mId;

    @Column(name = "date")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentLocalDate")
    @NotNull
    private LocalDate mDate;

    @Column(name = "comment")
    private String mComment;

    @ManyToMany
    @JoinTable(name = "day_restriction_assignment_type",
            joinColumns = { @JoinColumn(name = "day_restriction_id") },
            inverseJoinColumns = { @JoinColumn(name = "assignment_type_id") })
    private Set<AssignmentType> mAssignmentTypes = new HashSet<>();
    // ^ This represents the assignment types the employee cannot fulfill

    public long getId() {
        return mId;
    }

    public LocalDate getDate() {
        return mDate;
    }

    public void setDate(LocalDate date) {
        mDate = date;
    }

    public String getComment() {
        return mComment;
    }

    public void setComment(String comment) {
        mComment = comment;
    }

    public Set<AssignmentType> getAssignmentTypes() {
        return ImmutableSet.copyOf(mAssignmentTypes);
    }

    public void setAssignmentTypes(Set<AssignmentType> assignmentTypes) {
        mAssignmentTypes = ImmutableSet.copyOf(assignmentTypes);
    }
}
