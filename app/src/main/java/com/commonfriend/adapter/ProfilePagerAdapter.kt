package com.commonfriend.adapter

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ImageSpan
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.commonfriend.MainActivity
import com.commonfriend.R
import com.commonfriend.databinding.ProfileViewItemBinding
import com.commonfriend.fragment.RemoveProfileFromListInterface
import com.commonfriend.models.ProfileModel
import com.commonfriend.utils.Pref
import com.commonfriend.utils.Util
import com.commonfriend.utils.gone
import com.commonfriend.utils.isNull
import com.commonfriend.utils.visible
import com.commonfriend.utils.visibleIf

class ProfilePagerAdapter(
    var context: Context,
    var clickListener: OnClickListener,
    var removeProfileFromListInterface: RemoveProfileFromListInterface
) : RecyclerView.Adapter<ProfilePagerAdapter.ViewHolder>() {

    var objList: ArrayList<ProfileModel> = ArrayList()
    var currentPosition = 0
    var showProgressAnimation = true
    var onAnimationEndCalled = false
    val onPageChange = MutableLiveData<Int>(0)
    private var oldButtonY = 0
    private var totalHeight = 0f
    var ALBUM_SCREEN_TYPE = ""

    class ViewHolder(var binding: ProfileViewItemBinding) : RecyclerView.ViewHolder(binding.root) {}


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ProfileViewItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return objList.size
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        setView(holder.binding, position)

    }

    @SuppressLint("ResourceType", "SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.M)
    fun setView(binding: ProfileViewItemBinding, position: Int) {


        binding.root.tag = "${R.string.app_name}$position"

        // Save the scroll position when the fragment is destroyed
//        binding.scrollView.viewTreeObserver.addOnGlobalLayoutListener {
//            scrollPositions.put(position, binding.scrollView.scrollY)
//        }
//
//        // Restore the scroll position when the fragment is recreated
//        if (scrollPositions[position] != null) {
//            binding.scrollView.post {
//                binding.scrollView.scrollTo(0, scrollPositions[position])
//            }
//        }


        binding.scrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            val totalScrollLength =
                (binding.scrollView.getChildAt(0).height - binding.scrollView.height)
            val newButtonY = binding.floatingBtn.y.toInt()
            if (oldButtonY == 0)
                oldButtonY = newButtonY

            if (newButtonY < oldButtonY) {

                val currentHeight = (totalScrollLength - scrollY).toFloat()
                if (totalHeight == 0f)
                    totalHeight = currentHeight
                val opacity = currentHeight / totalHeight
                binding.floatingBtn.isEnabled = currentHeight > (totalHeight * 80 / 100)

                binding.floatingBtn.alpha = opacity
            } else if (newButtonY == oldButtonY) {
                binding.floatingBtn.alpha = 1f
                binding.floatingBtn.isEnabled = true
            } else {
                totalHeight = 0f
            }
        }




        with(objList[position]) {
            with(binding) {

                val drawable: Drawable =
                    ContextCompat.getDrawable(context, R.drawable.dr_ic_verified)!!
                drawable.setBounds(
                    0, 0,
                    context.resources.getDimension(com.intuit.sdp.R.dimen._15sdp).toInt(),
                    context.resources.getDimension(com.intuit.sdp.R.dimen._15sdp).toInt()
                )

                val spannableString = SpannableStringBuilder(name)
                spannableString.append("  ")
                val imageSpan = ImageSpan(drawable, ImageSpan.ALIGN_BASELINE)
                spannableString.setSpan(
                    imageSpan,
                    spannableString.length - 1,
                    spannableString.length,
                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE
                )
//                txtName.text = spannableString

                txtName.text =
                    if (isProfileLocked == "1" || status != "2")
                        name
                    else if (isAadharVerified == "1") spannableString else name


                txtName.viewTreeObserver.addOnGlobalLayoutListener {
                    if (txtName.lineCount > 1) {
                        if (isProfileLocked == "1" || status != "2") {
                            txtName.gone()
                            cvName.gone()
                            llTwoLinesName.visible()
                            cvFirstLine.setCardBackgroundColor(
                                ContextCompat.getColor(
                                    context,
                                    if (isProfileLocked == "1") R.color.color_white else R.color.color_light_grey
                                )
                            )
                            cvSecondLine.setCardBackgroundColor(
                                ContextCompat.getColor(
                                    context,
                                    if (isProfileLocked == "1") R.color.color_white else R.color.color_light_grey
                                )
                            )

                        }
                    }
                }

//                if (isAadharVerified == "1")
//                    imgVerified.visibility =
//                        if (isProfileLocked == "1" || status != "2") View.VISIBLE else View.GONE

                // 1 - locked , 0 - visible
                if (isProfileLocked == "0") {
                    if (profilePic.isNotEmpty())
                        imgProfile.setImageURI(profilePic)
                    else
                        imgProfile.setActualImageResource(
                            ContextCompat.getColor(
                                context,
                                R.color.color_light_grey
                            )
                        )
                } else {
                    imgProfile.setImageResource(R.drawable.dr_ic_profile_lock)
                }
                imgBackground.visibleIf(isProfileLocked == "0" && profilePic.isNotEmpty())

                // For Lock Profile
                cvName.visibility =
                    if (isProfileLocked == "1" || status != "2") View.VISIBLE else View.GONE

                cvDesignation.visibility = if (isProfileLocked == "1") View.VISIBLE else View.GONE
                cvOffice.visibility = if (isProfileLocked == "1") View.VISIBLE else View.GONE
                cvIndustry.visibility = if (isProfileLocked == "1") View.VISIBLE else View.GONE
                cvReligion.visibility = if (isProfileLocked == "1") View.VISIBLE else View.GONE
                cvHabits.visibility = if (isProfileLocked == "1") View.VISIBLE else View.GONE

                // Always Visible
                cvProfession.visibility = View.GONE
                cvCulture.visibility = View.GONE
                cvZodiac.visibility = View.GONE
                cvLocation.visibility = View.GONE

                when (statusText.lowercase()) {
                    context.getString(R.string.filter_new),
                    context.getString(R.string.filter_request_received),
                    context.getString(R.string.filter_missed) -> {

                        shimmerEffect.showShimmer(true)
                    }

                    else -> shimmerEffect.hideShimmer()
                }

                llStatus.visibility =
                    if (statusText.isNotEmpty() && status != "2") View.VISIBLE else View.GONE
                txtStatus.text = statusText
//                imgStatusDot.visibility = if (statusDot == "1") View.VISIBLE else View.INVISIBLE
                imgIntroduced.visibility = if (status == "2") View.VISIBLE else View.GONE


                cvTime.visibility = if (time.isNotEmpty()) View.VISIBLE else View.GONE
                txtTime.text = time
//                txtTime.setTextColor(ContextCompat.getColor(context,
//                    if (isProfileLocked == "1") R.color.viewBlack else R.color.colorWhite))
//                imgTime.setImageResource(if (isProfileLocked == "1") R.drawable.ic_black_time else R.drawable.ic_white_time)

                txtAddress.text = "$age, $currentLocation"
                txtAddress.setTextColor(
                    ContextCompat.getColor(
                        context,
                        if (isProfileLocked == "1") R.color.color_grey else R.color.color_white
                    )
                )

//                txtNumber.text = mobileNo

                llHeight.visibility = if (height.isEmpty()) View.GONE else View.VISIBLE
                if (height.isNotEmpty()) {
                    val feet = height.toFloat() / 12
                    val inches = height.toFloat() % 12
                    txtHeight.text = "${feet.toInt()}'${inches.toInt()} ft"
                }
//                if (height.isNotEmpty()) {
//                    val centimeters = height.toFloat() * 2.54
//                    val feet = centimeters / 30.48
//                    val inches = (feet - feet.toInt()) * 12
//                    txtHeight.text = "${feet.toInt()}'${inches.toInt()} ft"
//                }


                llBirthDate.visibility = if (dob.isEmpty()) View.GONE else View.VISIBLE
                txtBirthDate.text = dob

                llRelationship.visibility =
                    if (relationshipStatus.isEmpty()) View.GONE else View.VISIBLE
                txtRelationshipStatus.text = relationshipStatus

                llDisability.visibility = if (disability.isEmpty()) View.GONE else View.VISIBLE
                txtDisability.text = disability

                val financePercentage = if (finance.isNotEmpty()) finance.toFloat().toInt() else 0

                incomeProgressBarView.progress =
                    if (financePercentage < 1) 9 else if (financePercentage > 99) 91 else financePercentage

                imgFinanceUserImage.alpha = if (financePercentage < 1) 0.5f else 1f
                imgFinanceProfileImage.alpha = if (financePercentage > 99) 0.5f else 1f

                rlFinanceProfiles.visibleIf(financePercentage != -1)


                txtOpponentNameIncome.text = income
                txtOpponentNameIncome.visibility =
                    if (financePercentage < 1) View.VISIBLE else View.GONE

                imgFinanceUserImage.setImageURI(
                    Pref.getStringValue(
                        Pref.PREF_USER_DISPLAY_PICTURE,
                        ""
                    )
                )
                imgFinanceProfileImage.setImageURI(profilePic)

                if (financePercentage == -1) {
                    incomeProgressBarView.progressDrawable.setColorFilter(
                        ContextCompat.getColor(
                            context,
                            R.color.color_light_blue
                        ), PorterDuff.Mode.SRC_IN
                    )
                } else {

                    incomeProgressBarView.progressDrawable = ContextCompat.getDrawable(
                        context,
                        if (financePercentage >= 50) R.drawable.dr_brown_white_progress else R.drawable.dr_white_brown_progress
                    )

                }

                setTextView(llProfession, txtProfession, professions, isProfileLocked)
                setTextView(llDesignation, txtDesignation, designation, isProfileLocked, 1)
                setTextView(llOffice, txtOffice, office, isProfileLocked, 2)
                setTextView(llIndustry, txtIndustry, industry, isProfileLocked, 3)
                setTextView(llReligion, txtReligion, religions, isProfileLocked, 4)
                setTextView(llCulture, txtCulture, culture, isProfileLocked)
                setTextView(llHabits, txtHabits, eatingHabit, isProfileLocked, 5)
                setTextView(llZodiac, txtZodiac, zodiac, isProfileLocked)

                llLocation.visibility = if (settleLocation.isEmpty()) View.GONE else View.VISIBLE
                txtLocation.text = settleLocation
                lblSettleLocation.text =
                    context.getString(if (settleLocation.contains(",")) R.string.settle_locations else R.string.settle_location)

                lblOffice.text = workPlaceTitle

                llEducation.visibility =
                    if (educationList.isEmpty()) View.INVISIBLE else View.VISIBLE
                binding.rvEducationList.layoutManager = LinearLayoutManager(context)
                val educationAdapter = EducationAdapter(context)
                binding.rvEducationList.adapter = educationAdapter
                educationAdapter.addData(educationList)

                if (ALBUM_SCREEN_TYPE == "1") {

                    if (isProfileLocked == "1") {

                        imgAlbumFirst.setImageResource(R.drawable.dr_ic_profile_locks)
                        imgAlbumSecond.setImageResource(R.drawable.dr_ic_profile_locks)
                        imgAlbumThird.setImageResource(R.drawable.dr_ic_profile_locks)

                        llAlbumFirst.visible()
                        llAlbumSecond.visible()
                        llAlbumThird.visible()

                    } else {
                        if (albumList.isNotEmpty()) {

                            if (albumList[0].isNotEmpty()) {
                                imgAlbumFirst.setImageURI(albumList[0])
                                llAlbumFirst.visible()
                            } else {
                                llAlbumFirst.gone()
                            }

                            if (albumList.size > 1) {
                                if (albumList[1].isNotEmpty()) {
                                    imgAlbumSecond.setImageURI(albumList[1])
                                    llAlbumSecond.visible()
                                } else {
                                    llAlbumSecond.gone()
                                }
                            } else {
                                llAlbumSecond.gone()
                            }


                            if (albumList.size > 2) {
                                if (albumList[2].isNotEmpty()) {
                                    imgAlbumThird.setImageURI(albumList[2])
                                    llAlbumThird.visible()
                                } else {
                                    llAlbumThird.gone()
                                }
                            } else {
                                llAlbumThird.gone()
                            }

//                            imgAlbumSecond.setActualImageResource(
//                                ContextCompat.getColor(
//                                    context,
//                                    R.color.color_light_grey
//                                )
//                            )

                        } else {

                            llAlbumFirst.gone()
                            llAlbumSecond.gone()
                            llAlbumThird.gone()

                        }
                    }
                } else {
                    llAlbumFirst.gone()
                    llAlbumSecond.gone()
                    llAlbumThird.gone()
                }



                rlOpinions.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        context,
                        if (opinionView == "2" || opinionView == "3") R.color.color_light_grey else R.color.color_base_grey
                    )
                )

                onPageChange.observe(context as MainActivity) {

                    bottomView.visibleIf(position == currentPosition)
                    floatingBtn.visibleIf(position == currentPosition)
                    if (position == currentPosition && showProgressAnimation) {
                        floatingBtn.alpha = 1f
                        floatingBtn.isEnabled = true
                    }

                    if (showOpinions == "1") {
                        opinionsProgressBarView.progress = 0

                    } else {
                        if (position == currentPosition) {
                            if (showProgressAnimation) {
                                Util.createProgressBarAnimation(
                                    opinionsProgressBarView,
                                    similarAnswer.toFloat().toInt(),
                                    similarAnswer.toFloat().toInt() <= 50
                                )
                            } else {
                                opinionsProgressBarView.progress = similarAnswer.toFloat().toInt()
                            }
                        } else {
                            opinionsProgressBarView.progress = 0
                        }

                    }

                    opinionsProgressBarView.progressDrawable = ContextCompat.getDrawable(
                        context,
                        if (showOpinions == "1")
                            R.drawable.dr_only_white_progress
                        else if (similarAnswer.toFloat().toInt() <= 50)
                            R.drawable.dr_white_brown_progress else R.drawable.dr_brown_white_progress
                    )

                    val rightPadding =
                        binding.llOpinionText.width - (binding.llOpinionText.width * similarAnswer.toFloat()
                            .toInt() / 100)
                    val leftPadding =
                        binding.llOpinionText.width * similarAnswer.toFloat().toInt() / 100

                    if (showOpinions == "1") {
                        binding.txtOpinions.text = context.getString(R.string.not_available)
                        binding.txtOpinions.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.color_grey
                            )
                        )
                        binding.llOpinionText.setPadding(0, 0, 0, 0)
                    } else {
                        binding.txtOpinions.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.color_white
                            )
                        )
                        if (similarAnswer.toFloat().toInt() <= 50) {
                            binding.llOpinionText.setPadding(leftPadding, 0, 0, 0)
                        } else {
                            binding.llOpinionText.setPadding(0, 0, rightPadding, 0)
                        }
                        binding.txtOpinions.text = context.getString(
                            if (similarAnswer.toFloat()
                                    .toInt() <= 50
                            ) R.string.different else R.string.similar
                        )
                    }


                    // FOR BAN ANIMATION
                    if (position == currentPosition) {
                        if (objList[position].isBanned == "1") {
                            Util.showProgressForAnimation(context)
                            binding.bannedView.visible()
                            binding.bannedLottie.visible()
                            binding.bannedLottie.repeatCount = 0
                            binding.bannedLottie.setAnimation("banned_lottie.json")
                            binding.bannedLottie.playAnimation()
                            binding.bannedLottie.addAnimatorListener(object : AnimatorListener {
                                override fun onAnimationStart(p0: Animator) {
                                    onAnimationEndCalled = false
                                }

                                override fun onAnimationEnd(p0: Animator) {
                                    if (!onAnimationEndCalled) {
                                        Util.print(">>>>>>>>>> IWAS CALLED")
                                        onAnimationEndCalled = true
                                        // Uncomment the line below if you want to remove the profile after animation
                                        removeProfileFromListInterface.removeProfileCallBack(objList[position])
                                    }
                                }

                                override fun onAnimationCancel(p0: Animator) {
                                }

                                override fun onAnimationRepeat(p0: Animator) {
                                }
                            })
                        } else {
                            binding.bannedLottie.animation = null
                            binding.bannedView.gone()
                            binding.bannedLottie.gone()
                        }
                    }

                }



                lblSneakPeak.text = sneakPeakText

                lblSneakPeak.visibleIf(isOpinions != "1")
                txtOpinionsEnable.visibleIf(isOpinions == "1")

