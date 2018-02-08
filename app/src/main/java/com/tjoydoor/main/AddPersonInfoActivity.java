package com.tjoydoor.main;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.lzy.imagepicker.ImagePicker;
import com.lzy.imagepicker.bean.ImageItem;
import com.lzy.imagepicker.ui.ImageGridActivity;
import com.tjoydoor.Global;
import com.tjoydoor.entity.UserInfo;
import com.tjoydoor.util.SPUtil;
import com.tjoydoor.util.TextUtil;
import com.tjoydoor.util.ToastUtil;
import com.tjoydoor.util.glide.GlideUtil;
import com.tjoydoor.view.RoundImageView;
import com.tjoydoor.view.button.StateButton;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.tjoydoor.R;

@SuppressWarnings("unchecked")
public class AddPersonInfoActivity extends AppCompatActivity {

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
    @BindView(R.id.et_name)
    EditText etName;
    @BindView(R.id.et_dept)
    EditText etDept;
    @BindView(R.id.et_position)
    EditText etPosition;
    @BindView(R.id.ll_msg)
    LinearLayout llMsg;
    @BindView(R.id.iv_head)
    RoundImageView ivHead;
    @BindView(R.id.iv_camera)
    ImageView ivCamera;
    @BindView(R.id.btn_save)
    StateButton btnSave;


    private String photoPath;
    private List<UserInfo> userInfoList;
    private int IMAGE_PICKER = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Global.Varibale.isBigText) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addpersoninfo);
        ButterKnife.bind(this);
        tvTitle.setText("人员添加");
        userInfoList = new ArrayList<>();

        etName.setText("");
        etDept.setText("");
        etPosition.setText("");
    }

    @OnClick({R.id.fl_left, R.id.fl_right, R.id.iv_head, R.id.btn_save})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fl_left:
                finish();
                break;
            case R.id.fl_right:
                break;
            case R.id.iv_head:
                Intent intent = new Intent(this, ImageGridActivity.class);
                startActivityForResult(intent, IMAGE_PICKER);
                break;
            case R.id.btn_save:
                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                dialog.setMessage("确定信息无误保存？确定后将进入样本录入!").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String name = etName.getText().toString().trim();
                        String dept = etDept.getText().toString().trim();
                        String position = etPosition.getText().toString().trim();
                        String path = photoPath;

                        if (TextUtil.isEmpty(name) || TextUtil.isEmpty(dept) || TextUtil.isEmpty(position) || TextUtil.isEmpty(path)) {
                            ToastUtil.showMessage(AddPersonInfoActivity.this, "请填写完整");
                        } else {
                            userInfoList.clear();
                            UserInfo userInfo = new UserInfo();
                            userInfo.setUserName(name);
                            userInfo.setDeptName(dept);
                            userInfo.setGroupName(position);
                            userInfo.setPicPath(path);
                            userInfoList.add(userInfo);
                            //把之前的叠加起来
                            if (SPUtil.getInstance(AddPersonInfoActivity.this).getUserInfoList() != null) {
                                userInfoList.addAll(SPUtil.getInstance(AddPersonInfoActivity.this).getUserInfoList());
                            }
                            //保存总的
                            SPUtil.getInstance(AddPersonInfoActivity.this).putString(Global.Const.userInfoList, new Gson().toJson(userInfoList));
                            ToastUtil.showMessage(AddPersonInfoActivity.this, "个人数据保存成功");

                            Global.Varibale.dymicPersonId = etName.getText().toString().trim();
                            startActivity(new Intent(AddPersonInfoActivity.this, AddFaceDymicActivity.class));
                            finish();

                        }
                    }
                }).setNegativeButton("取消", null).show();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == ImagePicker.RESULT_CODE_ITEMS) {

            if (data != null && requestCode == IMAGE_PICKER) {
                //个人显示图片
                ArrayList<ImageItem> images = (ArrayList<ImageItem>) data.getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);

                photoPath = images.get(0).path;
                GlideUtil.loadImageViewAsBitmap(this, photoPath, ivHead);

                if (!TextUtil.isEmpty(photoPath)) {
                    ivCamera.setVisibility(View.GONE);
                }
            } else {
                ToastUtil.showMessage(AddPersonInfoActivity.this, "没有数据");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GlideUtil.clearMemory(this);
    }
}
