package com.yammer.stresstime.entities;

import com.yammer.stresstime.managers.GroupManager;
import com.yammer.stresstime.test.DatabaseTest;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class GroupTest extends DatabaseTest {

    private GroupManager mGroupManager;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        mGroupManager = new GroupManager(getSessionFactory());
    }

    @Test
    public void testNewGroupHasEmptyAssignableDays() {
        Group group = new Group();
        assertNotNull(group.getAssignableDays());
        assertThat(group.getAssignableDays(), empty());
    }
}
