package de.uniluebeck.itm.tr.eventstore.eval;

import com.google.common.collect.ArrayTable;
import com.google.common.collect.Table;

import java.util.Formatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

public abstract class RunStatsHelper {

    public static Table<Integer, RunStats.Field, String> toTable(List<RunStats> runStats) {
        Iterable<Integer> rowKeys = IntRangeIterator.createIterable(0, runStats.size());
        Iterable<RunStats.Field> colKeys = newArrayList(RunStats.Field.values());
        ArrayTable<Integer, RunStats.Field, String> table = ArrayTable.<Integer, RunStats.Field, String>create(rowKeys, colKeys);
        IntStream.range(0, runStats.size()).forEach(rowKey -> {
            for (RunStats.Field colKey : colKeys) {
                String value = runStats.get(rowKey).get(colKey);
                table.put(rowKey, colKey, value);
            }
        });
        return table;
    }

    public static String toTableString(List<RunStats> stats) {

        Table<Integer, RunStats.Field, String> table = toTable(stats);
        Set<RunStats.Field> columnKeys = table.columnKeySet();
        Set<Integer> rowKeys = table.rowKeySet();
        Map<RunStats.Field, Integer> colWidths = newHashMap();

        // calculate maximum column width
        columnKeys.forEach((k) -> colWidths.put(k, k.name().length() + 2));
        columnKeys.forEach((col) -> {
            rowKeys.forEach((row) -> {
                int curWidth = table.get(row, col).length() + 2;
                if (colWidths.get(col) < curWidth) {
                    colWidths.put(col, curWidth);
                }
            });
        });

        Formatter formatter = new Formatter();

        // print table header
        columnKeys.forEach((colKey) -> {
            Integer colWidth = colWidths.get(colKey);
            formatter.format("%" + colWidth + "s", colKey);
        });

        formatter.format("%s%n", " ");

        // print table values
        rowKeys.forEach((rowKey) -> {
            columnKeys.forEach((colKey) -> {

                String value = table.get(rowKey, colKey);
                Integer colWidth = colWidths.get(colKey);

                formatter.format("%" + colWidth + "s", value);
            });
            formatter.format("%s%n", " ");
        });

        return formatter.toString();
    }
}
