package com.commonfriend.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.commonfriend.R
import com.commonfriend.databinding.RowMessageListBinding
import com.commonfriend.models.ChatModel
import com.commonfriend.utils.Util
import com.commonfriend.utils.visible
import com.commonfriend.utils.visibleIf

class ChatAdapter(
        var context: Context,
        var clickListener: View.OnClickListener,
        var dataLoadingCompleted: ChatDataFullyLoaded,
) : RecyclerView.Adapter<ChatAdapter.ViewHolder>() {
    var objList: ArrayList<ChatModel> = ArrayList()
    class ViewHolder(var binding: RowMessageListBinding) : RecyclerView.ViewHolder(binding.root) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
                RowMessageListBinding.inflate(
                        LayoutInflater.from(
                                parent.context
                        ), parent, false
                )
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        with(holder.binding) {
//            val roundingParams = RoundingParams()
//            roundingParams.setBorderWidth(
//                if (objList[position].isCommonFriend == "1") context.resources.getDimension(com.intuit.sdp.R.dimen._1sdp) else 0f
//            )
//            roundingParams.setBorderColor(ContextCompat.getColor(context, R.color.color_blue))
//            roundingParams.setRoundAsCircle(true)
//            imgSenderProfilePic.hierarchy.roundingParams = roundingParams


//            llIntroView.visibility = if (objList[position].isCommonFriend == "1") View.VISIBLE else View.GONE

            val firstName =
                    if (objList[position].chatName.contains(" ")) objList[position].chatName.split(" ")[0] else objList[position].chatName
            txtSenderName.text =
                    if (objList[position].isCommonFriend == "1") "$firstName & You" else objList[position].chatName

//            if (objList[position].isCommonFriend == "1") imgSenderProfilePic.setImageResource(R.drawable.ic_chat_face)
//            else {
//                if (objList[position].senderProfilePic.isNotEmpty()) {
//                    imgSenderProfilePic.setImageURI(objList[position].senderProfilePic)
//                    txtUserInits.visibility = View.INVISIBLE
//                } else {
//                    txtUserInits.visibility = View.VISIBLE
//                    txtUserInits.text = Util.getNameInitials(objList[position].chatName)
//                }
//            }

            imgCommonFriendProfile.visibleIf(objList[position].isCommonFriend == "1")

            if (objList[position].senderProfilePic.isNotEmpty()) {
                imgSenderProfilePic.setImageURI(objList[position].senderProfilePic)
                txtUserInits.visibility = View.INVISIBLE
            } else {
                txtUserInits.visibility = View.VISIBLE
                txtUserInits.text = Util.getNameInitials(objList[position].chatName)
            }


            txtMessage.text = objList[position].message
            txtTime.text = objList[position].time

            txtMessageCount.visibleIf(objList[position].messageCount > 0)
            txtMessageCount.text = objList[position].messageCount.toString()

            cvMain.tag = position
            cvMain.setOnClickListener(clickListener)

            imgSenderProfilePic.tag = position
            imgSenderProfilePic.setOnClickListener(clickListener)


            if (position == objList.size - 1) {
                dataLoadingCompleted.dataLoadingCompleted()
            }
        }

    }

    override fun getItemCount(): Int {
        return objList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addData(mObjList: ArrayList<ChatModel>) {
        objList = ArrayList()
        objList.addAll(mObjList)
        this.notifyDataSetChanged()
    }
}

interface ChatDataFullyLoaded {
    fun dataLoadingCompleted()
}