package com.tjoydoor.adapter;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.orhanobut.logger.Logger;
import com.tjoydoor.Global;
import com.tjoydoor.netokhttp.NetOkhttp;
import com.tjoydoor.util.DialogUtil;

import java.util.ArrayList;
import java.util.List;

import cn.thinkjoy.face.imp.FaceDeleteListener;
import cn.thinkjoy.face.manage.FaceManage;
import cn.thinkjoy.face.model.ErrorMsg;
import cn.thinkjoy.face.model.FaceDeleteInfo;
import cn.tjoydoor.R;


/**
 * Created by hebin
 * on 2017/1/13.
 */

public class FaceListAdapter extends BaseAdapter {

    private List<String> list;
    private Activity mactivity;

    public FaceListAdapter(Activity activity) {
        this.mactivity = activity;

    }

    public void setData(List<String> list) {
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
            convertView = LayoutInflater.from(mactivity).inflate(R.layout.item_face, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Glide.with(mactivity)
                .load(list.get(position))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.ic_loading)
                .override(50, 50)
                .into(holder.iv_face);

        if (position==(list.size()-1)){
            DialogUtil.closeProgressDialog();
        }

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

                        List<String> faceIdList = new ArrayList<>();

                        Logger.i("position = " + position + "\nfaceId = " +Global.Varibale.listFaceId.get(position));

                        faceIdList.add(Global.Varibale.listFaceId.get(position));

                        FaceManage.newInstance(mactivity).deleteFace(faceIdList, Global.Varibale.faceSetId, new FaceDeleteListener() {
                            @Override
                            public void onFaceDeleteListener(FaceDeleteInfo deleteInfo, ErrorMsg error) {
                                if (error.getCode() == 0) {
//                                    Logger.i(deleteInfo.getDeleteCount() + "--" + deleteInfo.getFaceCount());
                                    NetOkhttp.refreshFaceSet(mactivity);
                                } else {
                                    Toast.makeText(mactivity, "删除失败! " + error.getMsg(), Toast.LENGTH_SHORT).show();
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

        ImageView iv_face;

        ViewHolder(View view) {
            iv_face = (ImageView) view.findViewById(R.id.iv_face);
        }
    }
}
