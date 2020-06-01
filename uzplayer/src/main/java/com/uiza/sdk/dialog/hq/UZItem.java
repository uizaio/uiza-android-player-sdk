package com.uiza.sdk.dialog.hq;

import android.widget.CheckedTextView;

import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.ui.TrackNameProvider;

import java.util.Locale;

//https://www.image-engineering.de/library/technotes/991-separating-sd-hd-full-hd-4k-and-8k
public class UZItem {
    private CheckedTextView checkedTextView;
    private String description;
    private Format format;

    private UZItem(Format format, String description) {
        this.format = format;
        this.description = description;
    }

    private UZItem() {
        this.description = "Unknow";
    }

    public static UZItem create(Format format, String description) {
        return new UZItem(format, description);

    }

    public static UZItem create() {
        return new UZItem();
    }

    public CheckedTextView getCheckedTextView() {
        return checkedTextView;
    }

    public void setCheckedTextView(CheckedTextView checkedTextView) {
        this.checkedTextView = checkedTextView;
    }

    public String getDescription() {
        return description;
    }

    void setDescription(String description) {
        this.description = description;
    }

    public Format getFormat() {
        return format;
    }

    public void setFormat(Format format) {
        this.format = format;
    }
}
