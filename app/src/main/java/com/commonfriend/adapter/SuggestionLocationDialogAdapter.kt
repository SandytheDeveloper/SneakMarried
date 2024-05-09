package com.commonfriend.adapter


import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.commonfriend.databinding.RawSuggestionDialogBinding
import com.commonfriend.models.GeneralModel


class SuggestionLocationDialogAdapter(
    var context: Context,
    var clickListener: View.OnClickListener
) :
    RecyclerView.Adapter<SuggestionLocationDialogAdapter.ViewHolder>() {

    var objList: ArrayList<GeneralModel> = ArrayList()
    var objMainList: ArrayList<GeneralModel> = ArrayList()

    class ViewHolder(var binding: RawSuggestionDialogBinding) :
        RecyclerView.ViewHolder(binding.root)


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            RawSuggestionDialogBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )
        )

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding) {
            with(objMainList[position])
            {
                txtHomeTown.text = this.name
                rlMainSuggestionView.tag = position
                rlMainSuggestionView.setOnClickListener(clickListener)
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

}