package com.commonfriend.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.commonfriend.R
import com.commonfriend.databinding.RowChatListBinding
import com.commonfriend.models.ChatModel
import com.commonfriend.utils.Pref
import com.commonfriend.utils.Util
import com.commonfriend.utils.visible
import com.commonfriend.utils.visibleIf

class ChatDetailsAdapter(
    var context: Context,
    var clickListener: View.OnClickListener,
) : RecyclerView.Adapter<ChatDetailsAdapter.ViewHolder>() {

    var objList: ArrayList<ChatModel> = ArrayList()
    private val MAX_LENGHT = 15
    var userDetailArray : ChatModel = ChatModel()

    class ViewHolder(var binding: RowChatListBinding) : RecyclerView.ViewHolder(binding.root) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            RowChatListBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ),
                parent, false
            )
        )
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {


        if (objList[position].systemMessage > 0){
            holder.binding.llSenderMessage.visibility = View.VISIBLE
            holder.binding.llLeftMember.visibility = View.GONE
            holder.binding.llReceiverMessage.visibility = View.GONE
        } else if (objList[position].infoMessage > 0) {
            holder.binding.llLeftMember.visibility = View.VISIBLE
            holder.binding.llSenderMessage.visibility = View.GONE
            holder.binding.llReceiverMessage.visibility = View.GONE
        } else if (objList[position].senderId == Pref.getStringValue(Pref.PREF_USER_ID,"").toString()){
            holder.binding.llReceiverMessage.visibility = View.VISIBLE
            holder.binding.llSenderMessage.visibility = View.GONE
            holder.binding.llLeftMember.visibility = View.GONE
        } else {
            holder.binding.llSenderMessage.visibility = View.VISIBLE
            holder.binding.llLeftMember.visibility = View.GONE
            holder.binding.llReceiverMessage.visibility = View.GONE
        }

        if (objList[position].infoMessage > 0) {

            holder.binding.txtLeftMember.text = objList[position].message

        } else if (objList[position].senderId == Pref.getStringValue(Pref.PREF_USER_ID, "")
                .toString()
        ) {

            setTime(
                objList[position].message.length > MAX_LENGHT,
                holder.binding.llReceiverTime,
                holder.binding.llReceiverTime2
            )

            holder.binding.viewReceiverTop.visibleIf(objList[position].message.length > MAX_LENGHT)

            holder.binding.txtReceiverMessage.text = objList[position].message
            holder.binding.txtReceiverTime.text = objList[position].time
            holder.binding.txtReceiverTime2.text = objList[position].time

            holder.binding.imgStatus.setImageResource(if (objList[position].status == 2) R.drawable.ic_blue_tick else R.drawable.ic_double_tick)
            holder.binding.imgStatus2.setImageResource(if (objList[position].status == 2) R.drawable.ic_blue_tick else R.drawable.ic_double_tick)

            setView(holder.binding.rlMain, position)

        } else {

            setTime(
                objList[position].message.length > MAX_LENGHT,
                holder.binding.txtSenderTime,
                holder.binding.txtSenderTime2
            )

            holder.binding.viewSenderTop.visibleIf(objList[position].message.length > MAX_LENGHT)


//            holder.binding.txtSenderName.text = objList[position].senderName
            holder.binding.txtSenderMessage.text = objList[position].message
            holder.binding.txtSenderTime.text = objList[position].time
            holder.binding.txtSenderTime2.text = objList[position].time

            if (objList[position].systemMessage > 0){
                holder.binding.imgSenderProfile.setImageResource(R.drawable.ic_chat_face)
                holder.binding.txtSenderInits.visibility = View.INVISIBLE
            } else {
                with(userDetailArray) {
                    if (senderProfilePic.isNotEmpty()) {
                        holder.binding.imgSenderProfile.setImageURI(senderProfilePic)
                        holder.binding.txtSenderInits.visibility = View.INVISIBLE
                    } else {
                        holder.binding.txtSenderInits.visibility = View.VISIBLE
                        holder.binding.txtSenderInits.text =
                            Util.getNameInitials(name)
                    }
                }
            }

//            if (position > 0)
//                holder.binding.txtSenderName.visibility =
//                    if (objList[position].senderId == objList[position - 1].senderId) View.GONE else View.VISIBLE
//            else
//                holder.binding.txtSenderName.visibility = View.VISIBLE

            setView(holder.binding.rlMain, position)

            if (position < objList.lastIndex) {
                holder.binding.llSenderProfile.visibility =
                    if (objList[position].senderId == objList[position + 1].senderId) View.INVISIBLE else View.VISIBLE
            } else
                holder.binding.llSenderProfile.visible()

        }

    }

    private fun setView(view: ViewGroup, position: Int) {
        view.setPadding(
            view.paddingLeft,
            if (position > 0)
                if (objList[position].senderId == objList[position - 1].senderId) 0 else view.paddingBottom
            else view.paddingBottom, view.paddingRight, view.paddingBottom
        )
    }

    private fun setTime(
        condition: Boolean,
        time1: View,
        time2: View)
    {
        time1.visibility = if (condition) View.GONE else View.VISIBLE
        time2.visibility = if (condition) View.VISIBLE else View.GONE
    }

    private fun setTime2(
        position: Int,
        textView: AppCompatTextView,
        time1: AppCompatTextView? = null,
        time2: AppCompatTextView? = null,
        lltime1: LinearLayout? = null,
        lltime2: LinearLayout? = null
    ) {

        if (objList[position].timePosition != 0) {
            if (time1 != null) {
                time1.visibility =
                    if (objList[position].timePosition == 2) View.GONE else View.VISIBLE
                time2!!.visibility =
                    if (objList[position].timePosition == 2) View.VISIBLE else View.GONE
            } else {
                lltime1!!.visibility =
                    if (objList[position].timePosition == 2) View.GONE else View.VISIBLE
                lltime2!!.visibility =
                    if (objList[position].timePosition == 2) View.VISIBLE else View.GONE
            }
        } else {

            textView.viewTreeObserver.addOnGlobalLayoutListener(object :
                ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {

                    val displayWidth = context.resources.displayMetrics.widthPixels / 2
                    textView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    val width = textView.width

                    if (time1 != null) {
                        time1.visibility = if (width > displayWidth) View.GONE else View.VISIBLE
                        time2!!.visibility = if (width > displayWidth) View.VISIBLE else View.GONE
                    } else {
                        lltime1!!.visibility = if (width > displayWidth) View.GONE else View.VISIBLE
                        lltime2!!.visibility = if (width > displayWidth) View.VISIBLE else View.GONE
                    }
                    objList[position].timePosition = if (width > displayWidth) 2 else 1

                    textView.setPadding(
                        textView.paddingLeft,
                        textView.paddingTop,
                        if (width > displayWidth) 0 else textView.paddingRight,
                        textView.paddingBottom
                    )
                }
            })
        }
    }

    override fun getItemCount(): Int {
        return objList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addData(mObjList: ArrayList<ChatModel>, isNew: Boolean) {
        if (isNew) {
            objList = ArrayList()
        }
        objList.addAll(mObjList)
        this.notifyDataSetChanged()
    }
    @SuppressLint("NotifyDataSetChanged")
    fun addMoreItemsAtStart(items: ArrayList<ChatModel>) {
        objList.addAll(0,items)
        notifyItemRangeInserted(0,items.size)
        if (items.size<objList.size)
            notifyItemChanged(items.size)
//        notifyDataSetChanged()
    }
}