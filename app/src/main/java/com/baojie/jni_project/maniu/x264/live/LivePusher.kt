package com.baojie.jni_project.maniu.x264.live

import android.view.SurfaceHolder
import androidx.appcompat.app.AppCompatActivity
import com.baojie.jni_project.maniu.videochat.YuvUtils
import com.blankj.utilcode.util.LogUtils

/**
 * @Description:
 * @Author baoJie
 * @Date 2022/10/29 20:44
 */
class LivePusher(
    activity: AppCompatActivity,
    width: Int,
    height: Int,
    bitrate: Int,
    fps: Int,
    cameraId: Int
) {
    private val TAG = this.javaClass.simpleName
    private lateinit var videoChannel: VideoChannel

    init {
        native_init()
        videoChannel = VideoChannel(this, activity, width, height, bitrate, fps, cameraId)
    }

    fun setPreviewDisplay(surfaceHolder: SurfaceHolder) {
        videoChannel.setPreviewDisplay(surfaceHolder)
    }

    fun startLive(url: String){
        native_start(url)
        videoChannel.startLive()
    }

    fun stopLive(){
        videoChannel.stopLive()
        native_stop()
    }

    fun switchCamera(){
        videoChannel.switchCamera()
    }

    //    jni回调java层的方法  byte[] data    char *data
    fun postData(data: ByteArray){
        LogUtils.dTag(TAG, "postData: ${data.size}")
        YuvUtils.writeBytes(data)
        YuvUtils.writeContent(data)
    }

    private external fun native_init()

    external fun native_pushVideo(data: ByteArray)

    external fun native_setVideoEncInfo(width: Int, height: Int, fps: Int, bitrate: Int)

    private external fun native_start(url: String)

    private external fun native_stop()
}