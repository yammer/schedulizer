package com.yammer.stresstime.entities;

import com.yammer.stresstime.managers.AssignableDayManager;
import com.yammer.stresstime.managers.GroupManager;
import com.yammer.stresstime.test.DatabaseTest;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class AssignableDayTest extends DatabaseTest {

    private AssignableDayManager assignableDayManager;
    private GroupManager groupManager;
    private Group group;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        assignableDayManager = new AssignableDayManager(getSessionFactory());
        groupManager = new GroupManager(getSessionFactory());
        group = groupManager.random();
    }

    @Test
    public void testDateFieldEqualityIsTimeAgnostic() {
        AssignableDay day = new AssignableDay(group, new LocalDate(1985, 2, 24));

        assertThat(day.getDate(), equalTo(new LocalDate(1985, 2, 24)));
        assertThat(day.getDate(), equalTo(new LocalDateTime(1985, 2, 24, 2, 30).toLocalDate()));

        assignableDayManager.save(day);
        refresh(day);

        assertThat(day.getDate(), equalTo(new LocalDate(1985, 2, 24)));
        assertThat(day.getDate(), equalTo(new LocalDateTime(1985, 2, 24, 1, 15).toLocalDate()));

        assignableDayManager.delete(day);
    }
}
