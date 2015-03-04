package com.yammer.stresstime.fixtures;

import com.google.common.collect.Lists;
import com.yammer.stresstime.entities.AssignmentType;
import com.yammer.stresstime.entities.Group;
import com.yammer.stresstime.managers.AssignmentTypeManager;
import org.hibernate.SessionFactory;

import java.util.List;

public class AssignmentTypesFixture {

    private List<AssignmentType> assignmentTypes;
    private GroupsFixture groupsFixture;
    private boolean saved;

    public AssignmentTypesFixture(GroupsFixture groupsFixture) {
        this.groupsFixture = groupsFixture;
        saved = false;
        List<Group> groups = groupsFixture.getGroups();
        assignmentTypes = Lists.newArrayList(new AssignmentType("Primary", groups.get(0)),
                new AssignmentType("Secondary", groups.get(0)),
                new AssignmentType("Primary Support", groups.get(0)),
                new AssignmentType("AT 1", groups.get(1)),
                new AssignmentType("AT 2", groups.get(1)),
                new AssignmentType("AT 3", groups.get(1)),
                new AssignmentType("AT 4", groups.get(1)),
                new AssignmentType("Name", groups.get(2)));
    }

    public void save(SessionFactory sessionFactory) {
        if (saved) return;
        saved = true;
        groupsFixture.save(sessionFactory);
        AssignmentTypeManager assignmentTypeManager = new AssignmentTypeManager(sessionFactory);
        assignmentTypes.stream().forEach(e -> assignmentTypeManager.save(e));
    }

    public List<AssignmentType> getAssignmentTypes() {
        return assignmentTypes;
    }
}
