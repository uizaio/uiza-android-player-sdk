package com.uiza.sdk.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.uiza.sdk.R;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class UZToast {
    private static List<Toast> toastList = new ArrayList<>();

    private UZToast() {
    }

    public static void show(@NonNull Context context, String s) {
        show(context, s, 0);
    }

    public static void show(@NonNull Context context, int resource) {
        show(context, resource, 0);
    }

    @SuppressLint("InflateParams")
    public static void show(@NonNull Context context, int resource, int length) {
        show(context, context.getResources().getString(resource), length);
    }

    @SuppressLint("InflateParams")
    public static void show(@NonNull Context context, String msg, int length) {
        clear();
        try {
            LayoutInflater inf = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (inf != null) {
                View layout = inf.inflate(R.layout.view_l_toast, null);
                TextView textView = layout.findViewById(R.id.tv_loading);
                textView.setText(msg);
                Toast toast = new Toast(context);
                toast.setGravity(Gravity.FILL_HORIZONTAL, 0, 0);
                toast.setDuration(length);
                toast.setView(layout);
                toast.show();
                toastList.add(toast);
            }
        } catch (Exception e) {
            Timber.e(e, "UZToast");
        }
    }

    private static void clear() {
        for (int i = 0; i < toastList.size(); i++) {
            if (toastList.get(i) != null) {
                toastList.get(i).cancel();
            }
        }
    }
}
