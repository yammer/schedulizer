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

    private AssignableDayManager mAssignableDayManager;
    private GroupManager mGroupManager;
    private Group mGroup;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        mAssignableDayManager = new AssignableDayManager(getSessionFactory());
        mGroupManager = new GroupManager(getSessionFactory());
        mGroup = mGroupManager.random();
    }

    @Test
    public void testDateFieldEqualityIsTimeAgnostic() {
        AssignableDay day = new AssignableDay(mGroup, new LocalDate(1985, 2, 24));

        assertThat(day.getDate(), equalTo(new LocalDate(1985, 2, 24)));
        assertThat(day.getDate(), equalTo(new LocalDateTime(1985, 2, 24, 2, 30).toLocalDate()));

        mAssignableDayManager.save(day);
        refresh(day);

        assertThat(day.getDate(), equalTo(new LocalDate(1985, 2, 24)));
        assertThat(day.getDate(), equalTo(new LocalDateTime(1985, 2, 24, 1, 15).toLocalDate()));

        mAssignableDayManager.delete(day);
    }
}
