package com.commonfriend.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.commonfriend.ChatDetailsActivity
import com.commonfriend.R
import com.commonfriend.databinding.RowHomeMessageListBinding
import com.commonfriend.models.ChatModel
import com.commonfriend.utils.ID
import com.commonfriend.utils.Util
import com.commonfriend.utils.visibleIf

class MessageAdapter(var context: Context, var clickListener: View.OnClickListener) :
    RecyclerView.Adapter<MessageAdapter.ViewHolder>() {
    var objList: ArrayList<ChatModel> = ArrayList()

    class ViewHolder(var binding: RowHomeMessageListBinding) : RecyclerView.ViewHolder(binding.root) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return MessageAdapter.ViewHolder(
            RowHomeMessageListBinding.inflate(
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

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        with(holder.binding) {
            with(objList[position]) {

                imgCommonFriendProfile.visibleIf(objList[position].isCommonFriend == "1")

                val firstName =
                    if (objList[position].chatName.contains(" ")) objList[position].chatName.split(" ")[0] else objList[position].chatName
                txtSenderName.text =
                    if (objList[position].isCommonFriend == "1") "$firstName & You" else objList[position].chatName


                if (objList[position].senderProfilePic.isNotEmpty()) {
                    imgSenderProfilePic.setImageURI(objList[position].senderProfilePic)
                    txtUserInits.visibility = View.INVISIBLE
                } else {
                    txtUserInits.visibility = View.VISIBLE
                    txtUserInits.text = Util.getNameInitials(objList[position].chatName)
                }

                txtMessage.text = this.message
                txtTime.text = this.time
                txtMessageCount.text = this.messageCount.toString()
                txtMessageCount.visibility = if(this.messageCount>0)View.VISIBLE else View.GONE

                /*if (objList[position].isCommonFriend == "1")
                    imgSenderProfilePic.setImageResource(R.drawable.ic_chat_face)
                else {
                    if (objList[position].senderProfilePic.isNotEmpty()) {
                        imgSenderProfilePic.setImageURI(objList[position].senderProfilePic)
                        txtUserInits.visibility = View.INVISIBLE
                    } else {
                        txtUserInits.visibility = View.VISIBLE
                        txtUserInits.text =
                            Util.getNameInitials(objList[position].chatName)
                    }
                }*/

                cvMain.tag = position
                cvMain.setOnClickListener {
                    context.startActivity(Intent(context, ChatDetailsActivity::class.java)
                        .putExtra(ID,this.chatId))
                }
            }
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    fun addData(mObjList: ArrayList<ChatModel>) {
        objList = ArrayList()
        objList.addAll(mObjList)
        this.notifyDataSetChanged()
    }
}