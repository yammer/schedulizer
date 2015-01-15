package com.yammer.stresstime.entities;

import com.google.common.collect.ImmutableSet;
import org.hibernate.annotations.Type;
import org.joda.time.LocalDate;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Set;

@Entity
@Table(name = "assignable_days")
public class AssignableDay {

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
    private Set<Assignment> mAssignments;

    public long getId() {
        return mId;
    }

    public LocalDate getDate() {
        return mDate;
    }

    public void setDate(LocalDate date) {
        mDate = date;
    }

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
