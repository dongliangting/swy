package com.iflytek.aiui.demo.chat.ui.chat;

import android.Manifest;
import android.app.Activity;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.databinding.BindingMethod;
import android.databinding.BindingMethods;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.SwipeDismissBehavior;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.aip.ImageFrame;
import com.baidu.aip.api.FaceApi;
import com.baidu.aip.db.DBManager;
import com.baidu.aip.entity.Feature;
import com.baidu.aip.entity.IdentifyRet;
import com.baidu.aip.entity.User;
import com.baidu.aip.face.CameraImageSource;
import com.baidu.aip.face.FaceDetectManager;
import com.baidu.aip.face.PreviewView;
import com.baidu.aip.face.camera.CameraView;
import com.baidu.aip.face.camera.ICameraControl;
import com.baidu.aip.manager.FaceEnvironment;
import com.baidu.aip.manager.FaceLiveness;
import com.baidu.aip.manager.FaceSDKManager;
import com.baidu.aip.ofr.LivenessSettingActivity;
import com.baidu.aip.ofr.utils.GlobalFaceTypeModel;
import com.baidu.aip.utils.FileUitls;
import com.baidu.aip.utils.PreferencesUtil;
import com.baidu.idl.facesdk.FaceInfo;
import com.baidu.idl.facesdk.FaceTracker;
import com.iflytek.aiui.demo.chat.ChatActivity;
import com.iflytek.aiui.demo.chat.R;
import com.iflytek.aiui.demo.chat.common.Constant;
import com.iflytek.aiui.demo.chat.databinding.ChatFragmentBinding;
import com.iflytek.aiui.demo.chat.model.ChatMessage;
import com.iflytek.aiui.demo.chat.model.MessageEvent;
import com.iflytek.aiui.demo.chat.msgrec.UdpListener;
import com.iflytek.aiui.demo.chat.repository.chat.RawMessage;
import com.iflytek.aiui.demo.chat.repository.translation.DestLanguage;
import com.iflytek.aiui.demo.chat.repository.translation.SrcLanguage;
import com.iflytek.aiui.demo.chat.ui.chat.adapter.MessageListAdapter;
import com.iflytek.aiui.demo.chat.ui.common.PermissionChecker;
import com.iflytek.aiui.demo.chat.ui.common.ScrollSpeedLinearLayoutManger;
import com.iflytek.aiui.demo.chat.ui.common.widget.PopupWindowFactory;
import com.iflytek.aiui.demo.chat.ui.zxing.BannerActivity;
import com.iflytek.aiui.demo.chat.ui.zxing.DefaultCaptureActivity;
import com.iflytek.aiui.demo.chat.ui.zxing.TestScanActivity;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechEvent;
import com.iflytek.cloud.SynthesizerListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.CompositePermissionListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;

/**
 * 聊天主界面Fragment
 */

// 设置支持矢量图
@BindingMethods({
        @BindingMethod(type = android.widget.ImageView.class,
                attribute = "srcCompat",
                method = "setImageDrawable")})
public class ChatFragment extends Fragment implements PermissionChecker {
    public static final Pattern emptyPattern = Pattern.compile("^\\s+$", Pattern.DOTALL);

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    private VoiceViewModel mVoiceViewModel;
    protected ChatViewModel mMessageModel;
    private PlayerViewModel mPlayerViewModel;
    private TranslationViewModel mTransCfgViewModel;
    //当前所有交互消息列表
    protected List<ChatMessage> mInteractMessages;

    private MessageListAdapter mMsgAdapter;
    protected ChatFragmentBinding mChatBinding;
    //按住录音动画控制类
    private PopupWindowFactory mVoicePop;
    private ImageView VolumeView;
    //当前状态，取值参考Constant中STATE定义
    private int mState;

    //唤醒波浪动画
    private boolean mWaveAnim = false;


