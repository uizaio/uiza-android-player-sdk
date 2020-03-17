package com.uiza.sdk.util;


import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.uiza.sdk.models.UZPlayback;

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

    public static <T> List<T> filter(@NonNull List<T> list, Pre<T, Boolean> pre) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return list.stream().filter(pre::get).collect(Collectors.toList());
        } else {
            List<T> col = new ArrayList<>();
            for (int i = 0; i < list.size(); i++)
                if (pre.get(list.get(i)))
                    col.add(list.get(i));
            return col;
        }
    }

    public static <T> List<UZPlayback> map(List<T> list, Pre<T, UZPlayback> pre) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return list.stream().map(pre::get).collect(Collectors.toList());
        } else {
            List<UZPlayback> cols = new ArrayList<>();
            for (int i = 0; i < list.size(); i++) {
                cols.add(pre.get(list.get(i)));
            }
            return cols;
        }
    }

    public interface Pre<T, R> {
        R get(T item);
    }
}
