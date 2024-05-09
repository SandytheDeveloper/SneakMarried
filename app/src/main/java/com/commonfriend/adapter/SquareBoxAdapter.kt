package com.commonfriend.adapter


import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.commonfriend.R
import com.commonfriend.databinding.RawCultureDialogBinding
import com.commonfriend.models.GeneralModel


class SquareBoxAdapter(var context: Context, var clickListener: View.OnClickListener) :
    RecyclerView.Adapter<SquareBoxAdapter.ViewHolder>(){

    var objList: ArrayList<GeneralModel> = ArrayList()

    class ViewHolder(var binding: RawCultureDialogBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            RawCultureDialogBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )
        )

    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.txtCulture.text = objList[position].name

        holder.binding.rlMain.tag = position
        holder.binding.rlMain.setOnClickListener(clickListener)


        holder.binding.checkbox.setBackgroundResource(
            if (objList[position].isSelected == 1)
                R.drawable.dr_ic_sqaure_fill
            else
                R.drawable.dr_ic_sqaure_unfill
        )
        //(context as FifteenTemplateActivity).showButton(objList.any { it.isSelected == 1 })
    }

    override fun getItemCount(): Int {


       /* if (objList.size < 1) {
            (context as FifteenTemplateActivity).showNoDataFoundString(true)

        } else {
            (context as FifteenTemplateActivity).showNoDataFoundString(false)

        }
*/        return objList.size
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