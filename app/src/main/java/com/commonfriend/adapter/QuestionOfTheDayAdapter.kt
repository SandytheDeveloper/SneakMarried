package com.commonfriend.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.commonfriend.SneakPeekActivity
import com.commonfriend.databinding.RowQuestionOfTheDayListBinding
import com.commonfriend.models.QuestionBankModel
import com.commonfriend.utils.ActivityIsFrom
import com.commonfriend.utils.ID
import com.commonfriend.utils.IS_FROM
import com.commonfriend.utils.QUESTION_ID

class QuestionOfTheDayAdapter(var context: Context, var clickListener: View.OnClickListener) :
    RecyclerView.Adapter<QuestionOfTheDayAdapter.ViewHolder>() {
    var objList: ArrayList<QuestionBankModel> = ArrayList()

    class ViewHolder(var binding: RowQuestionOfTheDayListBinding) : RecyclerView.ViewHolder(binding.root) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            RowQuestionOfTheDayListBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ),
                parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return objList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding) {
            with(objList[position])
            {
                txtOpinions.text = question
                txtQuestionTitle.text = questionTitle
                txtNumber.text = "#$questionNumber"
                btnAnswerOrSkip.tag = position
                cvMain.tag = position
                cvMain.setOnClickListener{
                    context.startActivity(
//                        Intent(context, SneakPeakActivity::class.java).putExtra(IS_FROM,ActivityIsFrom.QUESTION_OF_THE_DAY)
                        Intent(context, SneakPeekActivity::class.java).putExtra(IS_FROM,ActivityIsFrom.QUESTION_OF_THE_DAY)
                            .putExtra(ID, this.id)
                            .putExtra(QUESTION_ID, this.id))
                }
                btnAnswerOrSkip.setOnClickListener{
                    context.startActivity(
//                        Intent(context, SneakPeakActivity::class.java).putExtra(IS_FROM,ActivityIsFrom.QUESTION_OF_THE_DAY)
                        Intent(context, SneakPeekActivity::class.java).putExtra(IS_FROM,ActivityIsFrom.QUESTION_OF_THE_DAY)
                            .putExtra(ID, this.id)
                            .putExtra(QUESTION_ID, this.id))
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addData(mObjList: ArrayList<QuestionBankModel>) {
        objList = ArrayList()
        objList.addAll(mObjList)
        this.notifyDataSetChanged()
    }
}