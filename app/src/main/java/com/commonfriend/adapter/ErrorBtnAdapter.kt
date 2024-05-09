package com.commonfriend.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.commonfriend.databinding.DrButtonItemBinding
import com.commonfriend.models.FaqModel

class ErrorBtnAdapter( var clickListener:View.OnClickListener
) : RecyclerView.Adapter<ErrorBtnAdapter.ViewHolder>() {

    var objList: ArrayList<FaqModel> = ArrayList()

    class ViewHolder(var binding: DrButtonItemBinding) : RecyclerView.ViewHolder(binding.root) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            DrButtonItemBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ),
                parent, false
            )
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        with(holder.binding) {
            btnAction.text = objList[position].name

            btnAction.tag = position
            btnAction.setOnClickListener(clickListener)
        }
    }

    override fun getItemCount(): Int {
        return objList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addData(mObjList: ArrayList<FaqModel>) {
        objList = ArrayList()
        objList.addAll(mObjList)
        this.notifyDataSetChanged()
    }
}