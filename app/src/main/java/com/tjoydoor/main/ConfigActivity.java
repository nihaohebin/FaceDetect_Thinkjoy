package com.tjoydoor.main;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tjoydoor.Global;
import com.tjoydoor.util.SPUtil;
import com.tjoydoor.util.TextUtil;
import com.tjoydoor.util.ToastUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.tjoydoor.R;

import static com.tjoydoor.Global.Const.cameraId;


public class ConfigActivity extends AppCompatActivity {


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
    @BindView(R.id.rbFront)
    RadioButton rbFront;
    @BindView(R.id.rbRear)
    RadioButton rbRear;
    @BindView(R.id.radioGroup)
    RadioGroup radioGroup;
    @BindView(R.id.btnSave)
    Button btnSave;
    @BindView(R.id.etFocusValue)
    EditText etFocusValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_config);
        ButterKnife.bind(this);

        tvTitle.setText("应用设置");
        flLeft.setVisibility(View.INVISIBLE);


        if (SPUtil.getInstance(ConfigActivity.this).getInt(Global.Const.cameraId, 1) == 1) {
            rbFront.setChecked(true);
            rbFront.setButtonDrawable(R.drawable.rbcheck);
            rbRear.setButtonDrawable(R.drawable.rbnocheck);
        } else {
            rbRear.setChecked(true);
            rbFront.setButtonDrawable(R.drawable.rbnocheck);
            rbRear.setButtonDrawable(R.drawable.rbcheck);
        }

        int focusValue = SPUtil.getInstance(ConfigActivity.this).getInt(Global.Const.focusValue, 0);
        etFocusValue.setText(focusValue + "");
    }


    @OnClick({R.id.rbFront, R.id.rbRear, R.id.btnSave})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.rbFront:
                rbFront.setButtonDrawable(R.drawable.rbcheck);
                rbRear.setButtonDrawable(R.drawable.rbnocheck);
                break;
            case R.id.rbRear:
                rbFront.setButtonDrawable(R.drawable.rbnocheck);
                rbRear.setButtonDrawable(R.drawable.rbcheck);
                break;
            case R.id.btnSave:

                if (TextUtil.isEmpty(etFocusValue.getText().toString())) {
                    ToastUtil.showMessage(ConfigActivity.this, "请填写完整");
                    return;
                }

                if (rbFront.isChecked()) {
                    SPUtil.getInstance(ConfigActivity.this).putInt(cameraId, 1);
                } else {
                    SPUtil.getInstance(ConfigActivity.this).putInt(cameraId, 0);
                }

                SPUtil.getInstance(ConfigActivity.this).putInt(Global.Const.focusValue, Integer.parseInt(etFocusValue.getText().toString().trim()));

                ToastUtil.showMessage(ConfigActivity.this,"已保存");
                finish();

                break;
        }
    }

    @OnClick(R.id.fl_left)
    public void onViewClicked() {
    }
}
