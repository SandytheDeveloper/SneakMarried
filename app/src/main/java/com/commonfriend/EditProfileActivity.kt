package com.commonfriend


import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.ImageSpan
import android.text.style.UnderlineSpan
import android.view.View
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.commonfriend.adapter.EducationAdapter
import com.commonfriend.adapter.ErrorListAdapter
import com.commonfriend.adapter.QuestionOfTheDayAdapter
import com.commonfriend.base.BaseActivity
import com.commonfriend.custom.ErrorDialogComponent
import com.commonfriend.databinding.*
import com.commonfriend.idrequest.ApiRepository
import com.commonfriend.models.ErrorModel
import com.commonfriend.models.UserProfileModel
import com.commonfriend.utils.*
import com.commonfriend.viewmodels.QuestionViewModel
import com.commonfriend.viewmodels.UserViewModel
import com.commonfriend.viewmodels.ViewModelFactory
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.reactivex.internal.util.ErrorMode
import java.lang.Error
import java.util.*
import kotlin.collections.ArrayList


class EditProfileActivity : BaseActivity(), View.OnClickListener,
    ErrorDialogComponent.ErrorBottomSheetClickListener {

    private lateinit var binding: ActivityEditProfileBinding
    private var bottomSheetDialog: BottomSheetDialog? = null
    var lastPos = -1
    private var isFrom: ActivityIsFrom = ActivityIsFrom.NORMAL
    private lateinit var userViewModel: UserViewModel
    private var userProfileArrayList: ArrayList<UserProfileModel> = ArrayList()
    private lateinit var questionViewModel: QuestionViewModel
    private var editField = "" // 1=height, 2=birth_date
    private var forConfirmation: Boolean = false
    private var finishPreview: String = "0"

    private lateinit var profileErrorAdapter : ErrorListAdapter
    private lateinit var nameErrorAdapter : ErrorListAdapter
    private lateinit var titleErrorAdapter : ErrorListAdapter
    private lateinit var firstImageErrorAdapter : ErrorListAdapter
    private lateinit var secondImageErrorAdapter : ErrorListAdapter
    private lateinit var thirdImageErrorAdapter : ErrorListAdapter

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        logForCurrentScreen("edit_profile", Screens.EDIT_PROFILE_SCREEN.value)
        initialization()
// getFace()
    }


    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("ResourceAsColor")
    private fun initialization() {


        if (intent.hasExtra(IS_FROM))
            isFrom = intent.getSerializableExtra(IS_FROM) as ActivityIsFrom
        binding.btnSave.visibility =
            if (isFrom == ActivityIsFrom.FROM_MENU || isFrom == ActivityIsFrom.LOCKED_ACCOUNT) View.GONE else View.VISIBLE

        forConfirmation = (isFrom == ActivityIsFrom.NORMAL)

        // PROFILE
        binding.rvProfileErrors.layoutManager = FlexboxLayoutManager(this).apply { this.justifyContent = JustifyContent.FLEX_END }
        profileErrorAdapter = ErrorListAdapter(this,1)
        binding.rvProfileErrors.adapter = profileErrorAdapter

        // NAME
        binding.rvNameErrors.layoutManager = FlexboxLayoutManager(this).apply { this.justifyContent = JustifyContent.FLEX_START }
        nameErrorAdapter = ErrorListAdapter(this,1)
        binding.rvNameErrors.adapter = nameErrorAdapter

        // TITLE
        binding.rvTitleErrors.layoutManager = FlexboxLayoutManager(this).apply { this.justifyContent = JustifyContent.FLEX_START }
        titleErrorAdapter = ErrorListAdapter(this,2)
        binding.rvTitleErrors.adapter = titleErrorAdapter

        // FIRST IMAGE
        binding.rvFirstAlbumErrors.layoutManager = FlexboxLayoutManager(this).apply { this.justifyContent = JustifyContent.FLEX_END }
        firstImageErrorAdapter = ErrorListAdapter(this,1)
        binding.rvFirstAlbumErrors.adapter = firstImageErrorAdapter

        // SECOND IMAGE
        binding.rvSecondAlbumErrors.layoutManager = FlexboxLayoutManager(this).apply { this.justifyContent = JustifyContent.FLEX_END }
        secondImageErrorAdapter = ErrorListAdapter(this,1)
        binding.rvSecondAlbumErrors.adapter = secondImageErrorAdapter

        // THIRD IMAGE
        binding.rvThirdAlbumErrors.layoutManager = FlexboxLayoutManager(this).apply { this.justifyContent = JustifyContent.FLEX_END }
        thirdImageErrorAdapter = ErrorListAdapter(this,1)
        binding.rvThirdAlbumErrors.adapter = thirdImageErrorAdapter

        allApiResponses()


// open screen in click image view
        with(binding) {

            btnBack.visibleIf(forConfirmation)
            btnCross.visibleIf(!forConfirmation)
            llSkipAll.visibleIf(forConfirmation)

            llSkipAll.setOnClickListener(this@EditProfileActivity)
            btnBack.setOnClickListener(this@EditProfileActivity)
            btnCross.setOnClickListener(this@EditProfileActivity)
            imgEditHeight.setOnClickListener(this@EditProfileActivity)
            imgEditBirth.setOnClickListener(this@EditProfileActivity)
            imgEditRelationShip.setOnClickListener(this@EditProfileActivity)
            imgEditDisability.setOnClickListener(this@EditProfileActivity)
//            imgChangeEducationDetails.setOnClickListener(this@EditProfileActivity)
            llProfileView.setOnClickListener(this@EditProfileActivity)
            imgChangeFirstAlbum.setOnClickListener(this@EditProfileActivity)
            imgChangeSecondAlbum.setOnClickListener(this@EditProfileActivity)
            imgChangeThirdAlbum.setOnClickListener(this@EditProfileActivity)
            imgFirstAlbum.setOnClickListener(this@EditProfileActivity)
            imgSecondAlbum.setOnClickListener(this@EditProfileActivity)
            imgThirdAlbum.setOnClickListener(this@EditProfileActivity)
            btnSave.setOnClickListener(this@EditProfileActivity)
            imgChangePhoto.setOnClickListener(this@EditProfileActivity)
            txtEducationAddDetails.setOnClickListener(this@EditProfileActivity)
        }

        activityOnBackPressed(this,this) {
            onBackPress()
        }

    }


    override fun onResume() {
        super.onResume()

        forConfirmation = isFrom == ActivityIsFrom.NORMAL


        val spannableString = SpannableString(resources.getString(R.string.born_in_bangalore_i_was))
        val clickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun onClick(p0: View) {
                openA<SectionBreakerActivity>()
            }


            @RequiresApi(Build.VERSION_CODES.Q)
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.underlineColor =
                    ContextCompat.getColor(this@EditProfileActivity, R.color.color_red)
            }
        }


        val clickableSpan1: ClickableSpan = object : ClickableSpan() {
            override fun onClick(p0: View) {
                openA<SectionBreakerActivity>()
            }


            @RequiresApi(Build.VERSION_CODES.Q)
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.underlineColor =
                    ContextCompat.getColor(this@EditProfileActivity, R.color.color_black)
            }
        }


        spannableString.setSpan(clickableSpan, 14, 23, 0)
        spannableString.setSpan(clickableSpan1, 49, 58, 0)


        callApi(1)


        if (bottomSheetDialog != null) bottomSheetDialog!!.dismiss()


    }


    private fun onBackPress() {

        when (isFrom) {
            ActivityIsFrom.NOTIFICATION -> {
                openA<MainActivity> { putExtra(DATA, 1) }
                finish()
            }

            ActivityIsFrom.NORMAL -> {
                openA<StepsActivity> {}
                finish()
            }

            else -> finish()
        }
    }

    private fun setTextAndClick(
            textMessage: String,
            textView: TextView,
            iconSize: Boolean = true,
            forName: Boolean = false,
            isLocked: Boolean = false,
            isWorkPlace: Boolean = false)
    {
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(view: View) {
                when (view.id) {
                    R.id.txtName -> {
                        with(userProfileArrayList[0]) {
                            callApi(2, candidateName[0].categoryId, candidateName[0].questionId)
                        }
                    }

                    R.id.txtProfession -> {
                        with(userProfileArrayList[0]) {
                            callApi(
                                2,
                                candidateProfession[0].categoryId,
                                candidateProfession[0].questionId
                            )
                        }
                    }


                    R.id.txtDesignation -> {
                        with(userProfileArrayList[0]) {
                            callApi(
                                2,
                                candidateDesignation[0].categoryId,
                                candidateDesignation[0].questionId
                            )
                        }
                    }

                    /*R.id.txtOffice,*/ R.id.txtIndustry -> {
                        with(userProfileArrayList[0]) {
                            callApi(
                                2,
                                candidateWorkIndustry[0].categoryId,
                                candidateWorkIndustry[0].questionId
                            )
                        }
                    }


                    R.id.txtAnnualIncome -> {
                        with(userProfileArrayList[0]) {
                            callApi(
                                2, candidateEarning[0].categoryId, candidateEarning[0].questionId
                            )
                        }
                    }


                    R.id.txtNetWorth -> {
                        with(userProfileArrayList[0]) {
                            callApi(
                                2, candidateNetWorth[0].categoryId, candidateNetWorth[0].questionId
                            )
                        }
                    }


                    R.id.txtReligion -> {
                        with(userProfileArrayList[0]) {
                            callApi(
                                2, candidateReligion[0].categoryId, candidateReligion[0].questionId
                            )
                        }
                    }


                    R.id.txtCulture -> {
                        with(userProfileArrayList[0]) {
                            callApi(
                                2, candidateCulture[0].categoryId, candidateCulture[0].questionId
                            )
                        }
                    }


                    R.id.txtGender -> {
                        with(userProfileArrayList[0]) {
                            callApi(
                                2,
                                candidateGender[0].categoryId,
                                candidateGender[0].questionId,
                                candidateGender[0].generalId
                            )
                        }
                    }


                    R.id.txtEatingHabits -> {
                        with(userProfileArrayList[0]) {
                            if (candidateEatingHabit.isNotEmpty()) {
                                callApi(
                                        2,
                                        candidateEatingHabit[0].categoryId,
                                        candidateEatingHabit[0].questionId
                                )
                            }
                        }
                    }


                    R.id.txtLocation -> {
                        with(userProfileArrayList[0]) {
                            callApi(
                                2,
                                candidateLivingLocation[0].categoryId,
                                candidateLivingLocation[0].questionId
                            )
                        }
                    }


                    R.id.txtSettleLocations -> {
                        with(userProfileArrayList[0]) {
                            callApi(
                                2,
                                candidateSettleLocation[0].categoryId,
                                candidateSettleLocation[0].questionId
                            )
                        }
                    }
                }
            }
        }


        val drawable: Drawable =
            ContextCompat.getDrawable(this@EditProfileActivity,
                    if (isLocked) if (isWorkPlace) R.drawable.dr_ic_red_edit_locked else R.drawable.dr_ic_white_edit_locked else if (forName) R.drawable.dr_ic_edit_text_black else R.drawable.dr_ic_edit_text)!!

        drawable.setBounds(
            0,
            0,
            resources.getDimension(if (iconSize) com.intuit.sdp.R.dimen._20sdp else com.intuit.sdp.R.dimen._15sdp)
                .toInt(),
            resources.getDimension(if (iconSize) com.intuit.sdp.R.dimen._20sdp else com.intuit.sdp.R.dimen._15sdp)
                .toInt()
        )

        val colorSpan = ForegroundColorSpan(ContextCompat.getColor(this,R.color.color_red))

        val spannableString =
            SpannableStringBuilder(textMessage.ifEmpty { resources.getString(R.string.add_details) })
        if (textMessage.isNotEmpty()) {
            spannableString.append("  ")
            val imageSpan = ImageSpan(drawable, ImageSpan.ALIGN_BOTTOM)
            spannableString.setSpan(
                imageSpan,
                spannableString.length - 1,
                spannableString.length,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE
            )
            spannableString.setSpan(
                clickableSpan,
                spannableString.length - 1,
                spannableString.length,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE
            )

            if (isLocked) {
                spannableString.setSpan(
                        colorSpan, 0, spannableString.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE
                )
            }

            spannableString.append(" ") // To remove click beyond the image
        } else {
            val underLine = UnderlineSpan()
            spannableString.setSpan(
                underLine, 0, spannableString.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE
            )
            spannableString.setSpan(
                clickableSpan, 0, spannableString.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE
            )
            spannableString.setSpan(
                colorSpan, 0, spannableString.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE
            )
        }


        textView.text = spannableString
        textView.movementMethod = LinkMovementMethod.getInstance()
        textView.highlightColor = Color.TRANSPARENT


    }

    private fun addDataToAdapter(recyclerView: RecyclerView,adapter: ErrorListAdapter, errorList : ArrayList<ErrorModel>) {
        recyclerView.visibleIf(errorList.isNotEmpty())
        adapter.addData(errorList)
    }


    @SuppressLint("SetTextI18n")
    private fun setData() {


        with(binding) {
            with(userProfileArrayList[0]) {

                // Profile errors list
                addDataToAdapter(binding.rvProfileErrors,profileErrorAdapter,candidateProfilePic[0].errorList)
                // Name errors list
                addDataToAdapter(binding.rvNameErrors,nameErrorAdapter,candidateName[0].errorList)
                // Title errors list
                addDataToAdapter(binding.rvTitleErrors,titleErrorAdapter,candidateDesignation[0].errorList)

                if (candidateProfilePic[0].profilePic.isNotEmpty()) {
                    imgProfile.setImageURI(candidateProfilePic[0].profilePic)
                    setFilterOnImage(imgProfileFilter,imgChangePhoto,candidateProfilePic[0].isLock,candidateProfilePic[0].isChanged)
                }
                imgBackground.visibleIf(candidateProfilePic[0].profilePic.isNotEmpty())

                if (forConfirmation) {

                    setTextAndClick(
                        "${candidateName[0].firstName} ${candidateName[0].lastName}".lengthTrimming(
                            30
                        ), txtName, forName = true
                    )
                } else if (candidateName[0].isLock == "1" && candidateName[0].isChanged == "0") {

                    setTextAndClick(
                            "${candidateName[0].firstName} ${candidateName[0].lastName}".lengthTrimming(
                                    30
                            ), txtName, forName = true,isLocked = true
                    )

                } else {

                    txtName.text = "${candidateName[0].firstName} ${candidateName[0].lastName}"
                }
                txtAddress.text =
                    if (candidateAge[0].age.isNotEmpty() && candidateCurrentLocation[0].currentLocation.isNotEmpty()) "${candidateAge[0].age}, ${candidateCurrentLocation[0].currentLocation}" else if (candidateAge[0].age.isNotEmpty()) candidateAge[0].age else candidateCurrentLocation[0].currentLocation


                if (forConfirmation) {
                    setTextAndClick(candidateGender[0].gender, txtGender)
                } else {
                    txtGender.text = candidateGender[0].gender
                }
                setTextAndClick(candidateProfession[0].professionName, txtProfession)
                if (candidateProfession[0].professionName.trim().lowercase() == "student" ||
                    candidateProfession[0].professionName.trim().lowercase() == "retired" ||
                    candidateProfession[0].professionName.trim().lowercase() == "future homemaker" ||
                    candidateProfession[0].professionName.trim().lowercase() == "do not work") {
                    llDesignationView.gone()
//                    llOfficeView.gone()
                    llIndustryView.gone()


                } else {
                    llDesignationView.visible()
//                    llOfficeView.visible()
                    llIndustryView.visible()
                    setTextAndClick(candidateDesignation[0].designation, txtDesignation,isLocked = (candidateDesignation[0].isLock == "1" && candidateDesignation[0].isChanged == "0"),isWorkPlace = true)
//                    setTextAndClick(candidateWorkIndustry[0].workplace, txtOffice,isLocked = (candidateWorkIndustry[0].isLock == "1" && candidateWorkIndustry[0].isChanged == "0"),isWorkPlace = true)
                    setTextAndClick(candidateWorkIndustry[0].industry, txtIndustry)
                }

//                lblOffice.text = candidateWorkIndustry[0].title

                setTextAndClick(candidateEarning[0].income, txtAnnualIncome)
                setTextAndClick(candidateNetWorth[0].networth, txtNetWorth)
                setTextAndClick(candidateReligion[0].religion, txtReligion)
                setTextAndClick(candidateCulture[0].culture, txtCulture)
                setTextAndClick(if (candidateEatingHabit.isNotEmpty()) candidateEatingHabit[0].habitEating else "Loading...", txtEatingHabits)


                setTextAndClick(
                    candidateSettleLocation[0].settleLocation.joinToString(separator = COMMA_SPACE_SEPERATOR),
                    txtSettleLocations
                )

                lblSettleLocations.text =
                    getString(if (candidateSettleLocation[0].settleLocation.size > 1) R.string.settle_locations else R.string.settle_location)


                val spannableString = SpannableStringBuilder(resources.getString(R.string.add))
                val underLine = UnderlineSpan()
                val colorSpan = ForegroundColorSpan(Color.RED)
                spannableString.setSpan(
                    underLine, 0, spannableString.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE
                )
                spannableString.setSpan(
                    colorSpan, 0, spannableString.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE
                )

                imgEditHeight.visibleIf(userProfileArrayList[0].candidateHeight[0].isEditable == "1")
                imgEditBirth.visibleIf(userProfileArrayList[0].candidateDob[0].isEditable == "1")

                txtHeight.text = if (candidateHeight[0].height.isNotEmpty()) {
                    val feet = candidateHeight[0].height.toFloat() / 12
                    val inches = candidateHeight[0].height.toFloat() % 12
                    "${feet.toInt()}'${inches.toInt()} ft"
                } else spannableString


                txtBirthDate.text = candidateDob[0].dob.ifEmpty { spannableString }
                txtRelationShip.text = candidateRelationship[0].relation.ifEmpty { spannableString }
                txtDisability.text = candidateDisability[0].disability.capitalizedFirstLetter()
                    .ifEmpty { spannableString }

                if (albumScreenType == "1") {

                    when (albumList.size) {
                        1 -> {
                            imgFirstAlbum.setImageURI(albumList[0].imgUrl)
                            setFilterOnImage(imgFirstAlbumFilter, imgChangeFirstAlbum, albumList[0].isLock, albumList[0].isChanged)
                        }


                        2 -> {
                            imgFirstAlbum.setImageURI(albumList[0].imgUrl)
                            imgSecondAlbum.setImageURI(albumList[1].imgUrl)

                        setFilterOnImage(imgFirstAlbumFilter,imgChangeFirstAlbum,albumList[0].isLock,albumList[0].isChanged)
                        setFilterOnImage(imgSecondAlbumFilter,imgChangeSecondAlbum,albumList[1].isLock,albumList[1].isChanged)

                        }


                        3 -> {
                            imgFirstAlbum.setImageURI(albumList[0].imgUrl)
                            imgSecondAlbum.setImageURI(albumList[1].imgUrl)
                            imgThirdAlbum.setImageURI(albumList[2].imgUrl)

                        setFilterOnImage(imgFirstAlbumFilter,imgChangeFirstAlbum,albumList[0].isLock,albumList[0].isChanged)
                        setFilterOnImage(imgSecondAlbumFilter,imgChangeSecondAlbum,albumList[1].isLock,albumList[1].isChanged)
                        setFilterOnImage(imgThirdAlbumFilter,imgChangeThirdAlbum,albumList[2].isLock,albumList[2].isChanged)

                        }
                    }
                } else {
                    llAlbumFirst.gone()
                    llSecondAlbum.gone()
                    llThirdAlbum.gone()
                }


                imgChangeFirstAlbum.visibility =
                    if (albumList.size > 0) View.VISIBLE else View.GONE
                imgChangeSecondAlbum.visibility =
                    if (albumList.size > 1) View.VISIBLE else View.GONE
                imgChangeThirdAlbum.visibility =
                    if (albumList.size > 2) View.VISIBLE else View.GONE


                imgFirstAlbum.isEnabled = albumList.size <= 0
                imgSecondAlbum.isEnabled = albumList.size <= 1
                imgThirdAlbum.isEnabled = albumList.size <= 2


                rvEducationList.visibleIf(candidateEducation[0].education.isNotEmpty())
                txtEducationAddDetails.visibleIf(candidateEducation[0].education.isEmpty())
                rvEducationList.layoutManager = LinearLayoutManager(this@EditProfileActivity)
                val educationAdapter = EducationAdapter(this@EditProfileActivity,clickListener = this@EditProfileActivity,isFromEditProfile = true)
                rvEducationList.adapter = educationAdapter
                educationAdapter.addData(candidateEducation[0].education)
            }
        }


    }

    private fun setFilterOnImage(redFilterView: View, editButton : AppCompatImageView, isLock : String, isChanged : String) {
        redFilterView.visibleIf(isLock == "1" && isChanged == "0")
        editButton.setImageResource(
                if (isLock == "1" && isChanged == "0") R.drawable.dr_ic_white_edit_locked else R.drawable.dr_ic_edit_text)
    }


    private fun allApiResponses() {
        userViewModel = ViewModelProvider(
            this@EditProfileActivity, ViewModelFactory(ApiRepository())
        )[UserViewModel::class.java]


        questionViewModel = ViewModelProvider(
            this, ViewModelFactory(ApiRepository())
        )[QuestionViewModel::class.java]


        userViewModel.userProfileResponse.observe(this@EditProfileActivity) {
            Util.dismissProgress()
            if (it.success == 1) {
                if (it.data.isNotNull() && it.data.isNotEmpty()) {
                    if (finishPreview == "0") {
                        userProfileArrayList = it.data
                        setData()
                        binding.nestedScrollView.visible()
                    } else {
                        finishPreview = "0"
                        this.openA<ChecklistActivity>()
                    }
                }
            }
        }

        userViewModel.serviceDataResponse.observe(this@EditProfileActivity) {
            Util.dismissProgress()
        }


        questionViewModel.getSingleQuestionsResponse.observe(this) {
            bottomSheetDialog?.dismiss()
            CATEGORY_ID = 0
            LAST_POS = 0
            if (mainObjList.isNotEmpty()) {
                Util.templateMovement(this@EditProfileActivity, ActivityIsFrom.FROM_EDIT)
            }
        }


        userViewModel.sendEditProfileApiResponse.observe(this) {
            openA<VerificationDoneActivity> { putExtra(IS_FROM, ActivityIsFrom.FROM_EDIT) }
        }
    }


    @SuppressLint("CheckResult")
    private fun callApi(
        tag: Int, categoryId: String = "", questionId: String = "", generalId: String = ""
    ) {
        if (Util.isOnline(this)) {


            Util.showProgress(this)
            when (tag) {
                1 -> {
                    userViewModel.getProfileApiRequest(this@EditProfileActivity, finishPreview)
                }


                2 -> {
                    questionViewModel.getSingleQuestionListApiRequest(
                        this, categoryId, questionId, generalId
                    )
                }

            }
        } else {
            Util.showToastMessage(
                this@EditProfileActivity,
                resources.getString(R.string.please_check_internet_connection),
                true
            )
        }
    }


    private fun bottomSheetView(fromHeight : Boolean) {

        val title = if (fromHeight) getString(R.string.height_) else getString(R.string.date_of_birth)
        val subTitle = getString(
            R.string.your_can_be_edited_only_once_be_sure_before_you_confirm_this_change,
            title
        )

        errorDialogComponent = ErrorDialogComponent(
            this,
            ErrorDialogComponent.ErrorDialogFor.CHANGE_CONFIRMATION_LIST,
            title,
            subTitle,
            this
        ).apply {
            this.show()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnBack,R.id.btnCross -> {
                onBackPress()
            }

            R.id.imgChangeEducationDetails,R.id.txtEducationAddDetails -> {
                with(userProfileArrayList[0]) {
                    callApi(2, candidateEducation[0].categoryId, candidateEducation[0].questionId)
                }
            }


            R.id.btnSave -> {
                if (forConfirmation) {
                    finishPreview = "1"
                    callApi(1)
                } else {
                    finish()
                }
            }

            R.id.imgEditHeight -> {
                if (isFrom != ActivityIsFrom.NORMAL) {
                    editField = "1"
                    bottomSheetView(true)
                } else
                    callApi(
                        2,
                        userProfileArrayList[0].candidateHeight[0].categoryId,
                        userProfileArrayList[0].candidateHeight[0].questionId
                    )

            }

            R.id.imgEditBirth -> {
                if (isFrom != ActivityIsFrom.NORMAL) {
                    editField = "2"
                    bottomSheetView(false)
                } else
                    callApi(
                        2,
                        userProfileArrayList[0].candidateDob[0].categoryId,
                        userProfileArrayList[0].candidateDob[0].questionId
                    )
            }

            R.id.imgEditRelationShip -> {
                with(userProfileArrayList[0]) {
                    callApi(
                        2,
                        candidateRelationship[0].categoryId,
                        candidateRelationship[0].questionId
                    )
                }
            }

            R.id.imgEditDisability -> {
                with(userProfileArrayList[0]) {
                    callApi(
                        2,
                        candidateDisability[0].categoryId,
                        candidateDisability[0].questionId
                    )
                }

            }

            R.id.imgChangePhoto, R.id.imgChangeFirstAlbum, R.id.imgChangeSecondAlbum, R.id.imgChangeThirdAlbum, R.id.imgFirstAlbum, R.id.imgSecondAlbum, R.id.imgThirdAlbum -> {
                openA<PhotoAlbumActivity> { putExtra(IS_FROM, ActivityIsFrom.FROM_EDIT) }

            }

            R.id.llSkipAll -> {
                Util.skipAndOpenHomeScreen(this)
            }
        }
    }

    override fun onItemClick(itemID: String, isFrom: ErrorDialogComponent.ErrorDialogFor?) {
        errorDialogComponent?.dismiss()

        when(itemID) {
            "0"-> {
                Util.print(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>WAS CLICKED WAS CLICKED>>>>>>>>${editField}")
                callApi(
                    2,
                    if (editField == "1") userProfileArrayList[0].candidateHeight[0].categoryId else userProfileArrayList[0].candidateDob[0].categoryId,
                    if (editField == "1") userProfileArrayList[0].candidateHeight[0].questionId else userProfileArrayList[0].candidateDob[0].questionId)
            }
            "1"->{

            }
        }
    }


//    fun detectFaceFromImage() {
//
//
//        val url = URL("https://i.natgeofe.com/n/548467d8-c5f1-4551-9f58-6817a8d2c45e/NationalGeographic_2572187_square.jpg")
//        val bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream())
//        val image = InputImage.fromBitmap(bitmap,0)
//
//
//        val detector = FaceDetection.getClient()
//
//
//        val result = detector.process(image)
//            .addOnSuccessListener { faces ->
//
//
//                Util.print("$faces")
//
//
//            }
//            .addOnFailureListener { e ->
//
//
//                e.printStackTrace()
//            }
//
//
//// Configure the face detector.
//// val options = FaceDetectorOptions.Builder()
//// .setLandmarkMode(FaceDetectorOptions.LandmarkMode())
//// .setClassificationMode(FaceDetectorOptions.ClassificationMode.ALL)
//// .build()
//// faceDetector.setOptions(options)
//
//
//// Detect faces in the image.
//// val faces = faceDetector.detectInImage(bitmap)
//
//
//    }


    /*private fun getFace() {


    val url = URL("https://i.natgeofe.com/n/548467d8-c5f1-4551-9f58-6817a8d2c45e/NationalGeographic_2572187_square.jpg")
    val image = BitmapFactory.decodeStream(url.openConnection().getInputStream())


    val firebaseVisionImage = FirebaseVisionImage.fromBitmap(image)


    val options = FirebaseVisionFaceDetectorOptions.Builder()
    .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
    .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
    .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
    .setMinFaceSize(0.15f)
    .build()


    // Initialize the face detector.
    val faceDetector: FirebaseVisionFaceDetector = FirebaseVision.getInstance()
    .getVisionFaceDetector(options)


    // Detect faces in the image.
    faceDetector.detectInImage(firebaseVisionImage)
    .addOnSuccessListener(OnSuccessListener<List<FirebaseVisionFace>> { faces ->
    // Process the detected faces.
    // processFaces(image, faces)


    println("--------faces-----$faces--------------")
    })
    .addOnFailureListener { e ->
    e.printStackTrace()
    // Handle the error.
    }


    }*/


}

