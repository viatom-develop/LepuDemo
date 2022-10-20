package com.example.lpdemo.utils;

import java.util.Arrays;

public class EcgData {

    private long startTime;  // 时间戳s
    private int duration;
    private String fileName;
    private byte[] data;
    private short[] shortData;

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public short[] getShortData() {
        return shortData;
    }

    public void setShortData(short[] shortData) {
        this.shortData = shortData;
    }

    @Override
    public String toString() {
        return "EcgData{" +
                "startTime=" + startTime +
                ", duration=" + duration +
                ", fileName='" + fileName + '\'' +
                ", data=" + Arrays.toString(data) +
                ", shortData=" + Arrays.toString(shortData) +
                '}';
    }
}
