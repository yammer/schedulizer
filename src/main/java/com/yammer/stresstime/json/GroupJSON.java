package com.yammer.stresstime.json;

import com.yammer.stresstime.entities.Group;

public class GroupJSON {

    private String mName;
    private long mId;

    public GroupJSON(Group group) {
        this.mName = group.getName();
        this.mId = group.getId();
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        this.mId = id;
    }
}
