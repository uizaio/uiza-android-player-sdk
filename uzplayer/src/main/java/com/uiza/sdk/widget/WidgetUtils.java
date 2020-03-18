package com.uiza.sdk.widget;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

import androidx.annotation.NonNull;

public class WidgetUtils {

    private static final String COPY_LABEL = "Copy";

    private WidgetUtils() {
    }

    public static void setClipboard(@NonNull Context context, String text) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(COPY_LABEL, text);
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
            UZToast.show(context, "Copied!");
        }
    }
}
