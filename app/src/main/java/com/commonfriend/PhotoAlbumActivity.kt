package com.commonfriend

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.provider.Settings
import android.view.View
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.commonfriend.adapter.MultiplePhotosAdapter
import com.commonfriend.base.BaseActivity
import com.commonfriend.custom.ErrorDialogComponent
import com.commonfriend.custom.OnStartDragListener
import com.commonfriend.databinding.ActivityPhotoAlbumBinding
import com.commonfriend.idrequest.ApiRepository
import com.commonfriend.models.ImageModel
import com.commonfriend.models.PhotoModel
import com.commonfriend.utils.ActivityIsFrom
import com.commonfriend.utils.IS_FROM
import com.commonfriend.utils.ImagePicker
import com.commonfriend.utils.Pref
import com.commonfriend.utils.REQUEST_CODE
import com.commonfriend.utils.SELECTED_IMAGE
import com.commonfriend.utils.Screens
import com.commonfriend.utils.Util
import com.commonfriend.utils.activityOnBackPressed
import com.commonfriend.utils.isNotNull
import com.commonfriend.utils.isNull
import com.commonfriend.utils.toFile
import com.commonfriend.utils.visibleIf
import com.commonfriend.viewmodels.UploadImageViewModel
import com.commonfriend.viewmodels.UserViewModel
import com.commonfriend.viewmodels.ViewModelFactory
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.*
import kotlin.collections.ArrayList


