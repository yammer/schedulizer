package com.yammer.stresstime.entities;

import javax.persistence.*;

@Entity
@Table(name = "assignments")
public class Assignment {

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

    public AssignmentType getAssignmentType() {
        return mAssignmentType;
    }

    public void setAssignmentType(AssignmentType assignmentType) {
        mAssignmentType = assignmentType;
    }

    public AssignableDay getAssignableDay() {
        return mAssignableDay;
    }

    public void setAssignableDay(AssignableDay assignableDay) {
        mAssignableDay = assignableDay;
    }

    public Employee getEmployee() {
        return mEmployee;
    }

    public void setEmployee(Employee employee) {
        mEmployee = employee;
    }
}
