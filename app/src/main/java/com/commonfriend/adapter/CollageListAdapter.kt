package com.commonfriend.adapter


import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.commonfriend.R
import com.commonfriend.databinding.RawCollageDialogBinding
import com.commonfriend.models.GeneralModel
import com.commonfriend.template.FiveTemplateActivity
import com.commonfriend.utils.Util
import java.util.*


class CollageListAdapter(var context: Context, var clickListener: View.OnClickListener) :
    RecyclerView.Adapter<CollageListAdapter.ViewHolder>(){

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

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.txtLocation.text = objList[position].name

        holder.binding.imgLocation.setBackgroundResource(
            if ((objList[position].isSelected == 1)
            ) R.drawable.dr_ic_fill_black_circle else R.drawable.dr_ic_unfill_black_circle
        )

        holder.binding.txtLocation.setBackgroundResource(
            if ((objList[position].isSelected == 1)
            ) R.drawable.dr_collage_background_circle else com.google.android.material.R.color.mtrl_btn_transparent_bg_color
        )


        holder.binding.txtLocation.setTextColor(
            if (objList[position].isSelected == 1) ContextCompat.getColor(
                context,
                R.color.color_black
            ) else ContextCompat.getColor(context, R.color.color_blue)
        )


        holder.binding.rlMain.tag = position
        holder.binding.rlMain.setOnClickListener(clickListener)

    }

    override fun getItemCount(): Int {


        if (objList.size < 1) {
            (context as FiveTemplateActivity).showNoDataString(true)
        } else {
            (context as FiveTemplateActivity).showNoDataString(false)

        }
        return objList.size
    }


    @SuppressLint("NotifyDataSetChanged")
    fun addData(mObjList: ArrayList<GeneralModel>, isNew: Boolean) {
        Util.print("===================== DATA :: " + isNew +"::"+objList.size)
        if (isNew) {
            objList = ArrayList()
        }
        objList.addAll(mObjList)
        this.notifyDataSetChanged()

    }

}