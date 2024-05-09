package com.commonfriend.adapter


import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.commonfriend.databinding.RawOptionHeaderBinding
import com.commonfriend.models.GeneralModel
import com.commonfriend.template.ThirteenTemplateActivity


class OptionHeaderAdapter(
    var context: Context,
    var clickListener: InnerAdapterAdapter.DegreeAdapterItemClickListener,
    var tag: Int = 1
) :
    RecyclerView.Adapter<OptionHeaderAdapter.ViewHolder>() {

    var objList: ArrayList<GeneralModel> = ArrayList()

    class ViewHolder(var binding: RawOptionHeaderBinding) : RecyclerView.ViewHolder(binding.root)


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder {
        return ViewHolder(
            RawOptionHeaderBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.txtLocation.text = objList[position].name
        objList[position].qualificationListAdapter = InnerAdapterAdapter(context,clickListener,position,objList[position].subArrayData)
        holder.binding.rvOptionList.adapter = objList[position].qualificationListAdapter
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

        if (context is ThirteenTemplateActivity)
            (context as ThirteenTemplateActivity).showNoDataString(objList.isEmpty())
    }


    fun clearSelections(){
        for (i in 0 until objList.size)
        {
            objList[i].qualificationListAdapter?.clearSelection()
            notifyItemChanged(i)
        }
    }
}
