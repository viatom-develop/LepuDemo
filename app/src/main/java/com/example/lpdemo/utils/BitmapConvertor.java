package com.example.lpdemo.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import com.lepu.blepro.ext.lpbp2w.UserInfo;

public class BitmapConvertor {

    private Context mContext;
    private int mWidth;
    private int mHeight;
    private int mDataWidth;
    private byte[] mDataArray;
    private byte[] mRawBitmapData;

    public BitmapConvertor(Context context) {
        mContext = context;
    }

    public Bitmap generateBitmapSource(String text){
        Typeface typeface = Typeface.createFromAsset(mContext.getAssets(),"SOURCEHANSERIFCN-REGULAR.OTF");
        TextPaint textPaint = new TextPaint();
        textPaint.setTextSize(16);
        textPaint.setTypeface(typeface);
        textPaint.setColor(Color.WHITE);
        textPaint.setAntiAlias(false);//android12 Paint()构造方法中默认开启了抗锯齿，需要关闭，否则文字会变粗
        int width = (int) Math.ceil(textPaint.measureText(text));
        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        //textSize为16的汉字，其他字体宽16px，高22px, 但是由于思源字体问题，高度远高于22，这里写死为22，drawText时，截取22px
        Bitmap bitmap = Bitmap.createBitmap(width,22, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        //fontMetrics.ascent + 3,此处的
        canvas.drawText(text,0,Math.abs(fontMetrics.ascent + 1), textPaint);
        return bitmap;
    }

    public byte[] convertBitmapSync(Bitmap inputBitmap){

        mWidth = inputBitmap.getWidth();
        mHeight = inputBitmap.getHeight();
        int i = mWidth % 8;
        if (i == 0) {
            mDataWidth = mWidth;
        } else {
            //宽度必须是8的倍数,不足8位，扩充，后面扩充的位全部填充0
            mDataWidth = mWidth + (8 - i);
        }
        mDataArray = new byte[(mDataWidth * mHeight)];
        mRawBitmapData = new byte[(mDataWidth * mHeight) / 8];

        convertArgbToGrayscale(inputBitmap, mWidth, mHeight);
        createRawMonochromeData();
        return mRawBitmapData;

    }

    private void convertArgbToGrayscale(Bitmap bmpOriginal, int width, int height) {
        int pixel;
        int k = 0;
        int B=0,G=0,R=0;
        try {
            for (int x = 0; x < height; x++) {
                for (int y = 0; y < width; y++, k++) {
                    // get one pixel color
                    pixel = bmpOriginal.getPixel(y, x);

                    // retrieve color of all channels
                    R = Color.red(pixel);
                    G = Color.green(pixel);
                    B = Color.blue(pixel);
                    // take conversion up to one single value by calculating pixel intensity.
                    R = G = B = (int)(0.299 * R + 0.587 * G + 0.114 * B);
                    // set new pixel color to output bitmap
                    if (R < 128) {
                        mDataArray[k] = 0;
                    } else {
                        mDataArray[k] = 1;
                    }
                }
                if (mDataWidth > width) {
                    for (int p=width; p<mDataWidth; p++,k++){
                        mDataArray[k] = 1;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createRawMonochromeData() {
        int length = 0;
        for (int i = 0; i < mDataArray.length; i = i + 8) {
            byte first = mDataArray[i];
            for (int j = 1; j < 8; j++) {
                byte second = (byte) ((first << 1) | mDataArray[i + j]);
                first = second;
            }
            mRawBitmapData[length] = first;
            length++;
        }
    }

    public UserInfo.Icon createIcon(String name) {
        UserInfo.Icon icon = new UserInfo().new Icon();
        Bitmap inputBitmap = generateBitmapSource(name);
        icon.setWidth(inputBitmap.getWidth());
        icon.setHeight(inputBitmap.getHeight());
        Bitmap resizedBitmap = Bitmap.createBitmap(inputBitmap, 0, 0, inputBitmap.getWidth(), inputBitmap.getHeight());
        icon.setIcon(convertBitmapSync(resizedBitmap));
        return icon;
    }

}
