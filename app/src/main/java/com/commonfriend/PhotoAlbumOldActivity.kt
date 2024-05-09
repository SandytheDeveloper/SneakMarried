package com.commonfriend

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.View
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.commonfriend.adapter.ErrorImageAdapter
import com.commonfriend.adapter.MultiplePhotosAdapter
import com.commonfriend.base.BaseActivity
import com.commonfriend.custom.ErrorDialogComponent
import com.commonfriend.custom.OnStartDragListener
import com.commonfriend.databinding.ActivityPhotoAlbumOldBinding
import com.commonfriend.idrequest.ApiRepository
import com.commonfriend.models.ImageModel
import com.commonfriend.models.PhotoModel
import com.commonfriend.utils.ALBUM_LAST_POS
import com.commonfriend.utils.ActivityIsFrom
import com.commonfriend.utils.DATA
import com.commonfriend.utils.IS_FROM
import com.commonfriend.utils.ImagePicker
import com.commonfriend.utils.Pref
import com.commonfriend.utils.REQUEST_CODE
import com.commonfriend.utils.REQUEST_CODE_1
import com.commonfriend.utils.REQUEST_CODE_2
import com.commonfriend.utils.SELECTED_IMAGE
import com.commonfriend.utils.Screens
import com.commonfriend.utils.Util
import com.commonfriend.utils.checkResolution
import com.commonfriend.utils.gone
import com.commonfriend.utils.visible
import com.commonfriend.viewmodels.UploadImageViewModel
import com.commonfriend.viewmodels.UserViewModel
import com.commonfriend.viewmodels.ViewModelFactory
import com.yalantis.ucrop.UCrop
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.lang.StringBuilder
import java.util.*
import kotlin.collections.ArrayList


