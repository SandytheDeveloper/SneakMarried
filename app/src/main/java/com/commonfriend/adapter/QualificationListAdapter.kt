package com.commonfriend.adapter


import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.commonfriend.R
import com.commonfriend.databinding.RawAddQualificationBinding
import com.commonfriend.models.QualificationModel
import com.commonfriend.utils.visibleIf


class QualificationListAdapter(var context: Context, var clickListener: View.OnClickListener) :
    RecyclerView.Adapter<QualificationListAdapter.ViewHolder>() {

    var objList: ArrayList<QualificationModel> = ArrayList()

    class ViewHolder(var binding: RawAddQualificationBinding) :
        RecyclerView.ViewHolder(binding.root)


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            RawAddQualificationBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )
        )

    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding)
        {
            with(objList[position]) {
                txtLayout.hint =
                    if (this.isSpecialCertificate == "0"||this.isSpecialCertificate == "")
                        this.degreeName + if (this.typeName.isNotEmpty()) ", " + this.typeName else ""
                    else context.getString(R.string.special_certification)


                if (this.isSpecialCertificate == "0" ||this.isSpecialCertificate == "")
                    txtCollegeName.setText(this.collegeName)
                else
                    txtCollegeName.setText(this.degreeName)

                imgDelete.tag = position
                imgDelete.setOnClickListener(clickListener)

                rlAddNew.visibleIf(objList.isNotEmpty() && position == objList.size - 1)

                rlAddNew.tag = position
                rlAddNew.setOnClickListener(clickListener)

                addNewLayout.tag = position
                addNewLayout.setOnClickListener(clickListener)

                imgAdd.tag = position
                imgAdd.setOnClickListener(clickListener)
            }
        }

    }

    override fun getItemCount(): Int {


        /* if (objList.size < 1) {
             (context as FifteenTemplateActivity).showNoDataFoundString(true)

         } else {
             (context as FifteenTemplateActivity).showNoDataFoundString(false)

         }
 */        return objList.size
    }


    @SuppressLint("NotifyDataSetChanged")
    fun addData(mObjList: ArrayList<QualificationModel>) {
        objList = ArrayList()
        objList.addAll(mObjList)
        this.notifyDataSetChanged()

    }
}