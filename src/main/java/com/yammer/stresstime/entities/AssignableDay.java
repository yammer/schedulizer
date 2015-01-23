package com.yammer.stresstime.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.ImmutableSet;
import org.hibernate.annotations.*;
import org.hibernate.annotations.CascadeType;
import org.joda.time.LocalDate;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.util.Set;

@Entity
@Table(name = "assignable_days")
public class AssignableDay {

    public  AssignableDay() {
        // Required by Hibernate
    }

    public AssignableDay(Group group, LocalDate date) {
        setGroup(group);
        setDate(date);
    }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long mId;

    @Column(name = "date")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentLocalDate")
    @NotNull
    private LocalDate mDate;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group mGroup;

    @OneToMany(mappedBy = "mAssignableDay")
    @Cascade({CascadeType.DELETE})
    private Set<Assignment> mAssignments;

    public long getId() {
        return mId;
    }

    @JsonIgnore
    public LocalDate getDate() {
        return mDate;
    }

    @JsonProperty("date")
    public String getDateStr() {
        return mDate.toString();
    }

    public void setDate(LocalDate date) {
        mDate = date;
    }

    @JsonIgnore
    public Group getGroup() {
        return mGroup;
    }

    public void setGroup(Group group) {
        mGroup = group;
    }

    public Set<Assignment> getAssignments() {
        return ImmutableSet.copyOf(mAssignments);
    }

    public void setAssignments(Set<Assignment> assignments) {
        mAssignments = ImmutableSet.copyOf(assignments);
    }
}
