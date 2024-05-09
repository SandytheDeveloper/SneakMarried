package com.commonfriend.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.commonfriend.R
import com.commonfriend.databinding.RawChecklistBinding
import com.commonfriend.models.CheckListModel

class CheckListAdapter(
    var context: Context,
    var clickListener: View.OnClickListener,
    var clickType: Int,
    var viewType: Int = 1
) : RecyclerView.Adapter<CheckListAdapter.ViewHolder>() {

    var objList: ArrayList<CheckListModel> = ArrayList()
    var isEnabled: String = "1"

    class ViewHolder(var binding: RawChecklistBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ):ViewHolder {
        return ViewHolder(
            RawChecklistBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )
        )

    }

    @SuppressLint("ResourceAsColor", "LongLogTag", "NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        when (viewType) {
            2 -> {
                holder.binding.view2.visibility = View.VISIBLE
                holder.binding.txtSelectedSurname.text = objList[position].name

            }
            3 -> {
                holder.binding.view3.visibility = View.VISIBLE
                holder.binding.txtSuggestedSurname.text = objList[position].name

            }
            4 -> {
                holder.binding.view1.visibility = View.VISIBLE
                holder.binding.txtName.text = objList[position].name.capitalize()
//                holder.binding.txtName.setTextColor(ContextCompat.getColor(context, if(isEnabled=="1") R.color.color_black else R.color.color_grey))
                holder.binding.checkbox.setImageResource(if(objList[position].isSelected == 1) R.drawable.dr_ic_fill_black_circle else R.drawable.dr_ic_unfill_black_circle)
//                holder.binding.checkbox.setColorFilter(ContextCompat.getColor(context,if(isEnabled=="1") R.color.color_black else R.color.color_grey))

        }else -> { // viewType = 1
                holder.binding.view1.visibility = View.VISIBLE
                holder.binding.txtName.text = objList[position].name.capitalize()
//                holder.binding.txtName.setTextColor(ContextCompat.getColor(context, if(isEnabled=="1") R.color.color_black else R.color.color_grey))
                holder.binding.checkbox.setImageResource(if(objList[position].isSelected == 1) R.drawable.dr_ic_sqaure_fill else R.drawable.dr_ic_sqaure_unfill)
//                holder.binding.checkbox.setColorFilter(ContextCompat.getColor(context,if(isEnabled=="1") R.color.color_black else R.color.color_grey))
            }
        }

        holder.binding.btnRemove.tag = position
        holder.binding.btnRemove.setTag(R.string.app_name, clickType)
        holder.binding.btnRemove.setOnClickListener(clickListener)

        holder.binding.rlMain.tag = position
        holder.binding.rlMain.setTag(R.string.app_name, clickType)
        holder.binding.rlMain.setOnClickListener(if(isEnabled=="1")clickListener else null)

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