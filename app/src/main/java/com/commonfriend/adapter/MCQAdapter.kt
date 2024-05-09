package com.commonfriend.adapter


import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.Filter
import android.widget.Filterable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.commonfriend.R
import com.commonfriend.databinding.RawMcqItemBinding
import com.commonfriend.models.GeneralModel


class MCQAdapter
    (var context: Context, var clickListener: View.OnClickListener,var templateCode:Int) :
    RecyclerView.Adapter<MCQAdapter.ViewHolder>(), Filterable {

    var objList: ArrayList<GeneralModel> = ArrayList()
    var objMainList: ArrayList<GeneralModel> = ArrayList()

    class ViewHolder(var binding: RawMcqItemBinding) : RecyclerView.ViewHolder(binding.root)


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            RawMcqItemBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )
        )

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding) {
            with(objList[position]) {
                txtOption.text = this.name
                txtOption.setTextColor(ContextCompat.getColor(context,if (this.isSelected == 1) R.color.color_white else R.color.color_black))
                txtOption.setBackgroundResource(if (this.isSelected == 1) R.drawable.dr_bg_black_round else com.google.android.material.R.drawable.m3_tabs_transparent_background)

                llOption.layoutParams.width = if(templateCode==23)LayoutParams.WRAP_CONTENT else LayoutParams.MATCH_PARENT
                llOption.tag = position
                llOption.setOnClickListener(clickListener)
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
        objMainList = objList
        this.notifyDataSetChanged()

    }

    override fun getFilter(): Filter {

        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charString = constraint?.toString() ?: ""
                if (charString.isEmpty()) objList = objMainList else {
                    val filteredList = ArrayList<GeneralModel>()
                    objList.filter {
                        (it.name.toLowerCase().contains(constraint!!)) or
                                (it.name.toLowerCase().contains(constraint))
                    }
                        .forEach { filteredList.add(it) }
                    objList = filteredList

                }
                return FilterResults().apply { values = objList }
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {

                objList = if (results?.values == null)
                    ArrayList()
                else
                    results.values as ArrayList<GeneralModel>
                notifyDataSetChanged()


            }
        }
    }

}