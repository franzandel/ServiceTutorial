package com.tunaikumobile.servicetutorial

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*


/**
 * Created by Franz Andel on 2019-10-02.
 * Android Engineer
 */

class DownloadReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val bundle = intent?.extras

        if (bundle != null) {
            val string = bundle.getString(DownloadService.FILEPATH)
            val resultCode = bundle.getString(DownloadService.RESULT)
            val progress = bundle.getInt(DownloadService.PROGRESS)

            (context as MainActivity).pbDownload.progress = progress
            context.tvPercentage.text = progress.toString()

            when (resultCode) {
                DownloadState.DONE -> {
                    Toast.makeText(
                        context,
                        "Download complete. Download URI: " + string!!,
                        Toast.LENGTH_LONG
                    )
                        .show()
                    context.tvStatus.text = "Service finished executed"
                    context.btnDownload.isEnabled = true
                }
                DownloadState.FAILED ->
                    Toast.makeText(context, "Download failed", Toast.LENGTH_LONG).show()
                else -> {
                    Toast.makeText(context, "Download in Process", Toast.LENGTH_LONG).show()
                    context.tvStatus.text = "Service executing..."
                }
            }
        }
    }

}