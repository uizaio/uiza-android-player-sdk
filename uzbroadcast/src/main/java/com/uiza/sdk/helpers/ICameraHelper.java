package com.uiza.sdk.helpers;

import android.view.MotionEvent;

import androidx.annotation.NonNull;

import com.pedro.encoder.input.gl.render.filters.BaseFilterRender;
import com.pedro.encoder.input.video.CameraHelper;
import com.pedro.rtplibrary.base.Camera2Base;
import com.uiza.sdk.interfaces.UZCameraChangeListener;
import com.uiza.sdk.interfaces.UZCameraOpenException;
import com.uiza.sdk.interfaces.UZRecordListener;
import com.uiza.sdk.profile.AudioAttributes;
import com.uiza.sdk.profile.VideoAttributes;
import com.uiza.sdk.profile.VideoSize;

import java.io.IOException;
import java.util.List;

public interface ICameraHelper {
    /**
     * @param reTries retry connect reTries times
     */
    void setConnectReTries(int reTries);

    /**
     * @param uzCameraChangeListener
     */
    void setUZCameraChangeListener(UZCameraChangeListener uzCameraChangeListener);

    /**
     * @param uzRecordListener
     */
    void setUZRecordListener(UZRecordListener uzRecordListener);

    /**
     * Set filter in position 0.
     *
     * @param filterReader filter to set. You can modify parameters to filter after set it to stream.
     */
    void setFilter(BaseFilterRender filterReader);

    /**
     * Set filter in position 0.
     *
     * @param filterReader filter to set. You can modify parameters to filter after set it to stream.
     */
    void setFilter(int filterPosition, BaseFilterRender filterReader);

    /**
     * Get Anti alias is enabled.
     *
     * @return true is enabled, false is disabled.
     */
    boolean isAAEnabled();

    /**
     * Enable or disable Anti aliasing (This method use FXAA).
     *
     * @param aAEnabled true is AA enabled, false is AA disabled. False by default.
     */
    void enableAA(boolean aAEnabled);

    /**
     * get Stream Width
     */
    int getStreamWidth();

    /**
     * get Stream Height
     */
    int getStreamHeight();

    /**
     * Enable a muted microphone, can be called before, while and after stream.
     */
    void enableAudio();


    /**
     * Mute microphone, can be called before, while and after stream.
     */

    void disableAudio();

    /**
     * Get mute state of microphone.
     *
     * @return true if muted, false if enabled
     */
    boolean isAudioMuted();

    /**
     * Call this method before use @startStream. If not you will do a stream without audio.
     *
     * @param attrs {@link AudioAttributes}
     * @return true if success, false if you get a error (Normally because the encoder selected
     * doesn't support any configuration seated or your device hasn't a AAC encoder).
     */
    boolean prepareAudio(@NonNull AudioAttributes attrs);

    /**
     * Get video camera state
     *
     * @return true if disabled, false if enabled
     */
    boolean isVideoEnabled();

    /**
     * Use {@link VideoAttributes} and portrait
     *
     * @return true if success, false if you get a error (Normally because the encoder selected
     * doesn't support any configuration seated or your device hasn't a H264 encoder).
     */

    boolean prepareVideo(@NonNull VideoAttributes attrs);

    /**
     * @param attrs    {@link VideoAttributes}
     * @param rotation could be 90, 180, 270 or 0 (Normally 0 if you are streaming in landscape or 90
     *                 if you are streaming in Portrait). This only affect to stream result. NOTE: Rotation with
     *                 encoder is silence ignored in some devices.
     * @return true if success, false if you get a error (Normally because the encoder selected
     * doesn't support any configuration seated or your device hasn't a H264 encoder).
     */
    boolean prepareVideo(@NonNull VideoAttributes attrs,
                         int rotation
    );

    List<VideoSize> getSupportedResolutions();

    /**
     * Need be called after @prepareVideo or/and @prepareAudio. This method override resolution of
     *
     * @param liveEndpoint of the stream like: rtmp://ip:port/application/stream_name
     *                     <p>
     *                     RTMP: rtmp://192.168.1.1:1935/fmp4/live_stream_name
     * @startPreview to resolution seated in @prepareVideo. If you never startPreview this method
     * startPreview for you to resolution seated in @prepareVideo.
     */
    void startStream(String liveEndpoint);

    /**
     * Stop stream started with @startStream.
     */
    void stopStream();

    /**
     * Get stream state.
     *
     * @return true if streaming, false if not streaming.
     */
    boolean isStreaming();

    /**
     * Switch camera used. Can be called on preview or while stream, ignored with preview off.
     *
     * @throws UZCameraOpenException If the other camera doesn't support same resolution.
     */
    void switchCamera() throws UZCameraOpenException;

    void startPreview(CameraHelper.Facing cameraFacing);

    /**
     * Start preview
     */
    void startPreview(CameraHelper.Facing cameraFacing, int width, int height);

    /**
     * is Front Camera
     */
    boolean isFrontCamera();

    /**
     * check is on preview
     *
     * @return true if onpreview, false if not preview.
     */
    boolean isOnPreview();

    /**
     * Stop camera preview. Ignored if streaming or already stopped. You need call it after
     *
     * @stopStream to release camera properly if you will close activity.
     */
    void stopPreview();

    /**
     * Get record state.
     *
     * @return true if recording, false if not recoding.
     */
    boolean isRecording();

    /**
     * Start record a MP4 video. Need be called while stream.
     *
     * @param savePath where file will be saved.
     * @throws IOException If you init it before start stream.
     */
    void startRecord(String savePath) throws IOException;

    /**
     * Stop record MP4 video started with @startRecord. If you don't call it file will be unreadable.
     */
    void stopRecord();

    /**
     * Set video bitrate of H264 in kb while stream.
     *
     * @param bitrate H264 in kb.
     */
    void setVideoBitrateOnFly(int bitrate);

    int getBitrate();


    boolean reTry(long delay, String reason);

    /**
     * Check support Flashlight
     * if use Camera1 always return false
     *
     * @return true if support, false if not support.
     */
    boolean isLanternSupported();

    /**
     * @required: <uses-permission android:name="android.permission.FLASHLIGHT"/>
     */
    void enableLantern() throws Exception;

    /**
     * @required: <uses-permission android:name="android.permission.FLASHLIGHT"/>
     */
    void disableLantern();

    boolean isLanternEnabled();

    /**
     * Return max zoom level
     *
     * @return max zoom level
     */
    float getMaxZoom();

    /**
     * Return current zoom level
     *
     * @return current zoom level
     */
    float getZoom();

    /**
     * Set zoomIn or zoomOut to camera.
     * Use this method if you use a zoom slider.
     *
     * @param level Expected to be >= 1 and <= max zoom level
     * @see Camera2Base#getMaxZoom()
     */
    void setZoom(float level);

    /**
     * Set zoomIn or zoomOut to camera.
     *
     * @param event motion event. Expected to get event.getPointerCount() > 1
     */
    void setZoom(MotionEvent event);
}
