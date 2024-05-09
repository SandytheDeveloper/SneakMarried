package com.commonfriend.adapter


import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.commonfriend.R
import com.commonfriend.databinding.RawPlanToSettleDialogBinding
import com.commonfriend.models.GeneralModel
import com.commonfriend.template.SixTemplateActivity


class PlanToSettleInDialogAdapter(var context: Context, var clickListener: View.OnClickListener) :
    RecyclerView.Adapter<PlanToSettleInDialogAdapter.ViewHolder>() {

    var objMainList: ArrayList<GeneralModel> = ArrayList()

    class ViewHolder(var binding: RawPlanToSettleDialogBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            RawPlanToSettleDialogBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )
        )
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.txtPlanToSettle.text = objMainList[position].name
        holder.binding.txtPlanToSettle.setBackgroundResource(
            if (objMainList[position].isSelected == 1) R.drawable.dr_bg_white else com.google.android.material.R.drawable.m3_tabs_transparent_background
        )

        holder.binding.txtPlanToSettle.setTextColor(
            if (objMainList[position].isSelected == 1) ContextCompat.getColor(
                context,
                R.color.color_black
            ) else
                ContextCompat.getColor(context, R.color.color_blue)
        )

        (context as SixTemplateActivity).showButton(objMainList.filter { it.isSelected == 1 }.size>2)

        holder.binding.rlMain.tag = position
        holder.binding.rlMain.setOnClickListener(clickListener)
    }

    override fun getItemCount(): Int {

        return objMainList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addData(mObjList: ArrayList<GeneralModel>) {
        objMainList = ArrayList()
        objMainList.addAll(mObjList)
        this.notifyDataSetChanged()

    }
}