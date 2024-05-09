package com.commonfriend.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.commonfriend.R
import com.commonfriend.databinding.RowQuestionListBinding
import com.commonfriend.models.QuestionBankModel

class QuestionListAdapter(
    var context: Context, var clickListener: View.OnClickListener,
    ):
    RecyclerView.Adapter<QuestionListAdapter.ViewHolder>(){

    var objList: ArrayList<QuestionBankModel> = ArrayList()

    class ViewHolder(var binding: RowQuestionListBinding): RecyclerView.ViewHolder(binding.root)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            RowQuestionListBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )
        )
    }

    override fun getItemCount(): Int {

        return objList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

with(holder.binding){
    with(objList[position])
    {
        txtCount.text = "#${questionNumber}"
        txtQuestion.text = question

        llAnsweredView.visibility = if (status == "0") View.INVISIBLE else View.VISIBLE
        txtAnswered.text = context.getString(if (status == "1") R.string.answered else R.string.hidden )

    }
}




        // 0: not answer, 1 : answered , 2 : Answer with hidden

        //0 notAnswered 1 Answered 2 Hidden
        holder.binding.cvMain.setCardBackgroundColor( ContextCompat.getColor(context,
            when (objList[position].status) {
                "1" -> R.color.color_base_grey
                "2" -> R.color.color_black
                else -> R.color.color_white
            }
        ))

        holder.binding.cvMain.setStrokeColor(ContextCompat.getColor(context,
            when (objList[position].status) {
                "1" -> R.color.color_base_grey
                "2" -> R.color.color_black
                else -> R.color.color_base_grey
            }
        ))

        holder.binding.txtQuestion.setTextColor(ContextCompat.getColor(context,
            if (objList[position].status == "2") {
                R.color.color_white
            } else {
                R.color.color_black
            }
        ))

        holder.binding.txtCount.setTextColor(ContextCompat.getColor(context,
            if (objList[position].status == "2") {
                R.color.color_white
            } else {
                R.color.color_black
            }
        ))

        holder.binding.cvMain.tag = position
        holder.binding.cvMain.setOnClickListener(clickListener)

    }

    @SuppressLint("NotifyDataSetChanged")
    fun addData(mObjectList : ArrayList<QuestionBankModel>){
        objList = ArrayList()
        objList.addAll(mObjectList)
        notifyDataSetChanged()
    }

}