package com.commonfriend

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.commonfriend.adapter.ErrorImageAdapter
import com.commonfriend.base.BaseActivity
import com.commonfriend.databinding.ActivityMultipleFaceSelectionScreenBinding
import com.commonfriend.idrequest.ApiRepository
import com.commonfriend.models.PhotoModel
import com.commonfriend.utils.AlbumIsFrom
import com.commonfriend.utils.DATA
import com.commonfriend.utils.ID
import com.commonfriend.utils.IS_FROM
import com.commonfriend.utils.PROFILE_IMAGE
import com.commonfriend.utils.SELECTED_IMAGE
import com.commonfriend.utils.URLS
import com.commonfriend.utils.Util
import com.commonfriend.viewmodels.UserViewModel
import com.commonfriend.viewmodels.ViewModelFactory


class MultipleFaceSelectionScreenActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding: ActivityMultipleFaceSelectionScreenBinding
    private lateinit var errorImageAdapter: ErrorImageAdapter
    private lateinit var isFrom :AlbumIsFrom
    private lateinit var userViewModel: UserViewModel

    private  var urls: String=""
    private var isProfilePic ="0" // "1"  //if profile pic then 1 otherwise 0
    private  var  photoModel:PhotoModel?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMultipleFaceSelectionScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Util.statusBarColor(this, window)
        Util.showLoading(this)
        initialization()
    }

    @SuppressLint("CheckResult")
    private fun initialization() {

        isFrom = intent.getSerializableExtra(IS_FROM) as AlbumIsFrom

        isProfilePic = if(intent.hasExtra(ID)) intent.getStringExtra(ID).toString() else "0"
        allApiResponses()
        binding.customHeader.get().txtPageNO.visibility = View.GONE
        binding.customHeader.get().txtTitle.text = resources.getString(
            if (isFrom == AlbumIsFrom.CROP) R.string.my_photo_album else R.string.display_picture)

        if(isFrom == AlbumIsFrom.GROUP) {
            urls = intent.getStringExtra(URLS).toString()
            photoModel = intent.getSerializableExtra(DATA) as PhotoModel
//            binding.imgProfile.setImageURI(photoModel!!.photo)
//            Glide.with(this)
//                .load(photoModel!!.photo)
//                .fitCenter()
//                .into(binding.imgProfile)
            setImage(photoModel!!.photo)
        }else
            binding.imgProfile.setImageURI(PROFILE_IMAGE)

//        binding.imgProfile.hierarchy.actualImageScaleType = ScalingUtils.ScaleType.FIT_CENTER
//        binding.imgProfile.adjustViewBounds = true

        binding.txtError.text = Util.applyCustomFonts(this@MultipleFaceSelectionScreenActivity,
            null,
            resources.getString(if (isFrom == AlbumIsFrom.CROP) R.string.almost_there else R.string.let_s_get_you_a_profile_photo),
            resources.getString(if (isFrom == AlbumIsFrom.CROP) R.string.there_ else R.string.photo),R.color.color_black)

        binding.rvImageList.layoutManager = LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)
        errorImageAdapter = ErrorImageAdapter(this,this)
        binding.rvImageList.adapter = errorImageAdapter

        binding.llRecycleView.visibility = if (isFrom == AlbumIsFrom.CROP) View.GONE else View.VISIBLE
        binding.btnSave.visibility = if (isFrom == AlbumIsFrom.CROP) View.VISIBLE else View.GONE

        if (isFrom != AlbumIsFrom.CROP){
            errorImageAdapter.addData(photoModel!!.cropPhotosDetails)
        }
//            setData()

        binding.btnSave.setOnClickListener(this)
    }

    private fun setImage(photo : String) {
        // Load the image from the URL into the ImageView
        Util.print("----Started-------------------------------------------------")

        Glide.with(this)
            .load(photo)
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
    }

  /*  @SuppressLint("NotifyDataSetChanged")
    fun setData() {

        var obj  = MaultipleFaceModel()
        obj.photoUrl = "https://www.shutterstock.com/image-photo/micro-peacock-feather-hd-imagebest-600w-1127238599.jpg"
        errorImageAdapter.objList.add(obj)

        obj = MaultipleFaceModel()
        obj.photoUrl = "https://www.shutterstock.com/image-illustration/latest-textile-engraving-printing-designs-600w-2018069087.jpg"
        errorImageAdapter.objList.add(obj)

        obj = MaultipleFaceModel()
        obj.photoUrl = "https://www.shutterstock.com/image-photo/cat-on-table-image-hd-600w-2065729217.jpg"
        errorImageAdapter.objList.add(obj)

        obj = MaultipleFaceModel()
        obj.photoUrl = "https://www.shutterstock.com/image-illustration/latest-textile-engraving-printing-designs-600w-2018069087.jpg"
        errorImageAdapter.objList.add(obj)

        errorImageAdapter.notifyDataSetChanged()


    }
*/
    override fun onClick(view: View) {
        when (view.id) {

            R.id.btnSave -> {
//                startActivity(Intent(this, YourProfileActivity::class.java))
                finish()
            }
            R.id.imgProfile->{
                val pos = view.tag.toString().toInt()
                val index = errorImageAdapter.objList.indexOf(errorImageAdapter.objList.find { it.isSelected == 1 })
                errorImageAdapter.objList[pos].isSelected=1
                if(index!=-1){
                    errorImageAdapter.objList[index].isSelected=0
                    errorImageAdapter.notifyItemChanged(index)
                }
                errorImageAdapter.notifyItemChanged(pos)

                SELECTED_IMAGE =errorImageAdapter.objList[pos].photoUrl
              urls=  if(urls.isNotEmpty()){
                    StringBuilder().append(SELECTED_IMAGE).append("#####").append(urls).toString()
                }else
                  SELECTED_IMAGE
                callApi()
//                setResult(Activity.RESULT_OK)
//                finish()
            }
        }
    }


    @SuppressLint("CheckResult")
    private fun callApi() {
        if (Util.isOnline(this)) {
            Util.showLoading(this)
            userViewModel.uploadPhotoApiRequest(
                this@MultipleFaceSelectionScreenActivity, urls, isProfilePic,"1"
            )
        }

    }


    private fun allApiResponses() {
        userViewModel =
            ViewModelProvider(this, ViewModelFactory(ApiRepository()))[UserViewModel::class.java]
        userViewModel.photoAlbumResponse.observe(this) {
            if (it.data !=null && it.data.isNotEmpty()) {

                // validation_found : 1 - error (open bottomSheet), 2 - Open Group photo activity, 3 - SUCCESS
                val data = Intent()
                data.putExtra(DATA, it.data[0])
                setResult(RESULT_OK, data)
                finish()

            }else {
                setResult(RESULT_CANCELED)
                finish()
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        Util.dismissLoading()
    }
}