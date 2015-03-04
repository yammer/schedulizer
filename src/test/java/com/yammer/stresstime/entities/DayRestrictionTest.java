package com.yammer.stresstime.entities;

import com.yammer.stresstime.managers.AssignmentTypeManager;
import com.yammer.stresstime.managers.DayRestrictionManager;
import com.yammer.stresstime.test.DatabaseTest;
import org.junit.Before;

public class DayRestrictionTest extends DatabaseTest {

    private DayRestrictionManager dayRestrictionManager;
    private AssignmentTypeManager assignmentTypeManager;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        dayRestrictionManager = new DayRestrictionManager(getSessionFactory());
        assignmentTypeManager = new AssignmentTypeManager(getSessionFactory());
    }
}
