package com.commonfriend.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.commonfriend.R
import com.commonfriend.databinding.RowSelectContactListBinding
import com.commonfriend.models.ContactModel

class SelectContactAdapter(
    var context: Context, var clickListener: View.OnClickListener,
    var objList: List<ContactModel>,
    var multipleSelection: Boolean = false
) :
    RecyclerView.Adapter<SelectContactAdapter.ViewHolder>(), Filterable {

    var objMainList: List<ContactModel> = objList

    class ViewHolder(var binding: RowSelectContactListBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            RowSelectContactListBinding.inflate(
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
        with(holder.binding) {
            with(objList[position]) {
                txtNumber.text = this.phone
                txtName.text = this.names

                cvMainView.tag = position
                cvMainView.setOnClickListener(clickListener)


                cvMainView.setCardBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        if (this.isSelected == 1) R.color.color_light_blue else R.color.color_white
                    )
                )
                txtNumber.setTextColor(
                    ContextCompat.getColor(
                        context,
                        if (this.isSelected == 1) R.color.color_blue else R.color.color_grey
                    )
                )
                cvMainView.strokeColor = ContextCompat.getColor(
                    context,
                    if (this.isSelected == 1) R.color.color_light_blue else R.color.color_grey
                )
                imgSelected.setImageResource(if (this.isSelected != 1) R.drawable.ic_round_uncheck else R.drawable.ic_purple_dot)

                if (multipleSelection)
                    imgSelected.setImageResource(if(this.isSelected == 1) R.drawable.dr_ic_sqaure_fill else R.drawable.dr_ic_sqaure_unfill)
            }
        }
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