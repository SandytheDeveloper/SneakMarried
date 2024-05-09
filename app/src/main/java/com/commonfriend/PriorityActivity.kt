package com.commonfriend

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.commonfriend.adapter.PriorityAdapter
import com.commonfriend.base.BaseActivity
import com.commonfriend.databinding.ActivityPriorityBinding
import com.commonfriend.idrequest.ApiRepository
import com.commonfriend.models.PeopleModel
import com.commonfriend.utils.*
import com.commonfriend.viewmodels.CheckListViewModel
import com.commonfriend.viewmodels.ViewModelFactory
import org.json.JSONArray
import org.json.JSONObject
import java.util.Collections
import java.util.TimeZone

class PriorityActivity : BaseActivity(), View.OnClickListener {
    private lateinit var binding: ActivityPriorityBinding
    private lateinit var priorityAdapter: PriorityAdapter
    private lateinit var mainJsonObj: JSONObject
    private lateinit var checkListViewModel: CheckListViewModel
    private var lastPos = -1
    private var changedPos = -1
    private lateinit var isFrom: ActivityIsFrom
    private lateinit var touchHelper: ItemTouchHelper

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPriorityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initializations()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun initializations() {
        isFrom =
            if (intent.hasExtra(IS_FROM)) intent.getSerializableExtra(IS_FROM) as ActivityIsFrom
            else ActivityIsFrom.NORMAL

        with(binding.customHeader.get()) {

            txtMainTitle.visibility = View.VISIBLE
            txtPageNO.visibility = View.GONE
            txtMainTitle.text = resources.getString(R.string.priority)
            binding.customHeader.setBtnLeft(true)
            binding.customHeader.setBtnImageLeft(isFrom == ActivityIsFrom.FROM_MENU)
            progressBar.visibility = View.GONE

            if (isFrom == ActivityIsFrom.FROM_MENU) {
                imgCross.visible()
                btnLeft.gone()
            } else {
                btnLeft.visible()
                imgCross.gone()
                llSkipAll.visible()
            }

        }

        if (isFrom == ActivityIsFrom.FROM_MENU) {
            binding.btnContinue.setBackgroundResource(R.drawable.dr_bg_btn)
            binding.btnContinue.setTextColor(
                ContextCompat.getColor(
                    this@PriorityActivity, R.color.color_white
                )
            )
            binding.btnContinue.setOnClickListener(this@PriorityActivity)
        }

        binding.subTitle.text =
            if (isFrom == ActivityIsFrom.FROM_MENU)
                getString(R.string.drag_or_reorder_the_attributes_below_based_on_your_priority)
            else
                getString(R.string.click_the_attributes_below_based_on_your_priority)

        binding.rvFactor.layoutManager = LinearLayoutManager(this)
        priorityAdapter = PriorityAdapter(this, this, isFrom == ActivityIsFrom.FROM_MENU)
        binding.rvFactor.adapter = priorityAdapter

        touchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
        ) {

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {

                val adapter = recyclerView.adapter as PriorityAdapter
                val from = viewHolder.adapterPosition
                val to = target.adapterPosition

                val enableSize = priorityAdapter.objList.filter { it.isClicked }.size
                if (to >= enableSize) {
                    return false
                }


                Collections.swap(adapter.objList, from, to)
                adapter.notifyItemMoved(from, to)

                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)

//                val isDragable = priorityAdapter.objList.filter { !it.isClicked }
//                if (isDragable.isNotEmpty() && actionState == 2)
//                    priorityAdapter.notifyDataSetChanged()
//                Util.print("----------onSelectedChanged------${isDragable.isNotEmpty()}--------------------------------------------")

                if (viewHolder != null) {
                    val enableSize = priorityAdapter.objList.filter { it.isClicked }.size
                    if (enableSize <= viewHolder.adapterPosition || enableSize == 1) {
                        priorityAdapter.notifyDataSetChanged()
                    }
                }
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun clearView(
                recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder
            ) {
                super.clearView(recyclerView, viewHolder)
                priorityAdapter.notifyDataSetChanged()

            }
        })
        touchHelper.attachToRecyclerView(binding.rvFactor)




        allApiResponses()
    }


    private fun allApiResponses() {
        checkListViewModel = ViewModelProvider(
            this, ViewModelFactory(ApiRepository())
        )[CheckListViewModel::class.java]

        checkListViewModel.priorityListDataResponse.observe(this) {
            Util.dismissProgress()
            if (!it.data.isNullOrEmpty()) {
                priorityAdapter.objList = ArrayList()
                for (i in it.data.indices) {
                    val obj = PeopleModel()
                    obj.name = it.data[i]
                    obj.id = when (it.data[i]) {
                        "age" -> "1"
                        "height" -> "2"
                        "education" -> "3"
                        "profession" -> "4"
                        "income" -> "5"
                        "looks" -> "6"
                        else -> it.data[i]
                    }
                    if (isFrom == ActivityIsFrom.FROM_MENU) {
                        obj.isClicked = true
                    }
                    priorityAdapter.objList.add(obj)
                }
                priorityAdapter.addData(priorityAdapter.objList)
            }
        }
        checkListViewModel.saveCheckListDataResponse.observe(this) {


            bundle = Bundle().apply {
                putString("cf_id", Pref.getStringValue(Pref.PREF_USER_ID, ""))
                putString("priority_detail",
                    priorityAdapter.objList.joinToString("|") { it.id.toString() })
            }

            firebaseEventLog("priority_submitted", bundle)

            Util.showLottieDialog(this, "done_lottie.json", wrapContent = true) {
                Util.dismissProgress()
                if (isFrom != ActivityIsFrom.FROM_MENU) {
                    openA<MainActivity>()
                    finishAffinity()
                } else {
                    finish()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        callApi(1)
    }


    @SuppressLint("CheckResult")
    private fun callApi(tag: Int) {
        if (Util.isOnline(this)) {
            when (tag) {
                1 -> {
                    Util.showProgress(this)
                    checkListViewModel.getPriorityApiRequest(this)
                }

                2 -> {
                    Util.showProgress(this)
                    checkListViewModel.savePriortyListApiRequest(this, mainJsonObj.toString())
                }
            }
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    override fun onClick(view: View) {
        when (view.id) {

            R.id.imgUpBtn -> {
                lastPos = view.tag.toString().toInt()
                Util.swap(priorityAdapter.objList, lastPos, lastPos - 1)
                priorityAdapter.notifyItemMoved(lastPos, lastPos - 1)
                priorityAdapter.notifyItemChanged(lastPos)
                priorityAdapter.notifyItemChanged(lastPos - 1)
            }

            R.id.imgDownBtn -> {
                lastPos = view.tag.toString().toInt()
                Util.swap(priorityAdapter.objList, lastPos, lastPos + 1)
                priorityAdapter.notifyItemMoved(lastPos, lastPos + 1)
                priorityAdapter.notifyItemChanged(lastPos)
                priorityAdapter.notifyItemChanged(lastPos + 1)
            }

            R.id.txtPriority -> {
                lastPos = view.tag.toString().toInt()
                if (!priorityAdapter.objList[lastPos].isClicked && changedPos != lastPos) {
                    if (changedPos <= priorityAdapter.objList.size - 1) {
                        changedPos += 1
                    }
                    Util.swap(
                        priorityAdapter.objList, lastPos, changedPos
                    )
                }
                priorityAdapter.objList[changedPos].isClicked = true
                priorityAdapter.notifyDataSetChanged()
                if (priorityAdapter.objList.none { !it.isClicked }) {
                    binding.btnContinue.setBackgroundResource(R.drawable.dr_bg_btn)
                    binding.btnContinue.setTextColor(
                        ContextCompat.getColor(
                            this@PriorityActivity, R.color.color_white
                        )
                    )
                    binding.btnContinue.setOnClickListener(this@PriorityActivity)
                }
            }

            R.id.btnContinue -> {
                saveData()
            }
        }
    }

    private fun saveData() {
        mainJsonObj = JSONObject()
        mainJsonObj.put("priority", JSONArray(priorityAdapter.objList.map { it.name }))
        mainJsonObj.put("device_type", DEVICE_TYPE)
        mainJsonObj.put("device_token", Pref.getStringValue(Pref.PREF_DEVICE_TOKEN, "").toString())
        mainJsonObj.put(
            "language", Pref.getStringValue(Pref.PREF_LANGUAGE, DEFAULT_LANGUAGE).toString()
        )
        mainJsonObj.put("timezone", TimeZone.getDefault().id)
        callApi(2)
    }
}