class PhotoAlbumOldActivity : BaseActivity(), View.OnClickListener, OnStartDragListener,
    ErrorDialogComponent.ErrorBottomSheetClickListener {

    private lateinit var binding: ActivityPhotoAlbumOldBinding
    private lateinit var multiplePhotosAdapter: MultiplePhotosAdapter
    private var mItemTouchHelper: ItemTouchHelper? = null
    private lateinit var userViewModel: UserViewModel
    private var lastPos = -1
    var photoModel: PhotoModel? = null
    var REQUEST_TEXT = 1
    var multiFaces = false
    private var mainImage = ""
    private var isFrom: ActivityIsFrom = ActivityIsFrom.NORMAL

    lateinit var touchHelper: ItemTouchHelper
    private var isProfilePic = ""
    private var isRecycleClickable = true

    private lateinit var errorImageAdapter: ErrorImageAdapter
    private var urls = ""
    private var firstView = true

    fun get(): ActivityPhotoAlbumOldBinding {
        return binding
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPhotoAlbumOldBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Util.statusBarColor(this, window)

        initializations()

    }

    private fun initializations() {
        manageView(true)
        if (intent.hasExtra(IS_FROM))
            isFrom = intent.getSerializableExtra(IS_FROM) as ActivityIsFrom

        binding.buttonView.btnContinue.setOnClickListener(this)
        binding.buttonView.btnSkip.visibility = View.GONE

//        binding.customHeader.get().txtTitle.text =
//            resources.getText(if (isFrom == ActivityIsFrom.ASSOCIATE_ALBUM) R.string.my_profile else R.string.my_photo_album)

        binding.customHeader.get().btnLeft.visibility =
            if (isFrom == ActivityIsFrom.NORMAL) View.VISIBLE else View.GONE
        binding.customHeader.get().imgCross.visibility =
            if (isFrom != ActivityIsFrom.NORMAL) View.VISIBLE else View.GONE
        binding.customHeader.get().view.visibility = View.VISIBLE
        binding.customHeader.get().txtPageNO.visibility = View.GONE


//        binding.txtQuestion.text = Util.applyCustomFonts(
//            this,
//            null,
//            resources.getString(if (isFrom == ActivityIsFrom.ASSOCIATE_ALBUM) R.string.share_your_profile else R.string.share_your_photo_album),
//            resources.getString(if (isFrom == ActivityIsFrom.ASSOCIATE_ALBUM) R.string.profile_ else R.string.photo_album)
//        )

        binding.customHeader.get().progressBar.visibility = View.GONE
        binding.rvImageView.layoutManager =
            GridLayoutManager(
                this,
                if (isFrom == ActivityIsFrom.ASSOCIATE_ALBUM) 1 else 2,
                GridLayoutManager.VERTICAL,
                false
            )
        multiplePhotosAdapter = MultiplePhotosAdapter(this, this)
        binding.rvImageView.adapter = multiplePhotosAdapter

        touchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or
                    ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT, 0
        ) {

            var changes = 0

            override fun onMove(
                p0: RecyclerView,
                p1: RecyclerView.ViewHolder,
                p2: RecyclerView.ViewHolder
            ): Boolean {

                Util.print("-------------------------------------Working1")
                val sourcePosition = p1.adapterPosition
                val targetPosition = p2.adapterPosition

                if (targetPosition == 0) {
                    return false
                }

                if (sourcePosition < targetPosition) {
                    for (i in sourcePosition until targetPosition) {
                        Collections.swap(multiplePhotosAdapter.objList, i, i + 1)
                    }
                } else {
                    for (i in sourcePosition downTo targetPosition + 1) {
                        Collections.swap(multiplePhotosAdapter.objList, i, i - 1)
                    }
                }

                multiplePhotosAdapter.notifyItemMoved(sourcePosition, targetPosition)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                Util.print("-------------------------------------Working2")
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)
                Util.print("-------------------------------------Working3")
                if (viewHolder != null && viewHolder.adapterPosition == 0) {
                    multiplePhotosAdapter.notifyDataSetChanged()
                    return
                }
            }

            override fun clearView(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ) {
                super.clearView(recyclerView, viewHolder)
                Util.print("--------------------------------------Working4")
                changes = if (changes == 1) {
                    multiplePhotosAdapter.setData()
                    0
                } else 1
                saveAlbum()
            }
        })

        touchHelper.attachToRecyclerView(if (isFrom == ActivityIsFrom.ASSOCIATE_ALBUM) null else binding.rvImageView)

        allApiResponses()
        callApi(1)

    }


    @SuppressLint("NotifyDataSetChanged")
    private fun choosePhotos(imageList: ArrayList<ImageModel>) {

        var imageModel = ImageModel()
        imageModel.id = -1
        imageModel.count = "Display Picture"
        imageModel.mainImage = if (imageList.size > 0) imageList[0].mainImage else ""
        imageModel.imgUrl = if (imageList.size > 0) imageList[0].imgUrl else ""
        imageModel.fromGroupImage = if (imageList.size > 0) imageList[0].fromGroupImage else ""
        imageModel.isProfilePic = if (imageList.size > 0) imageList[0].isProfilePic else ""
        imageModel.index = if (imageList.size > 0) imageList[0].index else 0
        imageModel.isValidDisplayPic =
            if (imageList.size > 0) imageList[0].isValidDisplayPic else ""
        imageModel.errorMessage = if (imageList.size > 0) imageList[0].errorMessage else ""
        imageModel.subMessage = if (imageList.size > 0) imageList[0].subMessage else ""
        imageModel.handWrittenTexts =
            if (imageList.size > 0) imageList[0].handWrittenTexts else ArrayList()
        multiplePhotosAdapter.objList.add(imageModel)

        imageModel = ImageModel()
        imageModel.id = 1
        imageModel.count = "1"
        imageModel.mainImage = if (imageList.size > 1) imageList[1].mainImage else ""
        imageModel.imgUrl = if (imageList.size > 1) imageList[1].imgUrl else ""
        imageModel.fromGroupImage = if (imageList.size > 1) imageList[1].fromGroupImage else ""
        imageModel.isProfilePic = if (imageList.size > 1) imageList[1].isProfilePic else ""
        imageModel.index = if (imageList.size > 1) imageList[1].index else 0
        imageModel.isValidDisplayPic =
            if (imageList.size > 1) imageList[1].isValidDisplayPic else ""
        imageModel.errorMessage = if (imageList.size > 1) imageList[1].errorMessage else ""
        imageModel.subMessage = if (imageList.size > 1) imageList[1].subMessage else ""
        imageModel.handWrittenTexts =
            if (imageList.size > 1) imageList[1].handWrittenTexts else ArrayList()
        multiplePhotosAdapter.objList.add(imageModel)

        imageModel = ImageModel()
        imageModel.id = 2
        imageModel.count = "2"
        imageModel.mainImage = if (imageList.size > 2) imageList[2].mainImage else ""
        imageModel.imgUrl = if (imageList.size > 2) imageList[2].imgUrl else ""
        imageModel.fromGroupImage = if (imageList.size > 2) imageList[2].fromGroupImage else ""
        imageModel.isProfilePic = if (imageList.size > 2) imageList[2].isProfilePic else ""
        imageModel.index = if (imageList.size > 2) imageList[2].index else 0
        imageModel.isValidDisplayPic =
            if (imageList.size > 2) imageList[2].isValidDisplayPic else ""
        imageModel.errorMessage = if (imageList.size > 2) imageList[2].errorMessage else ""
        imageModel.subMessage = if (imageList.size > 2) imageList[2].subMessage else ""
        imageModel.handWrittenTexts =
            if (imageList.size > 2) imageList[2].handWrittenTexts else ArrayList()
        multiplePhotosAdapter.objList.add(imageModel)

        imageModel = ImageModel()
        imageModel.id = 3
        imageModel.count = "3"
        imageModel.mainImage = if (imageList.size > 3) imageList[3].mainImage else ""
        imageModel.imgUrl = if (imageList.size > 3) imageList[3].imgUrl else ""
        imageModel.fromGroupImage = if (imageList.size > 3) imageList[3].fromGroupImage else ""
        imageModel.isProfilePic = if (imageList.size > 3) imageList[3].isProfilePic else ""
        imageModel.index = if (imageList.size > 3) imageList[3].index else 0
        imageModel.isValidDisplayPic =
            if (imageList.size > 3) imageList[3].isValidDisplayPic else ""
        imageModel.errorMessage = if (imageList.size > 3) imageList[3].errorMessage else ""
        imageModel.subMessage = if (imageList.size > 3) imageList[3].subMessage else ""
        imageModel.handWrittenTexts =
            if (imageList.size > 3) imageList[3].handWrittenTexts else ArrayList()
        multiplePhotosAdapter.objList.add(imageModel)

        multiplePhotosAdapter.notifyDataSetChanged()
        setButtonView()
    }

    fun setButtonView() {
        binding.buttonView.btnContinue.isEnabled =
            !multiplePhotosAdapter.objList.any { it.mainImage.isEmpty() }
        binding.buttonView.btnContinue.isClickable = binding.buttonView.btnContinue.isEnabled
    }


    private fun openGalleryForImages() {

        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE)

    }

    /* fun getRealPathFromURI(uri: Uri): String {
         val cursor = contentResolver.query(uri, null, null, null, null)
         cursor?.use {
             it.moveToFirst()
             val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
             return if(columnIndex !=null) it.getString(columnIndex)else ""
         }
         return ""
     }
     */


    @SuppressLint("NotifyDataSetChanged")
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE) {
                if (data != null) {
                    val selectedImageUri: Uri? = data?.data
                    if (selectedImageUri != null) {
                        SELECTED_IMAGE = ImagePicker.getRealPathFromURI(
                            this@PhotoAlbumOldActivity,
                            selectedImageUri
                        )
                        if (!File(SELECTED_IMAGE).exists()) {
                            SELECTED_IMAGE = ""
                            Util.showToastMessage(
                                this@PhotoAlbumOldActivity,
                                "Could not find the filepath of the selected file",
                                true
                            )
                        } else {

                            if (checkResolution()) {

                                multiplePhotosAdapter.objList[lastPos].isSelected = 1
                                val photoArray =
                                    multiplePhotosAdapter.objList.filter { it.isSelected == 0 && it.mainImage.isNotEmpty() }

                                var url = ""
                                for (i in photoArray.indices) {
                                    url += photoArray[i].mainImage
                                    if (i != photoArray.lastIndex)
                                        url += "#####"
                                }

                                multiplePhotosAdapter.objList[lastPos].isSelected = 0

                                isProfilePic = if (lastPos == 0) "1" else "0"

                                loading(url)

                                // close
//                                startActivityForResult(
//                                    Intent(this, LoadingActivity::class.java).putExtra(
//                                        ID,
//                                        if (lastPos == 0) "1" else "0"
//                                    ).putExtra(
//                                        URLS,
//                                        url/*multiplePhotosAdapter.objList.filter { it.mainImage.isNotEmpty() }.joinToString("#####"){ it.mainImage }*/
//                                    ),
//                                    REQUEST_CODE_1
//                                )

                            } else {
                                photoModel = PhotoModel()
                                photoModel!!.message = getString(R.string.low_resolution)
                                photoModel!!.subMessage =
                                    getString(R.string.this_photo_is_too_fuzzy_please_send_us_a_sharper_higher_quality_image)
                                photoModel!!.handWrittenTexts.add(getString(R.string.low))
                                showErrorBottomDialog()
                            }
                        }

                    }
                }
            } else if (requestCode == UCrop.REQUEST_CROP) {
                val resultUri = UCrop.getOutput(data!!)
                if (resultUri != null) {

                    println("======CROP=====resultUri=============:: " + resultUri)

                    Util.showLoading(this)
                    SELECTED_IMAGE = ImagePicker.getRealPathFromURI(this, resultUri)

//                    startActivityForResult(
//                        Intent(this, LoadingActivity::class.java).putExtra(
//                            ID,
//                            if (multiplePhotosAdapter.objList[lastPos].id == -1) "1" else "0"
//                        ),
//                        REQUEST_CODE_1
//                    )


                    UploadImageViewModel.shared.uploadWithTransferUtility(
                        this,
                        SELECTED_IMAGE, false
                    )
                    {
//                        Util.dismissLoading()
                        multiplePhotosAdapter.objList[lastPos].imgUrl = it

                        println("=======CROP====selectedImagePath=============:: " + resultUri)
                        REQUEST_TEXT = 1
                        ALBUM_LAST_POS = lastPos
                        multiplePhotosAdapter.notifyDataSetChanged()
                        saveAlbum()
//                        setImage(it)
                    }


//                    startActivity(Intent(this, ErrorScreenActivity::class.java))

                } else {
                    Util.showToastMessage(this, resources.getString(R.string.reselect_image), true)
                }
            } else if (requestCode == REQUEST_CODE_1) {
                if (data != null) {
                    photoModel = data.getSerializableExtra(DATA) as PhotoModel
                    manageImageResponse(photoModel!!)
                }
                /*if(data!=null) {
                    photoModel = data.getSerializableExtra(DATA) as PhotoModel
                    println("===================photoModel?.validationFound :: " + photoModel?.validationFound +"::"+lastPos)
                    when (photoModel?.validationFound) { // validation_found : 1 - error (open bottomSheet), 2 - Open Group photo activity, 3 - SUCCESS
                        "3" -> {
//                            multiplePhotosAdapter.objList[lastPos].mainImage =  photoModel!!.photo
                            if (lastPos == 0) {

                                if (!multiFaces) {
                                    multiplePhotosAdapter.objList[lastPos].mainImage = photoModel!!.photo
                                    multiplePhotosAdapter.objList[lastPos].imgUrl = photoModel!!.photo
                                } else {
                                    multiplePhotosAdapter.objList[lastPos].mainImage = mainImage
                                    multiplePhotosAdapter.objList[lastPos].imgUrl = mainImage
                                    multiplePhotosAdapter.objList[lastPos].fromGroupImage = photoModel!!.photo
                                }

                                multiplePhotosAdapter.objList[lastPos].isValidDisplayPic = photoModel!!.isValidPic
                                multiplePhotosAdapter.objList[lastPos].errorMessage = photoModel!!.message
                                multiplePhotosAdapter.objList[lastPos].handWrittenTexts = photoModel!!.handWrittenTexts

//                                if (multiFaces)
//                                    multiplePhotosAdapter.objList[lastPos].fromGroupImage = photoModel!!.photo
//                                else
//                                    multiplePhotosAdapter.objList[lastPos].imgUrl = photoModel!!.photo

                                setButtonView()
                                multiFaces = false

                                multiplePhotosAdapter.notifyDataSetChanged()
                            } else {
                                if (!multiFaces) {
                                    multiplePhotosAdapter.objList[lastPos].mainImage = photoModel!!.photo
                                } else {
                                    multiplePhotosAdapter.objList[lastPos].mainImage = mainImage
                                    multiplePhotosAdapter.objList[lastPos].fromGroupImage = photoModel!!.photo
                                }

                                multiplePhotosAdapter.objList[lastPos].isValidDisplayPic = photoModel!!.isValidPic
                                multiplePhotosAdapter.objList[lastPos].errorMessage = photoModel!!.message
                                multiplePhotosAdapter.objList[lastPos].handWrittenTexts = photoModel!!.handWrittenTexts

                                ImagePicker.callUCCrop(this, Uri.parse(
                                    if (multiFaces) multiplePhotosAdapter.objList[lastPos].mainImage else photoModel!!.photo))
//                                if (multiFaces)
//                                    multiplePhotosAdapter.objList[lastPos].fromGroupImage = photoModel!!.photo
                                multiFaces = false
                                // CROP IMAGE
                            }
                        }
                        "1" -> {
                            multiFaces = false
                            showErrorBottomDialog()
                        }
                        "2" -> {
                            println("=================lastpos:: " + lastPos)
//                            if (photoModel!!.cropPhotosDetails.isNotEmpty() && lastPos ==0) {
                            if (photoModel!!.cropPhotosDetails.isNotEmpty()) {
                                multiFaces = true
                                mainImage = photoModel!!.photo
//                                multiplePhotosAdapter.objList[lastPos].mainImage = photoModel!!.photo
                                startActivityForResult(
                                    Intent(this, MultipleFaceSelectionScreenActivity::class.java).putExtra(
                                        ID,
                                        if (lastPos==0) "1" else "0"
                                    ).putExtra(IS_FROM, AlbumIsFrom.GROUP).putExtra(DATA, photoModel!!).putExtra(URLS,multiplePhotosAdapter.objList.filter { it.mainImage.isNotEmpty() }.joinToString("#####"){ it.mainImage }),
                                    REQUEST_CODE_1
                                )

                            }
                            else {
                                multiplePhotosAdapter.objList[lastPos].mainImage = photoModel!!.photo
                                multiplePhotosAdapter.objList[lastPos].imgUrl =  photoModel!!.photo
                                multiplePhotosAdapter.notifyDataSetChanged()

                            }
                        }

                    }
                }*/
            } else if (requestCode == REQUEST_CODE_2) {
//                startActivityForResult(
//                    Intent(this, LoadingActivity::class.java).putExtra(
//                        ID,
//                        if (multiplePhotosAdapter.objList[lastPos].id == -1) "1" else "0"
//                    ),
//                    REQUEST_CODE_1
//                )
            }
        }

    }


    private fun showErrorBottomDialog() {
        errorDialogComponent = ErrorDialogComponent(this,
            ErrorDialogComponent.ErrorDialogFor.SURETY,
            photoModel!!.message,
            photoModel!!.subMessage
        ).apply {
            this.show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("NotifyDataSetChanged")
    private fun openBottomDialog() {

        errorDialogComponent = ErrorDialogComponent(this,
            ErrorDialogComponent.ErrorDialogFor.PHOTO_ALBUM,
            "",
            "", this
        ).apply {
            this.show()
        }


        /*bottomRemovePhotoBinding = DilalogeRemovePhotoBinding.inflate(layoutInflater)
        removeDialog = BottomSheetDialog(this, R.style.AppBottomSheetDialogTheme)

        if (removeDialog!!.isShowing)
            removeDialog!!.dismiss()
        removeDialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        removeDialog!!.setContentView(bottomRemovePhotoBinding.root)
        removeDialog!!.show()
        removeDialog!!.setCancelable(true)

        bottomRemovePhotoBinding.btnEditPicture.setOnClickListener {
            bottomRemovePhotoBinding.btnEditPicture.setBackgroundDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.dr_bg_btn
                )
            )
            bottomRemovePhotoBinding.btnEditPicture.setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.color_yellow
                )
            )

            val uri = Uri.parse(multiplePhotosAdapter.objList[lastPos].mainImage)
            ImagePicker.callUCCrop(this, uri)

            Util.closeDialogAfterShortWait(
                bottomRemovePhotoBinding.btnEditPicture,
                removeDialog!!
            )

        }

        bottomRemovePhotoBinding.btnChangePicture.setOnClickListener {
            bottomRemovePhotoBinding.btnChangePicture.setBackgroundDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.dr_bg_btn
                )
            )
            bottomRemovePhotoBinding.btnChangePicture.setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.color_yellow
                )
            )
            Util.closeDialogAfterShortWait(
                bottomRemovePhotoBinding.btnChangePicture,
                removeDialog!!
            )

            openGallery()

        }

        bottomRemovePhotoBinding.btnMakeDisplay.setOnClickListener {
            bottomRemovePhotoBinding.btnMakeDisplay.setBackgroundDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.dr_bg_btn
                )
            )
            bottomRemovePhotoBinding.btnMakeDisplay.setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.color_yellow
                )
            )
            Util.closeDialogAfterShortWait(
                bottomRemovePhotoBinding.btnMakeDisplay,
                removeDialog!!
            )

            if (multiplePhotosAdapter.objList[lastPos].isValidDisplayPic == "0") {

                photoModel = PhotoModel()
                photoModel!!.photo = multiplePhotosAdapter.objList[lastPos].mainImage
                photoModel!!.message = multiplePhotosAdapter.objList[lastPos].errorMessage
                photoModel!!.subMessage = multiplePhotosAdapter.objList[lastPos].subMessage
                photoModel!!.handWrittenTexts =
                    multiplePhotosAdapter.objList[lastPos].handWrittenTexts
                showErrorBottomDialog()

            } else {

                val model = multiplePhotosAdapter.objList[lastPos]
                multiplePhotosAdapter.objList.removeAt(lastPos)
                multiplePhotosAdapter.objList.add(0, model)
                multiplePhotosAdapter.notifyDataSetChanged()
                saveAlbum()

            }

        }*/
    }

    private fun isValidate(): Boolean {
        var isError = true
        when {
            multiplePhotosAdapter.objList[0].imgUrl.isEmpty() -> {
                isError = false
                Util.showToastMessage(
                    this,
                    String.format(resources.getString(R.string.please_select_image)),
                    true
                )
            }
        }
        return isError
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun openGallery() {
        if (isRecycleClickable) { // To stop multiple click
            isRecycleClickable = false

//            lastPos = view.tag.toString().toInt()
            if (Util.checkPermissions(this)) {

                mItemTouchHelper?.attachToRecyclerView(if (lastPos != -1) binding.rvImageView else null)
                openGalleryForImages()
            }
        }

        Handler(Looper.getMainLooper()).postDelayed({
            isRecycleClickable = true
        }, 500)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onClick(view: View) {
        when (view.id) {
            R.id.imgProfile -> {
                lastPos = view.tag.toString().toInt()
                if (lastPos == 0 || multiplePhotosAdapter.objList[lastPos].mainImage.isEmpty())
                    openGallery()
                else
                    openBottomDialog()

//                if (isRecycleClickable) { // To stop multiple click
//                    isRecycleClickable = false
//
//                    lastPos = view.tag.toString().toInt()
//                    if (Util.checkPermissions(this)) {
//
//                        mItemTouchHelper?.attachToRecyclerView(if (lastPos != -1) binding.rvImageView else null)
//                        openGalleryForImages()
//                    }
//                }
//
//                Handler(Looper.getMainLooper()).postDelayed({
//                    isRecycleClickable = true
//                    },500)
            }

            R.id.imgFaces -> {

                val pos = view.tag.toString().toInt()
                val index =
                    errorImageAdapter.objList.indexOf(errorImageAdapter.objList.find { it.isSelected == 1 })
                errorImageAdapter.objList[pos].isSelected = 1
                if (index != -1) {
                    errorImageAdapter.objList[index].isSelected = 0
                    errorImageAdapter.notifyItemChanged(index)
                }
                errorImageAdapter.notifyItemChanged(pos)

                SELECTED_IMAGE = errorImageAdapter.objList[pos].photoUrl
                urls = if (urls.isNotEmpty()) {
                    StringBuilder().append(SELECTED_IMAGE).append("#####").append(urls)
                        .toString()
                } else
                    SELECTED_IMAGE
                Util.showLoading(this)
                callApi(3, null, urls, "1")
            }

//            R.id.btnRemove -> {
//                lastPos = view.tag.toString().toInt()
//                if (lastPos == 0)
//                    openGallery()
//                else
//                    removeBottomDialog(1)
//            }

            R.id.btnEdit -> {
                lastPos = view.tag.toString().toInt()
                if (lastPos == 0)
                    openGallery()
                else
                    openBottomDialog()
            }

            R.id.btnContinue -> {
//                if (isValidate()){
//                    saveAlbum()
//                }
                Util.showLottieDialog(this, "done_lottie.json",wrapContent = true)
                {
                    when (isFrom) {
                        ActivityIsFrom.NORMAL -> {
                            bundle = Bundle().apply {
                                putString("screen_type", Screens.PHOTO_ALBUM_SCREEN.screenType)
                                putString("screen_name", Screens.PHOTO_ALBUM_SCREEN.screenName)
                                putString(
                                    "user_id",
                                    Pref.getStringValue(Pref.PREF_USER_ID, "").toString()
                                )
                            }
                            firebaseEventLog("photo_uploaded", bundle)
                            Util.manageOnBoarding(this)
                        }

                        else -> {
                            finish()
                        }
                    }
                }
            }

            /* R.id.btnSkip -> {
                 Util.manageTemplate(this)
             }*/
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE) {

            mItemTouchHelper?.attachToRecyclerView(if (lastPos != -1) binding.rvImageView else null)
            openGalleryForImages()
        }
    }

    private fun saveAlbum() {

        val array: JSONArray = JSONArray()
        val albumList = multiplePhotosAdapter.objList.filter {
            it.mainImage.isNotEmpty() && it.mainImage.startsWith("http")
        }

        for (i in albumList.indices) {

            val json = JSONArray()
            for (text in albumList[i].handWrittenTexts) {
                json.put(text)
            }

            array.put(
                JSONObject()
                    .put("main_image", albumList[i].mainImage)
                    .put("crop_image", albumList[i].imgUrl)
                    .put("from_group_image", albumList[i].fromGroupImage)
                    .put("index", if (albumList.size > 3) if (albumList[i].index == 0) { i + 1 } else albumList[i].index else 0)
                    .put("is_profile_pic", if (i == 0) "1" else "0")
                    .put("is_valid_display_pic", albumList[i].isValidDisplayPic)
                    .put("error_message", albumList[i].errorMessage)
                    .put("sub_message", albumList[i].subMessage)
                    .put("hand_written_texts", json)
            )

        }

        callApi(2, array)

        Util.print("$array")

    }


    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder?) {
        viewHolder?.let {


            mItemTouchHelper?.startDrag(it)
        }
    }

    private fun loading(urls: String) {
        Util.showLoading(this)
        var url = urls

        if (SELECTED_IMAGE.startsWith("http"))
            callApi(3, null, SELECTED_IMAGE, "0")
        else {
            UploadImageViewModel.shared.uploadWithTransferUtility(
                this,
                SELECTED_IMAGE, false
            )
            {
                url = if (url.isNotEmpty())
                    StringBuilder().append(it).append("#####").append(url).toString()
                else
                    it
                callApi(3, null, url, "0")
            }
        }

    }

    private fun setImage(photo: String) {
        // Load the image from the URL into the ImageView
        Util.print("----Started-------------------------------------------------")

        Glide.with(this)
            .load(photo)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {

                    Util.dismissLoading()
                    Util.print("----Failed-------------------------------------------------")
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable,
                    models: Any,
                    target: Target<Drawable>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {

                    Util.dismissLoading()
                    Util.print("----Success-------------------------------------------------")
                    return false
                }

            })
            .into(binding.imageView)
    }

    private fun manageImageResponse(data: PhotoModel) {

//        Util.dismissLoading()

        if (data != null) {
            photoModel = data
            println("===================photoModel?.validationFound :: " + photoModel?.validationFound + "::" + lastPos)
            when (photoModel?.validationFound) { // validation_found : 1 - error (open bottomSheet), 2 - Open Group photo activity, 3 - SUCCESS
                "3" -> {
//                            multiplePhotosAdapter.objList[lastPos].mainImage =  photoModel!!.photo
                    if (lastPos == 0) {

                        if (!multiFaces) {
                            multiplePhotosAdapter.objList[lastPos].mainImage =
                                photoModel!!.photo
                            multiplePhotosAdapter.objList[lastPos].imgUrl = photoModel!!.photo
                            multiplePhotosAdapter.objList[lastPos].fromGroupImage = ""
                        } else {
                            multiplePhotosAdapter.objList[lastPos].mainImage = mainImage
                            multiplePhotosAdapter.objList[lastPos].imgUrl = mainImage
                            multiplePhotosAdapter.objList[lastPos].fromGroupImage =
                                photoModel!!.photo
                        }

                        multiplePhotosAdapter.objList[lastPos].isValidDisplayPic =
                            photoModel!!.isValidPic
                        multiplePhotosAdapter.objList[lastPos].errorMessage =
                            photoModel!!.message
                        multiplePhotosAdapter.objList[lastPos].subMessage =
                            photoModel!!.subMessage
                        multiplePhotosAdapter.objList[lastPos].handWrittenTexts =
                            photoModel!!.handWrittenTexts

//                                if (multiFaces)
//                                    multiplePhotosAdapter.objList[lastPos].fromGroupImage = photoModel!!.photo
//                                else
//                                    multiplePhotosAdapter.objList[lastPos].imgUrl = photoModel!!.photo

                        setButtonView()
                        multiFaces = false
                        ALBUM_LAST_POS = lastPos
                        multiplePhotosAdapter.notifyDataSetChanged()
                        saveAlbum()
//                            setImage(multiplePhotosAdapter.objList[lastPos].mainImage)
                    } else {
                        if (!multiFaces) {
                            multiplePhotosAdapter.objList[lastPos].mainImage =
                                photoModel!!.photo
                            multiplePhotosAdapter.objList[lastPos].fromGroupImage = ""
                        } else {
                            multiplePhotosAdapter.objList[lastPos].mainImage = mainImage
                            multiplePhotosAdapter.objList[lastPos].fromGroupImage =
                                photoModel!!.photo
                        }

                        multiplePhotosAdapter.objList[lastPos].isValidDisplayPic =
                            photoModel!!.isValidPic
                        multiplePhotosAdapter.objList[lastPos].errorMessage =
                            photoModel!!.message
                        multiplePhotosAdapter.objList[lastPos].subMessage =
                            photoModel!!.subMessage
                        multiplePhotosAdapter.objList[lastPos].handWrittenTexts =
                            photoModel!!.handWrittenTexts

                        ImagePicker.callUCCrop(
                            this, Uri.parse(
                                if (multiFaces) multiplePhotosAdapter.objList[lastPos].mainImage else photoModel!!.photo
                            )
                        )
//                                if (multiFaces)
//                                    multiplePhotosAdapter.objList[lastPos].fromGroupImage = photoModel!!.photo
                        multiFaces = false
                        Util.dismissLoading()
                        // CROP IMAGE
                    }
                }

                "1" -> {
                    Util.dismissLoading()
                    multiFaces = false
                    showErrorBottomDialog()
                }

                "2" -> {
                    println("=================lastpos:: " + lastPos)
//                            if (photoModel!!.cropPhotosDetails.isNotEmpty() && lastPos ==0) {
                    if (photoModel!!.cropPhotosDetails.isNotEmpty()) {
                        multiFaces = true
                        mainImage = photoModel!!.photo
//                                multiplePhotosAdapter.objList[lastPos].mainImage = photoModel!!.photo
//                            startActivityForResult(
//                                Intent(this, MultipleFaceSelectionScreenActivity::class.java).putExtra(
//                                    ID,
//                                    if (lastPos==0) "1" else "0"
//                                ).putExtra(IS_FROM, AlbumIsFrom.GROUP).putExtra(DATA, photoModel!!).putExtra(URLS,multiplePhotosAdapter.objList.filter { it.mainImage.isNotEmpty() }.joinToString("#####"){ it.mainImage }),
//                                REQUEST_CODE_1
//                            )

                        manageView(false)
                        urls =
                            multiplePhotosAdapter.objList.filter { it.mainImage.isNotEmpty() }
                                .joinToString("#####") { it.mainImage }
                        multiFaces()

                    } else {
                        data.validationFound = "3"
                        manageImageResponse(data)

//                            multiplePhotosAdapter.objList[lastPos].mainImage = photoModel!!.photo
//                            multiplePhotosAdapter.objList[lastPos].imgUrl =  photoModel!!.photo
//                            multiplePhotosAdapter.notifyDataSetChanged()

                    }
                }

            }
        }

    }

    private fun manageView(visibleView: Boolean) {
        firstView = visibleView
        if (firstView) {
            binding.rlFirstScreen.visible()
            binding.rlSecondScreen.gone()
        } else {
            binding.rlSecondScreen.visible()
            binding.rlFirstScreen.gone()
        }

        binding.customHeader.get().txtTitle.text = resources.getString(
            if (firstView) R.string.photo_album else R.string.display_picture
        )
    }

    private fun multiFaces() {

        Glide.with(this)
            .load(photoModel!!.photo)
            .fitCenter()
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {

                    Util.dismissLoading()
                    Util.print("----Failed-------------------------------------------------")
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable,
                    models: Any,
                    target: Target<Drawable>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {

                    Util.dismissLoading()
                    Util.print("----Success-------------------------------------------------")
                    return false
                }

            })
            .into(binding.imgProfile)

//        binding.txtError.text = Util.applyCustomFonts(this,
//            null,
//            resources.getString(R.string.let_s_get_you_a_profile_photo),
//            resources.getString(R.string.photo),R.color.color_brown)

        binding.rvImageList.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        errorImageAdapter = ErrorImageAdapter(this, this)
        binding.rvImageList.adapter = errorImageAdapter

        errorImageAdapter.addData(photoModel!!.cropPhotosDetails)


    }


    override fun onBackPressed() {
        if (firstView) {
            super.onBackPressed()
            when (isFrom) {
                ActivityIsFrom.NORMAL -> {
                    Util.manageBackClick(this)
//                    openA<StepsActivity>()
//                    finish()
                }

                else -> {
                    finish()
                }
            }
        } else {
            manageView(true)
        }
    }

    private fun allApiResponses() {
        userViewModel =
            ViewModelProvider(
                this,
                ViewModelFactory(ApiRepository())
            )[UserViewModel::class.java]

        userViewModel.getPhotoAlbumResponse.observe(this) {
            Util.dismissProgress()
            if (it.success == 1) {

                choosePhotos(it.data[0].photoAlbum)
                binding.shieldMessage.text = it.data[0].infoMessage
                binding.llPrivacy.visibility =
                    if (it.data[0].isShield == "1") View.VISIBLE else View.INVISIBLE

            }
        }

        userViewModel.saveUploadPhotoResponse.observe(this) {
            Util.dismissProgress()
            if (it.success == 1) {

                Pref.setStringValue(
                    Pref.PREF_USER_DISPLAY_PICTURE,
                    multiplePhotosAdapter.objList[0].fromGroupImage.ifEmpty { multiplePhotosAdapter.objList[0].mainImage }
                )

//                if (isFrom == ActivityIsFrom.CANDIDATE_ALBUM) {
//                    finish()
//                    Util.print("------finish-------------------------------------")
//                } else {
//                    Util.manageOnBoarding(this)
//                    Util.print("------manage-------------------------------------")
//                }
            }
        }
        userViewModel.photoAlbumResponse.observe(this) {
            if (it.data != null && it.data.isNotEmpty()) {

                // validation_found : 1 - error (open bottomSheet), 2 - Open Group photo activity, 3 - SUCCESS
//                    val data = Intent()
//                    data.putExtra(DATA, it.data[0])
//                    setResult(RESULT_OK, data)
//                    finish()
//                setImage(it.data[0])
                if (!firstView)
                    manageView(true)
                manageImageResponse(it.data[0])

            }
        }
    }

    private fun callApi(
        tag: Int,
        jsonArray: JSONArray? = null,
        imgPath: String = "",
        fromGroupPic: String = "0"
    ) {
        if (Util.isOnline(this)) {
            when (tag) {
                1 -> {
                    Util.showProgress(this)
                    userViewModel.getPhotoAlbumApiRequest(this)
                }

                2 -> {
                    userViewModel.saveUploadPhotoApiRequest(this, jsonArray!!)
                }

                3 -> {
                    userViewModel.uploadPhotoApiRequest(
                        this, imgPath, isProfilePic, fromGroupPic
                    )
                }
            }
        } else {
            Util.dismissProgress()
            Util.dismissLoading()
        }
    }

    override fun onItemClick(itemID: String,isFrom:ErrorDialogComponent.ErrorDialogFor?) {
        errorDialogComponent?.dismiss()
        when (itemID) {
            "0" -> {
                /*Util.closeDialogAfterShortWait(
                    bottomRemovePhotoBinding.btnMakeDisplay,
                    removeDialog!!
                )*/

                if (multiplePhotosAdapter.objList[lastPos].isValidDisplayPic == "0") {

                    photoModel = PhotoModel().apply {
                        this.photo = multiplePhotosAdapter.objList[lastPos].mainImage
                        this.message = multiplePhotosAdapter.objList[lastPos].errorMessage
                        this.subMessage = multiplePhotosAdapter.objList[lastPos].subMessage
                        this.handWrittenTexts =
                            multiplePhotosAdapter.objList[lastPos].handWrittenTexts
                    }
                    showErrorBottomDialog()

                } else {
                    val model = multiplePhotosAdapter.objList[lastPos]
                    multiplePhotosAdapter.objList.removeAt(lastPos)
                    multiplePhotosAdapter.objList.add(0, model)
                    multiplePhotosAdapter.notifyDataSetChanged()
                    saveAlbum()
                }
            }

            "1" -> {
                /*Util.closeDialogAfterShortWait(
                    bottomRemovePhotoBinding.btnChangePicture,
                    removeDialog!!
                )*/

                openGallery()
            }

            "2" -> {
                val uri = Uri.parse(multiplePhotosAdapter.objList[lastPos].mainImage)
                ImagePicker.callUCCrop(this, uri)


                /*Util.closeDialogAfterShortWait(
                    bottomRemovePhotoBinding.btnEditPicture,
                    removeDialog!!
                )*/

            }
        }
    }

}

