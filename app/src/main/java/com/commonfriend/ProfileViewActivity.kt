package com.commonfriend

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ImageSpan
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.commonfriend.adapter.EducationAdapter
import com.commonfriend.base.BaseActivity
import com.commonfriend.databinding.ActivityProfileViewBinding
import com.commonfriend.idrequest.ApiRepository
import com.commonfriend.models.ProfileModel
import com.commonfriend.utils.ActivityIsFrom
import com.commonfriend.utils.DATA
import com.commonfriend.utils.ID
import com.commonfriend.utils.IS_FROM
import com.commonfriend.utils.Pref
import com.commonfriend.utils.Util
import com.commonfriend.utils.activityOnBackPressed
import com.commonfriend.utils.gone
import com.commonfriend.utils.openA
import com.commonfriend.utils.visible
import com.commonfriend.utils.visibleIf
import com.commonfriend.viewmodels.ProfileViewModel
import com.commonfriend.viewmodels.ViewModelFactory

class ProfileViewActivity : BaseActivity() ,View.OnClickListener {

    lateinit var binding : ActivityProfileViewBinding
    private lateinit var profileViewModel: ProfileViewModel
    private var profileId = ""
    private lateinit var profileData : ProfileModel
    private var showProgressAnimation = true
    private var ALBUM_SCREEN_TYPE = ""
    private var isFrom: ActivityIsFrom = ActivityIsFrom.NORMAL
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initialization()
    }

    private fun initialization() {


        if (intent.hasExtra(ID))
            profileId = intent.getStringExtra(ID).toString()

        if (intent.hasExtra(IS_FROM))
            isFrom = intent.getSerializableExtra(IS_FROM) as ActivityIsFrom

        allApiResponses()

        binding.btnCross.setOnClickListener(this)

        activityOnBackPressed(this,this){
            onBackPress()
        }
    }

    private fun onBackPress() {

        if (isFrom == ActivityIsFrom.NOTIFICATION) {
            openA<MainActivity>()
        }
        finish()

    }

    override fun onResume() {
        super.onResume()
        callApi(1)
    }

    @SuppressLint("SetTextI18n")
    private fun setProfileData() {
        with(profileData) {
            with(binding.profileView) {

                val drawable: Drawable = ContextCompat.getDrawable(this@ProfileViewActivity, R.drawable.dr_ic_verified)!!
                drawable.setBounds(
                    0, 0,
                    resources.getDimension(com.intuit.sdp.R.dimen._15sdp).toInt(),
                    resources.getDimension(com.intuit.sdp.R.dimen._15sdp).toInt()
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

                txtName.text = if (isAadharVerified == "1") spannableString else name

                // 1 - locked , 0 - visible
                if (isProfileLocked == "0") {
                    if (profilePic.isNotEmpty())
                        imgProfile.setImageURI(profilePic)
                } else {
                    imgProfile.setImageResource(R.drawable.dr_ic_profile_lock)
                }
                imgBackground.visibleIf(isProfileLocked == "0" && profilePic.isNotEmpty())

                // For Lock Profile
                cvName.visibility = View.GONE

                cvDesignation.visibility = View.GONE
                cvOffice.visibility = View.GONE
                cvIndustry.visibility = View.GONE
                cvReligion.visibility = View.GONE
                cvHabits.visibility = View.GONE

                // Always Visible
                cvProfession.visibility = View.GONE
                cvCulture.visibility = View.GONE
                cvZodiac.visibility = View.GONE
                cvLocation.visibility = View.GONE

                llStatus.visibility = View.GONE
                imgIntroduced.visibility = if (status == "2") View.VISIBLE else View.GONE

                txtAddress.text = "$age, $currentLocation"
                txtAddress.setTextColor(ContextCompat.getColor(
                        this@ProfileViewActivity,R.color.color_white))

                llHeight.visibility = if (height.isEmpty()) View.GONE else View.VISIBLE
                if (height.isNotEmpty()) {
                    val centimeters = height.toFloat() * 2.54
                    val feet = centimeters / 30.48
                    val inches = (feet - feet.toInt()) * 12
                    txtHeight.text = "${feet.toInt()}'${inches.toInt()} ft"
                }


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

                imgFinanceUserImage.setImageURI(Pref.getStringValue(Pref.PREF_USER_DISPLAY_PICTURE,""))
                imgFinanceProfileImage.setImageURI(profilePic)

                if (financePercentage == -1) {
                    incomeProgressBarView.progressDrawable.setColorFilter(
                        ContextCompat.getColor(
                            this@ProfileViewActivity,
                            R.color.color_light_blue
                        ), PorterDuff.Mode.SRC_IN
                    )
                } else {

                    incomeProgressBarView.progressDrawable = ContextCompat.getDrawable(
                        this@ProfileViewActivity,
                        if (financePercentage >= 50) R.drawable.dr_brown_white_progress else R.drawable.dr_white_brown_progress
                    )

                }

                llLocation.visibility = if (settleLocation.isEmpty()) View.GONE else View.VISIBLE
                txtLocation.text = settleLocation
                lblSettleLocation.text = getString(if (settleLocation.contains(",")) R.string.settle_locations else R.string.settle_location)


                setTextView(llProfession, txtProfession, professions)
                setTextView(llDesignation, txtDesignation, designation)
                setTextView(llOffice, txtOffice, office)
                setTextView(llIndustry, txtIndustry, industry)
                setTextView(llReligion, txtReligion, religions)
                setTextView(llCulture, txtCulture, culture)
                setTextView(llHabits, txtHabits, eatingHabit)
                setTextView(llZodiac, txtZodiac, zodiac)


                lblOffice.text = workPlaceTitle

                llEducation.visibility =
                    if (educationList.isEmpty()) View.INVISIBLE else View.VISIBLE
                rvEducationList.layoutManager = LinearLayoutManager(this@ProfileViewActivity)
                val educationAdapter = EducationAdapter(this@ProfileViewActivity)
                rvEducationList.adapter = educationAdapter
                educationAdapter.addData(educationList)

                if (ALBUM_SCREEN_TYPE == "1") {

                    if (isProfileLocked == "1") {

                        imgAlbumFirst.setImageResource(R.drawable.dr_ic_profile_lock)
                        imgAlbumSecond.setImageResource(R.drawable.dr_ic_profile_lock)
                        imgAlbumThird.setImageResource(R.drawable.dr_ic_profile_lock)

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
                        this@ProfileViewActivity,
                        if (opinionView == "2" || opinionView == "3") R.color.color_light_grey else R.color.color_base_grey
                    )
                )


                if (isProfileLocked == "1" || showOpinions == "1"){
                    opinionsProgressBarView.progress = 0

                } else {

                    Util.print(">>>>>>>>>>>>>>>>>>>>>>>>>>SIMILAR ANS IS >>>>>>>>>>>>>>${similarAnswer.toFloat()}")

                    if (showProgressAnimation) {
                        Util.createProgressBarAnimation(
                            opinionsProgressBarView,
                            similarAnswer.toFloat().toInt(),
                            similarAnswer.toFloat().toInt() <= 50)
                    } else {
                        opinionsProgressBarView.progress = similarAnswer.toFloat().toInt()
                    }
                    showProgressAnimation = false

                }

                opinionsProgressBarView.progressDrawable = ContextCompat.getDrawable(
                    this@ProfileViewActivity,
                    if (isProfileLocked == "1" || showOpinions == "1")
                        R.drawable.dr_brown_white_progress
                    else if (similarAnswer.toFloat().toInt() <= 50)
                        R.drawable.dr_white_brown_progress else R.drawable.dr_brown_white_progress
                )

                llOpinionText.viewTreeObserver.addOnGlobalLayoutListener {
                    val rightPadding = llOpinionText.width - (llOpinionText.width * similarAnswer.toFloat().toInt() / 100)
                    val leftPadding = llOpinionText.width * similarAnswer.toFloat().toInt() / 100

                    if (isProfileLocked == "1" || showOpinions == "1") {
                        txtOpinions.text = getString(R.string.not_available)
                        txtOpinions.setTextColor(
                            ContextCompat.getColor(this@ProfileViewActivity,
                                R.color.color_grey))
                        llOpinionText.setPadding(0,0,0,0)
                    } else {
                        txtOpinions.setTextColor(
                            ContextCompat.getColor(this@ProfileViewActivity,
                                R.color.color_white))
                        if (similarAnswer.toFloat().toInt() <= 50) {
                            llOpinionText.setPadding(leftPadding,0,0,0)
                        } else {
                            llOpinionText.setPadding(0,0,rightPadding,0)
                        }
                        txtOpinions.text = getString( if (similarAnswer.toFloat().toInt() <= 50) R.string.different else R.string.similar)
                    }
                }




                imgSneakPeak.imageTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        this@ProfileViewActivity,
                        if (opinionView == "2" || opinionView == "3") R.color.color_grey else R.color.color_black
                    )
                )

                lblSneakPeak.text = sneakPeakText

                lblSneakPeak.visibleIf(isOpinions != "1")
                txtOpinionsEnable.gone()

//                imgViewSneakPeak.setImageDrawable(
//                    ContextCompat.getDrawable(this@ProfileViewActivity,
//                        when(sneakPeakButton){
//                            "1" -> R.drawable.dr_ic_sneak_first
//                            "3" -> R.drawable.dr_ic_sneak_third
//                            "4" -> R.drawable.dr_ic_sneak_fourth
//                            else /*"3"*/ -> R.drawable.dr_ic_sneak_second
//                        }))

                imgViewSneakPeak.setCardBackgroundColor(
                    ContextCompat.getColor(this@ProfileViewActivity,
                        when(sneakPeakButton) {
                            "1","3","4" -> R.color.color_blue
                            else /*"2"*/ -> R.color.color_light_blue
                        }))

                imgViewSneakPeak.isEnabled =
                    (sneakPeakButton == "1" || sneakPeakButton == "3" || sneakPeakButton == "4")

                imgSneakPeakDot.visibleIf(sneakPeakButton == "3")


                rlViewBy.visibleIf(sneakPeakViewBy.isNotEmpty())
                imgViewByFirst.visibleIf(sneakPeakViewBy.isNotEmpty())
                imgViewBySecond.visibleIf(sneakPeakViewBy.size > 1)

                if (sneakPeakViewBy.isNotEmpty()){
                    imgViewByFirst.setImageURI(sneakPeakViewBy[0])
                    if (sneakPeakViewBy.size > 1)
                        imgViewBySecond.setImageURI(sneakPeakViewBy[1])
                }






                floatingBtn.visibility = View.GONE
                llChatButton.visibility = View.GONE

                imgViewSneakPeak.setOnClickListener(this@ProfileViewActivity)

            }

        }

    }

    private fun setTextView(
        mainView: ViewGroup,
        view: TextView,
        text: String,
    ) {
        view.text = text
        mainView.visibility = if (text.isEmpty()) View.GONE else View.VISIBLE
    }


    fun allApiResponses() {
        profileViewModel = ViewModelProvider(
            this,
            ViewModelFactory(ApiRepository())
        )[ProfileViewModel::class.java]

        profileViewModel.profileListApiResponse.observe(this) {
            if (it.success == 1 && it.data.isNotEmpty()) {
                if (it.data[0].profileData.isNotEmpty()) {
                    ALBUM_SCREEN_TYPE = it.data[0].albumScreenType
                    profileData = it.data[0].profileData[0]
                    setProfileData()
                    binding.profileView.root.visibleIf(it.data[0].profileData.isNotEmpty())
                    callApi(3,false) // read Profile
                }
            }
        }

        profileViewModel.readSneakPeakApiResponse.observe(this) {
            if (it.data.isNotEmpty()) {
                this.let { activity ->
                    activity.bundle = Bundle().apply {
                        putString("request_from", Pref.getStringValue(Pref.PREF_USER_ID, ""))
                        putString("request_to", profileData.id)
                        putString(
                            "gen_from_to",
                            Util.genderInitalsForFirebase(profileData.gender)
                        )
                        putString("cf_id", Pref.getStringValue(Pref.PREF_USER_ID, ""))
                        putString("match_status", profileData.statusText)
                        putString(
                            "similar_percentage", profileData.similarAnswer
                        )
                        putString(
                            "common_questions", profileData.commonQuestions
                        )
                        putString(
                            "opinion_status", it.data[0].sneakPeakStatus)
                        putString(
                            "user_status", profileData.userStatus
                        )
                        putString(
                            "recommendation_type",
                            if (profileData.isProfileLocked == "1") "Locked" else "Unlocked"
                        )
                        putString(
                            "request_type",
                            if (profileData.exchangeButton == "3") "recommendations" else "chat"
                        )
                    }
                    activity.firebaseEventLog("view_opinions",activity.bundle)
                }
            }
        }

        profileViewModel.readExchangedProfileResponse.observe(this) {
        }
    }

    fun callApi(tag: Int, showProgress: Boolean = true) {
        if (Util.isOnline(this)) {
            if (showProgress) Util.showProgress(this)
            when (tag) {
                1 -> {
                    profileViewModel.profileListApiRequest(this,"",profileId)
                }
                2 -> {
                    profileViewModel.readSneakPeakApiRequest(this,profileId)
                }
                3 -> {
                    profileViewModel.readExchangedProfileApiRequest(
                        this,
                        profileData.id,
                        profileData.isBanned,
                    )
                }
            }
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        onBackPressedDispatcher.onBackPressed()
    }

    override fun onClick(view: View) {
        when(view.id){
            R.id.imgViewSneakPeak -> {
                callApi(2,false)
                startActivity(
                    Intent(this, SneakPeekActivity::class.java).putExtra(IS_FROM, ActivityIsFrom.SNEAK_PEAK)
                        .putExtra(ID,profileId)
                        .putExtra(DATA,profileData.profilePic)
                )
            }
            R.id.btnCross -> {
                onBackPress()
            }
        }

    }

}