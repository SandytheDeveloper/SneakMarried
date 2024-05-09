package com.commonfriend.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.commonfriend.R
import com.commonfriend.databinding.RawSelectUserItemBinding
import com.commonfriend.models.ProfileModel
import com.commonfriend.utils.Util

class SelectUsersAdapter(var context: Context, var clickListener: OnClickListener) :
    RecyclerView.Adapter<SelectUsersAdapter.ViewHolder>() {

    var objList: ArrayList<ProfileModel> = ArrayList()

    class ViewHolder(var binding: RawSelectUserItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            RawSelectUserItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return objList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding)
        {
            with(objList[position]) {

                receiverBtn.setCardBackgroundColor(ContextCompat.getColor(context,if (buttonType) R.color.color_blue else R.color.color_white))
                txtName.setTextColor(ContextCompat.getColor(context,if (buttonType) R.color.color_black else R.color.color_blue))

                txtName.text = name

                if (profilePic.isNotEmpty()) {
                    imgBtnProfile.setImageURI(profilePic)
                    txtUserInits.visibility = View.INVISIBLE
                } else {
                    txtUserInits.visibility = View.VISIBLE
                    txtUserInits.text = Util.getNameInitials(objList[position].name)
                }
            }
            receiverBtn.tag = position
            receiverBtn.setOnClickListener(clickListener)

        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addData(mObjList: ArrayList<ProfileModel>) {
        objList = ArrayList()
        objList.addAll(mObjList)
        this.notifyDataSetChanged()
    }
}