package com.commonfriend.adapter


import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.commonfriend.R
import com.commonfriend.databinding.RawCollageDialogBinding
import com.commonfriend.models.GeneralModel
import com.commonfriend.template.SecondTemplateActivity
import java.util.*


class LocationListAdapter(
    var context: Context, var clickListener: View.OnClickListener
) :
    RecyclerView.Adapter<LocationListAdapter.ViewHolder>() {

    var objList: ArrayList<GeneralModel> = ArrayList()

    class ViewHolder(var binding: RawCollageDialogBinding) : RecyclerView.ViewHolder(binding.root)


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder {
        return ViewHolder(
            RawCollageDialogBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.txtLocation.text = objList[position].name

        holder.binding.imgLocation.setBackgroundResource(if (objList[position].isSelected == 1) R.drawable.dr_ic_fill_black_circle else R.drawable.dr_ic_unfill_black_circle)



        (context as SecondTemplateActivity).showButton(objList.any { it.isSelected == 1 })

        holder.binding.rlMain.tag = position
        holder.binding.rlMain.setOnClickListener(clickListener)


    }


    override fun getItemCount(): Int {
            (context as SecondTemplateActivity).showNoDataFoundString(objList.size < 1)
        return objList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addData(mObjList: ArrayList<GeneralModel>, isNew: Boolean) {
        if (isNew) {
            objList = ArrayList()
        }
        objList.addAll(mObjList)
        this.notifyDataSetChanged()

    }


}