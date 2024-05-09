package com.commonfriend.adapter

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.annotation.SuppressLint
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
import com.commonfriend.ChannelActivity
import com.commonfriend.MainActivity
import com.commonfriend.R
import com.commonfriend.databinding.ProfileViewItemBinding
import com.commonfriend.databinding.RowMatchesItemBinding
import com.commonfriend.fragment.RemoveProfileFromListInterface
import com.commonfriend.models.ProfileModel
import com.commonfriend.utils.ActivityIsFrom
import com.commonfriend.utils.Pref
import com.commonfriend.utils.Util
import com.commonfriend.utils.gone
import com.commonfriend.utils.isNull
import com.commonfriend.utils.visible
import com.commonfriend.utils.visibleIf

class MatchesAdapter(
    var context: Context,
    var clickListener: OnClickListener,
) : RecyclerView.Adapter<MatchesAdapter.ViewHolder>() {

    var objList: ArrayList<ProfileModel> = ArrayList()
    var currentPosition = 0
    var showProgressAnimation = false
    val onPageChange = MutableLiveData(0)

    class ViewHolder(var binding: RowMatchesItemBinding) : RecyclerView.ViewHolder(binding.root) {}


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            RowMatchesItemBinding.inflate(
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
    fun setView(binding: RowMatchesItemBinding, position: Int) {


        binding.root.tag = "${R.string.app_name}$position"


        with(objList[position]) {
            with(binding) {

                val drawable: Drawable =
                    ContextCompat.getDrawable(context, R.drawable.dr_ic_verified)!!
                drawable.setBounds(
                    0, 0,
                    context.resources.getDimension(com.intuit.sdp.R.dimen._15sdp).toInt(),
                    context.resources.getDimension(com.intuit.sdp.R.dimen._15sdp).toInt()
                )

                val firstName =
                    if (name.contains(" ")) name.split(" ")[0] else name

                val spannableString = SpannableStringBuilder(firstName)
                spannableString.append("  ")
                val imageSpan = ImageSpan(drawable, ImageSpan.ALIGN_BASELINE)
                spannableString.setSpan(
                    imageSpan,
                    spannableString.length - 1,
                    spannableString.length,
                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE
                )

                txtName.text = if (isAadharVerified == "1") spannableString else firstName


                if (profilePic.isNotEmpty())
                    imgProfile.setImageURI(profilePic)
                else
                    imgProfile.setActualImageResource(
                        ContextCompat.getColor(
                            context,
                            R.color.color_light_grey
                        )
                    )

                imgBackground.visibleIf(profilePic.isNotEmpty())




                onPageChange.observe(context as MainActivity) {

                    if (position == it) {
                        if (showProgressAnimation) {
                            Util.createProgressBarAnimation(
                                opinionsProgressBarView,
                                similarAnswer.toFloat().toInt(),
                                similarAnswer.toFloat().toInt() <= 50
                            )
                            showProgressAnimation = false
                        } else {
                            opinionsProgressBarView.progress = similarAnswer.toFloat().toInt()
                        }
                    } else {
                        opinionsProgressBarView.progress = 0
                    }



                    opinionsProgressBarView.progressDrawable = ContextCompat.getDrawable(
                        context,
                        if (position != it)
                            R.drawable.dr_only_white_progress
                        else if (similarAnswer.toFloat().toInt() <= 50)
                            R.drawable.dr_white_brown_progress else R.drawable.dr_brown_white_progress
                    )

                    val rightPadding =
                        binding.llOpinionText.width - (binding.llOpinionText.width * similarAnswer.toFloat()
                            .toInt() / 100)
                    val leftPadding =
                        binding.llOpinionText.width * similarAnswer.toFloat().toInt() / 100

                    if (isProfileLocked == "1" || showOpinions == "1") {
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


                }

                binding.floatingBtn.setOnClickListener {
                    if (cid.isNotEmpty()){
                        context.startActivity(
                            ChannelActivity.newIntent(context, cid,
                                ActivityIsFrom.CHAT_SCREEN))
                    } else {
                        (context as MainActivity).changeFragment(3)
                    }
                }
            }

        }


    }

    @SuppressLint("NotifyDataSetChanged")
    fun addData(data: ArrayList<ProfileModel>) {
        objList = ArrayList()
        objList.addAll(data)
        notifyDataSetChanged()
    }


}