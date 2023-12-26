package com.example.lpdemo.utils;

/**
 * @author chenyongfeng
 */
public class DataController {

    public static int index = 0;

    public static int[] amp = {5, 10 ,20};
    public static int ampKey = 0;

    public static int nWave = 1; // BP 250HZ 2, DuoEK 125HZ 1, ER2-S信心相联 定制版本 500HZ 4
    public static int maxIndex;
    public static float mm2px;

    /**
     * received from device
     */
    public static float[] dataRec = new float[0];

    public static float[] feed(float[] src, float[] fs) {
        if (fs == null || fs.length == 0) {
            fs = new float[5];
        }

        if (src == null || src.length == 0) {
            src = new float[maxIndex];
        }

       for (int i = 0; i < fs.length; i++) {
           int tempIndex = (index + i) % src.length;
           src[tempIndex] = fs[i];
       }

        index = (index + fs.length) % src.length;

        return src;
    }

    synchronized public static void receive(float[] fs) {
        if (fs == null || fs.length == 0) {
            return;
        }

        float[] temp = new float[dataRec.length + fs.length];
        System.arraycopy(dataRec, 0, temp, 0, dataRec.length);
        System.arraycopy(fs, 0, temp, dataRec.length, fs.length);

        dataRec = temp;

    }

    synchronized public static float[] draw(int n) {
        if (n == 0 || n > dataRec.length) {
            return null;
        }

        float[] res = new float[n];
        float[] temp = new float[dataRec.length - n];
        System.arraycopy(dataRec, 0, res, 0, n);
        System.arraycopy(dataRec, n, temp, 0, dataRec.length-n);

        dataRec = temp;

        return res;
    }

    synchronized public static void clear() {
        nWave = 1;
        index = 0;
        dataRec = new float[0];
    }

}
