package com.iflytek.aiui.demo.chat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.baidu.aip.db.DBManager;
import com.baidu.aip.manager.FaceSDKManager;
import com.baidu.aip.ofr.UserGroupManagerActivity;
import com.baidu.aip.utils.PreferencesUtil;
import com.iflytek.aiui.demo.chat.common.Constant;
import com.iflytek.aiui.demo.chat.ui.about.AboutFragment;
import com.iflytek.aiui.demo.chat.ui.chat.ChatFragment;
import com.iflytek.aiui.demo.chat.ui.detail.DetailFragment;
import com.iflytek.aiui.demo.chat.ui.settings.SettingsFragment;
import com.iflytek.aiui.demo.chat.ui.test.HttpTestFragement;
import com.iflytek.aiui.demo.chat.ui.zxing.DefaultCaptureActivity;
import com.iflytek.aiui.demo.chat.ui.zxing.TestScanActivity;
import com.umeng.analytics.MobclickAgent;

import java.io.File;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasSupportFragmentInjector;
import gdut.bsx.share2.FileUtil;
import gdut.bsx.share2.Share2;
import gdut.bsx.share2.ShareContentType;

public class ChatActivity extends AppCompatActivity implements HasSupportFragmentInjector {
    @Inject
    DispatchingAndroidInjector<Fragment> fragmentInjector;

    private SettingsFragment mSettingsFragment;
    private ChatFragment mChatFragment;
    private AboutFragment mAboutFragment;
    private HttpTestFragement mHttpTestFragment;
    private ActionBarDrawerToggle toggle;
    private DrawerLayout drawer;
    private Toolbar toolbar;

    private boolean mIsExit;
    private boolean mIsChatFragment;

    // 矢量图兼容支持
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(ChatActivity.this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupActionBar();
        onCreateFinish();

        faceInit();    //人脸识别初始化
    }

    @Override
    public AndroidInjector<Fragment> supportFragmentInjector() {
        return fragmentInjector;
    }

