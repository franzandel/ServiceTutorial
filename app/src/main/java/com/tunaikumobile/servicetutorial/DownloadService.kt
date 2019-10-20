package com.tunaikumobile.servicetutorial

/**
 * Created by Franz Andel on 2019-10-02.
 * Android Engineer
 */

import android.app.DownloadManager
import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import java.io.File


class DownloadService : IntentService("DownloadService") {

    private var result = DownloadState.FAILED

    // will be called asynchronously by Android
    override fun onHandleIntent(intent: Intent?) {
        downloadPayjoy(intent)
    }

    private fun publishResults(outputPath: String, result: String, progress: Int) {
        val intent = Intent(NOTIFICATION).apply {
            putExtra(FILEPATH, outputPath)
            putExtra(RESULT, result)
            putExtra(PROGRESS, progress)
        }

        sendBroadcast(intent)
    }

    private fun downloadPayjoy(intent: Intent?) {
        val downloadThread: Thread
        val mDownloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val DOWNLOAD_DIRECTORY = Environment.getExternalStorageDirectory().toString() + "/tunaiku"
        val fileName = "payjoy.apk"
        val downloadDescription = "On Progress...."

        val downloadDirectory = File(DOWNLOAD_DIRECTORY)
        // have the object build the directory structure, if needed.
        if (!downloadDirectory.exists()) {
            downloadDirectory.mkdirs()
        } else {
            downloadDirectory.deleteRecursively()
        }

        val request =
            DownloadManager.Request(Uri.parse("https://www.payjoy.com/app?build=companion"))

        request.apply {
            setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)

            setTitle(fileName)
            setDescription(downloadDescription)

            setDestinationInExternalPublicDir("/tunaiku", fileName)

            allowScanningByMediaScanner()
            // SHOW DOWNLOAD IS IN PROGRESS IN NOTIFICATION BAR
            setVisibleInDownloadsUi(true)
            setMimeType("application/vnd.android.package-archive")
            // SHOW DOWNLOAD IS FINISHED IN NOTIFICATION BAR
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        }

        val query = DownloadManager.Query()
        val downloadId = mDownloadManager.enqueue(request)

        downloadThread = Thread(Runnable {
            var downloading = true

            while (downloading) {
                query.setFilterById(downloadId)

                val cursor = mDownloadManager.query(query)
                cursor.apply {
                    cursor.moveToFirst()

                    val bytesDownloaded = getInt(
                        getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                    )
                    val bytesTotal = getInt(
                        cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                    )

                    if (getInt(getColumnIndex(DownloadManager.COLUMN_STATUS)) === DownloadManager.STATUS_SUCCESSFUL) {
                        downloading = false
                    }

                    // INTEGER DIVISION AUTOMATICALLY ROUNDS RESULT
                    val dlProgress = ((bytesDownloaded.toDouble() / bytesTotal.toDouble()) * 100).toInt()
                    // Below Logic has the chance to return 0 result
//                    val dlProgress = ((bytesDownloaded * 100L) / bytesTotal).toInt()

                    result = if (dlProgress == 100) {
                        DownloadState.DONE
                    } else {
                        DownloadState.IN_PROGRESS
                    }

                    publishResults(DOWNLOAD_DIRECTORY, result, dlProgress)

                    close()
                }
            }
        })

        try {
            downloadThread.start()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    companion object {
        val DOWNLOAD_URL = "download_url"
        val FILENAME = "filename"
        val FILEPATH = "filepath"
        val RESULT = "result"
        val PROGRESS = "progress"
        val NOTIFICATION = "com.vogella.android.service.receiver"
    }
}