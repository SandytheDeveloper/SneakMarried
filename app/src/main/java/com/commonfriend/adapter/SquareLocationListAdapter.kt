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
import com.commonfriend.databinding.RawCultureDialogBinding
import com.commonfriend.models.GeneralModel
import com.commonfriend.template.EightTeenTemplateActivity
import com.commonfriend.template.TwentyTemplateActivity
import com.commonfriend.utils.LocationApi
import com.commonfriend.utils.Util


class SquareLocationListAdapter(
    var context: Context, var clickListener: View.OnClickListener,
    var type: String
) :
    RecyclerView.Adapter<SquareLocationListAdapter.ViewHolder>(), Filterable {

    private var selectedPlaceId: String = ""
    var objList: ArrayList<GeneralModel> = ArrayList()

    var mLocationAPI: LocationApi = LocationApi(type,context)

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
        holder.binding.txtCulture.text = objList[position].description

        holder.binding.rlMain.tag = position
        holder.binding.rlMain.setOnClickListener(clickListener)

        if (selectedPlaceId.isNotEmpty()) {
            if (selectedPlaceId.contains(",")) {
                for (i in selectedPlaceId.split(",")) {
                    if (objList[position].placeId == i)
                        objList[position].isSelected = 1
                }
            } else {
                if (selectedPlaceId == objList[position].placeId)
                    objList[position].isSelected = 1
            }
        }



        holder.binding.checkbox.setBackgroundResource(
            if (objList[position].isSelected == 1)
                R.drawable.dr_ic_sqaure_fill
            else
                R.drawable.dr_ic_sqaure_unfill
        )
        //  (context as TwentyTemplateActivity).showButton(objList.any { it.isSelected == 1 })
    }

    override fun getItemCount(): Int {

        (context as TwentyTemplateActivity).showNoDataFoundString(objList.size <= 1)

        return objList.size
    }


    fun addData(objList: ArrayList<GeneralModel>) {
        this.objList = ArrayList()
        this.objList.addAll(objList)
        notifyDataSetChanged()

    }

    fun setPlaceId(placeId: String) {
        this.selectedPlaceId = placeId
        notifyDataSetChanged()
        Util.print(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>placeids>>>>>>>>>>${this.selectedPlaceId}")
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


    fun clearData(){
        objList.clear()
        notifyDataSetChanged()
    }
}