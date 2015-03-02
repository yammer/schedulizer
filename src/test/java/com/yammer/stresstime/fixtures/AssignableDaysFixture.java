package com.yammer.stresstime.fixtures;

import com.google.common.collect.Lists;
import com.yammer.stresstime.entities.AssignableDay;
import com.yammer.stresstime.entities.Group;
import com.yammer.stresstime.managers.AssignableDayManager;
import com.yammer.stresstime.test.TestUtils;
import org.hibernate.SessionFactory;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;

public class AssignableDaysFixture {

    private List<AssignableDay> assignableDays;
    private GroupsFixture groupsFixture;
    private boolean saved;

    public AssignableDaysFixture(GroupsFixture groupsFixture) {
        this.groupsFixture = groupsFixture;
        saved = false;
        List<Group> groups = groupsFixture.getGroups();
        List<LocalDate> dates = Lists.newArrayList(new LocalDate(2015,2,10),
                new LocalDate(2015,2,11),
                new LocalDate(2015,2,12),
                new LocalDate(2015,2,13),
                new LocalDate(2015,2,14),
                new LocalDate(2015,2,15),
                new LocalDate(2015,2,16),
                new LocalDate(2015,2,17),
                new LocalDate(2015,2,18),
                new LocalDate(2015,2,19),
                new LocalDate(2015,2,20),
                new LocalDate(2015,3,7),
                new LocalDate(2015,4,12),
                new LocalDate(2015,12,17),
                new LocalDate(2016,2,29), // leap year ;)
                new LocalDate(2017,2,10));
        assignableDays = new ArrayList<>();
        dates.stream().forEach(d -> groups.stream().forEach(g -> assignableDays.add(new AssignableDay(g, d))));
    }

    public void save(SessionFactory sessionFactory) {
        if (saved) return;
        saved = true;
        groupsFixture.save(sessionFactory);
        AssignableDayManager assignableDayManager = new AssignableDayManager(sessionFactory);
        assignableDays.stream().forEach(e -> assignableDayManager.save(e));
    }

    public List<AssignableDay> getAssignableDays() {
        return assignableDays;
    }
}
