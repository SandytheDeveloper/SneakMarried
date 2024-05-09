package com.commonfriend.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.commonfriend.databinding.RawPriorityViewBinding
import com.commonfriend.models.PeopleModel
import java.util.*

class PriorityAdapter(var context: Context,var clickListener: View.OnClickListener,var fromMenu:Boolean = false) :
    RecyclerView.Adapter<PriorityAdapter.ViewHolder>() {

    var objList: ArrayList<PeopleModel> = ArrayList()

    class ViewHolder(var binding: RawPriorityViewBinding) : RecyclerView.ViewHolder(binding.root)


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            RawPriorityViewBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )
        )

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding)
        {
            with(objList[position]) {
                txtPriority.text = this.name.capitalize(Locale.ROOT)

                txtPriority.alpha =if (this.isClicked) 1.0f else 0.5f

                imgUpBtn.visibility =
                    if (position != 0 && this.isClicked) View.VISIBLE else View.GONE


                imgDownBtn.visibility =
                    if (position != objList.size - 1 && objList[position+1].isClicked && this.isClicked) View.VISIBLE  else View.GONE


                imgDownBtn.tag = position
                imgDownBtn.setOnClickListener(context as View.OnClickListener)

                imgUpBtn.tag = position
                imgUpBtn.setOnClickListener(context as View.OnClickListener)

                txtPriority.tag = position
                txtPriority.setOnClickListener(if(!fromMenu)clickListener else null)

                llPriority.tag = position
                llPriority.setOnClickListener(context as View.OnClickListener)
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