package com.example.lpdemo.utils

import android.util.Log

/**
 * for 8 channel ecg data
 * 1    =>     V6
 * 2    =>     I
 * 3    =>     II
 * 4    =>     V1
 * 5    =>     V2
 * 6    =>     V3
 * 7    =>     V4
 * 8    =>     V5
 * 9    =>     III = II-I
 * 10   =>     aVR = - (I+II)/2
 * 11   =>     aVL = I - II/2
 * 12   =>     aVF = II - I/2
 */

/**
 * for 4 channel ecg data
 * 1    =>     呼吸
 * 2    =>     I
 * 3    =>     II
 * 4    =>     V1
 * 5 6 7 8  =>  null
 * 9    =>     III = II-I
 * 10   =>     aVR = - (I+II)/2
 * 11   =>     aVL = I - II/2
 * 12   =>     aVF = II - I/2
 */
object Er3DataController {

    var maxIndex: Int = 0
    var mm2px : Float = 0f
    var index = 0

    var amp = intArrayOf(5, 10, 20)
    var ampKey = 0

    fun getAmpVal(): Int {
        return amp[ampKey]
    }

    var isLAOff = false  // I
    var isLLOff = false  // II

    /**
     * 12 channel data
     */
    var src1 = FloatArray(0)    // V6
    var src2 = FloatArray(0)    // I
    var src3 = FloatArray(0)    // II
    var src4 = FloatArray(0)    // V1
    var src5 = FloatArray(0)    // V2
    var src6 = FloatArray(0)    // V3
    var src7 = FloatArray(0)    // V4
    var src8 = FloatArray(0)    // V5
    var src9 = FloatArray(0)    // III = II-I
    var src10 = FloatArray(0)   // aVR = - (I+II)/2
    var src11 = FloatArray(0)   // aVL = I - II/2
    var src12 = FloatArray(0)   // aVF = II - I/2

    /**
     * data received from device
     * may contain more that 1 channel ecg data
     */
    var dataRec: FloatArray = FloatArray(0)

    /**
     * receive ecg data from device
     * default 8 channel
     */
    fun receive(fs: FloatArray, isLAOff: Boolean, isLLOff: Boolean) {
        if (fs.isEmpty()) {
            return
        }

        val tmp = FloatArray(dataRec.size + fs.size)
        System.arraycopy(dataRec, 0 , tmp, 0, dataRec.size)
        System.arraycopy(fs, 0, tmp, dataRec.size, fs.size)

        dataRec = tmp
        this.isLAOff = isLAOff
        this.isLLOff = isLLOff
    }

    /**
     * feed 8 channel data
     * calculate to 12 channel ecg data
     */
    private fun feed(fs: FloatArray) {
        if (fs.isEmpty())
            return

        if (src1.isEmpty()) {
            src1 = FloatArray(maxIndex) { 0f }
            src2 = FloatArray(maxIndex) { 0f }
            src3 = FloatArray(maxIndex) { 0f }
            src4 = FloatArray(maxIndex) { 0f }
            src5 = FloatArray(maxIndex) { 0f }
            src6 = FloatArray(maxIndex) { 0f }
            src7 = FloatArray(maxIndex) { 0f }
            src8 = FloatArray(maxIndex) { 0f }
            src9 = FloatArray(maxIndex) { 0f }
            src10 = FloatArray(maxIndex) { 0f }
            src11 = FloatArray(maxIndex) { 0f }
            src12 = FloatArray(maxIndex) { 0f }
        }

        for (i in fs.indices step 8) {
            // 是否与设备UI显示对齐
//            if (isLAOff || isLLOff) {
//                src1[index] = 0f
//                src2[index] = fs[i+1]
//                src3[index] = fs[i+2]
//                src4[index] = 0f
//                src5[index] = 0f
//                src6[index] = 0f
//                src7[index] = 0f
//                src8[index] = 0f
//                src9[index] = 0f
//                src10[index] = 0f
//                src11[index] = 0f
//                src12[index] = 0f
//            } else {
                src1[index] = fs[i]
                src2[index] = fs[i+1]
                src3[index] = fs[i+2]
                src4[index] = fs[i+3]
                src5[index] = fs[i+4]
                src6[index] = fs[i+5]
                src7[index] = fs[i+6]
                src8[index] = fs[i+7]
                src9[index] = fs[i+2] - fs[i+1]  // III = II-I
                src10[index] = -(fs[i+2] + fs[i+1])/2  // aVR = - (I+II)/2
                src11[index] = fs[i+1] - fs[i+2]/2  // aVL = I - II/2
                src12[index] = fs[i+2] - fs[i+1]/2  // aVF = II - I/2
//            }

            index++
            index %= maxIndex
        }
    }

    /**
     * draw ecg data size = n
     * the received data is 8 channel data
     * normally we use n = 5
     */
    fun draw(n: Int = 5) {
        if (n == 0)
            return
        /**
         * have no enough ecg data
         * add n zero data to data src
         */
        Log.d("Er3Test", "n*8 ${n*8}")
        Log.d("Er3Test", "dataRec.size ${dataRec.size}")
        val fs: FloatArray
        if (n*8 > dataRec.size) {
            fs = FloatArray(n*8)
        } else {
            fs = dataRec.copyOfRange(0, 8*n)
            dataRec = dataRec.copyOfRange(8*n, dataRec.size)
        }
        feed(fs)
    }

    fun clear() {
        index = 0
        dataRec = FloatArray(0)
    }

}