package com.commonfriend.adapter


import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.commonfriend.R
import com.commonfriend.databinding.RawSuggestionDialogBinding
import com.commonfriend.models.QuestionsModel


class SuggestionDialogAdapter(var context: Context, var clickListener: View.OnClickListener, var tag:Int=1) :
    RecyclerView.Adapter<SuggestionDialogAdapter.ViewHolder>() {

    var objList: ArrayList<QuestionsModel> = ArrayList()
    var objMainList: ArrayList<QuestionsModel> = ArrayList()

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
        with(holder.binding)
        {
            with(objMainList[position]){
                txtHomeTown.text = this.optionName
                rlMainSuggestionView.tag = position
                rlMainSuggestionView.setTag(R.string.app_name,tag)
                rlMainSuggestionView.setOnClickListener(clickListener)

            }
        }
    }


    override fun getItemCount(): Int {
        return objList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addData(mObjList: ArrayList<QuestionsModel>) {
        objList = ArrayList()
        objList.addAll(mObjList)
        objMainList = objList
        this.notifyDataSetChanged()

    }

}