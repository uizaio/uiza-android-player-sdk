package com.uiza.sdk.floatview;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewStub;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.google.android.exoplayer2.Player;
import com.uiza.sdk.R;
import com.uiza.sdk.animations.AnimationUtils;
import com.uiza.sdk.exceptions.UZException;
import com.uiza.sdk.models.UZPlayback;
import com.uiza.sdk.util.ConnectivityUtils;
import com.uiza.sdk.util.Constants;
import com.uiza.sdk.util.LocalData;
import com.uiza.sdk.util.TmpParamData;
import com.uiza.sdk.util.UZAppUtils;
import com.uiza.sdk.util.UZData;
import com.uiza.sdk.util.UZViewUtils;
import com.uiza.sdk.view.CommunicateMng;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import timber.log.Timber;

/**
 * Created by loitp on 1/28/2019.
 */

public class UZFloatVideoService extends Service implements UZFloatVideoView.Callback {
    private WindowManager mWindowManager;
    private View mFloatingView;
    private View viewDestroy;
    private RelativeLayout rlControl;
    private RelativeLayout moveView;
    private ImageView btExit;
    private ImageView btFullScreen;
    private ImageView btPlayPause;
    private TextView tvMsg;
    private UZFloatVideoView uzFLVideo;
    private ViewStub controlStub;
    private WindowManager.LayoutParams params;
    private String linkPlay;
    private boolean isLiveStream;
    private long contentPosition;
    private int screenWidth;
    private int screenHeight;
    private int pipTopPosition;
    private int videoW = 16;
    private int videoH = 9;
    private boolean isEZDestroy;
    private boolean isEnableVibration;
    private boolean isEnableSmoothSwitch;
    private boolean isAutoSize;
    private int videoWidthFromSettingConfig;
    private int videoHeightFromSettingConfig;
    private int marginL;
    private int marginT;
    private int marginR;
    private int marginB;
    private int progressBarColor;
    private int positionBeforeDisappearX = -1;
    private int positionBeforeDisappearY = -1;
    private CountDownTimer countDownTimer;
    private GestureDetector mTapDetector;
    private POS pos;
    private boolean isSendMsgToActivity;

