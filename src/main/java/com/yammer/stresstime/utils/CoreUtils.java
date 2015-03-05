package com.yammer.stresstime.utils;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

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

    public static <K, V, T extends Map<K, V>> Collector<Map<K, V>, T, T> mergingMapCollector(
            Supplier<T> supplier,
            BinaryOperator<V> merger) {

        BiConsumer<T, Map<K, V>> accumulator = (map, partial) -> {
            partial.entrySet().forEach(e -> map.merge(e.getKey(), e.getValue(), merger));
        };
        BinaryOperator<T> combiner = combiner(supplier, accumulator);

        return Collector.of(
                supplier, accumulator, combiner,
                Collector.Characteristics.IDENTITY_FINISH,
                Collector.Characteristics.CONCURRENT,
                Collector.Characteristics.UNORDERED);
    }

    public static <T, K> Collector<T, ?, Map<K, Long>> countingByCollector(
            Function<? super T, ? extends K> classifier) {

        return Collectors.groupingBy(classifier, Collectors.counting());
    }

    public static <T> BinaryOperator<T> combiner(
            Supplier<T> supplier,
            BiConsumer<? super T, ? super T> accumulator) {

        return (a, b) -> {
            T ans = supplier.get();
            accumulator.accept(ans, a);
            accumulator.accept(ans, b);
            return ans;
        };
    }

    public static <I> List<I> concatLists(List<I> a, List<I> b) {
        return Lists.newArrayList(Iterables.concat(a, b));
    }

    // Prevents instantiation
    private CoreUtils() {
        throw new AssertionError("Cannot instantiate object from " + this.getClass());
    }
}