//                imgViewSneakPeak.setImageDrawable(
//                        ContextCompat.getDrawable(context,
//                            when(sneakPeakButton){
//                                "1" -> R.drawable.dr_ic_sneak_peak_1
//                                "3" -> R.drawable.dr_ic_sneak_peak_3
//                                "4" -> R.drawable.dr_ic_sneak_peak_1
//                                else /*"2"*/ -> R.drawable.dr_ic_sneak_peak_2
//                            }))

                imgViewSneakPeak.setCardBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        when (sneakPeakButton) {
                            "1", "3", "4" -> R.color.color_blue
                            else /*"2"*/ -> R.color.color_light_blue
                        }
                    )
                )

                imgViewSneakPeak.isEnabled =
                    (sneakPeakButton == "1" || sneakPeakButton == "3" || sneakPeakButton == "4")

                imgSneakPeakDot.visibleIf(sneakPeakButton == "3")


                rlViewBy.visibleIf(sneakPeakViewBy.isNotEmpty())
                imgViewByFirst.visibleIf(sneakPeakViewBy.isNotEmpty())
                imgViewBySecond.visibleIf(sneakPeakViewBy.size > 1)

                if (sneakPeakViewBy.isNotEmpty()) {
                    imgViewByFirst.setImageURI(sneakPeakViewBy[0])
                    if (sneakPeakViewBy.size > 1)
                        imgViewBySecond.setImageURI(sneakPeakViewBy[1])
                }

            }
            setFloatingButton(binding, this)


        }


        binding.txtOpinionsEnable.setOnClickListener(clickListener)
        binding.imgCancel.setOnClickListener(clickListener)
        binding.floatingBtn.setOnClickListener(clickListener)
        binding.txtChatButton.setOnClickListener(clickListener)
        binding.imgViewSneakPeak.setOnClickListener(clickListener)

