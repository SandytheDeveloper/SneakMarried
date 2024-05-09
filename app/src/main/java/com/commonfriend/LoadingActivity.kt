package com.commonfriend

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.commonfriend.base.BaseActivity

import com.commonfriend.databinding.ActivityLoadingBinding
import com.commonfriend.idrequest.ApiRepository
import com.commonfriend.models.PhotoModel
import com.commonfriend.utils.DATA
import com.commonfriend.utils.ID
import com.commonfriend.utils.SELECTED_IMAGE
import com.commonfriend.utils.URLS
import com.commonfriend.utils.Util
import com.commonfriend.viewmodels.UploadImageViewModel
import com.commonfriend.viewmodels.UserViewModel
import com.commonfriend.viewmodels.ViewModelFactory
import java.lang.StringBuilder

class LoadingActivity : BaseActivity() {

    lateinit var binding: ActivityLoadingBinding

    private lateinit var userViewModel: UserViewModel
    private  var urls: String=""
    private val handler: Handler = Handler(Looper.getMainLooper())
    private val delay: Long = 1000L



    private var isProfilePic ="0" // "1"  //if profile pic then 1 otherwise 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoadingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Util.statusBarColor(this@LoadingActivity,window)
        allApiResponses()
        initialization()

    }

    private fun initialization() {
        // uploading photo
        isProfilePic = if(intent.hasExtra(ID)) intent.getStringExtra(ID).toString() else "0"
        urls = intent.getStringExtra(URLS).toString()

        handler.postDelayed(this::animateText, delay)

        println("===========DATA LLL ::"+ urls)
        if(SELECTED_IMAGE.startsWith("http"))
            callApi(SELECTED_IMAGE)
        else {
            UploadImageViewModel.shared.uploadWithTransferUtility(
                this,
                SELECTED_IMAGE, false
            )
            {
                urls = if(urls.isNotEmpty())
                    StringBuilder().append(it).append("#####").append(urls).toString()
                else
                    it
                callApi(urls)
            }
        }

    }


    @SuppressLint("CheckResult")
    private fun callApi(imgPath: String) {
        if (Util.isOnline(this)) {
            userViewModel.uploadPhotoApiRequest(
                this@LoadingActivity, imgPath, isProfilePic
            )
        }

    }


    private fun allApiResponses() {
        userViewModel =
            ViewModelProvider(this, ViewModelFactory(ApiRepository()))[UserViewModel::class.java]
        userViewModel.photoAlbumResponse.observe(this) {
            if (it.data !=null && it.data.isNotEmpty()) {

                    // validation_found : 1 - error (open bottomSheet), 2 - Open Group photo activity, 3 - SUCCESS
//                    val data = Intent()
//                    data.putExtra(DATA, it.data[0])
//                    setResult(RESULT_OK, data)
//                    finish()
                setImage(it.data[0])

            }else {
                setResult(RESULT_CANCELED)
                finish()
            }
        }

    }

    private fun animateText() {
        val text = binding.txtLoading.text.toString()
        val newText = text + "."
        binding.txtLoading.text = newText

        if (text == "Loading...") {
            binding.txtLoading.text = "Loading"
        }

        handler.postDelayed(this::animateText, delay)
    }

    fun setImage(model : PhotoModel) {
        // Load the image from the URL into the ImageView
        Util.print("----Started-------------------------------------------------")

        Glide.with(this)
            .load(model.photo)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {

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

                    val data = Intent()
                    data.putExtra(DATA, model)
                    setResult(RESULT_OK, data)
                    finish()

                    Util.print("----Success-------------------------------------------------")
                    return false
                }

            })
            .into(binding.imageView)
    }


}