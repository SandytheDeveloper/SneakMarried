package com.commonfriend.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.commonfriend.ChannelActivity
import com.commonfriend.ProfileViewActivity
import com.commonfriend.R
import com.commonfriend.databinding.RowChannelListBinding
import com.commonfriend.utils.ActivityIsFrom
import com.commonfriend.utils.ID
import com.commonfriend.utils.Pref
import com.commonfriend.utils.Util
import com.commonfriend.utils.gone
import com.commonfriend.utils.openA
import com.commonfriend.utils.visible
import com.commonfriend.utils.visibleIf
import io.getstream.chat.android.client.extensions.currentUserUnreadCount
import io.getstream.chat.android.client.extensions.internal.lastMessage
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.common.helper.DateFormatter
import io.getstream.chat.android.ui.utils.extensions.getLastMessage

class ChannelListAdapter(var context: Context, private var fromHome: Boolean = false) : RecyclerView.Adapter<ChannelListAdapter.ViewHolder>() {

    var objList: ArrayList<Channel> = ArrayList()
    private val commonFriendId = context.getString(R.string.common_friend_id)
    private val currentUserId = Pref.getStringValue(Pref.PREF_USER_ID,"").toString()

    class ViewHolder(var binding: RowChannelListBinding) : RecyclerView.ViewHolder(binding.root) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            RowChannelListBinding.inflate(
                        LayoutInflater.from(
                                parent.context
                        ), parent, false
                )
        )
    }

    @OptIn(InternalStreamChatApi::class)
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        with(holder.binding) {
            with(objList[position]){

                cvInnerMain.setCardBackgroundColor(ContextCompat.getColor(context,if (fromHome) R.color.color_base_grey else R.color.color_white))
                bottomLine.visibleIf(!fromHome)

//                val hasCommonFriend = members.any { member -> member.user.id == commonFriendId }
                val hasCommonFriend = getExtraValue("is_common_friend",false)

                val channelUser = Util.getChannelUser(context,members)
                val channelName = channelUser.name
                val channelProfile = channelUser.image
                val channelUserId = channelUser.id

                val lastMessageText = lastMessage?.text ?: ""
                val lastMessageTime = DateFormatter.from(context).formatDate(lastMessageAt)
                val unreadCountText = currentUserUnreadCount


                val firstName =
                    if (channelName.contains(" ")) channelName.split(" ")[0] else channelName

                txtChannelName.text =
                    if (hasCommonFriend) "$firstName & You" else channelName

                imgCommonFriendProfile.visibleIf(hasCommonFriend)

                txtUserInits.text = Util.getNameInitials(channelName)

                if (channelProfile.isNotEmpty()) {
                    imgChannelProfile.visible()
                    imgChannelProfile.setImageURI(channelProfile)
                } else {
                    imgChannelProfile.gone()
                }

                txtLastMessage.text = lastMessageText
                txtTime.text = lastMessageTime
                txtUnreadCount.visibleIf(unreadCountText > 0)
                txtUnreadCount.text = unreadCountText.toString()



                Util.print("1232 ----------------------------------")
                Util.print("1232 name $channelName $hasCommonFriend")
                Util.print("1232 lastMessage $lastMessageText")
                Util.print("1232 lastMessageTime $lastMessageTime")
                Util.print("1232 unreadCount $unreadCountText")
                Util.print("1232 channelProfile $channelProfile")

                cvMain.setOnClickListener {
                    context.startActivity(ChannelActivity.newIntent(context, cid, ActivityIsFrom.CHAT_SCREEN))
                }

                imgChannelProfile.setOnClickListener {
                    context.openA<ProfileViewActivity> { putExtra(ID, channelUserId) }
                }






            }






//            cvMain.tag = position
//            cvMain.setOnClickListener(clickListener)
//
//            imgSenderProfilePic.tag = position
//            imgSenderProfilePic.setOnClickListener(clickListener)


        }

    }


    override fun getItemCount(): Int {
        return objList.size
    }


    @SuppressLint("NotifyDataSetChanged")
    fun addData(mObjList: List<Channel>) {
        objList = ArrayList()
        objList.addAll(mObjList)
        this.notifyDataSetChanged()
    }
}
