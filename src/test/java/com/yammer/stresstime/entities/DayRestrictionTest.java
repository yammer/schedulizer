package com.yammer.stresstime.entities;

import com.google.common.collect.ImmutableSet;
import com.yammer.stresstime.managers.AssignmentTypeManager;
import com.yammer.stresstime.managers.DayRestrictionManager;
import com.yammer.stresstime.test.DatabaseTest;
import org.joda.time.LocalDate;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.Assert.assertNotNull;

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
