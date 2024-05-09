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
import com.commonfriend.databinding.RawErrorListBinding
import com.commonfriend.databinding.RowChannelListBinding
import com.commonfriend.models.ErrorModel
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

class ErrorListAdapter(var context: Context, private var type: Int = 0) : RecyclerView.Adapter<ErrorListAdapter.ViewHolder>() {

    var objList: ArrayList<ErrorModel> = ArrayList()
    class ViewHolder(var binding: RawErrorListBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            RawErrorListBinding.inflate(
                        LayoutInflater.from(
                                parent.context
                        ), parent, false
                )
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        with(holder.binding) {
            with(objList[position]){

                llView1.visibleIf (type == 1)
                llView2.visibleIf (type == 2)

                txtError1.text = error
                txtError2.text = error

            }

        }

    }


    override fun getItemCount(): Int {
        return objList.size
    }


    @SuppressLint("NotifyDataSetChanged")
    fun addData(mObjList: List<ErrorModel>) {
        objList = ArrayList()
        objList.addAll(mObjList)
        this.notifyDataSetChanged()
    }
}
