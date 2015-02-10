package com.yammer.stresstime.test;

import com.google.common.base.Throwables;
import org.junit.Assert;

import static junit.framework.TestCase.fail;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.instanceOf;

public class TestUtils {

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

    // Prevents instantiation
    private TestUtils() {
        throw new AssertionError("Cannot instantiate object from " + this.getClass());
    }
}
