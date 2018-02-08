// Copyright (c) Philipp Wagner. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.tjoydoor.view.camera;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

import com.tjoydoor.Global;
import com.tjoydoor.util.TextUtil;

import java.text.DecimalFormat;


/**
 * Author：hebin on 5/20/2016
 * <p/>
 * Annotations：
 */

@SuppressWarnings("ALL")
public class FaceOverlayViewAge extends View {

    private Paint mPaint;
    private Paint mTextPaint;
    private int mDisplayOrientation;
    private int mOrientation;
    private int previewWidth;
    private int previewHeight;
    private FaceResult[] mFaces;
    private double fps;
    private boolean isFront = false;
    private String age = "", sex = "";
    private Context context;


    public FaceOverlayViewAge(Context context) {
        super(context);
        initialize();
    }

    public FaceOverlayViewAge(Context context, AttributeSet attrs) {
        super(context, attrs);

        initialize();
    }

    public FaceOverlayViewAge(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    public FaceOverlayViewAge(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize();
    }


    private void initialize() {
        // We want a green box around the face:
        DisplayMetrics metrics = getResources().getDisplayMetrics();

        int stroke = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, metrics);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(Color.parseColor("#00ffff"));
        mPaint.setStrokeWidth(stroke);
        mPaint.setStyle(Paint.Style.STROKE);

        int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, metrics);
        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setDither(true);
        mTextPaint.setTextSize(size);
        mTextPaint.setColor(Color.parseColor("#00ffff"));
        mTextPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);

        if (mFaces != null && mFaces.length > 0) {

            float scaleX = (float) getWidth() / (float) previewWidth;
            float scaleY = (float) getHeight() / (float) previewHeight;

            switch (mDisplayOrientation) {
                case 90:
                case 270:
                    scaleX = (float) getWidth() / (float) previewHeight;
                    scaleY = (float) getHeight() / (float) previewWidth;
                    break;
            }

            canvas.save();
            canvas.rotate(-mOrientation);
            RectF rectF = new RectF();
            for (FaceResult face : mFaces) {
                PointF mid = new PointF();
                face.getMidPoint(mid);

                if (mid.x != 0.0f && mid.y != 0.0f) {
                    float eyesDis = face.eyesDistance();

                    rectF.set(new RectF(
                            (mid.x - eyesDis * 1.50f) * scaleX,
                            (mid.y - eyesDis * 1.50f) * scaleY,
                            (mid.x + eyesDis * 1.50f) * scaleX,
                            (mid.y + eyesDis * 2.00f) * scaleY));

                    if (isFront) {
                        float left = rectF.left;
                        float right = rectF.right;
                        rectF.left = getWidth() - right;
                        rectF.right = getWidth() - left;
                    }

                    if (!TextUtil.isEmpty(Global.Varibale.age)) {
                        age = Global.Varibale.age;
                        sex = Global.Varibale.sex;
                    } else {
                        age = "";
                        sex = "";
                    }

                    canvas.drawRect(rectF, mPaint);

                    canvas.drawText("年龄: " + age + " 岁", rectF.left, rectF.bottom + mTextPaint.getTextSize() * 1, mTextPaint);
                    canvas.drawText("性别: " + sex, rectF.left, rectF.bottom + mTextPaint.getTextSize() * 2, mTextPaint);
                    if (Global.Varibale.httpTime > 1000) {
                        canvas.drawText("耗时:" + Global.Varibale.httpTime + "ms" + "  网络延时较高，请稍候...", rectF.left, rectF.bottom + mTextPaint.getTextSize() * 3, mTextPaint);
                    } else {
                        canvas.drawText("耗时:" + Global.Varibale.httpTime + "ms", rectF.left, rectF.bottom + mTextPaint.getTextSize() * 3, mTextPaint);
                    }
                }
            }
            canvas.restore();
        }

        DecimalFormat df2 = new DecimalFormat(".##");
        canvas.drawText("fps: " + df2.format(fps) + "  分辨率：" + previewWidth + "x" + previewHeight, mTextPaint.getTextSize(), mTextPaint.getTextSize(), mTextPaint);
    }


    public void setOrientation(int orientation) {
        mOrientation = orientation;
    }

    public void setDisplayOrientation(int displayOrientation) {
        mDisplayOrientation = displayOrientation;
        invalidate();
    }

    public void setFPS(double fps) {
        this.fps = fps;
    }

    public void setFaces(FaceResult[] faces) {
        mFaces = faces;
        invalidate();
    }

    public void setPreviewWidth(int previewWidth) {
        this.previewWidth = previewWidth;
    }

    public void setPreviewHeight(int previewHeight) {
        this.previewHeight = previewHeight;
    }

    public void setFront(boolean front) {
        isFront = front;
    }
}