    //记录Toast方便清空
    private List<Toast> mTipsToast = new ArrayList<>();
    private PreviewView previewView;
    private ImageView matchAvatorIv;
    private TextView matchUserTv;
    private TextView scoreTv;
    private FaceDetectManager faceDetectManager;
    private CameraImageSource cameraImageSource;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        AndroidSupportInjection.inject(this);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                faceDetectManager.start();
                isFaceStart = true;
                faceDetectManager.setUseDetect(true);
            }
        }, 2000);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mChatBinding = DataBindingUtil.inflate(inflater, R.layout.chat_fragment,
                container, false);

        findView(mChatBinding.getRoot());
        init();
        addListener();
        DBManager.getInstance().init(getActivity());
        loadFeature2Memery();
        try {
            receiveBroadCast();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mChatBinding.getRoot();

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setInputState(Constant.STATE_VOICE);

        Dexter.withActivity(getActivity())
                .withPermissions(
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        mVoiceViewModel = ViewModelProviders.of(ChatFragment.this, mViewModelFactory).get(VoiceViewModel.class);
                        mMessageModel = ViewModelProviders.of(getActivity(), mViewModelFactory).get(ChatViewModel.class);
                        mPlayerViewModel = ViewModelProviders.of(ChatFragment.this, mViewModelFactory).get(PlayerViewModel.class);
                        mTransCfgViewModel = ViewModelProviders.of(ChatFragment.this, mViewModelFactory).get(TranslationViewModel.class);
                        if (!report.areAllPermissionsGranted()) {
                            mMessageModel.fakeAIUIResult(0, "permission", "请重启应用允许请求的权限");
                        }
                        //所有权限通过，初始化界面
                        onPermissionChecked();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    @CallSuper
    protected void onPermissionChecked() {
        initChatView();
        initPlayControl();
        initTextAction();
        initVoiceAction();
        initTransView();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void invalidLogin(MessageEvent event) {
        //todo
        List<ChatMessage> interactMessages = new ArrayList<>();
        RawMessage rawMessage = new RawMessage(RawMessage.FromType.AIUI, RawMessage.MsgType.TEXT,
                event.message.getBytes(), event.question, 0);
        RawMessage rawMessage2 = new RawMessage(RawMessage.FromType.USER, RawMessage.MsgType.TEXT,
                event.message.getBytes(), event.answer, 0);
        interactMessages.add(new ChatMessage(rawMessage, ChatFragment.this, mMessageModel, mPlayerViewModel));
        interactMessages.add(new ChatMessage(rawMessage2, ChatFragment.this, mMessageModel, mPlayerViewModel));

        mMsgAdapter.replace(interactMessages);
        mChatBinding.executePendingBindings();

        if (event.question.contains("产品条码")) {
            getActivity().startActivity(new Intent(getActivity(), DefaultCaptureActivity.class));
        }
        ivLine.setVisibility(View.VISIBLE);
    }

    private void initChatView() {
        //初始化交互消息展示列表
        ScrollSpeedLinearLayoutManger layout = new ScrollSpeedLinearLayoutManger(getActivity());
        layout.setSpeedSlow();
        layout.setStackFromEnd(true);
        mChatBinding.chatList.setLayoutManager(layout);

        mMsgAdapter = new MessageListAdapter(this);
        mChatBinding.chatList.setAdapter(mMsgAdapter);

        mChatBinding.chatList.setClipChildren(true);
        mChatBinding.chatList.setVerticalScrollBarEnabled(true);
        mChatBinding.chatList.getItemAnimator().setChangeDuration(0);

        mMsgAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                mChatBinding.chatList.smoothScrollToPosition(positionStart);
            }
        });

        //获取交互消息，更新展示
//        Transformations.map(mMessageModel.getInteractMessages(), input -> {
//            List<ChatMessage> interactMessages = new ArrayList<>();
//            for (RawMessage message : input) {
//                interactMessages.add(new ChatMessage(message, ChatFragment.this, mMessageModel, mPlayerViewModel));
//            }
//            return interactMessages;
//        }).observe(this, messages -> {
//            mInteractMessages = messages;
//            mMsgAdapter.replace(messages);
//            mChatBinding.executePendingBindings();
//        });
    }

    private void initTextAction() {
        //文本语义按钮监听
        mChatBinding.emotionSend.setOnClickListener(view -> doSend());
        mChatBinding.emotionSend.setOnKeyListener((view, keyCode, keyEvent) -> {
            if (keyEvent.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                doSend();
                return true;
            }
            return false;
        });
    }


    private void initVoiceAction() {
        //设置中唤醒模式开启关闭
        mVoiceViewModel.isWakeUpEnable().observe(this, enable -> {
            if (enable) {
                //设置中启用唤醒，进入待唤醒模式
                onWaitingWakeUp();
            } else {
                //唤醒关闭，进入按住说话的交互模式
                setInputState(Constant.STATE_VOICE);
            }
        });

        mVoiceViewModel.wakeUp().observe(this, wakeUpOrSleep -> {
            if (wakeUpOrSleep) {
                onWakeUp();
                startSeyContent();
            } else {
                onWaitingWakeUp();
            }
        });


        //音量变化
        mVoiceViewModel.volume().observe(this, volume -> {
            //更新居中的音量信息
            if (VolumeView != null && VolumeView.getDrawable().setLevel(volume)) {
                VolumeView.getDrawable().invalidateSelf();
            }

            //唤醒状态下更新底部的音量波浪动画
            if (mState == Constant.STATE_WAKEUP) {
                mChatBinding.visualizer.setVolume(volume);
            }
        });

        mVoiceViewModel.isActiveInteract().observe(this, active -> {
            if (!active) {
                showTips("您好像并没有开始说话");
            }
        });

        //根据左下角图标切换输入状态
        mChatBinding.emotionVoice.setOnClickListener(view -> {
            setInputState(mState == Constant.STATE_VOICE ?
                    Constant.STATE_TEXT : Constant.STATE_VOICE);
            dismissKeyboard(view.getWindowToken());
        });

        //初始化居中显示的按住说话动画
        View view = View.inflate(getActivity(), R.layout.layout_microphone, null);
        VolumeView = view.findViewById(R.id.iv_recording_icon);
        mVoicePop = new PopupWindowFactory(getActivity(), view);

        //按住说话按钮
        mChatBinding.voiceText.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mChatBinding.voiceText.setPressed(true);
                    if (mChatBinding.voiceText.isPressed()) {
                        mVoicePop.showAtLocation(v, Gravity.CENTER, 0, 0);
                        setInputState(Constant.STATE_VOICE_INPUTTING);
                        mVoiceViewModel.startSpeak();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    mVoicePop.dismiss();
                    mChatBinding.voiceText.setPressed(false);
                    setInputState(Constant.STATE_VOICE);
                    mVoiceViewModel.endSpeak();
                    break;
            }
            return true;
        });

        mVoicePop.getPopupWindow().setOnDismissListener(() -> {
            mChatBinding.voiceText.setPressed(false);
            mVoiceViewModel.endSpeak();
        });
    }

    //add by lds
    public static final int UDP_PORT = 8084;
    private UdpListener udpListener;

    /**
     * @return
     */
    private void receiveBroadCast() throws IOException {

        try {
            udpListener = new UdpListener(UDP_PORT);
            new Thread(udpListener).start();

            udpListener.setReceiveListener(new UdpListener.ReceiveListener() {
                @Override
                public void onReceiveListener(String content) {
                    //收到消息
                    Log.e("lt---", "content ===" + content);
                    mPlayerViewModel.startTTS(content, null, false);
                }
            });

        } catch (SocketException e) {
            e.printStackTrace();
        }

    }


    private void setInputState(int state) {
        mState = state;
        mChatBinding.setState(state);
        mChatBinding.executePendingBindings();
    }

    private void initTransView() {
        mTransCfgViewModel.isTranslationEnable().observe(this,
                enable -> mChatBinding.transContainer.setVisibility(enable ? View.VISIBLE : View.GONE));

        mTransCfgViewModel.getTranslationMode().observe(this, transParams -> {
            mChatBinding.srcLanguegeSpinner.setSelectedIndex(Arrays.asList(SrcLanguage.values()).indexOf(transParams.getSrcLanguage()));
            mChatBinding.dstLanguegeSpinner.setSelectedIndex(Arrays.asList(DestLanguage.values()).indexOf(transParams.getDestLanguage()));
        });

        mChatBinding.srcLanguegeSpinner.setItems(SrcLanguage.values());
        mChatBinding.srcLanguegeSpinner.setOnItemSelectedListener((view, position, id, item) -> mTransCfgViewModel.setSrcLanguage((SrcLanguage) item));

        mChatBinding.dstLanguegeSpinner.setItems(DestLanguage.values());
        mChatBinding.dstLanguegeSpinner.setOnItemSelectedListener((view, position, id, item) -> mTransCfgViewModel.setDestLanguage((DestLanguage) item));
    }

    /**
     * 播放界面初始化
     */
    private void initPlayControl() {
        //播放器控制
        mChatBinding.playControlBar.controlSongName.setSelected(true);
        mChatBinding.setPlayer(mPlayerViewModel);

        mPlayerViewModel.getPlayerTips().observe(this, s -> {
            showTips(s);
        });
        //监听播放器状态，更新控制界面
        mPlayerViewModel.getPlayState().observe(this, playState -> {
            mChatBinding.setPlayState(playState);
            final LinearLayout playControl = mChatBinding.playControlBar.playControl;
            final CoordinatorLayout controlContainer = mChatBinding.playControlBar.controlContainer;

            if (mPlayerViewModel.isActive() && playControl.getVisibility() == View.GONE) {
                playControl.setVisibility(View.VISIBLE);
                controlContainer.setVisibility(View.VISIBLE);

                //滑动停止当前播放并隐藏播放控制条
                SwipeDismissBehavior<View> swipe = new SwipeDismissBehavior();
                swipe.setSwipeDirection(SwipeDismissBehavior.SWIPE_DIRECTION_START_TO_END);
                swipe.setListener(new SwipeDismissBehavior.OnDismissListener() {
                    @Override
                    public void onDismiss(View view) {
                        controlContainer.setVisibility(View.GONE);
                        playControl.setVisibility(View.GONE);
                        mPlayerViewModel.stop();
                    }

                    @Override
                    public void onDragStateChanged(int state) {
                    }
                });

                //将隐藏的播放控制条恢复
                CoordinatorLayout.LayoutParams coordinatorParams =
                        (CoordinatorLayout.LayoutParams) playControl.getLayoutParams();
                coordinatorParams.setBehavior(swipe);

                AlphaAnimation appearAnimation = new AlphaAnimation(0, 1);
                appearAnimation.setDuration(500);
                playControl.startAnimation(appearAnimation);

                CoordinatorLayout.LayoutParams tParams = (CoordinatorLayout.LayoutParams) playControl.getLayoutParams();
                tParams.setMargins(0, 0, 0, 0);
                playControl.requestLayout();
                playControl.setAlpha(1.0f);
            }
        });
    }

    private void doSend() {
        //语音模式按发送按钮进入文本语义模式
        if (mState == Constant.STATE_VOICE) {
            setInputState(Constant.STATE_TEXT);
            return;
        }

        //文本语义
        String msg = mChatBinding.editText.getText().toString();
        if (!TextUtils.isEmpty(msg) && !emptyPattern.matcher(msg).matches()) {
            clearTips();
            mVoiceViewModel.sendText(msg);
            mChatBinding.editText.setText("");
        } else {
            showTips("发送内容不能为空");
        }
    }


    private String speakContent = "在呢,你好";

    private void startSeyContent() {
        //唤醒回答

        mPlayerViewModel.startTTS(speakContent, null, false);

        speakContent = "在呢,你好";
    }


    private void showTips(String toast) {
        clearTips();

        Toast tips = Toast.makeText(getContext(), toast, Toast.LENGTH_SHORT);
        tips.show();
        mTipsToast.add(tips);
    }


    private void clearTips() {
        for (Toast item : mTipsToast) {
            item.cancel();
        }

        mTipsToast.clear();
    }

    private void onWakeUp() {
        setInputState(Constant.STATE_WAKEUP);
        if (!mWaveAnim) {
            //底部音量动画
            mChatBinding.visualizer.startAnim();
            mWaveAnim = true;
        }
    }

    private void onWaitingWakeUp() {
        //进入待唤醒状态
        setInputState(Constant.STATE_WAITING_WAKEUP);
        mChatBinding.visualizer.stopAnim();
        mWaveAnim = false;
    }


    private void dismissKeyboard(IBinder windowToken) {
        Activity activity = getActivity();
        if (activity != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(windowToken, 0);
        }
    }

    /**
     * 切换至语义结果详情页
     *
     * @param content
     */
    public void switchToDetail(String content) {
        ((ChatActivity) getActivity()).switchToDetail(content);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mVoiceViewModel != null) {
            mVoiceViewModel.onChatResume();
        }
        mChatBinding.visualizer.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }


        Log.e("lt------","00000000000000");
        // 结束检测。
        faceDetectManager.stop();
        faceDetectManager.start();
        isFaceStart = true;
        faceDetectManager.setUseDetect(true);



    }

    @Override
    public void onPause() {
        super.onPause();
        if (mVoiceViewModel != null) {
            mVoiceViewModel.onChatPause();
        }

        Log.e("lt------","111111111111");
        mChatBinding.visualizer.onPause();
        // 结束检测。
        faceDetectManager.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mChatBinding.visualizer.release();
        faceDetectManager.stop();

        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }


    @Override
    public void checkPermission(String permission, final Runnable success, final Runnable failed) {
        Dexter.withActivity(getActivity()).withPermission(permission)
                .withListener(new CompositePermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        super.onPermissionGranted(response);
                        if (success != null) {
                            success.run();
                        }
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        super.onPermissionDenied(response);
                        if (failed != null) {
                            failed.run();
                        }
                    }
                }).check();
    }


