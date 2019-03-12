package com.iflytek.aiui.demo.chat.ui.zxing;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

import com.blankj.utilcode.util.KeyboardUtils;
import com.google.zxing.Result;
//import com.google.zxing.client.android.CaptureActivity;
//import com.google.zxing.client.android.ViewfinderView;
import com.iflytek.aiui.demo.chat.R;


/**
 * 默认的扫描界面
 */
public class DefaultCaptureActivity  {


//    public static final int SCAN_REQUEST_CODE = 102;
//    public static final String SCAN_REQUEST = "resultCode";
//    public static final int SCAN_FENGCHE_REQUEST_CODE = 103;
//
//    public static final int SCAN_CODE_SUCCESS = 1001;
//    private static final String TAG = DefaultCaptureActivity.class.getSimpleName();
//
//    private TextView mTvBack;
//    private TextView mTvNoScanCode;
//    private TextView mTvOpenTorch;
//    private TextView mTvInput;
//    private TextView mTvTitle;
//    private SurfaceView surfaceView;
//    private ViewfinderView viewfinderView;
//    private boolean isOpen;
//
//
//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_capture);
//
//
//        initView();
//        initListener();
//
//    }
//
//    private void initListener() {
//        mTvBack.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                finish();
//            }
//        });
//        mTvOpenTorch.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (isOpen) {
//                    isOpen = false;
//                    mTvOpenTorch.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.scan_torch_close, 0, 0);
//                    turnLightOff();
//                } else {
//                    isOpen = true;
//                    mTvOpenTorch.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.scan_torch_open, 0, 0);
//                    turnLightOn();
//                }
//            }
//        });
//    }
//
//    private void initView() {
//
//        Intent intent = getIntent();
//        final String tipContent = intent.getStringExtra("tipContent");
//        final String title = intent.getStringExtra("title");
//
//        surfaceView = findViewById(R.id.preview_view);
//        viewfinderView = findViewById(R.id.viewfinder_view);
//        mTvBack = (TextView)findViewById(R.id.tv_back);
//        mTvOpenTorch = (TextView)findViewById(R.id.tv_open_torch);
//        mTvInput = (TextView)findViewById(R.id.tv_input);
//        mTvTitle = (TextView)findViewById(R.id.tv_title);
//
//        //设置扫码提示
//        if (!TextUtils.isEmpty(tipContent)) {
//            viewfinderView.setTip(tipContent);
//        }
//        if (!TextUtils.isEmpty(title)) {
//            mTvTitle.setText(title);
//        }
//    }
//
//
//    @Override
//    public SurfaceView getSurfaceView() {
//        return (surfaceView == null) ? (SurfaceView) findViewById(R.id.preview_view) : surfaceView;
//    }
//
//    @Override
//    public ViewfinderView getViewfinderHolder() {
//        return (viewfinderView == null) ? (ViewfinderView) findViewById(R.id.viewfinder_view) : viewfinderView;
//    }
//
//    @Override
//    public void dealDecode(Result rawResult, Bitmap barcode, float scaleFactor) {
//        Intent intent = new Intent(this, WebViewActivity.class);
//        if (rawResult != null) {
//
//            final String result = rawResult.getText();
//
//
////            if (null != mMakePoint && mMakePoint.model == PointType.TYPE_SCAN_FENGCHE_CODE) {
////                pattern = "^[0-9]{12}$";
////            }
//
//            if (!TextUtils.isEmpty(result)) {
//                //扫码成功返回上级Activity
////                intent.putExtra(ScanResultAct.MSG_SCAN_CODE, rawResult.getText());
//                //设置Result 返回上一个页面扫码结果
//                intent.putExtra("url",  rawResult.getText());
//
//                startActivity(intent);
//                return;
//            }
//        }
//
//    }
//
//    /**
//     * 继续扫码
//     */
//    private void continuePreview() {
//        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
//        SurfaceHolder surfaceHolder = surfaceView.getHolder();
//        initCamera(surfaceHolder);
//        if (handler != null) {
//            handler.restartPreviewAndDecode();
//        }
//    }
//
//
//    @Override
//    public void onBackPressed() {
//        continuePreview();
//        super.onBackPressed();
//
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        KeyboardUtils.fixSoftInputLeaks(this);
//    }
}
