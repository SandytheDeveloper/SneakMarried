package com.commonfriend.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.commonfriend.R
import com.commonfriend.databinding.RowEditDetailsBinding
import com.commonfriend.models.EditDetailsModel

class EditDetailsAdapter(var context: Context, var clickListener: View.OnClickListener) :
    RecyclerView.Adapter<EditDetailsAdapter.ViewHolder>() {
    var objList: ArrayList<EditDetailsModel> = ArrayList()

    class ViewHolder(var binding: RowEditDetailsBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            RowEditDetailsBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ),
                parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return objList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.binding.btnEdit.text = objList[position].editItem

        holder.binding.btnEdit.tag = position
        holder.binding.btnEdit.setOnClickListener(clickListener)

        holder.binding.image.setImageResource(
            if (objList[position].isSelected == 1)
                R.drawable.ic_yellow_selected
            else
                R.drawable.ic_purple_unfilled
        )

        holder.binding.btnEdit.setBackgroundDrawable(
            ContextCompat.getDrawable(context,
            if (objList[position].isSelected == 1) R.drawable.dr_blue_circle else R.drawable.dr_purple_circle
        ))

        holder.binding.btnEdit.setTextColor(
            ContextCompat.getColor(context,
            if (objList[position].isSelected == 1) R.color.color_black else R.color.color_blue
        ))


    }

    @SuppressLint("NotifyDataSetChanged")
    fun addData(mObjList: ArrayList<EditDetailsModel>) {
        objList = ArrayList()
        objList.addAll(mObjList)
        this.notifyDataSetChanged()
    }
}