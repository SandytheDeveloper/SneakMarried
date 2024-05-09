package com.commonfriend.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.commonfriend.AadharVerificationActivity
import com.commonfriend.EditProfileActivity
import com.commonfriend.PhotoAlbumActivity
import com.commonfriend.SneakPeekActivity
import com.commonfriend.StepsActivity
import com.commonfriend.databinding.RowReminderListBinding
import com.commonfriend.models.ReminderModel
import com.commonfriend.utils.ActivityIsFrom
import com.commonfriend.utils.IS_FROM

class ReminderAdapter(var context: Context, var clickListener: View.OnClickListener) :
    RecyclerView.Adapter<ReminderAdapter.ViewHolder>() {
    var objList: ArrayList<ReminderModel> = ArrayList()


    class ViewHolder(var binding: RowReminderListBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            RowReminderListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        return objList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding) {
            with(objList[position])
            {
//                llReminderView.visibility =
//                    if (this.reminderType == "1" || this.reminderType == "2" || this.reminderType == "3" || this.reminderType == "6" || this.reminderType == "7") View.VISIBLE else View.GONE


                txtReminder.text = this.reminderNotify
                lblInfoReminder.text = this.reminderInfo

                imgContinue.tag = position
                imgContinue.setOnClickListener(clickListener)

                cvMain.tag = position

                //1 for penalty  2 for upload photos  3 for ans min ques 4 for changes rejected 5 for changes accepted 6 for onboarding questions
                imgContinue.tag = position
                llReminderView.tag = position
                llReminderView.setOnClickListener {
                    onClick(this.reminderType)
                }
                imgContinue.setOnClickListener {
                    onClick(this.reminderType)
                }


            }
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    fun addData(mObjList: ArrayList<ReminderModel>) {
        objList = ArrayList()
        objList.addAll(mObjList)
        this.notifyDataSetChanged()
    }

    fun onClick(reminderType: String) {

        when (reminderType) {
            "1" -> {

            }

            "2" -> {
                context.startActivity(
                    Intent(context, PhotoAlbumActivity::class.java).putExtra(
                        IS_FROM, ActivityIsFrom.CANDIDATE_ALBUM
                    )
                )
            }

            "3" -> {
                context.startActivity(
                    Intent(context, SneakPeekActivity::class.java).putExtra(
                        IS_FROM, ActivityIsFrom.PROFILE
                    )
                )
            }

            "6" -> {
                context.startActivity(
                    Intent(context, EditProfileActivity::class.java).putExtra(
                        IS_FROM,
                        ActivityIsFrom.FROM_MENU
                    )
                )
            }

            "7" -> {
                context.startActivity(
                    Intent(
                        context,
                        AadharVerificationActivity::class.java
                    )
                )
            }

            "8" -> {

                context.startActivity(
                    Intent(
                        context,
                        StepsActivity::class.java
                    )
                )
                (context as Activity).finishAffinity()
            }

        }

    }
}