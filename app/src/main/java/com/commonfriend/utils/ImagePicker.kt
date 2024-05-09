package com.commonfriend.utils

import android.Manifest
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Parcelable
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.commonfriend.R
import com.yalantis.ucrop.UCrop
import java.io.File


object ImagePicker {

    var finalFileURI: Uri? = null
    const val PICK_IMAGE_CHOOSER_REQUEST_CODE = 600
    const val PERMISSION_REQUEST_CODE = 1

    // For Image Selection
    public fun getPickImageChooserIntent(context: Context): Intent? {
        return getPickImageChooserIntent(
            context,
            context.resources.getString(R.string.choose_option),
            false,
            true
        )
    }

    public fun getPickImageGalleryIntent(context: Context): Intent? {
        return getPickImageChooserIntent(
            context,
            context.resources.getString(R.string.choose_option),
            false,
            false
        )
    }


    private fun getPickImageChooserIntent(
        context: Context,
        title: CharSequence?,
        includeDocuments: Boolean,
        includeCamera: Boolean
    ): Intent? {
        val allIntents: MutableList<Intent> = ArrayList()
        val packageManager = context.packageManager
        // collect all camera intents if Camera permission is available
        if (!isExplicitCameraPermissionRequired(context) && includeCamera) {
            allIntents.addAll(getCameraIntents(context, packageManager)!!)
        }
        var galleryIntents =  getGalleryIntents(packageManager, Intent.ACTION_GET_CONTENT, includeDocuments)
        if (galleryIntents.isEmpty()) { // if no intents found for get-content try pick intent action (Huawei P9).
            galleryIntents = getGalleryIntents(packageManager, Intent.ACTION_PICK, includeDocuments)
        }
        allIntents.addAll(galleryIntents)
        val target: Intent
        if (allIntents.isEmpty()) {
            target = Intent()
        } else {
            target = allIntents[allIntents.size - 1]
            allIntents.removeAt(allIntents.size - 1)
        }
        // Create a chooser from the main  intent
        val chooserIntent = Intent.createChooser(target, title)
        // Add all other intents
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, allIntents.toTypedArray<Parcelable>())
        return chooserIntent
    }

    /**
     * Get all Camera intents for capturing image using device camera apps.
     */
     fun getCameraIntents(
        context: Context,
        packageManager: PackageManager
    ): List<Intent>? {
        val allIntents: MutableList<Intent> = ArrayList()
        // Determine Uri of camera image to  save.
        val outputFileUri = getCaptureImageOutputUri(context)
        val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val listCam = packageManager.queryIntentActivities(captureIntent, 0)
        if(listCam.isEmpty()){
            captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri)
            allIntents.add(captureIntent)
        }
        else {
            for (res in listCam) {
                val intent = Intent(captureIntent)
                intent.component =
                    ComponentName(res.activityInfo.packageName, res.activityInfo.name)
                intent.setPackage(res.activityInfo.packageName)
                if (outputFileUri != null) {

                    intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri)
//                intent.putExtra(Intent.EXTRA_RETURN_RESULT, true)
//                intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
//                intent.putExtra("return-data", true)
                }

                allIntents.add(intent)
            }
        }
        return allIntents
    }

    fun getPickImageResultUri(
        context: Context,
        data: Intent?
    ): Uri? {
        var isCamera = true
        if (data != null && data.data != null) {
            val action = data.action
            isCamera = action != null && action == MediaStore.ACTION_IMAGE_CAPTURE
        }


        return if (isCamera || data!!.data == null) {
            if (finalFileURI == null) getCaptureImageOutputUri(context) else finalFileURI
        } else data.data
    }

    /**
     * Get URI to image received from capture  by camera.
     *
     * @param context used to access Android APIs, like content resolve, it is your activity/fragment/widget.
     */


    fun getCaptureImageOutputUri(context: Context): Uri? {
        val getImage = context.externalCacheDir
        if (getImage != null) {

            val photoFile = File.createTempFile(
                "tempImage",
                ".png",
                context.externalCacheDir
            )//File(getImage.path, "pickImageResult.png")//

            finalFileURI = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

                FileProvider.getUriForFile(context,
                     "com.example.getmarried",
                    photoFile
                )
            } else {
                Uri.fromFile(photoFile)
            }


        }

        return finalFileURI
    }

    /**
     * Check if explicetly requesting camera permission is required.<br></br>
     * It is required in Android Marshmellow and above if "CAMERA" permission is requested in the manifest.<br></br>
     * See [StackOverflow
 * question](http://stackoverflow.com/questions/32789027/android-m-camera-intent-permission-bug).
     */
    fun isExplicitCameraPermissionRequired(context: Context): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                hasPermissionInManifest(
                    context,
                    "android.permission.CAMERA"
                ) && context.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
    }

    /**
     * Check if the app requests a specific permission in the manifest.
     *
     * @param permissionName the permission to check
     * @return true - the permission in requested in manifest, false - not.
     */
    fun hasPermissionInManifest(
        context: Context,
        permissionName: String
    ): Boolean {
        val packageName = context.packageName
        try {
            val packageInfo = context.packageManager
                .getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
            val declaredPermisisons = packageInfo.requestedPermissions
            if (declaredPermisisons != null && declaredPermisisons.size > 0) {
                for (p in declaredPermisisons) {
                    if (p.equals(permissionName, ignoreCase = true)) {
                        return true
                    }
                }
            }
        } catch (e: PackageManager.NameNotFoundException) {
        }
        return false
    }

    /**
     * Get all Gallery intents for getting image from one of the apps of the device that handle images.
     */
    fun getGalleryIntents(
        packageManager: PackageManager,
        action: String,
        includeDocuments: Boolean
    ): List<Intent> {
        val intents: MutableList<Intent> = ArrayList()


        val galleryIntent =
            if (action === Intent.ACTION_GET_CONTENT) Intent(action) else Intent(
                action,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
        galleryIntent.type = "image/*"
        val listGallery =
            packageManager.queryIntentActivities(galleryIntent, 0)

        if(listGallery.isEmpty())
        {
            val galIntent = Intent(Intent.ACTION_OPEN_DOCUMENT).applyImageTypes()
            galIntent.addCategory(Intent.CATEGORY_OPENABLE)
            if (galIntent.resolveActivity(packageManager) != null) {
                intents.add(galIntent)
            }else
            {
                intents.add(Intent(Intent.ACTION_PICK).applyImageTypes())
            }
        }else {
            for (res in listGallery) {
                val intent = Intent(galleryIntent)
                intent.component =
                    ComponentName(res.activityInfo.packageName, res.activityInfo.name)
                intent.setPackage(res.activityInfo.packageName)
                intents.add(intent)
            }
            // remove documents intent
            if (!includeDocuments) {
                for (intent in intents) {
                    if (intent.component!!.className == "com.android.documentsui.DocumentsActivity") {
                        intents.remove(intent)
                        break
                    }
                }
            }

        }

        return intents
    }

    private fun Intent.applyImageTypes(): Intent {
        // Apply filter to show image only in intent
        type = "image/*"
        val mimeTypes: Array<String> = arrayOf("image/png","image/jpg","image/jpeg")
        if (mimeTypes.isNotEmpty()) {
            putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        }
        return this
    }


    fun getRealPathFromURI(
        context: Context,
        contentURI: Uri
    ): String {
        val result: String
        val cursor =
            context.contentResolver.query(contentURI, null, null, null, null)
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.path.toString()
        } else {
            cursor.moveToFirst()
            val idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            result = cursor.getString(idx)
            cursor.close()
        }
        return result
    }




    fun callImagePickerIntent(context: Activity) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                context.startActivityForResult(
                    getPickImageChooserIntent(
                        context
                    ), PICK_IMAGE_CHOOSER_REQUEST_CODE
                )


            } else {
                ActivityCompat.requestPermissions(
                    context,
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA
                    ),
                    PERMISSION_REQUEST_CODE
                )
            }
        } else {
            context.startActivityForResult(
                getPickImageChooserIntent(
                    context
                ), PICK_IMAGE_CHOOSER_REQUEST_CODE
            )
        }
    }

    fun callGalleryPickerIntent(context: Activity) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                context.startActivityForResult(
                    getPickImageGalleryIntent(
                        context
                    ), PICK_IMAGE_CHOOSER_REQUEST_CODE
                )


            } else {
                ActivityCompat.requestPermissions(
                    context,
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA
                    ),
                    PERMISSION_REQUEST_CODE
                )
            }
        } else {
            context.startActivityForResult(
                getPickImageGalleryIntent(
                    context
                ), PICK_IMAGE_CHOOSER_REQUEST_CODE
            )
        }
    }


    fun callUCCrop(context: Context, uri: Uri?) {
        val options = UCrop.Options()
        options.setLogoColor(
            ContextCompat.getColor(
                context,
                R.color.color_light_grey
            )
        )
        options.setStatusBarColor(
            ContextCompat.getColor(
                context,
                R.color.color_white
            )
        )
        options.setToolbarColor(
            ContextCompat.getColor(
                context,
                R.color.color_white
            )
        )
        options.setActiveControlsWidgetColor(
            ContextCompat.getColor(
                context,
                R.color.color_white
            )
        )

        options.setToolbarTitle("")
        val uCrop = UCrop.of(
            uri!!,
            Uri.fromFile(
                File(
                    context.cacheDir,
                    "tempPic" + System.currentTimeMillis() + ".png"
                )
            )
        )
//        options.setHideBottomControls(true)

        uCrop.withOptions(options)

        uCrop.withAspectRatio(1f, 1.1f)

        uCrop.start(context as AppCompatActivity)
    }

}
