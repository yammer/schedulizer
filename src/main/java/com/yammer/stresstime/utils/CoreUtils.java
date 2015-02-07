package com.yammer.stresstime.utils;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;

public class CoreUtils {

    public static String[] splitOnFirstOccurrenceAndTrim(String string, String pattern) {
        int i = string.indexOf(pattern);
        String first = string.substring(0, i).trim();
        String last = string.substring(i + 1).trim();
        return new String[] {first, last};
    }

    public static <E extends Throwable> Optional<E> getCause(Throwable root, Class<? extends E> klass) {
        return convertOptional(Throwables.getCausalChain(root).stream()
                .filter(klass::isInstance)
                .map(klass::cast)
                .findAny());
    }

    public static <T> Optional<T> convertOptional(java.util.Optional<? extends T> optional) {
        return (optional.isPresent()) ? Optional.of(optional.get()) : Optional.absent();
    }

    // Prevents instantiation
    private CoreUtils() {
        throw new AssertionError("Cannot instantiate object from " + this.getClass());
    }
}
