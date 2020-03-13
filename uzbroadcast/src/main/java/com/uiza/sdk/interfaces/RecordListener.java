package com.uiza.sdk.interfaces;


import com.uiza.sdk.enums.RecordStatus;

public interface RecordListener {

    void onStatusChange(RecordStatus status);
}