    protected void onCreateFinish() {
        mChatFragment = new ChatFragment();
        mSettingsFragment = new SettingsFragment();
        mAboutFragment = new AboutFragment();
        mHttpTestFragment = new HttpTestFragement();

        //切换到ChatFragment聊天交互界面
        switchChats();

        mChatFragment.setViewFinishListener(new ChatFragment.ViewFinish() {
            @Override
            public void onViewFinish() {
                mChatFragment.ivSettingBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        drawer.openDrawer(Gravity.START);
                    }
                });
            }
        });
    }

    /**
     * 切换到设置页面
     */
    public void switchToSettings() {
        switchFragment(mSettingsFragment, true);
    }

    /**
     * 切换到聊天交互页面
     */
    public void switchChats() {
        switchFragment(mChatFragment, false);
    }

    /**
     * 切换到关于页面
     */
    public void switchToAbout() {
        switchFragment(mAboutFragment, true);
    }

    public void switchToTest() {
        switchFragment(mHttpTestFragment, true);
    }

    /**
     * 切换到语义详情页
     */
    public void switchToDetail(String content) {
        switchFragment(DetailFragment.createDetailFragment(content), true);
    }

    protected void switchFragment(Fragment fragment, boolean backStack) {
        //收回抽屉动画
        drawer.closeDrawers();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if (backStack) {
            fragmentTransaction.addToBackStack(null);
        }
        //设置fragment切换的滑动动画
        if (fragment == mChatFragment) {
            fragmentTransaction.setCustomAnimations(
                    R.anim.slide_right_in, R.anim.slide_left_out,
                    R.anim.slide_left_in, R.anim.slide_right_out);
        } else {
            fragmentTransaction.setCustomAnimations(
                    R.anim.slide_left_in, R.anim.slide_right_out,
                    R.anim.slide_right_in, R.anim.slide_left_out);
        }

        fragmentTransaction.replace(R.id.container, fragment).commitAllowingStateLoss();
    }

    private void setupActionBar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        //设置ActionBar的title，icon
        getSupportFragmentManager().registerFragmentLifecycleCallbacks(new FragmentManager.FragmentLifecycleCallbacks() {
            @Override
            public void onFragmentResumed(FragmentManager fm, Fragment f) {
                super.onFragmentResumed(fm, f);
                if (f instanceof ChatFragment) {
                    mIsChatFragment = true;
                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                    toggle.setDrawerIndicatorEnabled(true);
                    getSupportActionBar().setTitle("AIUI");
                } else if (f instanceof SettingsFragment) {
                    mIsChatFragment = false;
                    toggle.setDrawerIndicatorEnabled(false);
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    getSupportActionBar().setTitle("设置");
                } else if (f instanceof DetailFragment) {
                    mIsChatFragment = false;
                    toggle.setDrawerIndicatorEnabled(false);
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    getSupportActionBar().setTitle("详情");
                } else if (f instanceof AboutFragment) {
                    mIsChatFragment = false;
                    toggle.setDrawerIndicatorEnabled(false);
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    getSupportActionBar().setTitle("关于");
                } else if (f instanceof HttpTestFragement) {
                    mIsChatFragment = false;
                    toggle.setDrawerIndicatorEnabled(false);
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    getSupportActionBar().setTitle("测试");
                }
            }
        }, false);

        //ActionBar的返回按钮监听
        toggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSupportFragmentManager().popBackStack();
            }
        });

        //设置侧边栏按钮跳转
        NavigationView navigation = findViewById(R.id.nav_view);
        navigation.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_settings: {
                        switchToSettings();
                        break;
                    }

                    case R.id.nav_about: {
                        switchToAbout();
//                        startActivity(new Intent(ChatActivity.this, TtsDemo.class));
//                        startActivity(new Intent(ChatActivity.this, MainActivity.class));

                        break;
                    }
                    case R.id.nav_face: {
//                        switchToAbout();
//                        startActivity(new Intent(ChatActivity.this, TtsDemo.class));
                        startActivity(new Intent(ChatActivity.this, UserGroupManagerActivity.class));
                        drawer.closeDrawers();
                        break;
                    }
                    case R.id.nav_scan: {
//                        switchToAbout();
//                        startActivity(new Intent(ChatActivity.this, TtsDemo.class));
                        startActivity(new Intent(ChatActivity.this, TestScanActivity.class));
                        drawer.closeDrawers();
                        break;
                    }

                    case R.id.nav_share_log: {
                        sendLog();
                        break;
                    }

                    case R.id.nav_net_test: {
                        switchToTest();
                    }
                }
                return false;
            }
        });
    }

    private void sendLog() {
        if (isFileExist(Constant.AIUI_LOG_PATH)) {
            new Share2.Builder(this)
                    .setContentType(ShareContentType.FILE)
                    .setTitle(getString(R.string.send_aiui_log))
                    .setOnActivityResult(100)
                    .setShareFileUri(FileUtil.getFileUri(this, ShareContentType.FILE, new File(Constant.AIUI_LOG_PATH)))
                    .build()
                    .shareBySystem();
        } else {
            showToast(getString(R.string.aiui_log_not_exist));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            switchChats();
        }
    }

    /**
     * 检测文件是否存在
     *
     * @param path 文件全路径
     * @return 是否存在
     */
    public static boolean isFileExist(String path) {
        File file = new File(path);

        return file.exists();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mIsChatFragment) {
            if (mIsExit) {
                System.exit(0);
            } else {
                showToast("再按一次退出");
                mIsExit = true;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mIsExit = false;
                    }
                }, 2000);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void showToast(String str) {
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }


    //    **************************************人脸识别初始化**************************

    private void faceInit() {

        PreferencesUtil.initPrefs(this);
        // 使用人脸1：n时使用
        DBManager.getInstance().init(this);
//        livnessTypeTip();
//        FaceEnvironment faceEnvironment = new FaceEnvironment();
//        // 模糊度范围 (0-1) 推荐小于0.7
//        faceEnvironment.setBlurrinessThreshold(FaceEnvironment.VALUE_BLURNESS);
//        // 光照范围 (0-1) 推荐大于40
//        faceEnvironment.setIlluminationThreshold(FaceEnvironment.VALUE_BLURNESS);
//        // 人脸yaw,pitch,row 角度，范围（-45，45），推荐-15-15
//        faceEnvironment.setPitch(FaceEnvironment.VALUE_HEAD_PITCH);
//        faceEnvironment.setRoll(FaceEnvironment.VALUE_HEAD_ROLL);
//        faceEnvironment.setYaw(FaceEnvironment.VALUE_HEAD_YAW);
//        // 最小检测人脸（在图片人脸能够被检测到最小值）80-200， 越小越耗性能，推荐120-200
//        faceEnvironment.setMinFaceSize(FaceEnvironment.VALUE_MIN_FACE_SIZE);
//        // 人脸置信度（0-1）推荐大于0.6
//        faceEnvironment.setNotFaceThreshold(FaceEnvironment.VALUE_NOT_FACE_THRESHOLD);
//        // 人脸遮挡范围 （0-1） 推荐小于0.5
//        faceEnvironment.setOcclulationThreshold(FaceEnvironment.VALUE_OCCLUSION);
//        // 是否进行质量检测,开启会降低性能
//        faceEnvironment.setCheckQuality(false);
//        FaceSDKManager.getInstance().getFaceDetector().setFaceEnvironment(faceEnvironment);
        FaceSDKManager.getInstance().init(this);
        FaceSDKManager.getInstance().setSdkInitListener(new FaceSDKManager.SdkInitListener() {
            @Override
            public void initStart() {
                toast("sdk init start");
            }

            @Override
            public void initSuccess() {
                toast("sdk init success");
            }

            @Override
            public void initFail(int errorCode, String msg) {
                toast("sdk init fail:" + msg);
            }
        });
    }

    private void toast(final String text) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ChatActivity.this, text, Toast.LENGTH_LONG).show();
            }
        });
    }

    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }
}
