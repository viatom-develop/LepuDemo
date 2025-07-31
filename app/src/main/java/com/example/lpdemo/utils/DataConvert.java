package com.example.lpdemo.utils;

public class DataConvert {

    public static String getEcgTimeStr(int ecgTime) {
        int recordHour = ecgTime / 3600;
        int recordMinute = (ecgTime % 3600) / 60;
        int recordSecond = (ecgTime % 3600) % 60;

        String recordHourStr = String.valueOf(recordHour);
        if (recordHour < 10) {
            recordHourStr = "0".concat(recordHourStr);
        }
        String recordMinuteStr = String.valueOf(recordMinute);
        if (recordMinute < 10) {
            recordMinuteStr = "0".concat(recordMinuteStr);
        }
        String recordSecondStr = String.valueOf(recordSecond);
        if (recordSecond < 10) {

            recordSecondStr = "0".concat(recordSecondStr);
        }

        String recordTime = "";
        recordTime = recordHourStr.concat(":").concat(recordMinuteStr).concat(":").concat(recordSecondStr);

        return recordTime;
    }

}
