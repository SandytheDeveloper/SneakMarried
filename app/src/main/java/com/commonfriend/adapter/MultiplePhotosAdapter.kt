package com.commonfriend.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.facebook.drawee.view.SimpleDraweeView
import com.commonfriend.PhotoAlbumActivity
import com.commonfriend.R
import com.commonfriend.databinding.RowPhotoAlbumBinding
import com.commonfriend.models.ImageModel
import com.commonfriend.utils.ALBUM_LAST_POS
import com.commonfriend.utils.Util
import com.commonfriend.utils.visibleIf
import java.io.File

class MultiplePhotosAdapter(var context: Context, var clickListener: View.OnClickListener) :
    RecyclerView.Adapter<MultiplePhotosAdapter.ViewHolder>() {

    var objList: ArrayList<ImageModel> = ArrayList()


    class ViewHolder(var binding: RowPhotoAlbumBinding) : RecyclerView.ViewHolder(binding.root)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            RowPhotoAlbumBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )
        )

    }

    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        with(holder.binding) {
            with(objList[position]) {

                txtStatus.visibility = if (position == 0) View.VISIBLE else View.GONE
                txtStatus.isEnabled = (position == 0 && mainImage.isNotEmpty())
                txtStatus.setTextColor(
                    ContextCompat.getColor(
                        context,
                        if (position == 0 && mainImage.isNotEmpty()) R.color.color_black else R.color.color_grey
                    )
                )

                btnEdit.visibility = if (mainImage.isNotEmpty()) View.VISIBLE else View.GONE

                btnEdit.setImageResource(if (isLock == "1" && objList[position].isChanged == "0") R.drawable.ic_photo_album_red_edit else R.drawable.dr_ic_edit_photo)
                imgProfileOverLay.visibleIf(isLock == "1" && isChanged == "0")

                val imgList = objList.filter { it.mainImage.isNotEmpty() }
                if (imgList.isEmpty()) {
                    imgProfile.setImageResource(R.drawable.ic_bg_photo_album_dark)
                    imgProfile.alpha = if (position == 0) 1f else 0.2f
                    imgProfile.isEnabled = (position == 0)
                } else {
                    imgProfile.alpha = 1f
                    imgProfile.isEnabled = true
                    if (mainImage.isNotEmpty()) {
                        if (this.mainImage.startsWith("http")) {
                            imgProfile.setImageURI(Uri.parse(this.mainImage), context)
//                            setImage(position,placeHolderImage,this.mainImage)
                        } else {
                            imgProfile.setImageURI(Uri.fromFile(File(this.mainImage)), context)
                        }

                    } else
                        imgProfile.setImageResource(R.drawable.ic_bg_photo_album_dark)
                }

                (context as PhotoAlbumActivity).get().txtDrag.visibility =
                    if (objList.filter { it.mainImage.isNotEmpty() }.size > 1) View.VISIBLE else View.INVISIBLE

                imgProfile.tag = position
                imgProfile.setOnClickListener(clickListener)
                btnEdit.tag = position
                btnEdit.setOnClickListener(clickListener)

            }
        }

        (context as PhotoAlbumActivity).setButtonView()

    }

    override fun getItemCount(): Int {
        return objList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addData(mObjList: ArrayList<ImageModel>) {
        objList = ArrayList()
        objList.addAll(mObjList)
        this.notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData() {
        objList[0].count = "Display Picture"
        objList[0].id = -1
        objList[1].count = "1"
        objList[1].id = 1
        objList[2].count = "2"
        objList[2].id = 2
        objList[3].count = "3"
        objList[3].id = 3
//        objList[4].count = "4"
//        objList[5].count = "5"
        notifyDataSetChanged()
    }

    private fun setImage(position: Int, view: SimpleDraweeView, image: String) {
        // Load the image from the URL into the ImageView
        Util.print("----Started-------------------------------------------------")

        Glide.with(context)
            .load(image)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {

                    view.setImageURI(image, context)
                    if (ALBUM_LAST_POS == position)
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

                    view.setImageURI(image, context)
                    if (ALBUM_LAST_POS == position)
                        Util.dismissLoading()
                    Util.print("----Success-------------------------------------------------")
                    return false
                }

            })
            .into(view)
    }

}