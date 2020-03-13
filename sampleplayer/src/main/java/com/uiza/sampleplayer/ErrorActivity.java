package com.uiza.sampleplayer;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.uiza.sdk.exceptions.ErrorConstant;

public class ErrorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error);
        TextView tvErr = findViewById(R.id.tv_err);
        String s = ErrorConstant.ERR_CODE_0 + " - " + ErrorConstant.ERR_0 + "\n"
                + ErrorConstant.ERR_CODE_1 + " - " + ErrorConstant.ERR_1 + "\n"
                + ErrorConstant.ERR_CODE_2 + " - " + ErrorConstant.ERR_2 + "\n"
                + ErrorConstant.ERR_CODE_3 + " - " + ErrorConstant.ERR_3 + "\n"
                + ErrorConstant.ERR_CODE_4 + " - " + ErrorConstant.ERR_4 + "\n"
                + ErrorConstant.ERR_CODE_5 + " - " + ErrorConstant.ERR_5 + "\n"
                + ErrorConstant.ERR_CODE_6 + " - " + ErrorConstant.ERR_6 + "\n"
                + ErrorConstant.ERR_CODE_7 + " - " + ErrorConstant.ERR_7 + "\n"
                + ErrorConstant.ERR_CODE_8 + " - " + ErrorConstant.ERR_8 + "\n"
                + ErrorConstant.ERR_CODE_9 + " - " + ErrorConstant.ERR_9 + "\n"
                + ErrorConstant.ERR_CODE_10 + " - " + ErrorConstant.ERR_10 + "\n"
                + ErrorConstant.ERR_CODE_11 + " - " + ErrorConstant.ERR_11 + "\n"
                + ErrorConstant.ERR_CODE_12 + " - " + ErrorConstant.ERR_12 + "\n"
                + ErrorConstant.ERR_CODE_13 + " - " + ErrorConstant.ERR_13 + "\n"
                + ErrorConstant.ERR_CODE_14 + " - " + ErrorConstant.ERR_14 + "\n"
                + ErrorConstant.ERR_CODE_15 + " - " + ErrorConstant.ERR_15 + "\n"
                + ErrorConstant.ERR_CODE_16 + " - " + ErrorConstant.ERR_16 + "\n"
                + ErrorConstant.ERR_CODE_17 + " - " + ErrorConstant.ERR_17 + "\n"
                + ErrorConstant.ERR_CODE_18 + " - " + ErrorConstant.ERR_18 + "\n"
                + ErrorConstant.ERR_CODE_19 + " - " + ErrorConstant.ERR_19 + "\n"
                + ErrorConstant.ERR_CODE_20 + " - " + ErrorConstant.ERR_20 + "\n"
                + ErrorConstant.ERR_CODE_21 + " - " + ErrorConstant.ERR_21 + "\n"
                + ErrorConstant.ERR_CODE_22 + " - " + ErrorConstant.ERR_22 + "\n"
                + ErrorConstant.ERR_CODE_23 + " - " + ErrorConstant.ERR_23 + "\n"
                + ErrorConstant.ERR_CODE_24 + " - " + ErrorConstant.ERR_24 + "\n"
                + ErrorConstant.ERR_CODE_400 + " - " + ErrorConstant.ERR_400 + "\n"
                + ErrorConstant.ERR_CODE_401 + " - " + ErrorConstant.ERR_401 + "\n"
                + ErrorConstant.ERR_CODE_404 + " - " + ErrorConstant.ERR_404 + "\n"
                + ErrorConstant.ERR_CODE_422 + " - " + ErrorConstant.ERR_422 + "\n"
                + ErrorConstant.ERR_CODE_500 + " - " + ErrorConstant.ERR_500 + "\n"
                + ErrorConstant.ERR_CODE_503 + " - " + ErrorConstant.ERR_503 + "\n";

        tvErr.setText(s);
    }
}
