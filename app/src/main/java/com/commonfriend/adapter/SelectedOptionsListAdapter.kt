package com.commonfriend.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.commonfriend.databinding.RawSuggestedLocationBinding
import com.commonfriend.models.CheckListModel

class SelectedOptionsListAdapter(
    var context: Context,
    var clickListener: View.OnClickListener
) :
    RecyclerView.Adapter<SelectedOptionsListAdapter.ViewHolder>() {

    var objList: ArrayList<CheckListModel> = ArrayList()

    class ViewHolder(var binding: RawSuggestedLocationBinding) :
        RecyclerView.ViewHolder(binding.root)


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            RawSuggestedLocationBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )
        )

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.txtSuggestionName.text = objList[position].name
        holder.binding.btnClose.tag = position
        holder.binding.btnClose.setOnClickListener(clickListener)


    }

    override fun getItemCount(): Int {
        return objList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addData(mObjList: ArrayList<CheckListModel>) {
        objList = ArrayList()
        objList.addAll(mObjList)
        this.notifyDataSetChanged()
    }
}