package com.yammer.schedulizer.entities;

import com.yammer.schedulizer.managers.AssignmentTypeManager;
import com.yammer.schedulizer.managers.DayRestrictionManager;
import com.yammer.schedulizer.test.DatabaseTest;
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
