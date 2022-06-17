package com.example.lpdemo.utils

import android.content.Context
import android.content.SharedPreferences
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.example.lpdemo.R
import com.example.lpdemo.UpdateActivity
import com.lepu.blepro.objs.Bluetooth

/**
 * author: wujuan
 * description:
 */
class DeviceAdapter(layoutResId: Int, data: MutableList<Bluetooth>?) : BaseQuickAdapter<Bluetooth, BaseViewHolder>(layoutResId, data) {

    override fun convert(holder: BaseViewHolder, item: Bluetooth) {
        val settings = context.getSharedPreferences(UpdateActivity.PREFS_NAME, Context.MODE_PRIVATE)
        //获取一个键值为"username"的值，若Preference中不存在，就用后面的值作为返回值
        val password = settings.getString(item.macAddr,"")

        holder.setText(R.id.name, "MAC:  "+ item.macAddr+"         $password")
    }
}