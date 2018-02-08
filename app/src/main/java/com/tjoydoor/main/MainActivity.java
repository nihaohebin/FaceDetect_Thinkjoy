package com.tjoydoor.main;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tjoydoor.Global;
import com.tjoydoor.receiver.NetworkStateReceiver;
import com.tjoydoor.util.SPUtil;
import com.tjoydoor.util.ScreenUtil;
import com.tjoydoor.util.TimeUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.thinkjoy.sdk.SDKInitializer;
import cn.tjoydoor.R;


public class MainActivity extends AppCompatActivity {


    @BindView(R.id.tvYear)
    TextView tvYear;
    @BindView(R.id.ivFacePhonePortrait)
    ImageView ivFacePhonePortrait;
    @BindView(R.id.ivFacePhoneLandscape)
    ImageView ivFacePhoneLandscape;
    @BindView(R.id.ivFacePadLandscape)
    ImageView ivFacePadLandscape;
    @BindView(R.id.llFrameFace)
    LinearLayout llFrameFace;
    @BindView(R.id.ivAgePhoneLandscape)
    ImageView ivAgePhoneLandscape;
    @BindView(R.id.ivAgePadLandScape)
    ImageView ivAgePadLandScape;
    @BindView(R.id.llFrameAge)
    LinearLayout llFrameAge;
    @BindView(R.id.tvMonth)
    TextView tvMonth;
    @BindView(R.id.tvDay)
    TextView tvDay;
    @BindView(R.id.llFaceDetect)
    LinearLayout llFaceDetect;
    @BindView(R.id.llAge)
    LinearLayout llAge;
    @BindView(R.id.llRealDetect)
    LinearLayout llRealDetect;
    @BindView(R.id.llPersonManager)
    LinearLayout llPersonManager;
    @BindView(R.id.llSetting)
    LinearLayout llSetting;


    private NetworkStateReceiver mNetworkStateReceiver;
    private int count = 0;
    private int count2 = 0;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ScreenUtil.setFullScreen(this);//全屏
        ScreenUtil.keepScreenLight(this);
        ScreenUtil.hideNavigation(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        SPUtil.getInstance(MainActivity.this).putBoolean(Global.Const.isFirstInApp,true);

        //注册网络监控广播监听
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        mNetworkStateReceiver = new NetworkStateReceiver();
        registerReceiver(mNetworkStateReceiver, filter);

        tvYear.setText(TimeUtil.getYear() + "年");
        tvMonth.setText(TimeUtil.getMonth());
        tvDay.setText(TimeUtil.getDay());
    }

    @Override
    protected void onStart() {
        super.onStart();
        llFrameFace.setVisibility(View.GONE);
        llFrameAge.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SDKInitializer.onResume(getApplicationContext());
    }


    @OnClick({R.id.ivFacePhonePortrait, R.id.ivFacePhoneLandscape,
            R.id.ivFacePadLandscape, R.id.ivAgePhoneLandscape,
            R.id.ivAgePadLandScape, R.id.llFaceDetect, R.id.llAge,
            R.id.llRealDetect, R.id.llPersonManager, R.id.llSetting})

    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivFacePhonePortrait:
                count=0;
                count2 = 0;
                Global.Varibale.isBigText = false;
                startActivity(new Intent(MainActivity.this, MainPhone_PActivity.class));
                break;
            case R.id.ivFacePhoneLandscape:
                count=0;
                count2 = 0;
                Global.Varibale.isBigText = false;
                startActivity(new Intent(MainActivity.this, MainPhone_LActivity.class));
                break;
            case R.id.ivFacePadLandscape:
                count=0;
                count2 = 0;
                Global.Varibale.isBigText = true;
                startActivity(new Intent(MainActivity.this, MainPhone_LActivity.class));
                break;
            case R.id.ivAgePhoneLandscape:
                count=0;
                count2 = 0;
                Global.Varibale.isBigText = false;
                startActivity(new Intent(MainActivity.this, MainPhone_Age_LActivity.class));
                break;
            case R.id.ivAgePadLandScape:
                count=0;
                count2 = 0;
                Global.Varibale.isBigText = true;
                startActivity(new Intent(MainActivity.this, MainPhone_Age_LActivity.class));
                break;
            case R.id.llFaceDetect:

                ++count;
                count2 = 0;
                llFrameAge.setVisibility(View.GONE);
                llFrameFace.setVisibility(View.VISIBLE);

                ivAgePadLandScape.setEnabled(false);
                ivAgePhoneLandscape.setEnabled(false);

                if (count % 2 != 0) {

                    llFrameFace.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim_translate_up2));
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            ivFacePadLandscape.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim_translate_up));
                            ivFacePhoneLandscape.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim_translate_up));
                            ivFacePhonePortrait.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim_translate_up));
                            ivFacePadLandscape.setEnabled(true);
                            ivFacePhoneLandscape.setEnabled(true);
                            ivFacePhonePortrait.setEnabled(true);

                        }
                    }, 200);


                } else {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            llFrameFace.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim_translate_down2));
                        }
                    }, 300);

                    ivFacePadLandscape.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim_translate_down));
                    ivFacePhoneLandscape.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim_translate_down));
                    ivFacePhonePortrait.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim_translate_down));
                    ivFacePadLandscape.setEnabled(false);
                    ivFacePhoneLandscape.setEnabled(false);
                    ivFacePhonePortrait.setEnabled(false);
                }

                break;
            case R.id.llAge:
                ++count2;
                count = 0;
                llFrameFace.setVisibility(View.GONE);
                llFrameAge.setVisibility(View.VISIBLE);
                ivFacePadLandscape.setEnabled(false);
                ivFacePhoneLandscape.setEnabled(false);
                ivFacePhonePortrait.setEnabled(false);

                if (count2 % 2 != 0) {

                    llFrameAge.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim_translate_up2));
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            ivAgePadLandScape.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim_translate_up));
                            ivAgePhoneLandscape.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim_translate_up));
                            ivAgePadLandScape.setEnabled(true);
                            ivAgePhoneLandscape.setEnabled(true);
                        }
                    }, 200);


                } else {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            llFrameAge.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim_translate_down2));
                        }
                    }, 300);

                    ivAgePadLandScape.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim_translate_down));
                    ivAgePhoneLandscape.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim_translate_down));
                    ivAgePadLandScape.setEnabled(false);
                    ivAgePhoneLandscape.setEnabled(false);
                }

                break;
            case R.id.llRealDetect:
                count=0;
                count2 = 0;
                startActivity(new Intent(MainActivity.this, MainPhone_Detect_LActivity.class));
                break;
            case R.id.llPersonManager:
                count=0;
                count2 = 0;
                Global.Varibale.isBigText = false;
                startActivity(new Intent(MainActivity.this, PersonManagerActivity.class));
                break;
            case R.id.llSetting:
                count=0;
                count2 = 0;
                startActivity(new Intent(MainActivity.this, ConfigActivity.class));
                break;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        SDKInitializer.onDestroy(getApplicationContext());

        unregisterReceiver(mNetworkStateReceiver);
    }
}
