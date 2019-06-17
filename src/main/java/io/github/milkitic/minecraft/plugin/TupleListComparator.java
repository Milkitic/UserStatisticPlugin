package io.github.milkitic.minecraft.plugin;

import io.github.milkitic.minecraft.plugin.Generic.Tuple;

import java.util.Comparator;

public class TupleListComparator implements Comparator<Tuple<String, Long>> {

    @Override
    public int compare(Tuple<String, Long> tuple1, Tuple<String, Long> tuple2) {
        int result;
        if (tuple1 == null && tuple2 == null)
            result = 0;
        else if (tuple2 == null)
            result = 1;
        else if (tuple1 == null)
            result = -1;
        else if (tuple1.second.equals(tuple2.second))
            result = 0;
        else
            result = tuple1.second > tuple2.second ? 1 : 0;
        return -result;
    }
}