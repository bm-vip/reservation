package com.azki.reservation.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author Behrooz Mohamadi
 */

public final class MapperHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(MapperHelper.class);

    @SafeVarargs
    public static <I, O> Optional<O> option(Function<I, O> function, I input, Class<? extends Throwable>... expectedExceptions) {
        return option(() -> function.apply(input), expectedExceptions);
    }

    @SafeVarargs
    public static <O> Optional<O> option(Supplier<O> supplier, Class<? extends Throwable>... expectedExceptions) {
        return Optional.ofNullable(get(supplier, expectedExceptions));
    }

    @SafeVarargs
    public static <I, O> O get(Function<I, O> function, I input, Class<? extends Throwable>... expectedExceptions) {
        return get(() -> function.apply(input), expectedExceptions);
    }

    @SafeVarargs
    public static <O> O get(Supplier<O> supplier, Class<? extends Throwable>... expectedExceptions) {
        return getOrDefault(supplier, null, expectedExceptions);
    }

    @SafeVarargs
    public static <I, O> O getOrDefault(Function<I, O> function, I input, O defaultValue, Class<? extends Throwable>... expectedExceptions) {
        return getOrDefault(() -> function.apply(input), defaultValue, expectedExceptions);
    }

    /**
     * If supplier throw null pointer exception or any expected exceptions return default value otherwise return
     * function's result
     *
     * @param supplier
     * @param defaultValue
     * @param <O> Class of output
     * @return
     */
    @SafeVarargs
    public static <O> O getOrDefault(Supplier<O> supplier, O defaultValue, Class<? extends Throwable>... expectedExceptions) {
        try {
            return Optional.ofNullable(supplier.get()).orElse(defaultValue);
        } catch (Throwable e) {
            LOGGER.debug(e.toString(), e);
            if (e instanceof NullPointerException || Arrays.asList(expectedExceptions).contains(e.getClass()))
                return defaultValue;
            if (expectedExceptions.length > 0) {
                for (Class<?> expectedException : expectedExceptions)
                    if (expectedException.isAssignableFrom(e.getClass()))
                        return defaultValue;
            }
            throw e;
        }
    }
}