//        if (FOR_FIRST_TIME)
//            checkVisible(binding.viewForButton,binding.floatingBtn)
//        binding.floatingBtn.visibleIf(buttonVisibility)

        var first = true
        binding.cvProfile.viewTreeObserver.addOnGlobalLayoutListener {

            if (first) {

                val scrollTop = context.resources.getDimension(com.intuit.sdp.R.dimen._30sdp)
                val scrollHeight = binding.scrollView.height - scrollTop.toInt()

                val layoutParams = binding.cvProfile.layoutParams
                layoutParams.height = scrollHeight

                binding.cvProfile.layoutParams = layoutParams
            }

            first = false

        }

    }

    private fun setTextView(
        mainView: ViewGroup,
        view: TextView,
        text: String,
        isProfileLocked: String,
        type: Int = 0
    ) {
        val finalText = if (isProfileLocked == "1") getDummyText(text, type) else text
        view.text = finalText
        mainView.visibility = if (finalText.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun getDummyText(txt: String, type: Int): String {
        var text = txt
        if (text.isNull())
            text = ""
        return when (type) {
            1 -> context.getString(R.string.loading1)
            2 -> context.getString(R.string.loading2)
            3 -> context.getString(R.string.loading3)
            4 -> context.getString(R.string.loading4)
            5 -> context.getString(R.string.loading5)
            else -> if ((text.isEmpty() || text == "-")) context.getString(R.string.loading) else text
        }
    }


    private fun setFloatingButton(binding: ProfileViewItemBinding, model: ProfileModel) {

        binding.floatingBtn.setImageResource(
            when (model.exchangeButton) {
                "1" -> R.drawable.ic_chat //  Able to sent request chat request
                "2" -> R.drawable.ic_chat_error //  ! mark button when user pending onboarding questions and photo album
                "3" -> R.drawable.ic_chat_locked
                "4" -> R.drawable.ic_chat_disabled // sent request and disable button
                "5" -> R.drawable.ic_chat_locked_disabled
                "6" -> R.drawable.ic_chat_received // Chat request received
                "7" -> R.drawable.ic_chat_received_error
                else -> R.drawable.ic_chat_disabled
            }
        )

        binding.txtChatButton.text = context.getString(
            when(model.exchangeButton){
                "1","2","4" -> R.string.send_request
                "3","5" -> R.string.unlock_profile
                "6","7" -> R.string.accept_request
                else -> R.string.send_request
            })

        binding.txtChatButton.backgroundTintList =
            ColorStateList.valueOf(
                ContextCompat.getColor(
                    context,
                    if ((model.exchangeButton == "8" && model.isLike == 1) || model.exchangeButton == "4" || model.exchangeButton == "5") R.color.color_light_blue else R.color.color_blue
                )
            )

        binding.imgCancel.visibility =
            if (model.exchangeButton == "8") View.GONE else View.VISIBLE
        binding.imgCancel.setImageResource(if (model.exchangeButton == "4" || model.exchangeButton == "5") R.drawable.dr_ic_cancel_cross_disabled else R.drawable.dr_ic_cancel_cross)

        binding.imgCancel.isEnabled =
            !((model.exchangeButton == "8" && model.isLike == 1) || model.exchangeButton == "4" || model.exchangeButton == "5")
        binding.txtChatButton.isEnabled =
            !((model.exchangeButton == "8" && model.isLike == 1) || model.exchangeButton == "4" || model.exchangeButton == "5")
        binding.floatingBtn.isEnabled =
            !((model.exchangeButton == "8" && model.isLike == 1) || model.exchangeButton == "4" || model.exchangeButton == "5")

    }

    @SuppressLint("NotifyDataSetChanged")
    fun addData(data: ArrayList<ProfileModel>) {
        objList = ArrayList()
        objList.addAll(data)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateDataAtPosition(position: Int, newData: ProfileModel) {
        objList[position] = newData
        this.notifyDataSetChanged()

    }


    /*@SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.M)
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val inflater = LayoutInflater.from(container.context)
        val binding = ProfileViewItemBinding.inflate(inflater, container, false)

        setView(binding,position)

        container.addView(binding.root)
        return binding.root
    }

    override fun getCount(): Int {
        return objList.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }*/

}