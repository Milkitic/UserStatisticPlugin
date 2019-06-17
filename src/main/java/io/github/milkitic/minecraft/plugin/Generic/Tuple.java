package io.github.milkitic.minecraft.plugin.Generic;

import java.text.MessageFormat;

public class Tuple<T1, T2> {
    public final T1 first;

    public final T2 second;

    public Tuple(T1 a, T2 b) {
        first = a;
        second = b;
    }

    public String toString() {
        return MessageFormat.format("({0}, {1})", first, second);
    }
}
