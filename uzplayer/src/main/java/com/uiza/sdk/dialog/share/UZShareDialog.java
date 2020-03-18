package com.uiza.sdk.dialog.share;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;

import com.uiza.sdk.R;
import com.uiza.sdk.util.UZAppUtils;
import com.uiza.sdk.util.UZData;
import com.uiza.sdk.util.UZViewUtils;
import com.uiza.sdk.widget.WidgetUtils;
import com.uiza.sdk.widget.flowlayout.FlowLayout;

import java.util.List;

/**
 * Created by loitp on 5/2/2018.
 */

public class UZShareDialog extends Dialog {

    //TODO correct this
    private static final String SUBJECT = "Uiza Sharing";
    private static final String MESSAGE = "https://play.google.com/store/apps/details?id=io.uiza.app";
    private static final String GOOGLE_DOCS_PACKAGE = "com.google.android.apps.docs";
    private static final String FACEBOOK_PACKAGE = "com.facebook.katana";
    private static final String CLIPBOARD = "clipboard";
    private Activity activity;
    private AlertDialog dialog;
    private FlowLayout ll;
    private boolean isLandscape;

    public UZShareDialog(@NonNull Activity activity, boolean isLandscape) {
        super(activity);
        this.activity = activity;
        this.isLandscape = isLandscape;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dlg_share);

        ll = findViewById(R.id.ll);
        ll.setChildSpacing(FlowLayout.SPACING_AUTO);
        ll.setChildSpacingForLastRow(FlowLayout.SPACING_ALIGN);
        ll.setRowSpacing(20f);

        findViewById(R.id.bt_exit).setOnClickListener(v -> dismiss());

        genUI();
    }

    private void genUI() {
        int screenW = UZViewUtils.getScreenWidth();
        int sizeIv;
        if (isLandscape) {
            sizeIv = screenW / 12;
        } else {
            sizeIv = screenW / 7;
        }

        List<ResolveInfo> resolveInfoList;
        if (UZData.getInstance().getResolveInfoList() == null) {
            Intent template = new Intent(Intent.ACTION_SEND);
            template.setType("text/plain");
            resolveInfoList = activity.getPackageManager().queryIntentActivities(template, 0);
            UZData.getInstance().setResolveInfoList(resolveInfoList);
        } else {
            resolveInfoList = UZData.getInstance().getResolveInfoList();
        }

        for (final ResolveInfo resolveInfo : resolveInfoList) {
            ImageView imageView = new ImageView(activity);
            imageView.setImageDrawable(resolveInfo.loadIcon(activity.getPackageManager()));
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            imageView.setPadding(sizeIv / 10, sizeIv / 10, sizeIv / 10, sizeIv / 10);
            imageView.setOnClickListener(v -> click(resolveInfo));
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(sizeIv, sizeIv);
            imageView.setLayoutParams(layoutParams);
            ll.addView(imageView);
        }
    }

    private void click(ResolveInfo resolveInfo) {
        String pkgName = resolveInfo.activityInfo.packageName;
        String label = (String) resolveInfo.loadLabel(activity.getPackageManager());
        if (pkgName.equals(GOOGLE_DOCS_PACKAGE) && label.toLowerCase().contains(CLIPBOARD)) {
            WidgetUtils.setClipboard(activity, MESSAGE);
        } else if (pkgName.equals(FACEBOOK_PACKAGE)) {
            UZAppUtils.sharingToSocialMedia(activity, resolveInfo.activityInfo.packageName, SUBJECT, MESSAGE);
        } else {
            UZAppUtils.sharingToSocialMedia(activity, resolveInfo.activityInfo.packageName, SUBJECT, MESSAGE);
        }
        dismiss();
    }
}