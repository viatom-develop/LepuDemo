package com.example.lpdemo.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import com.example.lpdemo.R;
import com.example.lpdemo.utils.Er3DataController;

/**
 * normal ecg view, use Er3DataController
 * use in 12 lead
 */
public class Er3EcgView extends View {

    private TextPaint mTextPaint;
    private Paint bPaint;
    private Paint linePaint;
    private Paint wPaint;
    private Paint redPaint;
    private Paint redPaint2;
    private Paint redPaint3;

    public int mWidth;
    public int mHeight;
    public float mTop;
    public float mBottom;
    public int mBase;

    private int maxIndex;

    private float[] dataSrc = null;

    public Er3EcgView(Context context) {
        super(context);
        init(null, 0);
    }

    public Er3EcgView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public Er3EcgView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.EcgView, defStyle, 0);

        a.recycle();

        // Set up a default TextPaint object
        iniPaint();
    }

    private void iniPaint() {
        mTextPaint = new TextPaint();
        mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.LEFT);
		mTextPaint.setTextSize(24);
		mTextPaint.setStyle(Paint.Style.FILL);
		mTextPaint.setStrokeWidth((float) 1.5);
		mTextPaint.setColor(getColor(R.color.black));

        redPaint = new Paint();
        redPaint.setColor(getColor(R.color.red_m));
        redPaint.setStyle(Paint.Style.STROKE);
        redPaint.setStrokeWidth(4.0f);

        redPaint2 = new Paint();
        redPaint2.setColor(getColor(R.color.red_b));
        redPaint2.setStyle(Paint.Style.STROKE);
        redPaint2.setStrokeWidth(2.0f);

        redPaint3 = new Paint();
        redPaint3.setColor(getColor(R.color.red_b));
        redPaint3.setStyle(Paint.Style.STROKE);
        redPaint3.setStrokeWidth(1.0f);

        linePaint = new Paint();
        linePaint.setAntiAlias(true);
        linePaint.setTextSize(15);
        linePaint.setStyle(Paint.Style.FILL);
        linePaint.setStrokeWidth((float) 4.0f);
        linePaint.setColor(getColor(R.color.black));

        wPaint = new Paint();
        wPaint.setColor(getColor(R.color.color_ecg_line));
        wPaint.setStyle(Paint.Style.STROKE);
        wPaint.setStrokeWidth(4.0f);
        wPaint.setTextAlign(Paint.Align.LEFT);
        wPaint.setTextSize(32);

        bPaint = new Paint();
        bPaint.setTextAlign(Paint.Align.LEFT);
        bPaint.setTextSize(32);
        bPaint.setColor(getColor(R.color.black));
        bPaint.setStyle(Paint.Style.STROKE);
        bPaint.setStrokeWidth(4.0f);
    }

    public void setDataSrc(float[] fs) {
        this.dataSrc = fs;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        iniParam();

        drawRuler(canvas);

        drawWave(canvas);
    }

    private void iniParam() {
        maxIndex = Er3DataController.INSTANCE.getMaxIndex();

        if (dataSrc == null) {
            dataSrc = new float[maxIndex];
        }

        mWidth = getWidth();
        mHeight = getHeight();

        mBase = (mHeight / 2);
        mTop = (float) (mBase - 20 / Er3DataController.INSTANCE.getMm2px());
        mBottom = (float) (mBase + 20 / Er3DataController.INSTANCE.getMm2px());
    }

    private void drawRuler(Canvas canvas) {
        float chartStartX = (float) (1.0 / (5.0 * Er3DataController.INSTANCE.getMm2px()));
        float standardYTop = mBase - (Er3DataController.INSTANCE.getAmpVal() * 0.5f / Er3DataController.INSTANCE.getMm2px());
        float standardTBottom = mBase + (Er3DataController.INSTANCE.getAmpVal() * 0.5f / Er3DataController.INSTANCE.getMm2px());

        canvas.drawLine(chartStartX+10, standardYTop, chartStartX+10, standardTBottom, linePaint);

        String rulerStr =  "1mV";
        canvas.drawText(rulerStr, chartStartX+15, standardTBottom+20, mTextPaint);
    }

    private void drawWave(Canvas canvas) {
        Path p = new Path();
        p.moveTo(0, mBase);
        for (int i=0; i<maxIndex; i++) {
            if (i == Er3DataController.INSTANCE.getIndex() && i < maxIndex - 5) {
                float y = (mBase - (Er3DataController.INSTANCE.getAmpVal() * dataSrc[i+4] / Er3DataController.INSTANCE.getMm2px()));
                float x = (float) (i+4) / 5 / Er3DataController.INSTANCE.getMm2px() / 2;
                p.moveTo(x, y);
                i += 4;
            } else {
                float y1 = mBase - (Er3DataController.INSTANCE.getAmpVal() * dataSrc[i] / Er3DataController.INSTANCE.getMm2px());
                float x1 = (float) i / 5 / Er3DataController.INSTANCE.getMm2px() / 2;
                p.lineTo(x1, y1);
            }
        }
        canvas.drawPath(p, wPaint);
    }

    public void clear() {
        this.invalidate();
    }

    private int getColor(int resource_id) {
        return getResources().getColor(resource_id);
    }
}
