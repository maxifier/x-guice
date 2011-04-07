package com.magenta.guice.events;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.EnumSet;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 16.06.2009
 * Time: 13:56:35
 */
public class EnumMatcher<T extends Enum<T>> implements EventMatcher<T> {
    private final EnumSet<T> enums;

    public EnumMatcher(T[] values) {
        if (values.length == 0) {
            enums = null;
        } else {
            enums = EnumSet.noneOf(values[0].getDeclaringClass());
            enums.addAll(Arrays.asList(values));
        }
    }

    @Override
    public boolean matches(@NotNull T event) {
        return enums == null || enums.contains(event);
    }
}
