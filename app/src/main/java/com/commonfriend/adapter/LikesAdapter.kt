package com.commonfriend.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.commonfriend.databinding.RawLikedListBinding
import com.commonfriend.models.SuggestionModel
import com.commonfriend.utils.Util

class LikesAdapter(var clickListener: OnClickListener) : RecyclerView.Adapter<LikesAdapter.ViewHolder>() {

    var objList: ArrayList<SuggestionModel> = ArrayList()

    class ViewHolder(var binding: RawLikedListBinding) : RecyclerView.ViewHolder(binding.root) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            RawLikedListBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ),
                parent, false
            )
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        if (objList[position].profilePic.isNotEmpty()) {
            holder.binding.imgProfile.setImageURI(objList[position].profilePic)
            holder.binding.txtUserInits.visibility = View.GONE
        } else {
            holder.binding.txtUserInits.visibility = View.VISIBLE
            holder.binding.txtUserInits.text = Util.getNameInitials(objList[position].name)
        }

        holder.binding.txtName.text = objList[position].name
        holder.binding.rlCaption.tag =position
        holder.binding.rlCaption.setOnClickListener(clickListener)
    }

    override fun getItemCount(): Int {
        return objList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addData(mObjList: ArrayList<SuggestionModel>) {
        objList = ArrayList()
        objList.addAll(mObjList)
        this.notifyDataSetChanged()
    }
}