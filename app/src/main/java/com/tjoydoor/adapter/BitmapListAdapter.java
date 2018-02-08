package com.tjoydoor.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.List;

import cn.tjoydoor.R;


/**
 * Created by hebin
 * on 2017/1/13.
 */

public class BitmapListAdapter extends BaseAdapter {

    private List<Bitmap> list;
    private Activity mactivity;

    public BitmapListAdapter(Activity activity) {
        this.mactivity = activity;
    }

    public void setData(List<Bitmap> list) {
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
            convertView = LayoutInflater.from(mactivity).inflate(R.layout.item_img, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.iv_face.setImageBitmap(list.get(position));

        return convertView;
    }

    private class ViewHolder {

        ImageView iv_face;

        ViewHolder(View view) {

            iv_face = (ImageView) view.findViewById(R.id.img_face);
        }
    }
}
