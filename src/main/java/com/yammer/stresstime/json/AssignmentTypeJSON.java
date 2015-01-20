package com.yammer.stresstime.json;

import com.yammer.stresstime.entities.AssignmentType;

public class AssignmentTypeJSON {
    private String mName;
    private String mDescription;
    private long mId;

    public AssignmentTypeJSON(AssignmentType assignmentType) {
        mName = assignmentType.getName();
        mDescription = assignmentType.getDescription();
        mId = assignmentType.getId();
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        mId = id;
    }
}
