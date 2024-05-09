package com.commonfriend.template

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.View.OnClickListener
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.commonfriend.base.BaseActivity
import com.commonfriend.R
import com.commonfriend.databinding.ActivityLocationPermissionBinding
import com.commonfriend.utils.*

class LocationPermissionActivity : BaseActivity(), OnClickListener {

    private lateinit var binding: ActivityLocationPermissionBinding

    companion object {
        const val PERMISSIONS_REQUEST_READ_LOCATION = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocationPermissionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        logForCurrentScreen("permission", "location_permission")
        initialization()
    }

    private fun checkPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ), PERMISSIONS_REQUEST_READ_LOCATION
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSIONS_REQUEST_READ_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (isGpsEnabled()) {
                        startActivity(
                            Intent(
                                this@LocationPermissionActivity,
                                SixteenTemplateActivity::class.java
                            )
                        )
                        finish()
                    } else {
                        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        startActivity(intent)
                    }
                } else {
                    if (!shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                        showOpenSettingsDialog(this)
                    }
                }
            }

            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun openAppSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }


    @SuppressLint("MissingPermission", "SetTextI18n")
    private fun getLocation() {
        if (checkPermissions() && isGpsEnabled()) {
            startActivity(
                Intent(
                    this@LocationPermissionActivity,
                    SixteenTemplateActivity::class.java
                )
            )
            finish()
        } else {
            requestPermissions()
        }
    }


    private fun initialization() {

        with(binding.customHeader.get()) {
            progressBar.gone()
            btnLeft.visible()
            txtPageNO.gone()
            llMain.setBackgroundColor(
                ContextCompat.getColor(this@LocationPermissionActivity, R.color.color_base_grey)
            )
            btnLeft.setOnClickListener(this@LocationPermissionActivity)
        }
        binding.btnAdd.setOnClickListener(this)

        activityOnBackPressed(this,this) {
            onBackPress()
        }
    }

    override fun onResume() {
        super.onResume()

        if (mainObjList[CATEGORY_ID].questionList[LAST_POS].infoMessage.isEmpty())
            binding.llPrivacy.visibility = View.INVISIBLE
        else
            binding.infoMessage.text =
                mainObjList[CATEGORY_ID].questionList[LAST_POS].infoMessage.replace(
                    "\\n",
                    System.lineSeparator()
                )
    }

    private fun onBackPress() {
        finish()
        Util.manageBackClick(this@LocationPermissionActivity)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnAdd -> {
                getLocation()
            }

            R.id.btnLeft -> {
                onBackPress()
            }
        }
    }


    private fun showOpenSettingsDialog(context: Context) {
        val alertDialog = AlertDialog.Builder(context)
        alertDialog.apply {
            setTitle("Permission Required")
            setMessage("To use this feature, you need to grant location permission. Would you like to open app settings now?")
            setPositiveButton("Yes") { _, _ ->
                openAppSettings(context)
            }
            setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
                startActivity(
                    Intent(
                        this@LocationPermissionActivity,
                        SixteenTemplateActivity::class.java
                    ).putExtra("PERMISSION_REQUIRED", false)
                )
                finish()
            }
            setCancelable(false)
        }

        alertDialog.create().show()
    }


    private fun isGpsEnabled(): Boolean {
        val locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        return locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true
    }
}
