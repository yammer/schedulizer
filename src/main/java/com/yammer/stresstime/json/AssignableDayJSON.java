package com.yammer.stresstime.json;

import com.google.common.collect.Lists;
import com.yammer.stresstime.entities.AssignableDay;

import java.util.List;
import java.util.stream.Collectors;

public class AssignableDayJSON {
    private List<AssignmentJSON> mAssignments;
    private String mDate;

    public AssignableDayJSON(AssignableDay assignableDay) {
        mDate = assignableDay.getDate().toString();
        mAssignments = assignableDay.getAssignments().stream()
                .map(a -> new AssignmentJSON(a))
                .collect(Collectors.toList());
    }

    public List<AssignmentJSON> getAssignments() {
        return mAssignments;
    }

    public void setAssignments(List<AssignmentJSON> assignments) {
        mAssignments = assignments;
    }

    public String getDate() {
        return mDate;
    }

    public void setDate(String date) {
        mDate = date;
    }
}
