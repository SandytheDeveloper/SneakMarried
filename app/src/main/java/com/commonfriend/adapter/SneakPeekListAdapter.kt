package com.commonfriend.adapter

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.commonfriend.EditProfileActivity
import com.commonfriend.PhotoAlbumActivity
import com.commonfriend.R
import com.commonfriend.SneakPeekActivity
import com.commonfriend.base.BaseActivity
import com.commonfriend.custom.ErrorDialogComponent
import com.commonfriend.databinding.RowQuestionCardsBinding
import com.commonfriend.models.QuestionBankModel
import com.commonfriend.utils.ActivityIsFrom
import com.commonfriend.utils.IS_FROM
import com.commonfriend.utils.Pref
import com.commonfriend.utils.Util
import com.commonfriend.utils.gone
import com.commonfriend.utils.openA
import com.commonfriend.utils.visible
import com.commonfriend.utils.visibleIf
import com.facebook.drawee.view.SimpleDraweeView
import com.google.android.material.card.MaterialCardView

class SneakPeekListAdapter(var context: Context, var clickListener:OnClickListener) :
    RecyclerView.Adapter<SneakPeekListAdapter.ViewHolder>(),
    ErrorDialogComponent.ErrorBottomSheetClickListener {

    var objList: ArrayList<QuestionBankModel> = ArrayList()
    var isFrom = ActivityIsFrom.NORMAL
    var profilePic = ""
    var errorDialogComponent: ErrorDialogComponent? = null
    var clickPosition = 0

    class ViewHolder(var binding: RowQuestionCardsBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            RowQuestionCardsBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )
        )
    }

    @SuppressLint("NewApi")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        with(holder.binding) {
            with(objList[position]) {

                if (profilePic.isEmpty())
                    profilePic = Pref.getStringValue(Pref.PREF_USER_DISPLAY_PICTURE, "").toString()

                lastBottomSpace.visibleIf(position == objList.lastIndex)

                val colorInt = try {
                    if (cardColor.isNotEmpty())
                        Color.parseColor(cardColor)
                    else
                        ContextCompat.getColor(context,R.color.color_blue)
                } catch (e:Exception){
                    ContextCompat.getColor(context,R.color.color_blue)
                }

                rlMain.backgroundTintList = ColorStateList.valueOf(colorInt)

                txtContributed.visibleIf(isContributed == "1")

                txtNumber.text = "#$questionNumber"
                llPrivacy.visibility = if (shield == "1") View.VISIBLE else View.INVISIBLE
                shieldMessage.text = if (status == "2") hiddenPrivacyMessage else infoMessage
                txtQuestions.text = question
                txtQuestionTitle.text = questionTitle
                txtYes.text = optionA
                txtNo.text = optionB

                imgYesVisibilityButton.tag = position
                imgYesVisibilityButton.setOnClickListener(clickListener)
                imgNoVisibilityButton.tag = position
                imgNoVisibilityButton.setOnClickListener(clickListener)

                if (sneakAvailable == "1") // do animation
                {
                    imgYesMyProfile.gone()
                    btnYes.setCardBackgroundColor(context.resources.getColor(R.color.transparent_))
                    btnYes.strokeColor = context.resources.getColor(R.color.color_white)
                    txtYes.setTextColor(context.resources.getColor(R.color.color_white))
                    imgYesVisibilityButton.gone()

                    imgNoMyProfile.gone()
                    btnNo.setCardBackgroundColor(context.resources.getColor(R.color.transparent_))
                    btnNo.strokeColor = context.resources.getColor(R.color.color_white)
                    txtNo.setTextColor(context.resources.getColor(R.color.color_white))
                    imgNoVisibilityButton.gone()

                    btnYes.isEnabled = false
                    btnNo.isEnabled = false

                } else {

                    setButton(
                        (mySelectedAnswer == optionA),
                        status,

                        btnYes,
                        imgYesMyProfile,
                        txtYes,
                        imgYesVisibilityButton,

                        btnNo,
                        imgNoMyProfile,
                        txtNo,
                        imgNoVisibilityButton,
                        position,
                        isFrom,
                        false,
                        doButtonAnimation
                    )
                }

                doButtonAnimation = false

                btnYes.setOnClickListener {
                    mySelectedAnswer = optionA
                    status = "1"

                    setButton(
                        true,
                        status,

                        btnYes,
                        imgYesMyProfile,
                        txtYes,
                        imgYesVisibilityButton,

                        btnNo,
                        imgNoMyProfile,
                        txtNo,
                        imgNoVisibilityButton,
                        position,
                        isFrom,
                        true
                    )
                    (context as SneakPeekActivity).lastPos = position
                    (context as SneakPeekActivity).opinionAnswer = true
                    (context as SneakPeekActivity).callApi(2)
                }

                btnNo.setOnClickListener {
                    mySelectedAnswer = optionB
                    status = "1"

                    setButton(
                        false,
                        status,

                        btnYes,
                        imgYesMyProfile,
                        txtYes,
                        imgYesVisibilityButton,

                        btnNo,
                        imgNoMyProfile,
                        txtNo,
                        imgNoVisibilityButton,
                        position,
                        isFrom,
                        true
                    )
                    (context as SneakPeekActivity).lastPos = position
                    (context as SneakPeekActivity).opinionAnswer = false
                    (context as SneakPeekActivity).callApi(2)
                }

                btnChangeYes.setOnClickListener {
                    if (errorDialogComponent != null) {
                        if (errorDialogComponent!!.isShowing)
                            errorDialogComponent!!.dismiss()
                    }

                    clickPosition = position

                    errorDialogComponent = ErrorDialogComponent(
                        context,
                        if (status == "1") ErrorDialogComponent.ErrorDialogFor.ON_ANSWER_CHANGE
                        else ErrorDialogComponent.ErrorDialogFor.ON_HIDDEN_ANSWER_CHANGE,
                        context.getString(R.string.thats_not_possible),
                        context.getString(if (status == "1") R.string.once_answered_changing_the_answer_is_not_possible_the_best_ else R.string.once_answered_changing_the_answer_is_not_possible),
                        this@SneakPeekListAdapter
                    ).apply {
                        this.show()
                    }
                }

                btnChangeNo.setOnClickListener {
                    if (errorDialogComponent != null) {
                        if (errorDialogComponent!!.isShowing)
                            errorDialogComponent!!.dismiss()
                    }

                    clickPosition = position

                    errorDialogComponent = ErrorDialogComponent(
                        context,
                        if (status == "1") ErrorDialogComponent.ErrorDialogFor.ON_ANSWER_CHANGE
                        else ErrorDialogComponent.ErrorDialogFor.ON_HIDDEN_ANSWER_CHANGE,
                        context.getString(R.string.thats_not_possible),
                        context.getString(if (status == "1") R.string.once_answered_changing_the_answer_is_not_possible_the_best_ else R.string.once_answered_changing_the_answer_is_not_possible),
                        this@SneakPeekListAdapter
                    ).apply {
                        this.show()
                    }
                }

                llOnChangeButton.visibleIf(status != "0" && isFrom != ActivityIsFrom.SNEAK_PEAK)
                btnChangeYes.visibility = if (mySelectedAnswer == optionA) View.INVISIBLE else View.VISIBLE
                btnChangeNo.visibility = if (mySelectedAnswer == optionB) View.INVISIBLE else View.VISIBLE

            }
        }
    }

    private fun setButton(

        isSelectedOptionIsA : Boolean,  // Option A is Selected
        status : String,                // 0: not answer, 1 : answered , 2 : Answer with hidden

        cardViewA: MaterialCardView,
        imgViewA: SimpleDraweeView,
        txtViewA: AppCompatTextView,
        visibilityBtnA : AppCompatImageView,

        cardViewB: MaterialCardView,
        imgViewB: SimpleDraweeView,
        txtViewB: AppCompatTextView,
        visibilityBtnB : AppCompatImageView,
        position: Int,
        isFrom : ActivityIsFrom,
        doProfileAnimation : Boolean = false,
        doButtonAnimation : Boolean = false
    ) {

        cardViewA.isEnabled = false
        cardViewB.isEnabled = false

        if (status == "0") {

            imgViewA.gone()
            cardViewA.setCardBackgroundColor(context.resources.getColor(R.color.transparent_))
            cardViewA.strokeColor = context.resources.getColor(R.color.color_white)
            txtViewA.setTextColor(context.resources.getColor(R.color.color_white))
            visibilityBtnA.gone()

            imgViewB.gone()
            cardViewB.setCardBackgroundColor(context.resources.getColor(R.color.transparent_))
            cardViewB.strokeColor = context.resources.getColor(R.color.color_white)
            txtViewB.setTextColor(context.resources.getColor(R.color.color_white))
            visibilityBtnB.gone()

            cardViewA.isEnabled = true
            cardViewB.isEnabled = true

        } else if (doButtonAnimation) {


            imgViewA.setImageURI(profilePic)
            imgViewB.setImageURI(profilePic)
            visibilityBtnA.setImageResource(R.drawable.dr_ic_visible)
            visibilityBtnB.setImageResource(R.drawable.dr_ic_visible)
            visibilityBtnA.gone()
            visibilityBtnB.gone()
            cardViewA.isEnabled = false
            cardViewB.isEnabled = false

            if (isSelectedOptionIsA){

                startAnimationForButton(
                    imgViewA,
                    cardViewA,
                    txtViewA)

                cardViewB.setCardBackgroundColor(context.resources.getColor(R.color.transparent_))
                cardViewB.strokeColor = context.resources.getColor(R.color.color_white)
                txtViewB.setTextColor(context.resources.getColor(R.color.color_white))
                visibilityBtnB.gone()
                imgViewB.gone()

            } else {

                startAnimationForButton(
                    imgViewB,
                    cardViewB,
                    txtViewB)

                cardViewA.setCardBackgroundColor(context.resources.getColor(R.color.transparent_))
                cardViewA.strokeColor = context.resources.getColor(R.color.color_white)
                txtViewA.setTextColor(context.resources.getColor(R.color.color_white))
                visibilityBtnA.gone()
                imgViewA.gone()
            }


        } else {

            if (doProfileAnimation && isSelectedOptionIsA){
                profileAnimation(imgViewA,position)
            } else {
                imgViewA.visibleIf(isSelectedOptionIsA)
            }
            imgViewA.setImageURI(profilePic)
            cardViewA.setCardBackgroundColor(
                context.resources.getColor(
                    if (isSelectedOptionIsA) if (status == "2") R.color.color_black else R.color.color_white else R.color.transparent_
                )
            )

            cardViewA.strokeColor =
                context.resources.getColor(if (isSelectedOptionIsA) if (status == "2") R.color.color_black else R.color.color_white else R.color.color_white)

            txtViewA.setTextColor(context.resources.getColor(if (isSelectedOptionIsA && status == "1") R.color.color_black else R.color.color_white))

            visibilityBtnA.setImageResource(if (status == "2") R.drawable.dr_ic_hidden else R.drawable.dr_ic_visible)
            visibilityBtnA.visibleIf(isSelectedOptionIsA && isFrom != ActivityIsFrom.SNEAK_PEAK)



            if (doProfileAnimation && (!isSelectedOptionIsA)){
                profileAnimation(imgViewB,position)
            } else {
                imgViewB.visibleIf(!isSelectedOptionIsA)
            }
            imgViewB.setImageURI(profilePic)
            cardViewB.setCardBackgroundColor(
                context.resources.getColor(
                    if (!isSelectedOptionIsA) if (status == "2") R.color.color_black else R.color.color_white else R.color.transparent_
                )
            )

            cardViewB.strokeColor =
                context.resources.getColor(if (!isSelectedOptionIsA) if (status == "2") R.color.color_black else R.color.color_white else R.color.color_white)

            txtViewB.setTextColor(context.resources.getColor(if ((!isSelectedOptionIsA) && status == "1") R.color.color_black else R.color.color_white))

            visibilityBtnB.setImageResource(if (status == "2") R.drawable.dr_ic_hidden else R.drawable.dr_ic_visible)
            visibilityBtnB.visibleIf(!isSelectedOptionIsA && isFrom != ActivityIsFrom.SNEAK_PEAK)

            cardViewA.isEnabled = false
            cardViewB.isEnabled = false
        }
    }

    private fun profileAnimation(imgView: SimpleDraweeView, position: Int) {

        val handler = Handler(Looper.getMainLooper())
        var runnable : Runnable? = null
        val handler2 = Handler(Looper.getMainLooper())
        val runnable2 = Runnable {

            (context as SneakPeekActivity).scrollToNext(position+1)

        }

        runnable = Runnable {

            imgView.visible()
            val animation = AnimationUtils.loadAnimation(context, R.anim.pop_up_anim)
            imgView.animation = animation
            Util.playSound(context, 2)
            animation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {}

                override fun onAnimationEnd(animation: Animation?) {
                    notifyItemChanged(position)
                    handler.removeCallbacks(runnable!!)
                    handler2.postDelayed(runnable2, 500)
                }

                override fun onAnimationRepeat(p0: Animation?) {}
            })
        }
        handler.postDelayed(runnable, 150)

    }


    private fun startAnimationForButton(
        imageView : SimpleDraweeView,
        cardView : MaterialCardView,
        textView: AppCompatTextView) {

        Handler(Looper.getMainLooper()).postDelayed({
            imageView.visible()
            imageView.animation = AnimationUtils.loadAnimation(context, R.anim.pop_up_anim)
            Util.playSound(context, 2)
        },150)

        Handler(Looper.getMainLooper()).postDelayed({
            animateCardViewColor(context, cardView, textView)
        },450)
    }


    private fun animateCardViewColor(context: Context, button: MaterialCardView, textView: AppCompatTextView) {
        val animatorSet = AnimatorSet()
        val whiteColor = ContextCompat.getColor(context, R.color.color_white )
        val blackColor = ContextCompat.getColor(context, R.color.color_black )

        // Background color animation
        val backgroundColorAnimator = ValueAnimator.ofArgb(button.cardBackgroundColor.defaultColor, whiteColor)
        backgroundColorAnimator.addUpdateListener { animator ->
            button.setCardBackgroundColor(animator.animatedValue as Int)
        }

        // Text color animation
        val textColorAnimator = ValueAnimator.ofArgb(textView.currentTextColor, blackColor)
        textColorAnimator.addUpdateListener { animator ->
            textView.setTextColor(animator.animatedValue as Int)
        }

        // Stroke color animation
        val strokeColorAnimator = ValueAnimator.ofArgb(button.strokeColor, whiteColor)
        strokeColorAnimator.addUpdateListener { animator ->
            button.strokeColor = animator.animatedValue as Int
        }

        // Add all the animators to the animator set
        animatorSet.playTogether(backgroundColorAnimator, textColorAnimator, strokeColorAnimator)

        // Start the animation
        animatorSet.start()
        animatorSet.doOnEnd {
        }
    }

    override fun getItemCount(): Int {
        return objList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addData(mObjList: ArrayList<QuestionBankModel>) {
        objList = ArrayList()
        objList.addAll(mObjList)
        this.notifyDataSetChanged()
    }

    override fun onItemClick(itemID: String, isFrom: ErrorDialogComponent.ErrorDialogFor?) {

        errorDialogComponent?.dismiss()
        when (itemID) {
            "0" -> {

                if (isFrom != null && (isFrom == ErrorDialogComponent.ErrorDialogFor.ON_ANSWER_CHANGE)) {
                    // Hide Question


                    objList[clickPosition].status = "2"
                    notifyItemChanged(clickPosition)
                    Util.showLottieDialog(context, "done_lottie.json",wrapContent = true)

                    (context as? SneakPeekActivity)?.lastPos = clickPosition
                    (context as? SneakPeekActivity)?.callApi(4,false)

                    Handler(Looper.getMainLooper()).postDelayed({
                        (context as BaseActivity).errorDialogComponent?.dismiss()
                    }, 300)
                }
            }
            "1" -> {

                if (isFrom != null && (isFrom == ErrorDialogComponent.ErrorDialogFor.ON_ANSWER_CHANGE || isFrom == ErrorDialogComponent.ErrorDialogFor.ON_HIDDEN_ANSWER_CHANGE)) {
                    // Cancel
                    Handler(Looper.getMainLooper()).postDelayed({
                        (context as BaseActivity).errorDialogComponent?.dismiss()
                    }, 300)
                }
            }

        }

    }
}