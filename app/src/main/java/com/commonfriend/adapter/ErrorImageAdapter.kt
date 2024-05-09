package com.commonfriend.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.commonfriend.databinding.RawErrorImageItemBinding
import com.commonfriend.models.MultipleFaceModel

class ErrorImageAdapter(var context: Context, var clickListener: View.OnClickListener) :
    RecyclerView.Adapter<ErrorImageAdapter.ViewHolder>() {

    var objList: ArrayList<MultipleFaceModel> = ArrayList()

    class ViewHolder(var binding: RawErrorImageItemBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            RawErrorImageItemBinding.inflate(
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

        holder.binding.imgFaces.setImageURI(objList[position].photoUrl)
//        holder.binding.imgFaces.alpha = if (objList[position].isSelected == 1) 1f else 0.5f
        holder.binding.imgFaces.tag = position
        holder.binding.imgFaces.setOnClickListener(clickListener)

    }

    @SuppressLint("NotifyDataSetChanged")
    fun addData(mObjList: ArrayList<MultipleFaceModel>) {
        objList = ArrayList()
//        if(mObjList.isNotEmpty())
//            mObjList[0].isSelected=1
        objList.addAll(mObjList)

        this.notifyDataSetChanged()
    }
}