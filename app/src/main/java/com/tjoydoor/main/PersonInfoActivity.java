package com.tjoydoor.main;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tjoydoor.Global;
import com.tjoydoor.adapter.FaceListAdapter;
import com.tjoydoor.entity.UserInfo;
import com.tjoydoor.util.DialogUtil;
import com.tjoydoor.util.SPUtil;
import com.tjoydoor.util.glide.GlideUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.thinkjoy.face.imp.FaceDetailListener;
import cn.thinkjoy.face.imp.FaceIdQueryListener;
import cn.thinkjoy.face.manage.FaceManage;
import cn.thinkjoy.face.model.ErrorMsg;
import cn.thinkjoy.face.model.FaceDetail;
import cn.tjoydoor.R;


public class PersonInfoActivity extends AppCompatActivity {


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
    @BindView(R.id.iv_head)
    ImageView ivHead;
    @BindView(R.id.et_name)
    EditText etName;
    @BindView(R.id.et_position)
    EditText etPosition;
    @BindView(R.id.et_dept)
    EditText etDept;
    @BindView(R.id.gv_faceList)
    GridView gvFaceList;

    private FaceListAdapter adapter;
    private List<String> listUrl = new ArrayList<>();
    private List<String> listFaceId = new ArrayList<>();

    private Map<String, String> mapFaceIdUrl = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Global.Varibale.isBigText) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person);
        ButterKnife.bind(this);

        etName.setFocusable(false);
        etDept.setFocusable(false);
        etPosition.setFocusable(false);

        tvTitle.setText(Global.Varibale.personId + "个人信息");
        ivRight.setVisibility(View.VISIBLE);
        ivRight.setImageResource(R.drawable.icon_addpic);

        for (UserInfo userInfo : SPUtil.getInstance(this).getUserInfoList()) {
            if (userInfo.getUserName().equals(Global.Varibale.personId)) {
                etName.setText(userInfo.getUserName());
                etDept.setText(userInfo.getDeptName());
                etPosition.setText(userInfo.getGroupName());
//                Logger.i("图片路径 = " + userInfo.getPicPath());  //本地图片
                GlideUtil.loadImageCrop(this, userInfo.getPicPath(), ivHead);
            }
        }

        if (adapter == null) {
            adapter = new FaceListAdapter(this);
        }

        getFaceIdList();

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                getFaceIdList();
            }
        }, 500);
    }

    private Handler handlerURL = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (msg.arg1 == 1) {

                if (listFaceId.size() == listUrl.size()) {
                    adapter.setData(listUrl);
                    gvFaceList.setAdapter(adapter);
                }else {
                    addFacePic();
                }
            }
        }
    };


    private int urlCount = 0;

    public void getFaceIdList() {

        DialogUtil.showProgressDialog(PersonInfoActivity.this, "正在加载样本，请稍后...", false);
        FaceManage.newInstance(this).getFaceId(Global.Varibale.faceSetId, Global.Varibale.personId, new FaceIdQueryListener() {
            @Override
            public void onFaceIdQueryListener(final List<String> faceIdInfo, ErrorMsg error) {
                if (error.getCode() == 0) {
                    if (faceIdInfo == null || faceIdInfo.size() == 0) {
                        DialogUtil.closeProgressDialog();
                        Toast.makeText(PersonInfoActivity.this, "没有该用户人脸样本，请添加人脸样本！", Toast.LENGTH_SHORT).show();
                    } else {
                        for (String faceId : faceIdInfo) {
                            listFaceId.add(faceId);
                        }
                        Global.Varibale.listFaceId = listFaceId;

                        addFacePic();
                    }
                } else {
                    DialogUtil.closeProgressDialog();
                    Toast.makeText(PersonInfoActivity.this, error.getMsg(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void addFacePic() {

        if (urlCount<listFaceId.size()){
            FaceManage.newInstance(PersonInfoActivity.this).getFaceDetail(listFaceId.get(urlCount), true, new FaceDetailListener() {
                @Override
                public void onFaceDetailListener(final FaceDetail detail, ErrorMsg error) {
                    if (error.getCode() == 0) {

                        listUrl.add(detail.getUrl());
                        ++urlCount;
                        Message msg = Message.obtain();
                        msg.arg1 = 1;
                        handlerURL.sendMessage(msg);

                    } else {
                        DialogUtil.closeProgressDialog();
                        Toast.makeText(PersonInfoActivity.this, error.getMsg(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }


    @OnClick({R.id.fl_left, R.id.fl_right})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fl_left:
                finish();
                break;
            case R.id.fl_right:
                Global.Varibale.dymicPersonId = Global.Varibale.personId;
                startActivity(new Intent(PersonInfoActivity.this, AddFaceDymicActivity.class));
                finish();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GlideUtil.clearMemory(this);
    }

}
