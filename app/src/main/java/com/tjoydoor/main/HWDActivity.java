package com.tjoydoor.main;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import com.orhanobut.logger.Logger;
import com.tjoydoor.netokhttp.NetOkhttp;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.tjoydoor.R;

public class HWDActivity extends AppCompatActivity {


    @BindView(R.id.btn_sendData)
    Button btnSendData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hwd);
        ButterKnife.bind(this);

    }

    @OnClick(R.id.btn_sendData)
    public void onViewClicked() {
        Logger.i("点击成功");

        new Thread(new Runnable() {
            @Override
            public void run() {
                NetOkhttp.send();
            }
        }).start();
    }
}