class PhotoAlbumActivity : BaseActivity(), View.OnClickListener, OnStartDragListener,
    ErrorDialogComponent.ErrorBottomSheetClickListener {

    private lateinit var binding: ActivityPhotoAlbumBinding
    private lateinit var multiplePhotosAdapter: MultiplePhotosAdapter
    private var mItemTouchHelper: ItemTouchHelper? = null
    private lateinit var userViewModel: UserViewModel
    private var lastPos = -1
    var photoModel: PhotoModel? = null
    private var isFrom: ActivityIsFrom = ActivityIsFrom.NORMAL

    lateinit var touchHelper: ItemTouchHelper
    private var isRecycleClickable = true
    private var isFirstScreen = false // true - FourPhoto , false - Single Photo


    private var dialog: AlertDialog? = null

    fun get(): ActivityPhotoAlbumBinding {
        return binding
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPhotoAlbumBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Util.statusBarColor(this, window)

        initializations()

    }

    private fun setScreenView() {

        binding.rlFirstScreen.visibleIf(isFirstScreen)
        binding.rlSecondScreen.visibleIf(!isFirstScreen)

        with(binding.customHeader.get()){

            val isFromOnboarding : Boolean = (isFrom == ActivityIsFrom.NORMAL || isFrom == ActivityIsFrom.FROM_EDIT_SECTION)

            txtTitle.text = resources.getString(if (isFirstScreen) R.string.photo_album else R.string.display_picture_)
            txtMainTitle.text = resources.getString(if (isFirstScreen) R.string.photo_album else R.string.display_picture_)

            binding.upperView.visibleIf(isFromOnboarding)

            txtTitle.visibleIf(isFromOnboarding)
            txtMainTitle.visibleIf(!isFromOnboarding)

            btnLeft.visibleIf(isFromOnboarding)
            imgCross.visibleIf(!isFromOnboarding)
            view.visibleIf(isFromOnboarding)

            txtPageNO.visibility = View.GONE
            progressBar.visibility = View.GONE


        }
    }

    private fun initializations() {


        if (intent.hasExtra(IS_FROM))
            isFrom = intent.getSerializableExtra(IS_FROM) as ActivityIsFrom

        isFirstScreen = Pref.getBooleanValue(Pref.PREF_ALBUM_SCREEN_TYPE,false)
        setScreenView()

        binding.buttonView.btnContinue.setOnClickListener(this)
        binding.imgDisplayPicture.setOnClickListener(this)
        binding.btnEditDisplayPicture.setOnClickListener(this)
        binding.buttonView.btnSkip.visibility = View.GONE

        binding.rvImageView.layoutManager =
            GridLayoutManager(
                this,
                2,
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
//                saveAlbum()
            }
        })

        touchHelper.attachToRecyclerView(if (isFrom == ActivityIsFrom.ASSOCIATE_ALBUM) null else binding.rvImageView)

        allApiResponses()
        callApi(1)


        activityOnBackPressed(this,this) {
            onBackPress()
        }

    }

    private fun onBackPress() {

        when (isFrom) {
            ActivityIsFrom.NORMAL, ActivityIsFrom.FROM_EDIT_SECTION -> {
                finish()
                Util.manageBackClick(this@PhotoAlbumActivity)
            }

            else -> {
                finish()
            }
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun choosePhotos(imageList: ArrayList<ImageModel>) {


        var imageModel = ImageModel()
        imageModel.count = "Display Picture"
        imageModel.mainImage = if (imageList.size > 0) imageList[0].mainImage else ""
        imageModel.sameMainImage = if (imageList.size > 0) imageList[0].mainImage else ""
        imageModel.oldImage = if (imageList.size > 0) imageList[0].oldImage else ""
        imageModel.index = if (imageList.size > 0) imageList[0].index else 0
        imageModel.imageName = if (imageList.size > 0) imageList[0].imageName else ""
        imageModel.imageSize = if (imageList.size > 0) imageList[0].imageSize else ""
        imageModel.isLock = if (imageList.size > 0) imageList[0].isLock else ""
        imageModel.isChanged = if (imageList.size > 0) imageList[0].isChanged else ""
        multiplePhotosAdapter.objList.add(imageModel)

        imageModel = ImageModel()
        imageModel.count = "1"
        imageModel.mainImage = if (imageList.size > 1) imageList[1].mainImage else ""
        imageModel.sameMainImage = if (imageList.size > 1) imageList[1].mainImage else ""
        imageModel.oldImage = if (imageList.size > 1) imageList[1].oldImage else ""
        imageModel.index = if (imageList.size > 1) imageList[1].index else 0
        imageModel.imageName = if (imageList.size > 1) imageList[1].imageName else ""
        imageModel.imageSize = if (imageList.size > 1) imageList[1].imageSize else ""
        imageModel.isLock = if (imageList.size > 1) imageList[1].isLock else ""
        imageModel.isChanged = if (imageList.size > 1) imageList[1].isChanged else ""
        multiplePhotosAdapter.objList.add(imageModel)

        imageModel = ImageModel()
        imageModel.count = "2"
        imageModel.mainImage = if (imageList.size > 2) imageList[2].mainImage else ""
        imageModel.sameMainImage = if (imageList.size > 2) imageList[2].mainImage else ""
        imageModel.oldImage = if (imageList.size > 2) imageList[2].oldImage else ""
        imageModel.index = if (imageList.size > 2) imageList[2].index else 0
        imageModel.imageName = if (imageList.size > 2) imageList[2].imageName else ""
        imageModel.imageSize = if (imageList.size > 2) imageList[2].imageSize else ""
        imageModel.isLock = if (imageList.size > 2) imageList[2].isLock else ""
        imageModel.isChanged = if (imageList.size > 2) imageList[2].isChanged else ""
        multiplePhotosAdapter.objList.add(imageModel)

        imageModel = ImageModel()
        imageModel.count = "3"
        imageModel.mainImage = if (imageList.size > 3) imageList[3].mainImage else ""
        imageModel.sameMainImage = if (imageList.size > 3) imageList[3].mainImage else ""
        imageModel.oldImage = if (imageList.size > 3) imageList[3].oldImage else ""
        imageModel.index = if (imageList.size > 3) imageList[3].index else 0
        imageModel.imageName = if (imageList.size > 3) imageList[3].imageName else ""
        imageModel.imageSize = if (imageList.size > 3) imageList[3].imageSize else ""
        imageModel.isLock = if (imageList.size > 3) imageList[3].isLock else ""
        imageModel.isChanged = if (imageList.size > 3) imageList[3].isChanged else ""
        multiplePhotosAdapter.objList.add(imageModel)

        multiplePhotosAdapter.notifyDataSetChanged()
        setButtonView()
    }

    fun setButtonView() {
        if (isFirstScreen) {

            binding.buttonView.btnContinue.isEnabled =
                !multiplePhotosAdapter.objList.any { it.mainImage.isEmpty() }
            binding.buttonView.btnContinue.isClickable = binding.buttonView.btnContinue.isEnabled

        } else {

            setDisplayImage(multiplePhotosAdapter.objList[0])

            binding.buttonView.btnContinue.isEnabled = multiplePhotosAdapter.objList[0].mainImage.isNotEmpty()
            binding.buttonView.btnContinue.isClickable = binding.buttonView.btnContinue.isEnabled

        }
    }

    private fun setDisplayImage(imageModel: ImageModel) {
        if (imageModel.mainImage.startsWith("http")) {
            binding.imgDisplayPicture.setImageURI(Uri.parse(imageModel.mainImage), this)
        } else {
            binding.imgDisplayPicture.setImageURI(Uri.fromFile(File(imageModel.mainImage)), this)
        }
        binding.btnEditDisplayPicture.setImageResource(if (imageModel.isLock == "1" && imageModel.isChanged == "0") R.drawable.ic_photo_album_red_edit else R.drawable.dr_ic_edit_photo)
        binding.imgErrorDisplayPicture.visibleIf(imageModel.isLock == "1" && imageModel.isChanged == "0")

    }

    @SuppressLint("IntentReset")
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


    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("NotifyDataSetChanged")
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE) {
                if (data != null) {
                    val selectedImageUri: Uri? = data?.data
                    if (selectedImageUri != null) {
                        SELECTED_IMAGE = try {
                            ImagePicker.getRealPathFromURI(
                                this@PhotoAlbumActivity,
                                selectedImageUri
                            )
                        } catch(e:Exception) {
                            ""
                        }

                        if (!File(SELECTED_IMAGE).exists()) {
                            SELECTED_IMAGE = ""
                            Util.showToastMessage(
                                this@PhotoAlbumActivity,
                                "Could not find the filepath of the selected file",
                                true
                            )
                        } else {

                            val (name, size) = getMediaFileMetaData(this, selectedImageUri)

                            Util.print("--------$name--$size-----------------------------------------------")

                            if (isFirstScreen && multiplePhotosAdapter.objList.find { itt -> itt.imageName == name && itt.imageSize == size }
                                    .isNotNull()) {

                                photoModel = PhotoModel()
                                photoModel!!.message = getString(R.string.similar_photo)
                                photoModel!!.subMessage =
                                    getString(R.string.this_photo_is_already_added_to_your_album_lets_try_something_different)
                                showErrorBottomDialog()
                                return

                            }

                            if (checkImageData()) {
                                try{
                                    multiplePhotosAdapter.objList[lastPos].mainImage = SELECTED_IMAGE
                                    multiplePhotosAdapter.objList[lastPos].imageName = name
                                    multiplePhotosAdapter.objList[lastPos].imageSize = size
                                    multiplePhotosAdapter.objList[lastPos].isChanged = "1"
                                    multiplePhotosAdapter.notifyDataSetChanged()

                                    if (!isFirstScreen)
                                        setButtonView()

                                } catch (e :Exception){
                                    e.printStackTrace()
                                }

                            }
//                            else {
//
//                                photoModel = PhotoModel()
//                                photoModel!!.message = getString(R.string.low_resolution)
//                                photoModel!!.subMessage =
//                                    getString(R.string.this_photo_is_too_fuzzy_please_send_us_a_sharper_higher_quality_image)
//                                photoModel!!.handWrittenTexts.add(getString(R.string.low))
//                                showErrorBottomDialog()
//
//                            }
                        }

                    }
                }
            }
        }

    }


    @RequiresApi(Build.VERSION_CODES.N)
    private fun checkImageData(): Boolean {
        val bitmap = BitmapFactory.decodeFile(SELECTED_IMAGE)

        if (bitmap.isNull()){
            return false
        }

        val resolution = Pref.getIntValue(Pref.PREF_RESOLUTION, 0)
        var width = bitmap.width
        var height = bitmap.height

        //check ratio
        var ratio1 = Pref.getStringValue(Pref.PREF_RATIO_1, "0.0").toString().toFloat()
        var ratio2 = Pref.getStringValue(Pref.PREF_RATIO_2, "0.0").toString().toFloat()

        if (ratio1 < ratio2) {
            ratio1 = Pref.getStringValue(Pref.PREF_RATIO_2, "0.0").toString().toFloat()
            ratio2 = Pref.getStringValue(Pref.PREF_RATIO_1, "0.0").toString().toFloat()
        }

        val finalRatio = if (width > height) {
            height * ratio1 / width
        } else {
            width * ratio1 / height
        }

        if (finalRatio < ratio2) {

            // show ratio error
            photoModel = PhotoModel()
            photoModel!!.message =
                if (width > height) getString(R.string.photo_too_wide) else getString(R.string.photo_too_tall)
            photoModel!!.subMessage =
                if (width > height)
                    getString(R.string.this_photo_is_too_wide_please_crop_it_and_share_a_more_square_image)
                else
                    getString(R.string.this_photo_is_too_tall_please_crop_it_and_share_a_more_square_image)
            showErrorBottomDialog()

            return false
        }


        // check resolution
        if (width >= resolution && height >= resolution) {

            val exif = ExifInterface(SELECTED_IMAGE)

            val rotation = when (exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }

            val matrix = Matrix()
            if (rotation != 0) {
                matrix.postRotate(rotation.toFloat())
            }

            val bit = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

            // Calculate minimum dimension for 1:1 aspect ratio
            val minDimension = bit.width.coerceAtMost(bit.height)

            // Determine the crop offsets to maintain square ratio
            val cropX = (bit.width - minDimension) / 2
            val cropY = (bit.height - minDimension) / 2

            // Create a new Bitmap with the cropped portion
            val croppedBitmap = Bitmap.createBitmap(bit, cropX, cropY, minDimension, minDimension)

            val newImageSize = 720
            // Resize the cropped Bitmap to the desired resolution
            val resizedBitmap = Bitmap.createScaledBitmap(croppedBitmap, newImageSize, newImageSize, false)

            // Save the resized and cropped image
            resizedBitmap.toFile().toString().let {
                SELECTED_IMAGE = it
            }

            // Recycle the unused Bitmaps
            croppedBitmap.recycle()
            bitmap.recycle()



            /*width = bit.width
            height = bit.height

            if (width > height) {
                width = (width * resolution) / height
                height = resolution
            } else {
                height = (height * resolution) / width
                width = resolution
            }

            SELECTED_IMAGE =
                Bitmap.createScaledBitmap(bit, width, height, false).toFile().toString()
            bitmap.recycle()*/

//            SELECTED_IMAGE = Bitmap.createScaledBitmap(bitmap, width, height, false).toFile().toString()

            return true

        } else {

            photoModel = PhotoModel()
            photoModel!!.message = getString(R.string.low_resolution)
            photoModel!!.subMessage =
                getString(R.string.this_photo_is_too_fuzzy_please_send_us_a_sharper_higher_quality_image)
            showErrorBottomDialog()

            return false
        }
    }

    /*fun centerCropBitmap(bitmap: Bitmap): Bitmap {
        val aspectRatio = yourImageView.measuredWidth / yourImageView.measuredHeight.toFloat()
        val bitmapAspectRatio = bitmap.width / bitmap.height.toFloat()
        val cropWidth: Int
        val cropHeight: Int
        if (aspectRatio > bitmapAspectRatio) {
            cropWidth = (bitmap.width * aspectRatio).toInt()
            cropHeight = bitmap.height
        } else {
            cropWidth = bitmap.width
            cropHeight = (bitmap.height / aspectRatio).toInt()
        }
        val left = (bitmap.width - cropWidth) / 2
        val top = (bitmap.height - cropHeight) / 2
        return Bitmap.createBitmap(bitmap, left, top, cropWidth, cropHeight)
    }*/


    private fun getMediaFileMetaData(context: Context, uri: Uri?): Pair<String, String> {

        var imageName = ""
        var imageSize = ""

        context.contentResolver
            .query(uri!!, null, null, null, null, null).use { cursor ->
                if (cursor != null && cursor.moveToFirst()) {

                    // name
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)


                    imageName = cursor.getString(nameIndex)
                    imageSize = cursor.getLong(sizeIndex).toString()

                    cursor.close()
                }
            }
        return Pair(imageName, imageSize)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun getRotatedImage(imagePath: String): Bitmap {
        val exif = ExifInterface(imagePath)

        val rotation = when (exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }

        val bitmap = BitmapFactory.decodeFile(imagePath)

        val matrix = Matrix()
        if (rotation != 0) {
            matrix.postRotate(rotation.toFloat())
        }

        val rotatedBitmap =
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        bitmap.recycle()

        return rotatedBitmap
    }

    private fun showErrorBottomDialog() {
        errorDialogComponent = ErrorDialogComponent(
            this,
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

        errorDialogComponent = ErrorDialogComponent(
            this,
            ErrorDialogComponent.ErrorDialogFor.PHOTO_ALBUM,
            "",
            "", this
        ).apply {
            this.show()
        }

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

                if (dialog != null && dialog!!.isShowing)
                    dialog!!.dismiss()
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
                if (lastPos == 0 || multiplePhotosAdapter.objList[lastPos].mainImage.isEmpty() || multiplePhotosAdapter.objList[lastPos].isLock == "1")
                    openGallery()
                else
                    openBottomDialog()
            }

            R.id.btnEdit -> {
                lastPos = view.tag.toString().toInt()
                if (lastPos == 0 || multiplePhotosAdapter.objList[lastPos].isLock == "1")
                    openGallery()
                else
                    openBottomDialog()
            }

            R.id.btnContinue -> {
                if (Util.isOnline(this)) {
                    Util.showLoading(this)
                    uploadAlbum()
                }

            }
            R.id.imgDisplayPicture , R.id.btnEditDisplayPicture -> {
                lastPos = 0
                openGallery()
            }

        }
    }

    private fun uploadAlbum(index: Int = 0) {

        // Check all photos are selected
        if (isFirstScreen && multiplePhotosAdapter.objList.find { it.mainImage.isEmpty() }.isNotNull()) {
            Util.showToastMessage(this, "Please select all Photos", true)
            return
        }

        if (index < multiplePhotosAdapter.objList.size) {

            with(multiplePhotosAdapter.objList[index]) {

                if (mainImage.startsWith("http") || mainImage.isEmpty())

                // continue
                    uploadAlbum(index + 1)
                else {
                    UploadImageViewModel.shared.uploadWithTransferUtility(
                        this@PhotoAlbumActivity,
                        mainImage, false
                    )
                    {
                        if (sameMainImage.startsWith("http"))
                            oldImage = sameMainImage

                        mainImage = it
                        // continue
                        uploadAlbum(index + 1)
                    }
                }
            }
        } else {
            saveAlbum()
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.find { itt -> itt == PackageManager.PERMISSION_GRANTED }
                    .isNotNull()/* && grantResults[0] == PackageManager.PERMISSION_GRANTED*/) {

                mItemTouchHelper?.attachToRecyclerView(if (lastPos != -1) binding.rvImageView else null)
                openGalleryForImages()
                if (dialog != null && dialog!!.isShowing)
                    dialog!!.dismiss()

            } else {
                dialog = AlertDialog.Builder(this)
                    .setTitle("Permissions Required")
                    .setMessage("This app needs the following permissions to function properly:\n\n* Photos and Storage Permission")
                    .setPositiveButton("Grant Permissions") { dialog, which ->
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri = Uri.fromParts("package", packageName, null)
                        intent.data = uri
                        startActivityForResult(intent, 5000)
                    }
                    .setNegativeButton("Cancel") { dialog, which ->
                        // Handle the case where the user denies the permissions
                    }
                    .create()

                dialog!!.show()
            }
        }
    }

    private fun saveAlbum() {

        val array = JSONArray()
        val albumList = multiplePhotosAdapter.objList

        for (i in albumList.indices) {

//            if ((!isFirstScreen) && i > 0) {
//                break
//            }

            array.put(
                JSONObject()
                    .put("main_image", albumList[i].mainImage)
                    .put(
                        "index", if (albumList.size > 3) if (albumList[i].index == 0) {
                            i + 1
                        } else albumList[i].index else 0
                    )
                    .put("image_name", albumList[i].imageName)
                    .put("old_image", albumList[i].oldImage)
                    .put("image_size", albumList[i].imageSize)
                    .put("is_lock", albumList[i].isLock)
                    .put("is_profile_pic", if (i == 0) "1" else "0")
            )

        }

        callApi(2, array)

    }


    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder?) {
        viewHolder?.let {
            mItemTouchHelper?.startDrag(it)
        }
    }


    /*private fun setImage(photo: String) {
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
    }*/


    private fun allApiResponses() {
        userViewModel =
            ViewModelProvider(
                this,
                ViewModelFactory(ApiRepository())
            )[UserViewModel::class.java]

        userViewModel.getPhotoAlbumResponse.observe(this) {
            Util.dismissProgress()
            if (it.success == 1) {

                val quality = if (it.data[0].quality.isEmpty()) 0 else it.data[0].quality.toInt()
                val ratio1 = if (it.data[0].ratio1.isEmpty()) 0f else it.data[0].ratio1.toFloat()
                val ratio2 = if (it.data[0].ratio2.isEmpty()) 0f else it.data[0].ratio2.toFloat()

                Pref.setIntValue(Pref.PREF_RESOLUTION, quality)
                Pref.setStringValue(Pref.PREF_RATIO_1, ratio1.toString())
                Pref.setStringValue(Pref.PREF_RATIO_2, ratio2.toString())

                isFirstScreen = it.data[0].albumScreenType == "1"
                Pref.setBooleanValue(Pref.PREF_ALBUM_SCREEN_TYPE,isFirstScreen)
                setScreenView()

                choosePhotos(it.data[0].photoAlbum)
                binding.shieldMessage.text = it.data[0].infoMessage
                binding.llPrivacy.visibility =
                    if (it.data[0].isShield == "1") View.VISIBLE else View.INVISIBLE


                if (Pref.getStringValue(Pref.PREF_USER_PROFILE_COMPLETED, "").toString() != "1" &&
                    isFrom != ActivityIsFrom.FROM_EDIT
                ) {

                    Handler(Looper.getMainLooper()).postDelayed(
                        {

                            photoModel = PhotoModel()
                            photoModel!!.message = getString(R.string.just_fyi)
                            photoModel!!.subMessage =
                                getString(R.string.your_photos_will_undergo_verification_by_me_only)
                            showErrorBottomDialog()
                        }, 500
                    )
                }

            }
        }

        userViewModel.saveUploadPhotoResponse.observe(this) {
            Util.dismissProgress()
            if (it.success == 1) {

                Pref.setStringValue(
                    Pref.PREF_USER_DISPLAY_PICTURE,
                    multiplePhotosAdapter.objList[0].mainImage
                )

                Util.showLottieDialog(this, "done_lottie.json", wrapContent = true)
                {
                    when (isFrom) {
                        ActivityIsFrom.NORMAL, ActivityIsFrom.FROM_EDIT_SECTION -> {
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
        }
    }

    private fun callApi(
        tag: Int,
        jsonArray: JSONArray? = null
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

            }
        } else {
            Util.dismissProgress()
            Util.dismissLoading()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onItemClick(itemID: String, isFrom: ErrorDialogComponent.ErrorDialogFor?) {
        errorDialogComponent?.dismiss()
        when (itemID) {
            "0" -> {

                val model = multiplePhotosAdapter.objList[lastPos]
                multiplePhotosAdapter.objList.removeAt(lastPos)
                multiplePhotosAdapter.objList.add(0, model)
                multiplePhotosAdapter.notifyDataSetChanged()
//
            }

            "1" -> {
                /*Util.closeDialogAfterShortWait(
                    bottomRemovePhotoBinding.btnChangePicture,
                    removeDialog!!
                )*/

                openGallery()
            }

//            "2" -> {
//                val uri = Uri.parse(multiplePhotosAdapter.objList[lastPos].mainImage)
//                ImagePicker.callUCCrop(this, uri)
//
//            }
        }
    }

}

