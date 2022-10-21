package com.baojie.jni_project.maniu.touping.push

import android.content.Intent
import android.media.projection.MediaProjectionManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.baojie.jni_project.R
import com.blankj.utilcode.constant.PermissionConstants
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.PermissionUtils

class PushActivity : AppCompatActivity() {

    private lateinit var mediaProjectionManager: MediaProjectionManager

    private var socketServerLive: SocketServerLive? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_push)
        checkWritePermission()
        mediaProjectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val captureIntent = mediaProjectionManager.createScreenCaptureIntent()
        startActivityForResult(captureIntent, 1)
        socketServerLive = SocketServerLive(13001)
    }


    private fun checkWritePermission() {
        PermissionUtils.permission(PermissionConstants.STORAGE)
            .callback(object : PermissionUtils.FullCallback{
                override fun onGranted(granted: MutableList<String>) {
                    LogUtils.d("permission onGranted")

                }

                override fun onDenied(
                    deniedForever: MutableList<String>,
                    denied: MutableList<String>
                ) {
                    LogUtils.d("permission onDenied")
                }

            })
            .request()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK || requestCode != 1) return
        data?.let {
            val mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, it)
            mediaProjection?.let { projection ->
                socketServerLive?.start(projection)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        socketServerLive?.close()
    }
}