//    ******************************  人脸 ***************

    private static final int FEATURE_DATAS_UNREADY = 1;
    private static final int IDENTITY_IDLE = 2;
    private static final int IDENTITYING = 3;
    private LinearLayout llBanner;
    private ImageView ivLine;
    public ImageView ivSettingBtn;

    private void findView(View view) {
        previewView = (PreviewView) view.findViewById(R.id.preview_view);
        matchAvatorIv = (ImageView) view.findViewById(R.id.match_avator_iv);
        matchUserTv = (TextView) view.findViewById(R.id.match_user_tv);
        scoreTv = (TextView) view.findViewById(R.id.score_tv);
        llBanner = view.findViewById(R.id.ll_banner_btn);
        ivLine = view.findViewById(R.id.iv_line);
        ivSettingBtn = view.findViewById(R.id.iv_setting_btn);
        llBanner = view.findViewById(R.id.ll_banner_btn);
        llBanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), BannerActivity.class));
            }
        });
        view.findViewById(R.id.tv_banner_btn2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), BannerActivity.class));
            }
        });
        view.findViewById(R.id.tv_scan_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), TestScanActivity.class));
            }
        });

        if (viewFinish != null) {
            viewFinish.onViewFinish();
        }

    }

    private ViewFinish viewFinish;

    public void setViewFinishListener(ViewFinish finishListener) {
        this.viewFinish = finishListener;
    }

    public interface ViewFinish {
         void onViewFinish();
    }


    private String groupId = "nxpt";

    private void init() {
//        Intent intent = getIntent();
//        if (intent != null) {
//            groupId = intent.getStringExtra("group_id");
//        }

        faceDetectManager = new FaceDetectManager(getActivity());
        // 从系统相机获取图片帧。
        final CameraImageSource cameraImageSource = new CameraImageSource(getActivity());
        // 图片越小检测速度越快，闸机场景640 * 480 可以满足需求。实际预览值可能和该值不同。和相机所支持的预览尺寸有关。
        // 可以通过 camera.getParameters().getSupportedPreviewSizes()查看支持列表。
        cameraImageSource.getCameraControl().setPreferredPreviewSize(1280, 720);
        // cameraImageSource.getCameraControl().setPreferredPreviewSize(640, 480);

        // 设置最小人脸，该值越小，检测距离越远，该值越大，检测性能越好。范围为80-200
        FaceSDKManager.getInstance().getFaceDetector().setMinFaceSize(100);
        // FaceSDKManager.getInstance().getFaceDetector().setNumberOfThreads(4);
        // 设置预览
        cameraImageSource.setPreviewView(previewView);
        // 设置图片源
        faceDetectManager.setImageSource(cameraImageSource);
        // 设置人脸过滤角度，角度越小，人脸越正，比对时分数越高
        faceDetectManager.getFaceFilter().setAngle(20);

//        textureView.setOpaque(false);
//        // 不需要屏幕自动变黑。
//        textureView.setKeepScreenOn(true);

        boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        if (isPortrait) {
            previewView.setScaleType(PreviewView.ScaleType.FIT_WIDTH);
            // 相机坚屏模式
            cameraImageSource.getCameraControl().setDisplayOrientation(CameraView.ORIENTATION_PORTRAIT);
        } else {
            previewView.setScaleType(PreviewView.ScaleType.FIT_HEIGHT);
            // 相机横屏模式
            cameraImageSource.getCameraControl().setDisplayOrientation(CameraView.ORIENTATION_HORIZONTAL);
        }

        setCameraType(cameraImageSource);
    }

    private void setCameraType(CameraImageSource cameraImageSource) {
        this.cameraImageSource = cameraImageSource;
        // TODO 选择使用前置摄像头
//        cameraImageSource.getCameraControl().setCameraFacing(ICameraControl.CAMERA_FACING_FRONT);

        // TODO 选择使用usb摄像头
        cameraImageSource.getCameraControl().setCameraFacing(ICameraControl.CAMERA_USB);
        // 如果不设置，人脸框会镜像，显示不准
        previewView.getTextureView().setScaleX(-1);

        // TODO 选择使用后置摄像头
//        cameraImageSource.getCameraControl().setCameraFacing(ICameraControl.CAMERA_FACING_BACK);
//        previewView.getTextureView().setScaleX(-1);
    }

    private Handler handler = new Handler();
    private volatile int identityStatus = FEATURE_DATAS_UNREADY;

    private void addListener() {
        // 设置回调，回调人脸检测结果。
        faceDetectManager.setOnFaceDetectListener(new FaceDetectManager.OnFaceDetectListener() {
            @Override
            public void onDetectFace(int retCode, FaceInfo[] infos, ImageFrame frame) {
                // TODO 显示检测的图片。用于调试，如果人脸sdk检测的人脸需要朝上，可以通过该图片判断
                final Bitmap bitmap =
                        Bitmap.createBitmap(frame.getArgb(), frame.getWidth(), frame.getHeight(), Bitmap.Config
                                .ARGB_8888);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
//                        testView.setImageBitmap(bitmap);
                    }
                });
                if (retCode == FaceTracker.ErrCode.OK.ordinal() && infos != null) {
                    asyncIdentity(frame, infos);
                }
//                showFrame(frame, infos);

            }
        });

    }

    private ExecutorService es = Executors.newSingleThreadExecutor();

    private void loadFeature2Memery() {
        if (identityStatus != FEATURE_DATAS_UNREADY) {
            return;
        }
        es.submit(new Runnable() {
            @Override
            public void run() {
                Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
                // android.os.Process.setThreadPriority (-4);
                FaceApi.getInstance().loadFacesFromDB(groupId);
//                toast("人脸数据加载完成，即将开始1：N");
                int count = FaceApi.getInstance().getGroup2Facesets().get(groupId).size();
//                displayTip("底库人脸个数：" + count, facesetsCountTv);
                identityStatus = IDENTITY_IDLE;
            }
        });
    }


    private void asyncIdentity(final ImageFrame imageFrame, final FaceInfo[] faceInfos) {
        if (identityStatus != IDENTITY_IDLE) {
            return;
        }

        es.submit(new Runnable() {
            @Override
            public void run() {
                if (faceInfos == null || faceInfos.length == 0) {
                    return;
                }
                int liveType = PreferencesUtil.getInt(LivenessSettingActivity.TYPE_LIVENSS, LivenessSettingActivity
                        .TYPE_NO_LIVENSS);
                if (liveType == LivenessSettingActivity.TYPE_NO_LIVENSS) {
                    identity(imageFrame, faceInfos[0]);
                } else if (liveType == LivenessSettingActivity.TYPE_RGB_LIVENSS) {

                    if (rgbLiveness(imageFrame, faceInfos[0]) > FaceEnvironment.LIVENESS_RGB_THRESHOLD) {
                        identity(imageFrame, faceInfos[0]);
                    } else {
                        // toast("rgb活体分数过低");
                    }
                }
            }
        });
    }

    private float rgbLiveness(ImageFrame imageFrame, FaceInfo faceInfo) {

        long starttime = System.currentTimeMillis();
        final float rgbScore = FaceLiveness.getInstance().rgbLiveness(imageFrame.getArgb(), imageFrame
                .getWidth(), imageFrame.getHeight(), faceInfo.landmarks);
//        final long duration = System.currentTimeMillis() - starttime;

//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                rgbLivenssDurationTv.setVisibility(View.VISIBLE);
//                rgbLivenessScoreTv.setVisibility(View.VISIBLE);
//                rgbLivenssDurationTv.setText("RGB活体耗时：" + duration);
//                rgbLivenessScoreTv.setText("RGB活体得分：" + rgbScore);
//            }
//        });

        return rgbScore;
    }


    private void identity(ImageFrame imageFrame, FaceInfo faceInfo) {


        float raw = Math.abs(faceInfo.headPose[0]);
        float patch = Math.abs(faceInfo.headPose[1]);
        float roll = Math.abs(faceInfo.headPose[2]);
        // 人脸的三个角度大于20不进行识别
        if (raw > 20 || patch > 20 || roll > 20) {
            return;
        }

        identityStatus = IDENTITYING;

        long starttime = System.currentTimeMillis();
        int[] argb = imageFrame.getArgb();
        int rows = imageFrame.getHeight();
        int cols = imageFrame.getWidth();
        int[] landmarks = faceInfo.landmarks;

        int type = PreferencesUtil.getInt(GlobalFaceTypeModel.TYPE_MODEL, GlobalFaceTypeModel.RECOGNIZE_LIVE);
        IdentifyRet identifyRet = null;
        if (type == GlobalFaceTypeModel.RECOGNIZE_LIVE) {
            identifyRet = FaceApi.getInstance().identity(argb, rows, cols, landmarks, groupId);
        } else if (type == GlobalFaceTypeModel.RECOGNIZE_ID_PHOTO) {
            identifyRet = FaceApi.getInstance().identityForIDPhoto(argb, rows, cols, landmarks, groupId);
        }

        if (identifyRet != null) {

            displayUserOfMaxScore(identifyRet.getUserId(), identifyRet.getScore());
        }
        identityStatus = IDENTITY_IDLE;
//        displayTip("特征抽取对比耗时:" + (System.currentTimeMillis() - starttime), featureDurationTv);
    }

    private TextView userOfMaxSocre;
    private String userIdOfMaxScore = "";
    private String lastUserId = "123";        //30秒之前的人
    private float maxScore = 0;
    private boolean isFaceStart = true;

    private void displayUserOfMaxScore(final String userId, final float score) {

        //*****************结果处理************************

        if (isFaceStart == false) {
            return;
        }

        handler.post(new Runnable() {
            @Override
            public void run() {

                if (score < 80) {
                    scoreTv.setText("");
                    matchUserTv.setText("");
                    matchAvatorIv.setImageBitmap(null);

                    Log.e("lt-----", "nnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn");
                    //人脸库里没有搜索到
                    lastUserId = "";
                    //唤醒回答
                    speakContent = "您好, 欢迎光临";
                    mVoiceViewModel.startWakeup();     //唤醒

                    faceDetectManager.stop();  //停止识别
                    isFaceStart = false;
                    handler.postDelayed(runnable, 30000);  //30秒后开启

                    showTips("停止识别");

                    return;
                }


                if (userIdOfMaxScore.equals(userId)) {

                    //人脸库有结果

                    if (score < maxScore) {
                        scoreTv.setText("" + score);
                    } else {
                        maxScore = score;
//                        userOfMaxSocre.setText("userId：" + userId + "\nscore：" + maxScore +"");
//                        scoreTv.setText(String.valueOf(maxScore) + "");

                        Log.e("lt----------", "userId：" + userId + "\nscore：" + maxScore + "");
                    }
                    if (matchUserTv.getText().toString().length() > 0) {
                        return;
                    }
                } else {
                    userIdOfMaxScore = userId;
                    maxScore = score;
                }


                scoreTv.setText(String.valueOf(maxScore));
                User user = FaceApi.getInstance().getUserInfo(groupId, userId);
                if (user == null) {
                    return;
                }
                matchUserTv.setText(user.getUserInfo());


                if (!TextUtils.equals(lastUserId, userId)) {
                    //和30秒之前不是同一个人

                    //唤醒回答
                    speakContent = user.getUserInfo() + "您好,欢迎光临";
                    mVoiceViewModel.startWakeup();    //唤醒
                    faceDetectManager.stop();  //停止识别
                    isFaceStart = false;
                    handler.postDelayed(runnable, 30000);  // 30秒后开启
                    showTips("停止识别");
                }
                //和30秒之前是同一个人  不做处理
                lastUserId = userId;
                List<Feature> featureList = user.getFeatureList();
                if (featureList != null && featureList.size() > 0) {
                    // featureTv.setText(new String(featureList.get(0).getFeature()));
                    File faceDir = FileUitls.getFaceDirectory();
                    if (faceDir != null && faceDir.exists()) {
                        File file = new File(faceDir, featureList.get(0).getImageName());
                        if (file != null && file.exists()) {
                            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                            matchAvatorIv.setImageBitmap(bitmap);
                        }
                    }
                }
//                List<Feature>  featureList = DBManager.getInstance().queryFeatureByUeserId(userId);
//                if (featureList != null && featureList.size() > 0) {
//                    File faceDir = FileUitls.getFaceDirectory();
//                    if (faceDir != null && faceDir.exists()) {
//                        File file = new File(faceDir, featureList.get(0).getImageName());
//                        if (file != null && file.exists()) {
//                            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
//                            testView.setImageBitmap(bitmap);
//                        }
//                    }
//                }
            }
        });
    }

    private void toast(final String text) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), text, Toast.LENGTH_LONG).show();
            }
        });
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            faceDetectManager.start();
            isFaceStart = true;

            showTips("开始识别");
        }
    };


}
