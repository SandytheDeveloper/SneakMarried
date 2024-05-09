package com.commonfriend.adapter


import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.commonfriend.R
import com.commonfriend.databinding.RawCollageDialogBinding
import com.commonfriend.models.CountryCodeModel
import java.util.*


class CountryCodeListAdapter(var context: Context, var clickListener: View.OnClickListener
) :
    RecyclerView.Adapter<CountryCodeListAdapter.ViewHolder>(), Filterable {

    var objList: ArrayList<CountryCodeModel> = ArrayList()
    var objMainList: ArrayList<CountryCodeModel> = ArrayList()

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

        holder.binding.imgLocation.setBackgroundResource(
            if (objList[position].isSelected == 1) R.drawable.dr_ic_fill_black_circle else R.drawable.dr_ic_unfill_black_circle)


        holder.binding.rlMain.tag = position
        holder.binding.rlMain.setOnClickListener(clickListener)


    }


    override fun getItemCount(): Int= objList.size

    @SuppressLint("NotifyDataSetChanged")
    fun addData(mObjList: ArrayList<CountryCodeModel>) {
        objList = ArrayList()
        objMainList = ArrayList()
        objMainList.addAll(mObjList)
        objList.addAll(objMainList)
        this.notifyDataSetChanged()

    }


    override fun getFilter(): Filter {

        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charString = constraint?.toString() ?: ""

                var filteredList = if (charString.trim().isEmpty()){
                    objMainList
                } else {

                    objMainList.filter {
                        (it.name.lowercase(Locale.getDefault()).contains(charString.trim()))
                    } as ArrayList<CountryCodeModel>

                }
                return FilterResults().apply { values = filteredList }
            }
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {

                objList = ArrayList()
                objList.addAll(if (results?.values == null)
                    ArrayList()
                else
                    results.values as ArrayList<CountryCodeModel>)

                notifyDataSetChanged()


            }


        }
    }
}