package com.yammer.stresstime.auth;

import com.google.common.base.Throwables;
import org.junit.Assert;
import org.junit.Test;

import java.util.AbstractMap;
import java.util.Map;

import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class AuthorizationHeaderTest {

    @Test
    public void testSingleParameterWithTokenValues() throws Exception {
        test("Scheme key = value", "scheme", p("key", "value"));
        test("Scheme key = value  ", "scheme", p("key", "value"));
        test("  Scheme key = value", "scheme", p("key", "value"));
        test("Scheme key=value", "scheme", p("key", "value"));
        test("Scheme key=value  ", "scheme", p("key", "value"));
        test("Scheme  key=value", "scheme", p("key", "value"));
    }

    @Test
    public void testSingleParameterWithQuotedStringValues() throws Exception {
        test("Scheme key = \"value\"", "scheme", p("key", "value"));
        test("Scheme key=\"value\"", "scheme", p("key", "value"));
    }

    @Test
    public void testSingleParameterWithQuotedStringWithEqual() throws Exception {
        test("Scheme key = \"val=ue\"", "scheme", p("key", "val=ue"));
    }

    @Test
    public void testMultipleParameters() throws Exception {
        test("Scheme first = value, second = value", "scheme", p("first", "value"), p("second", "value"));
        test("Scheme first = value,second = value", "scheme", p("first", "value"), p("second", "value"));
        test("Scheme first = value  , second = value", "scheme", p("first", "value"), p("second", "value"));
        test("Scheme first = \"value\", second = value", "scheme", p("first", "value"), p("second", "value"));
        test("Scheme first = value, second = value, third = value", "scheme", 
                p("first", "value"), p("second", "value"), p("third", "value"));
    }

    @Test
    public void testSchemeCaseInsentitivityAlwaysReturnsLowercase() throws Exception {
        test("SCHEME key = value", "scheme", p("key", "value"));
        test("Scheme key = value", "scheme", p("key", "value"));
        test("scheme key = value", "scheme", p("key", "value"));
        test("ScHeMe key = value", "scheme", p("key", "value"));
    }

    @Test
    public void testSchemes() throws Exception {
        test("SCH-EME key = value", "sch-eme", p("key", "value"));
        test("Sc_heme key = value", "sc_heme", p("key", "value"));
    }

    @Test
    public void testInvalidWithNull() throws Exception {
        assertThrows(NullPointerException.class, () -> AuthorizationHeader.decode(null));
    }

    @Test
    public void testInvalidWithoutParameters() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> AuthorizationHeader.decode("Schema"));
        assertThrows(IllegalArgumentException.class, () -> AuthorizationHeader.decode("Schema  "));
        assertThrows(IllegalArgumentException.class, () -> AuthorizationHeader.decode("  Schema"));
    }

    @Test
    public void testInvalidWithNonKeyValueParameters() throws Exception {
        assertThrows(IllegalArgumentException.class, () ->
                AuthorizationHeader.decode("Schema key, value"));
        assertThrows(IllegalArgumentException.class, () ->
                AuthorizationHeader.decode("Schema key = value, value"));
    }

    @SafeVarargs
    private final void test(String input, String scheme, Map.Entry<String, String>... params) {
        AuthorizationHeader header = AuthorizationHeader.decode(input);
        assertTrue(header.isScheme(scheme));
        assertThat(header.getScheme(), equalTo(scheme));
        for (Map.Entry<String, String> param : params) {
            String name = param.getKey();
            String value = param.getValue();
            assertTrue(header.hasParameter(name));
            assertThat(header.getParameter(name), equalTo(value));
        }
        assertThat(header.getParameters().keySet(), hasSize(params.length));
    }

    private <K, V> Map.Entry<K, V> p(K key, V value) {
        return new AbstractMap.SimpleImmutableEntry<>(key, value);
    }

    /* TODO: Extract to test utils */
    private void assertThrows(Class<? extends Throwable> exception, Runnable runnable) {
        try {
            runnable.run();
            fail("Expected exception " + exception.getSimpleName() + " but none was thrown");
        } catch (Throwable e) {
            Assert.assertThat(Throwables.getCausalChain(e), hasItem(instanceOf(exception)));
        }
    }
}
