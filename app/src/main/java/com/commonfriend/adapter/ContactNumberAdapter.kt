package com.commonfriend.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.commonfriend.R
import com.commonfriend.databinding.RowContactsDialogBinding
import com.commonfriend.models.ContactModel

class ContactNumberAdapter(
    var context: Context, var clickListener: View.OnClickListener,
    var objList:List<ContactModel>):
    RecyclerView.Adapter<ContactNumberAdapter.ViewHolder>(), Filterable {

    var objMainList: ArrayList<ContactModel> = ArrayList()

    class ViewHolder(var binding: RowContactsDialogBinding): RecyclerView.ViewHolder(binding.root) {

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            RowContactsDialogBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )
        )
    }

    override fun getItemCount(): Int {

        return objList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.txtMobileNumber.text = objList[position].phone
        holder.binding.txtName.text = objList[position].names

        holder.binding.rlMain.tag = position
        holder.binding.rlMain.setOnClickListener(clickListener)

        holder.binding.checkbox.setBackgroundResource(
            if (objList[position].isSelected == 1)
                R.drawable.dr_ic_sqaure_fill
            else
                R.drawable.dr_ic_sqaure_unfill )
//        (context as HomeScreenActivity).showButton(objList.filter { it.isSelected == 1 }.isNotEmpty())
    }

    override fun getFilter(): Filter {

        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                objList = objMainList
                val charString = constraint?.toString() ?: ""
                objList = if (charString.isEmpty()) objMainList else {
                    val filteredList = ArrayList<ContactModel>()
                    objList.filter {
                        (it.names.lowercase().contains(charString))
                    }.forEach { filteredList.add(it) }
                    filteredList

                }
                return FilterResults().apply { values = objList }
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {

                objList = if (results?.values == null)
                    ArrayList()
                else
                    results.values as ArrayList<ContactModel>
                notifyDataSetChanged()


            }
        }
    }

}