    public UZFloatVideoService() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        contentPosition = intent.getLongExtra(Constants.FLOAT_CONTENT_POSITION, 0);
        progressBarColor = intent.getIntExtra(Constants.FLOAT_PROGRESS_BAR_COLOR, Color.WHITE);
        int pipControlSkin = intent.getIntExtra(Constants.FLOAT_CONTROL_SKIN_ID, 0);
        if (controlStub != null && rlControl == null) {
            // this means control is not inflated yet
            if (pipControlSkin != 0)
                controlStub.setLayoutResource(pipControlSkin);
            inflateControls();
        }
        if (UZData.getInstance().getPlayback() == null)
            return START_NOT_STICKY;
        if (intent.getExtras() != null) {
            linkPlay = intent.getStringExtra(Constants.FLOAT_LINK_PLAY);
            isLiveStream = intent.getBooleanExtra(Constants.FLOAT_IS_LIVESTREAM, false);
            Timber.d("onStartCommand contentPosition: %d,linkPlay: %s", contentPosition, linkPlay);
            setupVideo();
        }
        return START_NOT_STICKY;
    }

    private void findViews() {
        moveView = mFloatingView.findViewById(R.id.move_view);
        uzFLVideo = mFloatingView.findViewById(R.id.uz_flvideo_view);
        controlStub = mFloatingView.findViewById(R.id.control_stub);

        tvMsg = mFloatingView.findViewById(R.id.tv_msg);
        UZViewUtils.setTextShadow(tvMsg, Color.BLACK);
        tvMsg.setOnClickListener(v -> AnimationUtils.play(v, Techniques.Pulse, new AnimationUtils.Callback() {
            @Override
            public void onCancel() {
            }

            @Override
            public void onEnd() {
                setupVideo();
            }

            @Override
            public void onRepeat() {
            }

            @Override
            public void onStart() {
            }
        }));

        viewDestroy = mFloatingView.findViewById(R.id.view_destroy);
        int colorViewDestroy = LocalData.getMiniPlayerColorViewDestroy();
        viewDestroy.setBackgroundColor(colorViewDestroy);
        isEZDestroy = LocalData.getMiniPlayerEzDestroy();
        isEnableVibration = LocalData.getMiniPlayerEnableVibration();
        isEnableSmoothSwitch = LocalData.getMiniPlayerEnableSmoothSwitch();

        isAutoSize = LocalData.getMiniPlayerAutoSize();
        if (!isAutoSize) {
            videoWidthFromSettingConfig = LocalData.getMiniPlayerSizeWidth();
            videoHeightFromSettingConfig = LocalData.getMiniPlayerSizeHeight();
        }
        //Drag and move floating view using user's touch action.
        dragAndMove();
    }

    private void inflateControls() {
        controlStub.inflate();
        // inflate controls from skin
        rlControl = mFloatingView.findViewById(R.id.controls_root);
        btExit = mFloatingView.findViewById(R.id.uiza_mini_exit);
        btFullScreen = mFloatingView.findViewById(R.id.uiza_mini_full_screen);
        btPlayPause = mFloatingView.findViewById(R.id.uiza_mini_pause_resume);
        setControlsClickListener();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
        UZPlayback mPlaybackInfo = UZData.getInstance().getPlayback();
        if (mPlaybackInfo == null) stopSelf();
        videoW = LocalData.getVideoWidth();
        videoH = LocalData.getVideoHeight();
        screenWidth = UZViewUtils.getScreenWidth();
        screenHeight = UZViewUtils.getScreenHeight();
        pipTopPosition = LocalData.getStablePipTopPosition();
        marginL = LocalData.getMiniPlayerMarginL();
        marginT = LocalData.getMiniPlayerMarginT();
        marginR = LocalData.getMiniPlayerMarginR();
        marginB = LocalData.getMiniPlayerMarginB();
        mFloatingView = LayoutInflater.from(this).inflate(R.layout.layout_uz_floating_video, null, false);
        findViews();
        //Add the view to the window.
        int LAYOUT_FLAG;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        else
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;

        //OPTION 1: floatview se neo vao 1 goc cua device
        /*params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);*/
        //OPTION 2: floatview se ko neo vao 1 goc cua device
        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                LAYOUT_FLAG, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);

        setSizeMoveView(true);

        //Specify the view position
        //OPTION 1
        //Initially view will be added to top-left corner
        //params.gravity = Gravity.TOP | Gravity.LEFT;
        //params.x = 0;
        //params.y = 0;
        //OPTION 2
        //right-bottom corner
        /*params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = screenWidth - getMoveViewWidth();
        params.y = screenHeight - getMoveViewHeight();*/
        //OPTION 3
        //init lan dau tien se neo vao canh BOTTOM_RIGHT cua man hinh
        /*params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = screenWidth - getMoveViewWidth();
        //params.y = screenHeight - getMoveViewHeight();
        params.y = screenHeight - getMoveViewHeight() - pipTopPosition;
        //LLog.d(TAG, "first position: " + params.x + "-" + params.y);*/
        //OPTION 4
        //float view o ben ngoai screen cua device
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = screenWidth - 1;
        params.y = screenHeight - 1;

        //Add the view to the window
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        if (mWindowManager != null)
            mWindowManager.addView(mFloatingView, params);
    }

    private void setControlsClickListener() {
        if (btExit != null)
            btExit.setOnClickListener(v -> stopSelf());
        if (btFullScreen != null)
            btFullScreen.setOnClickListener(v -> openApp());
        if (btPlayPause != null)
            btPlayPause.setOnClickListener(v -> toggleResumePause());
    }

    //==================================================================================================START CONFIG
    private void toggleResumePause() {
        btPlayPause.setImageResource(uzFLVideo.togglePauseResume()
                ? R.drawable.ic_pause_circle_outline_white_48
                : R.drawable.ic_play_circle_outline_white_48);
    }

    private void pauseVideo() {
        if (uzFLVideo == null || btPlayPause == null) return;
        uzFLVideo.pause();
        btPlayPause.setImageResource(R.drawable.ic_play_circle_outline_white_48);
    }

    private void resumeVideo() {
        if (uzFLVideo == null || btPlayPause == null) return;
        uzFLVideo.resume();
        btPlayPause.setImageResource(R.drawable.ic_pause_circle_outline_white_48);
    }

    private void disappear() {
        if (uzFLVideo == null) return;
        positionBeforeDisappearX = params.x;
        positionBeforeDisappearY = params.y;
        updateUISlide(screenWidth, screenHeight);
    }

    private void appear() {
        if (positionBeforeDisappearX == -1 || positionBeforeDisappearY == -1)
            return;
        updateUISlide(positionBeforeDisappearX, positionBeforeDisappearY);
        positionBeforeDisappearX = -1;
        positionBeforeDisappearY = -1;
    }

    private void openApp() {
        if (uzFLVideo == null || uzFLVideo.getPlayer() == null) {
            Timber.d("fuzVideo == null || fuzVideo.getPlayer() == null");
            return;
        }
        //stop video
        if (!isEnableSmoothSwitch)
            uzFLVideo.getPlayer().setPlayWhenReady(false);
        //moveView.setOnTouchListener(null);//disabled move view
        LocalData.setClickedPip(true);
        if (UZData.getInstance().getPlayback() == null) {
            Timber.d("getPlayback == null");
            return;
        }
        CommunicateMng.MsgFromServiceOpenApp msgFromServiceOpenApp = new CommunicateMng.MsgFromServiceOpenApp(null);
        msgFromServiceOpenApp.setPositionMiniPlayer(uzFLVideo.getCurrentPosition());
        CommunicateMng.postFromService(msgFromServiceOpenApp);
    }

    private boolean isControllerShowing() {
        return (rlControl != null) && (rlControl.getVisibility() == View.VISIBLE);
    }

    private void showController() {
        if (!isControllerShowing()) {
            rlControl.setVisibility(View.VISIBLE);
            setSizeMoveView(false);
        }
    }

    private void hideController() {
        if (isControllerShowing()) {
            rlControl.setVisibility(View.GONE);
            setSizeMoveView(false);
        }
    }

    private void toggleController() {
        if (isControllerShowing())
            hideController();
        else
            showController();
    }

    private int getMoveViewWidth() {
        return (moveView == null) ? 0 : moveView.getLayoutParams().width;
    }

    private int getMoveViewHeight() {
        return (moveView == null) ? 0 : moveView.getLayoutParams().height;
    }

    private int getVideoW() {
        return (uzFLVideo == null) ? 0 : uzFLVideo.getVideoWidth();
    }

    private int getVideoH() {
        return (uzFLVideo == null) ? 0 : uzFLVideo.getVideoHeight();
    }

    //==================================================================================================END CONFIG
    //==================================================================================================START UI
    private void updateUIVideoSizeOneTime(int videoW, int videoH) {
        int vW;
        int vH;
        if (isAutoSize) {
            vW = screenWidth / 2;
            vH = vW * videoH / (videoW == 0 ? 1 : videoW);
        } else {
            vW = videoWidthFromSettingConfig;
            vH = videoHeightFromSettingConfig;
        }
        int firstPositionX = LocalData.getMiniPlayerFirstPositionX();
        int firstPositionY = LocalData.getMiniPlayerFirstPositionY();
        if (firstPositionX == -1 || firstPositionY == -1) {
            firstPositionX = screenWidth - vW;
            firstPositionY = screenHeight - vH - pipTopPosition;
        }
        slideToPosition(firstPositionX, firstPositionY);
    }

    private void slideToPosition(int goToPosX, int goToPosY) {
        final int currentPosX = params.x;
        final int currentPosY = params.y;
        final int mGoToPosX;
        final int mGoToPosY;
        int videoW = getVideoW();
        int videoH = getVideoH();
        if (goToPosX <= 0)
            mGoToPosX = marginL;
        else if (goToPosX >= screenWidth - videoW)
            mGoToPosX = goToPosX - marginR;
        else
            mGoToPosX = goToPosX;
        if (goToPosY <= 0)
            mGoToPosY = marginT;
        else if (goToPosY >= screenHeight - videoH)
            mGoToPosY = goToPosY - marginB;
        else
            mGoToPosY = goToPosY;
        final int a = Math.abs(mGoToPosX - currentPosX);
        final int b = Math.abs(mGoToPosY - currentPosY);
        countDownTimer = new CountDownTimer(300, 3) {
            public void onTick(long t) {
                float step = (300.f - t) / 3;
                int tmpX;
                int tmpY;
                if (currentPosX > mGoToPosX) {
                    if (currentPosY > mGoToPosY) {
                        tmpX = currentPosX - (int) (a * step / 100);
                        tmpY = currentPosY - (int) (b * step / 100);
                    } else {
                        tmpX = currentPosX - (int) (a * step / 100);
                        tmpY = currentPosY + (int) (b * step / 100);
                    }
                } else {
                    if (currentPosY > mGoToPosY) {
                        tmpX = currentPosX + (int) (a * step / 100);
                        tmpY = currentPosY - (int) (b * step / 100);
                    } else {
                        tmpX = currentPosX + (int) (a * step / 100);
                        tmpY = currentPosY + (int) (b * step / 100);
                    }
                }
                updateUISlide(tmpX, tmpY);
            }

            public void onFinish() {
                updateUISlide(mGoToPosX, mGoToPosY);
            }
        }.start();
    }

    private void updateUISlide(int x, int y) {
        params.x = x;
        params.y = y;
        mWindowManager.updateViewLayout(mFloatingView, params);
    }

    //==================================================================================================END UI

    @SuppressLint("ClickableViewAccessibility")
    private void dragAndMove() {
        mTapDetector = new GestureDetector(getBaseContext(), new GestureTap());
        moveView.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mTapDetector.onTouchEvent(event);
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        //remember the initial position.
                        initialX = params.x;
                        initialY = params.y;
                        //get the touch location
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_UP:
                        onMoveUp();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        //Calculate the X and Y coordinates of the view.
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        //Update the layout with new X & Y coordinate
                        mWindowManager.updateViewLayout(mFloatingView, params);
                        getLocationOnScreen(mFloatingView);
                        return true;
                }
                return false;
            }
        });
    }

    private void notifyPos(POS tmpPos) {
        if (pos != tmpPos) {
            pos = tmpPos;
            if (isEZDestroy) {
                switch (pos) {
                    case TOP_LEFT:
                    case TOP_RIGHT:
                    case BOTTOM_LEFT:
                    case BOTTOM_RIGHT:
                    case CENTER_LEFT:
                    case CENTER_RIGHT:
                    case CENTER_TOP:
                    case CENTER_BOTTOM:
                        if (isEnableVibration)
                            UZAppUtils.vibrate(getBaseContext());
                        viewDestroy.setVisibility(View.VISIBLE);
                        break;
                    default:
                        if (viewDestroy.getVisibility() != View.GONE)
                            viewDestroy.setVisibility(View.GONE);
                        break;
                }
            } else {
                switch (pos) {
                    case TOP_LEFT:
                    case TOP_RIGHT:
                    case BOTTOM_LEFT:
                    case BOTTOM_RIGHT:
                        if (isEnableVibration)
                            UZAppUtils.vibrate(getBaseContext());
                        viewDestroy.setVisibility(View.VISIBLE);
                        break;
                    default:
                        if (viewDestroy.getVisibility() != View.GONE)
                            viewDestroy.setVisibility(View.GONE);
                        break;
                }
            }
        }
    }

    private void getLocationOnScreen(View view) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int posLeft = location[0];
        int posTop = location[1];
        int posRight = posLeft + view.getWidth();
        int posBottom = posTop + view.getHeight();
        int centerX = (posLeft + posRight) / 2;
        int centerY = (posTop + posBottom) / 2;
        if (centerX < 0)
            notifyPos(centerY < 0 ? POS.TOP_LEFT : centerY > screenHeight ? POS.BOTTOM_LEFT : POS.CENTER_LEFT);
        else if (centerX > screenWidth)
            notifyPos(centerY < 0 ? POS.TOP_RIGHT : centerY > screenHeight ? POS.BOTTOM_RIGHT : POS.CENTER_RIGHT);
        else {
            if (centerY < 0) {
                notifyPos(POS.CENTER_TOP);
            } else if (centerY > screenHeight) {
                notifyPos(POS.CENTER_BOTTOM);
            } else {
                if (posLeft < 0) {
                    notifyPos(POS.LEFT);
                } else if (posRight > screenWidth) {
                    notifyPos(POS.RIGHT);
                } else {
                    notifyPos(posTop < 0 ? POS.TOP : posBottom > screenHeight ? POS.BOTTOM : POS.CENTER);
                }
            }
        }
    }

    private void onMoveUp() {
        if (pos == null) return;
        int posX;
        int posY;
        int centerPosX;
        int centerPosY;
        switch (pos) {
            case TOP_LEFT:
            case TOP_RIGHT:
            case BOTTOM_LEFT:
            case BOTTOM_RIGHT:
                stopSelf();
                break;
            case CENTER:
                posX = params.x;
                posY = params.y;
                centerPosX = posX + getMoveViewWidth() / 2;
                centerPosY = posY + getMoveViewHeight() / 2;
                slideToPosition((centerPosX < screenWidth / 2) ? 0 : (screenWidth - getMoveViewWidth()),
                        (centerPosY < screenHeight / 2) ? 0 : (screenHeight - getMoveViewHeight() - pipTopPosition));
                break;
            case TOP:
            case CENTER_TOP:
                if (isEZDestroy && pos == POS.CENTER_TOP)
                    stopSelf();
                else
                    slideToTop();
                break;
            case BOTTOM:
            case CENTER_BOTTOM:
                if (isEZDestroy && pos == POS.CENTER_BOTTOM)
                    stopSelf();
                else
                    slideToBottom();
                break;
            case LEFT:
            case CENTER_LEFT:
                if (isEZDestroy && pos == POS.CENTER_LEFT)
                    stopSelf();
                else
                    slideToLeft();
                break;
            case RIGHT:
            case CENTER_RIGHT:
                if (isEZDestroy && pos == POS.CENTER_RIGHT)
                    stopSelf();
                else
                    slideToRight();
                break;
        }
    }

    private void slideToTop() {
        int posX = params.x;
        int centerPosX = posX + getMoveViewWidth() / 2;
        slideToPosition((centerPosX < screenWidth / 2) ? 0 : (screenWidth - getMoveViewWidth()), 0);
    }

    private void slideToBottom() {
        int posX = params.x;
        int centerPosX = posX + getMoveViewWidth() / 2;
        slideToPosition((centerPosX < screenWidth / 2) ? 0 : (screenWidth - getMoveViewWidth()),
                screenHeight - getMoveViewHeight() - pipTopPosition);
    }

    private void slideToLeft() {
        int posY = params.y;
        int centerPosY = posY + getMoveViewHeight() / 2;
        slideToPosition(0, (centerPosY < screenHeight / 2) ? 0 : (screenHeight - getMoveViewHeight() - pipTopPosition));
    }

    private void slideToRight() {
        int posY = params.y;
        int centerPosY = posY + getMoveViewHeight() / 2;
        slideToPosition(screenWidth - getMoveViewWidth(),
                (centerPosY < screenHeight / 2) ? 0 : (screenHeight - getMoveViewHeight() - pipTopPosition));
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        if (mFloatingView != null)
            mWindowManager.removeView(mFloatingView);
        if (uzFLVideo != null)
            uzFLVideo.onDestroy();
        if (countDownTimer != null)
            countDownTimer.cancel();
        LocalData.setClickedPip(false);
        super.onDestroy();
    }

    @Override
    public void isInitResult(boolean isInitSuccess) {
        if (isInitSuccess && uzFLVideo != null) {
            if (mFloatingView == null) return;
            Timber.d("miniplayer STEP 2 isInitResult true");
            editSizeOfMoveView();
            //sau khi da play thanh cong thi chuyen mini player ben ngoai screen vao trong screen
            updateUIVideoSizeOneTime(uzFLVideo.getVideoWidth(), uzFLVideo.getVideoHeight());
            if (!isSendMsgToActivity) {
                //LLog.d(TAG, "state finish loading PIP -> send msg to UZVideo");
                CommunicateMng.MsgFromServiceIsInitSuccess msgFromServiceIsInitSuccess = new CommunicateMng.MsgFromServiceIsInitSuccess(null);
                msgFromServiceIsInitSuccess.setInitSuccess(true);
                CommunicateMng.postFromService(msgFromServiceIsInitSuccess);
                isSendMsgToActivity = true;
            }
        }
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        switch (playbackState) {
            case Player.STATE_BUFFERING:
            case Player.STATE_IDLE:
            case Player.STATE_READY:
                break;
            case Player.STATE_ENDED:
                onStateEnded();
                break;
        }
    }

    private void onStateEnded() {
        if (UZData.getInstance().isPlayWithPlaylistFolder()) {
            if (uzFLVideo == null) return;
            Timber.d("Dang play o che do playlist/folder -> play next item");
            uzFLVideo.getLinkPlayOfNextItem(playback -> {
                if (playback == null || !playback.canPlay()) {
                    stopSelf();
                    return;
                }
                linkPlay = playback.getLinkPlay();
                contentPosition = 0;
                setupVideo();
            });
        } else {
            Timber.d("Dang play o che do entity -> stopSelf()");
            stopSelf();
        }
    }

    @Override
    public void onVideoSizeChanged(int width, int height) {
    }

    @Override
    public void onPlayerError(UZException error) {
        Timber.e("onPlayerError: %s", error.getMessage());
        stopSelf();
    }

    private void setupVideo() {
        if (TextUtils.isEmpty(linkPlay)) {
            Timber.e("setupVideo linkPlay == null || linkPlay.isEmpty() -> stopSelf");
            stopSelf();
            return;
        }
        Timber.d("setupVideo linkPlay: %s, isLivestream: %s", linkPlay, isLiveStream);
        if (ConnectivityUtils.isConnected(this)) {
            uzFLVideo.init(linkPlay, isLiveStream, contentPosition, progressBarColor, this);
            tvMsg.setVisibility(View.GONE);
        } else
            tvMsg.setVisibility(View.VISIBLE);
    }

    //click vo se larger, click lan nua de smaller
    private void setSizeMoveView(boolean isFirstSizeInit) {
        if (moveView == null) return;
        int w = 0;
        int h = 0;
        if (isFirstSizeInit) {
            w = screenWidth / 2;
            if (videoW == 0)
                h = w * 9 / 16;
            else
                h = w * videoH / videoW;

        }
        if (w != 0 && h != 0) {
            moveView.getLayoutParams().width = w;
            moveView.getLayoutParams().height = h;
            moveView.requestLayout();
        }
    }

    private void editSizeOfMoveView() {
        if (uzFLVideo == null || moveView == null || videoW == 0) return;
        int videoW = uzFLVideo.getVideoWidth();
        int videoH = uzFLVideo.getVideoHeight();
        int moveW;
        int moveH;
        if (isAutoSize) {
            moveW = getMoveViewWidth();
            moveH = moveW * videoH / (videoW == 0 ? 1 : videoW);
        } else {
            moveW = videoWidthFromSettingConfig;
            moveH = videoHeightFromSettingConfig;
        }
        TmpParamData.getInstance().setPlayerWidth(moveW);
        TmpParamData.getInstance().setPlayerHeight(moveH);
        moveView.getLayoutParams().width = moveW;
        moveView.getLayoutParams().height = moveH;
        moveView.requestLayout();
    }

    //listen msg from UZVideo
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(CommunicateMng.MsgFromActivity msgFromActivity) {
        if (msgFromActivity == null || uzFLVideo == null)
            return;
        if (msgFromActivity instanceof CommunicateMng.MsgFromActivityPosition) {
            //LLog.d(TAG, "Nhan duoc content position moi cua UZVideo va tien hanh seek toi day");
            if (isEnableSmoothSwitch) {
                //smooth but content position is delay
                Timber.d("miniplayer STEP 4 MsgFromActivityPosition -> isEnableSmoothSwitch true -> do nothing");
            } else {
                long contentPosition = ((CommunicateMng.MsgFromActivityPosition) msgFromActivity).getPosition();
                long contentBufferedPosition = uzFLVideo.getContentBufferedPosition();
                Timber.d("miniplayer STEP 4 MsgFromActivityPosition -> isEnableSmoothSwitch false -> contentBufferedPosition: %d, position: %d", contentBufferedPosition, contentPosition);
                uzFLVideo.seekTo(Math.min(contentPosition, contentBufferedPosition));
            }
        } else if (msgFromActivity instanceof CommunicateMng.MsgFromActivityIsInitSuccess) {
            Timber.d("miniplayer STEP 7 MsgFromActivityIsInitSuccess isInitSuccess: %b", ((CommunicateMng.MsgFromActivityIsInitSuccess) msgFromActivity).isInitSuccess());
            stopSelf();
        }
        if (msgFromActivity.getMsg() == null) return;
        handleMsgFromActivity(msgFromActivity);
    }

    private void handleMsgFromActivity(CommunicateMng.MsgFromActivity msgFromActivity) {
        String msg = msgFromActivity.getMsg();
        switch (msg) {
            case CommunicateMng.SHOW_MINI_PLAYER_CONTROLLER:
                showController();
                break;
            case CommunicateMng.HIDE_MINI_PLAYER_CONTROLLER:
                hideController();
                break;
            case CommunicateMng.TOGGLE_MINI_PLAYER_CONTROLLER:
                toggleController();
                break;
            case CommunicateMng.PAUSE_MINI_PLAYER:
                pauseVideo();
                break;
            case CommunicateMng.RESUME_MINI_PLAYER:
                resumeVideo();
                break;
            case CommunicateMng.TOGGLE_RESUME_PAUSE_MINI_PLAYER:
                toggleResumePause();
                break;
            case CommunicateMng.OPEN_APP_FROM_MINI_PLAYER:
                openApp();
                break;
            case CommunicateMng.DISAPPEAR:
                disappear();
                break;
            case CommunicateMng.APPEAR:
                appear();
                break;
        }
    }

    private enum POS {
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT,
        CENTER_LEFT,
        CENTER_RIGHT,
        CENTER_TOP,
        CENTER_BOTTOM,
        LEFT,
        RIGHT,
        TOP,
        BOTTOM,
        CENTER
    }

    private class GestureTap extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            boolean isTapToFullPlayer = LocalData.getMiniPlayerTapToFullPlayer();
            if (isTapToFullPlayer) {
                setSizeMoveView(false);//remove this line make animation switch from mini-player to full-player incorrectly
                openApp();
            } else
                toggleController();
            return true;
        }
    }
}