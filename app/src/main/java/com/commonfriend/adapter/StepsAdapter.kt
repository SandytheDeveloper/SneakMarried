package com.commonfriend.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.commonfriend.R
import com.commonfriend.databinding.RawStepsItemBinding
import com.commonfriend.models.CategoryModel
import com.commonfriend.utils.capitalizedFirstLetter
import com.commonfriend.utils.visibleIf




class StepsAdapter(
    var context: Context,
    var clickListener: View.OnClickListener,
) : RecyclerView.Adapter<StepsAdapter.ViewHolder>() {


    var objList: ArrayList<CategoryModel> = ArrayList()

    class ViewHolder(var binding: RawStepsItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            RawStepsItemBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ),
                parent, false
            )
        )
    }




    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        with(holder.binding)
        {
            with(objList[position]) {

                imgCompleted.setImageResource(
                    if (currentSelected) R.drawable.dr_ic_fill_black_circle else R.drawable.dr_ic_unfill_black_circle)

                imgEdit.visibleIf(isCompleted == 1)

                llMain.alpha = if(isCompleted==1 || currentSelected) 1F else 0.5F

                txtName.text = categoryName.capitalizedFirstLetter()
                txtInfo.text = infoMessage

                llMain.tag = position
                if (isCompleted == 1) {
                    llMain.setOnClickListener(clickListener)
                }
            }
        }


    }




    override fun getItemCount(): Int {
        return objList.size
    }




    @SuppressLint("NotifyDataSetChanged")
    fun addData(mObjList: ArrayList<CategoryModel>) {
        objList = ArrayList()
        objList.addAll(mObjList)
        this.notifyDataSetChanged()
    }
}







