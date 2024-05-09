package com.commonfriend.adapter


import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.commonfriend.*
import com.commonfriend.databinding.RowMenuListBinding
import com.commonfriend.fragment.MenuFragment
import com.commonfriend.models.GeneralModel
import com.commonfriend.utils.Pref
import com.commonfriend.utils.Util
import com.commonfriend.utils.visibleIf


class NavigationMenuAdapter
    (var context: Context, var clickListener: View.OnClickListener) :
    RecyclerView.Adapter<NavigationMenuAdapter.ViewHolder>() {

    var objList: ArrayList<GeneralModel> = ArrayList()

    class ViewHolder(var binding: RowMenuListBinding) : RecyclerView.ViewHolder(binding.root)


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            RowMenuListBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )
        )

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding)
        {
            with(objList[position])
            {

                txtMenuName.text = this.name
                imgIcon.setImageResource( this.iconImage)

                toggleBtnChatIntroduction.visibleIf(id == "14")
                toggleBtnChatIntroduction.isChecked = if (id == "14") Pref.getBooleanValue(Pref.PREF_CHAT_INTRODUCTION,false) else false


                llMenu.tag = position
                llMenu.setOnClickListener(clickListener)

                toggleBtnChatIntroduction.tag = position
                toggleBtnChatIntroduction.setOnClickListener(clickListener)
            }
        }
    }


    override fun getItemCount(): Int {
        return objList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addData(mObjList: ArrayList<GeneralModel>) {
        objList = ArrayList()
        objList.addAll(mObjList)
        this.notifyDataSetChanged()

    }


}