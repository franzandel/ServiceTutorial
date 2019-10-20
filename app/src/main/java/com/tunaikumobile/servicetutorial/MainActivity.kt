package com.tunaikumobile.servicetutorial

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : Activity() {

    private val downloadReceiver = DownloadReceiver()

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        registerReceiver(downloadReceiver, IntentFilter(DownloadService.NOTIFICATION))
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(downloadReceiver)
    }

    fun onClick(view: View) {
        if (haveStoragePermission()) {
            startDownloadService()
        }
    }

    private fun haveStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.e("Permission error", "You have permission")
                true
            } else {
                Log.e("Permission error", "You have asked for permission")
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    1
                )
                false
            }
        } else { //you dont need to worry about these stuff below api level 23
            Log.e("Permission error", "You already have the permission")
            true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults[0] === PackageManager.PERMISSION_GRANTED) {
            //you have the permission now.
            startDownloadService()
        }
    }

    private fun startDownloadService() {
        btnDownload.isEnabled = false
        tvStatus.text = "Service started"

        Intent(this, DownloadService::class.java).apply {
            putExtra(DownloadService.FILENAME, "payjoy.apk")
            putExtra(DownloadService.DOWNLOAD_URL, "https://www.payjoy.com/app?build=companion")
            startService(this)
        }
    }

//    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
//        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
//        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
//            if (serviceClass.name == service.service.className) {
//                return true
//            }
//        }
//        return false
//    }
}
