package com.commonfriend.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.commonfriend.R
import com.commonfriend.databinding.RawQuestionBankItemsBinding
import com.commonfriend.databinding.RawSuggestionDialogBinding
import com.commonfriend.models.FilterModel
import com.commonfriend.utils.visibleIf

class FilterAdapter(var context : Context,var clickListener: OnClickListener) : RecyclerView.Adapter<FilterAdapter.ViewHolder>() {

    var objList: ArrayList<FilterModel> = ArrayList()

    class ViewHolder(var binding: RawQuestionBankItemsBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            RawQuestionBankItemsBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ),
                parent, false
            )
        )
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.binding.txtTitle.text = objList[position].name
        holder.binding.llMain.background =
            context.resources.getDrawable(
                if (objList[position].id == "3")
                    R.drawable.dr_bg_blue_circle
                else
                    if (objList[position].isSelected == 1) R.drawable.dr_bg_brown_circle else R.drawable.dr_bg_grey_stroke_circle)

        holder.binding.imgAdd.visibleIf(objList[position].id == "3")
        holder.binding.txtTitle.setTextColor(ContextCompat.getColor(context,
            if (objList[position].id == "3") R.color.color_white else if (objList[position].isSelected == 1) R.color.color_white else R.color.color_grey))

        holder.binding.llMain.tag = position
        holder.binding.llMain.setOnClickListener(clickListener)

    }

    override fun getItemCount(): Int {
        return objList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addData(mObjList: ArrayList<FilterModel>) {
        objList = ArrayList()
        objList.addAll(mObjList)
        this.notifyDataSetChanged()
    }
}