package com.commonfriend.adapter


import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.commonfriend.R
import com.commonfriend.databinding.RawCollageDialogBinding
import com.commonfriend.models.GeneralModel
import com.commonfriend.template.ThirdTemplateActivity
import com.commonfriend.template.TwentyTwoTemplateActivity


class InnerAdapterAdapter(
    var context: Context,
    var clickListener: DegreeAdapterItemClickListener,
    var outerPos:Int,
    var objList: ArrayList<GeneralModel> = ArrayList()) :
    RecyclerView.Adapter<InnerAdapterAdapter.ViewHolder>() {


    inner class ViewHolder(var binding: RawCollageDialogBinding) : RecyclerView.ViewHolder(binding.root)
    {
        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val innerData = objList[position]
                    clickListener.onInnerItemClick(outerPos, position, innerData)
                }
            }
        }
    }
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
        with(holder.binding)
        {
            with(objList[position])
            {
                txtLocation.text = this.name
                imgLocation.setBackgroundResource(
                    if ((this.isSelected == 1)
                    ) R.drawable.dr_ic_fill_black_circle else R.drawable.dr_ic_unfill_black_circle
                )

                if (context is ThirdTemplateActivity)
                    (context as ThirdTemplateActivity).showButton(objList.any { it.isSelected == 1 })

                rlMain.tag = position
            }
        }
    }


    override fun getItemCount(): Int {
        return objList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addData(mObjList: ArrayList<GeneralModel>, isNew: Boolean) {
        if (isNew) {
            objList = ArrayList()
        }
        objList.addAll(mObjList)
        this.notifyDataSetChanged()

        if (context is TwentyTwoTemplateActivity)
            (context as TwentyTwoTemplateActivity).showNoDataString(objList.isEmpty())

    }

    interface DegreeAdapterItemClickListener {
        fun onInnerItemClick(outerPosition: Int, innerPosition: Int, innerData: GeneralModel)
    }

    fun clearSelection() {
        objList.find { it.isSelected==1 }?.isSelected=0
        notifyDataSetChanged()
    }

}
