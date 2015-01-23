package com.yammer.stresstime.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

@Entity
@Table(name = "assignments")
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
    private long mId;

    @ManyToOne
    @JoinColumn(name = "assignment_type_id")
    private AssignmentType mAssignmentType;

    @ManyToOne
    @JoinColumn(name = "assignable_day_id")
    private AssignableDay mAssignableDay;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee mEmployee;

    public long getId() {
        return mId;
    }

    @JsonIgnore
    public AssignmentType getAssignmentType() {
        return mAssignmentType;
    }

    public void setAssignmentType(AssignmentType assignmentType) {
        mAssignmentType = assignmentType;
    }

    @JsonIgnore
    public AssignableDay getAssignableDay() {
        return mAssignableDay;
    }

    public void setAssignableDay(AssignableDay assignableDay) {
        mAssignableDay = assignableDay;
    }

    @JsonIgnore
    public Employee getEmployee() {
        return mEmployee;
    }

    public void setEmployee(Employee employee) {
        mEmployee = employee;
    }

    public long getAssignmentTypeId() { return getAssignmentType().getId(); }

    public long getEmployeeId() { return getEmployee().getId(); }
}
