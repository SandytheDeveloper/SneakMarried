package com.commonfriend.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.commonfriend.databinding.RawNewProfileAlertListBinding
import com.commonfriend.models.PeopleModel

class NewProfileAlertAdapter(var context: Context,var clicklistener: OnClickListener) :
    RecyclerView.Adapter<NewProfileAlertAdapter.ViewHolder>() {

    var objList: ArrayList<PeopleModel> = ArrayList()

    class ViewHolder(var binding: RawNewProfileAlertListBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            RawNewProfileAlertListBinding.inflate(
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
                txtLikeCount.text = this.likedCount
                txtName.text = this.name
               txtName.visibility  = if(this.isLocked!="1")View.VISIBLE else View.GONE
                llLikeView.visibility  = if(likedCount.isEmpty() || likedCount=="0")View.GONE else View.VISIBLE
                txtIsLocked.visibility  = if(this.isLocked=="1")View.VISIBLE else View.GONE
                txtageLocation.text =if(this.age.isNotEmpty() && this.location.isNotEmpty()) "${this.age},${this.location}" else if(this.age.isNotEmpty()) this.age else this.location
                btnNewProfileViewProfile.tag = position
                btnNewProfileViewProfile.setOnClickListener( clicklistener)

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