package com.example.lpdemo.utils

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.example.lpdemo.R

class EcgAdapter(layoutResId: Int, data: MutableList<EcgData>?) : BaseQuickAdapter<EcgData, BaseViewHolder>(layoutResId, data) {

    override fun convert(holder: BaseViewHolder, item: EcgData) {
        holder.setText(R.id.name, "${item.fileName} duration : ${item.duration} s")
    }
}