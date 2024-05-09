package com.commonfriend.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.commonfriend.R
import com.commonfriend.databinding.RawReasonListBinding
import com.commonfriend.models.ReasonsModel

class ReasonsAdapter(
    var context: Context,
    var clickListener: View.OnClickListener,
) :   RecyclerView.Adapter<ReasonsAdapter.ViewHolder>()  {

    var objList: ArrayList<ReasonsModel> = ArrayList()

    class ViewHolder(var binding: RawReasonListBinding) : RecyclerView.ViewHolder(binding.root) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            RawReasonListBinding.inflate(
                LayoutInflater.from(
                    parent.context),
                parent, false))
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.binding.txtName.text = objList[position].reason
        holder.binding.checkbox.setImageResource(if(objList[position].isSelected == 1) R.drawable.dr_ic_sqaure_fill else R.drawable.dr_ic_sqaure_unfill)

        holder.binding.rlMain.tag = position
        holder.binding.rlMain.setOnClickListener(clickListener)

    }

    override fun getItemCount(): Int {
        return objList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addData(mObjList: ArrayList<ReasonsModel>) {
        objList = ArrayList()
        objList.addAll(mObjList)
        this.notifyDataSetChanged()
    }
}