package com.uiza.sdk.interfaces;


import com.uiza.sdk.enums.RecordStatus;

public interface UZRecordListener {

    void onStatusChange(RecordStatus status);
}
