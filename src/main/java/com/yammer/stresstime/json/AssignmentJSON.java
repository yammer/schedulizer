package com.yammer.stresstime.json;

import com.yammer.stresstime.entities.Assignment;

public class AssignmentJSON {

    private long mId;
    private long mAssignmentTypeId;
    private long mEmployeeId;

    public AssignmentJSON(Assignment assignment) {
        mId = assignment.getId();
        mAssignmentTypeId = assignment.getAssignmentType().getId();
        mEmployeeId = assignment.getEmployee().getId();
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        this.mId = id;
    }

    public long getAssignmentTypeId() {
        return mAssignmentTypeId;
    }

    public void setAssignmentTypeId(long assignmentTypeId) {
        mAssignmentTypeId = assignmentTypeId;
    }

    public long getEmployeeId() {
        return mEmployeeId;
    }

    public void setEmployeeId(long employeeId) {
        mEmployeeId = employeeId;
    }

}
