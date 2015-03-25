package com.yammer.schedulizer.entities;

import com.yammer.schedulizer.managers.GroupManager;
import com.yammer.schedulizer.test.DatabaseTest;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class GroupTest extends DatabaseTest {

    private GroupManager groupManager;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        groupManager = new GroupManager(getSessionFactory());
    }

    @Test
    public void testNewGroupHasEmptyAssignableDays() {
        Group group = new Group("Core Services");
        assertNotNull(group.getAssignableDays());
        assertThat(group.getAssignableDays(), empty());
    }
}
