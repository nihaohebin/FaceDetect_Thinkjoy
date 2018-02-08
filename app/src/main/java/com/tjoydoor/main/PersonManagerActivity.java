package com.tjoydoor.main;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tjoydoor.Global;
import com.tjoydoor.adapter.PersonListAdapter;
import com.tjoydoor.util.SPUtil;
import com.tjoydoor.util.ToastUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.tjoydoor.R;


public class PersonManagerActivity extends AppCompatActivity {


    @BindView(R.id.iv_left)
    ImageView ivLeft;
    @BindView(R.id.tv_left)
    TextView tvLeft;
    @BindView(R.id.fl_left)
    FrameLayout flLeft;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.iv_right)
    ImageView ivRight;
    @BindView(R.id.tv_right)
    TextView tvRight;
    @BindView(R.id.fl_right)
    FrameLayout flRight;
    @BindView(R.id.layout_title)
    RelativeLayout layoutTitle;
    @BindView(R.id.lv_peopellist)
    ListView lvPeopellist;


    private PersonListAdapter personListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Global.Varibale.isBigText) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_peoplemanage);
        ButterKnife.bind(this);

        tvTitle.setText("人员列表（" + Global.Varibale.faceSetName + "）");
        ivRight.setVisibility(View.VISIBLE);
        ivRight.setImageResource(R.drawable.icon_addperson);

        if (SPUtil.getInstance(PersonManagerActivity.this).getUserInfoList() != null) {
            if (SPUtil.getInstance(PersonManagerActivity.this).getUserInfoList().size() != 0) {
                personListAdapter = new PersonListAdapter(this);
                personListAdapter.setData(SPUtil.getInstance(PersonManagerActivity.this).getUserInfoList());
                lvPeopellist.setAdapter(personListAdapter);
            }
        } else {
            ToastUtil.showMessage(PersonManagerActivity.this, "请添加人员");
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
//        Logger.i("onStart");
        //返回是先onRestart再onStart
    }


    @Override
    protected void onRestart() {
        super.onRestart();

        if (SPUtil.getInstance(PersonManagerActivity.this).getUserInfoList() != null) {
            if (SPUtil.getInstance(PersonManagerActivity.this).getUserInfoList().size() != 0) {
                personListAdapter = new PersonListAdapter(this);
                personListAdapter.setData(SPUtil.getInstance(PersonManagerActivity.this).getUserInfoList());
                lvPeopellist.setAdapter(personListAdapter);
            }
        } else {
            ToastUtil.showMessage(PersonManagerActivity.this, "请添加人员");
        }
    }

    @OnClick({R.id.fl_left, R.id.fl_right})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fl_left:
                finish();
                break;
            case R.id.fl_right:
                startActivity(new Intent(PersonManagerActivity.this, AddPersonInfoActivity.class));
                break;
        }
    }

}
