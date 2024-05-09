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
import com.commonfriend.template.FourTemplateActivity

class FourTemplateAdapter(
    var context: Context,
    var clickListener: View.OnClickListener,
    var tag: Int = 1
) :
    RecyclerView.Adapter<FourTemplateAdapter.ViewHolder>() {

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
        with(holder.binding)
        {
            with(objList[position])
            {
                txtLocation.text = name
                imgLocation.isSelected =(isSelected == 1)
//                imgLocation.setBackgroundResource(
//                    if ((this.isSelected == 1)) R.drawable.dr_ic_fill_black_circle else R.drawable.dr_ic_unfill_black_circle
//                )

                // bottomsheet dialog box visibility and click event
                if (context is FourTemplateActivity) {
                    (context as FourTemplateActivity).showButton(objList.any { it.isSelected == 1 })
                }
//                else if (context is TwentyOneTemplateActivity) {
//                    (context as TwentyOneTemplateActivity).showButton(objList.any { it.isSelected == 1 })
//                }


                rlMain.tag = position
                rlMain.setTag(R.string.app_name,tag)
                rlMain.setOnClickListener(clickListener)

            }
        }

    }

    override fun getItemCount(): Int {
        return objList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addData(mObjList: ArrayList<GeneralModel>, clearList : Boolean = true) {
        if (clearList)
            objList = ArrayList()
        objList.addAll(mObjList)

        if (context is FourTemplateActivity)
            (context as FourTemplateActivity).showNoDataString(objList.isEmpty())

//        else if (context is TwentyOneTemplateActivity)
//            (context as TwentyOneTemplateActivity).showNoDataString(objList.isEmpty())

        this.notifyDataSetChanged()
    }
}