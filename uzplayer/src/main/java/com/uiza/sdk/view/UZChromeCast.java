package com.uiza.sdk.view;

import android.content.Context;
import android.view.View;

import androidx.annotation.UiThread;

import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastState;
import com.uiza.sdk.chromecast.Casty;
import com.uiza.sdk.exceptions.ErrorConstant;
import com.uiza.sdk.util.UZAppUtils;
import com.uiza.sdk.util.UZData;
import com.uiza.sdk.util.UZViewUtils;
import com.uiza.sdk.widget.UZMediaRouteButton;

import timber.log.Timber;

/**
 * Created by loitp on 2/27/2019.
 */

public class UZChromeCast {

    protected final String TAG = "TAG" + getClass().getSimpleName();
    //chromecast https://github.com/DroidsOnRoids/Casty
    private UZMediaRouteButton uzMediaRouteButton;
    private UZChromeCastListener listener;

    {
        if (!UZAppUtils.checkChromeCastAvailable())
            throw new NoClassDefFoundError(ErrorConstant.ERR_505);
    }

    public void setUZChromeCastListener(UZChromeCastListener listener) {
        this.listener = listener;
    }

    public void setupChromeCast(Context context) {
        if (UZAppUtils.isTV(context)) return;
        uzMediaRouteButton = new UZMediaRouteButton(context);
        setUpMediaRouteButton();
        addUIChromecastLayer(context);
    }

    @UiThread
    private void setUpMediaRouteButton() {
        Casty casty = UZData.getInstance().getCasty();
        if (casty != null) {
            casty.setUpMediaRouteButton(uzMediaRouteButton);
            casty.setOnConnectChangeListener(new Casty.OnConnectChangeListener() {
                @Override
                public void onConnected() {
                    if (listener != null) listener.onConnected();
                }

                @Override
                public void onDisconnected() {
                    if (listener != null) listener.onDisconnected();
                }
            });
        }
    }

    private void updateMediaRouteButtonVisibility(int state) {
        UZViewUtils.setVisibilityViews(state == CastState.NO_DEVICES_AVAILABLE ? View.GONE : View.VISIBLE, uzMediaRouteButton);
    }

    //tự tạo layout chromecast và background đen
    //Gen layout chromecast with black backgroudn programmatically
    private void addUIChromecastLayer(Context context) {
        //listener check state of chromecast
        CastContext castContext = null;
        try {
            castContext = CastContext.getSharedInstance(context);
        } catch (Exception e) {
            Timber.e(e, "Error addUIChromecastLayer:");
        }
        if (castContext == null) {
            UZViewUtils.goneViews(uzMediaRouteButton);
            return;
        }
        updateMediaRouteButtonVisibility(castContext.getCastState());
        castContext.addCastStateListener(this::updateMediaRouteButtonVisibility);
        if (listener != null) listener.addUIChromecast();
    }

    public void setTintMediaRouteButton(final int color) {
        if (uzMediaRouteButton != null) {
            uzMediaRouteButton.post(() -> uzMediaRouteButton.applyTint(color));
        }
    }

    public UZMediaRouteButton getUzMediaRouteButton() {
        return uzMediaRouteButton;
    }

    public interface UZChromeCastListener {
        void onConnected();

        void onDisconnected();

        void addUIChromecast();
    }
}
