package com.example.lpdemo.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.ColorRes;
import com.example.lpdemo.R;
import com.example.lpdemo.utils.DataController;

public class EcgBkg extends View {
    private TextPaint mTextPaint;
    private Paint bPaint;
    private Paint redPaint;
    private Paint bkg;
    private Paint bkg_paint_1;
    private Paint bkg_paint_2;

    public int mWidth;
    public int mHeight;
    public float mTop;
    public float mBottom;
    public int mBase;

    @ColorRes
    private int bgColor = R.color.white;
    @ColorRes
    private int gridColor5mm = R.color.color_ecg_grid_5mm;
    @ColorRes
    private int gridColor1mm = R.color.color_ecg_grid_1mm;

    public EcgBkg(Context context) {
        super(context);
        init(null, 0);
    }

    public EcgBkg(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public EcgBkg(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }


    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.EcgView, defStyle, 0);

        a.recycle();

        // Set up a default TextPaint object
        mTextPaint = new TextPaint();
        mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.LEFT);
        iniPaint();
    }

    private void iniPaint() {
        redPaint = new Paint();
        redPaint.setColor(getColor(R.color.red_m));
        redPaint.setStyle(Paint.Style.STROKE);
        redPaint.setStrokeWidth(4.0f);

        bkg = new Paint();
        bkg.setColor(getColor(bgColor));

        bkg_paint_1 = new Paint();
        bkg_paint_1.setStyle(Paint.Style.STROKE);
        bkg_paint_1.setStrokeWidth(2.0f);

        bkg_paint_2 = new Paint();
        bkg_paint_2.setColor(getColor(gridColor1mm));
        bkg_paint_2.setStyle(Paint.Style.STROKE);
        bkg_paint_2.setStrokeWidth(1.0f);

        bPaint = new Paint();
        bPaint.setTextAlign(Paint.Align.LEFT);
        bPaint.setTextSize(32);
        bPaint.setColor(getColor(R.color.black));
        bPaint.setStyle(Paint.Style.STROKE);
        bPaint.setStrokeWidth(4.0f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        iniParam();
        drawBkg(canvas);
    }

    private void iniParam() {
        mWidth = getWidth();
        mHeight = getHeight();

        mBase = (mHeight / 2);
        mTop = (mBase - 20 / DataController.mm2px);
        mBottom = (mBase + 20 / DataController.mm2px);
    }

    private void drawBkg(Canvas canvas) {

        canvas.drawColor(getColor(bgColor));

        bkg.setColor(getColor(bgColor));
        bkg_paint_1.setColor(getColor(gridColor5mm));
        bkg_paint_2.setColor(getColor(gridColor1mm));

        // 1mm y
        for (int i = 0; i < mHeight/2/(1/DataController.mm2px); i ++) {
            Path p = new Path();
            p.moveTo(0, mBase + i*(1/DataController.mm2px));
            p.lineTo(mWidth, mBase + i*(1/DataController.mm2px));

            p.moveTo(0, mBase - i*(1/DataController.mm2px));
            p.lineTo(mWidth, mBase - i*(1/DataController.mm2px));

            canvas.drawPath(p, bkg_paint_2);
        }

        // 5mm y
        for (int i = 0; i < mHeight/2/(5/DataController.mm2px); i++) {
            Path p = new Path();
            p.moveTo(0, mBase + i*(5/DataController.mm2px));
            p.lineTo(mWidth, mBase + i*(5/DataController.mm2px));

            p.moveTo(0, mBase - i*(5/DataController.mm2px));
            p.lineTo(mWidth, mBase - i*(5/DataController.mm2px));
            canvas.drawPath(p, bkg_paint_1);
        }

        // 1mm x
        for (int i = 0; i < mWidth/(1/ DataController.mm2px) + 1; i++) {
            Path p = new Path();
            p.moveTo(i/ DataController.mm2px, 0);
            p.lineTo(i/ DataController.mm2px, mHeight);
            canvas.drawPath(p, bkg_paint_2);
        }

        // 5mm x
        for (int i = 0; i < mWidth/(5/ DataController.mm2px) + 1; i++) {
            Path p = new Path();
            p.moveTo(i*5/ DataController.mm2px, 0);
            p.lineTo(i*5/ DataController.mm2px, mHeight);
            canvas.drawPath(p, bkg_paint_1);
        }

    }

    private int getColor(int resource_id) {
        return getResources().getColor(resource_id);
    }

}
