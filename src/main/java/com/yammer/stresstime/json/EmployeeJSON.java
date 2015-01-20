package com.yammer.stresstime.json;

import com.yammer.stresstime.entities.Employee;

public class EmployeeJSON {
    private String mName;
    private String mYid;
    private long mId;

    public EmployeeJSON(Employee employee) {
        mName = employee.getName();
        mYid = employee.getYammerId();
        mId = employee.getId();
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public String getYid() {
        return mYid;
    }

    public void setYid(String yid) {
        this.mYid = yid;
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        this.mId = id;
    }
}
