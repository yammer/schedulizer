package com.yammer.stresstime.test;

import com.google.common.base.Throwables;
import com.yammer.stresstime.entities.*;
import org.joda.time.LocalDate;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static junit.framework.TestCase.fail;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

public class TestUtils {

    private static AtomicInteger counter = new AtomicInteger(0);

    /**
     * Useful for retrieving a unique number within the whole test suite.
     * @return An integer unique within this test session context
     */
    public static int nextInt() {
        return counter.getAndIncrement();
    }

    public static String nextYammerId() {
        /* TODO: Create yammer id own counter to prevent premature overflow (really?) */
        return "<yid-" + nextInt() + ">";
    }

    public static String nextRandomString() {
        return "<random-string-" + nextInt() + ">";
    }

    public static void assertCauses(Class<? extends Throwable> exception, Runnable runnable) {
        try {
            runnable.run();
            fail("Expected exception " + exception.getSimpleName() + " but none was thrown");
        } catch (Throwable e) {
            /* TODO: Meaningful message */
            Assert.assertThat(Throwables.getCausalChain(e), hasItem(instanceOf(exception)));
        }
    }

    public static void assertThrows(Class<? extends Throwable> exception, Runnable runnable) {
        try {
            runnable.run();
            fail("Expected exception " + exception.getSimpleName() + " but none was thrown");
        } catch (Throwable e) {
            if (!exception.isInstance(e)) {
                throw e;
            }
        }
    }

    public static void assertListOfEntitiesEqualsAnyOrder(List<? extends BaseEntity> expected, List<? extends BaseEntity> found) {
        found.sort((e1, e2) -> Long.compare(e1.getId(), e2.getId()));
        expected.sort((e1, e2) -> Long.compare(e1.getId(), e2.getId()));
        assertArrayEquals(expected.toArray(), found.toArray());
    }

    private static <E extends BaseEntity> List<E> testPeriod(List<E> all, List<E> found, Predicate<? super E> filter){
        List<E> expected = all
                .stream()
                .filter(filter)
                .collect(Collectors.toList());
        assertListOfEntitiesEqualsAnyOrder(expected, found);
        return expected;
    }

    public static boolean isDateBetween(LocalDate d, LocalDate startDate, LocalDate endDate) {
        return ((d.isAfter(startDate) && d.isBefore(endDate)) ||
                d.isEqual(startDate) ||
                d.isEqual(endDate));
    }

    public static void testDayRestrictionEmployeePeriod(Employee employee, LocalDate startDate, LocalDate endDate,
                                                        List<DayRestriction> allDayRestrictions, List<DayRestriction> found) {
        if (startDate.isAfter(endDate)) {
            assertThat(found.size(), equalTo(0));
            return;
        }
        testPeriod(allDayRestrictions,
                found,
                d -> (d.getEmployee().equals(employee) && isDateBetween(d.getDate(), startDate, endDate)));
    }

    public static void testDayRestrictionGroupPeriod(Group group, LocalDate startDate, LocalDate endDate,
                                                        List<DayRestriction> allDayRestrictions, List<DayRestriction> found) {
        if (startDate.isAfter(endDate)) {
            assertThat(found.size(), equalTo(0));
            return;
        }
        testPeriod(allDayRestrictions,
                found,
                d -> (group.getEmployees().contains(d.getEmployee()) && isDateBetween(d.getDate(), startDate, endDate)));
    }

    public static void testAssignmentsEmployeePeriod(Employee employee, LocalDate startDate, LocalDate endDate,
                                                     List<Assignment> allAssignments, List<Assignment> found) {
        if (startDate.isAfter(endDate)) {
            assertThat(found.size(), equalTo(0));
            return;
        }
        testPeriod(allAssignments,
                found,
                a -> (a.getEmployee().equals(employee) && isDateBetween(a.getAssignableDay().getDate(), startDate, endDate)));
    }

    public static void testAssignableDaysGroupPeriod(Group group, LocalDate startDate, LocalDate endDate,
                                                     List<AssignableDay> allAssignableDays, List<AssignableDay> found) {
        if (startDate.isAfter(endDate)) {
            assertThat(found.size(), equalTo(0));
            return;
        }
        List<AssignableDay> expected =
                testPeriod(allAssignableDays,
                    found,
                    a -> (a.getGroup().equals(group) && isDateBetween(a.getDate(), startDate, endDate)));
        expected.sort((a, b) -> Long.compare(a.getId(), b.getId()));
        found.sort((a, b) -> Long.compare(a.getId(), b.getId()));
        for (int i = 0; i < expected.size(); i++) {
            List<Assignment> a1 = new ArrayList<>(expected.get(i).getAssignments());
            List<Assignment> a2 = new ArrayList<>(found.get(i).getAssignments());
            assertListOfEntitiesEqualsAnyOrder(a1, a2);
        }
    }

    // Prevents instantiation
    private TestUtils() {
        throw new AssertionError("Cannot instantiate object from " + this.getClass());
    }
}
