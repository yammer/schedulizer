package com.yammer.stresstime.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSet;
import org.hibernate.annotations.Type;
import org.joda.time.LocalDate;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "day_restrictions",
        uniqueConstraints = @UniqueConstraint(columnNames =
                {"date", "employee_id"}))
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

    @Column(name = "restriction_level")
    private int restrictionLevel;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    /* package private */ DayRestriction() {
        // Required by Hibernate
    }

    public DayRestriction(LocalDate date, Employee employee) {
        this.date = date;
        this.employee = employee;
    }

    public long getId() {
        return id;
    }

    @JsonProperty("date")
    public String getDateString() {
        return date.toString();
    }

    @JsonIgnore
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

    @JsonIgnore
    public Employee getEmployee() {
        return employee;
    }

    public long getEmployeeId() {
        return getEmployee().getId();
    }

    public int getRestrictionLevel() {
        return restrictionLevel;
    }

    public void setRestrictionLevel(int restrictionLevel) {
        this.restrictionLevel = restrictionLevel;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

}
