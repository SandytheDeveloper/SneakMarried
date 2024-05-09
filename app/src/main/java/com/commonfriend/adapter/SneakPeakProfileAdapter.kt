package com.commonfriend.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.commonfriend.R
import com.commonfriend.databinding.RawSneakPeakProfilesListBinding
import com.commonfriend.models.PeopleModel
import com.commonfriend.utils.COMMA_SPACE_SEPERATOR
import com.commonfriend.utils.Util
import com.commonfriend.utils.visibleIf

class SneakPeakProfileAdapter(var context: Context,var clickListener:OnClickListener) :
    RecyclerView.Adapter<SneakPeakProfileAdapter.ViewHolder>() {

    var objList: ArrayList<PeopleModel> = ArrayList()

    class ViewHolder(var binding: RawSneakPeakProfilesListBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            RawSneakPeakProfilesListBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        with(holder.binding) {
            with(objList[position]) {
//            imgProfile.setImageURI(this.profilePic)

                imgProfile.setActualImageResource(R.color.color_light_grey)

                if (profilePic.isNotEmpty()) {
                    imgProfile.setImageURI(profilePic)
                    txtUserPic.visibility = View.GONE
                } else {
                    txtUserPic.visibility = View.VISIBLE
                    txtUserPic.text = Util.getNameInitials(name)
                }
                cvName.visibleIf(this.isLocked == "1" || this.isIntroduced == "0")
                txtName.text =
                    if (this.isLocked == "1" || this.isIntroduced == "0") context.getString(R.string.loading2) else this.name
                txtHeaderTitle.visibility = if (tagLine.isNotEmpty()) View.VISIBLE else View.GONE
                txtHeaderTitle.text = tagLine
                txtageLocation.text =
                    if (this.age.isNotEmpty() && this.location.isNotEmpty()) "${this.age}$COMMA_SPACE_SEPERATOR${this.location}" else if (this.age.isNotEmpty()) this.age else this.location
                btnViewProfile.tag = position
                btnViewProfile.setOnClickListener(clickListener)

            }
        }
    }

    override fun getItemCount(): Int {
        return objList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addData(mObjList: ArrayList<PeopleModel>) {
        objList = ArrayList()
        objList.addAll(mObjList)
        this.notifyDataSetChanged()
    }
}