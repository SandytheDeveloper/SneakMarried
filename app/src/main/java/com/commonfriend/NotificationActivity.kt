package com.commonfriend

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.View.OnClickListener
import androidx.lifecycle.ViewModelProvider
import com.commonfriend.base.BaseActivity
import com.commonfriend.databinding.ActivityNotificationBinding
import com.commonfriend.idrequest.ApiRepository
import com.commonfriend.models.NotificationModel
import com.commonfriend.utils.Util
import com.commonfriend.utils.gone
import com.commonfriend.utils.isNull
import com.commonfriend.utils.visible
import com.commonfriend.viewmodels.UserViewModel
import com.commonfriend.viewmodels.ViewModelFactory

class NotificationActivity : BaseActivity(), OnClickListener {

    private lateinit var binding: ActivityNotificationBinding
    private lateinit var userViewModel: UserViewModel
    private lateinit var notificationModel: NotificationModel

    private val notificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    // Function to check if notification permissions are granted
    private fun areNotificationPermissionsGranted(): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            // On Android Oreo (API 26) and above, we can use NotificationManager.areNotificationsEnabled()
            notificationManager.areNotificationsEnabled()
        } else {
            // On older Android versions, we can check the app's notification settings
            val appNotificationsEnabled = Settings.Secure.getInt(
                contentResolver,
                "show_notifications",
                1
            ) == 1
            appNotificationsEnabled
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initialization()
    }

    private fun initialization() {
        notificationModel = NotificationModel()
        with(binding.customHeader.get()) {
            progressBar.gone()
            btnLeft.gone()
            imgCross.visible()
            txtMainTitle.visible()
            txtPageNO.gone()

//            val layoutParams = txtMainTitle.layoutParams as LinearLayout.LayoutParams
//            layoutParams.marginStart =
//                resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._30sdp)
//            txtMainTitle.layoutParams = layoutParams
            txtMainTitle.text = getString(R.string.notifications)
        }


        allApiResponses()

        binding.llButtonView.btnContinue.isEnabled = true

        /*binding.btnAllNotifications.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked)
            {
                if(areNotificationPermissionsGranted())
                {
                    binding.btnAllNotifications.isChecked = true
                    binding.btnQuestions.isChecked = true
                    binding.btnConnections.isChecked = true
                    binding.btnRecommendations.isChecked = true
                    binding.btnMessages.isChecked = true
                }
                else
                {
                    binding.btnAllNotifications.isChecked = false
                    showOpenSettingsDialog(this)
                }

            }
            else
            {
                binding.btnAllNotifications.isChecked = false
                binding.btnQuestions.isChecked = false
                binding.btnConnections.isChecked = false
                binding.btnRecommendations.isChecked = false
                binding.btnMessages.isChecked = false
            }
        }*/
        /*
                binding.btnQuestions.setOnCheckedChangeListener { _, isChecked ->
                    if(isChecked)
                    {
                        if(areNotificationPermissionsGranted())
                        {
                            binding.btnQuestions.isChecked = true
                        }
                        else {
                            binding.btnQuestions.isChecked = false
                            showOpenSettingsDialog(this)
                        }
                    }
                    else
                    {
                        binding.btnAllNotifications.isChecked = false
                        binding.btnQuestions.isChecked = false
                    }
                }

                binding.btnConnections.setOnCheckedChangeListener { _, isChecked ->
                    if(isChecked)
                    {
                        if(areNotificationPermissionsGranted())
                        {
                            binding.btnConnections.isChecked = true
                        }
                        else {
                            binding.btnConnections.isChecked = false
                            showOpenSettingsDialog(this)
                        }
                    }
                    else
                    {
                        binding.btnAllNotifications.isChecked = false
                        binding.btnConnections.isChecked = false
                    }
                }

                binding.btnRecommendations.setOnCheckedChangeListener { _, isChecked ->
                    if(isChecked)
                    {
                        if(areNotificationPermissionsGranted())
                        {
                            binding.btnRecommendations.isChecked = true
                        }
                        else {
                            binding.btnRecommendations.isChecked = false
                            showOpenSettingsDialog(this)
                        }
                    }
                    else
                    {
                        binding.btnAllNotifications.isChecked = false
                        binding.btnRecommendations.isChecked = false
                    }
                }

                binding.btnMessages.setOnCheckedChangeListener { _, isChecked ->
                    if(isChecked)
                    {
                        if(areNotificationPermissionsGranted())
                        {
                            binding.btnMessages.isChecked = true
                        }
                        else {
                            binding.btnMessages.isChecked = false
                            showOpenSettingsDialog(this)
                        }
                    }
                    else
                    {
                        binding.btnAllNotifications.isChecked = false
                        binding.btnMessages.isChecked = false
                    }
                }*/

        binding.btnAllNotifications.setOnClickListener(this)
        binding.btnConnections.setOnClickListener(this)
        binding.btnRecommendations.setOnClickListener(this)
        binding.btnMessages.setOnClickListener(this)
        binding.btnQuestions.setOnClickListener(this)
        binding.llButtonView.btnContinue.setOnClickListener(this)
        binding.llButtonView.btnContinue.text = getString(R.string.save)


        if (areNotificationPermissionsGranted())
            callApi(1)
        else {
            notificationModel = NotificationModel()
            setData()
        }
    }

    private fun saveData() {
        with(notificationModel) {
            questionsNotifications = if (binding.btnQuestions.isChecked) "1" else "0"
            messageotifications = if (binding.btnMessages.isChecked) "1" else "0"
            recommendationNotifications = if (binding.btnRecommendations.isChecked) "1" else "0"
            connectionNotifications = if (binding.btnConnections.isChecked) "1" else "0"
            allNotifications =
                if (questionsNotifications == "1" && messageotifications == "1" && recommendationNotifications == "1" && connectionNotifications == "1") "1" else "0"
        }
        callApi(2)
    }

    private fun allApiResponses() {
        userViewModel = ViewModelProvider(
            this@NotificationActivity, ViewModelFactory(ApiRepository())
        )[UserViewModel::class.java]
        userViewModel.notifciationResponse.observe(this) {
            Util.dismissProgress()
            if (!it.data.isNull()) {
                notificationModel = it.data[0]
                setData()

            }
        }
        userViewModel.notifciationSetResponse.observe(this) {
            Util.dismissProgress()
            if (!it.data.isNull()) {
                finish()
            }
        }
    }

    private fun setData() {
        with(binding) {
            btnQuestions.isChecked = notificationModel.questionsNotifications == "1"
            btnConnections.isChecked = notificationModel.connectionNotifications == "1"
            btnMessages.isChecked = notificationModel.messageotifications == "1"
            btnRecommendations.isChecked =
                notificationModel.recommendationNotifications == "1"
            btnAllNotifications.isChecked = notificationModel.allNotifications == "1"
        }
    }


    @SuppressLint("CheckResult")
    private fun callApi(tag: Int) {
        if (Util.isOnline(this)) {
            when (tag) {
                1 -> {
                    Util.showProgress(this)
                    userViewModel.getNotificationsApi(this)
                }

                2 -> {
                    Util.showProgress(this)
                    userViewModel.setNotificationsApi(this, notificationModel)
                }
            }
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            /*R.id.imgClose -> {
                finish()
            }*/

            R.id.btnContinue -> {
                saveData()
            }

            R.id.btnAllNotifications -> {

                if (binding.btnAllNotifications.isChecked) {
                    if (areNotificationPermissionsGranted()) {
                        binding.btnAllNotifications.isChecked = true
                        binding.btnQuestions.isChecked = true
                        binding.btnConnections.isChecked = true
                        binding.btnRecommendations.isChecked = true
                        binding.btnMessages.isChecked = true
                    } else {
                        binding.btnAllNotifications.isChecked = false
                        showOpenSettingsDialog(this)
                    }

                } else {
                    binding.btnAllNotifications.isChecked = false
                    binding.btnQuestions.isChecked = false
                    binding.btnConnections.isChecked = false
                    binding.btnRecommendations.isChecked = false
                    binding.btnMessages.isChecked = false
                }
            }

            R.id.btnRecommendations -> {
                if (!areNotificationPermissionsGranted()) {
                    binding.btnRecommendations.isChecked = false
                    showOpenSettingsDialog(this)
                }
                checkAllNotification()
            }

            R.id.btnConnections -> {
                if (!areNotificationPermissionsGranted()) {
                    binding.btnConnections.isChecked = false
                    showOpenSettingsDialog(this)
                }
                checkAllNotification()
            }

            R.id.btnMessages -> {
                if (!areNotificationPermissionsGranted()) {
                    binding.btnMessages.isChecked = false
                    showOpenSettingsDialog(this)
                }
                checkAllNotification()
            }

            R.id.btnQuestions -> {
                if (!areNotificationPermissionsGranted()) {
                    binding.btnQuestions.isChecked = false
                    showOpenSettingsDialog(this)
                }
                checkAllNotification()
            }
        }
    }

    private fun openNotificationSettings() {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        startActivity(intent)
    }


    private fun showOpenSettingsDialog(context: Context) {
        val alertDialog = AlertDialog.Builder(context)
        alertDialog.apply {
            setTitle("Permission Required")
            setMessage("To use this feature, you need to grant location permission. Would you like to open app settings now?")
            setPositiveButton("Yes") { _, _ ->
                openNotificationSettings()
            }
            setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            setCancelable(false)
        }

        alertDialog.create().show()
    }

    private fun checkAllNotification() {
        binding.btnAllNotifications.isChecked =
            binding.btnConnections.isChecked && binding.btnQuestions.isChecked && binding.btnRecommendations.isChecked && binding.btnMessages.isChecked

    }
}

