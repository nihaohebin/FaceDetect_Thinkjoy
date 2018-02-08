package com.tjoydoor.adapter;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.orhanobut.logger.Logger;
import com.tjoydoor.Global;
import com.tjoydoor.entity.UserInfo;
import com.tjoydoor.main.PersonInfoActivity;
import com.tjoydoor.netokhttp.NetOkhttp;
import com.tjoydoor.util.SPUtil;

import java.util.List;

import cn.thinkjoy.face.imp.FaceDeleteListener;
import cn.thinkjoy.face.imp.FaceIdQueryListener;
import cn.thinkjoy.face.manage.FaceManage;
import cn.thinkjoy.face.model.ErrorMsg;
import cn.thinkjoy.face.model.FaceDeleteInfo;
import cn.tjoydoor.R;


/**
 * Created by hebin
 * on 2017/1/13.
 */

public class PersonListAdapter extends BaseAdapter {

    private List<UserInfo> list;
    private Activity mactivity;
    private SPUtil spUtil;

    public PersonListAdapter(Activity activity) {
        this.mactivity = activity;
    }

    public void setData(List<UserInfo> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    private void removeList(int position) {
        this.list.remove(position);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {

        return list == null ? 0 : list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mactivity).inflate(R.layout.item_personname, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final UserInfo bean = list.get(position);
        holder.tv_name.setText(bean.getUserName());

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Global.Varibale.personId = bean.getUserName();
                mactivity.startActivity(new Intent(mactivity, PersonInfoActivity.class));
            }
        });
        convertView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(mactivity);
                dialog.setMessage("是否要删除？");
                dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //界面删除刷新
                        removeList(position);

                        spUtil = new SPUtil(mactivity);
                        //数据库删除刷新
                        List<UserInfo> userInfoList = spUtil.getUserInfoList();
                        for (int i = 0; i < userInfoList.size(); i++) {
                            if (userInfoList.get(i).getUserName().equals(bean.getUserName())) {
                                userInfoList.remove(i);
                                spUtil.putString(Global.Const.userInfoList,new Gson().toJson(userInfoList));
                            }
                        }

                        //该人旗下所有人脸删除
                        FaceManage.newInstance(mactivity).getFaceId(Global.Varibale.faceSetId, bean.getUserName(), new FaceIdQueryListener() {
                            @Override
                            public void onFaceIdQueryListener(List<String> faceIdInfo, ErrorMsg error) {
                                if (error.getCode() == 0) {
                                    if (faceIdInfo == null || faceIdInfo.size() == 0) {
                                        Toast.makeText(mactivity, "没有人脸数据,请添加人脸数据！", Toast.LENGTH_SHORT).show();
                                    } else {
                                        FaceManage.newInstance(mactivity).deleteFace(faceIdInfo, Global.Varibale.faceSetId, new FaceDeleteListener() {
                                            @Override
                                            public void onFaceDeleteListener(FaceDeleteInfo deleteInfo, ErrorMsg error) {
                                                if (error.getCode() == 0) {
                                                    Logger.i(deleteInfo.getDeleteCount() + "--" + deleteInfo.getFaceCount());
//                                                                Toast.makeText(activity, "删除成功", Toast.LENGTH_SHORT).show();
                                                    NetOkhttp.refreshFaceSet(mactivity);
                                                } else {
                                                                Toast.makeText(mactivity, "删除失败! " + error.getMsg(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                } else {
                                    Toast.makeText(mactivity, error.getMsg(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });
                dialog.setNegativeButton("取消", null);
                dialog.show();
                return true;
            }
        });
        return convertView;
    }

    private class ViewHolder {

        TextView tv_name;

        ViewHolder(View view) {

            tv_name = (TextView) view.findViewById(R.id.tv_name);
        }
    }
}
