package com.uiza.sdk.helpers;

import android.hardware.Camera;
import android.view.MotionEvent;

import androidx.annotation.NonNull;

import com.pedro.encoder.input.gl.render.filters.BaseFilterRender;
import com.pedro.encoder.input.video.CameraHelper;
import com.pedro.encoder.input.video.CameraOpenException;
import com.pedro.rtplibrary.rtmp.RtmpCamera1;
import com.uiza.sdk.enums.RecordStatus;
import com.uiza.sdk.interfaces.UZCameraChangeListener;
import com.uiza.sdk.interfaces.UZCameraOpenException;
import com.uiza.sdk.interfaces.UZRecordListener;
import com.uiza.sdk.profile.AudioAttributes;
import com.uiza.sdk.profile.VideoAttributes;
import com.uiza.sdk.profile.VideoSize;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * use camera1 library
 */
public class Camera1Helper implements ICameraHelper {

    private RtmpCamera1 rtmpCamera1;

    private UZCameraChangeListener uzCameraChangeListener;

    private UZRecordListener uzRecordListener;

    public Camera1Helper(@NonNull RtmpCamera1 camera) {
        this.rtmpCamera1 = camera;
    }

    @Override
    public void setConnectReTries(int reTries) {
        rtmpCamera1.setReTries(reTries);
    }

    @Override
    public boolean reTry(long delay, @NonNull String reason) {
        return rtmpCamera1.reTry(delay, reason);
    }

    @Override
    public void setUZCameraChangeListener(@NonNull UZCameraChangeListener uzCameraChangeListener) {
        this.uzCameraChangeListener = uzCameraChangeListener;
    }

    @Override
    public void setUZRecordListener(UZRecordListener uzRecordListener) {
        this.uzRecordListener = uzRecordListener;
    }


    @Override
    public void setFilter(@NotNull BaseFilterRender filterReader) {
        rtmpCamera1.getGlInterface().setFilter(filterReader);
    }

    @Override
    public void setFilter(int filterPosition, @NotNull BaseFilterRender filterReader) {
        rtmpCamera1.getGlInterface().setFilter(filterPosition, filterReader);
    }

    @Override
    public void enableAA(boolean aAEnabled) {
        rtmpCamera1.getGlInterface().enableAA(aAEnabled);
    }

    @Override
    public boolean isAAEnabled() {
        return rtmpCamera1.getGlInterface().isAAEnabled();
    }

    @Override
    public int getStreamWidth() {
        return rtmpCamera1.getStreamHeight();
    }

    @Override
    public int getStreamHeight() {
        return rtmpCamera1.getStreamWidth();
    }

    @Override
    public void enableAudio() {
        rtmpCamera1.enableAudio();
    }

    @Override
    public void disableAudio() {
        rtmpCamera1.disableAudio();
    }

    @Override
    public boolean isAudioMuted() {
        return rtmpCamera1.isAudioMuted();
    }

    @Override
    public boolean prepareAudio(@NonNull AudioAttributes attrs) {
        return rtmpCamera1.prepareAudio(
                attrs.getBitRate(),
                attrs.getSampleRate(),
                attrs.isStereo(),
                attrs.isEchoCanceler(),
                attrs.isNoiseSuppressor()
        );
    }

    @Override
    public boolean isVideoEnabled() {
        return rtmpCamera1.isVideoEnabled();
    }

    @Override
    public boolean prepareVideo(@NotNull VideoAttributes profile) {
        return prepareVideo(profile, 90);
    }

    @Override
    public boolean prepareVideo(@NotNull VideoAttributes attrs, int rotation) {
        return rtmpCamera1.prepareVideo(attrs.getSize().getWidth(),
                attrs.getSize().getHeight(),
                attrs.getFrameRate(),
                attrs.getBitRate(),
                false,
                attrs.getFrameInterval(),
                rotation,
                attrs.getAVCProfile(),
                attrs.getAVCProfileLevel()
        );
    }

    @Override
    public void startStream(@NotNull String liveEndpoint) {
        rtmpCamera1.startStream(liveEndpoint);
    }

    @Override
    public void stopStream() {
        rtmpCamera1.stopStream();
    }

    @Override
    public boolean isStreaming() {
        return rtmpCamera1.isStreaming();
    }

    @Override
    public void setVideoBitrateOnFly(int bitrate) {
        rtmpCamera1.setVideoBitrateOnFly(bitrate);
    }

    @Override
    public int getBitrate() {
        return rtmpCamera1.getBitrate();
    }

    @Override
    public boolean isFrontCamera() {
        return rtmpCamera1.isFrontCamera();
    }

    @Override
    public void switchCamera() throws UZCameraOpenException {
        try {
            rtmpCamera1.switchCamera();
        } catch (CameraOpenException e) {
            throw new UZCameraOpenException(e.getMessage());
        }
        if (uzCameraChangeListener != null)
            uzCameraChangeListener.onCameraChange(rtmpCamera1.isFrontCamera());
    }

    @Override
    public List<VideoSize> getSupportedResolutions() {
        List<Camera.Size> sizes;
        if (rtmpCamera1.isFrontCamera()) {
            sizes = rtmpCamera1.getResolutionsFront();
        } else {
            sizes = rtmpCamera1.getResolutionsBack();
        }
        List<VideoSize> usizes = new ArrayList<>();
        for (Camera.Size s : sizes) {
            usizes.add(VideoSize.fromSize(s));
        }
        return usizes;
    }

    @Override
    public void startPreview(@NotNull CameraHelper.Facing cameraFacing) {
        // because portrait
        rtmpCamera1.startPreview(cameraFacing, 480, 640);
    }

    @Override
    public void startPreview(@NotNull CameraHelper.Facing cameraFacing, int w, int h) {
        // because portrait
        rtmpCamera1.startPreview(cameraFacing, h, w);
    }

    @Override
    public boolean isOnPreview() {
        return rtmpCamera1.isOnPreview();
    }

    @Override
    public void stopPreview() {
        rtmpCamera1.stopPreview();
    }

    @Override
    public boolean isRecording() {
        return rtmpCamera1.isRecording();
    }

    @Override
    public void startRecord(@NotNull String savePath) throws IOException {
        if (uzRecordListener != null)
            rtmpCamera1.startRecord(savePath, status -> uzRecordListener.onStatusChange(RecordStatus.lookup(status)));
        else
            rtmpCamera1.startRecord(savePath);
    }

    @Override
    public void stopRecord() {
        rtmpCamera1.stopRecord();
        rtmpCamera1.startPreview();
    }

    @Override
    public boolean isLanternSupported() {
        return false;
    }

    @Override
    public void enableLantern() throws Exception {
        rtmpCamera1.enableLantern();
    }

    @Override
    public void disableLantern() {
        rtmpCamera1.disableLantern();
    }

    @Override
    public boolean isLanternEnabled() {
        return rtmpCamera1.isLanternEnabled();
    }

    @Override
    public float getMaxZoom() {
        return 1.0f;
    }

    @Override
    public float getZoom() {
        return 1.0f;
    }

    @Override
    public void setZoom(float level) {

    }

    @Override
    public void setZoom(@NotNull MotionEvent event) {
        rtmpCamera1.setZoom(event);
    }
}
