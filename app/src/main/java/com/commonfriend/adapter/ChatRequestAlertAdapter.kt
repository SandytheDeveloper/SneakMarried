package com.commonfriend.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.commonfriend.R
import com.commonfriend.databinding.RawChatInterestListBinding
import com.commonfriend.models.PeopleModel
import com.commonfriend.utils.Util
import com.commonfriend.utils.visibleIf

class ChatRequestAlertAdapter(var context: Context,var clicklistener: View.OnClickListener) :
    RecyclerView.Adapter<ChatRequestAlertAdapter.ViewHolder>() {

    var objList: ArrayList<PeopleModel> = ArrayList()
    var firstLetter = ""
    var secondLetter = ""

    class ViewHolder(var binding: RawChatInterestListBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            RawChatInterestListBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        with(holder.binding) {
            with(objList[position]) {

//                if (this.profilePic != null && this.profilePic.isNotEmpty())
//                    imgProfile.setImageURI(this.profilePic)
//                else {
//                    firstLetter = ""
//                    secondLetter = ""
//                    if (objList[position].name.contains(" ")) {
//                        var name = objList[position].name.split(" ")
//                        firstLetter = name[0].substring(0, 1)
//                        secondLetter = name[1].substring(0, 1)
//                        holder.binding.txtUserInits.text = firstLetter + secondLetter
//                    } else {
//                        holder.binding.txtUserInits.text =if(objList[position].name.isNotEmpty())objList[position].name.substring(0, 1) else ""
//                    }
//                }

                if (profilePic.isNotEmpty()) {
                    imgProfile.setImageURI(profilePic)
                    txtUserInits.visibility = View.INVISIBLE
                } else {
                    txtUserInits.visibility = View.VISIBLE
                    txtUserInits.text = Util.getNameInitials(objList[position].name)
                }

//                txtLikeCount.text = this.likedCount
//                llLikeView.visibility =
//                    if (this.likedCount.isNotEmpty() && this.likedCount!="0") View.VISIBLE else View.INVISIBLE
                cvName.visibleIf(this.isLocked == "1")
                llTimerView.visibility = if(this.time.isNotEmpty() && this.time!="0") View.VISIBLE else View.INVISIBLE
                txtTime.text = this.time
//                txtTime.text = StringBuilder().append(this.time).append(" ").append(context.getString(if(time=="1") R.string.day_left else R.string.days_left))
                txtName.text = if (this.isLocked == "1") context.getString(R.string.loading2) else this.name
                txtageLocation.text =
                    if (this.age.isNotEmpty()) "${this.age}, ${this.location}" else this.location
                btnChatViewProfile.tag = position
                btnChatViewProfile.setOnClickListener(clicklistener)

//                if ( position == 0 ) {
                val layoutParams = LinearLayout.LayoutParams(
                    if (objList.size <= 1) LinearLayout.LayoutParams.MATCH_PARENT else context.resources.getDimension(com.intuit.sdp.R.dimen._200sdp).toInt(),
                    context.resources.getDimension(com.intuit.sdp.R.dimen._220sdp).toInt()
                )

                layoutParams.setMargins(
                    0,
                    0,
                    if (objList.size <= 1) 0 else context.resources.getDimension(com.intuit.sdp.R.dimen._20sdp).toInt(),
                    context.resources.getDimension(com.intuit.sdp.R.dimen._5sdp).toInt()
                )
                cvData.layoutParams = layoutParams
//                }


            }
        }
    }

    override fun getItemCount(): Int {
        return objList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addData(mObjList: ArrayList<PeopleModel>) {
        objList = ArrayList()
        objList.addAll(mObjList)
        this.notifyDataSetChanged()
    }
}