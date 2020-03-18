package com.uiza.sdk.widget.imersivedialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

import com.uiza.sdk.R;
import com.uiza.sdk.widget.UZToast;

/**
 * Created by LENOVO on 3/30/2018.
 * extend from {@link android.support.v4.app.DialogFragment}
 */

public class ImmersiveDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setTitle("Example Dialog")
                .setMessage("Some text.")
                .create();

        // Temporarily set the dialogs window to not focusable to prevent the short
        // popup of the navigation bar.
        Window dialogWindow = alertDialog.getWindow();
        if (dialogWindow == null) return null;
        dialogWindow.addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                UZToast.show(getActivity(), "Touch OK");
            }
        });
        return alertDialog;
    }

    public void showImmersive() {

        Activity ac = getActivity();
        if (ac instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) ac;
            // Show the dialog.
//        show(activity.getFragmentManager(), null);
            show(activity.getSupportFragmentManager(), null);
            // It is necessary to call executePendingTransactions() on the FragmentManager
            // before hiding the navigation bar, because otherwise getWindow() would raise a
            // NullPointerException since the window was not yet created.
            getFragmentManager().executePendingTransactions();

            // Hide the navigation bar. It is important to do this after show() was called.
            // If we would do this in onCreateDialog(), we would get a requestFeature()
            // error.
            Window window = getDialog().getWindow();
            if (window == null) return;
            window.getDecorView().setSystemUiVisibility(
                    activity.getWindow().getDecorView().getSystemUiVisibility()
            );

            // Make the dialogs window focusable again.
            window.clearFlags(
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            );
        }
    }

}