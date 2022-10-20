package com.example.lpdemo.utils

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.example.lpdemo.R
import com.lepu.blepro.objs.Bluetooth

class DeviceAdapter(layoutResId: Int, data: MutableList<Bluetooth>?) : BaseQuickAdapter<Bluetooth, BaseViewHolder>(layoutResId, data) {

    override fun convert(holder: BaseViewHolder, item: Bluetooth) {
        holder.setText(R.id.name, item.name + "  " + item.macAddr)
    }
}