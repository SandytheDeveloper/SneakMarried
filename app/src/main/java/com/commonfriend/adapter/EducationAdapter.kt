package com.commonfriend.adapter


import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.commonfriend.R
import com.commonfriend.databinding.RawEducationListBinding
import com.commonfriend.models.ProfileModel
import com.commonfriend.utils.isNotNull
import com.commonfriend.utils.visibleIf


class EducationAdapter(
    var context: Context,
    var isProfileLock : String = "0",
    var clickListener: View.OnClickListener? = null,
    var isFromEditProfile : Boolean = false
) : RecyclerView.Adapter<EducationAdapter.ViewHolder>() {


    var objList: ArrayList<ProfileModel> = ArrayList()


    class ViewHolder(var binding: RawEducationListBinding) : RecyclerView.ViewHolder(binding.root) {}


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {


        return ViewHolder(
            RawEducationListBinding.inflate(
                LayoutInflater.from(
                    parent.context),
                parent, false))
    }


    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {


        with(holder.binding){
            with(objList[position]) {


                txtPostGraduation.text = higherEducationName


                llCourse.visibility = if (courseName.isEmpty()) View.GONE else View.VISIBLE
                txtCourse.text = courseName

                tvCourse.visibility = if (collegeName.isEmpty()) View.GONE else View.VISIBLE
//                tvCourse.text = context.getString(if(collegeName.isEmpty()) R.string.certification else R.string.course)




                llCollege.visibility = if (collegeName.isEmpty()) View.GONE else View.VISIBLE
                txtCollege.text = collegeName

                txtCollege.setTextColor(ContextCompat.getColor(context,if (isLock == "1") R.color.color_red else R.color.color_black))
                imgChangeEducationDetails.setImageResource(if (isLock == "1") R.drawable.dr_ic_red_edit_locked else R.drawable.dr_ic_edit_text)


                val layout = txtPostGraduation.layoutParams as ViewGroup.MarginLayoutParams
                layout.topMargin = if (position != 0) 5 else 10


                cvCource.visibility = if (isProfileLock == "1") View.VISIBLE else View.GONE
                cvCollege.visibility = if (isProfileLock == "1") View.VISIBLE else View.GONE
                imgChangeEducationDetails.visibleIf(isFromEditProfile)

                if (clickListener.isNotNull()) {
                    imgChangeEducationDetails.tag = position
                    imgChangeEducationDetails.setOnClickListener(clickListener)
                }

            }
        }


    }


    override fun getItemCount(): Int {
        return objList.size
    }


    @SuppressLint("NotifyDataSetChanged")
    fun addData(mObjList: ArrayList<ProfileModel>) {
        objList = ArrayList()
        objList.addAll(mObjList)
        this.notifyDataSetChanged()
    }
}

