package com.yammer.stresstime.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

@Entity
@Table(name = "assignments",
        uniqueConstraints = @UniqueConstraint(columnNames =
                {"assignment_type_id", "assignable_day_id", "employee_id"}))
public class Assignment {

    public Assignment() {
        // Required by Hibernate
    }

    public Assignment(Employee employee, AssignableDay assignableDay, AssignmentType assignmentType) {
        setEmployee(employee);
        setAssignableDay(assignableDay);
        setAssignmentType(assignmentType);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "assignment_type_id")
    private AssignmentType assignmentType;

    @ManyToOne
    @JoinColumn(name = "assignable_day_id")
    private AssignableDay assignableDay;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    public long getId() {
        return id;
    }

    @JsonIgnore
    public AssignmentType getAssignmentType() {
        return assignmentType;
    }

    public void setAssignmentType(AssignmentType assignmentType) {
        this.assignmentType = assignmentType;
    }

    @JsonIgnore
    public AssignableDay getAssignableDay() {
        return assignableDay;
    }

    public void setAssignableDay(AssignableDay assignableDay) {
        this.assignableDay = assignableDay;
    }

    @JsonIgnore
    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public long getAssignmentTypeId() { return getAssignmentType().getId(); }

    public long getEmployeeId() { return getEmployee().getId(); }
}
