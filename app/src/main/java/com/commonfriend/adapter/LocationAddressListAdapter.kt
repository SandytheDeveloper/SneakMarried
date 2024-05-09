package com.commonfriend.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.commonfriend.R
import com.commonfriend.databinding.RawCollageDialogBinding
import com.commonfriend.models.GeneralModel
import com.commonfriend.template.EightTeenTemplateActivity
import com.commonfriend.template.TwentyTemplateActivity
import com.commonfriend.utils.LocationApi

class LocationAddressListAdapter(
    var context: Context,
    var clickListener: OnClickListener,
    var type: String
) :
    RecyclerView.Adapter<LocationAddressListAdapter.ViewHolder>(), Filterable {

    private var selectedPlaceId: String = ""
    var objList: ArrayList<GeneralModel> = ArrayList()

    var mLocationAPI: LocationApi = LocationApi(type,context)


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


    class ViewHolder(var binding: RawCollageDialogBinding) : RecyclerView.ViewHolder(binding.root)


    override fun getItemCount(): Int {
        return objList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        with(holder.binding)
        {
            with(objList[position])
            {
                if (selectedPlaceId.isNotEmpty() && selectedPlaceId == this.description)
                    this.isSelected = 1

                txtLocation.text = this.description
                imgLocation.setBackgroundResource(if (this.isSelected == 1) R.drawable.dr_ic_fill_black_circle else R.drawable.dr_ic_unfill_black_circle)

                if (context is EightTeenTemplateActivity)
                    (context as EightTeenTemplateActivity).showButton(objList.any { it.isSelected == 1 })
/*                else if (context is TwentyTemplateActivity)
                    (context as TwentyTemplateActivity).showButton(objList.any { it.isSelected == 1 })*/

                rlMain.tag = position
                rlMain.setOnClickListener(clickListener)
            }
        }
    }


    fun addData(objList: ArrayList<GeneralModel>) {
        this.objList = ArrayList()
        this.objList.addAll(objList)
        notifyDataSetChanged()

    }

    fun setPlaceId(placeId: String) {
        this.selectedPlaceId = placeId.trim()
        notifyDataSetChanged()
    }


    override fun getFilter(): Filter {

        return object : Filter() {
            override fun performFiltering(constraint: CharSequence): FilterResults {
                val filterResults = FilterResults()

                objList = mLocationAPI.autoComplete(constraint.toString())
                filterResults.values = objList
                filterResults.count = objList.size
                return filterResults
            }

            override fun publishResults(
                constraint: CharSequence,
                results: FilterResults
            ) {
                if (results.count == 0) {
                    if (context is EightTeenTemplateActivity)
                        (context as EightTeenTemplateActivity).showNoDataFoundString(true)
                    if (context is TwentyTemplateActivity)
                        (context as TwentyTemplateActivity).showNoDataFoundString(true)
                } else {
                    notifyDataSetChanged()
                    if (context is EightTeenTemplateActivity)
                        (context as EightTeenTemplateActivity).showNoDataFoundString(false)
                    else if (context is TwentyTemplateActivity)
                        (context as TwentyTemplateActivity).showNoDataFoundString(false)

                }

                notifyDataSetChanged()
            }
        }
    }
}