package com.commonfriend.custom

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.view.Window
import android.view.WindowManager
import com.commonfriend.R
import com.commonfriend.adapter.ErrorBtnAdapter
import com.commonfriend.databinding.DialogErrorComponentBinding
import com.commonfriend.utils.DataUtil
import com.commonfriend.utils.visibleIf
import com.google.android.material.bottomsheet.BottomSheetDialog

class ErrorDialogComponent(
    context: Context,
    private val isFrom: ErrorDialogFor,
    private val error: String,
    private val errorDesc: String,
    private val clickListener: ErrorBottomSheetClickListener? = null
) : BottomSheetDialog(context,R.style.TransparentBottomSheetDialogTheme), OnClickListener {

    private var lastPos = -1
    private lateinit var binding: DialogErrorComponentBinding
    private var errorBtnListAdapter: ErrorBtnAdapter? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_error_component)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        binding = DialogErrorComponentBinding.inflate(layoutInflater)
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(window!!.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        window!!.attributes = lp
//        window?.decorView?.background = BlurDialogFragment.get(context, window?.decorView as ViewGroup, 10, 8)
        // Dim the background

        initialization()
        setContentView(binding.root)
    }

    private fun initialization() {
        errorBtnListAdapter = ErrorBtnAdapter(this)
        when (isFrom) {
            ErrorDialogFor.CONFIRMING -> DataUtil.confirmationList(context, errorBtnListAdapter!!)
            ErrorDialogFor.PHOTO_ALBUM -> DataUtil.photoAlbumErrorList(context, errorBtnListAdapter!!)
            ErrorDialogFor.UPLOAD_PHOTOS -> DataUtil.confirmationList(context, errorBtnListAdapter!!,
                context.getString(
                    R.string.upload_now
                ), context.getString(R.string.later))
            ErrorDialogFor.QUESTIONS -> DataUtil.confirmationList(context, errorBtnListAdapter!!,
                context.getString(
                    R.string.answer_now
                ),context.getString(R.string.later))
            ErrorDialogFor.REPORTING -> DataUtil.rejectReportList(context, errorBtnListAdapter!!)
            ErrorDialogFor.SURETY_WITH_CLICK -> DataUtil.suretyList(context, errorBtnListAdapter!!)
            ErrorDialogFor.COMPLETE_PROFILE -> DataUtil.confirmationList(context, errorBtnListAdapter!!,
                context.getString(R.string.complete_now),
                context.getString(R.string.later))
            ErrorDialogFor.CHAT_INTRODUCTION -> DataUtil.chatIntroductionList(context, errorBtnListAdapter!!)
            ErrorDialogFor.ON_ANSWER_CHANGE -> DataUtil.onChangeQuestionList(context, errorBtnListAdapter!!,true)
            ErrorDialogFor.ON_HIDDEN_ANSWER_CHANGE -> DataUtil.onChangeQuestionList(context, errorBtnListAdapter!!,false)
            ErrorDialogFor.PHOTO_ALBUM_PENDING -> DataUtil.photoAlbumPendingList(context, errorBtnListAdapter!!)
            ErrorDialogFor.CHANGE_CONFIRMATION_LIST -> DataUtil.changeConfirmationList(context, errorBtnListAdapter!!)
            else -> DataUtil.suretyList(context, errorBtnListAdapter!!)
        }
        binding.rvButtonList.adapter = errorBtnListAdapter

        binding.llMsgView.visibleIf(error.isNotEmpty())
        binding.txtMsg.text = error
        binding.txtMsgDesc.text = errorDesc

    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnAction -> {
                lastPos = view.tag.toString().toInt()
                if (isFrom == ErrorDialogFor.SURETY) {
                    dismiss()
                } else {
                    clickListener?.onItemClick(errorBtnListAdapter!!.objList[lastPos].id,isFrom)
                }
            }
        }
    }

    interface ErrorBottomSheetClickListener {
        fun onItemClick(itemID: String,isFrom: ErrorDialogFor?=null)
    }

    enum class ErrorDialogFor {
        CONFIRMING,
        REPORTING,
        SURETY,
        SURETY_WITH_CLICK,
        PHOTO_ALBUM,
        QUESTIONS,
        UPLOAD_PHOTOS,
        COMPLETE_PROFILE,
        CHAT_INTRODUCTION,
        ON_ANSWER_CHANGE,
        ON_HIDDEN_ANSWER_CHANGE,
        PHOTO_ALBUM_PENDING,
        CHANGE_CONFIRMATION_LIST
    }
}
