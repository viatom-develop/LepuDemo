package com.example.lpdemo.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import com.lepu.blepro.objs.Bluetooth;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class WaveEcgView extends View {
    public static final short NULL_VALUE = Short.MAX_VALUE;
    // Starting position of drawing
    private final float chartStartX = 0;
    // Sampling step
    private final int sampleStep = 1;
    //view height
    private float height;
    private long startTime;
    // line number of wave
    private int lineNum;
    // the distance(px) between two point
    private float xDis;
    // the distance(px) between two lines
    private float lineDis;
    // the padding of line
    private float linePadding;
    // the total length(px) of per line
    private float chartLineLength;
    // lineDis + 2 * linePadding
    private float wholeLineDis;

    // About ECG Ruler
    private float rulerStandardWidth = 20;
    private float rulerZeroWidth = 13;
    private float standard1mV;
    private final double[] standardNmV = {0.5, 1.0, 2.0};
    private double rulerStandard;
    // The distance between ruler and chart

    // About drawing
    private float screenW;
    private short[] chartY;
    private int validValueLength;
    private Paint linePaint, textPaint, recPaint, axisPaint;

    private Paint bkg_paint_1;
    private Paint bkg_paint_2;
    private float mGrid1mmLength;
    private float mSpeed = 6.25f;
    private int mAxisIndex;

    private int currentZoomPosition = 0;
    private int startPoint = 0;
    public float preTouchY = 0;
    public boolean isTouching = false;
    public float touchY = 0;
    private float yOffSet;

    private HashSet<Float> hashSet = new HashSet<>();
    private Float temp = 0f; // 去重画线

    //point frequency 125Hz
    private int HZ;
    private float SECONDS_PER_LINE;

    private static final int ONE_PAGE_LINES = 4;

    public int POINTS_PER_LINE;
    public int ONE_PAGE_POINTS;
    public int PREPARED_DRAW_POINTS;
    private int model;

    public WaveEcgView(Context context, long startTime,
                       short[] Y, int validValueLength,
                       float ScreenW, float viewHeight,
                       int currentZoomPosition, int model) {
        super(context);
        this.model = model;
        mGrid1mmLength = (float) 25.4 / getResources().getDisplayMetrics().xdpi;
        SECONDS_PER_LINE = ScreenW / (1 / mGrid1mmLength) / mSpeed;

        if (model == Bluetooth.MODEL_ER1
                || model == Bluetooth.MODEL_ER1S
                || model == Bluetooth.MODEL_ER1_S
                || model == Bluetooth.MODEL_ER1_H
                || model == Bluetooth.MODEL_ER1_W
                || model == Bluetooth.MODEL_ER1_L
                || model == Bluetooth.MODEL_ER1_N
                || model == Bluetooth.MODEL_HHM1
                || model == Bluetooth.MODEL_DUOEK
                || model == Bluetooth.MODEL_HHM2
                || model == Bluetooth.MODEL_HHM3
                || model == Bluetooth.MODEL_ER2
                || model == Bluetooth.MODEL_ER2_S
                || model == Bluetooth.MODEL_LEPU_ER2
                || model == Bluetooth.MODEL_CM_TOUCH
                || model == Bluetooth.MODEL_LP_ER2) {
            standard1mV = (float) ((1.0035 * 1800) / (4096 * 178.74));
            HZ = 125;
        } else if (model == Bluetooth.MODEL_BP2
                || model == Bluetooth.MODEL_CMB590840
                || model == Bluetooth.MODEL_BP2W) {
            standard1mV = 0.003098f;
            HZ = 125;
        } else if (model == Bluetooth.MODEL_PULSEBITEX
                || model == Bluetooth.MODEL_HHM4
                || model == Bluetooth.MODEL_CHECKME_LE
                || model == Bluetooth.MODEL_CHECKME_LP
                || model == Bluetooth.MODEL_CHECKME) {
            standard1mV = 4033 / (32767 * 12 * 8f);
            HZ = 500;
        } else if (model == Bluetooth.MODEL_PC80B
                || model == Bluetooth.MODEL_PC80B_BLE
                || model == Bluetooth.MODEL_PC80B_BLE2) {
            standard1mV = 1 / 330f;
            HZ = 150;
        } else if (model == Bluetooth.MODEL_ER3
                || model == Bluetooth.MODEL_M12
                || model == Bluetooth.MODEL_LEPOD
                || model == Bluetooth.MODEL_LEPOD_PRO) {
            standard1mV = 0.00244140625f;
            HZ = 250;
        } else {
            standard1mV = (float) ((1.0035 * 1800) / (4096 * 178.74));
            HZ = 125;
        }
        POINTS_PER_LINE = (int) (SECONDS_PER_LINE * HZ);
        ONE_PAGE_POINTS = (int) (SECONDS_PER_LINE * ONE_PAGE_LINES * HZ);
        PREPARED_DRAW_POINTS = ONE_PAGE_POINTS + POINTS_PER_LINE;

        this.currentZoomPosition = currentZoomPosition;
        chartY = Y;

        this.height = viewHeight;
        if (chartY.length == 0) {
            return;
        }

        this.startTime = startTime;
        this.validValueLength = validValueLength;
        this.screenW = ScreenW;

        initPaint();
        InitFixParams();
        getMinAndMax();
    }

    /**
     * Initialize paints
     */
    public void initPaint() {
        axisPaint = new Paint();
        axisPaint.setAntiAlias(true);
        axisPaint.setStyle(Paint.Style.FILL);
        axisPaint.setStrokeWidth((float) 2.0);
        axisPaint.setColor(Color.parseColor("#f2f2f2"));

        linePaint = new Paint();
        linePaint.setAntiAlias(true);
        linePaint.setTextSize(15);
        linePaint.setStyle(Paint.Style.FILL);
        linePaint.setStrokeWidth((float) 1.5);
        linePaint.setColor(Color.parseColor("#4E596F"));

        textPaint = new Paint();
        textPaint.setTextSize(36);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setStrokeWidth((float) 1.5);
        textPaint.setColor(Color.parseColor("#999999"));

        recPaint = new Paint();
        recPaint.setAntiAlias(true);
        recPaint.setStyle(Paint.Style.STROKE);
        recPaint.setStrokeWidth(4);
        recPaint.setColor(Color.argb(255, 48, 100, 0));

        bkg_paint_1 = new Paint();
        bkg_paint_1.setColor(Color.parseColor("#4DFF0000"));
        bkg_paint_1.setStyle(Paint.Style.STROKE);
        bkg_paint_1.setStrokeWidth(2.0f);

        bkg_paint_2 = new Paint();
        bkg_paint_2.setColor(Color.parseColor("#1AFF0000"));
        bkg_paint_2.setStyle(Paint.Style.STROKE);
        bkg_paint_2.setStrokeWidth(1.0f);
    }

    /**
     * Initialize parameters
     */
    public void InitFixParams() {
        chartLineLength = screenW;

        xDis = (screenW / (SECONDS_PER_LINE * HZ - 1)) * sampleStep;
        rulerZeroWidth = 1 / mGrid1mmLength;
        rulerStandardWidth = rulerZeroWidth * 3;

        linePadding = 2.0f;
        if (height == 0) {
            lineDis = (float) (screenW * 8 / (7.0 * 5.0));
            wholeLineDis = lineDis + 2 * linePadding;
        } else {
            wholeLineDis = (5 / mGrid1mmLength) * 5;
            lineDis = wholeLineDis - 2 * linePadding * ONE_PAGE_LINES;
        }
        lineNum = (int) Math.ceil(validValueLength * 1.0f / POINTS_PER_LINE);

        // 小于等于30s不需要画全屏
        Log.d("vvaa", "lineNum "+lineNum + " validValueLength "+validValueLength + " POINTS_PER_LINE "+POINTS_PER_LINE);
        height = wholeLineDis * lineNum;
        if (lineNum <= ONE_PAGE_LINES) {
            mAxisIndex = lineNum + 1;
        } else {
            mAxisIndex = ONE_PAGE_LINES + 1 + 1;
        }
    }


    /**
     * Get the maximum and minimum in ECG data
     * And select a ruler in the meantime
     */
    public void getMinAndMax() {
        rulerStandard = standardNmV[currentZoomPosition];
    }

    public int getCurrentZoomPosition() {
        return currentZoomPosition;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if (chartY == null || chartY.length == 0) {
            return;
        }
        drawBkg(canvas);
        drawAxis(canvas);
        drawPath(canvas, chartY);
    }

    public void drawBkg(Canvas canvas) {
        // 1mm x
        for (int i = 0; i < screenW/(1/ mGrid1mmLength) + 1; i++) {
            Path p = new Path();
            p.moveTo(i/ mGrid1mmLength, 0);
            p.lineTo(i/ mGrid1mmLength, height);
            canvas.drawPath(p, bkg_paint_2);
        }

        // 5mm x
        for (int i = 0; i < screenW/(5/ mGrid1mmLength) + 1; i++) {
            Path p = new Path();
            p.moveTo(i*5/ mGrid1mmLength, 0);
            p.lineTo(i*5/ mGrid1mmLength, height);
            canvas.drawPath(p, bkg_paint_1);
        }

        // 1mm y
        for (int i = 0; i < height/(1/mGrid1mmLength); i ++) {
            Path p = new Path();
            p.moveTo(0,  i*(1/mGrid1mmLength));
            p.lineTo(screenW,  i*(1/mGrid1mmLength));
            canvas.drawPath(p, bkg_paint_2);
        }
    }

    /**
     * Draw axes and ruler
     *
     * @param canvas
     */
    public void drawAxis(Canvas canvas) {
        hashSet.clear();
        for (int i = 0; i < mAxisIndex; i++) {

            for (int j=0; j<=i*5; j++) {
                temp = (wholeLineDis * i / (i*5) * j) + yOffSet;
                if (!hashSet.contains(temp)) {
                    hashSet.add(temp);
                    canvas.drawLine(chartStartX, temp, chartStartX
                            + chartLineLength, temp, bkg_paint_1);
                }
            }

            long time;
            time = (startPoint + POINTS_PER_LINE * (i-1)) / HZ + TimeUnit.MILLISECONDS.toSeconds(startTime);
            Date date = new Date(TimeUnit.SECONDS.toMillis(time));
            textPaint.setTextSize(50);
            linePaint.setTextSize(50);
            String timeStamp = new SimpleDateFormat("MM-dd HH:mm:ss", Locale.getDefault()).format(date);
            canvas.drawText(timeStamp, 50, wholeLineDis * i + yOffSet - 20, textPaint);
        }

        if (startPoint < POINTS_PER_LINE) {
            canvas.drawText(mSpeed+"mm/s", screenW - 280, wholeLineDis + yOffSet - 20, textPaint);
            drawRuler(canvas);
        }
    }

    private void drawRuler(Canvas canvas) {
        float zeroLineY = wholeLineDis / 5.0f * 3.0f + yOffSet;
        float standardLineY = wholeLineDis / 5.0f + yOffSet;

        canvas.drawLine(chartStartX, zeroLineY, chartStartX + rulerZeroWidth,
                zeroLineY, linePaint);
        // Draw first vertical line
        canvas.drawLine(chartStartX + rulerZeroWidth, zeroLineY, chartStartX
                + rulerZeroWidth, standardLineY, linePaint);
        // Draw 1mV line
        canvas.drawLine(chartStartX + rulerZeroWidth, standardLineY, chartStartX
                + rulerZeroWidth + rulerStandardWidth, standardLineY, linePaint);
        // Draw second vertical line
        canvas.drawLine(chartStartX + rulerZeroWidth + rulerStandardWidth,
                zeroLineY, chartStartX + rulerZeroWidth + rulerStandardWidth,
                standardLineY, linePaint);
        // Draw second zero line
        canvas.drawLine(chartStartX + rulerZeroWidth + rulerStandardWidth,
                zeroLineY, chartStartX + rulerZeroWidth * 2 + rulerStandardWidth,
                zeroLineY, linePaint);

        String rulerStr = "1mV";
        double ruleNum = 1 / rulerStandard;
        if (ruleNum > 0.5) {
            int ruleNumInt = (int) ruleNum;
            rulerStr = ruleNumInt + "mV";
        } else {
            rulerStr = ruleNum + "mV";
        }
        canvas.drawText(rulerStr, chartStartX + 5, standardLineY - 20, linePaint);
    }

    /**
     * Draw ECG Path
     *
     * @param canvas
     * @param Y
     */
    public void drawPath(Canvas canvas, short[] Y) {
        int line = 0;// The line drawing currently
        float preTempX = 0, preTempY = 0, preChartY = 0;

        //小于一页的数据取实际长度
        int length = Math.min(Math.min(PREPARED_DRAW_POINTS, validValueLength), validValueLength - startPoint);
        for (int i = 0, k = 0; i < length; i += sampleStep) {
            float tempX;
            tempX = chartStartX + k * xDis;
            int index = startPoint + i;
            if (index < 0 || index > Y.length-1) {
                break;
            }
            float yVal;
            if (model == Bluetooth.MODEL_PC80B
                || model == Bluetooth.MODEL_PC80B_BLE
                || model == Bluetooth.MODEL_PC80B_BLE2) {
                yVal = (Y[index] - 2048)*standard1mV;
            } else {
                yVal = (Y[index])*standard1mV;
            }
            float tempY = yOffSet + (float) (wholeLineDis / 5.0 * 3.0 + line - rulerStandard * (1/mGrid1mmLength*10) * (yVal));
            if (i == 0) { // First point
                preTempX = NULL_VALUE;
                preTempY = NULL_VALUE;
                preChartY = NULL_VALUE;
            }

            if (preTempX != NULL_VALUE && Y[index] != NULL_VALUE && preChartY != NULL_VALUE) {
                canvas.drawLine(preTempX, preTempY, tempX, tempY, linePaint);
            }
            preTempX = tempX;
            preTempY = tempY;
            preChartY = Y[index];
            k++;

            //If draw a line full, move to next line
            if (preTempX >= chartStartX + chartLineLength) {
                line += wholeLineDis;
                i--;
                preTempX = NULL_VALUE;
                preTempY = NULL_VALUE;
                preChartY = NULL_VALUE;
                k = 0;
            }
        }
    }

    public void setData(short[] Y) {
        chartY = Y;
        postInvalidate();
    }

    public void setCurrentZoomPosition(int zoomPosition) {
        rulerStandard = standardNmV[zoomPosition];
        postInvalidate();
    }

    public void setmSpeed(float mSpeed) {
        this.mSpeed = mSpeed;
        SECONDS_PER_LINE = screenW / (1 / mGrid1mmLength) / mSpeed;
        POINTS_PER_LINE = (int) (SECONDS_PER_LINE * HZ);
        ONE_PAGE_POINTS = (int) (SECONDS_PER_LINE * ONE_PAGE_LINES * HZ);
        PREPARED_DRAW_POINTS = ONE_PAGE_POINTS + POINTS_PER_LINE;
        InitFixParams();
        postInvalidate();
    }

    public float getmSpeed() {
        return mSpeed;
    }
    public int getHZ() {
        return HZ;
    }
    public double getRulerStandard() {
        return rulerStandard;
    }
    public float getStandard1mV() {
        return standard1mV;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            preTouchY = event.getY();
            isTouching = true;
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            isTouching = false;
        }
        if (isTouching) {
            float TempY = event.getY();
            touchY = TempY - preTouchY;
            preTouchY = TempY;
            if (startPoint == 0 && Math.abs(yOffSet) < touchY && touchY > 0) {
                return true;
            }
            //到达最后一分钟
            if (touchY < 0 && (validValueLength - startPoint) <= ONE_PAGE_POINTS) {
                return true;
            }
            if (yOffSet < 0 && touchY > 0 && (yOffSet + touchY > 0)) {
                if (startPoint > POINTS_PER_LINE) {
                    startPoint -= POINTS_PER_LINE;

                    if (yOffSet > 0) {
                        yOffSet = wholeLineDis - Math.abs(yOffSet);
                    } else {
                        yOffSet = -(wholeLineDis - Math.abs(yOffSet));
                    }
                }
            }
            yOffSet += touchY;
            float abs = Math.abs(yOffSet);
            if (abs > wholeLineDis) {
                abs %= wholeLineDis;
                if (yOffSet < 0) {
                    startPoint += (HZ * SECONDS_PER_LINE);
                } else {
                    startPoint -= (HZ * SECONDS_PER_LINE);
                }
                yOffSet = yOffSet > 0 ? (abs - wholeLineDis) : (-abs);
            }
            if (startPoint < 0) {
                startPoint = 0;
            }
            invalidate();
        }
        return true;
    }

}
