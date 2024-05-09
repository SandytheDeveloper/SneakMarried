package com.commonfriend

import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.Window
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.commonfriend.adapter.ThirdTemplateAdapter
import com.commonfriend.databinding.DialogCastBinding
import com.commonfriend.models.GeneralModel
import com.commonfriend.utils.Util
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog

class RelationShipDialog(
    context: Context,
    relationShipList: ArrayList<GeneralModel>,
    selectedId: String,
    private var listener: OnSelectRelationShipListener?
) : BottomSheetDialog(context, R.style.AppBottomSheetDialogTheme), OnClickListener {

    private var lastPos = -1
    private val binding: DialogCastBinding =
        DialogCastBinding.inflate(LayoutInflater.from(context))


    private var relationAdapter: ThirdTemplateAdapter = ThirdTemplateAdapter(context, this)

    init {

        if (isShowing)
            dismiss()
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)
        setCancelable(true)
        if (behavior.state != BottomSheetBehavior.STATE_EXPANDED) {
            behavior.peekHeight =
                Resources.getSystem().displayMetrics.heightPixels;
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        window?.let {
            Util.statusBarColor(
                context,
                it
            )
        }

        binding.rvLocation.visibility = View.VISIBLE

        binding.txtSuggestion.text = context.resources.getString(R.string.suggested_religion)
        binding.txtHeaderTitle.text = context.resources.getString(R.string.what_is_your_relationship)
        binding.edtSearch.requestFocus()
//        binding.rvTextSuggestion.layoutManager =
//            LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
//        binding.rvTextSuggestion.adapter = relationSuggestionDialogAdapter


        binding.rvTextSuggestion.visibility = View.GONE

        binding.rvLocation.layoutManager =
            LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        relationAdapter.addData(relationShipList,true)
        binding.rvLocation.adapter = relationAdapter

        // set Position Dialoge open
        relationAdapter.objList.find { it.isSelected == 1 }?.isSelected = 0
        relationAdapter.objList.find { it.id == selectedId }?.isSelected = 1
        relationAdapter.notifyDataSetChanged()


        binding.btnDialogContinue.isEnabled =
            relationAdapter.objList.filterIndexed { _, it -> it.isSelected == 1 }.isNotEmpty()
        binding.btnDialogContinue.isClickable = binding.btnDialogContinue.isEnabled

        binding.txtClear.setOnClickListener {
            binding.edtSearch.setText("")
        }
        binding.imgLeft.setOnClickListener { binding.edtSearch.setText("") }
//        binding.llOther.setOnClickListener(this)

        binding.txtSuggestion.visibility = View.GONE
        binding.txtChooseReligion.visibility = View.GONE
        binding.llSearch.visibility = View.GONE

        binding.btnDialogContinue.setOnClickListener(this)
        binding.btnCancel.setOnClickListener {
            dismiss()
        }



        show()
    }


    override fun onClick(view: View) {
        when (view.id) {
            R.id.rlMain -> {

                lastPos = view.tag.toString().toInt()
                relationAdapter.objList.find { it.isSelected == 1 }?.isSelected = 0
                relationAdapter.objList[lastPos].isSelected =
                    if (relationAdapter.objList[lastPos].isSelected == 0) 1 else 0
                relationAdapter.notifyDataSetChanged()
                binding.btnDialogContinue.isEnabled = true
                binding.btnDialogContinue.isClickable = true
            }
            R.id.btnDialogContinue -> {
                if(listener!=null)
                    listener!!.onSelectRelationShip(if(relationAdapter.objList.filter { it.isSelected == 1 }.isNotEmpty()) relationAdapter.objList.filter { it.isSelected == 1 }[0] else null)
                dismiss()
            }
           /* R.id.llOther -> {

            }*/
        }
    }


    interface OnSelectRelationShipListener {
        fun onSelectRelationShip(generalModel: GeneralModel?)

    }



}
