package com.baojie.jni_project.maniu.x264.live

import android.hardware.Camera
import android.view.SurfaceHolder
import androidx.appcompat.app.AppCompatActivity
import com.baojie.jni_project.maniu.x264.X264Activity

/**
 * @Description:
 * @Author baoJie
 * @Date 2022/10/29 20:53
 */
class VideoChannel(
    var livePusher: LivePusher,
    var activity: AppCompatActivity,
    var width: Int,
    var height: Int,
    var bitrate: Int,
    var fps: Int,
    var cameraId: Int
): Camera.PreviewCallback, CameraHelper.OnChangedSizeListener {

    private var cameraHelper: CameraHelper? = null
    private var isLiving = false

    init {
        cameraHelper = CameraHelper(activity, cameraId, width, height)
        cameraHelper?.setPreviewCallback(this)
        cameraHelper?.setOnChangedSizeListener(this)
    }


    fun setPreviewDisplay(surfaceHolder: SurfaceHolder){
        cameraHelper?.setPreviewDisplay(surfaceHolder)
    }

    override fun onPreviewFrame(data: ByteArray?, camera: Camera?) {
        if (isLiving){
            data ?: return
            livePusher.native_pushVideo(data)
        }
    }

    override fun onChanged(w: Int, h: Int) {
        livePusher.native_setVideoEncInfo(width, height, fps, bitrate)
    }

    fun switchCamera(){
        cameraHelper?.switchCamera()
    }

    fun startLive(){
        isLiving = true
    }

    fun stopLive(){
        isLiving = false
    }

}