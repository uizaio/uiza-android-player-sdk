package com.uiza.sdk.util;


import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public final class ListUtils {
    // default constructor is private
    private ListUtils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    /**
     * Returns true if the list is null or 0-length.
     *
     * @param list the list to be examined
     * @return true if list is null or zero length
     */
    public static <T> boolean isEmpty(@Nullable List<T> list) {
        return list == null || list.isEmpty();
    }

    /**
     * Returns a stream consisting of the elements of this stream that match
     * the given predicate.
     *
     * <p>This is an <a href="package-summary.html#StreamOps">intermediate
     * operation</a>.
     *
     * @param predicate a <a href="package-summary.html#NonInterference">non-interfering</a>,
     *                  <a href="package-summary.html#Statelessness">stateless</a>
     *                  predicate to apply to each element to determine if it
     *                  should be included
     * @return the new List
     */
    public static <T> List<T> filter(@NonNull List<T> list, Pre<T, Boolean> predicate) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return list.stream().filter(predicate::get).collect(Collectors.toList());
        } else {
            List<T> col = new ArrayList<>();
            for (int i = 0; i < list.size(); i++)
                if (predicate.get(list.get(i)))
                    col.add(list.get(i));
            return col;
        }
    }

    /**
     * Returns a stream consisting of the results of applying the given
     * function to the elements of this stream.
     *
     * <p>This is an <a href="package-summary.html#StreamOps">intermediate
     * operation</a>.
     *
     * @param <R> The element type of the new stream
     * @param predicate a <a href="package-summary.html#NonInterference">non-interfering</a>,
     *               <a href="package-summary.html#Statelessness">stateless</a>
     *               function to apply to each element
     * @return the new List
     */
    public static <T, R> List<R> map(List<T> list, Pre<T, R> predicate) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return list.stream().map(predicate::get).collect(Collectors.toList());
        } else {
            List<R> cols = new ArrayList<>();
            for (int i = 0; i < list.size(); i++) {
                cols.add(predicate.get(list.get(i)));
            }
            return cols;
        }
    }

    public interface Pre<T, R> {
        R get(T item);
    }
}
